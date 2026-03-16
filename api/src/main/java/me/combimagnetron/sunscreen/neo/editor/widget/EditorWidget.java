package me.combimagnetron.sunscreen.neo.editor.widget;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.sunscreen.neo.element.GenericInteractableModernElement;
import me.combimagnetron.sunscreen.neo.element.ModernElement;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.input.ListenerReferences;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.render.engine.context.RenderContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class EditorWidget extends GenericInteractableModernElement<EditorWidget, Canvas, EditorWidget.EditorWidgetListenerReferences> {
    private final EditorWidgetListenerReferences references = new EditorWidgetListenerReferences(this);
    private final Set<EditorWidgetTab> tabs = new HashSet<>();
    private int selected = 0;

    protected EditorWidget(@Nullable Identifier identifier) {
        super(identifier);
    }


    @Override
    public @NonNull Canvas render(@NonNull Size property, @Nullable RenderContext context) {
        return null;
    }

    @Override
    public @NonNull EditorWidgetListenerReferences listen() {
        return references;
    }

    public record EditorWidgetListenerReferences(@NotNull EditorWidget back) implements ListenerReferences<EditorWidget, EditorWidgetListenerReferences> {

    }

}
