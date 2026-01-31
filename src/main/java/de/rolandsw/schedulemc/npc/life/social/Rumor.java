package de.rolandsw.schedulemc.npc.life.social;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Rumor - Ein einzelnes Gerücht über einen Spieler
 *
 * Gerüchte verbreiten sich zwischen NPCs und beeinflussen
 * wie NPCs auf Spieler reagieren.
 */
public class Rumor {

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    /** Über wen das Gerücht ist */
    private final UUID subjectUUID;

    /** Typ des Gerüchts */
    private final RumorType type;

    /** Zusätzliche Details */
    private String details;

    /** Wann wurde das Gerücht erstellt (Game Day) */
    private final long createdDay;

    /** Wann läuft das Gerücht ab (Game Day) */
    private long expirationDay;

    /** Von wem stammt das Gerücht ursprünglich (NPC UUID) */
    private UUID sourceNPC;

    /** Wie oft wurde das Gerücht weitergegeben */
    private int spreadCount = 0;

    /** Intensität/Glaubwürdigkeit des Gerüchts (0-100) */
    private float credibility = 100.0f;

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════

    public Rumor(UUID subjectUUID, RumorType type, long currentDay) {
        this.subjectUUID = subjectUUID;
        this.type = type;
        this.createdDay = currentDay;
        this.expirationDay = currentDay + type.getBaseDurationDays();
        this.details = "";
    }

    public Rumor(UUID subjectUUID, RumorType type, String details, long currentDay) {
        this(subjectUUID, type, currentDay);
        this.details = details;
    }

    // ═══════════════════════════════════════════════════════════
    // FACTORY METHODS
    // ═══════════════════════════════════════════════════════════

    /**
     * Erstellt ein Gerücht über einen NPC
     * Hinweis: expirationDay wird später vom RumorNetwork beim Hinzufügen korrigiert
     */
    public static Rumor createNPC(UUID npcUUID, String details, int importance, int durationDays) {
        // Für NPC-bezogene Gerüchte verwenden wir HELPFUL als neutralen Typ
        // createdDay wird temporär auf 0 gesetzt, der eigentliche Tag wird beim addRumor gesetzt
        Rumor rumor = new Rumor(npcUUID, RumorType.HELPFUL, 0);
        rumor.details = details;
        // Speichere die Dauer temporär in expirationDay (wird bei addRumor korrigiert)
        rumor.expirationDay = durationDays;
        rumor.credibility = Math.min(100.0f, 50.0f + importance * 10);
        return rumor;
    }

    /**
     * Erstellt ein Gerücht über einen Spieler
     * Hinweis: expirationDay wird später vom RumorNetwork beim Hinzufügen korrigiert
     */
    public static Rumor createPlayer(UUID playerUUID, String details, int importance, int durationDays) {
        // Bestimme den Typ basierend auf den Details (neutral als Standard)
        RumorType type = details.toLowerCase().contains("kriminal") || details.toLowerCase().contains("verbrechen")
            ? RumorType.UNRELIABLE
            : RumorType.HELPFUL;
        Rumor rumor = new Rumor(playerUUID, type, 0);
        rumor.details = details;
        // Speichere die Dauer temporär in expirationDay (wird bei addRumor korrigiert)
        rumor.expirationDay = durationDays;
        rumor.credibility = Math.min(100.0f, 50.0f + importance * 10);
        return rumor;
    }

    /**
     * Erstellt ein Welt-Gerücht (ohne spezifisches Subjekt)
     * Hinweis: expirationDay wird später vom RumorNetwork beim Hinzufügen korrigiert
     */
    public static Rumor createWorld(String details, int importance, int durationDays) {
        // Für Welt-Events verwenden wir eine Null-UUID
        Rumor rumor = new Rumor(new UUID(0, 0), RumorType.HELPFUL, 0);
        rumor.details = details;
        // Speichere die Dauer temporär in expirationDay (wird bei addRumor korrigiert)
        rumor.expirationDay = durationDays;
        rumor.credibility = Math.min(100.0f, 60.0f + importance * 8);
        return rumor;
    }

    /**
     * Gibt an, ob die expirationDay korrigiert werden muss
     * (wenn createdDay == 0 und expirationDay klein ist, handelt es sich um eine Dauer)
     */
    public boolean needsExpirationCorrection() {
        return createdDay == 0 && expirationDay < 365;
    }

