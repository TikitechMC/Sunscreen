package me.combimagnetron.sunscreen.neo.element.impl;

import me.combimagnetron.passport.event.Dispatcher;
import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.cursor.CursorStyle;
import me.combimagnetron.sunscreen.neo.element.GenericInteractableModernElement;
import me.combimagnetron.sunscreen.neo.event.UserClickElementEvent;
import me.combimagnetron.sunscreen.neo.event.UserMoveStateChangeEvent;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.graphic.color.Color;
import me.combimagnetron.sunscreen.neo.graphic.text.Text;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.color.TextColor;
import me.combimagnetron.sunscreen.neo.input.InputHandler;
import me.combimagnetron.sunscreen.neo.input.ListenerReferences;
import me.combimagnetron.sunscreen.neo.input.base.Action;
import me.combimagnetron.sunscreen.neo.input.base.Module;
import me.combimagnetron.sunscreen.neo.input.base.MouseInputBase;
import me.combimagnetron.sunscreen.neo.input.base.impl.Modules;
import me.combimagnetron.sunscreen.neo.input.context.MouseInputContext;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.property.Visibility;
import me.combimagnetron.sunscreen.neo.render.engine.context.RenderContext;
import me.combimagnetron.sunscreen.neo.theme.decorator.ThemeDecorator;
import me.combimagnetron.sunscreen.util.helper.HoverHelper;
import me.combimagnetron.sunscreen.util.helper.PropertyHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class ButtonElement extends GenericInteractableModernElement<ButtonElement, Canvas, ButtonElement.ButtonElementListenerReferences> {
    private final ButtonElementListenerReferences references = new ButtonElementListenerReferences(this);
    private Text text;
    private ElementPhase phase = ElementPhase.DEFAULT;
    private int click = 0;
    private Vec2i textPosition = Vec2i.zero();
    private boolean autoCenterText = false;
    private Canvas canvas;

    public ButtonElement(@NotNull Identifier identifier) {
        this(identifier, null, null);
    }

    public ButtonElement(@NotNull Identifier identifier, @Nullable Text label, @Nullable Vec2i textPosition) {
        super(identifier);
        text = label;
        if (textPosition != null)
            this.textPosition = textPosition;
    }

    public ButtonElement(@NotNull Identifier identifier, @Nullable Text label) {
        super(identifier);
        this.text = label;
        this.autoCenterText = true;
    }

    @Override
    protected void lateInit() {
        super.lateInit();
        InputHandler handler = inputHandler();
        if (handler == null)
            return;
        references.subscribe(handler);
        handler.subscribe(identifier(), MouseInputContext.class, this::handleCursor);
        Vec2i size = PropertyHelper.vectorOrThrow(size(), Vec2i.class);
    }

    public @NotNull ButtonElement text(@Nullable Text text) {
        this.text = text;
        return this;
    }

    public @NotNull ButtonElement textPosition(@Nullable Vec2i position) {
        this.textPosition = position;
        this.autoCenterText = false;
        return this;
    }

    public @NotNull ButtonElement autoCenterText(boolean autoCenterText) {
        this.autoCenterText = autoCenterText;
        return this;
    }

    public @NotNull ButtonElement canvas(@Nullable Canvas canvas) {
        this.canvas = canvas;
        return this;
    }

    private void handleCursor(@NotNull UserMoveStateChangeEvent event) {
        if (event.user() != inputHandler().user())
            return;
        final MouseInputContext context = event.context();
        Vec2i cursor = context.position();
        boolean hover = HoverHelper.in(this, cursor);
        if (hover && context.leftPressed())
            click = 3;
        InputHandler handler = inputHandler();
        if (phase == ElementPhase.DISABLED) return;
        if (!hover && phase != ElementPhase.DEFAULT) {
            phase = ElementPhase.DEFAULT;
            handler.cursor(CursorStyle.pointer());
            return;
        }
        Visibility visibility = visibility();
        if (visibility.hide())
            return;
        if (click == 1) {
            Dispatcher.dispatcher()
                    .post(new UserClickElementEvent<>(event.user(), this, cursor.sub(position().value())));
        }
        if (click > 0) {
            click -= 1;
            phase = ElementPhase.CLICK;
        } else if (hover) {
            phase = ElementPhase.HOVER;
            handler.cursor(CursorStyle.click());
        }
    }

    public @NotNull ButtonElement disabled(boolean disabled) {
        this.phase = disabled ? ElementPhase.DISABLED : ElementPhase.DEFAULT;
        return this;
    }

    public @NotNull ButtonElement disable() {
        return disabled(true);
    }

    public @NotNull ButtonElement enable() {
        return disabled(false);
    }

    @Override
    public @NotNull ButtonElementListenerReferences listen() {
        return references;
    }

    @Override
    public @NotNull Canvas render(@NotNull Size property, @Nullable RenderContext context) {
        Size size = size();
        if (context == null)
            return Canvas.error(size);
        ThemeDecorator themeDecorator = context.decorator(this);
        if (!(themeDecorator instanceof ThemeDecorator.StateNineSliceThemeDecorator decorator))
            return Canvas.error(size);
        Vec2i sizeVec = PropertyHelper.vectorOrThrow(size, Vec2i.class);
        Canvas button = Canvas.empty(sizeVec);
        if (canvas != null) {
            button.place(canvas, Vec2i.zero());
        } else {
            button = decorator.render(size, context, phase);
        }
        if (text != null) {
            Text buttonText = text;
            if (phase == ElementPhase.DISABLED) buttonText.color(TextColor.color(Color.of(93, 93, 93)));
            else buttonText.color(TextColor.color(Color.of(255, 255, 255)));
            Canvas renderedText = buttonText.render(size, context).trim();
            Vec2i placePosition = textPosition;
            if (autoCenterText) {
                placePosition = Vec2i.of(
                    (sizeVec.x() - renderedText.size().x()) / 2,
                    (sizeVec.y() - renderedText.size().y()) / 2
                );
            }
            button.place(renderedText, placePosition);
        }
        // button.modifier(GraphicModifiers.mask(Shape.rectangle(Vec2i.of(20, 20)),
        // ModifierContext.of()));
        return button;
    }

    public static final class ButtonElementListenerReferences extends ListenerReferences<ButtonElement, ButtonElementListenerReferences> {
        private final ButtonElement buttonElement;

        public ButtonElementListenerReferences(ButtonElement buttonElement) {
            this.buttonElement = buttonElement;
        }

        @Override
        public @NotNull ButtonElement back() {
            return buttonElement;
        }

        @SuppressWarnings("unchecked")
        public @NotNull ButtonElementListenerReferences click(@NotNull Consumer<UserClickElementEvent<?>> eventConsumer) {
            put(UserClickElementEvent.class, eventConsumer);
            return this;
        }

    }

}
