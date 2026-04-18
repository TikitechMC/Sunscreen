package me.combimagnetron.sunscreen.neo.property;

import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.property.handler.PropertyHandler;
import me.combimagnetron.passport.util.math.Vec4i;
import me.combimagnetron.sunscreen.neo.render.Viewport;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

public class Margin extends RelativeMeasure.Vec4iRelativeMeasureGroup<Margin> implements Property<Vec4i, Margin> {
    private static final PropertyHandler<Margin> PROPERTY_HANDLER = (element, context, margin) -> null;
    private final Map<RelativeMeasure.Axis4d, RelativeMeasure.RelativeBuilder<@NotNull Margin>> axisMap = new LinkedHashMap<>();

    public Margin(@NotNull Vec4i Vec4i) {
        super(Vec4i);
    }

    public Margin(@NotNull RelativeMeasure.Vec4iRelativeMeasureGroup<?> measureGroup) {
        axisMap.putAll((Map<RelativeMeasure.Axis4d, RelativeMeasure.RelativeBuilder<Margin>>) (Map<?, ?>) measureGroup.axisBuilderMap());
    }

    public static <C> @NotNull Margin relative(RelativeMeasure.Vec4iRelativeMeasureGroup<@NotNull C> measureGroup) {
        return new Margin(measureGroup);
    }

    public static @NotNull Margin fixed(@NotNull Vec4i vec4i) {
        return new Margin(vec4i);
    }

    public static @NotNull Margin fixed(int padding) {
        return fixed(Vec2i.of(padding, padding));
    }

    public static @NotNull Margin fixed(@NotNull Vec2i vec2i) {
        return fixed(Vec4i.mirror(vec2i));
    }

    public static @NotNull Margin fit() {
        return new Fit();
    }

    @Override
    public @NotNull Class<@NotNull Vec4i> type() {
        return Vec4i.class;
    }

    @Override
    public @NotNull PropertyHandler<@NotNull Margin> handler() {
        return PROPERTY_HANDLER;
    }

    @Override
    public Map<RelativeMeasure.Axis4d, RelativeMeasure.RelativeBuilder<RelativeMeasure.Vec4iRelativeMeasureGroup<Margin>>> axisBuilderMap() {
        return (Map<RelativeMeasure.Axis4d, RelativeMeasure.RelativeBuilder<RelativeMeasure.Vec4iRelativeMeasureGroup<Margin>>>) (Map<?, ?>) axisMap;
    }

    @Override
    public void finish(@NotNull Viewport viewport) {

    }

    public static class Fit extends Margin implements FitToContent<Margin> {

        public Fit() {
            super((Vec4i) null);
        }

        @Override
        public @NotNull Class<Margin> parent() {
            return Margin.class;
        }

    }

}
