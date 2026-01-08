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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tool zum Setzen von Polizeistationen und Patrouillenpunkten für Polizei-NPCs
 * - Linksklick auf Polizei-NPC: NPC auswählen (via AttackEntityEvent Handler)
 * - Rechtsklick auf Block (ohne Station): Polizeistation setzen
 * - Rechtsklick auf Block (mit Station): Patrouillenpunkt hinzufügen (max 16)
 * - Shift+Rechtsklick auf NPC: Info anzeigen & zurücksetzen
 * SICHERHEIT: Thread-safe Map für concurrent access von mehreren Spielern
 */
public class NPCPatrolTool extends Item {

    // SICHERHEIT: ConcurrentHashMap für Thread-safe Spieler-NPC-Mapping
    private static final Map<UUID, Integer> selectedNPCs = new ConcurrentHashMap<>();

    public NPCPatrolTool() {
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
                    Component.translatable("message.npc.no_police_selected")
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

            // Prüfe ob es ein Polizist ist
            if (npc.getNpcData().getNpcType() != NPCType.POLIZEI) {
                player.sendSystemMessage(
                    Component.translatable("message.npc.not_police")
                        .withStyle(ChatFormatting.RED)
                );
                return InteractionResult.FAIL;
            }

            // Prüfe max 16 Patrouillenpunkte
            int currentSize = npc.getNpcData().getPatrolPoints().size();
            if (currentSize >= 16) {
                player.sendSystemMessage(
                    Component.translatable("message.npc.patrol_max_reached")
                        .withStyle(ChatFormatting.RED)
                );
                return InteractionResult.FAIL;
            }

            // Wenn noch keine Station gesetzt ist, setze die Station UND füge als ersten Patrol Point hinzu
            if (npc.getNpcData().getPoliceStation() == null) {
                npc.getNpcData().setPoliceStation(clickedPos);
                npc.getNpcData().addPatrolPoint(clickedPos); // Station ist IMMER Patrouillenpunkt 1!
                player.sendSystemMessage(
                    Component.translatable("message.npc.police_station_set")
                        .withStyle(ChatFormatting.GREEN)
                        .append(Component.literal(npc.getNpcName())
                            .withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal(" bei " + clickedPos.toShortString())
                            .withStyle(ChatFormatting.WHITE))
                );
                player.sendSystemMessage(
                    Component.translatable("message.npc.patrol_first_point")
                        .withStyle(ChatFormatting.AQUA)
                );
                player.sendSystemMessage(
                    Component.translatable("message.npc.patrol_set_more_points")
                        .withStyle(ChatFormatting.GRAY)
                );
            } else {
                // Station existiert bereits - füge weiteren Patrouillenpunkt hinzu
                npc.getNpcData().addPatrolPoint(clickedPos);
                int newSize = npc.getNpcData().getPatrolPoints().size();
                player.sendSystemMessage(
                    Component.translatable("message.npc.patrol_point_set", newSize, clickedPos.toShortString())
                        .withStyle(ChatFormatting.GREEN)
                );
            }

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
            // Prüfe ob es ein Polizist ist
            if (npc.getNpcData().getNpcType() != NPCType.POLIZEI) {
                player.sendSystemMessage(
                    Component.translatable("message.npc.patrol_tool_police_only")
                        .withStyle(ChatFormatting.RED)
                );
                return InteractionResult.FAIL;
            }

            // Shift+Rechtsklick = Info anzeigen & Reset-Option
            if (player.isCrouching()) {
                showPatrolInfo(player, npc);
                return InteractionResult.SUCCESS;
            }

            // Normal-Klick = NPC auswählen
            selectedNPCs.put(player.getUUID(), npc.getId());

            player.sendSystemMessage(
                Component.translatable("message.npc.police_officer_prefix")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(npc.getNpcName())
                        .withStyle(ChatFormatting.YELLOW))
                    .append(Component.translatable("message.common.selected")
                        .withStyle(ChatFormatting.GREEN))
            );

            // Zeige aktuellen Status
            boolean hasStation = npc.getNpcData().getPoliceStation() != null;
            int patrolCount = npc.getNpcData().getPatrolPoints().size();

            if (hasStation) {
                player.sendSystemMessage(
                    Component.translatable("message.npc.station_label")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(npc.getNpcData().getPoliceStation().toShortString())
                            .withStyle(ChatFormatting.WHITE))
                        .append(Component.translatable("message.npc.patrol_count_suffix", patrolCount)
                            .withStyle(ChatFormatting.GRAY))
                );
            } else {
                player.sendSystemMessage(
                    Component.translatable("message.npc.right_click_set_police_station")
                        .withStyle(ChatFormatting.GRAY)
                );
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }

    /**
     * Zeigt detaillierte Patrol-Informationen an
     */
    private void showPatrolInfo(Player player, CustomNPCEntity npc) {
        player.sendSystemMessage(
            Component.literal("═══ Patrol-Info: " + npc.getNpcName() + " ═══")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
        );

        BlockPos station = npc.getNpcData().getPoliceStation();
        if (station != null) {
            player.sendSystemMessage(
                Component.translatable("message.npc.police_station_label")
                    .withStyle(ChatFormatting.AQUA)
                    .append(Component.literal(station.toShortString())
                        .withStyle(ChatFormatting.WHITE))
            );
        } else {
            player.sendSystemMessage(
                Component.translatable("message.npc.police_station_label")
                    .withStyle(ChatFormatting.AQUA)
                    .append(Component.literal("Nicht gesetzt")
                        .withStyle(ChatFormatting.RED))
            );
        }

        int patrolCount = npc.getNpcData().getPatrolPoints().size();
        player.sendSystemMessage(
            Component.translatable("message.npc.patrol_points_count", patrolCount)
                .withStyle(ChatFormatting.AQUA)
        );

        if (patrolCount > 0) {
            player.sendSystemMessage(
                Component.translatable("message.npc.points_label")
                    .withStyle(ChatFormatting.GRAY)
            );
            for (int i = 0; i < Math.min(patrolCount, 5); i++) {
                BlockPos point = npc.getNpcData().getPatrolPoints().get(i);
                player.sendSystemMessage(
                    Component.literal("  " + (i + 1) + ". " + point.toShortString())
                        .withStyle(ChatFormatting.WHITE)
                );
            }
            if (patrolCount > 5) {
                player.sendSystemMessage(
                    Component.literal("  ... und " + (patrolCount - 5) + " weitere")
                        .withStyle(ChatFormatting.GRAY)
                );
            }
        }

        player.sendSystemMessage(
            Component.translatable("message.npc.reset_patrol_hint")
                .withStyle(ChatFormatting.YELLOW)
        );
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
}
