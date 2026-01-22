package de.rolandsw.schedulemc.npc.life;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.life.quest.QuestEventHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * NPCLifeSystemEvents - Zentrale Event-Handler für das NPC Life System
 *
 * Behandelt:
 * - Level-Lifecycle (Load, Unload)
 * - Server-Lifecycle
 * - Level-Tick für System-Updates
 * - Spieler-Tracking für Quest-Updates
 */
@Mod.EventBusSubscriber(modid = "schedulemc", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class NPCLifeSystemEvents {

    // ═══════════════════════════════════════════════════════════
    // LEVEL LIFECYCLE
    // ═══════════════════════════════════════════════════════════

    /**
     * Level wird geladen
     */
    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        // System initialisieren
        NPCLifeSystemIntegration.get(level);

        // TODO: Gespeicherte Daten laden
        // Dies würde normalerweise aus World-Saved-Data geladen werden
    }

    /**
     * Level wird entladen
     */
    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        // TODO: Daten speichern vor dem Entladen
        // Dies würde normalerweise in World-Saved-Data gespeichert werden

        // System entfernen
        NPCLifeSystemIntegration.remove(level);
    }

    // ═══════════════════════════════════════════════════════════
    // SERVER LIFECYCLE
    // ═══════════════════════════════════════════════════════════

    /**
     * Server wird gestoppt
     */
    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        // Alle Systeme für alle Levels speichern und entfernen
        for (ServerLevel level : event.getServer().getAllLevels()) {
            // TODO: Daten speichern
            NPCLifeSystemIntegration.remove(level);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // TICK EVENTS
    // ═══════════════════════════════════════════════════════════

    /**
     * Level-Tick für System-Updates
     */
    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.level instanceof ServerLevel level)) return;

        // Haupt-System ticken
        NPCLifeSystemIntegration.get(level).tick();
    }

    /**
     * Spieler-Tick für Quest-Tracking
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        // Nur alle 20 Ticks (1 Sekunde)
        if (player.tickCount % 20 != 0) return;

        // Quest-Location-Tracking
        QuestEventHandler.tickPlayerLocation(player);
    }

    // ═══════════════════════════════════════════════════════════
    // ENTITY EVENTS
    // ═══════════════════════════════════════════════════════════

    /**
     * Entity betritt ein Level
     */
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        // NPC-spezifische Initialisierung
        if (event.getEntity() instanceof CustomNPCEntity npc) {
            // Life-Data initialisieren falls nicht vorhanden
            if (npc.getLifeData() == null) {
                npc.initializeLifeSystem();
            }
        }

        // Spieler-spezifische Initialisierung
        if (event.getEntity() instanceof ServerPlayer player) {
            // Quest-Progress initialisieren
            NPCLifeSystemIntegration.get(level).getQuestManager().getProgress(player);
        }
    }

    /**
     * Living Entity Tick (für NPC-Updates)
     */
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof CustomNPCEntity npc)) return;
        if (!(npc.level() instanceof ServerLevel)) return;

        // NPC Life-System Update wird in CustomNPCEntity.tick() behandelt
        // Hier könnten zusätzliche Event-basierte Updates stattfinden
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITY
    // ═══════════════════════════════════════════════════════════

    /**
     * Prüft ob das Life-System für ein Level aktiv ist
     */
    public static boolean isSystemActive(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) return false;
        return NPCLifeSystemIntegration.get(serverLevel).isEnabled();
    }

    /**
     * Aktiviert/Deaktiviert das System für ein Level
     */
    public static void setSystemActive(ServerLevel level, boolean active) {
        NPCLifeSystemIntegration.get(level).setEnabled(active);
    }
}
