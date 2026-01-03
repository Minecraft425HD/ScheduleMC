package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.economy.CreditLoan;
import de.rolandsw.schedulemc.economy.CreditLoanManager;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet zum vorzeitigen Zurückzahlen eines Kredits
 * Client → Server
 */
public class RepayCreditLoanPacket {

    public RepayCreditLoanPacket() {
    }

    public void encode(FriendlyByteBuf buf) {
        // Keine Daten nötig - Spieler-UUID kommt aus Context
    }

    public static RepayCreditLoanPacket decode(FriendlyByteBuf buf) {
        return new RepayCreditLoanPacket();
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            CreditLoanManager loanManager = CreditLoanManager.getInstance(player.getServer());

            // Prüfe ob aktiver Kredit existiert
            CreditLoan loan = loanManager.getLoan(player.getUUID());
            if (loan == null) {
                player.sendSystemMessage(Component.literal(
                    "§c§lFehler: §7Du hast keinen aktiven Kredit!"
                ));
                return;
            }

            double remaining = loan.getRemaining();
            double balance = EconomyManager.getBalance(player.getUUID());

            // Prüfe ob genug Geld vorhanden
            if (balance < remaining) {
                player.sendSystemMessage(Component.literal(
                    "§c§lNicht genug Geld!\n" +
                    "§7Benötigt: §c" + String.format("%.2f€", remaining) + "\n" +
                    "§7Kontostand: §e" + String.format("%.2f€", balance)
                ));
                return;
            }

            // Führe Rückzahlung durch
            if (loanManager.repayLoan(player.getUUID())) {
                player.sendSystemMessage(Component.literal("═══════════════════════════════")
                    .withStyle(ChatFormatting.GREEN));
                player.sendSystemMessage(Component.literal("🏦 ")
                    .withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal("KREDIT ABBEZAHLT!")
                        .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
                player.sendSystemMessage(Component.literal("Betrag: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("-%.2f€", remaining))
                        .withStyle(ChatFormatting.RED)));
                player.sendSystemMessage(Component.literal("Neuer Kontostand: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("%.2f€", EconomyManager.getBalance(player.getUUID())))
                        .withStyle(ChatFormatting.GOLD)));
                player.sendSystemMessage(Component.literal("§a§lDu bist nun schuldenfrei!"));
                player.sendSystemMessage(Component.literal("═══════════════════════════════")
                    .withStyle(ChatFormatting.GREEN));
            } else {
                player.sendSystemMessage(Component.literal(
                    "§c§lFehler: §7Kredit konnte nicht zurückgezahlt werden!"
                ));
            }
        });
    }
}
