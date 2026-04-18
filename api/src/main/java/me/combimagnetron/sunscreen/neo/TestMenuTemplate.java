package me.combimagnetron.sunscreen.neo;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.editor.element.EditorNameElement;
import me.combimagnetron.sunscreen.neo.element.Elements;
import me.combimagnetron.sunscreen.neo.element.impl.SelectorElement;
import me.combimagnetron.sunscreen.neo.element.impl.ButtonElement;
import me.combimagnetron.sunscreen.neo.element.impl.text.TextFieldElement;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.graphic.NineSlice;
import me.combimagnetron.sunscreen.neo.graphic.color.Color;
import me.combimagnetron.sunscreen.neo.property.Decorator;
import me.combimagnetron.sunscreen.neo.property.Position;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.theme.ModernTheme;
import me.combimagnetron.sunscreen.neo.theme.color.ColorSchemes;
import me.combimagnetron.sunscreen.neo.theme.decorator.Target;
import me.combimagnetron.sunscreen.neo.theme.decorator.ThemeDecorator;
import org.jetbrains.annotations.NotNull;

public class TestMenuTemplate implements MenuTemplate {
    private static final Identifier IDENTIFIER = Identifier.of("sunscreen", "test_menu");
    public static final ModernTheme THEME = ModernTheme.theme(
        Identifier.of(
            "sunscreen",
            "test_menu/theme/test")
    ).colorScheme(
        ColorSchemes.EDITOR
    ).decorator(
        ThemeDecorator.stated(
                Target.typed(ButtonElement.class)
            )
            .standard(Canvas.resource("default.png"))
            .hovered(Canvas.resource("hovered.png"))
            .clicked(Canvas.resource("clicked.png"))
    ).decorator(
        ThemeDecorator.nineSlice(
            Target.typed(TextFieldElement.class),
            NineSlice.nineSlice(Canvas.empty(Vec2i.of(9, 9)).fill(Vec2i.zero(), Vec2i.of(9, 9), Color.of(13, 13, 13)))
        )
    ).decorator(
        ThemeDecorator.stated(
                Target.identifier(Identifier.of("sunscreen", "decorator/secondary_button"))
            )
            .standard(Canvas.resource("secondary_default.png"))
            .hovered(Canvas.resource("secondary_hover.png"))
            .clicked(Canvas.resource("secondary_click.png"))
    ).decorator(
        ThemeDecorator.stated(
            Target.typed(SelectorElement.class)
                )
                    .standard(Canvas.resource("editor_assets/e_default.png"))
        .hovered(Canvas.resource("editor_assets/e_hover.png"))
        .clicked(Canvas.resource("editor_assets/e_click.png"))
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
            THEME
        );
        root
            .element(
                Elements.button(
                        Identifier.of(
                            "sunscreen",
                            "test_menu/element/button"
                        )
                    )
                    .position(Position.fixed(Vec2i.of(450, 200))).size(Size.fixed(Vec2i.of(100, 20)))
                    .decorator(Decorator.decorator(Target.identifier(Identifier.of("sunscreen", "decorator/secondary_button"))))
            )/*.element(
            Elements.image(
                Identifier.of(
                    "sunscreen",
                    "test_menu/element/test_image"
                ),
                Canvas.empty(Vec2i.of(100, 100)).fill(Position.nil(), Size.fixed(Vec2i.of(100, 100)), Color.of(107, 3, 244))
            ).position(Position.fixed(Vec2i.of(0, 0)))
        ).element(
            Elements.button(
                Identifier.of(
                    "sunscreen",
                    "test_menu/element/button"
                )
            ).listen().mouse(event -> SunscreenLibrary.library().logger().debug(event.context().position().toString())).back()
                .position(Position.fixed(Vec2i.of(450, 200))).size(Size.fixed(Vec2i.of(100, 20)))
        ).element(
            Elements.shape(
                Identifier.of(
                "sunscreen",
                "test_menu/element/test_shape"
                ),
                Shape.line(Vec2i.of(200, 322), Vec2i.of(600, 20), 3),
                Color.of(0, 0, 0)
            ).position(Position.fixed(Vec2i.of(200, 200)))
        ).element(
            Elements.image(
                Identifier.of(
                    "sunscreen",
                    "test_menu/element/test_image_url"
                ),
                Canvas.url("https://i.imgur.com/YNMmJRM.png")
            ).position(Position.fixed(Vec2i.of(200, 300)))//.position(Position.relative(RelativeMeasure.vec2i().x().percentage(50).back().y().percentage(50).back()))
        ).element(
            new MultiValueSelectorElement(Identifier.of("hello")).position(Position.fixed(Vec2i.of(660, 41)))
        ).element(
            Elements.textField(
                Identifier.of("test")
            ).position(Position.fixed(Vec2i.of(20, 300))).size(Size.fixed(Vec2i.of(700, 10)))
        ).element(
            Elements.comparison(Identifier.of("test"), Canvas.resource("normal.png").replace(Color.of(205, 104, 61), Color.of(255, 0, 0)), Canvas.resource("inverted.png")).size(Size.fixed(Vec2i.of(200, 168))).position(Position.relative(RelativeMeasure.vec2i().x().percentage(50).pixel(-100).back().y().percentage(50).pixel(-84).back()))
        )*/;
    }

}
