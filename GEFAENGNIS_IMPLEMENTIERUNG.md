# Gefängnis-System - 3 Implementierungsvorschläge

## Aktuelle Situation

Das bestehende Crime-System hat bereits eine **Basis-Inhaftierung**:
- Spieler wird zum Respawn-Punkt teleportiert
- Effekte: Blindheit, Langsamkeit, kein Springen
- Zeitbasierte Entlassung (`JailReleaseTime`)
- Flucht-Prävention (Teleport zurück bei >100 Blocks)

**Limitierungen:**
- Kein echtes Gefängnis-Gebäude
- Keine Zellen-Zuweisung
- Keine Wächter-NPCs
- Keine Interaktionen (Arbeit, Kaution)

---

## Vorschlag 1: Plot-basiertes Gefängnis (Empfohlen)

### Konzept
Das Gefängnis wird als **GOVERNMENT-PlotType** mit Unter-Bereichen (Zellen) implementiert.

### Vorteile
- ✅ Nutzt bestehende PlotManager-Infrastruktur
- ✅ Zellen sind PlotArea-Objekte (bereits implementiert)
- ✅ Automatische Grenzen-Erkennung
- ✅ Admin-Tools bereits vorhanden
- ✅ Erweiterbar für mehrere Gefängnisse

### Struktur

```
Gefängnis-Plot (PlotType.GOVERNMENT)
├── Eingangsbereich
├── Zellentrakt A (Niedrige Sicherheit - WantedLevel 1-2)
│   ├── Zelle A1 (PlotArea)
│   ├── Zelle A2 (PlotArea)
│   └── Zelle A3 (PlotArea)
├── Zellentrakt B (Hohe Sicherheit - WantedLevel 3-5)
│   ├── Zelle B1 (PlotArea)
│   └── Zelle B2 (PlotArea)
├── Gefängnishof
├── Kantine
└── Arbeitsbereich
```

### Neue Dateien

#### `PrisonPlotManager.java`
```java
package de.rolandsw.schedulemc.npc.crime;

public class PrisonPlotManager {
    private static final Map<String, PrisonData> prisons = new HashMap<>();
    private static final Map<UUID, CellAssignment> prisonerCells = new HashMap<>();

    public static class PrisonData {
        private String prisonPlotId;
        private Map<Integer, List<String>> cellsBySecurityLevel; // WantedLevel -> CellAreaIds
        private BlockPos releasePoint;
        private String guardNpcId;
    }

    public static class CellAssignment {
        private UUID prisonerId;
        private String prisonId;
        private String cellAreaId;
        private long arrestTime;
        private long releaseTime;
        private int originalWantedLevel;
    }

    /**
     * Registriert ein Plot als Gefängnis
     */
    public static void registerPrison(String plotId, BlockPos releasePoint) {
        PlotRegion plot = PlotManager.getPlot(plotId);
        if (plot == null || plot.getPlotType() != PlotType.GOVERNMENT) {
            throw new IllegalArgumentException("Plot muss GOVERNMENT-Typ sein");
        }

        PrisonData prison = new PrisonData();
        prison.prisonPlotId = plotId;
        prison.releasePoint = releasePoint;
        prison.cellsBySecurityLevel = categorizeCells(plot);

        prisons.put(plotId, prison);
        savePrisonData();
    }

    /**
     * Kategorisiert Zellen nach Sicherheitsstufe basierend auf Namen
     * Zellen mit "max" im Namen = Stufe 5, "high" = 4, etc.
     */
    private static Map<Integer, List<String>> categorizeCells(PlotRegion plot) {
        Map<Integer, List<String>> result = new HashMap<>();

        for (PlotArea area : plot.getSubAreas()) {
            int level = determineSecurityLevel(area.getAreaName());
            result.computeIfAbsent(level, k -> new ArrayList<>()).add(area.getId());
        }

        return result;
    }

    private static int determineSecurityLevel(String cellName) {
        String lower = cellName.toLowerCase();
        if (lower.contains("max") || lower.contains("hochsicherheit")) return 5;
        if (lower.contains("high") || lower.contains("hoch")) return 4;
        if (lower.contains("medium") || lower.contains("mittel")) return 3;
        if (lower.contains("low") || lower.contains("niedrig")) return 2;
        return 1; // Standard
    }

    /**
     * Weist einem Gefangenen eine Zelle zu
     */
    public static BlockPos assignCell(ServerPlayer player, int wantedLevel) {
        String prisonId = getDefaultPrisonId();
        PrisonData prison = prisons.get(prisonId);

        if (prison == null) {
            LOGGER.warn("Kein Gefängnis registriert! Nutze Fallback.");
            return player.getRespawnPosition();
        }

        // Finde passende Zelle basierend auf WantedLevel
        int securityLevel = Math.min(wantedLevel, 5);
        String cellId = findAvailableCell(prison, securityLevel);

        if (cellId == null) {
            // Keine freie Zelle - suche niedrigere Sicherheitsstufe
            for (int level = securityLevel - 1; level >= 1; level--) {
                cellId = findAvailableCell(prison, level);
                if (cellId != null) break;
            }
        }

        if (cellId == null) {
            LOGGER.warn("Keine freie Zelle gefunden!");
            return prison.releasePoint;
        }

        // Zuweisung speichern
        PlotRegion prisonPlot = PlotManager.getPlot(prison.prisonPlotId);
        PlotArea cell = prisonPlot.getSubArea(cellId);

        CellAssignment assignment = new CellAssignment();
        assignment.prisonerId = player.getUUID();
        assignment.prisonId = prisonId;
        assignment.cellAreaId = cellId;
        assignment.arrestTime = System.currentTimeMillis();
        assignment.originalWantedLevel = wantedLevel;

        prisonerCells.put(player.getUUID(), assignment);
        savePrisonerData();

        // Zufällige Position innerhalb der Zelle
        return getRandomPositionInCell(cell);
    }

    private static String findAvailableCell(PrisonData prison, int securityLevel) {
        List<String> cells = prison.cellsBySecurityLevel.get(securityLevel);
        if (cells == null || cells.isEmpty()) return null;

        // Finde Zelle die nicht belegt ist
        Set<String> occupiedCells = prisonerCells.values().stream()
            .filter(a -> a.prisonId.equals(prison.prisonPlotId))
            .map(a -> a.cellAreaId)
            .collect(Collectors.toSet());

        for (String cellId : cells) {
            if (!occupiedCells.contains(cellId)) {
                return cellId;
            }
        }

        // Alle belegt - wähle zufällige für Überbelegung
        return cells.get(new Random().nextInt(cells.size()));
    }

    /**
     * Entlässt einen Gefangenen
     */
    public static void releasePrisoner(ServerPlayer player) {
        CellAssignment assignment = prisonerCells.remove(player.getUUID());
        if (assignment != null) {
            PrisonData prison = prisons.get(assignment.prisonId);
            if (prison != null) {
                player.teleportTo(
                    prison.releasePoint.getX(),
                    prison.releasePoint.getY(),
                    prison.releasePoint.getZ()
                );
            }
        }
        CrimeManager.clearWantedLevel(player.getUUID());
        savePrisonerData();
    }

    /**
     * Prüft ob Spieler im Gefängnis ist
     */
    public static boolean isInPrison(UUID playerId) {
        return prisonerCells.containsKey(playerId);
    }

    /**
     * Prüft ob Spieler versucht zu fliehen
     */
    public static boolean isEscapeAttempt(ServerPlayer player) {
        CellAssignment assignment = prisonerCells.get(player.getUUID());
        if (assignment == null) return false;

        PlotRegion prison = PlotManager.getPlot(
            prisons.get(assignment.prisonId).prisonPlotId
        );

        // Außerhalb des Gefängnis-Plots = Fluchtversuch
        return !prison.contains(player.blockPosition());
    }
}
```

