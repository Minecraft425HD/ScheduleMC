package de.rolandsw.schedulemc.vehicle.builder;

import de.rolandsw.schedulemc.vehicle.component.attribute.BatteryComponent;
import de.rolandsw.schedulemc.vehicle.component.attribute.DurabilityComponent;
import de.rolandsw.schedulemc.vehicle.component.attribute.TemperatureComponent;
import de.rolandsw.schedulemc.vehicle.component.body.BodyComponent;
import de.rolandsw.schedulemc.vehicle.component.body.BodySpecification;
import de.rolandsw.schedulemc.vehicle.component.control.ControlComponent;
import de.rolandsw.schedulemc.vehicle.component.control.LicensePlateComponent;
import de.rolandsw.schedulemc.vehicle.component.control.OwnershipComponent;
import de.rolandsw.schedulemc.vehicle.component.engine.EngineComponent;
import de.rolandsw.schedulemc.vehicle.component.engine.EngineSpecification;
import de.rolandsw.schedulemc.vehicle.component.fuel.FuelTankComponent;
import de.rolandsw.schedulemc.vehicle.component.fuel.FuelTankSpecification;
import de.rolandsw.schedulemc.vehicle.component.mobility.WheelComponent;
import de.rolandsw.schedulemc.vehicle.component.mobility.WheelSpecification;
import de.rolandsw.schedulemc.vehicle.component.storage.InventoryComponent;
import de.rolandsw.schedulemc.vehicle.core.component.IVehicleComponent;
import de.rolandsw.schedulemc.vehicle.core.entity.VehicleEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Builder pattern for creating vehicles easily.
 * Provides a fluent API for vehicle construction.
 *
 * Example usage:
 * <pre>
 * VehicleEntity car = VehicleBuilder.create(world)
 *     .withEngine(EngineSpecification.INLINE_SIX)
 *     .withBody(BodySpecification.SPORT)
 *     .withWheels(WheelSpecification.SPORT)
 *     .withFuelTank(FuelTankSpecification.MEDIUM)
 *     .withColor(0xFF0000)
 *     .withOwner(player)
 *     .build();
 * </pre>
 */
public class VehicleBuilder {

    private final Level world;
    private final List<IVehicleComponent> components = new ArrayList<>();

    private EngineSpecification engineSpec = EngineSpecification.DEFAULT;
    private BodySpecification bodySpec = BodySpecification.SEDAN;
    private WheelSpecification wheelSpec = WheelSpecification.STANDARD;
    private FuelTankSpecification tankSpec = FuelTankSpecification.MEDIUM;

    private int bodyColor = 0xFFFFFF;
    private String vehicleTypeId = "custom";
    private int inventorySlots = 0;
    private boolean withBattery = true;
    private boolean withTemperature = true;
    private boolean withDurability = true;

    @Nullable
    private Player owner;
    @Nullable
    private String licensePlate;

    private VehicleBuilder(Level world) {
        this.world = world;
    }

    /**
     * Creates a new vehicle builder.
     */
    public static VehicleBuilder create(Level world) {
        return new VehicleBuilder(world);
    }

    /**
     * Sets the engine specification.
     */
    public VehicleBuilder withEngine(EngineSpecification spec) {
        this.engineSpec = spec;
        return this;
    }

    /**
     * Sets the body specification.
     */
    public VehicleBuilder withBody(BodySpecification spec) {
        this.bodySpec = spec;
        return this;
    }

    /**
     * Sets the wheel specification.
     */
    public VehicleBuilder withWheels(WheelSpecification spec) {
        this.wheelSpec = spec;
        return this;
    }

    /**
     * Sets the fuel tank specification.
     */
    public VehicleBuilder withFuelTank(FuelTankSpecification spec) {
        this.tankSpec = spec;
        return this;
    }

    /**
     * Sets the body color.
     */
    public VehicleBuilder withColor(int color) {
        this.bodyColor = color;
        return this;
    }

    /**
     * Sets the vehicle type identifier.
     */
    public VehicleBuilder withType(String typeId) {
        this.vehicleTypeId = typeId;
        return this;
    }

    /**
     * Adds inventory with specified slot count.
     */
    public VehicleBuilder withInventory(int slots) {
        this.inventorySlots = slots;
        return this;
    }

    /**
     * Sets the vehicle owner.
     */
    public VehicleBuilder withOwner(Player player) {
        this.owner = player;
        return this;
    }

    /**
     * Sets the license plate text.
     */
    public VehicleBuilder withLicensePlate(String plate) {
        this.licensePlate = plate;
        return this;
    }

    /**
     * Enables/disables battery component.
     */
    public VehicleBuilder withBattery(boolean enabled) {
        this.withBattery = enabled;
        return this;
    }

    /**
     * Enables/disables temperature component.
     */
    public VehicleBuilder withTemperature(boolean enabled) {
        this.withTemperature = enabled;
        return this;
    }

    /**
     * Enables/disables durability component.
     */
    public VehicleBuilder withDurability(boolean enabled) {
        this.withDurability = enabled;
        return this;
    }

    /**
     * Adds a custom component.
     */
    public VehicleBuilder withComponent(IVehicleComponent component) {
        this.components.add(component);
        return this;
    }

    /**
     * Builds and returns the configured vehicle.
     */
    public VehicleEntity build() {
        // Create entity (would need proper entity type registration)
        VehicleEntity vehicle = new VehicleEntity(null, world); // TODO: Use registered entity type
        vehicle.setVehicleType(vehicleTypeId);

        // Add core components
        EngineComponent engine = new EngineComponent(engineSpec);
        vehicle.addComponent(engine);

        BodyComponent body = new BodyComponent(bodySpec);
        body.setColor(bodyColor);
        vehicle.addComponent(body);

        WheelComponent wheels = new WheelComponent(wheelSpec);
        vehicle.addComponent(wheels);

        FuelTankComponent tank = new FuelTankComponent(tankSpec);
        vehicle.addComponent(tank);

        // Add control component
        ControlComponent controls = new ControlComponent();
        vehicle.addComponent(controls);

        // Add optional components
        if (withBattery) {
            vehicle.addComponent(new BatteryComponent());
        }

        if (withTemperature) {
            vehicle.addComponent(new TemperatureComponent());
        }

        if (withDurability) {
            float maxDurability = 100.0f * bodySpec.getWeight();
            vehicle.addComponent(new DurabilityComponent(maxDurability));
        }

        // Add ownership if owner specified
        if (owner != null) {
            OwnershipComponent ownership = new OwnershipComponent();
            ownership.setOwner(owner.getUUID(), owner.getName().getString());
            vehicle.addComponent(ownership);
        }

        // Add license plate if specified
        if (licensePlate != null && !licensePlate.isEmpty()) {
            LicensePlateComponent plate = new LicensePlateComponent(licensePlate);
            vehicle.addComponent(plate);
        }

        // Add inventory if specified
        if (inventorySlots > 0) {
            vehicle.addComponent(new InventoryComponent(inventorySlots));
        }

        // Add any custom components
        for (IVehicleComponent component : components) {
            vehicle.addComponent(component);
        }

        return vehicle;
    }

    /**
     * Builds the vehicle and spawns it in the world at the specified position.
     */
    public VehicleEntity buildAndSpawn(double x, double y, double z) {
        VehicleEntity vehicle = build();
        vehicle.setPos(x, y, z);
        world.addFreshEntity(vehicle);
        return vehicle;
    }
}
