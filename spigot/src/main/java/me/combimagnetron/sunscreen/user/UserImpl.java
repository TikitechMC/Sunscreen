package me.combimagnetron.sunscreen.user;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import me.combimagnetron.passport.internal.entity.Entity;
import me.combimagnetron.passport.internal.entity.metadata.type.Vector3d;
import me.combimagnetron.passport.internal.network.Connection;
import me.combimagnetron.sunscreen.SunscreenLibrary;
import me.combimagnetron.sunscreen.neo.ActiveMenu;
import me.combimagnetron.sunscreen.neo.MenuTemplate;
import me.combimagnetron.sunscreen.neo.protocol.type.Location;
import me.combimagnetron.sunscreen.neo.render.ScreenInfo;
import me.combimagnetron.sunscreen.neo.render.Viewport;
import me.combimagnetron.sunscreen.neo.session.Session;
import me.combimagnetron.passport.util.math.Vec2i;
import net.minecraft.network.protocol.Packet;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserImpl implements SunscreenUser<Player> {
    private final Player player;
    private final Connection connection;
    private final ClientVersion version;
    private final float fov = 70;
    private final ScreenInfo screenInfo = new ScreenInfo(new Viewport(Vec2i.of(800, 450), Vec2i.of(800, 450), Vec2i.zero()));

    public static UserImpl of(Player player) {
        return new UserImpl(player);
    }

    private UserImpl(Player player) {
        this.player = player;
        this.connection = new PacketEventsConnectionImpl<>(player);
        this.version = PacketEvents.getAPI().getPlayerManager().getUser(player).getClientVersion();
        /*if (node == null) {
            AspectRatioMenu menu = new AspectRatioMenu(this);
            SunscreenLibrary.library().menuTicker().start(menu);
            SunscreenLibrary.library().sessionHandler().session(Session.session(menu, this));
            return;
        }
        if (node.value() == null) {
            //SunscreenLibrary.library().menuTicker().start(new UserSetupMenu(this));
            return;
        }
        this.screenSize = ScreenSize.fromString(node.value());*/
    }

    @Override
    public Player platformSpecificPlayer() {
        return player;
    }

    @Override
    public String name() {
        return player.getName();
    }

    @Override
    public UUID uniqueIdentifier() {
        return player.getUniqueId();
    }

    @Override
    public Connection connection() {
        return connection;
    }

    @Override
    public Vector3d position() {
        return Vector3d.vec3(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
    }

    @Override
    public void show(Entity entity) {
        WrapperPlayServerSpawnEntity clientSpawnEntity = new WrapperPlayServerSpawnEntity(entity.id().intValue(), Optional.of(entity.uuid()), EntityTypes.getById(version, entity.type().id()), new com.github.retrooper.packetevents.util.Vector3d(entity.position().x(), entity.position().y(), entity.position().z()), (float) entity.rotation().x(), (float) entity.rotation().y(), (float) entity.rotation().z(), entity.data().i(), Optional.empty());
        if (entity instanceof Entity.AbstractEntity abstractEntity) {
            abstractEntity.prepare();
        }
        List<EntityData<?>> entityData = entity.type().metadata().entityData();
        WrapperPlayServerEntityMetadata clientEntityMetadata = new WrapperPlayServerEntityMetadata(entity.id().intValue(), entityData);
        connection().send(clientSpawnEntity);
        connection().send(clientEntityMetadata);
    }

    @Override
    public int entityId() {
        return player.getEntityId();
    }

    @Override
    public Vector3d rotation() {
        return Vector3d.vec3(player.getLocation().getYaw(), player.getLocation().getPitch(), 0);
    }

    @Override
    public int gameMode() {
        return player.getGameMode().getValue();
    }

    @Override
    public @NotNull ScreenInfo screenInfo() {
        return screenInfo;
    }

    @Override
    public @Nullable Session session() {
        return SunscreenLibrary.library().sessionHandler().session(this);
    }

    @Override
    public @NotNull Session open(@NotNull MenuTemplate template) {
        final Session current = this.session();
        if (current != null) {
            current.menu().close();
        }

        new ActiveMenu(template, this, template.identifier());
        final Session created = this.session();
        if (created == null) {
            throw new IllegalStateException("Failed to create menu session for " + this.uniqueIdentifier());
        }
        return created;
    }

    @Override
    public @NotNull Location eyeLocation() {
        org.bukkit.Location eyeLocation = player.getEyeLocation();
        return new Location(eyeLocation.x(), eyeLocation.y(), eyeLocation.z());
    }

    @Override
    public float worldTime() {
        return player.getWorld().getTime();
    }

    @Override
    public void resendInv() {
        player.updateInventory();
    }

    public static class PacketEventsConnectionImpl<T> implements Connection {
        private final T player;

        public PacketEventsConnectionImpl(T player) {
            this.player = player;
        }

        @Override
        public void send(Packet<?> packetHolder) {
            ((CraftPlayer) player).getHandle().connection.send(packetHolder);
        }
    }

}
