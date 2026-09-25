package de.rolandsw.schedulemc.npc.crime.poster;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Item-Form des {@link WantedPosterBlock}. Trägt die Fahndungsdaten (Zielspieler,
 * Wanted-Level, Kopfgeld) als NBT, sodass sie beim Platzieren auf die
 * {@link WantedPosterBlockEntity} übernommen werden können.
 */
public class WantedPosterItem extends BlockItem {

    public static final String NBT_TARGET_UUID = "TargetUUID";
    public static final String NBT_TARGET_NAME = "TargetName";
    public static final String NBT_WANTED_LEVEL = "WantedLevel";
    public static final String NBT_BOUNTY_AMOUNT = "BountyAmount";

    public WantedPosterItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    /**
     * Erstellt einen fertig befüllten Fahndungsplakat-Stack für den angegebenen Spieler.
     */
    public static ItemStack create(UUID targetUUID, String targetName, int wantedLevel, double bountyAmount) {
        ItemStack stack = new ItemStack(WantedPosterRegistry.WANTED_POSTER_ITEM.get());
        CompoundTag tag = new CompoundTag();
        tag.putUUID(NBT_TARGET_UUID, targetUUID);
        tag.putString(NBT_TARGET_NAME, targetName);
        tag.putInt(NBT_WANTED_LEVEL, wantedLevel);
        tag.putDouble(NBT_BOUNTY_AMOUNT, bountyAmount);
        stack.setTag(tag);
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(NBT_TARGET_NAME)) {
            return;
        }
        tooltip.add(Component.literal("§7" + tag.getString(NBT_TARGET_NAME)));
        tooltip.add(Component.literal("§c" + "★".repeat(Math.max(0, tag.getInt(NBT_WANTED_LEVEL)))));
        tooltip.add(Component.literal("§a$" + String.format(Locale.ROOT, "%.2f", tag.getDouble(NBT_BOUNTY_AMOUNT))));
    }
}
