package me.combimagnetron.sunscreen.neo.editor.template;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.MenuTemplate;
import me.combimagnetron.sunscreen.neo.MenuRoot;
import me.combimagnetron.sunscreen.neo.editor.element.EditorNameElement;
import me.combimagnetron.sunscreen.neo.editor.element.MenuPreviewElement;
import me.combimagnetron.sunscreen.neo.editor.element.MultiValueSelectorElement;
import me.combimagnetron.sunscreen.neo.editor.element.ValueSelectorElement;
import me.combimagnetron.sunscreen.neo.element.impl.ButtonElement;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.graphic.NineSlice;
import me.combimagnetron.sunscreen.neo.graphic.color.Color;
import me.combimagnetron.sunscreen.neo.property.Position;
import me.combimagnetron.sunscreen.neo.property.Scale;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.property.Z;
import me.combimagnetron.sunscreen.neo.theme.ModernTheme;
import me.combimagnetron.sunscreen.neo.theme.color.ColorSchemes;
import me.combimagnetron.sunscreen.neo.theme.decorator.Target;
import me.combimagnetron.sunscreen.neo.theme.decorator.ThemeDecorator;
import org.jetbrains.annotations.NotNull;

public class EditorMenuTemplate implements MenuTemplate {
    private final static Identifier IDENTIFIER = Identifier.of("sunscreen", "internal/editor/main");
    public final static ModernTheme EDITOR_THEME = ModernTheme.theme(
            Identifier.of("sunscreen", "internal/editor/theme/basic")
        ).colorScheme(ColorSchemes.EDITOR)
        .decorator(
            ThemeDecorator.stated(
                    Target.typed(ButtonElement.class)
                )
                .standard(Canvas.empty(Vec2i.of(11, 11)).fill(Position.nil(), Size.fixed(Vec2i.of(11, 11)), Color.of(39, 39, 39)))
                .hovered(Canvas.empty(Vec2i.of(11, 11)).fill(Position.nil(), Size.fixed(Vec2i.of(11, 11)), Color.of(61, 61, 61)))
                .clicked(Canvas.empty(Vec2i.of(11, 11)).fill(Position.nil(), Size.fixed(Vec2i.of(11, 11)), Color.of(27, 27, 27)))
        ).decorator(
            ThemeDecorator.nineSlice(
                Target.typed(EditorNameElement.class),
                NineSlice.nineSlice(Canvas.empty(Vec2i.of(9, 9)).fill(Vec2i.zero(), Vec2i.of(9, 9), Color.of(27, 27, 27)))
            )
        );

    @Override
    public @NotNull Identifier identifier() {
        return IDENTIFIER;
    }

    @Override
    public void build(@NotNull MenuRoot root) {
        root.theme(
            EDITOR_THEME
        );
        root/*.element(
            Elements.image(Identifier.of("background"), Canvas.url("https://i.imgur.com/PNCpUmN.png")).position(Position.nil()).z(Z.z(0.05f))
        )*/.element(
            new MenuPreviewElement(Identifier.of("wow")).position(Position.fixed(Vec2i.of(139, 15))).size(Size.fixed(Vec2i.of(518, 432))).scale(Scale.fixed(1.561)).z(Z.z(0.1f))
        ).element(
            new MultiValueSelectorElement(Identifier.of("hello")).position(Position.fixed(Vec2i.of(660, 41)))
        ).element(
            new ValueSelectorElement(Identifier.of("bye")).position(Position.fixed(Vec2i.of(660, 90)))
        );
    }

}
