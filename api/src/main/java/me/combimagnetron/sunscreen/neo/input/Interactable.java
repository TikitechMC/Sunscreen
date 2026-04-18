package me.combimagnetron.sunscreen.neo.input;

import me.combimagnetron.sunscreen.neo.element.ModernElement;
import me.combimagnetron.sunscreen.neo.input.context.InputContext;
import me.combimagnetron.sunscreen.neo.input.context.MouseInputContext;
import me.combimagnetron.sunscreen.neo.input.context.ScrollInputContext;
import org.jetbrains.annotations.NotNull;

public interface Interactable<T extends ModernElement<T, ?>, R extends ListenerReferences<T, R>> {

    @NotNull R listen();

    @NotNull <C extends InputContext<?>> C input(Class<C> clazz);

    default @NotNull MouseInputContext mouse() {
        return input(MouseInputContext.class);
    }

    default @NotNull ScrollInputContext scroll() {
        return input(ScrollInputContext.class);
    }

}
