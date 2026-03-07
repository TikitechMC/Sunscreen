package me.combimagnetron.sunscreen.neo.property;

import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.element.ElementLike;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.property.handler.PropertyHandler;
import me.combimagnetron.passport.util.math.Vec4i;
import me.combimagnetron.sunscreen.neo.render.Viewport;
import me.combimagnetron.sunscreen.neo.render.engine.context.RenderContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class Padding extends RelativeMeasure.Vec4iRelativeMeasureGroup<Padding> implements Property<Vec4i, Padding> {
    private static final PropertyHandler<Padding> PROPERTY_HANDLER = (element, context, padding) -> null;
    private final Map<RelativeMeasure.Axis4d, RelativeMeasure.RelativeBuilder<Padding>> axisMap = new LinkedHashMap<>();

    public Padding(@NotNull Vec4i Vec4i) {
        super(Vec4i);
    }

    public Padding(@NotNull RelativeMeasure.Vec4iRelativeMeasureGroup<?> measureGroup) {
        //axisMap.putAll((Map<? extends RelativeMeasure.Axis4d, ? extends Vec4iRelativeBuilder<Padding>>) measureGroup.axisBuilderMap());
    }

    public static <C> @NotNull Padding relative(RelativeMeasure.Vec4iRelativeMeasureGroup<C> measureGroup) {
        return new Padding(measureGroup);
    }

    public static @NotNull Padding fixed(@NotNull Vec4i vec4i) {
        return new Padding(vec4i);
    }

    public static @NotNull Padding fixed(int padding) {
        return fixed(Vec2i.of(padding, padding));
    }

    public static @NotNull Padding fixed(@NotNull Vec2i vec2i) {
        return fixed(Vec4i.mirror(vec2i));
    }

    public static @NotNull Padding fit() {
        return new Fit();
    }

    @Override
    public @NotNull Class<Vec4i> type() {
        return Vec4i.class;
    }

    @Override
    public @NotNull PropertyHandler<Padding> handler() {
        return PROPERTY_HANDLER;
    }

    @Override
    public void finish(@NotNull Viewport viewport) {

    }

    public static class PropertyHandlerImpl implements PropertyHandler<Padding> {

        @Override
        public @Nullable Canvas apply(@Nullable ElementLike<? extends ElementLike<?>> parent, @NotNull RenderContext renderContext, Padding property) {
            Vec2i parentSize = parent == null ? renderContext.viewport().currentView() : parent.size().value();
            if (property instanceof FitToContent<?> fit) {

            }

            return null;
        }

    }

    public static class Fit extends Padding implements FitToContent<Padding> {

        public Fit() {
            super((Vec4i) null);
        }

        @Override
        public @NotNull Class<Padding> parent() {
            return Padding.class;
        }

    }

}
