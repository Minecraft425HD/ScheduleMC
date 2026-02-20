package de.rolandsw.schedulemc.wine.items;

import de.rolandsw.schedulemc.wine.WineQuality;
import de.rolandsw.schedulemc.wine.WineType;
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
 * Weintrauben mit Quality und Type in NBT
 */
public class GrapeItem extends Item {
    private final WineType wineType;

    public GrapeItem(WineType wineType, Properties properties) {
        super(properties);
        this.wineType = wineType;
    }

    public WineType getWineType() {
        return wineType;
    }

    /**
     * Erstellt ItemStack mit Quality-Daten
     */
    public static ItemStack create(WineType type, WineQuality quality, int count) {
        ItemStack stack = switch (type) {
            case RIESLING -> new ItemStack(WineItems.RIESLING_GRAPES.get(), count);
            case SPAETBURGUNDER -> new ItemStack(WineItems.SPAETBURGUNDER_GRAPES.get(), count);
            case CHARDONNAY -> new ItemStack(WineItems.CHARDONNAY_GRAPES.get(), count);
            case MERLOT -> new ItemStack(WineItems.MERLOT_GRAPES.get(), count);
        };

        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("Quality", quality.name());
        return stack;
    }

    public static WineQuality getQuality(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Quality")) {
            try {
                return WineQuality.valueOf(tag.getString("Quality"));
            } catch (IllegalArgumentException e) {
                return WineQuality.SCHLECHT;
            }
        }
        return WineQuality.SCHLECHT;
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return Component.translatable("item.schedulemc.grapes.name");
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.grape.type", wineType.getDisplayName()));

        WineQuality quality = getQuality(stack);
        tooltip.add(Component.translatable("tooltip.grape.quality", quality.getDisplayName()));
    }
}
