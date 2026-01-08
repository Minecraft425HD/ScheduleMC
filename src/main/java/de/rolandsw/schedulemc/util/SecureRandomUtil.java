package de.rolandsw.schedulemc.util;

import javax.annotation.Nonnull;
import java.security.SecureRandom;

/**
 * Zentrale Utility-Klasse für kryptographisch sichere Zufallszahlen
 *
 * SICHERHEIT: Verwendet SecureRandom statt Math.random() oder Random()
 * um Vorhersagbarkeit von Spiel-Mechaniken zu verhindern.
 *
 * Performance-Hinweis: SecureRandom ist thread-safe und kann gleichzeitig
 * von mehreren Threads verwendet werden.
 */
public class SecureRandomUtil {

    /**
     * SICHERHEIT: Singleton SecureRandom-Instanz für die gesamte Anwendung
     * Thread-safe: SecureRandom ist intern synchronized
     */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Privater Konstruktor - Utility-Klasse soll nicht instantiiert werden
     */
    private SecureRandomUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Gibt eine zufällige double-Zahl zwischen 0.0 (inklusiv) und 1.0 (exklusiv) zurück
     *
     * Ersetzt: Math.random()
     *
     * @return Zufällige Zahl zwischen 0.0 und 1.0
     */
    public static double nextDouble() {
        return SECURE_RANDOM.nextDouble();
    }

    /**
     * Gibt eine zufällige double-Zahl zwischen 0.0 (inklusiv) und bound (exklusiv) zurück
     *
     * @param bound Obere Grenze (exklusiv)
     * @return Zufällige Zahl zwischen 0.0 und bound
     */
    public static double nextDouble(double bound) {
        return SECURE_RANDOM.nextDouble() * bound;
    }

    /**
     * Gibt eine zufällige int-Zahl zwischen 0 (inklusiv) und bound (exklusiv) zurück
     *
     * Ersetzt: random.nextInt(bound)
     *
     * @param bound Obere Grenze (exklusiv), muss positiv sein
     * @return Zufällige Zahl zwischen 0 und bound-1
     */
    public static int nextInt(int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("bound must be positive");
        }
        return SECURE_RANDOM.nextInt(bound);
    }

    /**
     * Gibt eine zufällige int-Zahl zurück
     *
     * @return Zufällige int-Zahl
     */
    public static int nextInt() {
        return SECURE_RANDOM.nextInt();
    }

    /**
     * Gibt eine zufällige boolean-Wert zurück
     *
     * @return true oder false mit 50% Wahrscheinlichkeit
     */
    public static boolean nextBoolean() {
        return SECURE_RANDOM.nextBoolean();
    }

    /**
     * Gibt eine zufällige float-Zahl zwischen 0.0 und 1.0 zurück
     *
     * @return Zufällige float-Zahl
     */
    public static float nextFloat() {
        return SECURE_RANDOM.nextFloat();
    }

    /**
     * Gibt eine zufällige long-Zahl zurück
     *
     * @return Zufällige long-Zahl
     */
    public static long nextLong() {
        return SECURE_RANDOM.nextLong();
    }

    /**
     * Füllt ein byte-Array mit zufälligen Bytes
     *
     * @param bytes Das zu füllende Array
     */
    public static void nextBytes(@Nonnull byte[] bytes) {
        SECURE_RANDOM.nextBytes(bytes);
    }

    /**
     * Gibt eine zufällige Ganzzahl zwischen min (inklusiv) und max (inklusiv) zurück
     *
     * @param min Untere Grenze (inklusiv)
     * @param max Obere Grenze (inklusiv)
     * @return Zufällige Zahl zwischen min und max
     */
    public static int nextInt(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("min must be <= max");
        }
        if (min == max) {
            return min;
        }
        return min + SECURE_RANDOM.nextInt(max - min + 1);
    }

    /**
     * Gibt eine zufällige double-Zahl zwischen min und max zurück
     *
     * @param min Untere Grenze
     * @param max Obere Grenze
     * @return Zufällige Zahl zwischen min und max
     */
    public static double nextDouble(double min, double max) {
        if (min > max) {
            throw new IllegalArgumentException("min must be <= max");
        }
        return min + (SECURE_RANDOM.nextDouble() * (max - min));
    }

    /**
     * Gibt true zurück mit der gegebenen Wahrscheinlichkeit
     *
     * Beispiel: chance(0.33) gibt true in 33% der Fälle zurück
     *
     * @param probability Wahrscheinlichkeit zwischen 0.0 und 1.0
     * @return true mit der gegebenen Wahrscheinlichkeit
     */
    public static boolean chance(double probability) {
        if (probability < 0.0 || probability > 1.0) {
            throw new IllegalArgumentException("probability must be between 0.0 and 1.0");
        }
        return SECURE_RANDOM.nextDouble() < probability;
    }

    /**
     * Gibt direkten Zugriff auf die SecureRandom-Instanz
     * Nur für spezielle Anwendungsfälle verwenden
     *
     * @return Die SecureRandom-Instanz
     */
    public static SecureRandom getSecureRandom() {
        return SECURE_RANDOM;
    }
}
