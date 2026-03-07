package me.combimagnetron.sunscreen.neo.file;

import com.google.common.base.CaseFormat;
import me.combimagnetron.sunscreen.neo.MenuRoot;
import me.combimagnetron.sunscreen.neo.RootLike;
import me.combimagnetron.sunscreen.neo.element.ElementContainer;
import me.combimagnetron.sunscreen.neo.element.ElementLike;
import me.combimagnetron.sunscreen.neo.loader.MenuComponent;
import me.combimagnetron.sunscreen.neo.page.Page;
import me.combimagnetron.sunscreen.neo.property.*;
import me.combimagnetron.sunscreen.neo.theme.ModernTheme;
import me.combimagnetron.sunscreen.util.IdentifierHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public final class XmlEncoder {
    private static final String XML_VERSION = "1.0";
    private static final String XML_ENCODING = "UTF-16";

    private final MenuRoot root;
    private final MenuMetadata metadata;
    private final List<PageEntry> pages = new LinkedList<>();
    private final List<AssetReference> assets = new LinkedList<>();

    private XmlEncoder(@NotNull MenuRoot root, @NotNull MenuMetadata metadata) {
        this.root = root;
        this.metadata = metadata;
    }

    public static @NotNull XmlEncoder encoder(@NotNull MenuRoot root, @NotNull MenuMetadata metadata) {
        return new XmlEncoder(root, metadata);
    }

    public @NotNull XmlEncoder page(@NotNull Page page, @NotNull String filePath, boolean isRoot) {
        pages.add(new PageEntry(page, filePath, isRoot));
        return this;
    }

    public @NotNull XmlEncoder asset(@NotNull AssetReference asset) {
        assets.add(asset);
        return this;
    }

    public void encode(@NotNull Path outputDir) throws XmlEncoderException {
        try {
            Files.createDirectories(outputDir);
            String indexXml = encodeIndex();
            Files.writeString(outputDir.resolve("index.xml"), indexXml, StandardCharsets.UTF_16);

            for (PageEntry entry : pages) {
                String pageXml = encodePage(entry.page());
                Path pagePath = outputDir.resolve(entry.filePath());
                Files.createDirectories(pagePath.getParent());
                Files.writeString(pagePath, pageXml, StandardCharsets.UTF_16);
            }
        } catch (IOException | ParserConfigurationException | TransformerException e) {
            throw new XmlEncoderException("Failed to encode menu", e);
        }
    }

    public @NotNull String encodeIndex() throws ParserConfigurationException, TransformerException {
        Document doc = createDocument();
        Element indexEl = doc.createElement("index");
        doc.appendChild(indexEl);

        // Metadata section
        Element metadataEl = doc.createElement("metadata");
        indexEl.appendChild(metadataEl);

        appendTextElement(doc, metadataEl, "identifier", metadata.identifier().toString());
        appendTextElement(doc, metadataEl, "author", metadata.author());

        Element versionEl = doc.createElement("version");
        metadataEl.appendChild(versionEl);
        appendTextElement(doc, versionEl, "min", metadata.minVersion());
        appendTextElement(doc, versionEl, "max", metadata.maxVersion() != null ? metadata.maxVersion() : "null");
        appendTextElement(doc, versionEl, "original", metadata.originalVersion());

        if (!assets.isEmpty()) {
            Element assetsEl = doc.createElement("assets");
            indexEl.appendChild(assetsEl);

            for (AssetReference asset : assets) {
                Element assetEl = doc.createElement("asset");
                assetEl.setAttribute("type", asset.type());
                appendTextElement(doc, assetEl, "id", asset.id().toString());
                appendTextElement(doc, assetEl, "file", asset.file());
                assetsEl.appendChild(assetEl);
            }
        }

        Element pagesEl = doc.createElement("pages");
        indexEl.appendChild(pagesEl);
        for (PageEntry entry : pages) {
            Element pageEl = doc.createElement("page");
            if (entry.isRoot()) {
                pageEl.setAttribute("root", "true");
            }
            appendTextElement(doc, pageEl, "id", entry.page().identifier().toString());
            appendTextElement(doc, pageEl, "file", entry.filePath());
            pagesEl.appendChild(pageEl);
        }

        return documentToString(doc);
    }

    public @NotNull String encodePage(@NotNull Page page) throws ParserConfigurationException, TransformerException {
        Document doc = createDocument();
        Element pageEl = doc.createElement("page");
        doc.appendChild(pageEl);

        appendTextElement(doc, pageEl, "id", page.identifier().toString());
        ModernTheme theme = findTheme(page.components());
        if (theme != null) {
            appendTextElement(doc, pageEl, "theme", theme.identifier().toString());
        }

        List<MenuComponent<?>> otherMenuComponents = page.components().stream()
                .filter(c -> !(c instanceof ModernTheme))
                .toList();
        if (!otherMenuComponents.isEmpty()) {
            Element componentsEl = doc.createElement("components");
            pageEl.appendChild(componentsEl);

            for (MenuComponent<?> menuComponent : otherMenuComponents) {
                encodeComponent(doc, componentsEl, menuComponent);
            }
        }
        for (ElementLike<?> element : page.elementLikes()) {
            encodeElement(doc, pageEl, element);
        }

        return documentToString(doc);
    }

    public @NotNull String encodeRootLike(@NotNull RootLike<?> rootLike)
            throws ParserConfigurationException, TransformerException {
        Document doc = createDocument();
        Element rootEl = doc.createElement("root");
        doc.appendChild(rootEl);

        if (rootLike instanceof IdentifierHolder holder) {
            appendTextElement(doc, rootEl, "id", holder.identifier().toString());
        }

        ModernTheme theme = findTheme(rootLike.components());
        if (theme != null) {
            appendTextElement(doc, rootEl, "theme", theme.identifier().toString());
        }

        List<MenuComponent<?>> otherMenuComponents = rootLike.components().stream()
                .filter(c -> !(c instanceof ModernTheme))
                .toList();
        if (!otherMenuComponents.isEmpty()) {
            Element componentsEl = doc.createElement("components");
            rootEl.appendChild(componentsEl);

            for (MenuComponent<?> menuComponent : otherMenuComponents) {
                encodeComponent(doc, componentsEl, menuComponent);
            }
        }

        for (ElementLike<?> element : rootLike.elementLikes()) {
            encodeElement(doc, rootEl, element);
        }

        return documentToString(doc);
    }

    private void encodeElement(@NotNull Document doc, @NotNull Element parent, @NotNull ElementLike<?> element) {
        Element elementEl = doc.createElement("element");
        parent.appendChild(elementEl);

        String type = deriveElementType(element);
        elementEl.setAttribute("type", type);

        if (element instanceof ElementContainer<?> container) {
            if (!container.children().isEmpty()) {
                elementEl.setAttribute("children", "true");
            }
        }

        if (element.identifier() != null) {
            appendTextElement(doc, elementEl, "id", element.identifier().toString());
        }

        Collection<Property<?, ?>> properties = element.properties();
        if (!properties.isEmpty()) {
            Element propsEl = doc.createElement("properties");
            elementEl.appendChild(propsEl);

            for (Property<?, ?> property : properties) {
                encodeProperty(doc, propsEl, property);
            }
        }

        if (element instanceof ElementContainer<?> container) {
            Element childrenEl = doc.createElement("children");
            elementEl.appendChild(childrenEl);

            for (ElementLike<?> child : container.children()) {
                encodeElement(doc, childrenEl, child);
            }
        }
    }

    private void encodeProperty(@NotNull Document doc, @NotNull Element parent, @NotNull Property<?, ?> property) {
        Element propEl = doc.createElement("property");
        parent.appendChild(propEl);

        String type = derivePropertyType(property);
        propEl.setAttribute("type", type);

        switch (property) {
            case Size size -> encodeSizeProperty(doc, propEl, size);
            case Padding padding -> encodePaddingProperty(doc, propEl, padding);
            case Margin margin -> encodeMarginProperty(doc, propEl, margin);
            case Position position -> encodePositionProperty(doc, propEl, position);
            case Scale scale -> encodeScaleProperty(doc, propEl, scale);
            default -> {}
        }
    }

    private void encodeSizeProperty(@NotNull Document doc, @NotNull Element parent, @NotNull Size size) {
        if (size instanceof FitToContent<?>) {
            appendTextElement(doc, parent, "fit", "true");
            return;
        }

        Element xEl = doc.createElement("x");
        parent.appendChild(xEl);
        encodeVec2iAxis(doc, xEl, size.x());

        Element yEl = doc.createElement("y");
        parent.appendChild(yEl);
        encodeVec2iAxis(doc, yEl, size.y());
    }

    private void encodePaddingProperty(@NotNull Document doc, @NotNull Element parent, @NotNull Padding padding) {
        if (padding instanceof FitToContent<?>) {
            appendTextElement(doc, parent, "fit", "true");
            return;
        }

        encodeVec4iProperty(doc, parent, padding);
    }

    private void encodeMarginProperty(@NotNull Document doc, @NotNull Element parent, @NotNull Margin margin) {
        if (margin instanceof FitToContent<?>) {
            appendTextElement(doc, parent, "fit", "true");
            return;
        }

        encodeVec4iProperty(doc, parent, margin);
    }

    private void encodePositionProperty(@NotNull Document doc, @NotNull Element parent, @NotNull Position position) {
        Element xEl = doc.createElement("x");
        parent.appendChild(xEl);
        encodeVec2iAxis(doc, xEl, position.x());

        Element yEl = doc.createElement("y");
        parent.appendChild(yEl);
        encodeVec2iAxis(doc, yEl, position.y());
    }

    private void encodeScaleProperty(@NotNull Document doc, @NotNull Element parent, @NotNull Scale scale) {
        appendTextElement(doc, parent, "value", String.valueOf(scale.value()));
    }

    private void encodeVec4iProperty(@NotNull Document doc, @NotNull Element parent,
            @NotNull RelativeMeasure.Vec4iRelativeMeasureGroup<?> group) {
        Element upEl = doc.createElement("up");
        parent.appendChild(upEl);
        encodeVec4iAxis(doc, upEl, group.up());

        Element downEl = doc.createElement("down");
        parent.appendChild(downEl);
        encodeVec4iAxis(doc, downEl, group.down());

        Element leftEl = doc.createElement("left");
        parent.appendChild(leftEl);
        encodeVec4iAxis(doc, leftEl, group.left());

        Element rightEl = doc.createElement("right");
        parent.appendChild(rightEl);
        encodeVec4iAxis(doc, rightEl, group.right());
    }

    //TODO simplify all of the relative stuff, this cannot pass.
    private void encodeVec2iAxis(@NotNull Document doc, @NotNull Element parent,
            @NotNull RelativeMeasure.RelativeBuilder<?> builder) {
        appendTextElement(doc, parent, "pixel", "0");
    }

    private void encodeVec4iAxis(@NotNull Document doc, @NotNull Element parent,
            @NotNull RelativeMeasure.RelativeBuilder<?> builder) {
        appendTextElement(doc, parent, "pixel", "0");
    }

    private void encodeComponent(@NotNull Document doc, @NotNull Element parent, @NotNull MenuComponent<?> menuComponent) {
        Element compEl = doc.createElement("menuComponent");
        parent.appendChild(compEl);

        String type = deriveComponentType(menuComponent);
        compEl.setAttribute("type", type);

        if (menuComponent instanceof IdentifierHolder holder) {
            compEl.setTextContent(holder.identifier().toString());
        }
    }

    private @Nullable ModernTheme findTheme(@NotNull Collection<MenuComponent<?>> menuComponents) {
        return menuComponents.stream()
                .filter(c -> c instanceof ModernTheme)
                .map(c -> (ModernTheme) c)
                .findFirst()
                .orElse(null);
    }

    private @NotNull String deriveElementType(@NotNull ElementLike<?> element) {
        String className = element.getClass().getSimpleName();
        return camelCaseToKebabCase(className.replace("Element", ""));
    }

    private @NotNull String derivePropertyType(@NotNull Property<?, ?> property) {
        String className = property.getClass().getSimpleName();
        return className.toLowerCase();
    }

    private @NotNull String deriveComponentType(@NotNull MenuComponent<?> menuComponent) {
        if (menuComponent instanceof ModernTheme) {
            return "theme";
        }
        String className = menuComponent.getClass().getSimpleName();
        return camelCaseToKebabCase(className);
    }

    private @NotNull String camelCaseToKebabCase(@NotNull String input) {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, input);
    }

    private @NotNull Document createDocument() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.newDocument();
    }

    private @NotNull String documentToString(@NotNull Document doc) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, XML_ENCODING);
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();
    }

    private void appendTextElement(@NotNull Document doc, @NotNull Element parent,
            @NotNull String name, @NotNull String value) {
        Element el = doc.createElement(name);
        el.setTextContent(value);
        parent.appendChild(el);
    }

    private record PageEntry(Page page, String filePath, boolean isRoot) {
    }

    public static class XmlEncoderException extends Exception {
        public XmlEncoderException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
