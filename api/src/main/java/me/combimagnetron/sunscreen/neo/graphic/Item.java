package me.combimagnetron.sunscreen.neo.graphic;

import me.combimagnetron.passport.internal.entity.impl.display.Display;
import me.combimagnetron.sunscreen.neo.graphic.modifier.GraphicModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record Item<I>(@NotNull I item, @NotNull Display.Transformation transformation) implements GraphicLike<Item<I>> {

    public static <N> @NotNull Item<N> item(@NotNull N material) {
        return new Item<>(material, Display.Transformation.transformation());
    }

    @Override
    public @NotNull <T> Item<I> modifier(@NotNull GraphicModifier<T> modifier) {
        return null;
    }

    @Override
    public @NotNull BufferedColorSpace bufferedColorSpace() {
        return null;
    }

    public @NotNull Item<I> transform(@NotNull Display.Transformation transformation) {
        return new Item<>(item, transformation);
    }

}