package me.combimagnetron.sunscreen.user;

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
import me.combimagnetron.sunscreen.neo.session.Session;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class UserImpl implements SunscreenUser<Player> {
    private final Player player;
    private final Connection connection;
    private final ScreenInfo screenInfo = new ScreenInfo(new Viewport(Vec2i.of(800, 450), Vec2i.of(800, 450), Vec2i.zero()));

    public static UserImpl of(Player player) {
        return new UserImpl(player);
    }

    private UserImpl(Player player) {
        this.player = player;
        this.connection = null;
    }

    @Override
    public @NotNull ScreenInfo screenInfo() {
        return screenInfo;
    }

    @Override
    public Session session() {
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
        return null;
    }

    @Override
    public Player platformSpecificPlayer() {
        return player;
    }

    @Override
    public String name() {
        return player.getUsername();
    }

    @Override
    public UUID uniqueIdentifier() {
        return player.getUuid();
    }

    @Override
    public Connection connection() {
        return connection;
    }

    @Override
    public Vector3d position() {
        return Vector3d.vec3(player.getPosition().x(), player.getPosition().y(), player.getPosition().z());
    }

    @Override
    public void show(Entity entity) {

    }

    @Override
    public int entityId() {
        return player.getEntityId();
    }

    @Override
    public Vector3d rotation() {
        return Vector3d.vec3(player.getPosition().yaw(), player.getPosition().pitch(), 0);
    }

    @Override
    public int gameMode() {
        return player.getGameMode().ordinal();
    }

    @Override
    public float worldTime() {
        return player.getInstance().getTime();
    }

    @Override
    public void resendInv() {
        player.getInventory().update();
    }

    @Override
    public boolean useNativeUi() {
        return false;
    }

}
