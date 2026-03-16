package me.combimagnetron.sunscreen.neo.editor.virtual;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.sunscreen.neo.element.ElementLike;
import me.combimagnetron.sunscreen.neo.property.PropertyMap;

public class VirtualElement {
    private final PropertyMap propertyMap = new PropertyMap();
    private final Class<? extends ElementLike<?>> target;
    private final Identifier identifier;

    public VirtualElement(Class<? extends ElementLike<?>> target, Identifier identifier) {
        this.target = target;
        this.identifier = identifier;
    }

}
