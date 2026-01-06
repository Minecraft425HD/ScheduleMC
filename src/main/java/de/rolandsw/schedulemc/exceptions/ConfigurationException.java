package de.rolandsw.schedulemc.exceptions;

/**
 * Exception thrown when configuration-related operations fail.
 * <p>
 * This exception covers all configuration errors including:
 * <ul>
 *   <li>Invalid configuration file formats</li>
 *   <li>Missing required configuration values</li>
 *   <li>Configuration parsing errors</li>
 *   <li>Invalid configuration value ranges</li>
 *   <li>Configuration reload failures</li>
 *   <li>ModConfigHandler errors</li>
 * </ul>
 * </p>
 *
 * @author ScheduleMC Development Team
 * @since 1.0.0
 * @see de.rolandsw.schedulemc.config.ModConfigHandler
 */
public class ConfigurationException extends ScheduleMCException {

    private final String configKey;

    /**
     * Constructs a new configuration exception with the specified detail message.
     *
     * @param message the detail message explaining the configuration error
     */
    public ConfigurationException(String message) {
        super(message);
        this.configKey = null;
    }

    /**
     * Constructs a new configuration exception with configuration key context.
     *
     * @param message the detail message explaining the configuration error
     * @param configKey the configuration key that caused the error
     */
    public ConfigurationException(String message, String configKey) {
        super(String.format("%s (config key: %s)", message, configKey));
        this.configKey = configKey;
    }

    /**
     * Constructs a new configuration exception with the specified detail message and cause.
     *
     * @param message the detail message explaining the configuration error
     * @param cause the underlying cause of this exception
     */
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
        this.configKey = null;
    }

    /**
     * Constructs a new configuration exception with configuration key context and cause.
     *
     * @param message the detail message explaining the configuration error
     * @param configKey the configuration key that caused the error
     * @param cause the underlying cause of this exception
     */
    public ConfigurationException(String message, String configKey, Throwable cause) {
        super(String.format("%s (config key: %s)", message, configKey), cause);
        this.configKey = configKey;
    }

    /**
     * Gets the configuration key that caused the error.
     *
     * @return the configuration key, or null if not specified
     */
    public String getConfigKey() {
        return configKey;
    }
}