### Features

| Feature | Beschreibung |
|---------|--------------|
| **Automatische Zellen-Zuweisung** | Basierend auf WantedLevel |
| **Sicherheitsstufen** | 5 Stufen matching WantedLevel |
| **Überbelegungs-Handling** | Mehrere Spieler pro Zelle möglich |
| **Flucht-Erkennung** | Prüft ob Spieler Plot verlässt |
| **Persistenz** | Speichert Zuweisungen in JSON |

### Admin-Commands

```
/prison register <plotId> <releaseX> <releaseY> <releaseZ>
/prison addcell <plotId> <cellAreaId> <securityLevel>
/prison list
/prison inmates
/prison release <player>
/prison status <player>
```

---

## Vorschlag 2: Dimension-basiertes Gefängnis

### Konzept
Gefängnis ist eine **separate Dimension** mit prozedural generierten Zellen.

### Vorteile
- ✅ Komplett isoliert von der Hauptwelt
- ✅ Keine Fluchtmöglichkeit durch Teleport-Tricks
- ✅ Skaliert automatisch (unendlich viele Zellen)
- ✅ Eigene Regeln (kein Block-Breaking)

### Struktur

```
prison_dimension (ResourceKey)
├── Spawn-Bereich (0, 64, 0)
├── Zellentrakt (generiert)
│   ├── Reihe 0: Zellen 0-9
│   ├── Reihe 1: Zellen 10-19
│   └── ...
├── Gefängnishof (100, 64, 0)
└── Arbeitsbereich (200, 64, 0)
```

### Neue Dateien

