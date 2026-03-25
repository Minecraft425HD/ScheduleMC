package de.rolandsw.schedulemc.npc.data;

import net.minecraft.nbt.CompoundTag;

public class NPCShopData {
    private ShopInventory buyShop;
    private ShopInventory sellShop;

    public NPCShopData() {
        this.buyShop = new ShopInventory();
        this.sellShop = new ShopInventory();
    }

    public void save(CompoundTag tag) {
        tag.put("BuyShop", buyShop.save(new CompoundTag()));
        tag.put("SellShop", sellShop.save(new CompoundTag()));
    }

    public void load(CompoundTag tag) {
        buyShop = new ShopInventory();
        buyShop.load(tag.getCompound("BuyShop"));
        sellShop = new ShopInventory();
        sellShop.load(tag.getCompound("SellShop"));
    }

    public ShopInventory getBuyShop() { return buyShop; }
    public ShopInventory getSellShop() { return sellShop; }
}
