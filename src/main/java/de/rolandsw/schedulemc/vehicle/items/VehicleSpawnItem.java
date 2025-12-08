package de.rolandsw.schedulemc.vehicle.items;

import de.rolandsw.schedulemc.vehicle.builder.VehiclePresets;
import de.rolandsw.schedulemc.vehicle.core.entity.VehicleEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Item that spawns a vehicle when used on a block.
 */
public class VehicleSpawnItem extends Item {

    private final String vehicleType;

    public VehicleSpawnItem(String vehicleType, Properties properties) {
        super(properties);
        this.vehicleType = vehicleType;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();

        if (!world.isClientSide()) {
            BlockPos pos = context.getClickedPos();

            // Spawn vehicle slightly above the clicked block
            VehicleEntity vehicle = VehiclePresets.createPreset(world, vehicleType);
            vehicle.setPos(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);

            // Set owner if player used the item
            if (context.getPlayer() != null) {
                // Owner is set via OwnershipComponent if it exists
            }

            world.addFreshEntity(vehicle);

            // Consume item in survival mode
            if (!context.getPlayer().isCreative()) {
                context.getItemInHand().shrink(1);
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.CONSUME;
    }

    public String getVehicleType() {
        return vehicleType;
    }
}
