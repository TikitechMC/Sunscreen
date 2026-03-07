package me.combimagnetron.sunscreen.neo.event;

import me.combimagnetron.passport.event.Event;
import me.combimagnetron.sunscreen.user.SunscreenUser;

public record UserFinishTextInputEvent(SunscreenUser<?> user) implements UserEvent {

    @Override
    public Class<? extends Event> eventType() {
        return UserFinishTextInputEvent.class;
    }

}
