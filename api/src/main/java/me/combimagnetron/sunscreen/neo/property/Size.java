package me.combimagnetron.sunscreen.neo.property;

import me.combimagnetron.sunscreen.neo.editor.property.EditorPropertyInfo;
import me.combimagnetron.sunscreen.neo.editor.property.PropertyCategory;
import me.combimagnetron.sunscreen.neo.editor.property.PropertyValueType;
import me.combimagnetron.sunscreen.neo.property.handler.PropertyHandler;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.render.Viewport;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

@EditorPropertyInfo(category = PropertyCategory.PROPERTY, valueType = PropertyValueType.RING_VALUE)
public class Size extends RelativeMeasure.Vec2iRelativeMeasureGroup<Size> implements Property<Vec2i, Size> {
    private static final PropertyHandler<Size> PROPERTY_HANDLER = (element, context, size) -> null;
    private final Map<RelativeMeasure.Axis2d, RelativeMeasure.RelativeBuilder<Size>> axisMap = new LinkedHashMap<>();

    protected Size(@NotNull Vec2i vec2i) {
        super(vec2i);
    }

    @SuppressWarnings("unchecked")
    protected Size(@NotNull RelativeMeasure.Vec2iRelativeMeasureGroup<?> measureGroup) {
        axisMap.putAll((Map<RelativeMeasure.Axis2d, RelativeMeasure.RelativeBuilder<Size>>) (Map<?, ?>) measureGroup.axisBuilderMap());
    }

    public static <C> @NotNull Size relative(RelativeMeasure.Vec2iRelativeMeasureGroup<C> measureGroup) {
        return new Size(measureGroup);
    }

    public static @NotNull Size fixed(@NotNull Vec2i vec2i) {
        return new Size(vec2i);
    }

    public static @NotNull Size fit() {
        return new Fit();
    }

    @Override
    public @NotNull Class<Vec2i> type() {
        return Vec2i.class;
    }

    @Override
    public @NotNull PropertyHandler<Size> handler() {
        return PROPERTY_HANDLER;
    }

    @Override
    public void finish(@NotNull Viewport viewport) {
        Vec2i view = viewport.currentView();
        int x = axisMap.get(RelativeMeasure.Axis2d.X).finish(view.x());
        int y = axisMap.get(RelativeMeasure.Axis2d.Y).finish(view.y());
        vec2i = Vec2i.of(x, y);
    }

    @Override
    public Map<RelativeMeasure.Axis2d, RelativeMeasure.RelativeBuilder<RelativeMeasure.Vec2iRelativeMeasureGroup<Size>>> axisBuilderMap() {
        return (Map<RelativeMeasure.Axis2d, RelativeMeasure.RelativeBuilder<RelativeMeasure.Vec2iRelativeMeasureGroup<Size>>>) (Map<?, ?>) axisMap;
    }

    public static class Fit extends Size implements FitToContent<Size> {

        public Fit() {
            super((Vec2i) null);
        }

        @Override
        public @NotNull Class<Size> parent() {
            return Size.class;
        }

    }

}
