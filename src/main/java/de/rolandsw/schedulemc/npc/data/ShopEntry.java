package de.rolandsw.schedulemc.npc.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class ShopEntry {
    private ItemStack item;
    private int price;
    private boolean unlimited; // True = unbegrenzt, False = Lagerware
    private int stock; // Aktueller Lagerbestand (nur relevant wenn unlimited = false)
    private int requiredLevel; // Mindest-ProducerLevel (0 = kein Limit)

    public ShopEntry() {
        this.item = ItemStack.EMPTY;
        this.price = 0;
        this.unlimited = true; // Default: unbegrenzt
        this.stock = 0;
        this.requiredLevel = 0;
    }

    public ShopEntry(ItemStack item, int price) {
        this.item = item.copy();
        this.price = price;
        this.unlimited = true;
        this.stock = 0;
        this.requiredLevel = 0;
    }

    public ShopEntry(ItemStack item, int price, boolean unlimited, int stock) {
        this.item = item.copy();
        this.price = price;
        this.unlimited = unlimited;
        this.stock = stock;
        this.requiredLevel = 0;
    }

    public ShopEntry(ItemStack item, int price, boolean unlimited, int stock, int requiredLevel) {
        this.item = item.copy();
        this.price = price;
        this.unlimited = unlimited;
        this.stock = stock;
        this.requiredLevel = requiredLevel;
    }

    public ItemStack getItem() {
        return item;
    }

    public int getPrice() {
        return price;
    }

    public boolean isUnlimited() {
        return unlimited;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public void setRequiredLevel(int requiredLevel) {
        this.requiredLevel = requiredLevel;
    }

    public void reduceStock(int amount) {
        if (!unlimited) {
            this.stock = Math.max(0, this.stock - amount);
        }
    }

    public boolean hasStock(int amount) {
        return unlimited || stock >= amount;
    }

    public CompoundTag save(CompoundTag tag) {
        tag.put("Item", item.save(new CompoundTag()));
        tag.putInt("Price", price);
        tag.putBoolean("Unlimited", unlimited);
        tag.putInt("Stock", stock);
        if (requiredLevel > 0) tag.putInt("RequiredLevel", requiredLevel);
        return tag;
    }

    public void load(CompoundTag tag) {
        item = ItemStack.of(tag.getCompound("Item"));
        price = tag.getInt("Price");
        unlimited = tag.getBoolean("Unlimited");
        stock = tag.getInt("Stock");
        requiredLevel = tag.contains("RequiredLevel") ? tag.getInt("RequiredLevel") : 0;
    }
}
