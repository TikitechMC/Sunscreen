package me.combimagnetron.sunscreen.neo.theme.decorator;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.sunscreen.neo.element.ElementLike;
import org.jetbrains.annotations.NotNull;

public sealed interface Target<T> permits Target.IdentifierTarget, Target.TypedTarget {

    @NotNull T target();

    static @NotNull Target<Identifier> identifier(@NotNull Identifier identifier) {
        return new IdentifierTarget(identifier);
    }

    static <E extends ElementLike<E>> @NotNull Target<Class<E>> typed(@NotNull Class<E> clazz) {
        return new TypedTarget<>(clazz);
    }

    record IdentifierTarget(@NotNull Identifier target) implements Target<Identifier> {

    }

    record TypedTarget<E extends ElementLike<E>>(@NotNull Class<E> target) implements Target<Class<E>> {

    }

}