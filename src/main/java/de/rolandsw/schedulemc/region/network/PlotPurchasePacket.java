package de.rolandsw.schedulemc.region.network;

import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.economy.TransactionType;
import de.rolandsw.schedulemc.region.PlotManager;
import de.rolandsw.schedulemc.region.PlotRegion;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Packet für Plot-Kauf/Mietung (Client → Server)
 * Wird von Spielern verwendet, die einen Plot kaufen/mieten möchten
 */
public class PlotPurchasePacket {

    public enum PurchaseType {
        BUY,    // Plot kaufen
        RENT    // Plot mieten
    }

    private final String plotId;
    private final PurchaseType type;

    public PlotPurchasePacket(String plotId, PurchaseType type) {
        this.plotId = plotId;
        this.type = type;
    }

    /**
     * Encode - Schreibt Daten ins Packet
     */
    public static void encode(PlotPurchasePacket msg, FriendlyByteBuf buffer) {
        buffer.writeUtf(msg.plotId);
        buffer.writeEnum(msg.type);
    }

    /**
     * Decode - Liest Daten aus Packet
     */
    public static PlotPurchasePacket decode(FriendlyByteBuf buffer) {
        String plotId = buffer.readUtf();
        PurchaseType type = buffer.readEnum(PurchaseType.class);
        return new PlotPurchasePacket(plotId, type);
    }

    /**
     * Handle - Verarbeitet Packet auf Server-Seite
     */
    public static void handle(PlotPurchasePacket msg, Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            PlotRegion plot = PlotManager.getPlot(msg.plotId);

            if (plot == null) {
                player.sendSystemMessage(Component.literal("§cPlot nicht gefunden!"));
                return;
            }

            UUID playerUUID = player.getUUID();

            switch (msg.type) {
                case BUY:
                    if (!plot.isForSale()) {
                        player.sendSystemMessage(Component.literal("§cDieser Plot steht nicht zum Verkauf!"));
                        return;
                    }

                    double salePrice = plot.getSalePrice();

                    // Prüfe Guthaben
                    if (EconomyManager.getBalance(playerUUID) < salePrice) {
                        player.sendSystemMessage(Component.literal("§cNicht genug Guthaben! Benötigt: ")
                            .append(Component.literal(String.format("%.2f€", salePrice))
                                .withStyle(ChatFormatting.GOLD)));
                        return;
                    }

                    // Zahle an Besitzer
                    UUID oldOwnerUUID = UUID.fromString(plot.getOwnerUUID());
                    EconomyManager.withdraw(playerUUID, salePrice, TransactionType.PLOT_PURCHASE,
                        "Plot-Kauf: " + plot.getPlotName());
                    EconomyManager.deposit(oldOwnerUUID, salePrice, TransactionType.PLOT_SALE,
                        "Plot-Verkauf: " + plot.getPlotName());

                    // Übertrage Eigentum
                    plot.setOwner(playerUUID.toString());
                    plot.setForSale(false, 0);
                    plot.setForRent(false, 0);
                    PlotManager.savePlots();

                    player.sendSystemMessage(Component.literal("§aPlot erfolgreich gekauft für ")
                        .append(Component.literal(String.format("%.2f€", salePrice))
                            .withStyle(ChatFormatting.GOLD)));
                    break;

                case RENT:
                    if (!plot.isForRent() || plot.isRented()) {
                        player.sendSystemMessage(Component.literal("§cDieser Plot steht nicht zur Miete!"));
                        return;
                    }

                    double rentPrice = plot.getRentPricePerDay();

                    // Prüfe Guthaben (mind. 1 Tag)
                    if (EconomyManager.getBalance(playerUUID) < rentPrice) {
                        player.sendSystemMessage(Component.literal("§cNicht genug Guthaben! Benötigt: ")
                            .append(Component.literal(String.format("%.2f€", rentPrice))
                                .withStyle(ChatFormatting.GOLD)));
                        return;
                    }

                    // Zahle erste Miete
                    UUID ownerUUID = UUID.fromString(plot.getOwnerUUID());
                    EconomyManager.withdraw(playerUUID, rentPrice, TransactionType.PLOT_RENT,
                        "Plot-Miete (1 Tag): " + plot.getPlotName());
                    EconomyManager.deposit(ownerUUID, rentPrice, TransactionType.PLOT_RENT,
                        "Mieteinnahme: " + plot.getPlotName());

                    // Setze Mieter
                    plot.setRenter(playerUUID.toString());
                    plot.setRentStartTime(System.currentTimeMillis());
                    PlotManager.savePlots();

                    player.sendSystemMessage(Component.literal("§aPlot erfolgreich gemietet für ")
                        .append(Component.literal(String.format("%.2f€/Tag", rentPrice))
                            .withStyle(ChatFormatting.GOLD)));
                    break;
            }
        });
    }
}
