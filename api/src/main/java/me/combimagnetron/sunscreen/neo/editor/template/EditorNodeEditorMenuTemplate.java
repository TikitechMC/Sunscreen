package me.combimagnetron.sunscreen.neo.editor.template;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.sunscreen.neo.MenuRoot;
import me.combimagnetron.sunscreen.neo.MenuTemplate;
import org.jetbrains.annotations.NotNull;

public class EditorNodeEditorMenuTemplate implements MenuTemplate {
    private final static Identifier IDENTIFIER = Identifier.of("sunscreen", "internal/editor/node");

    @Override
    public @NotNull Identifier identifier() {
        return null;
    }

    @Override
    public void build(@NotNull MenuRoot root) {

    }

}
