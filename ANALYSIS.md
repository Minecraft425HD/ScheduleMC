# ScheduleMC – Vollständige Analyse: Gameplay, Performance & Optimierung

**Projekt:** ScheduleMC v3.6.0-beta | Minecraft 1.20.1 | Forge 47.4.0 | Java 17
**Umfang:** 1.419 Java-Dateien, ~43.654 LOC, 50+ Manager-Klassen, 14 Network-Handler, 139+ Commands

---

## Inhaltsverzeichnis

1. [Kritische Bugs (Sofort beheben)](#1-kritische-bugs-sofort-beheben)
2. [Performance-Analyse & Optimierungen](#2-performance-analyse--optimierungen)
3. [Gameplay-Analyse & Balancing](#3-gameplay-analyse--balancing)
4. [Architektur & Code-Qualität](#4-architektur--code-qualität)
5. [Bewertungs-Übersicht](#5-bewertungs-übersicht)
6. [Priorisierte Roadmap](#6-priorisierte-roadmap)

---

## 1. Kritische Bugs (Sofort beheben)

### 1.1 ThreadPoolManager – NullPointerException in Convenience-Methoden

**Datei:** `util/ThreadPoolManager.java` (Zeilen 227-288)
**Schweregrad:** KRITISCH – Crash bei Nutzung vor Initialisierung

Die statischen Convenience-Methoden (`submitIO`, `submitRender`, etc.) greifen direkt auf
statische Felder zu, anstatt die Getter mit Lazy-Initialization zu verwenden:

```java
// FEHLER: Nutzt statisches Feld direkt – NPE wenn Pool noch nicht initialisiert
public static Future<?> submitIO(Runnable task) {
    return IO_POOL.submit(task);
}

// FIX: Getter mit Lazy-Init verwenden
public static Future<?> submitIO(Runnable task) {
    return getIOPool().submit(task);
}
```

**Betroffene Methoden:** `submitIO`, `submitRender`, `submitComputation`, `submitAsync`,
`scheduleAtFixedRate`, `submitScheduled`

---

### 1.2 PerformanceMonitor – Format-String Crash

**Datei:** `util/PerformanceMonitor.java` (Zeile 159)
**Schweregrad:** KRITISCH – `IllegalFormatConversionException` bei jedem Aufruf von `printReport()`

```java
// FEHLER: Python/SLF4J-Syntax statt Java String.format()
LOGGER.info("║ {:27} │ {:6} │ {:5.2f}ms │ {:5.2f}ms │ {:5.2f}ms ║", ...);

// FIX: Java String.format()-Syntax verwenden
LOGGER.info(String.format("║ %-27s │ %6d │ %5.2fms │ %5.2fms │ %5.2fms ║",
    truncate(name, 27), stat.count.get(),
    stat.getAverageMs(), stat.getMinMs(), stat.getMaxMs()));
```

---

### 1.3 RateLimiter – Race Condition beim Window-Reset

**Datei:** `util/RateLimiter.java` (Zeilen 72-75)
**Schweregrad:** MITTEL-HOCH – Rate-Limits können während Fenster-Wechsel überschritten werden

```java
// FEHLER: Check und Reset nicht atomar
if (now - window.windowStart >= windowSizeMs) {
    window.reset(now);  // Nicht synchronisiert!
}
int currentCount = window.count.incrementAndGet();

// FIX: Synchronized Block
synchronized (window) {
    if (now - window.windowStart >= windowSizeMs) {
        window.reset(now);
    }
    currentCount = window.count.incrementAndGet();
}
```

---

### 1.4 RateLimiter – Memory Leak durch unbegrenzte Map

**Datei:** `util/RateLimiter.java` (Zeile 29)
**Schweregrad:** MITTEL – Speicher wächst unbegrenzt bei hoher Spieler-Fluktuation

`cleanupOldEntries()` existiert, wird aber nirgends automatisch aufgerufen.

**Fix:** Periodischen Cleanup im `ThreadPoolManager` registrieren:
```java
ThreadPoolManager.scheduleAtFixedRate(
    () -> rateLimiter.cleanupOldEntries(),
    5, 5, TimeUnit.MINUTES
);
```

---

### 1.5 IncrementalSaveManager – Fehlende Volatile-Deklaration

**Datei:** `util/IncrementalSaveManager.java` (Zeilen 92-95)
**Schweregrad:** MITTEL – Memory-Visibility-Problem bei Multi-Threading

```java
// FEHLER: Nicht volatile, aber von mehreren Threads gelesen/geschrieben
private int saveIntervalTicks = 20;
private int batchSize = 5;

// FIX:
private volatile int saveIntervalTicks = 20;
private volatile int batchSize = 5;
```

---

## 2. Performance-Analyse & Optimierungen

### 2.1 Vorhandene Performance-Infrastruktur (Gut)

| Komponente | Status | Bewertung |
|------------|--------|-----------|
| IncrementalSaveManager | Implementiert | 80-95% weniger Save-Freezes |
| ThreadPoolManager | Implementiert | RAM -100MB, Threads -75% |
| PlotSpatialIndex | Implementiert | O(1) statt O(n) Chunk-Lookups |
| PlotCache (LRU, 1000) | Implementiert | Schnelle wiederholte Zugriffe |
| PerformanceMonitor | Implementiert (Bug!) | Thread-safe Metriken |
| RateLimiter | Implementiert (Bugs!) | DoS-Schutz |
| ConfigCache | Implementiert | Konfigurationscaching |

### 2.2 Konkrete Performance-Optimierungen

#### A) EconomyManager – O(n) Save-Overhead eliminieren

**Datei:** `economy/EconomyManager.java` (Zeilen 175-176)
**Problem:** Bei jedem Save wird eine komplett neue HashMap erstellt und alle UUIDs in Strings konvertiert.
**Impact:** Bei 1000+ Spielern spürbar (alle 5 Min).

```java
// AKTUELL: O(n) Kopie + String-Konvertierung
Map<String, Double> saveMap = new HashMap<>();
balances.forEach((k, v) -> saveMap.put(k.toString(), v));

// VORSCHLAG: Custom Gson TypeAdapter der direkt aus ConcurrentHashMap serialisiert
public class UUIDBalanceSerializer implements JsonSerializer<ConcurrentHashMap<UUID, Double>> {
    @Override
    public JsonElement serialize(ConcurrentHashMap<UUID, Double> src, ...) {
        JsonObject obj = new JsonObject();
        src.forEach((uuid, balance) -> obj.addProperty(uuid.toString(), balance));
        return obj;
    }
}
```

#### B) IncrementalSaveManager – getDirtyCount() von O(n) auf O(1)

**Datei:** `util/IncrementalSaveManager.java` (Zeilen 337-345)
**Problem:** Iteriert die gesamte Saveable-Liste bei jedem Aufruf.

```java
// AKTUELL: O(n) pro Aufruf
public int getDirtyCount() {
    int dirty = 0;
    for (ISaveable saveable : saveables) {
        if (saveable.isDirty()) dirty++;
    }
    return dirty;
}

// VORSCHLAG: AtomicInteger Counter mitführen
private final AtomicInteger dirtyCount = new AtomicInteger(0);

public void markDirty(ISaveable saveable) {
    if (!saveable.wasDirty()) dirtyCount.incrementAndGet();
}
public void markClean(ISaveable saveable) {
    dirtyCount.decrementAndGet();
}
public int getDirtyCount() {
    return dirtyCount.get(); // O(1)
}
```

#### C) PlotCache – Effizientere LRU-Implementierung

**Datei:** `region/PlotCache.java` (Zeilen 76-82)
**Problem:** `LinkedHashMap(access-order=true)` reordnet bei jedem `get()` die interne Liste.
Synchronisierte Zugriffe auf LinkedHashMap erzeugen Lock-Contention.

**Vorschlag:** Caffeine Library oder ConcurrentLinkedHashMap verwenden:
```java
// Statt Collections.synchronizedMap(LinkedHashMap):
Cache<BlockPos, CacheEntry> cache = Caffeine.newBuilder()
    .maximumSize(1000)
    .expireAfterAccess(5, TimeUnit.MINUTES)
    .recordStats()
    .build();
```

#### D) PlotCache – invalidateRegion() Synchronisation optimieren

**Datei:** `region/PlotCache.java` (Zeilen 197-245)
**Problem:** TOCTOU Race-Condition bei `chunkIndex.get()` ohne Lock.
Gesamter Cache wird bei `invalidatePlot()` gesperrt → blockiert alle get()-Aufrufe.

**Vorschlag:** Copy-on-Read Pattern oder feinere Lock-Granularität:
```java
// Statt synchronized(cache) über den gesamten Cache:
// Verwende ReadWriteLock für concurrent reads, exclusive writes
private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

public PlotRegion get(BlockPos pos) {
    rwLock.readLock().lock();
    try { return cache.get(pos); }
    finally { rwLock.readLock().unlock(); }
}

public void invalidate(int plotId) {
    rwLock.writeLock().lock();
    try { /* invalidation logic */ }
    finally { rwLock.writeLock().unlock(); }
}
```

#### E) CustomNPCEntity – Tick-Optimierung

**Datei:** `npc/entity/CustomNPCEntity.java`
**Problem:** `syncEmotionState()` wird jeden Tick aufgerufen, auch wenn sich nichts geändert hat.
`calculateActivityStatus()` alle 100 Ticks recalculated statt on-demand.

**Vorschlag:**
```java
// Nur synchronisieren wenn sich der Zustand geändert hat:
private EmotionState lastSyncedEmotion = null;

private void syncEmotionState() {
    EmotionState current = getCurrentEmotion();
    if (!current.equals(lastSyncedEmotion)) {
        lastSyncedEmotion = current;
        sendSyncPacket(current); // Netzwerk-Traffic nur bei Änderung
    }
}
```

#### F) HealthCheckManager – Parallelisierte Checks

**Datei:** `util/HealthCheckManager.java`
**Problem:** 38 Health-Checks laufen sequentiell. Code-Duplikation (30+ ähnliche Methoden).

**Vorschlag:**
```java
// 1. Check-Registry statt 38 einzelne Methoden:
private final Map<String, Supplier<HealthStatus>> checks = new LinkedHashMap<>();

public void registerCheck(String name, Supplier<HealthStatus> check) {
    checks.put(name, check);
}

// 2. Parallel ausführen:
public Map<String, HealthStatus> runAllChecks() {
    return checks.entrySet().parallelStream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            e -> e.getValue().get()
        ));
}
```

#### G) BackupManager – Komprimierung einführen

**Datei:** `util/BackupManager.java`
**Problem:** Backups unkomprimiert gespeichert. Nur 5 Backups (hardcoded), kein Config.

**Vorschlag:**
```java
// GZIP-Komprimierung für ~70-80% kleinere Backups:
public static void createBackup(Path source) {
    Path backupPath = backupDir.resolve(source.getFileName() + ".gz");
    try (GZIPOutputStream gzos = new GZIPOutputStream(Files.newOutputStream(backupPath))) {
        Files.copy(source, gzos);
    }
}
```

### 2.3 Performance-Zusammenfassung

| Optimierung | Erwarteter Impact | Aufwand |
|-------------|-------------------|---------|
| ThreadPoolManager NPE Fix | Crash-Vermeidung | 10 Min |
| PerformanceMonitor Format Fix | Crash-Vermeidung | 5 Min |
| RateLimiter Synchronisation | Korrekte Rate-Limits | 20 Min |
| volatile saveIntervalTicks | Thread-Safety | 2 Min |
| EconomyManager Save | -30% Save-Zeit bei 1000+ Spieler | 1 Std |
| getDirtyCount O(1) | -95% CPU pro Statistik-Abfrage | 30 Min |
| PlotCache Caffeine | -40% Lock-Contention | 2 Std |
| NPC Emotion Sync | -80% NPC-Netzwerk-Traffic | 30 Min |
| HealthCheck Parallel | -60% Check-Laufzeit | 1 Std |
| Backup Komprimierung | -70% Backup-Größe | 30 Min |

---

## 3. Gameplay-Analyse & Balancing

### 3.1 Reputations-System – Unbalanciert (KRITISCH)

**Dateien:** `npc/life/NPCLifeSystemIntegration.java`, `npc/crime/CrimeManager.java`

#### Problem: "Todesspirale" ohne Ausweg

| Aktion | Reputation-Verlust | Erholung nötig |
|--------|-------------------|----------------|
| Crime beobachtet (Severity 1) | -3 ORDNUNG, -1.5 BÜRGER | 3+ gute Taten |
| Crime beobachtet (Severity 5) | -15 ORDNUNG, -7.5 BÜRGER | 15+ gute Taten |
| NPC getötet | -20 ORDNUNG, -15 BÜRGER, -25 HÄNDLER | ~60 gute Taten |
| Bestechung abgelehnt (!) | -10 ORDNUNG, -5 BÜRGER | 15 gute Taten |
| Bestechung angenommen | -2 Reputation | 2 gute Taten |

**Hauptprobleme:**
1. **Bestechung ablehnen wird BESTRAFT** (-10) statt belohnt → Spieler werden zur Korruption angereizt
2. **Keine Tilgung/Vergebung** – Einmal zerstörte Reputation ist kaum wiederherstellbar
3. **Kein Reputations-Verfall über Zeit** – Vergehen bleiben permanent
4. **3 NPC-Tode = permanente Feindschaft** aller Fraktionen, kein Weg zurück

#### Vorschläge:

```
A) Reputation-Decay einführen:
   - Negative Reputation verblasst: -10% pro Spieltag (nach 10 Tagen vergessen)
   - Positive Reputation verblasst langsamer: -5% pro Spieltag

B) Bestechungs-Logik umkehren:
   - Bestechung ABLEHNEN: +5 ORDNUNG, +3 BÜRGER (Belohnung für Ehrlichkeit)
   - Bestechung ANNEHMEN: -5 ORDNUNG, -3 BÜRGER (Strafe für Korruption)

C) Vergebungs-Quests:
   - Ab -20 Reputation: "Wiedergutmachungs-Quest" verfügbar
   - Questbelohnung: +15 Reputation bei der betroffenen Fraktion

D) Amnestie-System:
   - Alle 30 Spieltage: Teilamnestie (-50% aller negativen Reputation)
   - Oder: Geldstrafe zahlen für sofortige Teilamnestie

E) Diminishing Returns bei Strafen:
   - 1. Vergehen: volle Strafe
   - 2. Vergehen (gleicher Typ): 80% Strafe
   - 5. Vergehen: 50% Strafe (Bevölkerung "gewöhnt sich dran")
```

---

### 3.2 Crime/Wanted-System – Zu Bestrafend

**Datei:** `npc/crime/CrimeManager.java`

#### Probleme:

1. **Keine Crime-Severity-Differenzierung:** Ladendiebstahl = Mord (gleiche Wanted-Level-Erhöhung)
2. **Flucht ist zu einfach:** 30 Sekunden verstecken = -1 Wanted (unabhängig vom Level)
3. **Wanted-Decay zu langsam:** 1 Level/Tag → Level 5 braucht 5 Tage sauberes Spielen
4. **Keine Korruptions-Mechanik:** Keine Möglichkeit, Polizei zu bestechen

#### Vorschläge:

```
A) Crime-Severity-Skalierung:
   - Ladendiebstahl:  +1 Wanted
   - Einbruch:        +2 Wanted
   - Körperverletzung: +3 Wanted
   - Mord:            +4 Wanted
   - Massenmord:      +5 Wanted (sofort max)

B) Flucht-Skalierung nach Wanted-Level:
   - Level 1: 20 Sek verstecken = entkommen
   - Level 2: 40 Sek
   - Level 3: 60 Sek + Polizei sucht aktiv
   - Level 4: 90 Sek + Hubschrauber (Polizei sieht weiter)
   - Level 5: 120 Sek + Straßensperren (NPCs blockieren Wege)

C) Wanted-Decay beschleunigen:
   - Statt 1/Tag → 1/12 Stunden Spielzeit
   - Community-Service-Quests: Sofort -1 Wanted Level

D) Geldstrafe als Alternative:
   - Level 1-2: Geldstrafe zahlen → sofort clean
   - Level 3+: Gefängniszeit ODER hohe Geldstrafe
```

---

### 3.3 Gang-System – Fee-Falle für Casual-Spieler

**Datei:** `gang/GangManager.java` (Zeilen 324-390)

#### Problem:

- RECRUIT zahlt 100% der Gebühr (die höchste!)
- 3 verpasste Zahlungen = Auto-Kick
- Neue Spieler werden bestraft für Inaktivität

#### Vorschläge:

```
A) Gebühren-Staffelung umkehren:
   - RECRUIT: 25% (Einsteigerfreundlich)
   - MEMBER: 50%
   - UNDERBOSS: 75%
   - BOSS: 100% (höchste Verantwortung)

B) Urlaubs-System:
   - /gang vacation: 2 Wochen Gebührenbefreiung (1x pro Monat)
   - Automatisch wenn Spieler >7 Tage offline

C) Auto-Kick Grenze erhöhen:
   - 3 → 6 verpasste Zahlungen (6 Wochen Toleranz)
   - Warnung per /msg bei 3 und 5 verpassten Zahlungen

D) Gang-Aktivitäten als Gebühren-Ersatz:
   - Abgeschlossene Gang-Mission = 1 Woche Gebühr erlassen
   - XP-Beitrag ab Schwellwert = Gebühr erlassen
```

---

### 3.4 Markt-System – Opake Preise

**Dateien:** `market/DynamicMarketManager.java`, `npc/life/economy/DynamicPriceManager.java`

#### Probleme:

1. **Supply/Demand-Faktor zu schwach** (0.3) – Markt reagiert kaum auf Handel
2. **Decay zu aggressiv** (10%) – Markt vergisst in 50 Minuten
3. **Marktbedingungen rein zufällig** (30% tägliche Änderungschance)
4. **Keine Spieler-Sichtbarkeit** – Kein UI für Preistrends
5. **Keine Item-Kategorien** – Luxusgüter und Grundnahrung gleich elastisch

#### Vorschläge:

```
A) Supply/Demand-Faktor erhöhen:
   - 0.3 → 0.6 (Markt reagiert spürbar)
   - Decay reduzieren: 10% → 3% (langsamer vergessen)

B) Preis-Transparenz für Spieler:
   - /market trends: Zeigt Top 10 steigende/fallende Items
   - /market history <item>: Preisverlauf der letzten 7 Tage
   - NPC-Händler nennen aktuelle Marktlage ("Käse ist gerade teuer!")

C) Item-Elastizität einführen:
   - Grundnahrung: Elastizität 0.2 (Preise schwanken wenig)
   - Luxusgüter: Elastizität 1.5 (starke Schwankungen)
   - Illegale Waren: Elastizität 2.0 (sehr volatil)

D) Spieler-gesteuerte Markt-Events:
   - Wenn >10 Spieler gleiches Item verkaufen → "Überangebot"-Event
   - Wenn Item 3 Tage nicht gehandelt → "Mangel"-Event

E) Saisonale Preise:
   - Frühling: Agrarprodukte -20%
   - Winter: Heizmaterial +30%
   - Feste/Events: Luxusgüter +50%
```

---

### 3.5 NPC-KI – Passive Statisten statt lebendige Welt

**Dateien:** `npc/life/*`, `npc/entity/CustomNPCEntity.java`

#### Aktuelle Stärken:
- Tagesablauf (Arbeit → Freizeit → Zuhause)
- Fraktionszugehörigkeit
- Gerüchte-Netzwerk
- Emotionssystem

#### Fehlende Features für lebendige Welt:

```
A) NPC-zu-NPC Interaktionen:
   - NPCs handeln untereinander → beeinflusst Marktpreise
   - NPCs streiten sich → Spieler können schlichten
   - NPCs bilden Freundschaften → beeinflusst Gerüchte-Verbreitung

B) Proaktives NPC-Verhalten:
   - NPCs sprechen Spieler an (statt nur zu warten)
   - NPCs warnen vor Gefahren ("In der Gasse wurde jemand überfallen!")
   - NPCs fliehen bei hohem Wanted-Level des Spielers

C) NPC-Gedächtnis:
   - NPCs erinnern sich an vergangene Gespräche
   - Wiederholte Interaktionen vertiefen Beziehung
   - NPCs, die oft besucht werden, geben bessere Preise

D) Gerüchte mit Konsequenzen:
   - Gerüchte beeinflussen NPC-Verhalten (nicht nur Reputation)
   - NPCs können Gerüchte an Polizei weitergeben
   - Falsche Gerüchte möglich (Spieler kann Gerüchte streuen)

E) Wirtschaftliches NPC-Verhalten:
   - NPCs reagieren auf Marktpreise (kaufen billig, verkaufen teuer)
   - NPCs wechseln Beruf wenn aktueller Beruf unrentabel
   - NPCs streiken bei schlechten Bedingungen
```

---

### 3.6 Dialogue-System – Flavor-Text statt Gameplay

**Datei:** `npc/life/dialogue/DialogueManager.java`

#### Probleme:
1. Nur 2 generische Optionen (Handeln / Chatten)
2. Keine Gesprächs-Konsequenzen
3. Kein Gedächtnis vergangener Gespräche
4. Stille Fehler wenn Dialogue-Tree null zurückgibt

#### Vorschläge:

```
A) Konsequenz-basierte Dialoge:
   - "Kannst du mir einen Gefallen tun?" → Startet Quest
   - "Was weißt du über [NPC]?" → Gibt Hinweis (kostet Reputation)
   - "Ich brauche einen Rabatt." → Verhandlung basierend auf Reputation

B) Gesprächs-Memory:
   - NPC: "Letzte Woche hast du mir bei [Quest] geholfen. Dafür 10% Rabatt."
   - NPC: "Du hast mich das letzte Mal angelogen. Gespräch beendet."

C) Skill-basierte Dialog-Optionen:
   - Hoher Handel-Skill → "Überreden"-Option verfügbar
   - Hoher Crime-Skill → "Einschüchtern"-Option verfügbar
   - Hohe Reputation → "Bevorzugter Kunde"-Preise

D) Dynamische Dialog-Inhalte:
   - NPCs sprechen über aktuelle Marktsituation
   - NPCs warnen vor Spielern mit hohem Wanted-Level
   - NPCs beziehen sich auf aktuelle World-Events
```

---

### 3.7 Produktions-Ketten – Verbesserungspotential

#### Aktuelle Stärken:
- 14 Produktionsketten (8 illegal, 6 legal)
- Multi-Stage-Verarbeitung
- Qualitätssystem

#### Fehlende Features:

```
A) Risiko/Belohnung für illegale Produktion:
   - Illegal: Höhere Margen, aber Busted-Risiko
   - Legal: Niedrigere Margen, aber sicher und steuerlich absetzbar
   - Balancing: Illegal sollte ~2-3x profitabler sein als legal

B) Produktions-Events:
   - Zufällige Events: "Polizei-Razzia" → illegale Produktion pausiert für 1h
   - "Erntesaison" → legale Produktion +50% Output
   - "Lieferengpass" → bestimmte Rohstoffe teurer

C) Kooperative Produktion:
   - Spieler können Produktion aufteilen (einer erntet, einer verarbeitet)
   - Bonus für Arbeitsteilung: +20% Qualität

D) Verfall-System:
   - Verderbliche Waren (Käse, Bier) verlieren Qualität über Zeit
   - Lager-Upgrades verlangsamen Verfall
   - Schafft Handel-Urgency (verkaufe bevor es verdirbt)
```

---

### 3.8 Onboarding – Überwältigende Komplexität

#### Problem:
30+ Systeme gleichzeitig aktiv. Neue Spieler werden mit Economy, Plots, NPCs, Produktion,
Gangs, Crime, Markt, etc. überflutet.

#### Vorschläge:

```
A) Progressive System-Freischaltung:
   Tag 1:    Grundlagen (Economy, Plot kaufen)
   Tag 2-3:  Produktion (1 legale Kette)
   Tag 4-5:  NPCs & Handel
   Tag 6-7:  Gangs (Einladung erhalten)
   Tag 8-10: Illegale Produktion (wenn gewünscht)
   Tag 11+:  Alle Systeme freigeschaltet

B) Tutorial-Quest-Reihe:
   - "Willkommen in der Stadt" → Plot kaufen, Möbel platzieren
   - "Dein erstes Geschäft" → Käse produzieren und verkaufen
   - "Freunde finden" → 3 NPCs ansprechen
   - "Straßenleben" → Gang beitreten oder gründen

C) Mentor-System:
   - Erfahrene Spieler als Mentoren markierbar
   - Neue Spieler werden automatisch Mentoren zugewiesen
   - Mentor erhält XP-Bonus für betreute Neulinge
```

---

## 4. Architektur & Code-Qualität

### 4.1 God-Object Anti-Pattern

**Betroffene Klassen:**
- `CustomNPCEntity.java` (865 Zeilen) – Rendering, Verhalten, Persistenz, Interaktion alles in einer Klasse
- `HealthCheckManager.java` – 38 direkte Abhängigkeiten, 30+ duplizierte Methoden
- `ScheduleMC.java` (903 Zeilen) – Alle Subsysteme direkt registriert

**Vorschlag:** Composition over Inheritance
```java
// Statt monolithischem NPC:
public class CustomNPCEntity extends PathfinderMob {
    private final NPCBehaviorComponent behavior;
    private final NPCLifeComponent life;
    private final NPCRenderComponent render;
    private final NPCPersistenceComponent persistence;
}
```

### 4.2 Singleton-Abhängigkeiten

Fast alle Manager verwenden das Singleton-Pattern → unmöglich zu testen.

**Vorschlag:** Service-Registry oder einfaches Dependency Injection:
```java
// Statt: EconomyManager.getInstance().deposit(...)
// Service-Registry:
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

### 4.3 HealthCheckManager – Code-Duplikation

30+ Methoden mit identischem Pattern. Sollte durch eine Check-Registry ersetzt werden:
```java
// Statt 30 Methoden mit copy-paste:
public class HealthCheckManager {
    private final List<NamedHealthCheck> checks = new ArrayList<>();

    public void register(String name, Supplier<HealthStatus> check) {
        checks.add(new NamedHealthCheck(name, check));
    }

    // Automatische Registrierung aller Manager:
    public void autoRegister() {
        register("Economy", () -> checkManager(EconomyManager.getInstance()));
        register("Plots", () -> checkManager(PlotManager.getInstance()));
        // ...
    }
}
```

### 4.4 Fehlende Error-Handling Patterns

- **Kein Circuit-Breaker:** Wenn ein System ausfällt, kaskadieren Fehler
- **TransactionHistory.save()** Ergebnis wird in EconomyManager ignoriert
- **DialogueManager** gibt silent null zurück bei fehlenden Dialogue-Trees

**Vorschlag:** Circuit-Breaker für kritische Systeme:
```java
public class CircuitBreaker {
    private final int failureThreshold;
    private final AtomicInteger failures = new AtomicInteger(0);
    private volatile boolean open = false;

    public <T> T execute(Supplier<T> operation, T fallback) {
        if (open) return fallback;
        try {
            T result = operation.get();
            failures.set(0);
            return result;
        } catch (Exception e) {
            if (failures.incrementAndGet() >= failureThreshold) {
                open = true;
                scheduleReset();
            }
            return fallback;
        }
    }
}
```

### 4.5 Konfiguration – Zu Starr

- **Kein Hot-Reload:** Alle Config-Änderungen erfordern Neustart
- **Hardcoded Limits:** MAX_PLOTS=50.000, MAX_BACKUPS=5, etc.
- **Keine Config-Validierung:** Ungültige Kombinationen möglich
- **Keine Config-Migration:** Kein Upgrade-Pfad bei Config-Änderungen

---

## 5. Bewertungs-Übersicht

### Performance

| Bereich | Bewertung | Kommentar |
|---------|-----------|-----------|
| Save-System | 8/10 | IncrementalSaveManager ist exzellent |
| Thread-Management | 7/10 | ThreadPoolManager gut, aber NPE-Bug |
| Caching | 6/10 | PlotCache funktioniert, aber suboptimale Implementierung |
| Netzwerk | 7/10 | 14 Handler gut strukturiert |
| Speicher | 6/10 | Memory Leaks in RateLimiter, keine Limits in Registries |

### Gameplay

| Bereich | Bewertung | Kommentar |
|---------|-----------|-----------|
| Economy | 8/10 | Umfangreich mit Zinsen, Krediten, Steuern |
| Reputation | 3/10 | Todesspirale, Bestechungs-Bug, kein Verfall |
| Crime/Wanted | 4/10 | Keine Severity, zu bestrafend |
| NPCs | 5/10 | Gute Basis, aber zu passiv |
| Gangs | 6/10 | Gute Progression, aber Fee-Falle |
| Markt | 4/10 | Opak, schwache Reaktion, keine Transparenz |
| Produktion | 7/10 | 14 Ketten, Multi-Stage, Qualität |
| Dialogue | 4/10 | Flavor-Text ohne Konsequenzen |
| Onboarding | 2/10 | 30+ Systeme ohne Einführung |

### Code-Qualität

| Bereich | Bewertung | Kommentar |
|---------|-----------|-----------|
| Architektur | 6/10 | Gute Modularität, aber God-Objects |
| Thread-Safety | 5/10 | Mehrere Race Conditions |
| Testbarkeit | 3/10 | Singletons überall, kaum mockbar |
| Duplikation | 4/10 | HealthCheckManager, PlotManager Filter |
| Error-Handling | 5/10 | Silent Failures, kein Circuit-Breaker |
| Konfiguration | 4/10 | Zu starr, kein Hot-Reload |

---

## 6. Priorisierte Roadmap

### Phase 1: Kritische Fixes (Sofort)
1. ThreadPoolManager NPE-Bug fixen (Convenience-Methoden → Getter)
2. PerformanceMonitor Format-String fixen
3. RateLimiter Synchronisation + Auto-Cleanup
4. IncrementalSaveManager volatile-Felder
5. Bestechungs-Logik umkehren (ablehnen belohnen, annehmen bestrafen)

### Phase 2: Gameplay-Balancing (Kurzfristig)
6. Reputations-Decay über Zeit einführen
7. Crime-Severity-System implementieren
8. Gang-Gebühren invertieren (Recruits zahlen weniger)
9. Markt-Transparenz (Preistrends sichtbar machen)
10. Wanted-Level-Flucht nach Level skalieren

### Phase 3: Performance (Mittelfristig)
11. PlotCache auf Caffeine oder ReadWriteLock umstellen
12. EconomyManager Save-Overhead reduzieren
13. NPC Emotion-Sync nur bei Änderung
14. HealthCheckManager zu Registry-Pattern refactoren
15. Backup-Komprimierung (GZIP)

### Phase 4: Gameplay-Erweiterungen (Langfristig)
16. Tutorial/Onboarding-System
17. Vergebungs-Quests für Reputation
18. NPC-zu-NPC Interaktionen
19. Dialogue-Konsequenzen
20. Saisonale Marktpreise
21. Produktions-Events und Risiko-System

### Phase 5: Architektur (Langfristig)
22. Service-Registry statt Singletons
23. Circuit-Breaker für kritische Systeme
24. Config Hot-Reload
25. NPC-Entity zu Component-System refactoren
