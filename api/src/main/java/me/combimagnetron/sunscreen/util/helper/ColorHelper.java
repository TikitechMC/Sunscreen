package me.combimagnetron.sunscreen.util.helper;

import me.combimagnetron.sunscreen.neo.graphic.color.Color;
import me.combimagnetron.sunscreen.neo.graphic.color.ColorLike;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;

public class ColorHelper {

    private ColorHelper() {}

    public static @NotNull ColorLike mix(@NotNull ColorLike... colors) {
        int[] intColors = new int[colors.length];
        for (int i = 0; i < colors.length; i++) {
            intColors[i] = colors[i].rgba();
        }
        return Color.rgba(mix(intColors));
    }
    public static int mix(int... colors) {
        float ratio = 1f / (colors.length);
        int red = 0, green = 0, blue = 0, alpha = 0;
        for (int color : colors) {
            int addedAlpha = (color >> 24 & 0xff);
            int addedRed = ((color & 0xff0000) >> 16);
            int addedGreen = ((color & 0xff00) >> 8);
            int addedBlue = (color & 0xff);
            red += (int) (addedRed * ratio);
            blue += (int) (addedBlue * ratio);
            green += (int) (addedGreen * ratio);
            alpha += (int) (addedAlpha * ratio);
        }
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    public static int @NotNull [] extractColorData(@NotNull BufferedImage bufferedImage) {
        int[] rgb = new int[bufferedImage.getWidth() * bufferedImage.getHeight()];
        bufferedImage.getRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), rgb, 0, bufferedImage.getWidth());
        return rgb;
    }

}
