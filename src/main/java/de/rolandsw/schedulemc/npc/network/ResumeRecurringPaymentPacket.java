package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.economy.RecurringPaymentManager;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet für Fortsetzen eines Dauerauftrags
 */
public class ResumeRecurringPaymentPacket {
    private final String paymentId;

    public ResumeRecurringPaymentPacket(String paymentId) {
        this.paymentId = paymentId;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(paymentId);
    }

    /**
     * SICHERHEIT: Max-Länge für Strings gegen DoS/Memory-Angriffe
     */
    public static ResumeRecurringPaymentPacket decode(FriendlyByteBuf buf) {
        return new ResumeRecurringPaymentPacket(buf.readUtf(64)); // Payment ID max 64 chars
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            // Validierung: PaymentId nicht leer
            if (paymentId == null || paymentId.trim().isEmpty()) {
                player.sendSystemMessage(Component.translatable("message.bank.invalid_payment_id")
                    .withStyle(ChatFormatting.RED));
                return;
            }

            // Setze Dauerauftrag fort
            RecurringPaymentManager manager = RecurringPaymentManager.getInstance(player.getServer());

            if (manager.resumeRecurringPayment(player.getUUID(), paymentId)) {
                // Erfolgs-Nachricht
                player.sendSystemMessage(Component.literal("▶ ")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.translatable("network.bank.order_resumed")
                        .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
                player.sendSystemMessage(Component.translatable("network.bank.id_label")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(paymentId)
                        .withStyle(ChatFormatting.DARK_GRAY)));
            } else {
                // Fehler
                player.sendSystemMessage(Component.translatable("message.bank.standing_order_not_found")
                    .withStyle(ChatFormatting.RED));
            }
        });
    }
}
