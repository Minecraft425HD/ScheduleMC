package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.economy.RecurringPaymentManager;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet für Pausieren eines Dauerauftrags
 */
public class PauseRecurringPaymentPacket {
    private final String paymentId;

    public PauseRecurringPaymentPacket(String paymentId) {
        this.paymentId = paymentId;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(paymentId);
    }

    public static PauseRecurringPaymentPacket decode(FriendlyByteBuf buf) {
        return new PauseRecurringPaymentPacket(buf.readUtf());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            // Validierung: PaymentId nicht leer
            if (paymentId == null || paymentId.trim().isEmpty()) {
                player.sendSystemMessage(Component.literal("⚠ Ungültige Dauerauftrag-ID!")
                    .withStyle(ChatFormatting.RED));
                return;
            }

            // Pausiere Dauerauftrag
            RecurringPaymentManager manager = RecurringPaymentManager.getInstance(player.getServer());

            if (manager.pauseRecurringPayment(player.getUUID(), paymentId)) {
                // Erfolgs-Nachricht
                player.sendSystemMessage(Component.literal("⏸ ")
                    .withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal("Dauerauftrag pausiert")
                        .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)));
                player.sendSystemMessage(Component.literal("ID: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(paymentId)
                        .withStyle(ChatFormatting.DARK_GRAY)));
            } else {
                // Fehler
                player.sendSystemMessage(Component.literal("⚠ Dauerauftrag nicht gefunden!")
                    .withStyle(ChatFormatting.RED));
            }
        });
    }
}
