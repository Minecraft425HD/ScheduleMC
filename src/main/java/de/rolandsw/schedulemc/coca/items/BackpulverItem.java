package de.rolandsw.schedulemc.coca.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Backpulver (Natriumbicarbonat) - Zutat für Crack
 */
public class BackpulverItem extends Item {

    public BackpulverItem() {
        super(new Properties().stacksTo(64));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§7Natriumbicarbonat"));
        tooltip.add(Component.empty());
        tooltip.add(Component.literal("§8Wird zum Kochen von Crack benötigt"));
    }
}
