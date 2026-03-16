package me.combimagnetron.sunscreen.neo.editor.template;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.MenuRoot;
import me.combimagnetron.sunscreen.neo.MenuTemplate;
import me.combimagnetron.sunscreen.neo.element.Elements;
import me.combimagnetron.sunscreen.neo.property.Position;
import me.combimagnetron.sunscreen.neo.property.Size;
import org.jetbrains.annotations.NotNull;

public class EditorCreateProjectMenuTemplate implements MenuTemplate {
    private final static Identifier IDENTIFIER = Identifier.of("sunscreen", "internal/editor/create_project");

    @Override
    public @NotNull Identifier identifier() {
        return IDENTIFIER;
    }

    @Override
    public void build(@NotNull MenuRoot root) {
        root.theme(EditorMenuTemplate.EDITOR_THEME);
        root.element(Elements.button(Identifier.of("editor", "button_test")).size(Size.fixed(Vec2i.of(60, 10))).position(Position.nil()));
    }
}
