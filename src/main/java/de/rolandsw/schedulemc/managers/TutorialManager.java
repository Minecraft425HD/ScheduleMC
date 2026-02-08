package de.rolandsw.schedulemc.managers;

import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import de.rolandsw.schedulemc.util.GsonHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tutorial/Onboarding-System fuer neue Spieler.
 *
 * Progressive Freischaltung:
 * Phase 1 (Tag 1):    Grundlagen (Economy, Plot kaufen)
 * Phase 2 (Tag 2-3):  Produktion (1 legale Kette)
 * Phase 3 (Tag 4-5):  NPCs und Handel
 * Phase 4 (Tag 6-7):  Gangs (Einladung erhalten)
 * Phase 5 (Tag 8-10): Illegale Produktion (wenn gewuenscht)
 * Phase 6 (Tag 11+):  Alle Systeme freigeschaltet
 */
public class TutorialManager extends AbstractPersistenceManager<Map<String, TutorialManager.PlayerTutorialData>> {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static volatile TutorialManager instance;
    private static final Object INSTANCE_LOCK = new Object();

    private final ConcurrentHashMap<UUID, PlayerTutorialData> playerData = new ConcurrentHashMap<>();

    // Tutorial-Phasen
    public enum TutorialPhase {
        WELCOME(0, "Willkommen"),
        ECONOMY_BASICS(1, "Wirtschaft Grundlagen"),
        PLOT_SYSTEM(2, "Grundstuecke"),
        PRODUCTION_BASICS(3, "Produktion"),
        NPC_TRADING(4, "NPC Handel"),
        GANG_INTRODUCTION(5, "Gang-System"),
        ADVANCED_PRODUCTION(6, "Fortgeschrittene Produktion"),
        COMPLETED(7, "Abgeschlossen");

        private final int order;
        private final String displayName;

        TutorialPhase(int order, String displayName) {
            this.order = order;
            this.displayName = displayName;
        }

        public int getOrder() { return order; }
        public String getDisplayName() { return displayName; }

        @Nullable
        public TutorialPhase next() {
            TutorialPhase[] values = values();
            int nextOrd = this.ordinal() + 1;
            return nextOrd < values.length ? values[nextOrd] : null;
        }
    }

    // Tutorial-Schritte innerhalb jeder Phase
    public enum TutorialStep {
        // Phase 1: Willkommen
        CHECK_BALANCE("Pruefe dein Guthaben mit /money", TutorialPhase.WELCOME),
        VISIT_BANK("Besuche einen Bankautomaten", TutorialPhase.WELCOME),

        // Phase 2: Economy
        EARN_MONEY("Verdiene dein erstes Geld", TutorialPhase.ECONOMY_BASICS),
        MAKE_TRANSFER("Ueberweise Geld an einen Spieler", TutorialPhase.ECONOMY_BASICS),

        // Phase 3: Plot
        BUY_PLOT("Kaufe dein erstes Grundstueck mit /plot buy", TutorialPhase.PLOT_SYSTEM),
        PLACE_FURNITURE("Platziere etwas auf deinem Grundstueck", TutorialPhase.PLOT_SYSTEM),

        // Phase 4: Produktion
        START_PRODUCTION("Starte deine erste legale Produktion", TutorialPhase.PRODUCTION_BASICS),
        SELL_PRODUCT("Verkaufe ein produziertes Item", TutorialPhase.PRODUCTION_BASICS),

        // Phase 5: NPCs
        TALK_TO_NPC("Sprich mit 3 verschiedenen NPCs", TutorialPhase.NPC_TRADING),
        TRADE_WITH_NPC("Handle mit einem NPC", TutorialPhase.NPC_TRADING),

        // Phase 6: Gangs
        JOIN_OR_CREATE_GANG("Tritt einer Gang bei oder gruende eine", TutorialPhase.GANG_INTRODUCTION),
        COMPLETE_GANG_MISSION("Schliesse eine Gang-Mission ab", TutorialPhase.GANG_INTRODUCTION),

        // Phase 7: Advanced
        START_ILLEGAL_PRODUCTION("Starte eine illegale Produktion", TutorialPhase.ADVANCED_PRODUCTION),
        EXPLORE_MARKET("Pruefe die Marktpreise mit /market", TutorialPhase.ADVANCED_PRODUCTION);

        private final String description;
        private final TutorialPhase phase;

        TutorialStep(String description, TutorialPhase phase) {
            this.description = description;
            this.phase = phase;
        }

        public String getDescription() { return description; }
        public TutorialPhase getPhase() { return phase; }
    }

    // Spieler-Tutorial-Daten
    static class PlayerTutorialData {
        TutorialPhase currentPhase = TutorialPhase.WELCOME;
        Set<String> completedSteps = new HashSet<>();
        long firstJoinTimestamp = System.currentTimeMillis();
        boolean tutorialSkipped = false;

        public boolean isStepCompleted(TutorialStep step) {
            return completedSteps.contains(step.name());
        }

        public void completeStep(TutorialStep step) {
            completedSteps.add(step.name());
        }

        public int getPlayDays() {
            long elapsed = System.currentTimeMillis() - firstJoinTimestamp;
            return Math.max(1, (int) (elapsed / (24L * 60 * 60 * 1000)));
        }
    }

    // ═══════════════════════════════════════════════════════════
    // SINGLETON
    // ═══════════════════════════════════════════════════════════

    private TutorialManager(MinecraftServer server) {
        super(new File(server.getServerDirectory(), "config/schedulemc_tutorials.json"), GsonHelper.get());
        load();
    }

