package de.rolandsw.schedulemc.npc.items;

import de.rolandsw.schedulemc.npc.data.NPCType;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tool zum Setzen von Freizeitorten für NPCs (Bewohner & Verkäufer)
 * - Rechtsklick auf NPC: NPC auswählen
 * - Rechtsklick auf Block: Freizeitort hinzufügen (max 10)
 * - Shift+Rechtsklick auf NPC: Info anzeigen (alle Freizeitorte)
 * - Shift+Rechtsklick auf Block: Letzten Freizeitort entfernen
 *
 * Freizeitorte werden von Bewohnern den ganzen Tag über besucht (außer Heimzeit).
 * Verkäufer besuchen Freizeitorte nach Feierabend.
 * SICHERHEIT: Thread-safe Map für concurrent access von mehreren Spielern
 */
public class NPCLeisureTool extends Item {

    // SICHERHEIT: ConcurrentHashMap für Thread-safe Spieler-NPC-Mapping
    private static final Map<UUID, Integer> selectedNPCs = new ConcurrentHashMap<>();

    public NPCLeisureTool() {
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
                    Component.translatable("message.npc.no_npc_selected_click")
                        .withStyle(ChatFormatting.RED)
                );
                return InteractionResult.FAIL;
            }

            Entity entity = level.getEntity(npcId);

            if (!(entity instanceof CustomNPCEntity npc)) {
                player.sendSystemMessage(
                    Component.translatable("message.npc.selected_unavailable")
                        .withStyle(ChatFormatting.RED)
                );
                selectedNPCs.remove(player.getUUID());
                return InteractionResult.FAIL;
            }

            // Prüfe ob NPC Freizeitorte haben kann
            if (npc.getNpcData().getNpcType() != NPCType.BEWOHNER
                && npc.getNpcData().getNpcType() != NPCType.VERKAEUFER) {
                player.sendSystemMessage(
                    Component.translatable("message.npc.type_no_leisure")
                        .withStyle(ChatFormatting.RED)
                );
                return InteractionResult.FAIL;
            }

            // Shift+Rechtsklick = Letzten Freizeitort entfernen
            if (player.isCrouching()) {
                List<BlockPos> leisureLocations = npc.getNpcData().getLeisureLocations();
                if (leisureLocations.isEmpty()) {
                    player.sendSystemMessage(
                        Component.translatable("message.npc.no_leisure_available")
                            .withStyle(ChatFormatting.RED)
                    );
                } else {
                    int lastIndex = leisureLocations.size() - 1;
                    BlockPos removed = leisureLocations.get(lastIndex);
                    npc.getNpcData().removeLeisureLocation(lastIndex);
                    player.sendSystemMessage(
                        Component.translatable("message.npc.leisure_removed", (lastIndex + 1), removed.toShortString())
                            .withStyle(ChatFormatting.YELLOW)
                    );
                    player.sendSystemMessage(
                        Component.translatable("message.npc.remaining_locations", npc.getNpcData().getLeisureLocations().size())
                            .withStyle(ChatFormatting.GRAY)
                    );
                }
                return InteractionResult.SUCCESS;
            }

            // Normal-Rechtsklick = Freizeitort hinzufügen
            int currentSize = npc.getNpcData().getLeisureLocations().size();
            if (currentSize >= 10) {
                player.sendSystemMessage(
                    Component.translatable("message.npc.leisure_max_reached")
                        .withStyle(ChatFormatting.RED)
                );
                player.sendSystemMessage(
                    Component.translatable("message.npc.shift_right_click_remove")
                        .withStyle(ChatFormatting.GRAY)
                );
                return InteractionResult.FAIL;
            }

            npc.getNpcData().addLeisureLocation(clickedPos);
            int newSize = npc.getNpcData().getLeisureLocations().size();
            player.sendSystemMessage(
                Component.translatable("message.npc.leisure_set", newSize, clickedPos.toShortString())
                    .withStyle(ChatFormatting.GREEN)
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
            // Prüfe ob NPC Freizeitorte haben kann
            if (npc.getNpcData().getNpcType() != NPCType.BEWOHNER
                && npc.getNpcData().getNpcType() != NPCType.VERKAEUFER) {
                player.sendSystemMessage(
                    Component.translatable("message.npc.type_no_leisure")
                        .withStyle(ChatFormatting.RED)
                );
                return InteractionResult.FAIL;
            }

            // Shift+Rechtsklick = Info anzeigen
            if (player.isCrouching()) {
                showLeisureInfo(player, npc);
                return InteractionResult.SUCCESS;
            }

            // Normal-Klick = NPC auswählen
            selectedNPCs.put(player.getUUID(), npc.getId());

            player.sendSystemMessage(
                Component.translatable("message.npc.tool.select_prefix")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(npc.getNpcName())
                        .withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(" (" + npc.getNpcData().getNpcType().getDisplayName().getString() + ")")
                        .withStyle(ChatFormatting.GRAY))
                    .append(Component.translatable("message.common.selected")
                        .withStyle(ChatFormatting.GREEN))
            );

            // Zeige aktuellen Status
            int leisureCount = npc.getNpcData().getLeisureLocations().size();
            player.sendSystemMessage(
                Component.translatable("message.npc.leisure_count", leisureCount)
                    .withStyle(ChatFormatting.GRAY)
            );

            player.sendSystemMessage(
                Component.translatable("message.npc.right_click_add_leisure")
                    .withStyle(ChatFormatting.GRAY)
            );
            if (leisureCount > 0) {
                player.sendSystemMessage(
                    Component.translatable("message.npc.shift_right_click_remove_last")
                        .withStyle(ChatFormatting.GRAY)
                );
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }

    /**
     * Zeigt detaillierte Freizeitort-Informationen an
     */
    private void showLeisureInfo(Player player, CustomNPCEntity npc) {
        player.sendSystemMessage(
            Component.translatable("message.npc.tool.leisure_header", npc.getNpcName())
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
        );

        List<BlockPos> leisureLocations = npc.getNpcData().getLeisureLocations();
        int count = leisureLocations.size();

        player.sendSystemMessage(
            Component.translatable("message.npc.tool.count_detail", count)
                .withStyle(ChatFormatting.AQUA)
        );

        if (count > 0) {
            player.sendSystemMessage(
                Component.translatable("message.npc.tool.locations_label")
                    .withStyle(ChatFormatting.GRAY)
            );
            for (int i = 0; i < count; i++) {
                BlockPos location = leisureLocations.get(i);
                player.sendSystemMessage(
                    Component.literal("  " + (i + 1) + ". " + location.toShortString())
                        .withStyle(ChatFormatting.WHITE)
                );
            }
        } else {
            player.sendSystemMessage(
                Component.translatable("message.npc.no_leisure_set")
                    .withStyle(ChatFormatting.YELLOW)
            );
        }

        // Typ-spezifische Info
        if (npc.getNpcData().getNpcType() == NPCType.BEWOHNER) {
            player.sendSystemMessage(
                Component.translatable("message.npc.leisure_info_residents")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        } else if (npc.getNpcData().getNpcType() == NPCType.VERKAEUFER) {
            player.sendSystemMessage(
                Component.translatable("message.npc.leisure_info_sellers")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
    }

    /**
     * Gibt die ausgewählte NPC-ID für einen Spieler zurück
     */
    public static Integer getSelectedNPC(UUID playerUUID) {
        return selectedNPCs.get(playerUUID);
    }

    /**
     * Setzt den ausgewählten NPC für einen Spieler
     */
    public static void setSelectedNPC(UUID playerUUID, Integer npcId) {
        selectedNPCs.put(playerUUID, npcId);
    }

    /**
     * Entfernt die NPC-Auswahl für einen Spieler
     */
    public static void clearSelectedNPC(UUID playerUUID) {
        selectedNPCs.remove(playerUUID);
    }

    /**
     * Cleanup-Methode für Player Disconnect
     * Thread-safe cleanup der NPC-Auswahl für diesen Spieler
     */
    public static void cleanup(UUID playerUUID) {
        if (playerUUID == null) {
            return;
        }

        selectedNPCs.remove(playerUUID);
    }
}
