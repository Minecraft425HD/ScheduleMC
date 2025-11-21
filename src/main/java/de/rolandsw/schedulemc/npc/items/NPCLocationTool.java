package de.rolandsw.schedulemc.npc.items;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tool zum Setzen von Home- und Arbeitsstätte für NPCs
 * - Linksklick auf NPC: NPC auswählen
 * - Linksklick auf Block: Arbeitsort setzen für ausgewählten NPC
 * - Rechtsklick auf Block: Wohnort setzen für ausgewählten NPC
 */
public class NPCLocationTool extends Item {

    // Speichere ausgewählten NPC pro Spieler (UUID -> NPC Entity ID)
    private static final Map<UUID, Integer> selectedNPCs = new HashMap<>();

    public NPCLocationTool() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();

        if (player == null) {
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide) {
            // Prüfe ob ein NPC ausgewählt wurde
            Integer npcId = selectedNPCs.get(player.getUUID());
            if (npcId == null) {
                player.sendSystemMessage(
                    Component.literal("Kein NPC ausgewählt! Linksklick auf einen NPC.")
                        .withStyle(ChatFormatting.RED)
                );
                return InteractionResult.FAIL;
            }

            Entity entity = level.getEntity(npcId);

            if (!(entity instanceof CustomNPCEntity npc)) {
                player.sendSystemMessage(
                    Component.literal("Ausgewählter NPC nicht mehr verfügbar!")
                        .withStyle(ChatFormatting.RED)
                );
                selectedNPCs.remove(player.getUUID());
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
            // Speichere NPC ID in der Map
            selectedNPCs.put(player.getUUID(), npc.getId());

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

    /**
     * Gibt die ausgewählte NPC-ID für einen Spieler zurück
     */
    public static Integer getSelectedNPC(UUID playerUUID) {
        return selectedNPCs.get(playerUUID);
    }

    /**
     * Entfernt die NPC-Auswahl für einen Spieler
     */
    public static void clearSelectedNPC(UUID playerUUID) {
        selectedNPCs.remove(playerUUID);
    }
}
