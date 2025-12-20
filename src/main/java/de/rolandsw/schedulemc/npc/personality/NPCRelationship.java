package de.rolandsw.schedulemc.npc.personality;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import org.slf4j.Logger;

import java.util.UUID;

/**
 * NPC Relationship - Beziehung zwischen NPC und Spieler
 *
 * Beziehungslevel: -100 bis +100
 * - -100 bis -50: Feindlich (höhere Preise, ruft sofort Polizei)
 * - -49 bis -10: Unfreundlich (leicht höhere Preise)
 * - -9 bis +9: Neutral (normale Preise)
 * - +10 bis +49: Freundlich (leicht niedrigere Preise)
 * - +50 bis +100: Sehr freundlich (deutlich niedrigere Preise, Geschenke)
 */
public class NPCRelationship {

    private static final Logger LOGGER = LogUtils.getLogger();

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    private final UUID npcId;
    private final UUID playerId;
    private int relationshipLevel;  // -100 bis +100

    // Statistics
    private int totalPurchases;
    private int totalSales;
    private int timesStolen;
    private int timesHelped;

    // ═══════════════════════════════════════════════════════════
    // CONSTANTS
    // ═══════════════════════════════════════════════════════════

    public static final int MIN_RELATIONSHIP = -100;
    public static final int MAX_RELATIONSHIP = 100;

    // Relationship Changes
    private static final int PURCHASE_BONUS = 2;      // +2 pro Kauf
    private static final int SALE_BONUS = 1;          // +1 pro Verkauf
    private static final int THEFT_PENALTY = -20;     // -20 bei Diebstahl
    private static final int CAUGHT_PENALTY = -30;    // -30 wenn erwischt
    private static final int HELP_BONUS = 10;         // +10 bei Hilfe
    private static final int ATTACK_PENALTY = -50;    // -50 bei Angriff

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════

    public NPCRelationship(UUID npcId, UUID playerId) {
        this.npcId = npcId;
        this.playerId = playerId;
        this.relationshipLevel = 0;  // Start neutral
        this.totalPurchases = 0;
        this.totalSales = 0;
        this.timesStolen = 0;
        this.timesHelped = 0;
    }

    // ═══════════════════════════════════════════════════════════
    // RELATIONSHIP ACTIONS
    // ═══════════════════════════════════════════════════════════

    /**
     * Spieler kauft bei NPC
     */
    public void onPurchase(double amount) {
        totalPurchases++;

        // Größere Käufe = mehr Relationship
        int bonus = PURCHASE_BONUS;
        if (amount >= 1000) bonus += 2;  // +4 für große Käufe
        if (amount >= 5000) bonus += 3;  // +7 für sehr große Käufe

        changeRelationship(bonus);
        LOGGER.debug("NPC {} relationship with {} improved by {} (purchase)", npcId, playerId, bonus);
    }

    /**
     * Spieler verkauft an NPC
     */
    public void onSale(double amount) {
        totalSales++;

        int bonus = SALE_BONUS;
        if (amount >= 1000) bonus += 1;  // +2 für große Verkäufe

        changeRelationship(bonus);
        LOGGER.debug("NPC {} relationship with {} improved by {} (sale)", npcId, playerId, bonus);
    }

    /**
     * Spieler versucht zu stehlen
     */
    public void onTheftAttempt() {
        timesStolen++;
        changeRelationship(THEFT_PENALTY);
        LOGGER.debug("NPC {} relationship with {} decreased by {} (theft)", npcId, playerId, THEFT_PENALTY);
    }

    /**
     * Spieler wird beim Stehlen erwischt
     */
    public void onTheftCaught() {
        changeRelationship(CAUGHT_PENALTY);
        LOGGER.debug("NPC {} relationship with {} decreased by {} (caught)", npcId, playerId, CAUGHT_PENALTY);
    }

    /**
     * Spieler greift NPC an
     */
    public void onAttack() {
        changeRelationship(ATTACK_PENALTY);
        LOGGER.debug("NPC {} relationship with {} severely decreased by {} (attack)", npcId, playerId, ATTACK_PENALTY);
    }

    /**
     * Spieler hilft NPC (z.B. Quest, Verteidigung)
     */
    public void onHelp() {
        timesHelped++;
        changeRelationship(HELP_BONUS);
        LOGGER.debug("NPC {} relationship with {} improved by {} (help)", npcId, playerId, HELP_BONUS);
    }

