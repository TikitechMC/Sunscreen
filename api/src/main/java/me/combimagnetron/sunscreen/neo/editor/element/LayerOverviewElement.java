package me.combimagnetron.sunscreen.neo.editor.element;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.cursor.CursorStyle;
import me.combimagnetron.sunscreen.neo.editor.EditorController;
import me.combimagnetron.sunscreen.neo.editor.virtual.VirtualElement;
import me.combimagnetron.sunscreen.neo.editor.virtual.VirtualPage;
import me.combimagnetron.sunscreen.neo.element.GenericInteractableModernElement;
import me.combimagnetron.sunscreen.neo.event.UserMoveStateChangeEvent;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.graphic.color.Color;
import me.combimagnetron.sunscreen.neo.graphic.text.Text;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.color.TextColor;
import me.combimagnetron.sunscreen.neo.input.InputHandler;
import me.combimagnetron.sunscreen.neo.input.ListenerReferences;
import me.combimagnetron.sunscreen.neo.input.context.MouseInputContext;
import me.combimagnetron.sunscreen.neo.property.Position;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.render.engine.context.RenderContext;
import me.combimagnetron.sunscreen.util.helper.HoverHelper;
import me.combimagnetron.sunscreen.util.helper.PropertyHelper;
import me.combimagnetron.sunscreen.util.helper.editor.NameHelper;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;

public class LayerOverviewElement extends GenericInteractableModernElement<LayerOverviewElement, Canvas, LayerOverviewElement.LayerOverviewElementListenerReferences> {
    private final LayerOverviewElementListenerReferences references = new LayerOverviewElementListenerReferences(this);
    private final EditorController controller;
    private boolean unfolded = false;
    private CursorStyle cursorStyle = CursorStyle.pointer();
    private int click = 0;
    private int selected = 0;
    private int hardSelected = -1;

    protected LayerOverviewElement(@NotNull Identifier identifier, @NotNull EditorController controller) {
        super(identifier);
        this.controller = controller;
    }

    @Override
    protected void lateInit() {
        super.lateInit();
        final InputHandler inputHandler = inputHandler();
        if (inputHandler == null) return;
        inputHandler.subscribe(identifier(), MouseInputContext.class, this::handleCursor);
    }

    private void handleCursor(@NotNull UserMoveStateChangeEvent event) {
        if (event.user() != inputHandler().user()) return;
        MouseInputContext context = event.context();
        Vec2i cursor = context.position();
        boolean in = HoverHelper.in(this, cursor);
        click--;
        if (click == 1) {
            if (hardSelected != -1 && selected == hardSelected) {
                unfolded = !unfolded;
            }
            hardSelected = selected;
            controller.select(selected);
        }
        if (!in && selected >= 0) {
            selected = -1;
            return;
        }
        if (!in && cursorStyle == CursorStyle.click()) {
            inputHandler().cursor(CursorStyle.pointer());
            cursorStyle = CursorStyle.pointer();
            return;
        }
        if (!in) {
            return;
        }
        Vec2i posVec = PropertyHelper.vectorOrThrow(position(), Vec2i.class);
        int relativeY = cursor.y() - posVec.y() - 2;
        int visualIndex = relativeY / 21;
        selected = visualIndex;
        if (unfolded && hardSelected != -1) {
            int childCount = controller.pages().get(hardSelected).children().size();
            if (visualIndex > hardSelected && visualIndex <= hardSelected + childCount) {
                selected = hardSelected;
            } else if (visualIndex > hardSelected + childCount) {
                selected = visualIndex - childCount;
            }
        }
        if (selected > controller.pages().size() - 1) {
            selected = -1;
            inputHandler().cursor(CursorStyle.pointer());
            cursorStyle = CursorStyle.pointer();
            return;
        }
        inputHandler().cursor(CursorStyle.click());
        cursorStyle = CursorStyle.click();
        if (!context.leftPressed()) return;
        click = 3;
    }

    @Override
    public @NotNull LayerOverviewElementListenerReferences listen() {
        return references;
    }

