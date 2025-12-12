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
        parts.add(new ItemStack(ModItems.OAK_BODY.get())); // Placeholder, will be replaced

        // Determine which body to use based on the bodyPart
        if (bodyPart == PartRegistry.OAK_BODY) {
            parts.set(0, new ItemStack(ModItems.OAK_BODY.get()));
        } else if (bodyPart == PartRegistry.BIG_OAK_BODY) {
            parts.set(0, new ItemStack(ModItems.BIG_OAK_BODY.get()));
        } else if (bodyPart == PartRegistry.WHITE_TRANSPORTER_BODY) {
            parts.set(0, new ItemStack(ModItems.WHITE_TRANSPORTER_BODY.get()));
        } else if (bodyPart == PartRegistry.WHITE_SUV_BODY) {
            parts.set(0, new ItemStack(ModItems.WHITE_SUV_BODY.get()));
        } else if (bodyPart == PartRegistry.WHITE_SPORT_BODY) {
            parts.set(0, new ItemStack(ModItems.WHITE_SPORT_BODY.get()));
        }

        // Add engine (3 Cylinder)
        parts.add(new ItemStack(ModItems.ENGINE_3_CYLINDER.get()));

        // Add wheels - different bodies have different wheel requirements
        if (bodyPart == PartRegistry.WHITE_SUV_BODY) {
            // SUV needs 4 big wheels
            parts.add(new ItemStack(ModItems.BIG_WHEEL.get()));
            parts.add(new ItemStack(ModItems.BIG_WHEEL.get()));
            parts.add(new ItemStack(ModItems.BIG_WHEEL.get()));
            parts.add(new ItemStack(ModItems.BIG_WHEEL.get()));
        } else if (bodyPart == PartRegistry.WHITE_TRANSPORTER_BODY) {
            // Transporter needs 6 regular wheels
            parts.add(new ItemStack(ModItems.WHEEL.get()));
            parts.add(new ItemStack(ModItems.WHEEL.get()));
            parts.add(new ItemStack(ModItems.WHEEL.get()));
            parts.add(new ItemStack(ModItems.WHEEL.get()));
            parts.add(new ItemStack(ModItems.WHEEL.get()));
            parts.add(new ItemStack(ModItems.WHEEL.get()));
        } else {
            // Oak, Big Oak, and Sport bodies need 4 regular wheels
            parts.add(new ItemStack(ModItems.WHEEL.get()));
            parts.add(new ItemStack(ModItems.WHEEL.get()));
            parts.add(new ItemStack(ModItems.WHEEL.get()));
            parts.add(new ItemStack(ModItems.WHEEL.get()));
        }

        // Add small tank
        parts.add(new ItemStack(ModItems.SMALL_TANK.get()));

        // Add license plate
        parts.add(new ItemStack(ModItems.LICENSE_PLATE.get()));

        // Add iron license plate holder
        parts.add(new ItemStack(ModItems.IRON_LICENSE_PLATE_HOLDER.get()));

        // Create the vehicle using the factory's static method
        EntityGenericVehicle vehicle = VehicleFactory.createCar(world, parts);

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
