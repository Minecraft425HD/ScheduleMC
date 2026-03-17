package de.rolandsw.schedulemc.weapon.melee;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public abstract class MeleeWeaponItem extends SwordItem {
    public MeleeWeaponItem(Tier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment == Enchantments.SHARPNESS ||
               enchantment == Enchantments.SMITE ||
               enchantment == Enchantments.BANE_OF_ARTHROPODS ||
               enchantment == Enchantments.KNOCKBACK ||
               enchantment == Enchantments.FIRE_ASPECT ||
               enchantment == Enchantments.MOB_LOOTING ||
               enchantment == Enchantments.UNBREAKING ||
               enchantment == Enchantments.MENDING;
    }
}
