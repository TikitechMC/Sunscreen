package me.combimagnetron.sunscreen.neo.theme;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.sunscreen.neo.element.ModernElement;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.loader.MenuComponent;
import me.combimagnetron.sunscreen.neo.loader.ComponentLoader;
import me.combimagnetron.sunscreen.neo.loader.MenuComponentLoaderContext;
import me.combimagnetron.sunscreen.neo.theme.color.ColorScheme;
import me.combimagnetron.sunscreen.neo.theme.decorator.ThemeDecorator;
import me.combimagnetron.sunscreen.util.IdentifierHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public sealed interface ModernTheme extends MenuComponent<ModernTheme>, IdentifierHolder permits ModernTheme.SimpleModernTheme {

    @NotNull Identifier identifier();

    <E extends ModernElement<E, Canvas>, D extends ThemeDecorator<E>> @NotNull ModernTheme decorator(@NotNull ThemeDecorator<E> themeDecorator);

    <E extends ModernElement<E, Canvas>, D extends ThemeDecorator<E>> @NotNull ThemeDecorator<E> find(@NotNull Class<@NotNull ? extends E> clazz);

    @NotNull ModernTheme colorScheme(@NotNull ColorScheme colorScheme);

    @Nullable ColorScheme colorScheme();

    static @NotNull ModernTheme theme(@NotNull Identifier identifier) {
        return new SimpleModernTheme(identifier);
    }

    final class SimpleModernTheme implements ModernTheme {
        private final ComponentLoader<ModernTheme, MenuComponentLoaderContext> componentLoader = context -> (ModernTheme) context.menuRoot().components().stream().filter(menuComponent -> menuComponent.type().equals(ModernTheme.class)).findAny().orElseThrow();
        private final Map<Class<?>, ThemeDecorator<?>> decoratorMap = new HashMap<>();
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
        public @NotNull <E extends ModernElement<E, Canvas>, D extends ThemeDecorator<E>> ModernTheme decorator(@NotNull ThemeDecorator<E> themeDecorator) {
            decoratorMap.put(themeDecorator.target(), themeDecorator);
            return this;
        }

        @Override
        public @NotNull <E extends ModernElement<E, Canvas>, D extends ThemeDecorator<E>> ThemeDecorator<E> find(@NotNull Class<@NotNull ? extends E> clazz) {
            return (ThemeDecorator<E>) decoratorMap.get(clazz);
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
