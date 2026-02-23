package me.combimagnetron.sunscreen.neo.property;

import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.element.ElementLike;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.property.handler.PropertyHandler;
import me.combimagnetron.sunscreen.neo.render.Viewport;
import me.combimagnetron.sunscreen.neo.render.engine.context.RenderContext;
import me.combimagnetron.sunscreen.util.helper.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Scale extends RelativeMeasure.FloatRelativeMeasureGroup<Scale> implements Property<Float, Scale> {
    private static final Scale DEFAULT = Scale.fixed(1.56f);
    private static final PropertyHandler<Scale> HANDLER = (element, context, scale) -> null;
    private boolean lossless = true;

    public Scale(float value) {
        super(value);
    }

    public Scale(@NotNull RelativeMeasure.FloatRelativeMeasureGroup<Scale> measureGroup) {

    }

    public static @NotNull Scale fixed(float value) {
        return new Scale(value);
    }

    public static @NotNull Scale relative(@NotNull RelativeMeasure.FloatRelativeMeasureGroup<Scale> measureGroup) {
        return new Scale(measureGroup);
    }

    public static @NotNull Scale fit() {
        return new Fit();
    }

    public static @NotNull Scale none() {
        return DEFAULT;
    }

    public boolean lossless() {
        return lossless;
    }

    public Scale lossless(boolean lossless) {
        this.lossless = lossless;
        return this;
    }

    @Override
    public @NotNull Class<@NotNull Float> type() {
        return Float.class;
    }

    @Override
    public @NotNull PropertyHandler<@NotNull Scale> handler() {
        return HANDLER;
    }

    @Override
    public void finish(@NotNull Viewport viewport) {

    }

    @Override
    public void add(FloatRelativeBuilder<Scale> scaleFloatRelativeBuilder, Void unused) {

    }

    public @NotNull BigDecimal rounded() {
        return new BigDecimal(Float.toString(value)).setScale(3, RoundingMode.HALF_UP);
    }

    public static class PropertyHandlerImpl implements PropertyHandler<Scale> {

        @Override
        public @Nullable Canvas apply(@Nullable ElementLike<? extends ElementLike<?>> parent, RenderContext renderContext, Scale property) {
            Viewport viewport = renderContext.viewport();
            if (viewport == null) return null;
            Vec2i parentSize = parent == null ? renderContext.viewport().currentView() : parent.size().value();
            if (property instanceof FitToContent<?> fit) {

            }
            boolean rescaleBufferedOnly = false;
            float value = property.value();
            if (value != Float.MIN_VALUE) {
                rescaleBufferedOnly = MathHelper.isPowerOfTwo(value) && !property.lossless();
            }
            Canvas start = renderContext.start().entrySet().stream().findFirst().orElseThrow().getValue();
            if (start == null) return null;
            if (rescaleBufferedOnly) {
                return start.scale(value);
            }
            return null;
        }

    }

    public static class Fit extends Scale implements FitToContent<Scale> {

        public Fit() {
            super((Float) null);
        }

        @Override
        public @NotNull Class<Scale> parent() {
            return Scale.class;
        }

    }

}
