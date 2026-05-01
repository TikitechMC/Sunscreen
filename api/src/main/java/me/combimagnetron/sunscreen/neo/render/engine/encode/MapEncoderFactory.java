package me.combimagnetron.sunscreen.neo.render.engine.encode;

import me.combimagnetron.sunscreen.neo.render.engine.exception.FatalEncodeException;
import me.combimagnetron.sunscreen.neo.render.engine.grid.ProcessedRenderChunk;
import org.jetbrains.annotations.NotNull;

public final class MapEncoderFactory {
    private static final ThreadLocal<MapEncoderIHateMyselfMore> ENCODER =
            ThreadLocal.withInitial(MapEncoderIHateMyselfMore::new);

    private MapEncoderFactory() {}

    public static byte[] encode(@NotNull ProcessedRenderChunk chunk) {
        try {
            return ENCODER.get().encode(chunk);
        } catch (FatalEncodeException first) {
            try {
                return ENCODER.get().encode(MapQuantizer.quantized(chunk, 16, 1));
            } catch (FatalEncodeException second) {
                try {
                    return ENCODER.get().encode(MapQuantizer.quantized(chunk, 32, 1));
                } catch (FatalEncodeException third) {
                    return ENCODER.get().encode(MapQuantizer.quantized(chunk, 32, 2));
                }
            }
        }
    }
}
