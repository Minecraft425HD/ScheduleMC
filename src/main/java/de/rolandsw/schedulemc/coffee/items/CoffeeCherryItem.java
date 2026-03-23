package de.rolandsw.schedulemc.coffee.items;

import de.rolandsw.schedulemc.coffee.CoffeeQuality;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Kaffeekirschen - frisch geerntet von Kaffeebäumen
 * Enthalten 2 Kaffeebohnen pro Kirsche
 */
public class CoffeeCherryItem extends Item {

    private static final String NBT_QUALITY = "Quality";

    public CoffeeCherryItem() {
        super(new Properties().stacksTo(64));
    }

    public static CoffeeQuality getQuality(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(NBT_QUALITY)) {
            try { return CoffeeQuality.valueOf(tag.getString(NBT_QUALITY)); }
            catch (IllegalArgumentException ignored) {}
        }
        return CoffeeQuality.GUT;
    }

    public static ItemStack withQuality(ItemStack stack, CoffeeQuality quality) {
        stack.getOrCreateTag().putString(NBT_QUALITY, quality.name());
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.coffee_cherry.description"));
        tooltip.add(Component.translatable("tooltip.coffee_cherry.process_hint"));
        tooltip.add(Component.translatable("tooltip.coffee_cherry.quality", getQuality(stack).getDisplayName()));
    }
}
