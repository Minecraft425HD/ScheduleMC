# ScheduleMC Code Review - Vollstaendige Analyse

**Version:** 3.6.0-beta
**Datum:** 2026-02-10
**Scope:** Gesamte Codebase (1.452 Java-Dateien, ~21.000+ LOC)

---

## Zusammenfassung

Die Codebase ist insgesamt **gut strukturiert** und zeigt bereits viele Best Practices (Thread-Safety mit ConcurrentHashMap, atomare Datei-Schreibvorgaenge, Rate Limiting, Backup-Systeme). Es gibt jedoch einige **Fehler**, **Sicherheitsluecken** und **Optimierungsmoeglichkeiten**, die behoben werden sollten.

### Bewertung nach Kategorie

| Kategorie | Bewertung | Details |
|-----------|-----------|---------|
| **Thread-Safety** | Gut | ConcurrentHashMap, volatile, synchronized vorhanden |
| **Persistenz** | Sehr gut | Atomic writes, Backups, Retry-Mechanismen |
| **Performance** | Gut | Thread-Pools, Caching, Spatial Index |
| **Fehlerbehandlung** | Gut | EventHelper mit safeExecute, graceful degradation |
| **Code-Duplikation** | Mittel | AbstractPersistenceManager hilft, aber Registrierungsblocks |
| **Sicherheit** | Gut | Rate Limiting, Input Validation vorhanden |

---

## KRITISCHE FEHLER (Sofort beheben)

### 1. Race Condition in TransactionHistory.addTransaction()
**Datei:** `economy/TransactionHistory.java:72-83`
**Schweregrad:** HOCH

```java
// PROBLEM: computeIfAbsent + nachtraeglicher get() ist NICHT atomar!
transactions.computeIfAbsent(playerUUID, k -> new ArrayList<>()).add(transaction);
List<Transaction> playerTransactions = transactions.get(playerUUID);
if (playerTransactions.size() > MAX_TRANSACTIONS_PER_PLAYER) {
    playerTransactions.subList(0, playerTransactions.size() - MAX_TRANSACTIONS_PER_PLAYER).clear();
}
```

**Problem:** Die `ArrayList` innerhalb einer `ConcurrentHashMap` ist NICHT thread-safe. Mehrere Threads koennen gleichzeitig `add()` und `subList().clear()` auf derselben ArrayList aufrufen, was zu `ConcurrentModificationException` oder Datenverlust fuehrt.

**Loesung:**
```java
transactions.compute(playerUUID, (key, list) -> {
    if (list == null) list = new ArrayList<>();
    list.add(transaction);
    if (list.size() > MAX_TRANSACTIONS_PER_PLAYER) {
        list.subList(0, list.size() - MAX_TRANSACTIONS_PER_PLAYER).clear();
    }
    return list;
});
```

### 2. Race Condition in TransactionHistory.rotateOldTransactions()
**Datei:** `economy/TransactionHistory.java:243-261`
**Schweregrad:** HOCH

```java
// PROBLEM: Iteration ueber ConcurrentHashMap Values + Mutation der inneren ArrayList
for (Map.Entry<UUID, List<Transaction>> entry : transactions.entrySet()) {
    List<Transaction> playerTransactions = entry.getValue();
    playerTransactions.removeIf(t -> t.getTimestamp() < cutoffTime); // NOT THREAD-SAFE
}
```

**Problem:** `removeIf()` auf einer normalen `ArrayList` ist nicht thread-safe wenn gleichzeitig `addTransaction()` aufgerufen wird.

**Loesung:** `CopyOnWriteArrayList` verwenden ODER `compute()` fuer atomare Operationen nutzen.

### 3. Inkonsistente Dateipfade - Potentieller Datenverlust
**Datei:** Mehrere Economy-Manager
**Schweregrad:** MITTEL-HOCH

```java
// EconomyManager.java:32 - relativer Pfad
private static volatile File file = new File("config/plotmod_economy.json");

// InterestManager.java:37 - absoluter Pfad via Server
new File(server.getServerDirectory().toPath().resolve("config").resolve("plotmod_interest.json").toString())
```

**Problem:** Einige Manager verwenden relative Pfade (`"config/..."`) und andere absolute Pfade via `server.getServerDirectory()`. Wenn das Working Directory sich aendert (z.B. bei Forge-Updates), koennten relative Pfade auf falsche Dateien zeigen und Daten gehen verloren.

