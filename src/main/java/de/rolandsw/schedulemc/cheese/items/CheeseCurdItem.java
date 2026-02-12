package de.rolandsw.schedulemc.cheese.items;

import de.rolandsw.schedulemc.cheese.CheeseQuality;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Kasebruch (Curd) mit Quality in NBT
 */
public class CheeseCurdItem extends Item {
    public CheeseCurdItem(Properties properties) {
        super(properties);
    }

    /**
     * Erstellt ItemStack mit Quality-Daten
     */
    public static ItemStack create(CheeseQuality quality, int count) {
        ItemStack stack = new ItemStack(CheeseItems.CHEESE_CURD.get(), count);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("Quality", quality.name());
        return stack;
    }

    public static CheeseQuality getQuality(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Quality")) {
            try {
                return CheeseQuality.valueOf(tag.getString("Quality"));
            } catch (IllegalArgumentException e) {
                return CheeseQuality.SCHLECHT;
            }
        }
        return CheeseQuality.SCHLECHT;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        CheeseQuality quality = getQuality(stack);
        tooltip.add(Component.translatable("tooltip.cheese.quality", quality.getDisplayName()));
    }
}
