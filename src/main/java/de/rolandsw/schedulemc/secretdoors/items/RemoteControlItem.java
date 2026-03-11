package de.rolandsw.schedulemc.secretdoors.items;

import de.rolandsw.schedulemc.secretdoors.SecretDoors;
import de.rolandsw.schedulemc.secretdoors.blockentity.HiddenSwitchBlockEntity;
import de.rolandsw.schedulemc.secretdoors.blockentity.SecretDoorBlockEntity;
import de.rolandsw.schedulemc.secretdoors.blocks.AbstractSecretDoorBlock;
import de.rolandsw.schedulemc.secretdoors.blocks.HiddenSwitchBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Fernbedienung (Remote Control) für Geheimtüren.
 *
 * Funktionen:
 * - Shift+Rechtsklick auf Tür/Schalter: Verknüpfen/Trennen
 * - Rechtsklick auf Luft: Toggle alle verknüpften Türen in Reichweite (64 Blöcke)
 * - Rechtsklick auf Tür (ohne Shift): Nur diese Tür toggeln
 * - Tooltip: Zeigt alle verknüpften Türen
 *
 * NBT-Struktur:
 * - "linked_doors": ListTag von CompoundTag mit {x, y, z}
 * - "linking_mode": boolean
 */
public class RemoteControlItem extends Item {

    public static final int MAX_LINKED_DOORS = 20;
    public static final int MAX_RANGE = 64;

    public RemoteControlItem(Properties props) {
        super(props);
    }

    // ─────────────────────────────────────────────────────────────────
    // Rechtsklick auf Block
    // ─────────────────────────────────────────────────────────────────

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        Player player = ctx.getPlayer();
        if (player == null) return InteractionResult.PASS;

        BlockPos pos = ctx.getClickedPos();
        BlockState state = level.getBlockState(pos);
        ItemStack stack = ctx.getItemInHand();

        // Klick auf Geheimtür
        if (state.getBlock() instanceof AbstractSecretDoorBlock) {
            if (player.isShiftKeyDown()) {
                // Shift+Klick: Tür verknüpfen/trennen
                boolean linked = toggleLink(stack, pos);
                if (linked) {
                    player.sendSystemMessage(Component.literal("§a[Fernbedienung] Tür bei §e"
                        + pos.toShortString() + "§a verknüpft. Gesamt: §e"
                        + getLinkedDoorCount(stack)));
                } else {
                    player.sendSystemMessage(Component.literal("§7[Fernbedienung] Tür bei §e"
                        + pos.toShortString() + "§7 getrennt."));
                }
            } else {
                // Normaler Klick: Nur diese Tür toggeln
                if (level.getBlockEntity(pos) instanceof SecretDoorBlockEntity be) {
                    be.toggle(level, player);
                }
            }
            return InteractionResult.SUCCESS;
        }

        // Klick auf versteckten Schalter
        if (state.getBlock() instanceof HiddenSwitchBlock) {
            if (player.isShiftKeyDown() && level.getBlockEntity(pos) instanceof HiddenSwitchBlockEntity be) {
                if (be.isLinkingMode()) {
                    // Schalter im Verknüpfungs-Modus: Fernbedienung verknüpft den Schalter
                    player.sendSystemMessage(Component.literal("§7[Fernbedienung] Schalter-Verknüpfungs-Modus beendet."));
                    be.setLinkingMode(false);
                    be.setChanged();
                } else {
                    // Schalter-Verknüpfungs-Modus starten
                    be.setLinkingMode(true);
                    be.setChanged();
                    player.sendSystemMessage(Component.literal("§a[Schalter] Verknüpfungs-Modus AN. Klicke auf eine Tür."));
                }
            }
            return InteractionResult.SUCCESS;
        }

        // Klick im Verknüpfungs-Modus eines Schalters?
        // Prüfe ob ein benachbarter Schalter im Verknüpfungs-Modus ist
        // (wird durch HiddenSwitchBlock.tryLinkDoor gehandelt)

