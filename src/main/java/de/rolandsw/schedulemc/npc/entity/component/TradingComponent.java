package de.rolandsw.schedulemc.npc.entity.component;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.life.core.EmotionState;
import de.rolandsw.schedulemc.npc.life.core.NPCLifeData;
import net.minecraft.nbt.CompoundTag;

/**
 * Handels-Komponente fuer NPC-Entitaeten.
 *
 * Verwaltet handelsrelevante Logik:
 * - Handelsbereitschaft basierend auf Emotionen/Beduerfnissen
 * - Persoenlicher Preismodifikator
 * - Handels-Cooldown
 * - Rabatte fuer Stammkunden
 *
 * Extrahiert aus CustomNPCEntity.isWillingToTrade() und getPersonalPriceModifier().
 */
public class TradingComponent implements NPCComponent {

    private boolean willingToTrade = true;
    private float personalPriceModifier = 1.0f;
    private int tradeCooldown = 0;

    @Override
    public String getComponentId() {
        return "trading";
    }

    @Override
    public int getUpdateInterval() {
        return 20; // Jede Sekunde aktualisieren
    }

    @Override
    public void tick(CustomNPCEntity entity) {
        // Cooldown herunterzaehlen
        if (tradeCooldown > 0) tradeCooldown--;

        // Handelsbereitschaft berechnen
        NPCLifeData lifeData = entity.getLifeData();
        if (lifeData == null) {
            willingToTrade = true;
            personalPriceModifier = 1.0f;
            return;
        }

        // Handelsbereitschaft
        willingToTrade = calculateWillingness(lifeData);

        // Preismodifikator
        personalPriceModifier = calculatePriceModifier(lifeData);
    }

    private boolean calculateWillingness(NPCLifeData lifeData) {
        // Nicht handeln wenn verängstigt
        if (lifeData.getEmotions() != null &&
            lifeData.getEmotions().getCurrentEmotion() == EmotionState.FEARFUL &&
            lifeData.getEmotions().getIntensity() > 50) {
            return false;
        }

        // Nicht handeln wenn zu muede
        if (lifeData.getNeeds() != null && lifeData.getNeeds().getEnergy() < 10) {
            return false;
        }

        return true;
    }

    private float calculatePriceModifier(NPCLifeData lifeData) {
        float modifier = 1.0f;

        // Gier-Faktor
        if (lifeData.getTraits() != null) {
            modifier += lifeData.getTraits().getGreed() / 200.0f;
        }

        // Emotionsbasierte Modifikatoren
        if (lifeData.getEmotions() != null) {
            EmotionState emotion = lifeData.getEmotions().getCurrentEmotion();
            float intensity = lifeData.getEmotions().getIntensity();

            if (emotion == EmotionState.HAPPY) {
                modifier -= intensity / 400.0f; // Gluecklich = guenstiger
            } else if (emotion == EmotionState.FEARFUL) {
                modifier -= intensity / 300.0f; // Aengstlich = guenstiger
            }
        }

        return Math.max(0.5f, Math.min(1.5f, modifier));
    }

    // ═══════════════════════════════════════════════════════════
    // API
    // ═══════════════════════════════════════════════════════════

    public boolean isWillingToTrade() {
        return willingToTrade && tradeCooldown <= 0;
    }

    public float getPersonalPriceModifier() {
        return personalPriceModifier;
    }

    public void setTradeCooldown(int ticks) {
        this.tradeCooldown = ticks;
    }

    @Override
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("tradeCooldown", tradeCooldown);
        return tag;
    }

    @Override
    public void load(CompoundTag tag) {
        tradeCooldown = tag.getInt("tradeCooldown");
    }
}
