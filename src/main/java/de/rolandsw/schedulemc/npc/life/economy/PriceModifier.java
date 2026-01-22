package de.rolandsw.schedulemc.npc.life.economy;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.life.core.NPCLifeData;
import de.rolandsw.schedulemc.npc.life.social.Faction;
import de.rolandsw.schedulemc.npc.life.social.FactionManager;
import de.rolandsw.schedulemc.npc.life.social.FactionRelation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * PriceModifier - Berechnet alle Preis-Modifikatoren für Transaktionen
 *
 * Kombiniert verschiedene Faktoren:
 * - NPC Traits (Gier)
 * - NPC Emotionen
 * - Spieler-Reputation bei Fraktion
 * - Marktbedingungen
 * - Beziehung zum Spieler
 */
public class PriceModifier {

    // ═══════════════════════════════════════════════════════════
    // CONSTANTS
    // ═══════════════════════════════════════════════════════════

    /** Minimum Preis-Modifikator */
    public static final float MIN_MODIFIER = 0.5f;

    /** Maximum Preis-Modifikator */
    public static final float MAX_MODIFIER = 3.0f;

    // ═══════════════════════════════════════════════════════════
    // MAIN CALCULATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Berechnet den finalen Preis-Modifikator für eine Transaktion
     *
     * @param npc Der NPC-Verkäufer
     * @param player Der Spieler-Käufer
     * @param level Die ServerLevel
     * @param isBuying true wenn Spieler kauft, false wenn Spieler verkauft
     * @return Finaler Modifikator (z.B. 1.2 = 20% teurer)
     */
    public static float calculateModifier(CustomNPCEntity npc, ServerPlayer player,
                                          ServerLevel level, boolean isBuying) {

        float modifier = 1.0f;

        // 1. NPC Traits (Gier)
        modifier *= getTraitModifier(npc);

        // 2. NPC Emotionen
        modifier *= getEmotionModifier(npc);

        // 3. Fraktions-Beziehung
        modifier *= getFactionModifier(npc, player, level);

        // 4. Spieler-Beziehung (aus Memory)
        modifier *= getPlayerRelationModifier(npc, player);

        // 5. Marktbedingungen
        modifier *= getMarketModifier(level);

        // 6. Kauf vs. Verkauf Anpassung
        if (!isBuying) {
            // Beim Verkaufen (Spieler -> NPC) zahlt NPC weniger
            modifier *= 0.7f;
        }

        // Clamp
        return Math.max(MIN_MODIFIER, Math.min(MAX_MODIFIER, modifier));
    }

    /**
     * Berechnet nur den NPC-basierten Modifikator (ohne Markt)
     */
    public static float calculateNPCModifier(CustomNPCEntity npc, ServerPlayer player, ServerLevel level) {
        float modifier = 1.0f;

        modifier *= getTraitModifier(npc);
        modifier *= getEmotionModifier(npc);
        modifier *= getFactionModifier(npc, player, level);
        modifier *= getPlayerRelationModifier(npc, player);

        return Math.max(MIN_MODIFIER, Math.min(MAX_MODIFIER, modifier));
    }

    // ═══════════════════════════════════════════════════════════
    // INDIVIDUAL MODIFIERS
    // ═══════════════════════════════════════════════════════════

    /**
     * Modifikator basierend auf NPC-Traits (hauptsächlich Gier)
     */
    public static float getTraitModifier(CustomNPCEntity npc) {
        NPCLifeData lifeData = npc.getLifeData();
        if (lifeData == null) return 1.0f;

        // Trait-Modifikator aus NPCTraits
        return lifeData.getTraits().getTradeModifier();
    }

    /**
     * Modifikator basierend auf NPC-Emotionen
     */
    public static float getEmotionModifier(CustomNPCEntity npc) {
        NPCLifeData lifeData = npc.getLifeData();
        if (lifeData == null) return 1.0f;

        // Emotion-Modifikator aus NPCEmotions
        return lifeData.getEmotions().getPriceModifier();
    }

