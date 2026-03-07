package me.combimagnetron.sunscreen.neo.graphic;

import me.combimagnetron.sunscreen.neo.graphic.color.Color;
import me.combimagnetron.sunscreen.neo.graphic.color.ColorLike;
import me.combimagnetron.sunscreen.neo.graphic.modifier.GraphicModifier;
import me.combimagnetron.sunscreen.neo.graphic.shape.Shape;
import me.combimagnetron.sunscreen.neo.graphic.text.Text;
import me.combimagnetron.sunscreen.neo.property.Position;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.util.FileProvider;
import me.combimagnetron.sunscreen.util.helper.ColorHelper;
import me.combimagnetron.sunscreen.util.helper.PropertyHelper;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.BitSet;

public record Canvas(BufferedColorSpace bufferedColorSpace) implements GraphicLike<Canvas> {

    public Canvas(Vec2i size) {
        this(new BufferedColorSpace(size));
    }

    public static @NotNull Canvas image(@NotNull BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        Canvas canvas = new Canvas(Vec2i.of(width, height));
        canvas.bufferedColorSpace().pixels(ColorHelper.extractColorData(image), 0, 0, width, height);
        return canvas;
    }

    public static @NotNull Canvas error(@NotNull Size size) {
        Vec2i sizeVec = PropertyHelper.vectorOrThrow(size, Vec2i.class);
        return Canvas.empty(sizeVec);
    }

    public static @NotNull Canvas url(@NotNull String url) {
        try {
            URI uri = new URI(url);
            return image(ImageIO.read(uri.toURL()));
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static @NotNull Canvas data(@NotNull InputStream stream) {
        try {
            return image(ImageIO.read(stream));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static @NotNull Canvas file(@NotNull Path path) {
        if (!path.toFile().exists()) throw new IllegalStateException("File at gives path is null.");
        try {
            return image(ImageIO.read(path.toFile()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static @NotNull Canvas resource(@NotNull String string) {
        return file(FileProvider.resource().find(string).toPath());
    }

    public static @NotNull Canvas empty(@NotNull Vec2i size) {
        return new Canvas(size);
    }

    @Override
    public @NotNull <M> Canvas modifier(@NotNull GraphicModifier<M> modifier) {
        modifier.handler().apply(bufferedColorSpace, modifier.modifier(), modifier.context());
        return this;
    }

    @Override
    public @NotNull BufferedColorSpace bufferedColorSpace() {
        return bufferedColorSpace;
    }

    public @NotNull Canvas place(@NotNull Canvas canvas, @NotNull Vec2i position) {
        return place(canvas.bufferedColorSpace(), position);
    }

    public @NotNull Canvas place(@NotNull BufferedColorSpace colorSpace, @NotNull Vec2i position) {
        this.bufferedColorSpace.place(colorSpace, position);
        return this;
    }

    public @NotNull Vec2i size() {
        return bufferedColorSpace.size();
    }


    public @NotNull Canvas shape(@NotNull Shape shape, @NotNull Color color) {
        return shape(shape, color, Vec2i.zero());
    }

    public @NotNull Canvas shape(@NotNull Shape shape, @NotNull Color color, @NotNull Vec2i position) {
        Vec2i squareSize = shape.squareSize();
        BitSet bits = shape.shape();

        for (int i = bits.nextSetBit(0); i >= 0; i = bits.nextSetBit(i + 1)) {
            int x = i % squareSize.x() + position.x();
            int y = i / squareSize.x() + position.y();
            bufferedColorSpace.color(Vec2i.of(x, y), color);
        }

        return this;
    }

    public @NotNull Canvas shape(@NotNull Shape shape) {
        return shape(shape, Color.of(255, 255, 255));
    }

    public @NotNull Canvas fill(@NotNull Position position, @NotNull Size size, @NotNull ColorLike colorLike) {
        Vec2i sizeVec = PropertyHelper.vectorOrThrow(size, Vec2i.class);
        Vec2i start = PropertyHelper.vectorOrThrow(position, Vec2i.class);
        return fill(start, sizeVec, colorLike);
    }

    public @NotNull Canvas fill(@NotNull Vec2i start, @NotNull Vec2i size, @NotNull ColorLike colorLike) {
        Vec2i end = start.add(size);
        bufferedColorSpace.fill(start, end, colorLike);
        return this;
    }

    public @NotNull Canvas color(@NotNull Vec2i position, @NotNull ColorLike colorLike) {
        bufferedColorSpace.color(position, colorLike);
        return this;
    }

    public @NotNull Canvas erase(@NotNull Vec2i position) {
        bufferedColorSpace.erase(position);
        return this;
    }

    public @NotNull Canvas scale(float scale) {
        return new Canvas(bufferedColorSpace.scale(scale));
    }

    public @NotNull Canvas sub(@NotNull Vec2i position, @NotNull Vec2i size) {
        return new Canvas(bufferedColorSpace.sub(position.x(), position.y(), size.x(), size.y()));
    }

    public @NotNull Canvas text(@NotNull Text text, @NotNull Vec2i position) {
        Canvas place = text.render(Size.fixed(size()), null);
        return place(place, position);
    }

}
