package de.rolandsw.schedulemc.weapon.network;

import de.rolandsw.schedulemc.weapon.util.WeaponNBT;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class WeaponStopAutoFirePacket {
    public WeaponStopAutoFirePacket() {}

    public static void encode(WeaponStopAutoFirePacket msg, FriendlyByteBuf buf) {}

    public static WeaponStopAutoFirePacket decode(FriendlyByteBuf buf) {
        return new WeaponStopAutoFirePacket();
    }

    public static void handle(WeaponStopAutoFirePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            player.getPersistentData().remove(WeaponNBT.AUTO_FIRE_ACTIVE);
        });
        ctx.get().setPacketHandled(true);
    }
}
