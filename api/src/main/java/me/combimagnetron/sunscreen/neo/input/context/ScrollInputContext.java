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
        boolean forwards = newSlot == (lastSlot + 1) % 9;
        boolean backwards = newSlot == (lastSlot + 8) % 9;
        if (!forwards && !backwards) {
            return this;
        }
        return new ScrollInputContext(true, forwards ? 1 : -1, newSlot);
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