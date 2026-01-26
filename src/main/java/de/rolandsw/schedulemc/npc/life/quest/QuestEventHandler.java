package de.rolandsw.schedulemc.npc.life.quest;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * QuestEventHandler - Verarbeitet Events für das Quest-System
 *
 * Hört auf Minecraft-Events und aktualisiert Quest-Fortschritt entsprechend.
 */
@Mod.EventBusSubscriber(modid = "schedulemc", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class QuestEventHandler {

    // ═══════════════════════════════════════════════════════════
    // ITEM EVENTS
    // ═══════════════════════════════════════════════════════════

    /**
     * Verarbeitet Item-Pickup für Sammelquests
     */
    @SubscribeEvent
    public static void onItemPickup(EntityItemPickupEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        ItemStack stack = event.getItem().getItem();

        // Quest-Fortschritt aktualisieren
        QuestManager manager = QuestManager.getManager(level);
        QuestProgress progress = manager.getProgress(player);
        progress.reportItemProgress(stack);
    }

    // ═══════════════════════════════════════════════════════════
    // KILL EVENTS
    // ═══════════════════════════════════════════════════════════

    /**
     * Verarbeitet Kills für Eliminierungs-Quests
     */
    @SubscribeEvent
    public static void onEntityKill(LivingDeathEvent event) {
        Entity killer = event.getSource().getEntity();
        if (!(killer instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        LivingEntity victim = event.getEntity();
        String entityType = getEntityTypeName(victim);

        // Quest-Fortschritt aktualisieren
        QuestManager manager = QuestManager.getManager(level);
        QuestProgress progress = manager.getProgress(player);
        progress.reportKill(entityType);
    }

    /**
     * Ermittelt den Entity-Typ-Namen
     */
    private static String getEntityTypeName(Entity entity) {
        return entity.getType().getDescriptionId()
            .replace("entity.minecraft.", "")
            .replace("entity.schedulemc.", "");
    }

    // ═══════════════════════════════════════════════════════════
    // INTERACTION EVENTS
    // ═══════════════════════════════════════════════════════════

    /**
     * Verarbeitet NPC-Interaktionen für Dialog-/Verhandlungs-Quests
     */
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(event.getTarget() instanceof CustomNPCEntity npc)) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        // Quest-Fortschritt für NPC-Gespräche
        QuestManager manager = QuestManager.getManager(level);
        QuestProgress progress = manager.getProgress(player);
        progress.reportNPCInteraction(npc.getNpcData().getNpcUUID());
    }

    // ═══════════════════════════════════════════════════════════
    // LOCATION TRACKING
    // ═══════════════════════════════════════════════════════════

    /**
     * Muss regelmäßig aufgerufen werden um Positions-Ziele zu tracken
     * (z.B. alle 20 Ticks pro Spieler)
     */
    public static void tickPlayerLocation(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;

        BlockPos pos = player.blockPosition();

        QuestManager manager = QuestManager.getManager(level);
        QuestProgress progress = manager.getProgress(player);
        progress.reportLocationProgress(pos);
    }

    // ═══════════════════════════════════════════════════════════
    // MANUAL PROGRESS UPDATES
    // ═══════════════════════════════════════════════════════════

    /**
     * Meldet manuell Fortschritt für ein bestimmtes Objective
     * (für komplexe Objectives die nicht durch Events getrackt werden)
     */
    public static void reportManualProgress(ServerPlayer player, String questId, String objectiveId, int amount) {
        if (!(player.level() instanceof ServerLevel level)) return;

        QuestManager manager = QuestManager.getManager(level);
        QuestProgress progress = manager.getProgress(player);

        Quest quest = progress.getActiveQuest(questId);
        if (quest == null) return;

        QuestObjective objective = quest.getObjective(objectiveId);
        if (objective != null) {
            objective.addProgress(amount);
        }
    }

    /**
     * Meldet, dass ein Spieler Items an einen NPC geliefert hat
     */
    public static void reportDelivery(ServerPlayer player, CustomNPCEntity npc, ItemStack deliveredItem) {
        if (!(player.level() instanceof ServerLevel level)) return;

        QuestManager manager = QuestManager.getManager(level);
        QuestProgress progress = manager.getProgress(player);

        // Suche nach Liefer-Objectives
        for (Quest quest : progress.getActiveQuests().values()) {
            for (QuestObjective obj : quest.getObjectives()) {
                if (obj.getType() == QuestObjective.ObjectiveType.DELIVER_TO_NPC) {
                    if (obj.getTargetNPC() != null &&
                        obj.getTargetNPC().equals(npc.getNpcData().getNpcUUID())) {

                        if (obj.getTargetItem() != null &&
                            obj.getTargetItem() == deliveredItem.getItem()) {

                            obj.addProgress(deliveredItem.getCount());
                        }
                    }
                }
            }
        }
    }

    /**
     * Meldet erfolgreiche Verhandlung
     */
    public static void reportSuccessfulNegotiation(ServerPlayer player, CustomNPCEntity npc) {
        if (!(player.level() instanceof ServerLevel level)) return;

        QuestManager manager = QuestManager.getManager(level);
        QuestProgress progress = manager.getProgress(player);

        // Suche nach Verhandlungs-Objectives
        for (Quest quest : progress.getActiveQuests().values()) {
            for (QuestObjective obj : quest.getObjectives()) {
                if (obj.getType() == QuestObjective.ObjectiveType.NEGOTIATE_DEAL) {
                    if (obj.getTargetNPC() != null &&
                        obj.getTargetNPC().equals(npc.getNpcData().getNpcUUID())) {

                        obj.addProgress(1);
                    }
                }
            }
        }
    }

    /**
     * Meldet gefundene Information (für Ermittlungs-Quests)
     */
    public static void reportInformationFound(ServerPlayer player, String informationKey) {
        if (!(player.level() instanceof ServerLevel level)) return;

        QuestManager manager = QuestManager.getManager(level);
        QuestProgress progress = manager.getProgress(player);

        // Suche nach Informations-Objectives
        for (Quest quest : progress.getActiveQuests().values()) {
            for (QuestObjective obj : quest.getObjectives()) {
                if (obj.getType() == QuestObjective.ObjectiveType.FIND_INFORMATION) {
                    if (obj.getTargetEntityType() != null &&
                        obj.getTargetEntityType().equals(informationKey)) {

                        obj.addProgress(1);
                    }
                }
            }
        }
    }

    /**
     * Meldet, dass Eskorte-Ziel erreicht wurde
     */
    public static void reportEscortArrival(ServerPlayer player, CustomNPCEntity escortedNPC, BlockPos destination) {
        if (!(player.level() instanceof ServerLevel level)) return;

        QuestManager manager = QuestManager.getManager(level);
        QuestProgress progress = manager.getProgress(player);

        // Suche nach Eskorte-Objectives
        for (Quest quest : progress.getActiveQuests().values()) {
            for (QuestObjective obj : quest.getObjectives()) {
                if (obj.getType() == QuestObjective.ObjectiveType.ESCORT_NPC) {
                    if (obj.getTargetNPC() != null &&
                        obj.getTargetNPC().equals(escortedNPC.getNpcData().getNpcUUID())) {

                        // Prüfe ob Ziel erreicht
                        if (obj.getTargetLocation() != null) {
                            double distance = Math.sqrt(destination.distSqr(obj.getTargetLocation()));
                            if (distance <= obj.getLocationRadius()) {
                                obj.addProgress(1);
                            }
                        }
                    }
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // QUEST COMPLETION
    // ═══════════════════════════════════════════════════════════

    /**
     * Prüft ob ein Spieler Quests bei einem NPC abschließen kann
     */
    public static java.util.List<Quest> getCompletableQuests(ServerPlayer player, CustomNPCEntity npc) {
        if (!(player.level() instanceof ServerLevel level)) {
            return java.util.Collections.emptyList();
        }

        QuestManager manager = QuestManager.getManager(level);
        QuestProgress progress = manager.getProgress(player);

        return progress.getQuestsReadyToCompleteAt(npc.getNpcData().getNpcUUID());
    }

    /**
     * Schließt eine Quest beim Quest-Geber ab
     */
    public static boolean turnInQuest(ServerPlayer player, String questId) {
        if (!(player.level() instanceof ServerLevel level)) return false;

        QuestManager manager = QuestManager.getManager(level);
        return manager.completeQuest(player, questId);
    }
}
