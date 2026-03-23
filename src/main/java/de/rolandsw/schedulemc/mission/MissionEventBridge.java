package de.rolandsw.schedulemc.mission;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Verknuepft Forge-Game-Events mit dem PlayerMissionManager.
 *
 * Jedes relevante Spiel-Ereignis ruft trackProgress() auf, damit
 * Spieler-Missionen ihren Fortschritt automatisch tracken koennen.
 *
 * Statische fire*-Methoden erlauben zusaetzliches Tracking aus
 * Paket-Handlern und anderen Klassen ohne Event-Kontext.
 */
@Mod.EventBusSubscriber(modid = "schedulemc", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MissionEventBridge {

    // ═══════════════════════════════════════════════════════════
    // FORGE EVENT LISTENER
    // ═══════════════════════════════════════════════════════════

    /**
     * Spieler toetet eine lebende Entitaet → enemy_killed
     */
    @SubscribeEvent
    public static void onEntityKill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        track(player, "enemy_killed", 1);
    }

    /**
     * Spieler hebt Item auf → item_collected (Anzahl Items)
     */
    @SubscribeEvent
    public static void onItemPickup(EntityItemPickupEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        int count = event.getItem().getItem().getCount();
        track(player, "item_collected", count);
    }

    /**
     * Spieler interagiert mit einem NPC → npc_talked
     */
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(event.getTarget() instanceof CustomNPCEntity)) return;
        track(player, "npc_talked", 1);
    }

    // ═══════════════════════════════════════════════════════════
    // STATISCHE FIRE-METHODEN (fuer Paket-Handler / Manager)
    // ═══════════════════════════════════════════════════════════

    /** Spieler hat erfolgreich Geld eingezahlt (BankDepositPacket). */
    public static void fireBankDeposit(ServerPlayer player) {
        track(player, "bank_deposit", 1);
    }

    /** Spieler hat eine Gang-Mission abgeschlossen (GangActionPacket). */
    public static void fireGangMissionCompleted(ServerPlayer player) {
        track(player, "gang_mission_completed", 1);
    }

    /** Spieler hat einen Raub erfolgreich abgeschlossen. */
    public static void fireRobberyCompleted(ServerPlayer player) {
        track(player, "robbery_completed", 1);
        track(player, "mission_completed", 1);
    }

    /** Spieler hat Geld verdient (z.B. Verkauf, Transaktion). */
    public static void fireMoneyEarned(ServerPlayer player, int amount) {
        if (amount <= 0) return;
        track(player, "money_earned", amount);
    }

    /** Spieler hat ein Item an einen NPC verkauft. */
    public static void fireItemSoldToNpc(ServerPlayer player, int count) {
        track(player, "item_sold_to_npc", count);
    }

    /** Spieler hat ein Paket geliefert. */
    public static void firePackageDelivered(ServerPlayer player) {
        track(player, "package_delivered", 1);
    }

    /** Spieler hat eine Transaktion abgeschlossen (Bezahlung, Handel). */
    public static void fireTransactionCompleted(ServerPlayer player) {
        track(player, "transaction_completed", 1);
    }

    /** Spieler hat ein Territorium eingenommen. */
    public static void fireTerritoryCapture(ServerPlayer player) {
        track(player, "territory_captured", 1);
    }

    /** Spieler hat einen Stadtteil besucht. */
    public static void fireDistrictVisited(ServerPlayer player) {
        track(player, "district_visited", 1);
    }

    /** Spieler hat ein Item hergestellt. */
    public static void fireItemCrafted(ServerPlayer player, int count) {
        track(player, "item_crafted", count);
    }

    /** Spieler hat km in einem Fahrzeug zurueckgelegt. */
    public static void fireKmDriven(ServerPlayer player, int km) {
        if (km <= 0) return;
        track(player, "km_driven", km);
    }

    /** Allgemeiner Mission-Abschluss (bei claimMission). */
    public static void fireMissionCompleted(ServerPlayer player) {
        track(player, "mission_completed", 1);
    }

    // ═══════════════════════════════════════════════════════════
    // HELPER
    // ═══════════════════════════════════════════════════════════

    private static void track(ServerPlayer player, String key, int amount) {
        PlayerMissionManager mgr = PlayerMissionManager.getInstance();
        if (mgr != null) {
            mgr.trackProgress(player, key, amount);
        }
    }
}
