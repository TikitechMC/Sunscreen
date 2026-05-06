package me.combimagnetron.sunscreen.fabric.user;

import me.combimagnetron.passport.internal.network.ByteBuffer;
import me.combimagnetron.passport.user.UserHandler;
import me.combimagnetron.sunscreen.neo.session.Session;
import me.combimagnetron.sunscreen.user.SunscreenUser;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.kyori.adventure.audience.Audience;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class UserManager implements UserHandler<Audience, SunscreenUser<Audience>> {
    private final Map<UUID, UserImpl> userMap = new ConcurrentHashMap<>();

    public UserManager() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ensureUser(handler.getPlayer());
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer player = handler.getPlayer();
            UserImpl user = userMap.remove(player.getUUID());
            if (user == null) {
                return;
            }
            user.setNativeUi(false);
            Session session = user.session();
            if (session != null) {
                session.menu().close();
            }
        });
    }

    /**
     * Returns the Sunscreen user for this player, creating the {@link UserImpl} if missing.
     * Used so early packets (e.g. native UI handshake right after play) never see a null user before JOIN runs.
     */
    public @NotNull UserImpl ensureUser(@NotNull ServerPlayer player) {
        return userMap.computeIfAbsent(player.getUUID(), id -> UserImpl.of(player));
    }

    public UserImpl user(ServerPlayer player) {
        return userMap.get(player.getUUID());
    }

    public UserImpl userRaw(UUID uuid) {
        return userMap.get(uuid);
    }

    @Override
    public SunscreenUser<Audience> user(Audience audience) {
        if (audience instanceof UserImpl.PlayerAudience playerAudience) {
            return userMap.get(playerAudience.player().getUUID());
        }
        return null;
    }

    @Override
    public Optional<SunscreenUser<Audience>> user(UUID uuid) {
        return Optional.ofNullable(userMap.get(uuid));
    }

    @Override
    public Optional<SunscreenUser<Audience>> user(String s) {
        return userMap.values().stream().filter(user -> user.name().equalsIgnoreCase(s)).findFirst().map(user -> user);
    }

    @Override
    public Collection<SunscreenUser<Audience>> users() {
        return java.util.Collections.unmodifiableCollection(new java.util.ArrayList<>(userMap.values()));
    }

    @Override
    public Collection<SunscreenUser<Audience>> global() {
        return users();
    }

    @Override
    public SunscreenUser<Audience> deserialize(ByteBuffer byteBuffer) {
        return null;
    }
}