**Loesung:** Alle Pfade einheitlich via `server.getServerDirectory()` aufloesen.

### 4. EconomyManager.initialize() - Race Condition
**Datei:** `economy/EconomyManager.java:67-77`
**Schweregrad:** MITTEL

```java
public static void initialize(MinecraftServer server) {
    if (instance == null) {    // <-- NICHT synchronized!
        instance = new EconomyManager();
    }
    instance.server = server;
}
```

**Problem:** `initialize()` ist NICHT synchronized, aber `getInstance()` verwendet Double-Checked Locking. Wenn zwei Threads gleichzeitig `initialize()` aufrufen, kann es zu einem Race kommen.

**Loesung:** Entweder `synchronized` hinzufuegen oder die Logik in `getInstance()` integrieren.

---

## FEHLER (Sollten behoben werden)

### 5. TransactionHistory - `needsSave` nicht volatile
**Datei:** `economy/TransactionHistory.java:36`
**Schweregrad:** MITTEL

```java
private boolean needsSave = false; // Fehlt: volatile!
```

**Problem:** `needsSave` wird von `addTransaction()` (potentiell vom Server-Thread) geschrieben und von `save()` (potentiell vom IO-Thread via IncrementalSaveManager) gelesen. Ohne `volatile` koennte der IO-Thread nie die Aenderung sehen.

### 6. DynamicMarketManager - tickCounter nicht thread-safe
**Datei:** `market/DynamicMarketManager.java:64`
**Schweregrad:** NIEDRIG

```java
private volatile int tickCounter = 0;
```

**Problem:** `tickCounter++` (Zeile 213) ist KEINE atomare Operation trotz `volatile`. Bei Zugriff von mehreren Threads koennen Ticks verloren gehen. Da `tick()` aber vermutlich nur vom Server-Thread aufgerufen wird, ist dies in der Praxis kein Problem - `volatile` ist hier unnoetig und `AtomicInteger` waere fuer echte Thread-Safety noetig.

### 7. Transaction erstellt UUID bei jeder Transaktion
**Datei:** `economy/Transaction.java:46`
**Schweregrad:** NIEDRIG (Performance)

```java
this.transactionId = UUID.randomUUID().toString();
```

**Problem:** `UUID.randomUUID()` ist relativ teuer (liest von /dev/urandom). Bei 1000+ Transaktionen pro Minute koennte dies messbar sein. Ein einfacher Zaehler oder Timestamp + Sequence wuerde genuegen.

### 8. WalletManager - Thread-Safety bei Laden
**Datei:** `economy/WalletManager.java:143-144`
**Schweregrad:** MITTEL

```java
wallets.clear();
// Zwischen clear() und putAll() koennten andere Threads zugreifen!
data.forEach((key, value) -> { ... wallets.put(playerUUID, 0.0); });
```

**Problem:** Zwischen `clear()` und dem vollstaendigen Befuellen der Map koennten andere Threads `getBalance()` aufrufen und 0.0 erhalten.

**Loesung:** Die gesamte Load-Operation in einem synchronisierten Block durchfuehren oder eine komplett neue Map erstellen und atomar tauschen.

---

## OPTIMIERUNGSVORSCHLAEGE

### 9. ScheduleMC.java - Massives Registrierungsboilerplate
**Datei:** `ScheduleMC.java:188-301`
**Schweregrad:** Code-Qualitaet

Jede Produktionskette hat 4-5 identische Registrierungszeilen. Das sind ~120 Zeilen repetitiver Code.

**Vorschlag:** Registry-Pattern mit Schleife:
```java
// Statt 120 Zeilen Registrierung:
interface ModRegistrable {
    void registerAll(IEventBus bus);
}

List<ModRegistrable> modules = List.of(
    bus -> { CocaItems.ITEMS.register(bus); CocaBlocks.BLOCKS.register(bus); ... },
    bus -> { PoppyItems.ITEMS.register(bus); PoppyBlocks.BLOCKS.register(bus); ... },
    // ...
);
modules.forEach(m -> m.registerAll(modEventBus));
```

### 10. IncrementalSaveManager - CopyOnWriteArrayList bei Registration
**Datei:** `util/IncrementalSaveManager.java:114-126`
**Schweregrad:** Performance (niedrig)

