package me.combimagnetron.sunscreen.neo.registry;

import me.combimagnetron.passport.internal.registry.Registry;
import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.sunscreen.neo.MenuTemplate;
import me.combimagnetron.sunscreen.neo.cursor.CursorStyle;
import me.combimagnetron.sunscreen.neo.editor.virtual.argument.ElementConstructionProvider;
import me.combimagnetron.sunscreen.neo.file.adapter.ElementAdapter;
import me.combimagnetron.sunscreen.neo.file.adapter.PropertyAdapter;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.font.AtlasFont;
import me.combimagnetron.sunscreen.neo.theme.ModernTheme;
import me.combimagnetron.sunscreen.util.IdentifierHolder;
import org.jetbrains.annotations.NotNull;

public interface Registries {
    Registry<CursorStyle, Identifier> CURSOR_STYLES = Registry.identifier();
    Registry<MenuTemplate, Identifier> TEMPLATES = Registry.identifier();
    Registry<ModernTheme, Identifier> THEMES = Registry.identifier();
    Registry<AtlasFont, Identifier> FONTS = Registry.identifier();
    Registry<ElementConstructionProvider<?>, Identifier> CONSTRUCTION_PROVIDERS = Registry.identifier();
    Registry<ElementAdapter<?>, String> ELEMENT_ADAPTERS = Registry.string();
    Registry<PropertyAdapter<?>, String> PROPERTY_ADAPTERS = Registry.string();
    Advanced INTERNAL = new Advanced(PROPERTY_ADAPTERS, ELEMENT_ADAPTERS, CONSTRUCTION_PROVIDERS);

    static <T extends IdentifierHolder> boolean register(@NotNull Registry<T, Identifier> registry, @NotNull T t) {
        Identifier identifier = t.identifier();
        if (registry.contains(identifier)) return false;
        registry.register(identifier, t);
        return true;
    }

    static @NotNull Advanced advanced() {
        return INTERNAL;
    }

    static @NotNull Registry<MenuTemplate, Identifier> templates() {
        return TEMPLATES;
    }

    static @NotNull Registry<ModernTheme, Identifier> themes() {
        return THEMES;
    }

    static @NotNull Registry<AtlasFont, Identifier> fonts() {
        return FONTS;
    }

    record Advanced(Registry<PropertyAdapter<?>, String> propertyAdapters,
                    Registry<ElementAdapter<?>, String> elementAdapters,
                    Registry<ElementConstructionProvider<?>, Identifier> constructionProviders) {

    }

}