#### `PrisonDimension.java`
```java
package de.rolandsw.schedulemc.npc.crime.dimension;

public class PrisonDimension {
    public static final ResourceKey<Level> PRISON_KEY =
        ResourceKey.create(Registries.DIMENSION,
            new ResourceLocation("schedulemc", "prison"));

    public static final ResourceKey<DimensionType> PRISON_TYPE =
        ResourceKey.create(Registries.DIMENSION_TYPE,
            new ResourceLocation("schedulemc", "prison_type"));

    // Zellen-Generierung
    private static final int CELL_WIDTH = 5;
    private static final int CELL_DEPTH = 5;
    private static final int CELL_HEIGHT = 4;
    private static final int CELLS_PER_ROW = 10;
    private static final int CELL_SPACING = 2;

    /**
     * Berechnet Zellen-Position basierend auf Index
     */
    public static BlockPos getCellPosition(int cellIndex) {
        int row = cellIndex / CELLS_PER_ROW;
        int col = cellIndex % CELLS_PER_ROW;

        int x = col * (CELL_WIDTH + CELL_SPACING);
        int z = row * (CELL_DEPTH + CELL_SPACING);
        int y = 64;

        return new BlockPos(x, y, z);
    }

    /**
     * Generiert Zellen-Struktur bei Bedarf
     */
    public static void ensureCellExists(ServerLevel level, int cellIndex) {
        BlockPos cellPos = getCellPosition(cellIndex);

        // Prüfe ob Zelle bereits existiert
        if (level.getBlockState(cellPos).isAir()) {
            generateCell(level, cellPos);
        }
    }

    private static void generateCell(ServerLevel level, BlockPos pos) {
        // Boden
        for (int x = 0; x < CELL_WIDTH; x++) {
            for (int z = 0; z < CELL_DEPTH; z++) {
                level.setBlock(pos.offset(x, -1, z),
                    Blocks.STONE_BRICKS.defaultBlockState(), 3);
            }
        }

        // Wände
        for (int y = 0; y < CELL_HEIGHT; y++) {
            for (int x = 0; x < CELL_WIDTH; x++) {
                // Vorder- und Rückwand
                level.setBlock(pos.offset(x, y, 0),
                    Blocks.IRON_BARS.defaultBlockState(), 3);
                level.setBlock(pos.offset(x, y, CELL_DEPTH - 1),
                    Blocks.STONE_BRICKS.defaultBlockState(), 3);
            }
            for (int z = 0; z < CELL_DEPTH; z++) {
                // Seitenwände
                level.setBlock(pos.offset(0, y, z),
                    Blocks.STONE_BRICKS.defaultBlockState(), 3);
                level.setBlock(pos.offset(CELL_WIDTH - 1, y, z),
                    Blocks.STONE_BRICKS.defaultBlockState(), 3);
            }
        }

        // Decke
        for (int x = 0; x < CELL_WIDTH; x++) {
            for (int z = 0; z < CELL_DEPTH; z++) {
                level.setBlock(pos.offset(x, CELL_HEIGHT, z),
                    Blocks.STONE_BRICKS.defaultBlockState(), 3);
            }
        }

        // Inventar: Bett
        level.setBlock(pos.offset(1, 0, 1),
            Blocks.RED_BED.defaultBlockState(), 3);
    }
}
```

#### `PrisonDimensionManager.java`
```java
package de.rolandsw.schedulemc.npc.crime.dimension;

public class PrisonDimensionManager {
    private static final Map<UUID, Integer> playerCellIndex = new HashMap<>();
    private static AtomicInteger nextCellIndex = new AtomicInteger(0);

    // Original-Position speichern für Entlassung
    private static final Map<UUID, DimensionPosition> originalPositions = new HashMap<>();

    public static class DimensionPosition {
        ResourceKey<Level> dimension;
        BlockPos position;

        public DimensionPosition(ServerPlayer player) {
            this.dimension = player.level().dimension();
            this.position = player.blockPosition();
        }
    }

    /**
     * Teleportiert Spieler ins Gefängnis
     */
    public static void imprisonPlayer(ServerPlayer player, int wantedLevel) {
        // Original-Position speichern
        originalPositions.put(player.getUUID(), new DimensionPosition(player));

        // Zelle zuweisen
        int cellIndex = nextCellIndex.getAndIncrement();
        playerCellIndex.put(player.getUUID(), cellIndex);

        // Zur Gefängnis-Dimension teleportieren
        MinecraftServer server = player.getServer();
        ServerLevel prisonLevel = server.getLevel(PrisonDimension.PRISON_KEY);

        if (prisonLevel == null) {
            LOGGER.error("Gefängnis-Dimension nicht gefunden!");
            return;
        }

        // Zelle generieren falls nötig
        PrisonDimension.ensureCellExists(prisonLevel, cellIndex);

        BlockPos cellPos = PrisonDimension.getCellPosition(cellIndex);

        // Teleport
        player.teleportTo(prisonLevel,
            cellPos.getX() + 2.5, cellPos.getY(), cellPos.getZ() + 2.5,
            player.getYRot(), player.getXRot());

        // Haftzeit berechnen
        int jailSeconds = wantedLevel * 60;
        long releaseTime = player.level().getGameTime() + (jailSeconds * 20L);
        player.getPersistentData().putLong("JailReleaseTime", releaseTime);

        player.sendSystemMessage(Component.literal(
            "§c⚠ Du wurdest für " + jailSeconds + " Sekunden inhaftiert!"));
    }

    /**
     * Entlässt Spieler aus dem Gefängnis
     */
    public static void releasePlayer(ServerPlayer player) {
        DimensionPosition original = originalPositions.remove(player.getUUID());
        playerCellIndex.remove(player.getUUID());

        if (original != null) {
            ServerLevel targetLevel = player.getServer().getLevel(original.dimension);
            if (targetLevel != null) {
                player.teleportTo(targetLevel,
                    original.position.getX(),
                    original.position.getY(),
                    original.position.getZ(),
                    player.getYRot(), player.getXRot());
            }
        }

        player.getPersistentData().remove("JailReleaseTime");
        CrimeManager.clearWantedLevel(player.getUUID());

        player.sendSystemMessage(Component.literal(
            "§a✓ Du wurdest aus dem Gefängnis entlassen!"));
    }

    /**
     * Prüft ob Spieler in Gefängnis-Dimension ist
     */
    public static boolean isInPrisonDimension(ServerPlayer player) {
        return player.level().dimension().equals(PrisonDimension.PRISON_KEY);
    }

    /**
     * Verhindert Dimension-Wechsel während Haft
     */
    @SubscribeEvent
    public static void onDimensionChange(EntityTravelToDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (playerCellIndex.containsKey(player.getUUID())) {
                long releaseTime = player.getPersistentData().getLong("JailReleaseTime");
                if (player.level().getGameTime() < releaseTime) {
                    event.setCanceled(true);
                    player.sendSystemMessage(Component.literal(
                        "§c✗ Du kannst das Gefängnis nicht verlassen!"));
                }
            }
        }
    }
}
```

