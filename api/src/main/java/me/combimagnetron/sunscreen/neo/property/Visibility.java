package me.combimagnetron.sunscreen.neo.property;

import me.combimagnetron.sunscreen.neo.property.handler.PropertyHandler;
import org.jetbrains.annotations.NotNull;

public record Visibility(boolean hide) implements Property<Boolean, Visibility> {
    private final static Visibility HIDDEN = hidden(true);
    private final static Visibility VISIBLE = hidden(false);

    public static @NotNull Visibility hidden(boolean hidden) {
        return new Visibility(hidden);
    }

    public static @NotNull Visibility visible() {
        return VISIBLE;
    }

    public static @NotNull Visibility hidden() {
        return HIDDEN;
    }

    @Override
    public @NotNull Class<Boolean> type() {
        return Boolean.class;
    }

    @Override
    public @NotNull PropertyHandler<Visibility> handler() {
        return (e, r, v) -> null;
    }

}
