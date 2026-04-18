package me.combimagnetron.sunscreen.neo.editor.element;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.element.GenericInteractableModernElement;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.input.ListenerReferences;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.render.engine.context.RenderContext;
import me.combimagnetron.sunscreen.neo.theme.decorator.ThemeDecorator;
import me.combimagnetron.sunscreen.util.helper.PropertyHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public class RadioButtonElement extends GenericInteractableModernElement<RadioButtonElement, Canvas, RadioButtonElement.RadioButtomElementListenerReferences> {
    private final RadioButtomElementListenerReferences references = new RadioButtomElementListenerReferences(this);

    protected RadioButtonElement(@Nullable Identifier identifier) {
        super(identifier);
    }

    @Override
    public @NonNull RadioButtomElementListenerReferences listen() {
        return references;
    }

    @Override
    public @NonNull Canvas render(@NonNull Size property, @Nullable RenderContext context) {
        Vec2i sizeVec = PropertyHelper.vectorOrThrow(size(), Vec2i.class);
        if (context == null) return Canvas.error(size());
        ThemeDecorator decorator = context.theme().find(this.getClass());

        return null;
    }

    public static final class RadioButtomElementListenerReferences extends ListenerReferences<RadioButtonElement, RadioButtomElementListenerReferences> {
        private final @NotNull RadioButtonElement back;

        public RadioButtomElementListenerReferences(@NotNull RadioButtonElement back) {
            this.back = back;
        }

        @Override
        public @NotNull RadioButtonElement back() {
            return back;
        }

    }

}
