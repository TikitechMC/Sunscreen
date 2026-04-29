package me.combimagnetron.sunscreen.util.helper;

import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.element.ElementContainer;
import me.combimagnetron.sunscreen.neo.element.ElementLike;
import me.combimagnetron.sunscreen.neo.element.GenericInteractableModernElement;
import me.combimagnetron.sunscreen.neo.element.ModernElement;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.graphic.NineSlice;
import me.combimagnetron.sunscreen.neo.property.Decorator;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.theme.ModernTheme;
import me.combimagnetron.sunscreen.neo.theme.decorator.ThemeDecorator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Fills missing {@link Size} from element content ({@link ModernElement#intrinsicSize()}) and, when a
 * {@link ModernTheme} is present, from themed nine-slice <strong>source</strong> canvases.
 * <p>
 * Call only via {@link #seedMenuTree(Collection, ModernTheme)} after the menu tree is fully built (or for roots
 * passed to {@link me.combimagnetron.sunscreen.neo.ActiveMenu#add}).
 */
public final class ElementSizeSeeder {

    private ElementSizeSeeder() {}

    /**
     * Runs intrinsic sizing for every node under {@code roots}, then a theme pass for any size still missing.
     */
    public static void seedMenuTree(@NotNull Collection<ElementLike<?>> roots, @Nullable ModernTheme theme) {
        for (ElementLike<?> root : roots) {
            seedRecursive(root);
        }
        seedFromTheme(roots, theme);
    }

    private static void seedRecursive(@NotNull ElementLike<?> elementLike) {
        if (elementLike instanceof ElementContainer<?> container) {
            for (ModernElement<?, Canvas> child : container.children()) {
                seedRecursive(child);
            }
        }
        if (elementLike instanceof ModernElement<?, ?> modern) {
            seedElement(modern);
        }
    }

    private static void seedFromTheme(@NotNull Collection<ElementLike<?>> roots, @Nullable ModernTheme theme) {
        if (theme == null) return;
        for (ElementLike<?> root : roots) {
            seedFromThemeRecursive(root, theme);
        }
    }

    private static void seedFromThemeRecursive(@NotNull ElementLike<?> elementLike, @NotNull ModernTheme theme) {
        if (elementLike instanceof ElementContainer<?> container) {
            for (ModernElement<?, Canvas> child : container.children()) {
                seedFromThemeRecursive(child, theme);
            }
        }
        if (elementLike instanceof ModernElement<?, ?> modern) {
            seedElementFromTheme(modern, theme);
        }
    }

    private static void seedElementFromTheme(@NotNull ModernElement<?, ?> element, @NotNull ModernTheme theme) {
        if (!needsIntrinsicSeed(element.property(Size.class))) return;
        Vec2i fromTheme = sizeFromResolvedThemeDecorator(element, theme);
        if (fromTheme != null) element.size(Size.fixed(fromTheme));
    }

    private static void seedElement(@NotNull ModernElement<?, ?> element) {
        Size sizeProp = element.property(Size.class);
        if (!needsIntrinsicSeed(sizeProp)) return;
        Vec2i intrinsic = element.intrinsicSize();
        if (intrinsic == null) return;
        element.size(Size.fixed(intrinsic));
    }

    /**
     * @return true if we should assign a fixed intrinsic size before layout / render.
     */
    private static boolean needsIntrinsicSeed(@Nullable Size sizeProp) {
        if (sizeProp == null) return true;
        if (sizeProp instanceof Size.Fit) return true;
        if (sizeProp.value() != null) return false;
        // Unresolved relative size: resolved from viewport in the render pipeline.
        return sizeProp.axisBuilderMap().isEmpty();
    }

    /**
     * Mirrors {@code RenderContext.decorator} target resolution, then reads intrinsic size from nine-slice source art.
     */
    private static @Nullable Vec2i sizeFromResolvedThemeDecorator(@NotNull ModernElement<?, ?> element, @NotNull ModernTheme theme) {
        ThemeDecorator decorator = resolveThemeDecorator(element, theme);
        return sizeFromThemeDecorator(decorator);
    }

    private static @Nullable ThemeDecorator resolveThemeDecorator(@NotNull ModernElement<?, ?> element, @NotNull ModernTheme theme) {
        Decorator<?> dec = element.property(Decorator.class);
        if (dec == null || dec.target().target().equals(element.getClass())) {
            for (ThemeDecorator candidate : theme.decorators()) {
                if (candidate.target().target().equals(element.getClass())) return candidate;
            }
            return null;
        }
        return theme.find(dec.target());
    }

    private static @Nullable Vec2i sizeFromThemeDecorator(@Nullable ThemeDecorator decorator) {
        if (decorator == null) return null;
        if (decorator instanceof ThemeDecorator.StateNineSliceThemeDecorator stated) {
            return firstNineSliceSourceSize(stated);
        }
        if (decorator instanceof ThemeDecorator.NineSliceThemeDecorator simple) {
            return simple.nineSlice().sourceCanvas().size();
        }
        return null;
    }

    private static @Nullable Vec2i firstNineSliceSourceSize(@NotNull ThemeDecorator.StateNineSliceThemeDecorator stated) {
        NineSlice slice = stated.nineSlices().get(GenericInteractableModernElement.ElementPhase.DEFAULT);
        if (slice == null) {
            for (NineSlice candidate : stated.nineSlices().values()) {
                if (candidate != null) {
                    slice = candidate;
                    break;
                }
            }
        }
        return slice == null ? null : slice.sourceCanvas().size();
    }

}
