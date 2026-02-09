package de.rolandsw.schedulemc.npc.life.world;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * WorldEvent - Repräsentiert ein aktives Welt-Event
 *
 * Ein Event ist zeitlich begrenzt und beeinflusst die Spielwelt.
 */
public class WorldEvent {

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    private final UUID eventId;
    private final WorldEventType type;
    private final long startDay;
    private final int durationDays;

    /** Optionale Position (für lokale Events) */
    @Nullable
    private BlockPos epicenter;

    /** Optionaler Radius (für lokale Events) */
    private int radius = 0;

    /** Intensität (0-1) - kann sich über Zeit ändern */
    private float intensity = 1.0f;

    /** Wurde das Event angekündigt? */
    private boolean announced = false;

    /** Ist das Event aktiv? */
    private boolean active = true;

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════

    public WorldEvent(WorldEventType type, long startDay) {
        this.eventId = UUID.randomUUID();
        this.type = type;
        this.startDay = startDay;
        this.durationDays = type.getRandomDuration();
    }

    public WorldEvent(WorldEventType type, long startDay, int durationDays) {
        this.eventId = UUID.randomUUID();
        this.type = type;
        this.startDay = startDay;
        this.durationDays = durationDays;
    }

    private WorldEvent(UUID eventId, WorldEventType type, long startDay, int durationDays) {
        this.eventId = eventId;
        this.type = type;
        this.startDay = startDay;
        this.durationDays = durationDays;
    }

    // ═══════════════════════════════════════════════════════════
    // FACTORY METHODS
    // ═══════════════════════════════════════════════════════════

    /**
     * Erstellt ein globales Event
     */
    public static WorldEvent global(WorldEventType type, long startDay) {
        return new WorldEvent(type, startDay);
    }

    /**
     * Erstellt ein lokales Event mit Epizentrum
     */
    public static WorldEvent local(WorldEventType type, long startDay, BlockPos epicenter, int radius) {
        WorldEvent event = new WorldEvent(type, startDay);
        event.epicenter = epicenter;
        event.radius = radius;
        return event;
    }

    // ═══════════════════════════════════════════════════════════
    // LIFECYCLE
    // ═══════════════════════════════════════════════════════════

    /**
     * Prüft ob das Event abgelaufen ist
     */
    public boolean isExpired(long currentDay) {
        return currentDay >= startDay + durationDays;
    }

    /**
     * Prüft ob das Event gestartet hat
     */
    public boolean hasStarted(long currentDay) {
        return currentDay >= startDay;
    }

    /**
     * Berechnet den Fortschritt des Events (0-1)
     */
    public float getProgress(long currentDay) {
        if (!hasStarted(currentDay)) return 0;
        if (isExpired(currentDay)) return 1;

        long elapsed = currentDay - startDay;
        return (float) elapsed / durationDays;
    }

    /**
     * Berechnet verbleibende Tage
     */
    public int getRemainingDays(long currentDay) {
        return Math.max(0, (int)(startDay + durationDays - currentDay));
    }

    /**
     * Beendet das Event vorzeitig
     */
    public void end() {
        active = false;
    }

    // ═══════════════════════════════════════════════════════════
    // LOCATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Ist dies ein lokales Event?
     */
    public boolean isLocal() {
        return epicenter != null && radius > 0;
    }

    /**
     * Ist eine Position vom Event betroffen?
     */
    public boolean affectsPosition(BlockPos pos) {
        if (!isLocal()) return true; // Globale Events betreffen alles

        double distance = Math.sqrt(pos.distSqr(epicenter));
        return distance <= radius;
    }

    /**
     * Berechnet die Intensität an einer Position
     */
    public float getIntensityAt(BlockPos pos) {
        if (!isLocal()) return intensity;

        double distance = Math.sqrt(pos.distSqr(epicenter));
        if (distance > radius) return 0;

        // Lineare Abnahme vom Zentrum
        float distanceFactor = 1.0f - (float)(distance / radius);
        return intensity * distanceFactor;
    }

    // ═══════════════════════════════════════════════════════════
    // EFFECTS
    // ═══════════════════════════════════════════════════════════

