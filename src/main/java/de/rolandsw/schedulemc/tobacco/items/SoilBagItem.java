package de.rolandsw.schedulemc.tobacco.items;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Erdsack-Item mit verschiedenen Kapazitäten
 */
public class SoilBagItem extends Item {
    
    private final SoilBagType type;
    private static final int UNITS_PER_BAG = 1;
    
    public SoilBagItem(SoilBagType type) {
        super(new Properties()
                .stacksTo(1));
        this.type = type;
    }
    
    public SoilBagType getType() {
        return type;
    }

    /**
     * Gibt die Anzahl der Pflanzen zurück, für die dieser Erdsack reicht
     */
    public int getPlantsPerBag() {
        return type.getPlantsPerBag();
    }
    
    /**
     * Erstellt neuen vollen Erdsack
     */
    public static ItemStack createFull(SoilBagType type) {
        ItemStack stack = new ItemStack(getItemForType(type));
        setUnits(stack, UNITS_PER_BAG);
        return stack;
    }
    
    /**
     * Setzt Einheiten im Sack
     */
    public static void setUnits(ItemStack stack, int units) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("SoilUnits", Math.max(0, Math.min(UNITS_PER_BAG, units)));
    }
    
    /**
     * Gibt Einheiten zurück
     */
    public static int getUnits(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("SoilUnits")) {
            return tag.getInt("SoilUnits");
        }
        return UNITS_PER_BAG; // Neu = voll
    }
    
    /**
     * Verbraucht Einheiten
     */
    public static boolean consumeUnits(ItemStack stack, int amount) {
        int current = getUnits(stack);
        if (current >= amount) {
            setUnits(stack, current - amount);
            if (getUnits(stack) <= 0) {
                stack.shrink(1); // Leerer Sack verschwindet
            }
            return true;
        }
        return false;
    }
    
    /**
     * Gibt Item für Typ zurück
     */
    private static Item getItemForType(SoilBagType type) {
        return switch (type) {
            case SMALL -> TobaccoItems.SOIL_BAG_SMALL.get();
            case MEDIUM -> TobaccoItems.SOIL_BAG_MEDIUM.get();
            case LARGE -> TobaccoItems.SOIL_BAG_LARGE.get();
        };
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        int units = getUnits(stack);
        float percentage = (float) units / UNITS_PER_BAG;
        
        tooltip.add(Component.literal("§7Erde: §6" + units + "/" + UNITS_PER_BAG + " Einheiten"));
        tooltip.add(Component.literal("§7Reicht für: §e" + type.getPlantsPerBag() + " Pflanzen"));
        tooltip.add(Component.literal(""));
        
        String bar = createBar(percentage);
        tooltip.add(Component.literal("§7" + bar));
        
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§8Rechtsklick auf Topf zum Befüllen"));
    }
    
    private String createBar(float percentage) {
        int filled = (int) (percentage * 10);
        int empty = 10 - filled;
        return "§6" + "▰".repeat(Math.max(0, filled)) + "§8" + "▱".repeat(Math.max(0, empty));
    }
    
    @Override
    public Component getName(ItemStack stack) {
        int units = getUnits(stack);
        if (units <= 0) {
            return Component.literal("§7Leerer " + type.getDisplayName());
        } else if (units >= UNITS_PER_BAG) {
            return Component.literal(type.getColor() + "Voller " + type.getDisplayName());
        } else {
            return Component.literal(type.getColor() + type.getDisplayName());
        }
    }
    
    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }
    
    @Override
    public int getBarWidth(ItemStack stack) {
        float percentage = (float) getUnits(stack) / UNITS_PER_BAG;
        return Math.round(13.0F * percentage);
    }
    
    @Override
    public int getBarColor(ItemStack stack) {
        return 0x8B4513; // Braun für Erde
    }
}

/**
 * Verschiedene Erdsack-Typen
 */
enum SoilBagType {
    SMALL("Kleiner Erdsack", "§7", 1, 10.0),
    MEDIUM("Mittlerer Erdsack", "§e", 2, 25.0),
    LARGE("Großer Erdsack", "§6", 3, 50.0);
    
    private final String displayName;
    private final String color;
    private final int plantsPerBag;
    private final double basePrice;
    
    SoilBagType(String displayName, String color, int plantsPerBag, double basePrice) {
        this.displayName = displayName;
        this.color = color;
        this.plantsPerBag = plantsPerBag;
        this.basePrice = basePrice;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getColor() {
        return color;
    }
    
    public int getPlantsPerBag() {
        return plantsPerBag;
    }
    
    public double getBasePrice() {
        return basePrice;
    }
}
