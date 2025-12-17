package de.rolandsw.schedulemc.meth.items;

import de.rolandsw.schedulemc.meth.MethQuality;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Kristall-Meth - Produkt aus Kristallisator
 * Wird im Vakuum-Trockner finalisiert
 */
public class KristallMethItem extends Item {

    public KristallMethItem() {
        super(new Properties()
                .stacksTo(16));
    }

    /**
     * Erstellt ItemStack mit Qualität
     */
    public static ItemStack create(MethQuality quality, int count) {
        ItemStack stack = new ItemStack(MethItems.KRISTALL_METH.get(), count);
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
                return MethQuality.STANDARD;
            }
        }
        return MethQuality.STANDARD;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        MethQuality quality = getQuality(stack);

        tooltip.add(Component.literal("§7Qualität: " + quality.getColoredName()));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§7Feuchte " + quality.getColorDescription().toLowerCase() + "e Kristalle"));
        tooltip.add(Component.literal("§8Nächster Schritt: Vakuum-Trockner"));
    }

    @Override
    public Component getName(ItemStack stack) {
        MethQuality quality = getQuality(stack);
        return Component.literal(quality.getColorCode() + "Kristall-Meth (feucht)");
    }
}
