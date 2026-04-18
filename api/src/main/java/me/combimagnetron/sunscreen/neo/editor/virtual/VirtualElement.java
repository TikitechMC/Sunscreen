package me.combimagnetron.sunscreen.neo.editor.virtual;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.element.ElementContainer;
import me.combimagnetron.sunscreen.neo.element.ModernElement;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.property.PropertyMap;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.render.engine.context.RenderContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VirtualElement<M extends ModernElement<M, Canvas>> implements VirtualObject<M> {
    private final PropertyMap propertyMap = new PropertyMap();
    private final List<VirtualElement<?>> children = new ArrayList<>();
    private Vec2i position;
    private final M target;
    private final Identifier identifier;
    private final boolean isElementGroup;

    public VirtualElement(Vec2i position, @UnknownNullability ModernElement<?, Canvas> target, Identifier identifier) {
        this.position = position;
        this.target = (M) target;
        this.identifier = identifier;
        this.isElementGroup = ElementContainer.class.isAssignableFrom(target.getClass());

    }

    public @NotNull VirtualElement<M> add(@NotNull VirtualElement<?> child) {
        if (!isElementGroup) throw new IllegalArgumentException("May only add a child element to element groups!");
        this.children.add(child);
        return this;
    }

    public @NotNull Canvas render(@NotNull Vec2i size, @NotNull RenderContext context) {
        Canvas canvas = target.render(Size.fixed(size), context);
        for (VirtualElement<?> child : children) {
            canvas.place(child.render(size, context), child.position);
        }
        //System.out.println(canvas + " " + canvas.size() + " " + Arrays.toString(canvas.bufferedColorSpace().buffer()));
        return canvas;
    }

    public @NotNull Identifier identifier() {
        return identifier;
    }

    public @NotNull Vec2i size() {
        return target.size().value();
    }

    public @NotNull Vec2i position() {
        return position;
    }

    public @NotNull VirtualElement<M> position(@NotNull Vec2i position) {
        this.position = position;
        return this;
    }

    public @NotNull VirtualElement<M> size(@NotNull Vec2i size) {
        target.size(Size.fixed(size));
        return this;
    }

    public @NotNull M target() {
        return target;
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public boolean isElementGroup() {
        return isElementGroup;
    }

}
