package de.rolandsw.schedulemc.tobacco.network;

import de.rolandsw.schedulemc.economy.WalletManager;
import de.rolandsw.schedulemc.economy.items.CashItem;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.tobacco.business.NPCBusinessMetrics;
import de.rolandsw.schedulemc.tobacco.business.NPCResponse;
import de.rolandsw.schedulemc.tobacco.business.NegotiationEngine;
import de.rolandsw.schedulemc.tobacco.items.PackagedTobaccoItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet für Tabak-Verhandlung
 */
public class NegotiationPacket {
    private final int npcEntityId;
    private final int playerSlot;
    private final double offeredPrice;
    private final int offeredGrams;  // NEU: Anzahl Gramm zum Verkaufen

    public NegotiationPacket(int npcEntityId, int playerSlot, double offeredPrice, int offeredGrams) {
        this.npcEntityId = npcEntityId;
        this.playerSlot = playerSlot;
        this.offeredPrice = offeredPrice;
        this.offeredGrams = offeredGrams;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(npcEntityId);
        buf.writeInt(playerSlot);
        buf.writeDouble(offeredPrice);
        buf.writeInt(offeredGrams);
    }

    public static NegotiationPacket decode(FriendlyByteBuf buf) {
        return new NegotiationPacket(
            buf.readInt(),
            buf.readInt(),
            buf.readDouble(),
            buf.readInt()
        );
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            Entity entity = player.level().getEntity(npcEntityId);
            if (!(entity instanceof CustomNPCEntity npc)) return;

            ItemStack playerItem = player.getInventory().getItem(playerSlot);
            if (!(playerItem.getItem() instanceof PackagedTobaccoItem)) return;

            // Validierung der Gramm-Anzahl
            int availableGrams = PackagedTobaccoItem.getWeight(playerItem);
            if (offeredGrams <= 0 || offeredGrams > availableGrams) {
                player.sendSystemMessage(Component.literal("§cUngültige Grammzahl!"));
                return;
            }

            // Wallet-Check: NPC muss genug Geld haben
            int npcWallet = npc.getNpcData().getWallet();
            if (offeredPrice > npcWallet) {
                player.sendSystemMessage(Component.literal("§c✗ Der NPC hat nicht genug Geld!"));
                player.sendSystemMessage(Component.literal("§7NPC Geldbörse: " + npcWallet + "€, Preis: " + String.format("%.2f", offeredPrice) + "€"));
                return;
            }

            NPCBusinessMetrics metrics = new NPCBusinessMetrics(npc);
            NPCResponse response = NegotiationEngine.handleNegotiation(
                npc, player, playerItem, offeredPrice
            );

            if (response.isAccepted()) {
                // Prüfe Cooldown (1x pro Tag pro NPC)
                String cooldownKey = "LastTobaccoSale_" + player.getStringUUID();
                if (npc.getNpcData().getCustomData().contains(cooldownKey)) {
                    long currentDay = player.level().getDayTime() / 24000;
                    long lastSaleDay = npc.getNpcData().getCustomData().getLong(cooldownKey);

                    if (lastSaleDay >= currentDay) {
                        player.sendSystemMessage(Component.literal("§c✗ Dieser NPC hat heute bereits Tabak gekauft!"));
                        player.sendSystemMessage(Component.literal("§7Versuche es morgen nochmal."));
                        return;
                    }
                }

                // Verkauf durchführen
                double price = offeredPrice;
                long currentDay = player.level().getDayTime() / 24000;

                // Erstelle verkauftes Item mit der gewünschten Grammzahl
                ItemStack soldItem = PackagedTobaccoItem.create(
                    PackagedTobaccoItem.getType(playerItem),
                    PackagedTobaccoItem.getQuality(playerItem),
                    offeredGrams,
                    PackagedTobaccoItem.getPackagedDate(playerItem)  // Behalte Original-Datum
                );

                // Wenn nur ein Teil verkauft wird, reduziere das Spieler-Item
                if (offeredGrams < availableGrams) {
                    // Erstelle neues Item mit verbleibenden Gramm
                    int remainingGrams = availableGrams - offeredGrams;
                    ItemStack remainingItem = PackagedTobaccoItem.create(
                        PackagedTobaccoItem.getType(playerItem),
                        PackagedTobaccoItem.getQuality(playerItem),
                        remainingGrams,
                        PackagedTobaccoItem.getPackagedDate(playerItem)  // Behalte Original-Datum
                    );
                    player.getInventory().setItem(playerSlot, remainingItem);
                } else {
                    // Ganzes Paket wurde verkauft
                    playerItem.shrink(1);
                }

                // 50% Chance: Item geht ins NPC Inventar (kann gestohlen werden)
                if (player.level().getRandom().nextDouble() < 0.5) {
                    // Finde ersten leeren Slot im NPC Inventar
                    for (int i = 0; i < 9; i++) {
                        ItemStack slotItem = npc.getNpcData().getInventoryItem(i);
                        if (slotItem.isEmpty()) {
                            npc.getNpcData().setInventoryItem(i, soldItem);
                            break;
                        }
                    }
                }

                // NPC bezahlt: Ziehe Geld vom NPC-Wallet ab
                npc.getNpcData().removeMoney((int)price);

                // Geld zum Wallet-Item hinzufügen (Slot 8 = Slot 9 im UI)
                ItemStack walletItem = player.getInventory().getItem(8);
                if (walletItem.getItem() instanceof CashItem) {
                    CashItem.addValue(walletItem, price);

                    // Auch WalletManager aktualisieren (für Persistenz)
                    WalletManager.addMoney(player.getUUID(), price);
                    WalletManager.save();
                }

                // Metriken aktualisieren (mit den tatsächlich verkauften Gramm)
                metrics.recordPurchase(
                    player.getStringUUID(),
                    PackagedTobaccoItem.getType(soldItem),
                    PackagedTobaccoItem.getQuality(soldItem),
                    offeredGrams,  // Die tatsächlich verkauften Gramm
                    price,
                    player.level().getDayTime() / 24000
                );

                // Reputation ändern
                metrics.modifyReputation(player.getStringUUID(), response.getReputationChange());

                // Metriken speichern (aktualisiert Reputation und Zufriedenheit)
                metrics.save();

                // Setze Cooldown (aktueller Tag) - reuse variable from line 98
                currentDay = player.level().getDayTime() / 24000;
                npc.getNpcData().getCustomData().putLong("LastTobaccoSale_" + player.getStringUUID(), currentDay);

                // Erfolgsmeldung mit aktuellem Wallet-Item Wert
                if (walletItem.getItem() instanceof CashItem) {
                    double walletValue = CashItem.getValue(walletItem);
                    player.sendSystemMessage(Component.literal("§a✓ Verkauf erfolgreich! " + offeredGrams + "g für " + String.format("%.2f", price) + "€"));
                    player.sendSystemMessage(Component.literal("§7Deine Geldbörse: " + String.format("%.2f", walletValue) + "€ | NPC Geldbörse: " + npc.getNpcData().getWallet() + "€"));
                }
            } else {
                player.sendSystemMessage(Component.literal("§e" + response.getMessage()));
                if (response.getCounterOffer() > 0) {
                    player.sendSystemMessage(Component.literal("§7Gegenangebot: " + String.format("%.2f", response.getCounterOffer()) + "€"));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
