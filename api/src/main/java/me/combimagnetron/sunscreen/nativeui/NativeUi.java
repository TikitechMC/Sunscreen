package me.combimagnetron.sunscreen.nativeui;

import me.combimagnetron.sunscreen.SunscreenLibrary;
import me.combimagnetron.sunscreen.neo.render.Viewport;
import me.combimagnetron.sunscreen.user.SunscreenUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
/**
 * Native UI transport: platform implementations send/receive chunked frames; this type exposes
 * high-level {@linkplain #clientToServer client} and {@linkplain #serverToClient server} sends plus shared codecs.
 */
public interface NativeUi {
    String CHANNEL = "sunscreen:native_ui";
    byte C2S_HANDSHAKE = 1;
    byte C2S_MENU_ACTION = 2;
    byte S2C_RASTER = 1;
    byte S2C_MENU_CLOSE = 2;
    int SPIGOT_INNER_CHUNK_SAFE = 31_000;
    int FABRIC_INNER_CHUNK_SAFE = 1024 * 1024;
    int MAX_MENU_INSTANCE_ID_UTF8_BYTES = 512;
    UUID CLIENT_S2C_SLOT = new UUID(0L, 0L);
    NativeUiChunkBinary.Reassembler CLIENT_S2C_REASSEMBLER = new NativeUiChunkBinary.Reassembler();

    /** Sub-opcodes under {@link #C2S_MENU_ACTION}: {@code [sub][body...]}. */
    interface MenuInput {
        byte MOUSE = 1;
        byte SCROLL = 2;
        byte TEXT_APPEND = 3;
        byte TEXT_BACKSPACE = 4;
        int MAX_TEXT_APPEND_UTF8_BYTES = 8192;
    }

    default void clientToServer(@NotNull ByteBuffer logical) {
    }

    /**
     * Sends one fully prepared packet payload (already chunk envelope encoded) to the client.
     */
    void serverToClient(@NotNull SunscreenUser<?> user, @NotNull ByteBuffer payloadPacket);

    default void clientHandshake(boolean supportsNativeUi) {
        ByteBuffer out = ByteBuffer.allocate(2);
        out.put(C2S_HANDSHAKE);
        out.put((byte) (supportsNativeUi ? 1 : 0));
        out.flip();
        clientToServer(out);
    }

    default void clientMenuMouse(int lx, int ly, boolean left, boolean right) {
        ByteBuffer out = ByteBuffer.allocate(11).order(ByteOrder.BIG_ENDIAN);
        out.put(C2S_MENU_ACTION);
        out.put(MenuInput.MOUSE);
        out.putInt(lx);
        out.putInt(ly);
        out.put((byte) ((left ? 1 : 0) | (right ? 2 : 0)));
        out.flip();
        clientToServer(out);
    }

    default void clientMenuScroll(float dy) {
        ByteBuffer out = ByteBuffer.allocate(6).order(ByteOrder.BIG_ENDIAN);
        out.put(C2S_MENU_ACTION);
        out.put(MenuInput.SCROLL);
        out.putFloat(dy);
        out.flip();
        clientToServer(out);
    }

    default void clientMenuTextAppend(@NotNull String chars) {
        byte[] utf8 = chars.getBytes(StandardCharsets.UTF_8);
        if (utf8.length > MenuInput.MAX_TEXT_APPEND_UTF8_BYTES) {
            throw new IllegalArgumentException("text exceeds " + MenuInput.MAX_TEXT_APPEND_UTF8_BYTES + " UTF-8 bytes");
        }
        ByteBuffer out = ByteBuffer.allocate(2 + 4 + utf8.length).order(ByteOrder.BIG_ENDIAN);
        out.put(C2S_MENU_ACTION);
        out.put(MenuInput.TEXT_APPEND);
        out.putInt(utf8.length);
        out.put(utf8);
        out.flip();
        clientToServer(out);
    }

    default void clientMenuTextBackspace() {
        ByteBuffer out = ByteBuffer.allocate(2);
        out.put(C2S_MENU_ACTION);
        out.put(MenuInput.TEXT_BACKSPACE);
        out.flip();
        clientToServer(out);
    }

