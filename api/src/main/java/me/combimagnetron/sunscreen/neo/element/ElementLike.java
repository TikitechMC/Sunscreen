package me.combimagnetron.sunscreen.neo.element;

import me.combimagnetron.sunscreen.neo.property.PropertyContainer;
import me.combimagnetron.sunscreen.util.IdentifierHolder;

public interface ElementLike<E extends ElementLike<E>> extends PropertyContainer<E>, IdentifierHolder {

}
