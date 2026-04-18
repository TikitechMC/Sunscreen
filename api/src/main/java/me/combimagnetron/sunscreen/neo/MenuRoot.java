package me.combimagnetron.sunscreen.neo;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.sunscreen.neo.element.ElementLike;
import me.combimagnetron.sunscreen.neo.loader.MenuComponent;
import me.combimagnetron.sunscreen.neo.page.Page;
import me.combimagnetron.sunscreen.neo.theme.ModernTheme;
import me.combimagnetron.sunscreen.neo.theme.color.ColorSchemes;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public final class MenuRoot implements RootLike<MenuRoot> {
    private final static ModernTheme DEFAULT_THEME = ModernTheme.theme(Identifier.of("basic-theme")).colorScheme(ColorSchemes.BASIC_DARK);
    private final List<ElementLike<?>> elementLikes = new LinkedList<>();
    private final List<MenuComponent<?>> menuComponents = new LinkedList<>();

    MenuRoot() {

    }

    @Override
    public <E extends ElementLike<E>> @NotNull MenuRoot element(@NotNull ElementLike<E> e) {
        elementLikes.add(e);
        return this;
    }

    @Override
    public @NotNull <C extends MenuComponent<C>> MenuRoot component(@NotNull MenuComponent<C> menuComponent) {
        menuComponents.add(menuComponent);
        return this;
    }

    public void clear() {
        elementLikes.clear();
        menuComponents.clear();
    }

    public @NotNull MenuRoot page(@NotNull Page page) {
        return this;
    }

    @Override
    public @NotNull Collection<ElementLike<?>> elementLikes() {
        return elementLikes;
    }

    @Override
    public @NotNull Collection<MenuComponent<?>> components() {
        return menuComponents;
    }

}
