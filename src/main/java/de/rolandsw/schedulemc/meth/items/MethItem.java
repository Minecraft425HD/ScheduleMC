package de.rolandsw.schedulemc.meth.items;

import de.rolandsw.schedulemc.meth.MethQuality;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Fertiges Meth - Endprodukt aus Vakuum-Trockner
 * Kann verpackt und verkauft werden
 */
public class MethItem extends Item {

    public MethItem() {
        super(new Properties()
                .stacksTo(20));
    }

    /**
     * Erstellt ItemStack mit Qualität
     */
    public static ItemStack create(MethQuality quality, int count) {
        ItemStack stack = new ItemStack(MethItems.METH.get(), count);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("Quality", quality.name());
        return stack;
    }

    /**
     * Liest Qualität aus ItemStack
     */
    public static MethQuality getQuality(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Quality")) {
            try {
                return MethQuality.valueOf(tag.getString("Quality"));
            } catch (IllegalArgumentException e) {
                return MethQuality.SCHLECHT;
            }
        }
        return MethQuality.SCHLECHT;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        MethQuality quality = getQuality(stack);

        tooltip.add(Component.translatable("tooltip.quality.label").append(quality.getColoredName()));
        tooltip.add(Component.translatable("tooltip.meth.purity").append(Component.translatable("tooltip.meth.purity_value", getPurityPercent(quality))));
        tooltip.add(Component.translatable("tooltip.meth.weight").append(Component.translatable("tooltip.meth.weight_value", stack.getCount(), stack.getCount())));
        tooltip.add(Component.literal(""));

        String colorDesc = switch (quality) {
            case SCHLECHT -> "tooltip.meth.white_crystals";
            case GUT -> "tooltip.meth.yellowish_crystals";
            case SEHR_GUT -> "tooltip.meth.yellowish_crystals";
            case LEGENDAER -> "tooltip.meth.blue_crystals";
        };
        tooltip.add(Component.translatable(colorDesc));
        tooltip.add(Component.translatable("tooltip.meth.can_package"));

        if (quality == MethQuality.LEGENDAER) {
            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("tooltip.meth.say_my_name"));
        }
    }

    private int getPurityPercent(MethQuality quality) {
        return switch (quality) {
            case SCHLECHT -> 70 + ThreadLocalRandom.current().nextInt(10); // 70-79%
            case GUT -> 80 + ThreadLocalRandom.current().nextInt(10);      // 80-89%
            case SEHR_GUT -> 90 + ThreadLocalRandom.current().nextInt(6);  // 90-95%
            case LEGENDAER -> 96 + ThreadLocalRandom.current().nextInt(4); // 96-99%
        };
    }

    @Override
    public Component getName(ItemStack stack) {
        MethQuality quality = getQuality(stack);
        String key = switch (quality) {
            case SCHLECHT -> "item.schedulemc.meth.standard_name";
            case GUT -> "item.schedulemc.meth.gut_name";
            case SEHR_GUT -> "item.schedulemc.meth.gut_name";
            case LEGENDAER -> "item.schedulemc.meth.blue_sky_name";
        };
        return Component.translatable(key);
    }
}
