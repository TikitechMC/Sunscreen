package me.combimagnetron.sunscreen.fabric.mixin;

import com.mojang.authlib.GameProfile;
import me.combimagnetron.sunscreen.fabric.protocol.FabricMappingCompat;
import me.combimagnetron.sunscreen.fabric.protocol.FabricMenuTimePackets;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonPacketListenerImplMixin {

    @Shadow
    protected abstract GameProfile playerProfile();

    @Shadow
    @Final
    protected MinecraftServer server;

    @ModifyVariable(method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V", at = @At("HEAD"), argsOnly = true)
    private Packet<?> sunscreen$rewriteMenuWorldTime(Packet<?> packet) {
        var player = server.getPlayerList().getPlayer(FabricMappingCompat.gameProfileId(playerProfile()));
        return FabricMenuTimePackets.maybeRewriteTimePacket(player, packet);
    }
}
