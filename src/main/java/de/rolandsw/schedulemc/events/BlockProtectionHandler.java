package de.rolandsw.schedulemc.events;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.items.NPCLocationTool;
import de.rolandsw.schedulemc.npc.items.NPCLeisureTool;
import de.rolandsw.schedulemc.npc.items.NPCPatrolTool;
import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.region.PlotManager;
import de.rolandsw.schedulemc.region.PlotRegion;
import de.rolandsw.schedulemc.region.PlotType;
import de.rolandsw.schedulemc.region.blocks.PlotBlocks;
import de.rolandsw.schedulemc.secretdoors.SecretDoors;
import de.rolandsw.schedulemc.secretdoors.mission.SecretDoorMissionAccessManager;
import de.rolandsw.schedulemc.util.EventHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ScheduleMC 3.0 - Block-Schutz mit Trusted Players Support
 */
public class BlockProtectionHandler {

    private static final Logger LOGGER = LogUtils.getLogger();

    // NPC Work Location Cache (Performance-Optimierung)
    private static final Map<BlockPos, CustomNPCEntity> npcWorkLocationCache = new ConcurrentHashMap<>();
    private static volatile long lastNPCCacheUpdate = 0;
    // PERFORMANCE: Cache-Dauer von 5s auf 30s erhöht - NPC-Arbeitsorte ändern sich selten
    private static final long CACHE_DURATION_MS = 30000; // 30 Sekunden Cache

    /**
     * Verhindert das Abbauen von Blöcken in fremden Plots oder NPC-Arbeitsorten
     */
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        // PERFORMANCE: Skip wenn Event bereits von anderem Mod gecancelt wurde
        if (event.isCanceled()) return;
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

            // Null-Safety: Prüfe ob NPC-Daten vorhanden sind
            if (npc.getNpcData() == null) {
                player.sendSystemMessage(
                    Component.translatable("message.npc.data_unavailable"));
                event.setCanceled(true);
                return;
            }

