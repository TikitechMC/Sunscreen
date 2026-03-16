package me.combimagnetron.sunscreen.neo.render.engine.encode;

import me.combimagnetron.sunscreen.neo.render.engine.grid.ProcessedRenderChunk;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;

public final class MapEncoderFactory {
    private static final boolean SIMD_AVAILABLE;

    static {
        boolean available;
        try {
            Class.forName("jdk.incubator.vector.IntVector");
            available = true;
        } catch (ClassNotFoundException e) {
            available = false;
        }
        SIMD_AVAILABLE = available;
    }

    private MapEncoderFactory() {}

    public static ByteArrayOutputStream encode(@NotNull ProcessedRenderChunk chunk) {
        if (SIMD_AVAILABLE) {
            return new MapEncoderSimd(chunk).bytes();
        }
        return new MapEncoderIHateMyselfMore(chunk).bytes();
    }

    public static boolean simdAvailable() {
        return SIMD_AVAILABLE;
    }
}
