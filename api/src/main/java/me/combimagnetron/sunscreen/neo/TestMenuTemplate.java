package me.combimagnetron.sunscreen.neo;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.SunscreenLibrary;
import me.combimagnetron.sunscreen.neo.element.Elements;
import me.combimagnetron.sunscreen.neo.element.impl.ComparisonElement;
import me.combimagnetron.sunscreen.neo.element.impl.SliderElement;
import me.combimagnetron.sunscreen.neo.element.impl.ButtonElement;
import me.combimagnetron.sunscreen.neo.element.impl.text.TextFieldElement;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.graphic.NineSlice;
import me.combimagnetron.sunscreen.neo.graphic.color.Color;
import me.combimagnetron.sunscreen.neo.property.Position;
import me.combimagnetron.sunscreen.neo.property.RelativeMeasure;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.theme.ModernTheme;
import me.combimagnetron.sunscreen.neo.theme.color.ColorSchemes;
import me.combimagnetron.sunscreen.neo.theme.decorator.ThemeDecorator;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class TestMenuTemplate implements MenuTemplate {
    private static final Identifier IDENTIFIER = Identifier.of("sunscreen", "test_menu");

    @Override
    public @NotNull Identifier identifier() {
        return IDENTIFIER;
    }

    @Override
    public void build(@NotNull MenuRoot root) {
        Path dataPath = SunscreenLibrary.library().path();
        root.theme(
            ModernTheme.theme(
                Identifier.of(
                    "sunscreen",
                    "test_menu/theme/test")
            ).colorScheme(
                ColorSchemes.EDITOR
            ).decorator(
                ThemeDecorator.stated(
                    ButtonElement.class
                )
                    .standard(Canvas.resource("default.png"))
                    .hovered(Canvas.resource("hovered.png"))
                    .clicked(Canvas.resource("clicked.png"))
            ).decorator(
                ThemeDecorator.nineSlice(
                    TextFieldElement.class,
                    NineSlice.nineSlice(Canvas.empty(Vec2i.of(9, 9)).fill(Vec2i.zero(), Vec2i.of(9, 9), Color.of(13, 13, 13)))
                )
            )
        );
        //root.element(Elements.image(Identifier.of("woopsie_fuck"), Canvas.url("https://i.imgur.com/eIacYAm.png")).position(Position.nil()).scale(Scale.fixed(1.56f))
        root
            /*.element(
            new MenuPreviewElement(Identifier.of("wow")).position(Position.fixed(Vec2i.of(139, 15))).size(Size.relative(RelativeMeasure.vec2i().x().percentage(60).back().y().percentage(45).back()))
        ).element(
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
            new PaddingMarginElement(Identifier.of("hello")).position(Position.fixed(Vec2i.of(660, 41)))
        ).element(
            Elements.textField(
                Identifier.of("test")
            ).position(Position.fixed(Vec2i.of(20, 300))).size(Size.fixed(Vec2i.of(700, 10)))
        )*/.element(
            Elements.comparison(Identifier.of("test"), Canvas.resource("normal.png"), Canvas.resource("inverted.png")).size(Size.fixed(Vec2i.of(200, 168))).position(Position.relative(RelativeMeasure.vec2i().x().percentage(50).pixel(-100).back().y().percentage(50).pixel(-84).back()))
        );
    }

}
