package me.combimagnetron.sunscreen.neo.element;

import me.combimagnetron.passport.logic.state.MutableState;
import me.combimagnetron.passport.logic.state.State;
import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.sunscreen.neo.graphic.GraphicLike;
import me.combimagnetron.sunscreen.neo.input.InputHandler;
import me.combimagnetron.sunscreen.neo.input.Interactable;
import me.combimagnetron.sunscreen.neo.input.ListenerReferences;
import me.combimagnetron.sunscreen.neo.input.context.InputContext;
import me.combimagnetron.sunscreen.neo.input.context.MouseInputContext;
import me.combimagnetron.sunscreen.util.helper.HoverHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GenericInteractableModernElement<E extends ModernElement<E, G>, G extends GraphicLike<G>, R extends ListenerReferences<E, R>>
        extends GenericModernElement<E, G> implements Interactable<E, R> {
    private InputHandler inputHandler;
    protected final MutableState<ElementPhase> state = State.mutable(ElementPhase.DEFAULT);

    protected GenericInteractableModernElement(@Nullable Identifier identifier) {
        super(identifier);
    }

    protected void lateInit() {
        mouse().listen((event) -> {
            final MouseInputContext context = event.context();
            boolean cursorOnElement = HoverHelper.in(this, context.position());
            if (cursorOnElement && context.leftPressed() && state.value() != ElementPhase.CLICK) {
                state.state(ElementPhase.CLICK);
            } else if (cursorOnElement && state.value() != ElementPhase.HOVER) {
                state.state(ElementPhase.HOVER);
            } else {
                state.state(ElementPhase.DEFAULT);
            }
        });
    }

    public void inputHandler(@NotNull InputHandler inputHandler) {
        this.inputHandler = inputHandler;
        lateInit();
    }

    public @Nullable InputHandler inputHandler() {
        return inputHandler;
    }

    @Override
    public @NotNull <C extends InputContext<?>> C input(Class<C> clazz) {
        return inputHandler.context(clazz);
    }

    public interface ElementPhase {
        ElementPhase DEFAULT = new ElementPhase() {
        };
        ElementPhase HOVER = new ElementPhase() {
        };
        ElementPhase CLICK = new ElementPhase() {
        };
        ElementPhase DISABLED = new ElementPhase() {
        };
    }

}
