package me.combimagnetron.sunscreen.neo.editor.property;

import me.combimagnetron.sunscreen.neo.editor.input.SelectorInputContext;
import me.combimagnetron.sunscreen.neo.property.Property;
import org.jetbrains.annotations.NotNull;

public interface EditorProperty<T, C, I extends SelectorInputContext.InnerContext<?>> extends Property<T, C> {

    /**
     * applies the values of the selector widget in the editor to a property
     * only used in the editor to simulate position and size, thus doesn't allow for relative measures (those are 'calculated' in the editor)
     * @param innerContext the context to use
     * @return itself
     */
    @NotNull C apply(@NotNull I innerContext);

}
