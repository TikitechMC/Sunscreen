package me.combimagnetron.sunscreen.neo.graphic.text.transform;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.sunscreen.neo.graphic.color.Color;
import me.combimagnetron.sunscreen.neo.graphic.text.Text;
import me.combimagnetron.sunscreen.neo.graphic.text.TextImpl;
import me.combimagnetron.sunscreen.neo.graphic.text.decoration.DecorationType;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.color.TextColor;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.decoration.Decoration;
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
        Decoration decorationStyle = extractDecorations(component);

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
            return TextColor.color(Color.of(adventureColor));
        }
        return TextColor.color(Color.of(255, 255, 255));
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

    private static @Nullable Decoration extractDecorations(@NotNull Component component) {
        Set<DecorationType> decorationTypes = EnumSet.noneOf(DecorationType.class);

        for (TextDecoration decoration : TextDecoration.values()) {
            if (component.decoration(decoration) == TextDecoration.State.TRUE) {
                decorationTypes.add(mapDecoration(decoration));
            }
        }

        if (decorationTypes.isEmpty()) {
            return null;
        }
        return Decoration.of(decorationTypes.toArray(new DecorationType[0]));
    }

    private static @NotNull DecorationType mapDecoration(@NotNull TextDecoration decoration) {
        return switch (decoration) {
            case BOLD -> DecorationType.BOLD;
            case ITALIC -> DecorationType.ITALIC;
            case STRIKETHROUGH -> DecorationType.STRIKE_THROUGH;
            case UNDERLINED -> DecorationType.UNDERLINED;
            case OBFUSCATED -> DecorationType.ITALIC;
        };
    }

}