package me.combimagnetron.sunscreen.fabric.nativeui;

import io.netty.buffer.ByteBuf;
import me.combimagnetron.sunscreen.fabric.protocol.FabricMappingCompat;
import me.combimagnetron.sunscreen.nativeui.NativeUi;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record NativeUiChunkS2CPayload(
    byte @NotNull [] data
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<NativeUiChunkS2CPayload> TYPE =
        FabricMappingCompat.customPayloadType(NativeUi.CHANNEL);

    public static final StreamCodec<ByteBuf, NativeUiChunkS2CPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> buf.writeBytes(p.data()),
            buf -> {
                int len = buf.readableBytes();
                byte[] out = new byte[len];
                buf.readBytes(out);
                return new NativeUiChunkS2CPayload(out);
            }
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
