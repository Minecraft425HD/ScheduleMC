package de.rolandsw.schedulemc.weapon.network;

import de.rolandsw.schedulemc.weapon.gun.GunItem;
import de.rolandsw.schedulemc.weapon.item.WeaponItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.function.Supplier;

public class WeaponSetAmmoTypePacket {
    private final int ammoIndex;

    public static final List<RegistryObject<Item>> AMMO_TYPES = List.of(
            WeaponItems.AMMO_STANDARD,
            WeaponItems.AMMO_AP,
            WeaponItems.AMMO_TRACER,
            WeaponItems.AMMO_RUBBER
    );

    public WeaponSetAmmoTypePacket(int ammoIndex) {
        this.ammoIndex = ammoIndex;
    }

    public static void encode(WeaponSetAmmoTypePacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.ammoIndex);
    }

    public static WeaponSetAmmoTypePacket decode(FriendlyByteBuf buf) {
        return new WeaponSetAmmoTypePacket(buf.readInt());
    }

    public static void handle(WeaponSetAmmoTypePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            int index = msg.ammoIndex;
            if (index < 0 || index >= AMMO_TYPES.size()) return;
            ItemStack mainHand = player.getMainHandItem();
            if (!(mainHand.getItem() instanceof GunItem gun)) return;
            gun.setLoadedAmmoType(mainHand, AMMO_TYPES.get(index).get());
        });
        ctx.get().setPacketHandled(true);
    }
}
