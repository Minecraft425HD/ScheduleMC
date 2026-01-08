package de.rolandsw.schedulemc.events;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.items.NPCLocationTool;
import de.rolandsw.schedulemc.npc.items.NPCLeisureTool;
import de.rolandsw.schedulemc.npc.items.NPCPatrolTool;
import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.region.PlotManager;
import de.rolandsw.schedulemc.region.PlotRegion;
import de.rolandsw.schedulemc.util.EventHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ScheduleMC 3.0 - Block-Schutz mit Trusted Players Support
 */
public class BlockProtectionHandler {

    private static final Logger LOGGER = LogUtils.getLogger();

    // NPC Work Location Cache (Performance-Optimierung)
    private static final Map<BlockPos, CustomNPCEntity> npcWorkLocationCache = new ConcurrentHashMap<>();
    private static long lastNPCCacheUpdate = 0;
    private static final long CACHE_DURATION_MS = 5000; // 5 Sekunden Cache

    /**
     * Verhindert das Abbauen von Blöcken in fremden Plots oder NPC-Arbeitsorten
     */
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        EventHelper.handleBlockBreak(event, player -> {
            BlockPos pos = event.getPos();

            // Prüfe ob Block ein NPC Arbeitsort ist
            if (isNPCWorkLocation(player, pos)) {
                event.setCanceled(true);
                return;
            }

            if (!checkPlotPermission(player, pos, "abbauen")) {
                event.setCanceled(true);
            }
        });
    }

    /**
     * Behandelt Linksklick auf Blöcke für das LocationTool (Arbeitsort setzen)
     */
    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        EventHelper.handleLeftClickBlock(event, player -> {
            ItemStack stack = player.getItemInHand(event.getHand());

            // Prüfe ob Spieler das LocationTool hält
            if (!(stack.getItem() instanceof NPCLocationTool)) {
                return;
            }

            BlockPos clickedPos = event.getPos();

            if (!player.level().isClientSide) {
            // Prüfe ob ein NPC ausgewählt wurde
            Integer npcId = NPCLocationTool.getSelectedNPC(player.getUUID());
            if (npcId == null) {
                player.sendSystemMessage(
                    Component.translatable("message.npc.no_npc_selected")
                        .withStyle(ChatFormatting.RED)
                );
                event.setCanceled(true);
                event.setUseBlock(Event.Result.DENY);
                event.setUseItem(Event.Result.DENY);
                return;
            }

            Entity entity = player.level().getEntity(npcId);

            if (!(entity instanceof CustomNPCEntity npc)) {
                player.sendSystemMessage(
                    Component.translatable("message.npc.selected_unavailable")
                        .withStyle(ChatFormatting.RED)
                );
                NPCLocationTool.clearSelectedNPC(player.getUUID());
                event.setCanceled(true);
                event.setUseBlock(Event.Result.DENY);
                event.setUseItem(Event.Result.DENY);
                return;
            }

            // Setze Arbeitsort nur für VERKAEUFER
            if (npc.getNpcData().getNpcType() == NPCType.VERKAEUFER) {
                npc.getNpcData().setWorkLocation(clickedPos);
                player.sendSystemMessage(
                    Component.translatable("message.npc.workplace_set")
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

            // Verhindere Block-Abbau
            event.setCanceled(true);
            event.setUseBlock(Event.Result.DENY);
            event.setUseItem(Event.Result.DENY);
            }
        });
    }

    /**
     * Behandelt Linksklick auf NPCs für LocationTool, LeisureTool und PatrolTool (NPC auswählen)
     */
    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent event) {
        EventHelper.handleAttackEntity(event, player -> {
            Entity target = event.getTarget();

            // Prüfe ob Spieler das LocationTool, LeisureTool oder PatrolTool hält
            ItemStack mainHandItem = player.getMainHandItem();
            ItemStack offHandItem = player.getOffhandItem();

            boolean holdsLocationTool = (mainHandItem.getItem() instanceof NPCLocationTool) ||
                                       (offHandItem.getItem() instanceof NPCLocationTool);
            boolean holdsLeisureTool = (mainHandItem.getItem() instanceof NPCLeisureTool) ||
                                      (offHandItem.getItem() instanceof NPCLeisureTool);
            boolean holdsPatrolTool = (mainHandItem.getItem() instanceof NPCPatrolTool) ||
                                     (offHandItem.getItem() instanceof NPCPatrolTool);

            if (!holdsLocationTool && !holdsLeisureTool && !holdsPatrolTool) {
                return;
            }

            // Prüfe ob Ziel ein CustomNPC ist
            if (!(target instanceof CustomNPCEntity npc)) {
                return;
            }

            if (!player.level().isClientSide) {
            // Handle LocationTool
            if (holdsLocationTool) {
                NPCLocationTool.setSelectedNPC(player.getUUID(), npc.getId());

                player.sendSystemMessage(
                    Component.translatable("message.common.npc_prefix")
                        .withStyle(ChatFormatting.GREEN)
                        .append(Component.literal(npc.getNpcName())
                            .withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal(" (").append(Component.literal(npc.getNpcData().getNpcType().getDisplayName())).append(Component.literal(")"))
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
            }
            // Handle LeisureTool
            else if (holdsLeisureTool) {
                // Prüfe ob NPC Freizeitorte haben kann
                if (npc.getNpcData().getNpcType() != NPCType.BEWOHNER
                    && npc.getNpcData().getNpcType() != NPCType.VERKAEUFER) {
                    player.sendSystemMessage(
                        Component.translatable("message.npc.type_no_leisure")
                            .withStyle(ChatFormatting.RED)
                    );
                } else {
                    NPCLeisureTool.setSelectedNPC(player.getUUID(), npc.getId());

                    player.sendSystemMessage(
                        Component.translatable("message.common.npc_prefix")
                            .withStyle(ChatFormatting.GREEN)
                            .append(Component.literal(npc.getNpcName())
                                .withStyle(ChatFormatting.YELLOW))
                            .append(Component.literal(" (").append(Component.literal(npc.getNpcData().getNpcType().getDisplayName())).append(Component.literal(")"))
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
                }
            }
            // Handle PatrolTool
            else if (holdsPatrolTool) {
                // Prüfe ob es ein Polizist ist
                if (npc.getNpcData().getNpcType() != NPCType.POLIZEI) {
                    player.sendSystemMessage(
                        Component.translatable("message.npc.patrol_tool_police_only")
                            .withStyle(ChatFormatting.RED)
                    );
                } else {
                    NPCPatrolTool.setSelectedNPC(player.getUUID(), npc.getId());

                    player.sendSystemMessage(
                        Component.literal("Polizist ")
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
                }
            }
            }

            // Verhindere Schaden am NPC
            event.setCanceled(true);
        });
    }

    /**
     * Aktualisiert den NPC Work Location Cache
     * PERFORMANCE: Wird nur alle 5 Sekunden ausgeführt statt bei jedem Block-Event
     */
    private static void updateNPCWorkLocationCache(Player player) {
        long now = System.currentTimeMillis();
        if (now - lastNPCCacheUpdate < CACHE_DURATION_MS) {
            return; // Cache ist noch gültig
        }

        npcWorkLocationCache.clear();

        // Suche alle NPCs einmalig
        WorldBorder border = player.level().getWorldBorder();
        AABB searchArea = new AABB(
            border.getMinX(), player.level().getMinBuildHeight(), border.getMinZ(),
            border.getMaxX(), player.level().getMaxBuildHeight(), border.getMaxZ()
        );
        List<CustomNPCEntity> npcs = player.level().getEntitiesOfClass(
            CustomNPCEntity.class,
            searchArea
        );

        // Befülle Cache
        for (CustomNPCEntity npc : npcs) {
            BlockPos workLocation = npc.getNpcData().getWorkLocation();
            if (workLocation != null) {
                npcWorkLocationCache.put(workLocation, npc);
            }
        }

        lastNPCCacheUpdate = now;
        LOGGER.debug("NPC Work Location Cache aktualisiert: {} Einträge", npcWorkLocationCache.size());
    }

    /**
     * Prüft ob eine Position ein NPC Arbeitsort ist und schützt sie
     * OPTIMIERT: Verwendet Cache statt bei jedem Event alle NPCs zu durchsuchen
     */
    private boolean isNPCWorkLocation(Player player, BlockPos pos) {
        // Admin darf Arbeitsorte abbauen
        if (player.hasPermissions(2)) {
            return false;
        }

        // Aktualisiere Cache falls nötig (max alle 5 Sekunden)
        updateNPCWorkLocationCache(player);

        // O(1) Lookup statt O(n) Suche!
        CustomNPCEntity npc = npcWorkLocationCache.get(pos);
        if (npc != null) {
            player.displayClientMessage(
                Component.translatable("message.protection.work_location_of")
                    .append(Component.literal(npc.getNpcName()).withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal("!")),
                true
            );
            return true;
        }

        return false;
    }

    /**
     * Verhindert das Platzieren von Blöcken in fremden Plots
     */
    @SubscribeEvent
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        EventHelper.handleBlockPlace(event, player -> {
            BlockPos pos = event.getPos();

            if (!checkPlotPermission(player, pos, "platzieren")) {
                event.setCanceled(true);
            }
        });
    }

    /**
     * Zentrale Berechtigungsprüfung
     *
     * OPTIMIERT: Nutzt Spatial Index für O(1) statt O(n) Plot-Lookup
     *
     * @param player Der Spieler
     * @param pos Die Position
     * @param action Die Aktion (für Fehlermeldung)
     * @return true wenn erlaubt, false wenn verboten
     */
    private boolean checkPlotPermission(Player player, BlockPos pos, String action) {
        UUID playerUUID = player.getUUID();

        // Admin hat IMMER Zugriff!
        if (player.hasPermissions(2)) {
            return true;
        }

        // OPTIMIERT: Direkter Lookup statt Iteration über alle Plots
        PlotRegion plot = PlotManager.getPlotAt(pos);

        // Nicht in einem Plot = erlaubt
        if (plot == null) {
            return true;
        }

        // Öffentlicher Plot: KEIN Bauen/Abbauen erlaubt!
        if (plot.isPublic()) {
            player.displayClientMessage(
                Component.translatable("event.protection.public_plot")
                    .withStyle(ChatFormatting.RED),
                true
            );
            return false;
        }

        // Plot hat keinen Besitzer = frei
        if (!plot.hasOwner()) {
            return true;
        }

        // Besitzer oder Trusted?
        if (plot.hasAccess(playerUUID)) {
            return true;
        }

        // Keine Berechtigung - zeige Fehlermeldung
        Component ownerInfo;
        if (plot.isRented()) {
            ownerInfo = Component.translatable("event.protection.plot_rented");
        } else {
            ownerInfo = Component.translatable("event.protection.plot_other_owner");
        }

        player.displayClientMessage(
            Component.translatable("event.protection.no_permission", action)
                .withStyle(ChatFormatting.RED)
                .append(Component.literal("\n"))
                .append(ownerInfo.withStyle(ChatFormatting.DARK_GRAY)),
            true
        );

        LOGGER.debug("Plot-Schutz: {} versuchte Block zu {} bei {} (Plot: {})",
            player.getName().getString(), action, pos, plot.getPlotName());

        return false;
    }

    /**
     * Erlaubt Interaktion in öffentlichen Plots (Truhen, GUIs, etc.)
     *
     * OPTIMIERT: Nutzt Spatial Index für schnellen Plot-Lookup
     */
    @SubscribeEvent
    public void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        EventHelper.handleRightClickBlock(event, player -> {
            BlockPos pos = event.getPos();

            // Admin darf immer!
            if (player.hasPermissions(2)) {
                return;
            }

            // OPTIMIERT: Direkter Lookup statt Iteration
            PlotRegion plot = PlotManager.getPlotAt(pos);

            // Nicht in einem Plot = erlaubt
            if (plot == null) {
                return;
            }

            // Öffentlicher Plot: Interaktion ERLAUBT!
            if (plot.isPublic()) {
                return;
            }

            // Privater Plot ohne Besitzer: Erlauben
            if (!plot.hasOwner()) {
                return;
            }

            // Privater Plot: Nur Besitzer + Trusted
            if (!plot.hasAccess(player.getUUID())) {
                event.setCanceled(true);
                player.displayClientMessage(
                    Component.translatable("event.protection.cannot_use")
                        .withStyle(ChatFormatting.RED),
                    true
                );
            }
        });
    }
}
