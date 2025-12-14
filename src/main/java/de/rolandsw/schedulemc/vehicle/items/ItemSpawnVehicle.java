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

        // Add body
        parts.add(new ItemStack(ModItems.LIMOUSINE_CHASSIS.get())); // Placeholder, will be replaced

        // Determine which body to use based on the bodyPart
        if (bodyPart == PartRegistry.LIMOUSINE_CHASSIS) {
            parts.set(0, new ItemStack(ModItems.LIMOUSINE_CHASSIS.get()));
        } else if (bodyPart == PartRegistry.VAN_CHASSIS) {
            parts.set(0, new ItemStack(ModItems.VAN_CHASSIS.get()));
        } else if (bodyPart == PartRegistry.LKW_CHASSIS) {
            parts.set(0, new ItemStack(ModItems.LKW_CHASSIS.get()));
        } else if (bodyPart == PartRegistry.OFFROAD_CHASSIS) {
            parts.set(0, new ItemStack(ModItems.OFFROAD_CHASSIS.get()));
        } else if (bodyPart == PartRegistry.LUXUS_CHASSIS) {
            parts.set(0, new ItemStack(ModItems.LUXUS_CHASSIS.get()));
        }

        // Add engine (Normal Motor)
        parts.add(new ItemStack(ModItems.NORMAL_MOTOR.get()));

        // Add wheels - different bodies have different wheel requirements
        if (bodyPart == PartRegistry.OFFROAD_CHASSIS) {
            // Offroad chassis needs 4 offroad tires
            parts.add(new ItemStack(ModItems.OFFROAD_TIRE.get()));
            parts.add(new ItemStack(ModItems.OFFROAD_TIRE.get()));
            parts.add(new ItemStack(ModItems.OFFROAD_TIRE.get()));
            parts.add(new ItemStack(ModItems.OFFROAD_TIRE.get()));
        } else if (bodyPart == PartRegistry.LKW_CHASSIS) {
            // LKW chassis needs 6 standard tires
            parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
            parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
            parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
            parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
            parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
            parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
        } else {
            // Limousine, Van, and Luxus chassis need 4 standard tires
            parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
            parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
            parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
            parts.add(new ItemStack(ModItems.STANDARD_TIRE.get()));
        }

        // Add 15L tank
        parts.add(new ItemStack(ModItems.TANK_15L.get()));

        // Add license plate
        parts.add(new ItemStack(ModItems.LICENSE_PLATE.get()));

        // Add license plate holder
        parts.add(new ItemStack(ModItems.LICENSE_PLATE_HOLDER.get()));

        // Create the vehicle using the factory's static method
        EntityGenericVehicle vehicle = VehicleFactory.createVehicle(world, parts);

        if (vehicle == null) {
            return InteractionResult.FAIL;
        }

        // Set position and spawn
        vehicle.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        vehicle.setYRot(context.getPlayer() != null ? context.getPlayer().getYRot() : 0);
        vehicle.setFuelAmount(100);
        vehicle.setBatteryLevel(500);
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
