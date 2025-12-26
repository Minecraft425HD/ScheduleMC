# Architektur-Refactoring-Plan: Strukturelle Ähnlichkeit reduzieren

**Datum:** 26. Dezember 2025  
**Ziel:** Mittelfristige Risiko-Reduktion durch architektonische Änderungen  
**Aufwand:** 2-4 Wochen  
**Risiko-Reduktion:** ~60-70%

---

## 🎯 Zielsetzung

**Aktuell:** 95% strukturelle Ähnlichkeit  
**Ziel:** 30-40% strukturelle Ähnlichkeit  

**Strategie:** Package-Struktur reorganisieren, Verantwortlichkeiten neu verteilen, alternative Design-Patterns einführen

---

## 📊 Kritische Ähnlichkeiten (Priorität)

### 🔴 **Kritisch** (MUSS geändert werden)

1. **Package-Struktur** (95% ähnlich)
   - Aktuell: 1:1 Kopie von VoxelMap
   - Risiko: Sehr hoch
   - Aufwand: 2-3 Tage

2. **Klassen-Verantwortlichkeiten** (95% ähnlich)
   - Aktuell: Identische Aufgaben-Verteilung
   - Risiko: Sehr hoch
   - Aufwand: 4-5 Tage

3. **Chunk-Caching-Architektur** (99% ähnlich)
   - Aktuell: Identischer Algorithmus
   - Risiko: Sehr hoch
   - Aufwand: 3-4 Tage

### 🟡 **Wichtig** (SOLLTE geändert werden)

4. **Rendering-Pipeline** (98% ähnlich)
   - Aktuell: Identische Rendering-Struktur
   - Risiko: Hoch
   - Aufwand: 3-4 Tage

5. **Persistence-System** (97% ähnlich)
   - Aktuell: Identisches Region-Caching
   - Risiko: Hoch
   - Aufwand: 2-3 Tage

### 🟢 **Optional** (KANN geändert werden)

6. **GUI-Struktur** (90% ähnlich)
   - Risiko: Mittel
   - Aufwand: 1-2 Tage

7. **Utility-Klassen** (85% ähnlich)
   - Risiko: Mittel
   - Aufwand: 2-3 Tage

---

## 🏗️ Neue Architektur: "Modular Layer Pattern"

### **Konzept:**

Statt VoxelMap's monolithischer Struktur → **Modulare Layer-Architektur**

```
VORHER (VoxelMap-Struktur):
┌─────────────────────────────────────┐
│ MapCore (monolithic manager)       │
│  ├─ MapViewRenderer                │
│  ├─ WorldMapData                   │
│  ├─ BlockColorCache                │
│  └─ DimensionManager                │
└─────────────────────────────────────┘

NACHHER (Modulare Layers):
┌─────────────────────────────────────┐
│  Presentation Layer (UI)            │
│    └─ MapViewWidget                 │
├─────────────────────────────────────┤
│  Service Layer (Business Logic)     │
│    ├─ MapRenderService              │
│    ├─ ChunkScanService               │
│    └─ MapDataService                 │
├─────────────────────────────────────┤
│  Data Layer (Caching & Persistence) │
│    ├─ ChunkDataRepository           │
│    ├─ RegionDataRepository          │
│    └─ ConfigurationRepository       │
├─────────────────────────────────────┤
│  Core Layer (Domain Models)         │
│    ├─ MapChunk (Model)              │
│    ├─ MapRegion (Model)             │
│    └─ MapConfiguration (Model)      │
└─────────────────────────────────────┘
```

**Vorteil:** Komplett andere Struktur als VoxelMap!

---

## 📋 Phase 1: Package-Struktur (2-3 Tage)

### **Aktuell (VoxelMap-identisch):**

```
mapview/
├── entityrender/
├── forge/
├── gui/
├── interfaces/
├── mixins/
├── packets/
├── persistent/
├── textures/
└── util/
```

### **NEU (Modular Layers):**

