package me.combimagnetron.sunscreen;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import me.combimagnetron.passport.Passport;
import me.combimagnetron.passport.user.UserHandler;
import me.combimagnetron.passport.util.placeholder.PlaceholderRegistry;
import me.combimagnetron.sunscreen.neo.protocol.PlatformProtocolIntermediate;
import me.combimagnetron.sunscreen.protocol.SpigotPlatformProtocolIntermediate;
import me.combimagnetron.sunscreen.neo.session.SessionHandler;
import me.combimagnetron.sunscreen.user.SunscreenUser;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.InputStream;
import java.nio.file.Path;

public class SunscreenLibrarySpigot implements SunscreenLibrary<SunscreenPlugin, Player, ItemStack> {
    private final SpigotPlatformProtocolIntermediate intermediate = new SpigotPlatformProtocolIntermediate();
    private final SessionHandler sessionHandler = new SessionHandler();
    private final PlaceholderRegistry placeholderRegistry = new PlaceholderRegistry.Impl();
    private final SunscreenPlugin plugin;

    public SunscreenLibrarySpigot(SunscreenPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public me.combimagnetron.passport.Passport<SunscreenPlugin> passport() {
        return new Passport<>() {
            @Override
            public UserHandler<Player, SunscreenUser<Player>> users() {
                return plugin.userManager();
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
            public PacketEventsAPI<?> packetEventsApi() {
                return PacketEvents.getAPI();
            }

            @Override
            public SunscreenPlugin plugin() {
                return plugin;
            }
        };
    }

    @Override
    public Path path() {
        return plugin.getDataFolder().toPath();
    }

    @Override
    public @NotNull SunscreenPlugin plugin() {
        return plugin;
    }

    @Override
    public @Nullable InputStream resource(String path) {
        return plugin.getResource(path);
    }

    @Override
    public @NotNull SessionHandler sessionHandler() {
        return sessionHandler;
    }

    @Override
    public @NotNull UserHandler<Player, SunscreenUser<Player>> users() {
        return plugin.userManager();
    }

    @Override
    public @NotNull Logger logger() {
        return plugin.getComponentLogger();
    }

    @Override
    public @NotNull PlatformProtocolIntermediate<ItemStack> intermediate() {
        return intermediate;
    }

}
