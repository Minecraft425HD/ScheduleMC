package de.rolandsw.schedulemc.player;

import java.util.UUID;

/**
 * Represents a service contact (not a player) in the contacts app
 * Examples: Pannenhilfe (Towing Service), Taxi, Emergency Services, etc.
 */
public class ServiceContact {
    private final String serviceId; // Unique ID like "towing_service"
    private final String name;
    private final ServiceType type;

    public ServiceContact(String serviceId, String name, ServiceType type) {
        this.serviceId = serviceId;
        this.name = name;
        this.type = type;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getName() {
        return name;
    }

    public ServiceType getType() {
        return type;
    }

    /**
     * Gets the translation key for the service name
     */
    public String getTranslationKey() {
        return "service.contact." + serviceId + ".name";
    }

    /**
     * Gets the icon/emoji for this service type
     */
    public String getIcon() {
        return type.getIcon();
    }

    public enum ServiceType {
        TOWING("🔧"),
        TAXI("🚕"),
        EMERGENCY("🚑"),
        DELIVERY("📦");

        private final String icon;

        ServiceType(String icon) {
            this.icon = icon;
        }

        public String getIcon() {
            return icon;
        }
    }
}
