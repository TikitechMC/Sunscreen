package me.combimagnetron.sunscreen.fabric.protocol;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.server.level.ServerPlayer;

/**
 * Rewrites vanilla world time packets for players in a Sunscreen menu so the client sees a fixed
 * world age (-50) without changing {@link net.minecraft.server.level.ServerLevel} time.
 */
public final class FabricMenuTimePackets {
    private FabricMenuTimePackets() {
    }

    public static Packet<?> maybeRewriteTimePacket(ServerPlayer player, Packet<?> packet) {
        if (!(packet instanceof ClientboundSetTimePacket setTime)) {
            return packet;
        }
        if (!FabricPacketBridge.inMenu(player)) {
            return packet;
        }
        return new ClientboundSetTimePacket(-50L, FabricMappingCompat.getDayTime(setTime), true);
    }
}
