package de.rolandsw.schedulemc.player.network;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Registriert Player Settings Packets
 */
public class PlayerSettingsNetworkHandler {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(ScheduleMC.MOD_ID, "player_settings_network"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private static int id = 0;
    private static int id() {
        return id++;
    }

    public static void register() {
        INSTANCE.messageBuilder(PlayerSettingsPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(PlayerSettingsPacket::decode)
            .encoder(PlayerSettingsPacket::encode)
            .consumerMainThread(PlayerSettingsPacket::handle)
            .add();
    }

    /**
     * Sendet Packet vom Client zum Server
     */
    public static void sendToServer(PlayerSettingsPacket packet) {
        INSTANCE.sendToServer(packet);
    }
}
