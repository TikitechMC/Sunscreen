package me.combimagnetron.sunscreen.user;

import me.combimagnetron.passport.internal.network.ByteBuffer;
import me.combimagnetron.passport.user.UserHandler;
import me.combimagnetron.sunscreen.SunscreenPlugin;
import me.combimagnetron.sunscreen.neo.session.Session;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class UserManager implements Listener, UserHandler<Player, SunscreenUser<Player>> {
    private final Map<UUID, SunscreenUser<Player>> userMap = new HashMap<>();

    public UserManager(SunscreenPlugin library) {
        Bukkit.getServer().getPluginManager().registerEvents(this, library);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        SunscreenUser<Player> user = UserImpl.of(player);
        userMap.put(player.getUniqueId(), user);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        SunscreenUser<Player> user = userMap.get(player.getUniqueId());
        if (user == null) {
            userMap.remove(player.getUniqueId());
            return;
        }

        Session session = user.session();
        if (session != null) {
            session.menu().close();
        }
        userMap.remove(player.getUniqueId());
    }

    public SunscreenUser<Player> user(Player player) {
        return userMap.get(player.getUniqueId());
    }

    @Override
    public Optional<SunscreenUser<Player>> user(UUID uuid) {
        return Optional.ofNullable(userMap.get(uuid));
    }

    @Override
    public Optional<SunscreenUser<Player>> user(String s) {
        final Player player = Bukkit.getPlayer(s);
        if (player == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(userMap.get(player.getUniqueId()));
    }

    @Override
    public Collection<SunscreenUser<Player>> users() {
        return userMap.values();
    }

    @Override
    public Collection<SunscreenUser<Player>> global() {
        return users();
    }

    @Override
    public SunscreenUser<Player> deserialize(ByteBuffer byteBuffer) {
        return null;
    }

}
