package me.combimagnetron.sunscreen.nativeui;

import me.combimagnetron.passport.util.math.Vec3f;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class MenuRasterWireCodec {
    private static final int MAX_PIXELS = 1 << 22;
    /** Raw LZ4 block codec (wire header supplies raw/compressed lengths; no LZ4 frame wrapper). */
    private static final LZ4Factory LZ4_FACTORY = LZ4Factory.fastestInstance();

    private static Encoder encoder;
    private static Decoder decoder;

    private MenuRasterWireCodec() {
    }

    public record DecodedFrame(
        int logicalWidth,
        int logicalHeight,
        @NotNull List<ClientRasterTile> tiles
    ) {
    }

    public static @NotNull Encoder encoder() {
        if (encoder == null) encoder = new Encoder();
        return encoder;
    }

    public static @NotNull Decoder decoder() {
        if (decoder == null) decoder = new Decoder();
        return decoder;
    }

    public static final class Encoder implements AutoCloseable {
        private final ByteBuffer raw = ByteBuffer.allocate(1 << 24);
        private final LZ4Compressor compressor = LZ4_FACTORY.fastCompressor();

        private boolean closed;

        private Encoder() {
        }

        public synchronized ByteBuffer writeTiles(
            int logicalWidth,
            int logicalHeight,
            @NotNull List<ClientRasterTile> tiles
        ) {
            ensureOpen();
            raw.clear();
            if (logicalWidth <= 0 || logicalHeight <= 0) {
                throw new IllegalArgumentException("Canvas dimensions must be positive");
            }
            raw.putInt(tiles.size());
            for (ClientRasterTile t : tiles) {
                writeUtf(raw, t.scale().toPlainString());
                raw.putFloat(t.gridPosition().x());
                raw.putFloat(t.gridPosition().y());
                raw.putFloat(t.gridPosition().z());
                raw.putInt(t.width());
                raw.putInt(t.height());

                int[] px = t.argbPixels();
                if (px.length > MAX_PIXELS) {
                    throw new IllegalArgumentException("Invalid pixel buffer length: " + px.length);
                }
                raw.putInt(px.length);
                var pos = raw.position();
                raw.asIntBuffer().put(px);
                raw.position(pos + (px.length * Integer.BYTES));
            }
            raw.putInt(logicalWidth);
            raw.putInt(logicalHeight);

            int rawLen = raw.position();
            int maxCompressed = compressor.maxCompressedLength(rawLen);
            byte[] compressed = new byte[maxCompressed];
            int compressedLen = compressor.compress(raw.array(), 0, rawLen, compressed, 0);

            byte[] frame = new byte[Integer.BYTES * 2 + compressedLen];
            ByteBuffer out = ByteBuffer.wrap(frame).order(ByteOrder.BIG_ENDIAN);
            out.putInt(rawLen);
            out.putInt(compressedLen);
            out.put(compressed, 0, compressedLen);
            out.flip();
            return out;
        }

        private void ensureOpen() {
            if (closed) {
                throw new IllegalStateException("Encoder already closed");
            }
        }

        @Override
        public synchronized void close() {
            closed = true;
        }
    }

    public static final class Decoder implements AutoCloseable {
        private static final LZ4FastDecompressor DECOMPRESSOR = LZ4_FACTORY.fastDecompressor();

        private boolean closed;

        private Decoder() {
        }

        private void decompress(byte @NotNull [] compressedBytes, int compressedLen, byte @NotNull [] dst) {
            int consumed = DECOMPRESSOR.decompress(compressedBytes, 0, dst, 0, dst.length);
            if (consumed != compressedLen) {
                throw new IllegalStateException(
                    "LZ4 decode length mismatch: consumed " + consumed + ", expected " + compressedLen
                );
            }
        }

        public synchronized @NotNull DecodedFrame readFrame(@NotNull ByteBuffer frameInput) {
            ensureOpen();
            ByteBuffer frame = frameInput.order(ByteOrder.BIG_ENDIAN);
            int rawLen = frame.getInt();
            if (rawLen < 0 || rawLen > MAX_PIXELS * Integer.BYTES * 4) {
                throw new IllegalArgumentException("Invalid frame raw length: " + rawLen);
            }
            int compressedLen = frame.getInt();
            if (compressedLen < 0 || compressedLen > frame.remaining()) {
                throw new IllegalArgumentException(
                    "Invalid frame compressed length: " + compressedLen + " for remaining bytes " + frame.remaining()
                );
            }

            byte[] rawBytes = new byte[rawLen];
            byte[] compressedBytes = new byte[compressedLen];
            frame.get(compressedBytes);
            decompress(compressedBytes, compressedLen, rawBytes);
            ByteBuffer raw = ByteBuffer.wrap(rawBytes).order(ByteOrder.BIG_ENDIAN);

            var size = raw.getInt();
            List<ClientRasterTile> out = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                out.add(readTileAfterOpcode(raw));
            }
            var logicalW = raw.getInt();
            var logicalH = raw.getInt();
            if (raw.hasRemaining()) {
                throw new IllegalArgumentException("Trailing bytes in decompressed frame: " + raw.remaining());
            }
            return new DecodedFrame(logicalW, logicalH, out);
        }

        private @NotNull ClientRasterTile readTileAfterOpcode(@NotNull ByteBuffer raw) {
            BigDecimal scale = new BigDecimal(readUtf(raw));
            float gx = raw.getFloat();
            float gy = raw.getFloat();
            float gz = raw.getFloat();
            int w = raw.getInt();
            int h = raw.getInt();

            int len = raw.getInt();
            if (len < 0 || len > MAX_PIXELS) {
                throw new IllegalArgumentException("Invalid pixel buffer length: " + len);
            }
            if (len > raw.remaining() / Integer.BYTES) {
                throw new IllegalArgumentException("Truncated pixel data in frame");
            }
            int[] px = new int[len];
            raw.asIntBuffer().get(px);
            raw.position(raw.position() + len * Integer.BYTES);

            return new ClientRasterTile(scale, Vec3f.of(gx, gy, gz), w, h, px);
        }

        /** @deprecated Prefer {@link #readFrame(ByteBuffer)} for canvas dimensions. */
        @Deprecated
        public synchronized @NotNull List<ClientRasterTile> readTiles(@NotNull ByteBuffer frameInput) {
            return readFrame(frameInput).tiles();
        }

        private void ensureOpen() {
            if (closed) {
                throw new IllegalStateException("Decoder already closed");
            }
        }

        @Override
        public synchronized void close() {
            closed = true;
        }
    }

    private static void writeUtf(@NotNull ByteBuffer out, @NotNull String s) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        out.putInt(bytes.length);
        out.put(bytes);
    }

    private static @NotNull String readUtf(@NotNull ByteBuffer buf) {
        int len = buf.getInt();
        byte[] bytes = new byte[len];
        buf.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

}