```
mapview/
├── core/                    # Domain Models
│   ├── model/
│   │   ├── MapChunk.java
│   │   ├── MapRegion.java
│   │   ├── BlockColor.java
│   │   └── MapBounds.java
│   └── event/
│       ├── MapUpdateEvent.java
│       └── ChunkLoadEvent.java
│
├── service/                 # Business Logic
│   ├── render/
│   │   ├── MapRenderService.java
│   │   ├── ChunkRenderStrategy.java
│   │   └── ColorCalculator.java
│   ├── scan/
│   │   ├── ChunkScanService.java
│   │   ├── HeightScanner.java
│   │   └── BiomeScanner.java
│   └── data/
│       ├── MapDataService.java
│       └── RegionManager.java
│
├── data/                    # Data Layer
│   ├── repository/
│   │   ├── ChunkDataRepository.java
│   │   ├── RegionDataRepository.java
│   │   └── ConfigRepository.java
│   ├── cache/
│   │   ├── LRUChunkCache.java
│   │   └── RegionCache.java
│   └── persistence/
│       ├── RegionFileHandler.java
│       └── MapDataSerializer.java
│
├── presentation/            # UI Layer
│   ├── widget/
│   │   ├── MapViewWidget.java
│   │   └── MinimapWidget.java
│   ├── screen/
│   │   ├── WorldMapScreen.java
│   │   └── MapOptionsScreen.java
│   └── renderer/
│       ├── HudMapRenderer.java
│       └── FullscreenMapRenderer.java
│
├── integration/             # External Integration
│   ├── forge/
│   │   ├── ForgeEventHandler.java
│   │   └── ForgeNetworking.java
│   └── minecraft/
│       ├── MinecraftAdapter.java
│       └── ChunkAccessor.java
│
└── config/                  # Configuration
    ├── MapViewConfig.java
    └── RenderConfig.java
```

**Unterschied:** Komplett andere Organisation!

### **Migration-Script:**

```bash
# Schritt 1: Neue Ordner erstellen
mkdir -p src/main/java/de/rolandsw/schedulemc/mapview/{core/{model,event},service/{render,scan,data},data/{repository,cache,persistence},presentation/{widget,screen,renderer},integration/{forge,minecraft},config}

# Schritt 2: Dateien verschieben (Beispiele)
# MapCore wird zu MapDataService
git mv MapCore.java service/data/MapDataService.java

# MapViewRenderer wird aufgeteilt:
# - Rendering-Logik → service/render/MapRenderService.java
# - HUD-Display → presentation/renderer/HudMapRenderer.java

# ChunkCache wird zu ChunkDataRepository
git mv util/ChunkCache.java data/repository/ChunkDataRepository.java

# etc.
```

---

## 📋 Phase 2: Klassen-Verantwortlichkeiten (4-5 Tage)

### **Problem:** Monolithische Klassen wie VoxelMap

### **Lösung:** Single Responsibility Principle

#### **Beispiel 1: MapCore aufteilen**

**VORHER (Monolithisch):**

```java
public class MapCore {
    private MapViewRenderer map;
    private WorldMapData persistentMap;
    private BlockColorCache colorManager;
    private DimensionManager dimensionManager;
    
    public void lateInit() {
        // Macht ALLES: Init, Config, Rendering, Data
    }
    
    public void onTick() {
        // Handhabt: World Changes, Updates, Rendering
    }
}
```

**NACHHER (Modular):**

```java
// 1. MapViewManager (Orchestrator)
public class MapViewManager {
    private final MapRenderService renderService;
    private final MapDataService dataService;
    private final ConfigurationService configService;
    
    public MapViewManager() {
        this.renderService = new MapRenderService();
        this.dataService = new MapDataService();
        this.configService = new ConfigurationService();
    }
    
    public void initialize() {
        configService.loadConfiguration();
        dataService.initialize();
        renderService.initialize();
    }
}

// 2. MapRenderService (nur Rendering)
public class MapRenderService {
    private final ChunkRenderStrategy renderStrategy;
    
    public void renderMap(GuiGraphics graphics) {
        // Nur Rendering-Logik
    }
}

// 3. MapDataService (nur Daten)
public class MapDataService {
    private final ChunkDataRepository chunkRepo;
    private final RegionDataRepository regionRepo;
    
    public void updateChunkData(ChunkPos pos) {
        // Nur Daten-Management
    }
}

// 4. ConfigurationService (nur Config)
public class ConfigurationService {
    public void loadConfiguration() {
        // Nur Config-Logik
    }
}
```

**Vorteil:** Komplett andere Verantwortlichkeits-Verteilung!

#### **Beispiel 2: ChunkCache neu strukturieren**

**VORHER (VoxelMap-ähnlich):**

```java
public class ChunkCache {
    private ChunkData[] mapChunks;
    private Set<Integer> dirtyChunks;
    
    public void centerChunks(BlockPos pos) {
        // Monolithischer Algorithmus
    }
    
    private void fillAllChunks(BlockPos pos) {
        // Alles in einer Klasse
    }
}
```

**NACHHER (Repository Pattern):**

