package me.combimagnetron.sunscreen.neo.render.engine.encode;

import me.combimagnetron.sunscreen.neo.render.engine.grid.ProcessedRenderChunk;
import org.jetbrains.annotations.NotNull;

public final class MapEncoderFactory {
    private static final ThreadLocal<MapEncoderIHateMyselfMore> ENCODER =
            ThreadLocal.withInitial(MapEncoderIHateMyselfMore::new);

    private MapEncoderFactory() {}

    public static byte[] encode(@NotNull ProcessedRenderChunk chunk) {
        return ENCODER.get().encode(chunk);
    }
}
