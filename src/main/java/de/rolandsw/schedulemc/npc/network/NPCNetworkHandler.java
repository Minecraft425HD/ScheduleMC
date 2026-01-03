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

        // Performance-optimized: Balance-only sync (12 bytes vs 500-2000 bytes)
        INSTANCE.messageBuilder(SyncNPCBalancePacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(SyncNPCBalancePacket::decode)
            .encoder(SyncNPCBalancePacket::encode)
            .consumerMainThread(SyncNPCBalancePacket::handle)
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

        // Delta-Sync für NPC-Namen (optimiert: nur Änderungen statt Full-Sync)
        INSTANCE.messageBuilder(DeltaSyncNPCNamesPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(DeltaSyncNPCNamesPacket::decode)
            .encoder(DeltaSyncNPCNamesPacket::encode)
            .consumerMainThread(DeltaSyncNPCNamesPacket::handle)
            .add();

        INSTANCE.messageBuilder(PayFuelBillPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(PayFuelBillPacket::decode)
            .encoder(PayFuelBillPacket::encode)
            .consumerMainThread(PayFuelBillPacket::handle)
            .add();

        INSTANCE.messageBuilder(BankDepositPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(BankDepositPacket::decode)
            .encoder(BankDepositPacket::encode)
            .consumerMainThread(BankDepositPacket::handle)
            .add();

        INSTANCE.messageBuilder(BankWithdrawPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(BankWithdrawPacket::decode)
            .encoder(BankWithdrawPacket::encode)
            .consumerMainThread(BankWithdrawPacket::handle)
            .add();

        INSTANCE.messageBuilder(BankTransferPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(BankTransferPacket::decode)
            .encoder(BankTransferPacket::encode)
            .consumerMainThread(BankTransferPacket::handle)
            .add();

        INSTANCE.messageBuilder(SavingsDepositPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(SavingsDepositPacket::decode)
            .encoder(SavingsDepositPacket::encode)
            .consumerMainThread(SavingsDepositPacket::handle)
            .add();

        INSTANCE.messageBuilder(SavingsWithdrawPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(SavingsWithdrawPacket::decode)
            .encoder(SavingsWithdrawPacket::encode)
            .consumerMainThread(SavingsWithdrawPacket::handle)
            .add();

        INSTANCE.messageBuilder(StockTradePacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(StockTradePacket::decode)
            .encoder(StockTradePacket::encode)
            .consumerMainThread(StockTradePacket::handle)
            .add();

        INSTANCE.messageBuilder(OpenBankerMenuPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(OpenBankerMenuPacket::decode)
            .encoder(OpenBankerMenuPacket::encode)
            .consumerMainThread(OpenBankerMenuPacket::handle)
            .add();

        INSTANCE.messageBuilder(OpenBoerseMenuPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(OpenBoerseMenuPacket::decode)
            .encoder(OpenBoerseMenuPacket::encode)
            .consumerMainThread(OpenBoerseMenuPacket::handle)
            .add();

        INSTANCE.messageBuilder(CreateRecurringPaymentPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(CreateRecurringPaymentPacket::decode)
            .encoder(CreateRecurringPaymentPacket::encode)
            .consumerMainThread(CreateRecurringPaymentPacket::handle)
            .add();

        INSTANCE.messageBuilder(DeleteRecurringPaymentPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(DeleteRecurringPaymentPacket::decode)
            .encoder(DeleteRecurringPaymentPacket::encode)
            .consumerMainThread(DeleteRecurringPaymentPacket::handle)
            .add();

        INSTANCE.messageBuilder(PauseRecurringPaymentPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(PauseRecurringPaymentPacket::decode)
            .encoder(PauseRecurringPaymentPacket::encode)
            .consumerMainThread(PauseRecurringPaymentPacket::handle)
            .add();

        INSTANCE.messageBuilder(ResumeRecurringPaymentPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(ResumeRecurringPaymentPacket::decode)
            .encoder(ResumeRecurringPaymentPacket::encode)
            .consumerMainThread(ResumeRecurringPaymentPacket::handle)
            .add();

        // Credit Advisor Packets
        INSTANCE.messageBuilder(OpenCreditAdvisorMenuPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(OpenCreditAdvisorMenuPacket::decode)
            .encoder(OpenCreditAdvisorMenuPacket::encode)
            .consumerMainThread(OpenCreditAdvisorMenuPacket::handle)
            .add();

        INSTANCE.messageBuilder(ApplyCreditLoanPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(ApplyCreditLoanPacket::decode)
            .encoder(ApplyCreditLoanPacket::encode)
            .consumerMainThread(ApplyCreditLoanPacket::handle)
            .add();

        INSTANCE.messageBuilder(RepayCreditLoanPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(RepayCreditLoanPacket::decode)
            .encoder(RepayCreditLoanPacket::encode)
            .consumerMainThread(RepayCreditLoanPacket::handle)
            .add();

        INSTANCE.messageBuilder(RequestCreditDataPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
            .decoder(RequestCreditDataPacket::decode)
            .encoder(RequestCreditDataPacket::encode)
            .consumerMainThread(RequestCreditDataPacket::handle)
            .add();

        INSTANCE.messageBuilder(SyncCreditDataPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(SyncCreditDataPacket::decode)
            .encoder(SyncCreditDataPacket::encode)
            .consumerMainThread(SyncCreditDataPacket::handle)
            .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
