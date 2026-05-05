package me.combimagnetron.sunscreen.fabric.client.nativeui;

import com.mojang.blaze3d.systems.RenderSystem;
import me.combimagnetron.sunscreen.fabric.protocol.FabricMappingCompat;
import me.combimagnetron.sunscreen.nativeui.ClientRasterTile;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.jetbrains.annotations.NotNull;
import com.mojang.blaze3d.platform.NativeImage;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.List;

/**
 * RGBA8 storage in a direct {@link ByteBuffer}, uploaded into a Minecraft-managed dynamic texture.
 */
public final class DirectGlRgbTexture implements AutoCloseable {
    private final int width;
    private final int height;
    private final ByteBuffer byteBuffer;
    private final IntBuffer intView;
    private final NativeImage image;
    private final DynamicTexture texture;
    private final Object textureId;
    private boolean needsUpload;
    /** Reused row buffer for ARGB→ABGR conversion + {@link IntBuffer#put(int, int[], int, int)} bulk store. */
    private int[] rowScratch = new int[128];

    public DirectGlRgbTexture(int width, int height) {
        this.width = width;
        this.height = height;
        this.byteBuffer = ByteBuffer
                .allocateDirect(width * height * 4)
                .order(ByteOrder.nativeOrder());
        this.intView = byteBuffer.asIntBuffer();
        int n = width * height;
        for (int i = 0; i < n; i++) {
            intView.put(i, 0);
        }
        var l = MemoryUtil.memAddress(this.byteBuffer);
        this.image = new NativeImage(NativeImage.Format.RGBA, width, height, false, l);
        this.image.untrack();
        this.texture = RenderMappingCompat.newDynamicTexture("sunscreen/nativeui", this.image);
        this.textureId = FabricMappingCompat.parse("sunscreen:nativeui");
        RenderMappingCompat.register(this.textureId, this.texture);
        this.needsUpload = true;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    /**
     * Composites the given tiles into the CPU buffer. Tiles not included are left unchanged so the
     * server can send incremental (dirty) updates.
     */
    public void composeTiles(@NotNull List<ClientRasterTile> tiles) {
        for (ClientRasterTile tile : tiles) {
            int cx = Math.round(2.62f - tile.gridPosition().x());
            int cy = Math.round(1.265f - tile.gridPosition().y());
            int baseX = cx * 128;
            int baseY = cy * 128;
            int[] px = tile.argbPixels();
            int tw = tile.width();
            int th = tile.height();
            int expected = tw * th;
            if (px.length < expected) {
                continue;
            }
            int xStart = Math.max(0, baseX);
            int xEnd = Math.min(width, baseX + tw);
            int rowLen = xEnd - xStart;
            if (rowLen <= 0) {
                continue;
            }
            int yStart = Math.max(0, baseY);
            int yEnd = Math.min(height, baseY + th);
            if (yStart >= yEnd) {
                continue;
            }
            ensureRowScratch(rowLen);
            int txStart = xStart - baseX;
            for (int gy = yStart; gy < yEnd; gy++) {
                int ty = gy - baseY;
                int srcOffset = ty * tw + txStart;
                int dstOffset = xStart + gy * width;
                for (int i = 0; i < rowLen; i++) {
                    rowScratch[i] = argbToAbgr(px[srcOffset + i]);
                }
                intView.put(dstOffset, rowScratch, 0, rowLen);
            }
        }
        upload();
    }

    private static int argbToAbgr(int argb) {
        return (argb & 0xFF00FF00) | ((argb & 0xFF) << 16) | ((argb >>> 16) & 0xFF);
    }

    private void ensureRowScratch(int rowLen) {
        if (rowScratch.length < rowLen) {
            rowScratch = new int[rowLen];
        }
    }

    public void upload() {
        if (!RenderSystem.isOnRenderThread()) {
            needsUpload = true;
            return;
        }
        uploadNow();
    }

    private void uploadNow() {
        texture.upload();
        needsUpload = false;
    }

    public void draw(@NotNull GuiGraphics graphics, int ox, int oy, int drawW, int drawH, float uMax, float vMax) {
        RenderSystem.assertOnRenderThread();
        if (needsUpload) uploadNow();
        int srcW = Math.max(1, Math.min(width, Math.round(width * uMax)));
        int srcH = Math.max(1, Math.min(height, Math.round(height * vMax)));
        RenderMappingCompat.blit(
            graphics,
            textureId,
            ox,
            oy,
            0.0f,
            0.0f,
            drawW,
            drawH,
            srcW,
            srcH,
            width,
            height
        );
    }

    @Override
    public void close() {
        // Can't let NativeImage dealloc our GC managed ByteBuffer
        image.pixels = 0;
        RenderMappingCompat.release(textureId);
    }
}
