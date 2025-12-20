package de.rolandsw.schedulemc.tutorial;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.economy.EconomyManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Zentraler Manager für das Tutorial-System
 *
 * Features:
 * - Verwaltet Tutorial-Fortschritt aller Spieler
 * - Auto-Start für neue Spieler
 * - Schritt-Fortschritt & Belohnungen
 * - Persistierung in JSON
 */
public class TutorialManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static TutorialManager instance;

    private static final String FILE_NAME = "plotmod_tutorials.json";

    private final Map<UUID, TutorialData> tutorials = new ConcurrentHashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path savePath;

    private boolean needsSave = false;

    // Config
    private boolean autoStartForNewPlayers = true;
    private double completionReward = 10000.0;

    // ═══════════════════════════════════════════════════════════
    // SINGLETON
    // ═══════════════════════════════════════════════════════════

    private TutorialManager(MinecraftServer server) {
        this.savePath = server.getServerDirectory().toPath().resolve("config").resolve(FILE_NAME);
        load();
    }

    public static TutorialManager getInstance(MinecraftServer server) {
        if (instance == null) {
            instance = new TutorialManager(server);
        }
        return instance;
    }

    @Nullable
    public static TutorialManager getInstance() {
        return instance;
    }

    // ═══════════════════════════════════════════════════════════
    // PLAYER TUTORIAL MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    /**
     * Holt Tutorial-Daten eines Spielers (erstellt neue falls nicht vorhanden)
     */
    public TutorialData getTutorialData(UUID playerUUID) {
        return tutorials.computeIfAbsent(playerUUID, uuid -> {
            TutorialData data = new TutorialData(uuid);
            needsSave = true;
            return data;
        });
    }

    /**
     * Startet Tutorial für Spieler
     */
    public void startTutorial(ServerPlayer player) {
        TutorialData data = getTutorialData(player.getUUID());

        if (data.isCompleted()) {
            player.sendSystemMessage(Component.literal("§cDu hast das Tutorial bereits abgeschlossen!"));
            player.sendSystemMessage(Component.literal("§7Nutze §6/tutorial reset§7 zum Neustarten."));
            return;
        }

        data.setEnabled(true);
        data.setCurrentStep(TutorialStep.WELCOME);
        needsSave = true;

        player.sendSystemMessage(Component.literal("§6§l═══ TUTORIAL GESTARTET ═══"));
        showCurrentStep(player);
    }

    /**
     * Zeigt aktuellen Tutorial-Schritt an
     */
    public void showCurrentStep(ServerPlayer player) {
        TutorialData data = getTutorialData(player.getUUID());

        if (!data.isEnabled()) {
            player.sendSystemMessage(Component.literal("§7Tutorial ist deaktiviert. §6/tutorial start§7 zum Starten."));
            return;
        }

        TutorialStep step = data.getCurrentStep();

        // Header
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§6§l═══════════════════════════"));
        player.sendSystemMessage(Component.literal(step.getFormattedTitle()));
        player.sendSystemMessage(Component.literal("§6§l═══════════════════════════"));
        player.sendSystemMessage(Component.literal(""));

        // Beschreibung
        for (String line : step.getDescription().split("\n")) {
            player.sendSystemMessage(Component.literal(line));
        }

        // Tasks
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§e§lAUFGABEN:"));
        int taskIndex = 0;
        for (String task : step.getTasks()) {
            boolean completed = data.getTaskProgress(step) > taskIndex;
            String symbol = completed ? "§a✓" : "§7○";
            player.sendSystemMessage(Component.literal(String.format("%s §f%s", symbol, task)));
            taskIndex++;
        }

        // Fortschritt
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal(data.getFormattedProgress()));
        player.sendSystemMessage(Component.literal(""));

        // Belohnung
        if (step.getReward() > 0) {
            player.sendSystemMessage(Component.literal(String.format("§7Belohnung: §6+%.0f€", step.getReward())));
        }

        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§8Befehle: §7/tutorial next | /tutorial skip | /tutorial quit"));
        player.sendSystemMessage(Component.literal("§6§l═══════════════════════════"));
    }

    /**
     * Markiert aktuellen Schritt als abgeschlossen und geht weiter
     */
    public void completeCurrentStep(ServerPlayer player) {
        TutorialData data = getTutorialData(player.getUUID());

        if (!data.isEnabled()) {
            return;
        }

        TutorialStep completedStep = data.getCurrentStep();

        // Gebe Belohnung
        if (completedStep.getReward() > 0) {
            EconomyManager.deposit(player.getUUID(), completedStep.getReward());
            player.sendSystemMessage(Component.literal(String.format("§a§l✓ Belohnung erhalten: +%.0f€", completedStep.getReward())));
        }

        // Gehe zum nächsten Schritt
        boolean hasNext = data.advanceToNextStep();
        needsSave = true;

        if (hasNext) {
            player.sendSystemMessage(Component.literal("§a§lSchritt abgeschlossen!"));
            player.sendSystemMessage(Component.literal(""));

            // Zeige nächsten Schritt
            showCurrentStep(player);
        } else {
            // Tutorial fertig
            completeTutorial(player);
        }
    }

    /**
     * Schließt Tutorial komplett ab
     */
    private void completeTutorial(ServerPlayer player) {
        TutorialData data = getTutorialData(player.getUUID());

        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§6§l═══════════════════════════"));
        player.sendSystemMessage(Component.literal("§a§lTUTORIAL ABGESCHLOSSEN!"));
        player.sendSystemMessage(Component.literal("§6§l═══════════════════════════"));
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§7Glückwunsch! Du hast das Tutorial"));
        player.sendSystemMessage(Component.literal("§7erfolgreich abgeschlossen!"));
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal(String.format("§6§lAbschluss-Belohnung: +%.0f€", completionReward)));
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§eViel Erfolg in ScheduleMC!"));
        player.sendSystemMessage(Component.literal("§6§l═══════════════════════════"));

        // Finale Belohnung
        EconomyManager.deposit(player.getUUID(), completionReward);

        data.setEnabled(false);
        needsSave = true;
    }

    /**
     * Überspringt aktuellen Schritt
     */
    public void skipCurrentStep(ServerPlayer player) {
        TutorialData data = getTutorialData(player.getUUID());

        if (!data.isEnabled()) {
            player.sendSystemMessage(Component.literal("§cTutorial ist nicht aktiv!"));
            return;
        }

        TutorialStep skipped = data.getCurrentStep();
        boolean hasNext = data.skipCurrentStep();
        needsSave = true;

        player.sendSystemMessage(Component.literal(String.format("§7Schritt übersprungen: %s", skipped.getTitle())));

        if (hasNext) {
            showCurrentStep(player);
        } else {
            completeTutorial(player);
        }
    }

    /**
     * Beendet Tutorial (ohne Belohnungen)
     */
    public void quitTutorial(ServerPlayer player) {
        TutorialData data = getTutorialData(player.getUUID());

        data.setEnabled(false);
        needsSave = true;

        player.sendSystemMessage(Component.literal("§cTutorial beendet."));
        player.sendSystemMessage(Component.literal("§7Nutze §6/tutorial start§7 zum Fortsetzen."));
    }

    /**
     * Setzt Tutorial zurück
     */
    public void resetTutorial(ServerPlayer player) {
        TutorialData data = getTutorialData(player.getUUID());

        data.reset();
        needsSave = true;

        player.sendSystemMessage(Component.literal("§aTutorial zurückgesetzt!"));
        player.sendSystemMessage(Component.literal("§7Nutze §6/tutorial start§7 zum Starten."));
    }

    /**
     * Zeigt Tutorial-Status
     */
    public void showStatus(ServerPlayer player) {
        TutorialData data = getTutorialData(player.getUUID());

        player.sendSystemMessage(Component.literal("§6§l═══ TUTORIAL STATUS ═══"));
        player.sendSystemMessage(Component.literal(String.format("§7Aktiviert: %s", data.isEnabled() ? "§aJa" : "§cNein")));
        player.sendSystemMessage(Component.literal(String.format("§7Aktueller Schritt: §e%s", data.getCurrentStep().getTitle())));
        player.sendSystemMessage(Component.literal(String.format("§7Fortschritt: %s", data.getFormattedProgress())));

        if (data.isCompleted()) {
            long duration = data.getCompletionTime() - data.getStartTime();
            long minutes = duration / 60000;
            player.sendSystemMessage(Component.literal(String.format("§7Abgeschlossen in: §e%d Minuten", minutes)));
        }
    }

    // ═══════════════════════════════════════════════════════════
    // AUTO-START FOR NEW PLAYERS
    // ═══════════════════════════════════════════════════════════

    /**
     * Prüft ob Spieler neu ist und startet Tutorial automatisch
     */
    public void checkAutoStart(ServerPlayer player) {
        if (!autoStartForNewPlayers) {
            return;
        }

        UUID playerUUID = player.getUUID();

        // Wenn Spieler noch keine Tutorial-Daten hat
        if (!tutorials.containsKey(playerUUID)) {
            TutorialData data = getTutorialData(playerUUID);
            startTutorial(player);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // CONFIGURATION
    // ═══════════════════════════════════════════════════════════

    public void setAutoStartForNewPlayers(boolean autoStart) {
        this.autoStartForNewPlayers = autoStart;
    }

    public void setCompletionReward(double reward) {
        this.completionReward = reward;
    }

    // ═══════════════════════════════════════════════════════════
    // PERSISTENCE
    // ═══════════════════════════════════════════════════════════

    private void load() {
        if (!Files.exists(savePath)) {
            LOGGER.info("No tutorial data file found, starting fresh");
            return;
        }

        try (Reader reader = Files.newBufferedReader(savePath)) {
            Type type = new TypeToken<Map<UUID, TutorialData>>(){}.getType();
            Map<UUID, TutorialData> loaded = gson.fromJson(reader, type);

            if (loaded != null) {
                tutorials.putAll(loaded);
                LOGGER.info("Loaded {} tutorial entries", tutorials.size());
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load tutorial data", e);
        }
    }

    public void save() {
        if (!needsSave) {
            return;
        }

        try {
            Files.createDirectories(savePath.getParent());

            try (Writer writer = Files.newBufferedWriter(savePath)) {
                gson.toJson(tutorials, writer);
                needsSave = false;
                LOGGER.debug("Saved {} tutorial entries", tutorials.size());
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save tutorial data", e);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // STATISTICS
    // ═══════════════════════════════════════════════════════════

    public int getTotalPlayers() {
        return tutorials.size();
    }

    public int getCompletedTutorials() {
        return (int) tutorials.values().stream().filter(TutorialData::isCompleted).count();
    }

    public int getActiveTutorials() {
        return (int) tutorials.values().stream().filter(TutorialData::isEnabled).count();
    }

    public String getStatistics() {
        return String.format("Tutorials: %d total, %d completed, %d active",
            getTotalPlayers(), getCompletedTutorials(), getActiveTutorials());
    }
}
