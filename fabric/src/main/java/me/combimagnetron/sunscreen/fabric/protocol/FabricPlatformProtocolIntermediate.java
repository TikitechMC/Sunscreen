package me.combimagnetron.sunscreen.fabric.protocol;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import me.combimagnetron.passport.internal.entity.metadata.type.Vector3d;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.fabric.nativeui.NativeUiChunkC2SPayload;
import me.combimagnetron.sunscreen.fabric.nativeui.NativeUiChunkS2CPayload;
import me.combimagnetron.sunscreen.fabric.user.UserImpl;
import me.combimagnetron.sunscreen.nativeui.NativeUiChunkBinary;
import me.combimagnetron.sunscreen.neo.graphic.Item;
import me.combimagnetron.sunscreen.neo.protocol.PlatformProtocolIntermediate;
import me.combimagnetron.sunscreen.neo.protocol.type.EntityReference;
import me.combimagnetron.sunscreen.neo.protocol.type.Location;
import me.combimagnetron.sunscreen.user.SunscreenUser;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public final class FabricPlatformProtocolIntermediate implements PlatformProtocolIntermediate<ItemStack> {
    private static final int DISPLAY_ENTITY_ID = -10_000;

    private final Table<UUID, Integer, Object> entities = HashBasedTable.create();
    private static Consumer<NativeUiChunkC2SPayload> sender;

    @Override
    public EntityReference<?> spawnAndRideHorse(@NotNull SunscreenUser<?> user, @NotNull Location location) {
        ServerPlayer player = fabricPlayer(user);
        var level = FabricMappingCompat.serverLevel(player);
        net.minecraft.world.entity.Entity horse = FabricMappingCompat.create(level, EntityType.HORSE);
        if (horse == null) {
            throw new IllegalStateException("Unable to spawn horse entity");
        }
        horse.setPos(location.x(), location.y(), location.z());
        horse.setInvisible(true);
        horse.setInvulnerable(true);
        horse.setSilent(true);
        level.addFreshEntity(horse);
        FabricMappingCompat.startRiding(player, horse);
        entities.put(player.getUUID(), horse.getId(), horse);
        return new EntityReference<>(horse.getId(), horse);
    }

    @Override
    public EntityReference<?> spawnAndFillItemFrame(@NotNull SunscreenUser<?> user, @NotNull Location location, byte[] data, int mapId) {
        ServerPlayer player = fabricPlayer(user);
        updateMap(user, mapId, data);

        final BlockPos pos = BlockPos.containing(location.x(), location.y(), location.z());
        final ItemStack map = Items.FILLED_MAP.getDefaultInstance();
        map.set(DataComponents.MAP_ID, new MapId(mapId));

        var level = FabricMappingCompat.serverLevel(player);
        ItemFrame upper = new ItemFrame(level, pos, Direction.UP);
        upper.setInvisible(true);
        upper.setInvulnerable(true);
        upper.setItem(map.copy(), false);

        ItemFrame lower = new ItemFrame(level, pos, Direction.DOWN);
        lower.setInvisible(true);
        lower.setInvulnerable(true);
        lower.setItem(map.copy(), false);

        level.addFreshEntity(upper);
        level.addFreshEntity(lower);
        entities.put(player.getUUID(), upper.getId(), upper);
        entities.put(player.getUUID(), lower.getId(), lower);
        return new EntityReference<>(upper.getId(), upper);
    }

    @Override
    public EntityReference<?> spawnAndSpectateDisplay(@NotNull SunscreenUser<?> user, @NotNull Location location) {
        ServerPlayer player = fabricPlayer(user);
        long dayTimeArg = (long) user.worldTime();
        sendTime(player, -500L, dayTimeArg);
        fakeArmorStandHit(player);
        sendTime(player, -50L, dayTimeArg);
        player.connection.send(cameraPacketFor(player, DISPLAY_ENTITY_ID));
        UUID displayUuid = UUID.randomUUID();
        GameProfile displayProfile = new GameProfile(displayUuid, FabricMappingCompat.gameProfileName(player.getGameProfile()));
        copyProfileProperties(displayProfile, player.getGameProfile());
        ClientboundPlayerInfoUpdatePacket.Entry tabEntry = FabricMappingCompat.playerInfoEntry(
                displayUuid,
                displayProfile,
                false,
                0,
                GameType.CREATIVE,
                Component.empty()
        );
        ClientboundPlayerInfoUpdatePacket addPlayer = playerInfoAddPacket(player, tabEntry);
        ClientboundAddEntityPacket spawn = new ClientboundAddEntityPacket(
            DISPLAY_ENTITY_ID,
            displayUuid,
            player.getX(),
            player.getY(),
            player.getZ(),
            player.getXRot(),
            player.getYRot(),
            EntityType.PLAYER,
            0,
            Vec3.ZERO,
            player.getYHeadRot()
        );
        ClientboundSetEntityDataPacket metadata = new ClientboundSetEntityDataPacket(DISPLAY_ENTITY_ID, Collections.emptyList());
        player.connection.send(new ClientboundUpdateMobEffectPacket(
            player.getId(),
            new MobEffectInstance(MobEffects.INVISIBILITY, MobEffectInstance.INFINITE_DURATION, 255, false, false, false),
            false
        ));
        player.setInvisible(true);
        sendItems(user);
        player.connection.send(new ClientboundBundlePacket(List.of(addPlayer, spawn, metadata)));
        player.connection.send(cameraPacketFor(player, DISPLAY_ENTITY_ID));
        sendColumnBlock(player, Blocks.EXPOSED_COPPER_GRATE.defaultBlockState());
        player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, 0f));
        return null;
    }

    @Override
    public EntityReference<?> spawnItemDisplay(@NotNull SunscreenUser<?> user, @NotNull Location location, @NotNull Item<ItemStack> item, @NotNull Vec2i screenPos) {
        return null;
    }

    @Override
    public void setHorseArmor(@NotNull SunscreenUser<?> user, @NotNull String texturePath) {
        ServerPlayer player = fabricPlayer(user);
        LivingEntity horse = (LivingEntity) entities.row(player.getUUID()).values().stream()
            .filter(value -> value instanceof LivingEntity living && living.getType() == EntityType.HORSE)
            .findAny()
            .orElse(null);
        if (horse == null) {
            return;
        }
        final ItemStack armor = Items.LEATHER_HORSE_ARMOR.getDefaultInstance();
        armor.set(DataComponents.CUSTOM_MODEL_DATA, modelData("cursor_" + texturePath));
        horse.setItemSlot(EquipmentSlot.BODY, armor);
    }

    @Override
    public void removeEntity(@NotNull SunscreenUser<?> user, int id) {
        ServerPlayer player = fabricPlayer(user);
        entities.remove(player.getUUID(), id);
        net.minecraft.world.entity.Entity entity = FabricMappingCompat.serverLevel(player).getEntity(id);
        if (entity != null) {
            entity.discard();
        }
    }

    @Override
    public void updateMap(@NotNull SunscreenUser<?> user, int mapId, byte @NotNull [] data) {
        ServerPlayer player = fabricPlayer(user);
        MapItemSavedData.MapPatch patch = new MapItemSavedData.MapPatch(0, 0, 128, 128, data);
        player.connection.send(new ClientboundMapItemDataPacket(new MapId(mapId), (byte) 0, false, java.util.Optional.empty(), java.util.Optional.of(patch)));
    }

    @Override
    public void reset(@NotNull SunscreenUser<?> user, @NotNull Vector3d initialRotation) {
        ServerPlayer player = fabricPlayer(user);
        player.connection.send(new ClientboundSetCameraPacket(player));
        player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, (float) user.gameMode()));
        var level = FabricMappingCompat.serverLevel(player);
        long fixedDayTime = (long) user.worldTime();
        player.connection.send(new ClientboundSetTimePacket(
            -50L,
            fixedDayTime,
            true
        ));

        final Map<Integer, Object> tracked = entities.row(player.getUUID());
        int[] trackedIds = tracked.keySet().stream().mapToInt(Integer::intValue).toArray();
        IntArrayList toRemove = new IntArrayList(trackedIds.length + 1);
        toRemove.add(DISPLAY_ENTITY_ID);
        for (int id : trackedIds) {
            toRemove.add(id);
        }
        if (!toRemove.isEmpty()) {
            player.connection.send(new ClientboundRemoveEntitiesPacket(toRemove));
        }
        for (Integer id : trackedIds) {
            net.minecraft.world.entity.Entity entity = level.getEntity(id);
            if (entity != null) {
                entity.discard();
            }
        }
        tracked.clear();

        player.stopRiding();
        player.setInvisible(false);
        FabricMappingCompat.teleport(
            player,
            level,
            user.position().x(),
            user.position().y(),
            user.position().z(),
            (float) initialRotation.x(),
            (float) initialRotation.y()
        );

        player.connection.send(new ClientboundSetTimePacket(
            -50L,
            fixedDayTime,
            true
        ));
        player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, (float) user.gameMode()));
        player.connection.send(new ClientboundSetCameraPacket(player));

        sendColumnBlock(player, Blocks.AIR.defaultBlockState());

        player.connection.send(new ClientboundRemoveMobEffectPacket(player.getId(), MobEffects.INVISIBILITY));

        if (user instanceof UserImpl impl) {
            impl.resendInv();
        }
    }

    @Override
    public void gameTime(@NotNull SunscreenUser<?> user) {
        ServerPlayer player = fabricPlayer(user);
        player.connection.send(new ClientboundSetTimePacket(
            -50L,
            (long) user.worldTime(),
            true
        ));
    }

    @Override
    public void bundleDelimiter(@NotNull SunscreenUser<?> user) {
        // Vanilla uses StreamCodec.unit(one delimiter instance); new ClientboundBundleDelimiterPacket() breaks encoding.
    }

    @Override
    public void openEmptyAnvil(SunscreenUser<?> user) {
        ServerPlayer player = fabricPlayer(user);
        player.openMenu(new SimpleMenuProvider((containerId, playerInventory, ignored) -> {
            AnvilMenu menu = new AnvilMenu(containerId, playerInventory);
            ItemStack paper = Items.PAPER.getDefaultInstance();
            paper.set(DataComponents.CUSTOM_MODEL_DATA, modelData("air"));
            paper.set(DataComponents.CUSTOM_NAME, Component.empty());
            menu.getSlot(0).set(paper);
            return menu;
        }, Component.empty()));
    }

    @Override
    public void sendItems(@NotNull SunscreenUser<?> user) {
        ServerPlayer player = fabricPlayer(user);
        ItemStack stack = Items.TRIDENT.getDefaultInstance();
        stack.set(DataComponents.CUSTOM_NAME, Component.empty());
        stack.set(DataComponents.CUSTOM_MODEL_DATA, modelData("air"));
        int syncId = player.inventoryMenu.containerId;
        int stateId = player.inventoryMenu.incrementStateId();
        NonNullList<ItemStack> slots = NonNullList.withSize(44, ItemStack.EMPTY);
        for (int i = 0; i < 44; i++) {
            slots.set(i, stack.copy());
        }
        player.connection.send(new ClientboundContainerSetSlotPacket(syncId, stateId, 45, stack.copy()));
        player.connection.send(new ClientboundContainerSetContentPacket(syncId, stateId, slots, stack.copy()));
    }

    @Override
    public void removeMaps(@NotNull SunscreenUser<?> user) {
        ServerPlayer player = fabricPlayer(user);
        Map<Integer, Object> row = entities.row(player.getUUID());
        int[] ids = row.entrySet().stream()
            .filter(e -> e.getValue() instanceof ItemFrame)
            .mapToInt(Map.Entry::getKey)
            .toArray();
        for (int id : ids) {
            row.remove(id);
            net.minecraft.world.entity.Entity entity = FabricMappingCompat.serverLevel(player).getEntity(id);
            if (entity != null) {
                entity.discard();
            }
        }
    }

    private static BlockPos columnPos(ServerPlayer player) {
        return player.blockPosition().above();
    }

    private static void sendTime(ServerPlayer player, long gameTime, long dayTime) {
        player.connection.send(new ClientboundSetTimePacket(gameTime, dayTime, true));
    }

    /**
     * 1.21+ only exposes {@link ClientboundSetCameraPacket#ClientboundSetCameraPacket(net.minecraft.world.entity.Entity)}.
     * Fake spectate targets (tab list + spawn packet only) have no server entity, so we decode the same wire shape the codec uses.
     */
    private static ClientboundSetCameraPacket cameraPacketFor(ServerPlayer player, int entityId) {
        if (entityId == player.getId()) {
            return new ClientboundSetCameraPacket(player);
        }
        net.minecraft.world.entity.Entity entity = FabricMappingCompat.serverLevel(player).getEntity(entityId);
        if (entity != null) {
            return new ClientboundSetCameraPacket(entity);
        }
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeVarInt(entityId);
        return ClientboundSetCameraPacket.STREAM_CODEC.decode(buf);
    }

    private static ClientboundPlayerInfoUpdatePacket playerInfoAddPacket(ServerPlayer player, ClientboundPlayerInfoUpdatePacket.Entry entry) {
        var packet = new ClientboundPlayerInfoUpdatePacket(
                ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER,
                player
        );
        packet.entries = List.of(entry);
        return packet;
    }

    private static void fakeArmorStandHit(ServerPlayer player) {
        for (ArmorStand stand : FabricMappingCompat.serverLevel(player).getEntitiesOfClass(ArmorStand.class, player.getBoundingBox().inflate(10.0))) {
            player.connection.send(new ClientboundEntityEventPacket(stand, (byte) 32));
        }
    }

    private static void sendColumnBlock(ServerPlayer player, net.minecraft.world.level.block.state.BlockState state) {
        player.connection.send(new ClientboundBlockUpdatePacket(columnPos(player), state));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void copyProfileProperties(GameProfile target, GameProfile source) {
        Object targetProps = FabricMappingCompat.gameProfileProperties(target);
        Object sourceProps = FabricMappingCompat.gameProfileProperties(source);
        if (targetProps instanceof Map targetMap && sourceProps instanceof Map sourceMap) {
            targetMap.putAll(sourceMap);
        }
    }

    private CustomModelData modelData(String key) {
        int id = switch (key) {
            case "air" -> 9000;
            case "cursor_default" -> 1;
            case "cursor_text" -> 2;
            case "cursor_move" -> 3;
            case "cursor_resize_vertical" -> 4;
            case "cursor_resize_horizontal" -> 5;
            case "cursor_magnify" -> 6;
            case "cursor_await" -> 7;
            case "cursor_click" -> 8;
            default -> 1;
        };
        return FabricMappingCompat.customModelData(id);
    }

    private ServerPlayer fabricPlayer(SunscreenUser<?> user) {
        if (user instanceof UserImpl impl) {
            return impl.player();
        }
        throw new IllegalStateException("Expected Fabric user implementation");
    }

    @Override
    public void clientToServer(@NotNull ByteBuffer logical) {
        ByteBuffer buf = logical.slice();
        byte[] bytes = new byte[buf.remaining()];
        buf.get(bytes);
        ByteBuffer packet = NativeUiChunkBinary.encode(bytes.length, 0, 1, bytes);
        sender.accept(new NativeUiChunkC2SPayload(packet.array()));
    }

    @Override
    public void serverToClient(@NotNull SunscreenUser<?> user, @NotNull ByteBuffer payloadPacket) {
        ByteBuffer copy = payloadPacket.slice();
        byte[] out = new byte[copy.remaining()];
        copy.get(out);
        ServerPlayNetworking.send(fabricPlayer(user), new NativeUiChunkS2CPayload(out));
    }

    public static void setSender(Consumer<NativeUiChunkC2SPayload> s) {
        sender = s;
    }

}
