package de.rolandsw.schedulemc.npc.life.social;

import net.minecraft.ChatFormatting;

import java.util.Locale;

/**
 * Faction - Die 4 Haupt-Fraktionen in der Stadt
 *
 * Jede Fraktion hat eigene Charakteristiken und
 * Beziehungen zu anderen Fraktionen.
 */
public enum Faction {

    // ═══════════════════════════════════════════════════════════
    // FACTIONS
    // ═══════════════════════════════════════════════════════════

    /**
     * Normale Bürger - Die Mehrheit der NPCs
     * Neutral zu allen, arbeitet ehrlich
     */
    CITIZENS("Citizens", "Law-abiding citizens of the city",
        ChatFormatting.WHITE, 0, true),

    /**
     * Händler-Gilde - Reiche Kaufleute und Geschäftsleute
     * Fokus auf Profit, weitreichende Verbindungen
     */
    TRADERS("Traders' Guild", "The city's trade association",
        ChatFormatting.GOLD, 10, true),

    /**
     * Ordnungshüter - Polizei und Sicherheitskräfte
     * Setzen Gesetze durch, jagen Kriminelle
     */
    LAW("Law Enforcement", "Police and guards of the city",
        ChatFormatting.BLUE, 5, true),

    /**
     * Untergrund - Kriminelle, Schmuggler, Dealer
     * Illegale Geschäfte, Informationshandel
     */
    UNDERWORLD("Underworld", "The criminal network",
        ChatFormatting.DARK_GRAY, -5, false);

    private final String displayName;
    private final String description;
    private final ChatFormatting color;
    private final int baseReputation;
    private final boolean isLawful;

    Faction(String displayName, String description, ChatFormatting color,
            int baseReputation, boolean isLawful) {
        this.displayName = displayName;
        this.description = description;
        this.color = color;
        this.baseReputation = baseReputation;
        this.isLawful = isLawful;
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════

    /**
     * Anzeigename für UI
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Beschreibung der Fraktion
     */
    public String getDescription() {
        return description;
    }

    /**
     * Farbe für UI-Darstellung
     */
    public ChatFormatting getColor() {
        return color;
    }

    /**
     * Basis-Reputation bei dieser Fraktion
     * (Startwert für neue Spieler)
     */
    public int getBaseReputation() {
        return baseReputation;
    }

    /**
     * Ist diese Fraktion gesetzestreu?
     */
    public boolean isLawful() {
        return isLawful;
    }

    // ═══════════════════════════════════════════════════════════
    // FACTION RELATIONSHIPS
    // ═══════════════════════════════════════════════════════════

    /**
     * Basis-Beziehung zu einer anderen Fraktion
     *
     * @return Wert von -100 (Feinde) bis +100 (Verbündete)
     */
    public int getBaseRelationWith(Faction other) {
        if (this == other) return 100; // Eigene Fraktion

        return switch (this) {
            case CITIZENS -> switch (other) {
                case TRADERS -> 30;   // Gute Kunden
                case LAW -> 50;    // Vertrauen in Polizei
                case UNDERWORLD -> -30; // Misstrauen
                default -> 0;
            };
            case TRADERS -> switch (other) {
                case CITIZENS -> 30;    // Kunden
                case LAW -> 20;    // Geschäftsbeziehung
                case UNDERWORLD -> -10; // Manchmal nützlich, aber riskant
                default -> 0;
            };
            case LAW -> switch (other) {
                case CITIZENS -> 50;    // Zu beschützen
                case TRADERS -> 20;   // Steuerzahler
                case UNDERWORLD -> -80; // Hauptfeinde
                default -> 0;
            };
            case UNDERWORLD -> switch (other) {
                case CITIZENS -> -20;   // Potentielle Opfer/Kunden
                case TRADERS -> 10;   // Manchmal Geschäftspartner
                case LAW -> -80;   // Feinde
                default -> 0;
            };
        };
    }

    /**
     * Prüft ob diese Fraktion feindlich zu einer anderen ist
     */
    public boolean isHostileTo(Faction other) {
        return getBaseRelationWith(other) < -50;
    }

    /**
     * Prüft ob diese Fraktion freundlich zu einer anderen ist
     */
    public boolean isFriendlyTo(Faction other) {
        return getBaseRelationWith(other) > 30;
    }

    /**
     * Prüft ob diese Fraktion Verbrechen melden würde
     */
    public boolean wouldReportCrime() {
        return isLawful;
    }

    /**
     * Prüft ob diese Fraktion mit dem Untergrund handeln würde
     */
    public boolean wouldTradeWithUnderground() {
        return this == UNDERWORLD || this == TRADERS;
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITY
    // ═══════════════════════════════════════════════════════════

    /**
     * Übersetzungsschlüssel für Lokalisierung
     */
    public String getTranslationKey() {
        return "faction." + name().toLowerCase(Locale.ROOT);
    }

    /**
     * Gibt Faction aus Ordinal zurück (mit Fallback)
     */
    public static Faction fromOrdinal(int ordinal) {
        Faction[] values = values();
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return CITIZENS;
    }

    /**
     * Gibt Faction aus Name zurück (mit Fallback)
     */
    public static Faction fromName(String name) {
        try {
            return valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return CITIZENS;
        }
    }

    /**
     * Holt die passende Fraktion für einen NPC-Typ
     */
    public static Faction forNPCType(de.rolandsw.schedulemc.npc.data.NPCType npcType) {
        return switch (npcType) {
            case POLICE -> LAW;
            case MERCHANT -> TRADERS;
            case BANKER, BANK -> TRADERS;
            case DRUG_DEALER -> UNDERWORLD;
            default -> CITIZENS;
        };
    }
}
