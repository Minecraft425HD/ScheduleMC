package de.rolandsw.schedulemc.vehicle.items;

import de.rolandsw.schedulemc.vehicle.component.attribute.DurabilityComponent;
import de.rolandsw.schedulemc.vehicle.core.component.ComponentType;
import de.rolandsw.schedulemc.vehicle.core.entity.VehicleEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Item for repairing damaged vehicles.
 */
public class RepairKitItem extends Item {

    private static final float REPAIR_AMOUNT = 25.0f;

    public RepairKitItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player,
                                                 net.minecraft.world.entity.LivingEntity entity,
                                                 InteractionHand hand) {
        if (entity instanceof VehicleEntity vehicle) {
            DurabilityComponent durability = vehicle.getComponent(
                    ComponentType.DURABILITY, DurabilityComponent.class);

            if (durability != null) {
                float currentDurability = durability.getCurrentDurability();
                float maxDurability = durability.getMaxDurability();

                if (currentDurability < maxDurability) {
                    durability.repair(REPAIR_AMOUNT);

                    player.displayClientMessage(
                            Component.translatable("message.vehicle.repaired",
                                    (int) durability.getDurabilityPercentage() * 100),
                            true);

                    if (!player.isCreative()) {
                        stack.shrink(1);
                    }

                    return InteractionResult.SUCCESS;
                } else {
                    player.displayClientMessage(
                            Component.translatable("message.vehicle.already_repaired"), true);
                    return InteractionResult.FAIL;
                }
            }
        }

        return super.interactLivingEntity(stack, player, entity, hand);
    }
}
