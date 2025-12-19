# ScheduleMC Mod - Analyse und Verbesserungsvorschläge

## Übersicht

**ScheduleMC** ist eine umfangreiche Minecraft Forge Mod (v2.7.0-beta) für **Minecraft 1.20.1**, die ein komplettes Server-Wirtschafts- und Roleplay-System bietet.

### Aktuelle Statistiken
- **608 Java-Dateien** mit ca. 21.148 Codezeilen
- **155+ automatisierte Tests** (127 Unit + 28 Integration)
- **8 Produktionsketten** für verschiedene Substanzen
- **Mehrsprachig**: Deutsch und Englisch

---

## 1. Aktuelle Stärken der Mod

| Bereich | Stärke |
|---------|--------|
| Feature-Umfang | Umfassendes Wirtschafts-, NPC- und Kriminalitätssystem |
| Testabdeckung | 155+ automatisierte Tests |
| Architektur | Modulare Struktur mit klaren Grenzen |
| Performance | Spatial Indexing, Caching, Dirty-Flag Patterns |
| Datensicherheit | Atomare Schreibvorgänge, Backup-Rotation |
| Erweiterbarkeit | Klares API-Design, Strategy Patterns |

---

## 2. Kritische Verbesserungen (Hohe Priorität)

### 2.1 Datenbank-Migration

**Problem:** Aktuell wird JSON-basierte Datei-Speicherung verwendet.

**Vorschlag:**
- Migration zu **SQLite** (Single-Server) oder **MySQL/MariaDB** (Multi-Server)
- Vorteile:
  - Bessere Concurrent Access
  - SQL-Abfragen für komplexe Statistiken
  - Transaktionssicherheit
  - Skalierbarkeit für 1000+ Spieler

**Implementierungsplan:**
```java
// Neue Interface-Struktur
public interface IDataProvider {
    void save(String key, Object data);
    <T> T load(String key, Class<T> type);
    <T> List<T> query(String sql, Class<T> type);
}

// Implementierungen
public class JsonDataProvider implements IDataProvider { ... }
public class SQLiteDataProvider implements IDataProvider { ... }
public class MySQLDataProvider implements IDataProvider { ... }
```

### 2.2 Asynchrone Datei-Operationen

**Problem:** Synchrone I/O kann den Hauptthread blockieren.

**Vorschlag:**
```java
public CompletableFuture<Void> saveAsync(PlotData data) {
    return CompletableFuture.runAsync(() -> {
        saveToFile(data);
    }, Executors.newFixedThreadPool(2));
}
```

### 2.3 Anti-Cheat System

**Problem:** Keine Validierung von Spieler-Transaktionen.

**Vorschlag:**
- Server-seitige Validierung aller Geldtransaktionen
- Logging verdächtiger Aktivitäten
- Automatische Rollback-Mechanismen

---

## 3. Feature-Erweiterungen (Mittlere Priorität)

### 3.1 Erweitertes Gefängnis-System

**Aktuelle Situation:** Spieler werden nur bei der Polizeistation festgenommen.

**Neue Features:**
- Gefängnis-Dimension oder -Bereich
- Zeitbasierte Entlassung (Echtzeit oder Spielzeit)
- Bewährung/Parole-System
- Fluchtmöglichkeiten mit Konsequenzen
- Gefängnis-Jobs zur Strafverkürzung

```java
public class PrisonManager {
    private Map<UUID, PrisonSentence> prisoners = new HashMap<>();

    public void imprison(Player player, int minutes, String reason) {
        PrisonSentence sentence = new PrisonSentence(
            player.getUUID(),
            System.currentTimeMillis(),
            minutes * 60 * 1000,
            reason
        );
        prisoners.put(player.getUUID(), sentence);
        teleportToPrison(player);
    }

    public void checkForRelease() {
        long now = System.currentTimeMillis();
        prisoners.entrySet().removeIf(entry -> {
            if (entry.getValue().isExpired(now)) {
                releasePlayer(entry.getKey());
                return true;
            }
            return false;
        });
    }
}
```

### 3.2 Gang/Fraktion-System

**Neue Features:**
- Spieler-Gangs mit Hierarchie (Boss, Underboss, Soldaten)
- Territorien mit Einnahmen
- Gang-Kriege und Allianzen
- Gemeinsame Gang-Kasse
- Reputation zwischen Gangs

```java
public class Gang {
    private String name;
    private UUID leader;
    private Map<UUID, GangRank> members;
    private List<PlotRegion> territories;
    private double treasury;
    private Map<Gang, GangRelation> relations; // NEUTRAL, ALLY, WAR
}
```

