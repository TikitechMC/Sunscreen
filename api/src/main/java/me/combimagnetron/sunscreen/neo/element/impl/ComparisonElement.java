package me.combimagnetron.sunscreen.neo.element.impl;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.cursor.CursorStyle;
import me.combimagnetron.sunscreen.neo.element.GenericInteractableModernElement;
import me.combimagnetron.sunscreen.neo.event.UserMoveStateChangeEvent;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.graphic.color.Color;
import me.combimagnetron.sunscreen.neo.graphic.text.Text;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.font.FontProperties;
import me.combimagnetron.sunscreen.neo.input.ListenerReferences;
import me.combimagnetron.sunscreen.neo.input.context.MouseInputContext;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.registry.Registries;
import me.combimagnetron.sunscreen.neo.render.engine.context.RenderContext;
import me.combimagnetron.sunscreen.util.helper.HoverHelper;
import me.combimagnetron.sunscreen.util.helper.PropertyHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class ComparisonElement extends GenericInteractableModernElement<ComparisonElement, Canvas, ComparisonElement.PaddingMarginElementListenerReferences> {
    private final PaddingMarginElementListenerReferences references = new PaddingMarginElementListenerReferences(this);
    private CursorStyle style = CursorStyle.pointer();
    private Canvas left;
    private Canvas right;
    private float value = 50;

    public ComparisonElement(@NotNull Identifier identifier) {
        super(identifier);
    }

    public ComparisonElement(@NotNull Identifier identifier, @Nullable Canvas left, @Nullable Canvas right) {
        super(identifier);
        this.left = left;
        this.right = right;
    }

    public @NotNull ComparisonElement left(@Nullable Canvas canvas) {
        this.left = canvas;
        return this;
    }

    public @NotNull ComparisonElement right(@Nullable Canvas canvas) {
        this.right = canvas;
        return this;
    }

    @Override
    protected void lateInit() {
        super.lateInit();
        input(MouseInputContext.class).listen(this::handleCursor);
    }

    private void handleCursor(@NotNull UserMoveStateChangeEvent event) {
        if (event.user() != inputHandler().user()) return;
        final MouseInputContext context = event.context();
        Vec2i cursor = context.position();
        boolean hover = HoverHelper.in(this, cursor);

        if (!hover && style != CursorStyle.pointer()) {
            inputHandler().cursor(CursorStyle.pointer());
            style = CursorStyle.pointer();
            return;
        }
        if (!hover) return;

        if (style == CursorStyle.resizeHorizontal() && !event.context().leftPressed()) {
            inputHandler().cursor(CursorStyle.pointer());
            style = CursorStyle.pointer();
        }

        if (!event.context().leftPressed()) return;

        Vec2i position = PropertyHelper.vectorOrThrow(position(), Vec2i.class);
        Vec2i size = PropertyHelper.vectorOrThrow(size(), Vec2i.class);
        inputHandler().cursor(CursorStyle.resizeHorizontal());
        style = CursorStyle.resizeHorizontal();
        float relativeX = cursor.x() - position.x();
        float percentage = (relativeX / (float) size.x()) * 100;

        this.value = Math.clamp((int) percentage, 0, 100);
    }

    @Override
    public @NonNull PaddingMarginElementListenerReferences listen() {
        return references;
    }

    @Override
    public @NonNull Canvas render(@NonNull Size property, @Nullable RenderContext context) {
        if (context == null) return Canvas.error(size());
        if (left == null || right == null || left.size().x() != right.size().x() || left.size().y() != right.size().y()) return Canvas.error(size());
        final Canvas canvas = Canvas.empty(left.size());
        final Vec2i size = left.size();
        Vec2i tabSize = Vec2i.of(4, size.y());
        float valueF = (value/100f);
        canvas.place(left, Vec2i.zero());
        int section = right.size ().x() - (int) (valueF * right.size().x());
        canvas.place(right.sub(Vec2i.of(right.size().x() - section, 0), Vec2i.of(section, right.size().y())), Vec2i.of(right.size().x() - section, 0));
        canvas.place(Canvas.empty(tabSize).fill(Vec2i.zero(), tabSize, Color.of(39, 39, 39)), Vec2i.of((int) (valueF*size.x() - 2), 1));
        return canvas;
    }

    public record PaddingMarginElementListenerReferences(
        ComparisonElement back) implements ListenerReferences<ComparisonElement, PaddingMarginElementListenerReferences> {

    }

}
