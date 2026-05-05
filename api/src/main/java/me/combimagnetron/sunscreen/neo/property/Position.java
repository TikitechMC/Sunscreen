package me.combimagnetron.sunscreen.neo.property;

import me.combimagnetron.sunscreen.neo.editor.input.SelectorInputContext;
import me.combimagnetron.sunscreen.neo.editor.property.EditorProperty;
import me.combimagnetron.sunscreen.neo.property.handler.PropertyHandler;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.render.Viewport;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Position extends RelativeMeasure.Vec2iRelativeMeasureGroup<Position> implements EditorProperty<Vec2i, Position, SelectorInputContext.ValueInnerContext> {
    private static final PropertyHandler<Position> PROPERTY_HANDLER = (element, context, position) -> null;
    private static final Position ZERO = Position.fixed(Vec2i.zero());

    private final Map<RelativeMeasure.Axis2d, RelativeMeasure.RelativeBuilder<Size>> axisMap = new LinkedHashMap<>();
    private Target target = Target.TOP_LEFT;

    /**
     * Anchor on the element box: position value is the point named by the constant; {@link #resolve(Vec2i)} returns top-left
     * by subtracting {@link #offset(Vec2i)} from that point.
     */
    public enum Target {
        TOP_LEFT(s -> Vec2i.zero()),
        TOP_CENTER(s -> Vec2i.of(s.x() / 2, 0)),
        TOP_RIGHT(s -> Vec2i.of(s.x(), 0)),
        LEFT_CENTER(s -> Vec2i.of(0, s.y() / 2)),
        CENTER(s -> Vec2i.of(s.x() / 2, s.y() / 2)),
        RIGHT_CENTER(s -> Vec2i.of(s.x(), s.y() / 2)),
        BOTTOM_LEFT(s -> Vec2i.of(0, s.y())),
        BOTTOM_CENTER(s -> Vec2i.of(s.x() / 2, s.y())),
        BOTTOM_RIGHT(s -> Vec2i.of(s.x(), s.y()));

        private final Function<Vec2i, Vec2i> anchorToTopLeftOffset;

        Target(@NotNull Function<Vec2i, Vec2i> anchorToTopLeftOffset) {
            this.anchorToTopLeftOffset = anchorToTopLeftOffset;
        }

        public @NotNull Vec2i offset(@NotNull Vec2i elementSize) {
            return anchorToTopLeftOffset.apply(elementSize);
        }
    }

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

    public @NotNull Position target(@NotNull Target target) {
        this.target = target;
        return this;
    }

    public @NotNull Target target() {
        return target;
    }

    public @NotNull Vec2i resolve(@NotNull Vec2i size) {
        Vec2i value = value();
        return value.sub(target.offset(size));
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
    public Map<RelativeMeasure.Axis2d, RelativeMeasure.RelativeBuilder<RelativeMeasure.Vec2iRelativeMeasureGroup<Position>>> axisBuilderMap() {
        return (Map<RelativeMeasure.Axis2d, RelativeMeasure.RelativeBuilder<RelativeMeasure.Vec2iRelativeMeasureGroup<Position>>>) (Map<?, ?>) axisMap;
    }

    @Override
    public void finish(@NotNull Viewport viewport) {
        Vec2i view = viewport.currentView();
        int x = axisMap.get(RelativeMeasure.Axis2d.X).finish(view.x());
        int y = axisMap.get(RelativeMeasure.Axis2d.Y).finish(view.y());
        vec2i = Vec2i.of(x, y);
    }


    @Override
    public @NotNull Position apply(@NotNull SelectorInputContext.ValueInnerContext innerContext) {
        Integer[] values = innerContext.values();
        this.vec2i = Vec2i.of(values[0], values[1]);
        return this;
    }

}
