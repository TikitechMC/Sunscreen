package me.combimagnetron.sunscreen.protocol;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
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
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.InteractionHand;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class ProtocolHandler extends ChannelDuplexHandler {
    final UUID uuid;
    final Optional<SunscreenUser<Audience>> userOptional;

    private final Map<Class<Packet<?>>,Function<Packet<?>, Packet<?>>> packetTransformers = new HashMap<>();

    public ProtocolHandler(UUID uuid) {
        this.uuid = uuid;
        this.userOptional = SunscreenLibrary.library().users().user(uuid);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        Optional<SunscreenUser<Audience>> userOptional = SunscreenLibrary.library().users().user(uuid);
        if (userOptional.isEmpty() || !(msg instanceof Packet<?>)) {
            return;
        }
        SunscreenUser<?> user = userOptional.get();
        if (!inMenu(user)) return;

        msg = switch (msg) {
            case ClientboundSetTimePacket packet -> handleTimeUpdate(packet, user);
            case ClientboundBlockUpdatePacket packet -> handleBlockChange(packet, user);
            default -> msg;
        };

        if (msg == null) return;

        try {
            super.write(ctx, msg, promise);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Optional<SunscreenUser<Audience>> userOptional = SunscreenLibrary.library().users().user(uuid);
        if (userOptional.isEmpty() || !(msg instanceof Packet<?>)) {
            return;
        }
        SunscreenUser<?> user = userOptional.get();
        if (!inMenu(user)) return;

        msg = switch (msg) {
            case ServerboundMovePlayerPacket.Rot packet -> handleRotation(packet, user);
            case ServerboundInteractPacket packet -> handleInteractEntity(packet, user);
            case ServerboundPlayerInputPacket packet -> handleSneak(packet, user);
            case ServerboundUseItemPacket packet -> handleUseItem(packet, user);
            case ServerboundRenameItemPacket packet -> handleNameItem(packet, user);
            case ServerboundSetCarriedItemPacket packet -> handleSlotChange(packet, user);
            case ServerboundContainerClickPacket packet -> handleClick(packet, user);
            case ServerboundContainerClosePacket packet -> handleCloseWindow(packet, user);
            case ServerboundPlayerActionPacket packet -> handleDigging(packet, user);
            default -> msg;
        };
    }

    private ServerboundPlayerActionPacket handleDigging(ServerboundPlayerActionPacket packet, SunscreenUser<?> user) {
        if (inMenu(user)) {
            final Session session = user.session();
            if (session == null) return packet;

            final InputHandler inputHandler = session.menu().inputHandler();
            ServerboundPlayerActionPacket.Action diggingAction = packet.getAction();
            if (diggingAction == ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM) {
                inputHandler.peek(MouseInputContext.class, old -> old.withRightPressed(false), user);
            }
            boolean left;
            if (diggingAction == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
                left = true;
            } else if (diggingAction == ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK) {
                left = false;
            } else {
                return packet;
            }
            inputHandler.peek(MouseInputContext.class, old -> old.withLeftPressed(left), user);
        } else {
            BlockPos pos = packet.getPos();
            Player player = (Player) user.platformSpecificPlayer();
            World world = player.getWorld();
            Block block = world.getBlockAt(pos.getX(), pos.getY(), pos.getZ());
            if (!block.getBlockData().getMaterial().name().contains("COPPER_GRATE")) return packet;
            Bukkit.broadcastMessage("je vader");
            player.playSound(new Location(player.getWorld(), pos.getX(), pos.getY(), pos.getZ()), Sound.BLOCK_COPPER_GRATE_HIT, SoundCategory.BLOCKS, 1f, 1f);
        }

        return packet;
    }

    private ServerboundContainerClosePacket handleCloseWindow(ServerboundContainerClosePacket packet, SunscreenUser<?> user) {
        if (!inMenu(user)) return packet;
        final Session session = user.session();
        if (session == null) return packet;
        final InputHandler inputHandler = session.menu().inputHandler();
        TextInputContext context = inputHandler.context(TextInputContext.class);
        if (!context.active()) return packet;
        inputHandler.peek(TextInputContext.class, old -> old.withActive(false), user);

        return packet;
    }

    private ServerboundContainerClickPacket handleClick(ServerboundContainerClickPacket packet, SunscreenUser<?> user) {
        if (!inMenu(user)) return packet;
        final Session session = user.session();
        if (session == null) return packet;
        final InputHandler inputHandler = session.menu().inputHandler();
        TextInputContext context = inputHandler.context(TextInputContext.class);
        if (!context.active()) return packet;
        inputHandler.peek(TextInputContext.class, old -> old.withActive(false), user);
        Scheduler.delayTick(() -> {
            user.connection().send(new ServerboundContainerClosePacket(packet.containerId()));
        });

        return packet;
    }


    private ServerboundSetCarriedItemPacket handleSlotChange(ServerboundSetCarriedItemPacket packet, SunscreenUser<?> user) {
        if (!inMenu(user)) return packet;
        int slot = packet.getSlot();
        final Session session = user.session();
        if (session != null) session.menu().inputHandler().peek(ScrollInputContext.class, old -> old.onSlotChange(slot), user);

        return packet;
    }

    private ServerboundRenameItemPacket handleNameItem(ServerboundRenameItemPacket packet, SunscreenUser<?> user) {
        if (!inMenu(user)) return packet;

        final Session session = user.session();
        if (session == null) return packet;
        final InputHandler inputHandler = session.menu().inputHandler();
        final String input = packet.getName();
        TextInputContext context = inputHandler.context(TextInputContext.class);
        if (!context.active()) return packet;
        if (input.length() == 50) {
            inputHandler.peek(TextInputContext.class, old -> old.append(input), user);
            PlatformProtocolIntermediate protocolIntermediate = SunscreenLibrary.library().intermediate();
            protocolIntermediate.openEmptyAnvil(user);
            return packet;
        }
        inputHandler.peek(TextInputContext.class, old -> old.withStream(input), user);
        return packet;
    }

    private ServerboundUseItemPacket handleUseItem(ServerboundUseItemPacket packet, SunscreenUser<?> user) {
        if (!inMenu(user)) return packet;

        final Session session = user.session();
        if (session != null && packet.getHand() == InteractionHand.MAIN_HAND) {
            final InputHandler inputHandler = session.menu().inputHandler();
            inputHandler.peek(MouseInputContext.class, old -> old.withRightPressed(true), user);
        }

        return packet;
    }

    private ServerboundMovePlayerPacket handleRotation(ServerboundMovePlayerPacket packet, SunscreenUser<?> user) {
        if (!inMenu(user)) return packet;
        final Session session = user.session();
        if (session == null) return packet;
        final InputHandler inputHandler = session.menu().inputHandler();
        float yaw = packet.yRot;
        float pitch = packet.xRot;
        inputHandler.peek(MouseInputContext.class, old -> old.withPosition(RotationHelper.convert(yaw, pitch, user.screenInfo())), user);

        return packet;
    }

    private ServerboundPlayerInputPacket handleSneak(ServerboundPlayerInputPacket packet, SunscreenUser<?> user) {
        if (inMenu(user) && packet.input().shift()) user.session().menu().close();
        return packet;
    }

    @Nullable
    private ServerboundInteractPacket handleInteractEntity(ServerboundInteractPacket packet, SunscreenUser<?> user) {
        if (!inMenu(user)) return packet;

        return packet.getEntityId() != user.entityId() ? packet : null;
    }

    private ClientboundSetTimePacket handleTimeUpdate(ClientboundSetTimePacket packet, SunscreenUser<?> user) {
        if (!inMenu(user)) return packet;
        return new ClientboundSetTimePacket(-2000, packet.dayTime(), packet.tickDayTime());
    }

    @Nullable
    private ClientboundBlockUpdatePacket handleBlockChange(ClientboundBlockUpdatePacket packet, SunscreenUser<?> user) {
        Player player = (Player) user.platformSpecificPlayer();
        if (!packet.getPos().equals(new BlockPos((int) player.getX(), (int) player.getY(), (int) player.getZ()))) return packet;
        return null;
    }

    static boolean inMenu(@NotNull SunscreenUser<?> user) {
        return SunscreenLibrary.library().sessionHandler().inMenu(user);
    }
}
