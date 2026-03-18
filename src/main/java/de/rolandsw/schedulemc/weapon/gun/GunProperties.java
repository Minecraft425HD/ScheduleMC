package de.rolandsw.schedulemc.weapon.gun;

import net.minecraft.world.item.Item;

public class GunProperties {
    private final int durability;
    private final int baseDamage;
    private final double baseAccuracy;
    private final int baseCooldown;
    private final int maxAmmo;
    private final Item ammoType;
    private final boolean usesMagazines;
    private final int range;

    private GunProperties(Builder builder) {
        this.durability = builder.durability;
        this.baseDamage = builder.baseDamage;
        this.baseAccuracy = builder.baseAccuracy;
        this.baseCooldown = builder.baseCooldown;
        this.maxAmmo = builder.maxAmmo;
        this.ammoType = builder.ammoType;
        this.usesMagazines = builder.usesMagazines;
        this.range = builder.range;
    }

    public int getDurability() { return durability; }
    public int getBaseDamage() { return baseDamage; }
    public double getBaseAccuracy() { return baseAccuracy; }
    public int getBaseCooldown() { return baseCooldown; }
    public int getMaxAmmo() { return maxAmmo; }
    public Item getAmmoType() { return ammoType; }
    public boolean usesMagazines() { return usesMagazines; }
    public int getRange() { return range; }

    public static class Builder {
        private int durability = 300;
        private int baseDamage = 5;
        private double baseAccuracy = 0.8;
        private int baseCooldown = 10;
        private int maxAmmo = 30;
        private Item ammoType = null;
        private boolean usesMagazines = true;
        private int range = 100;

        public Builder durability(int val) { durability = val; return this; }
        public Builder damage(int val) { baseDamage = val; return this; }
        public Builder accuracy(double val) { baseAccuracy = val; return this; }
        public Builder cooldown(int val) { baseCooldown = val; return this; }
        public Builder maxAmmo(int val) { maxAmmo = val; return this; }
        public Builder ammoType(Item val) { ammoType = val; return this; }
        public Builder usesMagazines(boolean val) { usesMagazines = val; return this; }
        public Builder range(int val) { range = val; return this; }
        public GunProperties build() { return new GunProperties(this); }
    }
}
