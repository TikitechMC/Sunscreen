package me.combimagnetron.sunscreen.command;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.SunscreenLibrary;
import me.combimagnetron.sunscreen.example.BiddingHouseMenuTemplate;
import me.combimagnetron.sunscreen.neo.TestMenuTemplate;
import me.combimagnetron.sunscreen.neo.ActiveMenu;
import me.combimagnetron.sunscreen.neo.editor.template.EditorMenuTemplate;
import me.combimagnetron.sunscreen.neo.editor.template.EditorStartOverviewMenuTemplate;
import me.combimagnetron.sunscreen.neo.element.ElementLike;
import me.combimagnetron.sunscreen.neo.property.Position;
import me.combimagnetron.sunscreen.neo.session.Session;
import me.combimagnetron.sunscreen.user.SunscreenUser;
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
        ActiveMenu menu = new ActiveMenu(new TestMenuTemplate(), user, Identifier.of("aa"));
    }

    @Subcommand("add")
    public void add(@NotNull CommandActor actor) {
        SunscreenUser<?> user = SunscreenLibrary.library().users().user(actor.uniqueId()).orElseThrow();
        Session session = user.session();
        if (session == null) return;
        ActiveMenu menu = session.menu();
    }

    @Subcommand("move")
    public void move(@NotNull CommandActor actor) {
        SunscreenUser<?> user = SunscreenLibrary.library().users().user(actor.uniqueId()).orElseThrow();
        Session session = user.session();
        if (session == null) return;
        ActiveMenu menu = session.menu();
        ElementLike<?> elementLike = menu.element(Identifier.of("aaa"));
        elementLike.position(Position.fixed(Vec2i.of(100, 200)));
    }



}
