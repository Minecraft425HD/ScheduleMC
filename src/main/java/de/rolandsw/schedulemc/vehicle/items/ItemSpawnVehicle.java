package de.rolandsw.schedulemc.vehicle.items;

import de.rolandsw.schedulemc.vehicle.entity.vehicle.VehicleFactory;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.parts.Part;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.parts.PartRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ItemSpawnVehicle extends Item {

    private final Part bodyPart;

    public ItemSpawnVehicle(Part bodyPart) {
        super(new Item.Properties().stacksTo(1));
        this.bodyPart = bodyPart;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        if (world.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockPos pos = context.getClickedPos();
        Direction facing = context.getClickedFace();
        BlockPos spawnPos = pos.relative(facing);

        // Check if there's enough space to spawn the vehicle (2 blocks high)
        if (!world.getBlockState(spawnPos).isAir() || !world.getBlockState(spawnPos.above()).isAir()) {
            return InteractionResult.FAIL;
        }

        // Create the list of parts for the vehicle
        List<ItemStack> parts = new ArrayList<>();

        // Add body (chassis)
        parts.add(InternalVehiclePartItem.create(bodyPart));

        // Add engine (Normal Motor)
        parts.add(InternalVehiclePartItem.create(PartRegistry.NORMAL_MOTOR));

        // Add wheels - different bodies have different wheel requirements
        if (bodyPart == PartRegistry.OFFROAD_CHASSIS) {
            parts.add(new ItemStack(ModItems.OFFROAD_TIRE.get()));
            parts.add(new ItemStack(ModItems.OFFROAD_TIRE.get()));
            parts.add(new ItemStack(ModItems.OFFROAD_TIRE.get()));
            parts.add(new ItemStack(ModItems.OFFROAD_TIRE.get()));
        } else if (bodyPart == PartRegistry.TRUCK_CHASSIS) {
            parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
            parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
            parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
            parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
            parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
            parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
        } else {
            parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
            parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
            parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
            parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
        }

        // Add basic fender (not for trucks and sports cars)
        if (bodyPart != PartRegistry.TRUCK_CHASSIS && bodyPart != PartRegistry.LUXUS_CHASSIS) {
            parts.add(InternalVehiclePartItem.create(PartRegistry.FENDER_BASIC));
        }

        // Add 15L tank
        parts.add(InternalVehiclePartItem.create(PartRegistry.TANK_15L));

        // Add license plate (empty text, set by owner later)
        parts.add(InternalVehiclePartItem.createLicensePlate(""));

        // Add license plate holder
        parts.add(InternalVehiclePartItem.create(PartRegistry.LICENSE_PLATE_HOLDER));

        // Create the vehicle using the factory's static method
        EntityGenericVehicle vehicle = VehicleFactory.createVehicle(world, parts);

        if (vehicle == null) {
            return InteractionResult.FAIL;
        }

        // Set position and spawn
        vehicle.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        vehicle.setYRot(context.getPlayer() != null ? context.getPlayer().getYRot() : 0);
        vehicle.setFuelAmount(vehicle.getMaxFuel() / 4);
        vehicle.setBatteryLevel(500);

        // Set owner and vehicle UUID
        if (context.getPlayer() != null) {
            vehicle.setOwnerId(context.getPlayer().getUUID());
        }
        vehicle.setVehicleUUID(UUID.randomUUID());

        world.addFreshEntity(vehicle);
        vehicle.setIsSpawned(true);
        vehicle.initTemperature();

        // Consume the item
        if (context.getPlayer() != null && !context.getPlayer().isCreative()) {
            context.getItemInHand().shrink(1);
        }

        return InteractionResult.CONSUME;
    }
}
