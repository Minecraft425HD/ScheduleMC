package de.rolandsw.schedulemc.mushroom.items;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Mist-Sack-Item für Pilzzucht (3 Stufen)
 */
public class MistBagItem extends Item {

    private final MistBagType type;
    private static final int UNITS_PER_BAG = 1;

    public MistBagItem(MistBagType type) {
        super(new Properties().stacksTo(1));
        this.type = type;
    }

    public MistBagType getType() {
        return type;
    }

    /**
     * Gibt die Anzahl der Pilzkulturen zurück, für die dieser Mist-Sack reicht
     */
    public int getPlantsPerBag() {
        return type.getPlantsPerBag();
    }

    /**
     * Erstellt neuen vollen Mist-Sack
     */
    public static ItemStack createFull(MistBagType type) {
        ItemStack stack = new ItemStack(getItemForType(type));
        setUnits(stack, UNITS_PER_BAG);
        return stack;
    }

    /**
     * Setzt Einheiten im Sack
     */
    public static void setUnits(ItemStack stack, int units) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("MistUnits", Math.max(0, Math.min(UNITS_PER_BAG, units)));
    }

    /**
     * Gibt Einheiten zurück
     */
    public static int getUnits(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("MistUnits")) {
            return tag.getInt("MistUnits");
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
    private static Item getItemForType(MistBagType type) {
        return switch (type) {
            case SMALL -> MushroomItems.MIST_BAG_SMALL.get();
            case MEDIUM -> MushroomItems.MIST_BAG_MEDIUM.get();
            case LARGE -> MushroomItems.MIST_BAG_LARGE.get();
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        int units = getUnits(stack);
        float percentage = (float) units / UNITS_PER_BAG;

        tooltip.add(Component.translatable("tooltip.mist_bag.substrate", units, UNITS_PER_BAG));
        tooltip.add(Component.translatable("tooltip.mist_bag.enough_for", type.getPlantsPerBag()));
        tooltip.add(Component.literal(""));

        String bar = createBar(percentage);
        tooltip.add(Component.literal("§7" + bar));

        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.mist_bag.right_click_pot"));
        tooltip.add(Component.translatable("tooltip.mist_bag.ideal_for_mushrooms"));
    }

    private String createBar(float percentage) {
        int filled = (int) (percentage * 10);
        int empty = 10 - filled;
        return "§2" + "▰".repeat(Math.max(0, filled)) + "§8" + "▱".repeat(Math.max(0, empty));
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
        return 0x3D2817; // Dunkelbraun für Mist
    }
}

/**
 * Verschiedene Mist-Sack-Typen
 */
enum MistBagType {
    SMALL("Kleiner Mist-Sack", "§7", 1, 15.0),
    MEDIUM("Mittlerer Mist-Sack", "§2", 2, 35.0),
    LARGE("Großer Mist-Sack", "§6", 3, 60.0);

    private final String displayName;
    private final String color;
    private final int plantsPerBag;
    private final double basePrice;

    MistBagType(String displayName, String color, int plantsPerBag, double basePrice) {
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
