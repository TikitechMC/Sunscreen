package me.combimagnetron.sunscreen.neo.element.impl.text;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.cursor.CursorStyle;
import me.combimagnetron.sunscreen.neo.element.impl.TextElement;
import me.combimagnetron.sunscreen.neo.event.UserMoveStateChangeEvent;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.graphic.text.Text;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.font.FontProperties;
import me.combimagnetron.sunscreen.neo.input.context.MouseInputContext;
import me.combimagnetron.sunscreen.neo.input.context.TextInputContext;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.registry.Registries;
import me.combimagnetron.sunscreen.neo.render.engine.context.RenderContext;
import me.combimagnetron.sunscreen.neo.theme.decorator.ThemeDecorator;
import me.combimagnetron.sunscreen.util.helper.HoverHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * {@link TextElement} implementation for single-lined/limited, small text inputs.
 */
public class TextFieldElement extends TextElement<TextFieldElement> {

    public TextFieldElement(@NotNull Identifier identifier) {
        super(identifier);
    }

    @Override
    protected void lateInit() {
        super.lateInit();
        input(MouseInputContext.class).listen(this::handleCursor);
    }

    private void handleCursor(UserMoveStateChangeEvent stateChangeEvent) {
        final MouseInputContext inputContext = stateChangeEvent.context();
        Vec2i cursor = inputContext.position();
        boolean hover = HoverHelper.in(this, cursor);
        if (!hover) return;
        if (!inputContext.leftPressed()) {
            inputHandler().cursor(CursorStyle.pointer());
            return;
        }
        TextInputContext textInputContext = context();
        if (textInputContext.active()) return;
        inputHandler().anvil();
        inputHandler().cursor(CursorStyle.textCaret());
    }

    @Override
    public @NotNull Canvas render(@NotNull Size property, @Nullable RenderContext context) {
        if (context == null) return Canvas.error(size());
        final TextInputContext textInputContext = context();
        final String input = textInputContext.stream().value();
        ThemeDecorator<?> decorator = context.theme().find(this.getClass());
        Canvas canvas = decorator.render(size(), context);
        canvas.text(Text.basic(input).font(Registries.fonts().get(Identifier.of("sunscreen", "font/sunburned"))).fontProperties(FontProperties.properties().baseline(-6)), Vec2i.of(2,2));
        return canvas;
    }

}
