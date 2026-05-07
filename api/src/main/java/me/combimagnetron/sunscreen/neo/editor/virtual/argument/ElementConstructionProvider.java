package me.combimagnetron.sunscreen.neo.editor.virtual.argument;

import me.combimagnetron.passport.internal.registry.Registry;
import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.editor.virtual.argument.impl.*;
import me.combimagnetron.sunscreen.neo.element.Elements;
import me.combimagnetron.sunscreen.neo.element.ModernElement;
import me.combimagnetron.sunscreen.neo.element.impl.ButtonElement;
import me.combimagnetron.sunscreen.neo.element.impl.DropdownElement;
import me.combimagnetron.sunscreen.neo.element.impl.SelectorElement;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.graphic.text.Text;
import me.combimagnetron.sunscreen.neo.layout.Layout;
import me.combimagnetron.sunscreen.neo.property.Decorator;
import me.combimagnetron.sunscreen.neo.property.Position;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.registry.Registries;
import me.combimagnetron.sunscreen.neo.theme.decorator.Target;
import me.combimagnetron.sunscreen.util.IdentifierHolder;
import org.jetbrains.annotations.NotNull;

public interface ElementConstructionProvider<M extends ModernElement<M, Canvas>> extends IdentifierHolder {

    ElementConstructionProvider<ButtonElement> BUTTON_ELEMENT_ELEMENT_CONSTRUCTION_PROVIDER = new ElementConstructionProvider<>() {

        @Override
        public @NotNull Identifier identifier() {
            return Identifier.of("constructor", "button_element");
        }

        @Override
        public @NotNull ButtonElement construct(@NotNull Layout<?> layout, @NotNull Argument<?>... arguments) {
            IdentifierArgument identifierArgument = orError(IdentifierArgument.class, arguments[0]);
            ButtonElement buttonElement = Elements.button(identifierArgument.decode(layout)).size(Size.fixed(Vec2i.of(75, 22)));
            if (arguments.length == 1) return buttonElement;
            TextArgument textArgument = orError(TextArgument.class, arguments[1]);
            buttonElement.text(textArgument.decode(layout));
            if (arguments.length == 2) return buttonElement;
            Vec2iArgument vec2iArgument = orError(Vec2iArgument.class, arguments[2]);
            buttonElement.textPosition(vec2iArgument.decode(layout));
            return buttonElement;
        }

        @Override
        public @NotNull ArgumentInfo arguments() {
            return new ArgumentInfo(3, new ArgumentInfo.TypeInfo(IdentifierArgument.class, true, "Name"), new ArgumentInfo.TypeInfo(TextArgument.class, false, "Text (Optional)"), new ArgumentInfo.TypeInfo(Vec2iArgument.class, false, "Text Position (Optional"));
        }

        @Override
        public @NotNull Preview<ButtonElement> preview() {
            return identifier -> Elements.button(identifier).size(Size.fixed(Vec2i.of(75, 22))).position(Position.fixed(Vec2i.of(22, 19)));
        }

        @Override
        public @NotNull Class<ButtonElement> type() {
            return ButtonElement.class;
        }

    };

