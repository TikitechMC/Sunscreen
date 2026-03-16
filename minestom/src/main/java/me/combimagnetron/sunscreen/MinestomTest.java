package me.combimagnetron.sunscreen;

import me.combimagnetron.passport.event.EventBus;
import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.ActiveMenu;
import me.combimagnetron.sunscreen.neo.TestMenuTemplate;
import me.combimagnetron.sunscreen.neo.element.Elements;
import me.combimagnetron.sunscreen.neo.event.UserMoveStateChangeEvent;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.graphic.color.Color;
import me.combimagnetron.sunscreen.neo.graphic.text.Text;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.color.TextColor;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.font.AtlasFont;
import me.combimagnetron.sunscreen.neo.property.Position;
import me.combimagnetron.sunscreen.neo.registry.Registries;
import me.combimagnetron.sunscreen.neo.render.engine.pipeline.RenderPipeline;
import me.combimagnetron.sunscreen.user.SunscreenUser;
import me.combimagnetron.sunscreen.util.FileProvider;
import me.combimagnetron.sunscreen.util.Scheduler;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerSwapItemEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import okhttp3.*;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MinestomTest {
    private static final Identifier FONT_ID = Identifier.of("sunscreen", "font/minecraft");
    private static final Identifier SMALL_FONT_ID = Identifier.of("sunscreen", "font/sunburned");

    static void main(String[] args) {
        MinecraftServer minecraftServer = MinecraftServer.init(new Auth.Online());

        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();

        instanceContainer.setChunkSupplier(LightingChunk::new);
        instanceContainer.setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.GRASS_BLOCK));

        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(instanceContainer);
            player.setRespawnPoint(new Pos(0, 42, 0));
        });
        new SunscreenLibraryMinestom();
        new PacketListener();
        AtlasFont atlasFont = AtlasFont.font(FONT_ID).fromTtfFile(FileProvider.resource().find("minecraft_font.ttf").toPath(), 8);
        AtlasFont sunburned = AtlasFont.font(SMALL_FONT_ID).fromTtfFile(FileProvider.resource().find("sunburned.ttf").toPath(), 15.8f);
        Registries.register(Registries.FONTS, atlasFont);
        Registries.register(Registries.FONTS, sunburned);
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
            .url("http://localhost:8080/request-code")
            .build();

        String code;
        try (Response response = client.newCall(request).execute()) {
            code = response.body().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Request socketRequest = new Request.Builder()
            .url("ws://localhost:8080/session/" + code)
            .build();

        AtomicInteger id = new AtomicInteger(0);
        final WebSocket socket = client.newWebSocket(socketRequest, new WebSocketListener() {

            @Override
            public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                System.out.println("Closing: " + reason);
            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable throwable, Response response) {
                throwable.printStackTrace();
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
                ActiveMenu menu = SunscreenLibrary.library().users().user("Combimagnetron").get().session().menu();
                menu.remove(Identifier.of("preview-" + id.getAndIncrement()));
                BufferedImage image;
                try {
                    image = ImageIO.read(new ByteArrayInputStream(bytes.toByteArray()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                menu.add(Elements.image(Identifier.of("preview-"+id), Canvas.image(image)).position(Position.fixed(Vec2i.of(75, 100))));
                System.out.println("Received binary: " + bytes.size() + " bytes");
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                System.out.println(text);
            }

        });

        AtomicBoolean clicked = new AtomicBoolean(false);
        MinecraftServer.getGlobalEventHandler().addListener(PlayerSwapItemEvent.class, event -> {
            final Player player = event.getPlayer();
            SunscreenUser<?> user = SunscreenLibrary.library().users().user(player);
            ActiveMenu menu = new ActiveMenu(new TestMenuTemplate(), user, Identifier.of("hi"));
            menu.add(
                Elements.label(Identifier.of("code"), Text.basic(code.replace("/", " ")).font(Registries.fonts().get(FONT_ID)).color(TextColor.color(Color.of(255, 255, 255)))).position(Position.fixed(Vec2i.of(350, 200))));
            EventBus.subscribe(UserMoveStateChangeEvent.class, stateChangeEvent -> {
                if (!stateChangeEvent.context().leftPressed()) return;
                if (clicked.get()) return;
                System.out.println("leftclick!!!!");
                try {
                    socket.send(ByteString.of(Files.readAllBytes(Path.of("test.png"))));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                clicked.set(true);
            });
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> socket.send("close")));

        System.out.println(code);

        minecraftServer.start("0.0.0.0", 25565);
        System.out.println("hi");

    }

}
