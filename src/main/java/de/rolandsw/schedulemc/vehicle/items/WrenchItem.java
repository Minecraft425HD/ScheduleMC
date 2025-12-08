package de.rolandsw.schedulemc.vehicle.items;

import de.rolandsw.schedulemc.vehicle.core.entity.VehicleEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Wrench for modifying and customizing vehicles.
 * Opens a GUI to change components, colors, etc.
 */
public class WrenchItem extends Item {

    public WrenchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player,
                                                 net.minecraft.world.entity.LivingEntity entity,
                                                 InteractionHand hand) {
        if (entity instanceof VehicleEntity vehicle) {
            if (!player.level().isClientSide()) {
                // TODO: Open vehicle customization GUI
                player.displayClientMessage(
                        Component.translatable("message.vehicle.wrench_used"), true);
            }

            return InteractionResult.SUCCESS;
        }

        return super.interactLivingEntity(stack, player, entity, hand);
    }
}
