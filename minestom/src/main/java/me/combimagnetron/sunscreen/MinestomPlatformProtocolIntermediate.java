package me.combimagnetron.sunscreen;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import me.combimagnetron.passport.internal.entity.metadata.type.Vector3d;
import me.combimagnetron.sunscreen.neo.protocol.PlatformProtocolIntermediate;
import me.combimagnetron.sunscreen.neo.protocol.type.EntityReference;
import me.combimagnetron.sunscreen.neo.protocol.type.Location;
import me.combimagnetron.sunscreen.user.SunscreenUser;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.*;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.entity.metadata.other.ItemFrameMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.type.AnvilInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.Equippable;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MinestomPlatformProtocolIntermediate implements PlatformProtocolIntermediate {
    private final static AttributeModifier ATTRIBUTE_MODIFIER = new AttributeModifier(Key.key("sunscreen:attribute"), 0, AttributeOperation.ADD_MULTIPLIED_BASE);
    private final Table<UUID, Integer, Object> entities = HashBasedTable.create();

    @Override
    public EntityReference<?> spawnAndRideHorse(@NotNull SunscreenUser<?> user, @NotNull Location location) {
        Player player = player(user);
        LivingEntity horse = new LivingEntity(EntityType.HORSE);
        horse.setInvisible(true);
        int entityId = horse.getEntityId();
        List<EntityAttributesPacket.Property> properties = List.of(
            new EntityAttributesPacket.Property(
                Attribute.JUMP_STRENGTH,
                0.0,
                List.of(ATTRIBUTE_MODIFIER)
            ),
            new EntityAttributesPacket.Property(
                Attribute.SCALE,
                0.01,
                List.of(ATTRIBUTE_MODIFIER)
            )
        );
        final Pos pos = player.getPosition();
        SpawnEntityPacket spawnEntityPacket = new SpawnEntityPacket(entityId, horse.getUuid(), horse.getEntityType(), pos, pos.yaw(), 0, Vec.ZERO);
        EntityAttributesPacket entityAttributesPacket = new EntityAttributesPacket(entityId, properties);
        EntityEquipmentPacket entityEquipmentPacket = equipment("cursor_default", entityId);
        EntityTeleportPacket entityTeleportPacket = new EntityTeleportPacket(entityId, pos, Vec.ZERO, 0, false);
        SetPassengersPacket setPassengersPacket = new SetPassengersPacket(entityId, List.of(player.getEntityId()));
        PlayerRotationPacket playerRotationPacket = new PlayerRotationPacket(0f, false, -37.3f, false);
        player.getPlayerConnection().sendPackets(spawnEntityPacket, entityAttributesPacket, entityEquipmentPacket, entityTeleportPacket, setPassengersPacket, playerRotationPacket);
        entities.put(player.getUuid(), entityId, EntityType.HORSE);
        return null;
    }

    @Override
    public EntityReference<?> spawnAndFillItemFrame(@NotNull SunscreenUser<?> user, @NotNull Location location, byte[] data, int mapId) {
        Player player = player(user);
        MapDataPacket mapDataPacket = new MapDataPacket(mapId, (byte) 0, false, false, List.of(), new MapDataPacket.ColorContent((byte) 128, (byte) 128, (byte) 0, (byte) 0, data));
        ItemStack itemStack = ItemStack.of(Material.FILLED_MAP).with(DataComponents.MAP_ID, mapId);
        LivingEntity upperFrame = new LivingEntity(EntityType.ITEM_FRAME);
        LivingEntity lowerFrame = new LivingEntity(EntityType.ITEM_FRAME);
        upperFrame.setInvisible(true);
        lowerFrame.setInvisible(true);
        upperFrame.editEntityMeta(ItemFrameMeta.class, itemFrameMeta -> {
            itemFrameMeta.setItem(itemStack);
            itemFrameMeta.setDirection(Direction.UP);
        });
        lowerFrame.editEntityMeta(ItemFrameMeta.class, itemFrameMeta -> {
            itemFrameMeta.setItem(itemStack);
            itemFrameMeta.setDirection(Direction.DOWN);
        });
        entities.put(player.getUuid(), upperFrame.getEntityId(), EntityType.ITEM_FRAME);
        entities.put(player.getUuid(), lowerFrame.getEntityId(), EntityType.ITEM_FRAME);
        final Pos pos = toMinestom(user.position()).add(0, 1, 0);
        SpawnEntityPacket spawnEntityPacketUpper = new SpawnEntityPacket(upperFrame.getEntityId(), upperFrame.getUuid(), upperFrame.getEntityType(), pos, pos.yaw(), 0, Vec.ZERO);
        SpawnEntityPacket spawnEntityPacketLower = new SpawnEntityPacket(lowerFrame.getEntityId(), lowerFrame.getUuid(), lowerFrame.getEntityType(), pos, pos.yaw(), 0, Vec.ZERO);
        player.getPlayerConnection().sendPackets(mapDataPacket, spawnEntityPacketUpper, spawnEntityPacketLower, upperFrame.getMetadataPacket(), lowerFrame.getMetadataPacket());
        return null;
    }

    @Override
    public EntityReference<?> spawnAndSpectateDisplay(@NotNull SunscreenUser<?> user, @NotNull Location location) {
        Player player = player(user);
        UUID uuid = UUID.randomUUID();
        final PlayerSkin playerSkin = player.getSkin();
        List<PlayerInfoUpdatePacket.Property> properties = new ArrayList<>();
        if (playerSkin != null) {
            properties.add(new PlayerInfoUpdatePacket.Property("textures", playerSkin.textures(), playerSkin.signature()));
        }
        player.setInvisible(true);
        EntityEffectPacket entityEffectPacket = new EntityEffectPacket(player.getEntityId(), new Potion(PotionEffect.INVISIBILITY, 1, -1, ((byte) 0)));
        PlayerInfoUpdatePacket playerInfoUpdatePacket = new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.ADD_PLAYER, new PlayerInfoUpdatePacket.Entry(uuid, player.getUsername(), properties, false, 0, GameMode.CREATIVE, null, null, 0, true));
        SpawnEntityPacket spawnEntityPacket = new SpawnEntityPacket(-10_000, uuid, EntityType.PLAYER, toMinestom(user.position()), player.getPosition().yaw(), 0, Vec.ZERO);
        EntityRotationPacket entityRotationPacket = new EntityRotationPacket(-10_000, player.getPosition().yaw(), player.getPosition().pitch(), player.isOnGround());
        CameraPacket cameraPacket = new CameraPacket(-10_000);
        BlockChangePacket blockChangePacket = new BlockChangePacket(toMinestom(user.position()).add(0, 1, 0), Block.BARRIER);
        ChangeGameStatePacket changeGameStatePacket = new ChangeGameStatePacket(ChangeGameStatePacket.Reason.CHANGE_GAMEMODE, 0);
        player.getPlayerConnection().sendPackets(entityEffectPacket, playerInfoUpdatePacket, spawnEntityPacket, entityRotationPacket, cameraPacket, blockChangePacket, changeGameStatePacket);
        return null;
    }

    @Override
    public void setHorseArmor(@NotNull SunscreenUser<?> user, @NotNull String texturePath) {
        Player player = player(user);
        int horseId = entities.row(user.uniqueIdentifier()).entrySet().stream().filter(entry -> entry.getValue() == EntityType.HORSE).findAny().orElseThrow().getKey();
        EntityEquipmentPacket entityEquipmentPacket = equipment("cursor_" + texturePath, horseId);
        player.getPlayerConnection().sendPacket(entityEquipmentPacket);
    }

    @Override
    public void removeEntity(@NotNull SunscreenUser<?> user, int id) {
        Player player = player(user);
        entities.remove(user.uniqueIdentifier(), id);
        DestroyEntitiesPacket destroyEntitiesPacket = new DestroyEntitiesPacket(id);
        player.getPlayerConnection().sendPackets(destroyEntitiesPacket);
    }

    @Override
    public void updateMap(@NotNull SunscreenUser<?> user, int mapId, byte @NotNull [] data) {
        final Player player = (Player) user.platformSpecificPlayer();
        player.getPlayerConnection().sendPacket(new MapDataPacket(mapId, (byte) 0, false, false, List.of(), new MapDataPacket.ColorContent((byte) 128, (byte) 128, (byte) 0, (byte) 0, data)));
    }

    @Override
    public void reset(@NotNull SunscreenUser<?> user, @NotNull Vector3d initialRotation) {
        Player player = player(user);
        final Map<Integer, Object> trackedEntities = entities.row(user.uniqueIdentifier());
        final List<Integer> ids = trackedEntities.keySet().stream().toList();
        final PlayerConnection playerConnection = player.getPlayerConnection();
        if (!ids.isEmpty()) {
            playerConnection.sendPackets(new DestroyEntitiesPacket(ids));
        }
        player.setInvisible(false);
        DestroyEntitiesPacket destroyEntitiesPacket = new DestroyEntitiesPacket(-10_000);
        trackedEntities.clear();
        PlayerPositionAndLookPacket playerPositionAndLookPacket = new PlayerPositionAndLookPacket(0, new Vec(user.position().x(), user.position().y(), user.position().z()), Vec.ZERO, (float) initialRotation.x(), (float) initialRotation.y(), 0);
        TimeUpdatePacket timeUpdate = new TimeUpdatePacket(player.getInstance().getWorldAge(), player.getInstance().getTime(), true);
        ChangeGameStatePacket gameState = new ChangeGameStatePacket(ChangeGameStatePacket.Reason.CHANGE_GAMEMODE, user.gameMode());
        CameraPacket camera = new CameraPacket(user.entityId());
        BlockChangePacket blockChangePacket = new BlockChangePacket(toMinestom(user.position()).add(0, 1, 0), Block.AIR);
        RemoveEntityEffectPacket removeEntityEffectPacket = new RemoveEntityEffectPacket(player.getEntityId(), PotionEffect.INVISIBILITY);
        player.refreshPosition(player.getPosition());
        playerConnection.sendPackets(destroyEntitiesPacket, playerPositionAndLookPacket, timeUpdate, gameState, camera, blockChangePacket, removeEntityEffectPacket);
    }

    @Override
    public void gameTime(@NotNull SunscreenUser<?> user) {
        final Player player = (Player) user.platformSpecificPlayer();
        final Instance instance = player.getInstance();
        player.getPlayerConnection().sendPacket(new TimeUpdatePacket(-2000, instance.getTime(), true));
    }

    @Override
    public void bundleDelimiter(@NotNull SunscreenUser<?> user) {

    }

    @Override
    public void openEmptyAnvil(SunscreenUser<?> user) {
        final Player player = (Player) user.platformSpecificPlayer();
        AnvilInventory anvilInventory = new AnvilInventory("");
        anvilInventory.addItemStack(ItemStack.of(Material.PAPER).with(DataComponents.CUSTOM_NAME, Component.empty()).with(DataComponents.ITEM_MODEL, "minecraft:air"));
        player.openInventory(anvilInventory);
    }

    private Player player(SunscreenUser<?> user) {
        return (Player) user.platformSpecificPlayer();
    }

    private static @NotNull Pos toMinestom(@NotNull Vector3d vector3d) {
        return new Pos(vector3d.x(), vector3d.y(), vector3d.z());
    }

    private @NotNull EntityEquipmentPacket equipment(@NotNull String texturePath, int id) {
        return new EntityEquipmentPacket(id, Map.of(EquipmentSlot.SADDLE, ItemStack.of(Material.SADDLE), EquipmentSlot.BODY, ItemStack.of(Material.COPPER_HORSE_ARMOR).with(DataComponents.EQUIPPABLE, new Equippable(EquipmentSlot.BODY, SoundEvent.ITEM_GOAT_HORN_SOUND_2, "sunscreen:" + texturePath, null, null, false, false, false, false, false, SoundEvent.ITEM_GOAT_HORN_SOUND_0))));
    }

}