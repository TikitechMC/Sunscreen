package me.combimagnetron.sunscreen.neo;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.SunscreenLibrary;
import me.combimagnetron.sunscreen.neo.editor.element.EditorElements;
import me.combimagnetron.sunscreen.neo.editor.element.EditorNameElement;
import me.combimagnetron.sunscreen.neo.element.Elements;
import me.combimagnetron.sunscreen.neo.element.impl.SelectorElement;
import me.combimagnetron.sunscreen.neo.element.impl.ButtonElement;
import me.combimagnetron.sunscreen.neo.element.impl.text.TextFieldElement;
import me.combimagnetron.sunscreen.neo.event.UserClickElementEvent;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.graphic.NineSlice;
import me.combimagnetron.sunscreen.neo.graphic.color.Color;
import me.combimagnetron.sunscreen.neo.graphic.shape.Shape;
import me.combimagnetron.sunscreen.neo.graphic.text.Text;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.color.TextColor;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.font.FontProperties;
import me.combimagnetron.sunscreen.neo.property.Decorator;
import me.combimagnetron.sunscreen.neo.property.Position;
import me.combimagnetron.sunscreen.neo.property.RelativeMeasure;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.registry.Registries;
import me.combimagnetron.sunscreen.neo.theme.ModernTheme;
import me.combimagnetron.sunscreen.neo.theme.color.ColorSchemes;
import me.combimagnetron.sunscreen.neo.theme.decorator.Target;
import me.combimagnetron.sunscreen.neo.theme.decorator.ThemeDecorator;
import org.jetbrains.annotations.NotNull;

public class TestMenuTemplate implements MenuTemplate {
    private static final Identifier IDENTIFIER = Identifier.of("sunscreen", "test_menu");
    private static final Identifier FONT_SUNBURNED = Identifier.of("sunscreen", "font/sunburned");

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

    private static @NotNull Text caption(@NotNull String content) {
        return Text.basic(content)
            .font(Registries.fonts().get(FONT_SUNBURNED))
            .color(TextColor.color(Color.of(220, 220, 220)))
            .fontProperties(FontProperties.properties().baseline(-6));
    }

    @Override
    public @NotNull Identifier identifier() {
        return IDENTIFIER;
    }

