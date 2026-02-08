package de.rolandsw.schedulemc.npc.entity.component;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.nbt.CompoundTag;

/**
 * Fahr-Komponente fuer NPC-Entitaeten.
 *
 * Verwaltet den Fahrzustand eines NPCs:
 * - Fahrmodus (aktiv/inaktiv)
 * - Fahrzeugrotation (Yaw)
 * - Fahrzeugfarbe
 * - Sirenen-Status (Polizei)
 *
 * Extrahiert aus CustomNPCEntity fuer bessere Modularitaet.
 */
public class DrivingComponent implements NPCComponent {

    private boolean isDriving = false;
    private float vehicleYaw = 0f;
    private int vehicleColor = 0;
    private boolean sirenActive = false;

    // Fahrgeschwindigkeit und -richtung
    private double speed = 0;
    private double targetX, targetY, targetZ;
    private boolean hasTarget = false;

    @Override
    public String getComponentId() {
        return "driving";
    }

    @Override
    public int getUpdateInterval() {
        return isDriving ? 1 : 20; // Jeden Tick wenn fahrend, sonst jede Sekunde
    }

    @Override
    public void tick(CustomNPCEntity entity) {
        if (!isDriving) return;

        // Fahrzeug-Rotation mit Entity synchronisieren
        if (hasTarget) {
            double dx = targetX - entity.getX();
            double dz = targetZ - entity.getZ();
            vehicleYaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // DRIVING API
    // ═══════════════════════════════════════════════════════════

    public void startDriving(int color) {
        this.isDriving = true;
        this.vehicleColor = color;
    }

    public void stopDriving() {
        this.isDriving = false;
        this.hasTarget = false;
        this.speed = 0;
    }

    public void setDriveTarget(double x, double y, double z, double speed) {
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
        this.speed = speed;
        this.hasTarget = true;
    }

    // Getters
    public boolean isDriving() { return isDriving; }
    public float getVehicleYaw() { return vehicleYaw; }
    public int getVehicleColor() { return vehicleColor; }
    public boolean isSirenActive() { return sirenActive; }
    public double getSpeed() { return speed; }

    // Setters
    public void setVehicleYaw(float yaw) { this.vehicleYaw = yaw; }
    public void setVehicleColor(int color) { this.vehicleColor = color; }
    public void setSirenActive(boolean active) { this.sirenActive = active; }

    // ═══════════════════════════════════════════════════════════
    // PERSISTENCE
    // ═══════════════════════════════════════════════════════════

    @Override
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("isDriving", isDriving);
        tag.putFloat("vehicleYaw", vehicleYaw);
        tag.putInt("vehicleColor", vehicleColor);
        tag.putBoolean("sirenActive", sirenActive);
        return tag;
    }

    @Override
    public void load(CompoundTag tag) {
        isDriving = tag.getBoolean("isDriving");
        vehicleYaw = tag.getFloat("vehicleYaw");
        vehicleColor = tag.getInt("vehicleColor");
        sirenActive = tag.getBoolean("sirenActive");
    }
}
