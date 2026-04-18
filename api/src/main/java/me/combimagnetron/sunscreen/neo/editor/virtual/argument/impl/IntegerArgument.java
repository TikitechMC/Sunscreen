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
import me.combimagnetron.sunscreen.neo.layout.Layout;
import me.combimagnetron.sunscreen.neo.property.Decorator;
import me.combimagnetron.sunscreen.neo.property.Position;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.theme.decorator.Target;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class IntegerArgument implements Argument<Integer> {
    private final Identifier fieldIdentifier = Identifier.of("input/field_argument_" + ThreadLocalRandom.current().nextInt());

    @Override
    public @NotNull Class<Integer> type() {
        return Integer.class;
    }

    @Override
    public @NonNull Integer decode(@NotNull Layout<?> layout) {
        TextFieldElement fieldElement = (TextFieldElement) layout.child(fieldIdentifier);
        if (fieldElement == null) return 0;
        String value = fieldElement.value();
        if (!StringUtils.isNumeric(value)) return 0;
        return Integer.parseInt(value);
    }

    @Override
    public @NotNull Collection<? extends ModernElement<?, Canvas>> fields(@NotNull Vec2i position) {
        return List.of(
            Elements.textField(fieldIdentifier).decorator(Decorator.decorator(Target.typed(EditorNameElement.class))).size(Size.fixed(Vec2i.of(193, 10))).position(Position.fixed(position.add(2, 0)))
        );
    }

}
