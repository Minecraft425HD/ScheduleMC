package de.rolandsw.schedulemc.tobacco.items;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Gießkanne - Zum Bewässern der Töpfe
 */
public class WateringCanItem extends Item {
    
    private static final int MAX_WATER = 1000; // Maximale Wasserkapazität
    
    public WateringCanItem() {
        super(new Properties()
                .stacksTo(1)
                .durability(0)); // Keine Haltbarkeit, nur Wasser-Level
    }
    
    /**
     * Erstellt volle Gießkanne
     */
    public static ItemStack createFull() {
        ItemStack stack = new ItemStack(TobaccoItems.WATERING_CAN.get());
        setWaterLevel(stack, MAX_WATER);
        return stack;
    }
    
    /**
     * Erstellt leere Gießkanne
     */
    public static ItemStack createEmpty() {
        ItemStack stack = new ItemStack(TobaccoItems.WATERING_CAN.get());
        setWaterLevel(stack, 0);
        return stack;
    }
    
    /**
     * Setzt Wasserlevel
     */
    public static void setWaterLevel(ItemStack stack, int level) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("WaterLevel", Math.max(0, Math.min(MAX_WATER, level)));
    }
    
    /**
     * Gibt Wasserlevel zurück
     */
    public static int getWaterLevel(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("WaterLevel")) {
            return tag.getInt("WaterLevel");
        }
        return 0;
    }
    
    /**
     * Prüft ob leer
     */
    public static boolean isEmpty(ItemStack stack) {
        return getWaterLevel(stack) <= 0;
    }
    
    /**
     * Prüft ob voll
     */
    public static boolean isFull(ItemStack stack) {
        return getWaterLevel(stack) >= MAX_WATER;
    }
    
    /**
     * Füllt Wasser auf
     */
    public static void fill(ItemStack stack, int amount) {
        int current = getWaterLevel(stack);
        setWaterLevel(stack, current + amount);
    }
    
    /**
     * Verbraucht Wasser
     */
    public static boolean consume(ItemStack stack, int amount) {
        int current = getWaterLevel(stack);
        if (current >= amount) {
            setWaterLevel(stack, current - amount);
            return true;
        }
        return false;
    }
    
    /**
     * Gibt Füllstand in Prozent zurück
     */
    public static float getPercentage(ItemStack stack) {
        return (float) getWaterLevel(stack) / MAX_WATER;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        int waterLevel = getWaterLevel(stack);
        float percentage = getPercentage(stack);
        
        String bar = createWaterBar(percentage);
        tooltip.add(Component.translatable("tooltip.watering_can.water").append(Component.literal(" " + bar + " §f" + waterLevel + "/" + MAX_WATER)));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.watering_can.right_click_pot"));
        tooltip.add(Component.translatable("tooltip.watering_can.right_click_sink"));
    }
    
    /**
     * Erstellt visuellen Wasser-Balken
     */
    private String createWaterBar(float percentage) {
        int filled = (int) (percentage * 10);
        int empty = 10 - filled;
        
        return "§b" + "▰".repeat(filled) + "§7" + "▱".repeat(empty);
    }
    
    @Override
    public Component getName(ItemStack stack) {
        if (isEmpty(stack)) {
            return Component.literal("§7Leere Gießkanne");
        } else if (isFull(stack)) {
            return Component.literal("§bVolle Gießkanne");
        } else {
            return Component.literal("§fGießkanne");
        }
    }
    
    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }
    
    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * getPercentage(stack));
    }
    
    @Override
    public int getBarColor(ItemStack stack) {
        return 0x3A8FFF; // Blau
    }
}
