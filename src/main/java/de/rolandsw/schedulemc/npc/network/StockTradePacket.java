package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.economy.TransactionType;
import de.rolandsw.schedulemc.npc.bank.StockMarketData;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet für Börsenhandel (Kauf/Verkauf von Gold/Diamant/Smaragd)
 */
public class StockTradePacket {
    private final TradeType tradeType;  // BUY oder SELL
    private final StockType stockType;  // GOLD, DIAMOND, EMERALD
    private final int quantity;

    public enum TradeType {
        BUY, SELL
    }

    public enum StockType {
        GOLD(Items.GOLD_INGOT, "Goldbarren"),
        DIAMOND(Items.DIAMOND, "Diamant"),
        EMERALD(Items.EMERALD, "Smaragd");

        private final Item item;
        private final String displayName;

        StockType(Item item, String displayName) {
            this.item = item;
            this.displayName = displayName;
        }

        public Item getItem() {
            return item;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public StockTradePacket(TradeType tradeType, StockType stockType, int quantity) {
        this.tradeType = tradeType;
        this.stockType = stockType;
        this.quantity = quantity;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(tradeType);
        buf.writeEnum(stockType);
        buf.writeInt(quantity);
    }

    public static StockTradePacket decode(FriendlyByteBuf buf) {
        return new StockTradePacket(
            buf.readEnum(TradeType.class),
            buf.readEnum(StockType.class),
            buf.readInt()
        );
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            if (quantity <= 0) {
                player.sendSystemMessage(Component.translatable("message.common.quantity_positive")
                    .withStyle(ChatFormatting.RED));
                return;
            }

            StockMarketData stockMarket = StockMarketData.getInstance(player.server);
            Item tradedItem = stockType.getItem();
            double currentPrice = stockMarket.getCurrentPrice(tradedItem);
            double totalCost = currentPrice * quantity;

            if (tradeType == TradeType.BUY) {
                handleBuy(player, tradedItem, stockType.getDisplayName(), quantity, currentPrice, totalCost);
            } else {
                handleSell(player, tradedItem, stockType.getDisplayName(), quantity, currentPrice, totalCost);
            }
        });
    }

