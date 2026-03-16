package me.combimagnetron.sunscreen.example;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;import me.combimagnetron.sunscreen.neo.MenuRoot;
import me.combimagnetron.sunscreen.neo.MenuTemplate;
import me.combimagnetron.sunscreen.neo.element.Elements;
import me.combimagnetron.sunscreen.neo.element.impl.ButtonElement;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.graphic.color.Color;import me.combimagnetron.sunscreen.neo.graphic.text.Text;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.color.TextColor;import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.font.FontProperties;
import me.combimagnetron.sunscreen.neo.property.Position;
import me.combimagnetron.sunscreen.neo.property.RelativeMeasure;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.registry.Registries;
import me.combimagnetron.sunscreen.neo.theme.ModernTheme;
import me.combimagnetron.sunscreen.neo.theme.decorator.Target;
import me.combimagnetron.sunscreen.neo.theme.decorator.ThemeDecorator;
import org.jetbrains.annotations.NotNull;

public class BiddingHouseMenuTemplate implements MenuTemplate {
    private static final Identifier IDENTIFIER = Identifier.of("combimagnetron", "menu/example/bidding_house");

    @Override
    public @NotNull Identifier identifier() {
        return IDENTIFIER;
    }

    @Override
    public void build(@NotNull MenuRoot root) {
        root.theme(
            ModernTheme.theme(Identifier.of("combimagnetron", "theme/example/bidding_house"))
                .decorator(
                    ThemeDecorator.stated(Target.typed(ButtonElement.class))
                        .standard(Canvas.resource("default.png"))
                        .hovered(Canvas.resource("hovered.png"))
                        .clicked(Canvas.resource("clicked.png"))
                )
        );
        root.element(
            Elements.image(
                Identifier.of("combimagnetron", "element/example/bidding_house/overlay"),
                Canvas.empty(Vec2i.of(800, 450)).fill(Vec2i.zero(), Vec2i.of(800, 450), Color.of(0xC0101010, 0xC0101010, 0xD0101010, 0xD0101010))
            ).position(Position.nil())
        ).element(
            Elements.image(
                Identifier.of("combimagnetron", "element/example/bidding_house/background_image"),
                Canvas.resource("background.png")
            ).position(Position.relative(RelativeMeasure.vec2i().x().percentage(50).pixel(-134).back().y().percentage(50).pixel(-97).back()))
        ).element(
            Elements.button(
                Identifier.of("combimagnetron", "element/example/bidding_house/manage_button"),
                Text.basic("Manage").font(Registries.fonts().get(Identifier.of("sunscreen", "font/sunburned"))).color(TextColor.color(Color.of(195, 36, 84))).fontProperties(FontProperties.properties().baseline(-6)),
                Vec2i.of(7, 8)
            ).size(Size.fixed(Vec2i.of(45, 21))).position(Position.relative(RelativeMeasure.vec2i().x().percentage(50).pixel(-134).pixel(25).back().y().percentage(50).pixel(-97).pixel(151).back()))
        );
    }
}
