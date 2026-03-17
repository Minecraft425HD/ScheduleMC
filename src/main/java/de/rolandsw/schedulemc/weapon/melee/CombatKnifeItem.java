package de.rolandsw.schedulemc.weapon.melee;

import net.minecraft.world.item.Tiers;

public class CombatKnifeItem extends MeleeWeaponItem {
    public CombatKnifeItem() {
        super(Tiers.IRON, 3, -1.6F, new Properties().durability(200));
    }
}
