package de.rolandsw.schedulemc.npc.life.dialogue;

import de.rolandsw.schedulemc.mission.MissionEventBridge;
import de.rolandsw.schedulemc.mission.MissionStatus;
import de.rolandsw.schedulemc.mission.PlayerMissionManager;
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
            "Trigger: " + emotion.getDisplayName(),
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
            "Add tag: " + tag,
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
            "Spread rumor: " + type.getDisplayName(),
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
            "Clear flag: " + flag,
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
            "Open trade menu",
            (ctx, npc) -> {
                ctx.setFlag("open_trade_menu");
                ctx.endDialogue();
            }
        );
    }

    /**
     * Alias für openTradeMenu - Öffnet den Handel
     */
    public static DialogueAction openTrade() {
        return openTradeMenu();
    }

    /**
     * Teilt Gerüchte mit dem Spieler
     */
    public static DialogueAction shareRumors() {
        return new DialogueAction(
            "share_rumors",
            "Share rumors",
            (ctx, npc) -> {
                if (npc.level() instanceof ServerLevel level) {
                    RumorNetwork network = RumorNetwork.getNetwork(level);  // NOPMD
                    // Gebe dem NPC bekannte Gerüchte an den Spieler weiter
                    ctx.setFlag("rumors_shared");
                    // Die tatsächliche Anzeige der Gerüchte wird vom Dialogue-System behandelt
                }
            }
        );
    }

    /**
     * Startet eine Preisverhandlung
     */
    public static DialogueAction startNegotiation() {
        return new DialogueAction(
            "start_negotiation",
            "Verhandlung starten",
            (ctx, npc) -> {
                ctx.setFlag("negotiation_started");
                ctx.setVariable("base_price", npc.getNpcData() != null ?
                    npc.getNpcData().getWallet() * 0.1 : 100);
            }
        );
    }

    /**
     * Modifiziert die Fraktions-Reputation (einfacher Alias)
     */
    public static DialogueAction modifyFaction(Faction faction, int amount) {
        return modifyFactionReputation(faction, amount);
    }

    /**
     * Alarmiert NPCs in der Nähe
     */
    public static DialogueAction alertNearbyNPCs(String reason) {
        return new DialogueAction(
            "alert_npcs_" + reason,
            "NPCs alarmieren: " + reason,
            (ctx, npc) -> {
                if (npc.level() instanceof ServerLevel level) {
                    // Finde NPCs im Umkreis von 32 Blöcken
                    var nearbyNPCs = level.getEntitiesOfClass(
                        de.rolandsw.schedulemc.npc.entity.CustomNPCEntity.class,
                        npc.getBoundingBox().inflate(32)
                    );

                    for (var nearbyNPC : nearbyNPCs) {
                        if (nearbyNPC != npc && nearbyNPC.getLifeData() != null) {
                            // Setze Emotions-Trigger basierend auf Grund
                            switch (reason) {
                                case "criminal" -> nearbyNPC.getLifeData().getEmotions()
                                    .trigger(EmotionState.SUSPICIOUS, 0.5f);
                                case "dangerous" -> nearbyNPC.getLifeData().getEmotions()
                                    .trigger(EmotionState.FEARFUL, 0.7f);
                                default -> nearbyNPC.getLifeData().getEmotions()
                                    .trigger(EmotionState.SUSPICIOUS, 0.3f);
                            }
                        }
                    }
                }
            }
        );
    }

    /**
     * Startet einen Bestechungsversuch
     */
    public static DialogueAction startBribery() {
        return new DialogueAction(
            "start_bribery",
            "Bestechung starten",
            (ctx, npc) -> {
                ctx.setFlag("bribery_started");
                NPCLifeData life = npc.getLifeData();
                if (life != null) {
                    // Bestechungspreis basiert auf Gier
                    int basePrice = 100;
                    int greedBonus = life.getTraits().getGreed() * 5;
                    ctx.setVariable("bribe_amount", basePrice + greedBonus);
                }
            }
        );
    }

    /**
     * Prüft ob NPC eine Quest hat
     */
    public static DialogueAction checkForQuest() {
        return new DialogueAction(
            "check_quest",
            "Check quest",
            (ctx, npc) -> {
                // Setze Flag wenn Quest verfügbar
                // Die tatsächliche Quest-Logik wird vom Quest-System behandelt
                ctx.setFlag("quest_available");
            }
        );
    }

    /**
     * Bietet eine Quest an
     */
    public static DialogueAction offerQuest() {
        return new DialogueAction(
            "offer_quest",
            "Quest anbieten",
            (ctx, npc) -> {
                ctx.setFlag("quest_offered");
                // Die Quest-Daten werden vom Quest-System bereitgestellt
            }
        );
    }

    /**
     * Öffnet illegalen Handel
     */
    public static DialogueAction openIllegalTrade() {
        return new DialogueAction(
            "open_illegal_trade",
            "Open illegal trade",
            (ctx, npc) -> {
                ctx.setFlag("open_illegal_trade_menu");
                ctx.endDialogue();
            }
        );
    }

    /**
     * Zahlt Geld
     */
    /**
     * Entschuldigung mit Schmerzensgeld: zieht den dynamisch berechneten
     * Betrag ab und versöhnt den NPC vollständig.
     * Setzt Flags: reconciliation_paid / reconciliation_no_money
     */
    /**
     * Legt die Preis-Aufschlüsselung (Persönlichkeit, Beziehung, Markt …)
     * als Dialog-Variable {var:price_breakdown} ab.
     */
    public static DialogueAction explainPrices() {
        return new DialogueAction("explain_prices", "Explain price composition",
            (ctx, npc) -> {
                var player = ctx.getPlayer();
                if (player != null && player.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                    ctx.setVariable("price_breakdown",
                        de.rolandsw.schedulemc.npc.life.economy.PriceModifier
                            .getPriceBreakdown(npc, player, sl, true));
                }
            });
    }

    public static DialogueAction apologizeWithPayment() {
        return new DialogueAction("apologize_payment", "Apologize and pay compensation",
            (ctx, npc) -> {
                var player = ctx.getPlayer();
                if (player == null) return;
                int cost = ReconciliationHelper.compensationFor(npc, player);
                double balance = de.rolandsw.schedulemc.economy.WalletManager.getBalance(player.getUUID());
                if (balance >= cost) {
                    de.rolandsw.schedulemc.economy.WalletManager.removeMoney(player.getUUID(), cost);
                    npc.getNpcData().addMoney(cost);
                    ReconciliationHelper.applyReconciliation(npc, player, true);
                    ctx.setFlag("reconciliation_paid");
                } else {
                    ctx.setFlag("reconciliation_no_money");
                }
            });
    }

    /**
     * Rein verbale Entschuldigung: chance-basiert (Beziehung, Ehrlichkeit,
     * Gier). Setzt Flags: reconciliation_accepted / reconciliation_rejected
     */
    public static DialogueAction apologizeVerbal() {
        return new DialogueAction("apologize_verbal", "Apologize verbally",
            (ctx, npc) -> {
                var player = ctx.getPlayer();
                if (player == null) return;
                var life = npc.getLifeData();
                var traits = life != null ? life.getTraits()
                    : new de.rolandsw.schedulemc.npc.life.core.NPCTraits(0, 0, 0);
                int relLevel = de.rolandsw.schedulemc.npc.personality.NPCRelationshipManager
                    .getInstance().getOrCreateRelationship(npc.getNpcData().getNpcUUID(), player.getUUID())
                    .getRelationshipLevel();
                float chance = ReconciliationHelper.verbalApologyChance(
                    traits.getGreed(), traits.getHonesty(), relLevel);
                if (npc.getRandom().nextFloat() < chance) {
                    ReconciliationHelper.applyReconciliation(npc, player, false);
                    ctx.setFlag("reconciliation_accepted");
                } else {
                    ctx.setFlag("reconciliation_rejected");
                }
            });
    }

    /**
     * Beruhigt den NPC um die angegebene Intensität (für Nicht-Aggressoren).
     */
    public static DialogueAction calmDown(float amount) {
        return new DialogueAction("calm_down", "Calm the NPC",
            (ctx, npc) -> {
                var life = npc.getLifeData();
                if (life != null) {
                    life.getEmotions().calm(amount);
                }
            });
    }

    /**
     * Berechnet das aktuelle Schmerzensgeld und legt es als
     * Dialog-Variable {var:compensation} ab.
     */
    /**
     * Lädt die Daten der offenen Warenanfrage in Context-Variablen
     * ({supply_amount}, {supply_item}, {supply_payment}, {supply_meeting}).
     */
    public static DialogueAction describeSupplyRequest() {
        return new DialogueAction("describe_supply_request", "Warenanfrage beschreiben",
            (ctx, npc) -> {
                var mgr = de.rolandsw.schedulemc.npc.life.quest.SupplyRequestManager.getInstance();
                if (mgr == null || npc.getNpcData() == null || ctx.getPlayer() == null) return;
                mgr.getPendingRequest(npc.getNpcData().getNpcUUID(), ctx.getPlayer().getUUID())
                    .ifPresent(req -> {
                        var item = de.rolandsw.schedulemc.npc.life.quest.SupplyRequestPlanner
                            .itemFromRegistryId(req.itemId);
                        String itemName = item != null
                            ? net.minecraft.network.chat.Component.translatable(item.getDescriptionId()).getString()
                            : req.itemId;
                        ctx.setVariable("supply_amount", req.amount);
                        ctx.setVariable("supply_item", itemName);
                        ctx.setVariable("supply_payment", req.payment);
                        ctx.setVariable("supply_meeting", req.meetingX + ", " + req.meetingY + ", " + req.meetingZ);
                    });
            });
    }

    /** Nimmt die offene Warenanfrage an (erzeugt eine SUPPLY-Quest). */
    public static DialogueAction acceptSupplyRequest() {
        return new DialogueAction("accept_supply_request", "Warenanfrage annehmen",
            (ctx, npc) -> {
                var mgr = de.rolandsw.schedulemc.npc.life.quest.SupplyRequestManager.getInstance();
                if (mgr == null || ctx.getPlayer() == null) return;
                String title = mgr.acceptRequest(ctx.getPlayer(), npc);
                if (title != null) {
                    ctx.setFlag("supply_accepted");
                    ctx.getPlayer().sendSystemMessage(
                        net.minecraft.network.chat.Component.translatable(
                            "supply.schedulemc.accepted", title));
                }
            });
    }

    /** Lehnt die offene Warenanfrage ab. */
    public static DialogueAction declineSupplyRequest() {
        return new DialogueAction("decline_supply_request", "Warenanfrage ablehnen",
            (ctx, npc) -> {
                var mgr = de.rolandsw.schedulemc.npc.life.quest.SupplyRequestManager.getInstance();
                if (mgr == null || ctx.getPlayer() == null) return;
                mgr.declineRequest(ctx.getPlayer(), npc);
                ctx.setFlag("supply_declined");
            });
    }

    public static DialogueAction computeCompensation() {
        return new DialogueAction("compute_compensation", "Compute compensation",
            (ctx, npc) -> {
                var player = ctx.getPlayer();
                if (player != null) {
                    ctx.setVariable("compensation", ReconciliationHelper.compensationFor(npc, player));
                }
            });
    }

    public static DialogueAction payMoney(int amount) {
        return new DialogueAction(
            "pay_money_" + amount,
            "Bezahlen: " + amount + " coins",
            (ctx, npc) -> {
                var player = ctx.getPlayer();
                if (player != null) {
                    // Geld vom Spieler abziehen
                    double balance = de.rolandsw.schedulemc.economy.WalletManager.getBalance(player.getUUID());
                    if (balance >= amount) {
                        de.rolandsw.schedulemc.economy.WalletManager.removeMoney(player.getUUID(), amount);
                        ctx.setFlag("payment_successful");
                    } else {
                        ctx.setFlag("payment_failed");
                    }
                }
            }
        );
    }

    /**
     * Alias für setVariable mit String-Wert
     */
    public static DialogueAction setVariable(String key, String value) {
        return new DialogueAction(
            "set_var_" + key,
            "Variable: " + key + " = " + value,
            (ctx, npc) -> ctx.setVariable(key, value)
        );
    }

    public static DialogueAction giveTempDiscount(float discount, int durationMinutes) {
        return new DialogueAction(
            "temp_discount_" + (int)(discount * 100),
            "Temporary discount: " + (int)(discount * 100) + "%",
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
            "Complete quest: " + questId,
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
    // MISSION ACTIONS
    // ═══════════════════════════════════════════════════════════

    /**
     * Gibt dem Spieler eine Mission (akzeptiert sie automatisch).
     * @param definitionId ID der MissionDefinition aus MissionRegistry
     */
    public static DialogueAction giveMission(String definitionId) {
        return new DialogueAction(
            "give_mission_" + definitionId,
            "Mission vergeben: " + definitionId,
            (ctx, npc) -> {
                PlayerMissionManager mgr = PlayerMissionManager.getInstance();
                if (mgr != null) {
                    mgr.acceptMission(ctx.getPlayer(), definitionId);
                }
            }
        );
    }

    /**
     * Zahlt die Belohnung einer abgeschlossenen Mission aus (COMPLETED → CLAIMED).
     * @param definitionId ID der MissionDefinition
     */
    public static DialogueAction claimMissionReward(String definitionId) {
        return new DialogueAction(
            "claim_mission_" + definitionId,
            "Missionsbelohnung auszahlen: " + definitionId,
            (ctx, npc) -> {
                PlayerMissionManager mgr = PlayerMissionManager.getInstance();
                if (mgr == null) return;
                mgr.getPlayerMissions(ctx.getPlayer().getUUID()).stream()
                    .filter(m -> m.getDefinitionId().equals(definitionId)
                              && m.getStatus() == MissionStatus.COMPLETED)
                    .findFirst()
                    .ifPresent(m -> mgr.claimMission(ctx.getPlayer(), m.getMissionId()));
            }
        );
    }

    /**
     * Feuert ein Missions-Tracking-Event.
     * @param trackingKey Schlüssel, z.B. "package_delivered", "npc_talked"
     */
    public static DialogueAction trackMissionEvent(String trackingKey) {
        return new DialogueAction(
            "track_event_" + trackingKey,
            "Event tracken: " + trackingKey,
            (ctx, npc) -> MissionEventBridge.fireTransactionCompleted(ctx.getPlayer())
        );
    }

    // ═══════════════════════════════════════════════════════════
    // NO-OP
    // ═══════════════════════════════════════════════════════════

    public static DialogueAction none() {
        return new DialogueAction("none", "Keine Aktion", (ctx, npc) -> {});
    }
}
