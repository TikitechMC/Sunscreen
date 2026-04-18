package me.combimagnetron.sunscreen.neo.editor.input;

import com.google.common.collect.ImmutableMap;
import me.combimagnetron.passport.event.Event;
import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.sunscreen.neo.event.UserUpdateSelectorInputEvent;
import me.combimagnetron.sunscreen.neo.input.context.InputContext;
import me.combimagnetron.sunscreen.user.SunscreenUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public record SelectorInputContext(@NotNull ImmutableMap<Identifier, InnerContext<?>> innerContexts) implements InputContext<UserUpdateSelectorInputEvent> {

    public @NotNull SelectorInputContext inner(@NotNull Identifier id, @NotNull InnerContext<?> context) {
        var map = new java.util.HashMap<>(innerContexts);
        map.put(id, context);
        return new SelectorInputContext(ImmutableMap.copyOf(map));
    }

    @Override
    public boolean active() {
        return false;
    }

    @Override
    public @NotNull Class<UserUpdateSelectorInputEvent> eventType() {
        return UserUpdateSelectorInputEvent.class;
    }

    @Override
    public @NonNull UserUpdateSelectorInputEvent constructEvent(@NotNull SunscreenUser<?> user) {
        return new UserUpdateSelectorInputEvent(user, this);
    }

    public interface InnerContext<T> {

        @NotNull T @NotNull [] values();

        int size();

        default @Nullable T index(int index) {
            if (index > size() - 1) return null;
            return values()[index];
        }

    }

    public record MultiValueInnerContext(Integer[] values) implements InnerContext<Integer> {
        private final static int SIZE = 8;

        @Override
        public int size() {
            return SIZE;
        }

    }


    public record ValueInnerContext(Integer[] values) implements InnerContext<Integer> {
        private final static int SIZE = 4;

        @Override
        public int size() {
            return SIZE;
        }

    }

}
