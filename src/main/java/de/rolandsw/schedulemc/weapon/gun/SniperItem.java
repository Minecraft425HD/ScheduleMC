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

    @Override
    protected float getConfigDamage() {
        return WeaponConfig.SNIPER_DAMAGE.get();
    }

    @Override
    protected double getConfigAccuracy() {
        return WeaponConfig.SNIPER_ACCURACY.get();
    }

    @Override
    protected int getConfigCooldown() {
        return WeaponConfig.SNIPER_COOLDOWN.get();
    }

    @Override
    protected int getConfigReloadTicks() {
        return WeaponConfig.SNIPER_RELOAD_TICKS.get();
    }
}
