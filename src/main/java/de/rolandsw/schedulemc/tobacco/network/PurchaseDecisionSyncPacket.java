package de.rolandsw.schedulemc.tobacco.network;

import de.rolandsw.schedulemc.tobacco.screen.TobaccoNegotiationScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet zur Synchronisation der Purchase Decision vom Server zum Client
 * Wird verwendet, um die Kaufbereitschaft in der TobaccoNegotiationScreen anzuzeigen
 */
public class PurchaseDecisionSyncPacket {
    private final int totalScore;           // 0-100+ Punkte
    private final boolean willingToBuy;     // Kaufbereitschaft (true/false)
    private final int desiredAmount;        // Gewünschte Menge in Gramm (0-10)
    private final int npcWallet;            // NPC Geld in Euro

    public PurchaseDecisionSyncPacket(int totalScore, boolean willingToBuy, int desiredAmount, int npcWallet) {
        this.totalScore = totalScore;
        this.willingToBuy = willingToBuy;
        this.desiredAmount = desiredAmount;
        this.npcWallet = npcWallet;
    }

    /**
     * Kodiert das Packet in den Byte-Buffer
     */
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(totalScore);
        buf.writeBoolean(willingToBuy);
        buf.writeInt(desiredAmount);
        buf.writeInt(npcWallet);
    }

    /**
     * Dekodiert das Packet aus dem Byte-Buffer
     */
    public static PurchaseDecisionSyncPacket decode(FriendlyByteBuf buf) {
        return new PurchaseDecisionSyncPacket(
            buf.readInt(),
            buf.readBoolean(),
            buf.readInt(),
            buf.readInt()
        );
    }

    /**
     * Behandelt das Packet auf der Client-Seite
     */
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Client-Side: Update Screen
            if (Minecraft.getInstance().screen instanceof TobaccoNegotiationScreen screen) {
                screen.updatePurchaseDecision(totalScore, willingToBuy, desiredAmount, npcWallet);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    // Getters für Debugging
    public int getTotalScore() {
        return totalScore;
    }

    public boolean isWillingToBuy() {
        return willingToBuy;
    }

    public int getDesiredAmount() {
        return desiredAmount;
    }

    public int getNpcWallet() {
        return npcWallet;
    }
}
