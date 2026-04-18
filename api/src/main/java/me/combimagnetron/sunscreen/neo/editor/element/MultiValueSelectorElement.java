package me.combimagnetron.sunscreen.neo.editor.element;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.cursor.CursorStyle;
import me.combimagnetron.sunscreen.neo.editor.input.SelectorInputContext;
import me.combimagnetron.sunscreen.neo.element.GenericInteractableModernElement;
import me.combimagnetron.sunscreen.neo.event.UserMoveStateChangeEvent;
import me.combimagnetron.sunscreen.neo.event.UserUpdateSelectorInputEvent;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.graphic.color.Color;
import me.combimagnetron.sunscreen.neo.graphic.shape.Shape;
import me.combimagnetron.sunscreen.neo.graphic.text.Text;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.font.FontProperties;
import me.combimagnetron.sunscreen.neo.input.InputHandler;
import me.combimagnetron.sunscreen.neo.input.ListenerReferences;
import me.combimagnetron.sunscreen.neo.input.context.MouseInputContext;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.property.Visibility;
import me.combimagnetron.sunscreen.neo.registry.Registries;
import me.combimagnetron.sunscreen.neo.render.engine.context.RenderContext;
import me.combimagnetron.sunscreen.util.FileProvider;
import me.combimagnetron.sunscreen.util.helper.HoverHelper;
import me.combimagnetron.sunscreen.util.helper.PropertyHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class MultiValueSelectorElement extends GenericInteractableModernElement<MultiValueSelectorElement, Canvas, MultiValueSelectorElement.MultiValueSelectorElemenListenerReferences> {
    private static final Vec2i SIZE = Vec2i.of(137, 38);
    private static final Canvas SELECTED_PANES = Canvas.file(FileProvider.resource().find("panes_selected.png").toPath());
    private final MultiValueSelectorElemenListenerReferences references = new MultiValueSelectorElemenListenerReferences(this);
    private CursorStyle style = CursorStyle.pointer();
    private Vec2i startPos = null;
    private Section previous = null;
    private Section hovered = null;
    private int[] values = new int[8];

    protected MultiValueSelectorElement(@Nullable Identifier identifier) {
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
        Visibility visibility = visibility();
        if (visibility.hide()) return;
        Vec2i cursor = context.position();
        boolean hover = HoverHelper.in(this, cursor);
        if (hovered == null && style != CursorStyle.pointer()) {
            inputHandler().cursor(CursorStyle.pointer());
            style = CursorStyle.pointer();
        }
        if (!hover) {
            hovered = null;
            startPos = null;
            return;
        }
        Vec2i position = PropertyHelper.vectorOrThrow(position(), Vec2i.class);
        Vec2i relativePos = cursor.sub(position);
        previous = hovered;
        hovered = Section.at(relativePos);
        if (hovered != previous) {
            startPos = null;
        }
        if (startPos == null) {
            startPos = cursor;
            return;
        }
        if (!context.leftPressed()) return;
        inputHandler().cursor(CursorStyle.resizeHorizontal());
        style = CursorStyle.resizeHorizontal();
        if (hovered == null) return;
        int ordinal = hovered.ordinal();
        int delta = startPos.x() - cursor.x();
        values[ordinal] -= delta;
        startPos = cursor;
        inputHandler().peek(SelectorInputContext.class, old -> old.inner(identifier(), new SelectorInputContext.MultiValueInnerContext(ArrayUtils.toObject(values))), event.user());
    }

    @Override
    public @NotNull MultiValueSelectorElement.MultiValueSelectorElemenListenerReferences listen() {
        return references;
    }

    @Override
    public @NotNull Canvas render(@NotNull Size property, @Nullable RenderContext context) {
        Vec2i size = PropertyHelper.vectorOrThrow(size(), Vec2i.class);
        if (context == null) return Canvas.error(Size.fixed(size));
        int dynamicWidth = size.x();
        final Color top = Color.of(133, 133, 133);//context.theme().colorScheme()
        final Color mid = Color.of(61, 61, 61);
        final Color low = Color.of(39, 39, 39);
        final Color other = Color.of(27, 27, 27);
        final Canvas canvas = Canvas.empty(SIZE);
        canvas.fill(Vec2i.of(1, 1), Vec2i.of(135, 7), top);
        canvas.fill(Vec2i.of(1, 8), Vec2i.of(14, 22), mid);
        canvas.fill(Vec2i.of(122, 8), Vec2i.of(14, 22), mid);
        canvas.fill(Vec2i.of(1, 31), Vec2i.of(135, 6), low);
        canvas.fill(Vec2i.of(15, 8), Vec2i.of(107, 23), other);
        canvas.fill(Vec2i.of(16, 9), Vec2i.of(105, 7), low);
        canvas.fill(Vec2i.of(16, 23), Vec2i.of(105, 7), top);
        canvas.fill(Vec2i.of(16, 16), Vec2i.of(14, 7), mid);
        canvas.fill(Vec2i.of(107, 16), Vec2i.of(14, 7), mid);
        canvas.shape(Shape.filledLine(Vec2i.of(0, 0), Vec2i.of(13, 6)), mid, Vec2i.of(1, 1));
        canvas.shape(Shape.filledLine(Vec2i.of(0, 6), Vec2i.of(13, 0), true), mid, Vec2i.of(1, 30));
        canvas.shape(Shape.filledLine(Vec2i.of(0, 6), Vec2i.of(13, 0)), mid, Vec2i.of(122, 1));
        canvas.shape(Shape.filledLine(Vec2i.of(0, 0), Vec2i.of(13, 6), true), mid, Vec2i.of(122, 30));
        canvas.shape(Shape.filledLine(Vec2i.of(0, 0), Vec2i.of(13, 6)), mid, Vec2i.of(16, 9));
        canvas.shape(Shape.filledLine(Vec2i.of(0, 6), Vec2i.of(13, 0), true), mid, Vec2i.of(16, 23));
        canvas.shape(Shape.filledLine(Vec2i.of(0, 6), Vec2i.of(13, 0)), mid, Vec2i.of(107, 9));
        canvas.shape(Shape.filledLine(Vec2i.of(0, 0), Vec2i.of(13, 6), true), mid, Vec2i.of(107, 23));
        if (hovered != null) canvas.place(hovered.overlay, hovered.position);
        canvas.text(Text.basic("(Content)").font(Registries.fonts().get(Identifier.of("sunscreen", "font/sunburned"))).fontProperties(FontProperties.properties().baseline(-6)), Vec2i.of(48, 17));
        canvas.text(Text.basic(String.valueOf(values[0])).font(Registries.fonts().get(Identifier.of("sunscreen", "font/sunburned"))).fontProperties(FontProperties.properties().baseline(-6)), Vec2i.of(60, 2));
        canvas.text(Text.basic(String.valueOf(values[1])).font(Registries.fonts().get(Identifier.of("sunscreen", "font/sunburned"))).fontProperties(FontProperties.properties().baseline(-6)), Vec2i.of(60, 10));
        canvas.text(Text.basic(String.valueOf(values[6])).font(Registries.fonts().get(Identifier.of("sunscreen", "font/sunburned"))).fontProperties(FontProperties.properties().baseline(-6)), Vec2i.of(126, 17));
        canvas.text(Text.basic(String.valueOf(values[7])).font(Registries.fonts().get(Identifier.of("sunscreen", "font/sunburned"))).fontProperties(FontProperties.properties().baseline(-6)), Vec2i.of(112, 17));
        canvas.text(Text.basic(String.valueOf(values[4])).font(Registries.fonts().get(Identifier.of("sunscreen", "font/sunburned"))).fontProperties(FontProperties.properties().baseline(-6)), Vec2i.of(6, 17));
        canvas.text(Text.basic(String.valueOf(values[5])).font(Registries.fonts().get(Identifier.of("sunscreen", "font/sunburned"))).fontProperties(FontProperties.properties().baseline(-6)), Vec2i.of(20, 17));
        canvas.text(Text.basic(String.valueOf(values[3])).font(Registries.fonts().get(Identifier.of("sunscreen", "font/sunburned"))).fontProperties(FontProperties.properties().baseline(-6)), Vec2i.of(60, 24));
        canvas.text(Text.basic(String.valueOf(values[2])).font(Registries.fonts().get(Identifier.of("sunscreen", "font/sunburned"))).fontProperties(FontProperties.properties().baseline(-6)), Vec2i.of(60, 31));
        return canvas;
    }

    public static final class MultiValueSelectorElemenListenerReferences extends ListenerReferences<MultiValueSelectorElement, MultiValueSelectorElemenListenerReferences> {
        private final MultiValueSelectorElement back;

        public MultiValueSelectorElemenListenerReferences(MultiValueSelectorElement back) {
            this.back = back;
        }

        @Override
        public MultiValueSelectorElement back() {
            return back;
        }

        public @NotNull MultiValueSelectorElemenListenerReferences selector(@NotNull Consumer<UserUpdateSelectorInputEvent> consumer) {
            put(UserUpdateSelectorInputEvent.class, consumer);
            return this;
        }

    }

    public enum Section {
        TOP_OUTSIDE(Vec2i.of(2, 0), SELECTED_PANES.sub(Vec2i.of(0, 0), Vec2i.of(133, 9))),
        TOP_INSIDE(Vec2i.of(17, 8), SELECTED_PANES.sub(Vec2i.of(0, 55), Vec2i.of(103, 9))),
        BOTTOM_OUTSIDE(Vec2i.of(2, 30), SELECTED_PANES.sub(Vec2i.of(0, 9), Vec2i.of(133, 8))),
        BOTTOM_INSIDE(Vec2i.of(17, 22), SELECTED_PANES.sub(Vec2i.of(0, 64), Vec2i.of(103, 9))),
        LEFT_OUTSIDE(Vec2i.of(0, 0), SELECTED_PANES.sub(Vec2i.of(48, 17), Vec2i.of(16, 38))),
        LEFT_INSIDE(Vec2i.of(15, 8), SELECTED_PANES.sub(Vec2i.of(16, 17), Vec2i.of(16, 23))),
        RIGHT_OUTSIDE(Vec2i.of(121, 0), SELECTED_PANES.sub(Vec2i.of(32, 17), Vec2i.of(16, 38))),
        RIGHT_INSIDE(Vec2i.of(106, 8), SELECTED_PANES.sub(Vec2i.of(0, 17), Vec2i.of(16, 23)));

        private final Vec2i position;
        private final Canvas overlay;
        Section(Vec2i position, Canvas overlay) {
            this.position = position;
            this.overlay = overlay;
        }

        boolean contains(Vec2i point) {
            Vec2i size = overlay.size();
            return point.x() >= position.x() && point.x() < position.x() + size.x() &&
                point.y() >= position.y() && point.y() < position.y() + size.y();
        }

        static Section at(Vec2i point) {
            for (Section section : values()) {
                if (section.contains(point)) return section;
            }
            return null;
        }

    }

}
