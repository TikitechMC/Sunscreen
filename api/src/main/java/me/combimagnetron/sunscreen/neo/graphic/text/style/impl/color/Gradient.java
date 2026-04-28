package me.combimagnetron.sunscreen.neo.graphic.text.style.impl.color;

import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.graphic.color.Color;
import me.combimagnetron.sunscreen.neo.graphic.color.ColorLike;
import me.combimagnetron.sunscreen.neo.graphic.color.SpacedColorLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface Gradient extends SpacedColorLike, TextColor {

    static @NotNull Gradient radial(@NotNull ColorLike center, @NotNull ColorLike edge) {
        return new RadialGradient(center, edge);
    }

    static @NotNull Gradient linear(@NotNull ColorLike start, @NotNull ColorLike end) {
        return new LinearGradient(start, end, Vec2i.of(100, 1));
    }

    static @NotNull Gradient elliptical(@NotNull ColorLike center, @NotNull ColorLike edge) {
        return new RadialGradient(center, edge);
    }

    static @NotNull Gradient conical(@NotNull ColorLike start, @NotNull ColorLike end) {
        return new LinearGradient(start, end, Vec2i.of(100, 1));
    }

    @NotNull
    ColorLike start();

    @NotNull
    ColorLike end();

    record LinearGradient(@NotNull ColorLike start, @NotNull ColorLike end, @NotNull Vec2i size) implements Gradient {

        @Override
        public @Nullable ColorLike at(@NotNull Vec2i vec2i) {
            if (!in(vec2i))
                return null;
            float ratio = size.x() > 1 ? (float) vec2i.x() / (size.x() - 1) : 0;
            return interpolate(start, end, ratio);
        }

        @Override
        public boolean in(@NotNull Vec2i vec2i) {
            return vec2i.x() >= 0 && vec2i.x() < size.x() && vec2i.y() >= 0 && vec2i.y() < size.y();
        }

        @Override
        public int red() {
            return start.red();
        }

        @Override
        public int green() {
            return start.green();
        }

        @Override
        public int blue() {
            return start.blue();
        }

        @Override
        public int alpha() {
            return start.alpha();
        }

        @Override
        public @NotNull net.kyori.adventure.text.format.TextColor textColor() {
            return start.textColor();
        }

        private static ColorLike interpolate(ColorLike a, ColorLike b, float ratio) {
            int r = (int) (a.red() + (b.red() - a.red()) * ratio);
            int g = (int) (a.green() + (b.green() - a.green()) * ratio);
            int bl = (int) (a.blue() + (b.blue() - a.blue()) * ratio);
            int al = (int) (a.alpha() + (b.alpha() - a.alpha()) * ratio);
            return Color.of(r, g, bl, al);
        }
    }

    record RadialGradient(@NotNull ColorLike center, @NotNull ColorLike edge) implements Gradient {

        @Override
        public @Nullable ColorLike at(@NotNull Vec2i vec2i) {
            return center;
        }

        @Override
        public @NotNull Vec2i size() {
            return Vec2i.of(1, 1);
        }

        @Override
        public boolean in(@NotNull Vec2i vec2i) {
            return true;
        }

        @Override
        public int red() {
            return center.red();
        }

        @Override
        public int green() {
            return center.green();
        }

        @Override
        public int blue() {
            return center.blue();
        }

        @Override
        public int alpha() {
            return center.alpha();
        }

        @Override
        public @NotNull net.kyori.adventure.text.format.TextColor textColor() {
            return center.textColor();
        }

        @Override
        public @NotNull ColorLike start() {
            return center;
        }

        @Override
        public @NotNull ColorLike end() {
            return edge;
        }
    }

}
