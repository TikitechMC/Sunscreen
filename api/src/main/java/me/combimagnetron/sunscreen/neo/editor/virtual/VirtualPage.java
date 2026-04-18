package me.combimagnetron.sunscreen.neo.editor.virtual;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.ActiveMenu;
import me.combimagnetron.sunscreen.neo.editor.EditorController;
import me.combimagnetron.sunscreen.neo.editor.input.SelectorInputContext;
import me.combimagnetron.sunscreen.neo.element.ModernElement;
import me.combimagnetron.sunscreen.neo.event.UserMoveStateChangeEvent;
import me.combimagnetron.sunscreen.neo.event.UserUpdateSelectorInputEvent;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.graphic.color.Color;
import me.combimagnetron.sunscreen.neo.input.InputHandler;
import me.combimagnetron.sunscreen.neo.page.Page;
import me.combimagnetron.sunscreen.neo.render.engine.context.RenderContext;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class VirtualPage implements VirtualObject<Page> {
    private final Map<Identifier, VirtualElement<?>> children = new LinkedHashMap<>();
    private final Vec2i size;
    private final EditorController controller;
    private final Identifier identifier;
    private final String displayName;
    private Identifier selected;
    private Canvas previous;
    private Canvas current;
    private DragMode dragMode;
    private Vec2i dragStart;
    private Vec2i originalPos;
    private Vec2i originalSize;

    public VirtualPage(@NotNull Vec2i size, @NotNull Identifier identifier, @NotNull String displayName, @NotNull EditorController controller) {
        this.size = size;
        this.identifier = identifier;
        this.displayName = displayName;
        this.controller = controller;
        InputHandler inputHandler = controller.menu().inputHandler();
        inputHandler.subscribe(identifier, SelectorInputContext.class, this::selector);
    }

    private void selector(@NotNull UserUpdateSelectorInputEvent event) {
        if (event.user() != controller.menu().user()) return;
        SelectorInputContext context = event.context();
        if (context == null) return;
        int[] posValues = ArrayUtils.toPrimitive((Integer[]) context.innerContexts().get(Identifier.of("top_right/position/value")).values());
        int[] sizeValues = ArrayUtils.toPrimitive((Integer[]) context.innerContexts().get(Identifier.of("top_right/size/value")).values());
        VirtualElement<?> selectedElement = selected();
        if (selectedElement == null) return;
        Vec2i size = selectedElement.size();
        Vec2i position = selectedElement.position();
        Vec2i newSize = Vec2i.of(sizeValues[0], sizeValues[2]);
        Vec2i newPosition = Vec2i.of(posValues[0], posValues[2]);
        if (!size.equals(newSize)) {
            selectedElement.size(newSize);
        }
        if (!position.equals(newPosition)) {
            selectedElement.position(newPosition);
        }
    }

    public boolean dirty() {
        return !Objects.equals(previous, current);
    }

    public @NotNull Collection<VirtualElement<?>> children() {
        return children.values();
    }

    //this is just crazy, im so sorry if you're reading this
    private DragMode handleAt(@NotNull Vec2i cursor, @NotNull VirtualElement<?> element) {
        Vec2i pos = element.position();
        Vec2i size = element.size();
        int centerX = pos.x() + size.x() / 2;
        int centerY = pos.y() + size.y() / 2;
        boolean left = cursor.x() >= pos.x() - 8 && cursor.x() < pos.x();
        boolean right = cursor.x() > pos.x() + size.x() && cursor.x() <= pos.x() + size.x() + 8;
        boolean top = cursor.y() >= pos.y() - 8 && cursor.y() < pos.y();
        boolean bottom = cursor.y() > pos.y() + size.y() && cursor.y() <= pos.y() + size.y() + 8;
        boolean middleX = cursor.x() >= centerX - 4 && cursor.x() <= centerX + 4;
        boolean middleY = cursor.y() >= centerY - 4 && cursor.y() <= centerY + 4;
        if (top && left) return DragMode.RESIZE_NW;
        if (top && right) return DragMode.RESIZE_NE;
        if (bottom && left) return DragMode.RESIZE_SW;
        if (bottom && right) return DragMode.RESIZE_SE;
        if (top && middleX) return DragMode.RESIZE_N;
        if (bottom && middleX) return DragMode.RESIZE_S;
        if (left && middleY) return DragMode.RESIZE_W;
        if (right && middleY) return DragMode.RESIZE_E;
        return DragMode.MOVE;
    }

    public @NotNull Canvas render(@NotNull RenderContext context) {
        previous = current;
        Canvas canvas = Canvas.empty(size);
        canvas.fill(Vec2i.zero(), size, Color.of(255, 255, 255));
        VirtualElement<?> selectedChild = selected();
        if (selectedChild != null) {
            Vec2i pos = selectedChild.position();
            Vec2i size = selectedChild.size();
            Color border = Color.of(77, 155, 230);
            Color accent = Color.of(72, 74, 119);
            canvas.fill(pos.sub(1, 1), Vec2i.of(size.x() + 2, 1), border);
            canvas.fill(pos.sub(1, 1), Vec2i.of(1, size.y() + 2), border);
            canvas.fill(pos.add(size.x(), -1), Vec2i.of(1, size.y() + 2), border);
            canvas.fill(pos.add(-1, size.y()), Vec2i.of(size.x() + 2, 1), border);

            canvas.fill(pos.sub(2, 2), Vec2i.of(2, 2), accent);
            canvas.fill(pos.add(size.x(), -2), Vec2i.of(2, 2), accent);
            canvas.fill(pos.add(-2, size.y()), Vec2i.of(2, 2), accent);
            canvas.fill(pos.add(size.x(), size.y()), Vec2i.of(2, 2), accent);

            canvas.fill(pos.add(size.x() / 2 - 1, -1), Vec2i.of(2, 1), accent);
            canvas.fill(pos.add(size.x() / 2 - 1, size.y()), Vec2i.of(2, 1), accent);
            canvas.fill(pos.add(-1, size.y() / 2 - 1), Vec2i.of(1, 2), accent);
            canvas.fill(pos.add(size.x(), size.y() / 2 - 1), Vec2i.of(1, 2), accent);
        }
        for (VirtualElement<?> child : children.values()) {
            canvas.place(child.render(size, context), child.position());
        }
        current = canvas;
        return canvas;
    }

    public void dragStart(@NotNull Vec2i cursor) {
        VirtualElement<?> element = selected();
        if (element == null) return;
        dragMode = handleAt(cursor, element);
        dragStart = cursor;
        originalPos = element.position();
        originalSize = element.size();
    }

    //todo: clean this mess up, because what???
    public void dragUpdate(@NotNull Vec2i cursor) {
        VirtualElement<?> element = selected();
        if (element == null || dragStart == null) return;
        Vec2i delta = cursor.sub(dragStart.x(), dragStart.y());
        switch (dragMode) {
            case MOVE -> element.position(originalPos.add(delta.x(), delta.y()));
            case RESIZE_N -> {
                element.position(originalPos.add(0, delta.y()));
                element.size(originalSize.add(0, -delta.y()));
            }
            case RESIZE_S -> element.size(originalSize.add(0, delta.y()));
            case RESIZE_E -> element.size(originalSize.add(delta.x(), 0));
            case RESIZE_W -> {
                element.position(originalPos.add(delta.x(), 0));
                element.size(originalSize.add(-delta.x(), 0));
            }
            case RESIZE_NE -> {
                element.position(originalPos.add(0, delta.y()));
                element.size(originalSize.add(delta.x(), -delta.y()));
            }
            case RESIZE_NW -> {
                element.position(originalPos.add(delta.x(), delta.y()));
                element.size(originalSize.add(-delta.x(), -delta.y()));
            }
            case RESIZE_SE -> element.size(originalSize.add(delta.x(), delta.y()));
            case RESIZE_SW -> {
                element.position(originalPos.add(delta.x(), 0));
                element.size(originalSize.add(-delta.x(), delta.y()));
            }
        }
        Vec2i size = element.size();
        Vec2i position = element.position();
        ActiveMenu menu = controller.menu();
        controller.menu().inputHandler().peek(SelectorInputContext.class,
            old -> old.inner(Identifier.of("top_right/size/value"), new SelectorInputContext.ValueInnerContext(new Integer[]{size.x(), 0, size.y(), 0})),
            menu.user()
        );
        controller.menu().inputHandler().peek(SelectorInputContext.class,
            old -> old.inner(Identifier.of("top_right/position/value"), new SelectorInputContext.ValueInnerContext(new Integer[]{position.x(), 0, position.y(), 0})),
            menu.user()
        );
    }

    public void dragEnd() {
        dragStart = null;
        dragMode = null;
    }

    public @NotNull VirtualPage page(@NotNull VirtualElement<?> element) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int x = random.nextInt(size.x() - element.size().x());
        int y = random.nextInt(size.y() - element.size().y());
        element.position(Vec2i.of(x, y));
        children.put(element.identifier(), element);
        return this;
    }

    public void select(@NotNull Identifier identifier) {
        this.selected = identifier;
    }

    public void select(@NotNull Vec2i cursor) {
        selected = children.entrySet().stream()
            .filter(e -> {
                Vec2i pos = e.getValue().position();
                Vec2i s = e.getValue().size();
                return cursor.x() >= pos.x() && cursor.x() < pos.x() + s.x()
                    && cursor.y() >= pos.y() && cursor.y() < pos.y() + s.y();
            })
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);
    }

    public @Nullable <M extends ModernElement<M, Canvas>> VirtualElement<M> selected() {
        return (VirtualElement<M>) children.get(selected);
    }

    public @NotNull Vec2i size() {
        return size;
    }

    public @NotNull Identifier identifier() {
        return identifier;
    }

    public @NotNull String displayName() {
        return displayName;
    }

    private enum DragMode {
        MOVE, RESIZE_N, RESIZE_S, RESIZE_E, RESIZE_W, RESIZE_NE, RESIZE_NW, RESIZE_SE, RESIZE_SW
    }

}
