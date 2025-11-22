package de.rolandsw.schedulemc.npc.events;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * NPCs können nicht sterben - sie werden stattdessen "knockout"
 * - Bei 0 HP: NPC bleibt bewegungslos stehen
 * - Nächster Tag: NPC erholt sich und kann sich wieder bewegen
 */
public class NPCKnockoutHandler {

    @SubscribeEvent
    public void onNPCDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof CustomNPCEntity npc)) return;
        if (npc.level().isClientSide) return;

        // Prüfe ob Schaden tödlich wäre
        float newHealth = npc.getHealth() - event.getAmount();

        if (newHealth <= 0) {
            // Verhindere Tod
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

            System.out.println("[KNOCKOUT] NPC " + npc.getNpcName() + " knockout an Tag " + currentDay);
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

                System.out.println("[KNOCKOUT] NPC " + npc.getNpcName() + " erholt sich an Tag " + currentDay);
            } else {
                // Noch knockout - verhindere Bewegung
                npc.setDeltaMovement(0, 0, 0);
                npc.getNavigation().stop();
                npc.setTarget(null); // Kein Kampf während knockout
            }
        }
    }
}
