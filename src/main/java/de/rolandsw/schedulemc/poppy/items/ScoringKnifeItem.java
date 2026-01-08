package de.rolandsw.schedulemc.poppy.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Ritzmesser zum manuellen Ritzen von Mohnkapseln
 * Wird im Crafting-Grid mit Mohnkapseln kombiniert
 */
public class ScoringKnifeItem extends Item {

    public ScoringKnifeItem() {
        super(new Properties()
                .stacksTo(1)
                .durability(128)); // 128 Verwendungen
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.scoring_knife.for_scoring"));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.scoring_knife.usage_label"));
        tooltip.add(Component.translatable("tooltip.scoring_knife.craft_with_pod"));
        tooltip.add(Component.translatable("tooltip.scoring_knife.yields_opium"));
        tooltip.add(Component.literal(""));
        int durability = stack.getMaxDamage() - stack.getDamageValue();
        tooltip.add(Component.translatable("tooltip.scoring_knife.durability", durability, stack.getMaxDamage()));
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.setDamageValue(stack.getDamageValue() + 1);

        if (copy.getDamageValue() >= copy.getMaxDamage()) {
            return ItemStack.EMPTY; // Messer zerbricht
        }

        return copy;
    }
}
