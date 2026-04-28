package me.combimagnetron.sunscreen.neo.graphic.text.style.impl.color;

import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.graphic.color.ColorLike;
import me.combimagnetron.sunscreen.neo.graphic.color.SpacedColorLike;
import me.combimagnetron.sunscreen.neo.graphic.text.style.Style;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface TextColor extends SpacedColorLike, Style<TextColor> permits Gradient, TextColor.TextColorImpl {

    static @NotNull TextColor color(@NotNull ColorLike colorLike) {
        return new TextColorImpl(colorLike);
    }

    static @NotNull TextColor color(@NotNull Gradient gradient) {
        return new TextColorImpl(gradient);
    }

    record TextColorImpl(ColorLike colorLike)
        implements me.combimagnetron.sunscreen.neo.graphic.text.style.impl.color.TextColor {

        public static @NotNull TextColorImpl color(@NotNull ColorLike colorLike) {
            return new TextColorImpl(colorLike);
        }

        public static @NotNull TextColorImpl color(@NotNull Gradient gradient) {
            return new TextColorImpl(gradient);
        }

        @Override
        public @Nullable ColorLike at(@NotNull Vec2i vec2i) {
            if (colorLike instanceof Gradient gradient) {
                return gradient.at(vec2i);
            }
            return colorLike;
        }

        @Override
        public @NotNull Vec2i size() {
            if (colorLike instanceof Gradient gradient) {
                return gradient.size();
            }
            return Vec2i.of(1, 1);
        }

        @Override
        public boolean in(@NotNull Vec2i vec2i) {
            if (colorLike instanceof Gradient gradient) {
                return gradient.in(vec2i);
            }
            return true;
        }

        @Override
        public int red() {
            return colorLike.red();
        }

        @Override
        public int green() {
            return colorLike.green();
        }

        @Override
        public int blue() {
            return colorLike.blue();
        }

        @Override
        public int alpha() {
            return colorLike.alpha();
        }

        @Override
        public net.kyori.adventure.text.format.@NotNull TextColor textColor() {
            return colorLike.textColor();
        }

    }

}
