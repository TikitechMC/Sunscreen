package me.combimagnetron.sunscreen.neo.property;

import me.combimagnetron.passport.util.data.RuntimeDefinable;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.passport.util.math.Vec4i;
import me.combimagnetron.sunscreen.neo.render.Viewport;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

/**
 *
 * @param <C> class for the relative measure
 * @param <K> type for builder
 * @param <B> builder type
 * @param <V> variable to input and get the relative shizzle
 * @param <R> measure again, to return for builder methods
 * @param <L> segment types
 */
public interface RelativeMeasure<C, K, I, B extends RuntimeDefinable.Builder<?, I>, V, R extends RelativeMeasure<C, K, I, B, V, R, L>, L> extends RuntimeDefinable<C, B, V, L> {

    static <C> @NotNull Vec2iRelativeMeasureGroup<@NotNull C> vec2i() {
        return new DummyVec2iRelativeMeasureGroup<>();
    }

    static <C> @NotNull Vec4iRelativeMeasureGroup<@NotNull C> vec4i() {
        return new DummyVec4iRelativeMeasureGroup<>();
    }

    final class DummyVec2iRelativeMeasureGroup<C> extends Vec2iRelativeMeasureGroup<@NotNull C> {
        @Override
        public void finish(@NotNull Viewport unused) {}
    }

    final class DummyVec4iRelativeMeasureGroup<C> extends Vec4iRelativeMeasureGroup<@NotNull C> {
        @Override
        public void finish(@NotNull Viewport unused) {}
    }

    interface RelativeMeasureGroup<K> {

        @Nullable K value();

        void finish(@NotNull Viewport screenSize);

    }

    final class RelativeBuilder<G> {
        private final Collection<OffsetType> offsetTypes = new ArrayList<>();
        private final G parent;

        private RelativeBuilder(G parent) {
            this.parent = parent;
        }

        public G back() {
            return parent;
        }

        public @NotNull RelativeBuilder<G> offset(@NotNull OffsetType offsetType) {
            offsetTypes.add(offsetType);
            return this;
        }

        public @NotNull RelativeBuilder<G> percentage(double percentage) {
            return offset(OffsetType.percentage(percentage));
        }

        public @NotNull RelativeBuilder<G> pixel(int pixel) {
            return offset(OffsetType.pixel(pixel));
        }

        public int finish(int input) {
            int finished = 0;
            for (OffsetType offsetType : offsetTypes) {
                finished += offsetType.value(input);
            }
            return finished;
        }

        static <G> @NotNull RelativeBuilder<G> of(G parent) {
            return new RelativeBuilder<>(parent);
        }

    }

    abstract class Vec2iRelativeMeasureGroup<C> implements RelativeMeasureGroup<Vec2i> {
        private final Map<Axis2d, RelativeBuilder<Vec2iRelativeMeasureGroup<C>>> axisBuilderMap = Map.of(
            Axis2d.X, RelativeBuilder.of(this),
            Axis2d.Y, RelativeBuilder.of(this)
        );
        protected Vec2i vec2i;
        protected Supplier<Vec2i> supplier;

        public Vec2iRelativeMeasureGroup(@NotNull Vec2i vec2i) {
            this.vec2i = vec2i;
        }

        public Vec2iRelativeMeasureGroup(@NotNull Supplier<Vec2i> supplier) {
            this.supplier = supplier;
        }

        public Vec2iRelativeMeasureGroup() {

        }

        public @NotNull RelativeBuilder<Vec2iRelativeMeasureGroup<C>> x() {
            return axisBuilderMap.get(Axis2d.X);
        }

        public @NotNull RelativeBuilder<Vec2iRelativeMeasureGroup<C>> y() {
            return axisBuilderMap.get(Axis2d.Y);
        }

        public @Nullable Vec2i value() {
            return supplier == null ? vec2i : supplier.get();
        }

        public Map<Axis2d, RelativeBuilder<Vec2iRelativeMeasureGroup<C>>> axisBuilderMap() {
            return axisBuilderMap;
        }

        public abstract void finish(@NotNull Viewport screenSize);

    }

    abstract class Vec4iRelativeMeasureGroup<C> implements RelativeMeasureGroup<Vec4i> {
        private final Map<Axis4d, RelativeBuilder<Vec4iRelativeMeasureGroup<C>>> axisBuilderMap = Map.of(
            Axis4d.UP, RelativeBuilder.of(this),
            Axis4d.DOWN, RelativeBuilder.of(this),
            Axis4d.LEFT, RelativeBuilder.of(this),
            Axis4d.RIGHT, RelativeBuilder.of(this)
        );
        protected Vec4i vec4i;

        public Vec4iRelativeMeasureGroup(@NotNull Vec4i vec4i) {
            this.vec4i = vec4i;
        }

        public Vec4iRelativeMeasureGroup() {

        }

        public @NotNull RelativeBuilder<Vec4iRelativeMeasureGroup<C>> up() {
            return axisBuilderMap.get(Axis4d.UP);
        }

        public @NotNull RelativeBuilder<Vec4iRelativeMeasureGroup<C>> down() {
            return axisBuilderMap.get(Axis4d.DOWN);
        }

        public @NotNull RelativeBuilder<Vec4iRelativeMeasureGroup<C>> left() {
            return axisBuilderMap.get(Axis4d.LEFT);
        }

        public @NotNull RelativeBuilder<Vec4iRelativeMeasureGroup<C>> right() {
            return axisBuilderMap.get(Axis4d.RIGHT);
        }

        public @Nullable Vec4i value() {
            return vec4i;
        }

        public abstract void finish(@NotNull Viewport screenSize);

    }

    abstract class FloatRelativeMeasureGroup<C> implements RelativeMeasureGroup<Float> {
        private final RelativeBuilder<FloatRelativeMeasureGroup<C>> relativeBuilder = RelativeBuilder.of(this);
        protected float value;

        public FloatRelativeMeasureGroup(float value) {
            this.value = value;
        }

        public FloatRelativeMeasureGroup() {

        }

        public @NotNull RelativeBuilder<FloatRelativeMeasureGroup<C>> set() {
            return relativeBuilder;
        }

        public Float value() {
            return value;
        }

        public abstract void finish(@NotNull Viewport screenSize);

    }

    enum Axis2d {
        X, Y
    }

    enum Axis4d {
        UP, DOWN, LEFT, RIGHT
    }

    interface OffsetType {

        int value(int input);

        static <I> OffsetType pixel(int pixel) {
            return new PixelOffsetType(pixel);
        }

        static <I> OffsetType percentage(double percentage) {
            return new PercentageOffsetType(percentage);
        }

        record PixelOffsetType(int pixel) implements OffsetType {

            @Override
            public int value(int input) {
                return pixel;
            }

        }

        record PercentageOffsetType(Double value) implements OffsetType {

            @Override
            public int value(int input) {
                return (int) ((value/100)*input);
            }

        }

    }

}
