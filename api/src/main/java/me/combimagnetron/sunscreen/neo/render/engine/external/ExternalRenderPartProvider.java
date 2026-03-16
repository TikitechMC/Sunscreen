package me.combimagnetron.sunscreen.neo.render.engine.external;

import me.combimagnetron.sunscreen.neo.graphic.BufferedColorSpace;
import me.combimagnetron.sunscreen.neo.render.engine.context.RenderContext;
import org.jetbrains.annotations.NotNull;

public interface ExternalRenderPartProvider {

    /**
     *
     * @param space the color space to apply pixel changes to
     * @param context the given context
     * @return a boolean that is true whenever a rerender is needed on the client.
     */
    boolean render(@NotNull BufferedColorSpace space, @NotNull RenderContext context);

}
