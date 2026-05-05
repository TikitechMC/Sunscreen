package me.combimagnetron.sunscreen.fabric.protocol;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.component.CustomModelData;
#if MC_1_21_1
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
#elif MC_1_21_11
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import java.util.List;
#endif

import java.util.UUID;

public final class FabricMappingCompat {

    private FabricMappingCompat() {
    }

    public static UUID gameProfileId(GameProfile profile) {
        #if MC_1_21_1
        return profile.getId();
        #elif MC_1_21_11
        return profile.id();
        #else
        throw new IllegalStateException("Unsupported minecraft version");
        #endif
    }

    public static String gameProfileName(GameProfile profile) {
        #if MC_1_21_1
        return profile.getName();
        #elif MC_1_21_11
        return profile.name();
        #else
        throw new IllegalStateException("Unsupported minecraft version");
        #endif
    }

    public static Object gameProfileProperties(GameProfile profile) {
        #if MC_1_21_1
        return profile.getProperties();
        #elif MC_1_21_11
        return profile.properties();
        #else
        throw new IllegalStateException("Unsupported minecraft version");
        #endif
    }

    public static ServerLevel serverLevel(ServerPlayer player) {
        return (ServerLevel) player.level();
    }

    public static <T extends Entity> T create(ServerLevel level, EntityType<T> type) {
        #if MC_1_21_1
        return type.create(level);
        #elif MC_1_21_11
        return type.create(level, EntitySpawnReason.COMMAND);
        #else
        throw new IllegalStateException("Unsupported minecraft version");
        #endif
    }

    public static void startRiding(ServerPlayer player, Entity mount) {
        #if MC_1_21_1
        player.startRiding(mount, true);
        #elif MC_1_21_11
        player.startRiding(mount, true, true);
        #else
        throw new IllegalStateException("Unsupported minecraft version");
        #endif
    }

    public static void teleport(ServerPlayer player, ServerLevel level, double x, double y, double z, float yaw, float pitch) {
        #if MC_1_21_1
        player.teleportTo(level, x, y, z, java.util.Set.of(), yaw, pitch);
        #elif MC_1_21_11
        player.teleportTo(level, x, y, z, java.util.Set.of(), yaw, pitch, false);
        #else
        throw new IllegalStateException("Unsupported minecraft version");
        #endif
    }

    public static int containerId(ServerboundContainerClickPacket packet) {
        #if MC_1_21_1
        return packet.getContainerId();
        #elif MC_1_21_11
        return packet.containerId();
        #else
        throw new IllegalStateException("Unsupported minecraft version");
        #endif
    }

    public static boolean isPressShift(Object action) {
        String name = String.valueOf(action);
        return "PRESS_SHIFT_KEY".equals(name) || "PRESS_SHIFT".equals(name);
    }

    public static long getDayTime(ClientboundSetTimePacket packet) {
        #if MC_1_21_1
        return packet.getDayTime();
        #elif MC_1_21_11
        return packet.dayTime();
        #else
        throw new IllegalStateException("Unsupported minecraft version");
        #endif
    }

    public static ClientboundPlayerInfoUpdatePacket.Entry playerInfoEntry(
        UUID uuid,
        GameProfile profile,
        boolean listed,
        int latency,
        Object gameType,
        Object displayName
    ) {
        #if MC_1_21_1
        return new ClientboundPlayerInfoUpdatePacket.Entry(
            uuid,
            profile,
            listed,
            latency,
            (net.minecraft.world.level.GameType) gameType,
            (net.minecraft.network.chat.Component) displayName,
            null
        );
        #elif MC_1_21_11
        return new ClientboundPlayerInfoUpdatePacket.Entry(
            uuid,
            profile,
            listed,
            latency,
            (net.minecraft.world.level.GameType) gameType,
            (net.minecraft.network.chat.Component) displayName,
            false,
            0,
            null
        );
        #else
        throw new IllegalStateException("Unsupported minecraft version");
        #endif
    }

    public static Packet<?> createSetCarriedItemPacket(int slot) {
        #if MC_1_21_1
        return new ClientboundSetCarriedItemPacket(slot);
        #elif MC_1_21_11
        return new ClientboundSetHeldSlotPacket(slot);
        #else
        throw new IllegalStateException("Unsupported minecraft version");
        #endif
    }

    public static Object parse(String id) {
        #if MC_1_21_1
        return ResourceLocation.parse(id);
        #elif MC_1_21_11
        return Identifier.parse(id);
        #else
        throw new IllegalStateException("Unsupported minecraft version");
        #endif
    }

    public static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> customPayloadType(String id) {
        #if MC_1_21_1
        return new CustomPacketPayload.Type<>((ResourceLocation) parse(id));
        #elif MC_1_21_11
        return new CustomPacketPayload.Type<>((Identifier) parse(id));
        #else
        throw new IllegalStateException("Unsupported minecraft version");
        #endif
    }

    public static String namespacedPath(Object key) {
        #if MC_1_21_1
        return ((ResourceLocation) key).getPath();
        #elif MC_1_21_11
        return ((Identifier) key).getPath();
        #else
        throw new IllegalStateException("Unsupported minecraft version");
        #endif
    }

    public static CustomModelData customModelData(int id) {
        #if MC_1_21_1
        return new CustomModelData(id);
        #elif MC_1_21_11
        return new CustomModelData(List.of(), List.of(), List.of(), List.of(id));
        #else
        throw new IllegalStateException("Unsupported minecraft version");
        #endif
    }
}
