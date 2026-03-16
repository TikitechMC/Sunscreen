package me.combimagnetron.sunscreen.neo.element.impl;

import me.combimagnetron.passport.logic.state.MutableState;
import me.combimagnetron.passport.logic.state.State;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.element.GenericModernElement;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.sunscreen.neo.graphic.text.Text;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.font.FontProperties;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.render.engine.context.RenderContext;
import me.combimagnetron.sunscreen.util.helper.PropertyHelper;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LabelElement extends GenericModernElement<LabelElement, Canvas> {
    private final MutableState<Text> text;

    public LabelElement(@NotNull Identifier identifier, @NotNull Component text) {
        this(identifier, Text.adventure(text));
    }

    public LabelElement(@NotNull Identifier identifier, @NotNull Text text) {
        super(identifier);
        this.text = State.mutable(text);
    }

    public LabelElement(@NotNull Identifier identifier, @NotNull State<Text> componentState) {
        super(identifier);
        this.text = (MutableState<Text>) componentState;
        MutableState<Text> componentMutableState = (MutableState<Text>) componentState;
        componentMutableState.observe((old, current) -> {
        });
    }

    @Override
    public @NotNull Canvas render(@NotNull Size property, @Nullable RenderContext renderContext) {
        Vec2i size = PropertyHelper.vectorOrThrow(property, Vec2i.class);
        return Canvas.empty(size).text(text.value(), Vec2i.zero());
    }

}
