package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.economy.TransactionType;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet für Abhebung vom Girokonto (Konto -> Bargeld)
 * Spieler hebt Geld ab und erhält Goldbarren/Diamanten/Smaragde
 */
public class BankWithdrawPacket {
    private final double amount;

    public BankWithdrawPacket(double amount) {
        this.amount = amount;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeDouble(amount);
    }

    public static BankWithdrawPacket decode(FriendlyByteBuf buf) {
        return new BankWithdrawPacket(buf.readDouble());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            // Prüfe ob Spieler genug Geld hat
            double balance = EconomyManager.getBalance(player.getUUID());
            if (balance < amount) {
                player.sendSystemMessage(Component.literal("⚠ Nicht genug Guthaben!")
                    .withStyle(ChatFormatting.RED));
                player.sendSystemMessage(Component.literal("Verfügbar: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("%.2f€", balance))
                        .withStyle(ChatFormatting.YELLOW)));
                return;
            }

            // Konvertiere Betrag in Items
            // 1 Goldbarren = 250€, 1 Diamant = 450€, 1 Smaragd = 180€
            int goldValue = 250;
            int diamondValue = 450;
            int emeraldValue = 180;

            double remaining = amount;
            int goldToGive = 0;
            int diamondsToGive = 0;
            int emeraldsToGive = 0;

            // Optimale Aufteilung: Maximiere Goldbarren, dann Diamanten, dann Smaragde
            goldToGive = (int) (remaining / goldValue);
            remaining -= goldToGive * goldValue;

            if (remaining >= diamondValue) {
                diamondsToGive = (int) (remaining / diamondValue);
                remaining -= diamondsToGive * diamondValue;
            }

            if (remaining >= emeraldValue) {
                emeraldsToGive = (int) Math.ceil(remaining / emeraldValue);
                remaining = 0;
            }

            // Wenn noch Rest übrig, kann nicht exakt ausgezahlt werden
            if (remaining > 0) {
                player.sendSystemMessage(Component.literal("⚠ Betrag kann nicht exakt ausgezahlt werden!")
                    .withStyle(ChatFormatting.RED));
                player.sendSystemMessage(Component.literal("Tipp: Wählen Sie einen Betrag, der durch 250, 450 oder 180 teilbar ist")
                    .withStyle(ChatFormatting.GRAY));
                return;
            }

            // Prüfe ob genug Platz im Inventar
            int slotsNeeded = 0;
            if (goldToGive > 0) slotsNeeded += (goldToGive + 63) / 64;  // Goldbarren stacken bis 64
            if (diamondsToGive > 0) slotsNeeded += (diamondsToGive + 63) / 64;  // Diamanten stacken bis 64
            if (emeraldsToGive > 0) slotsNeeded += (emeraldsToGive + 63) / 64;  // Smaragde stacken bis 64

            int emptySlots = 0;
            for (int i = 0; i < 36; i++) {  // Nur Hauptinventar (ohne Rüstung/Offhand)
                if (player.getInventory().getItem(i).isEmpty()) {
                    emptySlots++;
                }
            }

            if (emptySlots < slotsNeeded) {
                player.sendSystemMessage(Component.literal("⚠ Nicht genug Platz im Inventar!")
                    .withStyle(ChatFormatting.RED));
                player.sendSystemMessage(Component.literal("Benötigt: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(slotsNeeded + " freie Slots")
                        .withStyle(ChatFormatting.YELLOW)));
                return;
            }

            // Ziehe Geld vom Konto ab
            if (!EconomyManager.withdraw(player.getUUID(), amount, TransactionType.ATM_WITHDRAW, "Bank-Abhebung")) {
                player.sendSystemMessage(Component.literal("⚠ Fehler bei der Abbuchung!")
                    .withStyle(ChatFormatting.RED));
                return;
            }

            // Gebe Items
            if (goldToGive > 0) {
                giveItems(player, Items.GOLD_INGOT, goldToGive);
            }
            if (diamondsToGive > 0) {
                giveItems(player, Items.DIAMOND, diamondsToGive);
            }
            if (emeraldsToGive > 0) {
                giveItems(player, Items.EMERALD, emeraldsToGive);
            }

            // Erfolgs-Nachricht
            player.sendSystemMessage(Component.literal("═══════════════════════════════")
                .withStyle(ChatFormatting.GREEN));
            player.sendSystemMessage(Component.literal("🏦 ")
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.literal("ABHEBUNG ERFOLGREICH")
                    .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
            player.sendSystemMessage(Component.literal("Betrag: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.format("-%.2f€", amount))
                    .withStyle(ChatFormatting.RED)));

            StringBuilder items = new StringBuilder();
            if (goldToGive > 0) items.append(goldToGive).append("x Goldbarren ");
            if (diamondsToGive > 0) items.append(diamondsToGive).append("x Diamant ");
            if (emeraldsToGive > 0) items.append(emeraldsToGive).append("x Smaragd");

            player.sendSystemMessage(Component.literal("Erhalten: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(items.toString().trim())
                    .withStyle(ChatFormatting.YELLOW)));

            player.sendSystemMessage(Component.literal("Neuer Kontostand: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.format("%.2f€", EconomyManager.getBalance(player.getUUID())))
                    .withStyle(ChatFormatting.AQUA)));
            player.sendSystemMessage(Component.literal("═══════════════════════════════")
                .withStyle(ChatFormatting.GREEN));
        });
    }

    /**
     * Gibt Items an Spieler
     */
    private void giveItems(net.minecraft.server.level.ServerPlayer player, net.minecraft.world.item.Item item, int amount) {
        int remaining = amount;
        while (remaining > 0) {
            int stackSize = Math.min(remaining, 64);
            ItemStack stack = new ItemStack(item, stackSize);
            player.getInventory().add(stack);
            remaining -= stackSize;
        }
    }
}