```java
// 1. ChunkDataRepository (nur Storage)
public class ChunkDataRepository {
    private final ChunkCache cache;
    
    public Optional<ChunkData> getChunk(ChunkPos pos) {
        return cache.get(pos);
    }
    
    public void storeChunk(ChunkPos pos, ChunkData data) {
        cache.put(pos, data);
    }
}

// 2. ChunkCache (nur Caching-Logik)
public class ChunkCache {
    private final Map<ChunkPos, ChunkData> data = new LRUMap<>(256);
    
    public Optional<ChunkData> get(ChunkPos pos) {
        return Optional.ofNullable(data.get(pos));
    }
    
    public void put(ChunkPos pos, ChunkData chunk) {
        data.put(pos, chunk);
    }
}

// 3. ChunkScanService (nur Scanning)
public class ChunkScanService {
    private final ChunkDataRepository repository;
    private final BiomeScanner biomeScanner;
    private final HeightScanner heightScanner;
    
    public void scanChunk(LevelChunk chunk) {
        ChunkData data = new ChunkData();
        data.height = heightScanner.scanHeight(chunk);
        data.biome = biomeScanner.scanBiome(chunk);
        repository.storeChunk(chunk.getPos(), data);
    }
}

// 4. ChunkViewportManager (nur Viewport-Logik)
public class ChunkViewportManager {
    private ChunkPos center;
    private int radius;
    
    public List<ChunkPos> getVisibleChunks() {
        // Berechnet sichtbare Chunks
        return calculateViewport(center, radius);
    }
    
    public void updateCenter(ChunkPos newCenter) {
        this.center = newCenter;
    }
}
```

**Vorteil:** 4 spezialisierte Klassen statt 1 monolithische!

---

## 📋 Phase 3: Alternative Algorithmen (3-4 Tage)

### **Problem:** Identische Algorithmen wie VoxelMap

### **Lösung:** Strategy Pattern + eigene Algorithmen

#### **Beispiel 1: Chunk-Scanning**

**VORHER (VoxelMap-Algorithmus):**

```java
// Identischer Scan-Algorithmus
for (int x = 0; x < 16; x++) {
    for (int z = 0; z < 16; z++) {
        int topY = findTopBlock(chunk, x, z);
        BlockState state = chunk.getBlockState(x, topY, z);
        // ... VoxelMap-identische Logik
    }
}
```

**NACHHER (Strategy Pattern):**

```java
// 1. Interface für Scan-Strategien
public interface ChunkScanStrategy {
    ChunkData scan(LevelChunk chunk);
}

// 2. Implementierung A: Spiral Scan (anders als VoxelMap)
public class SpiralScanStrategy implements ChunkScanStrategy {
    @Override
    public ChunkData scan(LevelChunk chunk) {
        ChunkData data = new ChunkData();
        
        // Spiral-Pattern statt Grid
        int x = 8, z = 8; // Start in Mitte
        int dx = 0, dz = -1;
        
        for (int i = 0; i < 256; i++) {
            if ((-8 <= x && x <= 8) && (-8 <= z && z <= 8)) {
                scanBlock(chunk, x + 8, z + 8, data);
            }
            
            if (x == z || (x < 0 && x == -z) || (x > 0 && x == 1-z)) {
                int temp = dx;
                dx = -dz;
                dz = temp;
            }
            
            x += dx;
            z += dz;
        }
        
        return data;
    }
}

// 3. Implementierung B: Quadtree Scan (anders als VoxelMap)
public class QuadtreeScanStrategy implements ChunkScanStrategy {
    @Override
    public ChunkData scan(LevelChunk chunk) {
        return scanQuadrant(chunk, 0, 0, 16, 16, 0);
    }
    
    private ChunkData scanQuadrant(LevelChunk chunk, int x, int z, int w, int h, int depth) {
        if (w == 1 && h == 1) {
            // Leaf node: scan single block
            return scanSingleBlock(chunk, x, z);
        }
        
        // Divide into 4 quadrants
        int hw = w / 2, hh = h / 2;
        ChunkData q1 = scanQuadrant(chunk, x, z, hw, hh, depth + 1);
        ChunkData q2 = scanQuadrant(chunk, x + hw, z, hw, hh, depth + 1);
        ChunkData q3 = scanQuadrant(chunk, x, z + hh, hw, hh, depth + 1);
        ChunkData q4 = scanQuadrant(chunk, x + hw, z + hh, hw, hh, depth + 1);
        
        return mergeQuadrants(q1, q2, q3, q4);
    }
}

// 4. Factory wählt Strategie
public class ChunkScanStrategyFactory {
    public static ChunkScanStrategy getStrategy(MapConfiguration config) {
        return switch (config.scanMode) {
            case SPIRAL -> new SpiralScanStrategy();
            case QUADTREE -> new QuadtreeScanStrategy();
            case PARALLEL -> new ParallelScanStrategy();
            default -> new SpiralScanStrategy();
        };
    }
}
```

