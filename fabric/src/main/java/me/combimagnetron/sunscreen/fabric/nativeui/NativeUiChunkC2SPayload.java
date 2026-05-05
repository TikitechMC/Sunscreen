package me.combimagnetron.sunscreen.fabric.nativeui;

import io.netty.buffer.ByteBuf;
import me.combimagnetron.sunscreen.fabric.protocol.FabricMappingCompat;
import me.combimagnetron.sunscreen.nativeui.NativeUi;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record NativeUiChunkC2SPayload(
    byte @NotNull [] data
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<NativeUiChunkC2SPayload> TYPE =
        FabricMappingCompat.customPayloadType(NativeUi.CHANNEL);

    public static final StreamCodec<ByteBuf, NativeUiChunkC2SPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> buf.writeBytes(p.data()),
        buf -> {
            int len = buf.readableBytes();
            byte[] out = new byte[len];
            buf.readBytes(out);
            return new NativeUiChunkC2SPayload(out);
        }
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
