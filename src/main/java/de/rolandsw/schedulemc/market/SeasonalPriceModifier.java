package de.rolandsw.schedulemc.market;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Saisonales Preismodifikator-System.
 *
 * Jahreszeiten-Zyklus basierend auf Spieltagen (120 Tage = 1 Jahr):
 * - FRUEHLING (Tag 0-29):  Landwirtschaft -20%, Baumaterial +10%
 * - SOMMER (Tag 30-59):    Nahrung -15%, Getraenke +20%, Chemie +10%
 * - HERBST (Tag 60-89):    Ernte: Nahrung -30%, Luxus +15%
 * - WINTER (Tag 90-119):   Heizmaterial +30%, Landwirtschaft +25%, Kleidung +20%
 *
 * Kategorien werden automatisch auf Produktions- und Marktpreise angewendet.
 */
public class SeasonalPriceModifier {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static volatile SeasonalPriceModifier instance;

    private static final int DAYS_PER_SEASON = 30;
    private static final int DAYS_PER_YEAR = DAYS_PER_SEASON * 4; // 120 Tage

    public enum Season {
        FRUEHLING("Fruehling", "\u00A7a\u2618"),
        SOMMER("Sommer", "\u00A7e\u2600"),
        HERBST("Herbst", "\u00A76\u2663"),
        WINTER("Winter", "\u00A7b\u2744");

        private final String displayName;
        private final String icon;

        Season(String displayName, String icon) {
            this.displayName = displayName;
            this.icon = icon;
        }

        public String getDisplayName() { return displayName; }
        public String getIcon() { return icon; }
    }

    // Kategorie -> Saison -> Multiplikator
    private final Map<String, Map<Season, Float>> seasonalModifiers = new ConcurrentHashMap<>();

    // Aktuelle Saison (cached)
    private Season currentSeason = Season.FRUEHLING;
    private long currentDay = 0;

    // ═══════════════════════════════════════════════════════════
    // SINGLETON
    // ═══════════════════════════════════════════════════════════

    private SeasonalPriceModifier() {
        registerDefaultModifiers();
    }

    public static SeasonalPriceModifier getInstance() {
        if (instance == null) {
            synchronized (SeasonalPriceModifier.class) {
                if (instance == null) {
                    instance = new SeasonalPriceModifier();
                }
            }
        }
        return instance;
    }

    // ═══════════════════════════════════════════════════════════
    // DEFAULT MODIFIERS
    // ═══════════════════════════════════════════════════════════

    private void registerDefaultModifiers() {
        // Landwirtschaft: billig im Fruehling/Herbst, teuer im Winter
        registerCategory("PLANT", Map.of(
            Season.FRUEHLING, 0.80f,  // -20% Saatzeit
            Season.SOMMER, 1.0f,
            Season.HERBST, 0.70f,     // -30% Erntezeit
            Season.WINTER, 1.25f      // +25% Knappheit
        ));

        // Pilze: lieben Herbst (feucht), schlecht im Sommer (trocken)
        registerCategory("MUSHROOM", Map.of(
            Season.FRUEHLING, 1.0f,
            Season.SOMMER, 1.15f,     // +15%
            Season.HERBST, 0.75f,     // -25% Pilzsaison
            Season.WINTER, 1.10f      // +10%
        ));

        // Chemie: Sommer ist gut (laenger hell), Winter schlecht
        registerCategory("CHEMICAL", Map.of(
            Season.FRUEHLING, 1.0f,
            Season.SOMMER, 0.90f,     // -10%
            Season.HERBST, 1.0f,
            Season.WINTER, 1.15f      // +15% schwieriger zu produzieren
        ));

        // Nahrung
        registerCategory("FOOD", Map.of(
            Season.FRUEHLING, 0.90f,  // -10%
            Season.SOMMER, 0.85f,     // -15%
            Season.HERBST, 0.70f,     // -30% Ernte
            Season.WINTER, 1.30f      // +30% Knappheit
        ));

        // Waffen: teurer im Winter (Konflikte), billiger im Sommer
        registerCategory("WEAPONS", Map.of(
            Season.FRUEHLING, 1.0f,
            Season.SOMMER, 0.95f,
            Season.HERBST, 1.05f,
            Season.WINTER, 1.20f      // +20%
        ));

        // Luxusgueter: teuer im Winter (Feste), billig im Sommer
        registerCategory("LUXURY", Map.of(
            Season.FRUEHLING, 1.0f,
            Season.SOMMER, 0.90f,
            Season.HERBST, 1.15f,     // +15% Vorbereitung auf Winter
            Season.WINTER, 1.25f      // +25% Festsaison
        ));

        // Baumaterial: teuer im Fruehling (Bausaison)
        registerCategory("BUILDING", Map.of(
            Season.FRUEHLING, 1.20f,  // +20% Bausaison
            Season.SOMMER, 1.10f,     // +10%
            Season.HERBST, 0.90f,     // -10%
            Season.WINTER, 0.85f      // -15% niemand baut
        ));

        LOGGER.info("SeasonalPriceModifier: {} Kategorien registriert", seasonalModifiers.size());
    }

