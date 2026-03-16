package me.combimagnetron.sunscreen.neo.editor.element;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.cursor.CursorStyle;
import me.combimagnetron.sunscreen.neo.element.GenericInteractableModernElement;
import me.combimagnetron.sunscreen.neo.event.UserMoveStateChangeEvent;
import me.combimagnetron.sunscreen.neo.event.UserScrollStateChangeEvent;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.graphic.color.Color;
import me.combimagnetron.sunscreen.neo.input.ListenerReferences;
import me.combimagnetron.sunscreen.neo.input.context.MouseInputContext;
import me.combimagnetron.sunscreen.neo.input.context.ScrollInputContext;
import me.combimagnetron.sunscreen.neo.property.Scale;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.render.engine.context.RenderContext;
import me.combimagnetron.sunscreen.util.helper.HoverHelper;
import me.combimagnetron.sunscreen.util.helper.PropertyHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class MenuPreviewElement extends GenericInteractableModernElement<MenuPreviewElement, Canvas, MenuPreviewElement.MenuPreviewElementListenerReferences> {
    private final static Color GRAY = Color.of(180, 180, 180);
    private final static Color WHITE = Color.of(255, 255, 255);
    private final MenuPreviewElementListenerReferences references = new MenuPreviewElementListenerReferences(this);
    private CursorStyle style = CursorStyle.pointer();
    private Canvas canvas;
    private Vec2i lastPos = null;
    private Vec2i center = Vec2i.of(940, 940);

    public MenuPreviewElement(@Nullable Identifier identifier) {
        super(identifier);
        Vec2i size = Vec2i.of(2048, 2048);
        canvas = Canvas.empty(size);
        for (int y = 0; y < size.y(); y++) {
            for (int x = 0; x < size.x(); x++) {
                boolean gray = ((x / 7) + (y / 7)) % 2 == 0;
                canvas.color(Vec2i.of(x, y), gray ? GRAY : WHITE);
            }
        }
        canvas.place(Canvas.resource("normal.png"), Vec2i.of(1024, 1024));
    }

    @Override
    protected void lateInit() {
        super.lateInit();
        input(ScrollInputContext.class).listen(this::handleScroll);
        input(MouseInputContext.class).listen(this::handleCursor);
    }

    @SuppressWarnings("ConstantConditions")
    private void handleCursor(@NotNull UserMoveStateChangeEvent event) {
        if (event.user() != inputHandler().user()) return;
        final MouseInputContext context = event.context();
        if (lastPos == null && style == CursorStyle.move()) {
            inputHandler().cursor(CursorStyle.pointer());
            style = CursorStyle.pointer();
        }
        if (!context.leftPressed()) {
            lastPos = null;
            return;
        }
        Vec2i cursor = context.position();
        if (!HoverHelper.in(this, cursor)) return;
        inputHandler().cursor(CursorStyle.move());
        style = CursorStyle.move();
        if (lastPos != null) {
            Vec2i delta = cursor.sub(lastPos);
            center = center.sub(delta);
            Vec2i size = PropertyHelper.vectorOrThrow(size(), Vec2i.class);
            Vec2i canvasSize = canvas.size();
            center = Vec2i.of(
                Math.clamp(center.x(), 0, canvasSize.x() - size.x()),
                Math.clamp(center.y(), 0, canvasSize.y() - size.y())
            );
        }
        lastPos = cursor;
    }

    @SuppressWarnings("ConstantConditions")
    private void handleScroll(@NotNull UserScrollStateChangeEvent event) {
        if (event.user() != inputHandler().user()) return;
        final ScrollInputContext context = event.context();
        final double currentScale = scale().value();
        double newScale;
        if (context.value() == 1f) {
            newScale = currentScale*1.010;
        } else {
            newScale = currentScale/1.010;
        }
        if (newScale <= 0.1f) {
            newScale = 0.1f;
        }
        if (newScale >= 1.561f) {
            newScale = 1.561f;
        }
        scale(Scale.fixed(newScale));
    }

    @Override
    public @NonNull MenuPreviewElementListenerReferences listen() {
        return references;
    }

    @Override
    public @NonNull Canvas render(@NonNull Size property, @Nullable RenderContext context) {
        Vec2i size = PropertyHelper.vectorOrThrow(size(), Vec2i.class);
        return canvas.sub(center, size);
    }

    public record MenuPreviewElementListenerReferences(@NotNull MenuPreviewElement back) implements ListenerReferences<MenuPreviewElement, MenuPreviewElementListenerReferences> {

    }

}