### 3.3 Heist/Überfall-System

**Neue Features:**
- Banküberfälle mit Timer
- Geldtransporter-Überfälle
- Team-basierte Missionen
- Alarm-Systeme mit Polizei-Reaktion
- Fluchtfahrzeug-Integration

```java
public class HeistManager {
    public HeistInstance startBankHeist(List<Player> crew, BankLocation bank) {
        // Starte Timer für Tresor-Öffnung
        // Aktiviere Polizei-Alarm nach X Sekunden
        // Spawne Fluchtwagen-Marker
        // Tracke Beute
    }
}
```

### 3.4 Aktien-/Börsen-System

**Neue Features:**
- Investitionen in NPC-Shops
- Dynamische Aktienkurse basierend auf Shop-Performance
- Dividenden-Ausschüttungen
- Markt-Manipulation möglich (Insider-Trading)
- Wirtschafts-Crash-Events

```java
public class StockMarket {
    private Map<NPCShop, Stock> stocks = new HashMap<>();

    public double getStockPrice(NPCShop shop) {
        // Basierend auf: Umsatz, Bewertung, Markttrends
        double basePrice = shop.getWeeklyRevenue() / 100;
        double trend = calculateMarketTrend();
        return basePrice * trend;
    }

    public void buyStock(Player player, NPCShop shop, int quantity) {
        double price = getStockPrice(shop) * quantity;
        if (economyManager.withdraw(player, price)) {
            addShares(player, shop, quantity);
        }
    }
}
```

### 3.5 Immobilien-Erweiterungen

**Neue Features:**
- **Hypotheken-System**: Grundstückskauf auf Kredit
- **Versicherungen**: Gegen Raids, Diebstahl, Feuer
- **Grundstückssteuern**: Jährliche Steuern basierend auf Wert
- **Zwangsversteigerungen**: Bei Zahlungsausfall
- **Maklerei**: NPCs die Grundstücke vermitteln

```java
public class MortgageManager {
    public Mortgage createMortgage(Player player, PlotRegion plot, int years) {
        double plotPrice = plot.getPrice();
        double downPayment = plotPrice * 0.2; // 20% Anzahlung
        double loanAmount = plotPrice * 0.8;
        double monthlyRate = calculateMonthlyPayment(loanAmount, years, 0.05);

        return new Mortgage(player.getUUID(), plot.getId(),
                           loanAmount, monthlyRate, years * 12);
    }
}
```

---

## 4. NPC-System Verbesserungen

### 4.1 Erweiterte KI

**Aktuelle Situation:** Basis-Navigation mit einfachen Goals.

**Verbesserungen:**
- **Gedächtnis-System**: NPCs erinnern sich an Spieler
- **Beziehungen**: Freundschaft/Feindschaft zu Spielern
- **Stimmungen**: Beeinflusst Preise und Interaktionen
- **Klatsch/Gerüchte**: NPCs tauschen Informationen aus

```java
public class NPCMemory {
    private Map<UUID, PlayerMemory> memories = new HashMap<>();

    public void rememberPlayer(UUID player, MemoryType type, String context) {
        memories.computeIfAbsent(player, k -> new PlayerMemory())
                .addMemory(type, context, System.currentTimeMillis());
    }

    public double getPriceModifier(UUID player) {
        PlayerMemory memory = memories.get(player);
        if (memory == null) return 1.0;

        // Gute Beziehung = günstiger
        // Schlechte Beziehung = teurer
        return 1.0 - (memory.getRelationship() * 0.1);
    }
}
```

### 4.2 NPC-Dialoge mit Quests

**Neue Features:**
- Quest-System über NPC-Dialoge
- Lieferaufträge
- Beschaffungs-Quests
- Story-basierte Missionen

```java
public class NPCQuestSystem {
    public Quest generateDeliveryQuest(NPC source, NPC target) {
        Item item = source.getRandomSaleItem();
        int quantity = random.nextInt(5, 20);
        double reward = item.getValue() * quantity * 1.5;

        return new DeliveryQuest(source, target, item, quantity, reward);
    }
}
```

### 4.3 NPC-Routinen

**Erweiterte Tagesabläufe:**
- Mittagspause (Shop schließt)
- Wochenend-Öffnungszeiten
- Urlaub/Krankheit
- Events (Feiertage, Sales)

---

## 5. Produktions-System Erweiterungen

### 5.1 Labor-System

