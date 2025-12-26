package de.rolandsw.schedulemc.mapview.core.event;

/**
 * Base class for all map-related events.
 * Part of Phase 2C refactoring to replace direct Observer pattern
 * with Event Bus architecture for loose coupling.
 */
public abstract class MapEvent {

    private final long timestamp;
    private boolean cancelled = false;

    protected MapEvent() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Gets the timestamp when this event was created.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Checks if this event has been cancelled.
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Cancels this event (if cancellable).
     */
    public void cancel() {
        this.cancelled = true;
    }

    /**
     * Gets a human-readable name for this event type.
     */
    public String getEventName() {
        return this.getClass().getSimpleName();
    }
}
