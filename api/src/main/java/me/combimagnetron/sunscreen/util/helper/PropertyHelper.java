package me.combimagnetron.sunscreen.util.helper;

import me.combimagnetron.sunscreen.neo.property.Property;
import me.combimagnetron.sunscreen.neo.property.RelativeMeasure;
import org.jetbrains.annotations.NotNull;

public class PropertyHelper {

    private PropertyHelper() {}

    public static <T, P extends Property<T, P> & RelativeMeasure.RelativeMeasureGroup<?>> @NotNull T vectorOrThrow(@NotNull P property, @NotNull Class<T> clazz) {
        if (property.value() == null) throw new IllegalStateException("Value cannot be null when vectorOrThrow is called!");
        return (T) property.value();
    }

}
