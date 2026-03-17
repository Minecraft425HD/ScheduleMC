package de.rolandsw.schedulemc.weapon.network;

import de.rolandsw.schedulemc.weapon.gun.GunItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class WeaponFirePacket {
    private final int slot;
    private final int shots;

    public WeaponFirePacket(int slot, int shots) {
        this.slot = slot;
        this.shots = shots;
    }

    public static void encode(WeaponFirePacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.slot);
        buf.writeInt(msg.shots);
    }

    public static WeaponFirePacket decode(FriendlyByteBuf buf) {
        return new WeaponFirePacket(buf.readInt(), buf.readInt());
    }

    public static void handle(WeaponFirePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            ItemStack gunStack = msg.slot == -1
                    ? player.getOffhandItem()
                    : player.getInventory().getItem(msg.slot);
            if (gunStack.getItem() instanceof GunItem gun) {
                gun.performShots(player.level(), player, gunStack, msg.shots);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
