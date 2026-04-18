package me.combimagnetron.sunscreen;

import me.combimagnetron.sunscreen.neo.input.InputHandler;
import me.combimagnetron.sunscreen.neo.input.context.MouseInputContext;
import me.combimagnetron.sunscreen.neo.input.context.TextInputContext;
import me.combimagnetron.sunscreen.neo.protocol.PlatformProtocolIntermediate;
import me.combimagnetron.sunscreen.neo.session.Session;
import me.combimagnetron.sunscreen.user.SunscreenUser;
import me.combimagnetron.sunscreen.util.Scheduler;
import me.combimagnetron.sunscreen.util.helper.RotationHelper;
import net.kyori.adventure.audience.Audience;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.event.player.PlayerPacketOutEvent;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.client.play.*;
import net.minestom.server.network.packet.server.play.CloseWindowPacket;
import net.minestom.server.network.packet.server.play.TimeUpdatePacket;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class PacketListener {

    public PacketListener() {
        GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();
        eventHandler.addListener(PlayerPacketOutEvent.class, event -> {
            Optional<SunscreenUser<Audience>> userOptional = SunscreenLibrary.library().users().user(event.getPlayer().getUuid());
            if (userOptional.isEmpty()) {
                return;
            }
            SunscreenUser<?> user = userOptional.get();
            if (!inMenu(user)) return;
            switch (event.getPacket()) {
                case TimeUpdatePacket _ -> handleTimeUpdate(event, user);
                default -> {}
            }
        });
        eventHandler.addListener(PlayerPacketEvent.class, event -> {
            Optional<SunscreenUser<Audience>> userOptional = SunscreenLibrary.library().users().user(event.getPlayer().getUuid());
            if (userOptional.isEmpty()) {
                return;
            }
            SunscreenUser<?> user = userOptional.get();
            if (!inMenu(user)) return;
            switch (event.getPacket()) {
                case ClientPlayerActionPacket _ -> handleDigging(event, user);
                case ClientUseItemPacket _ -> handleItemUse(event, user);
                case ClientInputPacket inputPacket -> handleSneak(inputPacket, user);
                case ClientPlayerRotationPacket(float yaw, float pitch, byte _) -> handleRotation(yaw, pitch, user);
                case ClientNameItemPacket(String itemName) -> handleNameItem(itemName, user);
                case ClientHeldItemChangePacket(short slot) -> handleSlotChange(slot, user);
                case ClientClickWindowPacket(int windowId, int _, short _, byte _, ClientClickWindowPacket.ClickType _,
                    Map<Short, ItemStack.Hash> _, ItemStack.Hash _) -> handleClose(windowId, user);
                case ClientCloseWindowPacket(int windowId) -> handleClose(windowId, user);
                default -> {}
            }
        });
    }

    private void handleClose(int windowId, SunscreenUser<?> user) {
        final Session session = user.session();
        if (session == null) return;
        final InputHandler inputHandler = session.menu().inputHandler();
        TextInputContext context = inputHandler.context(TextInputContext.class);
        Player player = (Player) user.platformSpecificPlayer();
        if (!context.active()) return;
        inputHandler.peek(TextInputContext.class, old -> old.withActive(false), user);
        SunscreenLibrary.library().intermediate().sendItems(user);
        Scheduler.delayTick(() -> {
            CloseWindowPacket closeWindow = new CloseWindowPacket(windowId);
            player.getPlayerConnection().sendPacket(closeWindow);
        });
    }

    private void handleSlotChange(short slot, SunscreenUser<?> user) {
        
    }

    private void handleItemUse(PlayerPacketEvent playerPacketEvent, SunscreenUser<?> user) {
        ClientUseItemPacket useItemPacket = (ClientUseItemPacket) playerPacketEvent.getPacket();
        final Session session = user.session();
        if (session == null) return;
        playerPacketEvent.setCancelled(true);
        final PlayerHand hand = useItemPacket.hand();
        if (hand != PlayerHand.MAIN) return;
        final InputHandler inputHandler = session.menu().inputHandler();
        inputHandler.peek(MouseInputContext.class, old -> old.withRightPressed(true), user);
    }

    private void handleNameItem(String itemName, SunscreenUser<?> user) {
        final Session session = user.session();
        if (session == null) return;
        final InputHandler inputHandler = session.menu().inputHandler();
        TextInputContext context = inputHandler.context(TextInputContext.class);
        if (!context.active()) return;
        if (itemName.length() == 50) {
            inputHandler.peek(TextInputContext.class, old -> old.append(itemName), user);
            PlatformProtocolIntermediate protocolIntermediate = SunscreenLibrary.library().intermediate();
            protocolIntermediate.openEmptyAnvil(user);
            return;
        }
        inputHandler.peek(TextInputContext.class, old -> old.withStream(itemName), user);
    }

    private void handleDigging(PlayerPacketEvent playerPacketEvent, SunscreenUser<?> user) {
        ClientPlayerActionPacket playerActionPacket = (ClientPlayerActionPacket) playerPacketEvent.getPacket();
        final Session session = user.session();
        if (session == null) return;
        final InputHandler inputHandler = session.menu().inputHandler();
        ClientPlayerActionPacket.Status status = playerActionPacket.status();
        boolean click;
        if (status == ClientPlayerActionPacket.Status.UPDATE_ITEM_STATE) {
            inputHandler.peek(MouseInputContext.class, old -> old.withRightPressed(false), user);
        }
        if (status == ClientPlayerActionPacket.Status.STARTED_DIGGING) {
            click = true;
            playerPacketEvent.setCancelled(true);
        } else if (status == ClientPlayerActionPacket.Status.CANCELLED_DIGGING) {
            click = false;
            playerPacketEvent.setCancelled(true);
        } else {
            return;
        }
        inputHandler.peek(MouseInputContext.class, old -> old.withLeftPressed(click), user);
    }

    private void handleTimeUpdate(PlayerPacketOutEvent event, SunscreenUser<?> user) {
        Player player = (Player) user.platformSpecificPlayer();
        TimeUpdatePacket timeUpdatePacket = (TimeUpdatePacket) event.getPacket();
        if (timeUpdatePacket.worldAge() < 0) return;
        event.setCancelled(true);
        player.getPlayerConnection().sendPacket(new TimeUpdatePacket(-2000, timeUpdatePacket.timeOfDay(), timeUpdatePacket.tickDayTime()));
    }

    public void handleSneak(ClientInputPacket input, SunscreenUser<?> user) {
        if (input.shift()) user.session().menu().close();
    }

    private void handleRotation(float yaw, float pitch, SunscreenUser<?> user) {
        final Session session = user.session();
        if (session == null) return;
        final InputHandler inputHandler = session.menu().inputHandler();
        inputHandler.peek(MouseInputContext.class, old -> old.withPosition(RotationHelper.convert(yaw, pitch, user.screenInfo())), user);
    }

    static boolean inMenu(@NotNull SunscreenUser<?> user) {
        return SunscreenLibrary.library().sessionHandler().inMenu(user);
    }

}
