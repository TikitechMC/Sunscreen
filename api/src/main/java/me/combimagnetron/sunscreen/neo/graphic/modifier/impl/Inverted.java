package me.combimagnetron.sunscreen.neo.graphic.modifier.impl;

import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.graphic.modifier.GraphicModifier;
import me.combimagnetron.sunscreen.neo.graphic.modifier.ModifierContext;
import me.combimagnetron.sunscreen.neo.graphic.modifier.handler.GraphicModifierHandler;
import org.jetbrains.annotations.NotNull;

public record Inverted(@NotNull ModifierContext context) implements GraphicModifier<Void> {

    @Override
    public @NotNull GraphicModifierHandler<Void> handler() {
        return (current, modifier, modifierContext) -> {
            Vec2i size = current.size();
            for (int x = 0; x < size.x(); x++) {
                for (int y = 0; y < size.y(); y++) {
                    int color = current.at(x, y);
                    int inverted = (color & 0xFF000000) | (~color & 0x00FFFFFF);
                    current.colorDirectInteger(x, y, inverted);
                }
            }
        };
    }

    @Override
    public @NotNull Void modifier() {
        return null;
    }

}
