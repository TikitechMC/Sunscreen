package me.combimagnetron.sunscreen.neo.editor.template;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.ActiveMenu;
import me.combimagnetron.sunscreen.neo.MenuTemplate;
import me.combimagnetron.sunscreen.neo.MenuRoot;
import me.combimagnetron.sunscreen.neo.editor.EditorController;
import me.combimagnetron.sunscreen.neo.editor.element.*;
import me.combimagnetron.sunscreen.neo.editor.virtual.VirtualElement;
import me.combimagnetron.sunscreen.neo.editor.virtual.VirtualPage;
import me.combimagnetron.sunscreen.neo.editor.virtual.argument.Argument;
import me.combimagnetron.sunscreen.neo.editor.virtual.argument.ElementConstructionProvider;
import me.combimagnetron.sunscreen.neo.editor.widget.EditorWidget;
import me.combimagnetron.sunscreen.neo.editor.widget.EditorWidgetTab;
import me.combimagnetron.sunscreen.neo.element.Elements;
import me.combimagnetron.sunscreen.neo.element.ModernElement;
import me.combimagnetron.sunscreen.neo.element.impl.ButtonElement;
import me.combimagnetron.sunscreen.neo.element.impl.SelectorElement;
import me.combimagnetron.sunscreen.neo.element.impl.text.TextFieldElement;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.graphic.NineSlice;
import me.combimagnetron.sunscreen.neo.graphic.color.Color;
import me.combimagnetron.sunscreen.neo.graphic.shape.Shape;
import me.combimagnetron.sunscreen.neo.graphic.text.Text;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.color.TextColor;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.font.FontProperties;
import me.combimagnetron.sunscreen.neo.input.context.TextInputContext;
import me.combimagnetron.sunscreen.neo.layout.Layout;
import me.combimagnetron.sunscreen.neo.property.*;
import me.combimagnetron.sunscreen.neo.registry.Registries;
import me.combimagnetron.sunscreen.neo.theme.ModernTheme;
import me.combimagnetron.sunscreen.neo.theme.color.ColorSchemes;
import me.combimagnetron.sunscreen.neo.theme.decorator.Target;
import me.combimagnetron.sunscreen.neo.theme.decorator.ThemeDecorator;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import static me.combimagnetron.sunscreen.neo.graphic.text.Text.vanilla;

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
                .disabled(Canvas.empty(Vec2i.of(11, 11)).fill(Position.nil(), Size.fixed(Vec2i.of(11, 11)), Color.of(13, 13, 13)))
        ).decorator(
            ThemeDecorator.stated(
                    Target.typed(SelectorElement.class)
                )
                .standard(Canvas.resource("editor_assets/e_default.png"))
                .hovered(Canvas.resource("editor_assets/e_hover.png"))
                .clicked(Canvas.resource("editor_assets/e_click.png"))
        ).decorator(
            ThemeDecorator.stated(
                    Target.identifier(Identifier.of("sunscreen", "internal/editor/theme/decorator/button_confirm"))
                )
                .standard(Canvas.empty(Vec2i.of(11, 11)).fill(Position.nil(), Size.fixed(Vec2i.of(11, 11)), Color.of(35, 144, 99)))
                .hovered(Canvas.empty(Vec2i.of(11, 11)).fill(Position.nil(), Size.fixed(Vec2i.of(11, 11)), Color.of(30, 188, 115)))
                .clicked(Canvas.empty(Vec2i.of(11, 11)).fill(Position.nil(), Size.fixed(Vec2i.of(11, 11)), Color.of(22, 90, 76)))
                .disabled(Canvas.empty(Vec2i.of(11, 11)).fill(Position.nil(), Size.fixed(Vec2i.of(11, 11)), Color.of(55, 78, 74)))
        ).decorator(
            ThemeDecorator.nineSlice(
                Target.typed(EditorNameElement.class),
                NineSlice.nineSlice(Canvas.empty(Vec2i.of(9, 9)).fill(Vec2i.zero(), Vec2i.of(9, 9), Color.of(27, 27, 27)))
            )
        );

    private final EditorController controller;

    public EditorMenuTemplate(@NotNull EditorController controller) {
        this.controller = controller;
    }

    @Override
    public @NotNull Identifier identifier() {
        return IDENTIFIER;
    }

    @Override
    public void build(@NotNull MenuRoot root) {
        root.theme(
            EDITOR_THEME
        );
        root.element(
            Elements.shape(Identifier.of("right_background"), Shape.rectangle(Vec2i.of(139, 450)), Color.of(13, 13, 13)).position(Position.nil())
        ).element(
            Elements.shape(Identifier.of("top_background"), Shape.rectangle(Vec2i.of(518, 15)), Color.of(13, 13, 13)).position(Position.fixed(Vec2i.of(139, 0)))
        ).element(
            Elements.shape(Identifier.of("bottom_background"), Shape.rectangle(Vec2i.of(518, 3)), Color.of(13, 13, 13)).position(Position.fixed(Vec2i.of(139, 447)))
        ).element(
            Elements.shape(Identifier.of("left_background"), Shape.rectangle(Vec2i.of(143, 450)), Color.of(13, 13, 13)).position(Position.fixed(Vec2i.of(657, 0)))
        ).element(
            EditorElements.preview(Identifier.of("preview"), controller).position(Position.fixed(Vec2i.of(139, 15))).size(Size.fixed(Vec2i.of(518, 432))).scale(Scale.fixed(1.561)).z(Z.z(0.1f))
        ).element(
            EditorWidget.widget(
                Identifier.of("top_right")
            ).tab(
                    EditorWidgetTab.tab(
                            vanilla("Layout"),
                            Position.fixed(Vec2i.of(658, 15))
                        )
                        .add(Elements.shape(Identifier.of("top_right/padding_margin/label_background"), Shape.rectangle(Vec2i.of(137, 10)), Color.of(27, 27, 27)).position(Position.fixed(Vec2i.of(2, 2))))
                        .add(Elements.shape(Identifier.of("top_right/padding_margin/value_background"), Shape.rectangle(Vec2i.of(137, 38)), Color.of(39, 39, 39)).position(Position.fixed(Vec2i.of(2, 12))))
                        .add(Elements.label(Identifier.of("top_right/padding_margin/label"), vanilla("Padding & Margin").color(TextColor.color(Color.of(180, 180, 180)))).position(Position.fixed(Vec2i.of(3, 3))))
                        .add(EditorElements.multiValue(Identifier.of("top_right/padding_margin/value")).position(Position.fixed(Vec2i.of(2, 12))))
                        .add(Elements.shape(Identifier.of("top_right/size/label_background"), Shape.rectangle(Vec2i.of(137, 10)), Color.of(27, 27, 27)).position(Position.fixed(Vec2i.of(2, 51))))
                        .add(Elements.shape(Identifier.of("top_right/size/value_background"), Shape.rectangle(Vec2i.of(137, 38)), Color.of(39, 39, 39)).position(Position.fixed(Vec2i.of(2, 61))))
                        .add(Elements.label(Identifier.of("top_right/size/label"), vanilla("Size").color(TextColor.color(Color.of(180, 180, 180)))).position(Position.fixed(Vec2i.of(3, 52))))
                        .add(EditorElements.value(Identifier.of("top_right/size/value")).position(Position.fixed(Vec2i.of(2, 61))))
                        .add(Elements.shape(Identifier.of("top_right/position/label_background"), Shape.rectangle(Vec2i.of(137, 10)), Color.of(27, 27, 27)).position(Position.fixed(Vec2i.of(2, 100))))
                        .add(Elements.shape(Identifier.of("top_right/position/value_background"), Shape.rectangle(Vec2i.of(137, 38)), Color.of(39, 39, 39)).position(Position.fixed(Vec2i.of(2, 110))))
                        .add(Elements.label(Identifier.of("top_right/position/label"), vanilla("Position").color(TextColor.color(Color.of(180, 180, 180)))).position(Position.fixed(Vec2i.of(3, 100))))
                        .add(EditorElements.value(Identifier.of("top_right/position/value")).position(Position.fixed(Vec2i.of(2, 110))))
                        .add(Elements.image(Identifier.of("top_right/layout/placeholder"), Canvas.resource("editor_assets/test2.png")).position(Position.fixed(Vec2i.of(2, 149))))
                        .position(Position.fixed(Vec2i.of(0, 14))).size(Size.fixed(Vec2i.of(141, 187)))
            ).tab(
                    EditorWidgetTab.tab(
                            vanilla("Design"),
                            Position.fixed(Vec2i.of(658, 15))
                        )
                        .position(Position.fixed(Vec2i.of(0, 14))).size(Size.fixed(Vec2i.of(141, 187)))
            ).tab(
                    EditorWidgetTab.tab(
                        vanilla("Actions"),
                        Position.fixed(Vec2i.of(658, 15))
                    )
                .position(Position.fixed(Vec2i.of(0, 14))).size(Size.fixed(Vec2i.of(141, 187))))
            .size(Size.fixed(Vec2i.of(141, 201))).position(Position.fixed(Vec2i.of(658, 15)))
        ).element(
            EditorWidget.widget(
                Identifier.of("mid_right")
            ).tab(
                EditorWidgetTab.tab(
                        vanilla("Color"),
                        Position.fixed(Vec2i.of(658, 217))
                    )
                    .position(Position.fixed(Vec2i.of(0, 14)))
                    .size(Size.fixed(Vec2i.of(141, 102)))
            ).tab(
                EditorWidgetTab.tab(
                        vanilla("Palette"),
                        Position.fixed(Vec2i.of(658, 217))
                    )
                    .position(Position.fixed(Vec2i.of(0, 14)))
                    .size(Size.fixed(Vec2i.of(141, 102)))
            ).size(Size.fixed(Vec2i.of(141, 116))).position(Position.fixed(Vec2i.of(658, 217)))
        ).element(
            EditorWidget.widget(
                Identifier.of("low_right")
            ).tab(
                EditorWidgetTab.tab(
                        vanilla("Navigation"),
                        Position.fixed(Vec2i.of(658, 334))
                    )
                    .position(Position.fixed(Vec2i.of(0, 14)))
                    .size(Size.fixed(Vec2i.of(141, 101)))
            ).tab(
                EditorWidgetTab.tab(
                        vanilla("Property"),
                        Position.fixed(Vec2i.of(658, 334))
                    )
                    .position(Position.fixed(Vec2i.of(0, 14))).
                    size(Size.fixed(Vec2i.of(141, 101)))
                ).size(Size.fixed(Vec2i.of(141, 115))).position(Position.fixed(Vec2i.of(658, 334)))
        ).element(
            EditorWidget.widget(
                Identifier.of("top_left")
            ).tab(
                EditorWidgetTab.tab(
                        vanilla("Pages"),
                        Position.fixed(Vec2i.of(15, 15))
                    )
                    .add(EditorElements.layerOverview(Identifier.of("top_left/layer_overview"), controller).position(Position.nil()).size(Size.fixed(Vec2i.of(123, 136))))
                    .add(Elements.button(Identifier.of("top_left/layer_button"), Text.adventure(Component.text("aaaa", net.kyori.adventure.text.format.TextColor.color(125, 240, 3)).font(Key.key("sunscreen", "font/minecraft"))), Vec2i.of(7, 2)).size(Size.fixed(Vec2i.of(59, 11))).position(Position.fixed(Vec2i.of(2, 138))).listen().click(event -> {
                        if (!(event.element() instanceof ButtonElement)) return;
                        if (!event.element().identifier().key().string().equals("top_left/layer_button")) return;
                        ActiveMenu menu = event.menu();
                        menu.element(Identifier.of("new_layer/wizard")).visibility(Visibility.visible());
                    }).back())
                    .add(Elements.button(Identifier.of("top_left/element_button"), vanilla("New Element"), Vec2i.of(1, 2)).size(Size.fixed(Vec2i.of(59, 11))).position(Position.fixed(Vec2i.of(62, 138))))
                    .position(Position.fixed(Vec2i.of(0, 14))).size(Size.fixed(Vec2i.of(123, 151)))
            ).size(Size.fixed(Vec2i.of(123, 165))).position(Position.fixed(Vec2i.of(15, 15)))
        ).element(
            EditorWidget.widget(
                Identifier.of("bottom_left")
            ).tab(
                EditorWidgetTab.tab(
                        vanilla("Elements"),
                        Position.fixed(Vec2i.of(15, 181))
                    ).position(Position.fixed(Vec2i.of(0, 14))).size(Size.fixed(Vec2i.of(123, 253)))
                    .add(Elements.button(Identifier.of("bottom_left/filter_button"), vanilla("Filters"), Vec2i.of(1, 1)).position(Position.fixed(Vec2i.of(2, 2))).size(Size.fixed(Vec2i.of(34, 9))))
                    .add(new SelectorElement(Identifier.of("bottom_left/origin_selector"), 11).entry(vanilla("Local")).entry(vanilla("Asset Lib")).position(Position.fixed(Vec2i.of(40, 1))).size(Size.fixed(Vec2i.of(82, 11))))
                    .add(Elements.image(Identifier.of("bottom_left/line_i_guess"), Canvas.empty(Vec2i.of(1, 11)).fill(Vec2i.zero(), Vec2i.of(1, 11), Color.of(27, 27, 27))).position(Position.fixed(Vec2i.of(37, 1))))
                    .add(Elements.image(Identifier.of("bottom_left/line_i_guess_one_more"), Canvas.empty(Vec2i.of(121, 1)).fill(Vec2i.zero(), Vec2i.of(121, 1), Color.of(27, 27, 27))).position(Position.fixed(Vec2i.of(1, 12))))
                    .add(EditorElements.elementLibrary(Identifier.of("bottom_left/element_library"), controller).size(Size.fixed(Vec2i.of(123, 241))).position(Position.fixed(Vec2i.of(0, 13))))
            ).size(Size.fixed(Vec2i.of(123, 267))).position(Position.fixed(Vec2i.of(15, 181))))
        .element(
            EditorElements.frame(Identifier.of("top_frame")).position(Position.fixed(Vec2i.of(139, 1))).size(Size.fixed(Vec2i.of(660, 13)))
        ).element(
            EditorElements.frame(Identifier.of("actions_frame")).position(Position.fixed(Vec2i.of(37, 1))).size(Size.fixed(Vec2i.of(101, 13)))
        ).element(
            Elements.image(Identifier.of("viz_logo"), Canvas.resource("editor_assets/viz_logo.png")).position(Position.fixed(Vec2i.of(1, 1)))
        ).element(
                Layout.group(
                    Identifier.of("new_layer/wizard"),
                    EditorElements.frame(
                        Identifier.of("new_layer/wizard/frame")
                    ).size(Size.fixed(Vec2i.of(198, 261))).position(Position.fixed(Vec2i.of(0, 9))),
                    Elements.image(
                        Identifier.of("new_layer/wizard/frame_extension"),
                        Canvas.empty(Vec2i.of(198, 9)).fill(Vec2i.zero(), Vec2i.of(198, 9), Color.of(27, 27, 27))
                    ).position(Position.nil()),
                    Elements.label(
                        Identifier.of("new_layer/wizard/label"),
                        Text.basic("New Page").font(Registries.fonts().get(Identifier.of("sunscreen", "font/minecraft"))).fontProperties(FontProperties.properties().baseline(-2))
                    ).size(Size.fixed(Vec2i.of(143, 20))).position(Position.fixed(Vec2i.of(1, 1))),
                    Elements.label(
                        Identifier.of("new_layer/wizard/display_name_label"),
                        Text.basic("Display name").font(Registries.fonts().get(Identifier.of("sunscreen", "font/minecraft"))).color(TextColor.color(Color.of(180, 180, 180))).fontProperties(FontProperties.properties().baseline(-2))
                    ).size(Size.fixed(Vec2i.of(143, 20))).position(Position.fixed(Vec2i.of(2, 11))),
                    Elements.label(
                        Identifier.of("new_layer/wizard/identifier_label"),
                        Text.basic("Identifier").font(Registries.fonts().get(Identifier.of("sunscreen", "font/minecraft"))).color(TextColor.color(Color.of(180, 180, 180))).fontProperties(FontProperties.properties().baseline(-2))
                    ).size(Size.fixed(Vec2i.of(143, 20))).position(Position.fixed(Vec2i.of(2, 32))),
                    EditorElements.pageNameElement(
                        Identifier.of("new_layer/wizard/name_element")
                    ).size(Size.fixed(Vec2i.of(193, 10))).position(Position.fixed(Vec2i.of(2, 20))).decorator(Decorator.decorator(Target.typed(EditorNameElement.class))),
                    Elements.button(
                        Identifier.of("new_layer/wizard/confirm_button"),
                        Text.basic("Create").font(Registries.fonts().get(Identifier.of("sunscreen", "font/minecraft"))).fontProperties(FontProperties.properties().baseline(-2)),
                        Vec2i.of(31, 3)
                    ).disable().listen().click(event -> {
                        if (!event.element().identifier().key().string().equals("new_layer/wizard/confirm_button")) return;
                        Layout<?> layout = (Layout<?>) event.menu().element(Identifier.of("new_layer/wizard"));
                        PageNameElement nameElement = (PageNameElement) layout.child(Identifier.of("new_layer/wizard/name_element"));
                        TextFieldElement xFieldElement = (TextFieldElement) layout.child(Identifier.of("new_layer/wizard/x_field"));
                        TextFieldElement yFieldElement = (TextFieldElement) layout.child(Identifier.of("new_layer/wizard/y_field"));
                        if (!nameElement.validate()) return;
                        if (!StringUtils.isNumeric(xFieldElement.value()) || !StringUtils.isNumeric(yFieldElement.value())) return;
                        controller.page(new VirtualPage(Vec2i.of(Integer.parseInt(xFieldElement.value()), Integer.parseInt(yFieldElement.value())), nameElement.fakeIdentifier(), nameElement.displayName(), controller));
                        nameElement.clear();
                        xFieldElement.clear();
                        yFieldElement.clear();
                        layout.visibility(Visibility.hidden());
                        event.menu().inputHandler().peek(TextInputContext.class, TextInputContext::clear, event.user());
                    }).back().size(Size.fixed(Vec2i.of(96, 14))).position(Position.fixed(Vec2i.of(100, 254))).decorator(Decorator.decorator(Target.identifier(Identifier.of("sunscreen", "internal/editor/theme/decorator/button_confirm")))),
                    Elements.button(
                        Identifier.of("new_layer/wizard/cancel_button"),
                        Text.basic("Cancel").font(Registries.fonts().get(Identifier.of("sunscreen", "font/minecraft"))).color(TextColor.color(Color.of(180, 180, 180))).fontProperties(FontProperties.properties().baseline(-2)),
                        Vec2i.of(31, 3)
                    ).listen().click(event -> {
                        if (!event.element().identifier().key().string().equals("new_layer/wizard/cancel_button")) return;
                        Layout<?> layout = (Layout<?>) event.menu().element(Identifier.of("new_layer/wizard"));
                        layout.visibility(Visibility.hidden());
                    }).back().size(Size.fixed(Vec2i.of(96, 14))).position(Position.fixed(Vec2i.of(2, 254))),
                    Elements.textField(
                        Identifier.of("new_layer/wizard/x_field")
                    ).listen().updated(event -> {
                        Layout<?> layout = (Layout<?>) event.menu().element(Identifier.of("new_layer/wizard"));
                        TextFieldElement textFieldElement = (TextFieldElement) layout.child(Identifier.of("new_layer/wizard/x_field"));
                        if (textFieldElement == null) {
                            return;
                        }
                        if (!textFieldElement.selected()) return;
                        ButtonElement buttonElement = (ButtonElement) layout.child(Identifier.of("new_layer/wizard/confirm_button"));
                        if (!textFieldElement.value().isEmpty() && StringUtils.isNumeric(textFieldElement.value())) {
                            buttonElement.enable();
                            return;
                        }
                        buttonElement.disable();
                    }).back().decorator(Decorator.decorator(Target.typed(EditorNameElement.class))).size(Size.fixed(Vec2i.of(94, 11))).position(Position.fixed(Vec2i.of(2, 61))).decorator(Decorator.decorator(Target.typed(SelectorElement.class))),
                    Elements.label(
                        Identifier.of("new_layer/wizard/size_cross"),
                        Text.small("x")
                    ).size(Size.fixed(Vec2i.of(143, 20))).position(Position.fixed(Vec2i.of(97, 65))),
                    Elements.textField(
                        Identifier.of("new_layer/wizard/y_field")
                    ).listen().updated(event -> {
                        Layout<?> layout = (Layout<?>) event.menu().element(Identifier.of("new_layer/wizard"));
                        TextFieldElement textFieldElement = (TextFieldElement) layout.child(Identifier.of("new_layer/wizard/y_field"));
                        if (textFieldElement == null) {
                            return;
                        }
                        if (!textFieldElement.selected()) return;
                        ButtonElement buttonElement = (ButtonElement) layout.child(Identifier.of("new_layer/wizard/confirm_button"));
                        if (!textFieldElement.value().isEmpty() && StringUtils.isNumeric(textFieldElement.value())) {
                            buttonElement.enable();
                            return;
                        }
                        buttonElement.disable();
                    }).back().decorator(Decorator.decorator(Target.typed(EditorNameElement.class))).size(Size.fixed(Vec2i.of(94, 11))).position(Position.fixed(Vec2i.of(102, 61))).decorator(Decorator.decorator(Target.typed(SelectorElement.class))),
                    Elements.label(
                        Identifier.of("new_layer/wizard/size_label"),
                        Text.basic("Size").font(Registries.fonts().get(Identifier.of("sunscreen", "font/minecraft"))).color(TextColor.color(Color.of(180, 180, 180))).fontProperties(FontProperties.properties().baseline(-2))
                    ).size(Size.fixed(Vec2i.of(143, 20))).position(Position.fixed(Vec2i.of(2, 53)))
                ).size(Size.fixed(Vec2i.of(400, 400))).position(Position.fixed(Vec2i.of(301, 105))).visibility(Visibility.hidden())
            ).element(
                Layout.group(
                    Identifier.of("new_element/wizard"),
                    EditorElements.frame(
                        Identifier.of("new_element/wizard/frame")
                    ).size(Size.fixed(Vec2i.of(198, 261))).position(Position.fixed(Vec2i.of(0, 9))),
                    Elements.image(
                        Identifier.of("new_element/wizard/frame_extension"),
                        Canvas.empty(Vec2i.of(198, 9)).fill(Vec2i.zero(), Vec2i.of(198, 9), Color.of(27, 27, 27))
                    ).position(Position.nil()),
                    Elements.label(
                        Identifier.of("new_element/wizard/label"),
                        Text.basic("New Element").font(Registries.fonts().get(Identifier.of("sunscreen", "font/minecraft"))).fontProperties(FontProperties.properties().baseline(-2))
                    ).size(Size.fixed(Vec2i.of(143, 20))).position(Position.fixed(Vec2i.of(1, 1))),
                    Elements.button(
                        Identifier.of("new_element/wizard/confirm_button"),
                        Text.basic("Create").font(Registries.fonts().get(Identifier.of("sunscreen", "font/minecraft"))).fontProperties(FontProperties.properties().baseline(-2)),
                        Vec2i.of(31, 3)
                    ).listen().click(event -> {
                        if (!event.element().identifier().key().string().equals("new_element/wizard/confirm_button")) return;
                        Layout<?> layout = (Layout<?>) event.menu().element(Identifier.of("new_element/wizard"));
                        ElementConstructionProvider<?> provider = controller.provider();
                        if (provider == null) return;
                        ModernElement<?, Canvas> element = provider.construct(layout, controller.providerArgumentMap().get(provider).toArray(new Argument[]{}));
                        controller.removeElementSetup();
                        VirtualPage page = controller.selected();
                        if (page == null) {
                            return;
                        }
                        page.page(new VirtualElement<>(Vec2i.of(20, 20), element, element.identifier()));
                    }).back().size(Size.fixed(Vec2i.of(96, 14))).position(Position.fixed(Vec2i.of(100, 254))).decorator(Decorator.decorator(Target.identifier(Identifier.of("sunscreen", "internal/editor/theme/decorator/button_confirm")))),
                    Elements.button(
                        Identifier.of("new_element/wizard/cancel_button"),
                        Text.basic("Cancel").font(Registries.fonts().get(Identifier.of("sunscreen", "font/minecraft"))).color(TextColor.color(Color.of(180, 180, 180))).fontProperties(FontProperties.properties().baseline(-2)),
                        Vec2i.of(31, 3)
                    ).listen().click(event -> {
                        if (!event.element().identifier().key().string().equals("new_element/wizard/cancel_button")) return;
                        Layout<?> layout = (Layout<?>) event.menu().element(Identifier.of("new_element/wizard"));
                        layout.visibility(Visibility.hidden());
                    }).back().size(Size.fixed(Vec2i.of(96, 14))).position(Position.fixed(Vec2i.of(2, 254)))
                ).size(Size.fixed(Vec2i.of(400, 400))).position(Position.fixed(Vec2i.of(301, 105))).visibility(Visibility.hidden())
            );
    }

}
