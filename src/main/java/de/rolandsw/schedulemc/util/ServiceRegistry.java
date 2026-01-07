package de.rolandsw.schedulemc.util;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Service Locator pattern implementation for dependency management.
 * <p>
 * Provides centralized service registration and lookup with:
 * <ul>
 *   <li>Type-safe service retrieval</li>
 *   <li>Lazy initialization support</li>
 *   <li>Singleton enforcement</li>
 *   <li>Service lifecycle management</li>
 *   <li>Thread-safe operations</li>
 * </ul>
 * </p>
 *
 * <h2>Benefits:</h2>
 * <ul>
 *   <li>Decouples service consumers from service implementations</li>
 *   <li>Simplifies testing with mock services</li>
 *   <li>Centralizes dependency management</li>
 *   <li>Reduces tight coupling between modules</li>
 * </ul>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * // Register services during mod initialization
 * ServiceRegistry.register(EconomyManager.class, EconomyManager.getInstance());
 * ServiceRegistry.register(PlotManager.class, PlotManager.getInstance());
 *
 * // Or with lazy initialization
 * ServiceRegistry.registerLazy(
 *     TransactionHistory.class,
 *     TransactionHistory::getInstance
 * );
 *
 * // Retrieve services
 * EconomyManager economy = ServiceRegistry.get(EconomyManager.class);
 *
 * // Optional-based retrieval for safer code
 * Optional<PlotManager> plotManager = ServiceRegistry.find(PlotManager.class);
 * plotManager.ifPresent(pm -> pm.createPlot(...));
 * }</pre>
 *
 * @author ScheduleMC Development Team
 * @since 3.2.0
 */
public class ServiceRegistry {

    private static final Logger LOGGER = LogUtils.getLogger();

    // Service instance registry
    private static final Map<Class<?>, Object> services = new ConcurrentHashMap<>();

    // Lazy service suppliers
    private static final Map<Class<?>, Supplier<?>> lazyServices = new ConcurrentHashMap<>();

    /**
     * Registers a service instance.
     *
     * @param serviceClass Service class/interface
     * @param instance Service instance
     * @param <T> Service type
     * @throws IllegalArgumentException if service already registered
     */
    public static <T> void register(@Nonnull Class<T> serviceClass, @Nonnull T instance) {
        if (services.containsKey(serviceClass)) {
            throw new IllegalArgumentException(
                "Service already registered: " + serviceClass.getSimpleName()
            );
        }

        services.put(serviceClass, instance);
        LOGGER.debug("Registered service: {}", serviceClass.getSimpleName());
    }

    /**
     * Registers a service with lazy initialization.
     * <p>
     * The supplier will be called only once, on first access.
     * </p>
     *
     * @param serviceClass Service class/interface
     * @param supplier Service instance supplier
     * @param <T> Service type
     */
    public static <T> void registerLazy(@Nonnull Class<T> serviceClass,
                                       @Nonnull Supplier<T> supplier) {
        if (services.containsKey(serviceClass) || lazyServices.containsKey(serviceClass)) {
            throw new IllegalArgumentException(
                "Service already registered: " + serviceClass.getSimpleName()
            );
        }

        lazyServices.put(serviceClass, supplier);
        LOGGER.debug("Registered lazy service: {}", serviceClass.getSimpleName());
    }

    /**
     * Registers or replaces a service instance.
     * <p>
     * Useful for testing or hot-reloading scenarios.
     * </p>
     *
     * @param serviceClass Service class/interface
     * @param instance Service instance
     * @param <T> Service type
     */
    public static <T> void registerOrReplace(@Nonnull Class<T> serviceClass, @Nonnull T instance) {
        Object previous = services.put(serviceClass, instance);
        lazyServices.remove(serviceClass);

        if (previous != null) {
            LOGGER.warn("Replaced existing service: {}", serviceClass.getSimpleName());
        } else {
            LOGGER.debug("Registered service: {}", serviceClass.getSimpleName());
        }
    }

