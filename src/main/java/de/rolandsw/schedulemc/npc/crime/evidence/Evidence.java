package de.rolandsw.schedulemc.npc.crime.evidence;

import com.google.gson.annotations.SerializedName;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Feature 10: Einzelner Beweis in einer Beweiskette
 *
 * Jeder Beweis hat einen Typ, Zuverlaessigkeit und Beschreibung.
 * Beweise werden bei Verbrechen automatisch erstellt und koennen
 * die Strafhoehe beeinflussen.
 */
public class Evidence {

    /**
     * Typen von Beweisen
     */
    public enum EvidenceType {
        /** Zeugenaussage eines NPC */
        WITNESS_TESTIMONY(0.6f),
        /** Physischer Beweis (z.B. Tatwaffe, gestohlene Items) */
        PHYSICAL_EVIDENCE(0.8f),
        /** Kamera-Aufnahme (z.B. SecurityCam Block) */
        CAMERA_FOOTAGE(0.9f),
        /** Auf frischer Tat ertappt */
        CAUGHT_IN_ACT(1.0f),
        /** Polizei-Bericht */
        POLICE_REPORT(0.85f),
        /** Gestaendnis */
        CONFESSION(0.95f);

        /** Basis-Zuverlaessigkeit dieses Beweistyps (0.0 - 1.0) */
        public final float baseReliability;

        EvidenceType(float baseReliability) {
            this.baseReliability = baseReliability;
        }
    }

    @SerializedName("id")
    private final String id;

    @SerializedName("type")
    private final EvidenceType type;

    @SerializedName("timestamp")
    private final long timestamp;

    @SerializedName("locationX")
    private final int locationX;

    @SerializedName("locationY")
    private final int locationY;

    @SerializedName("locationZ")
    private final int locationZ;

    @SerializedName("reliability")
    private final float reliability;

    @SerializedName("description")
    private final String description;

    @SerializedName("witnessUUID")
    @Nullable
    private final String witnessUUID;

    public Evidence(EvidenceType type, @Nullable BlockPos location, float reliability,
                    String description, @Nullable UUID witnessUUID) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.timestamp = System.currentTimeMillis();
        this.locationX = location != null ? location.getX() : 0;
        this.locationY = location != null ? location.getY() : 0;
        this.locationZ = location != null ? location.getZ() : 0;
        this.reliability = Math.max(0.0f, Math.min(1.0f, reliability));
        this.description = description;
        this.witnessUUID = witnessUUID != null ? witnessUUID.toString() : null;
    }

    // Getters
    public String getId() { return id; }
    public EvidenceType getType() { return type; }
    public long getTimestamp() { return timestamp; }
    public float getReliability() { return reliability; }
    public String getDescription() { return description; }

    @Nullable
    public BlockPos getLocation() {
        if (locationX == 0 && locationY == 0 && locationZ == 0) return null;
        return new BlockPos(locationX, locationY, locationZ);
    }

    @Nullable
    public UUID getWitnessUUID() {
        return witnessUUID != null ? UUID.fromString(witnessUUID) : null;
    }

    /**
     * Berechnet den Beweiswert (0.0 - 1.0) unter Beruecksichtigung
     * von Beweistyp und individueller Zuverlaessigkeit
     */
    public float getEvidenceValue() {
        return type.baseReliability * reliability;
    }

    @Override
    public String toString() {
        return String.format("Evidence[%s, reliability=%.2f, %s]",
            type.name(), reliability, description);
    }
}
