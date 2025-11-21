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
 * - Linksklick auf NPC: NPC auswählen
 * - Linksklick auf Block: Arbeitsort setzen für ausgewählten NPC
 * - Rechtsklick auf Block: Wohnort setzen für ausgewählten NPC
 */
public class NPCLocationTool extends Item {

    private static final String TAG_SELECTED_NPC = "SelectedNPC";

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
            CompoundTag tag = stack.getOrCreateTag();

            // Prüfe ob ein NPC ausgewählt wurde
            if (!tag.contains(TAG_SELECTED_NPC)) {
                player.sendSystemMessage(
                    Component.literal("Kein NPC ausgewählt! Linksklick auf einen NPC.")
                        .withStyle(ChatFormatting.RED)
                );
                return InteractionResult.FAIL;
            }

            int npcId = tag.getInt(TAG_SELECTED_NPC);
            Entity entity = level.getEntity(npcId);

            if (!(entity instanceof CustomNPCEntity npc)) {
                player.sendSystemMessage(
                    Component.literal("Ausgewählter NPC nicht mehr verfügbar!")
                        .withStyle(ChatFormatting.RED)
                );
                tag.remove(TAG_SELECTED_NPC);
                return InteractionResult.FAIL;
            }

            // Rechtsklick = Wohnort, Linksklick = Arbeitsort
            // Bei useOn ist es immer Rechtsklick auf einen Block
            npc.getNpcData().setHomeLocation(clickedPos);
            player.sendSystemMessage(
                Component.literal("Wohnort gesetzt für ")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(npc.getNpcName())
                        .withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(" bei " + clickedPos.toShortString())
                        .withStyle(ChatFormatting.WHITE))
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

            // Speichere NPC ID
            tag.putInt(TAG_SELECTED_NPC, npc.getId());

            player.sendSystemMessage(
                Component.literal("NPC ")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(npc.getNpcName())
                        .withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(" ausgewählt!")
                        .withStyle(ChatFormatting.GREEN))
            );
            player.sendSystemMessage(
                Component.literal("Linksklick auf Block = Arbeitsort | Rechtsklick = Wohnort")
                    .withStyle(ChatFormatting.GRAY)
            );

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }
}
