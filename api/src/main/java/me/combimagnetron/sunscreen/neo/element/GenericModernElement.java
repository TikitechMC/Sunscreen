package me.combimagnetron.sunscreen.neo.element;

import me.combimagnetron.sunscreen.neo.graphic.GraphicLike;
import me.combimagnetron.sunscreen.neo.property.Property;
import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.sunscreen.neo.property.PropertyMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings("unchecked")
public abstract class GenericModernElement<E extends ModernElement<E, G>, G extends GraphicLike<G>> implements ModernElement<E, G> {
    private final PropertyMap propertyMap = new PropertyMap();
    private final Identifier identifier;

    protected GenericModernElement(@NotNull Identifier identifier) {
        this.identifier = identifier;
    }

    @Override
    public @NotNull Identifier identifier() {
        return identifier;
    }

    @Override
    public @NotNull <T, C> E property(@NotNull Property<T, C> property) {
        propertyMap.put((Class<? extends Property<?, ?>>) property.getClass(), property);
        return (E) this;
    }

    @Override
    public <T, C, P extends Property<T, C>> @NotNull P property(@NotNull Class<P> propertyClass) {
        return (P) propertyMap.get(propertyClass);
    }

    @Override
    public @NotNull Collection<Property<?, ?>> properties() {
        return propertyMap.values();
    }

    public @NotNull PropertyMap propertyMap() {
        return propertyMap;
    }

}
