package me.combimagnetron.sunscreen.neo.input.base;

import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.event.UserMoveStateChangeEvent;
import me.combimagnetron.sunscreen.neo.input.base.impl.ClickModule;
import org.jetbrains.annotations.NotNull;

public abstract class Module implements Comparable<Module> {
    private final Vec2i position;
    private final Vec2i size;

    protected Module(Vec2i position, Vec2i size) {
        this.position = position;
        this.size = size;
    }

    public abstract Action<?> handle(@NotNull UserMoveStateChangeEvent event);

    public abstract int priority();

    public @NotNull Vec2i size() {
        return size;
    }

    public @NotNull Vec2i position() {
        return position;
    }

    @Override
    public int compareTo(@NotNull Module o) {
        return priority() - o.priority();
    }

}