    /**
     * Modifikator basierend auf Fraktions-Beziehung
     */
    public static float getFactionModifier(CustomNPCEntity npc, ServerPlayer player, ServerLevel level) {
        // Bestimme Fraktion des NPCs
        Faction npcFaction = Faction.forNPCType(npc.getNpcType());

        // Hole Spieler-Beziehung zur Fraktion
        FactionManager manager = FactionManager.getManager(level);
        FactionRelation relation = manager.getRelation(player.getUUID(), npcFaction);

        return relation.getPriceModifier();
    }

    /**
     * Modifikator basierend auf persönlicher Beziehung zum Spieler
     */
    public static float getPlayerRelationModifier(CustomNPCEntity npc, ServerPlayer player) {
        NPCLifeData lifeData = npc.getLifeData();
        if (lifeData == null) return 1.0f;

        UUID playerUUID = player.getUUID();

        // Prüfe Tags und Erinnerungen
        var memory = lifeData.getMemory();

        // Stammkunde?
        int transactionCount = memory.getPlayerProfile(playerUUID).getTotalTransactions();
        float loyaltyBonus = 0.0f;
        if (transactionCount >= 10) {
            loyaltyBonus = -0.1f; // 10% Rabatt für Stammkunden
        } else if (transactionCount >= 5) {
            loyaltyBonus = -0.05f; // 5% Rabatt
        }

        // Negative Tags?
        float tagPenalty = 0.0f;
        if (memory.playerHasTag(playerUUID, "Kriminell")) {
            tagPenalty += 0.15f; // 15% teurer
        }
        if (memory.playerHasTag(playerUUID, "Gefährlich")) {
            tagPenalty += 0.1f;
        }
        if (memory.playerHasTag(playerUUID, "Dieb")) {
            tagPenalty += 0.2f;
        }

        // Positive Tags?
        if (memory.playerHasTag(playerUUID, "Vertrauenswürdig")) {
            tagPenalty -= 0.1f;
        }
        if (memory.playerHasTag(playerUUID, "Großzügig")) {
            tagPenalty -= 0.05f;
        }

        return 1.0f + loyaltyBonus + tagPenalty;
    }

    /**
     * Modifikator basierend auf Marktbedingungen
     */
    public static float getMarketModifier(ServerLevel level) {
        DynamicPriceManager manager = DynamicPriceManager.getManager(level);
        return manager.getGlobalMarketCondition().getPriceMultiplier();
    }

    // ═══════════════════════════════════════════════════════════
    // CONVENIENCE METHODS
    // ═══════════════════════════════════════════════════════════

    /**
     * Berechnet den finalen Preis für ein Item
     */
    public static int calculateFinalPrice(int basePrice, CustomNPCEntity npc,
                                          ServerPlayer player, ServerLevel level, boolean isBuying) {
        float modifier = calculateModifier(npc, player, level, isBuying);
        return Math.max(1, Math.round(basePrice * modifier));
    }

    /**
     * Gibt eine lesbare Zusammenfassung der Preismodifikatoren
     */
    public static String getPriceBreakdown(CustomNPCEntity npc, ServerPlayer player,
                                           ServerLevel level, boolean isBuying) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Preis-Aufschlüsselung ===\n");

        float traitMod = getTraitModifier(npc);
        float emotionMod = getEmotionModifier(npc);
        float factionMod = getFactionModifier(npc, player, level);
        float relationMod = getPlayerRelationModifier(npc, player);
        float marketMod = getMarketModifier(level);

        sb.append(String.format("NPC-Persönlichkeit: ×%.2f\n", traitMod));
        sb.append(String.format("NPC-Stimmung: ×%.2f\n", emotionMod));
        sb.append(String.format("Fraktions-Standing: ×%.2f\n", factionMod));
        sb.append(String.format("Persönliche Beziehung: ×%.2f\n", relationMod));
        sb.append(String.format("Marktbedingungen: ×%.2f\n", marketMod));

        if (!isBuying) {
            sb.append("Verkaufs-Abzug: ×0.70\n");
        }

        float total = calculateModifier(npc, player, level, isBuying);
        sb.append(String.format("\nGesamt: ×%.2f", total));

        return sb.toString();
    }
}