### Features

| Feature | Beschreibung |
|---------|--------------|
| **Separate Dimension** | Komplett isoliert |
| **Prozedurale Zellen** | Automatisch generiert |
| **Teleport-Schutz** | Kein Dimensions-Wechsel |
| **Original-Position** | Rückkehr zum Arrest-Ort |
| **Unendliche Kapazität** | Skaliert automatisch |

### Gamerules für Gefängnis-Dimension

```java
// In PrisonDimension.java
public static void applyPrisonRules(ServerLevel level) {
    level.getGameRules().getRule(GameRules.RULE_DO_MOB_SPAWNING).set(false, level.getServer());
    level.getGameRules().getRule(GameRules.RULE_DOMOBLOOT).set(false, level.getServer());
    level.getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).set(true, level.getServer());
    // Block-Breaking per Event Handler verhindern
}
```

---

## Vorschlag 3: Manager-basiertes Hybrid-System

### Konzept
Ein **PrisonManager** orchestriert alle Gefängnis-Logik und kann verschiedene Backends nutzen (Plot, Dimension, oder Config).

### Vorteile
- ✅ Höchste Flexibilität
- ✅ Unterstützt alle Gefängnis-Typen
- ✅ Erweiterbar für Arbeit, Kaution, Bewährung
- ✅ Event-System für Mod-Kompatibilität
- ✅ Vollständige Feature-Suite

### Architektur

```
PrisonManager (Zentrale Steuerung)
├── PrisonBackend (Interface)
│   ├── PlotPrisonBackend (Vorschlag 1)
│   ├── DimensionPrisonBackend (Vorschlag 2)
│   └── ConfigPrisonBackend (Einfachste)
├── SentenceManager (Haftzeiten)
├── BailManager (Kaution)
├── WorkManager (Strafarbeit)
├── ParoleManager (Bewährung)
└── GuardManager (Wächter-NPCs)
```

### Neue Dateien