            // Setze Arbeitsort nur für VERKAEUFER
            if (npc.getNpcData().getNpcType() == NPCType.VERKAEUFER) {
                npc.getNpcData().getLocationData().setWorkLocation(clickedPos);
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
            // Null-Safety: Prüfe ob NPC-Daten vorhanden sind
            if (npc.getNpcData() == null) {
                player.sendSystemMessage(
                    Component.translatable("message.npc.data_unavailable"));
                event.setCanceled(true);
                return;
            }

            // Handle LocationTool
            if (holdsLocationTool) {
                NPCLocationTool.setSelectedNPC(player.getUUID(), npc.getId());

                player.sendSystemMessage(
                    Component.translatable("message.common.npc_prefix")
                        .withStyle(ChatFormatting.GREEN)
                        .append(Component.literal(npc.getNpcName())
                            .withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal(" (").append(Component.literal(npc.getNpcData().getNpcType().getDisplayName().getString())).append(Component.literal(")"))
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
                            .append(Component.literal(" (").append(Component.literal(npc.getNpcData().getNpcType().getDisplayName().getString())).append(Component.literal(")"))
                                .withStyle(ChatFormatting.GRAY))
                            .append(Component.translatable("message.common.selected")
                                .withStyle(ChatFormatting.GREEN))
                    );

                    // Zeige aktuellen Status
                    int leisureCount = npc.getNpcData().getLocationData().getLeisureLocations().size();
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
                        Component.translatable("message.npc.police_officer_prefix")
                            .withStyle(ChatFormatting.GREEN)
                            .append(Component.literal(npc.getNpcName())
                                .withStyle(ChatFormatting.YELLOW))
                            .append(Component.translatable("message.common.selected")
                                .withStyle(ChatFormatting.GREEN))
                    );

                    // Zeige aktuellen Status
                    boolean hasStation = npc.getNpcData().getPoliceData().getPoliceStation() != null;
                    int patrolCount = npc.getNpcData().getPoliceData().getPatrolPoints().size();

                    if (hasStation) {
                        player.sendSystemMessage(
                            Component.translatable("message.npc.station_label")
                                .withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(npc.getNpcData().getPoliceData().getPoliceStation().toShortString())
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
            if (npc.getNpcData() == null) continue;
            BlockPos workLocation = npc.getNpcData().getLocationData().getWorkLocation();
            if (workLocation != null) {
                npcWorkLocationCache.put(workLocation, npc);
            }
        }

        lastNPCCacheUpdate = now;
        LOGGER.debug("NPC work location cache updated: {} entries", npcWorkLocationCache.size());
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
        // PERFORMANCE: Skip wenn Event bereits von anderem Mod gecancelt wurde
        if (event.isCanceled()) return;
        EventHelper.handleBlockPlace(event, player -> {
            BlockPos pos = event.getPos();

            if (!checkPlotPermission(player, pos, "platzieren")) {
                event.setCanceled(true);
                return;
            }

            if (!checkPlotTypeBlockRestrictions(player, event, pos)) {
                event.setCanceled(true);
            }
        });
    }

    private boolean checkPlotTypeBlockRestrictions(Player player, BlockEvent.EntityPlaceEvent event, BlockPos pos) {
        PlotRegion plot = PlotManager.getPlotAt(pos);
        String blockId = getPlacedBlockId(event);
        if (blockId == null) return true;

        if (isSecretDoorControlledBlock(blockId)) {
            return canPlaceOrUseSecretDoorBlock(player, pos, false);
        }

        PlotType requiredType = getRequiredPlotTypeForBlock(blockId);
        if (requiredType == null) {
            return true;
        }

        if (plot == null || plot.getType() != requiredType) {
            player.displayClientMessage(
                Component.translatable("event.protection.block_requires_plot_type",
                    requiredType.getDisplayName()),
                true
            );
            return false;
        }

        // INDUSTRIAL-Sonderregel: Nur auf Factory Floor + nur in gekauft/vermietetem Plot
        if (requiredType == PlotType.INDUSTRIAL) {
            if (!plot.hasOwner() && !plot.isRented()) {
                player.displayClientMessage(
                    Component.translatable("event.protection.industrial.requires_ownership")
                        .withStyle(ChatFormatting.RED),
                    true
                );
                return false;
            }

            if (!plot.hasAccess(player.getUUID())) {
                player.displayClientMessage(
                    Component.translatable("event.protection.industrial.no_access")
                        .withStyle(ChatFormatting.RED),
                    true
                );
                return false;
            }

            if (!event.getLevel().getBlockState(pos.below()).is(PlotBlocks.INDUSTRIAL_FLOOR.get())) {
                player.displayClientMessage(
                    Component.translatable("event.protection.industrial.factory_floor_required")
                        .withStyle(ChatFormatting.RED),
                    true
                );
                return false;
            }
        }

        return true;
    }

    private PlotType getRequiredPlotTypeForBlock(String blockIdRaw) {
        String blockId = normalizeBlockId(blockIdRaw);
        for (PlotType type : PlotType.values()) {
            Set<String> configuredBlocks = getConfiguredBlocksForType(type);
            if (configuredBlocks.contains("all")) {
                continue;
            }
            if (configuredBlocks.contains(blockId)) {
                return type;
            }
        }
        return null;
    }

    private Set<String> getConfiguredBlocksForType(PlotType type) {
        List<? extends String> rawList = switch (type) {
            case RESIDENTIAL -> ModConfigHandler.COMMON.RESIDENTIAL_PLOT_BLOCKS.get();
            case COMMERCIAL -> ModConfigHandler.COMMON.COMMERCIAL_PLOT_BLOCKS.get();
            case INDUSTRIAL -> ModConfigHandler.COMMON.INDUSTRIAL_PLOT_BLOCKS.get();
            case SHOP -> ModConfigHandler.COMMON.SHOP_PLOT_BLOCKS.get();
            case PUBLIC -> ModConfigHandler.COMMON.PUBLIC_PLOT_BLOCKS.get();
            case GOVERNMENT -> ModConfigHandler.COMMON.GOVERNMENT_PLOT_BLOCKS.get();
            case PRISON -> ModConfigHandler.COMMON.PRISON_PLOT_BLOCKS.get();
            case TOWING_YARD -> ModConfigHandler.COMMON.TOWING_YARD_PLOT_BLOCKS.get();
        };

        Set<String> normalized = new HashSet<>();
        for (String s : rawList) {
            normalized.add(normalizeBlockId(s));
        }
        return normalized;
    }

    private String getPlacedBlockId(BlockEvent.EntityPlaceEvent event) {
        var key = ForgeRegistries.BLOCKS.getKey(event.getPlacedBlock().getBlock());
        if (key == null) return null;
        return key.getPath();
    }

    private String normalizeBlockId(String raw) {
        String value = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
        if (value.contains(":")) {
            return value.substring(value.indexOf(':') + 1);
        }
        return value;
    }

    private boolean isSecretDoorControlledBlock(String blockIdRaw) {
        String id = normalizeBlockId(blockIdRaw);
        return "secret_door".equals(id) || "hatch".equals(id) || "hidden_switch_stone".equals(id);
    }

    private boolean isSecretDoorControlledBlockState(BlockState state) {
        return state.is(SecretDoors.SECRET_DOOR.get())
            || state.is(SecretDoors.HATCH.get())
            || state.is(SecretDoors.HIDDEN_SWITCH_STONE.get());
    }

    private boolean canPlaceOrUseSecretDoorBlock(Player player, BlockPos pos, boolean allowMissionOverride) {
        if (player.hasPermissions(2)) {
            return true;
        }

        if (allowMissionOverride && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer
            && SecretDoorMissionAccessManager.hasMissionOrEventAccess(serverPlayer, pos)) {
            return true;
        }

        PlotRegion plot = PlotManager.getPlotAt(pos);
        if (plot == null) {
            player.displayClientMessage(
                Component.literal("§cHidden Switch/Tür/Luke nur auf gekauftem oder gemietetem Grundstück erlaubt."),
                true
            );
            return false;
        }

        Set<PlotType> allowedTypes = getConfiguredSecretDoorPlotTypes();
        if (!allowedTypes.contains(plot.getType())) {
            player.displayClientMessage(
                Component.literal("§cDieser Block ist auf diesem Grundstückstyp nicht erlaubt."),
                true
            );
            return false;
        }

        if (!plot.hasOwner() && !plot.isRented()) {
            player.displayClientMessage(
                Component.literal("§cGrundstück muss gekauft oder gemietet sein."),
                true
            );
            return false;
        }

        if (!plot.hasAccess(player.getUUID())) {
            player.displayClientMessage(
                Component.literal("§cDu hast keinen Zugriff auf dieses Grundstück."),
                true
            );
            return false;
        }

        return true;
    }

    private Set<PlotType> getConfiguredSecretDoorPlotTypes() {
        Set<PlotType> result = new HashSet<>();
        for (String raw : ModConfigHandler.COMMON.SECRET_DOOR_ALLOWED_PLOT_TYPES.get()) {
            if (raw == null) continue;
            try {
                result.add(PlotType.valueOf(raw.trim().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ignored) {
                LOGGER.warn("Unknown plot type in secret_door_allowed_plot_types config: {}", raw);
            }
        }
        if (result.isEmpty()) {
            result.add(PlotType.RESIDENTIAL);
            result.add(PlotType.INDUSTRIAL);
        }
        return result;
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
        net.minecraft.network.chat.MutableComponent ownerInfo;
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
        // PERFORMANCE: Skip wenn Event bereits von anderem Mod gecancelt wurde
        if (event.isCanceled()) return;
        EventHelper.handleRightClickBlock(event, player -> {
            BlockPos pos = event.getPos();

            // Admin darf immer!
            if (player.hasPermissions(2)) {
                return;
            }

            BlockState clickedState = event.getLevel().getBlockState(pos);
            if (isSecretDoorControlledBlockState(clickedState)) {
                if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer
                    && SecretDoorMissionAccessManager.hasMissionOrEventAccess(serverPlayer, pos)) {
                    SecretDoorMissionAccessManager.markMissionDoorUsed(serverPlayer, pos);
                    return;
                }
                if (!canPlaceOrUseSecretDoorBlock(player, pos, false)) {
                    event.setCanceled(true);
                }
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
