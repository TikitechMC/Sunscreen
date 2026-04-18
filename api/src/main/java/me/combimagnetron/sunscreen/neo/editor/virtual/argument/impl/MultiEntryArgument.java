package me.combimagnetron.sunscreen.neo.editor.virtual.argument.impl;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.ActiveMenu;
import me.combimagnetron.sunscreen.neo.editor.element.MultiEntryFieldElement;
import me.combimagnetron.sunscreen.neo.editor.virtual.argument.Argument;
import me.combimagnetron.sunscreen.neo.element.ModernElement;
import me.combimagnetron.sunscreen.neo.element.impl.SelectorElement;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.graphic.text.Text;
import me.combimagnetron.sunscreen.neo.layout.Layout;
import me.combimagnetron.sunscreen.neo.property.Decorator;
import me.combimagnetron.sunscreen.neo.property.Position;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.theme.decorator.Target;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MultiEntryArgument implements Argument<Collection<Text>> {
    private final Identifier elementIdentifier = Identifier.of("multi_entry_argument_" + ThreadLocalRandom.current().nextInt());

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull Class<Collection<Text>> type() {
        return (Class<Collection<Text>>) (Class<?>) Collection.class;
    }

    @Override
    public @NonNull Collection<Text> decode(@NotNull Layout<?> layout) {
        MultiEntryFieldElement entryFieldElement = (MultiEntryFieldElement) layout.child(elementIdentifier);
        if (entryFieldElement == null) return List.of();
        return entryFieldElement.entries().stream().map(Text::vanilla).toList();
    }

    @Override
    public @NotNull Collection<? extends ModernElement<?, Canvas>> fields(@NotNull Vec2i position) {
        return List.of(
            new MultiEntryFieldElement(elementIdentifier, 12).position(Position.fixed(position.add(1, 0))).size(Size.fixed(Vec2i.of(195, 70))).decorator(Decorator.decorator(Target.typed(SelectorElement.class)))
        );
    }

}
