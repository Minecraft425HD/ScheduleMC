package de.rolandsw.schedulemc.region.network;

import de.rolandsw.schedulemc.config.ModConfigHandler;
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
                player.sendSystemMessage(Component.translatable("message.plot.not_found"));
                return;
            }

            // Prüfe ob Spieler Besitzer ist (null-safe: player.getUUID() ist nie null)
            if (!player.getUUID().toString().equals(plot.getOwnerUUID())) {
                player.sendSystemMessage(Component.translatable("message.plot.not_owner"));
                return;
            }

            switch (msg.type) {
                case SELL:
                    if (msg.price <= 0) {
                        player.sendSystemMessage(Component.translatable("message.plot.price_invalid"));
                        return;
                    }
                    if (msg.price < ModConfigHandler.COMMON.MIN_PLOT_PRICE.get()
                            || msg.price > ModConfigHandler.COMMON.MAX_PLOT_PRICE.get()) {
                        player.sendSystemMessage(Component.translatable("message.plot.price_out_of_range",
                            String.format("%.2f", ModConfigHandler.COMMON.MIN_PLOT_PRICE.get()),
                            String.format("%.2f", ModConfigHandler.COMMON.MAX_PLOT_PRICE.get())));
                        return;
                    }
                    plot.setSalePrice(msg.price);
                    plot.setForSale(true);
                    plot.setForRent(false);
                    PlotManager.savePlots();
                    player.sendSystemMessage(Component.translatable("message.plot.listed_for_sale")
                        .append(Component.literal(String.format("%.2f€", msg.price))
                            .withStyle(ChatFormatting.GOLD)));
                    break;

                case RENT:
                    if (msg.price <= 0) {
                        player.sendSystemMessage(Component.translatable("message.plot.price_invalid"));
                        return;
                    }
                    if (msg.price < ModConfigHandler.COMMON.MIN_RENT_PRICE.get()) {
                        player.sendSystemMessage(Component.translatable("message.plot.rent_price_too_low",
                            String.format("%.2f", ModConfigHandler.COMMON.MIN_RENT_PRICE.get())));
                        return;
                    }
                    plot.setRentPricePerDay(msg.price);
                    plot.setForRent(true);
                    plot.setForSale(false);
                    PlotManager.savePlots();
                    player.sendSystemMessage(Component.translatable("message.plot.rent_listed", msg.price));
                    break;

                case CANCEL:
                    plot.setForSale(false);
                    plot.setForRent(false);
                    PlotManager.savePlots();
                    player.sendSystemMessage(Component.translatable("message.plot.offer_ended"));
                    break;
            }
        });
    }
}
