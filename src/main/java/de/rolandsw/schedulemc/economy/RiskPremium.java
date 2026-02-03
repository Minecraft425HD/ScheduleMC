package de.rolandsw.schedulemc.economy;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

/**
 * Berechnet Risiko-Aufschläge für illegale Produkte.
 *
 * Faktoren die den Aufschlag beeinflussen:
 * 1. Basis-Risiko der Kategorie (Cannabis < Heroin)
 * 2. Aktuelles Wanted-Level des Servers (mehr Polizei = höhere Aufschläge)
 * 3. Kürzliche Razzien (nach Razzia steigen Preise temporär)
 * 4. Maschinen-Konfiszierungs-Risiko (illegale Maschinen können beschlagnahmt werden)
 *
 * Dies simuliert realistisch, dass illegale Waren teurer sind
 * WEIL sie Risiko mitbringen, nicht trotz des Risikos.
 */
public class RiskPremium {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Basis-Risiko-Multiplikatoren pro Kategorie.
     * Dieser Aufschlag wird IMMER auf illegale Waren angewendet.
     *
     * 1.0 = kein Aufschlag, 1.3 = 30% Aufschlag
     */
    private static final double RISK_CANNABIS = 1.15;       // Niedrigstes Risiko
    private static final double RISK_TOBACCO = 1.10;        // Sehr niedriges Risiko
    private static final double RISK_MUSHROOM = 1.20;       // Niedriges Risiko
    private static final double RISK_LSD = 1.30;            // Mittleres Risiko
    private static final double RISK_MDMA = 1.30;           // Mittleres Risiko
    private static final double RISK_COCAINE = 1.40;        // Hohes Risiko
    private static final double RISK_METH = 1.45;           // Hohes Risiko
    private static final double RISK_HEROIN = 1.50;         // Höchstes Risiko

    /**
     * Wanted-Level Multiplikatoren (0-5).
     * Höheres Wanted-Level = Polizei aktiver = höhere Risiko-Aufschläge.
     */
    private static final double[] WANTED_LEVEL_MULTIPLIERS = {
            1.0,   // Level 0: Kein Fahndungsdruck
            1.05,  // Level 1: Leichter Verdacht
            1.12,  // Level 2: Aktive Beobachtung
            1.20,  // Level 3: Intensive Fahndung
            1.35,  // Level 4: Großfahndung
            1.50   // Level 5: Maximaler Fahndungsdruck
    };

    /**
     * Konfiszierungs-Risiko-Aufschlag für illegale Maschinen.
     * Dieser Aufschlag wird auf den Maschinen-Preis addiert,
     * da der Spieler die Maschine bei einer Razzia verlieren kann.
     */
    private static final double CONFISCATION_RISK_MULTIPLIER = 1.25;

    // Aktuelle Razzia-Daten (werden vom PolizeiSystem gesetzt)
    private static volatile long lastRaidTimestamp = 0;
    private static volatile int recentRaidCount = 0;

    private RiskPremium() {
        // Utility class
    }

    /**
     * Berechnet den Risiko-Aufschlag für ein illegales Produkt.
     *
     * @param category    die Item-Kategorie
     * @param wantedLevel aktuelles Wanted-Level (0-5)
     * @return Multiplikator (1.0 = kein Aufschlag, 1.5 = 50% Aufschlag)
     */
    public static double calculateRiskMultiplier(ItemCategory category, int wantedLevel) {
        if (!category.isIllegal()) {
            return 1.0; // Legale Produkte haben keinen Risiko-Aufschlag
        }

        // 1. Basis-Risiko der Kategorie
        double baseRisk = getBaseRisk(category);

        // 2. Wanted-Level Multiplikator
        int clampedLevel = Math.max(0, Math.min(5, wantedLevel));
        double wantedMultiplier = WANTED_LEVEL_MULTIPLIERS[clampedLevel];

        // 3. Razzia-Bonus (wenn kürzlich Razzien stattfanden)
        double raidBonus = calculateRaidBonus();

        double totalRisk = baseRisk * wantedMultiplier * raidBonus;

        LOGGER.debug("Risk premium for {}: base={:.2f} × wanted={:.2f} × raid={:.2f} = {:.2f}",
                category.name(), baseRisk, wantedMultiplier, raidBonus, totalRisk);

        return totalRisk;
    }

    /**
     * Berechnet den Konfiszierungs-Risiko-Aufschlag für Maschinen.
     * Nur für illegale Maschinen relevant.
     *
     * @param category die Item-Kategorie
     * @return Multiplikator für Maschinenpreis
     */
    public static double getConfiscationRiskMultiplier(ItemCategory category) {
        if (category == ItemCategory.MACHINE_ILLEGAL) {
            return CONFISCATION_RISK_MULTIPLIER;
        }
        return 1.0;
    }

