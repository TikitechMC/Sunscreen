package me.combimagnetron.sunscreen;

import me.combimagnetron.passport.logic.state.MutableState;
import me.combimagnetron.passport.logic.state.State;
import me.combimagnetron.sunscreen.neo.MenuTemplate;
import me.combimagnetron.sunscreen.neo.MenuRoot;
import me.combimagnetron.sunscreen.neo.element.Elements;
import me.combimagnetron.sunscreen.neo.element.impl.ButtonElement;
import me.combimagnetron.sunscreen.neo.element.impl.text.TextBoxElement;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.graphic.NineSlice;
import me.combimagnetron.sunscreen.neo.graphic.modifier.GraphicModifiers;
import me.combimagnetron.sunscreen.neo.graphic.modifier.ModifierContext;
import me.combimagnetron.sunscreen.neo.graphic.shape.Shape;
import me.combimagnetron.sunscreen.neo.graphic.text.Text;
import me.combimagnetron.sunscreen.neo.input.keybind.Keybind;
import me.combimagnetron.sunscreen.neo.layout.Layout;
import me.combimagnetron.sunscreen.neo.page.Page;
import me.combimagnetron.sunscreen.neo.property.Padding;
import me.combimagnetron.sunscreen.neo.property.Position;
import me.combimagnetron.sunscreen.neo.property.RelativeMeasure;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.selector.Selector;
import me.combimagnetron.sunscreen.neo.selector.filter.Filter;
import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.theme.ModernTheme;
import me.combimagnetron.sunscreen.neo.theme.decorator.ThemeDecorator;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class TestModernTemplate implements MenuTemplate {

    @Override
    public @NotNull Identifier identifier() {
        return null;
    }

    @Override
    public void build(@NotNull MenuRoot root) {
        //Defining an Element
        MutableState<Text> changingText = State.mutable(Text.basic("Hello!"));
        root.element(Elements.label(Identifier.of("test_modern", "element/label"), changingText)
        ).theme(
                ModernTheme.theme(Identifier.of("test_modern", "theme/test_theme"))
                        .decorator(ThemeDecorator.nineSlice(ButtonElement.class, NineSlice.nineSlice(Canvas.file(Path.of("assets/button_nineslice.png")))))
        ).page(
                Page.page(
                        Identifier.of("test_modern", "page/test_page")
                ).element(
                        Layout.flow(
                                Identifier.of("test_modern", "page/test_page/layout/test_flex"),
                                Elements.image(Identifier.of("test_modern", "page/test_page/element/image"), Canvas.url(""))
                        )
                )
        );
        //Building an automatically flowing layout (FlowLayout) with the earlier defined element and a new one
        root.element(
                Layout.flow(
                        Identifier.of("test_modern", "layout/test_flex"),
                        //Selector.filtered(Filter.identifiable(Identifier.Namespace.of("test_modern"))),
                        Elements.image(Identifier.of("test_image"), Canvas.empty(Vec2i.of(100, 200))),
                        Elements.button(Identifier.of("test_button")).listen().mouse(event -> event.context().leftPressed()).back(),
                        Elements.textBox(Identifier.split("test:textbox"))
                                .size(Size.fit())
                                .padding(Padding.relative(RelativeMeasure.vec4i()
                                        .up().percentage(20).pixel(6).back()
                                        .down().pixel(6).back()
                                        .left().percentage(25.6).back()
                                        .right().pixel(3).back())
                                )
        ));

        //Conventional way
        TextBoxElement textBoxElement = Elements.textBox(Identifier.of("test_modern", "element/textbox_2"));

        //Making a new canvas and applying a GraphicModifier
        Canvas canvas = Canvas.empty(Vec2i.of(100, 300));

        canvas.modifier(
                GraphicModifiers.mask(
                        Shape.rectangle(Vec2i.of(16, 16)),
                        ModifierContext.of(Position.fixed(Vec2i.zero()))
                )
        );

        //Registering a keybind
        root.keybind(Keybind.of(Keybind.NamedKey.S, Keybind.NamedModifier.CTRL).listen().pressed(keybindPressedEvent -> {

        }).back());

        //Properties (Size, Position, Margin etc.) can be either fixed or relative
        Position fixed = Position.fixed(Vec2i.of(0, 0));
        Position relative = Position.relative(RelativeMeasure.vec2i().x().percentage(50).back().y().pixel(2).back());
    }

}