    @Override
    public void build(@NotNull MenuRoot root) {
        root.theme(THEME);

        root.element(
            Elements.image(
                Identifier.of("sunscreen", "test_menu/panel/backdrop"),
                Canvas.empty(Vec2i.of(1000, 600)).fill(
                    Vec2i.zero(),
                    Vec2i.of(1000, 600),
                    Color.of(0xD0101010, 0xD0101010, 0xE0101010, 0xE0101010)
                )
            ).position(Position.fixed(Vec2i.zero()))
        ).element(
            Elements.label(
                Identifier.of("sunscreen", "test_menu/label/title"),
                caption("Sunscreen interaction harness")
            ).position(Position.fixed(Vec2i.of(14, 10)))
        ).element(
            Elements.label(
                Identifier.of("sunscreen", "test_menu/label/hint"),
                caption("Watch server log for click / text / selector events")
            ).position(Position.fixed(Vec2i.of(14, 26)))
        ).element(
            new SelectorElement(Identifier.of("sunscreen", "test_menu/selector/tabs"), 12)
                .entry(Text.vanilla("Alpha"))
                .entry(Text.vanilla("Beta"))
                .entry(Text.vanilla("Gamma"))
                .position(Position.fixed(Vec2i.of(14, 46)))
                .size(Size.fixed(Vec2i.of(200, 14)))
                .listen()
                .select(event ->
                    SunscreenLibrary.library().logger().info(
                        "Test harness tab select relative={} element={}",
                        event.coords(),
                        event.element().identifier()
                    ))
                .back()
        ).element(
            Elements.label(
                Identifier.of("sunscreen", "test_menu/label/button_positioning_hint"),
                caption("Buttons: manual text offset, auto-centered text, and center-anchored positioning")
            ).position(Position.fixed(Vec2i.of(14, 60)))
        ).element(
            Elements.button(Identifier.of("sunscreen", "test_menu/button/primary"))
                .position(Position.fixed(Vec2i.of(14, 84)))
                .size(Size.fixed(Vec2i.of(120, 22)))
                .listen()
                .click(event -> logClick("primary", event))
                .back()
        ).element(
            Elements.button(Identifier.of("sunscreen", "test_menu/button/secondary"))
                .position(Position.fixed(Vec2i.of(144, 84)))
                .size(Size.fixed(Vec2i.of(120, 22)))
                .decorator(Decorator.decorator(Target.identifier(Identifier.of("sunscreen", "decorator/secondary_button"))))
                .listen()
                .click(event -> logClick("secondary", event))
                .back()
        ).element(
            Elements.button(
                Identifier.of("sunscreen", "test_menu/button/labeled_manual"),
                caption("Label xy"),
                Vec2i.of(6, 5)
            )
                .position(Position.fixed(Vec2i.of(274, 84)))
                .size(Size.fixed(Vec2i.of(120, 22)))
                .listen()
                .click(event -> logClick("labeled_manual", event))
                .back()
        ).element(
            Elements.button(
                Identifier.of("sunscreen", "test_menu/button/labeled_centered"),
                caption("Centered label")
            )
                .position(Position.fixed(Vec2i.of(404, 84)))
                .size(Size.fixed(Vec2i.of(140, 22)))
                .listen()
                .click(event -> logClick("labeled_centered", event))
                .back()
        ).element(
            Elements.button(
                Identifier.of("sunscreen", "test_menu/button/position_center_fixed"),
                caption("Center target (fixed)")
            )
                .position(Position.fixed(Vec2i.of(500, 136)).target(Position.Target.CENTER))
                .size(Size.fixed(Vec2i.of(170, 22)))
                .listen()
                .click(event -> logClick("position_center_fixed", event))
                .back()
        ).element(
            Elements.button(
                Identifier.of("sunscreen", "test_menu/button/position_center_relative"),
                caption("Center target (relative)")
            )
                .position(
                    Position.relative(
                        RelativeMeasure.vec2i()
                            .x().percentage(50).back()
                            .y().percentage(50).pixel(-120).back()
                    ).target(Position.Target.CENTER)
                )
                .size(Size.fixed(Vec2i.of(182, 22)))
                .listen()
                .click(event -> logClick("position_center_relative", event))
                .back()
        ).element(
            Elements.textField(Identifier.of("sunscreen", "test_menu/field/inline"))
                .position(Position.fixed(Vec2i.of(14, 164)))
                .size(Size.fixed(Vec2i.of(380, 24)))
                .listen()
                .updated(event -> SunscreenLibrary.library().logger().debug(
                    "Test harness inline field: {}",
                    event.context().stream().value()
                ))
                .back()
        ).element(
            Elements.label(
                Identifier.of("sunscreen", "test_menu/label/anvil_hint"),
                caption("Anvil field (clears on focus):")
            ).position(Position.fixed(Vec2i.of(14, 194)))
        ).element(
            Elements.textField(Identifier.of("sunscreen", "test_menu/field/anvil"))
                .shouldClear(true)
                .position(Position.fixed(Vec2i.of(14, 210)))
                .size(Size.fixed(Vec2i.of(380, 24)))
                .listen()
                .updated(event -> SunscreenLibrary.library().logger().info(
                    "Test harness anvil field: {}",
                    event.context().stream().value()
                ))
                .back()
        ).element(
            EditorElements.multiValue(Identifier.of("sunscreen", "test_menu/editor/multi_value"))
                .position(Position.fixed(Vec2i.of(14, 246)))
                .listen()
                .selector(event -> SunscreenLibrary.library().logger().info(
                    "Test harness multi-value drag: {}",
                    event.context()
                ))
                .back()
        ).element(
            EditorElements.value(Identifier.of("sunscreen", "test_menu/editor/value_strip"))
                .position(Position.fixed(Vec2i.of(164, 246)))
        ).element(
            Elements.shape(
                Identifier.of("sunscreen", "test_menu/shape/divider"),
                Shape.line(Vec2i.zero(), Vec2i.of(360, 0), 2),
                Color.of(90, 90, 120)
            ).position(Position.fixed(Vec2i.of(14, 292)))
        ).element(
            Elements.image(
                Identifier.of("sunscreen", "test_menu/image/swatches"),
                Canvas.empty(Vec2i.of(120, 72)).fill(
                    Vec2i.zero(),
                    Vec2i.of(120, 72),
                    Color.of(107, 3, 244)
                )
            ).position(Position.fixed(Vec2i.of(14, 302)))
        ).element(
            Elements.comparison(
                Identifier.of("sunscreen", "test_menu/image/comparison"),
                Canvas.resource("normal.png").replace(Color.of(205, 104, 61), Color.of(255, 0, 0)),
                Canvas.resource("inverted.png")
            )
                .size(Size.fixed(Vec2i.of(160, 134)))
                .position(Position.fixed(Vec2i.of(150, 302)))
        ).element(
            Elements.button(Identifier.of("sunscreen", "test_menu/button/move_probe"))
                .position(Position.fixed(Vec2i.of(330, 302)))
                .size(Size.fixed(Vec2i.of(140, 22)))
                .listen()
                .mouse(event ->
                    SunscreenLibrary.library().logger().trace(
                        "Test harness move_probe cursor={} left={}",
                        event.context().position(),
                        event.context().leftPressed()
                    ))
                .back()
        ).element(
            Elements.button(Identifier.of("sunscreen", "test_menu/button/disabled"))
                .position(Position.fixed(Vec2i.of(330, 332)))
                .size(Size.fixed(Vec2i.of(140, 22)))
                .disable()
        );
    }

    private static void logClick(@NotNull String role, @NotNull UserClickElementEvent<?> event) {
        SunscreenLibrary.library().logger().info(
            "Test harness button [{}] click at relative={}",
            role,
            event.coords()
        );
    }

}
