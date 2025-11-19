package de.rolandsw.schedulemc.region;

import net.minecraft.core.BlockPos;
import java.util.*;

/**
 * ScheduleMC 3.0 - Erweiterte PlotRegion
 * 
 * Neue Features:
 * - Trusted Players
 * - Plot-Namen & Beschreibung
 * - Verkauf & Transfer
 * - Mietsystem
 * - Rating-System
 * - Statistiken
 */
public class PlotRegion {
    
    // ═══════════════════════════════════════════════════════════
    // BASIS-DATEN
    // ═══════════════════════════════════════════════════════════
    private String ownerUUID;
    private String ownerName;  // Spieler-Name des Besitzers
    private final BlockPos min;
    private final BlockPos max;
    private double price;
    
    // ═══════════════════════════════════════════════════════════
    // PLOT-IDENTIFIKATION
    // ═══════════════════════════════════════════════════════════
    private String plotId;
    private String plotName;
    private String description;
    
    // ═══════════════════════════════════════════════════════════
    // TRUSTED PLAYERS
    // ═══════════════════════════════════════════════════════════
    private Set<String> trustedPlayers;
    
    // ═══════════════════════════════════════════════════════════
    // VERKAUF
    // ═══════════════════════════════════════════════════════════
    private boolean forSale;
    private double salePrice;
    
    // ═══════════════════════════════════════════════════════════
    // MIETSYSTEM
    // ═══════════════════════════════════════════════════════════
    private boolean forRent;
    private double rentPricePerDay;
    private String renterUUID;
    private long rentEndTime;
    
    // ═══════════════════════════════════════════════════════════
    // RATING-SYSTEM
    // ═══════════════════════════════════════════════════════════
    private Map<String, Integer> ratings;
    
    // ═══════════════════════════════════════════════════════════
    // STATISTIKEN
    // ═══════════════════════════════════════════════════════════
    private long creationTime;
    private long lastVisited;
    private int visitCount;

    // Öffentlicher Plot?
    private boolean isPublic;

    /**
     * Konstruktor für neue Plots
     *
     * BEHOBEN: Der erste Parameter ist jetzt plotId (wurde vorher fälschlicherweise ignoriert)
     */
    public PlotRegion(String plotId, BlockPos min, BlockPos max, double price) {
        this.ownerUUID = "";  // Kein Besitzer bei Erstellung
        this.ownerName = null;
        this.min = min;
        this.max = max;
        this.price = price;
        this.isPublic = false;

        // ID & Namen - VERWENDE die übergebene plotId!
        this.plotId = plotId;
        this.plotName = "Unbenannter Plot";
        this.description = "";

        // Collections initialisieren
        this.trustedPlayers = new HashSet<>();
        this.ratings = new HashMap<>();

        // Verkauf & Miete
        this.forSale = false;
        this.salePrice = 0;
        this.forRent = false;
        this.rentPricePerDay = 0;
        this.renterUUID = "";
        this.rentEndTime = 0;

        // Statistiken
        this.creationTime = System.currentTimeMillis();
        this.lastVisited = System.currentTimeMillis();
        this.visitCount = 0;
    }

    // ═══════════════════════════════════════════════════════════
    // BASIS GETTER/SETTER
    // ═══════════════════════════════════════════════════════════
    
    public String getOwnerUUID() { return ownerUUID; }
    public void setOwnerUUID(String uuid) { this.ownerUUID = uuid; }
    
    public UUID getOwnerUUIDAsUUID() {
        if (ownerUUID == null || ownerUUID.isEmpty()) return null;
        try { return UUID.fromString(ownerUUID); }
        catch (IllegalArgumentException e) { return null; }
    }
    
    public void setOwnerUUID(UUID uuid) {
        this.ownerUUID = uuid != null ? uuid.toString() : "";
    }
    
    /**
     * Setzt Besitzer mit UUID und Namen gleichzeitig
     */
    public void setOwner(UUID uuid, String name) {
        this.ownerUUID = uuid.toString();
        this.ownerName = name;
    }
    
    /**
     * Gibt den Namen des Besitzers zurück
     */
    public String getOwnerName() {
        return ownerName != null ? ownerName : "Niemand";
    }
    
    /**
     * Setzt den Namen des Besitzers
     */
    public void setOwnerName(String name) {
        this.ownerName = name;
    }
    
    /**
     * Ist Plot öffentlich?
     */
    public boolean isPublic() {
        return isPublic;
    }
    
