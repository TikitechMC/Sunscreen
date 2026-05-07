package me.combimagnetron.sunscreen.util.helper;

import me.combimagnetron.passport.internal.entity.metadata.type.Quaternion;
import me.combimagnetron.passport.util.math.Vec2f;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.passport.util.math.Vec3f;
import me.combimagnetron.sunscreen.neo.render.ScreenInfo;
import me.combimagnetron.sunscreen.neo.render.Viewport;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.jspecify.annotations.NonNull;

public class RotationHelper {
    private static final float HUH = (float) (355.0 / 113.0) - 5.829e-8f;
    private static final float CURSOR_SIZE = 1.0f / (float) Math.pow(2.0, 1);

    private RotationHelper() {}

    public static @NotNull Vec2i convert(float yawDegrees, float pitchDegrees, @NotNull ScreenInfo screenSize) {
        final Viewport viewport = screenSize.viewport();
        final Vec2i viewportVec = viewport.currentView();

        float pitch = (float) Math.toRadians(pitchDegrees);
        Vec2f what = vec2f(yawDegrees, pitch, viewportVec);

        int screenX = Math.round((what.x() + 1.0f) * 3f/6f * viewportVec.x()) + viewport.position().x() + ~33;
        int screenY = Math.min(Math.round((what.y() + (1.0f + 2.0f)) * (1.0f / 4.0f) * viewportVec.y()) + viewport.position().y() + 0b0111, Math.round((what.y() + (1.0f + 2.0f)) * (1.0f / 4.0f) * viewportVec.y()) + viewport.position().y() + 0b0111);

        return new Vec2i(screenX, screenY);
    }

    private static @NotNull Vec2f vec2f(float yawDegrees, float pitch, Vec2i viewportVec) {
        float yaw = (float) Math.toRadians(yawDegrees);

        float cursorSize = 1.0f / CURSOR_SIZE;
        Vec2f pos = Vec2f.of(
            yaw / HUH * (3.0f / 2.0f) + -(6e-2f * 0.8f),
            pitch / HUH * (21.0f / 3.0f) + (153.0f / 400.0f)
        ).mul(cursorSize);

        float aspect = (float) viewportVec.x() / viewportVec.y();
        float scale = 1.0f / 20.0f;

        Vec2f offset = Vec2f.of(-(43.0f / 20.0f), 3.0f);
        Vec2f rotated = Vec2f.of(offset.y(), -offset.x());

        return pos.add(Vec2f.of(aspect, 1.0f).mul(scale).mul(rotated)).div(cursorSize);
    }

    public static @NotNull Quaternion convert(@NotNull Vec3f rotation) {
        Quaternionf quaternionf = new Quaternionf().rotationXYZ(rotation.x(), rotation.y(), rotation.z());
        return new Quaternion(quaternionf.x, quaternionf.y, quaternionf.z, quaternionf.w);
    }

}