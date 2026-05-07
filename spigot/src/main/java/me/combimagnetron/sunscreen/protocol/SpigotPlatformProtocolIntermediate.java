package me.combimagnetron.sunscreen.protocol;

import com.destroystokyo.paper.ClientOption;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.protocol.attribute.AttributeOperation;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemAttributeModifiers;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemEquippable;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemModel;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemTooltipDisplay;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.player.*;
import com.github.retrooper.packetevents.protocol.potion.PotionTypes;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import com.github.retrooper.packetevents.protocol.teleport.RelativeFlag;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.Dummy;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.combimagnetron.passport.internal.entity.Entity;
import me.combimagnetron.passport.internal.entity.impl.display.Display;
import me.combimagnetron.passport.internal.entity.impl.display.ItemDisplay;
import me.combimagnetron.passport.internal.entity.impl.passive.horse.Horse;
import me.combimagnetron.passport.internal.entity.impl.tile.ItemFrame;
import me.combimagnetron.passport.internal.entity.metadata.type.Vector3d;
import me.combimagnetron.passport.internal.network.Connection;
import me.combimagnetron.passport.user.User;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.passport.util.math.Vec3f;
import me.combimagnetron.sunscreen.neo.graphic.Item;
import me.combimagnetron.sunscreen.neo.input.context.TextInputContext;
import me.combimagnetron.sunscreen.neo.protocol.PlatformProtocolIntermediate;
import me.combimagnetron.sunscreen.neo.protocol.type.EntityReference;
import me.combimagnetron.sunscreen.neo.protocol.type.Location;
import me.combimagnetron.sunscreen.user.SunscreenUser;
import me.combimagnetron.sunscreen.util.helper.RotationHelper;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SpigotPlatformProtocolIntermediate implements PlatformProtocolIntermediate<org.bukkit.inventory.ItemStack> {
    private static final WrapperPlayServerUpdateAttributes.PropertyModifier MODIFIER = new WrapperPlayServerUpdateAttributes.PropertyModifier(UUID.randomUUID(), 0, WrapperPlayServerUpdateAttributes.PropertyModifier.Operation.MULTIPLY_BASE);
    protected final Table<UUID, Integer, Object> entities = HashBasedTable.create();
    private EquipmentSlot equipmentSlot = EquipmentSlot.BODY;

    public SpigotPlatformProtocolIntermediate() {
        PacketEvents.getAPI().getEventManager().registerListener(new ProtocolListener(), PacketListenerPriority.HIGHEST);
        PacketEvents.getAPI().getEventManager().registerListener(new AnvilListener(), PacketListenerPriority.HIGHEST);
    }

    public @NotNull Map<Integer, Object> entities(@NotNull UUID uuid) {
        return entities.row(uuid);
    }

    @Override
    public EntityReference<?> spawnAndRideHorse(@NotNull SunscreenUser<?> user, @NotNull Location location) {
        Player player = (Player) user.platformSpecificPlayer();
        Horse horse = Horse.horse(loc2Vec3(location).add(Vector3d.vec3(0, 6, 0)));
        horse.invisible(true);
        horse.noGravity(true);
        horse.crouching(true);
        if (protocolVersion(player) >= ClientVersion.V_1_21_5.getProtocolVersion()) {
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
        player.sendActionBar(Component.text(" "));
        return new EntityReference<>(horse.id().intValue(), horse);
    }

    @Override
    public EntityReference<?> spawnAndFillItemFrame(@NotNull SunscreenUser<?> user, @NotNull Location location, byte @NotNull [] data, int mapId) {
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

    private static void fakeArmorStandHit(@NotNull User<?> user) {
        Player player = (Player) user.platformSpecificPlayer();
        for (org.bukkit.entity.Entity nearbyEntity : player.getNearbyEntities(10, 10, 10)) {
            if (!(nearbyEntity instanceof ArmorStand armorStand)) continue;
            user.connection().send(new WrapperPlayServerEntityStatus(armorStand.getEntityId(), 32));
        }
    }

    @Override
    public EntityReference<?> spawnAndSpectateDisplay(@NotNull SunscreenUser<?> user, @NotNull Location location) {
        Player player = (Player) user.platformSpecificPlayer();
        gameTime(user, -500);
        fakeArmorStandHit(user);
        gameTime(user, -50);
        WrapperPlayServerCamera camera = new WrapperPlayServerCamera(-10_000);
        UUID uuid = UUID.randomUUID();
        List<TextureProperty> properties = new ArrayList<>();
        for (ProfileProperty property : player.getPlayerProfile().getProperties()) {
            properties.add(new TextureProperty(property.getName(), property.getValue(), property.getSignature()));
        }
        UserProfile profile = new UserProfile(uuid, player.getName(), properties);
        WrapperPlayServerSpawnEntity spawnEntity = new WrapperPlayServerSpawnEntity(-10_000, uuid, EntityTypes.PLAYER, new com.github.retrooper.packetevents.protocol.world.Location(player.getX(), player.getY(), player.getZ(), player.getLocation().getYaw(), player.getLocation().getPitch()), player.getYaw(), 0, com.github.retrooper.packetevents.util.Vector3d.zero());
        int index = protocolVersion(player) >= ClientVersion.V_1_21_9.getProtocolVersion() ? 16 : 17;
        WrapperPlayServerEntityMetadata metadata = new WrapperPlayServerEntityMetadata(-10_000, List.of(new EntityData<>(index, EntityDataTypes.BYTE, (byte) player.getClientOption(ClientOption.SKIN_PARTS).getRaw())));
        WrapperPlayServerPlayerInfoUpdate infoUpdate = new WrapperPlayServerPlayerInfoUpdate(WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER, new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(profile, false, 0, GameMode.CREATIVE, null, null, 0, true));
        WrapperPlayServerBlockChange blockChange = new WrapperPlayServerBlockChange(new Vector3i((int) player.getX(), (int) player.getY() + 1, (int) player.getZ()), WrappedBlockState.getDefaultState(StateTypes.EXPOSED_COPPER_GRATE));
        WrapperPlayServerEntityTeleport positionSync = new WrapperPlayServerEntityTeleport(-10_000, new com.github.retrooper.packetevents.util.Vector3d(player.getX(), player.getY(), player.getZ()), com.github.retrooper.packetevents.util.Vector3d.zero(), player.getYaw(), player.getPitch(), RelativeFlag.NONE, player.isOnGround());
        user.connection().send(new WrapperPlayServerEntityMetadata(player.getEntityId(), List.of(new EntityData<>(0, EntityDataTypes.BYTE, (byte) 0x20))));
        sendItems(user);
        user.connection().send(new WrapperPlayServerBundle());
        user.connection().send(infoUpdate);
        user.connection().send(spawnEntity);
        user.connection().send(metadata);
        //this did NOT fix the inaccuracy with rotation, thanks mojang for the 256/360 angle!!!!
        //user.connection().send(positionSync);
        user.connection().send(new WrapperPlayServerBundle());
        user.connection().send(camera);
        player.sendBlockChange(player.getLocation().add(0, 1, 0), Material.EXPOSED_COPPER_GRATE.createBlockData());
        //user.connection().send(blockChange);
        user.connection().send(new WrapperPlayServerChangeGameState(WrapperPlayServerChangeGameState.Reason.CHANGE_GAME_MODE, 0));
        return null;
    }

    @Override
    public EntityReference<?> spawnItemDisplay(@NotNull SunscreenUser<?> user, @NotNull Location location, @NotNull Item<org.bukkit.inventory.ItemStack> item, @NotNull Vec2i screenPos) {
        ItemDisplay display = ItemDisplay.itemDisplay(loc2Vec3(location));
        Display.Transformation transformation = item.transformation();
        if (transformation == null) transformation = Display.Transformation.transformation();
        transformation = transformation.translation(Vector3d.vec3(960 - screenPos.x(), -15_000 + 540 - screenPos.y(), 0));
        transformation = transformation.scale(transformation.scale().mul(100));
        display.transformation(transformation);
        display.rotation(Vector3d.vec3(0));
        display.displayType(ItemDisplay.DisplayType.FIXED);
        display.nameVisible(false);
        display.item(SpigotConversionUtil.fromBukkitItemStack(item.item()));
        user.show(display);
        entities.put(user.uniqueIdentifier(), display.id().intValue(), display);
        return null;
    }

    @Override
    public void sendItems(@NotNull SunscreenUser<?> user) {
        ItemStack stack =
            ItemStack.builder().type(ItemTypes.TRIDENT)
                .component(ComponentTypes.ATTRIBUTE_MODIFIERS,
                    new ItemAttributeModifiers(
                        List.of(
                            new ItemAttributeModifiers.ModifierEntry(
                                Attributes.ATTACK_SPEED,
                                new ItemAttributeModifiers.Modifier(UUID.randomUUID(), "a", -1D, AttributeOperation.MULTIPLY_TOTAL),
                                ItemAttributeModifiers.EquipmentSlotGroup.ANY
                            )
                        )
                    )
                )
                .component(ComponentTypes.TOOLTIP_DISPLAY, new ItemTooltipDisplay(true, Set.of()))
                .component(ComponentTypes.ITEM_NAME, Component.empty())
                .component(ComponentTypes.ITEM_MODEL, new ItemModel(ResourceLocation.minecraft("air"))).build();
        hideTooltip(user, stack);
        WrapperPlayServerWindowItems items = new WrapperPlayServerWindowItems(0, 0, Collections.nCopies(44, stack), stack);
        WrapperPlayServerSetSlot slot = new WrapperPlayServerSetSlot(0, 0, 45, stack);
        user.connection().send(slot);
        user.connection().send(items);
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
        user.connection().send(new WrapperPlayServerEntityMetadata(player.getEntityId(), List.of(new EntityData<>(0, EntityDataTypes.BYTE, (byte) 0))));
        user.connection().send(new WrapperPlayServerPlayerPositionAndLook(user.position().x(), user.position().y(), user.position().z(), (float) initialRotation.x(), (float) initialRotation.y(), (byte)0, 0, false));
        user.connection().send(timeUpdate);
        user.connection().send(gameState);
        user.connection().send(camera);
        player.sendBlockChange(player.getLocation().add(0, 1, 0), Material.AIR.createBlockData());
        //user.connection().send(new WrapperPlayServerBlockChange(new Vector3i((int) player.getX(), (int) player.getY() + 1, (int) player.getZ()), WrappedBlockState.getDefaultState(StateTypes.AIR)));
        user.connection().send(new WrapperPlayServerRemoveEntityEffect(player.getEntityId(), PotionTypes.INVISIBILITY));
        user.resendInv();
        //fakeArmorStandHit(user);
    }

    @Override
    public void gameTime(@NotNull SunscreenUser<?> user) {
        gameTime(user, -50);
    }

    private void gameTime(@NotNull SunscreenUser<?> user, long worldAge) {
        WrapperPlayServerTimeUpdate time = new WrapperPlayServerTimeUpdate(worldAge, (long) user.worldTime(), false);
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
        TextInputContext context = user.session().menu().inputHandler().context(TextInputContext.class);
        String value = "";//context.stream().value();
        Component name = Component.text(value);
        ItemStack itemStack = ItemStack.builder()
            .type(ItemTypes.PAPER)
            .component(ComponentTypes.ITEM_MODEL, new ItemModel(ResourceLocation.minecraft("air")))
            .component(ComponentTypes.ITEM_NAME, name)
            .amount(1)
            .build();
        hideTooltip(user, itemStack);
        ArrayList<ItemStack> items = new ArrayList<>(
            List.of(
                itemStack
            )
        );
        items.addAll(Collections.nCopies(38, ItemStack.EMPTY));
        WrapperPlayServerWindowItems windowItems = new WrapperPlayServerWindowItems(-10_000, 0, items, null);
        user.connection().send(openWindow);
        user.connection().send(windowItems);
    }

    @Override
    public void removeMaps(@NotNull SunscreenUser<?> user) {
        final Map<Integer, Object> trackedEntities = entities.row(user.uniqueIdentifier());
        final int[] ids = trackedEntities.entrySet().stream().filter(entity -> entity.getValue() instanceof ItemFrame).map(Map.Entry::getKey).mapToInt(Integer::intValue).toArray();
        for (int id : ids) {
            trackedEntities.remove(id);
        }
        if (ids.length > 0) {
            user.connection().send(new WrapperPlayServerDestroyEntities(ids));
        }
    }

    private static @NotNull Vector3d loc2Vec3(@NotNull Location location) {
        return Vector3d.vec3(location.x(), location.y(), location.z());
    }

    private static void send(@NotNull SunscreenUser<?> user, @NotNull PacketWrapper<?>... wrappers) {
        Connection connection = user.connection();
        for (PacketWrapper<?> wrapper : wrappers) {
            connection.send(wrapper);
        }
    }

    private static int protocolVersion(@NotNull Player player) {
        return PacketEvents.getAPI().getServerManager().getVersion().getProtocolVersion();
        //return PacketEvents.getAPI().getPlayerManager().getClientVersion(player).getProtocolVersion();
    }

    private void hideTooltip(@NotNull SunscreenUser<?> user, @NotNull ItemStack itemStack) {
        Player player = (Player) user.platformSpecificPlayer();
        int protocolVersion = protocolVersion(player);
        if (protocolVersion >= ClientVersion.V_1_21_6.getProtocolVersion()) {
            itemStack.setComponent(ComponentTypes.TOOLTIP_DISPLAY, new ItemTooltipDisplay(true, Set.of()));
        } else {
            itemStack.setComponent(ComponentTypes.HIDE_TOOLTIP, Dummy.DUMMY);
        }
    }

    private static @NotNull WrapperPlayServerEntityEquipment horseEquipment(@NotNull String texturePath, int id, EquipmentSlot equipmentSlot) {
        return new WrapperPlayServerEntityEquipment(
                id,
                List.of(
                        new Equipment(equipmentSlot,
                                ItemStack.builder().type(ItemTypes.SADDLE).build()),
                        new Equipment(EquipmentSlot.BODY,
                                ItemStack.builder().type(ItemTypes.DIAMOND_HORSE_ARMOR).
                                        component(ComponentTypes.EQUIPPABLE,
                                                new ItemEquippable(EquipmentSlot.BODY, Sounds.ITEM_ARMOR_EQUIP_GENERIC, new ResourceLocation("sunscreen", texturePath), null, null, false, false, false)).build())));

    }

}
