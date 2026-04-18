package me.combimagnetron.sunscreen.neo.editor.virtual;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.sunscreen.neo.element.ModernElement;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.loader.ComponentLoader;
import me.combimagnetron.sunscreen.neo.loader.MenuComponentLoaderContext;
import me.combimagnetron.sunscreen.neo.theme.ModernTheme;
import me.combimagnetron.sunscreen.neo.theme.color.ColorScheme;
import me.combimagnetron.sunscreen.neo.theme.decorator.Target;
import me.combimagnetron.sunscreen.neo.theme.decorator.ThemeDecorator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VirtualTheme implements ModernTheme {
    private final Map<Target<?>, ThemeDecorator> decorators = new HashMap<>();
    private final Identifier identifier;
    private ColorScheme colorScheme;

    public VirtualTheme(Identifier identifier) {
        this.identifier = identifier;
    }

    @Override
    public @NotNull VirtualTheme decorator(@NotNull ThemeDecorator decorator) {
        decorators.put(decorator.target(), decorator);
        return this;
    }

    @Override
    public @NotNull Identifier identifier() {
        return identifier;
    }

    @Override
    public @NotNull <E extends ModernElement<E, Canvas>> ThemeDecorator find(@NotNull Class<? extends @NotNull E> clazz) {
        return decorators.values().stream().filter(decorator -> decorator.target().target().equals(clazz)).findAny().orElseThrow();
    }

    @Override
    public @Nullable ThemeDecorator find(@NotNull Target<?> target) {
        return decorators.get(target);
    }

    @Override
    public @NotNull ModernTheme colorScheme(@NotNull ColorScheme colorScheme) {
        this.colorScheme = colorScheme;
        return this;
    }

    @Override
    public @Nullable ColorScheme colorScheme() {
        return colorScheme;
    }

    @Override
    public @NotNull Collection<ThemeDecorator> decorators() {
        return decorators.values();
    }

    @Override
    public @NotNull ComponentLoader<ModernTheme, MenuComponentLoaderContext> loader() {
        return null;
    }

    @Override
    public @NotNull Class<ModernTheme> type() {
        return null;
    }
}