    @Nullable
    public static TutorialManager getInstance() { return instance; }

    public static TutorialManager getInstance(MinecraftServer server) {
        TutorialManager result = instance;
        if (result == null) {
            synchronized (INSTANCE_LOCK) {
                result = instance;
                if (result == null) {
                    instance = result = new TutorialManager(server);
                }
            }
        }
        return result;
    }

    public static void resetInstance() {
        synchronized (INSTANCE_LOCK) {
            if (instance != null) instance.save();
            instance = null;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // TUTORIAL LOGIC
    // ═══════════════════════════════════════════════════════════

    public void onPlayerJoin(ServerPlayer player) {
        UUID uuid = player.getUUID();
        PlayerTutorialData data = playerData.computeIfAbsent(uuid, k -> {
            markDirty();
            return new PlayerTutorialData();
        });

        if (data.tutorialSkipped || data.currentPhase == TutorialPhase.COMPLETED) return;

        // Begruessung mit aktuellem Tutorial-Fortschritt
        sendTutorialMessage(player, data);
    }

    public void completeStep(ServerPlayer player, TutorialStep step) {
        UUID uuid = player.getUUID();
        PlayerTutorialData data = playerData.get(uuid);
        if (data == null || data.tutorialSkipped) return;

        if (!data.isStepCompleted(step)) {
            data.completeStep(step);
            markDirty();

            player.sendSystemMessage(Component.literal(
                "\u00A7a\u2714 Tutorial: \u00A7f" + step.getDescription() + " \u00A7a\u2714"
            ));

            // Pruefe ob Phase abgeschlossen ist
            checkPhaseCompletion(player, data);
        }
    }

    private void checkPhaseCompletion(ServerPlayer player, PlayerTutorialData data) {
        TutorialPhase current = data.currentPhase;

        // Alle Schritte der aktuellen Phase pruefen
        boolean allComplete = true;
        for (TutorialStep step : TutorialStep.values()) {
            if (step.getPhase() == current && !data.isStepCompleted(step)) {
                allComplete = false;
                break;
            }
        }

        if (allComplete) {
            TutorialPhase next = current.next();
            if (next != null) {
                data.currentPhase = next;
                markDirty();

                if (next == TutorialPhase.COMPLETED) {
                    player.sendSystemMessage(Component.literal(
                        "\u00A76\u00A7l\u2B50 Tutorial abgeschlossen! \u2B50\u00A7r\n" +
                        "\u00A77Alle Systeme sind jetzt freigeschaltet. Viel Spass!"
                    ));
                } else {
                    player.sendSystemMessage(Component.literal(
                        "\u00A76\u00A7l\u2605 Neue Phase freigeschaltet: \u00A7f" + next.getDisplayName() + " \u00A76\u00A7l\u2605"
                    ));
                    sendTutorialMessage(player, data);
                }
            }
        }
    }

    public void skipTutorial(ServerPlayer player) {
        UUID uuid = player.getUUID();
        PlayerTutorialData data = playerData.computeIfAbsent(uuid, k -> new PlayerTutorialData());
        data.tutorialSkipped = true;
        data.currentPhase = TutorialPhase.COMPLETED;
        markDirty();

        player.sendSystemMessage(Component.literal(
            "\u00A77Tutorial uebersprungen. Alle Systeme sind freigeschaltet."
        ));
    }

    public TutorialPhase getPlayerPhase(UUID playerUUID) {
        PlayerTutorialData data = playerData.get(playerUUID);
        if (data == null || data.tutorialSkipped) return TutorialPhase.COMPLETED;
        return data.currentPhase;
    }

    private void sendTutorialMessage(ServerPlayer player, PlayerTutorialData data) {
        StringBuilder msg = new StringBuilder();
        msg.append("\u00A76\u00A7l=== Tutorial: ").append(data.currentPhase.getDisplayName()).append(" ===\u00A7r\n");

        for (TutorialStep step : TutorialStep.values()) {
            if (step.getPhase() == data.currentPhase) {
                boolean done = data.isStepCompleted(step);
                msg.append(done ? "\u00A7a\u2714 " : "\u00A7c\u2718 ");
                msg.append("\u00A77").append(step.getDescription()).append("\n");
            }
        }
        msg.append("\u00A78Ueberspringe mit /tutorial skip");

        player.sendSystemMessage(Component.literal(msg.toString()));
    }

    // ═══════════════════════════════════════════════════════════
    // PERSISTENCE
    // ═══════════════════════════════════════════════════════════

    @Override
    protected Type getDataType() {
        return new TypeToken<Map<String, PlayerTutorialData>>(){}.getType();
    }

    @Override
    protected void onDataLoaded(Map<String, PlayerTutorialData> data) {
        playerData.clear();
        for (Map.Entry<String, PlayerTutorialData> entry : data.entrySet()) {
            try {
                playerData.put(UUID.fromString(entry.getKey()), entry.getValue());
            } catch (IllegalArgumentException e) {
                LOGGER.error("Invalid UUID in tutorial data: {}", entry.getKey());
            }
        }
        LOGGER.info("TutorialManager loaded: {} players", playerData.size());
    }

    @Override
    protected Map<String, PlayerTutorialData> getCurrentData() {
        Map<String, PlayerTutorialData> data = new HashMap<>();
        playerData.forEach((uuid, td) -> data.put(uuid.toString(), td));
        return data;
    }

    @Override protected String getComponentName() { return "TutorialManager"; }
    @Override protected String getHealthDetails() { return playerData.size() + " Spieler"; }
    @Override protected void onCriticalLoadFailure() { playerData.clear(); }
}
