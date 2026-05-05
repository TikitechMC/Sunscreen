package me.combimagnetron.sunscreen.neo;

import me.combimagnetron.passport.internal.entity.metadata.type.Vector3d;
import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.SunscreenLibrary;
import me.combimagnetron.sunscreen.neo.cursor.CursorStyle;
import me.combimagnetron.sunscreen.neo.editor.element.NoticeElement;
import me.combimagnetron.sunscreen.neo.element.ElementLike;
import me.combimagnetron.sunscreen.neo.element.GenericInteractableModernElement;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.graphic.text.Text;
import me.combimagnetron.sunscreen.neo.input.InputHandler;
import me.combimagnetron.sunscreen.neo.input.context.MouseInputContext;
import me.combimagnetron.sunscreen.neo.input.context.ScrollInputContext;
import me.combimagnetron.sunscreen.neo.input.context.TextInputContext;
import me.combimagnetron.sunscreen.neo.layout.Layout;
import me.combimagnetron.sunscreen.neo.loader.MenuComponent;
import me.combimagnetron.sunscreen.neo.loader.MenuComponentLoaderContext;
import me.combimagnetron.sunscreen.neo.property.Position;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.protocol.PlatformProtocolIntermediate;
import me.combimagnetron.sunscreen.neo.protocol.type.Location;
import me.combimagnetron.sunscreen.neo.render.engine.pipeline.RenderPipeline;
import me.combimagnetron.sunscreen.neo.render.engine.pipeline.RenderThreadPoolHandler;
import me.combimagnetron.sunscreen.neo.session.Session;
import me.combimagnetron.sunscreen.neo.theme.ModernTheme;
import me.combimagnetron.sunscreen.user.SunscreenUser;
import me.combimagnetron.sunscreen.util.IdentifierHolder;
import me.combimagnetron.sunscreen.util.helper.ElementSizeSeeder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ActiveMenu implements IdentifierHolder {
    private final Map<Class<MenuComponent<?>>, MenuComponent<?>> loadedComponents = new HashMap<>();
    private final Vector3d initialRotation;
    private final Identifier identifier;
    private final SunscreenUser<?> user;
    /**
     * When true, the client negotiated native UI and we skip spectate, horse, and map packets; the server still runs
     * the render loop but streams rasters instead of map updates.
     */
    private final boolean useNativeClientPath;
    private RenderPipeline renderPipeline;
    private MenuRoot menuRoot = new MenuRoot();
    private InputHandler inputHandler = InputHandler.defaults(this);

    public ActiveMenu(@NotNull MenuTemplate template, @NotNull SunscreenUser<?> user, @NotNull Identifier identifier) {
        this.initialRotation = user.rotation();
        this.user = user;
        this.identifier = identifier;
        this.useNativeClientPath = user.useNativeUi();
        PlatformProtocolIntermediate intermediate = SunscreenLibrary.library().intermediate();
        if (!useNativeClientPath) {
            intermediate.gameTime(user);
        }
        show(template);
        if (!useNativeClientPath) {
            Location location = user.eyeLocation();
            intermediate.spawnAndSpectateDisplay(user, location);
            intermediate.spawnAndRideHorse(user, user.eyeLocation());
        }
        SunscreenLibrary.library().sessionHandler().session(new Session(this, user));
    }

    public void show(@NotNull MenuTemplate template) {
        PlatformProtocolIntermediate intermediate = SunscreenLibrary.library().intermediate();
        if (renderPipeline != null) {
            renderPipeline.stop();
            loadedComponents.clear();
            menuRoot = new MenuRoot();
            inputHandler.close();
            inputHandler = InputHandler.defaults(this);
            inputHandler.cursor(CursorStyle.pointer());
            if (!useNativeClientPath) {
                intermediate.removeMaps(user);
            }
        }
        template.build(menuRoot);
        loadComponents();
        ElementSizeSeeder.seedMenuTree(menuRoot.elementLikes(), loadedTheme());
        for (ElementLike<?> elementLike : menuRoot.elementLikes()) {
            if (elementLike instanceof GenericInteractableModernElement<?, ?, ?> interactableModernElement) {
                interactableModernElement.inputHandler(inputHandler);
            }
            if (elementLike instanceof Layout<?> layout) {
                layout.inputHandler(inputHandler);
            }
        }
        renderPipeline = RenderThreadPoolHandler.start(user, menuRoot, loadedComponents.values());
    }

    private void loadComponents() {
        MenuComponentLoaderContext context = new MenuComponentLoaderContext(null, null, null, menuRoot);
        for (final MenuComponent<?> component : menuRoot.components()) {
            MenuComponent<?> loaded = component.loader().load(context);
            loadedComponents.put((Class<MenuComponent<?>>) component.getClass(), loaded);
        }
    }

    private @Nullable ModernTheme loadedTheme() {
        return loadedComponents.values().stream()
            .filter(ModernTheme.class::isInstance)
            .map(ModernTheme.class::cast)
            .findFirst()
            .orElse(null);
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
        ElementSizeSeeder.seedMenuTree(Arrays.asList(elementLikes), loadedTheme());
        renderPipeline.submit(elementLikes);
        return this;
    }

    public @NotNull ActiveMenu notice(@NotNull Identifier identifier, @NotNull Text text, @NotNull Vec2i position) {
        NoticeElement element = new NoticeElement(identifier, text, this);
        Canvas test = text.render(Size.fixed(Vec2i.of(200, 10)), null).trim();
        add(element.position(Position.fixed(position)).size(Size.fixed(Vec2i.of(test.size().x() + 4, 12))));
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
        if (useNativeClientPath) {
            // TODO(?)
            return this;
        }
        PlatformProtocolIntermediate protocolIntermediate = SunscreenLibrary.library().intermediate();
        protocolIntermediate.setHorseArmor(user, style.asset());
        return this;
    }


    public void openVanillaTextCapture(@NotNull SunscreenUser<?> user) {
        if (useNativeClientPath) {
            return;
        }
        SunscreenLibrary.library().intermediate().openEmptyAnvil(user);
    }

    public @NotNull SunscreenUser<?> user() {
        return user;
    }

    /**
     * When true, cursor position must come from native client packets (e.g. raster UI), not from player look / map bridge.
     */
    public boolean useNativeClientPath() {
        return useNativeClientPath;
    }

    public void acceptNativeClientMouse(int lx, int ly, boolean left, boolean right) {
        if (!useNativeClientPath) {
            return;
        }
        inputHandler.peek(
            MouseInputContext.class,
            old -> new MouseInputContext(old.active(), right, left, Vec2i.of(lx, ly)),
            user
        );
    }

    public void acceptNativeClientScroll(float dy) {
        if (!useNativeClientPath) {
            return;
        }
        inputHandler.peek(
            ScrollInputContext.class,
            old -> new ScrollInputContext(true, dy, old.lastSlot()),
            user
        );
    }

    public void acceptNativeClientTextAppend(@NotNull String chars) {
        if (!useNativeClientPath) {
            return;
        }
        inputHandler.peek(TextInputContext.class, old -> old.append(chars), user);
    }

    public void acceptNativeClientTextBackspace() {
        if (!useNativeClientPath) {
            return;
        }
        inputHandler.peek(TextInputContext.class, TextInputContext::deleteLastCodePoint, user);
    }

    public void close() {
        SunscreenLibrary.library().sessionHandler().remove(user);
        if (renderPipeline != null) {
            renderPipeline.stop();
        }
        loadedComponents.clear();
        inputHandler.close();
        PlatformProtocolIntermediate intermediate = SunscreenLibrary.library().intermediate();
        if (!useNativeClientPath) {
            intermediate.removeMaps(user);
        } else {
            intermediate.serverCloseMenu(user);
        }
        intermediate.reset(user, initialRotation);
    }

}
