package me.combimagnetron.sunscreen.neo.editor.project;

import me.combimagnetron.passport.util.data.Identifier;

public class EditorProject {
    private final String displayName;
    private final Identifier identifier;

    public EditorProject(String displayName, Identifier identifier) {
        this.displayName = displayName;
        this.identifier = identifier;
    }
}
