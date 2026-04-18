package me.combimagnetron.sunscreen.neo.file;

import me.combimagnetron.sunscreen.neo.MenuRoot;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public final class XmlEncoder {
    private static final String XML_VERSION = "1.0";
    private static final String XML_ENCODING = "UTF-16";

    private final MenuRoot root;
    private final MenuMetadata metadata;
    //private final List<PageEntry> pages = new LinkedList<>();
    private final List<AssetReference> assets = new LinkedList<>();

    private XmlEncoder(@NotNull MenuRoot root, @NotNull MenuMetadata metadata) {
        this.root = root;
        this.metadata = metadata;
    }

}
