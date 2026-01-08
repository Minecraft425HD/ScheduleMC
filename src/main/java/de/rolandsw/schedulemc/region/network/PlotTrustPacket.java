package de.rolandsw.schedulemc.region.network;

import de.rolandsw.schedulemc.region.PlotManager;
import de.rolandsw.schedulemc.region.PlotRegion;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet für Plot-Trust-Verwaltung (Client → Server)
 */
public class PlotTrustPacket {

    public enum TrustAction {
        ADD,    // Spieler hinzufügen
        REMOVE  // Spieler entfernen
    }

    private final String plotId;
    private final String playerName;
    private final TrustAction action;

    public PlotTrustPacket(String plotId, String playerName, TrustAction action) {
        this.plotId = plotId;
        this.playerName = playerName;
        this.action = action;
    }

    /**
     * Encode - Schreibt Daten ins Packet
     */
    public static void encode(PlotTrustPacket msg, FriendlyByteBuf buffer) {
        buffer.writeUtf(msg.plotId);
        buffer.writeUtf(msg.playerName);
        buffer.writeEnum(msg.action);
    }

    /**
     * Decode - Liest Daten aus Packet
     * SICHERHEIT: Max-Länge für Strings gegen DoS/Memory-Angriffe
     */
    public static PlotTrustPacket decode(FriendlyByteBuf buffer) {
        String plotId = buffer.readUtf(256);
        String playerName = buffer.readUtf(16); // MC username max 16 chars
        TrustAction action = buffer.readEnum(TrustAction.class);
        return new PlotTrustPacket(plotId, playerName, action);
    }

    /**
     * Handle - Verarbeitet Packet auf Server-Seite
     */
    public static void handle(PlotTrustPacket msg, Supplier<NetworkEvent.Context> ctx) {
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

            // Validiere Spielername
            if (msg.playerName == null || msg.playerName.trim().isEmpty()) {
                player.sendSystemMessage(Component.translatable("message.plot.playername_empty"));
                return;
            }

            // Prüfe ob Spieler existiert (online oder offline via UserCache)
            com.mojang.authlib.GameProfile profile = player.server.getProfileCache().get(msg.playerName).orElse(null);
            java.util.UUID targetUUID = null;

            if (profile != null) {
                targetUUID = profile.getId();
            } else {
                // Spieler nicht gefunden
                player.sendSystemMessage(Component.translatable("message.plot.player_not_found_prefix")
                    .append(Component.literal(msg.playerName).withStyle(ChatFormatting.GOLD))
                    .append(Component.literal("' wurde nie auf dem Server gesehen!")));
                return;
            }

            switch (msg.action) {
                case ADD:
                    // Prüfe ob Spieler sich selbst trustet
                    if (msg.playerName.equalsIgnoreCase(player.getName().getString())) {
                        player.sendSystemMessage(Component.translatable("message.plot.cannot_trust_self"));
                        return;
                    }

                    if (plot.getTrustedPlayers().contains(targetUUID.toString())) {
                        player.sendSystemMessage(Component.literal("§e")
                            .append(Component.literal(msg.playerName).withStyle(ChatFormatting.GOLD))
                            .append(Component.translatable("message.plot.already_trusted_suffix")));
                        return;
                    }

                    plot.addTrustedPlayer(targetUUID);
                    PlotManager.savePlots();

                    player.sendSystemMessage(Component.literal("§a")
                        .append(Component.literal(msg.playerName).withStyle(ChatFormatting.GOLD))
                        .append(Component.translatable("message.plot.trust_added_suffix")));
                    break;

                case REMOVE:
                    if (!plot.getTrustedPlayers().contains(targetUUID.toString())) {
                        player.sendSystemMessage(Component.literal("§e")
                            .append(Component.literal(msg.playerName).withStyle(ChatFormatting.GOLD))
                            .append(Component.literal(" ist nicht in der Trust-Liste!")));
                        return;
                    }

                    plot.removeTrustedPlayer(targetUUID);
                    PlotManager.savePlots();

                    player.sendSystemMessage(Component.literal("§c")
                        .append(Component.literal(msg.playerName).withStyle(ChatFormatting.GOLD))
                        .append(Component.literal(" wurde aus der Trust-Liste entfernt!")));
                    break;
            }
        });
    }
}
