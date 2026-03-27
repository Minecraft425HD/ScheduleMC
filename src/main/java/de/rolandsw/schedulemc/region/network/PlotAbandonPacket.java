package de.rolandsw.schedulemc.region.network;

import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.economy.PlotTaxService;
import de.rolandsw.schedulemc.economy.StateAccount;
import de.rolandsw.schedulemc.economy.TransactionType;
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
     * SICHERHEIT: Max-Länge für plotId gegen DoS/Memory-Angriffe
     */
    public static PlotAbandonPacket decode(FriendlyByteBuf buffer) {
        String plotId = buffer.readUtf(256);
        return new PlotAbandonPacket(plotId);
    }

    /**
     * Handle - Verarbeitet Packet auf Server-Seite
     */
    public static void handle(PlotAbandonPacket msg, Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            PlotRegion plot = PlotManager.getPlot(msg.plotId);

            if (plot == null) {
                player.sendSystemMessage(Component.translatable("message.plot.not_found"));
                return;
            }

            // Prüfe ob Spieler Besitzer ist (null-safe: player.getUUID() ist nie null)
            if (!player.getUUID().toString().equals(plot.getOwnerUUID())) {
                player.sendSystemMessage(Component.translatable("message.plot.not_owner"));
                return;
            }

            String plotName = plot.getPlotName();
            double originalPrice = plot.getPrice();
            long purchaseTime = plot.getPurchaseTime();
            boolean specWindow = PlotTaxService.isInSpeculationWindow(purchaseTime);

            // Aufteilung berechnen (50/50, ggf. mit Spekulationssteuer auf Spieleranteil)
            double[] split = PlotTaxService.calculateAbandonSplit(purchaseTime, originalPrice);
            double playerShare = split[0];
            double stateShare = split[1];

            // Plot zurück auf Server-Besitz setzen
            plot.setOwnerUUID(""); // Leer = Server-owned
            plot.setOwnerName(null);
            plot.setForSale(false);
            plot.setForRent(false);
            plot.getTrustedPlayers().clear();
            plot.setDescription("");
            plot.setPlotName("Freies Grundstück");
            plot.setPurchaseTime(0L);

            PlotManager.savePlots();

            // Geldüberweisung
            EconomyManager.deposit(player.getUUID(), playerShare, TransactionType.PLOT_SALE,
                "Grundstück aufgegeben: " + plotName);
            StateAccount.deposit((int) Math.round(stateShare),
                "Grundstücksauflösung: " + plotName);

            player.sendSystemMessage(Component.translatable("message.plot.abandoned_prefix")
                .append(Component.literal(plotName).withStyle(ChatFormatting.GRAY)));
            if (specWindow) {
                player.sendSystemMessage(Component.translatable("message.plot.abandoned_refund_speculation",
                    String.format("%.2f", playerShare), String.format("%.2f", stateShare)));
            } else {
                player.sendSystemMessage(Component.translatable("message.plot.abandoned_refund",
                    String.format("%.2f", playerShare), String.format("%.2f", stateShare)));
            }
        });
    }
}
