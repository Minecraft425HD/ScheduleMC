package de.rolandsw.schedulemc.exceptions;

/**
 * Exception thrown when NPC system operations fail.
 * <p>
 * This exception covers all NPC-related errors including:
 * <ul>
 *   <li>NPC spawn failures</li>
 *   <li>Invalid NPC personality or trait assignments</li>
 *   <li>NPC AI pathfinding errors</li>
 *   <li>Police AI pursuit system failures</li>
 *   <li>Crime detection errors</li>
 *   <li>NPC dialogue system failures</li>
 *   <li>Relationship tracking errors</li>
 * </ul>
 * </p>
 *
 * @author ScheduleMC Development Team
 * @since 1.0.0
 * @see de.rolandsw.schedulemc.npc.NPCManager
 * @see de.rolandsw.schedulemc.npc.police.PoliceAIHandler
 */
public class NPCException extends ScheduleMCException {

    /**
     * Constructs a new NPC exception with the specified detail message.
     *
     * @param message the detail message explaining the NPC error
     */
    public NPCException(String message) {
        super(message);
    }

    /**
     * Constructs a new NPC exception with the specified detail message and cause.
     *
     * @param message the detail message explaining the NPC error
     * @param cause the underlying cause of this exception
     */
    public NPCException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new NPC exception with the specified cause.
     *
     * @param cause the underlying cause of this exception
     */
    public NPCException(Throwable cause) {
        super(cause);
    }
}
