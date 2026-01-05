package de.rolandsw.schedulemc.region;

import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * PlotArea - Unterbereich eines Plots (z.B. Wohnung in Mehrfamilienhaus)
 *
 * Features:
 * - Eigene Begrenzungen innerhalb des Haupt-Plots
 * - Separates Mietsystem
 * - Eigene Trusted-List (Gäste)
 * - Unabhängige Vermietung vom Haupt-Plot
 */
public class PlotArea {

    // ═══════════════════════════════════════════════════════════
    // IDENTIFIKATION
    // ═══════════════════════════════════════════════════════════
    private String id;                    // "apt_1", "apt_2", etc.
    private String name;                  // "Wohnung 1. OG Links"
    private String parentPlotId;          // ID des Haupt-Plots

    // ═══════════════════════════════════════════════════════════
    // BEGRENZUNGEN
    // ═══════════════════════════════════════════════════════════
    private BlockPos minCorner;
    private BlockPos maxCorner;

    // ═══════════════════════════════════════════════════════════
    // VERMIETUNG
    // ═══════════════════════════════════════════════════════════
    private String renterUUID;            // Mieter (leer = verfügbar)
    private double monthlyRent;           // Miete pro Monat
    private long rentalStartDate;         // Mietbeginn (System.currentTimeMillis())
    private long rentalEndDate;           // Mietende (0 = unbefristet)
    private boolean forRent;              // Zur Miete verfügbar?

    // ═══════════════════════════════════════════════════════════
    // RECHTE
    // ═══════════════════════════════════════════════════════════
    private Set<String> trustedPlayers;   // Gäste (zusätzlich zum Mieter)

    // ═══════════════════════════════════════════════════════════
    // KONSTRUKTOR
    // ═══════════════════════════════════════════════════════════

    /**
     * Erstellt neuen Unterbereich
     */
    public PlotArea(String id, String name, String parentPlotId,
                    BlockPos minCorner, BlockPos maxCorner, double monthlyRent) {
        this.id = id;
        this.name = name;
        this.parentPlotId = parentPlotId;
        this.minCorner = minCorner;
        this.maxCorner = maxCorner;
        this.monthlyRent = monthlyRent;

        this.renterUUID = "";
        this.rentalStartDate = 0;
        this.rentalEndDate = 0;
        this.forRent = true;  // Standardmäßig zur Miete verfügbar
        this.trustedPlayers = new HashSet<>();
    }

    // ═══════════════════════════════════════════════════════════
    // GETTER & SETTER
    // ═══════════════════════════════════════════════════════════

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getParentPlotId() { return parentPlotId; }

    public BlockPos getMinCorner() { return minCorner; }
    public BlockPos getMaxCorner() { return maxCorner; }

    public double getMonthlyRent() { return monthlyRent; }
    public void setMonthlyRent(double rent) { this.monthlyRent = rent; }

    public String getRenterUUID() { return renterUUID != null ? renterUUID : ""; }
    public void setRenterUUID(String uuid) { this.renterUUID = uuid; }