    /**
     * Retrieves a service instance.
     *
     * @param serviceClass Service class/interface
     * @param <T> Service type
     * @return Service instance
     * @throws IllegalStateException if service not found
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public static <T> T get(@Nonnull Class<T> serviceClass) {
        // Check if already initialized
        Object service = services.get(serviceClass);
        if (service != null) {
            return (T) service;
        }

        // Check for lazy initialization
        Supplier<?> supplier = lazyServices.get(serviceClass);
        if (supplier != null) {
            synchronized (lazyServices) {
                // Double-check in synchronized block
                service = services.get(serviceClass);
                if (service != null) {
                    return (T) service;
                }

                // Initialize service
                service = supplier.get();
                services.put(serviceClass, service);
                lazyServices.remove(serviceClass);

                LOGGER.debug("Lazy-initialized service: {}", serviceClass.getSimpleName());
                return (T) service;
            }
        }

        throw new IllegalStateException(
            "Service not registered: " + serviceClass.getSimpleName() +
            ". Did you forget to register it during mod initialization?"
        );
    }

    /**
     * Retrieves a service instance as Optional.
     * <p>
     * Safer alternative to {@link #get(Class)} that doesn't throw exceptions.
     * </p>
     *
     * @param serviceClass Service class/interface
     * @param <T> Service type
     * @return Optional containing service instance, or empty if not found
     */
    @Nonnull
    public static <T> Optional<T> find(@Nonnull Class<T> serviceClass) {
        try {
            return Optional.of(get(serviceClass));
        } catch (IllegalStateException e) {
            return Optional.empty();
        }
    }

    /**
     * Gets service with fallback supplier if not found.
     *
     * @param serviceClass Service class/interface
     * @param fallback Fallback supplier
     * @param <T> Service type
     * @return Service instance or fallback value
     */
    @Nonnull
    public static <T> T getOrDefault(@Nonnull Class<T> serviceClass,
                                    @Nonnull Supplier<T> fallback) {
        return find(serviceClass).orElseGet(fallback);
    }

    /**
     * Checks if a service is registered.
     *
     * @param serviceClass Service class/interface
     * @return true if service is registered (initialized or lazy)
     */
    public static boolean isRegistered(@Nonnull Class<?> serviceClass) {
        return services.containsKey(serviceClass) || lazyServices.containsKey(serviceClass);
    }

    /**
     * Unregisters a service.
     * <p>
     * Useful for testing or reloading scenarios.
     * </p>
     *
     * @param serviceClass Service class/interface
     * @return true if service was registered
     */
    public static boolean unregister(@Nonnull Class<?> serviceClass) {
        boolean removed = services.remove(serviceClass) != null;
        removed |= lazyServices.remove(serviceClass) != null;

        if (removed) {
            LOGGER.debug("Unregistered service: {}", serviceClass.getSimpleName());
        }

        return removed;
    }

    /**
     * Clears all registered services.
     * <p>
     * WARNING: This will break any code that relies on service lookup.
     * Only use for testing or shutdown scenarios.
     * </p>
     */
    public static void clearAll() {
        LOGGER.warn("Clearing all services ({} initialized, {} lazy)",
            services.size(), lazyServices.size());
        services.clear();
        lazyServices.clear();
    }

    /**
     * Gets the number of registered services.
     *
     * @return Total number of services (initialized + lazy)
     */
    public static int getServiceCount() {
        return services.size() + lazyServices.size();
    }

    /**
     * Gets the number of initialized services.
     *
     * @return Number of initialized services
     */
    public static int getInitializedCount() {
        return services.size();
    }

    /**
     * Gets the number of lazy services not yet initialized.
     *
     * @return Number of lazy services
     */
    public static int getLazyCount() {
        return lazyServices.size();
    }

    /**
     * Gets registry statistics.
     *
     * @return Statistics string
     */
    public static String getStats() {
        return String.format(
            "ServiceRegistry: %d total (%d initialized, %d lazy)",
            getServiceCount(), getInitializedCount(), getLazyCount()
        );
    }

    private ServiceRegistry() {
        throw new UnsupportedOperationException("Utility class");
    }
}
