package de.rolandsw.schedulemc.cannabis.items;

import de.rolandsw.schedulemc.cannabis.CannabisStrain;
import de.rolandsw.schedulemc.cannabis.CannabisQuality;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Getrocknete Cannabis-Blüten
 * Bereit zum Trimmen
 */
public class DriedBudItem extends Item {

    public DriedBudItem(Properties properties) {
        super(properties);
    }

    public static ItemStack create(CannabisStrain strain, CannabisQuality quality, int count) {
        ItemStack stack = new ItemStack(CannabisItems.DRIED_BUD.get(), count);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("Strain", strain.name());
        tag.putString("Quality", quality.name());
        tag.putInt("Weight", 1); // Jedes Item = 1g
        return stack;
    }

    public static CannabisStrain getStrain(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("Strain")) {
            try {
                return CannabisStrain.valueOf(stack.getTag().getString("Strain"));
            } catch (IllegalArgumentException e) {
                return CannabisStrain.HYBRID;
            }
        }
        return CannabisStrain.HYBRID;
    }

    public static CannabisQuality getQuality(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("Quality")) {
            try {
                return CannabisQuality.valueOf(stack.getTag().getString("Quality"));
            } catch (IllegalArgumentException e) {
                return CannabisQuality.MIDS;
            }
        }
        return CannabisQuality.MIDS;
    }

    public static int getWeight(ItemStack stack) {
        // Jedes Item = 1g, Gesamtgewicht = Stack-Count
        return 1;
    }

    @Override
    public Component getName(ItemStack stack) {
        CannabisStrain strain = getStrain(stack);
        return Component.literal("§e🍂 ")
            .append(Component.literal(strain.getColorCode()))
            .append(Component.translatable("item.dried_bud.name", strain.getDisplayName()));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CannabisStrain strain = getStrain(stack);
        CannabisQuality quality = getQuality(stack);
        int weight = getWeight(stack);

        tooltip.add(Component.translatable("tooltip.cannabis.strain_label").append(strain.getColoredName()));
        tooltip.add(Component.translatable("tooltip.quality.label").append(quality.getColoredName()));
        tooltip.add(Component.translatable("tooltip.cannabis.weight_label").append(Component.translatable("tooltip.cannabis.weight_value", (weight * stack.getCount()), stack.getCount())));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.dried_bud.ready_to_trim"));
        tooltip.add(Component.translatable("tooltip.dried_bud.use_station"));
    }
}
