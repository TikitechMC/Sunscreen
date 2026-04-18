package me.combimagnetron.sunscreen.neo.file;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface XmlEncodable<T> {

    @NotNull T decode(@NotNull Element element);

    void encode(@NotNull T type, @NotNull Element parent, @NotNull Document document);

}
