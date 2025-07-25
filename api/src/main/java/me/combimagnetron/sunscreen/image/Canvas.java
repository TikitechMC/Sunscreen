package me.combimagnetron.sunscreen.image;

import me.combimagnetron.sunscreen.SunscreenLibrary;
import me.combimagnetron.sunscreen.image.effect.Effect;
import me.combimagnetron.sunscreen.menu.Size;
import me.combimagnetron.sunscreen.style.Text;
import me.combimagnetron.sunscreen.util.Vec2d;
import me.combimagnetron.sunscreen.util.Vec2i;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;

public interface Canvas {

    Canvas sub(Vec2i size, Vec2i coords);

    Canvas pixel(Pixel pixel);

    Canvas place(Canvas canvas, Vec2i coords);

    Canvas fill(Vec2i coords, Vec2i size, me.combimagnetron.sunscreen.image.Color color);

    Canvas trim();

    Canvas scale(Vec2d scale);

    Canvas text(Text text, Vec2i coords, Color color);

    Canvas text(Text text, Vec2i coords);

    Vec2i size();

    Pixel pixel(Vec2i coords);

    boolean animated();

    default Canvas effect(Effect effect) {
        return effect.apply(this);
    }

    static Canvas image(BufferedImage image) {
        return new StaticImpl(image);
    }

    static Canvas image(Size size) {
        if (size == null) {
            return new StaticImpl(new BufferedImage(256, 256, 2));
        }
        return new StaticImpl(new BufferedImage((int) size.x().pixel(), (int) size.y().pixel(), 2));
    }

    static Canvas image(Vec2i size) {
        return new StaticImpl(new BufferedImage(size.x(), size.y(), 2));
    }

    static Canvas image(ImageProvider imageProvider) {
        return image(imageProvider.image());
    }

    interface InternalCanvas extends Canvas {
        BufferedImage image();
    }

    class StaticImpl implements InternalCanvas {
        private final BufferedImage image;

        protected StaticImpl(BufferedImage image) {
            this.image = image;
        }

        @Override
        public Canvas sub(Vec2i size, Vec2i coords) {
            return new StaticImpl(image.getSubimage(coords.x(), coords.y(), size.x(), size.y()));
        }

        public BufferedImage image() {
            return image;
        }

        @Override
        public Canvas pixel(Pixel pixel) {
            image.setRGB(pixel.x(), pixel.y(), pixel.color().rgb());
            return new StaticImpl(image);
        }

        @Override
        public Canvas place(Canvas canvas, Vec2i coords) {
            BufferedImage place = ((InternalCanvas) canvas).image();
            Graphics2D graphics = image.createGraphics();
            graphics.drawImage(place, coords.x(), coords.y(), null);
            graphics.dispose();
            return new StaticImpl(image);
        }

        @Override
        public Canvas fill(Vec2i coords, Vec2i size, me.combimagnetron.sunscreen.image.Color color) {
            Graphics2D graphics = image.createGraphics();
            graphics.setColor(new java.awt.Color(color.rgb()));
            graphics.fillRect(coords.x(), coords.y(), size.x(), size.y());
            graphics.dispose();
            return new StaticImpl(image);
        }

        @Override
        public Canvas trim() {
            return Canvas.image(Canvas.trim(image));
        }

        @Override
        public Canvas scale(Vec2d scale) {
            return Canvas.image((BufferedImage) image.getScaledInstance(1, 1, Image.SCALE_DEFAULT));
        }

        @Override
        public Canvas text(Text text, Vec2i coords, Color color) {
            Graphics2D graphics = image.createGraphics();
            graphics.setColor(new java.awt.Color(color.rgb()));
            graphics.setFont(text.font().internal());
            graphics.drawString(text.content(), coords.x(), coords.y());
            graphics.dispose();
            return this;
        }

        @Override
        public Canvas text(Text text, Vec2i coords) {
            return text(text, coords, Color.white());
        }

        @Override
        public Vec2i size() {
            return Vec2i.of(image.getWidth(), image.getHeight());
        }

        @Override
        public Pixel pixel(Vec2i coords) {
            return Pixel.of(coords, me.combimagnetron.sunscreen.image.Color.of(image.getRGB(coords.x(), coords.y())));
        }

        @Override
        public boolean animated() {
            return false;
        }
    }

    class IntArrayImpl implements InternalCanvas {
        private final int[][] pixels;

        protected IntArrayImpl(Vec2i size) {
            pixels = new int[size.x()][size.y()];
        }

        @Override
        public BufferedImage image() {
            return null;
        }

        @Override
        public Canvas sub(Vec2i size, Vec2i coords) {
            return null;
        }

        @Override
        public Canvas pixel(Pixel pixel) {
            return null;
        }

        @Override
        public Canvas place(Canvas canvas, Vec2i coords) {
            return null;
        }

        @Override
        public Canvas fill(Vec2i coords, Vec2i size, Color color) {
            return null;
        }

        @Override
        public Canvas trim() {
            return null;
        }

        @Override
        public Canvas scale(Vec2d scale) {
            return null;
        }

        @Override
        public Canvas text(Text text, Vec2i coords, Color color) {
            return null;
        }

        @Override
        public Canvas text(Text text, Vec2i coords) {
            return null;
        }

        @Override
        public Vec2i size() {
            return null;
        }

        @Override
        public Pixel pixel(Vec2i coords) {
            return null;
        }

        @Override
        public boolean animated() {
            return false;
        }
    }

    interface ImageProvider {

        BufferedImage image();

        static FileImageProvider file(Path path) {
            return new FileImageProvider(path);
        }

        static UrlImageProvider url(String url) {
            return new UrlImageProvider(url);
        }

        static ResourcesImageProvider resources(String path) {
            return new ResourcesImageProvider(path);
        }

        record UrlImageProvider(String url) implements ImageProvider {

            @Override
            public BufferedImage image() {
                try {
                    return ImageIO.read(URI.create(url).toURL());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        record FileImageProvider(Path path) implements ImageProvider {

            @Override
            public BufferedImage image() {
                try {
                    return ImageIO.read(SunscreenLibrary.library().path().resolve(path).toFile());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        record ResourcesImageProvider(String path) implements ImageProvider {

            @Override
            public BufferedImage image() {
                try {
                    return ImageIO.read(SunscreenLibrary.library().resource(path));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

    static BufferedImage trim(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int top, bottom, left, right;
        boolean emptyRow;
        searchTop:
        for (top = 0; top < height; top++) {
            for (int x = 0; x < width; x++) {
                if (isNonEmptyPixel(image, x, top)) break searchTop;
            }
        }
        searchBottom:
        for (bottom = height - 1; bottom > top; bottom--) {
            for (int x = 0; x < width; x++) {
                if (isNonEmptyPixel(image, x, bottom)) break searchBottom;
            }
        }
        searchLeft:
        for (left = 0; left < width; left++) {
            for (int y = top; y <= bottom; y++) {
                if (isNonEmptyPixel(image, left, y)) break searchLeft;
            }
        }
        searchRight:
        for (right = width - 1; right > left; right--) {
            for (int y = top; y <= bottom; y++) {
                if (isNonEmptyPixel(image, right, y)) break searchRight;
            }
        }
        return image.getSubimage(left, top, (right - left + 1), (bottom - top + 1));
    }

    private static boolean isNonEmptyPixel(BufferedImage image, int x, int y) {
        int pixel = image.getRGB(x, y);
        java.awt.Color color = new java.awt.Color(pixel, true);
        return color.getAlpha() != 0;
    }

}
