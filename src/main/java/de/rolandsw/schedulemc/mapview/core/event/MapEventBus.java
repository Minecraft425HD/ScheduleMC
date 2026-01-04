package de.rolandsw.schedulemc.mapview.core.event;

import de.rolandsw.schedulemc.mapview.MapViewConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Event bus for map-related events.
 * Provides loose coupling between event producers and consumers.
 *
 * Part of Phase 2C refactoring - replaces direct Observer pattern
 * with flexible Event Bus architecture.
 *
 * Features:
 * - Type-safe event handling
 * - Thread-safe subscription management
 * - Support for event cancellation
 * - Performance metrics
 */
public class MapEventBus {

    private static final MapEventBus INSTANCE = new MapEventBus();

    // Event listeners mapped by event type
    private final Map<Class<? extends MapEvent>, List<Consumer<? extends MapEvent>>> listeners;

    // Performance metrics
    private long totalEventsDispatched = 0;

    private MapEventBus() {
        this.listeners = new ConcurrentHashMap<>();
    }

    /**
     * Gets the singleton EventBus instance.
     */
    public static MapEventBus getInstance() {
        return INSTANCE;
    }

    /**
     * Subscribes a listener to a specific event type.
     *
     * @param eventType The event class to listen for
     * @param listener  The callback to invoke when event occurs
     * @param <T>       Event type parameter
     */
    public <T extends MapEvent> void subscribe(Class<T> eventType, Consumer<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                .add(listener);
    }

    /**
     * Unsubscribes a listener from a specific event type.
     *
     * @param eventType The event class
     * @param listener  The listener to remove
     * @param <T>       Event type parameter
     */
    public <T extends MapEvent> void unsubscribe(Class<T> eventType, Consumer<T> listener) {
        List<Consumer<? extends MapEvent>> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            eventListeners.remove(listener);
        }
    }

    /**
     * Posts an event to all subscribed listeners.
     * Listeners are invoked synchronously in subscription order.
     *
     * @param event The event to post
     * @param <T>   Event type parameter
     */
    @SuppressWarnings("unchecked")
    public <T extends MapEvent> void post(T event) {
        if (event == null) {
            return;
        }

        totalEventsDispatched++;

        List<Consumer<? extends MapEvent>> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            for (Consumer<? extends MapEvent> listener : eventListeners) {
                if (!event.isCancelled()) {
                    try {
                        ((Consumer<T>) listener).accept(event);
                    } catch (Exception e) {
                        // Log error but continue processing other listeners
                        MapViewConstants.getLogger().error("Error processing event {}: {}", event.getEventName(), e.getMessage(), e);
                    }
                }
            }
        }
    }

    /**
     * Gets the total number of events dispatched since bus creation.
     */
    public long getTotalEventsDispatched() {
        return totalEventsDispatched;
    }

    /**
     * Clears all listeners. Use with caution.
     */
    public void clearAllListeners() {
        listeners.clear();
    }

    /**
     * Gets the number of listeners for a specific event type.
     */
    public int getListenerCount(Class<? extends MapEvent> eventType) {
        List<Consumer<? extends MapEvent>> eventListeners = listeners.get(eventType);
        return eventListeners != null ? eventListeners.size() : 0;
    }
}
