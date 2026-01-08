package de.rolandsw.schedulemc.coca.items;

import de.rolandsw.schedulemc.coca.CocaType;
import de.rolandsw.schedulemc.coca.CrackQuality;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Crack Rock - Gekochtes Kokain
 * Schneller Effekt, höhere Suchtgefahr
 */
public class CrackRockItem extends Item {

    public CrackRockItem() {
        super(new Properties().stacksTo(32));
    }

    public static ItemStack create(CocaType type, CrackQuality quality, int count) {
        ItemStack stack = new ItemStack(CocaItems.CRACK_ROCK.get(), count);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("CocaType", type.name());
        tag.putString("Quality", quality.name());
        tag.putInt("Weight", 1); // Jedes Item = 1g
        return stack;
    }

    public static CocaType getType(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("CocaType")) {
            try {
                return CocaType.valueOf(stack.getTag().getString("CocaType"));
            } catch (IllegalArgumentException e) {
                return CocaType.BOLIVIANISCH;
            }
        }
        return CocaType.BOLIVIANISCH;
    }

    public static CrackQuality getQuality(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("Quality")) {
            try {
                return CrackQuality.valueOf(stack.getTag().getString("Quality"));
            } catch (IllegalArgumentException e) {
                return CrackQuality.STANDARD;
            }
        }
        return CrackQuality.STANDARD;
    }

    public static int getWeight(ItemStack stack) {
        // Jedes Item = 1g, Gesamtgewicht = Stack-Count
        return 1;
    }

    public static double calculatePrice(ItemStack stack) {
        CocaType type = getType(stack);
        CrackQuality quality = getQuality(stack);

        // Crack ist pro Gramm günstiger als Kokain, aber schnellerer Umsatz
        // Preis pro Gramm * Anzahl Items
        double basePrice = type.getBasePrice() * 0.8;
        return basePrice * quality.getPriceMultiplier() * stack.getCount() / 10.0;
    }

    @Override
    public Component getName(ItemStack stack) {
        CocaType type = getType(stack);
        CrackQuality quality = getQuality(stack);

        String icon = quality == CrackQuality.FISHSCALE ? "§b💎" : "§f🪨";
        return Component.literal(icon + " ")
            .append(Component.literal(quality.getColorCode()))
            .append(Component.translatable("item.crack_rock.name", type.getDisplayName()));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CocaType type = getType(stack);
        CrackQuality quality = getQuality(stack);
        int weight = getWeight(stack);
        double price = calculatePrice(stack);

        tooltip.add(Component.translatable("tooltip.coca.type_label").append(type.getColoredName()));
        tooltip.add(Component.translatable("tooltip.quality.label").append(quality.getColoredName()));
        tooltip.add(Component.translatable("tooltip.coca.weight").append(Component.literal("§f" + (weight * stack.getCount()) + "g §8(" + stack.getCount() + "x 1g)")));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.crack.value").append(Component.literal("§f" + String.format("%.2f", price) + "€")));
        tooltip.add(Component.empty());

        if (quality == CrackQuality.FISHSCALE) {
            tooltip.add(Component.translatable("tooltip.crack.fishscale_shine"));
        }

        tooltip.add(Component.translatable("tooltip.crack.cooked_cocaine"));
    }
}
