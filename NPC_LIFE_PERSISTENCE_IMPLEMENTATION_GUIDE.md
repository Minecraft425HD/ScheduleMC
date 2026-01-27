# NPC Life System - Persistenz-Implementierung

## Übersicht

Alle 9 NPC Life System Manager werden mit JSON-Persistenz via AbstractPersistenceManager ausgestattet.

## Status

### ✅ Bereits Implementiert

1. **FactionManager** - Fraktionsbeziehungen
2. **WitnessManager** - Zeugensystem

### 🔄 Zu Implementieren

3. **CompanionManager** - Begleiter-System
4. **QuestManager** - Quest-System
5. **DialogueManager** - Dialog-System
6. **NPCInteractionManager** - NPC-Interaktionen (nur transiente Daten)
7. **WorldEventManager** - Welt-Events
8. **DynamicPriceManager** - Dynamische Preise
9. **NPCRelationshipManager** - NPC-Beziehungen (bereits implementiert, nur registrieren)

---

## Implementierungs-Pattern

### Für Manager 3-8 (CompanionManager bis DynamicPriceManager):

```java
// 1. Imports hinzufügen
import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import de.rolandsw.schedulemc.util.GsonHelper;
import net.minecraft.server.MinecraftServer;
import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;

// 2. Class Deklaration ändern
public class XxxManager extends AbstractPersistenceManager<XxxManager.XxxData> {

    // 3. Singleton mit Double-Checked Locking
    private static volatile XxxManager instance;
    private static final Object INSTANCE_LOCK = new Object();

    @Nullable
    public static XxxManager getInstance() {
        return instance;
    }

    public static XxxManager getInstance(MinecraftServer server) {
        XxxManager result = instance;
        if (result == null) {
            synchronized (INSTANCE_LOCK) {
                result = instance;
                if (result == null) {
                    instance = result = new XxxManager(server);
                }
            }
        }
        return result;
    }

    // 4. ConcurrentHashMap für Thread-Safety
    private final Map<UUID, Data> data = new ConcurrentHashMap<>();

    // 5. Konstruktor
    private XxxManager(MinecraftServer server) {
        super(
            new File(server.getServerDirectory(), "config/npc_life_xxx.json"),
            GsonHelper.get()
        );
        load();
    }

    // 6. markDirty() bei jeder Daten-Änderung hinzufügen
    public void modifyData() {
        // ... data modification ...
        markDirty();
    }

    // 7. AbstractPersistenceManager Methoden implementieren
    @Override
    protected Type getDataType() {
        return new TypeToken<XxxData>(){}.getType();
    }

    @Override
    protected void onDataLoaded(XxxData data) {
        this.data.clear();
        if (data.xxx != null) {
            this.data.putAll(data.xxx);
        }
    }

    @Override
    protected XxxData getCurrentData() {
        XxxData data = new XxxData();
        data.xxx = new HashMap<>(this.data);
        return data;
    }

    @Override
    protected String getComponentName() {
        return "XxxManager";
    }

    @Override
    protected String getHealthDetails() {
        return String.format("%d entries", data.size());
    }

    @Override
    protected void onCriticalLoadFailure() {
        data.clear();
    }

    // 8. Data Class für JSON
    public static class XxxData {
        public Map<UUID, YourDataType> xxx;
        // weitere Felder...
    }
}
```

---

## Spezifische Implementierungen

### 3. CompanionManager
- **File**: `config/npc_life_companions.json`
- **Data Class**: `CompanionData` mit `allCompanions`, `playerCompanions`
- **NBT Methoden entfernen**: Zeile 384-432
- **markDirty() hinzufügen bei**: recruit(), releaseCompanion(), transferCompanion()

### 4. QuestManager
- **File**: `config/npc_life_quests.json`
- **Data Class**: `QuestData` mit `questIdCounter`, `playerProgress`
- **NBT Methoden entfernen**: Zeile 551-576
- **markDirty() hinzufügen bei**: acceptQuest(), completeQuest()

### 5. DialogueManager
- **File**: `config/npc_life_dialogues.json`
- **Data Class**: `DialogueData` mit `npcTrees` (NPC-spezifische Dialogbäume)
- **NBT Methoden entfernen**: Zeile 334-371
- **Hinweis**: `registeredTrees` und `activeDialogues` sind transient (nicht persistieren)
- **markDirty() hinzufügen bei**: assignTreeToNPC(), removeTreeFromNPC()

### 6. NPCInteractionManager
- **File**: `config/npc_life_interactions.json`
- **WICHTIG**: Dieser Manager hat nur transiente Daten!
- **Lösung**: Empty implementation mit leerem save/load
- **Data Class**: Leere Klasse oder null
- **Grund**: `activeInteractions` und `interactionCooldowns` sind Runtime-State

### 7. WorldEventManager
- **File**: `config/npc_life_events.json`
- **Data Class**: `WorldEventData` mit `activeEvents`, `eventHistory`, `lastCheckedDay`
- **NBT Methoden entfernen**: Zeile 406-448
- **markDirty() hinzufügen bei**: startEvent(), endEvent()

### 8. DynamicPriceManager
- **File**: `config/npc_life_prices.json`
- **Data Class**: `PriceData` mit `globalCondition`, `categoryConditions`, `temporaryModifiers`, `lastKnownDay`
- **NBT Methoden entfernen**: Zeile 318-369
- **markDirty() hinzufügen bei**: setGlobalMarketCondition(), setCategoryCondition(), addTemporaryModifier()
- **Hinweis**: `priceHistory` ist transient

