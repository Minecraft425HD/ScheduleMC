package de.rolandsw.schedulemc.weapon.gun;

import de.rolandsw.schedulemc.weapon.config.WeaponConfig;
import de.rolandsw.schedulemc.weapon.item.WeaponItems;
import java.util.Set;

public class Ak47Item extends GunItem {
    public Ak47Item() {
        super(new GunProperties.Builder()
                .durability(1200).damage(8).accuracy(0.85).cooldown(3)
                .maxAmmo(30).ammoType(WeaponItems.AK47_MAGAZINE.get()).usesMagazines(true).range(120).build());
    }

    @Override
    protected int getConfigRange() {
        return WeaponConfig.AK47_RANGE.get();
    }

    @Override
    public Set<Integer> getCompatibleFireModes() {
        return Set.of(0, 2);
    }
}
