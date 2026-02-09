package de.rolandsw.schedulemc.tobacco.business;

import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
import de.rolandsw.schedulemc.tobacco.TobaccoType;
import net.minecraft.nbt.CompoundTag;

/**
 * Einzelner Tabak-Kauf (für History)
 */
public class Purchase {
    private final String playerUUID;
    private final TobaccoType type;
    private final TobaccoQuality quality;
    private final int weight;
    private final double price;
    private final long day;

    public Purchase(String playerUUID, TobaccoType type, TobaccoQuality quality, int weight, double price, long day) {
        this.playerUUID = playerUUID;
        this.type = type;
        this.quality = quality;
        this.weight = weight;
        this.price = price;
        this.day = day;
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("PlayerUUID", playerUUID);
        tag.putString("Type", type.name());
        tag.putString("Quality", quality.name());
        tag.putInt("Weight", weight);
        tag.putDouble("Price", price);
        tag.putLong("Day", day);
        return tag;
    }

    public static Purchase fromNBT(CompoundTag tag) {
        TobaccoType type;
        try { type = TobaccoType.valueOf(tag.getString("Type")); }
        catch (IllegalArgumentException e) { type = TobaccoType.values()[0]; }
        TobaccoQuality quality;
        try { quality = TobaccoQuality.valueOf(tag.getString("Quality")); }
        catch (IllegalArgumentException e) { quality = TobaccoQuality.values()[0]; }
        return new Purchase(
            tag.getString("PlayerUUID"),
            type,
            quality,
            tag.getInt("Weight"),
            tag.getDouble("Price"),
            tag.getLong("Day")
        );
    }

    // Getters
    public String getPlayerUUID() { return playerUUID; }
    public TobaccoType getType() { return type; }
    public TobaccoQuality getQuality() { return quality; }
    public int getWeight() { return weight; }
    public double getPrice() { return price; }
    public long getDay() { return day; }
}
