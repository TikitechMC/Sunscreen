package me.combimagnetron.sunscreen.neo.editor.element;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.ActiveMenu;
import me.combimagnetron.sunscreen.neo.element.GenericModernElement;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.graphic.color.Color;
import me.combimagnetron.sunscreen.neo.graphic.text.Text;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.render.engine.context.RenderContext;
import me.combimagnetron.sunscreen.util.helper.PropertyHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class NoticeElement extends GenericModernElement<NoticeElement, Canvas> {
    private final Text text;
    private final ActiveMenu menu;
    private int progress = 100;
    private int tick = 3;

    public NoticeElement(@NotNull Identifier identifier, @NotNull Text text, @NotNull ActiveMenu menu) {
        super(identifier);
        this.text = text;
        this.menu = menu;
    }

    @Override
    public @NonNull Canvas render(@NonNull Size property, @Nullable RenderContext context) {
        tick--;
        if (tick == 0) {
            progress--;
            tick = 3;
        }
        if (progress == -1) {
            menu.remove(identifier());
        }
        Vec2i sizeVec = PropertyHelper.vectorOrThrow(size(), Vec2i.class);
        Vec2i progressSizeVec = PropertyHelper.vectorOrThrow(size(), Vec2i.class).sub(2, 0);
        Canvas canvas = Canvas.empty(sizeVec);
        Canvas frame = FrameElement.frame(sizeVec.sub(0, 1));
        canvas.place(frame, Vec2i.zero());
        canvas.text(text, Vec2i.of(2, 2));
        canvas.fill(Vec2i.of(0, sizeVec.y() - 1), Vec2i.of((int) (progressSizeVec.x() * (progress / 100f)), 1), Color.of(249, 194, 43));
        return canvas;
    }

}
