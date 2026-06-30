package de.rolandsw.schedulemc.npc.life.witness;

import de.rolandsw.schedulemc.npc.life.social.RumorType;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * CrimeType - Einheitliches Enum fuer alle Verbrechenstypen
 *
 * Jedes Verbrechen hat:
 * - Schwere (1-10) fuer NPC-Reaktionen
 * - Wanted-Sterne (1-5) fuer Fahndungssystem
 * - Geldstrafe und Gefaengnistage
 * - Kopfgeld und Geruecht-Typ
 */
public enum CrimeType {

    // ═══════════════════════════════════════════════════════════
    // THEFT (Diebstahl)
    // ═══════════════════════════════════════════════════════════

    PETTY_THEFT("Petty theft", 2, 50, RumorType.THEFT, 1, 500.0, 1),
    SHOPLIFTING("Shoplifting", 3, 100, RumorType.THEFT, 1, 500.0, 1),
    BURGLARY("Burglary", 6, 500, RumorType.BURGLARY, 2, 2000.0, 3),
    ROBBERY("Robbery", 7, 750, RumorType.ASSAULT, 2, 2500.0, 3),

    // ═══════════════════════════════════════════════════════════
    // VIOLENCE (Gewalt)
    // ═══════════════════════════════════════════════════════════

    THREAT("Threat", 3, 100, RumorType.ASSAULT, 1, 300.0, 1),
    ASSAULT("Assault", 5, 300, RumorType.ASSAULT, 2, 2000.0, 3),
    AGGRAVATED_ASSAULT("Aggravated assault", 8, 1000, RumorType.ASSAULT, 3, 5000.0, 5),
    ARMED_VIOLENCE("Armed violence", 9, 2000, RumorType.ASSAULT, 4, 20000.0, 10),
    ILLEGAL_FIREARM("Illegal firearm", 6, 750, RumorType.WANTED_BY_POLICE, 2, 3000.0, 3),

    // ═══════════════════════════════════════════════════════════
    // DRUGS (Drogen)
    // ═══════════════════════════════════════════════════════════

    DRUG_USE("Drug use", 2, 50, RumorType.DRUG_DEALING, 1, 300.0, 1),
    DRUG_DEALING_SMALL("Drug dealing (small)", 4, 200, RumorType.DRUG_DEALING, 2, 3000.0, 4),
    DRUG_DEALING_LARGE("Drug dealing (large)", 7, 1000, RumorType.DRUG_DEALING, 2, 3000.0, 4),

    // ═══════════════════════════════════════════════════════════
    // OTHER (Sonstige)
    // ═══════════════════════════════════════════════════════════

    VANDALISM("Vandalism", 2, 75, RumorType.VANDALISM, 1, 400.0, 1),
    TRESPASSING("Trespassing", 3, 100, RumorType.UNRELIABLE, 1, 300.0, 1),
    BRIBERY("Bribery", 4, 250, RumorType.UNFAIR_TRADER, 1, 1000.0, 2),
    FRAUD("Fraud", 5, 400, RumorType.UNFAIR_TRADER, 1, 1000.0, 2),
    EVADING_POLICE("Evading police", 4, 200, RumorType.WANTED_BY_POLICE, 1, 500.0, 1),

    // ═══════════════════════════════════════════════════════════
    // SEVERE (Schwere Verbrechen - aus altem CrimeType)
    // ═══════════════════════════════════════════════════════════

    MURDER("Murder", 10, 5000, RumorType.ASSAULT, 3, 10000.0, 7),
    GANG_VIOLENCE("Gang violence", 8, 2500, RumorType.ASSAULT, 3, 5000.0, 5),
    ARSON("Arson", 8, 3000, RumorType.VANDALISM, 3, 7000.0, 6),
    BLACK_MARKET("Black market", 4, 500, RumorType.UNFAIR_TRADER, 1, 1000.0, 2),

    // ═══════════════════════════════════════════════════════════
    // EXTREME (Hoechste Stufe - aus altem CrimeType)
    // ═══════════════════════════════════════════════════════════

    PRISON_ESCAPE("Prison escape", 9, 5000, RumorType.WANTED_BY_POLICE, 4, 20000.0, 10),
    POLICE_ASSAULT("Police assault", 10, 10000, RumorType.WANTED_BY_POLICE, 5, 50000.0, 14),
    TERRORISM("Terrorism", 10, 20000, RumorType.WANTED_BY_POLICE, 5, 100000.0, 20),

