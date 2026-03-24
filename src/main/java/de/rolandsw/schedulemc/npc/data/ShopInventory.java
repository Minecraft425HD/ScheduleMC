package de.rolandsw.schedulemc.npc.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ShopInventory {
    final private List<ShopEntry> entries;

    public ShopInventory() {
        this.entries = new ArrayList<>();
    }

    public void addEntry(ItemStack item, int price) {
        entries.add(new ShopEntry(item, price));
    }

    public void addEntry(ShopEntry entry) {
        entries.add(entry);
    }

    public void clear() {
        entries.clear();
    }

    public List<ShopEntry> getEntries() {
        return entries;
    }

    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (ShopEntry entry : entries) {
            list.add(entry.save(new CompoundTag()));
        }
        tag.put("Entries", list);
        return tag;
    }

    public void load(CompoundTag tag) {
        entries.clear();
        ListTag list = tag.getList("Entries", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            ShopEntry entry = new ShopEntry();
            entry.load(list.getCompound(i));
            entries.add(entry);
        }
    }
}
