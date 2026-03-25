package de.rolandsw.schedulemc.npc.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class ShopEntry {
    private ItemStack item;
    private int price;
    private boolean unlimited; // True = unbegrenzt, False = Lagerware
    private int stock; // Aktueller Lagerbestand (nur relevant wenn unlimited = false)

    public ShopEntry() {
        this.item = ItemStack.EMPTY;
        this.price = 0;
        this.unlimited = true; // Default: unbegrenzt
        this.stock = 0;
    }

    public ShopEntry(ItemStack item, int price) {
        this.item = item.copy();
        this.price = price;
        this.unlimited = true;
        this.stock = 0;
    }

    public ShopEntry(ItemStack item, int price, boolean unlimited, int stock) {
        this.item = item.copy();
        this.price = price;
        this.unlimited = unlimited;
        this.stock = stock;
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
        return tag;
    }

    public void load(CompoundTag tag) {
        item = ItemStack.of(tag.getCompound("Item"));
        price = tag.getInt("Price");
        unlimited = tag.getBoolean("Unlimited");
        stock = tag.getInt("Stock");
    }
}
