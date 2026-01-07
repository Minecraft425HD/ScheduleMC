package de.rolandsw.schedulemc.npc.network;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.economy.TransactionType;
import de.rolandsw.schedulemc.npc.bank.TransferLimitTracker;
import de.rolandsw.schedulemc.util.PacketHandler;
import de.rolandsw.schedulemc.util.RateLimiter;
import de.rolandsw.schedulemc.util.StringUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Packet für Überweisung zwischen Spielern
 * SICHERHEIT: Audit-Logging für alle Transfer-Versuche + Rate Limiting
 */
public class BankTransferPacket {

    private static final Logger LOGGER = LogUtils.getLogger();
    // SICHERHEIT: Rate Limiting - Max 5 transfers per second (in addition to daily limits)
    private static final RateLimiter TRANSFER_RATE_LIMITER = new RateLimiter("bankTransfer", 5);

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
     * + Input-Sanitization gegen Command-Injection
     */
    public static BankTransferPacket decode(FriendlyByteBuf buf) {
        return new BankTransferPacket(
            StringUtils.sanitizeUserInput(buf.readUtf(16)), // MC username max 16 chars + SANITIZED
            buf.readDouble()
        );
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            // SICHERHEIT: Rate Limiting - prevent transfer spam/DoS attacks
            if (!TRANSFER_RATE_LIMITER.allowOperation(player.getUUID())) {
                LOGGER.warn("[AUDIT] Transfer BLOCKED - Rate limit exceeded: Player={}",
                    player.getName().getString());
                player.sendSystemMessage(Component.literal("⚠ Zu viele Überweisungen! Bitte langsamer.")
                    .withStyle(ChatFormatting.RED));
                return;
            }

            // Prüfe ob Betrag positiv
            if (amount <= 0) {
                // AUDIT: Log fehlgeschlagenen Transfer
                LOGGER.warn("[AUDIT] Transfer FAILED - Invalid amount: Player={}, Target={}, Amount={}",
                    player.getName().getString(), targetPlayerName, amount);
                player.sendSystemMessage(Component.literal("⚠ Betrag muss positiv sein!")
                    .withStyle(ChatFormatting.RED));
                return;
            }

            // Prüfe tägliches Transfer-Limit
            TransferLimitTracker tracker = TransferLimitTracker.getInstance(player.server);
            double remaining = tracker.getRemainingLimit(player.getUUID());

            if (amount > remaining) {
                double dailyLimit = ModConfigHandler.COMMON.BANK_TRANSFER_DAILY_LIMIT.get();
                // AUDIT: Log Limit-Überschreitung
                LOGGER.warn("[AUDIT] Transfer BLOCKED - Limit exceeded: Player={}, Target={}, Amount={}, Remaining={}",
                    player.getName().getString(), targetPlayerName, amount, remaining);
                player.sendSystemMessage(Component.literal("⚠ Tägliches Transfer-Limit überschritten!")
                    .withStyle(ChatFormatting.RED));
                player.sendSystemMessage(Component.literal("Limit: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("%.2f€", dailyLimit))
                        .withStyle(ChatFormatting.YELLOW)));
                player.sendSystemMessage(Component.literal("Heute bereits überwiesen: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("%.2f€", dailyLimit - remaining))
                        .withStyle(ChatFormatting.YELLOW)));
                player.sendSystemMessage(Component.literal("Noch verfügbar: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("%.2f€", remaining))
                        .withStyle(ChatFormatting.YELLOW)));
                return;
            }

            // Finde Ziel-Spieler
            ServerPlayer targetPlayer = player.server.getPlayerList().getPlayerByName(targetPlayerName);
            if (targetPlayer == null) {
                // AUDIT: Log fehlgeschlagenen Transfer (Player nicht gefunden)
                LOGGER.warn("[AUDIT] Transfer FAILED - Target not found: Player={}, Target={}, Amount={}",
                    player.getName().getString(), targetPlayerName, amount);
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
                // AUDIT: Log erfolgreichen Transfer
                LOGGER.info("[AUDIT] Transfer SUCCESS: From={} ({}) -> To={} ({}) Amount={}€",
                    player.getName().getString(), player.getUUID(),
                    targetPlayerName, targetUUID, String.format("%.2f", amount));

                // Update Transfer-Tracker
                tracker.checkAndUpdateLimit(player.getUUID(), amount);

                // Erfolgs-Nachricht an Sender
                player.sendSystemMessage(Component.literal("═══════════════════════════════")
                    .withStyle(ChatFormatting.GREEN));
                player.sendSystemMessage(Component.literal("💸 ")
                    .withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal("ÜBERWEISUNG ERFOLGREICH")
                        .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
                player.sendSystemMessage(Component.literal("Empfänger: ")
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
                player.sendSystemMessage(Component.literal("Verfügbar: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("%.2f€", EconomyManager.getBalance(player.getUUID())))
                        .withStyle(ChatFormatting.YELLOW)));
            }
        });
    }
}
