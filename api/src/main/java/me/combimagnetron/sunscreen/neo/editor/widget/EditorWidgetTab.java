package me.combimagnetron.sunscreen.neo.editor.widget;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.sunscreen.neo.element.ElementLike;
import me.combimagnetron.sunscreen.neo.graphic.text.Text;

import java.util.HashMap;
import java.util.Map;

public class EditorWidgetTab {
    private final Map<Identifier, ElementLike<?>> elements = new HashMap<>();
    private final Text displayName;

    public EditorWidgetTab(Text displayName) {
        this.displayName = displayName;
    }


}
