package me.combimagnetron.sunscreen.neo.input.context;

import me.combimagnetron.passport.event.Event;
import me.combimagnetron.passport.event.EventBus;
import me.combimagnetron.sunscreen.user.SunscreenUser;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface InputContext<E extends Event> {

    boolean active();

    @NotNull
    Class<E> eventType();

    @NotNull
    E constructEvent(@NotNull SunscreenUser<?> user);

    default void listen(@NotNull Consumer<E> eventConsumer) {
        EventBus.subscribe(eventType(), eventConsumer);
    }

}
