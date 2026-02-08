package de.rolandsw.schedulemc.util;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Circuit-Breaker Pattern fuer fehlertolerante Systeme.
 *
 * Schuetzt kritische Systeme vor Kaskadenausfaellen:
 * - CLOSED: Normal, Anfragen werden durchgelassen
 * - OPEN: Zu viele Fehler, Anfragen werden sofort abgelehnt
 * - HALF_OPEN: Testphase, einzelne Anfragen werden durchgelassen
 *
 * Konfigurierbar pro System:
 * - failureThreshold: Anzahl Fehler bis OPEN (default: 5)
 * - resetTimeoutMs: Zeit bis HALF_OPEN (default: 30s)
 * - halfOpenMaxAttempts: Tests in HALF_OPEN (default: 3)
 *
 * Verwendung:
 *   CircuitBreaker cb = CircuitBreaker.create("economy", 5, 30000);
 *   double balance = cb.execute(() -> EconomyManager.getBalance(uuid), 0.0);
 */
public class CircuitBreaker {

    private static final Logger LOGGER = LogUtils.getLogger();

    // Globale Registry aller Circuit-Breakers
    private static final Map<String, CircuitBreaker> registry = new ConcurrentHashMap<>();

    public enum State {
        CLOSED,     // Normal
        OPEN,       // Blockiert
        HALF_OPEN   // Testphase
    }

    private final String name;
    private final int failureThreshold;
    private final long resetTimeoutMs;
    private final int halfOpenMaxAttempts;

    private volatile State state = State.CLOSED;
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicLong lastFailureTime = new AtomicLong(0);
    private final AtomicInteger halfOpenAttempts = new AtomicInteger(0);

    // Statistiken
    private final AtomicLong totalCalls = new AtomicLong(0);
    private final AtomicLong totalFailures = new AtomicLong(0);
    private final AtomicLong totalRejections = new AtomicLong(0);

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTION
    // ═══════════════════════════════════════════════════════════

    private CircuitBreaker(String name, int failureThreshold, long resetTimeoutMs, int halfOpenMaxAttempts) {
        this.name = name;
        this.failureThreshold = failureThreshold;
        this.resetTimeoutMs = resetTimeoutMs;
        this.halfOpenMaxAttempts = halfOpenMaxAttempts;
    }

    /**
     * Erstellt oder holt einen benannten Circuit-Breaker.
     */
    public static CircuitBreaker create(String name, int failureThreshold, long resetTimeoutMs) {
        return registry.computeIfAbsent(name,
            k -> new CircuitBreaker(k, failureThreshold, resetTimeoutMs, 3));
    }

    /**
     * Erstellt mit allen Parametern.
     */
    public static CircuitBreaker create(String name, int failureThreshold, long resetTimeoutMs,
                                        int halfOpenMaxAttempts) {
        return registry.computeIfAbsent(name,
            k -> new CircuitBreaker(k, failureThreshold, resetTimeoutMs, halfOpenMaxAttempts));
    }

    /**
     * Holt einen existierenden Circuit-Breaker.
     */
    public static CircuitBreaker get(String name) {
        CircuitBreaker cb = registry.get(name);
        if (cb == null) {
            throw new IllegalStateException("CircuitBreaker nicht gefunden: " + name);
        }
        return cb;
    }

    // ═══════════════════════════════════════════════════════════
    // EXECUTION
    // ═══════════════════════════════════════════════════════════

    /**
     * Fuehrt eine Operation mit Circuit-Breaker-Schutz aus.
     *
     * @param operation Die auszufuehrende Operation
     * @param fallback  Fallback-Wert bei OPEN oder Fehler
     * @return Ergebnis der Operation oder Fallback
     */
    public <T> T execute(Supplier<T> operation, T fallback) {
        totalCalls.incrementAndGet();

        if (!allowRequest()) {
            totalRejections.incrementAndGet();
            return fallback;
        }

        try {
            T result = operation.get();
            onSuccess();
            return result;
        } catch (Exception e) {
            onFailure(e);
            return fallback;
        }
    }

