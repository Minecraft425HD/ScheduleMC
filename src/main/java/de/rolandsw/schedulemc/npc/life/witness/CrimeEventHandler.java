package de.rolandsw.schedulemc.npc.life.witness;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * CrimeEventHandler - Erkennt Verbrechen und registriert sie beim WitnessManager
 *
 * Hört auf verschiedene Minecraft-Events und wertet sie als potentielle
 * Verbrechen aus.
 */
@Mod.EventBusSubscriber(modid = "schedulemc", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CrimeEventHandler {

    // ═══════════════════════════════════════════════════════════
    // VIOLENCE DETECTION
    // ═══════════════════════════════════════════════════════════

    /**
     * Erkennt Angriffe auf NPCs und andere Entitäten
     */
    @SubscribeEvent
    public static void onEntityAttack(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        Entity target = event.getTarget();

        // Angriff auf NPC erkennen
        if (target instanceof de.rolandsw.schedulemc.npc.entity.CustomNPCEntity npc) {
            // Bestimme Verbrechenstyp basierend auf Schaden
            float damage = player.getAttackStrengthScale(0.5F);
            CrimeType crimeType;

            if (damage > 5) {
                crimeType = CrimeType.AGGRAVATED_ASSAULT;
            } else if (damage > 2) {
                crimeType = CrimeType.ASSAULT;
            } else {
                crimeType = CrimeType.THREAT;
            }

            // Hat Spieler eine Waffe?
            if (isHoldingWeapon(player)) {
                crimeType = CrimeType.ARMED_VIOLENCE;
            }

            // Verbrechen registrieren
            WitnessManager.getManager(level).registerCrime(
                player,
                crimeType,
                target.blockPosition(),
                level,
                npc.getNpcData().getNpcUUID()
            );
        }
    }

    /**
     * Erkennt tödliche Angriffe
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;

        // Prüfe ob durch Spieler getötet
        Entity killer = event.getSource().getEntity();
        if (!(killer instanceof ServerPlayer player)) return;

        LivingEntity victim = event.getEntity();

        // NPC getötet
        if (victim instanceof de.rolandsw.schedulemc.npc.entity.CustomNPCEntity npc) {
            // Schwerstes Verbrechen
            WitnessManager.getManager(level).registerCrime(
                player,
                CrimeType.ARMED_VIOLENCE, // oder MURDER wenn hinzugefügt
                victim.blockPosition(),
                level,
                npc.getNpcData().getNpcUUID()
            );
        }
    }

    // ═══════════════════════════════════════════════════════════
    // THEFT DETECTION
    // ═══════════════════════════════════════════════════════════

    /**
     * Registriert einen Diebstahl (aufgerufen von externen Systemen)
     */
    public static void registerTheft(ServerPlayer player, BlockPos location, int value) {
        if (!(player.level() instanceof ServerLevel level)) return;

        CrimeType crimeType;
        if (value < 100) {
            crimeType = CrimeType.PETTY_THEFT;
        } else if (value < 500) {
            crimeType = CrimeType.SHOPLIFTING;
        } else {
            crimeType = CrimeType.BURGLARY;
        }

        WitnessManager.getManager(level).registerCrime(
            player,
            crimeType,
            location,
            level,
            null
        );
    }

    /**
     * Registriert einen Raub (Diebstahl mit Gewalt)
     */
    public static void registerRobbery(ServerPlayer player, BlockPos location, @Nullable UUID victimUUID) {
        if (!(player.level() instanceof ServerLevel level)) return;

        WitnessManager.getManager(level).registerCrime(
            player,
            CrimeType.ROBBERY,
            location,
            level,
            victimUUID
        );
    }

    // ═══════════════════════════════════════════════════════════
    // DRUG DETECTION
    // ═══════════════════════════════════════════════════════════

    /**
     * Registriert Drogenkonsum (aufgerufen vom Drug-System)
     */
    public static void registerDrugUse(ServerPlayer player, BlockPos location) {
        if (!(player.level() instanceof ServerLevel level)) return;

        WitnessManager.getManager(level).registerCrime(
            player,
            CrimeType.DRUG_USE,
            location,
            level,
            null
        );
    }

    /**
     * Registriert Drogenhandel (aufgerufen vom Trade-System)
     */
    public static void registerDrugDealing(ServerPlayer player, BlockPos location, int amount) {
        if (!(player.level() instanceof ServerLevel level)) return;

        CrimeType crimeType = amount > 5 ? CrimeType.DRUG_DEALING_LARGE : CrimeType.DRUG_DEALING_SMALL;

        WitnessManager.getManager(level).registerCrime(
            player,
            crimeType,
            location,
            level,
            null
        );
    }

    // ═══════════════════════════════════════════════════════════
    // OTHER CRIMES
    // ═══════════════════════════════════════════════════════════

    /**
     * Registriert Vandalismus
     */
    public static void registerVandalism(ServerPlayer player, BlockPos location) {
        if (!(player.level() instanceof ServerLevel level)) return;

        WitnessManager.getManager(level).registerCrime(
            player,
            CrimeType.VANDALISM,
            location,
            level,
            null
        );
    }

    /**
     * Registriert Hausfriedensbruch
     */
    public static void registerTrespassing(ServerPlayer player, BlockPos location) {
        if (!(player.level() instanceof ServerLevel level)) return;

        WitnessManager.getManager(level).registerCrime(
            player,
            CrimeType.TRESPASSING,
            location,
            level,
            null
        );
    }

    /**
     * Registriert Betrug
     */
    public static void registerFraud(ServerPlayer player, BlockPos location, @Nullable UUID victimUUID) {
        if (!(player.level() instanceof ServerLevel level)) return;

        WitnessManager.getManager(level).registerCrime(
            player,
            CrimeType.FRAUD,
            location,
            level,
            victimUUID
        );
    }

    /**
     * Registriert Flucht vor der Polizei
     */
    public static void registerEvadingPolice(ServerPlayer player, BlockPos location) {
        if (!(player.level() instanceof ServerLevel level)) return;

        WitnessManager.getManager(level).registerCrime(
            player,
            CrimeType.EVADING_POLICE,
            location,
            level,
            null
        );
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITY
    // ═══════════════════════════════════════════════════════════

    /**
     * Prüft ob der Spieler eine Waffe hält
     */
    private static boolean isHoldingWeapon(Player player) {
        var mainHand = player.getMainHandItem();

        // Prüfe auf Schwerter, Äxte, etc.
        return mainHand.getItem() instanceof net.minecraft.world.item.SwordItem ||
               mainHand.getItem() instanceof net.minecraft.world.item.AxeItem ||
               mainHand.getItem() instanceof net.minecraft.world.item.TridentItem ||
               mainHand.getItem().getDescriptionId().contains("sword") ||
               mainHand.getItem().getDescriptionId().contains("knife") ||
               mainHand.getItem().getDescriptionId().contains("gun") ||
               mainHand.getItem().getDescriptionId().contains("weapon");
    }

    /**
     * Registriert ein generisches Verbrechen
     */
    public static void registerCrime(ServerPlayer player, CrimeType crimeType,
                                     BlockPos location, @Nullable UUID victimUUID) {
        if (!(player.level() instanceof ServerLevel level)) return;

        WitnessManager.getManager(level).registerCrime(
            player,
            crimeType,
            location,
            level,
            victimUUID
        );
    }
}
