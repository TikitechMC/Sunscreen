package me.combimagnetron.sunscreen.neo.render.engine.grid;

import me.combimagnetron.passport.util.math.Vec3f;
import me.combimagnetron.sunscreen.neo.graphic.BufferedColorSpace;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public record ItemRenderChunk(@NotNull BufferedColorSpace bufferedColorSpace, @NotNull Vec3f position, @NotNull BigDecimal scale, int contentHash) implements RenderChunk {

}
