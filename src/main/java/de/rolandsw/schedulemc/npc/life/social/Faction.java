package de.rolandsw.schedulemc.npc.life.social;

import net.minecraft.ChatFormatting;

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
    BUERGER("Bürger", "Rechtschaffene Bürger der Stadt",
        ChatFormatting.WHITE, 0, true),

    /**
     * Händler-Gilde - Reiche Kaufleute und Geschäftsleute
     * Fokus auf Profit, weitreichende Verbindungen
     */
    HAENDLER("Händler-Gilde", "Die Handelsvereinigung der Stadt",
        ChatFormatting.GOLD, 10, true),

    /**
     * Ordnungshüter - Polizei und Sicherheitskräfte
     * Setzen Gesetze durch, jagen Kriminelle
     */
    ORDNUNG("Ordnungshüter", "Polizei und Wachen der Stadt",
        ChatFormatting.BLUE, 5, true),

    /**
     * Untergrund - Kriminelle, Schmuggler, Dealer
     * Illegale Geschäfte, Informationshandel
     */
    UNTERGRUND("Untergrund", "Das kriminelle Netzwerk",
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
            case BUERGER -> switch (other) {
                case HAENDLER -> 30;   // Gute Kunden
                case ORDNUNG -> 50;    // Vertrauen in Polizei
                case UNTERGRUND -> -30; // Misstrauen
                default -> 0;
            };
            case HAENDLER -> switch (other) {
                case BUERGER -> 30;    // Kunden
                case ORDNUNG -> 20;    // Geschäftsbeziehung
                case UNTERGRUND -> -10; // Manchmal nützlich, aber riskant
                default -> 0;
            };
            case ORDNUNG -> switch (other) {
                case BUERGER -> 50;    // Zu beschützen
                case HAENDLER -> 20;   // Steuerzahler
                case UNTERGRUND -> -80; // Hauptfeinde
                default -> 0;
            };
            case UNTERGRUND -> switch (other) {
                case BUERGER -> -20;   // Potentielle Opfer/Kunden
                case HAENDLER -> 10;   // Manchmal Geschäftspartner
                case ORDNUNG -> -80;   // Feinde
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
        return this == UNTERGRUND || this == HAENDLER;
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITY
    // ═══════════════════════════════════════════════════════════

    /**
     * Übersetzungsschlüssel für Lokalisierung
     */
    public String getTranslationKey() {
        return "faction." + name().toLowerCase();
    }

    /**
     * Gibt Faction aus Ordinal zurück (mit Fallback)
     */
    public static Faction fromOrdinal(int ordinal) {
        Faction[] values = values();
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return BUERGER;
    }

    /**
     * Gibt Faction aus Name zurück (mit Fallback)
     */
    public static Faction fromName(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return BUERGER;
        }
    }

    /**
     * Holt die passende Fraktion für einen NPC-Typ
     */
    public static Faction forNPCType(de.rolandsw.schedulemc.npc.data.NPCType npcType) {
        return switch (npcType) {
            case POLIZEI, POLICE -> ORDNUNG;
            case VERKAEUFER, MERCHANT -> HAENDLER;
            case BANKER, BANK -> HAENDLER;
            case DRUG_DEALER -> UNTERGRUND;
            default -> BUERGER;
        };
    }
}
