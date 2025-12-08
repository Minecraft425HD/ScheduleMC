package de.rolandsw.schedulemc.vehicle.builder;

import de.rolandsw.schedulemc.vehicle.component.body.BodySpecification;
import de.rolandsw.schedulemc.vehicle.component.engine.EngineSpecification;
import de.rolandsw.schedulemc.vehicle.component.fuel.FuelTankSpecification;
import de.rolandsw.schedulemc.vehicle.component.mobility.WheelSpecification;
import de.rolandsw.schedulemc.vehicle.core.entity.VehicleEntity;
import net.minecraft.world.level.Level;

/**
 * Predefined vehicle configurations for easy creation.
 * These are the "default" vehicles players can build/spawn.
 */
public class VehiclePresets {

    /**
     * Basic sedan - balanced all-rounder.
     */
    public static VehicleEntity createSedan(Level world) {
        return VehicleBuilder.create(world)
                .withType("sedan")
                .withEngine(EngineSpecification.INLINE_THREE)
                .withBody(BodySpecification.SEDAN)
                .withWheels(WheelSpecification.STANDARD)
                .withFuelTank(FuelTankSpecification.MEDIUM)
                .withInventory(27)
                .withColor(0xCCCCCC)
                .build();
    }

    /**
     * Sport car - high speed, low capacity.
     */
    public static VehicleEntity createSportCar(Level world) {
        return VehicleBuilder.create(world)
                .withType("sport")
                .withEngine(EngineSpecification.INLINE_SIX)
                .withBody(BodySpecification.SPORT)
                .withWheels(WheelSpecification.SPORT)
                .withFuelTank(FuelTankSpecification.SMALL)
                .withInventory(9)
                .withColor(0xFF0000)
                .build();
    }

    /**
     * SUV - more passengers, moderate power.
     */
    public static VehicleEntity createSUV(Level world) {
        return VehicleBuilder.create(world)
                .withType("suv")
                .withEngine(EngineSpecification.INLINE_SIX)
                .withBody(BodySpecification.SUV)
                .withWheels(WheelSpecification.OFFROAD)
                .withFuelTank(FuelTankSpecification.LARGE)
                .withInventory(54)
                .withColor(0x000000)
                .build();
    }

    /**
     * Truck - heavy cargo, powerful engine.
     */
    public static VehicleEntity createTruck(Level world) {
        return VehicleBuilder.create(world)
                .withType("truck")
                .withEngine(EngineSpecification.TRUCK_V8)
                .withBody(BodySpecification.TRUCK)
                .withWheels(WheelSpecification.TRUCK)
                .withFuelTank(FuelTankSpecification.TRUCK)
                .withInventory(54)
                .withColor(0x0000FF)
                .build();
    }

    /**
     * Transporter - balanced cargo vehicle.
     */
    public static VehicleEntity createTransporter(Level world) {
        return VehicleBuilder.create(world)
                .withType("transporter")
                .withEngine(EngineSpecification.INLINE_SIX)
                .withBody(BodySpecification.TRANSPORTER)
                .withWheels(WheelSpecification.STANDARD)
                .withFuelTank(FuelTankSpecification.LARGE)
                .withInventory(54)
                .withColor(0xFFFFFF)
                .build();
    }

    /**
     * Gets a preset by name.
     */
    public static VehicleEntity createPreset(Level world, String presetName) {
        return switch (presetName.toLowerCase()) {
            case "sedan" -> createSedan(world);
            case "sport" -> createSportCar(world);
            case "suv" -> createSUV(world);
            case "truck" -> createTruck(world);
            case "transporter" -> createTransporter(world);
            default -> createSedan(world); // Default fallback
        };
    }
}
