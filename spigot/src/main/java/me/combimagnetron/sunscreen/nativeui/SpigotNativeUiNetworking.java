package me.combimagnetron.sunscreen.nativeui;

import me.combimagnetron.sunscreen.SunscreenPlugin;
import me.combimagnetron.sunscreen.user.UserImpl;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class SpigotNativeUiNetworking implements PluginMessageListener {
    private final SunscreenPlugin plugin;

    public SpigotNativeUiNetworking(@NotNull SunscreenPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerChannels() {
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, NativeUi.CHANNEL, this);
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, NativeUi.CHANNEL);
    }

    public void unregisterChannels() {
        plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin, NativeUi.CHANNEL, this);
        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin, NativeUi.CHANNEL);
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NotNull [] message) {
        if (!NativeUi.CHANNEL.equals(channel)) {
            return;
        }
        try {
            Optional<?> userOpt = me.combimagnetron.sunscreen.SunscreenLibrary.library().users().user(player.getUniqueId());
            if (userOpt.isEmpty()) {
                return;
            }
            Object u = userOpt.get();
            if (!(u instanceof UserImpl user)) {
                return;
            }
            NativeUi.receiveServerC2SPacket(user, message);
        } catch (RuntimeException ex) {
            plugin.getComponentLogger().warn("Invalid native UI chunk from {}", player.getName(), ex);
        }
    }
}
