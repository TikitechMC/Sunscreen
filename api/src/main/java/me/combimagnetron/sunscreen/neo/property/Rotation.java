package me.combimagnetron.sunscreen.neo.property;

import me.combimagnetron.passport.internal.entity.metadata.type.Quaternion;
import me.combimagnetron.passport.util.math.Vec3f;
import me.combimagnetron.sunscreen.neo.property.handler.PropertyHandler;
import me.combimagnetron.sunscreen.util.helper.RotationHelper;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

public record Rotation(@NotNull Vec3f value) implements Property<Vec3f, Rotation> {
    private static final Rotation ZERO = new Rotation(Vec3f.of(0, 0, 0));

    public static @NotNull Rotation zero() {
        return ZERO;
    }

    public static @NotNull Rotation of(@NotNull Vec3f vec3f) {
        return new Rotation(vec3f);
    }

    public static @NotNull Rotation of(float x, float y, float z) {
        return new Rotation(Vec3f.of(x, y, z));
    }

    public @NotNull Rotation pitch(float pitch) {
        return new Rotation(value.mul(0, 1, 1).add(pitch, 0, 0));
    }

    public @NotNull Rotation yaw(float yaw) {
        return new Rotation(value.mul(1, 0, 1).add(0, yaw, 0));
    }

    public @NotNull Rotation roll(float roll) {
        return new Rotation(value.mul(1, 1, 0).add(0, 0, roll));
    }

    public @NotNull Quaternion quaternion() {
        return RotationHelper.convert(value);
    }

    @Override
    public @NotNull Class<Vec3f> type() {
        return Vec3f.class;
    }

    @Override
    public @NotNull PropertyHandler<Rotation> handler() {
        return null;
    }

}
