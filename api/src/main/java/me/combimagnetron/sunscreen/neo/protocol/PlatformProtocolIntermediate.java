package me.combimagnetron.sunscreen.neo.protocol;

import me.combimagnetron.passport.internal.entity.metadata.type.Vector3d;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.passport.util.math.Vec3f;
import me.combimagnetron.sunscreen.neo.graphic.Item;
import me.combimagnetron.sunscreen.neo.protocol.type.EntityReference;
import me.combimagnetron.sunscreen.neo.protocol.type.Location;
import me.combimagnetron.sunscreen.user.SunscreenUser;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public interface PlatformProtocolIntermediate<I> {

    EntityReference<?> spawnAndRideHorse(@NotNull SunscreenUser<?> user, @NotNull Location location);

    EntityReference<?> spawnAndFillItemFrame(@NotNull SunscreenUser<?> user, @NotNull Location location, byte @NotNull [] data, int mapId);

    EntityReference<?> spawnAndSpectateDisplay(@NotNull SunscreenUser<?> user, @NotNull Location location);

    EntityReference<?> spawnItemDisplay(@NotNull SunscreenUser<?> user, @NotNull Location location, @NotNull Item<I> item, @NotNull Vec2i screenPos);

    void setHorseArmor(@NotNull SunscreenUser<?> user, @NotNull String texturePath);

    void removeEntity(@NotNull SunscreenUser<?> user, int id);

    void updateMap(@NotNull SunscreenUser<?> user, int mapId, byte @NotNull [] data);

    void reset(@NotNull SunscreenUser<?> user, @NotNull Vector3d initialRotation);

    void gameTime(@NotNull SunscreenUser<?> user);

    void bundleDelimiter(@NotNull SunscreenUser<?> user);

    void openEmptyAnvil(@NotNull SunscreenUser<?> user);

    void sendItems(@NotNull SunscreenUser<?> user);

    void removeMaps(@NotNull SunscreenUser<?> user);

    default void removeEntity(@NotNull SunscreenUser<?> user, @NotNull EntityReference<?> reference) {
        removeEntity(user, reference.id());
    }

}
