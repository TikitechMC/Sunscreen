package me.combimagnetron.sunscreen.neo.file.adapter;

import me.combimagnetron.passport.internal.registry.Registry;
import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.passport.util.math.Vec4i;
import me.combimagnetron.sunscreen.neo.editor.virtual.argument.ElementConstructionProvider;
import me.combimagnetron.sunscreen.neo.file.PackedMenu;
import me.combimagnetron.sunscreen.neo.file.XmlEncodable;
import me.combimagnetron.sunscreen.neo.property.*;
import me.combimagnetron.sunscreen.neo.registry.Registries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.util.*;

public interface PropertyAdapter<P extends Property<?, P>> extends XmlEncodable<P> {

    SizePropertyAdapter SIZE_PROPERTY_ADAPTER = new SizePropertyAdapter();

    PositionPropertyAdapter POSITION_PROPERTY_ADAPTER = new PositionPropertyAdapter();

    MarginPropertyAdapter MARGIN_PROPERTY_ADAPTER = new MarginPropertyAdapter();

    PaddingPropertyAdapter PADDING_PROPERTY_ADAPTER = new PaddingPropertyAdapter();

    ScalePropertyAdapter SCALE_PROPERTY_ADAPTER = new ScalePropertyAdapter();

    Class<P> type();

    PropertyAdapter<?>[] ADAPTERS = new PropertyAdapter[]{SIZE_PROPERTY_ADAPTER, POSITION_PROPERTY_ADAPTER, MARGIN_PROPERTY_ADAPTER, PADDING_PROPERTY_ADAPTER, SCALE_PROPERTY_ADAPTER};

    static <P extends Property<?, P>> @NotNull PropertyAdapter<P> find(@NotNull Class<? extends Property<?, ?>> type) {
        return (PropertyAdapter<P>) Arrays.stream(ADAPTERS).filter(adapter -> adapter.type() == type).findFirst().orElseThrow();
    }

    static <P extends Property<?, P>> @NotNull PropertyAdapter<P> find(@NotNull String type) {
        return (PropertyAdapter<P>) Arrays.stream(ADAPTERS).filter(adapter -> adapter.getClass().getSimpleName().toLowerCase().contains(type)).findFirst().orElseThrow();
    }

