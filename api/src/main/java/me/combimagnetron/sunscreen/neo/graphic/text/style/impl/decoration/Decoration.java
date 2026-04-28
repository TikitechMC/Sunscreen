package me.combimagnetron.sunscreen.neo.graphic.text.style.impl.decoration;

import me.combimagnetron.sunscreen.neo.graphic.text.decoration.DecorationType;
import me.combimagnetron.sunscreen.neo.graphic.text.style.Style;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public record Decoration(@NotNull Set<DecorationType> decorationTypes) implements Style<Decoration> {

    public static @NotNull Decoration of(@NotNull DecorationType @NotNull... decorationTypes) {
        return new Decoration(Set.of(decorationTypes));
    }

    public static @NotNull Decoration none() {
        return new Decoration(Set.of());
    }

}