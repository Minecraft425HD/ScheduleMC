package de.rolandsw.schedulemc.vehicle.items;

import de.rolandsw.schedulemc.vehicle.core.entity.VehicleEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Wrench for modifying and customizing vehicles.
 * Opens a GUI to change components, colors, etc.
 */
public class WrenchItem extends Item {

    public WrenchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!world.isClientSide() && player.getVehicle() instanceof VehicleEntity vehicle) {
            // TODO: Open vehicle customization GUI
            player.displayClientMessage(
                    Component.translatable("message.vehicle.wrench_used"), true);
            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.pass(stack);
    }
}