    /**
     * Korrigiert die expirationDay basierend auf dem aktuellen Tag
     */
    public void correctExpiration(long currentDay) {
        if (needsExpirationCorrection()) {
            // expirationDay enthält temporär die Dauer
            int duration = (int) expirationDay;
            // Jetzt korrigieren
            // Wir können createdDay nicht ändern (final), also nur expirationDay anpassen
            expirationDay = currentDay + duration;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // SPREAD MECHANICS
    // ═══════════════════════════════════════════════════════════

    /**
     * Versucht das Gerücht weiterzuverbreiten
     *
     * @return true wenn Gerücht erfolgreich verbreitet wurde
     */
    public boolean trySpread() {
        // Chance basierend auf Typ und Glaubwürdigkeit
        float chance = type.getSpreadChance() * (credibility / 100.0f);

        // Jede Weitergabe verringert die Chance
        chance *= Math.pow(0.9, spreadCount);

        if (ThreadLocalRandom.current().nextDouble() < chance) {
            spreadCount++;
            // Glaubwürdigkeit sinkt mit jeder Weitergabe ("Stille Post" Effekt)
            credibility = Math.max(10.0f, credibility - 5.0f);
            return true;
        }
        return false;
    }

    /**
     * Erhöht die Verbreitungszählung (ohne Chance-Check)
     */
    public void recordSpread() {
        spreadCount++;
        credibility = Math.max(10.0f, credibility - 5.0f);
    }

    /**
     * Verstärkt das Gerücht (z.B. wenn erneut beobachtet)
     */
    public void reinforce(long currentDay) {
        credibility = Math.min(100.0f, credibility + 20.0f);
        // Verlängere Ablaufzeit
        expirationDay = Math.max(expirationDay, currentDay + type.getBaseDurationDays() / 2);
    }

    // ═══════════════════════════════════════════════════════════
    // QUERIES
    // ═══════════════════════════════════════════════════════════

    /**
     * Prüft ob das Gerücht abgelaufen ist
     */
    public boolean isExpired(long currentDay) {
        return currentDay >= expirationDay;
    }

    /**
     * Prüft ob das Gerücht noch glaubwürdig ist
     */
    public boolean isCredible() {
        return credibility >= 20.0f;
    }

    /**
     * Berechnet den aktuellen Reputations-Effekt
     * (basierend auf Glaubwürdigkeit)
     */
    public int getEffectiveReputationImpact() {
        return (int) (type.getReputationImpact() * (credibility / 100.0f));
    }

    /**
     * Verbleibende Tage bis Ablauf
     */
    public long getDaysRemaining(long currentDay) {
        return Math.max(0, expirationDay - currentDay);
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════

    public UUID getSubjectUUID() {
        return subjectUUID;
    }

    public RumorType getType() {
        return type;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public long getCreatedDay() {
        return createdDay;
    }

    public long getExpirationDay() {
        return expirationDay;
    }

    public void setExpirationDay(long expirationDay) {
        this.expirationDay = expirationDay;
    }

    public UUID getSourceNPC() {
        return sourceNPC;
    }

    public void setSourceNPC(UUID sourceNPC) {
        this.sourceNPC = sourceNPC;
    }

    public int getSpreadCount() {
        return spreadCount;
    }

    public float getCredibility() {
        return credibility;
    }

    public void setCredibility(float credibility) {
        this.credibility = Math.max(0, Math.min(100, credibility));
    }

    // ═══════════════════════════════════════════════════════════
    // NBT SERIALIZATION
    // ═══════════════════════════════════════════════════════════

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("SubjectUUID", subjectUUID);
        tag.putString("Type", type.name());
        tag.putString("Details", details);
        tag.putLong("CreatedDay", createdDay);
        tag.putLong("ExpirationDay", expirationDay);
        if (sourceNPC != null) {
            tag.putUUID("SourceNPC", sourceNPC);
        }
        tag.putInt("SpreadCount", spreadCount);
        tag.putFloat("Credibility", credibility);
        return tag;
    }

    public static Rumor load(CompoundTag tag) {
        UUID subjectUUID = tag.getUUID("SubjectUUID");
        RumorType type = RumorType.fromName(tag.getString("Type"));
        long createdDay = tag.getLong("CreatedDay");

        Rumor rumor = new Rumor(subjectUUID, type, createdDay);
        rumor.details = tag.getString("Details");
        rumor.expirationDay = tag.getLong("ExpirationDay");
        if (tag.contains("SourceNPC")) {
            rumor.sourceNPC = tag.getUUID("SourceNPC");
        }
        rumor.spreadCount = tag.getInt("SpreadCount");
        rumor.credibility = tag.getFloat("Credibility");

        return rumor;
    }

    // ═══════════════════════════════════════════════════════════
    // DEBUG
    // ═══════════════════════════════════════════════════════════

    @Override
    public String toString() {
        return String.format("Rumor{type=%s, credibility=%.0f%%, spread=%d}",
            type.getDisplayName(), credibility, spreadCount);
    }

    public String getFullDescription() {
        return String.format("%s: %s (Glaubwürdigkeit: %.0f%%)",
            type.getDisplayName(),
            details.isEmpty() ? "Keine Details" : details,
            credibility
        );
    }
}
