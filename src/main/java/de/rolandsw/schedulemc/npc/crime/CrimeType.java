package de.rolandsw.schedulemc.npc.crime;

import net.minecraft.network.chat.Component;

/**
 * Verschiedene Verbrechen-Typen mit Strafen
 */
public enum CrimeType {
    // ========== MINOR CRIMES (1 Star) ==========
    THEFT(1, 500.0, 1),
    TRESPASSING(1, 300.0, 1),
    BLACK_MARKET(1, 1000.0, 2),
    VANDALISM(1, 400.0, 1),

    // ========== MODERATE CRIMES (2 Stars) ==========
    ASSAULT(2, 2000.0, 3),
    DRUG_TRAFFICKING(2, 3000.0, 4),
    ROBBERY(2, 2500.0, 3),
    BURGLARY(2, 2000.0, 3),

    // ========== SERIOUS CRIMES (3 Stars) ==========
    MURDER(3, 10000.0, 7),
    GANG_VIOLENCE(3, 5000.0, 5),
    ARSON(3, 7000.0, 6),

    // ========== EXTREME CRIMES (4-5 Stars) ==========
    PRISON_ESCAPE(4, 20000.0, 10),
    POLICE_ASSAULT(5, 50000.0, 14),
    TERRORISM(5, 100000.0, 20);

    private final int wantedStars;      // Wanted-Sterne
    private final double fine;           // Geldstrafe
    private final int prisonDays;        // Gefängnis-Tage

    CrimeType(int wantedStars, double fine, int prisonDays) {
        this.wantedStars = wantedStars;
        this.fine = fine;
        this.prisonDays = prisonDays;
    }

    public int getWantedStars() {
        return wantedStars;
    }

    public double getFine() {
        return fine;
    }

    public int getPrisonDays() {
        return prisonDays;
    }

    public String getDisplayName() {
        return Component.translatable("enum.crime_type." + this.name().toLowerCase()).getString();
    }

    /**
     * Gibt Schweregrad zurück (für Strafen-Multiplikation bei Wiederholungstätern)
     */
    public int getSeverity() {
        return wantedStars;
    }

    /**
     * Gibt formatierte Beschreibung zurück
     */
    public String getFormattedInfo() {
        String starsStr = "⭐".repeat(wantedStars);
        return String.format("§c%s %s §7- Strafe: §c%.2f€ §7/ §e%d Tage",
            starsStr, getDisplayName(), fine, prisonDays);
    }

    /**
     * Gibt Farbe basierend auf Schwere zurück
     */
    public String getColorCode() {
        return switch (wantedStars) {
            case 1 -> "§e"; // Gelb
            case 2 -> "§6"; // Orange
            case 3 -> "§c"; // Rot
            case 4 -> "§4"; // Dunkelrot
            case 5 -> "§5"; // Lila
            default -> "§7"; // Grau
        };
    }
}
