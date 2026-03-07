package me.combimagnetron.sunscreen.neo.graphic;

import me.combimagnetron.passport.util.math.Vec2f;
import me.combimagnetron.sunscreen.neo.graphic.color.Color;
import me.combimagnetron.sunscreen.neo.graphic.color.ColorLike;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.util.helper.ColorHelper;
import org.jetbrains.annotations.NotNull;

public final class BufferedColorSpace {
    private final int[] buffer;
    private final Vec2i size;

    public BufferedColorSpace(@NotNull Vec2i size) {
        this.buffer = new int[size.x() * size.y()];
        this.size = size;
    }

    private BufferedColorSpace(@NotNull Vec2i size, int @NotNull [] data) {
        this.buffer = data;
        this.size = size;
    }

    public @NotNull ColorLike at(@NotNull Vec2i pixel) {
        return Color.of(buffer[pixelIndex(pixel, size)]);
    }

    public int at(int x, int y) {
        return buffer[pixelIndex(x, y, size.x())];
    }

    public @NotNull BufferedColorSpace sub(int x, int y, int width, int height) {
        int[] tempBuffer = new int[width * height];
        if (x + width > size.x() || y + height > size.y()) throw new IllegalArgumentException("Sub colorspace may not exceed parent size");
        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                tempBuffer[pixelIndex(w, h, width)] = buffer[pixelIndex(x + w, y + h, size.x())];
            }
        }
        return new BufferedColorSpace(Vec2i.of(width, height), tempBuffer);
    }

    public @NotNull BufferedColorSpace sub(@NotNull Vec2i position, @NotNull Vec2i size) {
        return sub(position.x(), position.y(), size.x(), size.y());
    }

    public @NotNull BufferedColorSpace scale(float multiplier) {
        Vec2i scaledSize = Vec2i.of((int) (size.x() * multiplier), (int) (size.y() * multiplier));
        int[] tempBuffer = new int[scaledSize.x() * scaledSize.y()];
        float xRatio = (float) size.x() / scaledSize.x();
        float yRatio = (float) size.y() / scaledSize.y();
        for (int y = 0; y < scaledSize.y(); y++) {
            for (int x = 0; x < scaledSize.x(); x++) {
                int sourceX = (int) Math.min(Math.floor(x * xRatio), size.x());
                int sourceY = (int) Math.min(Math.floor(y * xRatio), size.y());
                tempBuffer[pixelIndex(sourceX, sourceY, scaledSize.x())] = at(sourceX, sourceY);
            }
        }
        return new BufferedColorSpace(scaledSize, tempBuffer);
    }

    public void pixels(int[] pixels, int x, int y, int width, int height) {
        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                if (x + w >= 0 && x + w < size().x() && y + h >= 0 && y + h < size.y()) {
                    this.colorDirectAnalog(pixelIndex(x + w, y + h, size.x()), pixels[pixelIndex(w, h, width)]);
                }
            }
        }
    }

    public void replace(@NotNull Color target, @NotNull Color replacement) {
        int targetColor = target.rgba();
        int replacementColor = replacement.rgba();
        for (int i = 0; i < buffer.length; i++) {
            int color = buffer[i];
            if (color != targetColor) continue;
            buffer[i] = replacementColor;
        }
    }

    public void fill(@NotNull Vec2i start, @NotNull Vec2i end, @NotNull ColorLike colorLike) {
        int width = end.x() - start.x();
        int height = end.y() - start.y();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (start.x() + width > size.x() || start.y() + height > size.y()) return;
                color(x + start.x(), y + start.y(), colorLike);
            }
        }
    }

    public void place(@NotNull BufferedColorSpace bufferedColorSpace, @NotNull Vec2i position) {
        place(bufferedColorSpace, position.x(), position.y());
    }

    public void place(@NotNull BufferedColorSpace bufferedColorSpace, int x, int y) {
        final Vec2i size = bufferedColorSpace.size();
        pixels(bufferedColorSpace.buffer(), x, y, size.x(), size.y());
    }

    public void erase(int x, int y) {
        buffer[pixelIndex(x, y, size.x())] = 0;
    }

    public void erase(@NotNull Vec2i pixel) {
        erase(pixel.x(), pixel.y());
    }

    public void colorDirect(int index, @NotNull ColorLike colorLike) {
        colorDirectAnalog(index, colorLike.rgba());
    }

    public void colorDirectAnalog(int index, int color) {
        int alpha = (color >> 24) & 0xFF;
        if (alpha == 0) return;
        if (alpha == 255) {
            buffer[index] = color;
            return;
        }
        final int currentColor = buffer[index];
        buffer[index] = ColorHelper.mix(currentColor, color);
    }

    public void colorDirectInteger(int x, int y, int color) {
        colorDirectAnalog(pixelIndex(x, y, size().x()), color);
    }

    public void color(int x, int y, @NotNull ColorLike colorLike) {
        colorDirect(pixelIndex(x, y, size.x()), colorLike);
    }

    public void color(@NotNull Vec2i pixel, @NotNull ColorLike colorLike) {
        color(pixel.x(), pixel.y(), colorLike);
    }

    public static int pixelIndex(Vec2i pixel, Vec2i size) {
        return pixelIndex(pixel.x(), pixel.y(), size.x());
    }

    private static int pixelIndex(int x, int y, int width) {
        return x + y * width;
    }

    public int[] buffer() {
        return buffer;
    }

    public Vec2i size() {
        return size;
    }
}
