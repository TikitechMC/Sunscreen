package me.combimagnetron.sunscreen.menu.editor.menu.element.sidebar;

import me.combimagnetron.sunscreen.image.Canvas;
import me.combimagnetron.sunscreen.menu.Size;
import me.combimagnetron.sunscreen.menu.editor.menu.EditorMenu;
import me.combimagnetron.sunscreen.menu.editor.menu.element.SectionElement;
import me.combimagnetron.sunscreen.element.Element;
import me.combimagnetron.sunscreen.menu.Position;
import me.combimagnetron.sunscreen.element.SimpleBufferedElement;
import me.combimagnetron.sunscreen.style.Style;
import me.combimagnetron.sunscreen.util.Identifier;
import me.combimagnetron.sunscreen.util.Vec2i;
import org.jetbrains.annotations.NotNull;

public class ElementTabElement extends SimpleBufferedElement {
    private Position position = Position.pixel(0, 0);

    public ElementTabElement(Size size, Identifier identifier, Position position) {
        super(size, identifier, position);
    }

    public static ElementTabElement of(Size size, Identifier identifier, Position position) {
        return new ElementTabElement(size, identifier, position);
    }

    @Override
    public @NotNull Canvas canvas() {
        return render();
    }

    private Canvas render() {
        Canvas result = Canvas.image(size());
        result.place(SectionElement.sectionElement(Identifier.of("_"), Position.pixel(0, 0), size()).canvas(), Vec2i.of(0, 0));
        result.fill(Vec2i.of(2, 2), Vec2i.of(size().x().pixel() - 4, 21), EditorMenu.Colors.Secondary);
        return result;
    }

    @Override
    public <T> Element<Canvas> style(Style<T> style, Position pos2D, T t) {
        return null;
    }

    @Override
    public <T> Element<Canvas> style(Style<T> style, T t) {
        return null;
    }
}
