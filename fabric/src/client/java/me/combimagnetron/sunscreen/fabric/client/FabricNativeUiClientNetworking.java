package me.combimagnetron.sunscreen.fabric.client;

import me.combimagnetron.sunscreen.SunscreenLibrary;
import me.combimagnetron.sunscreen.fabric.nativeui.FabricNativeUiNetworking;
import me.combimagnetron.sunscreen.fabric.nativeui.NativeUiChunkS2CPayload;
import me.combimagnetron.sunscreen.fabric.protocol.FabricPlatformProtocolIntermediate;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client-side receiver for the unified native-UI chunk channel, C2S sink binding, and join handshake.
 */
public final class FabricNativeUiClientNetworking {
    private static final Logger LOGGER = LoggerFactory.getLogger("Sunscreen/NativeUi");

    private FabricNativeUiClientNetworking() {
    }

    public static void register() {
        FabricNativeUiNetworking.registerPayloadTypes();

        FabricPlatformProtocolIntermediate.setSender(ClientPlayNetworking::send);

        ClientPlayNetworking.registerGlobalReceiver(
            NativeUiChunkS2CPayload.TYPE,
            (payload, context) ->
                NativeMenuRasterReceiver.onTransportChunk(
                    payload.data()
                )
        );

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> NativeMenuRasterReceiver.resetAll());

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            NativeMenuRasterReceiver.resetAll();
            var lib = SunscreenLibrary.library();
            if (lib == null) {
                LOGGER.warn("SunscreenLibraryFabric not initialised; native UI C2S will not send until common mod loads.");
                return;
            }
            lib.intermediate().clientHandshake(true);
            LOGGER.debug("Sent Sunscreen native UI support announce (one-way).");
        });
    }
}
