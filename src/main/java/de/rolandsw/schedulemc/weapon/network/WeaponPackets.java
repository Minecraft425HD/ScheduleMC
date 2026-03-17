package de.rolandsw.schedulemc.weapon.network;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class WeaponPackets {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ScheduleMC.MOD_ID, "weapon"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        INSTANCE.registerMessage(packetId++,
                WeaponStartAutoFirePacket.class,
                WeaponStartAutoFirePacket::encode,
                WeaponStartAutoFirePacket::decode,
                WeaponStartAutoFirePacket::handle);
        INSTANCE.registerMessage(packetId++,
                WeaponStopAutoFirePacket.class,
                WeaponStopAutoFirePacket::encode,
                WeaponStopAutoFirePacket::decode,
                WeaponStopAutoFirePacket::handle);
        INSTANCE.registerMessage(packetId++,
                WeaponReloadPacket.class,
                WeaponReloadPacket::encode,
                WeaponReloadPacket::decode,
                WeaponReloadPacket::handle);
        INSTANCE.registerMessage(packetId++,
                WeaponFirePacket.class,
                WeaponFirePacket::encode,
                WeaponFirePacket::decode,
                WeaponFirePacket::handle);
        INSTANCE.registerMessage(packetId++,
                WeaponSetAmmoTypePacket.class,
                WeaponSetAmmoTypePacket::encode,
                WeaponSetAmmoTypePacket::decode,
                WeaponSetAmmoTypePacket::handle);
    }

    public static void sendToServer(Object packet) {
        INSTANCE.sendToServer(packet);
    }
}
