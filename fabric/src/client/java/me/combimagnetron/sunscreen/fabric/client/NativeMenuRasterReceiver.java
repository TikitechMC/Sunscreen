package me.combimagnetron.sunscreen.fabric.client;

import me.combimagnetron.sunscreen.fabric.client.nativeui.ServerRasterMenuScreen;
import me.combimagnetron.sunscreen.nativeui.MenuRasterWireCodec;
import me.combimagnetron.sunscreen.nativeui.NativeUi;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * Dispatches native-UI S2C: single-chunk messages bypass multi-chunk reassembly so they never interrupt a raster stream.
 */
public final class NativeMenuRasterReceiver {
    private static final Logger LOGGER = LoggerFactory.getLogger("Sunscreen/NativeUi");
    private static volatile @Nullable String activeMenuId;
    private static volatile ByteBuffer currentRaster = null;

    private NativeMenuRasterReceiver() {
    }

    public static void onTransportChunk(byte @NotNull [] packetBytes) {
        ByteBuffer logical = NativeUi.receiveClientS2CPacket(packetBytes);
        if (logical == null) {
            return;
        }
        dispatchLogical(logical);
    }

    private static void dispatchLogical(@NotNull ByteBuffer logical) {
        switch (NativeUi.decodeClientS2C(logical)) {
            case NativeUi.S2CLogical.Close() -> Minecraft.getInstance().execute(() -> {
                NativeMenuRasterReceiver.resetAll();
                if (Minecraft.getInstance().screen instanceof ServerRasterMenuScreen) {
                    Minecraft.getInstance().setScreen(null);
                }
            });
            case NativeUi.S2CLogical.Raster(String menuId, ByteBuffer rasterWire) -> onRasterPayload(menuId, rasterWire);
            case NativeUi.S2CLogical.Unknown() -> LOGGER.warn("Unknown native UI S2C logical frame.");
        }
    }

    private static void onRasterPayload(@NotNull String menuId, @NotNull ByteBuffer rasterWire) {
        ensureActiveMenu(menuId);
        currentRaster = rasterWire.slice();
        try {
            var frame = MenuRasterWireCodec.decoder().readFrame(currentRaster);
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof ServerRasterMenuScreen scr && scr.menuId().equals(menuId)) {
                scr.ingestRasterFrame(frame);
            } else {
                mc.setScreen(new ServerRasterMenuScreen(menuId));
                if (mc.screen instanceof ServerRasterMenuScreen scr2) {
                    scr2.ingestRasterFrame(frame);
                }
            }
            currentRaster = null;
        } catch (RuntimeException e) {
            LOGGER.error("Failed to decode native menu raster for {}", menuId, e);
        }
    }

    private static synchronized void ensureActiveMenu(@NotNull String menuId) {
        if (menuId.equals(activeMenuId)) {
            return;
        }
        activeMenuId = menuId;
    }

    public static synchronized void resetAll() {
        activeMenuId = null;
        currentRaster = null;
        NativeUi.resetClientS2CReassembly();
    }
}
