package me.combimagnetron.sunscreen.neo.element.impl;

import me.combimagnetron.passport.event.Event;
import me.combimagnetron.passport.event.EventBus;
import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.sunscreen.neo.element.GenericInteractableModernElement;
import me.combimagnetron.sunscreen.neo.event.UserTextStateChangeEvent;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.input.ListenerReferences;
import me.combimagnetron.sunscreen.neo.input.context.InputContext;
import me.combimagnetron.sunscreen.neo.input.context.TextInputContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class TextElement<E extends TextElement<E>> extends GenericInteractableModernElement<E, Canvas, TextElement.TextElementListenerReferences<E>> {
    protected final TextElementListenerReferences<E> references = new TextElementListenerReferences<>(self());

    protected TextElement(@NotNull Identifier identifier) {
        super(identifier);
    }

    @SuppressWarnings("unchecked")
    private E self() {
        return (E) this;
    }

    protected @NotNull TextInputContext context() {
        return input(TextInputContext.class);
    }

    @Override
    public @NotNull TextElementListenerReferences<E> listen() {
        return references;
    }

    public static final class TextElementListenerReferences<E extends TextElement<E>> extends ListenerReferences<E, TextElementListenerReferences<E>> {
        private final E back;

        public TextElementListenerReferences(E back) {
            this.back = back;
        }

        public @NotNull TextElementListenerReferences<E> updated(@NotNull Consumer<UserTextStateChangeEvent> event) {
            put(UserTextStateChangeEvent.class, event);
            return this;
        }

        @Override
        public E back() {
            return back;
        }

    }

}