```java
private final List<ISaveable> saveables = new CopyOnWriteArrayList<>();
```

**Problem:** `CopyOnWriteArrayList.add(insertIndex, saveable)` kopiert das gesamte interne Array bei jedem `register()` Aufruf. Bei 35+ Registrierungen beim Startup sind das 35 Array-Kopien.

**Vorschlag:** Da Registration nur beim Startup passiert und Iteration waehrend der Laufzeit, waere es besser, eine regulaere `ArrayList` zu verwenden und erst nach der Registrierung zu sperren, oder alle Registrierungen zu sammeln und dann einmal zu sortieren.

### 11. Server-Tick Handler - Zu viel pro Tick
**Datei:** `ScheduleMC.java:728-815`
**Schweregrad:** Performance (mittel)

Der `onServerTick` Handler ruft **bei jedem Tick** folgendes auf:
- `PoliceAIHandler.updatePlayerCache()`
- `PoliceAIHandler.updatePoliceCache()`
- 8x `getInstance(server).tick(dayTime)` fuer Economy-Manager

**Problem:** 10+ Methoden-Aufrufe pro Tick (20x/Sekunde = 200 Aufrufe/Sekunde). Einige davon (z.B. `StockMarketData.tick()`, `TransferLimitTracker.tick()`) machen intern nur einen Day-Change-Check.

**Vorschlag:** Day-Change-Check einmal zentral machen und als Flag weitergeben:
```java
boolean isNewDay = (currentDay != lastDay);
if (isNewDay) {
    interestManager.onDayChange(dayTime);
    loanManager.onDayChange(dayTime);
    // ... etc
}
```

### 12. PlotManager.getPlotAt() - Fallback O(n) Scan
**Datei:** `region/PlotManager.java:206-218`
**Schweregrad:** Performance (niedrig)

Der Fallback zur linearen Suche ueber alle Plots (`for (PlotRegion plot : plots.values())`) wird bei Spatial-Index-Misses getriggert. Bei 50.000 Plots waere das ein teurer Scan.

**Vorschlag:** Den Fallback entfernen oder nur im Debug-Modus aktivieren. Stattdessen den Spatial Index robuster machen.

### 13. Verwendung von `double` fuer Geldbetraege
**Datei:** Gesamtes Economy-System
**Schweregrad:** Praezision (mittel)

```java
private static final Map<UUID, Double> balances = new ConcurrentHashMap<>();
```

**Problem:** `double` hat Praezisionsprobleme bei Dezimalzahlen. `0.1 + 0.2 != 0.3` in IEEE 754. Bei vielen Transaktionen koennten sich Rundungsfehler akkumulieren.

**Vorschlag:** Entweder:
1. Intern mit `long` in Cent rechnen (100 = 1.00 EUR), oder
2. `BigDecimal` verwenden (langsamer aber praezise), oder
3. Alle Ergebnisse mit `Math.round(value * 100.0) / 100.0` runden

### 14. CustomNPCEntity - Viele Synced Data Accessors
**Datei:** `npc/entity/CustomNPCEntity.java:68-101`
**Schweregrad:** Netzwerk-Performance (niedrig)

13 EntityDataAccessors werden definiert. Jede Aenderung wird automatisch an alle Clients in der Naehe synchronisiert. Bei 100+ NPCs generiert das erheblichen Netzwerk-Traffic.

**Vorschlag:** Daten die sich selten aendern (NPC_TYPE, MERCHANT_CATEGORY, etc.) koennten ueber NBT gespeichert und nur bei Bedarf via Custom Packets gesendet werden, statt als permanent-synced Data.

### 15. EconomyManager - Static + Singleton Mischung
**Datei:** `economy/EconomyManager.java`
**Schweregrad:** Design (niedrig)

Der `EconomyManager` mischt statische Methoden (`deposit()`, `withdraw()`) mit einem Singleton-Pattern (`getInstance()`). Die Daten sind statisch (`static Map<UUID, Double> balances`), aber die Instanz implementiert `ISaveable`.

**Vorschlag:** Entweder vollstaendig statisch ODER vollstaendig Instanz-basiert - die Mischung macht Testing schwieriger.

---

## SICHERHEITSHINWEISE

### 16. Keine Overflow-Pruefung bei Deposits
**Datei:** `economy/EconomyManager.java:274`
**Schweregrad:** MITTEL

