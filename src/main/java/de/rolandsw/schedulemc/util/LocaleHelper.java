package de.rolandsw.schedulemc.util;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Helper für Locale-basierte Textauswahl
 *
 * Ermöglicht dynamische Sprachwahl basierend auf Client-Einstellungen
 */
public class LocaleHelper {

    /**
     * Gibt die aktuelle Client-Locale zurück (nur Client-Side)
     *
     * @return Locale-String (z.B. "de_de", "en_us")
     */
    @OnlyIn(Dist.CLIENT)
    public static String getClientLocale() {
        try {
            return Minecraft.getInstance().getLanguageManager().getSelected();
        } catch (Exception e) {
            return "de_de"; // Fallback
        }
    }

    /**
     * Prüft ob die aktuelle Locale Deutsch ist
     *
     * @param locale Die zu prüfende Locale
     * @return true wenn Deutsch
     */
    public static boolean isGerman(String locale) {
        return locale != null && locale.toLowerCase().startsWith("de");
    }

    /**
     * Prüft ob die aktuelle Locale Englisch ist
     *
     * @param locale Die zu prüfende Locale
     * @return true wenn Englisch
     */
    public static boolean isEnglish(String locale) {
        return locale != null && locale.toLowerCase().startsWith("en");
    }

    /**
     * Wählt automatisch zwischen DE/EN Text basierend auf Locale
     *
     * @param locale Die Locale
     * @param textDE Deutscher Text
     * @param textEN Englischer Text
     * @return Der passende Text
     */
    public static String selectLocalized(String locale, String textDE, String textEN) {
        if (isGerman(locale)) {
            return textDE;
        } else {
            return textEN; // English als Default
        }
    }

    /**
     * Client-Side Locale-Auswahl
     *
     * @param textDE Deutscher Text
     * @param textEN Englischer Text
     * @return Der passende Text
     */
    @OnlyIn(Dist.CLIENT)
    public static String selectClientLocalized(String textDE, String textEN) {
        String locale = getClientLocale();
        return selectLocalized(locale, textDE, textEN);
    }

    /**
     * Server-Side Fallback (immer Deutsch)
     *
     * @param textDE Deutscher Text
     * @param textEN Englischer Text (unused)
     * @return Deutscher Text
     */
    public static String selectServerLocalized(String textDE, String textEN) {
        return textDE; // Server verwendet immer DE
    }
}
