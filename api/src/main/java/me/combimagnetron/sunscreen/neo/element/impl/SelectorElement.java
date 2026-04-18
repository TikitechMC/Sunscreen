package me.combimagnetron.sunscreen.neo.element.impl;

import me.combimagnetron.passport.event.Dispatcher;
import me.combimagnetron.passport.event.EventBus;
import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.cursor.CursorStyle;
import me.combimagnetron.sunscreen.neo.element.GenericInteractableModernElement;
import me.combimagnetron.sunscreen.neo.event.UserClickElementEvent;
import me.combimagnetron.sunscreen.neo.event.UserMoveStateChangeEvent;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.graphic.text.Text;
import me.combimagnetron.sunscreen.neo.input.InputHandler;
import me.combimagnetron.sunscreen.neo.input.ListenerReferences;
import me.combimagnetron.sunscreen.neo.input.context.MouseInputContext;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.property.Visibility;
import me.combimagnetron.sunscreen.neo.render.engine.context.RenderContext;
import me.combimagnetron.sunscreen.neo.theme.decorator.ThemeDecorator;
import me.combimagnetron.sunscreen.util.helper.HoverHelper;
import me.combimagnetron.sunscreen.util.helper.PropertyHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class SelectorElement extends GenericInteractableModernElement<SelectorElement, Canvas, SelectorElement.SelectorListenerReferences> {
    private final SelectorListenerReferences references = new SelectorListenerReferences(this);
    private final List<Text> entries = new LinkedList<>();
    private final int height;
    private CursorStyle style = CursorStyle.pointer();
    private int selected = 0;
    private int hovered = -1;
    private int click = 0;

    public SelectorElement(@NotNull Identifier identifier, @NotNull List<Text> entries, int height) {
        super(identifier);
        this.entries.addAll(entries);
        this.height = height;
    }

    public SelectorElement(@NotNull Identifier identifier, int height) {
        super(identifier);
        this.height = height;
    }

    public @NotNull SelectorElement entry(@NotNull Text text) {
        this.entries.add(text);
        return this;
    }

    public @NotNull SelectorElement select(int entry) {
        this.selected = entry;
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
        if (event.user() != inputHandler().user())
            return;
        MouseInputContext context = event.context();
        Vec2i cursor = context.position();
        Visibility visibility = visibility();
        if (visibility.hide())
            return;
        InputHandler handler = inputHandler();
        if (!HoverHelper.in(this, cursor) && style == CursorStyle.click()) {
            handler.cursor(CursorStyle.pointer());
            style = CursorStyle.pointer();
        }
        if (!HoverHelper.in(this, cursor)) {
            hovered = -1;
            return;
        }
        style = CursorStyle.click();
        handler.cursor(CursorStyle.click());
        Vec2i relative = cursor.sub(position().value());
        int index = indexAt(relative);
        hovered = index;
        if (index == -1)
            return;
        if (context.leftPressed())
            click = 3;
        if (click == 1 && index != selected) {
            selected = index;
            Dispatcher.dispatcher().post(new UserClickElementEvent<>(handler.user(), this, relative));
        }
        if (click > 0)
            click--;
    }

    private int indexAt(@NotNull Vec2i relative) {
        int x = 0;
        for (int i = 0; i < entries.size(); i++) {
            Text text = entries.get(i);
            int width = text.render(Size.fixed(Vec2i.of(100, 20)), null).trim().size().x() + 3;
            if (relative.x() >= x && relative.x() < x + width && relative.y() < height)
                return i;
            x += width;
        }
        return -1;
    }

    public int selected() {
        return selected;
    }

    @Override
    public @NotNull SelectorListenerReferences listen() {
        return references;
    }

    @Override
    public @NotNull Canvas render(@NotNull Size property, @Nullable RenderContext context) {
        Size size = size();
        if (context == null)
            return Canvas.error(size);
        ThemeDecorator themeDecorator = context.decorator(this);
        if (!(themeDecorator instanceof ThemeDecorator.StateNineSliceThemeDecorator decorator))
            return Canvas.error(size);
        Vec2i sizeVec = PropertyHelper.vectorOrThrow(size, Vec2i.class);
        Canvas canvas = Canvas.empty(sizeVec);
        int x = 0;
        for (int i = 0; i < entries.size(); i++) {
            Text text = entries.get(i);
            Canvas renderedText = text.render(Size.fixed(Vec2i.of(100, 20)), null).trim();
            int width = renderedText.size().x() + 4;
            Size entrySize = Size.fixed(Vec2i.of(width, height));
            ElementPhase phase = phase(i);
            Canvas entry = decorator.render(entrySize, context, phase);
            entry.place(renderedText, Vec2i.of(2, 2));
            canvas.place(entry, Vec2i.of(x, 0));
            x += width + 1;
        }
        return canvas;
    }

    private @NotNull ElementPhase phase(int index) {
        if (index == selected)
            return ElementPhase.CLICK;
        if (index == hovered)
            return ElementPhase.HOVER;
        return ElementPhase.DEFAULT;
    }

    public static final class SelectorListenerReferences extends ListenerReferences<SelectorElement, SelectorListenerReferences> {
        private final SelectorElement element;

        public SelectorListenerReferences(SelectorElement element) {
            this.element = element;
        }

        @Override
        public @NotNull SelectorElement back() {
            return element;
        }

        @SuppressWarnings("unchecked")
        public @NotNull SelectorListenerReferences select(@NotNull Consumer<UserClickElementEvent<?>> eventConsumer) {
            put(UserClickElementEvent.class, eventConsumer);
            return this;
        }

        public SelectorElement element() {
            return element;
        }

    }

}