package me.combimagnetron.sunscreen.neo.editor.property;

import org.jetbrains.annotations.NotNull;

public @interface EditorPropertyInfo {

    @NotNull PropertyCategory category();

    @NotNull PropertyValueType valueType();

}
