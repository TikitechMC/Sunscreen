package me.combimagnetron.sunscreen.neo.render.engine.grid;

import me.combimagnetron.passport.util.math.Vec3f;
import me.combimagnetron.sunscreen.neo.graphic.BufferedColorSpace;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Arrays;

public record ProcessedRenderChunk(@NotNull BufferedColorSpace bufferedColorSpace,
                                   @NotNull Vec3f position, @NotNull BigDecimal scale) implements RenderChunk {

    @Override
    public int contentHash() {
        return Arrays.hashCode(bufferedColorSpace.buffer());
    }

    @Override
    public int hashCode() {
        return contentHash();
    }

}
