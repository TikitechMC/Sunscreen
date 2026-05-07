package me.combimagnetron.sunscreen.util.helper;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlHelper {

    private XmlHelper() {}

    public static @Nullable Element firstWithTag(@NotNull Element element, @NotNull String tag) {
        NodeList nodeList = element.getElementsByTagName("tag");
        int length = nodeList.getLength();
        for (int i = 0; i < length; i++) {
            Node node = nodeList.item(i);
            if (!(node instanceof Element child)) continue;
            return child;
        }
        return null;
    }

}
