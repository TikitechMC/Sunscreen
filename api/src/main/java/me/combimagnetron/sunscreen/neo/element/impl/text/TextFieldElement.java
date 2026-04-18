package me.combimagnetron.sunscreen.neo.element.impl.text;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.cursor.CursorStyle;
import me.combimagnetron.sunscreen.neo.element.impl.TextElement;
import me.combimagnetron.sunscreen.neo.event.UserMoveStateChangeEvent;
import me.combimagnetron.sunscreen.neo.event.UserTextStateChangeEvent;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.graphic.text.Text;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.font.FontProperties;
import me.combimagnetron.sunscreen.neo.input.InputHandler;
import me.combimagnetron.sunscreen.neo.input.context.MouseInputContext;
import me.combimagnetron.sunscreen.neo.input.context.TextInputContext;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.registry.Registries;
import me.combimagnetron.sunscreen.neo.render.engine.context.RenderContext;
import me.combimagnetron.sunscreen.neo.theme.decorator.ThemeDecorator;
import me.combimagnetron.sunscreen.util.helper.HoverHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TextFieldElement extends TextElement<TextFieldElement> {
    private CursorStyle style = CursorStyle.pointer();
    private Vec2i textPosition = Vec2i.of(2, 2);
    private boolean shouldClear = false;
    private boolean selected = false;
    private boolean newCycle = false;
    private String value = "";

    public TextFieldElement(@NotNull Identifier identifier) {
        super(identifier);
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

    public @NotNull TextFieldElement shouldClear(boolean shouldClear) {
        this.shouldClear = shouldClear;
        return this;
    }

    public void clear() {
        this.value = "";
    }

    public @NotNull TextFieldElement textPosition(@NotNull Vec2i vec2i) {
        this.textPosition = vec2i;
        return this;
    }

    private void handleCursor(UserMoveStateChangeEvent stateChangeEvent) {
        if (stateChangeEvent.user() != inputHandler().user()) return;
        if (visibility().hide()) return;
        final MouseInputContext inputContext = stateChangeEvent.context();
        Vec2i cursor = inputContext.position();
        boolean hover = HoverHelper.in(this, cursor);
        if (!hover && style == CursorStyle.textCaret()) {
            style = CursorStyle.pointer();
            inputHandler().cursor(CursorStyle.pointer());
            return;
        }
        if (newCycle) newCycle = false;
        if (!hover) {
            selected = false;
            return;
        }
        TextInputContext textInputContext = context();
        if (!inputContext.leftPressed() && !textInputContext.active()) {
            inputHandler().cursor(CursorStyle.pointer());
            style = CursorStyle.pointer();
            return;
        }
        selected = true;
        if (textInputContext.active()) {
            return;
        }
        if (shouldClear) {
            inputHandler().peek(TextInputContext.class, TextInputContext::clear, inputHandler().user());
        } else {
            //newCycle = true;
        }
        inputHandler().anvil(shouldClear);
        inputHandler().peek(MouseInputContext.class, old -> old.withLeftPressed(false), inputHandler().user());
        inputHandler().cursor(CursorStyle.textCaret());
        style = CursorStyle.textCaret();
    }

    private void handleText(@NotNull UserTextStateChangeEvent event) {
        if (event.user() != inputHandler().user()) return;
        if (!selected) return;
        TextInputContext context = event.context();
        String updated = context.stream().value();
        //if (value.isEmpty() && newCycle) return;
        this.value = updated;
    }

    @Override
    public @NotNull Canvas render(@NotNull Size property, @Nullable RenderContext context) {
        if (context == null) return Canvas.error(size());
        ThemeDecorator decorator = context.decorator(this);
        Canvas canvas = decorator.render(size(), context);
        canvas.text(Text.basic(value).font(Registries.fonts().get(Identifier.of("sunscreen", "font/minecraft"))).fontProperties(FontProperties.properties().baseline(-2)), textPosition);
        return canvas;
    }

    public @NotNull String value() {
        return value;
    }

    public boolean selected() {
        return selected;
    }

}