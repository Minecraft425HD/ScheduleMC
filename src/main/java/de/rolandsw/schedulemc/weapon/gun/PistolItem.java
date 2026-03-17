package de.rolandsw.schedulemc.weapon.gun;

import de.rolandsw.schedulemc.weapon.config.WeaponConfig;
import de.rolandsw.schedulemc.weapon.item.WeaponItems;
import java.util.Set;

public class PistolItem extends GunItem {
    public PistolItem() {
        super(new GunProperties.Builder()
                .durability(400).damage(6).accuracy(0.95).cooldown(10)
                .maxAmmo(15).ammoType(WeaponItems.PISTOL_MAGAZINE.get()).usesMagazines(true).range(60).build());
    }

    @Override
    protected int getConfigRange() {
        return WeaponConfig.PISTOL_RANGE.get();
    }

    @Override
    public Set<Integer> getCompatibleFireModes() {
        return Set.of(0, 1);
    }
}
