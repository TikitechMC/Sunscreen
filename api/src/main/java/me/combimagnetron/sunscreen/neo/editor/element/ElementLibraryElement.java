package me.combimagnetron.sunscreen.neo.editor.element;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.cursor.CursorStyle;
import me.combimagnetron.sunscreen.neo.editor.EditorController;
import me.combimagnetron.sunscreen.neo.editor.virtual.VirtualElement;
import me.combimagnetron.sunscreen.neo.editor.virtual.VirtualPage;
import me.combimagnetron.sunscreen.neo.editor.virtual.argument.ElementConstructionProvider;
import me.combimagnetron.sunscreen.neo.element.GenericInteractableModernElement;
import me.combimagnetron.sunscreen.neo.element.ModernElement;
import me.combimagnetron.sunscreen.neo.element.impl.ButtonElement;
import me.combimagnetron.sunscreen.neo.element.impl.DropdownElement;
import me.combimagnetron.sunscreen.neo.event.UserMoveStateChangeEvent;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.graphic.color.Color;
import me.combimagnetron.sunscreen.neo.graphic.text.Text;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.color.TextColor;
import me.combimagnetron.sunscreen.neo.input.InputHandler;
import me.combimagnetron.sunscreen.neo.input.ListenerReferences;
import me.combimagnetron.sunscreen.neo.input.context.MouseInputContext;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.render.engine.context.RenderContext;
import me.combimagnetron.sunscreen.util.helper.HoverHelper;
import me.combimagnetron.sunscreen.util.helper.PropertyHelper;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

public class ElementLibraryElement extends GenericInteractableModernElement<ElementLibraryElement, Canvas, ElementLibraryElement.LayerOverviewElementListenerReferences> {
    private final LayerOverviewElementListenerReferences references = new LayerOverviewElementListenerReferences(this);
    private final EditorController controller;
    private boolean unfolded = false;
    private CursorStyle cursorStyle = CursorStyle.pointer();
    private int selected = 0;
    private int click = 0;

    protected ElementLibraryElement(@NotNull Identifier identifier, @NotNull EditorController controller) {
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
        if (!in && cursorStyle == CursorStyle.click()) {
            inputHandler().cursor(CursorStyle.pointer());
            cursorStyle = CursorStyle.pointer();
        }
        if (!in) {
            selected = -1;
            return;
        }
        Vec2i posVec = PropertyHelper.vectorOrThrow(position(), Vec2i.class);
        selected = (cursor.y() - posVec.y() - 4) / 52;
        if (selected > ElementConstructionProvider.PROVIDERS.length - 1) {
            selected = -1;
            inputHandler().cursor(CursorStyle.pointer());
            cursorStyle = CursorStyle.pointer();
            return;
        }
        inputHandler().cursor(CursorStyle.click());
        cursorStyle = CursorStyle.click();
        if (context.leftPressed()) click = 3;
        if (click == 1) {
            if (controller.selected() != null) {
                controller.elementSetup(ElementConstructionProvider.PROVIDERS[selected]);
            } else {
                controller.menu().notice(Identifier.of("aaaaaaa"), Text.vanilla("No page selected!"), cursor.sub(0, 15));
            }
        }
        if (click > 0) click -= 1;
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
        for (ElementConstructionProvider<?> constructionProvider : ElementConstructionProvider.PROVIDERS) {
            canvas.place(renderElement(constructionProvider, sizeVec), Vec2i.of(2, 1 + pageY));
            pageY += 53;
        }
        if (selected != -1) {
            canvas.fill(Vec2i.of(1, selected * 53), Vec2i.of(sizeVec.x() - 2, 1), Color.of(255, 255, 255));
            canvas.fill(Vec2i.of(1, selected * 53 + 53), Vec2i.of(sizeVec.x() - 2, 1), Color.of(255, 255, 255));
            canvas.fill(Vec2i.of(1, selected * 53), Vec2i.of(1, 53), Color.of(255, 255, 255));
            canvas.fill(Vec2i.of(sizeVec.x() - 2, 1 + selected * 53), Vec2i.of(1, 53), Color.of(255, 255, 255));
        }
        return canvas;
    }

    private @NotNull Canvas renderElement(@NotNull ElementConstructionProvider<?> provider, @NotNull Vec2i size) {
        ElementConstructionProvider.Preview<?> preview = provider.preview();
        Vec2i realSize = Vec2i.of(size.x() - 4, 52);
        Canvas canvas = Canvas.empty(realSize);
        canvas.fill(Vec2i.zero(), realSize, Color.of(27, 27, 27));
        canvas.fill(Vec2i.of(1, 9), realSize.sub(2, 10), Color.of(13, 13, 13));
        ModernElement<?, Canvas> modernElement = preview.base(Identifier.of("preview_" + ThreadLocalRandom.current().nextInt()));
        Vec2i placement = PropertyHelper.vectorOrThrow(modernElement.position(), Vec2i.class);
        Canvas place = modernElement.render(null, controller.context());
        canvas.place(place, placement);
        String typeName = provider.type().getSimpleName();
        canvas.text(Text.vanilla(String.join(" ", typeName.split("(?=[A-Z])"))), Vec2i.of(1, 1));
        return canvas;
    }

    public static final class LayerOverviewElementListenerReferences extends ListenerReferences<ElementLibraryElement, LayerOverviewElementListenerReferences> {
        private final @NotNull ElementLibraryElement back;

        public LayerOverviewElementListenerReferences(@NotNull ElementLibraryElement back) {
            this.back = back;
        }

        @Override
        public @NotNull ElementLibraryElement back() {
            return back;
        }

    }

}