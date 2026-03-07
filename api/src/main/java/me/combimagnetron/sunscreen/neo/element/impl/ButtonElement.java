package me.combimagnetron.sunscreen.neo.element.impl;

import me.combimagnetron.passport.event.Dispatcher;
import me.combimagnetron.passport.event.EventBus;
import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.cursor.CursorStyle;
import me.combimagnetron.sunscreen.neo.element.GenericInteractableModernElement;
import me.combimagnetron.sunscreen.neo.event.UserClickElementEvent;
import me.combimagnetron.sunscreen.neo.event.UserMoveStateChangeEvent;
import me.combimagnetron.sunscreen.neo.event.UserScrollStateChangeEvent;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.graphic.text.Text;
import me.combimagnetron.sunscreen.neo.input.InputHandler;
import me.combimagnetron.sunscreen.neo.input.ListenerReferences;
import me.combimagnetron.sunscreen.neo.input.context.MouseInputContext;
import me.combimagnetron.sunscreen.neo.input.context.ScrollInputContext;
import me.combimagnetron.sunscreen.neo.property.Scale;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.property.Visibility;
import me.combimagnetron.sunscreen.neo.render.engine.context.RenderContext;
import me.combimagnetron.sunscreen.neo.theme.ModernTheme;
import me.combimagnetron.sunscreen.neo.theme.decorator.ThemeDecorator;
import me.combimagnetron.sunscreen.util.helper.HoverHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.function.Consumer;

public class ButtonElement extends GenericInteractableModernElement<ButtonElement, Canvas, ButtonElement.ButtonElementListenerReferences> {
    private final ButtonElementListenerReferences references = new ButtonElementListenerReferences(this);
    private final Text text;
    private ElementPhase phase = ElementPhase.DEFAULT;
    private int click = 0;
    private Vec2i textPosition = Vec2i.zero();

    public ButtonElement(@NotNull Identifier identifier) {
        this(identifier, null, null);
    }

    public ButtonElement(@NotNull Identifier identifier, @Nullable Text label, @Nullable Vec2i textPosition) {
        super(identifier);
        text = label;
        if (textPosition != null) this.textPosition = textPosition;
    }

    @Override
    protected void lateInit() {
        super.lateInit();
        input(MouseInputContext.class).listen(this::handleCursor);
    }

    private void handleCursor(@NotNull UserMoveStateChangeEvent event) {
        if (event.user() != inputHandler().user()) return;
        final MouseInputContext context = event.context();
        Vec2i cursor = context.position();
        Visibility visibility = visibility();
        if (visibility.hide()) return;
        boolean hover = HoverHelper.in(this, cursor);
        if (hover && context.leftPressed()) click = 6;
        InputHandler handler = inputHandler();
        if (!hover && phase != ElementPhase.DEFAULT) {
            phase = ElementPhase.DEFAULT;
            handler.cursor(CursorStyle.pointer());
            return;
        }
        if (click > 0) {
            click -= 1;
            phase = ElementPhase.CLICK;
            Dispatcher.dispatcher().post(new UserClickElementEvent<>(event.user(), this, cursor.sub(position().value())));
        } else if (hover) {
            phase = ElementPhase.HOVER;
            handler.cursor(CursorStyle.click());
        }
    }

    @Override
    public @NotNull ButtonElementListenerReferences listen() {
        return references;
    }

    @Override
    public @NotNull Canvas render(@NotNull Size property, @Nullable RenderContext context) {
        Size size = size();
        if (context == null) return Canvas.error(size);
        ModernTheme theme = context.theme();
        ThemeDecorator<?> themeDecorator = theme.find(this.getClass());
        if (!(themeDecorator instanceof ThemeDecorator.StateNineSliceThemeDecorator<?> decorator)) return Canvas.error(size);
        Canvas rendered = decorator.render(size, context, phase);
        if (text != null) {
            rendered = rendered.place(text.render(size, context), textPosition);
        }
        return rendered;
    }

    public record ButtonElementListenerReferences(ButtonElement buttonElement) implements ListenerReferences<ButtonElement, ButtonElementListenerReferences> {

        @Override
        public @NotNull ButtonElement back() {
            return buttonElement;
        }

        @SuppressWarnings("unchecked")
        public @NotNull ButtonElementListenerReferences click(@NotNull Consumer<UserClickElementEvent<ButtonElement>> eventConsumer) {
            EventBus.subscribe(UserClickElementEvent.class, (Consumer) eventConsumer);
            return this;
        }

    }

}
