package me.combimagnetron.sunscreen.neo.element;

import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.graphic.GraphicLike;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.render.Renderable;
import org.jetbrains.annotations.Nullable;

public interface ModernElement<E extends ModernElement<E, G>, G extends GraphicLike<G>> extends ElementLike<E>, Renderable<Size, G> {

    /**
     * Natural pixel size from content (bitmap, text, shape, etc.), or {@code null} if unknown without theme / explicit {@link Size}.
     */
    default @Nullable Vec2i intrinsicSize() {
        return null;
    }
}