### 9. NPCRelationshipManager
- **BEREITS IMPLEMENTIERT** - Eigene JSON-Persistenz
- **Nur**: In IncrementalSaveManager registrieren

---

## ScheduleMC.java Registrierung

In `onServerStarted()` nach Zeile 435 einfügen:

```java
// NPC Life System Manager - All 9 managers with persistence
LOGGER.info("Initializing NPC Life System Managers...");

// 1. FactionManager
de.rolandsw.schedulemc.npc.life.social.FactionManager.getInstance(event.getServer());

// 2. WitnessManager
de.rolandsw.schedulemc.npc.life.witness.WitnessManager.getInstance(event.getServer());

// 3. CompanionManager
de.rolandsw.schedulemc.npc.life.companion.CompanionManager.getInstance(event.getServer());

// 4. QuestManager
de.rolandsw.schedulemc.npc.life.quest.QuestManager.getInstance(event.getServer());

// 5. DialogueManager
de.rolandsw.schedulemc.npc.life.dialogue.DialogueManager.getInstance(event.getServer());

// 6. NPCInteractionManager
de.rolandsw.schedulemc.npc.life.social.NPCInteractionManager.getInstance(event.getServer());

// 7. WorldEventManager
de.rolandsw.schedulemc.npc.life.world.WorldEventManager.getInstance(event.getServer());

// 8. DynamicPriceManager
de.rolandsw.schedulemc.npc.life.economy.DynamicPriceManager.getInstance(event.getServer());

// 9. NPCRelationshipManager (already has persistence)
de.rolandsw.schedulemc.npc.personality.NPCRelationshipManager.getInstance().load();

LOGGER.info("NPC Life System Managers initialized");
```

In IncrementalSaveManager Registrierung (nach Zeile 490) einfügen:

```java
// NPC Life System Managers (Priority 5)
saveManager.register(de.rolandsw.schedulemc.npc.life.social.FactionManager.getInstance());
saveManager.register(de.rolandsw.schedulemc.npc.life.witness.WitnessManager.getInstance());
saveManager.register(de.rolandsw.schedulemc.npc.life.companion.CompanionManager.getInstance());
saveManager.register(de.rolandsw.schedulemc.npc.life.quest.QuestManager.getInstance());
saveManager.register(de.rolandsw.schedulemc.npc.life.dialogue.DialogueManager.getInstance());
saveManager.register(de.rolandsw.schedulemc.npc.life.social.NPCInteractionManager.getInstance());
saveManager.register(de.rolandsw.schedulemc.npc.life.world.WorldEventManager.getInstance());
saveManager.register(de.rolandsw.schedulemc.npc.life.economy.DynamicPriceManager.getInstance());
saveManager.register(de.rolandsw.schedulemc.npc.personality.NPCRelationshipManager.getInstance());

LOGGER.info("Registered {} NPC Life System managers", 9);
```

---

## Migration von ServerLevel zu MinecraftServer

### Problem
Alte Manager verwendeten: `getManager(ServerLevel level)`

### Lösung
Alle Aufrufe ersetzen durch: `getInstance(server)` oder `getInstance()`

### Suche & Ersetze
```bash
# FactionManager
FactionManager.getManager(level) → FactionManager.getInstance(server)

# WitnessManager
WitnessManager.getManager(level) → WitnessManager.getInstance(server)

# etc. für alle 9 Manager
```

---

## Checkliste

Für jeden Manager:
- [ ] Imports hinzufügen
- [ ] Extends AbstractPersistenceManager
- [ ] Singleton mit Double-Checked Locking
- [ ] ConcurrentHashMap für Thread-Safety
- [ ] Konstruktor mit File + GsonHelper
- [ ] markDirty() bei allen Änderungen
- [ ] 6 abstrakte Methoden implementieren
- [ ] Data Class für JSON erstellen
- [ ] Alte NBT save/load entfernen
- [ ] In ScheduleMC.java initialisieren
- [ ] In IncrementalSaveManager registrieren
- [ ] Alte getManager(level) Aufrufe ersetzen

---

## Testing

Nach der Implementierung:
1. Server starten
2. Logs prüfen: "NPC Life System Managers initialized"
3. JSON-Dateien prüfen in `config/npc_life_*.json`
4. Health-Check durchführen: `/health persistence`
5. Save-Test: Daten ändern, Server stoppen/neu starten
6. Verify persistence: Daten müssen nach Neustart vorhanden sein

---

## Performance

- **Thread-Safe**: ConcurrentHashMap verhindert Race Conditions
- **Incremental Saves**: Nur dirty Manager werden gespeichert
- **Backup System**: Automatische Backups bei jedem Save
- **Atomic Writes**: Temp-File + Atomic Move
- **Corruption Recovery**: Automatische Backup-Wiederherstellung

---

## Fehlerbehandlung

Bei kritischem Ladefehler:
1. onCriticalLoadFailure() cleartalle Daten
2. Korrupte Datei wird als `.CORRUPT_timestamp` gesichert
3. Manager startet mit leeren Daten
4. Backup wird automatisch wiederhergestellt falls vorhanden

