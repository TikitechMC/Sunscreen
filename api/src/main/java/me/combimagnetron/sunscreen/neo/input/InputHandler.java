package me.combimagnetron.sunscreen.neo.input;

import me.combimagnetron.passport.event.Dispatcher;
import me.combimagnetron.passport.event.Event;
import me.combimagnetron.passport.event.EventBus;
import me.combimagnetron.passport.event.EventSubscription;
import me.combimagnetron.passport.logic.state.State;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.SunscreenLibrary;
import me.combimagnetron.sunscreen.neo.ActiveMenu;
import me.combimagnetron.sunscreen.neo.cursor.CursorStyle;
import me.combimagnetron.sunscreen.neo.input.context.InputContext;
import me.combimagnetron.sunscreen.neo.input.context.MouseInputContext;
import me.combimagnetron.sunscreen.neo.input.context.ScrollInputContext;
import me.combimagnetron.sunscreen.neo.input.context.TextInputContext;
import me.combimagnetron.sunscreen.user.SunscreenUser;
import me.combimagnetron.sunscreen.util.Scheduler;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.beans.EventHandler;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class InputHandler {
    private final Collection<EventSubscription<?>> subscriptions = new HashSet<>();
    private final Map<Class<? extends InputContext<?>>, InputContext<?>> inputContextMap = new HashMap<>();
    private final ActiveMenu activeMenu;

    protected InputHandler(ActiveMenu activeMenu) {
        this.activeMenu = activeMenu;
        inputContextMap.put(MouseInputContext.class, new MouseInputContext(false, false, false, Vec2i.zero()));
        inputContextMap.put(ScrollInputContext.class, new ScrollInputContext(false, 0f, 0));
        inputContextMap.put(TextInputContext.class, new TextInputContext(false, State.immutable(""), State.immutable(""), false));
    }

    public static @NotNull InputHandler defaults(@NotNull ActiveMenu activeMenu) {
        return new InputHandler(activeMenu);
    }

    public <C extends InputContext<?>> @NotNull InputHandler add(@NotNull Class<C> type, C context) {
        inputContextMap.put(type, context);
        return this;
    }

    public <C extends InputContext<?>> @NotNull C peek(@NotNull Class<C> type, @NotNull Function<C, C> function, @NotNull SunscreenUser<?> user) {
        C currentInput = (C) inputContextMap.get(type);
        C mutatedInput = function.apply(currentInput);
        inputContextMap.put(type, mutatedInput);
        Event event = mutatedInput.constructEvent(user);
        Dispatcher.dispatcher().post(currentInput.eventType(), event);
        return mutatedInput;
    }

    public <C extends InputContext<?>> @NotNull C context(@NotNull Class<C> type) {
        return (C) inputContextMap.get(type);
    }

    public <C extends InputContext<?>, E extends Event> void subscribe(@NotNull Class<C> contextType, @NotNull Consumer<E> consumer) {
        C context = context(contextType);
        subscriptions.add(EventBus.subscribe(context.eventType(), (Consumer) consumer));
    }

    public void close() {
        for (EventSubscription<?> subscription : subscriptions) {
            subscription.close();
        }
    }

    public void cursor(@NotNull CursorStyle cursorStyle) {
        activeMenu.cursor(cursorStyle);
    }

    public void anvil(boolean shouldClear) {
        TextInputContext context = context(TextInputContext.class);
        String current = context.stream().value();
        SunscreenLibrary.library().intermediate().openEmptyAnvil(activeMenu.user());
        peek(TextInputContext.class, old -> old.withActive(true), user());
        boolean reset = !context(TextInputContext.class).reset();
        if (!shouldClear && reset) {
           Scheduler.delayTick(() -> peek(TextInputContext.class, old -> old.append(current).withReset(true), user()));
        }
    }

    public @NotNull SunscreenUser<?> user() {
        return activeMenu.user();
    }

}
