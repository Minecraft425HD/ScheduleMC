package de.rolandsw.schedulemc.util;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Zentrales Service-Registry fuer alle Manager-Instanzen.
 *
 * Vorteile gegenueber direktem Singleton-Zugriff:
 * - Zentrale Initialisierungs-Reihenfolge
 * - Sauberes Shutdown/Cleanup
 * - Testbarkeit (Mock-Services registrieren)
 * - Dependency-Tracking
 * - Lazy Initialization mit Supplier
 *
 * Verwendung:
 *   ServiceRegistry.register(EconomyManager.class, () -> EconomyManager.getInstance(server));
 *   EconomyManager em = ServiceRegistry.get(EconomyManager.class);
 *
 * Shutdown:
 *   ServiceRegistry.shutdownAll(); // Ruft cleanup() fuer alle Services auf
 */
public final class ServiceRegistry {

    private static final Logger LOGGER = LogUtils.getLogger();

    // Service-Instanzen
    private static final Map<Class<?>, ServiceEntry<?>> services = new ConcurrentHashMap<>();

    // Initialisierungs-Reihenfolge fuer geordnetes Shutdown
    private static final List<Class<?>> initOrder = Collections.synchronizedList(new ArrayList<>());

    // Shutdown-Hooks
    private static final Map<Class<?>, Runnable> shutdownHooks = new ConcurrentHashMap<>();

    private ServiceRegistry() {}

    // ═══════════════════════════════════════════════════════════
    // REGISTRATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Registriert einen Service mit Lazy-Initialization.
     */
    public static <T> void register(Class<T> serviceClass, Supplier<T> factory) {
        services.put(serviceClass, new ServiceEntry<>(factory));
        LOGGER.debug("Service registriert (lazy): {}", serviceClass.getSimpleName());
    }

    /**
     * Registriert einen bereits initialisierten Service.
     */
    public static <T> void registerInstance(Class<T> serviceClass, T instance) {
        ServiceEntry<T> entry = new ServiceEntry<>(() -> instance);
        entry.instance = instance;
        entry.initialized = true;
        services.put(serviceClass, entry);
        initOrder.add(serviceClass);
        LOGGER.debug("Service registriert (eager): {}", serviceClass.getSimpleName());
    }

    /**
     * Registriert einen Shutdown-Hook fuer einen Service.
     */
    public static <T> void registerShutdownHook(Class<T> serviceClass, Runnable hook) {
        shutdownHooks.put(serviceClass, hook);
    }

    // ═══════════════════════════════════════════════════════════
    // LOOKUP
    // ═══════════════════════════════════════════════════════════

    /**
     * Holt einen Service. Initialisiert lazy wenn noetig.
     *
     * @throws IllegalStateException wenn Service nicht registriert
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> serviceClass) {
        ServiceEntry<T> entry = (ServiceEntry<T>) services.get(serviceClass);
        if (entry == null) {
            throw new IllegalStateException("Service nicht registriert: " + serviceClass.getSimpleName());
        }

        if (!entry.initialized) {
            synchronized (entry) {
                if (!entry.initialized) {
                    try {
                        entry.instance = entry.factory.get();
                        entry.initialized = true;
                        initOrder.add(serviceClass);
                        LOGGER.info("Service initialisiert: {}", serviceClass.getSimpleName());
                    } catch (Exception e) {
                        LOGGER.error("Fehler bei Service-Initialisierung: {}", serviceClass.getSimpleName(), e);
                        throw new IllegalStateException("Service konnte nicht initialisiert werden: " +
                            serviceClass.getSimpleName(), e);
                    }
                }
            }
        }

        return entry.instance;
    }

    /**
     * Holt einen Service oder null wenn nicht registriert.
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public static <T> T getOptional(Class<T> serviceClass) {
        ServiceEntry<T> entry = (ServiceEntry<T>) services.get(serviceClass);
        if (entry == null || !entry.initialized) return null;
        return entry.instance;
    }

    /**
     * Prueft ob ein Service registriert ist.
     */
    public static boolean isRegistered(Class<?> serviceClass) {
        return services.containsKey(serviceClass);
    }

    /**
     * Prueft ob ein Service initialisiert ist.
     */
    public static boolean isInitialized(Class<?> serviceClass) {
        ServiceEntry<?> entry = services.get(serviceClass);
        return entry != null && entry.initialized;
    }

    // ═══════════════════════════════════════════════════════════
    // LIFECYCLE
    // ═══════════════════════════════════════════════════════════

    /**
     * Faehrt alle Services in umgekehrter Initialisierungsreihenfolge herunter.
     */
    public static void shutdownAll() {
        LOGGER.info("ServiceRegistry shutdown gestartet ({} Services)", initOrder.size());

        // Umgekehrte Reihenfolge
        List<Class<?>> reversed = new ArrayList<>(initOrder);
        Collections.reverse(reversed);

        for (Class<?> serviceClass : reversed) {
            try {
                Runnable hook = shutdownHooks.get(serviceClass);
                if (hook != null) {
                    hook.run();
                    LOGGER.debug("Shutdown-Hook ausgefuehrt: {}", serviceClass.getSimpleName());
                }
            } catch (Exception e) {
                LOGGER.error("Fehler beim Shutdown von {}", serviceClass.getSimpleName(), e);
            }
        }

        services.clear();
        initOrder.clear();
        shutdownHooks.clear();
        LOGGER.info("ServiceRegistry shutdown abgeschlossen");
    }

    /**
     * Entfernt einen einzelnen Service.
     */
    public static void unregister(Class<?> serviceClass) {
        Runnable hook = shutdownHooks.remove(serviceClass);
        if (hook != null) {
            try {
                hook.run();
            } catch (Exception e) {
                LOGGER.error("Fehler beim Shutdown-Hook von {}", serviceClass.getSimpleName(), e);
            }
        }
        services.remove(serviceClass);
        initOrder.remove(serviceClass);
        LOGGER.debug("Service entfernt: {}", serviceClass.getSimpleName());
    }

    // ═══════════════════════════════════════════════════════════
    // DIAGNOSTICS
    // ═══════════════════════════════════════════════════════════

    /**
     * Gibt Status-Report aller registrierten Services zurueck.
     */
    public static String getStatusReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ServiceRegistry Status ===\n");
        sb.append(String.format("Registriert: %d | Initialisiert: %d\n\n",
            services.size(), initOrder.size()));

        for (Map.Entry<Class<?>, ServiceEntry<?>> entry : services.entrySet()) {
            String name = entry.getKey().getSimpleName();
            boolean init = entry.getValue().initialized;
            boolean hasHook = shutdownHooks.containsKey(entry.getKey());
            sb.append(String.format("  %s %s %s\n",
                init ? "\u2714" : "\u2718",
                name,
                hasHook ? "(shutdown-hook)" : ""));
        }

        return sb.toString();
    }

    /**
     * Gibt die Anzahl registrierter Services zurueck.
     */
    public static int getServiceCount() {
        return services.size();
    }

    /**
     * Gibt die Anzahl initialisierter Services zurueck.
     */
    public static int getInitializedCount() {
        return initOrder.size();
    }

    // ═══════════════════════════════════════════════════════════
    // INTERNAL
    // ═══════════════════════════════════════════════════════════

    private static class ServiceEntry<T> {
        final Supplier<T> factory;
        volatile T instance;
        volatile boolean initialized;

        ServiceEntry(Supplier<T> factory) {
            this.factory = factory;
        }
    }
}
