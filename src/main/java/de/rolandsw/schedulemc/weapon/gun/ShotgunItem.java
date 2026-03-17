package de.rolandsw.schedulemc.weapon.gun;

import de.rolandsw.schedulemc.weapon.config.WeaponConfig;
import de.rolandsw.schedulemc.weapon.item.WeaponItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ShotgunItem extends GunItem {
    public ShotgunItem() {
        super(new GunProperties.Builder()
                .durability(600).damage(4).accuracy(0.6).cooldown(25)
                .maxAmmo(8).ammoType(WeaponItems.SHOTGUN_SHELLS.get()).usesMagazines(true).range(30).build());
    }

    @Override
    protected int getConfigRange() {
        return WeaponConfig.SHOTGUN_RANGE.get();
    }

    @Override
    public void shootProjectile(Level level, Player player, ItemStack gunStack) {
        for (int i = 0; i < 5; i++) {
            super.shootProjectile(level, player, gunStack);
        }
    }
}
