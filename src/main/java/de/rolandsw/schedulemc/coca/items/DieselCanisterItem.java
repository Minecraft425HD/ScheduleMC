package de.rolandsw.schedulemc.coca.items;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Diesel-Kanister für die Extraktionswanne
 * Kapazität: 1000 mB
 */
public class DieselCanisterItem extends Item {

    public static final int MAX_CAPACITY = 1000; // 1000 mB = 1 Liter

    public DieselCanisterItem() {
        super(new Properties()
                .stacksTo(1)); // Kanister nicht stapelbar
    }

    /**
     * Erstellt einen vollen Diesel-Kanister
     */
    public static ItemStack createFull() {
        ItemStack stack = new ItemStack(CocaItems.DIESEL_CANISTER.get(), 1);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("DieselAmount", MAX_CAPACITY);
        return stack;
    }

    /**
     * Erstellt einen leeren Diesel-Kanister
     */
    public static ItemStack createEmpty() {
        ItemStack stack = new ItemStack(CocaItems.DIESEL_CANISTER.get(), 1);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("DieselAmount", 0);
        return stack;
    }

    /**
     * Gibt die aktuelle Diesel-Menge zurück (in mB)
     */
    public static int getDieselAmount(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("DieselAmount")) {
            return tag.getInt("DieselAmount");
        }
        return MAX_CAPACITY; // Default: voll
    }

    /**
     * Setzt die Diesel-Menge
     */
    public static void setDieselAmount(ItemStack stack, int amount) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("DieselAmount", Math.max(0, Math.min(amount, MAX_CAPACITY)));
    }

    /**
     * Verbraucht Diesel aus dem Kanister
     * @return true wenn genug Diesel vorhanden war
     */
    public static boolean consumeDiesel(ItemStack stack, int amount) {
        int current = getDieselAmount(stack);
        if (current >= amount) {
            setDieselAmount(stack, current - amount);
            return true;
        }
        return false;
    }

    /**
     * Prüft ob der Kanister leer ist
     */
    public static boolean isEmpty(ItemStack stack) {
        return getDieselAmount(stack) <= 0;
    }

    /**
     * Prüft ob der Kanister voll ist
     */
    public static boolean isFull(ItemStack stack) {
        return getDieselAmount(stack) >= MAX_CAPACITY;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        int amount = getDieselAmount(stack);
        float percentage = (amount / (float) MAX_CAPACITY) * 100;

        tooltip.add(Component.literal("§7Inhalt: §e" + amount + " mB §7/ §f" + MAX_CAPACITY + " mB"));
        tooltip.add(Component.literal("§7Füllstand: §e" + String.format("%.0f", percentage) + "%"));
        tooltip.add(Component.literal(""));
        if (amount > 0) {
            tooltip.add(Component.literal("§aVerwendbar in Extraktionswanne"));
        } else {
            tooltip.add(Component.literal("§cLeer - muss aufgefüllt werden"));
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        int amount = getDieselAmount(stack);
        if (amount <= 0) {
            return Component.literal("Leerer Diesel-Kanister");
        } else if (amount >= MAX_CAPACITY) {
            return Component.literal("Voller Diesel-Kanister");
        }
        return Component.literal("Diesel-Kanister (" + amount + " mB)");
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        int amount = getDieselAmount(stack);
        return amount > 0 && amount < MAX_CAPACITY;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int amount = getDieselAmount(stack);
        return Math.round((amount / (float) MAX_CAPACITY) * 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xFFAA00; // Orange/Gelb für Diesel
    }
}