#### `PrisonManager.java` (Hauptklasse)
```java
package de.rolandsw.schedulemc.npc.crime.prison;

public class PrisonManager {
    private static PrisonManager instance;
    private static final Logger LOGGER = LogUtils.getLogger();

    // Backend (austauschbar)
    private PrisonBackend backend;

    // Sub-Manager
    private SentenceManager sentenceManager;
    private BailManager bailManager;
    private WorkManager workManager;
    private ParoleManager paroleManager;
    private GuardManager guardManager;

    // Gefangene
    private Map<UUID, PrisonerData> prisoners = new ConcurrentHashMap<>();

    public static class PrisonerData {
        UUID playerId;
        String playerName;
        int originalWantedLevel;
        long arrestTime;
        long releaseTime;
        long remainingTime;         // Für Pause bei Logout
        String prisonId;
        String cellId;
        double bailAmount;          // Kautions-Betrag
        boolean onParole;           // Bewährung aktiv
        long paroleEndTime;         // Bewährungs-Ende
        int workCredits;            // Arbeitspunkte
        List<String> crimes;        // Begangene Verbrechen
    }

    private PrisonManager() {
        // Standard-Backend aus Config
        String backendType = ModConfigHandler.COMMON.PRISON_BACKEND_TYPE.get();
        switch (backendType) {
            case "dimension" -> backend = new DimensionPrisonBackend();
            case "plot" -> backend = new PlotPrisonBackend();
            default -> backend = new ConfigPrisonBackend();
        }

        sentenceManager = new SentenceManager(this);
        bailManager = new BailManager(this);
        workManager = new WorkManager(this);
        paroleManager = new ParoleManager(this);
        guardManager = new GuardManager(this);
    }

    public static PrisonManager getInstance() {
        if (instance == null) {
            instance = new PrisonManager();
        }
        return instance;
    }

    // ==================== HAUPTFUNKTIONEN ====================

    /**
     * Inhaftiert einen Spieler
     */
    public void imprisonPlayer(ServerPlayer player, int wantedLevel, List<String> crimes) {
        UUID playerId = player.getUUID();

        // Haftzeit berechnen
        int baseSeconds = sentenceManager.calculateSentence(wantedLevel, crimes);
        long releaseTime = player.level().getGameTime() + (baseSeconds * 20L);

        // Kaution berechnen
        double bail = bailManager.calculateBail(wantedLevel, crimes);

        // Gefangenen-Daten erstellen
        PrisonerData data = new PrisonerData();
        data.playerId = playerId;
        data.playerName = player.getName().getString();
        data.originalWantedLevel = wantedLevel;
        data.arrestTime = System.currentTimeMillis();
        data.releaseTime = releaseTime;
        data.remainingTime = baseSeconds * 20L;
        data.bailAmount = bail;
        data.crimes = new ArrayList<>(crimes);

        // Zelle zuweisen über Backend
        CellAssignment cell = backend.assignCell(player, wantedLevel);
        data.prisonId = cell.prisonId();
        data.cellId = cell.cellId();

        prisoners.put(playerId, data);

        // Spieler teleportieren
        backend.teleportToCell(player, cell);

        // Effekte anwenden
        applyPrisonEffects(player, baseSeconds);

        // Event feuern
        MinecraftForge.EVENT_BUS.post(new PlayerImprisonedEvent(player, data));

        // Nachricht
        player.sendSystemMessage(Component.literal(String.format(
            "§c⚔ INHAFTIERT!\n" +
            "§7Haftzeit: §f%d Sekunden\n" +
            "§7Kaution: §f%.2f€\n" +
            "§7Zelle: §f%s",
            baseSeconds, bail, cell.cellId()
        )));

        savePrisonerData();
    }

    /**
     * Entlässt einen Spieler
     */
    public void releasePlayer(ServerPlayer player, ReleaseReason reason) {
        UUID playerId = player.getUUID();
        PrisonerData data = prisoners.remove(playerId);

        if (data == null) return;

        // Zelle freigeben
        backend.releaseCell(data.prisonId, data.cellId);

        // Zum Ausgang teleportieren
        backend.teleportToExit(player, data.prisonId);

        // Effekte entfernen
        removePrisonEffects(player);

        // WantedLevel behandeln basierend auf Grund
        switch (reason) {
            case TIME_SERVED -> CrimeManager.clearWantedLevel(playerId);
            case BAIL_PAID -> CrimeManager.setWantedLevel(playerId,
                Math.max(0, data.originalWantedLevel - 2));
            case PAROLE -> {
                paroleManager.startParole(player, data);
                CrimeManager.setWantedLevel(playerId, data.originalWantedLevel);
            }
            case ESCAPE -> CrimeManager.addWantedLevel(playerId, 2,
                player.level().getDayTime());
            case ADMIN -> CrimeManager.clearWantedLevel(playerId);
        }

        // Event feuern
        MinecraftForge.EVENT_BUS.post(new PlayerReleasedEvent(player, reason));

        // Nachricht
        String reasonText = switch (reason) {
            case TIME_SERVED -> "Haft verbüßt";
            case BAIL_PAID -> "Kaution bezahlt";
            case PAROLE -> "Bewährung";
            case ESCAPE -> "GEFLOHEN!";
            case ADMIN -> "Admin-Entlassung";
        };

        player.sendSystemMessage(Component.literal(
            "§a✓ Entlassen: " + reasonText));

        savePrisonerData();
    }

    /**
     * Zahlt Kaution
     */
    public boolean payBail(ServerPlayer player) {
        PrisonerData data = prisoners.get(player.getUUID());
        if (data == null) return false;

        return bailManager.payBail(player, data);
    }

    /**
     * Beantragt Bewährung
     */
    public boolean requestParole(ServerPlayer player) {
        PrisonerData data = prisoners.get(player.getUUID());
        if (data == null) return false;

        return paroleManager.requestParole(player, data);
    }

    /**
     * Führt Gefängnis-Arbeit aus
     */
    public void doWork(ServerPlayer player, WorkType workType) {
        PrisonerData data = prisoners.get(player.getUUID());
        if (data == null) return;

        workManager.performWork(player, data, workType);
    }

    // ==================== TICK HANDLER ====================

    public void onServerTick(ServerLevel level) {
        long currentTick = level.getGameTime();

        for (Map.Entry<UUID, PrisonerData> entry : prisoners.entrySet()) {
            UUID playerId = entry.getKey();
            PrisonerData data = entry.getValue();

            ServerPlayer player = level.getServer()
                .getPlayerList().getPlayer(playerId);

            if (player == null) {
                // Offline - Zeit pausieren
                continue;
            }

            // Zeit abgelaufen?
            if (currentTick >= data.releaseTime) {
                releasePlayer(player, ReleaseReason.TIME_SERVED);
                continue;
            }

            // Fluchtversuch prüfen
            if (backend.isEscapeAttempt(player, data)) {
                handleEscapeAttempt(player, data);
            }

            // Wächter-Updates
            guardManager.update(player, data);
        }
    }

    private void handleEscapeAttempt(ServerPlayer player, PrisonerData data) {
        // Zurück teleportieren
        CellAssignment cell = new CellAssignment(data.prisonId, data.cellId, null);
        backend.teleportToCell(player, cell);

        // Strafe: +30 Sekunden
        data.releaseTime += 30 * 20L;

        // WantedLevel erhöhen
        CrimeManager.addWantedLevel(player.getUUID(), 1,
            player.level().getDayTime());

        player.sendSystemMessage(Component.literal(
            "§c✗ Fluchtversuch! +30 Sekunden Haftzeit!"));

        // Alarm für Wächter
        guardManager.triggerAlarm(player, data);
    }

    // ==================== HILFSFUNKTIONEN ====================

    private void applyPrisonEffects(ServerPlayer player, int seconds) {
        int ticks = seconds * 20;

        // Langsamkeit
        player.addEffect(new MobEffectInstance(
            MobEffects.MOVEMENT_SLOWDOWN, ticks, 1, false, false));

        // Kein Springen
        player.addEffect(new MobEffectInstance(
            MobEffects.JUMP, ticks, 250, false, false));

        // Mining Fatigue (kein Block-Abbau)
        player.addEffect(new MobEffectInstance(
            MobEffects.DIG_SLOWDOWN, ticks, 4, false, false));
    }

    private void removePrisonEffects(ServerPlayer player) {
        player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        player.removeEffect(MobEffects.JUMP);
        player.removeEffect(MobEffects.DIG_SLOWDOWN);
    }

    public boolean isPrisoner(UUID playerId) {
        return prisoners.containsKey(playerId);
    }

    public PrisonerData getPrisonerData(UUID playerId) {
        return prisoners.get(playerId);
    }

    public Collection<PrisonerData> getAllPrisoners() {
        return Collections.unmodifiableCollection(prisoners.values());
    }
}
```

