package de.rolandsw.schedulemc.npc.events;

import de.rolandsw.schedulemc.economy.StateAccount;
import de.rolandsw.schedulemc.npc.crime.ItemLegalityChecker;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;

/**
 * Beschlagnahmt bei der Verhaftung alle illegalen Gegenstände aus dem
 * Spielerinventar (auch aus mitgeführten Shulker-Kisten). Die Items werden
 * vernichtet; der Schätzwert wird der Staatskasse gutgeschrieben.
 */
public final class PoliceItemConfiscation {

    public record Result(int count, double value) {
        public boolean isEmpty() {
            return count == 0;
        }
    }

    private PoliceItemConfiscation() {
    }

    public static Result confiscate(ServerPlayer player) {
        int count = 0;
        double value = 0.0;
        var inv = player.getInventory();

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;

            if (ItemLegalityChecker.isIllegal(stack)) {
                count += stack.getCount();
                value += ItemLegalityChecker.estimateValue(stack);
                inv.setItem(i, ItemStack.EMPTY);
                continue;
            }

            // Shulker-Kiste: illegale Inhalte herausfiltern
            if (isShulker(stack)) {
                Result inner = purgeShulker(stack);
                count += inner.count();
                value += inner.value();
            }
        }

        if (count > 0 && player.getServer() != null) {
            StateAccount.getInstance(player.getServer())
                .deposit(value, "Confiscated illegal items");
            player.sendSystemMessage(Component.translatable(
                "event.police.items_confiscated", count, String.format("%.2f", value)));
        }
        return new Result(count, value);
    }

    private static boolean isShulker(ItemStack stack) {
        return stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof ShulkerBoxBlock;
    }

    /** Entfernt illegale Items aus dem Shulker-NBT, schreibt den Rest zurück. */
    private static Result purgeShulker(ItemStack shulker) {
        CompoundTag tag = shulker.getTag();
        if (tag == null || !tag.contains("BlockEntityTag")) return new Result(0, 0.0);
        CompoundTag bet = tag.getCompound("BlockEntityTag");
        if (!bet.contains("Items")) return new Result(0, 0.0);

        ListTag items = bet.getList("Items", 10);
        ListTag kept = new ListTag();
        int count = 0;
        double value = 0.0;
        for (int i = 0; i < items.size(); i++) {
            CompoundTag itemTag = items.getCompound(i);
            ItemStack contained = ItemStack.of(itemTag);
            if (!contained.isEmpty() && ItemLegalityChecker.isIllegal(contained)) {
                count += contained.getCount();
                value += ItemLegalityChecker.estimateValue(contained);
            } else {
                kept.add(itemTag);
            }
        }

        if (count > 0) {
            if (kept.isEmpty()) {
                bet.remove("Items");
                if (bet.isEmpty()) {
                    tag.remove("BlockEntityTag");
                }
            } else {
                bet.put("Items", kept);
            }
        }
        return new Result(count, value);
    }
}