    default void serverSendTiles(@NotNull SunscreenUser<?> user, List<ClientRasterTile> tiles) {
        if (tiles.isEmpty()) {
            return;
        }
        var session = user.session();
        if (session == null) return;
        Viewport vp = user.screenInfo().viewport();
        int logicalW = vp.currentView().x();
        int logicalH = vp.currentView().y();
        var payload = MenuRasterWireCodec.encoder().writeTiles(logicalW, logicalH, tiles);
        var arr = payload.array();
        var menuInstanceId = session.menu().identifier().string();
        byte[] id = menuInstanceId.getBytes(StandardCharsets.UTF_8);
        if (id.length > MAX_MENU_INSTANCE_ID_UTF8_BYTES) {
            throw new IllegalArgumentException("menu instance id too long: " + id.length);
        }
        ByteBuffer out = ByteBuffer.allocate(1 + 4 + id.length + arr.length).order(ByteOrder.BIG_ENDIAN);
        out.put(S2C_RASTER);
        out.putInt(id.length);
        out.put(id);
        out.put(arr);
        out.flip();
        for (NativeUiChunkBinary.ChunkSlice slice : NativeUiChunkBinary.sliceLogical(out, SPIGOT_INNER_CHUNK_SAFE)) {
            serverToClient(
                user,
                NativeUiChunkBinary.encode(slice.totalSize(), slice.chunkIndex(), slice.chunkCount(), slice.chunk())
            );
        }
    }

    default void serverCloseMenu(@NotNull SunscreenUser<?> user) {
        ByteBuffer out = ByteBuffer.allocate(1);
        out.put(S2C_MENU_CLOSE);
        out.flip();
        serverToClient(user, NativeUiChunkBinary.encode(1, 0, 1, new byte[] {S2C_MENU_CLOSE}));
    }

    /**
     * After reassembling a C2S logical buffer on the server.
     */
    static void receiveServerC2S(@NotNull SunscreenUser<?> user, @NotNull ByteBuffer logical) {
        if (!logical.hasRemaining()) {
            return;
        }
        ByteBuffer buf = logical.slice().order(ByteOrder.BIG_ENDIAN);
        byte type = buf.get();
        switch (type) {
            case C2S_HANDSHAKE -> {
                if (buf.remaining() < 1) {
                    user.setNativeUi(false);
                    return;
                }
                boolean supports = buf.get() != 0;
                if (!supports) {
                    user.setNativeUi(false);
                    return;
                }
                user.setNativeUi(true);
            }
            case C2S_MENU_ACTION -> {
                if (buf.remaining() < 1) {
                    return;
                }
                var session = SunscreenLibrary.library().sessionHandler().session(user);
                if (session != null) {
                    deliverNativeMenuClientAction(session.menu(), buf.slice());
                }
            }
            default -> SunscreenLibrary.library().logger().warn(
                "Unknown native UI C2S type {} from {}",
                c2sName(type),
                user
            );
        }
    }

    /**
     * Platform server receivers pass raw packet bytes here; API decodes chunk header and dispatches C2S logical payload.
     */
    static void receiveServerC2SPacket(@NotNull SunscreenUser<?> user, byte @NotNull [] packetBytes) {
        NativeUiChunkBinary.ChunkSlice slice = NativeUiChunkBinary.decode(ByteBuffer.wrap(packetBytes));
        if (slice.chunkCount() != 1 || slice.chunkIndex() != 0 || slice.totalSize() != slice.chunk().length) {
            SunscreenLibrary.library().logger().warn("Native UI C2S must be single-chunk.");
            return;
        }
        receiveServerC2S(user, ByteBuffer.wrap(slice.chunk()));
    }

    /**
     * Platform client receivers pass raw packet bytes here; API decodes chunk header and reassembles when needed.
     *
     * @return completed logical S2C payload or {@code null} while waiting for more chunks
     */
    static @Nullable ByteBuffer receiveClientS2CPacket(byte @NotNull [] packetBytes) {
        NativeUiChunkBinary.ChunkSlice slice = NativeUiChunkBinary.decode(ByteBuffer.wrap(packetBytes));
        if (slice.chunkCount() == 1) {
            if (slice.chunkIndex() != 0 || slice.totalSize() != slice.chunk().length) {
                SunscreenLibrary.library().logger().warn("Malformed native UI S2C single-chunk envelope.");
                return null;
            }
            return ByteBuffer.wrap(slice.chunk());
        }
        byte[] full = CLIENT_S2C_REASSEMBLER.pushMulti(
            CLIENT_S2C_SLOT,
            slice.totalSize(),
            slice.chunkIndex(),
            slice.chunkCount(),
            slice.chunk()
        );
        return full == null ? null : ByteBuffer.wrap(full);
    }