    static void main(@NotNull String[] args) throws TransformerException, IOException, SAXException {
//        Document document = PackedMenu.builder.newDocument();
//        Element test = document.createElement("elements");
//        ButtonElement buttonElement = Elements.button(Identifier.of("test"));
//        Position position = Position.relative(RelativeMeasure.vec2i().x().pixel(50).percentage(20).back().y().pixel(-6).back());
//        Scale scale = Scale.relative(RelativeMeasure.single().set().pixel(60).percentage(6).back());
//        Margin margin = Margin.relative(RelativeMeasure.vec4i().up().pixel(50).back().down().percentage(6).back().left().percentage(-9).pixel(24).back().right().pixel(2).back());
//        Size size = Size.fixed(Vec2i.of(100, 100));//Size.relative(RelativeMeasure.vec2i().x().pixel(200).percentage(5).pixel(-6).back().y().percentage(5).back());
//        buttonElement.size(size).margin(margin).scale(scale).position(position);
//        ElementAdapter.BUTTON_ELEMENT_ADAPTER.encode(buttonElement, test, document);
//        document.appendChild(test);
//        TransformerFactory transformerFactory = TransformerFactory.newInstance();
//        Transformer transformer = transformerFactory.newTransformer();
//        StringWriter stringWriter = new StringWriter();
//        transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
//        System.out.println(stringWriter);

        Document document = PackedMenu.builder.parse(new StringBufferInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><properties>\n" +
            "\t\t\t<margin complex=\"true\" type=\"vec4\">\n" +
            "\t\t\t\t<right>\n" +
            "\t\t\t\t\t<pixel>2</pixel>\n" +
            "\t\t\t\t</right>\n" +
            "\t\t\t\t<left>\n" +
            "\t\t\t\t\t<percentage>-9.0</percentage>\n" +
            "\t\t\t\t\t<pixel>24</pixel>\n" +
            "\t\t\t\t</left>\n" +
            "\t\t\t\t<up>\n" +
            "\t\t\t\t\t<pixel>50</pixel>\n" +
            "\t\t\t\t</up>\n" +
            "\t\t\t\t<down>\n" +
            "\t\t\t\t\t<percentage>6.0</percentage>\n" +
            "\t\t\t\t</down>\n" +
            "\t\t\t</margin>\n" +
            "\t\t\t<position complex=\"true\" type=\"vec2\">\n" +
            "\t\t\t\t<x>\n" +
            "\t\t\t\t\t<pixel>50</pixel>\n" +
            "\t\t\t\t\t<percentage>20.0</percentage>\n" +
            "\t\t\t\t</x>\n" +
            "\t\t\t\t<y>\n" +
            "\t\t\t\t\t<pixel>-6</pixel>\n" +
            "\t\t\t\t</y>\n" +
            "\t\t\t</position>\n" +
            "\t\t\t<scale complex=\"true\" type=\"single\">\n" +
            "\t\t\t\t<double>\n" +
            "\t\t\t\t\t<pixel>60</pixel>\n" +
            "\t\t\t\t\t<percentage>6.0</percentage>\n" +
            "\t\t\t\t</double>\n" +
            "\t\t\t</scale>\n" +
            "\t\t\t<size complex=\"false\">\n" +
            "\t\t\t\t<x>100</x>\n" +
            "\t\t\t\t<y>100</y>\n" +
            "\t\t\t</size>\n" +
            "\t\t</properties>"));
        Element properties = (Element) document.getElementsByTagName("properties").item(0);
        System.out.println(properties.getChildNodes().getLength());
        NodeList children = properties.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node property = children.item(i);
            if (!(property instanceof Element element)) continue;
            Property<?, ?> prop = find(element.getTagName()).decode(element);
            System.out.println(prop);
        }
    }

    class SizePropertyAdapter implements PropertyAdapter<Size> {

        @Override
        public @NotNull Size decode(@NotNull Element element) {
            boolean isComplex = isComplex(element);
            Size size;
            if (isComplex) {
                size = Size.relative((RelativeMeasure.Vec2iRelativeMeasureGroup<?>) handleComplex(element));
            } else {
                size = null;
            }
            return size;
        }

        @Override
        public void encode(@NotNull Size type, @NotNull Element parent, @NotNull Document document) {
            RelativeMeasure.RelativeMeasureGroup<?> complex = complex(type);
            boolean isComplex = complex.value() == null;
            Element element = create(type, document, isComplex);
            if (isComplex) {
                handleComplex(complex, element, document);
            } else {
                handleDefault(complex, element, document);
            }
            parent.appendChild(element);
        }

        @Override
        public Class<Size> type() {
            return Size.class;
        }

    }

    class PositionPropertyAdapter implements PropertyAdapter<Position> {

        @Override
        public @NotNull Position decode(@NotNull Element element) {
            boolean isComplex = isComplex(element);
            Position position;
            if (isComplex) {
                position = Position.relative((RelativeMeasure.Vec2iRelativeMeasureGroup<?>) handleComplex(element));
            } else {
                position = null;
            }
            return position;
        }

        @Override
        public void encode(@NotNull Position type, @NotNull Element parent, @NotNull Document document) {
            RelativeMeasure.RelativeMeasureGroup<?> complex = complex(type);
            boolean isComplex = complex.value() == null;
            Element element = create(type, document, isComplex);
            if (isComplex) {
                handleComplex(complex, element, document);
            } else {
                handleDefault(complex, element, document);
            }
            parent.appendChild(element);
        }

        @Override
        public Class<Position> type() {
            return Position.class;
        }

    }

    class MarginPropertyAdapter implements PropertyAdapter<Margin> {

        @Override
        public @NotNull Margin decode(@NotNull Element element) {
            boolean isComplex = isComplex(element);
            Margin margin;
            if (isComplex) {
                margin = Margin.relative((RelativeMeasure.Vec4iRelativeMeasureGroup<?>) handleComplex(element));
            } else {
                margin = null;
            }
            return margin;
        }

        @Override
        public void encode(@NotNull Margin type, @NotNull Element parent, @NotNull Document document) {
            RelativeMeasure.RelativeMeasureGroup<?> complex = complex(type);
            boolean isComplex = complex.value() == null;
            Element element = create(type, document, isComplex);
            if (isComplex) {
                handleComplex(complex, element, document);
            } else {
                handleDefault(complex, element, document);
            }
            parent.appendChild(element);
        }

        @Override
        public Class<Margin> type() {
            return Margin.class;
        }

    }

    class PaddingPropertyAdapter implements PropertyAdapter<Padding> {

        @Override
        public @NotNull Padding decode(@NotNull Element element) {
            boolean isComplex = isComplex(element);
            Padding padding;
            if (isComplex) {
                padding = Padding.relative((RelativeMeasure.Vec4iRelativeMeasureGroup<?>) handleComplex(element));
            } else {
                padding = null;
            }
            return padding;
        }

        @Override
        public void encode(@NotNull Padding type, @NotNull Element parent, @NotNull Document document) {
            RelativeMeasure.RelativeMeasureGroup<?> complex = complex(type);
            boolean isComplex = complex.value() == null;
            Element element = create(type, document, isComplex);
            if (isComplex) {
                handleComplex(complex, element, document);
            } else {
                handleDefault(complex, element, document);
            }
            parent.appendChild(element);
        }

        @Override
        public Class<Padding> type() {
            return Padding.class;
        }


    }

    class ScalePropertyAdapter implements PropertyAdapter<Scale> {

        @Override
        public @NotNull Scale decode(@NotNull Element element) {
            boolean isComplex = isComplex(element);
            Scale scale;
            if (isComplex) {
                scale = Scale.relative((RelativeMeasure.DoubleRelativeMeasureGroup<?>) handleComplex(element));
            } else {
                scale = null;
            }
            return scale;
        }

        @Override
        public void encode(@NotNull Scale type, @NotNull Element parent, @NotNull Document document) {
            RelativeMeasure.RelativeMeasureGroup<?> complex = complex(type);
            boolean isComplex = complex.value() == null;
            Element element = create(type, document, isComplex);
            if (isComplex) {
                handleComplex(complex, element, document);
            } else {
                handleDefault(complex, element, document);
            }
            parent.appendChild(element);
        }

        @Override
        public Class<Scale> type() {
            return Scale.class;
        }


    }

    static void handleDefault(@NotNull RelativeMeasure.RelativeMeasureGroup<?> relativeMeasureGroup, @NotNull Element element, @NotNull Document document) {
        switch (relativeMeasureGroup.value()) {
            case Vec2i value -> addVec2i(value, element, document);
            case Vec4i value -> addVec4i(value, element, document);
            case Double value -> addDouble(value, element, document);
            default -> {}
        }
    }

    static @Nullable RelativeMeasure.RelativeMeasureGroup<?> handleComplex(@NotNull Element element) {
        switch (element.getAttribute("type")) {
            case "single" -> {
                RelativeMeasure.DoubleRelativeMeasureGroup<?> builder = RelativeMeasure.single();
                for (int i = 0; i < element.getChildNodes().getLength(); i++) {
                    Node node = element.getChildNodes().item(i);
                    if (!(node instanceof Element inner)) continue;
                    String axisName = inner.getTagName();
                    List<RelativeMeasure.OffsetType> offsetTypes = offsets(inner.getChildNodes());
                    for (RelativeMeasure.OffsetType offsetType : offsetTypes) {
                        builder.set().offset(offsetType);
                    }
                }
                return builder;
            }
            case "vec2" -> {
                RelativeMeasure.Vec2iRelativeMeasureGroup builder = RelativeMeasure.vec2i();
                for (int i = 0; i < element.getChildNodes().getLength(); i++) {
                    Node node = element.getChildNodes().item(i);
                    if (!(node instanceof Element inner)) continue;
                    String axisName = inner.getTagName();
                    RelativeMeasure.RelativeBuilder<RelativeMeasure.Vec2iRelativeMeasureGroup<?>> builder1 = new RelativeMeasure.RelativeBuilder<>(builder);
                    RelativeMeasure.Axis2d axis = RelativeMeasure.Axis2d.valueOf(axisName.toUpperCase(Locale.ROOT));
                    List<RelativeMeasure.OffsetType> offsetTypes = offsets(inner.getChildNodes());
                    for (RelativeMeasure.OffsetType offsetType : offsetTypes) {
                        builder1.offset(offsetType);
                    }
                    builder.axisBuilderMap().put(axis, builder1);
                }
                return builder;
            }
            case "vec4" -> {
                RelativeMeasure.Vec4iRelativeMeasureGroup builder = RelativeMeasure.vec4i();
                for (int i = 0; i < element.getChildNodes().getLength(); i++) {
                    Node node = element.getChildNodes().item(i);
                    if (!(node instanceof Element inner)) continue;
                    String axisName = inner.getTagName();
                    RelativeMeasure.RelativeBuilder<RelativeMeasure.Vec4iRelativeMeasureGroup<?>> builder1 = new RelativeMeasure.RelativeBuilder<>(builder);
                    RelativeMeasure.Axis4d axis = RelativeMeasure.Axis4d.valueOf(axisName.toUpperCase(Locale.ROOT));
                    List<RelativeMeasure.OffsetType> offsetTypes = offsets(inner.getChildNodes());
                    for (RelativeMeasure.OffsetType offsetType : offsetTypes) {
                        builder1.offset(offsetType);
                    }
                    builder.axisBuilderMap().put(axis, builder1);
                }
                return builder;
            }
            default -> {
                return null;
            }
        }
    }

    static @NotNull List<RelativeMeasure.OffsetType> offsets(@NotNull NodeList children) {
        List<RelativeMeasure.OffsetType> offsetTypes = new ArrayList<>();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (!(node instanceof Element element)) continue;
            switch (element.getTagName()) {
                case "percentage" -> offsetTypes.add(RelativeMeasure.OffsetType.percentage(Double.parseDouble(element.getTextContent())));
                case "pixel" -> offsetTypes.add(RelativeMeasure.OffsetType.pixel(Integer.parseInt(element.getTextContent())));
                default -> {}
            }
        }
        return offsetTypes;
    }

    static void handleComplex(@NotNull RelativeMeasure.RelativeMeasureGroup<?> relativeMeasureGroup, @NotNull Element element, @NotNull Document document) {
        switch (relativeMeasureGroup) {
            case RelativeMeasure.Vec2iRelativeMeasureGroup<?> group -> group.axisBuilderMap().forEach((axis, relative) -> {
                element.setAttribute("type", "vec2");
                Element inner = axisBased(axis.toString().toLowerCase(), relative.offsetTypes(), document);
                element.appendChild(inner);
            });
            case RelativeMeasure.Vec4iRelativeMeasureGroup<?> group -> group.axisBuilderMap().forEach((axis, relative) -> {
                element.setAttribute("type", "vec4");
                Element inner = axisBased(axis.toString().toLowerCase(), relative.offsetTypes(), document);
                element.appendChild(inner);
            });
            case RelativeMeasure.DoubleRelativeMeasureGroup<?> group -> {
                element.setAttribute("type", "single");
                Element inner = axisBased("double", group.set().offsetTypes(), document);
                element.appendChild(inner);
            }
            default -> {}
        }
    }

    static @NotNull Element axisBased(@NotNull String label, @NotNull Collection<RelativeMeasure.OffsetType> offsetTypes, @NotNull Document document) {
        Element inner = document.createElement(label);
        for (RelativeMeasure.OffsetType offsetType : offsetTypes) {
            switch (offsetType) {
                case RelativeMeasure.OffsetType.PercentageOffsetType(Double value) -> {
                    Element offset = document.createElement("percentage");
                    offset.setTextContent(String.valueOf(value));
                    inner.appendChild(offset);
                }
                case RelativeMeasure.OffsetType.PixelOffsetType(int value) -> {
                    Element offset = document.createElement("pixel");
                    offset.setTextContent(String.valueOf(value));
                    inner.appendChild(offset);
                }
            }
        }
        return inner;
    }

    static void addVec2i(@NotNull Vec2i vec2i, @NotNull Element element, @NotNull Document document) {
        Element x = document.createElement("x");
        Element y = document.createElement("y");
        x.setTextContent(String.valueOf(vec2i.x()));
        y.setTextContent(String.valueOf(vec2i.y()));
        element.appendChild(x);
        element.appendChild(y);
    }

    static void addVec4i(@NotNull Vec4i vec4i, @NotNull Element element, @NotNull Document document) {
        Element up = document.createElement("up");
        Element down = document.createElement("down");
        Element left = document.createElement("left");
        Element right = document.createElement("right");
        up.setTextContent(String.valueOf(vec4i.first()));
        down.setTextContent(String.valueOf(vec4i.second()));
        left.setTextContent(String.valueOf(vec4i.third()));
        right.setTextContent(String.valueOf(vec4i.fourth()));
        element.appendChild(up);
        element.appendChild(down);
        element.appendChild(left);
        element.appendChild(right);
    }

    static void addDouble(@NotNull Double d, @NotNull Element element, @NotNull Document document) {
        Element value = document.createElement("double");
        value.setTextContent(String.valueOf(d));
        element.appendChild(value);
    }

    static <P extends Property<?, P>> @NotNull Element create(@NotNull P property, @NotNull Document document, boolean isComplex) {
        Element element = document.createElement(low(property.getClass().getSimpleName()));
        element.setAttribute("complex", String.valueOf(isComplex));
        return element;
    }

    static boolean isComplex(@NotNull Element element) {
        return Boolean.parseBoolean(element.getAttribute("complex"));
    }

    static <P extends Property<?, P>> RelativeMeasure.RelativeMeasureGroup<?> complex(@NotNull P property) {
        if (!(property instanceof RelativeMeasure.RelativeMeasureGroup<?> relativeMeasure)) return null;
        return relativeMeasure;
    }

    //root for weird language stuff with lowercase
    static @NotNull String low(@NotNull String string) {
        return string.toLowerCase(Locale.ROOT);
    }

    static void defaults() {
        Registry<PropertyAdapter<?>, String> registry = Registries.advanced().propertyAdapters();
        for (PropertyAdapter<?> adapter : ADAPTERS) {
            registry.register(adapter.getClass().getSimpleName().toLowerCase().replace("adapter", ""), adapter);
        }
    }

}
