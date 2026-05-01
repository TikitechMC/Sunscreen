package me.combimagnetron.sunscreen;

import me.combimagnetron.passport.Passport;
import me.combimagnetron.passport.user.UserHandler;
import me.combimagnetron.sunscreen.neo.editor.virtual.argument.ElementConstructionProvider;
import me.combimagnetron.sunscreen.neo.protocol.PlatformProtocolIntermediate;
import me.combimagnetron.sunscreen.neo.session.SessionHandler;
import me.combimagnetron.sunscreen.user.SunscreenUser;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.InputStream;
import java.nio.file.Path;

public interface SunscreenLibrary<T, P extends Audience, I> {
    static <T, P extends Audience, I> SunscreenLibrary<T, P, I> library() {
        return (SunscreenLibrary<T, P, I>) Holder.INSTANCE;
    }

    Passport<T> passport();

    Path path();

    @NotNull T plugin();

    @Nullable InputStream resource(String path);

    @NotNull SessionHandler sessionHandler();

    @NotNull UserHandler<P, SunscreenUser<P>> users();

    @NotNull Logger logger();

    @NotNull PlatformProtocolIntermediate<I> intermediate();

    final class Holder {
        public static SunscreenLibrary<?, ? extends Audience, ?> INSTANCE = null;

        static {
            ElementConstructionProvider.defaults();
        }

    }

}
