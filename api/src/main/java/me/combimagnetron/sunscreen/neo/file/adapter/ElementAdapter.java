package me.combimagnetron.sunscreen.neo.file.adapter;

import com.google.common.base.CaseFormat;
import me.combimagnetron.passport.internal.registry.Registry;
import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.sunscreen.neo.element.Elements;
import me.combimagnetron.sunscreen.neo.element.ModernElement;
import me.combimagnetron.sunscreen.neo.element.impl.ButtonElement;
import me.combimagnetron.sunscreen.neo.file.XmlEncodable;
import me.combimagnetron.sunscreen.neo.property.Property;
import me.combimagnetron.sunscreen.neo.property.PropertyMap;
import me.combimagnetron.sunscreen.neo.registry.Registries;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Locale;
import java.util.Map;

public interface ElementAdapter<M extends ModernElement<M, ?>> extends XmlEncodable<M> {

    ButtonElementAdapter BUTTON_ELEMENT_ADAPTER = new ButtonElementAdapter();

    @NotNull Class<M> type();

    @NotNull String typeName();

    ElementAdapter<?>[] ADAPTERS = new ElementAdapter[]{BUTTON_ELEMENT_ADAPTER};

    class ButtonElementAdapter implements ElementAdapter<ButtonElement> {

        @Override
        public @NotNull ButtonElement decode(@NotNull Element element) {
            return null;
        }

        @Override
        public void encode(@NotNull ButtonElement type, @NotNull Element parent, @NotNull Document document) {
            Element element = writeBase(type, new BaseData(type.identifier(), type.propertyMap()), parent, document);
            parent.appendChild(element);
        }

        @Override
        public @NotNull Class<ButtonElement> type() {
            return ButtonElement.class;
        }

        @Override
        public @NotNull String typeName() {
            return "button-element";
        }

    }

    static <P extends Property<?, P>, M extends ModernElement<M, ?>> @NotNull Element writeBase(@NotNull M parent, @NotNull BaseData baseData, @NotNull Element element, @NotNull Document document) {
        Element top = document.createElement(toSnakeCase(parent.getClass().getSimpleName()));//document.createElement(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, parent.getClass().getSimpleName()));
        Element properties = document.createElement("properties");
        Element identifier = document.createElement("identifier");
        identifier.setTextContent(baseData.identifier().string());
        for (Map.Entry<Class<? extends Property<?, ?>>, Property<?, ?>> classPropertyEntry : baseData.propertyMap.entrySet()) {
            Class<? extends Property<?, ?>> type = classPropertyEntry.getKey();
            P property = (P) classPropertyEntry.getValue();
            PropertyAdapter<P> adapter = PropertyAdapter.find(type);
            adapter.encode(property, properties, document);
        }
        top.appendChild(identifier);
        top.appendChild(properties);
        return top;
    }

    static @NotNull BaseData base(@NotNull Element element, @NotNull Document document) {
        return null;

    }

    record BaseData(@NotNull Identifier identifier, @NotNull PropertyMap propertyMap) {

    }

    static String toSnakeCase(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    static void defaults() {
        Registry<ElementAdapter<?>, String> registry = Registries.advanced().elementAdapters();
        for (ElementAdapter<?> adapter : ADAPTERS) {
            registry.register(toSnakeCase(adapter.getClass().getSimpleName().toLowerCase().replace("adapter", "")), adapter);
        }
    }

}
