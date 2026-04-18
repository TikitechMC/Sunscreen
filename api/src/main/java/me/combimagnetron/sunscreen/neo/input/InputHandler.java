package me.combimagnetron.sunscreen.neo.input;

import com.google.common.collect.ImmutableMap;
import me.combimagnetron.passport.event.Dispatcher;
import me.combimagnetron.passport.event.Event;
import me.combimagnetron.passport.event.EventBus;
import me.combimagnetron.passport.event.EventSubscription;
import me.combimagnetron.passport.logic.state.State;
import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.SunscreenLibrary;
import me.combimagnetron.sunscreen.neo.ActiveMenu;
import me.combimagnetron.sunscreen.neo.cursor.CursorStyle;
import me.combimagnetron.sunscreen.neo.editor.input.SelectorInputContext;
import me.combimagnetron.sunscreen.neo.input.context.InputContext;
import me.combimagnetron.sunscreen.neo.input.context.MouseInputContext;
import me.combimagnetron.sunscreen.neo.input.context.ScrollInputContext;
import me.combimagnetron.sunscreen.neo.input.context.TextInputContext;
import me.combimagnetron.sunscreen.user.SunscreenUser;
import me.combimagnetron.sunscreen.util.Scheduler;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.beans.EventHandler;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class InputHandler {
    private final Map<Identifier, List<EventSubscription<?>>> subscriptions = new HashMap<>();
    private final Map<Class<? extends InputContext<?>>, InputContext<?>> inputContextMap = new HashMap<>();
    private final ActiveMenu activeMenu;

    protected InputHandler(ActiveMenu activeMenu) {
        this.activeMenu = activeMenu;
        inputContextMap.put(MouseInputContext.class, new MouseInputContext(false, false, false, Vec2i.zero()));
        inputContextMap.put(ScrollInputContext.class, new ScrollInputContext(false, 0f, 0));
        inputContextMap.put(TextInputContext.class, new TextInputContext(false, State.immutable(""), State.immutable(""), false));
        inputContextMap.put(SelectorInputContext.class, new SelectorInputContext(
            ImmutableMap.of(
                Identifier.of("top_right/size/value"), new SelectorInputContext.ValueInnerContext(new Integer[]{0, 0, 0, 0}),
                Identifier.of("top_right/position/value"), new SelectorInputContext.ValueInnerContext(new Integer[]{0, 0, 0, 0}),
                Identifier.of("top_right/padding_margin/value"), new SelectorInputContext.MultiValueInnerContext(new Integer[]{0, 0, 0, 0, 0, 0, 0, 0})
            ))
        );
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

    public <C extends InputContext<?>, E extends Event> void subscribe(@NotNull Identifier origin, @NotNull Class<C> contextType, @NotNull Consumer<E> consumer) {
        C context = context(contextType);
        subscriptions.putIfAbsent(origin, new ArrayList<>());
        List<EventSubscription<?>> list = subscriptions.get(origin);
        list.add(EventBus.subscribe(context.eventType(), (Consumer) consumer));
    }

    public <E extends Event> void listen(@NotNull Identifier origin, @NotNull Class<E> eventType, @NotNull Consumer<E> consumer) {
        subscriptions.putIfAbsent(origin, new ArrayList<>());
        List<EventSubscription<?>> list = subscriptions.get(origin);
        list.add(EventBus.subscribe(eventType, (Consumer) consumer));
    }

    public void close() {
        for (List<EventSubscription<?>> internal: subscriptions.values()) {
            for (EventSubscription<?> eventSubscription : internal) {
                eventSubscription.close();
            }
        }
    }

    public void unlink(@NotNull Identifier elementId) {
        System.out.println("removing it maybe? " + elementId.string() + " " + subscriptions.get(elementId));
        for (EventSubscription<?> eventSubscription : List.copyOf(subscriptions.get(elementId))) {
            eventSubscription.close();
        }
        subscriptions.remove(elementId);
        System.out.println("removed it maybe? " + elementId.string() + " " + subscriptions.get(elementId));
    }

    public void cursor(@NotNull CursorStyle cursorStyle) {
        activeMenu.cursor(cursorStyle);
    }

    public void anvil(boolean shouldClear) {
        TextInputContext context = context(TextInputContext.class);
        String current = context.stream().value();
        SunscreenLibrary.library().intermediate().openEmptyAnvil(activeMenu.user());
        peek(TextInputContext.class, old -> old.withActive(true), user());
        boolean reset = context(TextInputContext.class).reset();
        if (!shouldClear && reset) {
            Scheduler.delayTick(() -> peek(TextInputContext.class, old -> old.append(current).withReset(true), user()));
        }
    }

    public @NotNull SunscreenUser<?> user() {
        return activeMenu.user();
    }

}