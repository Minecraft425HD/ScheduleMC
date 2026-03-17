package de.rolandsw.schedulemc.weapon.melee;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.phys.Vec3;

public class BaseballBatItem extends MeleeWeaponItem {
    public BaseballBatItem() {
        super(Tiers.IRON, 4, -2.8F, new Properties().durability(250));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            Vec3 lookVec = player.getLookAngle();
            target.push(lookVec.x * 2.5, lookVec.y * 2.5 + 0.5, lookVec.z * 2.5);
            player.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.PLAYERS, 1.0F, 0.8F);
        }
        return super.hurtEnemy(stack, target, attacker);
    }
}
