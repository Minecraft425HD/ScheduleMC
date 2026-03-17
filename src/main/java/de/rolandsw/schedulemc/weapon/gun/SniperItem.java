package de.rolandsw.schedulemc.weapon.gun;

import de.rolandsw.schedulemc.weapon.config.WeaponConfig;
import de.rolandsw.schedulemc.weapon.item.WeaponItems;

public class SniperItem extends GunItem {
    public SniperItem() {
        super(new GunProperties.Builder()
                .durability(800).damage(20).accuracy(0.98).cooldown(40)
                .maxAmmo(5).ammoType(WeaponItems.SNIPER_MAGAZINE.get()).usesMagazines(true).range(400).build());
    }

    @Override
    protected int getConfigRange() {
        return WeaponConfig.SNIPER_RANGE.get();
    }
}
