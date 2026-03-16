package me.combimagnetron.sunscreen.neo.theme;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.sunscreen.neo.element.ModernElement;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.loader.MenuComponent;
import me.combimagnetron.sunscreen.neo.loader.ComponentLoader;
import me.combimagnetron.sunscreen.neo.loader.MenuComponentLoaderContext;
import me.combimagnetron.sunscreen.neo.theme.color.ColorScheme;
import me.combimagnetron.sunscreen.neo.theme.decorator.Target;
import me.combimagnetron.sunscreen.neo.theme.decorator.ThemeDecorator;
import me.combimagnetron.sunscreen.util.IdentifierHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public sealed interface ModernTheme extends MenuComponent<ModernTheme>, IdentifierHolder permits ModernTheme.SimpleModernTheme {

    @NotNull Identifier identifier();

    <E extends ModernElement<E, Canvas>> @NotNull ModernTheme decorator(@NotNull ThemeDecorator themeDecorator);

    <E extends ModernElement<E, Canvas>> @NotNull ThemeDecorator find(@NotNull Class<? extends @NotNull E> clazz);

    @Nullable ThemeDecorator find(@NotNull Target<?> target);

    @NotNull ModernTheme colorScheme(@NotNull ColorScheme colorScheme);

    @Nullable ColorScheme colorScheme();

    static @NotNull ModernTheme theme(@NotNull Identifier identifier) {
        return new SimpleModernTheme(identifier);
    }

    final class SimpleModernTheme implements ModernTheme {
        private final ComponentLoader<ModernTheme, MenuComponentLoaderContext> componentLoader = context -> (ModernTheme) context.menuRoot().components().stream().filter(menuComponent -> menuComponent.type().equals(ModernTheme.class)).findAny().orElseThrow();
        private final Map<Target<?>, ThemeDecorator> decoratorMap = new HashMap<>();
        private final Identifier identifier;
        private ColorScheme colorScheme;

        private SimpleModernTheme(Identifier identifier) {
            this.identifier = identifier;
        }

        @Override
        public @NotNull Identifier identifier() {
            return identifier;
        }

        @Override
        public @NotNull <E extends ModernElement<E, Canvas>> ModernTheme decorator(@NotNull ThemeDecorator themeDecorator) {
            decoratorMap.put(themeDecorator.target(), themeDecorator);
            return this;
        }

        @Override
        public @NotNull <E extends ModernElement<E, Canvas>> ThemeDecorator find(@NotNull Class<? extends @NotNull E> clazz) {
            return (ThemeDecorator) decoratorMap.values().stream().filter(decorator -> decorator.target().target().equals(clazz)).findAny().orElseThrow();
        }

        @Override
        public @Nullable ThemeDecorator find(@NotNull Target<?> target) {
            return decoratorMap.get(target);
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
        public @NotNull ComponentLoader<ModernTheme, MenuComponentLoaderContext> loader() {
            return componentLoader;
        }

        @Override
        public @NotNull Class<ModernTheme> type() {
            return ModernTheme.class;
        }

    }

}
