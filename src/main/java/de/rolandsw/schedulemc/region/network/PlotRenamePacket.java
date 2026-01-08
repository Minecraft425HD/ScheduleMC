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
 * Packet für Plot-Umbenennung (Client → Server)
 */
public class PlotRenamePacket {

    private final String plotId;
    private final String newName;

    public PlotRenamePacket(String plotId, String newName) {
        this.plotId = plotId;
        this.newName = newName;
    }

    /**
     * Encode - Schreibt Daten ins Packet
     */
    public static void encode(PlotRenamePacket msg, FriendlyByteBuf buffer) {
        buffer.writeUtf(msg.plotId);
        buffer.writeUtf(msg.newName);
    }

    /**
     * Decode - Liest Daten aus Packet
     * SICHERHEIT: Max-Länge für Strings gegen DoS/Memory-Angriffe
     */
    public static PlotRenamePacket decode(FriendlyByteBuf buffer) {
        String plotId = buffer.readUtf(256);
        String newName = buffer.readUtf(32);
        return new PlotRenamePacket(plotId, newName);
    }

    /**
     * Handle - Verarbeitet Packet auf Server-Seite
     */
    public static void handle(PlotRenamePacket msg, Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            PlotRegion plot = PlotManager.getPlot(msg.plotId);

            if (plot == null) {
                player.sendSystemMessage(Component.translatable("message.plot.not_found"));
                return;
            }

            // Prüfe ob Spieler Besitzer ist
            if (!plot.getOwnerUUID().equals(player.getUUID().toString())) {
                player.sendSystemMessage(Component.translatable("message.plot.not_owner"));
                return;
            }

            // Validiere Name
            if (msg.newName == null || msg.newName.trim().isEmpty()) {
                player.sendSystemMessage(Component.translatable("message.plot.name_empty"));
                return;
            }

            if (msg.newName.length() > 32) {
                player.sendSystemMessage(Component.translatable("message.plot.name_too_long"));
                return;
            }

            String oldName = plot.getPlotName();
            plot.setPlotName(msg.newName.trim());
            PlotManager.savePlots();

            player.sendSystemMessage(Component.translatable("message.plot.renamed")
                .append(Component.literal(oldName).withStyle(ChatFormatting.GRAY))
                .append(Component.literal(" §a→ "))
                .append(Component.literal(msg.newName).withStyle(ChatFormatting.GOLD)));
        });
    }
}
