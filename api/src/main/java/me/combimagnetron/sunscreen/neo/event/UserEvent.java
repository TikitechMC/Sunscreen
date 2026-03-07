package me.combimagnetron.sunscreen.neo.event;

import me.combimagnetron.passport.event.Event;
import me.combimagnetron.sunscreen.neo.ActiveMenu;
import me.combimagnetron.sunscreen.user.SunscreenUser;
import org.jetbrains.annotations.NotNull;

public interface UserEvent extends Event {

    @NotNull SunscreenUser<?> user();

    default @NotNull ActiveMenu menu() {
        return user().session().menu();
    }

}
