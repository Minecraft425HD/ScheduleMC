package de.rolandsw.schedulemc.data;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

/**
 * Repräsentiert ein Item im Shop mit Kauf/Verkaufspreisen
 */
public class ShopItem {
    
    private final String itemId; // z.B. "minecraft:diamond"
    private double buyPrice;
    private double sellPrice;
    private boolean canBuy;
    private boolean canSell;
    private int stock; // -1 = unlimited
    
    public ShopItem(String itemId, double buyPrice, double sellPrice) {
        this.itemId = itemId;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.canBuy = true;
        this.canSell = true;
        this.stock = -1; // Unbegrenzt
    }
    
    // Getter
    public String getItemId() { return itemId; }
    public double getBuyPrice() { return buyPrice; }
    public double getSellPrice() { return sellPrice; }
    public boolean canBuy() { return canBuy; }
    public boolean canSell() { return canSell; }
    public int getStock() { return stock; }
    
    // Setter
    public void setBuyPrice(double price) { this.buyPrice = price; }
    public void setSellPrice(double price) { this.sellPrice = price; }
    public void setCanBuy(boolean can) { this.canBuy = can; }
    public void setCanSell(boolean can) { this.canSell = can; }
    public void setStock(int stock) { this.stock = stock; }
    
    /**
     * Gibt das Minecraft-Item zurück
     */
    public Item getItem() {
        try {
            return BuiltInRegistries.ITEM.get(new ResourceLocation(itemId));
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Gibt einen ItemStack zurück
     */
    public ItemStack getItemStack(int amount) {
        Item item = getItem();
        if (item == null) return ItemStack.EMPTY;
        return new ItemStack(item, amount);
    }
    
    /**
     * Prüft ob Item verfügbar ist
     */
    public boolean isAvailable() {
        return getItem() != null;
    }
    
    /**
     * Prüft ob genug auf Lager ist
     */
    public boolean hasStock(int amount) {
        if (stock == -1) return true; // Unbegrenzt
        return stock >= amount;
    }
    
    /**
     * Reduziert Lagerbestand
     */
    public void reduceStock(int amount) {
        if (stock == -1) return; // Unbegrenzt
        stock -= amount;
        if (stock < 0) stock = 0;
    }
    
    /**
     * Erhöht Lagerbestand
     */
    public void increaseStock(int amount) {
        if (stock == -1) return; // Unbegrenzt
        stock += amount;
    }
    
    /**
     * Gibt Anzeigename des Items zurück
     */
    public String getDisplayName() {
        Item item = getItem();
        if (item == null) return itemId;
        return item.getDescription().getString();
    }
    
    /**
     * Berechnet Gesamtpreis für Kauf
     */
    public double getTotalBuyPrice(int amount) {
        return buyPrice * amount;
    }
    
    /**
     * Berechnet Gesamtpreis für Verkauf
     */
    public double getTotalSellPrice(int amount) {
        return sellPrice * amount;
    }
    
    @Override
    public String toString() {
        return String.format("ShopItem[%s, Buy=%.2f€, Sell=%.2f€, Stock=%d]",
            itemId, buyPrice, sellPrice, stock);
    }
}