    @Nullable
    public UUID getRenterUUIDAsUUID() {
        if (renterUUID == null || renterUUID.isEmpty()) return null;
        try {
            return UUID.fromString(renterUUID);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public long getRentalStartDate() { return rentalStartDate; }
    public void setRentalStartDate(long date) { this.rentalStartDate = date; }

    public long getRentalEndDate() { return rentalEndDate; }
    public void setRentalEndDate(long date) { this.rentalEndDate = date; }

    public boolean isForRent() { return forRent; }
    public void setForRent(boolean forRent) { this.forRent = forRent; }

    public Set<String> getTrustedPlayers() {
        if (trustedPlayers == null) trustedPlayers = new HashSet<>();
        return trustedPlayers;
    }

    // ═══════════════════════════════════════════════════════════
    // VERMIETUNGS-LOGIK
    // ═══════════════════════════════════════════════════════════

    /**
     * Ist aktuell vermietet?
     */
    public boolean isRented() {
        if (renterUUID == null || renterUUID.isEmpty()) {
            return false;
        }

        // Unbefristet vermietet
        if (rentalEndDate == 0) {
            return true;
        }

        // Prüfe ob Mietende noch nicht erreicht
        return rentalEndDate > System.currentTimeMillis();
    }

    /**
     * Ist Miete abgelaufen?
     */
    public boolean isRentExpired() {
        if (renterUUID == null || renterUUID.isEmpty()) {
            return false;
        }

        if (rentalEndDate == 0) {
            return false; // Unbefristet
        }

        return rentalEndDate <= System.currentTimeMillis();
    }

    /**
     * Gibt verbleibende Miettage zurück
     */
    public long getRentDaysLeft() {
        if (!isRented()) return 0;
        if (rentalEndDate == 0) return Long.MAX_VALUE; // Unbefristet

        long diff = rentalEndDate - System.currentTimeMillis();
        return Math.max(0, diff / (1000 * 60 * 60 * 24));
    }

    /**
     * Gibt verbleibende Mietstunden zurück
     */
    public long getRentHoursLeft() {
        if (!isRented()) return 0;
        if (rentalEndDate == 0) return Long.MAX_VALUE; // Unbefristet

        long diff = rentalEndDate - System.currentTimeMillis();
        return Math.max(0, diff / (1000 * 60 * 60));
    }

    /**
     * Beendet die Miete
     */
    public void endRent() {
        this.renterUUID = "";
        this.rentalStartDate = 0;
        this.rentalEndDate = 0;
        this.trustedPlayers.clear();
    }

    /**
     * Startet neue Miete
     */
    public void startRent(UUID renter, int durationDays) {
        this.renterUUID = renter.toString();
        this.rentalStartDate = System.currentTimeMillis();

        if (durationDays > 0) {
            this.rentalEndDate = rentalStartDate + (durationDays * 24L * 60 * 60 * 1000);
        } else {
            this.rentalEndDate = 0; // Unbefristet
        }
    }

    // ═══════════════════════════════════════════════════════════
    // TRUSTED PLAYERS
    // ═══════════════════════════════════════════════════════════

    public void addTrustedPlayer(UUID uuid) {
        getTrustedPlayers().add(uuid.toString());
    }

    public void removeTrustedPlayer(UUID uuid) {
        getTrustedPlayers().remove(uuid.toString());
    }

    public boolean isTrusted(UUID uuid) {
        return getTrustedPlayers().contains(uuid.toString());
    }

    public void clearTrustedPlayers() {
        getTrustedPlayers().clear();
    }

    // ═══════════════════════════════════════════════════════════
    // BERECHTIGUNGEN
    // ═══════════════════════════════════════════════════════════

    /**
     * Hat Spieler Zugriff auf diesen Bereich?
     * (Mieter ODER Trusted Player)
     */
    public boolean hasAccess(UUID uuid) {
        if (uuid == null) return false;

        // Ist Mieter?
        if (isRented() && uuid.toString().equals(renterUUID)) {
            return true;
        }

        // Ist Trusted?
        if (isTrusted(uuid)) {
            return true;
        }

        return false;
    }

    /**
     * Kann Spieler diesen Bereich verwalten?
     * (Nur Mieter, nicht Trusted Players)
     */
    public boolean canManage(UUID uuid) {
        return isRented() && uuid.toString().equals(renterUUID);
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITY
    // ═══════════════════════════════════════════════════════════

    /**
     * Prüft ob Position innerhalb dieses Bereichs liegt
     */
    public boolean contains(BlockPos pos) {
        return pos.getX() >= minCorner.getX() && pos.getX() <= maxCorner.getX() &&
               pos.getY() >= minCorner.getY() && pos.getY() <= maxCorner.getY() &&
               pos.getZ() >= minCorner.getZ() && pos.getZ() <= maxCorner.getZ();
    }

    /**
     * Berechnet Volumen
     */
    public long getVolume() {
        long dx = maxCorner.getX() - minCorner.getX() + 1;
        long dy = maxCorner.getY() - minCorner.getY() + 1;
        long dz = maxCorner.getZ() - minCorner.getZ() + 1;
        return dx * dy * dz;
    }

    /**
     * Gibt Zentrum zurück
     */
    public BlockPos getCenter() {
        return new BlockPos(
            (minCorner.getX() + maxCorner.getX()) / 2,
            (minCorner.getY() + maxCorner.getY()) / 2,
            (minCorner.getZ() + maxCorner.getZ()) / 2
        );
    }

    /**
     * Prüft ob sich dieser Bereich mit einem anderen überschneidet
     */
    public boolean overlaps(PlotArea other) {
        return !(maxCorner.getX() < other.minCorner.getX() ||
                 minCorner.getX() > other.maxCorner.getX() ||
                 maxCorner.getY() < other.minCorner.getY() ||
                 minCorner.getY() > other.maxCorner.getY() ||
                 maxCorner.getZ() < other.minCorner.getZ() ||
                 minCorner.getZ() > other.maxCorner.getZ());
    }

    /**
     * Prüft ob dieser Bereich mit gegebenen Koordinaten überschneidet
     */
    public boolean overlaps(BlockPos otherMin, BlockPos otherMax) {
        return !(maxCorner.getX() < otherMin.getX() ||
                 minCorner.getX() > otherMax.getX() ||
                 maxCorner.getY() < otherMin.getY() ||
                 minCorner.getY() > otherMax.getY() ||
                 maxCorner.getZ() < otherMin.getZ() ||
                 minCorner.getZ() > otherMax.getZ());
    }

    @Override
    public String toString() {
        String status = isRented() ? "Vermietet" : (forRent ? "Verfügbar" : "Nicht verfügbar");
        return String.format("PlotArea[ID=%s, Name='%s', Rent=%.2f€/Monat, Status=%s, Volume=%d]",
            id, name, monthlyRent, status, getVolume());
    }
}
