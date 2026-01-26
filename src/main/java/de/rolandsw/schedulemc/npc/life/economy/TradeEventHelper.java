package de.rolandsw.schedulemc.npc.life.economy;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.life.core.EmotionState;
import de.rolandsw.schedulemc.npc.life.core.MemoryType;
import de.rolandsw.schedulemc.npc.life.core.NPCLifeData;
import de.rolandsw.schedulemc.npc.life.social.Faction;
import de.rolandsw.schedulemc.npc.life.social.FactionManager;
import de.rolandsw.schedulemc.npc.life.social.RumorNetwork;
import de.rolandsw.schedulemc.npc.life.social.RumorType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/**
 * TradeEventHelper - Hilfsmethoden für Trade-Events
 *
 * Verarbeitet Transaktionen und aktualisiert das NPC-Life-System
 * basierend auf Käufen und Verkäufen.
 */
public class TradeEventHelper {

    // ═══════════════════════════════════════════════════════════
    // TRANSACTION PROCESSING
    // ═══════════════════════════════════════════════════════════

    /**
     * Verarbeitet einen Kauf (Spieler kauft vom NPC)
     *
     * @param npc Der verkaufende NPC
     * @param player Der kaufende Spieler
     * @param item Das gekaufte Item
     * @param price Der bezahlte Preis
     * @param basePrice Der ursprüngliche Basispreis
     * @param level Die ServerLevel
     */
    public static void processPurchase(CustomNPCEntity npc, ServerPlayer player,
                                       ItemStack item, int price, int basePrice, ServerLevel level) {

        processTransaction(npc, player, item, price, basePrice, level, true);
    }

    /**
     * Verarbeitet einen Verkauf (Spieler verkauft an NPC)
     *
     * @param npc Der kaufende NPC
     * @param player Der verkaufende Spieler
     * @param item Das verkaufte Item
     * @param price Der erhaltene Preis
     * @param basePrice Der ursprüngliche Basispreis
     * @param level Die ServerLevel
     */
    public static void processSale(CustomNPCEntity npc, ServerPlayer player,
                                   ItemStack item, int price, int basePrice, ServerLevel level) {

        processTransaction(npc, player, item, price, basePrice, level, false);
    }

    /**
     * Verarbeitet eine Transaktion
     */
    private static void processTransaction(CustomNPCEntity npc, ServerPlayer player,
                                          ItemStack item, int price, int basePrice,
                                          ServerLevel level, boolean playerIsBuying) {

        UUID playerUUID = player.getUUID();
        NPCLifeData lifeData = npc.getLifeData();

        if (lifeData == null) return;

        // 1. Erinnerung speichern
        String itemName = item.getHoverName().getString();
        String memoryText = String.format("%s: %s für %d",
            playerIsBuying ? "Verkauf" : "Kauf", itemName, price);

        lifeData.getMemory().addMemory(
            playerUUID,
            MemoryType.TRANSACTION,
            memoryText,
            Math.min(5, 1 + price / 500) // Wichtigkeit basierend auf Wert
        );

        // 2. Profil aktualisieren
        lifeData.getMemory().getPlayerProfile(playerUUID).recordTransaction(price);

        // 3. Fairness bewerten
        boolean wasFair = evaluateFairness(price, basePrice, playerIsBuying);

        if (!wasFair) {
            // Unfaire Transaktion
            if (playerIsBuying && price < basePrice * 0.7f) {
                // Spieler hat zu wenig bezahlt (durch Verhandlung?)
                lifeData.getMemory().addPlayerTag(playerUUID, "Geizig");
            } else if (!playerIsBuying && price > basePrice * 1.5f) {
                // Spieler hat zu viel verlangt
                lifeData.getMemory().addPlayerTag(playerUUID, "Habgierig");
            }
        } else {
            // Faire Transaktion - positive Auswirkung
            if (price > basePrice * 1.1f && playerIsBuying) {
                // Spieler hat mehr bezahlt als nötig
                lifeData.getMemory().addPlayerTag(playerUUID, "Großzügig");
                lifeData.getEmotions().trigger(EmotionState.HAPPY, 20.0f, 600);

                // Gerücht verbreiten
                RumorNetwork.getNetwork(level).createRumor(
                    playerUUID,
                    RumorType.GENEROUS,
                    "Großzügiger Käufer",
                    level.getDayTime() / 24000,
                    npc.getNpcData().getNpcUUID()
                );
            }
        }

        // 4. Fraktions-Reputation
        Faction npcFaction = Faction.forNPCType(npc.getNpcType());
        FactionManager.getManager(level).onTransaction(playerUUID, npcFaction, price, wasFair);

        // 5. NPC-Emotion bei großer Transaktion
        if (price >= 1000) {
            lifeData.getEmotions().trigger(EmotionState.HAPPY, 25.0f, 1200);
        }

        // 6. Energie-Kosten für NPC (Arbeit)
        lifeData.getNeeds().satisfy(de.rolandsw.schedulemc.npc.life.core.NeedType.ENERGY, -1);
    }

