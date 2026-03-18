package de.rolandsw.schedulemc.weapon.upgrade;

import net.minecraft.world.item.Item;

public abstract class BaseFireModeUpgradeItem extends Item {

    public BaseFireModeUpgradeItem() {
        super(new Item.Properties().stacksTo(1));
    }

    public abstract FireModeUpgradeType getUpgradeType();
}
