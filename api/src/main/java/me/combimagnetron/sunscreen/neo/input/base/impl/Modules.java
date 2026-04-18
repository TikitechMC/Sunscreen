package me.combimagnetron.sunscreen.neo.input.base.impl;

import me.combimagnetron.passport.util.math.Vec2i;
import org.jetbrains.annotations.NotNull;

public interface Modules {

    static @NotNull ClickModule click(@NotNull Vec2i position, @NotNull Vec2i size) {
        return new ClickModule(position, size);
    }

    static @NotNull HoverModule hover(@NotNull Vec2i position, @NotNull Vec2i size) {
        return new HoverModule(position, size);
    }

    static @NotNull DragModule drag(@NotNull Vec2i position, @NotNull Vec2i size) {
        return new DragModule(position, size);
    }

}