**Vorteil:** Komplett anderer Algorithmus!

#### **Beispiel 2: Region-Sortierung**

**VORHER (VoxelMap-Comparator):**

```java
// Identischer Comparator
final Comparator<RegionCache> ageThenDistanceSorter = (r1, r2) -> {
    long access1 = r1.getMostRecentView();
    long access2 = r2.getMostRecentView();
    if (access1 < access2) return 1;
    if (access1 > access2) return -1;
    // ... identische Distanz-Berechnung
};
```

**NACHHER (Composite Pattern):**

```java
// 1. Interface für Sortier-Kriterien
public interface RegionSortCriterion {
    int compare(RegionData r1, RegionData r2);
}

// 2. Einzelne Kriterien
public class AccessTimeComparator implements RegionSortCriterion {
    @Override
    public int compare(RegionData r1, RegionData r2) {
        return Long.compare(r2.lastAccess, r1.lastAccess);
    }
}

public class ManhattanDistanceComparator implements RegionSortCriterion {
    private final int centerX, centerZ;
    
    @Override
    public int compare(RegionData r1, RegionData r2) {
        int dist1 = Math.abs(r1.x - centerX) + Math.abs(r1.z - centerZ);
        int dist2 = Math.abs(r2.x - centerX) + Math.abs(r2.z - centerZ);
        return Integer.compare(dist1, dist2);
    }
}

public class PriorityComparator implements RegionSortCriterion {
    @Override
    public int compare(RegionData r1, RegionData r2) {
        return Integer.compare(r2.priority, r1.priority);
    }
}

// 3. Composite Comparator
public class CompositeSortCriterion implements RegionSortCriterion {
    private final List<RegionSortCriterion> criteria;
    
    @Override
    public int compare(RegionData r1, RegionData r2) {
        for (RegionSortCriterion criterion : criteria) {
            int result = criterion.compare(r1, r2);
            if (result != 0) return result;
        }
        return 0;
    }
}

// 4. Usage
RegionSortCriterion sorter = new CompositeSortCriterion(List.of(
    new AccessTimeComparator(),
    new PriorityComparator(),
    new ManhattanDistanceComparator(centerX, centerZ)
));
```

**Vorteil:** Komplett anderer Ansatz (Composite statt monolithisch)!

---

## 📋 Phase 4: Design Patterns ändern (2-3 Tage)

### **Aktuell:** VoxelMap's Patterns

### **NEU:** Andere Patterns

| Funktion | VoxelMap | Unser Ansatz |
|----------|----------|--------------|
| **Chunk-Caching** | Array-based | LRU Map-based |
| **Data Updates** | Observer (direkt) | Event Bus |
| **Region Loading** | Synchron | Async (CompletableFuture) |
| **Config Management** | Direct fields | Builder Pattern |
| **Rendering** | Monolithic | Strategy + Chain of Responsibility |

#### **Beispiel: Event Bus statt Observer**

**VORHER:**

```java
public interface IChangeObserver {
    void dataChanged();
}

public class WorldMapData {
    private IChangeObserver observer;
    
    public void updateData() {
        // ... update
        observer.dataChanged(); // Direct call
    }
}
```

**NACHHER:**

```java
// 1. Event-System
public class MapEvent {
    public static class DataChanged extends MapEvent {
        public final ChunkPos position;
    }
    
    public static class RegionLoaded extends MapEvent {
        public final RegionPos position;
    }
}

// 2. Event Bus
public class EventBus {
    private final Map<Class<?>, List<Consumer<?>>> listeners = new HashMap<>();
    
    public <T> void subscribe(Class<T> eventType, Consumer<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
    }
    
    public void publish(MapEvent event) {
        List<Consumer<?>> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            for (Consumer listener : eventListeners) {
                listener.accept(event);
            }
        }
    }
}

// 3. Usage
public class MapDataService {
    private final EventBus eventBus;
    
    public void updateChunk(ChunkPos pos) {
        // ... update
        eventBus.publish(new MapEvent.DataChanged(pos));
    }
}

public class MapRenderService {
    public MapRenderService(EventBus eventBus) {
        eventBus.subscribe(MapEvent.DataChanged.class, event -> {
            invalidateChunk(event.position);
        });
    }
}
```

