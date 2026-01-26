package de.rolandsw.schedulemc.coffee.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Gebrühter Kaffee - trinkbar, gibt Speed-Buff
 */
public class BrewedCoffeeItem extends Item {

    public BrewedCoffeeItem() {
        super(new Properties().stacksTo(16).food(
            net.minecraft.world.food.FoodProperties.Builder.of()
                .nutrition(1)
                .saturationMod(0.1f)
                .alwaysEat()
                .build()
        ));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            // Speed I für 3 Minuten
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 3600, 0));
            // Haste I für 3 Minuten
            entity.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 3600, 0));
        }
        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.brewed_coffee.description"));
        tooltip.add(Component.literal("§9+3:00 Speed I"));
        tooltip.add(Component.literal("§9+3:00 Haste I"));
    }
}