    ElementConstructionProvider<DropdownElement> DROPDOWN_ELEMENT_ELEMENT_CONSTRUCTION_PROVIDER = new ElementConstructionProvider<>() {

        @Override
        public @NotNull Identifier identifier() {
            return Identifier.of("constructor", "dropdown_element");
        }

        @Override
        public @NotNull DropdownElement construct(@NotNull Layout<?> layout, @NotNull Argument<?>... arguments) {
            IdentifierArgument identifierArgument = orError(IdentifierArgument.class, arguments[0]);
            IntegerArgument integerArgument = orError(IntegerArgument.class, arguments[1]);
            DropdownElement dropdownElement = new DropdownElement(identifierArgument.decode(layout), integerArgument.decode(layout));
            MultiEntryArgument multiEntryArgument = orError(MultiEntryArgument.class, arguments[2]);
            for (Text entry : multiEntryArgument.decode(layout)) {
                System.out.println(entry);
                dropdownElement.entry(entry);
            }
            dropdownElement.unfolded(true);
            dropdownElement.size(Size.fixed(Vec2i.of(70, 50)));
            dropdownElement.decorator(Decorator.decorator(Target.typed(SelectorElement.class)));
            return dropdownElement;
        }

        @Override
        public @NotNull ArgumentInfo arguments() {
            return new ArgumentInfo(3, new ArgumentInfo.TypeInfo(IdentifierArgument.class, true, "Name"), new ArgumentInfo.TypeInfo(IntegerArgument.class, true, "Height"), new ArgumentInfo.TypeInfo(MultiEntryArgument.class, true, "Entries"));
        }

        @Override
        public @NotNull Preview<DropdownElement> preview() {
            return identifier -> new DropdownElement(
                identifier,
                11
            ).unfolded(true).select(1).entry(Text.vanilla("Wow Entry")).entry(Text.vanilla("Damn Bro")).size(Size.fixed(Vec2i.of(70, 50))).position(Position.fixed(Vec2i.of(24, 12))).decorator(Decorator.decorator(Target.typed(SelectorElement.class)));
        }

        @Override
        public @NotNull Class<DropdownElement> type() {
            return DropdownElement.class;
        }

    };

    ElementConstructionProvider<SelectorElement> SELECTOR_ELEMENT_ELEMENT_CONSTRUCTION_PROVIDER = new ElementConstructionProvider<>() {

        @Override
        public @NotNull Identifier identifier() {
            return Identifier.of("constructor", "selector_element");
        }

        @Override
        public @NotNull SelectorElement construct(@NotNull Layout<?> layout, @NotNull Argument<?>... arguments) {
            return null;
        }

        @Override
        public @NotNull ArgumentInfo arguments() {
            return new ArgumentInfo(3, new ArgumentInfo.TypeInfo(IdentifierArgument.class, true, "Name"), new ArgumentInfo.TypeInfo(IntegerArgument.class, true, "Height"), new ArgumentInfo.TypeInfo(MultiEntryArgument.class, true, "Entries"));
        }

        @Override
        public @NotNull Preview<SelectorElement> preview() {
            return identifier -> new SelectorElement(identifier, 11).entry(Text.vanilla("First")).entry(Text.vanilla("Second")).size(Size.fixed(Vec2i.of(70, 11))).position(Position.fixed(Vec2i.of(25, 20)));
        }

        @Override
        public @NotNull Class<SelectorElement> type() {
            return SelectorElement.class;
        }

    };
    ElementConstructionProvider<?>[] PROVIDERS = new ElementConstructionProvider[]{BUTTON_ELEMENT_ELEMENT_CONSTRUCTION_PROVIDER, DROPDOWN_ELEMENT_ELEMENT_CONSTRUCTION_PROVIDER, SELECTOR_ELEMENT_ELEMENT_CONSTRUCTION_PROVIDER};

    @NotNull M construct(@NotNull Layout<?> layout, @NotNull Argument<?>... arguments);

    @NotNull ArgumentInfo arguments();

    @NotNull Preview<M> preview();

    @NotNull Class<M> type();

    interface Preview<M extends ModernElement<M, Canvas>> {

        @NotNull M base(@NotNull Identifier identifier);

    }

    static <T extends Argument<?>> T orError(@NotNull Class<T> type, @NotNull Argument<?> argument) {
        if (argument.getClass() != type) throw new RuntimeException("Wrong argument type given");
        return (T) argument;
    }

    static void defaults() {
        Registry<ElementConstructionProvider<?>, Identifier> elementConstructionProviderRegistry = Registries.advanced().constructionProviders();
        for (ElementConstructionProvider<?> provider : PROVIDERS) {
            Registries.register(elementConstructionProviderRegistry, provider);
        }
    }

}
