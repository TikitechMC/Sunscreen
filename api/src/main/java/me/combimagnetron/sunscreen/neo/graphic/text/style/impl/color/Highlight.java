package me.combimagnetron.sunscreen.neo.graphic.text.style.impl.color;

import me.combimagnetron.sunscreen.neo.graphic.color.Color;
import me.combimagnetron.sunscreen.neo.graphic.color.ColorLike;
import me.combimagnetron.sunscreen.neo.graphic.text.style.Style;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

public sealed interface Highlight extends ColorLike, Style<Highlight> {

    static @NotNull Highlight highlight(@NotNull Color color) {
        return new HighlightImpl(color);
    }

    record HighlightImpl(@NotNull Color color) implements Highlight {

        @Override
        public int red() {
            return color.red();
        }

        @Override
        public int green() {
            return color.green();
        }

        @Override
        public int blue() {
            return color.blue();
        }

        @Override
        public int alpha() {
            return color.alpha();
        }

        @Override
        public @NotNull TextColor textColor() {
            return null;
        }

    }

}
