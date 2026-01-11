package de.rolandsw.schedulemc.vehicle.items;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemLicensePlate extends ItemCraftingComponent {

    public ItemLicensePlate() {
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        String text = getText(stack);

        if (!text.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.license_plate_text", Component.literal(text).withStyle(ChatFormatting.DARK_GRAY)).withStyle(ChatFormatting.GRAY));
        }

        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    public static void setText(ItemStack stack, String text) {
        CompoundTag compound = stack.getOrCreateTag();

        compound.putString("plate_text", text);
    }

    @Nonnull
    public static String getText(ItemStack stack) {
        if (!stack.hasTag()) {
            return "";
        }
        CompoundTag compound = stack.getTag();
        if (!compound.contains("plate_text")) {
            return "";
        }
        return compound.getString("plate_text");
    }

}
