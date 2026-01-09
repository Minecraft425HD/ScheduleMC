package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.economy.CreditLoanManager;
import de.rolandsw.schedulemc.economy.RecurringPaymentInterval;
import de.rolandsw.schedulemc.economy.RecurringPaymentManager;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Packet für Erstellen eines Dauerauftrags
 */
public class CreateRecurringPaymentPacket {
    private final String recipientName;
    private final double amount;
    private final int intervalOrdinal;

    public CreateRecurringPaymentPacket(String recipientName, double amount, RecurringPaymentInterval interval) {
        this.recipientName = recipientName;
        this.amount = amount;
        this.intervalOrdinal = interval.ordinal();
    }

    private CreateRecurringPaymentPacket(String recipientName, double amount, int intervalOrdinal) {
        this.recipientName = recipientName;
        this.amount = amount;
        this.intervalOrdinal = intervalOrdinal;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(recipientName);
        buf.writeDouble(amount);
        buf.writeInt(intervalOrdinal);
    }

    /**
     * SICHERHEIT: Max-Länge für Strings gegen DoS/Memory-Angriffe
     */
    public static CreateRecurringPaymentPacket decode(FriendlyByteBuf buf) {
        return new CreateRecurringPaymentPacket(
            buf.readUtf(16), // MC username max 16 chars
            buf.readDouble(),
            buf.readInt()
        );
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            // Validierung: Betrag positiv
            if (amount <= 0) {
                player.sendSystemMessage(Component.translatable("message.bank.amount_must_positive")
                    .withStyle(ChatFormatting.RED));
                return;
            }

            // Validierung: Empfänger nicht leer
            if (recipientName == null || recipientName.trim().isEmpty()) {
                player.sendSystemMessage(Component.translatable("message.bank.recipient_required")
                    .withStyle(ChatFormatting.RED));
                return;
            }

            // Validierung: Intervall gültig
            if (intervalOrdinal < 0 || intervalOrdinal >= RecurringPaymentInterval.values().length) {
                player.sendSystemMessage(Component.translatable("message.bank.invalid_interval")
                    .withStyle(ChatFormatting.RED));
                return;
            }

            RecurringPaymentInterval interval = RecurringPaymentInterval.values()[intervalOrdinal];

            // 10er-Limit prüfen (inkl. Kredit-Daueraufträge)
            RecurringPaymentManager manager = RecurringPaymentManager.getInstance(player.getServer());
            CreditLoanManager loanManager = CreditLoanManager.getInstance(player.getServer());

            int currentPayments = manager.getPayments(player.getUUID()).size();
            int creditCount = loanManager.hasActiveLoan(player.getUUID()) ? 1 : 0;
            int totalCount = currentPayments + creditCount;
            int maxPerPlayer = ModConfigHandler.COMMON.RECURRING_MAX_PER_PLAYER.get();

            if (totalCount >= maxPerPlayer) {
                player.sendSystemMessage(Component.literal("═══════════════════════════════")
                    .withStyle(ChatFormatting.RED));
                player.sendSystemMessage(Component.translatable("message.common.limit_reached")
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                player.sendSystemMessage(Component.translatable("message.common.already_have_prefix")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(totalCount + "/" + maxPerPlayer)
                        .withStyle(ChatFormatting.YELLOW))
                    .append(Component.translatable("message.bank.active_orders_suffix")
                        .withStyle(ChatFormatting.GRAY)));
                if (creditCount > 0) {
                    player.sendSystemMessage(Component.translatable("message.bank.incl_loan_repayment")
                        .withStyle(ChatFormatting.GOLD));
                }
                player.sendSystemMessage(Component.translatable("message.bank.delete_existing_first")
                    .withStyle(ChatFormatting.YELLOW));
                player.sendSystemMessage(Component.literal("═══════════════════════════════")
                    .withStyle(ChatFormatting.RED));
                return;
            }

            // Empfänger-UUID finden
            UUID recipientUUID = null;
            for (ServerPlayer p : player.getServer().getPlayerList().getPlayers()) {
                if (p.getName().getString().equalsIgnoreCase(recipientName)) {
                    recipientUUID = p.getUUID();
                    break;
                }
            }

            // Prüfe ob Empfänger existiert
            if (recipientUUID == null) {
                player.sendSystemMessage(Component.translatable("message.common.player_not_found")
                    .withStyle(ChatFormatting.RED));
                player.sendSystemMessage(Component.translatable("message.common.name_label")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(recipientName)
                        .withStyle(ChatFormatting.YELLOW)));
                return;
            }

            // Erstelle Dauerauftrag
            String description = interval.getDisplayName() + " an " + recipientName;

            if (manager.createRecurringPayment(player.getUUID(), recipientUUID, amount,
                interval.getDaysInterval(), description)) {
                // Erfolgs-Nachricht
                player.sendSystemMessage(Component.literal("═══════════════════════════════")
                    .withStyle(ChatFormatting.GREEN));
                player.sendSystemMessage(Component.literal("📋 ")
                    .withStyle(ChatFormatting.GOLD)
                    .append(Component.translatable("message.bank.standing_order_created")
                        .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
                player.sendSystemMessage(Component.translatable("message.bank.recipient_label")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(recipientName)
                        .withStyle(ChatFormatting.AQUA)));
                player.sendSystemMessage(Component.translatable("message.bank.amount_label")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("%.2f€", amount))
                        .withStyle(ChatFormatting.GOLD)));
                player.sendSystemMessage(Component.translatable("message.bank.interval_label")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(interval.getDisplayName())
                        .withStyle(ChatFormatting.LIGHT_PURPLE)));
                player.sendSystemMessage(Component.literal("═══════════════════════════════")
                    .withStyle(ChatFormatting.GREEN));
            } else {
                // Fehler
                player.sendSystemMessage(Component.translatable("message.bank.standing_order_failed")
                    .withStyle(ChatFormatting.RED));
                player.sendSystemMessage(Component.translatable("message.common.possible_reasons")
                    .withStyle(ChatFormatting.GRAY));
                player.sendSystemMessage(Component.translatable("network.bank.max_count_reached")
                    .withStyle(ChatFormatting.YELLOW));
                player.sendSystemMessage(Component.translatable("message.bank.invalid_parameters")
                    .withStyle(ChatFormatting.YELLOW));
            }
        });
    }
}
