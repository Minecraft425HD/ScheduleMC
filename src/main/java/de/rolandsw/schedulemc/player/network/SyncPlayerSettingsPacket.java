package de.rolandsw.schedulemc.player.network;

import de.rolandsw.schedulemc.player.PlayerSettings;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet zum Synchronisieren von Spieler-Einstellungen (Server → Client)
 * Sendet die gespeicherten Einstellungen vom Server zum Client
 */
public class SyncPlayerSettingsPacket {

    private final boolean utilityWarningsEnabled;
    private final double electricityThreshold;
    private final double waterThreshold;

    public SyncPlayerSettingsPacket(PlayerSettings settings) {
        this.utilityWarningsEnabled = settings.isUtilityWarningsEnabled();
        this.electricityThreshold = settings.getElectricityWarningThreshold();
        this.waterThreshold = settings.getWaterWarningThreshold();
    }

    public SyncPlayerSettingsPacket(boolean utilityWarningsEnabled,
                                   double electricityThreshold,
                                   double waterThreshold) {
        this.utilityWarningsEnabled = utilityWarningsEnabled;
        this.electricityThreshold = electricityThreshold;
        this.waterThreshold = waterThreshold;
    }

    /**
     * Encode - Schreibt Daten ins Packet
     */
    public static void encode(SyncPlayerSettingsPacket msg, FriendlyByteBuf buffer) {
        buffer.writeBoolean(msg.utilityWarningsEnabled);
        buffer.writeDouble(msg.electricityThreshold);
        buffer.writeDouble(msg.waterThreshold);
    }

    /**
     * Decode - Liest Daten aus Packet
     */
    public static SyncPlayerSettingsPacket decode(FriendlyByteBuf buffer) {
        boolean utilityWarningsEnabled = buffer.readBoolean();
        double electricityThreshold = buffer.readDouble();
        double waterThreshold = buffer.readDouble();
        return new SyncPlayerSettingsPacket(utilityWarningsEnabled, electricityThreshold, waterThreshold);
    }

    /**
     * Handle - Verarbeitet Packet auf Client-Seite
     */
    public static void handle(SyncPlayerSettingsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Verwende atomares Update um Race Conditions bei schnell aufeinanderfolgenden Paketen zu vermeiden
            ClientPlayerSettings.update(msg.utilityWarningsEnabled, msg.electricityThreshold, msg.waterThreshold);
        });
        ctx.get().setPacketHandled(true);
    }
}
