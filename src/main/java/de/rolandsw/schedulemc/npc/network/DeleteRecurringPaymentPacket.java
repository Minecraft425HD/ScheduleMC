package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.economy.RecurringPaymentManager;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet für Löschen eines Dauerauftrags
 */
public class DeleteRecurringPaymentPacket {
    private final String paymentId;

    public DeleteRecurringPaymentPacket(String paymentId) {
        this.paymentId = paymentId;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(paymentId);
    }

    /**
     * SICHERHEIT: Max-Länge für Strings gegen DoS/Memory-Angriffe
     */
    public static DeleteRecurringPaymentPacket decode(FriendlyByteBuf buf) {
        return new DeleteRecurringPaymentPacket(buf.readUtf(64)); // Payment ID max 64 chars
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            // Validierung: PaymentId nicht leer
            if (paymentId == null || paymentId.trim().isEmpty()) {
                player.sendSystemMessage(Component.translatable("message.bank.invalid_payment_id")
                    .withStyle(ChatFormatting.RED));
                return;
            }

            // Lösche Dauerauftrag
            RecurringPaymentManager manager = RecurringPaymentManager.getInstance(player.getServer());

            if (manager.deleteRecurringPayment(player.getUUID(), paymentId)) {
                // Erfolgs-Nachricht
                player.sendSystemMessage(Component.literal("═══════════════════════════════")
                    .withStyle(ChatFormatting.YELLOW));
                player.sendSystemMessage(Component.literal("🗑 ")
                    .withStyle(ChatFormatting.RED)
                    .append(Component.translatable("message.bank.order_deleted")
                        .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)));
                player.sendSystemMessage(Component.literal("ID: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(paymentId)
                        .withStyle(ChatFormatting.DARK_GRAY)));
                player.sendSystemMessage(Component.literal("═══════════════════════════════")
                    .withStyle(ChatFormatting.YELLOW));
            } else {
                // Fehler
                player.sendSystemMessage(Component.literal("⚠ Dauerauftrag nicht gefunden!")
                    .withStyle(ChatFormatting.RED));
                player.sendSystemMessage(Component.literal("ID: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(paymentId)
                        .withStyle(ChatFormatting.YELLOW)));
            }
        });
    }
}
