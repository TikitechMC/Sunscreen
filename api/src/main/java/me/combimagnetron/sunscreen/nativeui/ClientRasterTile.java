package me.combimagnetron.sunscreen.nativeui;

import me.combimagnetron.passport.util.math.Vec3f;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

/** One 128× (or sub) tile from the client render pipeline, ready for GPU upload. */
public record ClientRasterTile(
    @NotNull BigDecimal scale,
    @NotNull Vec3f gridPosition,
    int width,
    int height,
    int @NotNull [] argbPixels
) {
}
