package de.rolandsw.schedulemc.region.network;

import de.rolandsw.schedulemc.region.PlotManager;
import de.rolandsw.schedulemc.region.PlotRegion;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet für Plot-Aufgabe (Client → Server)
 * WARNUNG: Diese Aktion ist irreversibel!
 */
public class PlotAbandonPacket {

    private final String plotId;

    public PlotAbandonPacket(String plotId) {
        this.plotId = plotId;
    }

    /**
     * Encode - Schreibt Daten ins Packet
     */
    public static void encode(PlotAbandonPacket msg, FriendlyByteBuf buffer) {
        buffer.writeUtf(msg.plotId);
    }

    /**
     * Decode - Liest Daten aus Packet
     */
    public static PlotAbandonPacket decode(FriendlyByteBuf buffer) {
        String plotId = buffer.readUtf();
        return new PlotAbandonPacket(plotId);
    }

    /**
     * Handle - Verarbeitet Packet auf Server-Seite
     */
    public static void handle(PlotAbandonPacket msg, Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            PlotRegion plot = PlotManager.getPlot(msg.plotId);

            if (plot == null) {
                player.sendSystemMessage(Component.literal("§cPlot nicht gefunden!"));
                return;
            }

            // Prüfe ob Spieler Besitzer ist
            if (!plot.getOwnerUUID().equals(player.getUUID().toString())) {
                player.sendSystemMessage(Component.literal("§cDu bist nicht der Besitzer dieses Plots!"));
                return;
            }

            String plotName = plot.getPlotName();

            // Setze Plot zurück auf Server-Besitz
            plot.setOwner(null); // null = Server-owned
            plot.setForSale(false, 0);
            plot.setForRent(false, 0);
            plot.getTrustedPlayers().clear();
            plot.setDescription("");
            plot.setPlotName("Freies Grundstück");

            PlotManager.savePlots();

            player.sendSystemMessage(Component.literal("§c§lPlot aufgegeben: ")
                .append(Component.literal(plotName).withStyle(ChatFormatting.GRAY)));
            player.sendSystemMessage(Component.literal("§7Der Plot steht nun wieder zum Verkauf."));
        });
    }
}
