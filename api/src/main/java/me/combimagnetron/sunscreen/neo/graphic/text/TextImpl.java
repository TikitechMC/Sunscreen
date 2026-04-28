package me.combimagnetron.sunscreen.neo.graphic.text;

import me.combimagnetron.passport.logic.state.State;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.graphic.BufferedColorSpace;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.graphic.color.Color;
import me.combimagnetron.sunscreen.neo.graphic.text.style.Style;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.color.Highlight;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.color.TextColor;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.decoration.Decoration;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.font.Font;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.font.FontProperties;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.render.engine.context.RenderContext;
import me.combimagnetron.sunscreen.util.helper.PropertyHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class TextImpl implements Text {
    private final String content;
    private final Font font;
    private final FontProperties fontProperties;
    private final TextColor textColor;
    private final Highlight highlight;
    private final List<Text> children;
    private final Decoration decoration;

    private TextImpl(String content, Font font, FontProperties fontProperties, TextColor textColor, Highlight highlight,
            List<Text> children, Decoration decoration) {
        this.content = content;
        this.font = font;
        this.fontProperties = fontProperties;
        this.textColor = textColor;
        this.highlight = highlight;
        this.children = children;
        this.decoration = decoration;
    }

    public static @NotNull TextImpl basic(@NotNull String content) {
        return new TextImpl(content, null, FontProperties.properties(), TextColor.color(Color.of(255, 255, 255)),
                Highlight.highlight(Color.none()), new ArrayList<>(), null);
    }

    public static @NotNull TextImpl chained(@NotNull Text @NotNull... texts) {
        TextImpl root = basic("");
        for (Text text : texts) {
            root = (TextImpl) root.append(text);
        }
        return root;
    }

    public static @NotNull TextImpl fromComponent(@NotNull Component component) {
        String plainText = PlainTextComponentSerializer.plainText().serialize(component);
        return basic(plainText);
    }

    public static @NotNull TextImpl state(@NotNull State<String> state) {
        return basic(state.value());
    }

    @Override
    public @NotNull <S extends Style<?>> Text style(@NotNull S style) {
        if (style instanceof FontProperties props) {
            return new TextImpl(content, font, props, textColor, highlight, children, decoration);
        }
        if (style instanceof TextColor color) {
            return new TextImpl(content, font, fontProperties, color, highlight, children, decoration);
        }
        if (style instanceof Font f) {
            return new TextImpl(content, f, fontProperties, textColor, highlight, children, decoration);
        }
        if (style instanceof Highlight h) {
            return new TextImpl(content, font, fontProperties, textColor, h, children, decoration);
        }
        if (style instanceof Decoration d) {
            return new TextImpl(content, font, fontProperties, textColor, highlight, children, d);
        }
        return this;
    }

    @Override
    public @NotNull FontProperties fontProperties() {
        return fontProperties;
    }

    @Override
    public @NotNull TextColor color() {
        return textColor;
    }

    @Override
    public @NotNull Font font() {
        return font;
    }

    @Override
    public @NotNull Highlight highlight() {
        return highlight;
    }

    @Override
    public @NotNull Decoration decoration() {
        return decoration;
    }

    @Override
    public @NotNull Text content(@NotNull String string) {
        return new TextImpl(string, font, fontProperties, textColor, highlight, children, decoration);
    }

    @Override
    public @NotNull Text content(@NotNull Component component) {
        String plainText = PlainTextComponentSerializer.plainText().serialize(component);
        return content(plainText);
    }

    @Override
    public @NotNull Text append(@NotNull Text text) {
        List<Text> newChildren = new ArrayList<>(children);
        newChildren.add(text);
        return new TextImpl(content, font, fontProperties, textColor, highlight, newChildren, decoration);
    }

    @Override
    public @NotNull Canvas render(@NotNull Size property, @Nullable RenderContext context) {
        Vec2i size = PropertyHelper.vectorOrThrow(property, Vec2i.class);
        Canvas canvas = Canvas.empty(size);
        int xOffset = renderContent(canvas);

        for (Text child : children) {
            Canvas childCanvas = child.render(Size.fixed(Vec2i.of(size.x() - xOffset, size.y())), context);
            canvas.place(childCanvas, Vec2i.of(xOffset, 0));
            xOffset += childCanvas.size().x();
        }

        return canvas;
    }

    private int renderContent(@NotNull Canvas canvas) {
        if (font == null || content.isEmpty()) {
            return 0;
        }
        int xOffset = 0;
        float shear = fontProperties.shear();
        float tracking = fontProperties.tracking();
        float kerning = fontProperties.kerning();

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            Canvas charCanvas = font.character(c);

            if (charCanvas == null) {
                xOffset += (int) tracking;
                continue;
            }

            BufferedColorSpace charSpace = charCanvas.bufferedColorSpace();
            BufferedColorSpace tintedSpace = applyColorTint(charSpace);
            BufferedColorSpace shearedSpace = shear != 0 ? applyShear(tintedSpace, shear) : tintedSpace;
            canvas.place(new Canvas(shearedSpace), Vec2i.of(xOffset, (int) fontProperties.baseline()));
            xOffset += charCanvas.size().x() + (int) tracking;
            if (i < content.length() - 1) {
                xOffset += (int) kerning;
            }
        }

        return xOffset;
    }

    private @NotNull BufferedColorSpace applyColorTint(@NotNull BufferedColorSpace source) {
        Vec2i size = source.size();
        BufferedColorSpace result = new BufferedColorSpace(size);
        for (int y = 0; y < size.y(); y++) {
            for (int x = 0; x < size.x(); x++) {
                int color = source.at(x, y);
                if (color == 0) continue;
                result.colorDirect(x + y * size.x(), textColor);
            }
        }
        return result;
    }

    private @NotNull BufferedColorSpace applyShear(@NotNull BufferedColorSpace source, float shear) {
        Vec2i sourceSize = source.size();
        int extraWidth = (int) Math.ceil(Math.abs(shear) * sourceSize.y());
        Vec2i newSize = Vec2i.of(sourceSize.x() + extraWidth, sourceSize.y());
        BufferedColorSpace result = new BufferedColorSpace(newSize);

        for (int y = 0; y < sourceSize.y(); y++) {
            int shearOffset = (int) (shear * (sourceSize.y() - y - 1));
            for (int x = 0; x < sourceSize.x(); x++) {
                int pixel = source.at(x, y);
                if (((pixel >> 24) & 0xFF) == 0) continue;
                int newX = x + shearOffset;
                if (newX >= 0 && newX < newSize.x()) {
                    result.colorDirectAnalog(newX + y * newSize.x(), pixel);
                }
            }
        }

        return result;
    }

    public String contentString() {
        return content;
    }

    public List<Text> children() {
        return children;
    }

}
