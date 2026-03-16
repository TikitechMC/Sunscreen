package me.combimagnetron.sunscreen.neo.property;

import me.combimagnetron.sunscreen.neo.property.handler.PropertyHandler;
import org.jetbrains.annotations.NotNull;

public record Z(float value) implements Property<Float, Z> {
    private static final Z INDEX = Z.z(0f);

    public static @NotNull Z z(float value) {
        return new Z(value);
    }

    public static @NotNull Z middle() {
        return INDEX;
    }

    @Override
    public @NotNull Class<Float> type() {
        return Float.class;
    }

    @Override
    public @NotNull PropertyHandler<Z> handler() {
        return (_, _, _) -> null;
    }

}
