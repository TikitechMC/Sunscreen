package me.combimagnetron.sunscreen;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.MenuRoot;
import me.combimagnetron.sunscreen.neo.MenuTemplate;
import me.combimagnetron.sunscreen.neo.element.Elements;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.graphic.Item;
import me.combimagnetron.sunscreen.neo.property.Position;
import me.combimagnetron.sunscreen.neo.property.Rotation;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class TestItemMenuTemplate implements MenuTemplate {

    @Override
    public @NotNull Identifier identifier() {
        return Identifier.of("sunscreen", "test/item");
    }

    @Override
    public void build(@NotNull MenuRoot root) {
        root.element(
            Elements.image(
                Identifier.of("test"),
                Item.item(ItemStack.of(Material.BROWN_BANNER))
            ).rotation(Rotation.of(96.8f, 45.4f, 78.1f)).position(Position.fixed(Vec2i.of(460, 540))));
        //root.element(Elements.image(Identifier.of("wowzers"), Canvas.resource("normal.png")).position(Position.fixed(Vec2i.of(300, 120))));
    }

}
