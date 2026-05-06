package me.combimagnetron.sunscreen.neo.element.impl;

import me.combimagnetron.sunscreen.neo.element.GenericModernElement;
import me.combimagnetron.sunscreen.neo.graphic.GraphicLike;
import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.render.engine.context.RenderContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class ImageElement<G extends GraphicLike<G>> extends GenericModernElement<ImageElement<G>, G> {
    private final G graphic;

    public ImageElement(@NotNull Identifier identifier, @NotNull G graphicLike) {
        super(identifier);
        this.graphic = graphicLike;
    }

    @Override
    public @Nullable Vec2i intrinsicSize() {
        return graphic.bufferedColorSpace().size();
    }

    @Override
    public @NonNull G render(@NonNull Size property, @Nullable RenderContext context) {
        return graphic;
    }

}
