package me.combimagnetron.sunscreen.neo.editor.virtual.argument.impl;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.ActiveMenu;
import me.combimagnetron.sunscreen.neo.editor.element.EditorNameElement;
import me.combimagnetron.sunscreen.neo.editor.virtual.argument.Argument;
import me.combimagnetron.sunscreen.neo.element.Elements;
import me.combimagnetron.sunscreen.neo.element.ModernElement;
import me.combimagnetron.sunscreen.neo.element.impl.text.TextFieldElement;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.graphic.text.Text;
import me.combimagnetron.sunscreen.neo.layout.Layout;
import me.combimagnetron.sunscreen.neo.property.Decorator;
import me.combimagnetron.sunscreen.neo.property.Position;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.theme.decorator.Target;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class TextArgument implements Argument<Text> {
    private final Identifier elementIdentifier = Identifier.of("input/text_field_argument_" + ThreadLocalRandom.current().nextInt());

    @Override
    public @NotNull Class<Text> type() {
        return Text.class;
    }

    @Override
    public @NotNull Text decode(@NotNull Layout<?> layout) {
        TextFieldElement fieldElement = (TextFieldElement) layout.child(elementIdentifier);
        if (fieldElement == null) return Text.vanilla("Error");
        return Text.vanilla(fieldElement.value());
    }

    @Override
    public @NotNull Collection<? extends ModernElement<?, Canvas>> fields(@NotNull Vec2i position) {
        return List.of(
            Elements.textField(elementIdentifier).size(Size.fixed(Vec2i.of(193, 10))).position(Position.fixed(position.add(2, 0))).decorator(Decorator.decorator(Target.typed(EditorNameElement.class)))
        );
    }

}
