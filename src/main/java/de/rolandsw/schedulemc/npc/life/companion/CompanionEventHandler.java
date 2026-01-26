package de.rolandsw.schedulemc.npc.life.companion;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.UUID;

/**
 * CompanionEventHandler - Verarbeitet Events für das Begleiter-System
 *
 * Behandelt:
 * - Schaden an Begleitern
 * - Tod von Begleitern
 * - Spieler-Interaktionen mit Begleitern
 * - Spieler-Login/Logout
 */
@Mod.EventBusSubscriber(modid = "schedulemc", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CompanionEventHandler {

    // ═══════════════════════════════════════════════════════════
    // DAMAGE EVENTS
    // ═══════════════════════════════════════════════════════════

    /**
     * Verarbeitet Schaden an Begleitern
     */
    @SubscribeEvent
    public static void onCompanionDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof CustomNPCEntity npc)) return;
        if (!(npc.level() instanceof ServerLevel level)) return;

        // Prüfe ob es ein Begleiter ist
        CompanionData companionData = getCompanionData(npc, level);
        if (companionData == null) return;

        // Schaden auf Begleiter-Daten übertragen
        float damage = event.getAmount();

        // Reduziere Schaden basierend auf Level
        float damageReduction = 1.0f - (companionData.getLevel() * 0.05f);
        damage *= Math.max(0.5f, damageReduction);

        event.setAmount(damage);

        // Daten aktualisieren
        companionData.damage(damage);

        // Bei kritischem Schaden: Besitzer warnen
        if (companionData.getHealth() < companionData.getMaxHealth() * 0.25f) {
            warnOwnerAboutCompanionHealth(level, companionData);
        }

        // Wenn Schaden vom Besitzer kommt: Loyalität sinkt
        DamageSource source = event.getSource();
        if (source.getEntity() instanceof ServerPlayer player) {
            if (player.getUUID().equals(companionData.getOwnerUUID())) {
                companionData.addLoyalty(-5);

                // Bei niedriger Loyalität: Begleiter flieht
                if (companionData.getLoyalty() < 10) {
                    handleCompanionDeserts(level, companionData, npc);
                }
            }
        }
    }

    /**
     * Verarbeitet den Tod eines Begleiters
     */
    @SubscribeEvent
    public static void onCompanionDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof CustomNPCEntity npc)) return;
        if (!(npc.level() instanceof ServerLevel level)) return;

        CompanionData companionData = getCompanionData(npc, level);
        if (companionData == null) return;

        // Begleiter wird kampfunfähig (nicht permanent tot)
        companionData.onIncapacitated();

        // Besitzer benachrichtigen
        notifyOwnerOfIncapacitation(level, companionData);

        // Entity aus aktiven entfernen
        CompanionManager.getManager(level).dismiss(companionData.getCompanionUUID());

        // Event abbrechen um permanenten Tod zu verhindern
        event.setCanceled(true);

        // Entity unsichtbar machen und entfernen
        npc.discard();
    }

    // ═══════════════════════════════════════════════════════════
    // PLAYER EVENTS
    // ═══════════════════════════════════════════════════════════

    /**
     * Verarbeitet Spieler-Login
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        // Begleiter-Daten können hier geladen werden
        // (falls nicht bereits durch World-Load geladen)
    }

    /**
     * Verarbeitet Spieler-Logout
     */
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        // Alle aktiven Begleiter des Spielers despawnen
        CompanionManager.getManager(level).dismissAll(player.getUUID());
    }

    /**
     * Verarbeitet Interaktion mit Begleiter
     */
    @SubscribeEvent
    public static void onCompanionInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(event.getTarget() instanceof CustomNPCEntity npc)) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        CompanionData companionData = getCompanionData(npc, level);
        if (companionData == null) return;

        // Nur Besitzer darf interagieren
        if (!player.getUUID().equals(companionData.getOwnerUUID())) {
            return;
        }

        // Interaktion registrieren
        companionData.onOwnerInteraction();

        // Hier könnte ein Companion-GUI geöffnet werden
        // openCompanionGUI(player, companionData);
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITY METHODS
    // ═══════════════════════════════════════════════════════════

    /**
     * Holt CompanionData für eine Entity
     */
    private static CompanionData getCompanionData(CustomNPCEntity npc, ServerLevel level) {
        UUID npcUUID = npc.getNpcData().getNpcUUID();

        // Suche in allen Begleitern
        CompanionManager manager = CompanionManager.getManager(level);
        return manager.getCompanion(npcUUID);
    }

    /**
     * Warnt den Besitzer über niedrige Gesundheit des Begleiters
     */
    private static void warnOwnerAboutCompanionHealth(ServerLevel level, CompanionData companionData) {
        UUID ownerUUID = companionData.getOwnerUUID();
        if (ownerUUID == null) return;

        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(ownerUUID);
        if (owner != null) {
            // Hier könnte eine Nachricht gesendet werden
            // owner.displayClientMessage(Component.literal("Dein Begleiter ist schwer verletzt!"), true);
        }
    }

    /**
     * Benachrichtigt den Besitzer über Kampfunfähigkeit
     */
    private static void notifyOwnerOfIncapacitation(ServerLevel level, CompanionData companionData) {
        UUID ownerUUID = companionData.getOwnerUUID();
        if (ownerUUID == null) return;

        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(ownerUUID);
        if (owner != null) {
            // owner.displayClientMessage(
            //     Component.literal(companionData.getName() + " wurde besiegt!"),
            //     false
            // );
        }
    }

    /**
     * Begleiter desertiert (bei sehr niedriger Loyalität)
     */
    private static void handleCompanionDeserts(ServerLevel level, CompanionData companionData, CustomNPCEntity entity) {
        UUID ownerUUID = companionData.getOwnerUUID();
        if (ownerUUID == null) return;

        // Begleiter entlassen
        CompanionManager manager = CompanionManager.getManager(level);
        manager.releaseCompanion(ownerUUID, companionData.getCompanionUUID());

        // Besitzer benachrichtigen
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(ownerUUID);
        if (owner != null) {
            // owner.displayClientMessage(
            //     Component.literal(companionData.getName() + " hat dich verlassen!"),
            //     false
            // );
        }

        // Entity wird zu normalem NPC
        entity.discard();
    }

    // ═══════════════════════════════════════════════════════════
    // COMPANION COMMANDS (called from GUI or chat)
    // ═══════════════════════════════════════════════════════════

    /**
     * Sendet einen Befehl an alle aktiven Begleiter eines Spielers
     */
    public static void sendCommandToAll(ServerPlayer player, CompanionData.CompanionCommand command) {
        if (!(player.level() instanceof ServerLevel level)) return;

        CompanionManager manager = CompanionManager.getManager(level);
        List<CompanionData> companions = manager.getCompanions(player.getUUID());

        for (CompanionData companion : companions) {
            if (manager.isActive(companion.getCompanionUUID())) {
                companion.giveCommand(command);
            }
        }
    }

    /**
     * Beschwört einen bestimmten Begleiter
     */
    public static boolean summonCompanion(ServerPlayer player, UUID companionUUID) {
        if (!(player.level() instanceof ServerLevel level)) return false;

        CompanionManager manager = CompanionManager.getManager(level);
        CustomNPCEntity entity = manager.summon(player, companionUUID);
        return entity != null;
    }

    /**
     * Entlässt einen Begleiter temporär
     */
    public static void dismissCompanion(ServerPlayer player, UUID companionUUID) {
        if (!(player.level() instanceof ServerLevel level)) return;

        CompanionManager manager = CompanionManager.getManager(level);
        CompanionData data = manager.getCompanion(companionUUID);

        // Prüfe Besitz
        if (data != null && player.getUUID().equals(data.getOwnerUUID())) {
            manager.dismiss(companionUUID);
        }
    }

    /**
     * Heilt einen Begleiter (gegen Kosten)
     */
    public static boolean healCompanion(ServerPlayer player, UUID companionUUID, int cost) {
        if (!(player.level() instanceof ServerLevel level)) return false;

        CompanionManager manager = CompanionManager.getManager(level);
        CompanionData data = manager.getCompanion(companionUUID);

        if (data == null || !player.getUUID().equals(data.getOwnerUUID())) {
            return false;
        }

        // Kosten prüfen und abziehen
        net.minecraft.nbt.CompoundTag playerData = player.getPersistentData().getCompound("ScheduleMC");
        long money = playerData.getLong("wallet");

        if (money < cost) return false;

        playerData.putLong("wallet", money - cost);
        player.getPersistentData().put("ScheduleMC", playerData);

        // Heilen
        data.heal(data.getMaxHealth());

        // Wenn kampfunfähig: wiederbeleben
        if (data.getState() == CompanionData.CompanionState.INCAPACITATED) {
            data.onRevive();
        }

        return true;
    }

    /**
     * Füttert einen Begleiter (erhöht Zufriedenheit)
     */
    public static void feedCompanion(ServerPlayer player, UUID companionUUID, float satisfactionBoost) {
        if (!(player.level() instanceof ServerLevel level)) return;

        CompanionManager manager = CompanionManager.getManager(level);
        CompanionData data = manager.getCompanion(companionUUID);

        if (data != null && player.getUUID().equals(data.getOwnerUUID())) {
            data.setSatisfaction(data.getSatisfaction() + satisfactionBoost);
            data.addLoyalty(2); // Füttern erhöht Loyalität
        }
    }

    /**
     * Gibt Erfahrung an einen Begleiter
     */
    public static void grantExperience(UUID companionUUID, ServerLevel level, int amount) {
        CompanionManager manager = CompanionManager.getManager(level);
        CompanionData data = manager.getCompanion(companionUUID);

        if (data != null) {
            data.addExperience(amount);
        }
    }
}
