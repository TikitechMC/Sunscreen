package me.combimagnetron.sunscreen.protocol;

import com.destroystokyo.paper.profile.ProfileProperty;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemEquippable;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemModel;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.player.*;
import com.github.retrooper.packetevents.protocol.potion.PotionTypes;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import me.combimagnetron.passport.internal.entity.Entity;
import me.combimagnetron.passport.internal.entity.impl.passive.horse.Horse;
import me.combimagnetron.passport.internal.entity.impl.tile.ItemFrame;
import me.combimagnetron.passport.internal.entity.metadata.type.Vector3d;
import me.combimagnetron.passport.internal.network.Connection;
import me.combimagnetron.sunscreen.neo.protocol.PlatformProtocolIntermediate;
import me.combimagnetron.sunscreen.neo.protocol.type.EntityReference;
import me.combimagnetron.sunscreen.neo.protocol.type.Location;
import me.combimagnetron.sunscreen.neo.render.engine.cache.RenderCache;
import me.combimagnetron.sunscreen.neo.render.engine.context.RenderContext;
import me.combimagnetron.sunscreen.user.SunscreenUser;
import me.combimagnetron.sunscreen.util.Scheduler;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class SpigotPlatformProtocolIntermediate implements PlatformProtocolIntermediate {
    private static final WrapperPlayServerUpdateAttributes.PropertyModifier MODIFIER = new WrapperPlayServerUpdateAttributes.PropertyModifier(UUID.randomUUID(), 0, WrapperPlayServerUpdateAttributes.PropertyModifier.Operation.MULTIPLY_BASE);
    protected final Table<UUID, Integer, Object> entities = HashBasedTable.create();
    private EquipmentSlot equipmentSlot = EquipmentSlot.BODY;

    public SpigotPlatformProtocolIntermediate() {
        PacketEvents.getAPI().getEventManager().registerListener(new ProtocolListener(), PacketListenerPriority.LOW);
    }

    public @NotNull Map<Integer, Object> entities(@NotNull UUID uuid) {
        return entities.row(uuid);
    }

    @Override
    public EntityReference<?> spawnAndRideHorse(@NotNull SunscreenUser<?> user, @NotNull Location location) {
        Player player = (Player) user.platformSpecificPlayer();
        Horse horse = Horse.horse(loc2Vec3(location));
        horse.invisible(true);
        horse.crouching(true);
        if (PacketEvents.getAPI().getPlayerManager().getClientVersion(player).getProtocolVersion() >= ClientVersion.V_1_21_5.getProtocolVersion()) {
            equipmentSlot = EquipmentSlot.SADDLE;
        } else {
            horse.saddled(true);
        }
        WrapperPlayServerEntityEquipment entityEquipment = horseEquipment("cursor_default", horse.id().intValue(), equipmentSlot);
        WrapperPlayServerSetPassengers passengers = new WrapperPlayServerSetPassengers(horse.id().intValue(), new int[]{user.entityId()});
        List<WrapperPlayServerUpdateAttributes.Property> attributes = List.of(
                new WrapperPlayServerUpdateAttributes.Property(
                        Attributes.JUMP_STRENGTH,
                        0.0D,
                        List.of(MODIFIER)
                ),
                new WrapperPlayServerUpdateAttributes.Property(
                        Attributes.SCALE,
                        0.01,
                        List.of(MODIFIER)
                )
        );
        WrapperPlayServerEntityTeleport entityTeleport = new WrapperPlayServerEntityTeleport(horse.id().intValue(), new com.github.retrooper.packetevents.util.Vector3d(horse.position().x(), horse.position().y() - 1.7, horse.position().z()), 0f, 180f, false);
        WrapperPlayServerUpdateAttributes updateAttributes = new WrapperPlayServerUpdateAttributes(horse.id().intValue(), attributes);
        user.connection().send(new WrapperPlayServerPlayerRotation(0f, -180f));
        user.show(horse);
        user.connection().send(updateAttributes);
        user.connection().send(entityEquipment);
        user.connection().send(entityTeleport);
        user.connection().send(passengers);
        user.connection().send(new WrapperPlayServerPlayerRotation(0f, -37.3f));
        entities.put(user.uniqueIdentifier(), horse.id().intValue(), horse);
        return new EntityReference<>(horse.id().intValue(), horse);
    }

    @Override
    public EntityReference<?> spawnAndFillItemFrame(@NotNull SunscreenUser<?> user, @NotNull Location location, byte[] data, int mapId) {
        WrapperPlayServerMapData mapData = new WrapperPlayServerMapData(mapId, (byte) 0, false, false, null, 128, 128, 0, 0, data);
        ItemStack itemStack = ItemStack.builder().type(ItemTypes.FILLED_MAP).component(ComponentTypes.MAP_ID, mapId).build();
        ItemFrame upper = ItemFrame.frame(loc2Vec3(location), itemStack);
        ItemFrame lower = ItemFrame.frame(loc2Vec3(location), itemStack);
        upper.id(Entity.EntityId.of(mapId));
        lower.id(Entity.EntityId.of(Integer.MIN_VALUE + mapId));
        upper.invisible(true);
        upper.direction(ItemFrame.Direction.UP);
        lower.invisible(true);
        lower.direction(ItemFrame.Direction.DOWN);
        user.connection().send(mapData);
        user.show(upper);
        user.show(lower);
        entities.put(user.uniqueIdentifier(), upper.id().intValue(), upper);
        entities.put(user.uniqueIdentifier(), lower.id().intValue(), lower);
        return new EntityReference<>(upper.id().intValue(), upper);
    }

    @Override
    public EntityReference<?> spawnAndSpectateDisplay(@NotNull SunscreenUser<?> user, @NotNull Location location) {
        Player player = (Player) user.platformSpecificPlayer();
        WrapperPlayServerCamera camera = new WrapperPlayServerCamera(-10_000);
        UUID uuid = UUID.randomUUID();
        List<TextureProperty> properties = new ArrayList<>();
        for (ProfileProperty property : player.getPlayerProfile().getProperties()) {
            properties.add(new TextureProperty(property.getName(), property.getValue(), property.getSignature()));
        }
        UserProfile profile = new UserProfile(uuid, player.getName(), properties);
        WrapperPlayServerSpawnEntity spawnEntity = new WrapperPlayServerSpawnEntity(-10_000, uuid, EntityTypes.PLAYER, vec32PeLoc(user.position(), user.rotation()), player.getYaw(), 0, com.github.retrooper.packetevents.util.Vector3d.zero());
        WrapperPlayServerPlayerInfoUpdate infoUpdate = new WrapperPlayServerPlayerInfoUpdate(WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER, new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(profile, false, 0, GameMode.CREATIVE, null, null, 0, true));
        WrapperPlayServerBlockChange blockChange = new WrapperPlayServerBlockChange(new Vector3i((int) player.getX(), (int) player.getY() + 1, (int) player.getZ()), WrappedBlockState.getDefaultState(StateTypes.EXPOSED_COPPER_GRATE));
        user.connection().send(new WrapperPlayServerEntityEffect(player.getEntityId(), PotionTypes.INVISIBILITY, 255, -1, ((byte) 0)));
        // todo: fix invis potion
        ItemStack stack = ItemStack.builder().type(ItemTypes.TRIDENT).component(ComponentTypes.ITEM_NAME, Component.empty()).component(ComponentTypes.ITEM_MODEL, new ItemModel(ResourceLocation.minecraft("air"))).build();
        WrapperPlayServerWindowItems items = new WrapperPlayServerWindowItems(0, 0, Collections.nCopies(44, stack), stack);
        WrapperPlayServerSetSlot slot = new WrapperPlayServerSetSlot(0, 0, 45, stack);
        player.setInvisible(true);
        user.connection().send(slot);
        user.connection().send(items);
        user.connection().send(infoUpdate);
        user.connection().send(spawnEntity);
        user.connection().send(camera);
        user.connection().send(blockChange);
        user.connection().send(new WrapperPlayServerChangeGameState(WrapperPlayServerChangeGameState.Reason.CHANGE_GAME_MODE, 0));
        return null;
    }

    @Override
    public void setHorseArmor(@NotNull SunscreenUser<?> user, @NotNull String texturePath) {
        Horse horse = (Horse) entities.row(user.uniqueIdentifier()).values().stream().filter(object -> object instanceof Horse).findAny().orElseThrow();
        int horseId = horse.id().intValue();
        WrapperPlayServerEntityEquipment entityEquipment = horseEquipment("cursor_" + texturePath, horseId, equipmentSlot);
        user.connection().send(entityEquipment);
    }

    @Override
    public void removeEntity(@NotNull SunscreenUser<?> user, int id) {
        entities.remove(user.uniqueIdentifier(), id);
        WrapperPlayServerDestroyEntities destroyEntities = new WrapperPlayServerDestroyEntities(id);
        user.connection().send(destroyEntities);
    }

    @Override
    public void updateMap(@NotNull SunscreenUser<?> user, int mapId, byte @NotNull [] data) {
        WrapperPlayServerMapData mapData = new WrapperPlayServerMapData(mapId, (byte) 0, false, false, null, 128, 128, 0, 0, data);
        user.connection().send(mapData);
    }

    @Override
    public void reset(@NotNull SunscreenUser<?> user, @NotNull Vector3d initialRotation) {
        Player player = (Player) user.platformSpecificPlayer();
        WrapperPlayServerCamera camera = new WrapperPlayServerCamera(user.entityId());
        WrapperPlayServerChangeGameState gameState = new WrapperPlayServerChangeGameState(WrapperPlayServerChangeGameState.Reason.CHANGE_GAME_MODE, user.gameMode());
        WrapperPlayServerTimeUpdate timeUpdate = new WrapperPlayServerTimeUpdate(player.getWorld().getGameTime(), player.getPlayerTime());
        final Map<Integer, Object> trackedEntities = entities.row(user.uniqueIdentifier());
        final int[] ids = trackedEntities.keySet().stream().mapToInt(Integer::intValue).toArray();
        if (ids.length > 0) {
            user.connection().send(new WrapperPlayServerDestroyEntities(ids));
        }
        user.connection().send(new WrapperPlayServerDestroyEntities(-10_000));
        trackedEntities.clear();
        player.setInvisible(false);
        user.connection().send(new WrapperPlayServerPlayerPositionAndLook(user.position().x(), user.position().y(), user.position().z(), (float) initialRotation.x(), (float) initialRotation.y(), (byte)0, 0, false));
        user.connection().send(timeUpdate);
        user.connection().send(gameState);
        user.connection().send(camera);
        user.connection().send(new WrapperPlayServerBlockChange(new Vector3i((int) player.getX(), (int) player.getY() + 1, (int) player.getZ()), WrappedBlockState.getDefaultState(StateTypes.AIR)));
        user.connection().send(new WrapperPlayServerRemoveEntityEffect(player.getEntityId(), PotionTypes.INVISIBILITY));
        user.resendInv();
    }

    @Override
    public void gameTime(@NotNull SunscreenUser<?> user) {
        WrapperPlayServerTimeUpdate time = new WrapperPlayServerTimeUpdate(-2000, (long) user.worldTime(), false);
        user.connection().send(time);
    }

    @Override
    public void bundleDelimiter(@NotNull SunscreenUser<?> user) {
        WrapperPlayServerBundle bundle = new WrapperPlayServerBundle();
        user.connection().send(bundle);
    }

    @Override
    public void openEmptyAnvil(SunscreenUser<?> user) {
        WrapperPlayServerOpenWindow openWindow = new WrapperPlayServerOpenWindow(-10_000, 8, Component.empty());
        ArrayList<ItemStack> items = new ArrayList<>(
            List.of(ItemStack.builder()
                .type(ItemTypes.PAPER)
                .component(ComponentTypes.ITEM_MODEL, new ItemModel(ResourceLocation.minecraft("air")))
                .component(ComponentTypes.ITEM_NAME, Component.empty())
                .amount(1)
                .build())
        );
        items.addAll(Collections.nCopies(38, ItemStack.EMPTY));
        WrapperPlayServerWindowItems windowItems = new WrapperPlayServerWindowItems(-10_000, 0, items, null);
        user.connection().send(openWindow);
        user.connection().send(windowItems);
//        Player player = (Player) user.platformSpecificPlayer();
//        Scheduler.repeat(() -> {
//            WrapperPlayServerBundle bundle = new WrapperPlayServerBundle();
//            WrapperPlayServerCloseWindow closeWindow = new WrapperPlayServerCloseWindow();
//            WrapperPlayServerSetSlot slot = new WrapperPlayServerSetSlot(0, 0, 0, ItemStack.builder().type(ItemTypes.WRITABLE_BOOK).build());
//            WrapperPlayServerOpenBook openBook = new WrapperPlayServerOpenBook(InteractionHand.MAIN_HAND);
//            send(user, bundle, closeWindow, slot, openBook, bundle);
//        }, new Scheduler.Duration(50, TimeUnit.MILLISECONDS));
    }

    private static @NotNull Vector3d loc2Vec3(@NotNull Location location) {
        return Vector3d.vec3(location.x(), location.y(), location.z());
    }

    private static @NotNull com.github.retrooper.packetevents.protocol.world.Location vec32PeLoc(@NotNull Vector3d position, @NotNull Vector3d rotation) {
        return new com.github.retrooper.packetevents.protocol.world.Location(new com.github.retrooper.packetevents.util.Vector3d(position.x(), position.y(), position.z()), (float) rotation.x(), (float) rotation.y());
    }

    private static void send(@NotNull SunscreenUser<?> user, @NotNull PacketWrapper<?>... wrappers) {
        Connection connection = user.connection();
        for (PacketWrapper<?> wrapper : wrappers) {
            connection.send(wrapper);
        }
    }

    private static @NotNull WrapperPlayServerEntityEquipment horseEquipment(@NotNull String texturePath, int id, EquipmentSlot equipmentSlot) {
        return new WrapperPlayServerEntityEquipment(
                id,
                List.of(
                        new Equipment(equipmentSlot,
                                ItemStack.builder().type(ItemTypes.SADDLE).build()),
                        new Equipment(EquipmentSlot.BODY,
                                ItemStack.builder().type(ItemTypes.COPPER_HORSE_ARMOR).
                                        component(ComponentTypes.EQUIPPABLE,
                                                new ItemEquippable(EquipmentSlot.BODY, Sounds.ITEM_ARMOR_EQUIP_GENERIC, new ResourceLocation("sunscreen", texturePath), null, null, false, false, false)).build())));

    }

}
