package de.rolandsw.schedulemc.economy.events;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.economy.WalletManager;
import de.rolandsw.schedulemc.economy.items.CashItem;
import de.rolandsw.schedulemc.util.EventHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Respawn-System mit Krankenhausrechnung
 */
@Mod.EventBusSubscriber(modid = ScheduleMC.MOD_ID)
public class RespawnHandler {
    
    // KONFIGURIERBAR - Krankenhausrechnung
    private static double HOSPITAL_FEE = 500.0; // 500€ Krankenhausrechnung
    
    // KONFIGURIERBAR - Respawn-Position
    private static BlockPos HOSPITAL_SPAWN = new BlockPos(0, 64, 0); // Standard-Position
    
    /**
     * Setzt Respawn-Position
     */
    public static void setHospitalSpawn(BlockPos pos) {
        HOSPITAL_SPAWN = pos;
    }
    
    /**
     * Gibt Respawn-Position zurück
     */
    public static BlockPos getHospitalSpawn() {
        return HOSPITAL_SPAWN;
    }
    
    /**
     * Setzt Krankenhausrechnung
     */
    public static void setHospitalFee(double fee) {
        HOSPITAL_FEE = Math.max(0, fee);
    }
    
    /**
     * Gibt Krankenhausrechnung zurück
     */
    public static double getHospitalFee() {
        return HOSPITAL_FEE;
    }
    
    /**
     * Bei Tod: Markiere für Krankenhausrechnung
     */
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        EventHelper.handleServerPlayerLivingEvent(event, player -> {
            // Bargeld überlebt Tod automatisch (ist im WalletManager gespeichert!)
            // Keine Aktion nötig - WalletManager ist UUID-basiert und persistent

            player.displayClientMessage(Component.translatable(
                "event.respawn.death_message"
            ), false);
        });
    }
    
    /**
     * Bei Respawn: Krankenhausrechnung abziehen
     */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        EventHelper.handleServerPlayerOnly(event, player -> {
            if (event.isEndConquered()) return; // Nicht bei End-Sieg
        
        // Teleportiere zum Krankenhaus (1 Tick später damit Welt geladen ist)
        player.getServer().execute(() -> {
            player.teleportTo(
                player.serverLevel(),
                HOSPITAL_SPAWN.getX() + 0.5,
                HOSPITAL_SPAWN.getY(),
                HOSPITAL_SPAWN.getZ() + 0.5,
                player.getYRot(),
                player.getXRot()
            );
        });

        // Hole aktuelles Bargeld aus WalletManager
        double savedBalance = WalletManager.getBalance(player.getUUID());

        // Ziehe Krankenhausrechnung ab
        double newBalance = Math.max(0, savedBalance - HOSPITAL_FEE);
        double actualFee = savedBalance - newBalance;

        // Update WalletManager (Geldbörse im Inventar zeigt Wert automatisch in GUI)
        WalletManager.setBalance(player.getUUID(), newBalance);
        
        // Nachricht an Spieler
        if (actualFee > 0) {
            player.displayClientMessage(Component.translatable(
                "event.respawn.hospital_bill",
                String.format("%.0f€", HOSPITAL_FEE),
                String.format("%.0f€", actualFee),
                String.format("%.0f€", newBalance)
            ), false);
        } else {
            player.displayClientMessage(Component.translatable(
                "event.respawn.no_money_treatment",
                String.format("%.0f€", HOSPITAL_FEE)
            ), false);
        }

            player.playSound(net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP, 1.0f, 0.5f);
        });
    }
}