    @Override
    public @NotNull Canvas render(@NotNull Size property, @Nullable RenderContext context) {
        Vec2i sizeVec = PropertyHelper.vectorOrThrow(size(), Vec2i.class);
        Canvas canvas = Canvas.empty(sizeVec);
        int pageY = 0;
        int pageIndex = 0;
        for (VirtualPage page : controller.pages()) {
            canvas.place(renderPage(page, sizeVec), Vec2i.of(2, 2 + pageY));
            if (unfolded && pageIndex == hardSelected) {
                pageY += 21;
                for (VirtualElement<?> child : page.children()) {
                    canvas.place(renderEntry(child, sizeVec, 0), Vec2i.of(6, 2 + pageY));
                    pageY += 21;
                }
            } else {
                pageY += 21;
            }
            pageIndex++;
        }
        if (selected != -1) {
            fill(canvas, selected, sizeVec);
        }
        if (hardSelected != -1) {
            fill(canvas, hardSelected, sizeVec);
        }
        return canvas;
    }

    private void fill(@NotNull Canvas canvas, int index, @NotNull Vec2i sizeVec) {
        canvas.fill(Vec2i.of(1, 1 + index * 21), Vec2i.of(sizeVec.x() - 2, 1), Color.of(255, 255, 255));
        canvas.fill(Vec2i.of(1, 1 + index * 21 + 22), Vec2i.of(sizeVec.x() - 2, 1), Color.of(255, 255, 255));
        canvas.fill(Vec2i.of(1, 1 + index * 21), Vec2i.of(1, 23), Color.of(255, 255, 255));
        canvas.fill(Vec2i.of(sizeVec.x() - 2, 1 + index * 21), Vec2i.of(1, 23), Color.of(255, 255, 255));
    }

    private @NotNull Canvas renderPage(@NotNull VirtualPage page, @NotNull Vec2i size) {
        Vec2i realSize = Vec2i.of(size.x() - 4, 21);
        Canvas canvas = Canvas.empty(realSize);
        canvas.fill(Vec2i.zero(), realSize, Color.of(39, 39, 39));
        canvas.fill(Vec2i.of(1, 1), Vec2i.of(19, 19), Color.of(61, 61, 61));
        final TextColor smallColor = TextColor.color(Color.of(133, 133, 133));
        canvas.text(Text.small(page.children().size() + " Elements").color(smallColor), Vec2i.of(21, 9));
        Vec2i pageSize = page.size();
        canvas.text(Text.small(pageSize.x() + "x" + pageSize.y() + "px").color(smallColor), Vec2i.of(21, 15));
        canvas.text(Text.vanilla(page.displayName()).color(TextColor.color(Color.of(180, 180, 180))), Vec2i.of(21, 1));
        return canvas;
    }

    private @NotNull Canvas renderEntry(@NotNull VirtualElement<?> element, @NotNull Vec2i size, int priority) {
        Vec2i realSize = Vec2i.of(size.x() - 8, 21);
        Canvas canvas = Canvas.empty(realSize);
        canvas.fill(Vec2i.zero(), realSize, Color.of(27, 27, 27));
        canvas.fill(Vec2i.of(1, 1), Vec2i.of(19, 19), Color.of(39, 39, 39));
        final TextColor smallColor = TextColor.color(Color.of(133, 133, 133));
        canvas.text(Text.small(String.join(" ", element.target().getClass().getSimpleName().split("(?=[A-Z])"))).color(smallColor), Vec2i.of(21, 9));
        Vec2i elementSize = element.size();
        canvas.text(Text.small(elementSize.x() + "x" + elementSize.y() + "px").color(smallColor), Vec2i.of(21, 15));
        canvas.text(Text.vanilla(NameHelper.suggestDisplayName(element.identifier().string())).color(TextColor.color(Color.of(180, 180, 180))), Vec2i.of(21, 1));
        return canvas;
    }

    public static final class LayerOverviewElementListenerReferences extends ListenerReferences<LayerOverviewElement, LayerOverviewElementListenerReferences> {
        private final @NotNull LayerOverviewElement back;

        public LayerOverviewElementListenerReferences(@NotNull LayerOverviewElement back) {
            this.back = back;
        }

        @Override
        public @NotNull LayerOverviewElement back() {
            return back;
        }

    }

}