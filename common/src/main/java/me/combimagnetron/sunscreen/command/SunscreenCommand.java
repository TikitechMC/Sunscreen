package me.combimagnetron.sunscreen.command;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.SSCustomGames;
import me.combimagnetron.sunscreen.SunscreenLibrary;
import me.combimagnetron.sunscreen.example.BiddingHouseMenuTemplate;
import me.combimagnetron.sunscreen.neo.TestMenuTemplate;
import me.combimagnetron.sunscreen.neo.ActiveMenu;
import me.combimagnetron.sunscreen.neo.editor.EditorController;
import me.combimagnetron.sunscreen.neo.editor.template.EditorMenuTemplate;
import me.combimagnetron.sunscreen.neo.editor.template.EditorStartOverviewMenuTemplate;
import me.combimagnetron.sunscreen.neo.element.ElementLike;
import me.combimagnetron.sunscreen.neo.property.Position;
import me.combimagnetron.sunscreen.neo.session.Session;
import me.combimagnetron.sunscreen.user.SunscreenUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.CommandPlaceholder;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.command.CommandActor;

@Command("sunscreen")
public class SunscreenCommand {

    @CommandPlaceholder
    public void sunscreen(@NotNull CommandActor actor) {
        SunscreenUser<?> user = SunscreenLibrary.library().users().user(actor.uniqueId()).orElseThrow();
        TextComponent.Builder builder = Component.text();
        builder.append(Component.text("Sunscreen Commands").style(Style.style(TextColor.fromHexString("#4D9BE6"), TextDecoration.BOLD))).append(Component.newline());
        builder.append(Component.text(" ● /sunscreen ").style(Style.style(TextColor.fromHexString("#F9C22B"))).append(Component.text("editor").style(Style.style(TextColor.fromHexString("#F79617"), TextDecoration.UNDERLINED))));
        user.message(builder.asComponent());
    }

    @Subcommand("editor")
    public void editor(@NotNull CommandActor actor) {
        SunscreenUser<?> user = SunscreenLibrary.library().users().user(actor.uniqueId()).orElseThrow();
        EditorController controller = new EditorController(user);
        controller.start();
    }

    @Subcommand("test")
    public void test(@NotNull CommandActor actor) {
        SunscreenUser<?> user = SunscreenLibrary.library().users().user(actor.uniqueId()).orElseThrow();
        user.open(new SSCustomGames());
    }

}
