package me.combimagnetron.sunscreen.neo.editor.element;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.cursor.CursorStyle;
import me.combimagnetron.sunscreen.neo.element.GenericInteractableModernElement;
import me.combimagnetron.sunscreen.neo.event.UserClickElementEvent;
import me.combimagnetron.sunscreen.neo.event.UserMoveStateChangeEvent;
import me.combimagnetron.sunscreen.neo.event.UserTextStateChangeEvent;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.graphic.color.Color;
import me.combimagnetron.sunscreen.neo.graphic.text.Text;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.font.FontProperties;
import me.combimagnetron.sunscreen.neo.input.InputHandler;
import me.combimagnetron.sunscreen.neo.input.ListenerReferences;
import me.combimagnetron.sunscreen.neo.input.context.MouseInputContext;
import me.combimagnetron.sunscreen.neo.input.context.TextInputContext;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.registry.Registries;
import me.combimagnetron.sunscreen.neo.render.engine.context.RenderContext;
import me.combimagnetron.sunscreen.neo.theme.decorator.Target;
import me.combimagnetron.sunscreen.neo.theme.decorator.ThemeDecorator;
import me.combimagnetron.sunscreen.util.helper.HoverHelper;
import me.combimagnetron.sunscreen.util.helper.PropertyHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class MultiEntryFieldElement extends GenericInteractableModernElement<MultiEntryFieldElement, Canvas, MultiEntryFieldElement.MultiEntryListenerReferences> {
    private final MultiEntryListenerReferences references = new MultiEntryListenerReferences(this);
    private final List<String> entries = new ArrayList<>();
    private final int rowHeight;
    private final List<String> currentInputs = new ArrayList<>();
    private int activeField = -1;
    private int hoveredButton = -1;
    private int buttonClickCooldown = 0;
    private CursorStyle cursorStyle = CursorStyle.pointer();

    public MultiEntryFieldElement(@NotNull Identifier identifier, int rowHeight) {
        super(identifier);
        this.rowHeight = rowHeight;
        currentInputs.add("");
    }

    @Override
    protected void lateInit() {
        super.lateInit();
        InputHandler handler = inputHandler();
        if (handler == null) return;
        references.subscribe(handler);
        handler.subscribe(identifier(), MouseInputContext.class, this::handleCursor);
        handler.subscribe(identifier(), TextInputContext.class, this::handleText);
    }

    private void handleCursor(@NotNull UserMoveStateChangeEvent event) {
        if (event.user() != inputHandler().user()) return;
        MouseInputContext context = event.context();
        Vec2i cursor = context.position();
        if (visibility().hide()) return;
        InputHandler handler = inputHandler();
        if (!HoverHelper.in(this, cursor)) {
            hoveredButton = -1;
            activeField = -1;
            if (cursorStyle != CursorStyle.pointer()) {
                handler.cursor(CursorStyle.pointer());
                cursorStyle = CursorStyle.pointer();
            }
            return;
        }
        Vec2i posVec = PropertyHelper.vectorOrThrow(position(), Vec2i.class);
        Vec2i relative = cursor.sub(posVec);
        Vec2i sizeVec = PropertyHelper.vectorOrThrow(size(), Vec2i.class);
        int fieldCount = currentInputs.size();
        int buttonY = fieldCount * rowHeight;
        if (relative.y() >= buttonY && relative.y() < buttonY + rowHeight) {
            hoveredButton = 0;
            handler.cursor(CursorStyle.click());
            cursorStyle = CursorStyle.click();
            if (context.leftPressed()) buttonClickCooldown = 3;
            if (buttonClickCooldown == 1) {
                commitActiveField();
                currentInputs.add("");
                activeField = currentInputs.size() - 1;
                handler.anvil(true);
                handler.peek(MouseInputContext.class, old -> old.withLeftPressed(false), handler.user());
            }
            if (buttonClickCooldown > 0) buttonClickCooldown--;
            return;
        }
        hoveredButton = -1;
        int fieldIndex = relative.y() / rowHeight;
        if (fieldIndex >= fieldCount) {
            handler.cursor(CursorStyle.pointer());
            cursorStyle = CursorStyle.pointer();
            return;
        }
        handler.cursor(CursorStyle.textCaret());
        cursorStyle = CursorStyle.textCaret();
        if (context.leftPressed() && activeField != fieldIndex) {
            commitActiveField();
            activeField = fieldIndex;
            handler.anvil(true);
            handler.peek(MouseInputContext.class, old -> old.withLeftPressed(false), handler.user());
        }
    }

    private void handleText(@NotNull UserTextStateChangeEvent event) {
        if (event.user() != inputHandler().user()) return;
        if (activeField < 0 || activeField >= currentInputs.size()) return;
        TextInputContext context = event.context();
        currentInputs.set(activeField, context.stream().value());
    }

    private void commitActiveField() {
        if (activeField < 0 || activeField >= currentInputs.size()) return;
        String value = currentInputs.get(activeField).trim();
        if (value.isEmpty()) return;
        if (activeField < entries.size()) {
            entries.set(activeField, value);
        } else {
            entries.add(value);
        }
    }

    public @NotNull List<String> entries() {
        commitActiveField();
        return Collections.unmodifiableList(entries);
    }

    public void clear() {
        entries.clear();
        currentInputs.clear();
        currentInputs.add("");
        activeField = -1;
    }

    @Override
    public @NotNull MultiEntryListenerReferences listen() {
        return references;
    }

    @Override
    public @NotNull Canvas render(@NotNull Size property, @Nullable RenderContext context) {
        Size size = size();
        if (context == null) return Canvas.error(size);
        ThemeDecorator themeDecorator = context.decorator(this);
        if (!(themeDecorator instanceof ThemeDecorator.StateNineSliceThemeDecorator decorator)) return Canvas.error(size);
        Vec2i sizeVec = PropertyHelper.vectorOrThrow(size, Vec2i.class);
        int fieldCount = currentInputs.size();
        int totalHeight = (fieldCount + 1) * rowHeight;
        Canvas canvas = Canvas.empty(Vec2i.of(sizeVec.x(), totalHeight));
        for (int i = 0; i < fieldCount; i++) {
            ElementPhase phase = (i == activeField) ? ElementPhase.CLICK : ElementPhase.DEFAULT;
            Canvas field = decorator.render(Size.fixed(Vec2i.of(sizeVec.x(), rowHeight)), context, phase);
            String input = currentInputs.get(i);
            if (!input.isEmpty()) {
                Canvas textCanvas = Text.basic(input)
                        .font(Registries.fonts().get(Identifier.of("sunscreen", "font/minecraft")))
                        .fontProperties(FontProperties.properties().baseline(-2))
                        .render(Size.fixed(Vec2i.of(sizeVec.x() - 4, rowHeight)), null)
                        .trim();
                field.place(textCanvas, Vec2i.of(2, 2));
            }
            canvas.place(field, Vec2i.of(0, i * rowHeight));
        }
        ElementPhase addPhase = (hoveredButton == 0) ? ElementPhase.HOVER : ElementPhase.DEFAULT;
        Vec2i buttonSize = Vec2i.of(sizeVec.x(), rowHeight);
        ThemeDecorator.StateNineSliceThemeDecorator addButtonDecorator = (ThemeDecorator.StateNineSliceThemeDecorator) context.theme().find(Target.identifier(Identifier.of("sunscreen", "internal/editor/theme/decorator/button_confirm")));
        Canvas addButton = addButtonDecorator.render(Size.fixed(buttonSize.sub(2, 2)), context, addPhase);
        addButton.text(Text.vanilla("New Entry"), Vec2i.of(1, 1));
        canvas.place(addButton, Vec2i.of(0, fieldCount * rowHeight).add(1, 1));
        return canvas;
    }

    public static final class MultiEntryListenerReferences extends ListenerReferences<MultiEntryFieldElement, MultiEntryListenerReferences> {
        private final MultiEntryFieldElement element;

        public MultiEntryListenerReferences(@NotNull MultiEntryFieldElement element) {
            this.element = element;
        }

        @Override
        public @NotNull MultiEntryFieldElement back() {
            return element;
        }

        @SuppressWarnings("unchecked")
        public @NotNull MultiEntryListenerReferences onEntryAdded(@NotNull Consumer<UserClickElementEvent<?>> consumer) {
            put(UserClickElementEvent.class, consumer);
            return this;
        }
    }
}