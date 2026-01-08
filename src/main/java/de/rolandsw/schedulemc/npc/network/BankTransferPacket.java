package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.economy.TransactionType;
import de.rolandsw.schedulemc.npc.bank.TransferLimitTracker;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Packet für Überweisung zwischen Spielern
 */
public class BankTransferPacket {
    private final String targetPlayerName;
    private final double amount;

    public BankTransferPacket(String targetPlayerName, double amount) {
        this.targetPlayerName = targetPlayerName;
        this.amount = amount;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(targetPlayerName);
        buf.writeDouble(amount);
    }

    /**
     * SICHERHEIT: Max-Länge für playerName gegen DoS/Memory-Angriffe
     */
    public static BankTransferPacket decode(FriendlyByteBuf buf) {
        return new BankTransferPacket(
            buf.readUtf(16), // MC username max 16 chars
            buf.readDouble()
        );
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            // Prüfe ob Betrag positiv
            if (amount <= 0) {
                player.sendSystemMessage(Component.literal("⚠ Betrag muss positiv sein!")
                    .withStyle(ChatFormatting.RED));
                return;
            }

            // Prüfe tägliches Transfer-Limit
            TransferLimitTracker tracker = TransferLimitTracker.getInstance(player.server);
            double remaining = tracker.getRemainingLimit(player.getUUID());

            if (amount > remaining) {
                double dailyLimit = ModConfigHandler.COMMON.BANK_TRANSFER_DAILY_LIMIT.get();
                player.sendSystemMessage(Component.literal("⚠ Tägliches Transfer-Limit überschritten!")
                    .withStyle(ChatFormatting.RED));
                player.sendSystemMessage(Component.literal("Limit: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("%.2f€", dailyLimit))
                        .withStyle(ChatFormatting.YELLOW)));
                player.sendSystemMessage(Component.translatable("message.bank.today_transferred")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("%.2f€", dailyLimit - remaining))
                        .withStyle(ChatFormatting.YELLOW)));
                player.sendSystemMessage(Component.translatable("message.stock.still_available")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("%.2f€", remaining))
                        .withStyle(ChatFormatting.YELLOW)));
                return;
            }

            // Finde Ziel-Spieler
            ServerPlayer targetPlayer = player.server.getPlayerList().getPlayerByName(targetPlayerName);
            if (targetPlayer == null) {
                player.sendSystemMessage(Component.literal("⚠ Spieler nicht gefunden: ")
                    .withStyle(ChatFormatting.RED)
                    .append(Component.literal(targetPlayerName)
                        .withStyle(ChatFormatting.YELLOW)));
                return;
            }

            UUID targetUUID = targetPlayer.getUUID();

            // Prüfe ob nicht an sich selbst
            if (player.getUUID().equals(targetUUID)) {
                player.sendSystemMessage(Component.literal("⚠ Sie können nicht an sich selbst überweisen!")
                    .withStyle(ChatFormatting.RED));
                return;
            }

            // Erstelle Ziel-Konto falls nicht vorhanden
            if (!EconomyManager.hasAccount(targetUUID)) {
                EconomyManager.createAccount(targetUUID);
            }

            // HINWEIS: Balance-Prüfung erfolgt atomar in EconomyManager.transfer()
            // Separate Prüfung entfernt wegen TOCTOU Race Condition

            // Führe Transfer durch
            String description = "Überweisung an " + targetPlayerName;
            if (EconomyManager.transfer(player.getUUID(), targetUUID, amount, description)) {
                // Update Transfer-Tracker
                tracker.checkAndUpdateLimit(player.getUUID(), amount);

                // Erfolgs-Nachricht an Sender
                player.sendSystemMessage(Component.literal("═══════════════════════════════")
                    .withStyle(ChatFormatting.GREEN));
                player.sendSystemMessage(Component.literal("💸 ")
                    .withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal("ÜBERWEISUNG ERFOLGREICH")
                        .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
                player.sendSystemMessage(Component.translatable("message.bank.recipient_label")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(targetPlayerName)
                        .withStyle(ChatFormatting.YELLOW)));
                player.sendSystemMessage(Component.literal("Betrag: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("-%.2f€", amount))
                        .withStyle(ChatFormatting.RED)));
                player.sendSystemMessage(Component.literal("Neuer Kontostand: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("%.2f€", EconomyManager.getBalance(player.getUUID())))
                        .withStyle(ChatFormatting.AQUA)));

                double newRemaining = tracker.getRemainingLimit(player.getUUID());
                player.sendSystemMessage(Component.literal("Verbleibendes Tageslimit: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("%.2f€", newRemaining))
                        .withStyle(ChatFormatting.YELLOW)));
                player.sendSystemMessage(Component.literal("═══════════════════════════════")
                    .withStyle(ChatFormatting.GREEN));

                // Benachrichtigung an Empfänger
                targetPlayer.sendSystemMessage(Component.literal("═══════════════════════════════")
                    .withStyle(ChatFormatting.GREEN));
                targetPlayer.sendSystemMessage(Component.literal("💰 ")
                    .withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal("GELD ERHALTEN")
                        .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
                targetPlayer.sendSystemMessage(Component.literal("Von: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(player.getName().getString())
                        .withStyle(ChatFormatting.YELLOW)));
                targetPlayer.sendSystemMessage(Component.literal("Betrag: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("+%.2f€", amount))
                        .withStyle(ChatFormatting.GREEN)));
                targetPlayer.sendSystemMessage(Component.literal("Neuer Kontostand: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("%.2f€", EconomyManager.getBalance(targetUUID)))
                        .withStyle(ChatFormatting.AQUA)));
                targetPlayer.sendSystemMessage(Component.literal("═══════════════════════════════")
                    .withStyle(ChatFormatting.GREEN));
            } else {
                // Atomare Prüfung fehlgeschlagen - nicht genug Geld
                player.sendSystemMessage(Component.literal("⚠ Überweisung fehlgeschlagen - nicht genug Guthaben!")
                    .withStyle(ChatFormatting.RED));
                player.sendSystemMessage(Component.translatable("message.bank.available")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("%.2f€", EconomyManager.getBalance(player.getUUID())))
                        .withStyle(ChatFormatting.YELLOW)));
            }
        });
    }
}
