package me.combimagnetron.sunscreen.neo.theme.decorator;

import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.element.ElementLike;
import me.combimagnetron.sunscreen.neo.element.GenericInteractableModernElement;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.graphic.NineSlice;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.render.Renderable;
import me.combimagnetron.sunscreen.neo.render.engine.context.RenderContext;
import me.combimagnetron.sunscreen.util.helper.PropertyHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Interface representing theme entries to link to an element, to retexture a button in a theme for example.
 */
public sealed interface ThemeDecorator extends Renderable<Size, Canvas> permits Divider, ThemeDecorator.NineSliceThemeDecorator, ThemeDecorator.StateNineSliceThemeDecorator {

    @NotNull Target<?> target();

    static <E extends ElementLike<E>> @NotNull NineSliceThemeDecorator nineSlice(@NotNull Target<?> target, @NotNull NineSlice nineSlice) {
        return new NineSliceThemeDecorator(target, nineSlice);
    }

    static <E extends ElementLike<E>> @NotNull StateNineSliceThemeDecorator stateNineSlice(@NotNull Target<?> target, @NotNull Map<GenericInteractableModernElement.ElementPhase, NineSlice> phases) {
        return new StateNineSliceThemeDecorator(target, phases);
    }

    static <E extends ElementLike<E>> @NotNull StateNineSliceThemeDecorator stated(@NotNull Target<?> target) {
        return new StateNineSliceThemeDecorator(target, new HashMap<>());
    }

    record NineSliceThemeDecorator(@NotNull Target<?> target, @NotNull NineSlice nineSlice) implements ThemeDecorator {

        @Override
        public @NotNull Canvas render(@NotNull Size property, @Nullable RenderContext context) {
            Vec2i sizeVec = PropertyHelper.vectorOrThrow(property, Vec2i.class);
            return nineSlice.size(sizeVec);
        }

    }

    record StateNineSliceThemeDecorator(@NotNull Target<?> target, @NotNull Map<GenericInteractableModernElement.ElementPhase, NineSlice> nineSlices) implements ThemeDecorator {

        public @NotNull Canvas render(@NotNull Size property, @Nullable RenderContext context, @NotNull GenericInteractableModernElement.ElementPhase phase) {
            if (context == null) throw new IllegalArgumentException("Context may not be null while constructing decorators.");
            NineSlice nineSlice = nineSlices.get(phase);
            if (nineSlice == null) return Canvas.empty(Vec2i.zero());
            Vec2i sizeVec = PropertyHelper.vectorOrThrow(property, Vec2i.class);
            return nineSlice.size(sizeVec);
        }

        @Override
        public @NotNull Canvas render(@NotNull Size property, @Nullable RenderContext context) {
            return render(property, context, GenericInteractableModernElement.ElementPhase.DEFAULT);
        }

        public @NotNull StateNineSliceThemeDecorator standard(@NotNull Canvas canvas) {
            nineSlices.put(GenericInteractableModernElement.ElementPhase.DEFAULT, NineSlice.nineSlice(canvas));
            return this;
        }

        public @NotNull StateNineSliceThemeDecorator clicked(@NotNull Canvas canvas) {
            nineSlices.put(GenericInteractableModernElement.ElementPhase.CLICK, NineSlice.nineSlice(canvas));
            return this;
        }

        public @NotNull StateNineSliceThemeDecorator hovered(@NotNull Canvas canvas) {
            nineSlices.put(GenericInteractableModernElement.ElementPhase.HOVER, NineSlice.nineSlice(canvas));
            return this;
        }

        public @NotNull StateNineSliceThemeDecorator disabled(@NotNull Canvas canvas) {
            nineSlices.put(GenericInteractableModernElement.ElementPhase.DISABLED, NineSlice.nineSlice(canvas));
            return this;
        }

        public @NotNull StateNineSliceThemeDecorator standard(@NotNull NineSlice slice) {
            nineSlices.put(GenericInteractableModernElement.ElementPhase.DEFAULT, slice);
            return this;
        }

        public @NotNull StateNineSliceThemeDecorator clicked(@NotNull NineSlice slice) {
            nineSlices.put(GenericInteractableModernElement.ElementPhase.CLICK, slice);
            return this;
        }

        public @NotNull StateNineSliceThemeDecorator hovered(@NotNull NineSlice slice) {
            nineSlices.put(GenericInteractableModernElement.ElementPhase.HOVER, slice);
            return this;
        }

        public @NotNull StateNineSliceThemeDecorator disabled(@NotNull NineSlice slice) {
            nineSlices.put(GenericInteractableModernElement.ElementPhase.DISABLED, slice);
            return this;
        }

        public @NotNull StateNineSliceThemeDecorator state(@NotNull GenericInteractableModernElement.ElementPhase phase, @NotNull Canvas canvas) {
            nineSlices.put(phase, NineSlice.nineSlice(canvas));
            return this;
        }

        public @NotNull StateNineSliceThemeDecorator state(@NotNull GenericInteractableModernElement.ElementPhase phase, @NotNull NineSlice slice) {
            nineSlices.put(phase, slice);
            return this;
        }

    }

}
