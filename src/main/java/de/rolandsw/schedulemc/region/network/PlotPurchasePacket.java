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

                        // Bestimme Netto-Kaufpreis: ohne Besitzer = Basispreis, sonst Verkaufspreis
                        double salePrice = !plot.hasOwner() ? plot.getPrice() : plot.getSalePrice();

                        // Alten Besitzer + purchaseTime VOR Eigentumsübertrag sichern
                        boolean hadOwner = plot.hasOwner();
                        UUID oldOwnerUUID = hadOwner ? UUID.fromString(plot.getOwnerUUID()) : null;
                        long prevPurchaseTime = plot.getPurchaseTime();

                        // MwSt. berechnen und Gesamtbetrag (Kaufpreis + MwSt.) vom Käufer abziehen
                        PlotTaxService.BuyerCostResult taxResult =
                            PlotTaxService.applyBuyerCosts(playerUUID, salePrice, plot.getPlotName());
                        if (!taxResult.success) {
                            player.sendSystemMessage(Component.translatable("message.plot.insufficient_funds")
                                .append(Component.literal(String.format("%.2f€", taxResult.totalPaid))
                                    .withStyle(ChatFormatting.GOLD)));
                            return;
                        }

                        // Netto-Kaufpreis an alten Besitzer oder – wenn staatseigen – an Staatskasse
                        if (hadOwner) {
                            EconomyManager.deposit(oldOwnerUUID, salePrice, TransactionType.PLOT_SALE,
                                "Plot-Verkauf: " + plot.getPlotName());
                            // Spekulationssteuer prüfen: 40% wenn Weiterverkauf < 7 Tage
                            double specTax = PlotTaxService.applySellerSpeculationTax(
                                oldOwnerUUID, prevPurchaseTime, salePrice, plot.getPlotName());
                            if (specTax > 0) {
                                // Alten Besitzer über Spekulationssteuer informieren (falls online)
                                UUID finalOldOwner = oldOwnerUUID;
                                double finalSpecTax = specTax;
                                player.getServer().getPlayerList().getPlayers().stream()
                                    .filter(p -> p.getUUID().equals(finalOldOwner))
                                    .findFirst()
                                    .ifPresent(oldOwner -> oldOwner.sendSystemMessage(
                                        Component.translatable("message.plot.speculation_tax_charged",
                                            plot.getPlotName(), String.format("%.2f", finalSpecTax))));
                            }
                        } else {
                            // Staatseigenes Grundstück → Nettokaufpreis an Staatskasse
                            StateAccount.deposit((int) Math.round(salePrice),
                                "Grundstücksverkauf: " + plot.getPlotName());
                        }

                        // purchaseTime für neuen Besitzer stempeln + Eigentumsübertrag
                        plot.setPurchaseTime(System.currentTimeMillis());
                        plot.setOwner(playerUUID, player.getName().getString());
                        plot.setForSale(false);
                        plot.setForRent(false);
                        PlotManager.savePlots();

                        // Käufer-Bestätigung mit Aufschlüsselung (Preis + MwSt. + Gesamt)
                        player.sendSystemMessage(Component.translatable("message.plot.purchased_with_tax",
                            String.format("%.2f", salePrice),
                            String.format("%.2f", taxResult.vatAmount),
                            String.format("%.2f", taxResult.totalPaid)));
                        break;

                    case RENT:
                        if (!plot.isForRent() || plot.isRented()) {
                            player.sendSystemMessage(Component.translatable("message.plot.not_for_rent"));
                            return;
                        }

                        // Sicherheitscheck: Besitzer muss existieren (verhindert Geldverlust bei
                        // korrupten Daten, wo ownerUUID leer/null ist aber forRent=true gesetzt ist)
                        UUID ownerUUID = plot.getOwnerUUIDAsUUID();
                        if (ownerUUID == null) {
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

                        // Zahle Miete an Besitzer (ownerUUID bereits oben validiert)
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
