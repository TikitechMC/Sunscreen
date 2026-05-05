package me.combimagnetron.sunscreen.fabric.protocol;

import me.combimagnetron.sunscreen.SunscreenLibrary;
import me.combimagnetron.sunscreen.fabric.user.UserImpl;
import me.combimagnetron.sunscreen.neo.input.InputHandler;
import me.combimagnetron.sunscreen.neo.input.context.MouseInputContext;
import me.combimagnetron.sunscreen.neo.input.context.ScrollInputContext;
import me.combimagnetron.sunscreen.neo.input.context.TextInputContext;
import me.combimagnetron.sunscreen.neo.session.Session;
import me.combimagnetron.sunscreen.user.SunscreenUser;
import me.combimagnetron.sunscreen.util.Scheduler;
import me.combimagnetron.sunscreen.util.helper.RotationHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.NotNull;

public final class FabricPacketBridge {
    private FabricPacketBridge() {
    }

    public static boolean inMenu(@NotNull ServerPlayer player) {
        UserImpl user = tryUser(player);
        return user != null && SunscreenLibrary.library().sessionHandler().inMenu(user);
    }

    public static void handleRotation(@NotNull ServerPlayer player, float yaw, float pitch) {
        SunscreenUser<?> user = user(player);
        if (!inMenu(player)) {
            return;
        }
        Session session = user.session();
        if (session == null) {
            return;
        }
        if (session.menu().useNativeClientPath()) {
            return;
        }
        InputHandler inputHandler = session.menu().inputHandler();
        inputHandler.peek(MouseInputContext.class, old -> old.withPosition(RotationHelper.convert(yaw, pitch, user.screenInfo())), user);
    }

    public static void handleSneak(@NotNull ServerPlayer player, boolean shiftPressed) {
        if (!shiftPressed || !inMenu(player)) {
            return;
        }
        SunscreenUser<?> user = user(player);
        Session session = user.session();
        if (session != null) {
            session.menu().close();
        }
    }

    public static boolean handleDigging(@NotNull ServerPlayer player, @NotNull ServerboundPlayerActionPacket packet) {
        ServerboundPlayerActionPacket.Action action = packet.getAction();
        boolean startDigging = action == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK;
        boolean cancelDigging = action == ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK;
        boolean releaseUseItem = action == ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM;
        if (!inMenu(player)) {
            if (startDigging) {
                var state = player.level().getBlockState(packet.getPos());
                Object id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
                if (FabricMappingCompat.namespacedPath(id).contains("copper_grate")) {
                    player.level().playSound(player, packet.getPos(), SoundEvents.COPPER_GRATE_HIT, SoundSource.BLOCKS, 1f, 1f);
                }
            }
            return false;
        }
        SunscreenUser<?> user = user(player);
        Session session = user.session();
        if (session == null) {
            return false;
        }
        InputHandler inputHandler = session.menu().inputHandler();
        if (releaseUseItem) {
            inputHandler.peek(MouseInputContext.class, old -> old.withRightPressed(false), user);
        }
        if (startDigging) {
            inputHandler.peek(MouseInputContext.class, old -> old.withLeftPressed(true), user);
            return true;
        }
        if (cancelDigging) {
            inputHandler.peek(MouseInputContext.class, old -> old.withLeftPressed(false), user);
            return true;
        }
        return true;
    }

    /**
     * @return true if the use-item packet should be cancelled (Spigot parity: do not swing/consume items in menu).
     */
    public static boolean handleUseItem(@NotNull ServerPlayer player, @NotNull InteractionHand hand) {
        if (!inMenu(player)) {
            return false;
        }
        SunscreenUser<?> user = user(player);
        Session session = user.session();
        if (session == null) {
            return false;
        }
        if (hand == InteractionHand.MAIN_HAND) {
            session.menu().inputHandler().peek(MouseInputContext.class, old -> old.withRightPressed(true), user);
        }
        return true;
    }

    /**
     * @return true if the interact packet should be cancelled (Spigot parity: block self-interact while in menu).
     */
    public static boolean handleInteractEntity(@NotNull ServerPlayer player, @NotNull ServerboundInteractPacket packet) {
        if (!inMenu(player)) {
            return false;
        }
        net.minecraft.world.entity.Entity target = packet.getTarget(FabricMappingCompat.serverLevel(player));
        if (target == null) {
            return false;
        }
        SunscreenUser<?> user = user(player);
        return target.getId() == user.entityId();
    }

    public static void handleNameItem(@NotNull ServerPlayer player, @NotNull String itemName) {
        if (!inMenu(player)) {
            return;
        }
        SunscreenUser<?> user = user(player);
        Session session = user.session();
        if (session == null) {
            return;
        }
        InputHandler inputHandler = session.menu().inputHandler();
        TextInputContext context = inputHandler.context(TextInputContext.class);
        if (!context.active()) {
            return;
        }
        if (itemName.length() == 50) {
            inputHandler.peek(TextInputContext.class, old -> old.append(itemName), user);
            SunscreenLibrary.library().intermediate().openEmptyAnvil(user);
            return;
        }
        inputHandler.peek(TextInputContext.class, old -> old.withStream(itemName), user);
    }

    public static void handleSlotChange(@NotNull ServerPlayer player, int slot) {
        if (!inMenu(player)) {
            return;
        }
        if (slot == 4) {
            return;
        }
        SunscreenUser<?> user = user(player);
        Session session = user.session();
        if (session == null) {
            return;
        }
        session.menu().inputHandler().peek(ScrollInputContext.class, old -> old.onSlotChange(slot), user);
        player.connection.send(FabricMappingCompat.createSetCarriedItemPacket(4));
    }

    public static void handleContainerClick(@NotNull ServerPlayer player, int containerId) {
        if (!inMenu(player)) {
            return;
        }
        SunscreenUser<?> user = user(player);
        Session session = user.session();
        if (session == null) {
            return;
        }
        InputHandler inputHandler = session.menu().inputHandler();
        TextInputContext context = inputHandler.context(TextInputContext.class);
        if (!context.active()) {
            return;
        }
        inputHandler.peek(TextInputContext.class, old -> old.withActive(false), user);
        SunscreenLibrary.library().intermediate().sendItems(user);
        Scheduler.delayTick(() -> {
            player.connection.send(new ClientboundContainerClosePacket(containerId));
            if (player.containerMenu != null && player.containerMenu.containerId == containerId) {
                player.closeContainer();
            }
        });
    }

    public static void handleContainerClose(@NotNull ServerPlayer player) {
        if (!inMenu(player)) {
            return;
        }
        SunscreenUser<?> user = user(player);
        Session session = user.session();
        if (session == null) {
            return;
        }
        InputHandler inputHandler = session.menu().inputHandler();
        TextInputContext context = inputHandler.context(TextInputContext.class);
        if (!context.active()) {
            return;
        }
        SunscreenLibrary.library().intermediate().sendItems(user);
        inputHandler.peek(TextInputContext.class, old -> old.withActive(false), user);
    }

    private static UserImpl user(ServerPlayer player) {
        UserImpl user = tryUser(player);
        if (user == null) {
            throw new IllegalStateException("No Sunscreen user found for " + player.getUUID());
        }
        return user;
    }

    private static UserImpl tryUser(ServerPlayer player) {
        me.combimagnetron.sunscreen.fabric.SunscreenLibraryFabric library = me.combimagnetron.sunscreen.fabric.SunscreenFabricMod.library();
        if (library == null) {
            return null;
        }
        return library.user(player.getUUID());
    }
}
