package de.rolandsw.schedulemc.exceptions;

/**
 * Exception thrown when permission checks fail.
 * <p>
 * This exception covers all authorization errors including:
 * <ul>
 *   <li>Insufficient permissions for plot operations</li>
 *   <li>Unauthorized access to protected resources</li>
 *   <li>Missing role or permission requirements</li>
 *   <li>Plot protection violations</li>
 *   <li>Admin command access denied</li>
 *   <li>Inventory protection failures</li>
 * </ul>
 * </p>
 *
 * @author ScheduleMC Development Team
 * @since 1.0.0
 * @see de.rolandsw.schedulemc.region.PlotProtectionHandler
 */
public class PermissionException extends ScheduleMCException {

    private final String requiredPermission;
    private final String playerName;

    /**
     * Constructs a new permission exception with the specified detail message.
     *
     * @param message the detail message explaining the permission error
     */
    public PermissionException(String message) {
        super(message);
        this.requiredPermission = null;
        this.playerName = null;
    }

    /**
     * Constructs a new permission exception with permission context.
     *
     * @param message the detail message explaining the permission error
     * @param playerName the name of the player who was denied access
     * @param requiredPermission the permission that was required
     */
    public PermissionException(String message, String playerName, String requiredPermission) {
        super(String.format("%s (player: %s, required: %s)", message, playerName, requiredPermission));
        this.playerName = playerName;
        this.requiredPermission = requiredPermission;
    }

    /**
     * Constructs a new permission exception with the specified detail message and cause.
     *
     * @param message the detail message explaining the permission error
     * @param cause the underlying cause of this exception
     */
    public PermissionException(String message, Throwable cause) {
        super(message, cause);
        this.requiredPermission = null;
        this.playerName = null;
    }

    /**
     * Gets the permission that was required but not granted.
     *
     * @return the required permission, or null if not specified
     */
    public String getRequiredPermission() {
        return requiredPermission;
    }

    /**
     * Gets the name of the player who was denied access.
     *
     * @return the player name, or null if not specified
     */
    public String getPlayerName() {
        return playerName;
    }
}
