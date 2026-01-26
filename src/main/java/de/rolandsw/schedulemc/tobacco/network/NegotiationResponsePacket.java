package de.rolandsw.schedulemc.tobacco.network;

import de.rolandsw.schedulemc.tobacco.business.NPCResponse;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server -> Client Packet für Verhandlungs-Antworten
 * Enthält NPC-Stimmung und Runden-Info für die GUI
 */
public class NegotiationResponsePacket {
    private final boolean accepted;
    private final double counterOffer;
    private final String message;
    private final int reputationChange;
    private final float mood;       // 0-100
    private final int round;        // Aktuelle Runde
    private final int maxRounds;    // Max Runden (basierend auf NPC Persönlichkeit)

    public NegotiationResponsePacket(NPCResponse response, int maxRounds) {
        this.accepted = response.isAccepted();
        this.counterOffer = response.getCounterOffer();
        this.message = response.getMessage();
        this.reputationChange = response.getReputationChange();
        this.mood = response.getMood();
        this.round = response.getRound();
        this.maxRounds = maxRounds;
    }

    public NegotiationResponsePacket(boolean accepted, double counterOffer, String message,
                                      int reputationChange, float mood, int round, int maxRounds) {
        this.accepted = accepted;
        this.counterOffer = counterOffer;
        this.message = message;
        this.reputationChange = reputationChange;
        this.mood = mood;
        this.round = round;
        this.maxRounds = maxRounds;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(accepted);
        buf.writeDouble(counterOffer);
        buf.writeUtf(message);
        buf.writeInt(reputationChange);
        buf.writeFloat(mood);
        buf.writeInt(round);
        buf.writeInt(maxRounds);
    }

    public static NegotiationResponsePacket decode(FriendlyByteBuf buf) {
        return new NegotiationResponsePacket(
            buf.readBoolean(),
            buf.readDouble(),
            buf.readUtf(),
            buf.readInt(),
            buf.readFloat(),
            buf.readInt(),
            buf.readInt()
        );
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Client-seitige Verarbeitung wird von ClientTobaccoScreenHandler übernommen
            ClientTobaccoScreenHandler.handleNegotiationResponse(this);
        });
        ctx.get().setPacketHandled(true);
    }

    // Getters
    public boolean isAccepted() { return accepted; }
    public double getCounterOffer() { return counterOffer; }
    public String getMessage() { return message; }
    public int getReputationChange() { return reputationChange; }
    public float getMood() { return mood; }
    public int getRound() { return round; }
    public int getMaxRounds() { return maxRounds; }
}
