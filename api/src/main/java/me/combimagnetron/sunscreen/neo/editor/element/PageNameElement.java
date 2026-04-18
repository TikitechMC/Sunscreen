package me.combimagnetron.sunscreen.neo.editor.element;

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
import me.combimagnetron.sunscreen.util.helper.PropertyHelper;
import me.combimagnetron.sunscreen.util.helper.editor.NameHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PageNameElement extends TextElement<PageNameElement> {
    private String displayName = "";
    private Identifier fakeIdentifier = Identifier.of("");
    private boolean selected = false;
    private CursorStyle style = CursorStyle.pointer();
    private String firstWord = "page";

    protected PageNameElement(@NotNull Identifier identifier) {
        super(identifier);
    }

    @Override
    protected void lateInit() {
        super.lateInit();
        InputHandler handler = inputHandler();
        if (handler == null) return;
        handler.subscribe(identifier(), MouseInputContext.class, this::handleCursor);
        handler.subscribe(identifier(), TextInputContext.class, this::handleText);
    }

    private void handleCursor(@NotNull UserMoveStateChangeEvent stateChangeEvent) {
        final MouseInputContext inputContext = stateChangeEvent.context();
        Vec2i cursor = inputContext.position();
        if (visibility().hide()) return;
        if (stateChangeEvent.user() != inputHandler().user()) return;
        boolean hover = HoverHelper.in(this, cursor);
        if (!hover) {
            selected = false;
            return;
        }
        if (!hover && style == CursorStyle.textCaret()) {
            style = CursorStyle.pointer();
            inputHandler().cursor(CursorStyle.pointer());
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
        inputHandler().anvil(true);
        inputHandler().peek(MouseInputContext.class, old -> old.withLeftPressed(false), inputHandler().user());
        inputHandler().cursor(CursorStyle.textCaret());
        style = CursorStyle.textCaret();
    }

    private void handleText(@NotNull UserTextStateChangeEvent event) {
        if (event.user() != inputHandler().user()) return;
        if (!selected) return;
        TextInputContext context = event.context();
        this.displayName = context.stream().value();
        this.fakeIdentifier = Identifier.split(NameHelper.suggestIdentifier(displayName, "cursor", firstWord));
    }

    public void clear() {
        displayName = "";
        fakeIdentifier = Identifier.of("");
    }

    public @NotNull PageNameElement firstWord(@NotNull String word) {
        this.firstWord = word;
        return this;
    }

    @Override
    public @NotNull Canvas render(@NotNull Size property, @Nullable RenderContext context) {
        if (context == null) return Canvas.error(size());
        ThemeDecorator decorator = context.decorator(this);
        Vec2i sizeVec = PropertyHelper.vectorOrThrow(size(), Vec2i.class);
        Vec2i offset = sizeVec.mul(1, 2).add(0, 12);
        Canvas canvas = Canvas.empty(offset);
        Canvas decoratorCanvas = decorator.render(size(), context);
        canvas.place(decoratorCanvas, Vec2i.zero());
        canvas.text(Text.basic(displayName).font(Registries.fonts().get(Identifier.of("sunscreen", "font/minecraft"))).fontProperties(FontProperties.properties().baseline(-2)), Vec2i.of(1,1));
        canvas.place(decorator.render(size(), context), offset.mul(0, 1).sub(0, 11));
        String id = fakeIdentifier.string();
        if (displayName.isEmpty()) id = "";
        canvas.text(Text.basic(id).font(Registries.fonts().get(Identifier.of("sunscreen", "font/minecraft"))).fontProperties(FontProperties.properties().baseline(-2)), Vec2i.of(1,1).add(offset.mul(0, 1).sub(0, 11)));
        return canvas;
    }

    public boolean validate() {
        return displayName.length() > 3;
    }

    public @NotNull String displayName() {
        return displayName;
    }

    public @NotNull Identifier fakeIdentifier() {
        return fakeIdentifier;
    }

    public boolean selected() {
        return selected;
    }

}