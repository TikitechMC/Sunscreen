package me.combimagnetron.sunscreen.neo.render.engine.pipeline;

import me.combimagnetron.sunscreen.neo.MenuRoot;
import me.combimagnetron.sunscreen.neo.loader.MenuComponent;
import me.combimagnetron.sunscreen.user.SunscreenUser;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.*;

public class RenderThreadPoolHandler {
    public final static ScheduledExecutorService EXECUTOR_SERVICE =
        Executors.newScheduledThreadPool(1, Thread.ofVirtual().factory());

    public static @NotNull RenderPipeline start(@NotNull SunscreenUser<?> user, @NotNull MenuRoot menuRoot, @NotNull Collection<MenuComponent<?>> loadedComponents) {
        return new RenderPipeline(EXECUTOR_SERVICE, user, UUID.randomUUID(), menuRoot.elementLikes(), loadedComponents);
    }

}
