package de.rolandsw.schedulemc.npc.events;

import de.rolandsw.schedulemc.npc.crime.CrimeManager;
import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

/**
 * NPCs können nicht sterben - sie werden stattdessen "knockout"
 * - Bei 0 HP: NPC bleibt bewegungslos stehen
 * - Nächster Tag: NPC erholt sich und kann sich wieder bewegen
 * - Wanted-Level für Angriffe auf NPCs (nur wenn gesehen)
 */
public class NPCKnockoutHandler {

    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public void onNPCDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof CustomNPCEntity npc)) return;
        if (npc.level().isClientSide) return;

        // Prüfe ob Angreifer ein Spieler ist
        Player attacker = null;
        if (event.getSource().getEntity() instanceof Player) {
            attacker = (Player) event.getSource().getEntity();
        }

        // Prüfe ob Schaden tödlich wäre (Knockout)
        float newHealth = npc.getHealth() - event.getAmount();
        boolean isKnockout = newHealth <= 0;

        if (isKnockout) {
            // ═══════════════════════════════════════════
            // KNOCKOUT - Verhindere Tod
            // ═══════════════════════════════════════════
            event.setCanceled(true);

            // Prüfe ob NPC bereits knockout ist
            if (npc.getPersistentData().getBoolean("IsKnockedOut")) {
                return; // Schon knockout
            }

            // Setze NPC auf "knockout"
            npc.setHealth(1.0f); // Minimal HP
            npc.getPersistentData().putBoolean("IsKnockedOut", true);

            long currentDay = npc.level().getDayTime() / 24000;
            npc.getPersistentData().putLong("KnockoutDay", currentDay);

            // Stoppe Bewegung
            npc.setDeltaMovement(0, 0, 0);
            npc.getNavigation().stop();

            // Visuelle Effekte
            npc.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, Integer.MAX_VALUE, 255, false, false));
            npc.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, Integer.MAX_VALUE, 255, false, false));
            npc.addEffect(new MobEffectInstance(MobEffects.GLOWING, Integer.MAX_VALUE, 0, false, false));

            // Angreifer informieren
            if (event.getSource().getEntity() != null) {
                event.getSource().getEntity().sendSystemMessage(
                    Component.literal("§e" + npc.getNpcName() + " ist kampfunfähig!")
                );
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[KNOCKOUT] NPC {} knockout an Tag {}", npc.getNpcName(), currentDay);
            }
        }

        // ═══════════════════════════════════════════
        // WANTED-LEVEL FÜR ANGRIFFE (nur wenn Spieler)
        // ═══════════════════════════════════════════
        if (attacker instanceof ServerPlayer player) {
            // Suche Zeugen in 16 Block Radius
            List<CustomNPCEntity> witnesses = npc.level().getEntitiesOfClass(
                CustomNPCEntity.class,
                AABB.ofSize(player.position(), 16, 16, 16)
            );

            // Entferne das Opfer aus der Zeugenliste
            witnesses.remove(npc);

            if (!witnesses.isEmpty()) {
                // Prüfe ob POLIZEI dabei ist
                boolean policePresent = false;
                for (CustomNPCEntity witness : witnesses) {
                    if (witness.getNpcType() == NPCType.POLIZEI) {
                        policePresent = true;
                        break;
                    }
                }

                double detectionChance;
                if (policePresent) {
                    // POLIZEI anwesend = 100% Erkennung!
                    detectionChance = 1.0;
                } else {
                    // Normale Zeugen: 15% pro Zeuge, max 90%
                    detectionChance = Math.min(0.9, witnesses.size() * 0.15);
                }

                if (Math.random() < detectionChance) {
                    // Verbrechen wurde gesehen!
                    long currentDay = npc.level().getDayTime() / 24000;
                    int starsToAdd;
                    String crimeType;

                    if (isKnockout) {
                        // Knockout
                        if (npc.getNpcType() == NPCType.POLIZEI) {
                            starsToAdd = 4; // Polizist knockout
                            crimeType = "Polizist kampfunfähig geschlagen";
                        } else {
                            starsToAdd = 3; // NPC knockout
                            crimeType = "NPC kampfunfähig geschlagen";
                        }
                    } else {
                        // Nur Schaden
                        starsToAdd = 1;
                        crimeType = "Körperverletzung";
                    }

                    CrimeManager.addWantedLevel(player.getUUID(), starsToAdd, currentDay);

                    int currentWantedLevel = CrimeManager.getWantedLevel(player.getUUID());
                    String stars = "⭐".repeat(currentWantedLevel);

                    if (policePresent) {
                        player.sendSystemMessage(Component.literal("§c⚠ POLIZEI hat dich gesehen!"));
                    } else {
                        player.sendSystemMessage(Component.literal("§c⚠ Du wurdest gesehen!"));
                    }
                    player.sendSystemMessage(Component.literal("§7Verbrechen: §c" + crimeType));
                    player.sendSystemMessage(Component.literal("§c" + stars + " Fahndungsstufe: " + currentWantedLevel));

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("[CRIME] Player {} - {} - Wanted Level: {} (+{} Sterne{})",
                            player.getName().getString(), crimeType, currentWantedLevel, starsToAdd,
                            policePresent ? ", POLIZEI dabei!" : "");
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onNPCTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof CustomNPCEntity npc)) return;
        if (npc.level().isClientSide) return;

        // Nur alle 20 Ticks (1 Sekunde) prüfen
        if (npc.tickCount % 20 != 0) return;

        if (npc.getPersistentData().getBoolean("IsKnockedOut")) {
            long knockoutDay = npc.getPersistentData().getLong("KnockoutDay");
            long currentDay = npc.level().getDayTime() / 24000;

            // Nächster Tag = Erholung
            if (currentDay > knockoutDay) {
                // NPC erholt sich
                npc.getPersistentData().remove("IsKnockedOut");
                npc.getPersistentData().remove("KnockoutDay");

                // Entferne Effekte
                npc.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                npc.removeEffect(MobEffects.WEAKNESS);
                npc.removeEffect(MobEffects.GLOWING);

                // Stelle HP wieder her
                npc.setHealth(npc.getMaxHealth());

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[KNOCKOUT] NPC {} erholt sich an Tag {}", npc.getNpcName(), currentDay);
                }
            } else {
                // Noch knockout - verhindere Bewegung
                npc.setDeltaMovement(0, 0, 0);
                npc.getNavigation().stop();
                npc.setTarget(null); // Kein Kampf während knockout
            }
        }
    }
}