        return InteractionResult.PASS;
    }

    // ─────────────────────────────────────────────────────────────────
    // Rechtsklick auf Luft: alle Türen toggeln
    // ─────────────────────────────────────────────────────────────────

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) return InteractionResultHolder.success(player.getItemInHand(hand));

        ItemStack stack = player.getItemInHand(hand);

        if (getLinkedDoorCount(stack) == 0) {
            player.sendSystemMessage(Component.literal(
                "§7[Fernbedienung] Keine Türen verknüpft. §eShift+Rechtsklick §7auf eine Geheimtür."));
            return InteractionResultHolder.success(stack);
        }

        int toggled = 0;
        List<long[]> doors = getLinkedDoorsRaw(stack);
        List<long[]> toRemove = new ArrayList<>();

        for (long[] entry : doors) {
            BlockPos doorPos = BlockPos.of(entry[0]);
            // Reichweiten-Prüfung
            if (player.distanceToSqr(doorPos.getX() + 0.5, doorPos.getY() + 0.5, doorPos.getZ() + 0.5)
                > (double) MAX_RANGE * MAX_RANGE) {
                continue;
            }
            BlockState doorState = level.getBlockState(doorPos);
            if (doorState.getBlock() instanceof AbstractSecretDoorBlock) {
                if (level.getBlockEntity(doorPos) instanceof SecretDoorBlockEntity be) {
                    be.toggle(level, player);
                    toggled++;
                }
            } else {
                // Verknüpfung entfernen wenn Block nicht mehr existiert
                toRemove.add(entry);
            }
        }

        // Verwaiste Verknüpfungen entfernen
        for (long[] entry : toRemove) {
            removeLinkedDoor(stack, BlockPos.of(entry[0]));
        }

        if (toggled > 0) {
            player.sendSystemMessage(Component.literal("§a[Fernbedienung] §e" + toggled + "§a Tür(en) geschaltet."));
        } else {
            player.sendSystemMessage(Component.literal("§7[Fernbedienung] Keine Türen in Reichweite (§e"
                + MAX_RANGE + " §7Blöcke)."));
        }

        return InteractionResultHolder.success(stack);
    }

    // ─────────────────────────────────────────────────────────────────
    // Tooltip
    // ─────────────────────────────────────────────────────────────────

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                 List<Component> tooltip, TooltipFlag flag) {
        int count = getLinkedDoorCount(stack);
        if (count == 0) {
            tooltip.add(Component.literal("§7Keine Türen verknüpft").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("§eShift+Rechtsklick §7auf Tür zum Verknüpfen").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.literal("§7Verknüpfte Türen: §e" + count + " §7/ §e" + MAX_LINKED_DOORS));
            List<long[]> doors = getLinkedDoorsRaw(stack);
            int shown = Math.min(doors.size(), 5);
            for (int i = 0; i < shown; i++) {
                BlockPos pos = BlockPos.of(doors.get(i)[0]);
                tooltip.add(Component.literal("  §8→ §7" + pos.toShortString()).withStyle(ChatFormatting.GRAY));
            }
            if (doors.size() > 5) {
                tooltip.add(Component.literal("  §8... und §7" + (doors.size() - 5) + "§8 mehr"));
            }
        }
        tooltip.add(Component.literal("§7Reichweite: §e" + MAX_RANGE + " §7Blöcke").withStyle(ChatFormatting.GRAY));
    }

    // ─────────────────────────────────────────────────────────────────
    // Statische Hilfsmethoden für NBT
    // ─────────────────────────────────────────────────────────────────

    /**
     * Verknüpft oder trennt eine Tür. Gibt true zurück wenn verknüpft, false wenn getrennt.
     */
    public static boolean toggleLink(ItemStack stack, BlockPos pos) {
        long encoded = pos.asLong();
        ListTag doorList = getDoorList(stack);

        // Prüfe ob bereits vorhanden → trennen
        for (int i = 0; i < doorList.size(); i++) {
            CompoundTag tag = doorList.getCompound(i);
            if (tag.getLong("pos") == encoded) {
                doorList.remove(i);
                saveDoorList(stack, doorList);
                return false; // Getrennt
            }
        }

        // Nicht vorhanden → verknüpfen (falls Max nicht erreicht)
        if (doorList.size() >= MAX_LINKED_DOORS) {
            return false;
        }

        CompoundTag newTag = new CompoundTag();
        newTag.putLong("pos", encoded);
        doorList.add(newTag);
        saveDoorList(stack, doorList);
        return true; // Verknüpft
    }

    public static void removeLinkedDoor(ItemStack stack, BlockPos pos) {
        long encoded = pos.asLong();
        ListTag doorList = getDoorList(stack);
        doorList.removeIf(tag -> tag instanceof CompoundTag ct && ct.getLong("pos") == encoded);
        saveDoorList(stack, doorList);
    }

    public static List<BlockPos> getLinkedDoors(ItemStack stack) {
        List<BlockPos> result = new ArrayList<>();
        for (long[] entry : getLinkedDoorsRaw(stack)) {
            result.add(BlockPos.of(entry[0]));
        }
        return result;
    }

    public static int getLinkedDoorCount(ItemStack stack) {
        return getDoorList(stack).size();
    }

    private static List<long[]> getLinkedDoorsRaw(ItemStack stack) {
        List<long[]> result = new ArrayList<>();
        ListTag doorList = getDoorList(stack);
        for (int i = 0; i < doorList.size(); i++) {
            CompoundTag tag = doorList.getCompound(i);
            result.add(new long[]{tag.getLong("pos")});
        }
        return result;
    }

    private static ListTag getDoorList(ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTag();
        if (!nbt.contains("linked_doors", Tag.TAG_LIST)) {
            nbt.put("linked_doors", new ListTag());
        }
        return nbt.getList("linked_doors", Tag.TAG_COMPOUND);
    }

    private static void saveDoorList(ItemStack stack, ListTag list) {
        stack.getOrCreateTag().put("linked_doors", list);
    }
}
