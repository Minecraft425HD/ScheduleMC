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
 * Espresso - stärkerer Effekt als normaler Kaffee
 */
public class EspressoItem extends Item {

    public EspressoItem() {
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
            // Speed II für 5 Minuten
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 6000, 1));
            // Haste II für 5 Minuten
            entity.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 6000, 1));
            // Regeneration I für 30 Sekunden
            entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 0));
        }
        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.espresso.description"));
        tooltip.add(Component.literal("§9+5:00 Speed II"));
        tooltip.add(Component.literal("§9+5:00 Haste II"));
        tooltip.add(Component.literal("§c+0:30 Regeneration I"));
    }
}
