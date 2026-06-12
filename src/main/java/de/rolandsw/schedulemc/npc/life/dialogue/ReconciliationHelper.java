package de.rolandsw.schedulemc.npc.life.dialogue;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.life.core.MemoryType;
import de.rolandsw.schedulemc.npc.life.core.NPCEmotions;
import de.rolandsw.schedulemc.npc.life.core.NPCLifeData;
import de.rolandsw.schedulemc.npc.life.core.NPCTraits;
import de.rolandsw.schedulemc.npc.life.witness.CrimeType;
import de.rolandsw.schedulemc.npc.life.witness.WitnessManager;
import de.rolandsw.schedulemc.npc.life.NPCLifeSystemIntegration;
import de.rolandsw.schedulemc.npc.personality.NPCRelationship;
import de.rolandsw.schedulemc.npc.personality.NPCRelationshipManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Versöhnungslogik: Ein Spieler kann einen verängstigten/verärgerten NPC
 * (z.B. nach einem Angriff) per Entschuldigung besänftigen — gegen
 * Schmerzensgeld oder mit Worten (chance-basiert).
 *
 * Reine Berechnungen sind statisch und ohne Minecraft-Bootstrap testbar.
 */
public final class ReconciliationHelper {

    /** Basis-Schmerzensgeld in Euro. */
    public static final float BASE_COMPENSATION = 100f;
    /** Memory-Tag mit 1-Tag-Cooldown gegen Entschuldigungs-Spam. */
    public static final String RECENTLY_RECONCILED_TAG = "RecentlyReconciled";

    private ReconciliationHelper() {
    }

    /**
     * Schmerzensgeld: 100€ × Gier-Faktor (0.5–1.5) × Intensitäts-Faktor
     * (1.0–2.0) × Freundes-Rabatt (0.75 ab Beziehung 10).
     * Bei kürzlicher Versöhnung (Spam) verdoppelt sich der Preis.
     */
    public static int calculateCompensation(int greed, float emotionIntensity,
                                            int relationshipLevel, boolean recentlyReconciled) {
        float greedFactor = 1.0f + greed / 200f;
        float intensityFactor = 1.0f + Math.max(0f, Math.min(100f, emotionIntensity)) / 100f;
        float relationFactor = relationshipLevel >= 10 ? 0.75f : 1.0f;
        float spamFactor = recentlyReconciled ? 2.0f : 1.0f;
        return Math.round(BASE_COMPENSATION * greedFactor * intensityFactor * relationFactor * spamFactor);
    }

    /**
     * Erfolgschance einer rein verbalen Entschuldigung:
     * Basis 30%, +Beziehung, +Ehrlichkeit des NPCs, −Gier des NPCs.
     */
    public static float verbalApologyChance(int greed, int honesty, int relationshipLevel) {
        float chance = 0.3f + relationshipLevel / 200f - greed / 400f + honesty / 400f;
        return Math.max(0.05f, Math.min(0.9f, chance));
    }

    public static int compensationFor(CustomNPCEntity npc, ServerPlayer player) {
        NPCLifeData life = npc.getLifeData();
        NPCTraits traits = life != null ? life.getTraits() : new NPCTraits(0, 0, 0);
        NPCEmotions emotions = life != null ? life.getEmotions() : null;
        NPCRelationship rel = NPCRelationshipManager.getInstance()
            .getOrCreateRelationship(npc.getNpcData().getNpcUUID(), player.getUUID());
        boolean recent = life != null
            && life.getMemory().playerHasTag(player.getUUID(), RECENTLY_RECONCILED_TAG);
        return calculateCompensation(traits.getGreed(),
            emotions != null ? emotions.getIntensity() : 50f,
            rel.getRelationshipLevel(), recent);
    }

    /**
     * Wendet eine erfolgreiche Versöhnung an.
     *
     * @param paid true = Schmerzensgeld bezahlt (volle Wirkung),
     *             false = verbale Entschuldigung angenommen (kleine Wirkung)
     */
    public static void applyReconciliation(CustomNPCEntity npc, ServerPlayer player, boolean paid) {
        NPCLifeData life = npc.getLifeData();
        if (life != null) {
            if (paid) {
                life.getEmotions().reset();
            } else {
                life.getEmotions().calm(40f);
            }
            life.getMemory().removePlayerTag(player.getUUID(), "Aggressor");
            life.getMemory().addPlayerTag(player.getUUID(), RECENTLY_RECONCILED_TAG);
            life.getMemory().addMemory(player.getUUID(), MemoryType.CONVERSATION,
                paid ? "Apology accepted (compensation paid)" : "Apology accepted", 5);
        }

        NPCRelationshipManager.getInstance()
            .getOrCreateRelationship(npc.getNpcData().getNpcUUID(), player.getUUID())
            .modifyRelationship(paid ? 20 : 8);

        // Eigene, noch nicht gemeldete Gewalt-Reports dieses NPCs zurückziehen
        if (player.level() instanceof ServerLevel serverLevel) {
            WitnessManager witnessManager = NPCLifeSystemIntegration.get(serverLevel).getWitnessManager();
            if (witnessManager != null) {
                witnessManager.withdrawReportsBy(npc.getNpcData().getNpcUUID(), player.getUUID(), CrimeType.ASSAULT);
                witnessManager.withdrawReportsBy(npc.getNpcData().getNpcUUID(), player.getUUID(), CrimeType.AGGRAVATED_ASSAULT);
            }
        }
    }
}