    /**
     * Wird vom Polizei-System aufgerufen wenn eine Razzia stattfindet.
     */
    public static void onRaidOccurred() {
        onRaidOccurred("GENERAL");
    }

    /**
     * Wird vom Polizei-System aufgerufen wenn eine Razzia stattfindet.
     * Erlaubt Angabe eines Grundes für detailliertes Logging und Tracking.
     *
     * Bekannte Gründe:
     * - "MACHINE_CONFISCATION" : Illegale Maschinen wurden beschlagnahmt
     * - "ILLEGAL_PLANTS"      : Illegale Pflanzen wurden gefunden
     * - "CONTRABAND_FOUND"    : Allgemeine Konterband-Funde
     * - "GENERAL"             : Allgemeine Razzia ohne spezifischen Grund
     *
     * @param reason der Grund der Razzia (für Logging und zukünftiges Event-Tracking)
     */
    public static void onRaidOccurred(String reason) {
        lastRaidTimestamp = System.currentTimeMillis();
        recentRaidCount++;
        LOGGER.info("Raid registered [reason={}] - recent raid count: {}", reason, recentRaidCount);
    }

    /**
     * Wird periodisch aufgerufen um Razzia-Daten zu aktualisieren.
     * Reduziert recentRaidCount über Zeit.
     */
    public static void decayRaidData() {
        long timeSinceLastRaid = System.currentTimeMillis() - lastRaidTimestamp;
        // Nach 30 Minuten Echtzeit beginnt der Decay
        long decayThreshold = 30 * 60 * 1000L;

        if (timeSinceLastRaid > decayThreshold && recentRaidCount > 0) {
            recentRaidCount = Math.max(0, recentRaidCount - 1);
        }
    }

    /**
     * Setzt die Razzia-Daten zurück (z.B. bei Server-Neustart).
     */
    public static void resetRaidData() {
        lastRaidTimestamp = 0;
        recentRaidCount = 0;
    }

    // ═══════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════

    private static double getBaseRisk(ItemCategory category) {
        return switch (category) {
            case CANNABIS -> RISK_CANNABIS;
            case TOBACCO_PRODUCT -> RISK_TOBACCO;
            case MUSHROOM -> RISK_MUSHROOM;
            case LSD -> RISK_LSD;
            case MDMA -> RISK_MDMA;
            case COCAINE -> RISK_COCAINE;
            case METH -> RISK_METH;
            case HEROIN -> RISK_HEROIN;
            case SEED_ILLEGAL, CHEMICAL -> 1.10;
            case MACHINE_ILLEGAL -> 1.05;
            default -> 1.0;
        };
    }

    /**
     * Berechnet Razzia-Bonus basierend auf kürzlichen Razzien.
     * Mehr Razzien = höhere Preise (Angebot wird knapper).
     */
    private static double calculateRaidBonus() {
        if (recentRaidCount == 0) return 1.0;

        long timeSinceLastRaid = System.currentTimeMillis() - lastRaidTimestamp;
        // Effekt klingt über 60 Minuten ab
        long effectDuration = 60 * 60 * 1000L;

        if (timeSinceLastRaid > effectDuration) return 1.0;

        // Stärke basierend auf Anzahl und Aktualität
        double recency = 1.0 - ((double) timeSinceLastRaid / effectDuration);
        double raidImpact = Math.min(recentRaidCount * 0.05, 0.30); // Max 30% Aufschlag

        return 1.0 + (raidImpact * recency);
    }

    /**
     * Gibt eine formatierte Beschreibung des Risiko-Aufschlags zurück.
     *
     * @param category    die Item-Kategorie
     * @param wantedLevel aktuelles Wanted-Level
     * @return Formatierter String für UI
     */
    public static String getFormattedRiskInfo(ItemCategory category, int wantedLevel) {
        if (!category.isIllegal()) {
            return "§aKein Risiko";
        }

        double multiplier = calculateRiskMultiplier(category, wantedLevel);
        int percent = (int) ((multiplier - 1.0) * 100);

        if (percent <= 10) {
            return "§aNiedriges Risiko §7(+" + percent + "%)";
        } else if (percent <= 25) {
            return "§eMittleres Risiko §7(+" + percent + "%)";
        } else if (percent <= 40) {
            return "§6Hohes Risiko §7(+" + percent + "%)";
        } else {
            return "§cSehr hohes Risiko §7(+" + percent + "%)";
        }
    }
}
