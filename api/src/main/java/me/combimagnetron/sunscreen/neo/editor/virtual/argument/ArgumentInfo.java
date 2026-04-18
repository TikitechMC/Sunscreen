package me.combimagnetron.sunscreen.neo.editor.virtual.argument;

import org.jetbrains.annotations.NotNull;

public record ArgumentInfo(int count, @NotNull TypeInfo @NotNull... argumentTypes) {

    public static ArgumentInfo info(int count, @NotNull TypeInfo @NotNull... argumentTypes) {
        return new ArgumentInfo(count, argumentTypes);
    }

    public ArgumentInfo {
    }

    public record TypeInfo(@NotNull Class<? extends Argument<?>> type, boolean required, String displayName) {

    }

}
