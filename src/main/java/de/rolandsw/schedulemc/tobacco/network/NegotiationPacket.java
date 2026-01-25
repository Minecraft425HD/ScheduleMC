package de.rolandsw.schedulemc.tobacco.network;

import de.rolandsw.schedulemc.economy.WalletManager;
import de.rolandsw.schedulemc.economy.items.CashItem;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.life.NPCLifeSystemIntegration;
import de.rolandsw.schedulemc.npc.life.core.EmotionState;
import de.rolandsw.schedulemc.npc.life.quest.QuestEventHandler;
import de.rolandsw.schedulemc.npc.life.world.WorldEventManager;
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
import net.minecraft.server.level.ServerLevel;
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
        PacketHandler.handleServerPacket(ctx, player -> {
            Entity entity = player.level().getEntity(npcEntityId);
            if (!(entity instanceof CustomNPCEntity npc)) return;

            // Null-Safety: Prüfe ob NPC-Daten vorhanden sind
            if (npc.getNpcData() == null) {
                player.sendSystemMessage(Component.translatable("message.npc.data_unavailable"));
                return;
            }

            // ═══════════════════════════════════════════════════════════
            // NPC LIFE SYSTEM INTEGRATION: Willingness Check
            // ═══════════════════════════════════════════════════════════
            if (!npc.isWillingToTrade()) {
                // NPC möchte nicht handeln (Emotionen/Bedürfnisse)
                if (npc.getLifeData() != null && npc.getLifeData().getEmotions() != null) {
                    EmotionState emotion = npc.getLifeData().getEmotions().getCurrentEmotion();
                    if (emotion == EmotionState.FEARFUL) {
                        player.sendSystemMessage(Component.translatable("message.npc.too_scared_to_trade"));
                    } else if (emotion == EmotionState.ANGRY) {
                        player.sendSystemMessage(Component.translatable("message.npc.too_angry_to_trade"));
                    } else {
                        player.sendSystemMessage(Component.translatable("message.npc.not_willing_to_trade"));
                    }
                } else {
                    player.sendSystemMessage(Component.translatable("message.npc.not_willing_to_trade"));
                }
                return;
            }

            ItemStack playerItem = player.getInventory().getItem(playerSlot);
            if (!(playerItem.getItem() instanceof PackagedDrugItem)) {
                player.sendSystemMessage(Component.translatable("message.negotiation.not_a_drug_item"));
                return;
            }

            DrugType drugType = PackagedDrugItem.getDrugType(playerItem);
            if (drugType == null) {
                player.sendSystemMessage(Component.translatable("message.negotiation.invalid_drug_type"));
                return;
            }

            // Validierung der Gramm-Anzahl
            int availableGrams = PackagedDrugItem.getWeight(playerItem);
            if (offeredGrams <= 0 || offeredGrams > availableGrams) {
                player.sendSystemMessage(Component.translatable("message.tobacco.invalid_grams"));
                return;
            }

            // WICHTIG: Man kann nur exakt passende Päckchen verkaufen!
            // Aus einem 5g Glas kann man nicht 1g herausnehmen
            if (offeredGrams != availableGrams) {
                player.sendSystemMessage(Component.translatable("message.tobacco.must_sell_complete_package"));
                player.sendSystemMessage(Component.translatable("message.tobacco.package_contains", availableGrams));
                player.sendSystemMessage(Component.translatable("message.tobacco.need_correct_package", offeredGrams, offeredGrams));
                return;
            }

            // Wallet-Check: NPC muss genug Geld haben
            int npcWallet = npc.getNpcData().getWallet();
            if (offeredPrice > npcWallet) {
                player.sendSystemMessage(Component.translatable("message.tobacco.npc_insufficient_funds"));
                player.sendSystemMessage(Component.translatable("message.tobacco.npc_wallet_info", npcWallet, String.format("%.2f", offeredPrice)));
                return;
            }

            // ═══════════════════════════════════════════════════════════
            // NPC LIFE SYSTEM INTEGRATION: Price Modifiers
            // ═══════════════════════════════════════════════════════════
            float npcPriceModifier = npc.getPersonalPriceModifier();

            // WorldEventManager Preismodifikator
            float worldEventModifier = 1.0f;
            if (player.level() instanceof ServerLevel serverLevel) {
                WorldEventManager worldEventManager = WorldEventManager.getManager(serverLevel);
                worldEventModifier = worldEventManager.getCombinedPriceModifier(player.blockPosition());
            }

            // Kombinierter Modifikator: NPC-Emotionen + World Events
            float combinedModifier = npcPriceModifier * worldEventModifier;

            // Der NPC erwartet einen höheren Preis wenn der Modifikator > 1.0 ist
            // Das bedeutet: offeredPrice muss angepasst werden für die Verhandlung
            double adjustedOfferedPrice = offeredPrice / combinedModifier;

            NPCBusinessMetrics metrics = new NPCBusinessMetrics(npc);
            NPCResponse response = NegotiationEngine.handleNegotiation(
                npc, player, playerItem, adjustedOfferedPrice
            );

            if (response.isAccepted()) {
                // Prüfe Cooldown (1x pro Tag pro NPC)
                String cooldownKey = "LastTobaccoSale_" + player.getStringUUID();
                if (npc.getNpcData().getCustomData().contains(cooldownKey)) {
                    long currentDay = player.level().getDayTime() / 24000;
                    long lastSaleDay = npc.getNpcData().getCustomData().getLong(cooldownKey);

                    if (lastSaleDay >= currentDay) {
                        player.sendSystemMessage(Component.translatable("message.tobacco.npc_already_bought_today"));
                        player.sendSystemMessage(Component.translatable("message.tobacco.try_tomorrow"));
                        return;
                    }
                }

                // Verkauf durchführen
                double price = offeredPrice;
                long currentDay = player.level().getDayTime() / 24000;

                // Parse Type und Quality aus PackagedDrugItem
                String variantStr = PackagedDrugItem.getVariant(playerItem);
                String qualityStr = PackagedDrugItem.getQuality(playerItem);
                long packagedDate = PackagedDrugItem.getPackageDate(playerItem);

                // Für Tabak: Spezifische Typen parsen
                TobaccoType tobaccoType = null;
                TobaccoQuality tobaccoQuality = null;
                if (drugType == DrugType.TOBACCO) {
                    tobaccoType = variantStr != null ? TobaccoType.valueOf(variantStr.split("\\.")[1]) : TobaccoType.VIRGINIA;
                    tobaccoQuality = qualityStr != null ? TobaccoQuality.valueOf(qualityStr.split("\\.")[1]) : TobaccoQuality.GUT;
                }

                // Erstelle verkauftes Item (komplettes Päckchen, da offeredGrams == availableGrams)
                ItemStack soldItem;
                if (drugType == DrugType.TOBACCO && tobaccoQuality != null && tobaccoType != null) {
                    soldItem = PackagedDrugItem.create(
                        DrugType.TOBACCO,
                        offeredGrams,
                        tobaccoQuality,
                        tobaccoType,
                        packagedDate
                    );
                } else {
                    // Für andere Drogenarten: Kopiere das Original-Item
                    soldItem = playerItem.copy();
                }

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

                // Geld zum Wallet hinzufügen (WalletManager)
                ItemStack walletItem = player.getInventory().getItem(8);
                if (walletItem.getItem() instanceof CashItem) {
                    // Füge Geld im WalletManager hinzu
                    WalletManager.addMoney(player.getUUID(), price);
                    WalletManager.save();
                }

                // Metriken aktualisieren (mit den tatsächlich verkauften Gramm)
                if (drugType == DrugType.TOBACCO && tobaccoType != null && tobaccoQuality != null) {
                    metrics.recordPurchase(
                        player.getStringUUID(),
                        tobaccoType,
                        tobaccoQuality,
                        offeredGrams,
                        price,
                        player.level().getDayTime() / 24000
                    );
                } else {
                    // Für andere Drogenarten: Einfache Aufzeichnung
                    metrics.recordGenericPurchase(
                        player.getStringUUID(),
                        drugType,
                        offeredGrams,
                        price,
                        player.level().getDayTime() / 24000
                    );
                }

                // Reputation ändern
                metrics.modifyReputation(player.getStringUUID(), response.getReputationChange());

                // Metriken speichern (aktualisiert Reputation und Zufriedenheit)
                metrics.save();

                // Setze Cooldown (aktueller Tag) - reuse variable from line 98
                currentDay = player.level().getDayTime() / 24000;
                npc.getNpcData().getCustomData().putLong("LastTobaccoSale_" + player.getStringUUID(), currentDay);

                // Quest-System: Melde erfolgreiche Verhandlung
                QuestEventHandler.reportSuccessfulNegotiation(player, npc);

                // Life-System: Koordiniere Manager-Updates
                if (player.level() instanceof ServerLevel serverLevel) {
                    NPCLifeSystemIntegration integration = NPCLifeSystemIntegration.get(serverLevel);
                    integration.onTradeCompleted(player, npc, (int) price);
                }

                // Erfolgsmeldung mit aktuellem Wallet-Wert
                if (walletItem.getItem() instanceof CashItem) {
                    double walletValue = WalletManager.getBalance(player.getUUID());
                    String drugName = drugType.getDisplayName();
                    player.sendSystemMessage(Component.translatable("message.negotiation.sale_success", offeredGrams, drugName, String.format("%.2f", price)));
                    player.sendSystemMessage(Component.translatable("message.tobacco.wallet_summary", String.format("%.2f", walletValue), npc.getNpcData().getWallet()));
                }
            } else {
                player.sendSystemMessage(Component.translatable("message.negotiation.response_prefix", response.getMessage()));
                if (response.getCounterOffer() > 0) {
                    player.sendSystemMessage(Component.translatable("message.tobacco.counteroffer", String.format("%.2f", response.getCounterOffer())));
                }
            }
        });
    }
}
