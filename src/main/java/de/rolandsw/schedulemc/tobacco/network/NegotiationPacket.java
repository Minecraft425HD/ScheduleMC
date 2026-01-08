package de.rolandsw.schedulemc.tobacco.network;

import de.rolandsw.schedulemc.economy.WalletManager;
import de.rolandsw.schedulemc.economy.items.CashItem;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.production.core.DrugType;
import de.rolandsw.schedulemc.production.items.PackagedDrugItem;
import de.rolandsw.schedulemc.tobacco.TobaccoType;
import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
import de.rolandsw.schedulemc.tobacco.business.NPCBusinessMetrics;
import de.rolandsw.schedulemc.tobacco.business.NPCResponse;
import de.rolandsw.schedulemc.tobacco.business.NegotiationEngine;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
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
        PacketHandler.handleServerPacket(ctx, player -> {
            Entity entity = player.level().getEntity(npcEntityId);
            if (!(entity instanceof CustomNPCEntity npc)) return;

            ItemStack playerItem = player.getInventory().getItem(playerSlot);
            if (!(playerItem.getItem() instanceof PackagedDrugItem) ||
                PackagedDrugItem.getDrugType(playerItem) != DrugType.TOBACCO) return;

            // Validierung der Gramm-Anzahl
            int availableGrams = PackagedDrugItem.getWeight(playerItem);
            if (offeredGrams <= 0 || offeredGrams > availableGrams) {
                player.sendSystemMessage(Component.literal("§cUngültige Grammzahl!"));
                return;
            }

            // WICHTIG: Man kann nur exakt passende Päckchen verkaufen!
            // Aus einem 5g Glas kann man nicht 1g herausnehmen
            if (offeredGrams != availableGrams) {
                player.sendSystemMessage(Component.literal("§c✗ Du kannst nur das komplette Päckchen verkaufen!"));
                player.sendSystemMessage(Component.literal("§7Dieses Päckchen enthält " + availableGrams + "g."));
                player.sendSystemMessage(Component.literal("§7Wenn du " + offeredGrams + "g verkaufen möchtest, brauchst du ein " + offeredGrams + "g Päckchen."));
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

                // Parse Type und Quality aus PackagedDrugItem
                String variantStr = PackagedDrugItem.getVariant(playerItem);
                TobaccoType type = variantStr != null ? TobaccoType.valueOf(variantStr.split("\\.")[1]) : TobaccoType.VIRGINIA;

                String qualityStr = PackagedDrugItem.getQuality(playerItem);
                TobaccoQuality quality = qualityStr != null ? TobaccoQuality.valueOf(qualityStr.split("\\.")[1]) : TobaccoQuality.GUT;

                long packagedDate = PackagedDrugItem.getPackageDate(playerItem);

                // Erstelle verkauftes Item (komplettes Päckchen, da offeredGrams == availableGrams)
                ItemStack soldItem = PackagedDrugItem.create(
                    DrugType.TOBACCO,
                    offeredGrams,
                    quality,
                    type,
                    packagedDate  // Behalte Original-Datum
                );

                // Entferne das komplette Päckchen aus dem Inventar
                playerItem.shrink(1);

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

                // Performance-Optimierung: Sync nur Wallet statt Full NPC Data
                npc.syncWalletToClient();

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
                    type,
                    quality,
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
    }
}