    // ═══════════════════════════════════════════════════════════
    // CATEGORY MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    public void registerCategory(String category, Map<Season, Float> modifiers) {
        seasonalModifiers.put(category.toUpperCase(), new ConcurrentHashMap<>(modifiers));
    }

    // ═══════════════════════════════════════════════════════════
    // SEASON CALCULATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Aktualisiert die aktuelle Saison basierend auf dem Spieltag.
     */
    public void updateSeason(long gameDay) {
        this.currentDay = gameDay;
        Season newSeason = getSeasonForDay(gameDay);
        if (newSeason != currentSeason) {
            Season old = currentSeason;
            currentSeason = newSeason;
            LOGGER.info("Saisonwechsel: {} -> {} (Tag {})", old.getDisplayName(),
                newSeason.getDisplayName(), gameDay);
        }
    }

    /**
     * Berechnet die Saison fuer einen bestimmten Spieltag.
     */
    public static Season getSeasonForDay(long day) {
        int dayInYear = (int) (Math.abs(day) % DAYS_PER_YEAR);
        int seasonIndex = Math.min(dayInYear / DAYS_PER_SEASON, Season.values().length - 1);
        return Season.values()[seasonIndex];
    }

    /**
     * Gibt den Fortschritt innerhalb der aktuellen Saison zurueck (0.0 - 1.0).
     */
    public float getSeasonProgress() {
        int dayInYear = (int) (currentDay % DAYS_PER_YEAR);
        int dayInSeason = dayInYear % DAYS_PER_SEASON;
        return (float) dayInSeason / DAYS_PER_SEASON;
    }

    // ═══════════════════════════════════════════════════════════
    // PRICE MODIFICATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Gibt den saisonalen Preismodifikator fuer eine Kategorie zurueck.
     * Interpoliert sanft zwischen Saisonen am Uebergang.
     */
    public float getModifier(String category) {
        Map<Season, Float> mods = seasonalModifiers.get(category.toUpperCase());
        if (mods == null) return 1.0f;

        float currentMod = mods.getOrDefault(currentSeason, 1.0f);

        // Sanfte Interpolation am Saisonuebergang (erste/letzte 3 Tage)
        float progress = getSeasonProgress();
        if (progress < 0.1f) {
            // Uebergang von vorheriger Saison
            Season prev = getPreviousSeason(currentSeason);
            float prevMod = mods.getOrDefault(prev, 1.0f);
            float blend = progress / 0.1f; // 0->1 ueber 3 Tage
            return prevMod + (currentMod - prevMod) * blend;
        } else if (progress > 0.9f) {
            // Uebergang zu naechster Saison
            Season next = getNextSeason(currentSeason);
            float nextMod = mods.getOrDefault(next, 1.0f);
            float blend = (progress - 0.9f) / 0.1f; // 0->1 ueber 3 Tage
            return currentMod + (nextMod - currentMod) * blend;
        }

        return currentMod;
    }

    /**
     * Gibt den saisonalen Preismodifikator fuer die aktuelle Saison zurueck (ohne Interpolation).
     */
    public float getRawModifier(String category) {
        Map<Season, Float> mods = seasonalModifiers.get(category.toUpperCase());
        if (mods == null) return 1.0f;
        return mods.getOrDefault(currentSeason, 1.0f);
    }

    // ═══════════════════════════════════════════════════════════
    // QUERIES
    // ═══════════════════════════════════════════════════════════

    public Season getCurrentSeason() {
        return currentSeason;
    }

    public long getCurrentDay() {
        return currentDay;
    }

    public int getDaysUntilNextSeason() {
        int dayInYear = (int) (currentDay % DAYS_PER_YEAR);
        int dayInSeason = dayInYear % DAYS_PER_SEASON;
        return DAYS_PER_SEASON - dayInSeason;
    }

    /**
     * Generiert einen Saisonbericht fuer Spieler.
     */
    public String getSeasonReport() {
        StringBuilder sb = new StringBuilder();
        sb.append(currentSeason.getIcon()).append(" \u00A7l").append(currentSeason.getDisplayName())
          .append("\u00A7r \u00A77(Tag ").append(currentDay).append(")\n");
        sb.append("\u00A77Naechste Saison in: \u00A7f").append(getDaysUntilNextSeason()).append(" Tagen\n\n");
        sb.append("\u00A76Preisaenderungen:\n");

        for (Map.Entry<String, Map<Season, Float>> entry : seasonalModifiers.entrySet()) {
            float mod = entry.getValue().getOrDefault(currentSeason, 1.0f);
            if (Math.abs(mod - 1.0f) > 0.01f) {
                int percent = Math.round((mod - 1.0f) * 100);
                String color = percent > 0 ? "\u00A7c+" : "\u00A7a";
                sb.append("  \u00A7f").append(entry.getKey()).append(": ")
                  .append(color).append(percent).append("%\u00A7r\n");
            }
        }

        return sb.toString();
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITY
    // ═══════════════════════════════════════════════════════════

    private static Season getNextSeason(Season season) {
        Season[] values = Season.values();
        return values[(season.ordinal() + 1) % values.length];
    }

    private static Season getPreviousSeason(Season season) {
        Season[] values = Season.values();
        return values[(season.ordinal() + values.length - 1) % values.length];
    }
}
