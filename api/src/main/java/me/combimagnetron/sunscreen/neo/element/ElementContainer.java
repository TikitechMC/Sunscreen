package me.combimagnetron.sunscreen.neo.element;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.render.Renderable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface ElementContainer<E extends ModernElement<E, Canvas>> extends ModernElement<E, Canvas> {

    @NotNull Collection<ModernElement<?, Canvas>> children();

    <L extends ModernElement<L, Canvas>> @NotNull ElementContainer<@NotNull E> add(@NotNull L elementLike);

    <L extends ModernElement<L, Canvas>> @NotNull ElementContainer<@NotNull E> add(@NotNull Iterable<@NotNull L> elementLike);

    <L extends ModernElement<L, Canvas>> @NotNull ElementContainer<@NotNull E> remove(@NotNull L elementLike);

    @NotNull ElementContainer<@NotNull E> remove(@NotNull Identifier identifier);

}
