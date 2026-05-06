package me.combimagnetron.sunscreen.protocol;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.*;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import me.combimagnetron.passport.internal.entity.impl.Interaction;
import me.combimagnetron.sunscreen.SunscreenLibrary;
import me.combimagnetron.sunscreen.neo.input.InputHandler;
import me.combimagnetron.sunscreen.neo.input.context.MouseInputContext;
import me.combimagnetron.sunscreen.neo.input.context.ScrollInputContext;
import me.combimagnetron.sunscreen.neo.input.context.TextInputContext;
import me.combimagnetron.sunscreen.neo.protocol.PlatformProtocolIntermediate;
import me.combimagnetron.sunscreen.neo.session.Session;
import me.combimagnetron.sunscreen.user.SunscreenUser;
import me.combimagnetron.sunscreen.util.Scheduler;
import me.combimagnetron.sunscreen.util.helper.RotationHelper;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ProtocolListener implements PacketListener {

    @Override
    public void onPacketReceive(PacketReceiveEvent packetReceiveEvent) {
        Optional<SunscreenUser<Audience>> userOptional = SunscreenLibrary.library().users().user(packetReceiveEvent.getUser().getUUID());
        if (userOptional.isEmpty()) {
            return;
        }
        SunscreenUser<?> user = userOptional.get();
        switch (packetReceiveEvent.getPacketType()) {
            case PacketType.Play.Client.PLAYER_ROTATION -> handleRotation(packetReceiveEvent, user);
            case PacketType.Play.Client.INTERACT_ENTITY -> handleInteractEntity(packetReceiveEvent, user);
            case PacketType.Play.Client.PLAYER_INPUT -> handleSneak(new WrapperPlayClientPlayerInput(packetReceiveEvent), user);
            case PacketType.Play.Client.PLAYER_DIGGING -> handleDigging(packetReceiveEvent, user);
            case PacketType.Play.Client.USE_ITEM -> handleUseItem(packetReceiveEvent, user);
            case PacketType.Play.Client.NAME_ITEM -> handleNameItem(new WrapperPlayClientNameItem(packetReceiveEvent), user);
            case PacketType.Play.Client.HELD_ITEM_CHANGE -> handleSlotChange(new WrapperPlayClientHeldItemChange(packetReceiveEvent), user);
            case PacketType.Play.Client.CLICK_WINDOW -> handleClick(new WrapperPlayClientClickWindow(packetReceiveEvent), user);
            case PacketType.Play.Client.CLOSE_WINDOW -> handleCloseWindow(new WrapperPlayClientCloseWindow(packetReceiveEvent), user);
            default -> {}
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent packetSendEvent) {
        Optional<SunscreenUser<Audience>> userOptional = SunscreenLibrary.library().users().user(packetSendEvent.getUser().getUUID());
        if (userOptional.isEmpty()) {
            return;
        }
        SunscreenUser<?> user = userOptional.get();
        if (!inMenu(user)) return;
        switch (packetSendEvent.getPacketType()) {
            case PacketType.Play.Server.TIME_UPDATE -> handleTimeUpdate(new WrapperPlayServerTimeUpdate(packetSendEvent), user);
            case PacketType.Play.Server.BLOCK_CHANGE -> handleBlockChange(packetSendEvent, user);
            default -> {}
        }
    }

    private void handleSlotChange(WrapperPlayClientHeldItemChange wrapperPlayClientSlotStateChange, SunscreenUser<?> user) {
        if (!inMenu(user)) return;
        int slot = wrapperPlayClientSlotStateChange.getSlot();
        if (slot == 4) return;
        final Session session = user.session();
        if (session == null) return;
        session.menu().inputHandler().peek(ScrollInputContext.class, old -> old.onSlotChange(slot), user);
        WrapperPlayServerHeldItemChange heldItemChange = new WrapperPlayServerHeldItemChange(4);
        user.connection().send(heldItemChange);
    }

    private void handleNameItem(WrapperPlayClientNameItem wrapperPlayClientNameItem, SunscreenUser<?> user) {
        if (!inMenu(user)) return;
        final Session session = user.session();
        if (session == null) return;
        final InputHandler inputHandler = session.menu().inputHandler();
        final String input = wrapperPlayClientNameItem.getItemName();
        TextInputContext context = inputHandler.context(TextInputContext.class);
        if (!context.active()) return;
        if (input.length() == 50) {
            inputHandler.peek(TextInputContext.class, old -> old.append(input), user);
            PlatformProtocolIntermediate protocolIntermediate = SunscreenLibrary.library().intermediate();
            protocolIntermediate.openEmptyAnvil(user);
            return;
        }
        inputHandler.peek(TextInputContext.class, old -> old.withStream(input), user);
    }

    private void handleUseItem(PacketReceiveEvent packetReceiveEvent, SunscreenUser<?> user) {
        if (!inMenu(user)) return;
        final Session session = user.session();
        if (session == null) return;
        packetReceiveEvent.setCancelled(true);
        WrapperPlayClientUseItem useItem = new WrapperPlayClientUseItem(packetReceiveEvent);
        if (useItem.getHand() != InteractionHand.MAIN_HAND) return;
        final InputHandler inputHandler = session.menu().inputHandler();
        inputHandler.peek(MouseInputContext.class, old -> old.withRightPressed(true), user);
    }

    private void handleDigging(PacketReceiveEvent event, SunscreenUser<?> user) {
        WrapperPlayClientPlayerDigging wrapperPlayClientPlayerDigging = new WrapperPlayClientPlayerDigging(event);
        if (inMenu(user)) {
            final Session session = user.session();
            if (session == null) return;
            event.setCancelled(true);
            final InputHandler inputHandler = session.menu().inputHandler();
            DiggingAction diggingAction = wrapperPlayClientPlayerDigging.getAction();
            if (diggingAction == DiggingAction.RELEASE_USE_ITEM) {
                inputHandler.peek(MouseInputContext.class, old -> old.withRightPressed(false), user);
            }
            boolean left;
            if (diggingAction == DiggingAction.START_DIGGING) {
                left = true;
            } else if (diggingAction == DiggingAction.CANCELLED_DIGGING) {
                left = false;
            } else {
                return;
            }
            inputHandler.peek(MouseInputContext.class, old -> old.withLeftPressed(left), user);
        } else {
            Vector3i vector3i = wrapperPlayClientPlayerDigging.getBlockPosition();
            Player player = (Player) user.platformSpecificPlayer();
            World world = player.getWorld();
            Block block = world.getBlockAt(vector3i.x, vector3i.y, vector3i.z);
            if (!block.getBlockData().getMaterial().name().contains("COPPER_GRATE")) return;
            player.playSound(new Location(player.getWorld(), vector3i.x, vector3i.y, vector3i.z), Sound.BLOCK_COPPER_GRATE_HIT, SoundCategory.BLOCKS, 1f, 1f);
        }
    }

    private void handleClick(WrapperPlayClientClickWindow window, SunscreenUser<?> user) {
        if (!inMenu(user)) return;
        final Session session = user.session();
        if (session == null) return;
        final InputHandler inputHandler = session.menu().inputHandler();
        TextInputContext context = inputHandler.context(TextInputContext.class);
        if (!context.active()) return;
        inputHandler.peek(TextInputContext.class, old -> old.withActive(false), user);
        SunscreenLibrary.library().intermediate().sendItems(user);
        Scheduler.delayTick(() -> {
            WrapperPlayServerCloseWindow closeWindow = new WrapperPlayServerCloseWindow(window.getWindowId());
            user.connection().send(closeWindow);
        });
    }

    private void handleCloseWindow(WrapperPlayClientCloseWindow closeWindow, SunscreenUser<?> user) {
        if (!inMenu(user)) return;
        final Session session = user.session();
        if (session == null) return;
        final InputHandler inputHandler = session.menu().inputHandler();
        TextInputContext context = inputHandler.context(TextInputContext.class);
        if (!context.active()) return;
        SunscreenLibrary.library().intermediate().sendItems(user);
        inputHandler.peek(TextInputContext.class, old -> old.withActive(false), user);
    }

    private void handleTimeUpdate(WrapperPlayServerTimeUpdate wrapperPlayServerTimeUpdate, SunscreenUser<?> user) {
        if (!inMenu(user)) return;
        wrapperPlayServerTimeUpdate.setWorldAge(-50);
    }

    private void handleBlockChange(PacketSendEvent packetSendEvent, SunscreenUser<?> user) {
        WrapperPlayServerBlockChange blockChange = new WrapperPlayServerBlockChange(packetSendEvent);
        Player player = (Player) user.platformSpecificPlayer();
        Vector3i vector3i = blockChange.getBlockPosition();
        Location location = player.getLocation();
        if (vector3i.x != location.getBlockX() && vector3i.z != location.getBlockZ()) return;
        //if (!blockChange.getBlockPosition().equals(new Vector3i((int) player.getX(), (int) player.getY() + 1, (int) player.getZ()))) return;
        packetSendEvent.setCancelled(true);
    }

    public void handleSneak(WrapperPlayClientPlayerInput input, SunscreenUser<?> user) {
        if (!inMenu(user)) return;
        if (input.isShift()) user.session().menu().close();
    }

    private void handleInteractEntity(PacketReceiveEvent packetReceiveEvent, SunscreenUser<?> user) {
        if (!inMenu(user)) return;
        WrapperPlayClientInteractEntity wrapperPlayClientInteractEntity = new WrapperPlayClientInteractEntity(packetReceiveEvent);
        if (wrapperPlayClientInteractEntity.getEntityId() != user.entityId()) return;
        packetReceiveEvent.setCancelled(true);
    }

    private void handleRotation(PacketReceiveEvent event, SunscreenUser<?> user) {
        WrapperPlayClientPlayerRotation wrapperPlayClientPlayerRotation = new WrapperPlayClientPlayerRotation(event);
        if (!inMenu(user)) return;
        event.setCancelled(true);
        final Session session = user.session();
        if (session == null) return;
        if (session.menu().useNativeClientPath()) {
            return;
        }
        final InputHandler inputHandler = session.menu().inputHandler();
        float rawYaw = wrapperPlayClientPlayerRotation.getYaw();
        float yaw = ((rawYaw + 360/2f) % 360 + 360) % 360 - 360/2f;
        float pitch = wrapperPlayClientPlayerRotation.getPitch();
        inputHandler.peek(MouseInputContext.class, old -> old.withPosition(RotationHelper.convert(yaw, pitch, user.screenInfo())), user);
    }

    static boolean inMenu(@NotNull SunscreenUser<?> user) {
        return SunscreenLibrary.library().sessionHandler().inMenu(user);
    }

}
