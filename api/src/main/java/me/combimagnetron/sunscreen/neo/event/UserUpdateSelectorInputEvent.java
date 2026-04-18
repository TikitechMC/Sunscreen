package me.combimagnetron.sunscreen.neo.event;

import me.combimagnetron.passport.event.Event;
import me.combimagnetron.sunscreen.neo.editor.input.SelectorInputContext;
import me.combimagnetron.sunscreen.user.SunscreenUser;
import org.jetbrains.annotations.NotNull;

public record UserUpdateSelectorInputEvent(@NotNull SunscreenUser<?> user, @NotNull SelectorInputContext context) implements UserEvent {

    @Override
    public Class<? extends Event> eventType() {
        return UserUpdateSelectorInputEvent.class;
    }

}
