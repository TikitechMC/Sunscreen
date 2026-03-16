package me.combimagnetron.sunscreen.neo.editor.element;

import me.combimagnetron.passport.util.data.Identifier;
import org.jetbrains.annotations.NotNull;

public interface EditorElements {

    static @NotNull FrameElement frame(@NotNull Identifier identifier) {
        return new FrameElement(identifier);
    }

    static @NotNull MenuPreviewElement preview(@NotNull Identifier identifier) {
        return new MenuPreviewElement(identifier);
    }

    static @NotNull MultiValueSelectorElement paddingMargin(@NotNull Identifier identifier) {
        return new MultiValueSelectorElement(identifier);
    }

    static @NotNull EditorNameElement nameElement(@NotNull Identifier identifier) {
        return new EditorNameElement(identifier);
    }

}
