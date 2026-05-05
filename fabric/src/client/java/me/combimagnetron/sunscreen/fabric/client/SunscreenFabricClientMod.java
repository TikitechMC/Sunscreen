package me.combimagnetron.sunscreen.fabric.client;

import me.combimagnetron.sunscreen.fabric.nativeui.FabricNativeUiNetworking;
import net.fabricmc.api.ClientModInitializer;

public final class SunscreenFabricClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricNativeUiNetworking.registerPayloadTypes();
        FabricNativeUiClientNetworking.register();
    }
}
