package me.combimagnetron.sunscreen.util.helper;

import me.combimagnetron.sunscreen.neo.element.ElementLike;
import me.combimagnetron.sunscreen.neo.property.Position;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.render.Viewport;
import org.jetbrains.annotations.NotNull;

public class HoverHelper {

    private HoverHelper() {}

    public static <E extends ElementLike<E>> boolean in(@NotNull ElementLike<E> elementLike, @NotNull Vec2i cursor) {
        final Position position = elementLike.propOrThrow(Position.class);
        final Size size = elementLike.propOrThrow(Size.class);
        Vec2i vecPosition = position.value();
        Vec2i vecSize = size.value();
        if (vecPosition == null || vecSize == null) return false;
        return in(vecPosition, vecSize, cursor);
    }

    public static boolean in(@NotNull Vec2i vecPosition, @NotNull Vec2i vecSize, @NotNull Vec2i cursor) {
        boolean xCheck = (cursor.x() >= vecPosition.x() && cursor.x() <= vecPosition.x() + vecSize.x());
        boolean yCheck = (cursor.y() >= vecPosition.y() && cursor.y() <= vecPosition.y() + vecSize.y());
        return xCheck && yCheck;
    }

    public static <E extends ElementLike<E>> boolean in(@NotNull ElementLike<E> elementLike, @NotNull Viewport viewport) {
        Vec2i vecPosition = viewport.position();
        Vec2i vecSize = viewport.currentView();
        Vec2i elementPosition = elementLike.propOrThrow(Position.class).value();
        boolean xCheck = (elementPosition.x() >= vecPosition.x() && elementPosition.x() <= vecPosition.x() + vecSize.x());
        boolean yCheck = (elementPosition.y() >= vecPosition.y() && elementPosition.y() <= vecPosition.y() + vecSize.y());
        return xCheck && yCheck;
    }

}
