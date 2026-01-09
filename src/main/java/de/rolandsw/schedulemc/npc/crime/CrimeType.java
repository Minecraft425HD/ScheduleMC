package de.rolandsw.schedulemc.npc.crime;

import net.minecraft.network.chat.Component;

/**
 * Verschiedene Verbrechen-Typen mit Strafen
 */
public enum CrimeType {
    // ========== MINOR CRIMES (1 Star) ==========
    THEFT(1, 500.0, 1, Component.translatable("enum.crime_type.theft").getString()),
    TRESPASSING(1, 300.0, 1, Component.translatable("enum.crime_type.trespassing").getString()),
    BLACK_MARKET(1, 1000.0, 2, Component.translatable("enum.crime_type.black_market").getString()),
    VANDALISM(1, 400.0, 1, Component.translatable("enum.crime_type.vandalism").getString()),

    // ========== MODERATE CRIMES (2 Stars) ==========
    ASSAULT(2, 2000.0, 3, Component.translatable("enum.crime_type.assault").getString()),
    DRUG_TRAFFICKING(2, 3000.0, 4, Component.translatable("enum.crime_type.drug_trafficking").getString()),
    ROBBERY(2, 2500.0, 3, Component.translatable("enum.crime_type.robbery").getString()),
    BURGLARY(2, 2000.0, 3, Component.translatable("enum.crime_type.burglary").getString()),

    // ========== SERIOUS CRIMES (3 Stars) ==========
    MURDER(3, 10000.0, 7, Component.translatable("enum.crime_type.murder").getString()),
    GANG_VIOLENCE(3, 5000.0, 5, Component.translatable("enum.crime_type.gang_violence").getString()),
    ARSON(3, 7000.0, 6, Component.translatable("enum.crime_type.arson").getString()),

    // ========== EXTREME CRIMES (4-5 Stars) ==========
    PRISON_ESCAPE(4, 20000.0, 10, Component.translatable("enum.crime_type.prison_escape").getString()),
    POLICE_ASSAULT(5, 50000.0, 14, Component.translatable("enum.crime_type.police_assault").getString()),
    TERRORISM(5, 100000.0, 20, Component.translatable("enum.crime_type.terrorism").getString());

    private final int wantedStars;      // Wanted-Sterne
    private final double fine;           // Geldstrafe
    private final int prisonDays;        // Gefängnis-Tage
    private final String displayName;    // Anzeigename

    CrimeType(int wantedStars, double fine, int prisonDays, String displayName) {
        this.wantedStars = wantedStars;
        this.fine = fine;
        this.prisonDays = prisonDays;
        this.displayName = displayName;
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
        return displayName;
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
            starsStr, displayName, fine, prisonDays);
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
