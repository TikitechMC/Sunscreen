package me.combimagnetron.sunscreen.neo.editor.element;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.cursor.CursorStyle;
import me.combimagnetron.sunscreen.neo.element.impl.TextElement;
import me.combimagnetron.sunscreen.neo.event.UserMoveStateChangeEvent;
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
import me.combimagnetron.sunscreen.util.helper.PropertyHelper;
import me.combimagnetron.sunscreen.util.helper.editor.NameHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EditorNameElement extends TextElement<EditorNameElement> {
    private boolean shouldClear = false;

    public EditorNameElement(@NotNull Identifier identifier) {
        super(identifier);
    }

    @Override
    protected void lateInit() {
        super.lateInit();
        InputHandler handler = inputHandler();
        if (handler == null) return;
        handler.subscribe(MouseInputContext.class, this::handleCursor);
    }

    private void handleCursor(UserMoveStateChangeEvent stateChangeEvent) {
        final MouseInputContext inputContext = stateChangeEvent.context();
        Vec2i cursor = inputContext.position();
        if (stateChangeEvent.user() != inputHandler().user()) return;
        boolean hover = HoverHelper.in(this, cursor);
        if (!hover) return;
        if (!inputContext.leftPressed()) {
            inputHandler().cursor(CursorStyle.pointer());
            return;
        }
        TextInputContext textInputContext = context();
        if (textInputContext.active()) return;
        inputHandler().anvil(shouldClear);
        inputHandler().peek(MouseInputContext.class, old -> old.withLeftPressed(false), inputHandler().user());
        inputHandler().cursor(CursorStyle.textCaret());
    }

    @Override
    public @NotNull Canvas render(@NotNull Size property, @Nullable RenderContext context) {
        if (context == null) return Canvas.error(size());
        final TextInputContext textInputContext = context();
        final String input = textInputContext.stream().value();
        ThemeDecorator decorator = context.theme().find(this.getClass());
        Vec2i sizeVec = PropertyHelper.vectorOrThrow(size(), Vec2i.class);
        Vec2i offset = sizeVec.mul(1, 2).add(0, 12);
        Canvas canvas = Canvas.empty(offset);
        Canvas decoratorCanvas = decorator.render(size(), context);
        canvas.place(decoratorCanvas, Vec2i.zero());
        canvas.text(Text.basic(input).font(Registries.fonts().get(Identifier.of("sunscreen", "font/minecraft"))).fontProperties(FontProperties.properties().baseline(-2)), Vec2i.of(1,1));
        canvas.place(decorator.render(size(), context), offset.mul(0, 1).sub(0, 11));
        String id = NameHelper.suggestIdentifier(input);
        if (input.isEmpty()) id = "";
        canvas.text(Text.basic(id).font(Registries.fonts().get(Identifier.of("sunscreen", "font/minecraft"))).fontProperties(FontProperties.properties().baseline(-2)), Vec2i.of(1,1).add(offset.mul(0, 1).sub(0, 11)));
        return canvas;
    }

}
