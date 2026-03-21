//package me.combimagnetron.sunscreen.protocol;
//
//import com.github.retrooper.packetevents.event.PacketListener;
//import com.github.retrooper.packetevents.event.PacketReceiveEvent;
//import com.github.retrooper.packetevents.event.PacketSendEvent;
//import com.github.retrooper.packetevents.protocol.packettype.PacketType;
//import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
//import com.github.retrooper.packetevents.protocol.player.DiggingAction;
//import com.github.retrooper.packetevents.protocol.player.InteractionHand;
//import com.github.retrooper.packetevents.protocol.sound.Sounds;
//import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
//import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
//import com.github.retrooper.packetevents.util.Vector3i;
//import com.github.retrooper.packetevents.wrapper.play.client.*;
//import com.github.retrooper.packetevents.wrapper.play.server.*;
//import me.combimagnetron.passport.internal.entity.impl.Interaction;
//import me.combimagnetron.sunscreen.SunscreenLibrary;
//import me.combimagnetron.sunscreen.neo.input.InputHandler;
//import me.combimagnetron.sunscreen.neo.input.context.MouseInputContext;
//import me.combimagnetron.sunscreen.neo.input.context.ScrollInputContext;
//import me.combimagnetron.sunscreen.neo.input.context.TextInputContext;
//import me.combimagnetron.sunscreen.neo.protocol.PlatformProtocolIntermediate;
//import me.combimagnetron.sunscreen.neo.session.Session;
//import me.combimagnetron.sunscreen.user.SunscreenUser;
//import me.combimagnetron.sunscreen.util.Scheduler;
//import me.combimagnetron.sunscreen.util.helper.RotationHelper;
//import net.kyori.adventure.audience.Audience;
//import net.kyori.adventure.text.Component;
//import org.bukkit.*;
//import org.bukkit.block.Block;
//import org.bukkit.entity.Player;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.Optional;
//
//public class ProtocolListener implements PacketListener {
//
//
//    static boolean inMenu(@NotNull SunscreenUser<?> user) {
//        return SunscreenLibrary.library().sessionHandler().inMenu(user);
//    }
//
//}
