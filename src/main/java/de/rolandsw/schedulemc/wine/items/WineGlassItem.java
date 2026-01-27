package de.rolandsw.schedulemc.wine.items;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Glas Wein zum Trinken
 *
 * Effekte:
 * - Regeneration I (2 Minuten)
 * - Nausea/Schwindel (30 Sekunden) - "Rausch"
 * - Resistance I (1 Minute)
 */
public class WineGlassItem extends Item {

    public WineGlassItem(Properties properties) {
        super(properties.food(new FoodProperties.Builder()
            .nutrition(1)
            .saturationMod(0.1f)
            .alwaysEat()
            .build()));
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level,
                                               @NotNull LivingEntity entity) {
        if (!level.isClientSide) {
            // Positive Effekte
            entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 2400, 0)); // 2 Minuten Regeneration I
            entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1200, 0)); // 1 Minute Resistance I

            // "Rausch"-Effekt
            entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 600, 0)); // 30 Sekunden Schwindel
        }

        return super.finishUsingItem(stack, level, entity);
    }
}