    /**
     * Berechnet den aktuellen Preismodifikator
     */
    public float getCurrentPriceModifier(long currentDay) {
        if (!hasStarted(currentDay) || isExpired(currentDay)) return 1.0f;

        float progress = getProgress(currentDay);

        // Intensität nimmt zu Beginn zu und gegen Ende ab
        float effectIntensity;
        if (progress < 0.2f) {
            effectIntensity = progress / 0.2f; // Aufbauphase
        } else if (progress > 0.8f) {
            effectIntensity = (1.0f - progress) / 0.2f; // Abbauphase
        } else {
            effectIntensity = 1.0f; // Volle Stärke
        }

        float baseModifier = type.getPriceModifier();
        return 1.0f + (baseModifier - 1.0f) * effectIntensity * intensity;
    }

    /**
     * Berechnet den aktuellen Stimmungseffekt
     */
    public float getCurrentMoodEffect(long currentDay) {
        if (!hasStarted(currentDay) || isExpired(currentDay)) return 0;

        float progress = getProgress(currentDay);
        float effectIntensity = progress < 0.2f ? progress / 0.2f :
                               (progress > 0.8f ? (1.0f - progress) / 0.2f : 1.0f);

        return type.getMoodEffect() * effectIntensity * intensity;
    }

    /**
     * Berechnet den aktuellen Sicherheitseffekt
     */
    public float getCurrentSafetyEffect(long currentDay) {
        if (!hasStarted(currentDay) || isExpired(currentDay)) return 0;

        return type.getSafetyEffect() * intensity;
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS / SETTERS
    // ═══════════════════════════════════════════════════════════

    public UUID getEventId() { return eventId; }
    public WorldEventType getType() { return type; }
    public long getStartDay() { return startDay; }
    public int getDurationDays() { return durationDays; }

    @Nullable public BlockPos getEpicenter() { return epicenter; }
    public void setEpicenter(BlockPos epicenter) { this.epicenter = epicenter; }

    public int getRadius() { return radius; }
    public void setRadius(int radius) { this.radius = radius; }

    public float getIntensity() { return intensity; }
    public void setIntensity(float intensity) { this.intensity = Math.max(0, Math.min(1, intensity)); }

    public boolean isAnnounced() { return announced; }
    public void setAnnounced(boolean announced) { this.announced = announced; }

    public boolean isActive() { return active; }

    // ═══════════════════════════════════════════════════════════
    // SERIALIZATION
    // ═══════════════════════════════════════════════════════════

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        tag.putUUID("eventId", eventId);
        tag.putString("type", type.name());
        tag.putLong("startDay", startDay);
        tag.putInt("durationDays", durationDays);
        tag.putFloat("intensity", intensity);
        tag.putBoolean("announced", announced);
        tag.putBoolean("active", active);

        if (epicenter != null) {
            tag.putInt("epicenterX", epicenter.getX());
            tag.putInt("epicenterY", epicenter.getY());
            tag.putInt("epicenterZ", epicenter.getZ());
            tag.putInt("radius", radius);
        }

        return tag;
    }

    public static WorldEvent load(CompoundTag tag) {
        UUID eventId = tag.getUUID("eventId");
        WorldEventType type;
        try {
            type = WorldEventType.valueOf(tag.getString("type"));
        } catch (IllegalArgumentException e) {
            type = WorldEventType.values()[0];
        }
        long startDay = tag.getLong("startDay");
        int durationDays = tag.getInt("durationDays");

        WorldEvent event = new WorldEvent(eventId, type, startDay, durationDays);

        event.intensity = tag.getFloat("intensity");
        event.announced = tag.getBoolean("announced");
        event.active = tag.getBoolean("active");

        if (tag.contains("epicenterX")) {
            event.epicenter = new BlockPos(
                tag.getInt("epicenterX"),
                tag.getInt("epicenterY"),
                tag.getInt("epicenterZ")
            );
            event.radius = tag.getInt("radius");
        }

        return event;
    }

    @Override
    public String toString() {
        return String.format("WorldEvent{type=%s, start=%d, duration=%d, intensity=%.1f, active=%s}",
            type, startDay, durationDays, intensity, active);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof WorldEvent other)) return false;
        return eventId.equals(other.eventId);
    }

    @Override
    public int hashCode() {
        return eventId.hashCode();
    }
}