#### `SentenceManager.java` (Haftzeit-Berechnung)
```java
package de.rolandsw.schedulemc.npc.crime.prison;

public class SentenceManager {
    private final PrisonManager prisonManager;

    // Basis-Haftzeiten pro Verbrechen (in Sekunden)
    private static final Map<String, Integer> CRIME_SENTENCES = Map.of(
        "DRUG_POSSESSION", 60,
        "DRUG_TRAFFICKING", 180,
        "ASSAULT", 120,
        "THEFT", 90,
        "MURDER", 300,
        "ESCAPE", 120,
        "RESISTING_ARREST", 60
    );

    public SentenceManager(PrisonManager manager) {
        this.prisonManager = manager;
    }

    /**
     * Berechnet Gesamthaftzeit
     */
    public int calculateSentence(int wantedLevel, List<String> crimes) {
        // Basis: WantedLevel * 60 Sekunden
        int baseTime = wantedLevel * 60;

        // + Zusatzzeit pro Verbrechen
        int crimeTime = crimes.stream()
            .mapToInt(crime -> CRIME_SENTENCES.getOrDefault(crime, 30))
            .sum();

        // Gesamtzeit mit Obergrenze
        int totalTime = baseTime + crimeTime;
        int maxTime = ModConfigHandler.COMMON.PRISON_MAX_SENTENCE_SECONDS.get();

        return Math.min(totalTime, maxTime);
    }

    /**
     * Reduziert Haftzeit durch Arbeit
     */
    public void reduceSentence(PrisonerData data, int seconds) {
        data.releaseTime -= seconds * 20L;
        data.remainingTime -= seconds * 20L;
    }
}
```

