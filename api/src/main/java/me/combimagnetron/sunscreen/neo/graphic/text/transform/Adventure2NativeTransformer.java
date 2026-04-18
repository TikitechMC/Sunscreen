package me.combimagnetron.sunscreen.neo.graphic.text.transform;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.sunscreen.neo.graphic.color.Color;
import me.combimagnetron.sunscreen.neo.graphic.text.Text;
import me.combimagnetron.sunscreen.neo.graphic.text.TextImpl;
import me.combimagnetron.sunscreen.neo.graphic.text.decoration.Decoration;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.color.TextColor;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.color.TextColorImpl;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.decoration.DecorationStyle;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.font.Font;
import me.combimagnetron.sunscreen.neo.registry.Registries;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class Adventure2NativeTransformer {

    public static @NotNull Text adventure(@NotNull Component component) {
        return traverse(component);
    }

    private static @NotNull TextImpl traverse(@NotNull Component component) {
        String content = extractContent(component);
        TextColor textColor = extractColor(component);
        Font font = extractFont(component);
        DecorationStyle decorationStyle = extractDecorations(component);

        TextImpl text = TextImpl.basic(content);
        text = (TextImpl) text.color(textColor);
        if (font != null) {
            text = (TextImpl) text.font(font);
        }
        if (decorationStyle != null) {
            text = (TextImpl) text.style(decorationStyle);
        }

        List<Component> children = component.children();
        if (!children.isEmpty()) {
            for (Component child : children) {
                text = (TextImpl) text.append(traverse(child));
            }
        }

        return text;
    }

    private static @NotNull String extractContent(@NotNull Component component) {
        if (component instanceof TextComponent textComponent) {
            return textComponent.content();
        }
        return "";
    }

    private static @NotNull TextColor extractColor(@NotNull Component component) {
        net.kyori.adventure.text.format.TextColor adventureColor = component.color();
        if (adventureColor != null) {
            return TextColorImpl.color(Color.of(adventureColor));
        }
        return TextColorImpl.color(Color.of(255, 255, 255));
    }

    private static @Nullable Font extractFont(@NotNull Component component) {
        Key adventureFont = component.font();
        if (adventureFont != null) {
            String fontKey = adventureFont.asString();
            String[] parts = fontKey.split(":", 2);
            if (parts.length == 2) {
                Identifier identifier = Identifier.of(parts[0], parts[1]);
                return Registries.fonts().get(identifier);
            }
        }
        return null;
    }

    private static @Nullable DecorationStyle extractDecorations(@NotNull Component component) {
        Set<Decoration> decorations = EnumSet.noneOf(Decoration.class);

        for (TextDecoration decoration : TextDecoration.values()) {
            if (component.decoration(decoration) == TextDecoration.State.TRUE) {
                decorations.add(mapDecoration(decoration));
            }
        }

        if (decorations.isEmpty()) {
            return null;
        }
        return DecorationStyle.of(decorations.toArray(new Decoration[0]));
    }

    private static @NotNull Decoration mapDecoration(@NotNull TextDecoration decoration) {
        return switch (decoration) {
            case BOLD -> Decoration.BOLD;
            case ITALIC -> Decoration.ITALIC;
            case STRIKETHROUGH -> Decoration.STRIKE_THROUGH;
            case UNDERLINED -> Decoration.UNDERLINED;
            case OBFUSCATED -> Decoration.ITALIC;
        };
    }

}