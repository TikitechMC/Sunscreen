package me.combimagnetron.sunscreen.neo.graphic.shape;

import me.combimagnetron.passport.util.math.Vec2i;
import org.jetbrains.annotations.NotNull;

import java.util.BitSet;

public class Rectangle implements Shape {
    private final BitSet shape = new BitSet();
    private final Vec2i size;

    protected Rectangle(Vec2i size) {
        this.size = size;
        for (int y = 0; y < size.y(); y++) {
            for (int x = 0; x < size.x(); x++) {
                shape.set(y * size.x() + x);
            }
        }
    }

    @Override
    public @NotNull Vec2i squareSize() {
        return size;
    }

    @Override
    public @NotNull BitSet shape() {
        return shape;
    }
}
