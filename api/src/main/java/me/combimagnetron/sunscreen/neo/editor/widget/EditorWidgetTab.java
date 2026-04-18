package me.combimagnetron.sunscreen.neo.editor.widget;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.element.ElementContainer;
import me.combimagnetron.sunscreen.neo.element.GenericInteractableModernElement;
import me.combimagnetron.sunscreen.neo.element.ModernElement;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.graphic.color.Color;
import me.combimagnetron.sunscreen.neo.graphic.text.Text;
import me.combimagnetron.sunscreen.neo.input.InputHandler;
import me.combimagnetron.sunscreen.neo.layout.Layout;
import me.combimagnetron.sunscreen.neo.property.*;
import me.combimagnetron.sunscreen.neo.render.engine.context.RenderContext;
import me.combimagnetron.sunscreen.util.helper.PropertyHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class EditorWidgetTab<M extends ModernElement<M, Canvas>> extends Layout.GroupLayout<M> {
    private final Text displayName;
    private final Position real;
    private boolean hidden = true;

    protected EditorWidgetTab(@NotNull Text displayName, @NotNull Position real) {
        super(Identifier.of(displayName.toString()));
        this.displayName = displayName;
        this.real = real;
    }

    @Override
    public void inputHandler(@NotNull InputHandler handler) {
        for (ModernElement<?, Canvas> elementLike : elements.values()) {
            Vec2i vecPos = PropertyHelper.vectorOrThrow(elementLike.position(), Vec2i.class);
            Vec2i layoutVecPos = PropertyHelper.vectorOrThrow(position(), Vec2i.class);
            Vec2i realVecPos = PropertyHelper.vectorOrThrow(real, Vec2i.class);
            elementLike.position(Position.fixed(realVecPos.add(vecPos).add(0, 14)));
            if (!(elementLike instanceof GenericInteractableModernElement<?,?,?> interactableModernElement)) continue;
            interactableModernElement.inputHandler(handler);
        }
    }

    public void hide() {
        visibility(true);
    }

    public void show() {
        visibility(false);
    }

    private void visibility(boolean visibility) {
        this.hidden = visibility;
        for (ModernElement<?, Canvas> element : elements.values()) {
            element.visibility(Visibility.hidden(visibility));
        }
    }

    public boolean hidden() {
        return hidden;
    }

    @Override
    public @NotNull Canvas render(@NotNull Size property, @Nullable RenderContext context) {
        Vec2i sizeVec = PropertyHelper.vectorOrThrow(size(), Vec2i.class);
        Canvas canvas = Canvas.empty(sizeVec);
        Color outsideFrameColor = Color.of(27, 27,27);//scheme.accent();
        Color backgroundFrameColor = Color.of(13, 13, 13);//scheme.background();
        canvas.fill(Vec2i.zero(), sizeVec, outsideFrameColor);
        canvas.fill(Vec2i.of(1, 1), sizeVec.sub(2), backgroundFrameColor);
        Vec2i layoutPosVec = PropertyHelper.vectorOrThrow(real, Vec2i.class);
        for (ModernElement<?, Canvas> value : elements.values()) {
            Vec2i posVec = PropertyHelper.vectorOrThrow(value.position(), Vec2i.class);
            canvas.place(value.render(property, context), posVec.sub(layoutPosVec).sub(0, 14));
        }
        return canvas;
    }

    public @NotNull Text displayName() {
        return displayName;
    }

    public static @NotNull EditorWidgetTab<?> tab(@NotNull Text displayName, @NotNull Position real) {
        return new EditorWidgetTab<>(displayName, real);
    }

    @Override
    public @NotNull EditorWidgetTab<M> decorator(@NotNull Decorator<?> decorator) {
        return (EditorWidgetTab<M>) super.decorator(decorator);
    }

    @Override
    public @NotNull EditorWidgetTab<M> z(@NotNull Z z) {
        return (EditorWidgetTab<M>) super.z(z);
    }

    @Override
    public @NotNull EditorWidgetTab<M> visibility(@NotNull Visibility visibility) {
        return (EditorWidgetTab<M>) super.visibility(visibility);
    }

    @Override
    public @NotNull EditorWidgetTab<M> scale(@NotNull Scale scale) {
        return (EditorWidgetTab<M>) super.scale(scale);
    }

    @Override
    public @NotNull EditorWidgetTab<M> padding(@NotNull Padding padding) {
        return (EditorWidgetTab<M>) super.padding(padding);
    }

    @Override
    public @NotNull EditorWidgetTab<M> margin(@NotNull Margin margin) {
        return (EditorWidgetTab<M>) super.margin(margin);
    }

    @Override
    public @NotNull EditorWidgetTab<M> position(@NotNull Position position) {
        return (EditorWidgetTab<M>) super.position(position);
    }

    @Override
    public @NotNull EditorWidgetTab<M> size(@NotNull Size size) {
        return (EditorWidgetTab<M>) super.size(size);
    }

    @Override
    public @NotNull <L extends ModernElement<L, Canvas>> EditorWidgetTab<M> add(@NonNull L elementLike) {
        return (EditorWidgetTab<M>) super.add(elementLike);
    }
}
