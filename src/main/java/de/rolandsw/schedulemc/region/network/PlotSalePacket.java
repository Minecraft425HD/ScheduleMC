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
 * Packet für Plot-Verkauf/Vermietung (Client → Server)
 * Wird vom Plot-Besitzer verwendet
 */
public class PlotSalePacket {

    public enum SaleType {
        SELL,   // Zum Verkauf stellen
        RENT,   // Zur Miete stellen
        CANCEL  // Angebot beenden
    }

    private final String plotId;
    private final double price;
    private final SaleType type;

    public PlotSalePacket(String plotId, double price, SaleType type) {
        this.plotId = plotId;
        this.price = price;
        this.type = type;
    }

    /**
     * Encode - Schreibt Daten ins Packet
     */
    public static void encode(PlotSalePacket msg, FriendlyByteBuf buffer) {
        buffer.writeUtf(msg.plotId);
        buffer.writeDouble(msg.price);
        buffer.writeEnum(msg.type);
    }

    /**
     * Decode - Liest Daten aus Packet
     * SICHERHEIT: Max-Länge für plotId gegen DoS/Memory-Angriffe
     */
    public static PlotSalePacket decode(FriendlyByteBuf buffer) {
        String plotId = buffer.readUtf(256);
        double price = buffer.readDouble();
        SaleType type = buffer.readEnum(SaleType.class);
        return new PlotSalePacket(plotId, price, type);
    }

    /**
     * Handle - Verarbeitet Packet auf Server-Seite
     */
    public static void handle(PlotSalePacket msg, Supplier<NetworkEvent.Context> ctx) {
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

            switch (msg.type) {
                case SELL:
                    if (msg.price <= 0) {
                        player.sendSystemMessage(Component.literal("§cPreis muss größer als 0 sein!"));
                        return;
                    }
                    plot.setSalePrice(msg.price);
                    plot.setForSale(true);
                    plot.setForRent(false);
                    PlotManager.savePlots();
                    player.sendSystemMessage(Component.literal("§aPlot zum Verkauf gestellt für ")
                        .append(Component.literal(String.format("%.2f€", msg.price))
                            .withStyle(ChatFormatting.GOLD)));
                    break;

                case RENT:
                    if (msg.price <= 0) {
                        player.sendSystemMessage(Component.literal("§cPreis muss größer als 0 sein!"));
                        return;
                    }
                    plot.setRentPricePerDay(msg.price);
                    plot.setForRent(true);
                    plot.setForSale(false);
                    PlotManager.savePlots();
                    player.sendSystemMessage(Component.literal("§aPlot zur Miete gestellt für ")
                        .append(Component.literal(String.format("%.2f€/Tag", msg.price))
                            .withStyle(ChatFormatting.GOLD)));
                    break;

                case CANCEL:
                    plot.setForSale(false);
                    plot.setForRent(false);
                    PlotManager.savePlots();
                    player.sendSystemMessage(Component.literal("§aAngebot beendet - Plot ist jetzt privat"));
                    break;
            }
        });
    }
}
