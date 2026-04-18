package me.combimagnetron.sunscreen.neo.input.base.impl;

import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.event.UserMoveStateChangeEvent;
import me.combimagnetron.sunscreen.neo.input.base.Action;
import me.combimagnetron.sunscreen.neo.input.base.Module;
import org.jetbrains.annotations.NotNull;

public class DragModule extends Module {

    protected DragModule(Vec2i position, Vec2i size) {
        super(position, size);
    }

    @Override
    public Action<?> handle(@NotNull UserMoveStateChangeEvent event) {
        return null;
    }

    @Override
    public int priority() {
        return 200;
    }

}