**Neue Features:**
- Verbessertes Meth-Labor mit Ausrüstung
- Laborunfälle/Explosionen bei falscher Bedienung
- Chemikalien-Beschaffung als Quest
- Qualitätsstufen basierend auf Equipment

### 5.2 Anbau-Erweiterungen

**Neue Features:**
- **Wetter-Effekte**: Regen = schnelleres Wachstum
- **Jahreszeiten-Mod Integration**: Saisonale Anbau-Zeiten
- **Schädlinge**: Können Ernte zerstören
- **Dünger-System**: Beschleunigtes Wachstum
- **Bewässerung**: Automatische Bewässerungsanlagen

```java
public class AdvancedGrowthHandler {
    public double calculateGrowthModifier(PlantPot pot, Level level) {
        double modifier = 1.0;

        // Wetter
        if (level.isRaining()) modifier *= 1.2;
        if (level.isThundering()) modifier *= 0.8;

        // Licht
        int light = level.getBrightness(pot.getBlockPos());
        modifier *= (light / 15.0);

        // Dünger
        if (pot.hasFertilizer()) modifier *= 1.5;

        return modifier;
    }
}
```

### 5.3 Vertriebskette

**Neue Features:**
- **Dealer-NPCs**: Kaufen Produkte zu dynamischen Preisen
- **Dealer-Territorien**: Exklusive Verkaufsgebiete
- **Polizei-Razzias**: Zufällige Kontrollen
- **Verstecke**: Sichere Lagerplätze

---

## 6. Fahrzeug-System Erweiterungen

### 6.1 Fahrzeug-Klassen

**Neue Fahrzeuge:**
- **Motorräder**: Schneller, weniger Schutz
- **LKW**: Langsamer, mehr Ladekapazität
- **Boote**: Wasser-Transport
- **Helikopter**: Premium, teuer im Unterhalt

### 6.2 Fahrzeug-Mechaniken

**Neue Features:**
- **Schadens-System**: Kollisionen beschädigen Fahrzeuge
- **Werkstätten**: NPC-Reparatur-Service
- **Tuning**: Leistungssteigerung
- **Versicherung**: Gegen Diebstahl/Totalschaden

```java
public class VehicleDamageComponent {
    private float condition = 100.0f; // 0-100

    public void onCollision(float impactForce) {
        float damage = impactForce * 0.1f;
        condition = Math.max(0, condition - damage);

        if (condition < 20) {
            // Fahrzeug fährt langsamer
            vehicle.setMaxSpeed(vehicle.getBaseMaxSpeed() * 0.5f);
        }

        if (condition <= 0) {
            // Totalschaden
            vehicle.setDrivable(false);
        }
    }
}
```

### 6.3 Verkehrsregeln

**Neue Features:**
- **Geschwindigkeitslimits**: In Plot-Zonen
- **Strafzettel**: Automatisch bei Überschreitung
- **Führerschein-System**: Muss erworben werden
- **Punkte-System**: Wie im echten Leben

---

## 7. Smartphone-Erweiterungen

### 7.1 Neue Apps

| App | Funktion |
|-----|----------|
| **Bank-App** | Kontostand, Überweisungen, Kreditanträge |
| **GPS-App** | Navigation zu Zielen, Plot-Suche |
| **Job-App** | Stellenangebote, Bewerbungen |
| **Social-App** | Spieler-Profile, Freundeslisten |
| **News-App** | Server-Events, Wirtschafts-News |
| **Musik-App** | Jukebox-Steuerung, Playlists |

### 7.2 Smartphone-Features

**Neue Features:**
- Klingelton-Anpassung
- Hintergrund-Designs
- App-Benachrichtigungen
- Foto-Funktion (Screenshots)

---

## 8. Server-Events

### 8.1 Wirtschafts-Events

| Event | Beschreibung | Auswirkung |
|-------|--------------|------------|
| **Börsen-Crash** | Aktien fallen | -30% Aktienwerte |
| **Wirtschafts-Boom** | Erhöhte Nachfrage | +20% Shop-Preise |
| **Inflation** | Geld wird weniger wert | Dynamische Preisanpassung |
| **Black Friday** | Rabatt-Aktion | -50% bei allen Shops |

### 8.2 Kriminalitäts-Events

| Event | Beschreibung | Auswirkung |
|-------|--------------|------------|
| **Polizei-Razzia** | Großrazzia in Gebieten | Erhöhte Polizeipräsenz |
| **Verbrechens-Welle** | NPCs werden überfallen | Mehr Sicherheits-Jobs |
| **Gefängnis-Ausbruch** | Gefangene fliehen | Kopfgeld-System aktiviert |

