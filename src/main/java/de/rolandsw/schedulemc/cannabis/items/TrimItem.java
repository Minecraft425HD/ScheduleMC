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

    public static ItemStack create(CannabisStrain strain, int weight) {
        ItemStack stack = new ItemStack(CannabisItems.TRIM.get(), 1);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("Strain", strain.name());
        tag.putInt("Weight", weight);
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
        if (stack.hasTag()) {
            return stack.getTag().getInt("Weight");
        }
        return 5;
    }

    @Override
    public Component getName(ItemStack stack) {
        CannabisStrain strain = getStrain(stack);
        return Component.literal("§7🍃 " + strain.getColorCode() + strain.getDisplayName() + " Trim");
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CannabisStrain strain = getStrain(stack);
        int weight = getWeight(stack);

        tooltip.add(Component.literal("§7Sorte: " + strain.getColoredName()));
        tooltip.add(Component.literal("§7Gewicht: §f" + weight + "g"));
        tooltip.add(Component.empty());
        tooltip.add(Component.literal("§8Blätterreste vom Trimmen"));
        tooltip.add(Component.literal("§a→ Kann zu Hash gepresst werden"));
        tooltip.add(Component.literal("§a→ Kann zu Öl extrahiert werden"));
    }
}
