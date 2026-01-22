package de.rolandsw.schedulemc.npc.life.dialogue;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.life.core.EmotionState;
import de.rolandsw.schedulemc.npc.life.core.MemoryType;
import de.rolandsw.schedulemc.npc.life.core.NPCLifeData;
import de.rolandsw.schedulemc.npc.life.social.Faction;
import de.rolandsw.schedulemc.npc.life.social.FactionManager;
import de.rolandsw.schedulemc.npc.life.social.RumorNetwork;
import de.rolandsw.schedulemc.npc.life.social.RumorType;
import net.minecraft.server.level.ServerLevel;

import java.util.function.BiConsumer;

/**
 * DialogueAction - Aktionen die durch Dialogentscheidungen ausgelöst werden
 *
 * Aktionen können den NPC-Zustand, Spieler-Reputation oder die Spielwelt beeinflussen.
 */
public class DialogueAction {

    private final String id;
    private final String description;
    private final BiConsumer<DialogueContext, CustomNPCEntity> action;

    public DialogueAction(String id, String description,
                         BiConsumer<DialogueContext, CustomNPCEntity> action) {
        this.id = id;
        this.description = description;
        this.action = action;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void execute(DialogueContext context, CustomNPCEntity npc) {
        action.accept(context, npc);
    }

    // ═══════════════════════════════════════════════════════════
    // EMOTION ACTIONS
    // ═══════════════════════════════════════════════════════════

    public static DialogueAction triggerEmotion(EmotionState emotion, float intensity) {
        return new DialogueAction(
            "emotion_" + emotion.name(),
            "Auslösen: " + emotion.getDisplayName(),
            (ctx, npc) -> {
                NPCLifeData life = npc.getLifeData();
                if (life != null) {
                    life.getEmotions().trigger(emotion, intensity);
                }
            }
        );
    }

    public static DialogueAction makeHappy(float intensity) {
        return triggerEmotion(EmotionState.HAPPY, intensity);
    }

    public static DialogueAction makeAngry(float intensity) {
        return triggerEmotion(EmotionState.ANGRY, intensity);
    }

    public static DialogueAction makeSuspicious(float intensity) {
        return triggerEmotion(EmotionState.SUSPICIOUS, intensity);
    }

    public static DialogueAction makeFearful(float intensity) {
        return triggerEmotion(EmotionState.FEARFUL, intensity);
    }

    // ═══════════════════════════════════════════════════════════
    // MEMORY/TAG ACTIONS
    // ═══════════════════════════════════════════════════════════

    public static DialogueAction addPlayerTag(String tag) {
        return new DialogueAction(
            "add_tag_" + tag,
            "Tag hinzufügen: " + tag,
            (ctx, npc) -> {
                NPCLifeData life = npc.getLifeData();
                if (life != null) {
                    life.getMemory().addPlayerTag(ctx.getPlayer().getUUID(), tag);
                }
            }
        );
    }

    public static DialogueAction removePlayerTag(String tag) {
        return new DialogueAction(
            "remove_tag_" + tag,
            "Tag entfernen: " + tag,
            (ctx, npc) -> {
                NPCLifeData life = npc.getLifeData();
                if (life != null) {
                    life.getMemory().removePlayerTag(ctx.getPlayer().getUUID(), tag);
                }
            }
        );
    }

    public static DialogueAction addMemory(MemoryType type, String description, int importance) {
        return new DialogueAction(
            "add_memory_" + type.name(),
            "Erinnerung: " + description,
            (ctx, npc) -> {
                NPCLifeData life = npc.getLifeData();
                if (life != null) {
                    life.getMemory().addMemory(
                        ctx.getPlayer().getUUID(),
                        type,
                        description,
                        importance
                    );
                }
            }
        );
    }

    // ═══════════════════════════════════════════════════════════
    // FACTION ACTIONS
    // ═══════════════════════════════════════════════════════════

    public static DialogueAction modifyFactionReputation(Faction faction, int amount) {
        return new DialogueAction(
            "faction_rep_" + faction.name() + "_" + amount,
            "Reputation " + (amount > 0 ? "+" : "") + amount + " bei " + faction.getDisplayName(),
            (ctx, npc) -> {
                if (npc.level() instanceof ServerLevel level) {
                    FactionManager.getManager(level).modifyReputation(
                        ctx.getPlayer().getUUID(), faction, amount
                    );
                }
            }
        );
    }

    public static DialogueAction modifyNPCFactionReputation(int amount) {
        return new DialogueAction(
            "npc_faction_rep_" + amount,
            "Reputation " + (amount > 0 ? "+" : "") + amount + " bei NPC-Fraktion",
            (ctx, npc) -> {
                if (npc.level() instanceof ServerLevel level) {
                    Faction npcFaction = Faction.forNPCType(npc.getNpcType());
                    FactionManager.getManager(level).modifyReputation(
                        ctx.getPlayer().getUUID(), npcFaction, amount
                    );
                }
            }
        );
    }

    // ═══════════════════════════════════════════════════════════
    // RUMOR ACTIONS
    // ═══════════════════════════════════════════════════════════

    public static DialogueAction spreadRumor(RumorType type, String details) {
        return new DialogueAction(
            "rumor_" + type.name(),
            "Gerücht verbreiten: " + type.getDisplayName(),
            (ctx, npc) -> {
                if (npc.level() instanceof ServerLevel level) {
                    RumorNetwork.getNetwork(level).createRumor(
                        ctx.getPlayer().getUUID(),
                        type,
                        details,
                        level.getDayTime() / 24000,
                        npc.getNpcData().getNpcUUID()
                    );
                }
            }
        );
    }

    // ═══════════════════════════════════════════════════════════
    // CONTEXT ACTIONS
    // ═══════════════════════════════════════════════════════════

    public static DialogueAction setFlag(String flag) {
        return new DialogueAction(
            "set_flag_" + flag,
            "Flag setzen: " + flag,
            (ctx, npc) -> ctx.setFlag(flag)
        );
    }

    public static DialogueAction clearFlag(String flag) {
        return new DialogueAction(
            "clear_flag_" + flag,
            "Flag löschen: " + flag,
            (ctx, npc) -> ctx.clearFlag(flag)
        );
    }

    public static DialogueAction setVariable(String key, Object value) {
        return new DialogueAction(
            "set_var_" + key,
            "Variable: " + key + " = " + value,
            (ctx, npc) -> ctx.setVariable(key, value)
        );
    }

    // ═══════════════════════════════════════════════════════════
    // TRADE ACTIONS
    // ═══════════════════════════════════════════════════════════

    public static DialogueAction openTradeMenu() {
        return new DialogueAction(
            "open_trade",
            "Handelsmenü öffnen",
            (ctx, npc) -> {
                ctx.setFlag("open_trade_menu");
                ctx.endDialogue();
            }
        );
    }

    public static DialogueAction giveTempDiscount(float discount, int durationMinutes) {
        return new DialogueAction(
            "temp_discount_" + (int)(discount * 100),
            "Temporärer Rabatt: " + (int)(discount * 100) + "%",
            (ctx, npc) -> {
                ctx.setVariable("temp_discount", discount);
                ctx.setVariable("temp_discount_duration", durationMinutes);
            }
        );
    }

    // ═══════════════════════════════════════════════════════════
    // DIALOGUE FLOW ACTIONS
    // ═══════════════════════════════════════════════════════════

    public static DialogueAction endDialogue() {
        return new DialogueAction(
            "end_dialogue",
            "Dialog beenden",
            (ctx, npc) -> ctx.endDialogue()
        );
    }

    public static DialogueAction jumpToNode(String nodeId) {
        return new DialogueAction(
            "jump_to_" + nodeId,
            "Springe zu: " + nodeId,
            (ctx, npc) -> ctx.setNextNodeId(nodeId)
        );
    }

    // ═══════════════════════════════════════════════════════════
    // QUEST ACTIONS
    // ═══════════════════════════════════════════════════════════

    public static DialogueAction startQuest(String questId) {
        return new DialogueAction(
            "start_quest_" + questId,
            "Quest starten: " + questId,
            (ctx, npc) -> {
                ctx.setFlag("quest_started_" + questId);
                ctx.setVariable("active_quest", questId);
            }
        );
    }

    public static DialogueAction completeQuest(String questId) {
        return new DialogueAction(
            "complete_quest_" + questId,
            "Quest abschließen: " + questId,
            (ctx, npc) -> {
                ctx.setFlag("quest_completed_" + questId);
                ctx.clearFlag("quest_started_" + questId);
            }
        );
    }

    // ═══════════════════════════════════════════════════════════
    // COMBINATORS
    // ═══════════════════════════════════════════════════════════

    public static DialogueAction sequence(DialogueAction... actions) {
        return new DialogueAction(
            "sequence",
            "Sequenz von Aktionen",
            (ctx, npc) -> {
                for (DialogueAction action : actions) {
                    action.execute(ctx, npc);
                }
            }
        );
    }

    public static DialogueAction conditional(DialogueCondition condition,
                                            DialogueAction ifTrue,
                                            DialogueAction ifFalse) {
        return new DialogueAction(
            "conditional",
            "Bedingte Aktion",
            (ctx, npc) -> {
                if (condition.test(ctx, npc)) {
                    ifTrue.execute(ctx, npc);
                } else if (ifFalse != null) {
                    ifFalse.execute(ctx, npc);
                }
            }
        );
    }

    // ═══════════════════════════════════════════════════════════
    // NO-OP
    // ═══════════════════════════════════════════════════════════

    public static DialogueAction none() {
        return new DialogueAction("none", "Keine Aktion", (ctx, npc) -> {});
    }
}
