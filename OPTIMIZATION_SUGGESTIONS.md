# ScheduleMC - Optimierungsvorschläge (Vollständige Analyse)

**Projekt:** ScheduleMC v3.6.0-beta | Minecraft 1.20.1 | Forge 47.4.0 | Java 17
**Umfang:** 1.452 Java-Dateien, ~219.500 LOC, 50+ Manager-Klassen
**Analyse-Datum:** 2026-02-09

---

## Inhaltsverzeichnis

1. [KRITISCH - Sofort beheben (Crashes/Datenverlust)](#1-kritisch---sofort-beheben)
2. [HOCH - Performance-Optimierungen](#2-hoch---performance-optimierungen)
3. [HOCH - Sicherheit & Exploits](#3-hoch---sicherheit--exploits)
4. [MITTEL - Code-Qualität & Wartbarkeit](#4-mittel---code-qualität--wartbarkeit)
5. [MITTEL - Architektur-Verbesserungen](#5-mittel---architektur-verbesserungen)
6. [NIEDRIG - Kleinere Verbesserungen](#6-niedrig---kleinere-verbesserungen)
7. [Zusammenfassung & Priorisierte Roadmap](#7-zusammenfassung--priorisierte-roadmap)

---

## 1. KRITISCH - Sofort beheben

### 1.1 EconomyManager: Transfer ist nicht atomar (Geldverlust möglich)

**Datei:** `economy/EconomyManager.java:388-410`
**Schweregrad:** KRITISCH
**Problem:** Die `transfer()`-Methode ruft `withdraw()` und `deposit()` sequentiell auf. Wenn der Server zwischen beiden Operationen abstürzt (oder eine Exception in `deposit()` auftritt), verschwindet das Geld des Senders ohne beim Empfänger anzukommen.

```java
// AKTUELL (UNSICHER):
public static boolean transfer(UUID from, UUID to, double amount, ...) {
    if (withdraw(from, amount)) {        // Geld wird abgezogen
        deposit(to, amount, ...);         // <-- Crash hier = Geld weg!
        return true;
    }
    return false;
}

// FIX: Atomare Operation mit ConcurrentHashMap
public static boolean transfer(UUID from, UUID to, double amount, ...) {
    // Beide Operationen in einer synchronisierten Transaktion
    synchronized (balances) {
        double fromBalance = balances.getOrDefault(from, 0.0);
        if (fromBalance < amount) return false;
        balances.put(from, fromBalance - amount);
        balances.merge(to, amount, Double::sum);
    }
    markDirty();
    return true;
}
```

**Zusätzlich:** Die `transfer()`-Methode loggt Transaktionen doppelt - einmal durch die internen `withdraw()`/`deposit()`-Aufrufe und nochmals explizit in Zeilen 402-405.

---

### 1.2 IncrementalSaveManager: SLF4J Format-String statt Java Format

**Datei:** `util/IncrementalSaveManager.java:220`
**Schweregrad:** KRITISCH - Format-String Crash bei jedem Save

```java
// FEHLER: {:.2f} ist Python-Syntax, nicht SLF4J
LOGGER.debug("Saved {} in {:.2f}ms", saveable.getName(), durationMs);

// FIX:
LOGGER.debug("Saved {} in {}ms", saveable.getName(), String.format("%.2f", durationMs));
```

Derselbe Fehler auch in Zeilen 254, 258, 275, 279.

---

### 1.3 HotReloadableConfig: Unkontrollierter Thread außerhalb ThreadPoolManager

**Datei:** `util/HotReloadableConfig.java:147-180`
**Schweregrad:** MITTEL-HOCH
**Problem:** Erstellt einen raw `new Thread()` statt den ThreadPoolManager zu verwenden. Dieser Thread:
- Wird nicht als Daemon markiert (kann JVM-Shutdown blockieren)
- Wird beim Server-Stop nicht beendet
- Umgeht das zentrale Thread-Management

```java
// AKTUELL:
watchThread = new Thread(() -> { ... });
watchThread.start();

// FIX:
ThreadPoolManager.getIOPool().submit(() -> { ... });
```

---

### 1.4 PlotCache.invalidateRegion(): Cache-Zugriff ohne Lock

**Datei:** `region/PlotCache.java:213-261`
**Schweregrad:** MITTEL-HOCH
**Problem:** `invalidateRegion()` greift in Zeile 243 auf `cache.remove(pos)` zu, ohne den `rwLock` zu halten. Das passiert innerhalb der Chunk-Index-Iteration, nachdem der Cache-Lock schon freigegeben wurde.

```java
// AKTUELL (BUG):
for (BlockPos pos : toRemove) {
    cache.remove(pos);           // <-- OHNE Lock! LinkedHashMap ist nicht thread-safe
    positionsInChunk.remove(pos);
}

// FIX: Alles unter dem WriteLock
rwLock.writeLock().lock();
try {
    // Cache + Chunk-Index-Bereinigung zusammen
} finally {
    rwLock.writeLock().unlock();
}
```

---

## 2. HOCH - Performance-Optimierungen

### 2.1 Server-Tick: 12+ Manager.getInstance() Aufrufe pro Tick

**Datei:** `ScheduleMC.java:728-815`
**Schweregrad:** HOCH
**Problem:** `onServerTick()` ruft jeden Tick (20x/Sek) für bis zu 12 Manager `.getInstance(server)` auf. Jeder Aufruf durchläuft Synchronisation und Null-Checks.

```java
// AKTUELL: 12 getInstance()-Aufrufe PRO TICK
InterestManager.getInstance(server).tick(dayTime);
LoanManager.getInstance(server).tick(dayTime);
TaxManager.getInstance(server).tick(dayTime);
SavingsAccountManager.getInstance(server).tick(dayTime);
// ... 8 weitere

// FIX: Manager-Referenzen einmalig beim Server-Start cachen
private InterestManager interestMgr;
private LoanManager loanMgr;
// ... im onServerStarted:
interestMgr = InterestManager.getInstance(server);
// ... im onServerTick:
interestMgr.tick(dayTime);
```

**Geschätzter Impact:** ~10-20μs pro Tick weniger (relevant bei 20 TPS)

---

### 2.2 PlotCache.get(): WriteLock statt ReadLock

**Datei:** `region/PlotCache.java:99-123`
**Schweregrad:** HOCH
**Problem:** Die `get()`-Methode verwendet `writeLock()` weil `LinkedHashMap(access-order=true)` bei get() die interne Reihenfolge ändert. Das blockiert alle parallelen Lesezugriffe.

```java
// AKTUELL: Exklusiver Write-Lock für JEDEN Cache-Lookup
rwLock.writeLock().lock(); // <-- Blockiert ALLE anderen Threads

// LÖSUNG A: Caffeine Library (empfohlen)
Cache<BlockPos, CacheEntry> cache = Caffeine.newBuilder()
    .maximumSize(1000)
    .build();

// LÖSUNG B: ConcurrentHashMap + manuelles LRU
// Tauscht exakte LRU-Reihenfolge gegen bessere Concurrency
```

**Geschätzter Impact:** ~40% weniger Lock-Contention bei vielen Spielern

---

### 2.3 IncrementalSaveManager.getDirtyCount(): O(n) pro Aufruf

**Datei:** `util/IncrementalSaveManager.java:337-345`
**Schweregrad:** MITTEL
**Problem:** Iteriert die gesamte Saveable-Liste (40+ Einträge) bei jedem Aufruf. Wird in `printStatus()`, `getStatistics()` und `toString()` aufgerufen.

```java
// AKTUELL: O(n)
public int getDirtyCount() {
    int dirty = 0;
    for (ISaveable saveable : saveables) {
        if (saveable.isDirty()) dirty++;
    }
    return dirty;
}

// FIX: O(1) mit AtomicInteger Counter
private final AtomicInteger dirtyCount = new AtomicInteger(0);
```

---

### 2.4 EconomyManager.saveAccounts(): HashMap-Kopie bei jedem Save

**Datei:** `economy/EconomyManager.java:173-179`
**Schweregrad:** MITTEL
**Problem:** Bei jedem Save (alle paar Sekunden) wird die gesamte `balances`-Map in eine neue `HashMap<String, Double>` kopiert und jede UUID in einen String konvertiert.

```java
// AKTUELL: O(n) Kopie + n UUID.toString()-Aufrufe
Map<String, Double> saveMap = new HashMap<>((int)(balances.size() / 0.75) + 1);
for (Map.Entry<UUID, Double> entry : balances.entrySet()) {
    saveMap.put(entry.getKey().toString(), entry.getValue());
}

// FIX: Custom Gson TypeAdapter der direkt aus ConcurrentHashMap serialisiert
```

**Geschätzter Impact:** Bei 1000+ Spielern ~30% weniger Save-Zeit

---

### 2.5 EconomyManager.getAllAccounts(): Ungeschützte Map-Kopie

**Datei:** `economy/EconomyManager.java:357-359`
**Schweregrad:** MITTEL
**Problem:** `getAllAccounts()` erstellt bei jedem Aufruf eine vollständige Kopie der Balances-Map. Wenn dies für Admin-Befehle wie Top-Listen verwendet wird, ist das bei vielen Spielern kostspielig.

```java
// AKTUELL: Vollständige Kopie
public static Map<UUID, Double> getAllAccounts() {
    return new HashMap<>(balances);
}

// FIX: Unmodifiable View zurückgeben
public static Map<UUID, Double> getAllAccounts() {
    return Collections.unmodifiableMap(balances);
}
```

---

### 2.6 CustomNPCEntity: NPCActivityStatus.values() Allokation

**Datei:** `npc/entity/CustomNPCEntity.java:644-650`
**Schweregrad:** NIEDRIG-MITTEL
**Problem:** `getActivityStatus()` ruft `NPCActivityStatus.values()` auf, was bei jedem Aufruf ein neues Array allokiert. Bei vielen NPCs und häufigen Aufrufen erzeugt das GC-Druck.

```java
// AKTUELL: Neues Array bei jedem Aufruf
NPCActivityStatus[] values = NPCActivityStatus.values();

// FIX: Statisches Array cachen
private static final NPCActivityStatus[] ACTIVITY_VALUES = NPCActivityStatus.values();
```

Selbes Pattern bei `getPersonality()` (Zeile 624: `NPCPersonality.valueOf()`).

---

### 2.7 ScheduleMC-Konstruktor: 60+ sequentielle Register-Aufrufe

**Datei:** `ScheduleMC.java:172-327`
**Schweregrad:** NIEDRIG
**Problem:** Der Konstruktor registriert 14 Produktionssysteme mit jeweils 4-5 `.register(modEventBus)` Aufrufen sequentiell. Das sind 60+ identische Aufrufe.

```java
// AKTUELL: 60+ einzelne Aufrufe
CoffeeItems.ITEMS.register(modEventBus);
CoffeeBlocks.BLOCKS.register(modEventBus);
CoffeeBlocks.ITEMS.register(modEventBus);
CoffeeBlockEntities.BLOCK_ENTITIES.register(modEventBus);
CoffeeMenuTypes.MENUS.register(modEventBus);
// x14 Systeme...

// FIX: Registry-Helper
private void registerProductionSystem(IEventBus bus, ProductionSystemRegistrar system) {
    system.getItems().register(bus);
    system.getBlocks().register(bus);
    system.getBlockItems().register(bus);
    system.getBlockEntities().register(bus);
    if (system.getMenuTypes() != null) system.getMenuTypes().register(bus);
}
```

---

## 3. HOCH - Sicherheit & Exploits

### 3.1 EconomyManager: double für Währung (Gleitkomma-Fehler)

**Datei:** `economy/EconomyManager.java` (gesamte Klasse)
**Schweregrad:** HOCH
**Problem:** Alle Guthaben werden als `double` gespeichert. Floating-Point-Arithmetik führt zu Rundungsfehlern:

```java
// Beispiel: 0.1 + 0.2 ≠ 0.3 in double-Arithmetik
// Nach vielen Transaktionen können Cent-Beträge "verschwinden" oder entstehen
balances.merge(uuid, amount, Double::sum);
```

**FIX:** `long` in Cent verwenden (1€ = 100 Cent) oder `BigDecimal`:
```java
private static final Map<UUID, Long> balances = new ConcurrentHashMap<>(); // Cent-basiert
```

---

### 3.2 EconomyManager.withdraw(): Unbegrenztes Dispo ohne Limit

**Datei:** `economy/EconomyManager.java:310-312`
**Schweregrad:** HOCH
**Problem:** Kommentar sagt "UNBEGRENZTES Dispo" - Spieler können beliebig ins Minus gehen. Zwar regelt der OverdraftManager Konsequenzen, aber ein Spieler kann kurz vor dem Bannen noch -999.999€ an einen Freund transferieren.

```java
// AKTUELL: Kein Limit für negative Balances
double resultBalance = balances.merge(uuid, -amount, Double::sum);
// resultBalance kann -∞ sein!

// FIX: Mindestens ein konfigurierbareres Dispo-Limit
double minBalance = -ModConfigHandler.COMMON.MAX_OVERDRAFT.get();
if (balances.getOrDefault(uuid, 0.0) - amount < minBalance) {
    return false;
}
```

---

### 3.3 EconomyManager.setBalance(): Negativ-Check fehlerhaft

**Datei:** `economy/EconomyManager.java:332-334`
**Schweregrad:** MITTEL
**Problem:** `setBalance()` setzt negative Beträge auf 0, aber `withdraw()` erlaubt unbegrenzte negative Balances. Inkonsistentes Verhalten.

```java
// setBalance: Negativer Betrag -> 0
if (amount < 0) { amount = 0; }

// withdraw: Negativer Balance erlaubt (kein Limit)
// -> Inkonsistenz
```

---

### 3.4 EconomyManager.hasAccount(): Debug-Logging für jeden Check

**Datei:** `economy/EconomyManager.java:232-236`
**Schweregrad:** MITTEL
**Problem:** `hasAccount()` loggt bei **jedem** Aufruf auf DEBUG-Level und macht dafür einen zusätzlichen `balances.get()` Lookup. In Hot-Paths (z.B. Plot-Zugriffsprüfung) kann das zu Tausenden von Log-Einträgen pro Sekunde führen.

```java
// AKTUELL: Zwei Map-Lookups + Logging bei JEDEM Aufruf
public static boolean hasAccount(UUID uuid) {
    boolean exists = balances.containsKey(uuid);
    LOGGER.debug("hasAccount({}) = {} (current balance: {})", uuid, exists,
        exists ? balances.get(uuid) : "N/A"); // <-- Zweiter Lookup!
    return exists;
}

// FIX: Nur Map-Lookup, kein Logging im Hot-Path
public static boolean hasAccount(UUID uuid) {
    return balances.containsKey(uuid);
}
```

---

### 3.5 RateLimiter: startAutoCleanup() wird nicht automatisch aufgerufen

**Datei:** `util/RateLimiter.java:98-103` & `economy/EconomyManager.java:39-41`
**Schweregrad:** MITTEL
**Problem:** Die drei RateLimiter in EconomyManager (`transferLimiter`, `withdrawLimiter`, `depositLimiter`) rufen niemals `startAutoCleanup()` auf. Bei hoher Spielerfluktuation wachsen die internen `windows`-Maps unbegrenzt.

```java
// FIX: Im EconomyManager.initialize():
public static void initialize(MinecraftServer server) {
    // ...
    transferLimiter.startAutoCleanup();
    withdrawLimiter.startAutoCleanup();
    depositLimiter.startAutoCleanup();
}
```

---

## 4. MITTEL - Code-Qualität & Wartbarkeit

### 4.1 Massive Code-Duplikation in Produktionssystemen

**Betroffene Pakete:** `coffee/`, `wine/`, `beer/`, `cheese/`, `chocolate/`, `honey/`, `tobacco/`, `coca/`, `poppy/`, `cannabis/`, `meth/`, `lsd/`, `mdma/`, `mushroom/`
**Schweregrad:** HOCH (Wartbarkeit)

**Problem:** 14 Produktionssysteme mit nahezu identischer Struktur:
- Jedes hat eigene `Items`, `Blocks`, `BlockEntities`, `MenuTypes`, `Networking`-Klassen
- Die BlockEntities erben zwar teilweise von `Abstract*`-Klassen (gut!), aber es gibt **kein** systemübergreifendes `AbstractProductionBlockEntity`
- Jedes System hat eigene Network-Handler (`CoffeeNetworking`, `WineNetworking`, `BeerNetworking`, etc.)
- Registrierung in `ScheduleMC.java` ist 14x Copy-Paste (Zeilen 188-279)

**Geschätzter Duplikations-Overhead:** ~8.000-12.000 LOC könnten durch ein generisches `ProductionSystem`-Framework eingespart werden.

```java
// VORSCHLAG: Generisches Produktionssystem
public class ProductionSystemDefinition {
    private final String name;
    private final DeferredRegister<Item> items;
    private final DeferredRegister<Block> blocks;
    private final DeferredRegister<BlockEntityType<?>> blockEntities;
    private final DeferredRegister<MenuType<?>> menus;

    public void registerAll(IEventBus bus) {
        items.register(bus);
        blocks.register(bus);
        blockEntities.register(bus);
        if (menus != null) menus.register(bus);
    }
}
```

---

### 4.2 God-Object: ScheduleMC.java (903 Zeilen)

**Datei:** `ScheduleMC.java`
**Schweregrad:** MITTEL
**Problem:** Die Hauptklasse ist für ALLES zuständig:
- Registrierung aller 14 Produktionssysteme
- 25+ Event-Handler-Registrierungen
- 20+ Netzwerk-Handler-Registrierungen
- 16 Manager-Ladevorgänge
- 30+ Manager-Registrierungen beim IncrementalSaveManager
- Server-Tick-Logik
- API-Initialisierung

**FIX:** Aufteilen in:
- `ModRegistration.java` - Alle Registry-Aufrufe
- `ModEventHandlers.java` - Event-Bus-Registrierungen
- `ModNetworkSetup.java` - Network-Handler-Setup
- `ModManagerInitializer.java` - Manager-Laden und Save-Registrierungen
- `ScheduleMC.java` - Nur noch Lifecycle und Delegation

---

### 4.3 Singleton-Muster überall: Untestbar

**Betroffene Klassen:** EconomyManager, PlotManager, WalletManager, CrimeManager, GangManager, TerritoryManager, AchievementManager, LoanManager, TaxManager, CreditScoreManager, und ~30 weitere
**Schweregrad:** MITTEL

**Problem:** Fast alle Manager verwenden das Singleton-Pattern mit statischen `getInstance()`-Methoden. Dies macht:
- Unit-Tests extrem schwierig (keine Injection von Mocks)
- Die Abhängigkeitsreihenfolge implizit und fehleranfällig
- Paralleles Testen unmöglich

**FIX:** Service-Registry-Pattern:
```java
public class ServiceRegistry {
    private static final Map<Class<?>, Object> services = new ConcurrentHashMap<>();

    public static <T> void register(Class<T> type, T instance) {
        services.put(type, instance);
    }

    public static <T> T get(Class<T> type) {
        return type.cast(services.get(type));
    }
}
```

---

### 4.4 Inkonsistente Muster bei Manager-Initialisierung

**Schweregrad:** MITTEL
**Problem:** Manager werden auf 4 verschiedene Arten initialisiert:

| Pattern | Beispiel | Problem |
|---------|----------|---------|
| `getInstance(server)` | LoanManager, TaxManager | server-Parameter wird gespeichert, aber nur beim ersten Aufruf |
| `getInstance()` | DynamicMarketManager | Kein Server-Zugang |
| Statisch `load()` | PlotManager, EconomyManager | Globaler Zustand, keine Instanz |
| Explicit `initialize(server)` | EconomyManager | Separate Methode nötig |

**FIX:** Ein einheitliches Pattern für alle Manager etablieren.

---

### 4.5 HealthCheckManager: getOverallHealth() und getQuickStatus() doppelte Arbeit

**Datei:** `util/HealthCheckManager.java:356-407`
**Schweregrad:** NIEDRIG
**Problem:** `getQuickStatus()` ruft `checkAllSystems()` UND `getOverallHealth()` auf. `getOverallHealth()` ruft ebenfalls `checkAllSystems()` auf. Das sind 2 vollständige Health-Checks für eine Status-Zeile.

```java
// AKTUELL: 2x checkAllSystems()
public static String getQuickStatus() {
    Map<String, ComponentHealth> results = checkAllSystems(); // <-- 1. Durchlauf
    long healthy = results.values().stream()...;
    SystemHealth overall = getOverallHealth();                  // <-- 2. Durchlauf!
    // ...
}

// FIX: Ergebnis wiederverwenden
public static String getQuickStatus() {
    Map<String, ComponentHealth> results = checkAllSystems();
    long healthy = results.values().stream().filter(...).count();
    SystemHealth overall = calculateOverallHealth(results); // Kein neuer Check
    // ...
}
```

---

### 4.6 checkSystem() prüft ALLE Systeme für ein einzelnes

**Datei:** `util/HealthCheckManager.java:412-415`
**Schweregrad:** NIEDRIG
**Problem:** `checkSystem(String systemKey)` führt `checkAllSystems()` (38 Checks!) aus, nur um einen einzelnen zurückzugeben.

```java
// AKTUELL: 38 Checks für 1 Ergebnis
public static ComponentHealth checkSystem(String systemKey) {
    Map<String, ComponentHealth> all = checkAllSystems();
    return all.get(systemKey);
}

// FIX: Direkt den einen Check ausführen
public static ComponentHealth checkSystem(String systemKey) {
    HealthCheckEntry entry = checkRegistry.get(systemKey);
    if (entry == null) return null;
    return checkSingletonManager(systemKey, entry.supplier);
}
```

---

## 5. MITTEL - Architektur-Verbesserungen

### 5.1 Fehlende generische Basis für Produktions-BlockEntities

**Problem:** Jedes Produktionssystem (Coffee, Wine, Beer, etc.) hat eigene Abstract-Klassen die denselben Boilerplate enthalten:
- `AbstractCoffeeRoasterBlockEntity`
- `AbstractWinePressBlockEntity`
- `AbstractBrewKettleBlockEntity`
- `AbstractConditioningTankBlockEntity`
- etc.

Alle implementieren das gleiche Muster: Input-Slot, Output-Slot, Progress-Tracking, Energy-Verbrauch.

**FIX:** Generische `AbstractProcessingBlockEntity<R extends Recipe>`:
```java
public abstract class AbstractProcessingBlockEntity<R> extends BlockEntity
        implements IUtilityConsumer, MenuProvider {
    protected int progress;
    protected int maxProgress;
    protected ItemStackHandler inputSlots;
    protected ItemStackHandler outputSlots;

    protected abstract R findRecipe();
    protected abstract void processRecipe(R recipe);
}
```

---

### 5.2 Fehlender Circuit-Breaker für kritische Systeme

**Schweregrad:** MITTEL
**Problem:** Wenn ein System (z.B. TransactionHistory) ausfällt, kaskadieren Fehler. Es gibt keinen Mechanismus um ein fehlerhaftes System temporär zu deaktivieren.

**Beispiel:** `EconomyManager.logTransaction()` (Zeile 373-382) ruft `TransactionHistory.getInstance()` auf. Wenn TransactionHistory einen Fehler hat, wird bei JEDER Transaktion eine Exception geworfen.

**FIX:**
```java
public class CircuitBreaker {
    private final int threshold;
    private final AtomicInteger failures = new AtomicInteger(0);
    private volatile boolean open = false;

    public <T> T execute(Supplier<T> op, T fallback) {
        if (open) return fallback;
        try {
            T result = op.get();
            failures.set(0);
            return result;
        } catch (Exception e) {
            if (failures.incrementAndGet() >= threshold) {
                open = true;
                scheduleHalfOpen();
            }
            return fallback;
        }
    }
}
```

---

### 5.3 Network-Handler Registrierung: 20+ einzelne register()-Aufrufe

**Datei:** `ScheduleMC.java:334-358`
**Schweregrad:** NIEDRIG
**Problem:** 20+ `XxxNetworking.register()` Aufrufe in `commonSetup()`. Jeder Handler registriert sich separat.

**FIX:** Network-Handler-Registry:
```java
public class ModNetworkRegistry {
    private static final List<Runnable> handlers = new ArrayList<>();

    public static void add(Runnable registrar) { handlers.add(registrar); }
    public static void registerAll() { handlers.forEach(Runnable::run); }
}
```

---

## 6. NIEDRIG - Kleinere Verbesserungen

### 6.1 CopyOnWriteArrayList in IncrementalSaveManager

**Datei:** `util/IncrementalSaveManager.java:70`
**Problem:** `CopyOnWriteArrayList` kopiert das gesamte Array bei jedem `add()`. Für die Registration (einmalig beim Start) ist das OK, aber `register()` macht sorted insert mit `add(index, element)`, was eine Kopie triggert.

Nach der Initialisierung wird die Liste nur noch gelesen (im Save-Loop). `ArrayList` mit externer Synchronisation für `register()` wäre effizienter.

---

### 6.2 ScheduleMC: Doppelte Server-Referenz Checks

**Datei:** `ScheduleMC.java:733, 738`
**Problem:** `server.overworld()` wird zwei Mal auf `null` geprüft (Zeile 733 und 738), der zweite Check ist redundant weil er im selben Tick-Handler ist.

---

### 6.3 CustomNPCEntity.remove(): Doppelte isClientSide-Prüfung

**Datei:** `npc/entity/CustomNPCEntity.java:901-924`
**Problem:** `remove()` prüft `!this.level().isClientSide` zwei Mal für den NPCEntityRegistry-Unregister und den Name-Unregister.

```java
// AKTUELL:
if (!this.level().isClientSide) {
    NPCEntityRegistry.unregisterNPC(this);
}
if (!this.level().isClientSide) {  // <-- Redundant
    // Name-Unregister...
}

// FIX: Zu einem Block zusammenfassen
if (!this.level().isClientSide) {
    NPCEntityRegistry.unregisterNPC(this);
    // Name-Unregister...
}
```

---

### 6.4 PlotCache: getChunkPos() Utility-Methode unbenutzt

**Datei:** `region/PlotCache.java:161-163`
**Problem:** Die private Methode `getChunkPos(BlockPos)` existiert, aber überall wird direkt `new ChunkPos(pos)` aufgerufen. Toter Code.

---

### 6.5 EconomyManager: Statische Felder + Singleton vermischt

**Datei:** `economy/EconomyManager.java`
**Problem:** `balances`, `file`, `needsSave` sind `static`, aber die Klasse hat auch eine Singleton-Instanz. Das mischt zwei Patterns (statisch und Singleton) und erschwert Testing und Reasoning.

---

## 7. Zusammenfassung & Priorisierte Roadmap

### Statistik

| Kategorie | Anzahl | Davon KRITISCH | Davon HOCH |
|-----------|--------|----------------|------------|
| Bugs / Crashes | 4 | 2 | 2 |
| Performance | 7 | 0 | 2 |
| Sicherheit | 5 | 0 | 2 |
| Code-Qualität | 6 | 0 | 1 |
| Architektur | 3 | 0 | 0 |
| Kleinere Fixes | 5 | 0 | 0 |
| **Gesamt** | **30** | **2** | **7** |

### Priorisierte Roadmap

#### Phase 1: Kritische Fixes (Sofort - geschätzter Aufwand: 2-4 Stunden)
1. **Transfer atomarisieren** (EconomyManager:388) - Geldverlust verhindern
2. **SLF4J Format-Strings fixen** (IncrementalSaveManager) - Crash verhindern
3. **PlotCache Lock-Bug fixen** (invalidateRegion) - Race Condition
4. **RateLimiter Auto-Cleanup aktivieren** - Memory Leak verhindern

#### Phase 2: Performance Quick-Wins (1-2 Tage)
5. Manager-Referenzen in onServerTick cachen
6. `hasAccount()` Logging entfernen
7. PlotCache: WriteLock-Problem lösen (Caffeine oder alternatives LRU)
8. Enum.values() caching in NPC-Entity

#### Phase 3: Sicherheit (1-2 Tage)
9. Overdraft-Limit einführen
10. `double` -> `long` (Cent-basiert) für Währung evaluieren
11. Konsistente Balance-Validierung

#### Phase 4: Code-Qualität (1-2 Wochen)
12. Generisches Produktionssystem-Framework
13. ScheduleMC.java aufspalten
14. HealthCheckManager optimieren
15. Manager-Initialisierungsmuster vereinheitlichen

#### Phase 5: Architektur (langfristig)
16. Service-Registry statt Singletons
17. Circuit-Breaker für kritische Systeme
18. Generische AbstractProcessingBlockEntity
19. Network-Handler-Registry
