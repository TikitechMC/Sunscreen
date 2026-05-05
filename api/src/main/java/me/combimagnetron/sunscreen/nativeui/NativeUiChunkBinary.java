package me.combimagnetron.sunscreen.nativeui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chunk envelope: {@code totalSize, chunkIndex, chunkCount, chunk bytes}. Only large S2C rasters use
 * {@code chunkCount > 1}; all other payloads are a single physical packet.
 */
public final class NativeUiChunkBinary {
    public record ChunkSlice(
        int totalSize,
        int chunkIndex,
        int chunkCount,
        byte @NotNull [] chunk
    ) {
    }

    private NativeUiChunkBinary() {
    }

    public static @NotNull ByteBuffer encode(
        int totalSize,
        int chunkIndex,
        int chunkCount,
        byte @NotNull [] chunk
    ) {
        ByteBuffer out = ByteBuffer.allocate(16 + chunk.length).order(ByteOrder.BIG_ENDIAN);
        out.putInt(totalSize);
        out.putInt(chunkIndex);
        out.putInt(chunkCount);
        out.putInt(chunk.length);
        out.put(chunk);
        out.flip();
        return out;
    }

    public static @NotNull ChunkSlice decode(@NotNull ByteBuffer message) {
        ByteBuffer buf = message.slice().order(ByteOrder.BIG_ENDIAN);
        if (buf.remaining() < 16) {
            throw new IllegalArgumentException("Truncated chunk envelope");
        }
        int totalSize = buf.getInt();
        int chunkIndex = buf.getInt();
        int chunkCount = buf.getInt();
        int chunkLen = buf.getInt();
        if (chunkLen < 0 || buf.remaining() < chunkLen) {
            throw new IllegalArgumentException("Invalid chunk length: " + chunkLen);
        }
        byte[] chunk = new byte[chunkLen];
        buf.get(chunk);
        return new ChunkSlice(totalSize, chunkIndex, chunkCount, chunk);
    }

    /**
     * Splits {@code logical} when it exceeds {@code maxInnerChunkBytes}; otherwise one slice with {@code chunkCount == 1}.
     */
    public static @NotNull List<ChunkSlice> sliceLogical(@NotNull ByteBuffer logical, int maxInnerChunkBytes) {
        if (maxInnerChunkBytes < 1) {
            throw new IllegalArgumentException("maxInnerChunkBytes");
        }
        ByteBuffer src = logical.slice();
        int n = src.remaining();
        if (n == 0) {
            throw new IllegalArgumentException("empty logical payload");
        }
        if (n <= maxInnerChunkBytes) {
            byte[] single = new byte[n];
            src.get(single);
            return List.of(new ChunkSlice(n, 0, 1, single));
        }
        int total = (n + maxInnerChunkBytes - 1) / maxInnerChunkBytes;
        List<ChunkSlice> out = new ArrayList<>(total);
        int basePos = src.position();
        for (int idx = 0; idx < total; idx++) {
            int from = idx * maxInnerChunkBytes;
            int len = Math.min(maxInnerChunkBytes, n - from);
            byte[] part = new byte[len];
            ByteBuffer dup = src.duplicate();
            dup.position(basePos + from);
            dup.get(part);
            out.add(new ChunkSlice(n, idx, total, part));
        }
        return out;
    }

    /**
     * Incremental reassembly state keyed by sender/receiver identity.
     */
    public static final class Reassembler {
        private final ConcurrentHashMap<UUID, State> states = new ConcurrentHashMap<>();

        /**
         * @return assembled payload when last chunk arrives; {@code null} while incomplete
         */
        public @Nullable byte[] pushMulti(
            @NotNull UUID key,
            int totalSize,
            int chunkIndex,
            int chunkCount,
            byte @NotNull [] chunk
        ) {
            if (chunkCount <= 1) {
                throw new IllegalArgumentException("chunkCount must be > 1");
            }
            if (chunkIndex < 0 || chunkIndex >= chunkCount) {
                throw new IllegalArgumentException("Bad chunk indices: " + chunkIndex + "/" + chunkCount);
            }
            State st = states.compute(key, (id, prev) -> {
                if (prev == null || prev.totalSize != totalSize || prev.chunkCount != chunkCount) {
                    if (chunkIndex != 0) {
                        throw new IllegalStateException("Missing initial chunk for multi-chunk transfer");
                    }
                    return new State(totalSize, chunkCount);
                }
                return prev;
            });
            synchronized (st) {
                if (st.totalSize != totalSize || st.chunkCount != chunkCount) {
                    states.remove(key);
                    throw new IllegalStateException("Chunk metadata changed mid-transfer");
                }
                st.buffer.put(chunk);
                if (chunkIndex + 1 < chunkCount) {
                    return null;
                }
                states.remove(key);
                st.buffer.flip();
                byte[] full = new byte[st.buffer.remaining()];
                st.buffer.get(full);
                return full;
            }
        }

        public void reset(@NotNull UUID key) {
            states.remove(key);
        }

        public void resetAll() {
            states.clear();
        }

        private static final class State {
            final int totalSize;
            final int chunkCount;
            final ByteBuffer buffer;

            State(int totalSize, int chunkCount) {
                this.totalSize = totalSize;
                this.chunkCount = chunkCount;
                this.buffer = ByteBuffer.allocate(totalSize);
            }
        }
    }
}
