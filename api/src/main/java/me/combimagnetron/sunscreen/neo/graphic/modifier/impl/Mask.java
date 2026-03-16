package me.combimagnetron.sunscreen.neo.graphic.modifier.impl;

import me.combimagnetron.sunscreen.neo.graphic.modifier.GraphicModifier;
import me.combimagnetron.sunscreen.neo.graphic.modifier.ModifierContext;
import me.combimagnetron.sunscreen.neo.graphic.modifier.handler.GraphicModifierHandler;
import me.combimagnetron.sunscreen.neo.graphic.shape.Shape;
import me.combimagnetron.sunscreen.neo.property.Position;
import me.combimagnetron.passport.util.math.Vec2i;
import org.jetbrains.annotations.NotNull;

public record Mask(@NotNull Shape modifier, @NotNull ModifierContext context) implements GraphicModifier<Shape> {
    private static final GraphicModifierHandler<Shape> HANDLER = ((current, modifier, modifierContext) -> {
        modifier.shape().stream().parallel().forEach(i -> {
            Vec2i pos = position(i, modifier.squareSize());
            Vec2i position = modifierContext.propOr(Position.class, Position.nil()).value();
            if (i == 0) return;
            current.erase(position.add(pos));
        });
    });

    @Override
    public @NotNull GraphicModifierHandler<Shape> handler() {
        return HANDLER;
    }

    private static Vec2i position(int index, Vec2i size) {
        return Vec2i.of(index % size.x(), index /size.y());
    }

}
