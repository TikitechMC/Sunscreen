package me.combimagnetron.sunscreen.neo.file.adapter;

import com.google.common.base.CaseFormat;
import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.file.XmlEncodable;
import me.combimagnetron.sunscreen.neo.graphic.color.Color;
import me.combimagnetron.sunscreen.neo.graphic.text.Text;
import me.combimagnetron.sunscreen.neo.graphic.text.style.Style;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.color.Gradient;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.font.AtlasFont;
import me.combimagnetron.sunscreen.neo.registry.Registries;
import me.combimagnetron.sunscreen.util.helper.XmlHelper;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Locale;

public abstract class PrimitiveAdapter<T> implements XmlEncodable<T> {

    public static final ColorAdapter COLOR_ADAPTER = new ColorAdapter();

    public static final Vec2iAdapter VEC_2I_ADAPTER = new Vec2iAdapter();

    @Override
    public void encode(@NotNull T type, @NotNull Element parent, @NotNull Document document) {
        final String tagName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, type.getClass().getSimpleName());
        encode(type, tagName, parent, document);
    }

    public abstract void encode(@NotNull T type, @NotNull String tagName, @NotNull Element parent, @NotNull Document document);


    public static class ColorAdapter extends PrimitiveAdapter<Color> {

        @Override
        public @NotNull Color decode(@NotNull Element element) {
            String type = element.getAttribute("type");
            switch (type) {
                case "hex-argb" -> {
                    Element child = XmlHelper.firstWithTag(element, "color");
                    if (child == null) return Color.none();
                    return Color.argbHex(child.getTextContent());
                }
                case "hex-rgb" -> {
                    Element child = XmlHelper.firstWithTag(element, "color");
                    if (child == null) return Color.none();
                    return Color.rgbHex(child.getTextContent());
                }
                case "rgb" -> {
                    Element red = XmlHelper.firstWithTag(element, "red");
                    Element green = XmlHelper.firstWithTag(element, "green");
                    Element blue = XmlHelper.firstWithTag(element, "blue");
                    if (red == null || green == null || blue == null) return Color.none();
                    return Color.of(Integer.parseInt(red.getTextContent()), Integer.parseInt(green.getTextContent()), Integer.parseInt(blue.getTextContent()));
                }
                case "argb" -> {
                    Element red = XmlHelper.firstWithTag(element, "red");
                    Element green = XmlHelper.firstWithTag(element, "green");
                    Element blue = XmlHelper.firstWithTag(element, "blue");
                    Element alpha = XmlHelper.firstWithTag(element, "alpha");
                    if (red == null || green == null || blue == null || alpha == null) return Color.none();
                    return Color.of(Integer.parseInt(red.getTextContent()), Integer.parseInt(green.getTextContent()), Integer.parseInt(blue.getTextContent()), Integer.parseInt(alpha.getTextContent()));
                }
                default -> {
                    return Color.none();
                }
            }
        }

        @Override
        public void encode(@NotNull Color type, @NotNull String name, @NotNull Element parent, @NotNull Document document) {
            boolean shouldAddAlpha = type.alpha() < 255;
            Element color = document.createElement("color");
            parent.setAttribute("type", shouldAddAlpha ? "argb" : "rgb");
            Element red = document.createElement("red");
            Element green = document.createElement("green");
            Element blue = document.createElement("blue");
            red.setTextContent(String.valueOf(type.red()));
            green.setTextContent(String.valueOf(type.green()));
            blue.setTextContent(String.valueOf(type.blue()));
            color.appendChild(red);
            color.appendChild(green);
            color.appendChild(blue);
            parent.appendChild(color);
            if (!shouldAddAlpha) return;
            Element alpha = document.createElement("alpha");
            alpha.setTextContent(String.valueOf(type.alpha()));
            color.appendChild(alpha);
        }

    }

    public static class Vec2iAdapter extends PrimitiveAdapter<Vec2i> {

        @Override
        public @NotNull Vec2i decode(@NotNull Element element) {
            Element x = XmlHelper.firstWithTag(element, "x");
            Element y = XmlHelper.firstWithTag(element, "y");
            if (x == null || y == null) return Vec2i.zero();
            return Vec2i.of(Integer.parseInt(x.getTextContent()), Integer.parseInt(y.getTextContent()));
        }

        @Override
        public void encode(@NotNull Vec2i type, @NotNull String string, @NotNull Element parent, @NotNull Document document) {
            Element vec = document.createElement(string);
            PropertyAdapter.addVec2i(type, vec, document);
            parent.appendChild(vec);
        }

    }

    public static class TextAdapter extends PrimitiveAdapter<Text> {

        @Override
        public @NotNull Text decode(@NotNull Element element) {
            return null;
        }

        @Override
        public void encode(@NotNull Text type, @NotNull String string, @NotNull Element parent, @NotNull Document document) {
            Element root = document.createElement(string);

        }

        interface StyleAdapter<T extends Style<?>> extends XmlEncodable<T> {

            AtlasFontStyleAdapter ATLAS_FONT_STYLE_ADAPTER = new AtlasFontStyleAdapter();
            GradientStyleAdapter GRADIENT_STYLE_ADAPTER = new GradientStyleAdapter();

            StyleAdapter<?>[] ADAPTERS = new StyleAdapter[]{};

            class AtlasFontStyleAdapter implements StyleAdapter<AtlasFont> {

                @Override
                public @Nullable AtlasFont decode(@NotNull Element element) {
                    String string = element.getTextContent();
                    return Registries.fonts().get(Identifier.split(string));
                }

                @Override
                public void encode(@NotNull AtlasFont type, @NotNull Element parent, @NotNull Document document) {
                    Identifier identifier = type.identifier();
                    Element child = document.createElement("font");
                    child.setTextContent(identifier.string());
                    parent.appendChild(child);
                }

            }

            class GradientStyleAdapter implements StyleAdapter<Gradient> {

                @Override
                public @NotNull Gradient decode(@NotNull Element element) {
                    Element fromElement = XmlHelper.firstWithTag(element, "from");
                    Element toElement = XmlHelper.firstWithTag(element, "to");
                    Element sizeElement = XmlHelper.firstWithTag(element, "size");
                    if (fromElement == null || toElement == null) return Gradient.conical(Color.of(0), Color.of(0));
                    Color from = PrimitiveAdapter.COLOR_ADAPTER.decode(fromElement);
                    Color to = PrimitiveAdapter.COLOR_ADAPTER.decode(toElement);
                    switch (element.getAttribute("type")) {
                        case "linear" -> {
                            return Gradient.linear(from, to);
                        }
                        case "conical" -> {
                            return Gradient.conical(from, to);
                        }
                        case "radial" -> {
                            return Gradient.radial(from, to);
                        }
                        case "elliptical" -> {
                            return Gradient.elliptical(from, to);
                        }
                        default -> {
                            return Gradient.linear(Color.none(), Color.none());
                        }
                    }
                }

                @Override
                public void encode(@NotNull Gradient type, @NotNull Element parent, @NotNull Document document) {
                    Element element = document.createElement("gradient");
                    COLOR_ADAPTER.encode((Color)type.start(), "from", element, document);
                    COLOR_ADAPTER.encode((Color)type.end(), "to", element, document);
                    VEC_2I_ADAPTER.encode(type.size(), element, document);
                    element.setAttribute("type", type.getClass().getSimpleName().toLowerCase(Locale.ROOT).replace("gradient", ""));
                }

            }

        }

    }

}
