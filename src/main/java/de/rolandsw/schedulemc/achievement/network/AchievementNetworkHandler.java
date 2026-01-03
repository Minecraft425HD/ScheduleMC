package de.rolandsw.schedulemc.achievement.network;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Network Handler für Achievement Packets
 */
public class AchievementNetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(ScheduleMC.MOD_ID, "achievement_network"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        // Client → Server: Request Achievement Data
        INSTANCE.messageBuilder(RequestAchievementDataPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(RequestAchievementDataPacket::decode)
            .encoder(RequestAchievementDataPacket::encode)
            .consumerMainThread(RequestAchievementDataPacket::handle)
            .add();

        // Server → Client: Sync Achievement Data
        INSTANCE.messageBuilder(SyncAchievementDataPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(SyncAchievementDataPacket::decode)
            .encoder(SyncAchievementDataPacket::encode)
            .consumerMainThread(SyncAchievementDataPacket::handle)
            .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
