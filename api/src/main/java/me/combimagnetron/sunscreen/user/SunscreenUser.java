package me.combimagnetron.sunscreen.user;

import me.combimagnetron.passport.user.User;
import me.combimagnetron.sunscreen.neo.protocol.type.Location;
import me.combimagnetron.sunscreen.neo.render.ScreenInfo;
import me.combimagnetron.sunscreen.neo.MenuTemplate;
import me.combimagnetron.sunscreen.neo.session.Session;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface SunscreenUser<T extends Audience> extends User<T> {

    @NotNull ScreenInfo screenInfo();

    @Nullable Session session();

    @NotNull Session open(@NotNull MenuTemplate template);

    @NotNull Location eyeLocation();

    boolean useNativeUi();

    default void setNativeUi(boolean enabled) {
    }

}
