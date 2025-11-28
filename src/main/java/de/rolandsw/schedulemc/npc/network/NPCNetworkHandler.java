package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Network Handler für NPC Packets
 */
public class NPCNetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(ScheduleMC.MOD_ID, "npc_network"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        INSTANCE.messageBuilder(SpawnNPCPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(SpawnNPCPacket::decode)
            .encoder(SpawnNPCPacket::encode)
            .consumerMainThread(SpawnNPCPacket::handle)
            .add();

        INSTANCE.messageBuilder(NPCActionPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(NPCActionPacket::decode)
            .encoder(NPCActionPacket::encode)
            .consumerMainThread(NPCActionPacket::handle)
            .add();

        INSTANCE.messageBuilder(SyncNPCDataPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(SyncNPCDataPacket::decode)
            .encoder(SyncNPCDataPacket::encode)
            .consumerMainThread(SyncNPCDataPacket::handle)
            .add();

        INSTANCE.messageBuilder(PurchaseItemPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(PurchaseItemPacket::decode)
            .encoder(PurchaseItemPacket::encode)
            .consumerMainThread(PurchaseItemPacket::handle)
            .add();

        INSTANCE.messageBuilder(OpenMerchantShopPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(OpenMerchantShopPacket::decode)
            .encoder(OpenMerchantShopPacket::encode)
            .consumerMainThread(OpenMerchantShopPacket::handle)
            .add();

        INSTANCE.messageBuilder(UpdateShopItemsPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(UpdateShopItemsPacket::decode)
            .encoder(UpdateShopItemsPacket::encode)
            .consumerMainThread(UpdateShopItemsPacket::handle)
            .add();

        INSTANCE.messageBuilder(OpenStealingMenuPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(OpenStealingMenuPacket::decode)
            .encoder(OpenStealingMenuPacket::encode)
            .consumerMainThread(OpenStealingMenuPacket::handle)
            .add();

        INSTANCE.messageBuilder(StealingAttemptPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(StealingAttemptPacket::decode)
            .encoder(StealingAttemptPacket::encode)
            .consumerMainThread(StealingAttemptPacket::handle)
            .add();

        INSTANCE.messageBuilder(WantedLevelSyncPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(WantedLevelSyncPacket::decode)
            .encoder(WantedLevelSyncPacket::encode)
            .consumerMainThread(WantedLevelSyncPacket::handle)
            .add();

        INSTANCE.messageBuilder(SyncNPCNamesPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(SyncNPCNamesPacket::decode)
            .encoder(SyncNPCNamesPacket::encode)
            .consumerMainThread(SyncNPCNamesPacket::handle)
            .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
