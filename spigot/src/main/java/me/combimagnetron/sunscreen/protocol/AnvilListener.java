package me.combimagnetron.sunscreen.protocol;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenWindow;
import me.combimagnetron.sunscreen.SunscreenLibrary;
import me.combimagnetron.sunscreen.user.SunscreenUser;
import me.combimagnetron.sunscreen.util.helper.FontHelper;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class AnvilListener implements PacketListener {
    private final static Component ANVIL = Component.empty().append(FontHelper.offset(-60)).append(Component.text("a").font(Key.key("sunscreen:anvil"))).append(FontHelper.offset(-118)).append(Component.text("b").font(Key.key("sunscreen:anvil"))).color(NamedTextColor.WHITE).append(FontHelper.offset(-110));

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.OPEN_WINDOW) {
            return;
        }
        WrapperPlayServerOpenWindow openWindow = new WrapperPlayServerOpenWindow(event);
        if (openWindow.getType() != 8) {
            return;
        }
        SunscreenUser<?> user = SunscreenLibrary.library().users().user(event.getUser().getUUID()).orElse(null);
        if (user == null) {
            return;
        }
        if (user.session() != null) {
            return;
        }
        Component title = openWindow.getTitle();
        Component product = ANVIL.append(title.color(NamedTextColor.DARK_GRAY)).font(Key.key("default"));
        openWindow.setTitle(product);
    }

}