package de.rolandsw.schedulemc.vehicle.items;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.List;

public class ItemCanister extends Item {

    public ItemCanister() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        // Leerer Kanister ist nicht wiederauffüllbar (Einweg-System)
        return super.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        if (stack.hasTag()) {
            CompoundTag comp = stack.getTag();

            if (comp.contains("fuel")) {
                CompoundTag fuel = comp.getCompound("fuel");

                FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(fuel);
                if (fluidStack == null || fluidStack.isEmpty()) {
                    addInfo("-", 0, tooltip);
                    super.appendHoverText(stack, worldIn, tooltip, flagIn);
                    return;
                }

                addInfo(fluidStack.getDisplayName().getString(), fluidStack.getAmount(), tooltip);
                super.appendHoverText(stack, worldIn, tooltip, flagIn);
                return;
            }
            addInfo("-", 0, tooltip);
            super.appendHoverText(stack, worldIn, tooltip, flagIn);
            return;
        }
        addInfo("-", 0, tooltip);

        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    private void addInfo(String fluid, int amount, List<Component> tooltip) {
        tooltip.add(Component.translatable("canister.fluid", Component.literal(fluid).withStyle(ChatFormatting.DARK_GRAY)).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("canister.amount", Component.literal(String.valueOf(amount)).withStyle(ChatFormatting.DARK_GRAY)).withStyle(ChatFormatting.GRAY));
    }

}
