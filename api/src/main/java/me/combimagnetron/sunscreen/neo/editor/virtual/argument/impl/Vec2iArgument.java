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

public class Vec2iArgument implements Argument<Vec2i> {
    private final int wackAssRandomThing = ThreadLocalRandom.current().nextInt();
    private final Identifier xFieldIdentifier = Identifier.of("input/x_field_argument_" + wackAssRandomThing);
    private final Identifier yFieldIdentifier = Identifier.of("input/y_field_argument_" + wackAssRandomThing);

    @Override
    public @NotNull Class<Vec2i> type() {
        return Vec2i.class;
    }

    @Override
    public @NonNull Vec2i decode(@NotNull Layout<?> layout) {
        TextFieldElement xFieldElement = (TextFieldElement) layout.child(xFieldIdentifier);
        TextFieldElement yFieldElement = (TextFieldElement) layout.child(yFieldIdentifier);
        if (xFieldElement == null || yFieldElement == null) return Vec2i.zero();
        String xValue = xFieldElement.value();
        String yValue = yFieldElement.value();
        if (!StringUtils.isNumeric(xValue) || !StringUtils.isNumeric(yValue)) return Vec2i.zero();
        return Vec2i.of(Integer.parseInt(xFieldElement.value()), Integer.parseInt(yFieldElement.value()));
    }

    @Override
    public @NotNull Collection<? extends ModernElement<?, Canvas>> fields(@NotNull Vec2i position) {
        return List.of(
            Elements.textField(xFieldIdentifier).decorator(Decorator.decorator(Target.typed(EditorNameElement.class))).size(Size.fixed(Vec2i.of(94, 11))).position(Position.fixed(position.add(2, 0))),
            Elements.textField(yFieldIdentifier).decorator(Decorator.decorator(Target.typed(EditorNameElement.class))).size(Size.fixed(Vec2i.of(94, 11))).position(Position.fixed(position.add(102 , 0)))
        );
    }

}
