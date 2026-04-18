package me.combimagnetron.sunscreen.neo.editor.virtual;

import me.combimagnetron.sunscreen.neo.MenuRoot;
import me.combimagnetron.sunscreen.neo.editor.virtual.argument.Argument;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class VirtualMenuRoot implements VirtualObject<MenuRoot> {
    private final List<VirtualPage> pages = new ArrayList<>();

    public @NotNull VirtualMenuRoot page(@NotNull VirtualPage page) {
        pages.add(page);
        return this;
    }

    public @NotNull Collection<VirtualPage> pages() {
        return pages;
    }
}
