package me.combimagnetron.sunscreen.fabric.mixin;

import me.combimagnetron.sunscreen.fabric.protocol.FabricMenuTimePackets;
import me.combimagnetron.sunscreen.fabric.protocol.FabricMappingCompat;
import me.combimagnetron.sunscreen.fabric.protocol.FabricPacketBridge;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin implements ServerPlayerConnection {
    @Shadow
    public ServerPlayer player;

    @Inject(method = "handleMovePlayer", at = @At("HEAD"))
    private void sunscreen$onMove(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
        FabricPacketBridge.handleRotation(player, packet.getYRot(player.getYRot()), packet.getXRot(player.getXRot()));
    }

    @Inject(method = "handlePlayerCommand", at = @At("HEAD"))
    private void sunscreen$onPlayerCommand(ServerboundPlayerCommandPacket packet, CallbackInfo ci) {
        FabricPacketBridge.handleSneak(player, FabricMappingCompat.isPressShift(packet.getAction()));
    }

    @Inject(method = "handlePlayerAction", at = @At("HEAD"), cancellable = true)
    private void sunscreen$onPlayerAction(ServerboundPlayerActionPacket packet, CallbackInfo ci) {
        boolean cancel = FabricPacketBridge.handleDigging(player, packet);
        if (cancel) {
            ci.cancel();
        }
    }

    @Inject(method = "handleUseItem", at = @At("HEAD"), cancellable = true)
    private void sunscreen$onUseItem(ServerboundUseItemPacket packet, CallbackInfo ci) {
        if (FabricPacketBridge.handleUseItem(player, packet.getHand())) {
            ci.cancel();
        }
    }

    @Inject(method = "handleInteract", at = @At("HEAD"), cancellable = true)
    private void sunscreen$onInteract(ServerboundInteractPacket packet, CallbackInfo ci) {
        if (FabricPacketBridge.handleInteractEntity(player, packet)) {
            ci.cancel();
        }
    }

    @Inject(method = "handleRenameItem", at = @At("HEAD"))
    private void sunscreen$onRename(ServerboundRenameItemPacket packet, CallbackInfo ci) {
        FabricPacketBridge.handleNameItem(player, packet.getName());
    }

    @Inject(method = "handleSetCarriedItem", at = @At("HEAD"))
    private void sunscreen$onSetCarriedItem(ServerboundSetCarriedItemPacket packet, CallbackInfo ci) {
        FabricPacketBridge.handleSlotChange(player, packet.getSlot());
    }

    @Inject(method = "handleContainerClick", at = @At("HEAD"))
    private void sunscreen$onContainerClick(ServerboundContainerClickPacket packet, CallbackInfo ci) {
        FabricPacketBridge.handleContainerClick(player, FabricMappingCompat.containerId(packet));
    }

    @Inject(method = "handleContainerClose", at = @At("HEAD"))
    private void sunscreen$onContainerClose(ServerboundContainerClosePacket packet, CallbackInfo ci) {
        FabricPacketBridge.handleContainerClose(player);
    }
}
