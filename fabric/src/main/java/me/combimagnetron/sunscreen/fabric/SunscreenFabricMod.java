package me.combimagnetron.sunscreen.fabric;

import com.mojang.brigadier.context.CommandContext;
import me.combimagnetron.passport.Passport;
import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.sunscreen.SunscreenLibrary;
import me.combimagnetron.sunscreen.fabric.nativeui.FabricNativeUiNetworking;
import me.combimagnetron.sunscreen.fabric.user.UserImpl;
import me.combimagnetron.sunscreen.neo.ActiveMenu;
import me.combimagnetron.sunscreen.neo.TestMenuTemplate;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.font.AtlasFont;
import me.combimagnetron.sunscreen.neo.registry.Registries;
import me.combimagnetron.sunscreen.util.FileProvider;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class SunscreenFabricMod implements ModInitializer {
    private static final Identifier FONT_ID = Identifier.of("sunscreen", "font/minecraft");
    private static final Identifier SMALL_FONT_ID = Identifier.of("sunscreen", "font/sunburned");
    private static SunscreenLibraryFabric library;

    @Override
    public void onInitialize() {
        library = new SunscreenLibraryFabric(this);
        SunscreenLibrary.Holder.INSTANCE = library;
        Passport.Holder.INSTANCE = library.passport();
        AtlasFont atlasFont = AtlasFont.font(FONT_ID)
            .fromTtfFile(FileProvider.resource().find("minecraft_font.ttf").toPath(), 8);
        AtlasFont sunburned = AtlasFont.font(SMALL_FONT_ID)
            .fromTtfFile(FileProvider.resource().find("sunburned.ttf").toPath(), 15.8f);
        Registries.register(Registries.FONTS, atlasFont);
        Registries.register(Registries.FONTS, sunburned);

        FabricNativeUiNetworking.registerPayloadTypes();

        ServerLifecycleEvents.SERVER_STARTING.register(server -> FabricNativeUiNetworking.registerServerReceivers());

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(
                Commands.literal("sunscreen")
                    .then(Commands.literal("handshake")
                        .executes(SunscreenFabricMod::handshakeStatus))
                    .then(Commands.literal("onlynative")
                        .executes(SunscreenFabricMod::openMenuNativeRequired))
                    .executes(SunscreenFabricMod::openMenuDefault)
            )
        );
        library().logger().info("Sunscreen has started.");
    }

    private static int handshakeStatus(CommandContext<CommandSourceStack> context) {
        if (library == null) {
            context.getSource().sendFailure(Component.literal("Sunscreen: server mod not initialised."));
            return 0;
        }
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(Component.literal("Sunscreen: run this as a player."));
            return 0;
        }
        UserImpl user = library.userManager().ensureUser(player);
        if (!user.useNativeUi()) {
            context.getSource().sendSuccess(
                () -> Component.literal(
                    "Sunscreen: server has no native UI handshake for you yet. "
                        + "With the client mod, join play and watch log Sunscreen/NativeUi."
                ),
                false
            );
            return 1;
        }
        context.getSource().sendSuccess(
            () -> Component.literal(
                "Sunscreen: handshake present — nativeRenderer=true"
            ),
            false
        );
        return 1;
    }

    private static int openMenuDefault(CommandContext<CommandSourceStack> context) {
        return openTestMenu(context, false);
    }

    private static int openMenuNativeRequired(CommandContext<CommandSourceStack> context) {
        return openTestMenu(context, true);
    }

    private static int openTestMenu(CommandContext<CommandSourceStack> context, boolean requireNativeHandshake) {
        if (library == null) {
            context.getSource().sendFailure(Component.literal("Sunscreen: server mod not initialised."));
            return 0;
        }
        try {
            if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
                context.getSource().sendFailure(Component.literal("Sunscreen: this command must be run by a player."));
                return 0;
            }
            UserImpl user = library.userManager().user(player);
            if (user == null) {
                library.logger().error("Sunscreen /sunscreen: no UserImpl for player {}", player.getUUID());
                context.getSource().sendFailure(Component.literal("Sunscreen: internal error (no user record). Rejoin the world."));
                return 0;
            }
            if (requireNativeHandshake) {
                if (!user.useNativeUi()) {
                    context.getSource().sendFailure(Component.literal(
                        "Sunscreen: native UI handshake missing on the server for this player. "
                            + "Use the Sunscreen Fabric client, enter play, then try again or run /sunscreen handshake."
                    ));
                    return 0;
                }
            }
            new ActiveMenu(new TestMenuTemplate(), user, Identifier.of("aa"));
            return 1;
        } catch (Throwable t) {
            library.logger().error("Sunscreen /sunscreen command failed", t);
            String detail = t.getMessage() == null ? "(no message)" : t.getMessage();
            context.getSource().sendFailure(
                Component.literal("Sunscreen: " + t.getClass().getSimpleName() + ": " + detail)
            );
            return 0;
        }
    }

    public static SunscreenLibraryFabric library() {
        return library;
    }
}
