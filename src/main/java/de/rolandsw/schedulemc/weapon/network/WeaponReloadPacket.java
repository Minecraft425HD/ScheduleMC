package de.rolandsw.schedulemc.weapon.network;

import de.rolandsw.schedulemc.weapon.gun.GunItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class WeaponReloadPacket {
    public WeaponReloadPacket() {}

    public static void encode(WeaponReloadPacket msg, FriendlyByteBuf buf) {}

    public static WeaponReloadPacket decode(FriendlyByteBuf buf) {
        return new WeaponReloadPacket();
    }

    public static void handle(WeaponReloadPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            ItemStack mainHand = player.getMainHandItem();
            if (mainHand.getItem() instanceof GunItem gun) {
                gun.reload(player, mainHand);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
