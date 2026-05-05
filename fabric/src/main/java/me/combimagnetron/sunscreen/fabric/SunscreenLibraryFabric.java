package me.combimagnetron.sunscreen.fabric;

import me.combimagnetron.passport.Passport;
import me.combimagnetron.passport.user.UserHandler;
import me.combimagnetron.passport.util.placeholder.PlaceholderRegistry;
import me.combimagnetron.sunscreen.SunscreenLibrary;
import me.combimagnetron.sunscreen.fabric.protocol.FabricPlatformProtocolIntermediate;
import me.combimagnetron.sunscreen.fabric.user.UserImpl;
import me.combimagnetron.sunscreen.fabric.user.UserManager;
import me.combimagnetron.sunscreen.neo.protocol.PlatformProtocolIntermediate;
import me.combimagnetron.sunscreen.neo.session.SessionHandler;
import me.combimagnetron.sunscreen.user.SunscreenUser;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.audience.Audience;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Path;

public final class SunscreenLibraryFabric implements SunscreenLibrary<Object, Audience, ItemStack> {
    private final FabricPlatformProtocolIntermediate intermediate = new FabricPlatformProtocolIntermediate();
    private final SessionHandler sessionHandler = new SessionHandler();
    private final PlaceholderRegistry placeholderRegistry = new PlaceholderRegistry.Impl();
    private final UserManager userManager = new UserManager();
    private final Object plugin;
    private final Logger logger = LoggerFactory.getLogger("Sunscreen");

    public SunscreenLibraryFabric(Object plugin) {
        this.plugin = plugin;
    }

    @Override
    public Passport<Object> passport() {
        return new Passport<>() {
            @Override
            public UserHandler<Audience, SunscreenUser<Audience>> users() {
                return userManager;
            }

            @Override
            public Path dataFolder() {
                return path();
            }

            @Override
            public PlaceholderRegistry placeholders() {
                return placeholderRegistry;
            }

            @Override
            public Object packetEventsApi() {
                return null;
            }

            @Override
            public Object plugin() {
                return plugin;
            }
        };
    }

    @Override
    public Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("sunscreen");
    }

    @Override
    public @NotNull Object plugin() {
        return plugin;
    }

    @Override
    public @Nullable InputStream resource(String path) {
        String resourcePath = path.startsWith("/") ? path : "/" + path;
        return SunscreenLibraryFabric.class.getResourceAsStream(resourcePath);
    }

    @Override
    public @NotNull SessionHandler sessionHandler() {
        return sessionHandler;
    }

    @Override
    public @NotNull UserHandler<Audience, SunscreenUser<Audience>> users() {
        return userManager;
    }

    @Override
    public @NotNull Logger logger() {
        return logger;
    }

    @Override
    public @NotNull PlatformProtocolIntermediate<ItemStack> intermediate() {
        return intermediate;
    }

    public UserManager userManager() {
        return userManager;
    }

    public @Nullable UserImpl user(java.util.UUID uuid) {
        return userManager.userRaw(uuid);
    }
}
