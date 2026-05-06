package me.combimagnetron.sunscreen.nativeui;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * C2S one-way native UI availability (Minecraft-agnostic). Platform code wraps this in its payload type.
 */
public record NativeUiClientNativeSupport(boolean supportsNativeUi) {
    public void write(@NotNull ByteBuffer buffer) {
        buffer.put((byte) (supportsNativeUi ? 1 : 0));
    }

    public static @NotNull NativeUiClientNativeSupport read(@NotNull ByteBuffer buffer) {
        return new NativeUiClientNativeSupport(buffer.get() != 0);
    }

    public static @NotNull ByteBuffer allocateWriteBuffer() {
        return ByteBuffer.allocate(Byte.BYTES);
    }
}