---

## 9. Performance-Optimierungen

### 9.1 Chunk-basiertes NPC-Loading

```java
public class NPCChunkManager {
    private Map<ChunkPos, List<NPCData>> chunkNPCs = new HashMap<>();

    public void onChunkLoad(ChunkPos pos) {
        List<NPCData> npcs = chunkNPCs.get(pos);
        if (npcs != null) {
            npcs.forEach(this::spawnNPC);
        }
    }

    public void onChunkUnload(ChunkPos pos) {
        List<NPCData> npcs = chunkNPCs.get(pos);
        if (npcs != null) {
            npcs.forEach(this::despawnNPC);
        }
    }
}
```

### 9.2 Network-Optimierung

- **Paket-Batching**: Mehrere Updates in einem Paket
- **Delta-Kompression**: Nur Änderungen senden
- **Client-Prediction**: Weniger Server-Roundtrips

### 9.3 Spatial Index Verbesserung

**Aktuell:** Einfacher Spatial Index
**Vorschlag:** R-Tree oder Quad-Tree für 10.000+ Plots

---

## 10. Admin-Tools

### 10.1 Web-Interface

**Features:**
- Server-Dashboard mit Statistiken
- Spieler-Verwaltung
- Wirtschafts-Übersicht
- Log-Viewer
- Konfiguration-Editor

### 10.2 Ingame-Admin-Tools

**Neue Commands:**
```
/admin economy reset <player>     - Wirtschaft zurücksetzen
/admin plot seize <id>            - Grundstück konfiszieren
/admin crime pardon <player>      - Strafregister löschen
/admin rollback <player> <time>   - Transaktionen rückgängig
/admin simulate event <type>      - Event simulieren
```

---

## 11. Mod-Kompatibilität

### 11.1 API für andere Mods

```java
// Öffentliche API
public interface ScheduleMCAPI {
    // Economy
    double getBalance(UUID player);
    boolean transfer(UUID from, UUID to, double amount);

    // Plots
    PlotRegion getPlotAt(BlockPos pos);
    boolean canBuild(UUID player, BlockPos pos);

    // NPCs
    List<CustomNPCEntity> getNPCsInRange(BlockPos pos, int radius);

    // Events
    void registerEconomyListener(EconomyEventListener listener);
    void registerPlotListener(PlotEventListener listener);
}
```

### 11.2 Event-System

```java
// Custom Forge Events
public class PlotPurchasedEvent extends Event {
    private final UUID buyer;
    private final PlotRegion plot;
    private final double price;
}

public class CrimeCommittedEvent extends Event {
    private final UUID criminal;
    private final CrimeType type;
    private final int wantedLevel;
}
```

---

## 12. Implementierungs-Prioritäten

### Phase 1: Kurzfristig (Basis-Verbesserungen)
1. Anti-Cheat System
2. Gefängnis-System
3. Neue Smartphone-Apps (Bank, GPS)
4. Fahrzeug-Schaden

### Phase 2: Mittelfristig (Feature-Erweiterungen)
1. Gang-System
2. Aktien-Börse
3. Erweiterte NPC-KI
4. Heist-System

### Phase 3: Langfristig (Infrastruktur)
1. Datenbank-Migration
2. Web-Interface
3. API für andere Mods
4. Multi-Server-Support

---

## 13. Technische Schulden

| Bereich | Problem | Lösung |
|---------|---------|--------|
| Persistenz | JSON-Dateien | SQLite/MySQL |
| I/O | Synchron | CompletableFuture |
| Events | Direkte Aufrufe | Forge Event Bus |
| Tests | Kein Load-Testing | JMH Benchmarks |
| Docs | Unvollständig | JavaDoc + Wiki |

---

## Fazit

ScheduleMC ist eine beeindruckend umfangreiche Mod mit solidem Fundament. Die wichtigsten Verbesserungsbereiche sind:

1. **Infrastruktur**: Datenbank-Migration und asynchrone Operationen
2. **Gameplay**: Gefängnis, Gangs, Heists für mehr Tiefe
3. **Wirtschaft**: Aktienmarkt und Hypotheken für Langzeit-Engagement
4. **NPCs**: Intelligentere KI und Quest-System
5. **Performance**: Optimierungen für große Server

Die vorgeschlagenen Erweiterungen würden das Roleplay-Erlebnis erheblich vertiefen und die Mod für größere Server-Communities skalierbar machen.
