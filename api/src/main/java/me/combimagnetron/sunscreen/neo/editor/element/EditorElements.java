package me.combimagnetron.sunscreen.neo.editor.element;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.sunscreen.neo.editor.EditorController;
import org.jetbrains.annotations.NotNull;

public interface EditorElements {

    static @NotNull FrameElement frame(@NotNull Identifier identifier) {
        return new FrameElement(identifier);
    }

    static @NotNull MenuPreviewElement preview(@NotNull Identifier identifier, @NotNull EditorController controller) {
        return new MenuPreviewElement(identifier, controller);
    }

    static @NotNull MultiValueSelectorElement multiValue(@NotNull Identifier identifier) {
        return new MultiValueSelectorElement(identifier);
    }

    static @NotNull ValueSelectorElement value(@NotNull Identifier identifier) {
        return new ValueSelectorElement(identifier);
    }

    static @NotNull EditorNameElement nameElement(@NotNull Identifier identifier) {
        return new EditorNameElement(identifier);
    }

    static @NotNull PageNameElement pageNameElement(@NotNull Identifier identifier) {
        return new PageNameElement(identifier);
    }

    static @NotNull LayerOverviewElement layerOverview(@NotNull Identifier identifier, @NotNull EditorController controller) {
        return new LayerOverviewElement(identifier, controller);
    }

    static @NotNull ElementLibraryElement elementLibrary(@NotNull Identifier identifier, @NotNull EditorController controller) {
        return new ElementLibraryElement(identifier, controller);
    }

}