#### `BailManager.java` (Kaution)
```java
package de.rolandsw.schedulemc.npc.crime.prison;

public class BailManager {
    private final PrisonManager prisonManager;

    public BailManager(PrisonManager manager) {
        this.prisonManager = manager;
    }

    /**
     * Berechnet Kautionsbetrag
     */
    public double calculateBail(int wantedLevel, List<String> crimes) {
        // Basis: WantedLevel * 1000€
        double baseBail = wantedLevel * 1000.0;

        // Multiplikator für schwere Verbrechen
        double multiplier = 1.0;
        if (crimes.contains("MURDER")) multiplier = 3.0;
        else if (crimes.contains("DRUG_TRAFFICKING")) multiplier = 2.0;
        else if (crimes.contains("ASSAULT")) multiplier = 1.5;

        return baseBail * multiplier;
    }

    /**
     * Zahlt Kaution
     */
    public boolean payBail(ServerPlayer player, PrisonerData data) {
        // Mindestens 1/3 der Zeit muss verbüßt sein
        long servedTime = player.level().getGameTime() -
            (data.releaseTime - data.remainingTime);
        long requiredTime = data.remainingTime / 3;

        if (servedTime < requiredTime) {
            player.sendSystemMessage(Component.literal(
                "§c✗ Du musst mindestens 1/3 deiner Haftzeit verbüßen!"));
            return false;
        }

        // Geld prüfen
        double bail = data.bailAmount;
        if (!EconomyManager.hasBalance(player.getUUID(), bail)) {
            player.sendSystemMessage(Component.literal(
                "§c✗ Nicht genug Geld für Kaution: " + bail + "€"));
            return false;
        }

        // Kaution abziehen
        EconomyManager.withdraw(player.getUUID(), bail);

        // Entlassen
        prisonManager.releasePlayer(player, ReleaseReason.BAIL_PAID);

        return true;
    }
}
```

#### `WorkManager.java` (Gefängnis-Arbeit)
```java
package de.rolandsw.schedulemc.npc.crime.prison;

public class WorkManager {
    private final PrisonManager prisonManager;
    private final SentenceManager sentenceManager;

    public enum WorkType {
        MINING(30, "Bergbau"),           // -30 Sek pro Einheit
        CLEANING(20, "Reinigung"),       // -20 Sek pro Einheit
        COOKING(25, "Kochen"),           // -25 Sek pro Einheit
        LAUNDRY(15, "Wäsche");           // -15 Sek pro Einheit

        final int timeReduction;
        final String displayName;

        WorkType(int reduction, String name) {
            this.timeReduction = reduction;
            this.displayName = name;
        }
    }

    public WorkManager(PrisonManager manager) {
        this.prisonManager = manager;
        this.sentenceManager = manager.getSentenceManager();
    }

    /**
     * Führt Arbeit aus und reduziert Haftzeit
     */
    public void performWork(ServerPlayer player, PrisonerData data, WorkType workType) {
        // Cooldown prüfen (1 Arbeit pro 5 Minuten)
        long lastWork = player.getPersistentData().getLong("LastPrisonWork");
        long now = player.level().getGameTime();

        if (now - lastWork < 5 * 60 * 20) { // 5 Minuten in Ticks
            long remaining = (5 * 60 * 20 - (now - lastWork)) / 20;
            player.sendSystemMessage(Component.literal(
                "§c✗ Nächste Arbeit möglich in " + remaining + " Sekunden"));
            return;
        }

        // Arbeit durchführen
        int reduction = workType.timeReduction;
        sentenceManager.reduceSentence(data, reduction);

        data.workCredits++;
        player.getPersistentData().putLong("LastPrisonWork", now);

        player.sendSystemMessage(Component.literal(String.format(
            "§a✓ %s erledigt! Haftzeit reduziert um %d Sekunden.",
            workType.displayName, reduction)));

        // Bonus bei 5+ Arbeiten
        if (data.workCredits >= 5 && data.workCredits % 5 == 0) {
            sentenceManager.reduceSentence(data, 60);
            player.sendSystemMessage(Component.literal(
                "§6★ Bonus: -60 Sekunden für gutes Verhalten!"));
        }
    }
}
```