    // ═══════════════════════════════════════════════════════════
    // TRAFFIC (Verkehrsdelikte - NEU)
    // ═══════════════════════════════════════════════════════════

    TRAFFIC_VIOLATION("Traffic violation", 1, 25, RumorType.UNRELIABLE, 1, 200.0, 0),
    RECKLESS_DRIVING("Reckless driving", 3, 100, RumorType.UNRELIABLE, 1, 500.0, 1),
    HIT_AND_RUN("Fahrerflucht", 6, 500, RumorType.ASSAULT, 2, 3000.0, 3);

    private final String displayName;
    private final int severity;        // 1-10 (NPC-Reaktionen)
    private final int baseBounty;      // Standard-Kopfgeld
    private final RumorType associatedRumor;
    private final int wantedStars;     // 1-5 Wanted-Sterne
    private final double fine;         // Geldstrafe in Euro
    private final int prisonDays;      // Gefaengnistage

    CrimeType(String displayName, int severity, int baseBounty, RumorType associatedRumor,
              int wantedStars, double fine, int prisonDays) {
        this.displayName = displayName;
        this.severity = severity;
        this.baseBounty = baseBounty;
        this.associatedRumor = associatedRumor;
        this.wantedStars = wantedStars;
        this.fine = fine;
        this.prisonDays = prisonDays;
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════

    public String getDisplayName() {
        return displayName;
    }

    public int getSeverity() {
        return severity;
    }

    public int getBaseBounty() {
        return baseBounty;
    }

    public RumorType getAssociatedRumor() {
        return associatedRumor;
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

    // ═══════════════════════════════════════════════════════════
    // CALCULATED VALUES
    // ═══════════════════════════════════════════════════════════

    public int calculateBounty(boolean repeatOffender, boolean attemptedFlight) {
        int bounty = baseBounty;
        if (repeatOffender) {
            bounty = (int) (bounty * 1.5);
        }
        if (attemptedFlight) {
            bounty = (int) (bounty * 1.2);
        }
        return bounty;
    }

    public float getBaseReportChance() {
        return Math.min(0.95f, 0.2f + (severity * 0.08f));
    }

    public int getWantedDuration() {
        return severity * 2;
    }

    public float getSafetyImpact() {
        return severity * 5.0f;
    }

    public boolean isViolent() {
        return this == THREAT || this == ASSAULT ||
               this == AGGRAVATED_ASSAULT || this == ARMED_VIOLENCE ||
               this == ILLEGAL_FIREARM ||
               this == ROBBERY || this == MURDER || this == GANG_VIOLENCE;
    }

    public boolean isPropertyCrime() {
        return this == PETTY_THEFT || this == SHOPLIFTING ||
               this == BURGLARY || this == ROBBERY ||
               this == VANDALISM || this == FRAUD || this == ARSON;
    }

    public boolean isDrugRelated() {
        return this == DRUG_USE || this == DRUG_DEALING_SMALL ||
               this == DRUG_DEALING_LARGE;
    }

    public boolean isTrafficRelated() {
        return this == TRAFFIC_VIOLATION || this == RECKLESS_DRIVING ||
               this == HIT_AND_RUN;
    }

    // ═══════════════════════════════════════════════════════════
    // FORMATTING (aus altem CrimeType uebernommen)
    // ═══════════════════════════════════════════════════════════

    public String getFormattedInfo() {
        String starsStr = "\u2B50".repeat(wantedStars);
        return String.format("\u00A7c%s %s \u00A77- Strafe: \u00A7c%.2f\u20AC \u00A77/ \u00A7e%d Tage",
            starsStr, getDisplayName(), fine, prisonDays);
    }

    public String getColorCode() {
        return switch (wantedStars) {
            case 1 -> "\u00A7e"; // Gelb
            case 2 -> "\u00A76"; // Orange
            case 3 -> "\u00A7c"; // Rot
            case 4 -> "\u00A74"; // Dunkelrot
            case 5 -> "\u00A75"; // Lila
            default -> "\u00A77"; // Grau
        };
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITY
    // ═══════════════════════════════════════════════════════════

    public String getTranslationKey() {
        return "crime." + name().toLowerCase(Locale.ROOT);
    }

    public static CrimeType fromName(String name) {
        try {
            return valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return PETTY_THEFT;
        }
    }

    public static CrimeType fromOrdinal(int ordinal) {
        CrimeType[] values = values();
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return PETTY_THEFT;
    }
}
