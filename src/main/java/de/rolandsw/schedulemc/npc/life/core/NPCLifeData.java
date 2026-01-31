package de.rolandsw.schedulemc.npc.life.core;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.events.PoliceAIHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * NPCLifeData - Container für alle Life-System Daten eines NPCs
 *
 * Enthält:
 * - NPCNeeds (Bedürfnisse: Energie, Sicherheit)
 * - NPCEmotions (6 Emotionszustände)
 * - NPCMemory (Smart-Gedächtnis mit Tages-Zusammenfassung)
 * - NPCTraits (3 Persönlichkeits-Achsen)
 */
public class NPCLifeData {

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    private final NPCNeeds needs;
    private final NPCEmotions emotions;
    private final NPCMemory memory;
    private final NPCTraits traits;

    /** Referenz zur NPC-Entity (nicht serialisiert) */
    @Nullable
    private transient CustomNPCEntity npcEntity;

    /** Ob das Life-System aktiv ist */
    private boolean enabled = true;

    /** Alter des NPCs in Spieltagen */
    private int ageInDays = 0;

    /** Letzter bekannter Tag (für Tagwechsel-Erkennung) */
    private long lastKnownDay = -1;

    /** Ticks seit letztem Update (für Performance) */
    private int ticksSinceUpdate = 0;
    private static final int UPDATE_INTERVAL = 20; // Alle 20 Ticks (1 Sekunde)

    /** Ticks seit letztem Safety-Update (seltener) */
    private int ticksSinceSafetyUpdate = 0;
    private static final int SAFETY_UPDATE_INTERVAL = 100; // Alle 5 Sekunden

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTORS
    // ═══════════════════════════════════════════════════════════

    public NPCLifeData() {
        this.needs = new NPCNeeds();
        this.emotions = new NPCEmotions();
        this.memory = new NPCMemory();
        this.traits = NPCTraits.randomize(ThreadLocalRandom.current());
    }

    public NPCLifeData(@Nullable CustomNPCEntity entity) {
        this();
        this.npcEntity = entity;

        // Falls NPC bereits eine Personality hat, Traits daraus ableiten
        if (entity != null) {
            var personality = entity.getPersonality();
            if (personality != null) {
                NPCTraits derived = NPCTraits.fromPersonality(personality);
                this.traits.setCourage(derived.getCourage());
                this.traits.setHonesty(derived.getHonesty());
                this.traits.setGreed(derived.getGreed());
            }
        }
    }

    /**
     * Erstellt NPCLifeData mit spezifischen Traits
     */
    public NPCLifeData(int courage, int honesty, int greed) {
        this.needs = new NPCNeeds();
        this.emotions = new NPCEmotions();
        this.memory = new NPCMemory();
        this.traits = new NPCTraits(courage, honesty, greed);
    }

    // ═══════════════════════════════════════════════════════════
    // TICK / UPDATE
    // ═══════════════════════════════════════════════════════════

    /**
     * Haupt-Tick-Methode - wird von CustomNPCEntity.tick() aufgerufen
     */
    public void tick(@Nullable CustomNPCEntity npc) {
        if (!enabled) return;

        this.npcEntity = npc;

        ticksSinceUpdate++;
        ticksSinceSafetyUpdate++;

        // Regelmäßiges Update
        if (ticksSinceUpdate >= UPDATE_INTERVAL) {
            ticksSinceUpdate = 0;

            // Bedürfnisse aktualisieren
            needs.tick();

            // Emotionen aktualisieren
            emotions.tick();
        }

        // Seltenes Safety-Update (Performance)
        if (ticksSinceSafetyUpdate >= SAFETY_UPDATE_INTERVAL && npc != null) {
            ticksSinceSafetyUpdate = 0;
            updateSafety(npc);
        }

        // Tagwechsel prüfen
        if (npc != null && npc.level() instanceof ServerLevel serverLevel) {
            long currentDay = serverLevel.getDayTime() / 24000;
            if (lastKnownDay != -1 && currentDay > lastKnownDay) {
                onDayChange(currentDay);
            }
            lastKnownDay = currentDay;
        }
    }

    /** Wiederverwendbare Listen für updateSafety (vermeidet Allokationen) */
    private final List<ServerPlayer> safetyNearbyPlayers = new ArrayList<>();
    private final List<CustomNPCEntity> safetyPoliceList = new ArrayList<>();

