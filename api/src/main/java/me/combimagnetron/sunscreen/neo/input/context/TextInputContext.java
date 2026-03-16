package me.combimagnetron.sunscreen.neo.input.context;

import me.combimagnetron.passport.logic.state.State;
import me.combimagnetron.sunscreen.neo.event.UserTextStateChangeEvent;
import me.combimagnetron.sunscreen.user.SunscreenUser;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public record TextInputContext(boolean active, @NotNull State<String> stream, @NotNull State<String> additional, boolean reset) implements InputContext<UserTextStateChangeEvent> {

    public @NotNull TextInputContext clear() {
        return new TextInputContext(active, State.immutable(""), additional, reset);
    }

    public @NotNull TextInputContext withStream(@NotNull String stream) {
        return new TextInputContext(active, State.immutable(additional.value() + stream), additional, reset);
    }

    public @NotNull TextInputContext withStreamSet(@NotNull String stream) {
        return new TextInputContext(active, this.stream, State.immutable(stream), reset);
    }

    public @NotNull TextInputContext withActive(boolean active) {
        return new TextInputContext(active, stream, additional, reset);
    }

    public @NotNull TextInputContext append(@NotNull String content) {
        return new TextInputContext(active, State.immutable(stream.value() + content), State.immutable(additional.value() + content), reset);
    }

    public @NotNull TextInputContext withReset(boolean reset) {
        return new TextInputContext(active, stream, additional, reset);
    }

    public @NotNull Collection<String> streamWithLineBreaks(int lineWidth) {
        List<String> result = new ArrayList<>();
        if (lineWidth <= 0) {
            return result;
        }
        final String value = stream.value();
        for (int i = 0; i < value.length(); i += lineWidth) {
            result.add(value.substring(i, Math.min(i + lineWidth, value.length())));
        }
        return result;
    }

    @Override
    public boolean active() {
        return active;
    }

    @Override
    public @NotNull Class<UserTextStateChangeEvent> eventType() {
        return UserTextStateChangeEvent.class;
    }

    @Override
    public @NonNull UserTextStateChangeEvent constructEvent(@NotNull SunscreenUser<?> user) {
        return new UserTextStateChangeEvent(user, this);
    }

}
