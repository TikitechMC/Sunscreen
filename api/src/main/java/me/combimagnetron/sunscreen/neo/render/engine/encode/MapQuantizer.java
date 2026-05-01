package me.combimagnetron.sunscreen.neo.render.engine.encode;

import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.graphic.BufferedColorSpace;
import me.combimagnetron.sunscreen.neo.render.engine.grid.ProcessedRenderChunk;
import org.jetbrains.annotations.NotNull;

final class MapQuantizer {

    private MapQuantizer() {}

    static @NotNull ProcessedRenderChunk quantized(@NotNull ProcessedRenderChunk chunk, int step, int blockSize) {
        BufferedColorSpace source = chunk.bufferedColorSpace();
        Vec2i size = source.size();
        BufferedColorSpace target = new BufferedColorSpace(size);
        int[] input = source.buffer();
        int[] output = target.buffer();

        for (int y = 0; y < size.y(); y += blockSize) {
            for (int x = 0; x < size.x(); x += blockSize) {
                int color = average(input, size.x(), size.y(), x, y, blockSize, step);
                for (int yy = y; yy < Math.min(y + blockSize, size.y()); yy++) {
                    for (int xx = x; xx < Math.min(x + blockSize, size.x()); xx++) {
                        output[xx + yy * size.x()] = color;
                    }
                }
            }
        }
        return new ProcessedRenderChunk(target, chunk.position(), chunk.scale());
    }

    private static int average(int[] pixels, int width, int height, int x, int y, int blockSize, int step) {
        int red = 0;
        int green = 0;
        int blue = 0;
        int count = 0;

        for (int yy = y; yy < Math.min(y + blockSize, height); yy++) {
            for (int xx = x; xx < Math.min(x + blockSize, width); xx++) {
                int color = pixels[xx + yy * width];
                red += (color >>> 16) & 0xFF;
                green += (color >>> 8) & 0xFF;
                blue += color & 0xFF;
                count++;
            }
        }

        red = quantize(red / count, step);
        green = quantize(green / count, step);
        blue = quantize(blue / count, step);
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }

    private static int quantize(int value, int step) {
        return Math.clamp((long) (value / step) * step, 0, 255);
    }
}