    /**
     * Aktualisiert den Sicherheitswert basierend auf Umgebung.
     * Optimiert: Nutzt Police-Cache statt getEntitiesOfClass(),
     * ersetzt Streams durch Loops, kombiniert recentCrime + knownCriminal in einem Pass.
     */
    private void updateSafety(CustomNPCEntity npc) {
        if (npc.level() instanceof ServerLevel level) {
            var npcPos = npc.blockPosition();
            var homePos = npc.getNpcData().getHomeLocation();

            // Spieler in der Nähe sammeln (Loop statt Stream)
            safetyNearbyPlayers.clear();
            for (ServerPlayer p : level.players()) {
                if (p.distanceToSqr(npc) < 400) { // 20 Blöcke
                    safetyNearbyPlayers.add(p);
                }
            }

            boolean isNight = level.isNight();

            // Polizei in der Nähe? (Nutze globalen Police-Cache statt getEntitiesOfClass)
            PoliceAIHandler.getPoliceInRadius(npc.position(), 30.0, safetyPoliceList);
            boolean policeNearby = !safetyPoliceList.isEmpty();

            // Freund in der Nähe? (Nutze gleichen Cache — nur NPCs in 15 Block Radius prüfen)
            // Statt getEntitiesOfClass: iteriere über alle geladenen Entities im Level
            boolean friendNearby = false;
            double friendRadiusSq = 15.0 * 15.0;
            Vec3 npcVec = npc.position();
            for (CustomNPCEntity candidate : level.getEntitiesOfClass(
                    CustomNPCEntity.class, npc.getBoundingBox().inflate(15),
                    e -> e != npc && e.getLifeData() != null && e.getLifeData().getMemory() != null)) {
                // Direkte Loop statt Stream.anyMatch
                for (ServerPlayer player : safetyNearbyPlayers) {
                    var myProfile = memory.getPlayerProfile(player.getUUID());
                    if (myProfile == null || myProfile.getRelationLevel() <= 20) continue;
                    var friendLifeData = candidate.getLifeData();
                    if (friendLifeData == null || friendLifeData.getMemory() == null) break;
                    var friendProfile = friendLifeData.getMemory().getPlayerProfile(player.getUUID());
                    if (friendProfile != null && friendProfile.getRelationLevel() > 20) {
                        friendNearby = true;
                        break;
                    }
                }
                if (friendNearby) break;
            }

            // Kombinierter Single-Pass: recentCrime + knownCriminal in einer Schleife
            boolean recentCrime = false;
            ServerPlayer knownCriminal = null;
            for (ServerPlayer p : safetyNearbyPlayers) {
                if (!recentCrime && de.rolandsw.schedulemc.npc.crime.CrimeManager.getWantedLevel(p.getUUID()) > 0) {
                    recentCrime = true;
                }
                if (knownCriminal == null && (memory.playerHasTag(p.getUUID(), "Kriminell") ||
                        memory.playerHasTag(p.getUUID(), "Gefährlich"))) {
                    knownCriminal = p;
                }
                if (recentCrime && knownCriminal != null) break;
            }

            needs.calculateSafety(level, npcPos, homePos, safetyNearbyPlayers,
                isNight, policeNearby, friendNearby, recentCrime, knownCriminal);
        }
    }

