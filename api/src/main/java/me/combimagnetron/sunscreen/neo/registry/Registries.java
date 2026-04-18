package me.combimagnetron.sunscreen.neo.registry;

import me.combimagnetron.passport.internal.registry.Registry;
import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.sunscreen.neo.MenuTemplate;
import me.combimagnetron.sunscreen.neo.cursor.CursorStyle;
import me.combimagnetron.sunscreen.neo.editor.virtual.argument.ElementConstructionProvider;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.font.AtlasFont;
import me.combimagnetron.sunscreen.neo.theme.ModernTheme;
import me.combimagnetron.sunscreen.util.IdentifierHolder;
import org.jetbrains.annotations.NotNull;

public interface Registries {
    Registry<CursorStyle> CURSOR_STYLES = Registry.empty();
    Registry<MenuTemplate> TEMPLATES = Registry.empty();
    Registry<ModernTheme> THEMES = Registry.empty();
    Registry<AtlasFont> FONTS = Registry.empty();
    Registry<ElementConstructionProvider<?>> CONSTRUCTION_PROVIDERS = Registry.empty();

    static <T extends IdentifierHolder> boolean register(@NotNull Registry<T> registry, @NotNull T t) {
        Identifier identifier = t.identifier();
        if (registry.contains(identifier)) return false;
        registry.register(identifier, t);
        return true;
    }

    static @NotNull Registry<ElementConstructionProvider<?>> constructionProviders() {
        return CONSTRUCTION_PROVIDERS;
    }

    static @NotNull Registry<MenuTemplate> templates() {
        return TEMPLATES;
    }

    static @NotNull Registry<ModernTheme> themes() {
        return THEMES;
    }

    static @NotNull Registry<AtlasFont> fonts() {
        return FONTS;
    }

}
