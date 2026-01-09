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
     * SICHERHEIT: Input-Validierung gegen DoS
     */
    public static PlotPurchasePacket decode(FriendlyByteBuf buffer) {
        // SICHERHEIT: Max-Länge 256 Zeichen für plotId
        String plotId = buffer.readUtf(256);
        PurchaseType type = buffer.readEnum(PurchaseType.class);
        return new PlotPurchasePacket(plotId, type);
    }

    /**
     * Handle - Verarbeitet Packet auf Server-Seite
     * SICHERHEIT: Atomare Plot-Transaktionen mit synchronized Block
     */
    public static void handle(PlotPurchasePacket msg, Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            PlotRegion plot = PlotManager.getPlot(msg.plotId);

            if (plot == null) {
                player.sendSystemMessage(Component.translatable("message.plot.not_found"));
                return;
            }

            UUID playerUUID = player.getUUID();

            // SICHERHEIT: Atomare Transaktion - Lock auf Plot-Objekt
            synchronized (plot) {
                switch (msg.type) {
                    case BUY:
                        // Prüfe ob Plot kaufbar ist (ohne Besitzer ODER explizit zum Verkauf)
                        if (plot.hasOwner() && !plot.isForSale()) {
                            player.sendSystemMessage(Component.translatable("message.plot.not_for_sale"));
                            return;
                        }

                        // Bestimme Preis: Plot ohne Besitzer = Standardpreis, sonst Verkaufspreis
                        double salePrice = !plot.hasOwner() ? plot.getPrice() : plot.getSalePrice();

                        // SICHERHEIT: Atomare Transaktion mit EconomyManager
                        if (!EconomyManager.withdraw(playerUUID, salePrice, TransactionType.PLOT_PURCHASE,
                                "Plot-Kauf: " + plot.getPlotName())) {
                            player.sendSystemMessage(Component.translatable("message.plot.insufficient_funds")
                                .append(Component.literal(String.format("%.2f€", salePrice))
                                    .withStyle(ChatFormatting.GOLD)));
                            return;
                        }

                        // Zahle an alten Besitzer (nur wenn Plot einen Besitzer hat)
                        if (plot.hasOwner()) {
                            UUID oldOwnerUUID = UUID.fromString(plot.getOwnerUUID());
                            EconomyManager.deposit(oldOwnerUUID, salePrice, TransactionType.PLOT_SALE,
                                "Plot-Verkauf: " + plot.getPlotName());
                        }

                        // Übertrage Eigentum
                        plot.setOwner(playerUUID, player.getName().getString());
                        plot.setForSale(false);
                        plot.setForRent(false);
                        PlotManager.savePlots();

                        player.sendSystemMessage(Component.translatable("message.plot.purchased")
                            .append(Component.literal(String.format("%.2f€", salePrice))
                                .withStyle(ChatFormatting.GOLD)));
                        break;

                    case RENT:
                        if (!plot.isForRent() || plot.isRented()) {
                            player.sendSystemMessage(Component.translatable("message.plot.not_for_rent"));
                            return;
                        }

                        double rentPrice = plot.getRentPricePerDay();

                        // SICHERHEIT: Atomare Transaktion
                        if (!EconomyManager.withdraw(playerUUID, rentPrice, TransactionType.PLOT_RENT,
                                "Plot-Miete (1 Tag): " + plot.getPlotName())) {
                            player.sendSystemMessage(Component.translatable("message.plot.insufficient_funds")
                                .append(Component.literal(String.format("%.2f€", rentPrice))
                                    .withStyle(ChatFormatting.GOLD)));
                            return;
                        }

                        // Zahle Miete an Besitzer
                        UUID ownerUUID = UUID.fromString(plot.getOwnerUUID());
                        EconomyManager.deposit(ownerUUID, rentPrice, TransactionType.PLOT_RENT,
                            "Mieteinnahme: " + plot.getPlotName());

                        // Setze Mieter und Mietzeit (1 Tag = 24 Stunden)
                        plot.setRenterUUID(playerUUID.toString());
                        plot.setRentEndTime(System.currentTimeMillis() + (24 * 60 * 60 * 1000));
                        PlotManager.savePlots();

                        player.sendSystemMessage(Component.translatable("message.plot.rent_success", rentPrice));
                        break;
                }
            }
        });
    }
}
