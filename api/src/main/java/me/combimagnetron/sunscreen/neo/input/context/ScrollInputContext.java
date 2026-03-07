package me.combimagnetron.sunscreen.neo.input.context;

import me.combimagnetron.sunscreen.neo.event.UserScrollStateChangeEvent;
import me.combimagnetron.sunscreen.user.SunscreenUser;
import org.jetbrains.annotations.NotNull;

public record ScrollInputContext(boolean active, float value, int lastSlot) implements InputContext<UserScrollStateChangeEvent> {

    private static final int HOTBAR_SIZE = 9;

    public static ScrollInputContext empty() {
        return new ScrollInputContext(false, 0f, 0);
    }

    public @NotNull ScrollInputContext onSlotChange(int newSlot) {
        int raw = newSlot - lastSlot;
        int delta = Math.abs(raw) > HOTBAR_SIZE / 2
            ? (raw > 0 ? raw - HOTBAR_SIZE : raw + HOTBAR_SIZE)
            : raw;

        if (delta == 0) return this;

        return new ScrollInputContext(true, Math.signum(delta), newSlot);
    }

    @Override
    public @NotNull Class<UserScrollStateChangeEvent> eventType() {
        return UserScrollStateChangeEvent.class;
    }

    @Override
    public @NotNull UserScrollStateChangeEvent constructEvent(@NotNull SunscreenUser<?> user) {
        return new UserScrollStateChangeEvent(user, this);
    }

    public @NotNull ScrollInputContext withActive(boolean active) {
        return new ScrollInputContext(active, value, lastSlot);
    }

    public @NotNull ScrollInputContext withValue(float value) {
        return new ScrollInputContext(active, value, lastSlot);
    }

}