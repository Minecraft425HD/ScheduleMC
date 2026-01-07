package de.rolandsw.schedulemc.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generic LRU (Least Recently Used) Cache implementation.
 *
 * <p>This cache automatically evicts the least recently accessed entry when
 * the maximum size is exceeded. Thread-safe operations should be externally
 * synchronized if needed.</p>
 *
 * <h2>Hauptfunktionalität:</h2>
 * <ul>
 *   <li><b>Auto-Eviction:</b> Älteste Einträge werden automatisch entfernt</li>
 *   <li><b>Access-Order:</b> LinkedHashMap mit access-order tracking</li>
 *   <li><b>Configurable Size:</b> Maximale Cache-Größe konfigurierbar</li>
 *   <li><b>Generic:</b> Funktioniert mit beliebigen Key/Value-Typen</li>
 * </ul>
 *
 * <h2>Beispiel-Verwendung:</h2>
 * <pre>{@code
 * // Cache mit maximal 100 Einträgen
 * LRUCache<String, Integer> cache = new LRUCache<>(100);
 *
 * cache.put("key1", 42);
 * cache.put("key2", 99);
 *
 * Integer value = cache.get("key1"); // 42
 *
 * // Bei 101. Eintrag wird ältester Entry automatisch entfernt
 * }</pre>
 *
 * <h2>Performance:</h2>
 * <ul>
 *   <li>get(): O(1)</li>
 *   <li>put(): O(1)</li>
 *   <li>Memory: O(n) where n = maxSize</li>
 * </ul>
 *
 * <h2>Thread-Safety:</h2>
 * <p>Diese Klasse ist NICHT thread-safe. Für concurrent access:</p>
 * <pre>{@code
 * Map<K, V> cache = Collections.synchronizedMap(new LRUCache<>(100));
 * }</pre>
 *
 * @param <K> Key-Typ
 * @param <V> Value-Typ
 * @author ScheduleMC Team
 * @version 1.0
 * @since 1.0.0
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {
    /**
     * Maximale Anzahl Einträge im Cache.
     * <p>Wenn überschritten, wird ältester Eintrag entfernt.</p>
     */
    private final int maxSize;

    /**
     * Erstellt einen LRU-Cache mit gegebener Maximalgröße.
     *
     * <p>Verwendet access-order (true) statt insertion-order für
     * korrektes LRU-Verhalten.</p>
     *
     * @param maxSize Maximale Anzahl Einträge (muss > 0 sein)
     * @throws IllegalArgumentException wenn maxSize <= 0
     */
    public LRUCache(int maxSize) {
        // initialCapacity = 16, loadFactor = 0.75, accessOrder = true
        super(16, 0.75f, true);

        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize must be > 0, got: " + maxSize);
        }

        this.maxSize = maxSize;
    }

    /**
     * Erstellt einen LRU-Cache mit gegebener Maximalgröße und initialCapacity.
     *
     * <p>Nützlich wenn erwartete Cache-Größe bekannt ist um Rehashing zu vermeiden.</p>
     *
     * @param maxSize Maximale Anzahl Einträge (muss > 0 sein)
     * @param initialCapacity Initiale Kapazität der HashMap
     * @throws IllegalArgumentException wenn maxSize <= 0
     */
    public LRUCache(int maxSize, int initialCapacity) {
        super(initialCapacity, 0.75f, true);

        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize must be > 0, got: " + maxSize);
        }

        this.maxSize = maxSize;
    }

    /**
     * Bestimmt ob ältester Entry entfernt werden soll.
     *
     * <p>Wird von LinkedHashMap nach jedem put() aufgerufen.
     * Returns true wenn Cache-Größe maxSize überschreitet.</p>
     *
     * @param eldest Ältester Entry im Cache
     * @return {@code true} wenn eldest entfernt werden soll
     */
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxSize;
    }

    /**
     * Gibt die maximale Cache-Größe zurück.
     *
     * @return Konfigurierte maximale Anzahl Einträge
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * Gibt die aktuelle Auslastung als Prozentsatz zurück.
     *
     * <p>Nützlich für Monitoring und Debug-Ausgaben.</p>
     *
     * @return Auslastung in Prozent (0.0 - 100.0)
     */
    public double getUtilization() {
        return (size() * 100.0) / maxSize;
    }
}