    /**
     * Bewertet ob eine Transaktion fair war
     */
    private static boolean evaluateFairness(int actualPrice, int basePrice, boolean playerIsBuying) {
        float ratio = (float) actualPrice / basePrice;

        if (playerIsBuying) {
            // Spieler kauft: Fair wenn er mindestens 80% des Basispreises zahlt
            return ratio >= 0.8f;
        } else {
            // Spieler verkauft: Fair wenn er maximal 120% des Basispreises bekommt
            return ratio <= 1.2f;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // SPECIAL TRANSACTIONS
    // ═══════════════════════════════════════════════════════════

    /**
     * Verarbeitet einen illegalen Kauf (z.B. Drogen)
     */
    public static void processIllegalPurchase(CustomNPCEntity npc, ServerPlayer player,
                                              ItemStack item, int price, ServerLevel level) {

        NPCLifeData lifeData = npc.getLifeData();
        if (lifeData == null) return;

        UUID playerUUID = player.getUUID();

        // Erinnerung speichern
        lifeData.getMemory().addMemory(
            playerUUID,
            MemoryType.TRANSACTION,
            "Illegaler Kauf: " + item.getHoverName().getString(),
            6 // Wichtig
        );

        // Tags
        lifeData.getMemory().addPlayerTag(playerUUID, "IllegalerKäufer");

        // Gerücht
        RumorNetwork.getNetwork(level).createRumor(
            playerUUID,
            RumorType.DRUG_DEALING,
            "Illegale Geschäfte",
            level.getDayTime() / 24000,
            npc.getNpcData().getNpcUUID()
        );

        // Untergrund-Reputation steigt
        FactionManager.getManager(level).modifyReputation(playerUUID, Faction.UNTERGRUND, 5);
    }

    /**
     * Verarbeitet ein Geschenk (Item ohne Gegenleistung)
     */
    public static void processGift(CustomNPCEntity npc, ServerPlayer player,
                                   ItemStack gift, int estimatedValue, ServerLevel level) {

        NPCLifeData lifeData = npc.getLifeData();
        if (lifeData == null) return;

        UUID playerUUID = player.getUUID();

        // Erinnerung speichern
        lifeData.getMemory().addMemory(
            playerUUID,
            MemoryType.GIFT_RECEIVED,
            "Geschenk: " + gift.getHoverName().getString(),
            Math.min(8, 4 + estimatedValue / 200)
        );

        // Positive Tags
        lifeData.getMemory().addPlayerTag(playerUUID, "Großzügig");
        if (estimatedValue >= 500) {
            lifeData.getMemory().addPlayerTag(playerUUID, "Wohlwollend");
        }

        // Positive Emotion
        float emotionIntensity = Math.min(80, 30 + estimatedValue / 20);
        lifeData.getEmotions().trigger(EmotionState.HAPPY, emotionIntensity, 6000);

        // Gerücht
        RumorNetwork.getNetwork(level).createRumor(
            playerUUID,
            RumorType.GENEROUS,
            "Schenkt Geschenke",
            level.getDayTime() / 24000,
            npc.getNpcData().getNpcUUID()
        );

        // Fraktions-Reputation
        Faction npcFaction = Faction.forNPCType(npc.getNpcType());
        FactionManager.getManager(level).onGoodDeed(playerUUID, npcFaction, estimatedValue / 50);
    }

    /**
     * Verarbeitet einen fehlgeschlagenen Diebstahlsversuch
     */
    public static void processFailedTheft(CustomNPCEntity npc, ServerPlayer player,
                                          ItemStack attemptedItem, ServerLevel level) {

        NPCLifeData lifeData = npc.getLifeData();
        if (lifeData == null) return;

        UUID playerUUID = player.getUUID();

        // Erinnerung speichern
        lifeData.getMemory().addMemory(
            playerUUID,
            MemoryType.CRIME_WITNESSED,
            "Diebstahlsversuch: " + attemptedItem.getHoverName().getString(),
            9 // Sehr wichtig
        );

        // Negative Tags
        lifeData.getMemory().addPlayerTag(playerUUID, "Dieb");
        lifeData.getMemory().addPlayerTag(playerUUID, "Unzuverlässig");

        // Negative Emotion
        lifeData.getEmotions().trigger(EmotionState.ANGRY, 70.0f);

        // Gerücht
        RumorNetwork.getNetwork(level).createRumor(
            playerUUID,
            RumorType.THEFT,
            "Versuchter Diebstahl",
            level.getDayTime() / 24000,
            npc.getNpcData().getNpcUUID()
        );

        // Behavior Engine warnen
        if (npc.getBehaviorEngine() != null) {
            npc.getBehaviorEngine().onWitnessCrime(player, "ATTEMPTED_THEFT", 5);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITY
    // ═══════════════════════════════════════════════════════════

    /**
     * Prüft ob ein Item als illegal gilt
     */
    public static boolean isIllegalItem(ItemStack item) {
        String itemId = item.getItem().getDescriptionId().toLowerCase();

        return itemId.contains("drug") ||
               itemId.contains("droge") ||
               itemId.contains("cocaine") ||
               itemId.contains("kokain") ||
               itemId.contains("heroin") ||
               itemId.contains("weed") ||
               itemId.contains("cannabis") ||
               itemId.contains("opium") ||
               itemId.contains("meth");
    }

    /**
     * Schätzt den Wert eines Items
     */
    public static int estimateItemValue(ItemStack item) {
        // Vereinfachte Schätzung basierend auf Seltenheit
        return switch (item.getRarity()) {
            case COMMON -> 10 * item.getCount();
            case UNCOMMON -> 50 * item.getCount();
            case RARE -> 200 * item.getCount();
            case EPIC -> 500 * item.getCount();
        };
    }
}
