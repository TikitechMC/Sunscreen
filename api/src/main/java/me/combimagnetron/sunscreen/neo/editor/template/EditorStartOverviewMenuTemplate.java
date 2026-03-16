package me.combimagnetron.sunscreen.neo.editor.template;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.ActiveMenu;
import me.combimagnetron.sunscreen.neo.MenuRoot;
import me.combimagnetron.sunscreen.neo.MenuTemplate;
import me.combimagnetron.sunscreen.neo.editor.element.EditorElements;
import me.combimagnetron.sunscreen.neo.element.Elements;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.graphic.color.Color;
import me.combimagnetron.sunscreen.neo.graphic.text.Text;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.color.TextColor;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.font.FontProperties;
import me.combimagnetron.sunscreen.neo.layout.Layout;
import me.combimagnetron.sunscreen.neo.property.Position;
import me.combimagnetron.sunscreen.neo.property.RelativeMeasure;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.property.Visibility;
import me.combimagnetron.sunscreen.neo.registry.Registries;
import org.jetbrains.annotations.NotNull;

public class EditorStartOverviewMenuTemplate implements MenuTemplate {
    private final static Identifier IDENTIFIER = Identifier.of("sunscreen", "internal/editor/start");

    @Override
    public @NotNull Identifier identifier() {
        return IDENTIFIER;
    }

    @Override
    public void build(@NotNull MenuRoot root) {
        root.theme(EditorMenuTemplate.EDITOR_THEME);
        Vec2i position = Vec2i.of(160, 109);
        root.element(
            EditorElements.frame(Identifier.of("main_frame"))
                .size(Size.relative(RelativeMeasure.vec2i().x().percentage(60).back().y().percentage(45).back())).position(Position.fixed(position))
        ).element(
            Elements.image(
                Identifier.of("new_project/frame"),
                Canvas.empty(Vec2i.of(112, 72))
                    .fill(Vec2i.zero(), Vec2i.of(112, 72), Color.of(39, 39, 39))
            ).position(Position.fixed(position.add(2, 2)))
        ).element(
            Elements.image(
                Identifier.of("new_project/inner_frame"),
                Canvas.empty(Vec2i.of(110, 61))
                    .fill(Vec2i.zero(), Vec2i.of(110, 61), Color.of(13, 13, 13))
            ).position(Position.fixed(position.add(3, 12)))
        ).element(
            Elements.label(
                Identifier.of("new_project/label"),
                Text.basic("New Project").font(Registries.fonts().get(Identifier.of("sunscreen", "font/minecraft"))).fontProperties(FontProperties.properties().baseline(-2))
            ).position(Position.fixed(position.add(3)))
        ).element(
            Elements.button(
                Identifier.of("new_project/from_template_label"),
                Text.basic("From Template").font(Registries.fonts().get(Identifier.of("sunscreen", "font/minecraft"))).fontProperties(FontProperties.properties().baseline(-2)),
                Vec2i.of(20, 2)
            ).position(Position.fixed(position.add(4, 13))).size(Size.fixed(Vec2i.of(108, 12)))
        ).element(
            Elements.button(
                Identifier.of("new_project/centered_label"),
                Text.basic("Centered").font(Registries.fonts().get(Identifier.of("sunscreen", "font/minecraft"))).fontProperties(FontProperties.properties().baseline(-2)),
                Vec2i.of(31, 2)
            ).listen().click(event -> {
                ActiveMenu menu = event.menu();
                menu.element(Identifier.of("new_project/wizard")).visibility(Visibility.visible());
            }).back().position(Position.fixed(position.add(4, 26))).size(Size.fixed(Vec2i.of(108, 12)))
        ).element(
            Elements.button(
                Identifier.of("new_project/full_screen_label"),
                Text.basic("Full screen").font(Registries.fonts().get(Identifier.of("sunscreen", "font/minecraft"))).fontProperties(FontProperties.properties().baseline(-2)),
                Vec2i.of(26, 2)
            ).position(Position.fixed(position.add(4, 39))).size(Size.fixed(Vec2i.of(108, 12))).visibility(Visibility.hidden())
        ).element(
            Elements.image(
                Identifier.of("projects_label"),
                Canvas.resource("projects_label.png")
            ).position(Position.fixed(position.sub(0, 21)))
        ).element(
            Layout.group(
                Identifier.of("new_project/wizard"),
                EditorElements.frame(
                    Identifier.of("new_project/wizard/frame")
                ).size(Size.fixed(Vec2i.of(198, 261))).position(Position.fixed(Vec2i.of(0, 9))),
                Elements.image(
                    Identifier.of("new_project/wizard/frame_extension"),
                    Canvas.empty(Vec2i.of(198, 9)).fill(Vec2i.zero(), Vec2i.of(198, 9), Color.of(27, 27, 27))
                ).position(Position.nil()),
                Elements.label(
                    Identifier.of("new_project/wizard/label"),
                    Text.basic("New Project Wizard").font(Registries.fonts().get(Identifier.of("sunscreen", "font/minecraft"))).fontProperties(FontProperties.properties().baseline(-2))
                ).size(Size.fixed(Vec2i.of(143, 20))).position(Position.fixed(Vec2i.of(1, 1))),
                Elements.label(
                    Identifier.of("new_project/wizard/display_name_label"),
                    Text.basic("Display name").font(Registries.fonts().get(Identifier.of("sunscreen", "font/minecraft"))).color(TextColor.color(Color.of(180, 180, 180))).fontProperties(FontProperties.properties().baseline(-2))
                ).size(Size.fixed(Vec2i.of(143, 20))).position(Position.fixed(Vec2i.of(2, 11))),
                Elements.label(
                    Identifier.of("new_project/wizard/identifier_label"),
                    Text.basic("Identifier").font(Registries.fonts().get(Identifier.of("sunscreen", "font/minecraft"))).color(TextColor.color(Color.of(180, 180, 180))).fontProperties(FontProperties.properties().baseline(-2))
                ).size(Size.fixed(Vec2i.of(143, 20))).position(Position.fixed(Vec2i.of(2, 32))),
                EditorElements.nameElement(
                    Identifier.of("new_project/wizard/name_element")
                ).size(Size.fixed(Vec2i.of(193, 10))).position(Position.fixed(Vec2i.of(2, 20)))
            ).size(Size.fixed(Vec2i.of(400, 400))).position(Position.fixed(Vec2i.of(301, 105))).visibility(Visibility.hidden())
        );
    }

}
