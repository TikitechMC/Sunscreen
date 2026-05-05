package me.combimagnetron.sunscreen.neo.element.impl;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.cursor.CursorStyle;
import me.combimagnetron.sunscreen.neo.element.GenericInteractableModernElement;
import me.combimagnetron.sunscreen.neo.event.UserMoveStateChangeEvent;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.graphic.color.Color;
import me.combimagnetron.sunscreen.neo.input.InputHandler;
import me.combimagnetron.sunscreen.neo.input.ListenerReferences;
import me.combimagnetron.sunscreen.neo.input.context.MouseInputContext;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.render.engine.context.RenderContext;
import me.combimagnetron.sunscreen.util.helper.HoverHelper;
import me.combimagnetron.sunscreen.util.helper.PropertyHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public class ComparisonElement extends GenericInteractableModernElement<ComparisonElement, Canvas, ComparisonElement.ComparisonElementListenerReferences> {
    private final ComparisonElementListenerReferences references = new ComparisonElementListenerReferences(this);
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
        InputHandler handler = inputHandler();
        if (handler == null) return;
        references.subscribe(handler);
        handler.subscribe(identifier(), MouseInputContext.class, this::handleCursor);
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
    public @NotNull ComparisonElement.ComparisonElementListenerReferences listen() {
        return references;
    }

    @Override
    public @NonNull Canvas render(@NonNull Size property, @Nullable RenderContext context) {
        if (context == null) return Canvas.error(size());
        if (left == null || right == null) return Canvas.error(size());
        final Vec2i targetSize = PropertyHelper.vectorOrThrow(size(), Vec2i.class);
        if (targetSize.x() <= 0 || targetSize.y() <= 0) return Canvas.error(size());
        final Canvas leftCanvas = left.size().equals(targetSize)
            ? left
            : new Canvas(left.bufferedColorSpace().resize(targetSize), left.classpathResource());
        final Canvas rightCanvas = right.size().equals(targetSize)
            ? right
            : new Canvas(right.bufferedColorSpace().resize(targetSize), right.classpathResource());
        final Canvas canvas = Canvas.empty(targetSize);
        final Vec2i size = targetSize;
        Vec2i tabSize = Vec2i.of(4, size.y());
        float valueF = (value/100f);
        canvas.place(leftCanvas, Vec2i.zero());
        int section = rightCanvas.size().x() - (int) (valueF * rightCanvas.size().x());
        canvas.place(
            rightCanvas.sub(Vec2i.of(rightCanvas.size().x() - section, 0), Vec2i.of(section, rightCanvas.size().y())),
            Vec2i.of(rightCanvas.size().x() - section, 0)
        );
        int tabX = Math.clamp((int) (valueF * size.x() - 2), 0, Math.max(0, size.x() - tabSize.x()));
        canvas.place(Canvas.empty(tabSize).fill(Vec2i.zero(), tabSize, Color.of(39, 39, 39)), Vec2i.of(tabX, 1));
        return canvas;
    }

    public static final class ComparisonElementListenerReferences extends ListenerReferences<ComparisonElement, ComparisonElementListenerReferences> {
        private final ComparisonElement back;

        public ComparisonElementListenerReferences(ComparisonElement back) {
            this.back = back;
        }

        @Override
        public ComparisonElement back() {
            return back;
        }

    }

}
