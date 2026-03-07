package me.combimagnetron.sunscreen.neo.property;

import me.combimagnetron.sunscreen.neo.property.handler.PropertyHandler;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.render.Viewport;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class Position extends RelativeMeasure.Vec2iRelativeMeasureGroup<Position> implements Property<Vec2i, Position> {
    private static final PropertyHandler<Position> PROPERTY_HANDLER = (element, context, position) -> null;
    private static final Position ZERO = Position.fixed(Vec2i.zero());

    private final Map<RelativeMeasure.Axis2d, RelativeMeasure.RelativeBuilder<Size>> axisMap = new LinkedHashMap<>();

    public static Position nil() {
        return ZERO;
    }

    public Position(@NotNull Vec2i vec2i) {
        super(vec2i);
    }

    public Position(@NotNull Supplier<Vec2i> supplier) {
        super(supplier);
    }

    @SuppressWarnings("unchecked")
    public Position(@NotNull RelativeMeasure.Vec2iRelativeMeasureGroup<?> measureGroup) {
        axisMap.putAll((Map<RelativeMeasure.Axis2d, RelativeMeasure.RelativeBuilder<Size>>) (Map<?, ?>) measureGroup.axisBuilderMap());
    }

    public static <C> @NotNull Position relative(RelativeMeasure.Vec2iRelativeMeasureGroup<C> measureGroup) {
        return new Position(measureGroup);
    }

    public static @NotNull Position fixed(@NotNull Vec2i vec2i) {
        return new Position(vec2i);
    }

    public static @NotNull Position supplied(@NotNull Supplier<Vec2i> supplier) {
        return new Position(supplier);
    }

    @Override
    public @NotNull Class<Vec2i> type() {
        return Vec2i.class;
    }

    @Override
    public @NotNull PropertyHandler<Position> handler() {
        return PROPERTY_HANDLER;
    }

    @Override
    public void finish(@NotNull Viewport viewport) {
        Vec2i view = viewport.currentView();
        int x = axisMap.get(RelativeMeasure.Axis2d.X).finish(view.x());
        int y = axisMap.get(RelativeMeasure.Axis2d.Y).finish(view.y());
        vec2i = Vec2i.of(x, y);
    }



}