#### `ParoleManager.java` (Bewährung)
```java
package de.rolandsw.schedulemc.npc.crime.prison;

public class ParoleManager {
    private final PrisonManager prisonManager;
    private final Map<UUID, ParoleData> parolees = new ConcurrentHashMap<>();

    public static class ParoleData {
        UUID playerId;
        long paroleEndTime;
        int violations;
        int maxViolations = 3;
        double restrictionRadius = 100.0; // Muss in der Nähe bleiben
        BlockPos paroleOfficerLocation;
    }

    /**
     * Startet Bewährung nach vorzeitiger Entlassung
     */
    public void startParole(ServerPlayer player, PrisonerData prisonData) {
        long remainingTime = prisonData.remainingTime;

        // Bewährungszeit = 2x verbleibende Haftzeit
        long paroleTime = remainingTime * 2;

        ParoleData parole = new ParoleData();
        parole.playerId = player.getUUID();
        parole.paroleEndTime = player.level().getGameTime() + paroleTime;
        parole.paroleOfficerLocation = getParoleOfficerLocation();

        parolees.put(player.getUUID(), parole);

        player.sendSystemMessage(Component.literal(String.format(
            "§e⚖ BEWÄHRUNG GESTARTET\n" +
            "§7Dauer: §f%d Sekunden\n" +
            "§7Melde dich regelmäßig beim Bewährungshelfer!\n" +
            "§7Verstöße: §f0/%d",
            paroleTime / 20, parole.maxViolations)));
    }

    /**
     * Prüft Bewährungsverstöße
     */
    public void checkParole(ServerPlayer player) {
        ParoleData parole = parolees.get(player.getUUID());
        if (parole == null) return;

        long now = player.level().getGameTime();

        // Bewährung abgelaufen?
        if (now >= parole.paroleEndTime) {
            completeParole(player);
            return;
        }

        // Verstöße prüfen
        boolean violation = false;
        String violationReason = "";

        // Zu weit vom Bewährungshelfer
        if (player.distanceToSqr(Vec3.atCenterOf(parole.paroleOfficerLocation))
            > parole.restrictionRadius * parole.restrictionRadius) {
            violation = true;
            violationReason = "Aufenthaltsbereich verlassen";
        }

        // Neues Verbrechen
        if (CrimeManager.getWantedLevel(player.getUUID()) > 0) {
            violation = true;
            violationReason = "Neues Verbrechen begangen";
            parole.violations += 2; // Doppelter Verstoß
        }

        if (violation) {
            parole.violations++;
            player.sendSystemMessage(Component.literal(String.format(
                "§c⚠ BEWÄHRUNGSVERSTOSS: %s\n§7Verstöße: §f%d/%d",
                violationReason, parole.violations, parole.maxViolations)));

            if (parole.violations >= parole.maxViolations) {
                revokeParole(player);
            }
        }
    }

    private void completeParole(ServerPlayer player) {
        parolees.remove(player.getUUID());
        CrimeManager.clearWantedLevel(player.getUUID());

        player.sendSystemMessage(Component.literal(
            "§a✓ Bewährung erfolgreich abgeschlossen! Strafregister gelöscht."));
    }

    private void revokeParole(ServerPlayer player) {
        ParoleData parole = parolees.remove(player.getUUID());

        // Zurück ins Gefängnis mit erhöhter Strafe
        int newWantedLevel = Math.min(5,
            CrimeManager.getWantedLevel(player.getUUID()) + 2);

        prisonManager.imprisonPlayer(player, newWantedLevel,
            List.of("PAROLE_VIOLATION"));

        player.sendSystemMessage(Component.literal(
            "§c✗ Bewährung widerrufen! Zurück ins Gefängnis!"));
    }
}
```

### Features-Übersicht

| Feature | Beschreibung | Manager |
|---------|--------------|---------|
| **Inhaftierung** | Automatische Zellen-Zuweisung | PrisonManager |
| **Haftzeit** | Berechnung basierend auf Verbrechen | SentenceManager |
| **Kaution** | Vorzeitige Entlassung gegen Geld | BailManager |
| **Arbeit** | Haftzeit-Reduzierung durch Arbeit | WorkManager |
| **Bewährung** | Vorzeitige Entlassung mit Auflagen | ParoleManager |
| **Wächter** | NPC-Wächter mit KI | GuardManager |
| **Events** | Forge-Events für Mod-Kompatibilität | PrisonManager |

---

## Vergleich der Vorschläge

| Kriterium | Vorschlag 1 (Plot) | Vorschlag 2 (Dimension) | Vorschlag 3 (Manager) |
|-----------|-------------------|------------------------|----------------------|
| **Komplexität** | Mittel | Hoch | Sehr Hoch |
| **Integration** | Sehr Gut | Mittel | Gut |
| **Erweiterbarkeit** | Gut | Mittel | Sehr Gut |
| **Flucht-Sicherheit** | Gut | Sehr Gut | Abhängig vom Backend |
| **Multi-Server** | Schwierig | Schwierig | Möglich |
| **Performance** | Gut | Sehr Gut | Gut |
| **Admin-Aufwand** | Mittel | Gering | Gering |
| **Feature-Tiefe** | Mittel | Basis | Vollständig |

---

## Empfehlung

### Für schnelle Implementierung: **Vorschlag 1 (Plot-basiert)**
- Nutzt bestehende Infrastruktur
- Weniger neue Klassen
- Admin kann Gefängnis mit bestehenden Commands bauen

### Für maximale Isolation: **Vorschlag 2 (Dimension)**
- Spieler können nicht entkommen
- Keine Interaktion mit Hauptwelt
- Ideal für Hardcore-Server

### Für vollständige Feature-Suite: **Vorschlag 3 (Manager)**
- Kaution, Arbeit, Bewährung
- Wächter-NPCs
- Event-System für Erweiterungen
- **Empfohlen für langfristige Entwicklung**

---

## Nächste Schritte

1. **Backend wählen** (Plot, Dimension, oder Config)
2. **PrisonManager implementieren** als zentrale Steuerung
3. **PoliceAIHandler anpassen** für neue Arrest-Logik
4. **Commands erstellen** für Admin-Verwaltung
5. **GUI erstellen** für Spieler-Interaktionen
