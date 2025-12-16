package de.rolandsw.schedulemc.mushroom.items;

import de.rolandsw.schedulemc.mushroom.MushroomType;
import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Frische Pilze - müssen getrocknet werden
 */
public class FreshMushroomItem extends Item {

    private final MushroomType mushroomType;

    public FreshMushroomItem(MushroomType mushroomType) {
        super(new Properties().stacksTo(64));
        this.mushroomType = mushroomType;
    }

    public MushroomType getMushroomType() {
        return mushroomType;
    }

    /**
     * Erstellt frische Pilze mit Qualität und Menge
     */
    public static ItemStack create(MushroomType type, TobaccoQuality quality, int amount) {
        ItemStack stack = new ItemStack(getItemForType(type), amount);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("Quality", quality.name());
        tag.putString("MushroomType", type.name());
        return stack;
    }

    public static TobaccoQuality getQuality(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Quality")) {
            return TobaccoQuality.valueOf(tag.getString("Quality"));
        }
        return TobaccoQuality.GUT;
    }

    private static Item getItemForType(MushroomType type) {
        return switch (type) {
            case CUBENSIS -> MushroomItems.FRESH_CUBENSIS.get();
            case AZURESCENS -> MushroomItems.FRESH_AZURESCENS.get();
            case MEXICANA -> MushroomItems.FRESH_MEXICANA.get();
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        TobaccoQuality quality = getQuality(stack);
        tooltip.add(Component.literal("§7Sorte: " + mushroomType.getColoredName()));
        tooltip.add(Component.literal("§7Qualität: " + quality.getColoredName()));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§c⚠ Verderblich!"));
        tooltip.add(Component.literal("§8Zum Trockner bringen"));
    }

    @Override
    public Component getName(ItemStack stack) {
        TobaccoQuality quality = getQuality(stack);
        return Component.literal(mushroomType.getColorCode() + "Frische " + mushroomType.getDisplayName() + " §7[" + quality.getColoredName() + "§7]");
    }
}
