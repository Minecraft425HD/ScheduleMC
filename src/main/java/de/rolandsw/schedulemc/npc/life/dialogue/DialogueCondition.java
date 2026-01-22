package de.rolandsw.schedulemc.npc.life.dialogue;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.life.core.EmotionState;
import de.rolandsw.schedulemc.npc.life.core.NPCLifeData;
import de.rolandsw.schedulemc.npc.life.social.Faction;
import de.rolandsw.schedulemc.npc.life.social.FactionManager;
import de.rolandsw.schedulemc.npc.life.social.FactionRelation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiPredicate;

/**
 * DialogueCondition - Bedingungen für Dialogoptionen
 *
 * Bestimmt ob eine Dialogoption verfügbar/sichtbar ist.
 */
public class DialogueCondition {

    private final String id;
    private final String description;
    private final BiPredicate<DialogueContext, CustomNPCEntity> predicate;

    public DialogueCondition(String id, String description,
                             BiPredicate<DialogueContext, CustomNPCEntity> predicate) {
        this.id = id;
        this.description = description;
        this.predicate = predicate;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public boolean test(DialogueContext context, CustomNPCEntity npc) {
        return predicate.test(context, npc);
    }

    // ═══════════════════════════════════════════════════════════
    // STANDARD CONDITIONS - EMOTIONS
    // ═══════════════════════════════════════════════════════════

    public static DialogueCondition npcIsHappy() {
        return new DialogueCondition("npc_happy", "NPC ist glücklich",
            (ctx, npc) -> {
                NPCLifeData life = npc.getLifeData();
                return life != null && life.getEmotions().getCurrentEmotion() == EmotionState.HAPPY;
            });
    }

    public static DialogueCondition npcIsAngry() {
        return new DialogueCondition("npc_angry", "NPC ist verärgert",
            (ctx, npc) -> {
                NPCLifeData life = npc.getLifeData();
                return life != null && life.getEmotions().getCurrentEmotion() == EmotionState.ANGRY;
            });
    }

    public static DialogueCondition npcIsFearful() {
        return new DialogueCondition("npc_fearful", "NPC hat Angst",
            (ctx, npc) -> {
                NPCLifeData life = npc.getLifeData();
                return life != null && life.getEmotions().getCurrentEmotion() == EmotionState.FEARFUL;
            });
    }

    public static DialogueCondition npcIsSuspicious() {
        return new DialogueCondition("npc_suspicious", "NPC ist misstrauisch",
            (ctx, npc) -> {
                NPCLifeData life = npc.getLifeData();
                return life != null && life.getEmotions().getCurrentEmotion() == EmotionState.SUSPICIOUS;
            });
    }

    // ═══════════════════════════════════════════════════════════
    // STANDARD CONDITIONS - REPUTATION
    // ═══════════════════════════════════════════════════════════

    public static DialogueCondition playerHasTag(String tag) {
        return new DialogueCondition("player_tag_" + tag, "Spieler hat Tag: " + tag,
            (ctx, npc) -> {
                NPCLifeData life = npc.getLifeData();
                return life != null && life.getMemory().playerHasTag(ctx.getPlayer().getUUID(), tag);
            });
    }

    public static DialogueCondition playerDoesNotHaveTag(String tag) {
        return new DialogueCondition("player_no_tag_" + tag, "Spieler hat nicht Tag: " + tag,
            (ctx, npc) -> {
                NPCLifeData life = npc.getLifeData();
                return life == null || !life.getMemory().playerHasTag(ctx.getPlayer().getUUID(), tag);
            });
    }

    public static DialogueCondition factionReputationAtLeast(Faction faction, int minRep) {
        return new DialogueCondition("faction_rep_" + faction.name() + "_" + minRep,
            "Mindestens " + minRep + " Reputation bei " + faction.getDisplayName(),
            (ctx, npc) -> {
                if (!(npc.level() instanceof ServerLevel level)) return false;
                FactionRelation rel = FactionManager.getManager(level)
                    .getRelation(ctx.getPlayer().getUUID(), faction);
                return rel.getReputation() >= minRep;
            });
    }

    public static DialogueCondition factionStandingAtLeast(Faction faction, FactionRelation.FactionStanding minStanding) {
        return new DialogueCondition("faction_standing_" + faction.name() + "_" + minStanding.name(),
            "Mindestens " + minStanding.getDisplayName() + " bei " + faction.getDisplayName(),
            (ctx, npc) -> {
                if (!(npc.level() instanceof ServerLevel level)) return false;
                FactionRelation rel = FactionManager.getManager(level)
                    .getRelation(ctx.getPlayer().getUUID(), faction);
                return rel.getStanding().ordinal() >= minStanding.ordinal();
            });
    }

    // ═══════════════════════════════════════════════════════════
    // STANDARD CONDITIONS - TRAITS
    // ═══════════════════════════════════════════════════════════

    public static DialogueCondition npcCourageAtLeast(int minCourage) {
        return new DialogueCondition("npc_courage_" + minCourage, "NPC Mut >= " + minCourage,
            (ctx, npc) -> {
                NPCLifeData life = npc.getLifeData();
                return life != null && life.getTraits().getCourage() >= minCourage;
            });
    }

    public static DialogueCondition npcHonestyAtLeast(int minHonesty) {
        return new DialogueCondition("npc_honesty_" + minHonesty, "NPC Ehrlichkeit >= " + minHonesty,
            (ctx, npc) -> {
                NPCLifeData life = npc.getLifeData();
                return life != null && life.getTraits().getHonesty() >= minHonesty;
            });
    }

    public static DialogueCondition npcGreedAtLeast(int minGreed) {
        return new DialogueCondition("npc_greed_" + minGreed, "NPC Gier >= " + minGreed,
            (ctx, npc) -> {
                NPCLifeData life = npc.getLifeData();
                return life != null && life.getTraits().getGreed() >= minGreed;
            });
    }

    // ═══════════════════════════════════════════════════════════
    // STANDARD CONDITIONS - PLAYER STATE
    // ═══════════════════════════════════════════════════════════

    public static DialogueCondition playerHasItem(String itemId) {
        return new DialogueCondition("player_has_" + itemId, "Spieler hat Item: " + itemId,
            (ctx, npc) -> {
                for (var stack : ctx.getPlayer().getInventory().items) {
                    if (stack.getItem().getDescriptionId().contains(itemId)) {
                        return true;
                    }
                }
                return false;
            });
    }

    public static DialogueCondition playerHasMoney(int amount) {
        return new DialogueCondition("player_money_" + amount, "Spieler hat mindestens " + amount + " Geld",
            (ctx, npc) -> {
                // Integration mit dem Economy-System des Mods
                // Vereinfacht: Prüfe auf emeralds oder ähnliches
                return true; // TODO: Echte Geldprüfung
            });
    }

    // ═══════════════════════════════════════════════════════════
    // STANDARD CONDITIONS - CONTEXT
    // ═══════════════════════════════════════════════════════════

    public static DialogueCondition hasVisitedNode(String nodeId) {
        return new DialogueCondition("visited_" + nodeId, "Node besucht: " + nodeId,
            (ctx, npc) -> ctx.hasVisitedNode(nodeId));
    }

    public static DialogueCondition hasNotVisitedNode(String nodeId) {
        return new DialogueCondition("not_visited_" + nodeId, "Node nicht besucht: " + nodeId,
            (ctx, npc) -> !ctx.hasVisitedNode(nodeId));
    }

    public static DialogueCondition isFirstConversation() {
        return new DialogueCondition("first_conversation", "Erstes Gespräch",
            (ctx, npc) -> {
                NPCLifeData life = npc.getLifeData();
                if (life == null) return true;
                return life.getMemory().getPlayerProfile(ctx.getPlayer().getUUID())
                    .getTotalTransactions() == 0;
            });
    }

    public static DialogueCondition contextFlagSet(String flag) {
        return new DialogueCondition("flag_" + flag, "Flag gesetzt: " + flag,
            (ctx, npc) -> ctx.hasFlag(flag));
    }

    // ═══════════════════════════════════════════════════════════
    // COMBINATORS
    // ═══════════════════════════════════════════════════════════

    public static DialogueCondition and(DialogueCondition... conditions) {
        return new DialogueCondition("and", "Alle Bedingungen erfüllt",
            (ctx, npc) -> {
                for (DialogueCondition c : conditions) {
                    if (!c.test(ctx, npc)) return false;
                }
                return true;
            });
    }

    public static DialogueCondition or(DialogueCondition... conditions) {
        return new DialogueCondition("or", "Eine Bedingung erfüllt",
            (ctx, npc) -> {
                for (DialogueCondition c : conditions) {
                    if (c.test(ctx, npc)) return true;
                }
                return false;
            });
    }

    public static DialogueCondition not(DialogueCondition condition) {
        return new DialogueCondition("not_" + condition.id, "Nicht: " + condition.description,
            (ctx, npc) -> !condition.test(ctx, npc));
    }

    // ═══════════════════════════════════════════════════════════
    // ALWAYS/NEVER
    // ═══════════════════════════════════════════════════════════

    public static DialogueCondition always() {
        return new DialogueCondition("always", "Immer",
            (ctx, npc) -> true);
    }

    public static DialogueCondition never() {
        return new DialogueCondition("never", "Nie",
            (ctx, npc) -> false);
    }
}
