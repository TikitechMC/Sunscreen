package me.combimagnetron.sunscreen.fabric.nativeui;

import me.combimagnetron.sunscreen.fabric.protocol.FabricMappingCompat;
import me.combimagnetron.sunscreen.fabric.user.UserImpl;
import me.combimagnetron.sunscreen.nativeui.NativeUi;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Registers the single native-UI chunk payload (both directions) and the C2S receiver.
 */
public final class FabricNativeUiNetworking {
    private static final Logger LOGGER = LoggerFactory.getLogger("Sunscreen/NativeUi");
    private static final AtomicBoolean PAYLOAD_TYPES_REGISTERED = new AtomicBoolean(false);
    private static final AtomicBoolean SERVER_RECEIVERS_REGISTERED = new AtomicBoolean(false);

    private FabricNativeUiNetworking() {
    }

    public static void registerPayloadTypes() {
        if (!PAYLOAD_TYPES_REGISTERED.compareAndSet(false, true)) {
            return;
        }
        PayloadTypeRegistry.playC2S().register(NativeUiChunkC2SPayload.TYPE, NativeUiChunkC2SPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(NativeUiChunkS2CPayload.TYPE, NativeUiChunkS2CPayload.STREAM_CODEC);
    }

    public static void registerServerReceivers() {
        if (!SERVER_RECEIVERS_REGISTERED.compareAndSet(false, true)) {
            return;
        }
        ServerPlayNetworking.registerGlobalReceiver(NativeUiChunkC2SPayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayer player = context.player();
                UserImpl user = UserImpl.tryOf(player);
                if (user == null) {
                    LOGGER.warn(
                        "Native UI chunk ignored for {}: user not ready.",
                        FabricMappingCompat.gameProfileName(player.getGameProfile())
                    );
                    return;
                }
                NativeUi.receiveServerC2SPacket(user, payload.data());
            });
        });
    }
}
