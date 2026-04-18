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
import me.combimagnetron.sunscreen.neo.input.InputHandler;
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

import java.util.Objects;

public class SliderElement extends GenericInteractableModernElement<SliderElement, Canvas, SliderElement.PaddingMarginElementListenerReferences> {
    private static final Vec2i SIZE = Vec2i.of(137, 38);
    private final PaddingMarginElementListenerReferences references = new PaddingMarginElementListenerReferences(this);
    private CursorStyle style = CursorStyle.pointer();
    private Vec2i startPos = null;
    private int value = 0;

    public SliderElement(@Nullable Identifier identifier) {
        super(identifier);
        size(Size.fixed(SIZE));
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
        if (!hover) {
            startPos = null;
            return;
        }
        Vec2i position = PropertyHelper.vectorOrThrow(position(), Vec2i.class);
        if (!context.leftPressed()) return;
        if (startPos == null) {
            startPos = cursor;
            value = startPos.x() - position.x();
            return;
        }
        inputHandler().cursor(CursorStyle.resizeHorizontal());
        style = CursorStyle.resizeHorizontal();
        int delta = cursor.x() - startPos.x();
        value += delta;
        value = Math.clamp(value, 0, 100);
        startPos = cursor;
    }

    @Override
    public @NonNull PaddingMarginElementListenerReferences listen() {
        return references;
    }

    @Override
    public @NonNull Canvas render(@NonNull Size property, @Nullable RenderContext context) {
        if (context == null) return Canvas.error(Size.fixed(SIZE));
        final Vec2i size = PropertyHelper.vectorOrThrow(size(), Vec2i.class);
        final Canvas normal = Canvas.resource("normal.png");
        final Canvas inverted = Canvas.resource("inverted.png");
        final Canvas canvas = Canvas.empty(normal.size().add(size.y()));
        canvas.fill(Vec2i.zero(), size, Color.of(13, 13, 13));
        Vec2i tabSize = Vec2i.of(2, size.y() - 2);
        float valueF = (value/100f);
        canvas.place(Canvas.empty(tabSize).fill(Vec2i.zero(), tabSize, Color.of(39, 39, 39)), Vec2i.of((int) (valueF*size.x() + 3), 1));
        canvas.text(Text.basic(String.valueOf(value)).font(Registries.fonts().get(Identifier.of("sunscreen", "font/sunburned"))).fontProperties(FontProperties.properties().baseline(-6)), Vec2i.of(60, 10));
        canvas.place(normal, Vec2i.of(0, size.y()));
        int section = inverted.size ().x() - (int) (valueF * inverted.size().x());
        canvas.place(inverted.sub(Vec2i.of(inverted.size().x() - section, 0), Vec2i.of(section, inverted.size().y())), Vec2i.of(inverted.size().x() - section, size.y()));
        return canvas;
    }

    public static final class PaddingMarginElementListenerReferences extends ListenerReferences<SliderElement, PaddingMarginElementListenerReferences> {
        private final SliderElement back;

        public PaddingMarginElementListenerReferences(
            SliderElement back) {
            this.back = back;
        }

        @Override
        public SliderElement back() {
            return back;
        }

    }

}