    /**
     * Fuehrt eine Operation ohne Rueckgabewert aus.
     */
    public void executeVoid(Runnable operation) {
        totalCalls.incrementAndGet();

        if (!allowRequest()) {
            totalRejections.incrementAndGet();
            return;
        }

        try {
            operation.run();
            onSuccess();
        } catch (Exception e) {
            onFailure(e);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // STATE MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    private boolean allowRequest() {
        switch (state) {
            case CLOSED:
                return true;

            case OPEN:
                // Pruefe ob Reset-Timeout abgelaufen
                if (System.currentTimeMillis() - lastFailureTime.get() > resetTimeoutMs) {
                    transitionTo(State.HALF_OPEN);
                    halfOpenAttempts.set(0);
                    return true;
                }
                return false;

            case HALF_OPEN:
                // Nur begrenzte Anfragen in HALF_OPEN
                return halfOpenAttempts.incrementAndGet() <= halfOpenMaxAttempts;

            default:
                return false;
        }
    }

    private void onSuccess() {
        switch (state) {
            case HALF_OPEN:
                successCount.incrementAndGet();
                if (successCount.get() >= halfOpenMaxAttempts) {
                    transitionTo(State.CLOSED);
                }
                break;
            case CLOSED:
                // Fehler-Zaehler zuruecksetzen nach Erfolg
                failureCount.set(0);
                break;
        }
    }

    private void onFailure(Exception e) {
        totalFailures.incrementAndGet();
        lastFailureTime.set(System.currentTimeMillis());

        switch (state) {
            case CLOSED:
                if (failureCount.incrementAndGet() >= failureThreshold) {
                    transitionTo(State.OPEN);
                }
                break;
            case HALF_OPEN:
                // Sofort zurueck zu OPEN
                transitionTo(State.OPEN);
                break;
        }

        LOGGER.warn("CircuitBreaker '{}' Fehler ({}): {}", name, state, e.getMessage());
    }

    private void transitionTo(State newState) {
        State old = this.state;
        this.state = newState;

        if (newState == State.CLOSED) {
            failureCount.set(0);
            successCount.set(0);
        }

        LOGGER.info("CircuitBreaker '{}': {} -> {}", name, old, newState);
    }

    // ═══════════════════════════════════════════════════════════
    // MANUAL CONTROL
    // ═══════════════════════════════════════════════════════════

    /**
     * Setzt den Circuit-Breaker manuell zurueck.
     */
    public void reset() {
        transitionTo(State.CLOSED);
        totalFailures.set(0);
        totalRejections.set(0);
        totalCalls.set(0);
    }

    /**
     * Oeffnet den Circuit-Breaker manuell (blockiert alle Anfragen).
     */
    public void forceOpen() {
        transitionTo(State.OPEN);
        lastFailureTime.set(System.currentTimeMillis());
    }

    // ═══════════════════════════════════════════════════════════
    // QUERIES
    // ═══════════════════════════════════════════════════════════

    public State getState() { return state; }
    public String getName() { return name; }
    public int getFailureCount() { return failureCount.get(); }
    public long getTotalCalls() { return totalCalls.get(); }
    public long getTotalFailures() { return totalFailures.get(); }
    public long getTotalRejections() { return totalRejections.get(); }

    public float getFailureRate() {
        long total = totalCalls.get();
        return total > 0 ? (float) totalFailures.get() / total : 0;
    }

    // ═══════════════════════════════════════════════════════════
    // GLOBAL DIAGNOSTICS
    // ═══════════════════════════════════════════════════════════

    /**
     * Status-Report aller Circuit-Breakers.
     */
    public static String getGlobalReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Circuit-Breaker Status ===\n");

        for (CircuitBreaker cb : registry.values()) {
            String stateIcon;
            switch (cb.state) {
                case CLOSED -> stateIcon = "\u00A7a\u2714 CLOSED";
                case OPEN -> stateIcon = "\u00A7c\u2718 OPEN";
                case HALF_OPEN -> stateIcon = "\u00A7e\u25CB HALF_OPEN";
                default -> stateIcon = "?";
            }

            sb.append(String.format("  %s \u00A7f%s \u00A77[calls:%d fails:%d rejected:%d rate:%.1f%%]\n",
                stateIcon, cb.name, cb.totalCalls.get(), cb.totalFailures.get(),
                cb.totalRejections.get(), cb.getFailureRate() * 100));
        }

        return sb.toString();
    }

    /**
     * Setzt alle Circuit-Breakers zurueck.
     */
    public static void resetAll() {
        for (CircuitBreaker cb : registry.values()) {
            cb.reset();
        }
        LOGGER.info("Alle Circuit-Breakers zurueckgesetzt");
    }
}
