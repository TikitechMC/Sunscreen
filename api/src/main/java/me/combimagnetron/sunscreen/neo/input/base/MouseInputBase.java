package me.combimagnetron.sunscreen.neo.input.base;

import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.element.GenericInteractableModernElement;
import me.combimagnetron.sunscreen.neo.event.UserMoveStateChangeEvent;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.input.context.MouseInputContext;
import me.combimagnetron.sunscreen.user.SunscreenUser;
import me.combimagnetron.sunscreen.util.helper.HoverHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.TreeSet;

public class MouseInputBase {
    private final TreeSet<Module> modules = new TreeSet<>();
    private final GenericInteractableModernElement<?, Canvas, ?> base;

    public static @NotNull MouseInputBase defaults(@NotNull GenericInteractableModernElement<?, Canvas, ?> base) {
        return new MouseInputBase(base);
    }

    public @NotNull MouseInputBase module(@NotNull Module module) {
        modules.add(module);
        return this;
    }

    private MouseInputBase(@NotNull GenericInteractableModernElement<?, Canvas, ?> base) {
        this.base = base;
    }

    public @Nullable Action<?> tick(@NotNull UserMoveStateChangeEvent event, @NotNull SunscreenUser<?> user) {
        if (base.inputHandler().user() != user) return null;
        final MouseInputContext context = event.context();
        final Vec2i cursor = context.position();
        for (Module module : modules) {
            if (!HoverHelper.in(module.position(), module.size(), cursor)) continue;
            module.handle(event);
        }
        return null;
    }

}
