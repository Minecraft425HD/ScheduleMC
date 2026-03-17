package de.rolandsw.schedulemc.weapon.gun;

import de.rolandsw.schedulemc.weapon.config.WeaponConfig;
import de.rolandsw.schedulemc.weapon.item.WeaponItems;

public class RevolverItem extends GunItem {
    public RevolverItem() {
        super(new GunProperties.Builder()
                .durability(300).damage(10).accuracy(0.9).cooldown(15)
                .maxAmmo(6).ammoType(WeaponItems.PISTOL_MAGAZINE.get()).usesMagazines(true).range(80).build());
    }

    @Override
    protected int getConfigRange() {
        return WeaponConfig.REVOLVER_RANGE.get();
    }
}
