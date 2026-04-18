package me.combimagnetron.sunscreen.neo.editor.widget;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.editor.element.EditorElements;
import me.combimagnetron.sunscreen.neo.editor.element.FrameElement;
import me.combimagnetron.sunscreen.neo.element.ModernElement;
import me.combimagnetron.sunscreen.neo.element.impl.SelectorElement;
import me.combimagnetron.sunscreen.neo.event.UserClickElementEvent;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.input.InputHandler;
import me.combimagnetron.sunscreen.neo.layout.Layout;
import me.combimagnetron.sunscreen.neo.property.Position;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.render.engine.context.RenderContext;
import me.combimagnetron.sunscreen.util.helper.PropertyHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class EditorWidget<M extends ModernElement<M, Canvas>> extends Layout.GroupLayout<M> {
    private final int shittyId = ThreadLocalRandom.current().nextInt();
    private final SelectorElement selectorElement = new SelectorElement(Identifier.of("selector_tab_" + shittyId), 11).position(Position.fixed(Vec2i.of(1, 1)));
    private final List<EditorWidgetTab<?>> tabs = new LinkedList<>();
    private int lastTab = 0;

    protected EditorWidget(@NotNull Identifier identifier) {
        super(identifier);
        add(EditorElements.frame(Identifier.of("frame_" + shittyId)).position(Position.nil()));
        add(selectorElement);
    }

    public static @NotNull EditorWidget<?> widget(@NotNull Identifier identifier) {
        return new EditorWidget<>(identifier);
    }

    public @NotNull EditorWidget<M> tab(@NotNull EditorWidgetTab<?> tab) {
        tabs.add(tab);
        return this;
    }

    @Override
    public void inputHandler(@NotNull InputHandler handler) {
        Vec2i sizeVec = PropertyHelper.vectorOrThrow(size(), Vec2i.class);
        selectorElement.size(Size.fixed(Vec2i.of(sizeVec.x(), 11)));
        elements.values().stream().filter(element -> element instanceof FrameElement).findAny().orElseThrow().size(Size.fixed(Vec2i.of(sizeVec.x(), 13)));
        super.inputHandler(handler);
        handler.listen(identifier(), UserClickElementEvent.class, this::handleClick);
        for (int i = 0; i < tabs.size(); i++) {
            EditorWidgetTab<?> tab = tabs.get(i);
            if (i == 0) {
                tab.show();
            } else {
                tab.hide();
            }
            selectorElement.entry(tab.displayName());
            tab.inputHandler(handler);
        }
    }

    private void handleClick(@NotNull UserClickElementEvent<?> event) {
        if (event.user() != handler().user()) return;
        if (!(event.element() instanceof SelectorElement element)) return;
        if (element.identifier() != selectorElement.identifier()) return;
        int selected = element.selected();
        for (int i = 0; i < tabs.size(); i++) {
            EditorWidgetTab<?> tab = tabs.get(i);
            if (i == selected) {
                tab.show();
            } else {
                tab.hide();
            }
        }
    }


    @Override
    public @NotNull Canvas render(@NotNull Size property, @Nullable RenderContext context) {
        Vec2i sizeVec = PropertyHelper.vectorOrThrow(size(), Vec2i.class);
        Canvas canvas = Canvas.empty(sizeVec);
        canvas.place(tabs.get(selectorElement.selected()).render(property, context), Vec2i.of(0, 14));
        canvas.place(super.render(property, context), Vec2i.zero());
        return canvas;
    }

    public @NotNull EditorWidget<M> tab(int selected) {
        selectorElement.select(selected);
        return this;
    }

}
