package me.combimagnetron.sunscreen.neo.editor.virtual.argument.impl;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.ActiveMenu;
import me.combimagnetron.sunscreen.neo.editor.element.EditorElements;
import me.combimagnetron.sunscreen.neo.editor.element.EditorNameElement;
import me.combimagnetron.sunscreen.neo.editor.element.PageNameElement;
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

public class IdentifierArgument implements Argument<Identifier> {
    private final int shittyId = ThreadLocalRandom.current().nextInt();
    private final Identifier elementIdentifier = Identifier.of("input/element_name_thingie" + shittyId);
    private final Identifier label = Identifier.of("input/label_thingie" + shittyId);

    @Override
    public @NotNull Class<Identifier> type() {
        return Identifier.class;
    }

    @Override
    public @NotNull Identifier decode(@NotNull Layout<?> layout) {
        PageNameElement fieldElement = (PageNameElement) layout.child(elementIdentifier);
        if (fieldElement == null) return Identifier.of("oopsie");
        return fieldElement.fakeIdentifier();
    }

    @Override
    public @NotNull Collection<? extends ModernElement<?, Canvas>> fields(@NotNull Vec2i position) {
        return List.of(
            EditorElements.pageNameElement(elementIdentifier).firstWord("element").size(Size.fixed(Vec2i.of(193, 10))).position(Position.fixed(position.add(2, 0))).decorator(Decorator.decorator(Target.typed(EditorNameElement.class)))
        );
    }

}
