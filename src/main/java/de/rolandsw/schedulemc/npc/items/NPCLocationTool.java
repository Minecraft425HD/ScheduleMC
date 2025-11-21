package de.rolandsw.schedulemc.npc.items;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Tool zum Setzen von Home- und Arbeitsstätte für NPCs
 * - Rechtsklick auf Block: Speichert Position (Normal: Home, Shift: Work)
 * - Rechtsklick auf NPC: Weist gespeicherte Position zu
 */
public class NPCLocationTool extends Item {

    private static final String TAG_STORED_POS = "StoredPos";
    private static final String TAG_MODE = "Mode"; // "home" oder "work"

    public NPCLocationTool() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        BlockPos clickedPos = context.getClickedPos();

        if (player == null) {
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide) {
            // Bestimme den Modus basierend auf Shift-Taste
            String mode = player.isShiftKeyDown() ? "work" : "home";

            // Speichere Position im Item NBT
            CompoundTag tag = stack.getOrCreateTag();
            tag.putLong(TAG_STORED_POS, clickedPos.asLong());
            tag.putString(TAG_MODE, mode);

            // Feedback an Spieler
            String modeText = mode.equals("home") ? "Wohnort" : "Arbeitsstätte";
            player.sendSystemMessage(
                Component.literal("Position gespeichert: " + modeText + " bei ")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(clickedPos.toShortString())
                        .withStyle(ChatFormatting.YELLOW))
            );
            player.sendSystemMessage(
                Component.literal("Rechtsklick auf einen NPC, um die Position zuzuweisen.")
                    .withStyle(ChatFormatting.GRAY)
            );

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, net.minecraft.world.entity.LivingEntity target, InteractionHand hand) {
        if (!(target instanceof CustomNPCEntity npc)) {
            return InteractionResult.PASS;
        }

        if (!player.level().isClientSide) {
            CompoundTag tag = stack.getOrCreateTag();

            if (!tag.contains(TAG_STORED_POS)) {
                player.sendSystemMessage(
                    Component.literal("Keine Position gespeichert! Rechtsklick auf einen Block.")
                        .withStyle(ChatFormatting.RED)
                );
                return InteractionResult.FAIL;
            }

            BlockPos storedPos = BlockPos.of(tag.getLong(TAG_STORED_POS));
            String mode = tag.getString(TAG_MODE);

            // Setze die Location im NPC
            if (mode.equals("home")) {
                npc.getNpcData().setHomeLocation(storedPos);
                player.sendSystemMessage(
                    Component.literal("Wohnort gesetzt für ")
                        .withStyle(ChatFormatting.GREEN)
                        .append(Component.literal(npc.getNpcName())
                            .withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal(" bei " + storedPos.toShortString())
                            .withStyle(ChatFormatting.WHITE))
                );
            } else {
                npc.getNpcData().setWorkLocation(storedPos);
                player.sendSystemMessage(
                    Component.literal("Arbeitsstätte gesetzt für ")
                        .withStyle(ChatFormatting.GREEN)
                        .append(Component.literal(npc.getNpcName())
                            .withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal(" bei " + storedPos.toShortString())
                            .withStyle(ChatFormatting.WHITE))
                );
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }
}
