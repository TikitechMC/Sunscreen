package me.combimagnetron.sunscreen;

import me.combimagnetron.passport.Passport;
import me.combimagnetron.passport.user.User;
import me.combimagnetron.passport.user.UserHandler;
import me.combimagnetron.passport.util.placeholder.PlaceholderRegistry;
import me.combimagnetron.sunscreen.neo.protocol.PlatformProtocolIntermediate;
import me.combimagnetron.sunscreen.neo.session.SessionHandler;
import me.combimagnetron.sunscreen.user.SunscreenUser;
import me.combimagnetron.sunscreen.user.UserManager;
import net.kyori.adventure.audience.Audience;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.InputStream;
import java.nio.file.Path;

public class SunscreenLibraryMinestom implements SunscreenLibrary<Object, Player, ItemStack> {
    private final MinestomPlatformProtocolIntermediate protocolIntermediate = new MinestomPlatformProtocolIntermediate();
    private final SessionHandler sessionHandler = new SessionHandler();
    private final UserManager userManager = new UserManager();

    protected SunscreenLibraryMinestom() {
        Holder.INSTANCE = this;
    }


    @Override
    public Passport<Object> passport() {
        return new Passport<>() {
            @Override
            public UserHandler<? extends Audience, ? extends User<? extends Audience>> users() {
                return userManager;
            }

            @Override
            public Path dataFolder() {
                return null;
            }

            @Override
            public PlaceholderRegistry placeholders() {
                return null;
            }

            @Override
            public Object packetEventsApi() {
                return null;
            }

            @Override
            public Object plugin() {
                return null;
            }
        };
    }

    @Override
    public Path path() {
        return null;
    }

    @Override
    public @NotNull Object plugin() {
        return null;
    }

    @Override
    public @Nullable InputStream resource(String path) {
        String resourcePath = path.startsWith("/") ? path : "/" + path;
        return SunscreenLibraryMinestom.class.getResourceAsStream(resourcePath);
    }

    @Override
    public @NotNull SessionHandler sessionHandler() {
        return sessionHandler;
    }

    @Override
    public @NotNull UserHandler<Player, SunscreenUser<Player>> users() {
        return userManager;
    }

    @Override
    public @NotNull Logger logger() {
        return MinecraftServer.LOGGER;
    }

    @Override
    public @NotNull PlatformProtocolIntermediate<ItemStack> intermediate() {
        return protocolIntermediate;
    }

}
