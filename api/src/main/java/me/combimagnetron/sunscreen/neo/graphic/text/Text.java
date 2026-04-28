package me.combimagnetron.sunscreen.neo.graphic.text;

import me.combimagnetron.passport.logic.state.State;
import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.graphic.color.Color;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.color.Gradient;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.color.Highlight;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.color.TextColor;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.decoration.Decoration;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.font.Font;
import me.combimagnetron.sunscreen.neo.graphic.text.style.Style;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.font.FontProperties;
import me.combimagnetron.sunscreen.neo.graphic.text.transform.Adventure2NativeTransformer;
import me.combimagnetron.sunscreen.neo.input.keybind.Keybind;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.registry.Registries;
import me.combimagnetron.sunscreen.neo.render.Renderable;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public sealed interface Text extends Renderable<Size, Canvas> permits TextImpl {

    static @NotNull Text adventure(@NotNull Component component) {
        return Adventure2NativeTransformer.adventure(component);
    }

    static @NotNull Text vanilla(@NotNull String content) {
        return Text.basic(content).font(Registries.fonts().get(Identifier.of("sunscreen", "font/minecraft"))).fontProperties(FontProperties.properties().baseline(-2));
    }

    static @NotNull Text small(@NotNull String content) {
        return Text.basic(content).font(Registries.fonts().get(Identifier.of("sunscreen", "font/sunburned"))).fontProperties(FontProperties.properties().baseline(-6));
    }

    static @NotNull Text keybind(@NotNull Keybind keybind) {
        return TextImpl.basic(keybind.toString());
    }

    static @NotNull Text basic(@NotNull String content) {
        return TextImpl.basic(content);
    }

    static @NotNull Text chained(@NotNull Text @NotNull... texts) {
        return TextImpl.chained(texts);
    }

    static @NotNull Text state(@NotNull State<String> state) {
        return TextImpl.state(state);
    }

    @NotNull
    <S extends Style<?>> Text style(@NotNull S style);

    default @NotNull Text fontProperties(@NotNull FontProperties properties) {
        return style(properties);
    }

    default @NotNull Text color(@NotNull TextColor textColor) {
        return style(textColor);
    }

    default @NotNull Text font(@NotNull Font font) {
        return style(font);
    }

    default @NotNull Text highlight(@NotNull Highlight highlight) {
        return style(highlight);
    }

    @NotNull FontProperties fontProperties();

    @NotNull TextColor color();

    @NotNull Font font();

    @NotNull Highlight highlight();

    @NotNull Decoration decoration();

    @NotNull Text content(@NotNull String string);

    @NotNull Text content(@NotNull Component component);

    @NotNull Text append(@NotNull Text text);

}
