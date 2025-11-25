package de.rolandsw.schedulemc.economy.events;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.economy.WalletManager;
import de.rolandsw.schedulemc.economy.items.CashItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        
        // Speichere Geldbörsen-Wert in WalletManager (überlebt Tod!)
        ItemStack wallet = player.getInventory().getItem(8); // Slot 9
        if (wallet.getItem() instanceof CashItem) {
            double walletValue = CashItem.getValue(wallet);
            WalletManager.setBalance(player.getUUID(), walletValue);
        }
        
        player.displayClientMessage(Component.literal(
            "§c☠ Du bist gestorben!\n" +
            "§7Du wirst ins Krankenhaus gebracht..."
        ), false);
    }
    
    /**
     * Bei Respawn: Krankenhausrechnung abziehen
     */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
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
        
        // Stelle Geldbörse wieder her
        double savedBalance = WalletManager.getBalance(player.getUUID());
        
        // Ziehe Krankenhausrechnung ab
        double newBalance = Math.max(0, savedBalance - HOSPITAL_FEE);
        double actualFee = savedBalance - newBalance;
        
        // Erstelle neue Geldbörse mit neuem Wert
        ItemStack newWallet = CashItem.create(newBalance);
        player.getInventory().setItem(8, newWallet); // Slot 9
        
        // Update WalletManager
        WalletManager.setBalance(player.getUUID(), newBalance);
        
        // Nachricht an Spieler
        if (actualFee > 0) {
            player.displayClientMessage(Component.literal(
                "§c╔════════════════════════╗\n" +
                "§c║  §f⚕ KRANKENHAUSRECHNUNG §c║\n" +
                "§c╠════════════════════════╣\n" +
                "§7║ Behandlungskosten: §e" + String.format("%.0f€", HOSPITAL_FEE) + "\n" +
                "§7║ Bezahlt: §c-" + String.format("%.0f€", actualFee) + "\n" +
                "§7║ Verbleibendes Guthaben: §a" + String.format("%.0f€", newBalance) + "\n" +
                "§c╚════════════════════════╝\n" +
                "§7Gute Besserung!"
            ), false);
        } else {
            player.displayClientMessage(Component.literal(
                "§c╔════════════════════════╗\n" +
                "§c║  §f⚕ KRANKENHAUSRECHNUNG §c║\n" +
                "§c╠════════════════════════╣\n" +
                "§c║ §lKEIN GELD für Behandlung!\n" +
                "§7║ Schulden: §c" + String.format("%.0f€", HOSPITAL_FEE) + "\n" +
                "§c╚════════════════════════╝\n" +
                "§7Du wurdest trotzdem behandelt..."
            ), false);
        }
        
        player.playSound(net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP, 1.0f, 0.5f);
    }
}