    static void resetClientS2CReassembly() {
        CLIENT_S2C_REASSEMBLER.resetAll();
    }

    /**
     * Decodes {@code logical[offset..endExclusive)} as {@code [sub-op][body]} and invokes the matching handler.
     */
    private static void deliverNativeMenuClientAction(
        @NotNull me.combimagnetron.sunscreen.neo.ActiveMenu menu,
        @NotNull ByteBuffer subFrame
    ) {
        if (!subFrame.hasRemaining()) {
            return;
        }
        ByteBuffer buf = subFrame.slice().order(ByteOrder.BIG_ENDIAN);
        byte sub = buf.get();
        switch (sub) {
            case MenuInput.MOUSE -> {
                if (buf.remaining() < 9) {
                    return;
                }
                int lx = buf.getInt();
                int ly = buf.getInt();
                byte flags = buf.get();
                boolean left = (flags & 1) != 0;
                boolean right = (flags & 2) != 0;
                menu.acceptNativeClientMouse(lx, ly, left, right);
            }
            case MenuInput.SCROLL -> {
                if (buf.remaining() < 4) {
                    return;
                }
                menu.acceptNativeClientScroll(buf.getFloat());
            }
            case MenuInput.TEXT_APPEND -> {
                if (buf.remaining() < 4) {
                    return;
                }
                int len = buf.getInt();
                if (len < 0 || len > MenuInput.MAX_TEXT_APPEND_UTF8_BYTES || buf.remaining() < len) {
                    SunscreenLibrary.library().logger().warn("Invalid native text append length: {}", len);
                    return;
                }
                byte[] utf8 = new byte[len];
                buf.get(utf8);
                String chars = new String(utf8, StandardCharsets.UTF_8);
                if (!chars.isEmpty()) {
                    menu.acceptNativeClientTextAppend(chars);
                }
            }
            case MenuInput.TEXT_BACKSPACE -> menu.acceptNativeClientTextBackspace();
            default -> SunscreenLibrary.library().logger().warn("Unknown native menu input sub-op: {}", sub);
        }
    }

    static @NotNull S2CLogical decodeClientS2C(@NotNull ByteBuffer logical) {
        if (!logical.hasRemaining()) {
            return S2CLogical.unknown();
        }
        ByteBuffer buf = logical.slice().order(ByteOrder.BIG_ENDIAN);
        return switch (buf.get()) {
            case S2C_MENU_CLOSE -> S2CLogical.close();
            case S2C_RASTER -> {
                if (buf.remaining() < 4) {
                    yield S2CLogical.unknown();
                }
                int idLen = buf.getInt();
                if (idLen < 0 || idLen > MAX_MENU_INSTANCE_ID_UTF8_BYTES || buf.remaining() < idLen) {
                    yield S2CLogical.unknown();
                }
                byte[] idBytes = new byte[idLen];
                buf.get(idBytes);
                String menuId = new String(idBytes, StandardCharsets.UTF_8);
                ByteBuffer wire = ByteBuffer.allocate(buf.remaining());
                wire.put(buf);
                wire.flip();
                yield S2CLogical.raster(menuId, wire);
            }
            default -> S2CLogical.unknown();
        };
    }

    sealed interface S2CLogical {
        record Close() implements S2CLogical {
        }

        record Raster(@NotNull String menuInstanceId, @NotNull ByteBuffer rasterWire) implements S2CLogical {
        }

        record Unknown() implements S2CLogical {
        }

        static @NotNull Close close() {
            return new Close();
        }

        static @NotNull Raster raster(@NotNull String menuInstanceId, @NotNull ByteBuffer rasterWire) {
            return new Raster(menuInstanceId, rasterWire);
        }

        static @NotNull Unknown unknown() {
            return new Unknown();
        }
    }

    private static @NotNull String c2sName(byte type) {
        return switch (type) {
            case C2S_HANDSHAKE -> "C2S_HANDSHAKE";
            case C2S_MENU_ACTION -> "C2S_MENU_ACTION";
            default -> "C2S_UNKNOWN(" + type + ")";
        };
    }
}