    /**
     * Ändert Relationship Level
     */
    private void changeRelationship(int amount) {
        int oldLevel = relationshipLevel;
        relationshipLevel = Math.max(MIN_RELATIONSHIP, Math.min(MAX_RELATIONSHIP, relationshipLevel + amount));

        // Log bei Tier-Wechsel
        RelationshipTier oldTier = RelationshipTier.fromLevel(oldLevel);
        RelationshipTier newTier = RelationshipTier.fromLevel(relationshipLevel);

        if (oldTier != newTier) {
            LOGGER.info("NPC {} relationship with {} changed from {} to {}",
                npcId, playerId, oldTier.getDisplayName(), newTier.getDisplayName());
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PRICE MODIFIER CALCULATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Berechnet Preis-Modifier basierend auf Beziehung
     *
     * -100: +50% Aufpreis
     * -50: +25% Aufpreis
     * 0: Normale Preise
     * +50: -10% Rabatt
     * +100: -20% Rabatt
     */
    public double getPriceModifier() {
        // Linear interpolation
        // Level -100 → modifier 1.5 (50% teurer)
        // Level 0 → modifier 1.0 (normal)
        // Level +100 → modifier 0.8 (20% günstiger)

        if (relationshipLevel < 0) {
            // Negative relationship: 1.0 bis 1.5
            return 1.0 + (Math.abs(relationshipLevel) / 100.0) * 0.5;
        } else {
            // Positive relationship: 1.0 bis 0.8
            return 1.0 - (relationshipLevel / 100.0) * 0.2;
        }
    }

    /**
     * Berechnet finalen Preis mit Relationship-Modifier
     */
    public double calculatePrice(double basePrice) {
        return basePrice * getPriceModifier();
    }

    // ═══════════════════════════════════════════════════════════
    // RELATIONSHIP TIER
    // ═══════════════════════════════════════════════════════════

    public RelationshipTier getTier() {
        return RelationshipTier.fromLevel(relationshipLevel);
    }

    /**
     * Relationship Tiers
     */
    public enum RelationshipTier {
        HOSTILE("Feindlich", "§c", -100, -50),
        UNFRIENDLY("Unfreundlich", "§6", -49, -10),
        NEUTRAL("Neutral", "§7", -9, 9),
        FRIENDLY("Freundlich", "§a", 10, 49),
        VERY_FRIENDLY("Sehr Freundlich", "§2", 50, 100);

        private final String displayName;
        private final String colorCode;
        private final int minLevel;
        private final int maxLevel;

        RelationshipTier(String displayName, String colorCode, int minLevel, int maxLevel) {
            this.displayName = displayName;
            this.colorCode = colorCode;
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getColorCode() {
            return colorCode;
        }

        public static RelationshipTier fromLevel(int level) {
            for (RelationshipTier tier : values()) {
                if (level >= tier.minLevel && level <= tier.maxLevel) {
                    return tier;
                }
            }
            return NEUTRAL;
        }

        public String getFormattedName() {
            return colorCode + displayName + "§r";
        }
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════

    public UUID getNpcId() {
        return npcId;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public int getRelationshipLevel() {
        return relationshipLevel;
    }

    public void setRelationshipLevel(int level) {
        this.relationshipLevel = Math.max(MIN_RELATIONSHIP, Math.min(MAX_RELATIONSHIP, level));
    }

    public int getTotalPurchases() {
        return totalPurchases;
    }

    public int getTotalSales() {
        return totalSales;
    }

    public int getTimesStolen() {
        return timesStolen;
    }

    public int getTimesHelped() {
        return timesHelped;
    }

    // ═══════════════════════════════════════════════════════════
    // NBT SERIALIZATION
    // ═══════════════════════════════════════════════════════════

    public CompoundTag save(CompoundTag tag) {
        tag.putUUID("NPCId", npcId);
        tag.putUUID("PlayerId", playerId);
        tag.putInt("RelationshipLevel", relationshipLevel);
        tag.putInt("TotalPurchases", totalPurchases);
        tag.putInt("TotalSales", totalSales);
        tag.putInt("TimesStolen", timesStolen);
        tag.putInt("TimesHelped", timesHelped);
        return tag;
    }

    public static NPCRelationship load(CompoundTag tag) {
        UUID npcId = tag.getUUID("NPCId");
        UUID playerId = tag.getUUID("PlayerId");

        NPCRelationship relationship = new NPCRelationship(npcId, playerId);
        relationship.relationshipLevel = tag.getInt("RelationshipLevel");
        relationship.totalPurchases = tag.getInt("TotalPurchases");
        relationship.totalSales = tag.getInt("TotalSales");
        relationship.timesStolen = tag.getInt("TimesStolen");
        relationship.timesHelped = tag.getInt("TimesHelped");

        return relationship;
    }

    // ═══════════════════════════════════════════════════════════
    // DISPLAY
    // ═══════════════════════════════════════════════════════════

    @Override
    public String toString() {
        return String.format("NPCRelationship{level=%d, tier=%s, priceModifier=×%.2f, purchases=%d, thefts=%d}",
            relationshipLevel, getTier().getDisplayName(), getPriceModifier(), totalPurchases, timesStolen);
    }

    public String getDetailedInfo() {
        RelationshipTier tier = getTier();
        return String.format(
            "%s%s§r\n" +
            "Level: %d/100\n" +
            "Preis-Modifier: ×%.2f (%.0f%% %s)\n" +
            "Käufe: %d | Verkäufe: %d\n" +
            "Diebstähle: %d | Hilfen: %d",
            tier.getColorCode(),
            tier.getDisplayName(),
            relationshipLevel,
            getPriceModifier(),
            Math.abs((getPriceModifier() - 1.0) * 100),
            getPriceModifier() < 1.0 ? "günstiger" : "teurer",
            totalPurchases,
            totalSales,
            timesStolen,
            timesHelped
        );
    }
}