    /**
     * Setzt Plot öffentlich
     */
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }
    
    public BlockPos getMin() { return min; }
    public BlockPos getMax() { return max; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    // ═══════════════════════════════════════════════════════════
    // PLOT-IDENTIFIKATION
    // ═══════════════════════════════════════════════════════════
    
    public String getPlotId() { return plotId; }
    public void setPlotId(String id) { this.plotId = id; }
    
    public String getPlotName() { return plotName; }
    public void setPlotName(String name) { 
        this.plotName = name != null && !name.isEmpty() ? name : "Unbenannter Plot"; 
    }
    
    public String getDescription() { return description != null ? description : ""; }
    public void setDescription(String desc) { this.description = desc != null ? desc : ""; }

    // ═══════════════════════════════════════════════════════════
    // TRUSTED PLAYERS
    // ═══════════════════════════════════════════════════════════
    
    public Set<String> getTrustedPlayers() {
        if (trustedPlayers == null) trustedPlayers = new HashSet<>();
        return trustedPlayers;
    }
    
    public void addTrustedPlayer(UUID uuid) {
        getTrustedPlayers().add(uuid.toString());
    }
    
    public void removeTrustedPlayer(UUID uuid) {
        getTrustedPlayers().remove(uuid.toString());
    }
    
    public boolean isTrusted(UUID uuid) {
        return getTrustedPlayers().contains(uuid.toString());
    }
    
    public int getTrustedCount() {
        return getTrustedPlayers().size();
    }
    
    public void clearTrustedPlayers() {
        getTrustedPlayers().clear();
    }

    // ═══════════════════════════════════════════════════════════
    // VERKAUF
    // ═══════════════════════════════════════════════════════════
    
    public boolean isForSale() { return forSale; }
    public void setForSale(boolean forSale) { this.forSale = forSale; }
    
    public double getSalePrice() { return salePrice; }
    public void setSalePrice(double price) { this.salePrice = price; }

    // ═══════════════════════════════════════════════════════════
    // MIETSYSTEM
    // ═══════════════════════════════════════════════════════════
    
    public boolean isForRent() { return forRent; }
    public void setForRent(boolean forRent) { this.forRent = forRent; }
    
    public double getRentPricePerDay() { return rentPricePerDay; }
    public void setRentPricePerDay(double price) { this.rentPricePerDay = price; }
    
    public String getRenterUUID() { return renterUUID != null ? renterUUID : ""; }
    public void setRenterUUID(String uuid) { this.renterUUID = uuid; }
    
    public long getRentEndTime() { return rentEndTime; }
    public void setRentEndTime(long time) { this.rentEndTime = time; }
    
    /**
     * Prüft ob Plot aktuell vermietet ist (und Miete noch läuft)
     */
    public boolean isRented() {
        return renterUUID != null && !renterUUID.isEmpty() && 
               rentEndTime > System.currentTimeMillis();
    }
    
    /**
     * Prüft ob Miete abgelaufen ist
     */
    public boolean isRentExpired() {
        return renterUUID != null && !renterUUID.isEmpty() && 
               rentEndTime > 0 && rentEndTime <= System.currentTimeMillis();
    }
    
    /**
     * Gibt verbleibende Miettage zurück
     */
    public long getRentDaysLeft() {
        if (!isRented()) return 0;
        long diff = rentEndTime - System.currentTimeMillis();
        return Math.max(0, diff / (1000 * 60 * 60 * 24));
    }
    
    /**
     * Gibt verbleibende Mietstunden zurück
     */
    public long getRentHoursLeft() {
        if (!isRented()) return 0;
        long diff = rentEndTime - System.currentTimeMillis();
        return Math.max(0, diff / (1000 * 60 * 60));
    }
    
    /**
     * Beendet die Miete
     */
    public void endRent() {
        this.renterUUID = "";
        this.rentEndTime = 0;
    }

    // ═══════════════════════════════════════════════════════════
    // RATING-SYSTEM
    // ═══════════════════════════════════════════════════════════
    
    public Map<String, Integer> getRatings() {
        if (ratings == null) ratings = new HashMap<>();
        return ratings;
    }
    
    /**
     * Fügt ein Rating hinzu (1-5 Sterne)
     */
    public void addRating(UUID playerUUID, int stars) {
        if (stars < 1) stars = 1;
        if (stars > 5) stars = 5;
        getRatings().put(playerUUID.toString(), stars);
    }
    
    /**
     * Entfernt ein Rating
     */
    public void removeRating(UUID playerUUID) {
        getRatings().remove(playerUUID.toString());
    }
    
    /**
     * Gibt Durchschnitts-Rating zurück (0.0 - 5.0)
     */
    public double getAverageRating() {
        if (getRatings().isEmpty()) return 0.0;
        int sum = getRatings().values().stream().mapToInt(Integer::intValue).sum();
        return (double) sum / getRatings().size();
    }
    
    /**
     * Gibt Anzahl der Ratings zurück
     */
    public int getRatingCount() {
        return getRatings().size();
    }
    
    /**
     * Prüft ob Spieler bereits bewertet hat
     */
    public boolean hasRated(UUID playerUUID) {
        return getRatings().containsKey(playerUUID.toString());
    }
    
    /**
     * Gibt Rating des Spielers zurück (oder 0 wenn nicht bewertet)
     */
    public int getPlayerRating(UUID playerUUID) {
        return getRatings().getOrDefault(playerUUID.toString(), 0);
    }
    
    /**
     * Gibt Sterne als String zurück (★★★★★)
     */
    public String getRatingStars() {
        double avg = getAverageRating();
        int fullStars = (int) Math.round(avg);
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            stars.append(i < fullStars ? "★" : "☆");
        }
        return stars.toString();
    }

    // ═══════════════════════════════════════════════════════════
    // STATISTIKEN
    // ═══════════════════════════════════════════════════════════
    
    public long getCreationTime() { return creationTime; }
    public void setCreationTime(long time) { this.creationTime = time; }
    
    public long getLastVisited() { return lastVisited; }
    public void setLastVisited(long time) { this.lastVisited = time; }
    
    public int getVisitCount() { return visitCount; }
    public void incrementVisitCount() {
        this.visitCount++;
        this.lastVisited = System.currentTimeMillis();
    }
    
    /**
     * Gibt Alter des Plots in Tagen zurück
     */
    public long getAgeDays() {
        long diff = System.currentTimeMillis() - creationTime;
        return diff / (1000 * 60 * 60 * 24);
    }

    // ═══════════════════════════════════════════════════════════
    // BERECHTIGUNGEN
    // ═══════════════════════════════════════════════════════════
    
    public boolean hasOwner() {
        return ownerUUID != null && !ownerUUID.isEmpty();
    }
    
    public boolean isOwnedBy(UUID uuid) {
        return uuid != null && uuid.toString().equals(ownerUUID);
    }
    
    /**
     * Prüft ob Spieler Zugriff auf diesen Plot hat
     * (Besitzer, Trusted Player ODER Mieter)
     */
    public boolean hasAccess(UUID uuid) {
        if (uuid == null) return false;
        if (isOwnedBy(uuid)) return true;
        if (isTrusted(uuid)) return true;
        if (isRented() && uuid.toString().equals(renterUUID)) return true;
        return false;
    }
    
    /**
     * Prüft ob Spieler Plot verwalten kann (nur Besitzer)
     */
    public boolean canManage(UUID uuid) {
        return isOwnedBy(uuid);
    }
    
    /**
     * Gibt effektiven Besitzer zurück (Mieter wenn vermietet, sonst Eigentümer)
     */
    public String getEffectiveOwnerUUID() {
        if (isRented()) return renterUUID;
        return ownerUUID;
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITY
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Prüft ob Position innerhalb des Plots liegt
     */
    public boolean contains(BlockPos pos) {
        return pos.getX() >= min.getX() && pos.getX() <= max.getX() &&
               pos.getY() >= min.getY() && pos.getY() <= max.getY() &&
               pos.getZ() >= min.getZ() && pos.getZ() <= max.getZ();
    }
    
    /**
     * Berechnet Volumen des Plots
     */
    public long getVolume() {
        long dx = max.getX() - min.getX() + 1;
        long dy = max.getY() - min.getY() + 1;
        long dz = max.getZ() - min.getZ() + 1;
        return dx * dy * dz;
    }
    
    /**
     * Gibt Zentrum des Plots zurück
     */
    public BlockPos getCenter() {
        return new BlockPos(
            (min.getX() + max.getX()) / 2,
            (min.getY() + max.getY()) / 2,
            (min.getZ() + max.getZ()) / 2
        );
    }
    
    /**
     * Gibt Spawn-Position des Plots zurück (Zentrum, aber auf Boden)
     */
    public BlockPos getSpawnPosition() {
        return new BlockPos(
            (min.getX() + max.getX()) / 2,
            min.getY() + 1,
            (min.getZ() + max.getZ()) / 2
        );
    }

    @Override
    public String toString() {
        return String.format("Plot[ID=%s, Name='%s', Owner=%s, Price=%.2f€, Rating=%.1f⭐ (%d), Trusted=%d]",
            plotId, plotName, ownerUUID, price, getAverageRating(), getRatingCount(), getTrustedCount());
    }
}
