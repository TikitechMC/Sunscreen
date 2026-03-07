package me.combimagnetron.sunscreen.neo;

import me.combimagnetron.passport.internal.entity.metadata.type.Vector3d;
import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.SunscreenLibrary;
import me.combimagnetron.sunscreen.neo.cursor.CursorStyle;
import me.combimagnetron.sunscreen.neo.element.ElementLike;
import me.combimagnetron.sunscreen.neo.element.GenericInteractableModernElement;
import me.combimagnetron.sunscreen.neo.input.InputHandler;
import me.combimagnetron.sunscreen.neo.input.context.InputContext;
import me.combimagnetron.sunscreen.neo.input.context.MouseInputContext;
import me.combimagnetron.sunscreen.neo.input.context.ScrollInputContext;
import me.combimagnetron.sunscreen.neo.layout.Layout;
import me.combimagnetron.sunscreen.neo.loader.MenuComponent;
import me.combimagnetron.sunscreen.neo.loader.MenuComponentLoaderContext;
import me.combimagnetron.sunscreen.neo.protocol.PlatformProtocolIntermediate;
import me.combimagnetron.sunscreen.neo.protocol.type.EntityReference;
import me.combimagnetron.sunscreen.neo.protocol.type.Location;
import me.combimagnetron.sunscreen.neo.render.engine.pipeline.RenderPipeline;
import me.combimagnetron.sunscreen.neo.render.engine.pipeline.RenderThreadPoolHandler;
import me.combimagnetron.sunscreen.neo.session.Session;
import me.combimagnetron.sunscreen.user.SunscreenUser;
import me.combimagnetron.sunscreen.util.IdentifierHolder;
import me.combimagnetron.sunscreen.util.Scheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActiveMenu implements IdentifierHolder {
    private final InputHandler inputHandler = InputHandler.defaults(this);
    private final Map<Class<MenuComponent<?>>, MenuComponent<?>> loadedComponents = new HashMap<>();
    private final MenuRoot menuRoot = new MenuRoot();
    private final Vector3d initialRotation;
    private final SunscreenUser<?> user;
    private final RenderPipeline renderPipeline;
    private final Identifier identifier;

    public ActiveMenu(@NotNull MenuTemplate template, @NotNull SunscreenUser<?> user, @NotNull Identifier identifier) {
        this.initialRotation = user.rotation();
        this.user = user;
        this.identifier = identifier;
        template.build(menuRoot);
        loadComponents();
        PlatformProtocolIntermediate intermediate = SunscreenLibrary.library().intermediate();
        intermediate.gameTime(user);
        for (ElementLike<?> elementLike : menuRoot.elementLikes()) {
            if (elementLike instanceof GenericInteractableModernElement<?,?,?> interactableModernElement) {
                interactableModernElement.inputHandler(inputHandler);
            }
            if (elementLike instanceof Layout<?> layout) {
                layout.inputHandler(inputHandler);
            }
        }
        renderPipeline = RenderThreadPoolHandler.start(user, menuRoot, loadedComponents.values());
        Location location = user.eyeLocation();
        intermediate.spawnAndSpectateDisplay(user, location);
        intermediate.spawnAndRideHorse(user, user.eyeLocation());
        SunscreenLibrary.library().sessionHandler().session(new Session(this, user));
    }

    private void loadComponents() {
        MenuComponentLoaderContext context = new MenuComponentLoaderContext(null, null, null, menuRoot);
        for (final MenuComponent<?> component : menuRoot.components()) {
            MenuComponent<?> loaded = component.loader().load(context);
            loadedComponents.put((Class<MenuComponent<?>>) component.getClass(), loaded);
        }
    }

    public @NotNull MenuRoot root() {
        return menuRoot;
    }

    public @NotNull RenderPipeline loop() {
        return renderPipeline;
    }

    public @NotNull Identifier identifier() {
        return identifier;
    }

    public @NotNull InputHandler inputHandler() {
        return inputHandler;
    }

    public @NotNull ActiveMenu add(@NotNull ElementLike<?> @NotNull... elementLikes) {
        renderPipeline.submit(elementLikes);
        return this;
    }

    public @Nullable ElementLike<?> element(@NotNull Identifier identifier) {
        return renderPipeline.element(identifier);
    }

    public @NotNull ActiveMenu remove(@NotNull Identifier identifier) {
        renderPipeline.submitForRemoval(identifier);
        return this;
    }

    public @NotNull ActiveMenu cursor(@NotNull CursorStyle style) {
        PlatformProtocolIntermediate protocolIntermediate = SunscreenLibrary.library().intermediate();
        protocolIntermediate.setHorseArmor(user, style.asset());
        return this;
    }

    public @NotNull SunscreenUser<?> user() {
        return user;
    }

    public void close() {
        SunscreenLibrary.library().sessionHandler().remove(user);
        renderPipeline.stop();
        PlatformProtocolIntermediate intermediate = SunscreenLibrary.library().intermediate();
        intermediate.reset(user, initialRotation);
    }

}
