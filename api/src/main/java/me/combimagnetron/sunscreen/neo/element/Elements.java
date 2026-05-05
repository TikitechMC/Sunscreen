package me.combimagnetron.sunscreen.neo.element;

import me.combimagnetron.passport.logic.state.State;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.element.impl.*;
import me.combimagnetron.sunscreen.neo.element.impl.text.TextBoxElement;
import me.combimagnetron.sunscreen.neo.element.impl.text.TextEditorElement;
import me.combimagnetron.sunscreen.neo.element.impl.text.TextFieldElement;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.graphic.GraphicLike;
import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.sunscreen.neo.graphic.color.Color;
import me.combimagnetron.sunscreen.neo.graphic.shape.Shape;
import me.combimagnetron.sunscreen.neo.graphic.text.Text;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Elements {

    static <G extends GraphicLike<G>> @NotNull ImageElement<G> image(@NotNull Identifier identifier, @NotNull G graphicLike) {
        return new ImageElement<>(identifier, graphicLike);
    }

    static @NotNull LabelElement label(@NotNull Identifier identifier, @NotNull Component text) {
        return new LabelElement(identifier, text);
    }

    static @NotNull LabelElement label(@NotNull Identifier identifier, @NotNull State<Text> text) {
        return new LabelElement(identifier, text);
    }

    static @NotNull LabelElement label(@NotNull Identifier identifier, @NotNull Text text) {
        return new LabelElement(identifier, text);
    }

    static @NotNull TextBoxElement textBox(@NotNull Identifier identifier, @NotNull Component initialMessage) {
        return new TextBoxElement(identifier);
    }

    static @NotNull TextBoxElement textBox(@NotNull Identifier identifier) {
        return new TextBoxElement(identifier);
    }

    static @NotNull TextFieldElement textField(@NotNull Identifier identifier, @NotNull Component initialMessage) {
        return new TextFieldElement(identifier);
    }

    static @NotNull TextFieldElement textField(@NotNull Identifier identifier) {
        return new TextFieldElement(identifier);
    }

    static @NotNull TextEditorElement textEditor(@NotNull Identifier identifier, @NotNull Component initialMessage) {
        return new TextEditorElement(identifier);
    }

    static @NotNull TextEditorElement textEditor(@NotNull Identifier identifier) {
        return new TextEditorElement(identifier);
    }

    static @NotNull ShapeElement shape(@NotNull Identifier identifier, @NotNull Shape shape, @NotNull Color color) {
        return new ShapeElement(identifier, shape, color);
    }

    static @NotNull ShapeElement shape(@NotNull Identifier identifier, @NotNull Shape shape) {
        return new ShapeElement(identifier, shape);
    }

    static @NotNull ButtonElement button(@NotNull Identifier identifier) {
        return new ButtonElement(identifier);
    }

    static @NotNull ButtonElement button(@NotNull Identifier identifier, @NotNull Text text) {
        return new ButtonElement(identifier, text);
    }

    static @NotNull ButtonElement button(@NotNull Identifier identifier, @NotNull Text text, @NotNull Vec2i position) {
        return new ButtonElement(identifier, text, position);
    }

    static @NotNull ComparisonElement comparison(@NotNull Identifier identifier, @Nullable Canvas left, @Nullable Canvas right) {
        return new ComparisonElement(identifier, left, right);
    }

    static @NotNull ComparisonElement comparison(@NotNull Identifier identifier) {
        return new ComparisonElement(identifier);
    }

}
