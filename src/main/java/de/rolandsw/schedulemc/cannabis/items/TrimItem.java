package de.rolandsw.schedulemc.cannabis.items;

import de.rolandsw.schedulemc.cannabis.CannabisStrain;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Trim - Blätterreste vom Trimmen
 * Kann zu Hash oder Öl verarbeitet werden
 */
public class TrimItem extends Item {

    public TrimItem(Properties properties) {
        super(properties);
    }

    public static ItemStack create(CannabisStrain strain, int count) {
        ItemStack stack = new ItemStack(CannabisItems.TRIM.get(), count);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("Strain", strain.name());
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

    public static int getWeight(ItemStack stack) {
        // Jedes Item = 1g, Gesamtgewicht = Stack-Count
        return 1;
    }

    @Override
    public Component getName(ItemStack stack) {
        CannabisStrain strain = getStrain(stack);
        return Component.literal("§7🍃 ")
            .append(Component.literal(strain.getColorCode()))
            .append(Component.literal(strain.getDisplayName()))
            .append(Component.translatable("item.trim.suffix"));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CannabisStrain strain = getStrain(stack);
        int weight = getWeight(stack);

        tooltip.add(Component.translatable("tooltip.cannabis.strain_label").append(strain.getColoredName()));
        tooltip.add(Component.translatable("tooltip.cannabis.weight_label").append(Component.literal("§f" + (weight * stack.getCount()) + "g §8(" + stack.getCount() + "x 1g)")));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.trim.leaf_remains"));
        tooltip.add(Component.translatable("tooltip.trim.to_hash"));
        tooltip.add(Component.translatable("tooltip.trim.to_oil"));
    }
}
