package me.combimagnetron.sunscreen.fabric.user;

import me.combimagnetron.passport.internal.entity.Entity;
import me.combimagnetron.passport.internal.entity.metadata.type.Vector3d;
import me.combimagnetron.passport.internal.network.Connection;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.SunscreenLibrary;
import me.combimagnetron.sunscreen.neo.ActiveMenu;
import me.combimagnetron.sunscreen.neo.MenuTemplate;
import me.combimagnetron.sunscreen.neo.protocol.type.Location;
import me.combimagnetron.sunscreen.neo.render.ScreenInfo;
import me.combimagnetron.sunscreen.neo.render.Viewport;
import me.combimagnetron.sunscreen.fabric.SunscreenFabricMod;
import me.combimagnetron.sunscreen.fabric.SunscreenLibraryFabric;
import me.combimagnetron.sunscreen.fabric.protocol.FabricMappingCompat;
import me.combimagnetron.sunscreen.neo.session.Session;
import me.combimagnetron.sunscreen.user.SunscreenUser;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collections;
import java.util.UUID;

public final class UserImpl implements SunscreenUser<Audience> {
    private final ServerPlayer player;
    private final Audience audience;
    private final ScreenInfo screenInfo = new ScreenInfo(new Viewport(Vec2i.of(800, 450), Vec2i.of(800, 450), Vec2i.zero()));
    private volatile boolean nativeUi;

    public static UserImpl of(ServerPlayer player) {
        return new UserImpl(player);
    }

    /**
     * Returns the Sunscreen user for this player when the Fabric library is initialised, creating
     * the {@link UserImpl} if the play-join handler has not run yet (e.g. very early custom payloads).
     */
    public static @Nullable UserImpl tryOf(ServerPlayer player) {
        SunscreenLibraryFabric library = SunscreenFabricMod.library();
        if (library == null) {
            return null;
        }
        return library.userManager().ensureUser(player);
    }

    private UserImpl(ServerPlayer player) {
        this.player = player;
        this.audience = new PlayerAudience(player);
    }

    public ServerPlayer player() {
        return player;
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
        final Session current = session();
        if (current != null) {
            current.menu().close();
        }
        new ActiveMenu(template, this, template.identifier());
        final Session created = session();
        if (created == null) {
            throw new IllegalStateException("Failed to create menu session for " + uniqueIdentifier());
        }
        return created;
    }

    @Override
    public @NotNull Location eyeLocation() {
        return new Location(player.getX(), player.getEyeY(), player.getZ());
    }

    @Override
    public Audience platformSpecificPlayer() {
        return audience;
    }

    @Override
    public String name() {
        return player.getName().getString();
    }

    @Override
    public UUID uniqueIdentifier() {
        return player.getUUID();
    }

    @Override
    public Connection connection() {
        return null;
    }

    @Override
    public Vector3d position() {
        return Vector3d.vec3(player.getX(), player.getY(), player.getZ());
    }

    @Override
    public void show(Entity entity) {
        // Fabric protocol implementation sends packets/entities directly where needed.
    }

    @Override
    public int entityId() {
        return player.getId();
    }

    @Override
    public Vector3d rotation() {
        return Vector3d.vec3(player.getYRot(), player.getXRot(), 0);
    }

    @Override
    public int gameMode() {
        return player.gameMode.getGameModeForPlayer().getId();
    }

    @Override
    public float worldTime() {
        return 0f;
    }

    @Override
    public void resendInv() {
        player.containerMenu.broadcastChanges();
    }

    @Override
    public boolean useNativeUi() {
        return nativeUi;
    }

    @Override
    public void setNativeUi(boolean enabled) {
        this.nativeUi = enabled;
    }

    public record PlayerAudience(ServerPlayer player) implements ForwardingAudience {
        @Override
        public Iterable<? extends Audience> audiences() {
            return Collections.emptyList();
        }
    }
}
