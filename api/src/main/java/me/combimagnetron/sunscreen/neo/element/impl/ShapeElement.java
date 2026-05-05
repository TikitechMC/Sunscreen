package me.combimagnetron.sunscreen.neo.element.impl;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.element.GenericModernElement;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.graphic.color.Color;
import me.combimagnetron.sunscreen.neo.graphic.shape.Shape;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.render.engine.context.RenderContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShapeElement extends GenericModernElement<ShapeElement, Canvas> {
    private final Color color;
    private final Shape shape;

    public ShapeElement(@NotNull Identifier identifier, @NotNull Shape shape, @NotNull Color color) {
        super(identifier);
        this.color = color;
        this.shape = shape;
    }

    public ShapeElement(@NotNull Identifier identifier, @NotNull Shape shape) {
        this(identifier, shape, Color.of(255, 255, 255));
    }

    public @NotNull Shape shape() {
        return shape;
    }

    @Override
    public @Nullable Vec2i intrinsicSize() {
        return shape.squareSize();
    }

    @Override
    public @NotNull Canvas render(@NotNull Size property, @Nullable RenderContext context) {
        Canvas canvas = Canvas.empty(shape.squareSize());
        canvas.shape(shape, color);
        return canvas;
    }

}
