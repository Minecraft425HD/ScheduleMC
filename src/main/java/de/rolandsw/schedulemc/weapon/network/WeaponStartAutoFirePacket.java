package de.rolandsw.schedulemc.weapon.network;

import de.rolandsw.schedulemc.weapon.gun.GunItem;
import de.rolandsw.schedulemc.weapon.util.WeaponNBT;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class WeaponStartAutoFirePacket {
    private final int slot;

    public WeaponStartAutoFirePacket(int slot) {
        this.slot = slot;
    }

    public static void encode(WeaponStartAutoFirePacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.slot);
    }

    public static WeaponStartAutoFirePacket decode(FriendlyByteBuf buf) {
        return new WeaponStartAutoFirePacket(buf.readInt());
    }

    public static void handle(WeaponStartAutoFirePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            ItemStack gunStack = msg.slot == -1 ? player.getOffhandItem() : player.getInventory().getItem(msg.slot);
            if (gunStack.getItem() instanceof GunItem) {
                player.getPersistentData().putBoolean(WeaponNBT.AUTO_FIRE_ACTIVE, true);
                player.getPersistentData().putInt(WeaponNBT.AUTO_FIRE_GUN_SLOT, msg.slot);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