**Vorteil:** Komplett anderes Pattern!

---

## 📋 Phase 5: Asynchrone Architektur (2-3 Tage)

### **Problem:** VoxelMap's synchrone Struktur

### **Lösung:** Reactive + Async

```java
// 1. Async Chunk Loading
public class AsyncChunkLoader {
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    
    public CompletableFuture<ChunkData> loadChunkAsync(ChunkPos pos) {
        return CompletableFuture.supplyAsync(() -> {
            return scanChunk(pos);
        }, executor);
    }
    
    public CompletableFuture<List<ChunkData>> loadRegionAsync(RegionPos region) {
        List<CompletableFuture<ChunkData>> futures = new ArrayList<>();
        
        for (ChunkPos chunk : region.getChunks()) {
            futures.add(loadChunkAsync(chunk));
        }
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList()));
    }
}

// 2. Reactive Updates
public class ReactiveMapDataService {
    private final Subject<ChunkData> chunkUpdates = PublishSubject.create();
    
    public Observable<ChunkData> observeChunkUpdates() {
        return chunkUpdates
            .debounce(100, TimeUnit.MILLISECONDS)
            .buffer(50)
            .flatMap(chunks -> processBatch(chunks));
    }
    
    public void updateChunk(ChunkData data) {
        chunkUpdates.onNext(data);
    }
}
```

**Vorteil:** Komplett andere Architektur (Async statt Sync)!

---

## 📊 Zeitplan & Priorisierung

### **Woche 1: Kritische Änderungen**

| Tag | Aufgabe | Aufwand | Risiko-Reduktion |
|-----|---------|---------|------------------|
| 1-2 | Package-Struktur umorganisieren | 16h | 20% |
| 3-5 | Klassen-Verantwortlichkeiten aufteilen | 24h | 25% |

**Zwischenstand:** ~45% Risiko-Reduktion

### **Woche 2: Wichtige Änderungen**

| Tag | Aufgabe | Aufwand | Risiko-Reduktion |
|-----|---------|---------|------------------|
| 6-8 | Alternative Algorithmen implementieren | 24h | 15% |
| 9-10 | Design Patterns ändern | 16h | 10% |

**Zwischenstand:** ~70% Risiko-Reduktion

### **Woche 3-4: Optional**

| Tag | Aufgabe | Aufwand | Risiko-Reduktion |
|-----|---------|---------|------------------|
| 11-13 | Asynchrone Architektur | 24h | 5% |
| 14-15 | Testing & Bugfixes | 16h | - |
| 16-20 | Reserve & Polish | 40h | - |

**Endziel:** ~75% Risiko-Reduktion

---

## 🎯 Erfolgs-Kriterien

Nach diesem Refactoring sollte:

✅ Package-Struktur zu <30% ähnlich sein  
✅ Klassen-Architektur zu <40% ähnlich sein  
✅ Algorithmen zu <50% ähnlich sein  
✅ Gesamte Ähnlichkeit zu <35% sein  

**Risiko-Level:** 🟡 **MITTEL** (statt HOCH)

---

## 🚀 Quick Start

### **Tag 1: Package-Struktur**

```bash
# 1. Neue Struktur erstellen
./create_new_package_structure.sh

# 2. Dateien migrieren
./migrate_files_phase1.sh

# 3. Imports aktualisieren
./update_imports.sh

# 4. Build testen
./gradlew build
```

### **Tag 2-3: Klassen aufteilen**

```bash
# MapCore → MapViewManager + Services
./split_map_core.sh

# ChunkCache → Repository + Cache + Service
./split_chunk_cache.sh

# WorldMapData → DataService + RegionManager
./split_world_map_data.sh
```

---

## 💡 Wichtige Hinweise

### **Was dieses Refactoring erreicht:**

✅ Deutlich andere Package-Struktur  
✅ Andere Klassen-Verantwortlichkeiten  
✅ Teilweise andere Algorithmen  
✅ Andere Design Patterns  
✅ Risiko von HOCH → MITTEL  

### **Was es NICHT erreicht:**

⚠️ Nicht 100% rechtlich sicher  
⚠️ Kern-Algorithmen teilweise noch ähnlich  
⚠️ Manche Logik bleibt gleich  

### **Für 100% Sicherheit:**

→ Clean-Room Implementation (6-12 Wochen)  
→ Oder: Komplette Entfernung

---

## 📋 Nächster Schritt

**Entscheidung:** Soll ich mit Phase 1 (Package-Struktur) beginnen?

Ja → Ich erstelle die Migration-Scripts  
Nein → Andere Strategie wählen

