package de.rolandsw.schedulemc.npc.events;

import de.rolandsw.schedulemc.economy.items.CashItem;
import de.rolandsw.schedulemc.npc.crime.CrimeManager;
import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

/**
 * Polizei-KI System:
 * - Patrouilliert und sucht Verbrecher
 * - Verfolgt Spieler mit Wanted-Level
 * - Festnahme bei Kontakt
 * - Gefängnis-System mit Timer
 */
public class PoliceAIHandler {

    private static final int DETECTION_RADIUS = 32; // 32 Blöcke
    private static final double ARREST_DISTANCE = 2.0; // 2 Blöcke
    private static final double POLICE_SPEED = 1.2; // 20% schneller

    /**
     * Polizei-KI: Sucht Verbrecher und verfolgt sie
     */
    @SubscribeEvent
    public void onPoliceAI(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof CustomNPCEntity npc)) return;
        if (npc.level().isClientSide) return;
        if (npc.getNpcType() != NPCType.POLIZEI) return;

        // Prüfe ob Polizei knockout ist
        if (npc.getPersistentData().getBoolean("IsKnockedOut")) {
            return; // Keine AI während knockout
        }

        // Nur alle 20 Ticks (1 Sekunde) prüfen
        if (npc.tickCount % 20 != 0) return;

        // Suche Verbrecher in der Nähe
        List<ServerPlayer> nearbyPlayers = npc.level().getEntitiesOfClass(
            ServerPlayer.class,
            AABB.ofSize(npc.position(), DETECTION_RADIUS, DETECTION_RADIUS, DETECTION_RADIUS)
        );

        ServerPlayer targetCriminal = null;
        int highestWantedLevel = 0;

        // Finde Spieler mit höchstem Wanted-Level
        for (ServerPlayer player : nearbyPlayers) {
            int wantedLevel = CrimeManager.getWantedLevel(player.getUUID());

            if (wantedLevel > 0 && wantedLevel > highestWantedLevel) {
                targetCriminal = player;
                highestWantedLevel = wantedLevel;
            }
        }

        if (targetCriminal != null) {
            // Verfolge Verbrecher
            double distance = npc.distanceTo(targetCriminal);

            if (distance < ARREST_DISTANCE) {
                // Festnahme!
                arrestPlayer(npc, targetCriminal);
            } else {
                // Verfolge
                npc.getNavigation().moveTo(targetCriminal, POLICE_SPEED);

                // Warnung alle 5 Sekunden
                if (npc.tickCount % 100 == 0) {
                    targetCriminal.sendSystemMessage(
                        Component.literal("§c⚠ POLIZEI! Bleib stehen!")
                    );
                }
            }
        } else {
            // Keine Verbrecher - patrouilliere normal
            // (TODO: Implementiere Patrol-Route wenn gewünscht)
        }
    }

    /**
     * Festnahme eines Spielers
     */
    private void arrestPlayer(CustomNPCEntity police, ServerPlayer player) {
        int wantedLevel = CrimeManager.getWantedLevel(player.getUUID());
        if (wantedLevel <= 0) return; // Kein Wanted-Level mehr

        // Verhindere mehrfache Festnahmen
        if (player.getPersistentData().getLong("JailReleaseTime") > 0) {
            return; // Schon im Gefängnis
        }

        // Strafe berechnen
        int fine = wantedLevel * 500; // 500€ pro Stern
        int jailTimeSeconds = wantedLevel * 60; // 60 Sekunden pro Stern

        // Geld aus Wallet-Item abziehen
        ItemStack wallet = player.getInventory().getItem(8);
        if (wallet.getItem() instanceof CashItem) {
            double currentMoney = CashItem.getValue(wallet);

            if (currentMoney >= fine) {
                // Strafe bezahlen
                CashItem.removeValue(wallet, fine);
                player.sendSystemMessage(Component.literal("§c✗ FESTGENOMMEN!"));
                player.sendSystemMessage(Component.literal("§7Strafe: §c" + fine + "€"));
            } else {
                // Nicht genug Geld → Gefängnis länger + alles konfisziert
                jailTimeSeconds *= 2;
                CashItem.setValue(wallet, 0);
                player.sendSystemMessage(Component.literal("§c✗ FESTGENOMMEN!"));
                player.sendSystemMessage(Component.literal("§7Alles Bargeld konfisziert!"));
                player.sendSystemMessage(Component.literal("§c§lDOPPELTE HAFTZEIT!"));
            }
        }

        // Teleportiere zu Gefängnis (Spawn-Punkt als Platzhalter)
        BlockPos jailPos = player.getRespawnPosition() != null ?
            player.getRespawnPosition() : BlockPos.ZERO;

        player.teleportTo(jailPos.getX(), jailPos.getY(), jailPos.getZ());

        // Setze Gefängnis-Timer
        long releaseTime = player.level().getGameTime() + (jailTimeSeconds * 20L); // Ticks
        player.getPersistentData().putLong("JailReleaseTime", releaseTime);
        player.getPersistentData().putInt("JailX", jailPos.getX());
        player.getPersistentData().putInt("JailY", jailPos.getY());
        player.getPersistentData().putInt("JailZ", jailPos.getZ());

        // Gefängnis-Effekte
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, jailTimeSeconds * 20, 2));
        player.addEffect(new MobEffectInstance(MobEffects.JUMP, jailTimeSeconds * 20, 250)); // Kein Springen

        // Reset Wanted-Level
        CrimeManager.clearWantedLevel(player.getUUID());

        // Meldungen
        player.sendSystemMessage(Component.literal("§7Haftzeit: §e" + jailTimeSeconds + " Sekunden"));
        player.sendSystemMessage(Component.literal("§7Fahndungsstufe zurückgesetzt"));

        System.out.println("[POLICE] " + police.getNpcName() + " arrested " +
            player.getName().getString() + " - Jail time: " + jailTimeSeconds + "s");
    }

    /**
     * Gefängnis-System: Hält Spieler im Gefängnis
     */
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        long releaseTime = player.getPersistentData().getLong("JailReleaseTime");

        if (releaseTime > 0) {
            long currentTime = player.level().getGameTime();

            if (currentTime >= releaseTime) {
                // ═══════════════════════════════════════════
                // FREILASSUNG
                // ═══════════════════════════════════════════
                player.getPersistentData().remove("JailReleaseTime");
                player.getPersistentData().remove("JailX");
                player.getPersistentData().remove("JailY");
                player.getPersistentData().remove("JailZ");

                player.sendSystemMessage(Component.literal("§a✓ Du bist frei!"));
                player.sendSystemMessage(Component.literal("§7Halte dich von nun an an das Gesetz."));

                // Entferne Effekte
                player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                player.removeEffect(MobEffects.JUMP);

                System.out.println("[JAIL] Player " + player.getName().getString() + " released");
            } else {
                // ═══════════════════════════════════════════
                // NOCH IM GEFÄNGNIS
                // ═══════════════════════════════════════════
                // Verhindere Flucht - teleportiere zurück
                int jailX = player.getPersistentData().getInt("JailX");
                int jailY = player.getPersistentData().getInt("JailY");
                int jailZ = player.getPersistentData().getInt("JailZ");

                if (player.distanceToSqr(jailX, jailY, jailZ) > 100) {
                    player.teleportTo(jailX, jailY, jailZ);
                    player.sendSystemMessage(Component.literal("§c✗ FLUCHT VERHINDERT!"));
                }

                // Zeit anzeigen alle 10 Sekunden
                if (currentTime % 200 == 0) {
                    long remainingTicks = releaseTime - currentTime;
                    long remainingSeconds = remainingTicks / 20;

                    player.sendSystemMessage(Component.literal(
                        "§7Noch §e" + remainingSeconds + " Sekunden §7im Gefängnis..."
                    ));
                }
            }
        }
    }
}
