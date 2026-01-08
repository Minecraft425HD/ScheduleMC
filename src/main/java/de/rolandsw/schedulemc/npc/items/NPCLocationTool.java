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
 * Tool zum Setzen von Home- und Arbeitsstätte für NPCs
 * - Rechtsklick auf NPC: NPC auswählen
 * - Rechtsklick auf Block: Wohnort setzen
 * - Shift+Rechtsklick auf Block: Arbeitsort setzen (nur VERKAEUFER)
 * - Shift+Rechtsklick auf NPC: Info anzeigen (Locations und Zeiten)
 *
 * BEWOHNER arbeiten nicht und brauchen daher keinen Arbeitsort.
 * Verwende das NPCLeisureTool für Freizeitorte!
 * SICHERHEIT: Thread-safe Map für concurrent access von mehreren Spielern
 */
public class NPCLocationTool extends Item {

    // SICHERHEIT: ConcurrentHashMap für Thread-safe Spieler-NPC-Mapping
    private static final Map<UUID, Integer> selectedNPCs = new ConcurrentHashMap<>();

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

            // Shift+Rechtsklick = Arbeitsort (nur für VERKAEUFER)
            // Rechtsklick = Wohnort
            if (player.isCrouching()) {
                // Arbeitsort setzen
                if (npc.getNpcData().getNpcType() == NPCType.VERKAEUFER) {
                    npc.getNpcData().setWorkLocation(clickedPos);
                    player.sendSystemMessage(
                        Component.translatable("message.npc.work_location_set")
                            .withStyle(ChatFormatting.GREEN)
                            .append(Component.literal(npc.getNpcName())
                                .withStyle(ChatFormatting.YELLOW))
                            .append(Component.translatable("message.common.at_location", clickedPos.toShortString())
                                .withStyle(ChatFormatting.WHITE))
                    );
                } else if (npc.getNpcData().getNpcType() == NPCType.BEWOHNER) {
                    player.sendSystemMessage(
                        Component.translatable("message.npc.residents_no_workplace")
                            .withStyle(ChatFormatting.YELLOW)
                    );
                    player.sendSystemMessage(
                        Component.translatable("message.npc.use_leisure_tool")
                            .withStyle(ChatFormatting.GRAY)
                    );
                } else {
                    player.sendSystemMessage(
                        Component.translatable("message.npc.type_no_workplace")
                            .withStyle(ChatFormatting.RED)
                    );
                }
            } else {
                // Heimort setzen
                npc.getNpcData().setHomeLocation(clickedPos);
                player.sendSystemMessage(
                    Component.translatable("message.npc.home_set")
                        .withStyle(ChatFormatting.GREEN)
                        .append(Component.literal(npc.getNpcName())
                            .withStyle(ChatFormatting.YELLOW))
                        .append(Component.translatable("message.common.at_location", clickedPos.toShortString())
                            .withStyle(ChatFormatting.WHITE))
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
            // Shift+Rechtsklick = Info anzeigen
            if (player.isCrouching()) {
                showNPCInfo(player, npc);
                return InteractionResult.SUCCESS;
            }

            // Speichere NPC ID in der Map
            selectedNPCs.put(player.getUUID(), npc.getId());

            player.sendSystemMessage(
                Component.literal("NPC ")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(npc.getNpcName())
                        .withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(" (" + npc.getNpcData().getNpcType().getDisplayName() + ")")
                        .withStyle(ChatFormatting.GRAY))
                    .append(Component.translatable("message.common.selected")
                        .withStyle(ChatFormatting.GREEN))
            );

            // Unterschiedliche Hinweise je nach NPC-Typ
            if (npc.getNpcData().getNpcType() == NPCType.BEWOHNER) {
                player.sendSystemMessage(
                    Component.translatable("message.npc.right_click_set_home")
                        .withStyle(ChatFormatting.GRAY)
                );
                player.sendSystemMessage(
                    Component.translatable("message.npc.residents_no_work")
                        .withStyle(ChatFormatting.YELLOW)
                );
            } else if (npc.getNpcData().getNpcType() == NPCType.VERKAEUFER) {
                player.sendSystemMessage(
                    Component.translatable("message.npc.right_click_home_shift_work")
                        .withStyle(ChatFormatting.GRAY)
                );
            } else if (npc.getNpcData().getNpcType() == NPCType.POLIZEI) {
                player.sendSystemMessage(
                    Component.translatable("message.npc.police_use_patrol_tool")
                        .withStyle(ChatFormatting.RED)
                );
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }

    /**
     * Zeigt detaillierte NPC-Informationen an
     */
    private void showNPCInfo(Player player, CustomNPCEntity npc) {
        player.sendSystemMessage(
            Component.translatable("message.npc.info_header", npc.getNpcName())
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
        );

        player.sendSystemMessage(
            Component.translatable("message.common.type_label")
                .withStyle(ChatFormatting.AQUA)
                .append(Component.literal(npc.getNpcData().getNpcType().getDisplayName())
                    .withStyle(ChatFormatting.WHITE))
        );

        BlockPos home = npc.getNpcData().getHomeLocation();
        if (home != null) {
            player.sendSystemMessage(
                Component.translatable("message.npc.home_label")
                    .withStyle(ChatFormatting.AQUA)
                    .append(Component.literal(home.toShortString())
                        .withStyle(ChatFormatting.WHITE))
            );
        } else {
            player.sendSystemMessage(
                Component.translatable("message.npc.home_label")
                    .withStyle(ChatFormatting.AQUA)
                    .append(Component.translatable("message.common.not_set"))
                        .withStyle(ChatFormatting.RED))
            );
        }

        // Arbeitsort nur für Verkäufer anzeigen
        if (npc.getNpcData().getNpcType() == NPCType.VERKAEUFER) {
            BlockPos work = npc.getNpcData().getWorkLocation();
            if (work != null) {
                player.sendSystemMessage(
                    Component.translatable("message.npc.workplace_label")
                        .withStyle(ChatFormatting.AQUA)
                        .append(Component.literal(work.toShortString())
                            .withStyle(ChatFormatting.WHITE))
                );
            } else {
                player.sendSystemMessage(
                    Component.translatable("message.npc.workplace_label")
                        .withStyle(ChatFormatting.AQUA)
                        .append(Component.translatable("message.common.not_set"))
                            .withStyle(ChatFormatting.RED))
                );
            }

            // Zeige Arbeitszeiten
            long workStart = npc.getNpcData().getWorkStartTime();
            long workEnd = npc.getNpcData().getWorkEndTime();
            player.sendSystemMessage(
                Component.translatable("message.npc.work_hours_label")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(ticksToTime(workStart) + " - " + ticksToTime(workEnd))
                        .withStyle(ChatFormatting.WHITE))
            );
            // Zeige Heimzeit für Verkäufer
            long homeTime = npc.getNpcData().getHomeTime();
            player.sendSystemMessage(
                Component.translatable("message.npc.home_time_label")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("ab " + ticksToTime(homeTime))
                        .withStyle(ChatFormatting.WHITE))
            );
        } else if (npc.getNpcData().getNpcType() == NPCType.BEWOHNER) {
            player.sendSystemMessage(
                Component.translatable("message.npc.workplace_label")
                    .withStyle(ChatFormatting.AQUA)
                    .append(Component.translatable("message.npc.residents_dont_work"))
                        .withStyle(ChatFormatting.YELLOW))
            );
            int leisureCount = npc.getNpcData().getLeisureLocations().size();
            player.sendSystemMessage(
                Component.translatable("message.npc.leisure_locations_label")
                    .withStyle(ChatFormatting.AQUA)
                    .append(Component.literal(leisureCount + "/10")
                        .withStyle(ChatFormatting.WHITE))
            );

            // Zeige Heimzeit (Schlafenszeit) als Zeitbereich
            String homeStart = ticksToTime(npc.getNpcData().getHomeTime());
            String homeEnd = ticksToTime(npc.getNpcData().getWorkStartTime()); // Aufstehzeit
            player.sendSystemMessage(
                Component.translatable("message.npc.sleep_time_label")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(homeStart + " - " + homeEnd)
                        .withStyle(ChatFormatting.YELLOW))
            );

            // Zeige Freizeit als Zeitbereich
            player.sendSystemMessage(
                Component.translatable("message.npc.leisure_label")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(homeEnd + " - " + homeStart + " (Aktiv in der Stadt)")
                        .withStyle(ChatFormatting.GREEN))
            );
        } else {
            // Zeige Heimzeit für andere NPC-Typen
            long homeTime = npc.getNpcData().getHomeTime();
            player.sendSystemMessage(
                Component.translatable("message.npc.home_time_label")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("ab " + ticksToTime(homeTime))
                        .withStyle(ChatFormatting.WHITE))
            );
        }
    }

    /**
     * Konvertiert Minecraft-Ticks zu Uhrzeit-String
     */
    private String ticksToTime(long ticks) {
        // Minecraft: 0 = 6:00, 6000 = 12:00, 12000 = 18:00, 18000 = 0:00
        long totalMinutes = (ticks + 6000) % 24000; // Offset +6000 damit 0 = 6:00
        long hours = (totalMinutes / 1000) % 24;
        long minutes = (totalMinutes % 1000) * 60 / 1000;
        return String.format("%02d:%02d", hours, minutes);
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