    /**
     * Verarbeitet Kauf
     */
    private void handleBuy(net.minecraft.server.level.ServerPlayer player, Item item, String itemName,
                          int quantity, double pricePerUnit, double totalCost) {
        // Prüfe ob Spieler genug Geld hat
        double balance = EconomyManager.getBalance(player.getUUID());
        if (balance < totalCost) {
            player.sendSystemMessage(Component.translatable("message.bank.insufficient_balance")
                .withStyle(ChatFormatting.RED));
            player.sendSystemMessage(Component.translatable("message.bank.required")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.format("%.2f€", totalCost))
                    .withStyle(ChatFormatting.YELLOW)));
            player.sendSystemMessage(Component.translatable("message.bank.available")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.format("%.2f€", balance))
                    .withStyle(ChatFormatting.YELLOW)));
            return;
        }

        // Prüfe ob genug Platz im Inventar
        int slotsNeeded = (quantity + 63) / 64;  // Items stacken bis 64
        int emptySlots = 0;
        for (int i = 0; i < 36; i++) {  // Nur Hauptinventar
            if (player.getInventory().getItem(i).isEmpty()) {
                emptySlots++;
            }
        }

        if (emptySlots < slotsNeeded) {
            player.sendSystemMessage(Component.translatable("message.common.no_inventory_space")
                .withStyle(ChatFormatting.RED));
            player.sendSystemMessage(Component.translatable("message.bank.required")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(slotsNeeded + " freie Slots")
                    .withStyle(ChatFormatting.YELLOW)));
            return;
        }

        // Ziehe Geld ab
        if (!EconomyManager.withdraw(player.getUUID(), totalCost, TransactionType.NPC_PURCHASE,
                "Börsenkauf: " + quantity + "x " + itemName)) {
            player.sendSystemMessage(Component.translatable("message.bank.debit_error")
                .withStyle(ChatFormatting.RED));
            return;
        }

        // Gebe Items
        int remaining = quantity;
        while (remaining > 0) {
            int stackSize = Math.min(remaining, 64);
            ItemStack stack = new ItemStack(item, stackSize);
            player.getInventory().add(stack);
            remaining -= stackSize;
        }

        // Erfolgs-Nachricht
        player.sendSystemMessage(Component.literal("═══════════════════════════════")
            .withStyle(ChatFormatting.GREEN));
        player.sendSystemMessage(Component.literal("📈 ")
            .withStyle(ChatFormatting.YELLOW)
            .append(Component.translatable("message.stock.purchase_successful"))
                .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
        player.sendSystemMessage(Component.translatable("message.stock.bought_label")
            .withStyle(ChatFormatting.GRAY)
            .append(Component.literal(quantity + "x " + itemName)
                .withStyle(ChatFormatting.YELLOW)));
        player.sendSystemMessage(Component.translatable("message.stock.price_per_unit")
            .withStyle(ChatFormatting.GRAY)
            .append(Component.literal(String.format("%.2f€", pricePerUnit))
                .withStyle(ChatFormatting.GOLD)));
        player.sendSystemMessage(Component.literal("Gesamtkosten: ")
            .withStyle(ChatFormatting.GRAY)
            .append(Component.literal(String.format("-%.2f€", totalCost))
                .withStyle(ChatFormatting.RED)));
        player.sendSystemMessage(Component.translatable("message.bank.new_balance_label")
            .withStyle(ChatFormatting.GRAY)
            .append(Component.literal(String.format("%.2f€", EconomyManager.getBalance(player.getUUID())))
                .withStyle(ChatFormatting.AQUA)));
        player.sendSystemMessage(Component.literal("═══════════════════════════════")
            .withStyle(ChatFormatting.GREEN));
    }

    /**
     * Verarbeitet Verkauf
     */
    private void handleSell(net.minecraft.server.level.ServerPlayer player, Item item, String itemName,
                           int quantity, double pricePerUnit, double totalRevenue) {
        // Prüfe ob Spieler genug Items hat
        int itemCount = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == item) {
                itemCount += stack.getCount();
            }
        }

        if (itemCount < quantity) {
            player.sendSystemMessage(Component.translatable("message.common.not_enough_items")
                .withStyle(ChatFormatting.RED));
            player.sendSystemMessage(Component.translatable("message.bank.required")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(quantity + "x " + itemName)
                    .withStyle(ChatFormatting.YELLOW)));
            player.sendSystemMessage(Component.translatable("message.bank.available")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(itemCount + "x")
                    .withStyle(ChatFormatting.YELLOW)));
            return;
        }

        // Entferne Items aus Inventar
        int remaining = quantity;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == item) {
                int toRemove = Math.min(stack.getCount(), remaining);
                stack.shrink(toRemove);
                remaining -= toRemove;
            }
        }

        // Zahle Geld ein
        EconomyManager.deposit(player.getUUID(), totalRevenue, TransactionType.SHOP_PAYOUT,
            "Börsenverkauf: " + quantity + "x " + itemName);

        // Erfolgs-Nachricht
        player.sendSystemMessage(Component.literal("═══════════════════════════════")
            .withStyle(ChatFormatting.GREEN));
        player.sendSystemMessage(Component.literal("📉 ")
            .withStyle(ChatFormatting.YELLOW)
            .append(Component.translatable("message.stock.sale_successful"))
                .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
        player.sendSystemMessage(Component.translatable("message.stock.sold_label")
            .withStyle(ChatFormatting.GRAY)
            .append(Component.literal(quantity + "x " + itemName)
                .withStyle(ChatFormatting.YELLOW)));
        player.sendSystemMessage(Component.translatable("message.stock.price_per_unit")
            .withStyle(ChatFormatting.GRAY)
            .append(Component.literal(String.format("%.2f€", pricePerUnit))
                .withStyle(ChatFormatting.GOLD)));
        player.sendSystemMessage(Component.translatable("message.stock.total_proceeds")
            .withStyle(ChatFormatting.GRAY)
            .append(Component.literal(String.format("+%.2f€", totalRevenue))
                .withStyle(ChatFormatting.GREEN)));
        player.sendSystemMessage(Component.translatable("message.bank.new_balance_label")
            .withStyle(ChatFormatting.GRAY)
            .append(Component.literal(String.format("%.2f€", EconomyManager.getBalance(player.getUUID())))
                .withStyle(ChatFormatting.AQUA)));
        player.sendSystemMessage(Component.literal("═══════════════════════════════")
            .withStyle(ChatFormatting.GREEN));
    }
}
