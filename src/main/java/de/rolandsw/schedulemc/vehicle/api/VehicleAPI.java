package de.rolandsw.schedulemc.vehicle.api;

import de.rolandsw.schedulemc.vehicle.builder.VehicleBuilder;
import de.rolandsw.schedulemc.vehicle.component.body.BodySpecification;
import de.rolandsw.schedulemc.vehicle.component.engine.EngineSpecification;
import de.rolandsw.schedulemc.vehicle.component.fuel.FuelTankSpecification;
import de.rolandsw.schedulemc.vehicle.component.mobility.WheelSpecification;
import de.rolandsw.schedulemc.vehicle.core.component.IVehicleComponent;
import de.rolandsw.schedulemc.vehicle.core.registry.ComponentRegistry;
import de.rolandsw.schedulemc.vehicle.core.registry.SystemRegistry;
import de.rolandsw.schedulemc.vehicle.core.system.IVehicleSystem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * Public API for the vehicle system.
 * Use this to create custom vehicles, components, and systems.
 *
 * This API allows other mods to:
 * - Create custom vehicle types
 * - Add custom components
 * - Add custom systems
 * - Register custom specifications (engines, bodies, etc.)
 *
 * Example usage for addon developers:
 * <pre>
 * // Register a custom engine
 * EngineSpecification myEngine = EngineSpecification.custom(
 *     "myaddon:super_engine",
 *     500.0f,  // power
 *     8000.0f, // max RPM
 *     1.5f,    // fuel consumption
 *     12       // cylinders
 * );
 *
 * // Create a vehicle with custom specs
 * VehicleEntity myVehicle = VehicleAPI.createBuilder(world)
 *     .withEngine(myEngine)
 *     .withBody(BodySpecification.SPORT)
 *     .build();
 *
 * // Register a custom component
 * VehicleAPI.registerComponent(
 *     new ResourceLocation("myaddon", "my_component"),
 *     MyCustomComponent::new
 * );
 *
 * // Register a custom system
 * VehicleAPI.registerSystem(new MyCustomSystem());
 * </pre>
 */
public class VehicleAPI {

    /**
     * Creates a new vehicle builder.
     */
    public static VehicleBuilder createBuilder(Level world) {
        return VehicleBuilder.create(world);
    }

    /**
     * Registers a custom component type.
     *
     * @param type The component type identifier
     * @param factory Factory function to create component instances
     */
    public static <T extends IVehicleComponent> void registerComponent(
            ResourceLocation type,
            ComponentRegistry.ComponentFactory<T> factory
    ) {
        ComponentRegistry.register(type, factory);
    }

    /**
     * Registers a custom vehicle system.
     *
     * @param system The system to register
     */
    public static void registerSystem(IVehicleSystem system) {
        SystemRegistry.register(system);
    }

    /**
     * Creates a custom engine specification.
     * Convenience method for EngineSpecification.custom()
     */
    public static EngineSpecification createCustomEngine(
            String id,
            float maxPower,
            float maxRpm,
            float fuelConsumption,
            int cylinders
    ) {
        return EngineSpecification.custom(id, maxPower, maxRpm, fuelConsumption, cylinders);
    }

    /**
     * Creates a custom body specification.
     * Convenience method for BodySpecification.custom()
     */
    public static BodySpecification createCustomBody(
            String id,
            int seats,
            float weight,
            float aerodynamics,
            ResourceLocation model,
            ResourceLocation texture
    ) {
        return BodySpecification.custom(id, seats, weight, aerodynamics, model, texture);
    }

    /**
     * Creates a custom wheel specification.
     * Convenience method for WheelSpecification.custom()
     */
    public static WheelSpecification createCustomWheels(
            String id,
            float diameter,
            float traction,
            float grip,
            ResourceLocation model,
            ResourceLocation texture
    ) {
        return WheelSpecification.custom(id, diameter, traction, grip, model, texture);
    }

    /**
     * Creates a custom fuel tank specification.
     * Convenience method for FuelTankSpecification.custom()
     */
    public static FuelTankSpecification createCustomTank(
            String id,
            float capacity
    ) {
        return FuelTankSpecification.custom(id, capacity);
    }
}
