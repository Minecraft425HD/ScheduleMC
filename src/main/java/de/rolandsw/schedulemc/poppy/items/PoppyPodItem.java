package de.rolandsw.schedulemc.poppy.items;

import de.rolandsw.schedulemc.poppy.PoppyType;
import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Geerntete Mohnkapseln - werden zu Rohopium verarbeitet.
 * Je eine Variante pro Mohnsorte (Afghanisch, Indisch, Türkisch).
 */
public class PoppyPodItem extends Item {

    private final PoppyType poppyType;

    public PoppyPodItem(PoppyType type) {
        super(new Properties().stacksTo(64));
        this.poppyType = type;
    }

    public PoppyType getPoppyType() {
        return poppyType;
    }

    /**
     * Erstellt eine ItemStack der richtigen Sorten-Variante mit Qualität in NBT.
     */
    public static ItemStack create(PoppyType type, TobaccoQuality quality, int count) {
        Item pod = switch (type) {
            case AFGHANISCH -> PoppyItems.AFGHANISCH_POPPY_POD.get();
            case TUERKISCH  -> PoppyItems.TUERKISCH_POPPY_POD.get();
            case INDISCH    -> PoppyItems.INDISCH_POPPY_POD.get();
        };
        ItemStack stack = new ItemStack(pod, count);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("PoppyType", type.name());
        tag.putString("Quality", quality.name());
        return stack;
    }

    /**
     * Liest den PoppyType aus dem Item selbst (über die Instanz) oder aus NBT als Fallback.
     */
    public static PoppyType getType(ItemStack stack) {
        if (stack.getItem() instanceof PoppyPodItem pod) {
            return pod.poppyType;
        }
        if (stack.hasTag() && stack.getTag().contains("PoppyType")) {
            try {
                return PoppyType.valueOf(stack.getTag().getString("PoppyType"));
            } catch (IllegalArgumentException e) {
                return PoppyType.TUERKISCH;
            }
        }
        return PoppyType.TUERKISCH;
    }

    public static TobaccoQuality getQuality(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("Quality")) {
            try {
                return TobaccoQuality.valueOf(stack.getTag().getString("Quality"));
            } catch (IllegalArgumentException e) {
                return TobaccoQuality.SCHLECHT;
            }
        }
        return TobaccoQuality.SCHLECHT;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        TobaccoQuality quality = getQuality(stack);

        tooltip.add(Component.translatable("tooltip.poppy.type_label").append(poppyType.getColoredName()));
        tooltip.add(Component.translatable("tooltip.quality.label").append(quality.getColoredName()));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.poppy_pod.craft_knife"));
        tooltip.add(Component.translatable("tooltip.poppy_pod.to_opium"));
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal(poppyType.getColorCode())
            .append(Component.translatable("item.poppy_pod.name"));
    }
}
