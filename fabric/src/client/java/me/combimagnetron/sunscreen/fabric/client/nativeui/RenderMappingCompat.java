package me.combimagnetron.sunscreen.fabric.client.nativeui;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
#if MC_1_21_1
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.systems.RenderSystem;
#elif MC_1_21_11
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.RenderPipelines;
import java.util.function.Supplier;
#endif
import net.minecraft.client.renderer.texture.DynamicTexture;


public final class RenderMappingCompat {

    private RenderMappingCompat() {
    }

    public static void blit(
        GuiGraphics graphics,
        Object textureId,
        int ox,
        int oy,
        float u,
        float v,
        int drawW,
        int drawH,
        int srcW,
        int srcH,
        int texW,
        int texH
    ) {
        #if MC_1_21_1
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(
            (ResourceLocation) textureId,
            ox,
            oy,
            drawW,
            drawH,
            u,
            v,
            srcW,
            srcH,
            texW,
            texH
        );
        RenderSystem.disableBlend();
        #elif MC_1_21_11
        graphics.blit(
             RenderPipelines.GUI_TEXTURED,
            (Identifier) textureId,
            ox,
            oy,
            u,
            v,
            drawW,
            drawH,
            srcW,
            srcH,
            texW,
            texH
        );
        #endif
    }

    public static DynamicTexture newDynamicTexture(String prefix, NativeImage image) {
        #if MC_1_21_1
        return new DynamicTexture(image);
        #elif MC_1_21_11
        return new DynamicTexture((Supplier<String>) () -> prefix, image);
        #else
        throw new IllegalStateException("Unsupported minecraft version");
        #endif
    }

    public static void register(Object key, DynamicTexture texture) {
        var manager = Minecraft.getInstance().getTextureManager();
        #if MC_1_21_1
        manager.register((ResourceLocation) key, texture);
        #elif MC_1_21_11
        manager.register((Identifier) key, texture);
        #endif
    }

    public static void release(Object key) {
        var manager = Minecraft.getInstance().getTextureManager();
        #if MC_1_21_1
        manager.release((ResourceLocation) key);
        #elif MC_1_21_11
        manager.release((Identifier) key);
        #endif
    }
}
