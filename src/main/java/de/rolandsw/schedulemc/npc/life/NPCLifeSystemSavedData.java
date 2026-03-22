package de.rolandsw.schedulemc.npc.life;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nonnull;

/**
 * NPCLifeSystemSavedData - Persistiert alle Level-basierten NPC Life System Daten
 *
 * Diese Klasse speichert:
 * - FactionManager Daten (Spieler-Fraktions-Beziehungen)
 * - RumorNetwork Daten (alle Gerüchte)
 * - WitnessManager Daten (Zeugenberichte, Wanted-Listen)
 * - DynamicPriceManager Daten (Marktbedingungen)
 * - CompanionManager Daten (Begleiter-Zuordnungen)
 * - WorldEventManager Daten (aktive Events)
 * - QuestManager Daten (Quest-Templates und Fortschritt)
 */
public class NPCLifeSystemSavedData extends SavedData {

    // ═══════════════════════════════════════════════════════════
    // CONSTANTS
    // ═══════════════════════════════════════════════════════════

    private static final String DATA_NAME = "schedulemc_npc_life_system";

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    private final ServerLevel level;
    private CompoundTag cachedData;

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════

    public NPCLifeSystemSavedData(ServerLevel level) {
        this.level = level;
        this.cachedData = new CompoundTag();
    }

    private NPCLifeSystemSavedData(ServerLevel level, CompoundTag tag) {
        this.level = level;
        this.cachedData = tag.copy();
        loadFromTag(tag);
    }

    // ═══════════════════════════════════════════════════════════
    // FACTORY
    // ═══════════════════════════════════════════════════════════

    /**
     * Holt oder erstellt die SavedData für ein Level
     */
    public static NPCLifeSystemSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            tag -> new NPCLifeSystemSavedData(level, tag),
            () -> new NPCLifeSystemSavedData(level),
            DATA_NAME
        );
    }

    // ═══════════════════════════════════════════════════════════
    // SAVE / LOAD
    // ═══════════════════════════════════════════════════════════

    @Override
    @Nonnull
    public CompoundTag save(@Nonnull CompoundTag tag) {
        // Hole aktuelle Daten vom Integration Manager
        NPCLifeSystemIntegration integration = NPCLifeSystemIntegration.get(level);

        // Speichere alle Subsysteme
        CompoundTag integrationData = integration.save();
        tag.put("integration", integrationData);

        // Metadaten
        tag.putLong("lastSaved", System.currentTimeMillis());
        tag.putInt("version", 1);

        return tag;
    }

    /**
     * Lädt Daten aus dem Tag
     */
    private void loadFromTag(CompoundTag tag) {
        if (!tag.contains("integration")) {
            return;
        }

        // Hole Integration Manager
        NPCLifeSystemIntegration integration = NPCLifeSystemIntegration.get(level);

        // Lade alle Subsysteme
        CompoundTag integrationData = tag.getCompound("integration");
        integration.load(integrationData);
    }

    /**
     * Markiert die Daten als geändert (für automatisches Speichern)
     */
    public void markDirty() {
        setDirty();
    }

    /**
     * Erzwingt sofortiges Speichern
     */
    public void forceSave() {
        setDirty();
        // Das Speichern wird von Minecraft automatisch durchgeführt
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITY
    // ═══════════════════════════════════════════════════════════

    /**
     * Gibt das Level zurück
     */
    public ServerLevel getLevel() {
        return level;
    }

    @Override
    public String toString() {
        return String.format("NPCLifeSystemSavedData{level=%s}", level.dimension().location());
    }
}