    /**
     * Wird beim Tagwechsel aufgerufen
     */
    private void onDayChange(long currentDay) {
        ageInDays++;

        // Memory: Tages-Zusammenfassung erstellen
        memory.onDayChange(currentDay);

        // Energie leicht regenerieren (neuer Tag = ausgeruht)
        if (!needs.isSleeping()) {
            needs.satisfy(NeedType.ENERGY, 10);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════

    public NPCNeeds getNeeds() {
        return needs;
    }

    public NPCEmotions getEmotions() {
        return emotions;
    }

    public NPCMemory getMemory() {
        return memory;
    }

    public NPCTraits getTraits() {
        return traits;
    }

    @Nullable
    public CustomNPCEntity getNpcEntity() {
        return npcEntity;
    }

    public void setNpcEntity(@Nullable CustomNPCEntity npcEntity) {
        this.npcEntity = npcEntity;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getAgeInDays() {
        return ageInDays;
    }

    // ═══════════════════════════════════════════════════════════
    // CONVENIENCE METHODS
    // ═══════════════════════════════════════════════════════════

    /**
     * Berechnet den kombinierten Preis-Modifikator
     * (Traits + Emotionen)
     */
    public float getCombinedPriceModifier() {
        float traitMod = traits.getTradeModifier();
        float emotionMod = emotions.getPriceModifier();

        // Multiplikativ kombinieren
        return traitMod * emotionMod;
    }

    /**
     * Berechnet den kombinierten Gesprächs-Modifikator
     */
    public float getCombinedSocialModifier() {
        return emotions.getSocialModifier();
    }

    /**
     * Prüft ob NPC kritische Bedürfnisse hat
     */
    public boolean hasCriticalNeeds() {
        return needs.hasCriticalNeed();
    }

    /**
     * Prüft ob NPC starke Emotion hat
     */
    public boolean hasStrongEmotion() {
        return emotions.hasStrongEmotion();
    }

    /**
     * Prüft ob NPC handeln möchte
     */
    public boolean isWillingToTrade() {
        // Nicht handeln wenn: kritische Bedürfnisse, starke negative Emotion
        if (needs.isCritical(NeedType.SAFETY)) return false;
        if (emotions.getCurrentEmotion() == EmotionState.FEARFUL && emotions.getIntensity() > 50) return false;
        if (emotions.getCurrentEmotion() == EmotionState.ANGRY && emotions.getIntensity() > 70) return false;

        return true;
    }

    /**
     * Prüft ob NPC sprechen möchte
     */
    public boolean isWillingToTalk() {
        if (needs.isCritical(NeedType.ENERGY)) return false;
        if (emotions.getCurrentEmotion() == EmotionState.FEARFUL) return false;

        return emotions.getSocialModifier() > 0.3f;
    }

    // ═══════════════════════════════════════════════════════════
    // NBT SERIALIZATION
    // ═══════════════════════════════════════════════════════════

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        tag.put("Needs", needs.save());
        tag.put("Emotions", emotions.save());
        tag.put("Memory", memory.save());
        tag.put("Traits", traits.save());

        tag.putBoolean("Enabled", enabled);
        tag.putInt("AgeInDays", ageInDays);
        tag.putLong("LastKnownDay", lastKnownDay);

        return tag;
    }

    public void load(CompoundTag tag) {
        if (tag.contains("Needs")) {
            needs.load(tag.getCompound("Needs"));
        }
        if (tag.contains("Emotions")) {
            emotions.load(tag.getCompound("Emotions"));
        }
        if (tag.contains("Memory")) {
            memory.load(tag.getCompound("Memory"));
        }
        if (tag.contains("Traits")) {
            traits.load(tag.getCompound("Traits"));
        }

        enabled = tag.getBoolean("Enabled");
        ageInDays = tag.getInt("AgeInDays");
        lastKnownDay = tag.getLong("LastKnownDay");
    }

    public static NPCLifeData fromTag(CompoundTag tag) {
        NPCLifeData data = new NPCLifeData();
        data.load(tag);
        return data;
    }

    // ═══════════════════════════════════════════════════════════
    // DEBUG
    // ═══════════════════════════════════════════════════════════

    @Override
    public String toString() {
        return String.format("NPCLifeData{age=%dd, %s, %s, %s}",
            ageInDays,
            emotions.getCurrentEmotion().getDisplayName(),
            needs.hasCriticalNeed() ? "CRITICAL" : "ok",
            traits.getArchetype()
        );
    }

    public String getDetailedStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== NPC Life Status ===\n");
        sb.append(String.format("Alter: %d Tage\n", ageInDays));
        sb.append(String.format("Energie: %.0f%% | Sicherheit: %.0f%%\n",
            needs.getEnergy(), needs.getSafety()));
        sb.append(String.format("Emotion: %s (%.0f%%)\n",
            emotions.getCurrentEmotion().getFormattedName(), emotions.getIntensity()));
        sb.append(String.format("Persönlichkeit: %s\n", traits.getArchetype()));
        sb.append(String.format("Preis-Mod: ×%.2f | Sozial-Mod: ×%.2f\n",
            getCombinedPriceModifier(), getCombinedSocialModifier()));
        sb.append(String.format("Handelsbereit: %s | Gesprächsbereit: %s",
            isWillingToTrade() ? "Ja" : "Nein", isWillingToTalk() ? "Ja" : "Nein"));
        return sb.toString();
    }
}
