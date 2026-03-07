package me.combimagnetron.sunscreen.util.helper;

import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

public class FontRendererHelper {
    private final static String characterRange = " _:/ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";

    private FontRendererHelper() {}

    public static @NotNull Char2ObjectMap<Canvas> font(@NotNull Path file, float fontSize) {
        final Char2ObjectMap<Canvas> charToCanvasMap = new Char2ObjectArrayMap<>();
        Font font;
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, file.toFile()).deriveFont(Font.PLAIN, fontSize);
        } catch (FontFormatException | IOException e) {
            throw new RuntimeException(e);
        }
        BufferedImage dummy = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = dummy.createGraphics();
        graphics2D.setFont(font);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        FontMetrics fontMetrics = graphics2D.getFontMetrics();
        int glyphHeight = fontMetrics.getHeight();

        for (char c : characterRange.toCharArray()) {
            int charWidth = fontMetrics.charWidth(c);
            BufferedImage atlas = new BufferedImage(charWidth, glyphHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D letterGraphics2D = atlas.createGraphics();
            letterGraphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            letterGraphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            letterGraphics2D.setFont(font);
            letterGraphics2D.setColor(Color.WHITE);
            letterGraphics2D.drawString(String.valueOf(c), 0, fontMetrics.getAscent());
            letterGraphics2D.dispose();
            charToCanvasMap.put(c, Canvas.image(atlas));
        }
        graphics2D.dispose();
        return charToCanvasMap;
    }
}