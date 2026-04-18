package me.combimagnetron.sunscreen.neo.editor.virtual.argument;

import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.ActiveMenu;
import me.combimagnetron.sunscreen.neo.element.ModernElement;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.layout.Layout;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface Argument<V> {

    @NotNull Class<V> type();

    @NotNull V decode(@NotNull Layout<?> layout);

    @NotNull Collection<? extends ModernElement<?, Canvas>> fields(@NotNull Vec2i position);

}
