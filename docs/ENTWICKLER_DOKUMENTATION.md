# ScheduleMC - Entwickler-Dokumentation

## Inhaltsverzeichnis

1. [Architektur-Übersicht](#architektur-übersicht)
2. [Projekt-Setup](#projekt-setup)
3. [Modul-Struktur](#modul-struktur)
4. [Core-Systeme](#core-systeme)
5. [Daten-Persistierung](#daten-persistierung)
6. [Netzwerk & Pakete](#netzwerk--pakete)
7. [Event-System](#event-system)
8. [Client-Server-Architektur](#client-server-architektur)
9. [API-Nutzung](#api-nutzung)
10. [Best Practices](#best-practices)
11. [Erweiterungen entwickeln](#erweiterungen-entwickeln)
12. [Testing](#testing)

---

## Architektur-Übersicht

### High-Level Architektur

```
┌─────────────────────────────────────────────────────────┐
│                    ScheduleMC Mod                        │
├─────────────────────────────────────────────────────────┤
│                                                           │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌─────────┐│
│  │  Plot    │  │ Economy  │  │   NPC    │  │ Tobacco ││
│  │  System  │  │  System  │  │  System  │  │ System  ││
│  └──────────┘  └──────────┘  └──────────┘  └─────────┘│
│       │             │              │             │      │
│  ┌────┴─────────────┴──────────────┴─────────────┴───┐│
│  │              Manager Layer                         ││
│  │  (PlotManager, EconomyManager, NPCData, etc.)     ││
│  └────────────────────────────────────────────────────┘│
│       │                                                 │
│  ┌────┴─────────────────────────────────────────────┐ │
│  │           Data Persistence Layer                  │ │
│  │        (JSON Files, Auto-Save, Dirty Flags)      │ │
│  └──────────────────────────────────────────────────┘ │
│       │                                                 │
│  ┌────┴─────────────────────────────────────────────┐ │
│  │              Network Layer                        │ │
│  │        (Packets, Client-Server Sync)             │ │
│  └──────────────────────────────────────────────────┘ │
│                                                         │
└─────────────────────────────────────────────────────────┘
         │                                   │
    ┌────┴────┐                         ┌───┴────┐
    │ Client  │                         │ Server │
    │ (GUI,   │                         │ (Logic,│
    │  Render)│                         │  Data) │
    └─────────┘                         └────────┘
```

### Design-Prinzipien

1. **Separation of Concerns**: Jedes Modul (Plot, Economy, NPC, Tobacco) ist eigenständig
2. **Thread-Safety**: Verwendung von `ConcurrentHashMap` und synchronisierten Methoden
3. **Event-Driven**: Forge Event-System für lose Kopplung
4. **Client-Server-Separation**: Klare Trennung zwischen Client und Server Code
5. **Data Persistence**: JSON-basierte Speicherung mit Auto-Save
6. **Performance**: Spatial Indexing, Caching, optimierte Algorithmen

---

## Projekt-Setup

### Voraussetzungen

- **JDK 17** (Temurin empfohlen)
- **Gradle 8.1+** (Wrapper inkludiert)
- **IntelliJ IDEA** oder **Eclipse** (mit ForgeGradle Plugin)
- **Git**

### Repository klonen

```bash
git clone https://github.com/YourUsername/ScheduleMC.git
cd ScheduleMC
```

### Projekt importieren

#### IntelliJ IDEA

1. File → Open
2. Wähle `build.gradle`
3. Import as Gradle Project
4. Warte auf Gradle Sync

#### Eclipse

1. File → Import → Gradle → Existing Gradle Project
2. Wähle Root Directory
3. Finish

### Gradle Tasks

```bash
# Build
./gradlew build

# Client starten
./gradlew runClient

# Server starten
./gradlew runServer

# Datengenerierung
./gradlew runData

# JAR erstellen
./gradlew jar
# Output: build/libs/ScheduleMC-1.0.0-alpha.jar

# Workspace bereinigen
./gradlew clean
```

### IDE Run Configurations

#### IntelliJ IDEA

Nach dem Import werden automatisch erstellt:
- **runClient**: Startet Minecraft Client
- **runServer**: Startet Dedicated Server
- **runData**: Data Generators

#### Eclipse

Verwende Gradle Tasks direkt aus dem Gradle View.

---

## Modul-Struktur

### Paket-Übersicht

```
de.rolandsw.schedulemc/
├── ScheduleMC.java              # Main Mod Class
├── ModCreativeTabs.java         # Creative Tabs
│
├── api/                          # Public API
│   └── PlotModAPI.java
│
├── commands/                     # Command System
│   ├── PlotCommand.java
│   ├── MoneyCommand.java
│   ├── DailyCommand.java
│   ├── ShopCommand.java
│   └── ...
│
├── config/                       # Configuration
│   └── ModConfigHandler.java
│
├── economy/                      # Economy System
│   ├── EconomyManager.java       # Core economy logic
│   ├── WalletManager.java        # Wallet system
│   ├── PlayerJoinHandler.java    # Account creation
│   ├── blocks/                   # ATM, Cash blocks
│   ├── items/                    # Cash items
│   ├── menu/                     # GUIs
│   ├── network/                  # Packets
│   └── events/                   # Event handlers
│
├── region/                       # Plot System
│   ├── PlotManager.java          # Plot CRUD
│   ├── PlotRegion.java           # Plot data
│   ├── PlotArea.java             # Apartments
│   ├── PlotSpatialIndex.java     # Performance
│   ├── PlotProtectionHandler.java# Protection
│   └── blocks/                   # Plot blocks
│
├── npc/                          # NPC System
│   ├── entity/
│   │   └── CustomNPCEntity.java  # NPC Entity
│   ├── data/                     # NPC data models
│   ├── commands/                 # NPC commands
│   ├── crime/                    # Crime system
│   ├── events/                   # Event handlers
│   ├── goals/                    # AI Goals
│   ├── menu/                     # Interaction GUIs
│   ├── network/                  # Packets
│   ├── pathfinding/              # Navigation
│   ├── items/                    # NPC items
│   └── client/                   # Rendering
│
├── tobacco/                      # Tobacco System
│   ├── blocks/                   # Plants, processing
│   ├── items/                    # Seeds, leaves, etc.
│   ├── business/                 # Negotiation
│   ├── blockentity/              # Block logic
│   ├── commands/                 # Commands
│   ├── config/                   # Config
│   ├── menu/                     # Packaging GUIs
│   ├── network/                  # Packets
│   └── screen/                   # Screens
│
├── managers/                     # Manager Classes
│   ├── DailyRewardManager.java
│   ├── RentManager.java
│   └── ShopManager.java
│
├── items/                        # Item Registry
│   └── ModItems.java
│
├── events/                       # Global Event Handlers
│
├── client/                       # Client-only Code
│   ├── overlays/                 # HUD Overlays
│   └── screens/                  # GUI Screens
│
├── util/                         # Utilities
│   ├── VersionChecker.java
│   └── GsonHelper.java
│
└── gui/                          # GUI Components
```

---

## Core-Systeme

### 1. Economy System

**Datei**: `de.rolandsw.schedulemc.economy.EconomyManager`

#### Architektur

```java
public class EconomyManager {
    // Thread-safe storage
    private final ConcurrentHashMap<UUID, Double> accounts;

    // Dirty flag for save optimization
    private boolean isDirty = false;

    // Singleton Pattern
    private static EconomyManager instance;

    public static EconomyManager getInstance() {
        if (instance == null) {
            instance = new EconomyManager();
        }
        return instance;
    }
}
```

#### Wichtige Methoden

```java
// Kontostand abrufen
public double getBalance(UUID playerId);

// Guthaben setzen
public void setBalance(UUID playerId, double amount);

// Geld hinzufügen
public void addMoney(UUID playerId, double amount);

// Geld abziehen
public boolean removeMoney(UUID playerId, double amount);

// Transfer zwischen Spielern
public boolean transferMoney(UUID from, UUID to, double amount);

// Speichern
public void save();

// Laden
public void load();
```

#### Thread-Safety

- Verwendet `ConcurrentHashMap` für thread-safe Zugriffe
- Alle Methoden sind synchronized oder verwenden atomare Operationen
- Dirty-Flag verhindert unnötige Speichervorgänge

#### Speicherung

```json
// config/plotmod_economy.json
{
  "550e8400-e29b-41d4-a716-446655440000": 1000.0,
  "6ba7b810-9dad-11d1-80b4-00c04fd430c8": 2500.50
}
```

### 2. Plot System

**Datei**: `de.rolandsw.schedulemc.region.PlotManager`

#### Architektur

```java
public class PlotManager {
    // Plot storage
    private final Map<Integer, PlotRegion> plots;

    // Spatial index for fast lookups
    private final PlotSpatialIndex spatialIndex;

    // Counter for plot IDs
    private int nextPlotId = 1;

    // Singleton
    private static PlotManager instance;
}
```

#### PlotRegion Datenmodell

```java
public class PlotRegion {
    private int id;
    private UUID ownerId;
    private BlockPos pos1, pos2;
    private double price;
    private boolean forSale;
    private Set<UUID> trustedPlayers;
    private List<Rating> ratings;

    // Rent system
    private boolean forRent;
    private double rentPrice;
    private UUID currentRenter;
    private long rentEndTime;

    // Apartments
    private List<PlotArea> apartments;
}
```

#### Spatial Indexing

**Problem**: Bei vielen Plots ist O(n) Lookup zu langsam

**Lösung**: `PlotSpatialIndex` für O(1) Lookup

```java
public class PlotSpatialIndex {
    private final Map<Long, Set<Integer>> chunkToPlots;

    // Konvertiert Position zu Chunk-Key
    private long getChunkKey(int chunkX, int chunkZ);

    // Findet Plot an Position
    public PlotRegion getPlotAt(BlockPos pos);

    // Aktualisiert Index
    public void addPlot(PlotRegion plot);
    public void removePlot(PlotRegion plot);
}
```

**Optimierung**: Plots werden in Chunk-basierten Buckets gespeichert

```
Chunk (0,0) → [Plot1, Plot5, Plot12]
Chunk (0,1) → [Plot3, Plot8]
Chunk (1,0) → [Plot2, Plot7, Plot15]
```

#### Plot-Schutz

**Datei**: `de.rolandsw.schedulemc.region.PlotProtectionHandler`

```java
@Mod.EventBusSubscriber(modid = ScheduleMC.MOD_ID)
public class PlotProtectionHandler {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        // 1. Position ermitteln
        BlockPos pos = event.getPos();

        // 2. Plot finden (O(1) dank Spatial Index)
        PlotRegion plot = PlotManager.getInstance()
            .getPlotAt(pos);

        // 3. Berechtigung prüfen
        if (plot != null && !plot.canBuild(player)) {
            event.setCanceled(true);
            // Nachricht senden
        }
    }

    // Analog für PlaceEvent, EntityInteract, etc.
}
```

### 3. NPC System

**Datei**: `de.rolandsw.schedulemc.npc.entity.CustomNPCEntity`

#### Entity-Struktur

```java
public class CustomNPCEntity extends PathfinderMob {
    // NPC Data
    private NPCData npcData;

    // AI Goals
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MoveToWorkGoal(this));
        this.goalSelector.addGoal(2, new MoveToHomeGoal(this));
        this.goalSelector.addGoal(3, new MoveToLeisureGoal(this));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    // Interaction
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (npcData.getType() == NPCType.MERCHANT) {
            openShop(player);
        } else if (npcData.getType() == NPCType.RESIDENT) {
            openDialogOrStealing(player);
        }
        return InteractionResult.SUCCESS;
    }
}
```

#### NPCData Modell

```java
public class NPCData {
    private UUID uuid;
    private String name;
    private NPCType type;           // RESIDENT, MERCHANT, POLICE
    private NPCPersonality personality; // FRIENDLY, NEUTRAL, AGGRESSIVE, SHY

    // Locations
    private BlockPos homeLocation;
    private BlockPos workLocation;
    private BlockPos leisureLocation;

    // Shop (for merchants)
    private ShopCategory shopCategory;

    // Skin
    private String skinUrl;
}
```

#### AI Goal: MoveToWorkGoal

```java
public class MoveToWorkGoal extends Goal {
    private final CustomNPCEntity npc;
    private BlockPos workPos;

    @Override
    public boolean canUse() {
        // Nur zwischen 9:00 und 17:00 Uhr
        long time = npc.level().getDayTime() % 24000;
        return time >= 3000 && time < 11000;
    }

    @Override
    public void tick() {
        // Navigate to work location
        if (workPos != null) {
            npc.getNavigation().moveTo(
                workPos.getX(),
                workPos.getY(),
                workPos.getZ(),
                1.0
            );
        }
    }
}
```

### 4. Crime & Police System

**Datei**: `de.rolandsw.schedulemc.npc.crime.CrimeManager`

#### Wanted-Level Verwaltung

```java
public class CrimeManager {
    // Player → Wanted Level
    private final Map<UUID, Integer> wantedLevels;

    // Player → Last Update Time
    private final Map<UUID, Long> lastUpdate;

    // Wanted-Level hinzufügen
    public void addWantedLevel(UUID playerId, int stars) {
        int current = wantedLevels.getOrDefault(playerId, 0);
        int newLevel = Math.min(5, current + stars);
        wantedLevels.put(playerId, newLevel);

        // Sync to client
        syncToClient(playerId);
    }

    // Automatischer Abbau (1 Stern pro Tag)
    public void tickWantedLevelDecay() {
        long currentTime = System.currentTimeMillis();

        for (UUID playerId : wantedLevels.keySet()) {
            long lastTime = lastUpdate.getOrDefault(playerId, currentTime);
            long elapsed = currentTime - lastTime;

            // 1 Tag = 24 * 60 * 60 * 1000 ms
            int daysElapsed = (int) (elapsed / 86400000);

            if (daysElapsed > 0) {
                int current = wantedLevels.get(playerId);
                int newLevel = Math.max(0, current - daysElapsed);
                wantedLevels.put(playerId, newLevel);
                lastUpdate.put(playerId, currentTime);

                syncToClient(playerId);
            }
        }
    }
}
```

#### Police AI

**Datei**: `de.rolandsw.schedulemc.npc.events.PoliceAIHandler`

```java
@SubscribeEvent
public static void onLivingUpdate(LivingEvent.LivingTickEvent event) {
    if (event.getEntity() instanceof CustomNPCEntity npc) {
        if (npc.getNPCData().getType() == NPCType.POLICE) {
            // 1. Scan für Spieler mit Wanted-Level
            List<Player> criminals = findCriminalsInRange(npc, 32.0);

            if (!criminals.isEmpty()) {
                // 2. Wähle nächsten Kriminellen
                Player target = criminals.get(0);

                // 3. Verfolge
                npc.getNavigation().moveTo(target, 1.2);

                // 4. Verhaften bei Kontakt
                if (npc.distanceTo(target) < 2.0) {
                    arrestPlayer(npc, target);
                }

                // 5. Verstärkung rufen bei hohem Wanted-Level
                int wantedLevel = CrimeManager.getInstance()
                    .getWantedLevel(target.getUUID());
                if (wantedLevel >= 3) {
                    callBackup(npc, target);
                }
            }
        }
    }
}
```

### 5. Tobacco System

**Datei**: `de.rolandsw.schedulemc.tobacco.blocks.TobaccoPlantBlock`

#### Wachstums-System

```java
public class TobaccoPlantBlock extends CropBlock {
    // 8 Wachstumsstufen (0-7)
    public static final int MAX_AGE = 7;

    @Override
    public void randomTick(BlockState state, ServerLevel level,
                           BlockPos pos, RandomSource random) {
        if (!level.isAreaLoaded(pos, 1)) return;

        // 1. Licht-Check
        if (level.getRawBrightness(pos, 0) >= 9) {
            int age = this.getAge(state);

            if (age < MAX_AGE) {
                // 2. Wachstums-Chance
                float growthSpeed = getGrowthSpeed(this, level, pos);

                if (random.nextInt((int)(25.0F / growthSpeed) + 1) == 0) {
                    // 3. Wachsen
                    level.setBlock(pos, this.getStateForAge(age + 1), 2);
                }
            }
        }
    }

    private static float getGrowthSpeed(Block block, BlockGetter level,
                                        BlockPos pos) {
        // Faktoren: Licht, Wasser, Dünger
        // Implementation siehe TobaccoConfig
    }
}
```

#### Tobacco Pot BlockEntity

**Datei**: `de.rolandsw.schedulemc.tobacco.blockentity.TobaccoPotBlockEntity`

```java
public class TobaccoPotBlockEntity extends BlockEntity {
    // Pot properties
    private SoilType soilType;
    private int waterLevel; // 0-100
    private int fertilizerLevel; // 0-100
    private TobaccoQuality quality;

    // Plant
    private TobaccoType plantedType;
    private int growthStage; // 0-7

    // Tick für Wasser-Verbrauch
    public void tick(Level level, BlockPos pos, BlockState state) {
        if (waterLevel > 0) {
            waterLevel -= 1; // 1% pro Tick (konfigurierbar)
            setChanged();
        }

        if (waterLevel < 20) {
            // Wachstum verlangsamt
        }
    }
}
```

#### Negotiation System

**Datei**: `de.rolandsw.schedulemc.tobacco.business.NegotiationEngine`

```java
public class NegotiationEngine {

    public double calculateOffer(TobaccoPackage product,
                                 NPCData npc,
                                 BusinessMetrics metrics) {
        // 1. Basispreis
        double basePrice = product.getBasePrice();

        // 2. Qualitäts-Multiplikator
        double qualityMult = switch(product.getQuality()) {
            case LOW -> 0.5;
            case MEDIUM -> 1.0;
            case HIGH -> 1.5;
            case PREMIUM -> 2.5;
        };

        // 3. NPC-Persönlichkeit
        double personalityMult = switch(npc.getPersonality()) {
            case FRIENDLY -> 1.2;
            case NEUTRAL -> 1.0;
            case AGGRESSIVE -> 0.8;
            case SHY -> 0.9;
        };

        // 4. Business-Metriken
        double demandMult = metrics.getDemand() / 100.0;
        double reputationMult = metrics.getReputation() / 100.0;

        // 5. Finaler Preis
        return basePrice * qualityMult * personalityMult
               * demandMult * reputationMult;
    }

    public boolean acceptCounterOffer(double offer, double counterOffer,
                                      NPCPersonality personality) {
        double threshold = switch(personality) {
            case FRIENDLY -> 1.2;    // Akzeptiert bis +20%
            case NEUTRAL -> 1.1;     // Akzeptiert bis +10%
            case AGGRESSIVE -> 1.05; // Akzeptiert bis +5%
            case SHY -> 1.15;        // Akzeptiert bis +15%
        };

        return counterOffer <= offer * threshold;
    }
}
```

---

## Daten-Persistierung

### JSON-basierte Speicherung

Alle Mod-Daten werden als JSON gespeichert:

```
config/
├── plotmod_plots.json      # Plots & Apartments
├── plotmod_economy.json    # Spieler-Konten
├── plotmod_daily.json      # Daily Rewards
├── plotmod_shops.json      # Shop-Daten
└── plotmod_crimes.json     # Wanted-Levels
```

### Auto-Save System

**Datei**: `de.rolandsw.schedulemc.ScheduleMC`

```java
public class ScheduleMC {
    private static final int SAVE_INTERVAL_TICKS = 6000; // 5 Minuten
    private int tickCounter = 0;

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            tickCounter++;

            if (tickCounter >= SAVE_INTERVAL_TICKS) {
                // Auto-Save
                EconomyManager.getInstance().save();
                PlotManager.getInstance().save();
                DailyRewardManager.getInstance().save();
                ShopManager.getInstance().save();
                CrimeManager.getInstance().save();

                tickCounter = 0;
            }
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        // Final Save
        saveAllData();
    }
}
```

### Dirty-Flag Optimierung

**Problem**: Unnötige Speichervorgänge bei unveränderten Daten

**Lösung**: Dirty-Flag

```java
public class EconomyManager {
    private boolean isDirty = false;

    public void addMoney(UUID playerId, double amount) {
        // ... logic ...
        isDirty = true; // Markiere als geändert
    }

    public void save() {
        if (!isDirty) {
            return; // Nichts zu speichern
        }

        // Speichere Daten
        saveToFile();
        isDirty = false;
    }
}
```

### GSON-basierte Serialisierung

```java
// Speichern
public void save() {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    String json = gson.toJson(accounts);

    try (FileWriter writer = new FileWriter(saveFile)) {
        writer.write(json);
    } catch (IOException e) {
        LOGGER.error("Failed to save economy data", e);
    }
}

// Laden
public void load() {
    if (!saveFile.exists()) return;

    Gson gson = new Gson();
    try (FileReader reader = new FileReader(saveFile)) {
        Type type = new TypeToken<ConcurrentHashMap<UUID, Double>>(){}.getType();
        accounts.putAll(gson.fromJson(reader, type));
    } catch (IOException e) {
        LOGGER.error("Failed to load economy data", e);
    }
}
```

---

## Netzwerk & Pakete

### Packet-Registrierung

**Datei**: `de.rolandsw.schedulemc.ScheduleMC`

```java
public class ScheduleMC {
    private static final String PROTOCOL_VERSION = "1";
    public static SimpleChannel NETWORK;

    public ScheduleMC() {
        NETWORK = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
        );

        // Register packets
        int id = 0;

        // Economy
        NETWORK.registerMessage(id++, ATMTransactionPacket.class,
            ATMTransactionPacket::encode,
            ATMTransactionPacket::decode,
            ATMTransactionPacket::handle);

        // NPC
        NETWORK.registerMessage(id++, SpawnNPCPacket.class,
            SpawnNPCPacket::encode,
            SpawnNPCPacket::decode,
            SpawnNPCPacket::handle);

        // ... more packets ...
    }
}
```

### Packet-Implementierung

**Beispiel**: `ATMTransactionPacket`

```java
public class ATMTransactionPacket {
    private final TransactionType type;
    private final double amount;

    public ATMTransactionPacket(TransactionType type, double amount) {
        this.type = type;
        this.amount = amount;
    }

    // Encoding
    public static void encode(ATMTransactionPacket packet, FriendlyByteBuf buf) {
        buf.writeEnum(packet.type);
        buf.writeDouble(packet.amount);
    }

    // Decoding
    public static ATMTransactionPacket decode(FriendlyByteBuf buf) {
        return new ATMTransactionPacket(
            buf.readEnum(TransactionType.class),
            buf.readDouble()
        );
    }

    // Handling (Server-side)
    public static void handle(ATMTransactionPacket packet,
                              Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            if (packet.type == TransactionType.WITHDRAW) {
                // Geld abheben
                EconomyManager economy = EconomyManager.getInstance();
                if (economy.removeMoney(player.getUUID(), packet.amount)) {
                    // Gebe Cash-Item
                    giveCashItem(player, packet.amount);
                }
            } else if (packet.type == TransactionType.DEPOSIT) {
                // Geld einzahlen
                // ... implementation ...
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public enum TransactionType {
        WITHDRAW, DEPOSIT
    }
}
```

### Client-Server Synchronisation

**Pattern**: Server authoritative

1. **Client** sendet Packet an Server
2. **Server** validiert Request
3. **Server** führt Aktion aus
4. **Server** sendet Update zurück an Client

**Beispiel**: Money Transfer

```java
// CLIENT: Spieler klickt "Send Money"
ScheduleMC.NETWORK.sendToServer(
    new MoneyTransferPacket(targetPlayer, amount)
);

// SERVER: Handle Packet
public static void handle(MoneyTransferPacket packet, ...) {
    ServerPlayer sender = ctx.get().getSender();

    // 1. Validierung
    if (!EconomyManager.getInstance().hasEnough(sender.getUUID(), packet.amount)) {
        sender.sendSystemMessage(Component.literal("Nicht genug Geld"));
        return;
    }

    // 2. Transaktion
    boolean success = EconomyManager.getInstance().transferMoney(
        sender.getUUID(),
        packet.targetUUID,
        packet.amount
    );

    // 3. Response
    if (success) {
        sender.sendSystemMessage(Component.literal("Geld gesendet"));

        // 4. Sync zu beiden Clients
        ScheduleMC.NETWORK.send(PacketDistributor.PLAYER.with(() -> sender),
            new BalanceUpdatePacket(EconomyManager.getInstance().getBalance(sender.getUUID())));
    }
}
```

---

## Event-System

### Forge Event Bus

ScheduleMC nutzt das Forge Event System für lose Kopplung.

### Event-Handler Beispiele

#### Block-Protection

```java
@Mod.EventBusSubscriber(modid = ScheduleMC.MOD_ID)
public class PlotProtectionHandler {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;

        Player player = event.getPlayer();
        BlockPos pos = event.getPos();

        PlotRegion plot = PlotManager.getInstance().getPlotAt(pos);

        if (plot != null && !plot.canBuild(player.getUUID())) {
            event.setCanceled(true);
            player.displayClientMessage(
                Component.literal("§cDu darfst hier nicht abbauen!"),
                true
            );
        }
    }
}
```

#### Player-Join Handler

```java
@Mod.EventBusSubscriber(modid = ScheduleMC.MOD_ID)
public class PlayerJoinHandler {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        UUID playerId = player.getUUID();

        // 1. Economy: Konto erstellen
        EconomyManager economy = EconomyManager.getInstance();
        if (!economy.hasAccount(playerId)) {
            economy.createAccount(playerId,
                ModConfigHandler.START_BALANCE.get());
        }

        // 2. Daily Rewards: Daten laden
        DailyRewardManager.getInstance().loadPlayerData(playerId);

        // 3. Crime: Wanted-Level sync
        CrimeManager.getInstance().syncToClient(playerId);

        // 4. Version Check: Update-Benachrichtigung
        VersionChecker.notifyUpdate(player);
    }
}
```

### Custom Events

Du kannst eigene Events erstellen:

```java
// Event-Klasse
public class PlotPurchaseEvent extends Event {
    private final PlotRegion plot;
    private final Player buyer;
    private final double price;

    public PlotPurchaseEvent(PlotRegion plot, Player buyer, double price) {
        this.plot = plot;
        this.buyer = buyer;
        this.price = price;
    }

    // Getters...
}

// Event auslösen
MinecraftForge.EVENT_BUS.post(new PlotPurchaseEvent(plot, player, price));

// Event abonnieren
@SubscribeEvent
public static void onPlotPurchase(PlotPurchaseEvent event) {
    // Custom logic
    LOGGER.info("{} bought plot {} for {}€",
        event.getBuyer().getName().getString(),
        event.getPlot().getId(),
        event.getPrice());
}
```

---

## Client-Server-Architektur

### Client-Only Code

**Wichtig**: Client-Code muss mit `@OnlyIn(Dist.CLIENT)` markiert werden

```java
@OnlyIn(Dist.CLIENT)
public class PlotInfoHudOverlay {
    public static void render(ForgeGui gui, GuiGraphics graphics,
                              float partialTick, int width, int height) {
        // Rendering-Code
    }
}
```

### Proxy-Pattern (veraltet, aber noch verwendet)

```java
public class ClientProxy extends CommonProxy {
    @Override
    public void init() {
        super.init();
        registerRenderers();
        registerScreens();
    }

    private void registerRenderers() {
        EntityRenderers.register(
            ModEntities.CUSTOM_NPC.get(),
            CustomNPCRenderer::new
        );
    }
}
```

### Screen & Menu System

**Menu** (Server-side logic):

```java
public class ATMMenu extends AbstractContainerMenu {
    public ATMMenu(int containerId, Inventory playerInv) {
        super(ModMenuTypes.ATM.get(), containerId);
        // Container-Setup
    }

    @Override
    public boolean stillValid(Player player) {
        // Validierung
        return true;
    }
}
```

**Screen** (Client-side rendering):

```java
@OnlyIn(Dist.CLIENT)
public class ATMScreen extends AbstractContainerScreen<ATMMenu> {
    private static final ResourceLocation TEXTURE =
        new ResourceLocation(ScheduleMC.MOD_ID, "textures/gui/atm.png");

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick,
                            int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }
}
```

---

## API-Nutzung

### PlotModAPI

**Datei**: `de.rolandsw.schedulemc.api.PlotModAPI`

```java
public class PlotModAPI {

    // Plot-System
    public static PlotRegion getPlotAt(BlockPos pos) {
        return PlotManager.getInstance().getPlotAt(pos);
    }

    public static boolean isProtected(BlockPos pos) {
        return PlotManager.getInstance().getPlotAt(pos) != null;
    }

    public static boolean canPlayerBuild(UUID playerId, BlockPos pos) {
        PlotRegion plot = getPlotAt(pos);
        return plot == null || plot.canBuild(playerId);
    }

    // Economy-System
    public static double getBalance(UUID playerId) {
        return EconomyManager.getInstance().getBalance(playerId);
    }

    public static boolean addMoney(UUID playerId, double amount) {
        EconomyManager.getInstance().addMoney(playerId, amount);
        return true;
    }

    public static boolean removeMoney(UUID playerId, double amount) {
        return EconomyManager.getInstance().removeMoney(playerId, amount);
    }

    // NPC-System
    public static void spawnNPC(Level level, BlockPos pos, NPCType type, String name) {
        // Implementation
    }
}
```

### Verwendung in anderen Mods

```java
// In deiner Mod
public class MyMod {

    public void myMethod(Player player, BlockPos pos) {
        // 1. Prüfe ob ScheduleMC geladen ist
        if (ModList.get().isLoaded("schedulemc")) {
            // 2. Verwende API
            if (PlotModAPI.canPlayerBuild(player.getUUID(), pos)) {
                // Spieler darf bauen
            } else {
                // Spieler ist nicht berechtigt
            }
        }
    }
}
```

---

## Best Practices

### 1. Thread-Safety

```java
// ✅ GUT: ConcurrentHashMap
private final ConcurrentHashMap<UUID, Double> accounts = new ConcurrentHashMap<>();

// ❌ SCHLECHT: HashMap ohne Synchronisation
private final HashMap<UUID, Double> accounts = new HashMap<>();
```

### 2. Null-Safety

```java
// ✅ GUT: Null-Checks
PlotRegion plot = PlotManager.getInstance().getPlotAt(pos);
if (plot != null) {
    plot.doSomething();
}

// ❌ SCHLECHT: Kein Null-Check
PlotRegion plot = PlotManager.getInstance().getPlotAt(pos);
plot.doSomething(); // NullPointerException!
```

### 3. Resource Locations

```java
// ✅ GUT: Konstanten verwenden
public static final ResourceLocation TEXTURE =
    new ResourceLocation(ScheduleMC.MOD_ID, "textures/gui/atm.png");

// ❌ SCHLECHT: Hardcoded Strings
ResourceLocation texture = new ResourceLocation("schedulemc", "textures/gui/atm.png");
```

### 4. Logging

```java
// ✅ GUT: Logger verwenden
private static final Logger LOGGER = LogUtils.getLogger();
LOGGER.info("Player {} bought plot {}", player.getName(), plotId);

// ❌ SCHLECHT: System.out
System.out.println("Player bought plot");
```

### 5. Configuration

```java
// ✅ GUT: Forge Config
public static ForgeConfigSpec.DoubleValue START_BALANCE;

START_BALANCE = builder
    .comment("Starting balance for new players")
    .defineInRange("startBalance", 1000.0, 0.0, 1000000.0);

// ❌ SCHLECHT: Hardcoded Werte
public static final double START_BALANCE = 1000.0;
```

### 6. Packet Handling

```java
// ✅ GUT: Enqueue work
ctx.get().enqueueWork(() -> {
    // Logic hier
});
ctx.get().setPacketHandled(true);

// ❌ SCHLECHT: Direktes Handling (Thread-unsafe)
ServerPlayer player = ctx.get().getSender();
doSomething(player); // Kann zu Race Conditions führen
ctx.get().setPacketHandled(true);
```

---

## Erweiterungen entwickeln

### Neues Feature hinzufügen

#### 1. Neues Item erstellen

```java
// ModItems.java
public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, ScheduleMC.MOD_ID);

    public static final RegistryObject<Item> MY_ITEM = ITEMS.register("my_item",
        () -> new Item(new Item.Properties()));
}

// ScheduleMC.java
public ScheduleMC() {
    ModItems.ITEMS.register(modEventBus);
}
```

#### 2. Neuen Block erstellen

```java
// ModBlocks.java
public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
        DeferredRegister.create(ForgeRegistries.BLOCKS, ScheduleMC.MOD_ID);

    public static final RegistryObject<Block> MY_BLOCK = BLOCKS.register("my_block",
        () -> new Block(BlockBehaviour.Properties.of()
            .strength(3.0F)
            .requiresCorrectToolForDrops()));
}
```

#### 3. Neuen Command hinzufügen

```java
public class MyCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("mycommand")
            .executes(context -> {
                // Command logic
                return Command.SINGLE_SUCCESS;
            })
        );
    }
}

// ScheduleMC.java
@SubscribeEvent
public void onCommandRegister(RegisterCommandsEvent event) {
    MyCommand.register(event.getDispatcher());
}
```

### Integration mit bestehenden Systemen

#### Economy-Integration

```java
public class MyFeature {
    public void chargePlayer(Player player, double amount) {
        UUID playerId = player.getUUID();

        if (EconomyManager.getInstance().removeMoney(playerId, amount)) {
            player.sendSystemMessage(Component.literal("Bezahlt: " + amount + "€"));
        } else {
            player.sendSystemMessage(Component.literal("Nicht genug Geld!"));
        }
    }
}
```

#### Plot-Integration

```java
public class MyFeature {
    public void checkPlotPermission(Player player, BlockPos pos) {
        PlotRegion plot = PlotManager.getInstance().getPlotAt(pos);

        if (plot != null) {
            if (plot.canBuild(player.getUUID())) {
                // Erlaubt
            } else {
                // Verboten
            }
        }
    }
}
```

---

## Testing

### Unit Tests

```java
public class EconomyManagerTest {
    private EconomyManager economy;
    private UUID testPlayerId;

    @Before
    public void setUp() {
        economy = new EconomyManager();
        testPlayerId = UUID.randomUUID();
    }

    @Test
    public void testCreateAccount() {
        economy.createAccount(testPlayerId, 1000.0);
        assertEquals(1000.0, economy.getBalance(testPlayerId), 0.01);
    }

    @Test
    public void testTransferMoney() {
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();

        economy.createAccount(player1, 1000.0);
        economy.createAccount(player2, 0.0);

        assertTrue(economy.transferMoney(player1, player2, 500.0));
        assertEquals(500.0, economy.getBalance(player1), 0.01);
        assertEquals(500.0, economy.getBalance(player2), 0.01);
    }
}
```

### Ingame-Testing

```bash
# 1. Client starten
./gradlew runClient

# 2. Welt erstellen (Creative Mode empfohlen)

# 3. Tests durchführen:
/plot wand
# Markiere Bereich
/plot create 500
/money
/daily
/npc spawn merchant TestNPC
```

### Performance-Testing

```java
// Measure Plot Lookup Performance
long start = System.nanoTime();
for (int i = 0; i < 10000; i++) {
    PlotRegion plot = PlotManager.getInstance().getPlotAt(randomPos());
}
long end = System.nanoTime();
LOGGER.info("10k lookups took: {} ms", (end - start) / 1_000_000);
```

---

## Debugging

### Logging

```java
private static final Logger LOGGER = LogUtils.getLogger();

// Info
LOGGER.info("Plot {} created by {}", plotId, playerName);

// Warning
LOGGER.warn("Player {} tried to buy plot without money", playerName);

// Error
LOGGER.error("Failed to save economy data", exception);

// Debug
LOGGER.debug("Spatial index size: {}", spatialIndex.size());
```

### IntelliJ IDEA Debugger

1. Setze Breakpoints in Code
2. Starte "runClient" im Debug-Modus
3. Spiel starten
4. Breakpoint wird getroffen

### Log-Dateien

```
logs/
├── debug.log      # Alle Log-Level
└── latest.log     # Info und höher
```

---

## Häufige Probleme

### Problem: Mod lädt nicht

**Lösung**:
1. Überprüfe `mods.toml` (Version, Dependencies)
2. Überprüfe Forge-Version
3. Schaue in `latest.log` nach Fehlern

### Problem: NPE beim Speichern

**Lösung**:
- Null-Checks vor Serialisierung
- Verwende `@Nullable` Annotationen

### Problem: Client-Server Desync

**Lösung**:
- Server als autoritativ behandeln
- Immer Packets für Sync verwenden
- Nie Client-Daten direkt ändern

---

<div align="center">

**Happy Coding!**

[⬆ Nach oben](#schedulemc---entwickler-dokumentation)

</div>
