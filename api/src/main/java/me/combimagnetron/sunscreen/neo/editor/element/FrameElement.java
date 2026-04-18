package me.combimagnetron.sunscreen.neo.editor.element;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.element.GenericModernElement;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.graphic.color.Color;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.render.engine.context.RenderContext;
import me.combimagnetron.sunscreen.neo.theme.ModernTheme;
import me.combimagnetron.sunscreen.neo.theme.color.ColorScheme;
import me.combimagnetron.sunscreen.util.helper.PropertyHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class FrameElement extends GenericModernElement<FrameElement, Canvas> {

    protected FrameElement(@NotNull Identifier identifier) {
        super(identifier);
    }

    @Override
    public @NonNull Canvas render(@NonNull Size property, @Nullable RenderContext context) {
        final Vec2i sizeVec = PropertyHelper.vectorOrThrow(size(), Vec2i.class);
        if (context == null) return Canvas.error(size());
        //final ModernTheme theme = context.theme();
        //final ColorScheme scheme = theme.colorScheme();
        return frame(sizeVec);
    }

    public static @NotNull Canvas frame(@NotNull Vec2i sizeVec) {
        Color outsideFrameColor = Color.of(27, 27,27);//scheme.accent();
        Color backgroundFrameColor = Color.of(13, 13, 13);//scheme.background();
        Canvas canvas = Canvas.empty(sizeVec);
        canvas.fill(Vec2i.zero(), sizeVec, outsideFrameColor);
        canvas.fill(Vec2i.of(1, 1), sizeVec.sub(2), backgroundFrameColor);
        return canvas;
    }

}
