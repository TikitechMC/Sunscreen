package me.combimagnetron.sunscreen;

import me.combimagnetron.passport.internal.entity.impl.display.Display;
import me.combimagnetron.passport.internal.entity.metadata.type.Quaternion;
import me.combimagnetron.passport.internal.entity.metadata.type.Vector3d;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.graphic.Item;
import me.combimagnetron.sunscreen.neo.protocol.type.Location;
import me.combimagnetron.sunscreen.user.SunscreenUser;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.CommandPlaceholder;
import revxrsal.commands.command.CommandActor;

@Command("testtest")
public class TestCommand {

    @CommandPlaceholder
    public void sunscreen(@NotNull CommandActor actor) {
        SunscreenUser<?> user = SunscreenLibrary.library().users().user(actor.uniqueId()).orElseThrow();
        user.open(new TestItemMenuTemplate());
//        Vec2i positionVec = Vec2i.of(460, 540);
//        Item<Object> item = Item.item(ItemStack.of(Material.MACE), Display.Transformation.transformation().scale(Vector3d.vec3(1)).rotationLeft(Quaternion.of(0, 388, 0, 340)));
//        SunscreenLibrary.library().intermediate().spawnItemDisplay(user, new Location(user.eyeLocation().x(), user.eyeLocation().y() + 2, user.eyeLocation().z()), item, positionVec);
    }

}
