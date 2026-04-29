package me.combimagnetron.sunscreen.neo.property;

import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.theme.decorator.Target;
import me.combimagnetron.sunscreen.util.helper.PropertyHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface PropertyContainer<R> {

    <T, C, P extends Property<T, C>> @Nullable P property(@NotNull Class<P> propertyClass);

    <T, C> @NotNull R property(@NotNull Property<@NotNull T, @NotNull C> property);

    @NotNull Collection<Property<?, ?>> properties();

    default <T, C, P extends Property<T, C>> @NotNull P propOr(@NotNull Class<P> propertyClass, @NotNull P or) {
        P p = property(propertyClass);
        return p == null ? or : p;
    }

    default <T, C, P extends Property<T, C>> @Nullable P propOrNull(@NotNull Class<P> propertyClass) {
        return property(propertyClass);
    }

    default <T, C, P extends Property<T, C>> @NotNull P propOrThrow(@NotNull Class<P> propertyClass) {
        P p = property(propertyClass);
        if (p == null) throw new IllegalStateException("Property of type %s is null.".formatted(propertyClass.getSimpleName()));
        return p;
    }

    default @NotNull Size size() {
        return propOrThrow(Size.class);
    }

    default @NotNull Position position() {
        var pos = propOrThrow(Position.class);
        if (pos.target() != Position.Target.TOP_LEFT) {
            return pos.resolve(PropertyHelper.vectorOrThrow(size(), Vec2i.class));
        }
        return pos;
    }

    default @NotNull Margin margin() {
        return propOrThrow(Margin.class);
    }

    default @NotNull Padding padding() {
        return propOrThrow(Padding.class);
    }

    default @NotNull Scale scale() {
        return propOr(Scale.class, Scale.none());
    }

    default @NotNull Visibility visibility() {
        return propOr(Visibility.class, Visibility.visible());
    }

    default @NotNull Z z() {
        return propOr(Z.class, Z.middle());
    }

    default @Nullable Decorator<?> decorator() {
        return propOrNull(Decorator.class);
    }

    default @NotNull R size(@NotNull Size size) {
        return property(size);
    }

    default @NotNull R position(@NotNull Position position) {
        return property(position);
    }

    default @NotNull R margin(@NotNull Margin margin) {
        return property(margin);
    }

    default @NotNull R padding(@NotNull Padding padding) {
        return property(padding);
    }

    default @NotNull R scale(@NotNull Scale scale) {
        return property(scale);
    }

    default @NotNull R visibility(@NotNull Visibility visibility) {
        return property(visibility);
    }

    default @NotNull R z(@NotNull Z z) {
        return property(z);
    }

    default @NotNull R decorator(@NotNull Decorator<?> decorator) {
        return property(decorator);
    }

}
