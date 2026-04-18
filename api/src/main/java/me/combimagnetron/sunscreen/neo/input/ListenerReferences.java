package me.combimagnetron.sunscreen.neo.input;

import me.combimagnetron.passport.event.Event;
import me.combimagnetron.passport.event.EventBus;
import me.combimagnetron.passport.event.EventFilter;
import me.combimagnetron.sunscreen.neo.element.ModernElement;
import me.combimagnetron.sunscreen.neo.event.UserMoveStateChangeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class ListenerReferences<T extends ModernElement<T, ?>, S extends ListenerReferences<T, S>> {
    private final Map<Class<? extends Event>, Consumer<?>> tempMap = new HashMap<>();

    public abstract T back();

    public S mouse(@NotNull Consumer<UserMoveStateChangeEvent> eventConsumer) {
        tempMap.put(UserMoveStateChangeEvent.class, eventConsumer);
        return (S) this;
    }

    protected void put(Class<? extends Event> type, Consumer<?> consumer) {
        tempMap.put(type, consumer);
    }

    public void subscribe(@NotNull InputHandler handler) {
        for (Map.Entry<Class<? extends Event>, Consumer<?>> classConsumerEntry : tempMap.entrySet()) {
            handler.listen(back().identifier() ,classConsumerEntry.getKey(), (Consumer) classConsumerEntry.getValue());
        }
    }

}
