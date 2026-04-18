package me.combimagnetron.sunscreen.neo.editor.element;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2d;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.cursor.CursorStyle;
import me.combimagnetron.sunscreen.neo.editor.EditorController;
import me.combimagnetron.sunscreen.neo.editor.virtual.VirtualPage;
import me.combimagnetron.sunscreen.neo.element.GenericInteractableModernElement;
import me.combimagnetron.sunscreen.neo.event.UserMoveStateChangeEvent;
import me.combimagnetron.sunscreen.neo.event.UserScrollStateChangeEvent;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.graphic.color.Color;
import me.combimagnetron.sunscreen.neo.graphic.text.Text;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.color.TextColor;
import me.combimagnetron.sunscreen.neo.input.InputHandler;
import me.combimagnetron.sunscreen.neo.input.ListenerReferences;
import me.combimagnetron.sunscreen.neo.input.context.MouseInputContext;
import me.combimagnetron.sunscreen.neo.input.context.ScrollInputContext;
import me.combimagnetron.sunscreen.neo.property.Scale;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.render.engine.context.RenderContext;
import me.combimagnetron.sunscreen.util.helper.HoverHelper;
import me.combimagnetron.sunscreen.util.helper.PropertyHelper;
import me.combimagnetron.sunscreen.util.helper.editor.NameHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MenuPreviewElement extends GenericInteractableModernElement<MenuPreviewElement, Canvas, MenuPreviewElement.MenuPreviewElementListenerReferences> {
    private final static Color GRAY = Color.of(180, 180, 180);
    private final static Color WHITE = Color.of(255, 255, 255);
    private final MenuPreviewElementListenerReferences references = new MenuPreviewElementListenerReferences(this);
    private final EditorController controller;
    private final Map<Entry, Vec2i> renderedPages = new HashMap<>();
    private CursorStyle style = CursorStyle.pointer();
    private Canvas canvas = Canvas.empty(Vec2i.of(2048, 2048));
    private Vec2i lastPos = null;
    private Vec2i center = Vec2i.of(259, 216);
    private int click = 0;
    private boolean leftWasPressed = false;

    protected MenuPreviewElement(@Nullable Identifier identifier, @NotNull EditorController controller) {
        super(identifier);
        this.controller = controller;
        populate();
    }

    private void populate() {
        for (int y = 0; y < canvas.size().y(); y++) {
            for (int x = 0; x < canvas.size().x(); x++) {
                boolean gray = ((x / 7) + (y / 7)) % 2 == 0;
                canvas.color(Vec2i.of(x, y), gray ? GRAY : WHITE);
            }
        }
    }

    private void renderPages() {
        for (VirtualPage page : controller.pages()) {
            Canvas pageCanvas = page.render(controller.context());
            final Entry entry = new Entry(pageCanvas, page.identifier());
            Optional<Entry> exists = renderedPages.keySet().stream().filter(filterEntry -> filterEntry.identifier == page.identifier()).findAny();
            boolean existsBool = exists.isPresent();
            if (page.dirty() && existsBool) {
                Entry entry1 = exists.get();
                Vec2i pos = renderedPages.get(entry1);
                renderedPages.remove(entry1);
                renderedPages.put(entry, pos);
                continue;
            }
            if (existsBool) {
                continue;
            }
            Vec2i pos = findNextSpace(pageCanvas.size());
            renderedPages.put(entry, pos);
        }
    }

    private Vec2i toGlobalPosition(@NotNull Vec2i screenPos) {
        Vec2i elementPos = PropertyHelper.vectorOrThrow(position(), Vec2i.class);
        return center.add(screenPos.x() - elementPos.x(), screenPos.y() - elementPos.y());
    }

    private @Nullable Entry pageAt(@NotNull Vec2i screenPos) {
        Vec2i global = toGlobalPosition(screenPos);
        return renderedPages.entrySet().stream()
            .filter(e -> {Vec2i pos = e.getValue();Vec2i size = e.getKey().canvas().size();
                return global.x() >= pos.x() && global.x() < pos.x() + size.x()
                    && global.y() >= pos.y() && global.y() < pos.y() + size.y();
            })
            .map(Map.Entry::getKey)
            .findFirst().orElse(null);
    }

    private Vec2i findNextSpace(@NotNull Vec2i size) {
        int rowY = 17;
        int rowX = 3;
        int rowHeight = 0;
        for (Map.Entry<Entry, Vec2i> entry : renderedPages.entrySet()) {
            Vec2i pos = entry.getValue();
            Vec2i pageSize = entry.getKey().canvas().size();
            if (pos.y() > rowY) {
                rowY = pos.y();
                rowX = pos.x() + pageSize.x() + 3;
                rowHeight = pageSize.y();
            } else {
                rowX = Math.max(rowX, pos.x() + pageSize.x() + 3);
                rowHeight = Math.max(rowHeight, pageSize.y());
            }
        }
        if (rowX + size.x() > 2048) {
            return Vec2i.of(3, rowY + rowHeight + 17);
        }
        return Vec2i.of(rowX, rowY);
    }

    @Override
    protected void lateInit() {
        super.lateInit();
        InputHandler handler = inputHandler();
        if (handler == null) return;
        handler.subscribe(identifier(), ScrollInputContext.class, this::handleScroll);
        handler.subscribe(identifier(), MouseInputContext.class, this::handleCursor);
    }

    @SuppressWarnings("ConstantConditions")
    private void handleCursor(@NotNull UserMoveStateChangeEvent event) {
        if (event.user() != inputHandler().user()) return;
        final MouseInputContext context = event.context();
        if (lastPos == null && style == CursorStyle.move()) {
            inputHandler().cursor(CursorStyle.pointer());
            style = CursorStyle.pointer();
        }
        Vec2i cursor = context.position();
        Entry entry = pageAt(cursor);

        click--;
        boolean entryExist = entry != null;
        if (click == 1 && entryExist) {
                Vec2i global = toGlobalPosition(cursor);
                Vec2i pagePos = renderedPages.get(entry);
                Vec2i local = global.sub(pagePos.x(), pagePos.y());
                controller.page(entry.identifier()).select(local);
        }

        if (context.leftPressed() && entryExist) {
            Vec2i global = toGlobalPosition(cursor);
            Vec2i pagePos = renderedPages.get(entry);
            Vec2i local = global.sub(pagePos.x(), pagePos.y());
            VirtualPage page = controller.page(entry.identifier());
            if (!leftWasPressed) {
                page.dragStart(local);
            } else {
                page.dragUpdate(local);
            }
        } else if (!context.leftPressed() && entryExist) {
            controller.page(entry.identifier()).dragEnd();
        }

        if (context.leftPressed()) click = 3;
        leftWasPressed = context.leftPressed();

        if (!context.rightPressed()) {
            lastPos = null;
            return;
        }
        if (!HoverHelper.in(this, cursor)) return;
        inputHandler().cursor(CursorStyle.move());
        style = CursorStyle.move();
        if (lastPos != null) {
            final double currentScale = scale().value();
            Vec2d delta = Vec2d.of(cursor.x(), cursor.y()).sub(lastPos.x(), lastPos.y()).mul(currentScale / 1.561);
            center = center.sub(delta.xi(), delta.yi());
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
        final MouseInputContext mouseContext = inputHandler().context(MouseInputContext.class);
        if (!HoverHelper.in(this, mouseContext.position())) return;
        final ScrollInputContext context = event.context();
        final double currentScale = scale().value();
        double newScale;
        if (context.value() == 1f) {
            newScale = currentScale*1.1;
        } else {
            newScale = currentScale/1.1;
        }
        if (newScale <= 0.1) {
            newScale = 0.1;
        }
        if (newScale >= 1.561) {
            newScale = 1.561;
        }
        scale(Scale.fixed(newScale));
    }

    @Override
    public @NonNull MenuPreviewElementListenerReferences listen() {
        return references;
    }

    @Override
    public @NotNull Canvas render(@NonNull Size property, @Nullable RenderContext context) {
        Vec2i size = PropertyHelper.vectorOrThrow(size(), Vec2i.class);
        Canvas rendered = canvas.sub(center, size);
        renderPages();
        for (Map.Entry<Entry, Vec2i> vec2iCanvasEntry : renderedPages.entrySet()) {
            Entry entry = vec2iCanvasEntry.getKey();
            Vec2i pos = vec2iCanvasEntry.getValue();
            Vec2i pageSize = entry.canvas().size();
            int viewX2 = center.x() + size.x();
            int viewY2 = center.y() + size.y();
            int pageX2 = pos.x() + pageSize.x();
            int pageY2 = pos.y() + pageSize.y();
            boolean intersects = pos.x() < viewX2 && pageX2 > center.x()
                && pos.y() < viewY2 && pageY2 > center.y();
            if (!intersects) continue;
            int clipX = Math.max(0, center.x() - pos.x());
            int clipY = Math.max(0, center.y() - pos.y());
            int clipW = Math.min(pageSize.x(), viewX2 - pos.x()) - clipX;
            int clipH = Math.min(pageSize.y(), viewY2 - pos.y()) - clipY;

            Canvas clipped = entry.canvas().sub(Vec2i.of(clipX, clipY), Vec2i.of(clipW, clipH));
            Vec2i screenPos = Vec2i.of(Math.max(0, pos.x() - center.x()), Math.max(0, pos.y() - center.y()));
            Canvas floatyThingText = Text.vanilla("Page \"" + NameHelper.suggestDisplayName(entry.identifier().string()) + "\"").color(TextColor.color(Color.of(255, 255, 255))).render(Size.fixed(Vec2i.of(100, 11)), null).trim();
            Canvas floatyThing = FrameElement.frame(Vec2i.of(15 + floatyThingText.size().x(), 13));
            floatyThing.place(Canvas.resource("editor_assets/pc_icon.png"), Vec2i.of(2, 2));
            floatyThing.place(floatyThingText, Vec2i.of(13, 3));
            int floatyY = (clipped.size().y() < pageSize.y() && clipped.size().y() < size.y())
                ? screenPos.y() + clipped.size().y() + 1
                : screenPos.y() - 14;
            int floatyX = screenPos.x() + 3;
            if (floatyThing.size().x() + 3 > clipped.size().x()) {
                int canvasOverflow = floatyThing.size().x() + 3 - clipped.size().x();
                floatyX = screenPos.x() + Math.max(0, 3 - canvasOverflow);
                canvasOverflow = Math.max(0, canvasOverflow - 3);
                if (canvasOverflow > 0) {
                    floatyThing = floatyThing.sub(Vec2i.of(canvasOverflow, 0), floatyThing.size().sub(canvasOverflow, 0));
                }
            }
            if (clipped.size().x() == pageSize.x() || (center.x() < pos.x())) floatyX -= 3;
            rendered.place(floatyThing, Vec2i.of(floatyX, floatyY));
            rendered.place(clipped, screenPos);
        }
        return rendered;
    }

    public static final class MenuPreviewElementListenerReferences extends ListenerReferences<MenuPreviewElement, MenuPreviewElementListenerReferences> {
        private final @NotNull MenuPreviewElement back;

        public MenuPreviewElementListenerReferences(@NotNull MenuPreviewElement back) {
            this.back = back;
        }

        @Override
        public @NotNull MenuPreviewElement back() {
            return back;
        }

    }

    private record Entry(@NotNull Canvas canvas, @NotNull Identifier identifier) {

    }

}