```java
double newBalance = balances.merge(uuid, amount, Double::sum);
```

**Problem:** Wenn ein Spieler extrem viele Deposits erhaelt, kann `Double.MAX_VALUE` ueberschritten werden und zu `Infinity` fuehren. Das bricht dann das gesamte Economy-System.

**Vorschlag:**
```java
double MAX_BALANCE = 999_999_999.99;
double newBalance = balances.merge(uuid, amount, (old, add) ->
    Math.min(old + add, MAX_BALANCE));
```

### 17. Rate Limiter - System-Typ Pruefung
**Datei:** `economy/EconomyManager.java:266-268`
**Schweregrad:** NIEDRIG

```java
if (type == TransactionType.OTHER || type == TransactionType.TRANSFER) {
    if (!depositLimiter.allowOperation(uuid)) { return; }
}
```

**Problem:** Wenn ein Exploit eine System-Transaktion (z.B. `INTEREST`) missbraucht, umgeht er das Rate Limiting. Alle Transaktionstypen sollten eine Plausibilitaetspruefung haben.

---

## ARCHITEKTUR-VORSCHLAEGE

### 18. AbstractPersistenceManager erweitern
Viele Manager (PlotManager, DynamicMarketManager, StateAccount) haben noch eigene Load/Save-Logik statt den `AbstractPersistenceManager` zu nutzen. Migration wuerde ~200 Zeilen duplizierten Code eliminieren.

### 19. Event-System fuer Economy-Aenderungen
Statt dass jeder Manager direkt `EconomyManager.deposit()` aufruft, waere ein Event-System sinnvoll:
```java
EconomyEvents.onDeposit(uuid, amount, type, description);
```
Das wuerde Logging, Achievements, und API-Notifications automatisch triggern.

### 20. Singleton-Reset bei Server-Restart
Einige Singletons (z.B. `TransactionHistory`, `InterestManager`) werden nie zurueckgesetzt. Bei einem Server-Restart innerhalb desselben JVM-Prozesses (z.B. im Dev-Modus) bleiben alte Daten bestehen.

**Vorschlag:** Alle Singletons sollten einen `resetInstance()` Mechanismus haben, der in `onServerStopping()` aufgerufen wird - aehnlich wie es fuer `GangManager` und `LockManager` bereits implementiert ist.

---

## POSITIVE ASPEKTE

Die Codebase hat viele Best Practices bereits implementiert:

1. **Thread-Safety**: ConcurrentHashMap, volatile, AtomicBoolean/Integer konsequent eingesetzt
2. **Persistenz**: Atomic writes, Backup-Rotation, Retry-Mechanismen, Corrupt-File-Sicherung
3. **Performance**: ThreadPoolManager, IncrementalSaveManager, Spatial Index, LRU-Cache
4. **Rate Limiting**: DoS-Protection fuer Economy-Operationen
5. **Input Validation**: Separate InputValidation-Klasse
6. **Error Handling**: EventHelper mit safeExecute verhindert Server-Crashes
7. **Monitoring**: HealthCheckManager, PerformanceMonitor
8. **API Design**: Saubere API-Interfaces mit Implementations

---

## PRIORITAETSLISTE

| Prioritaet | Issue | Aufwand |
|-----------|-------|---------|
| P0 - SOFORT | #1 TransactionHistory Race Condition | 30 min |
| P0 - SOFORT | #2 TransactionHistory Rotation Race | 30 min |
| P1 - BALD | #3 Inkonsistente Dateipfade | 2h |
| P1 - BALD | #4 EconomyManager.initialize() Race | 15 min |
| P1 - BALD | #5 needsSave volatile | 5 min |
| P1 - BALD | #8 WalletManager Load Race | 1h |
| P1 - BALD | #16 Overflow-Pruefung | 30 min |
| P2 - SPAETER | #11 Server-Tick Optimierung | 2h |
| P2 - SPAETER | #13 double zu long/cent Migration | 4h |
| P2 - SPAETER | #20 Singleton-Reset | 2h |
| P3 - NICE-TO-HAVE | #9 Registrierungsboilerplate | 1h |
| P3 - NICE-TO-HAVE | #15 Static/Singleton Mischung | 3h |
| P3 - NICE-TO-HAVE | #18 AbstractPersistenceManager | 3h |
