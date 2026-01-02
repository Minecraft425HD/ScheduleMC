package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.config.ModConfigHandler;
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
 * Packet für Einzahlung auf Girokonto (Bargeld -> Konto)
 * Spieler legt Goldbarren/Diamanten/Smaragde ein und erhält Geld auf Konto
 */
public class BankDepositPacket {
    private final double amount;

    public BankDepositPacket(double amount) {
        this.amount = amount;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeDouble(amount);
    }

    public static BankDepositPacket decode(FriendlyByteBuf buf) {
        return new BankDepositPacket(buf.readDouble());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            // Prüfe Limit
            double depositLimit = ModConfigHandler.COMMON.BANK_DEPOSIT_LIMIT.get();
            if (amount > depositLimit) {
                player.sendSystemMessage(Component.literal("⚠ Einzahlungslimit überschritten! Maximum: ")
                    .withStyle(ChatFormatting.RED)
                    .append(Component.literal(String.format("%.2f€", depositLimit))
                        .withStyle(ChatFormatting.YELLOW)));
                return;
            }

            // Prüfe ob Spieler genug Wertgegenstände hat
            // 1 Goldbarren = 250€, 1 Diamant = 450€, 1 Smaragd = 180€
            int goldValue = 250;
            int diamondValue = 450;
            int emeraldValue = 180;

            // Berechne benötigte Items
            double remaining = amount;
            int goldNeeded = 0;
            int diamondsNeeded = 0;
            int emeraldsNeeded = 0;

            // Versuche mit Goldbarren zu zahlen
            if (remaining >= goldValue) {
                goldNeeded = (int) (remaining / goldValue);
                remaining -= goldNeeded * goldValue;
            }

            // Rest mit Diamanten
            if (remaining >= diamondValue) {
                diamondsNeeded = (int) (remaining / diamondValue);
                remaining -= diamondsNeeded * diamondValue;
            }

            // Rest mit Smaragden
            if (remaining >= emeraldValue) {
                emeraldsNeeded = (int) Math.ceil(remaining / emeraldValue);
                remaining = 0;
            }

            // Wenn noch Rest übrig, akzeptiere keine ungenaue Zahlung
            if (remaining > 0) {
                player.sendSystemMessage(Component.literal("⚠ Betrag kann nicht exakt mit verfügbaren Wertgegenständen dargestellt werden!")
                    .withStyle(ChatFormatting.RED));
                return;
            }

            // Prüfe ob Spieler genug Items hat
            int goldCount = countItems(player, Items.GOLD_INGOT);
            int diamondCount = countItems(player, Items.DIAMOND);
            int emeraldCount = countItems(player, Items.EMERALD);

            if (goldCount < goldNeeded || diamondCount < diamondsNeeded || emeraldCount < emeraldsNeeded) {
                player.sendSystemMessage(Component.literal("⚠ Nicht genug Wertgegenstände!")
                    .withStyle(ChatFormatting.RED));
                player.sendSystemMessage(Component.literal("Benötigt: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(goldNeeded + "x Goldbarren, " +
                        diamondsNeeded + "x Diamant, " + emeraldsNeeded + "x Smaragd")
                        .withStyle(ChatFormatting.YELLOW)));
                return;
            }

            // Entferne Items aus Inventar
            removeItems(player, Items.GOLD_INGOT, goldNeeded);
            removeItems(player, Items.DIAMOND, diamondsNeeded);
            removeItems(player, Items.EMERALD, emeraldsNeeded);

            // Zahle auf Konto ein
            EconomyManager.deposit(player.getUUID(), amount, TransactionType.ATM_DEPOSIT, "Bank-Einzahlung");

            // Erfolgs-Nachricht
            player.sendSystemMessage(Component.literal("═══════════════════════════════")
                .withStyle(ChatFormatting.GREEN));
            player.sendSystemMessage(Component.literal("🏦 ")
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.literal("EINZAHLUNG ERFOLGREICH")
                    .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
            player.sendSystemMessage(Component.literal("Betrag: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.format("+%.2f€", amount))
                    .withStyle(ChatFormatting.GOLD)));

            if (goldNeeded > 0 || diamondsNeeded > 0 || emeraldsNeeded > 0) {
                StringBuilder items = new StringBuilder();
                if (goldNeeded > 0) items.append(goldNeeded).append("x Goldbarren ");
                if (diamondsNeeded > 0) items.append(diamondsNeeded).append("x Diamant ");
                if (emeraldsNeeded > 0) items.append(emeraldsNeeded).append("x Smaragd");

                player.sendSystemMessage(Component.literal("Eingezahlt: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(items.toString().trim())
                        .withStyle(ChatFormatting.YELLOW)));
            }

            player.sendSystemMessage(Component.literal("Neuer Kontostand: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.format("%.2f€", EconomyManager.getBalance(player.getUUID())))
                    .withStyle(ChatFormatting.AQUA)));
            player.sendSystemMessage(Component.literal("═══════════════════════════════")
                .withStyle(ChatFormatting.GREEN));
        });
    }

    /**
     * Zählt Items im Inventar
     */
    private int countItems(net.minecraft.server.level.ServerPlayer player, net.minecraft.world.item.Item item) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == item) {
                count += stack.getCount();
            }
        }
        return count;
    }

    /**
     * Entfernt Items aus Inventar
     */
    private void removeItems(net.minecraft.server.level.ServerPlayer player, net.minecraft.world.item.Item item, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == item) {
                int toRemove = Math.min(stack.getCount(), remaining);
                stack.shrink(toRemove);
                remaining -= toRemove;
            }
        }
    }
}
