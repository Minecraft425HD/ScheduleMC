package de.rolandsw.schedulemc.player.network;

import de.rolandsw.schedulemc.player.PlayerSettingsManager;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet zum Synchronisieren von Spieler-Einstellungen (Client → Server)
 */
public class PlayerSettingsPacket {

    private final boolean utilityWarningsEnabled;
    private final double electricityThreshold;
    private final double waterThreshold;

    public PlayerSettingsPacket(boolean utilityWarningsEnabled,
                               double electricityThreshold,
                               double waterThreshold) {
        this.utilityWarningsEnabled = utilityWarningsEnabled;
        this.electricityThreshold = electricityThreshold;
        this.waterThreshold = waterThreshold;
    }

    /**
     * Encode - Schreibt Daten ins Packet
     */
    public static void encode(PlayerSettingsPacket msg, FriendlyByteBuf buffer) {
        buffer.writeBoolean(msg.utilityWarningsEnabled);
        buffer.writeDouble(msg.electricityThreshold);
        buffer.writeDouble(msg.waterThreshold);
    }

    /**
     * Decode - Liest Daten aus Packet
     */
    public static PlayerSettingsPacket decode(FriendlyByteBuf buffer) {
        boolean utilityWarningsEnabled = buffer.readBoolean();
        double electricityThreshold = buffer.readDouble();
        double waterThreshold = buffer.readDouble();
        return new PlayerSettingsPacket(utilityWarningsEnabled, electricityThreshold, waterThreshold);
    }

    /**
     * Handle - Verarbeitet Packet auf Server-Seite
     */
    public static void handle(PlayerSettingsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            // Aktualisiere Einstellungen
            PlayerSettingsManager.setUtilityWarningsEnabled(player.getUUID(), msg.utilityWarningsEnabled);
            PlayerSettingsManager.setElectricityThreshold(player.getUUID(), msg.electricityThreshold);
            PlayerSettingsManager.setWaterThreshold(player.getUUID(), msg.waterThreshold);
        });
    }
}
