package me.combimagnetron.sunscreen.neo.property;

import me.combimagnetron.sunscreen.neo.property.handler.PropertyHandler;
import me.combimagnetron.sunscreen.neo.theme.decorator.Target;
import org.jetbrains.annotations.NotNull;

public record Decorator<T>(@NotNull Target<T> target) implements Property<Target<T>, Decorator<T>> {

    @Override
    public @NotNull Class<Target<T>> type() {
        return null;
    }

    @Override
    public @NotNull PropertyHandler<Decorator<T>> handler() {
        return (parent, renderContext, property) -> null;
    }

    public static <T> @NotNull Decorator<T> decorator(@NotNull Target<T> target) {
        return new Decorator<>(target);
    }

}
