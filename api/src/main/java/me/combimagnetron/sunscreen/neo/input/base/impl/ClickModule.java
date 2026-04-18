package me.combimagnetron.sunscreen.neo.input.base.impl;

import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.event.UserMoveStateChangeEvent;
import me.combimagnetron.sunscreen.neo.input.base.Action;
import me.combimagnetron.sunscreen.neo.input.base.Module;
import me.combimagnetron.sunscreen.neo.input.context.MouseInputContext;
import org.jetbrains.annotations.NotNull;

public class ClickModule extends Module {
    private int click = 0;

    protected ClickModule(Vec2i position, Vec2i size) {
        super(position, size);
    }

    @Override
    public Action<?> handle(@NotNull UserMoveStateChangeEvent event) {
        MouseInputContext context = event.context();
        click--;
        if (context.leftPressed()) {
            click = 3;
        }
        return null;
    }

    @Override
    public int priority() {
        return 100;
    }

}
