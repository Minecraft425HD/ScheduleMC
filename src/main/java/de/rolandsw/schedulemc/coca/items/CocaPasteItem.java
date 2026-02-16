package de.rolandsw.schedulemc.coca.items;

import de.rolandsw.schedulemc.coca.CocaType;
import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Koka-Paste (braun) - Zwischenprodukt aus Extraktion
 * Muss im Raffinerie-Ofen verarbeitet werden
 */
public class CocaPasteItem extends Item {

    private final CocaType cocaType;

    public CocaPasteItem(CocaType type) {
        super(new Properties()
                .stacksTo(20));
        this.cocaType = type;
    }

    /**
     * Erstellt ItemStack mit Typ und Qualität
     */
    public static ItemStack create(CocaType type, TobaccoQuality quality, int count) {
        // Wähle das richtige Item basierend auf dem CocaType
        Item pasteItem = switch (type) {
            case BOLIVIANISCH -> CocaItems.COCA_PASTE_BOLIVIANISCH.get();
            case KOLUMBIANISCH -> CocaItems.COCA_PASTE_KOLUMBIANISCH.get();
            case PERUANISCH -> CocaItems.COCA_PASTE_PERUANISCH.get();
        };

        ItemStack stack = new ItemStack(pasteItem, count);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("CocaType", type.name());
        tag.putString("Quality", quality.name());
        return stack;
    }

    /**
     * Liest Koka-Sorte aus ItemStack
     */
    public static CocaType getType(ItemStack stack) {
        // Wenn es ein CocaPasteItem ist, verwende den gespeicherten Typ
        if (stack.getItem() instanceof CocaPasteItem pasteItem) {
            return pasteItem.cocaType;
        }

        // Fallback: Lese aus NBT
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("CocaType")) {
            try {
                return CocaType.valueOf(tag.getString("CocaType"));
            } catch (IllegalArgumentException ignored) {}
        }
        return CocaType.BOLIVIANISCH; // Default
    }

    /**
     * Liest Qualität aus ItemStack
     */
    public static TobaccoQuality getQuality(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Quality")) {
            try {
                return TobaccoQuality.valueOf(tag.getString("Quality"));
            } catch (IllegalArgumentException ignored) {}
        }
        return TobaccoQuality.GUT; // Default
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        CocaType type = getType(stack);
        TobaccoQuality quality = getQuality(stack);

        tooltip.add(Component.translatable("tooltip.coca.type_label").append(type.getColoredName()));
        tooltip.add(Component.translatable("tooltip.quality.label").append(quality.getColoredName()));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.coca_paste.brown_paste"));
        tooltip.add(Component.translatable("tooltip.coca_paste.must_refine"));
    }

    @Override
    public Component getName(ItemStack stack) {
        CocaType type = getType(stack);
        return Component.translatable("item.coca_paste.name", type.getDisplayName());
    }
}
