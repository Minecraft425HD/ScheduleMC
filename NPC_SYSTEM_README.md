# Custom NPC System - Dokumentation

## Übersicht

Das Custom NPC System ermöglicht es, eigene NPCs im Spiel zu erstellen, die:
- Eigene Player-Skins verwenden können
- Mit Spielern interagieren können (Dialog, Handel)
- Erweiterbar für zukünftige Features sind
- Nicht auf Villager basieren

## Features

### ✅ Implementiert

1. **Custom NPC Entity**
   - Eigene Entity-Klasse (kein Villager)
   - Player-ähnliches Model
   - Persistente Datenspeicherung (NBT)
   - Unsterblich und despawnt nie

2. **NPC Spawner Tool**
   - GUI zum Spawnen von NPCs
   - Auswahl des NPC-Namens
   - Auswahl des Player-Skins aus verfügbaren Skins
   - Einfaches Platzieren per Rechtsklick

3. **Player-Skin System**
   - Skins werden in `config/schedulemc/npc_skins/` gespeichert
   - PNG-Dateien im Player-Skin Format (64x64)
   - Dynamisches Laden der Skins
   - Default-Skin als Fallback

4. **Interaktionssystem**
   - GUI mit 3 Hauptoptionen:
     - Dialog: Zeigt Dialog-Texte an
     - Verkaufen: Shop-System (TODO)
     - Kaufen: Shop-System (TODO)

5. **Erweiterbare Datenstruktur**
   - `NPCData`: Hauptklasse für alle NPC-Eigenschaften
   - `DialogEntry`: Einzelne Dialog-Einträge
   - `ShopInventory`: Shop-Items mit Preisen
   - `NPCBehavior`: Verhaltenseinstellungen
   - `CustomData`: CompoundTag für beliebige Erweiterungen

6. **Network System**
   - Client-Server Kommunikation
   - Spawn-Packet
   - Action-Packet (Dialog, Shop)
   - Sync-Packet für Daten

## Dateien-Struktur

```
src/main/java/de/rolandsw/schedulemc/npc/
├── client/
│   ├── model/
│   │   └── CustomNPCModel.java         # Player-ähnliches 3D-Model
│   ├── renderer/
│   │   └── CustomNPCRenderer.java      # Renderer mit Skin-Support
│   ├── screen/
│   │   ├── NPCSpawnerScreen.java       # GUI für Spawner-Tool
│   │   └── NPCInteractionScreen.java   # GUI für NPC-Interaktion
│   └── NPCClientEvents.java            # Client-Side Event Handler
├── data/
│   └── NPCData.java                    # Datenklasse für alle NPC-Eigenschaften
├── entity/
│   ├── CustomNPCEntity.java            # Haupt-Entity-Klasse
│   └── NPCEntities.java                # Entity-Registry
├── items/
│   ├── NPCSpawnerTool.java             # Tool zum Spawnen
│   └── NPCItems.java                   # Item-Registry
├── menu/
│   ├── NPCMenuTypes.java               # Menu-Registry
│   ├── NPCSpawnerMenu.java             # Container für Spawner-GUI
│   └── NPCInteractionMenu.java         # Container für Interaktions-GUI
└── network/
    ├── NPCNetworkHandler.java          # Haupt-Network Handler
    ├── SpawnNPCPacket.java             # Packet zum Spawnen
    ├── NPCActionPacket.java            # Packet für Aktionen
    └── SyncNPCDataPacket.java          # Packet zum Synchronisieren

config/schedulemc/npc_skins/
└── default.png                         # Default Player-Skin

src/main/resources/assets/schedulemc/
├── textures/
│   ├── entity/npc/
│   │   └── default.png                 # Default NPC Texture
│   ├── gui/
│   │   ├── npc_spawner.png             # Spawner GUI Background
│   │   └── npc_interaction.png         # Interaktions GUI Background
│   └── item/
│       └── npc_spawner_tool.png        # Tool Texture
├── models/item/
│   └── npc_spawner_tool.json           # Tool Model
└── lang/
    ├── de_de.json                      # Deutsche Übersetzungen
    └── en_us.json                      # Englische Übersetzungen
```

## Verwendung

### NPC Spawnen

1. **NPC Spawner Tool erhalten**
   - Im Creative-Modus: ScheduleMC Tab → "NPC Spawner"
   - Per Command: `/give @p schedulemc:npc_spawner_tool`

2. **NPC spawnen**
   - Mit Tool rechtsklicken (auf Block oder in die Luft)
   - GUI öffnet sich:
     - Name eingeben
     - Skin auswählen (< > Buttons)
     - "NPC Spawnen" klicken

3. **Mit NPC interagieren**
   - Rechtsklick auf NPC
   - GUI zeigt Optionen:
     - Dialog: Zeigt Dialog-Text und wechselt beim Klicken
     - Verkaufen: Öffnet Verkaufs-Shop (TODO)
     - Kaufen: Öffnet Kauf-Shop (TODO)

### Custom Skins hinzufügen

1. Player-Skin als PNG (64x64) erstellen
2. Datei in `config/schedulemc/npc_skins/` speichern
3. Skin wird automatisch im Spawner-GUI angezeigt

**Hinweis**: Minecraft Player-Skin Format verwenden!

## Erweiterung

### Neue Features hinzufügen

Die `NPCData`-Klasse ist für Erweiterungen konzipiert:

```java
// In NPCData.java:
private CompoundTag customData;  // Für beliebige Daten

// Beispiel: Quest-System
CompoundTag questData = npc.getNpcData().getCustomData();
questData.putString("currentQuest", "fetch_item");
questData.putInt("questProgress", 5);
```

### Dialog-System erweitern

```java
// Standard-Dialoge beim Spawnen (in SpawnNPCPacket.java)
data.addDialogEntry(new NPCData.DialogEntry("Hallo! Ich bin " + npcName + ".", ""));
data.addDialogEntry(new NPCData.DialogEntry("Wie kann ich dir helfen?", ""));
data.addDialogEntry(new NPCData.DialogEntry("Komm bald wieder!", ""));

// Mit Spieler-Antworten:
data.addDialogEntry(new NPCData.DialogEntry(
    "Was möchtest du tun?",
    "Ich brauche deine Hilfe!"
));
```

### Shop-System implementieren

```java
// In NPCData.java bereits vorbereitet:
ShopInventory buyShop = npc.getNpcData().getBuyShop();
buyShop.addEntry(new ItemStack(Items.DIAMOND), 100);  // Verkauft Diamanten für 100$

ShopInventory sellShop = npc.getNpcData().getSellShop();
sellShop.addEntry(new ItemStack(Items.EMERALD), 50);  // Kauft Smaragde für 50$
```

### Bewegungslogik hinzufügen

```java
// In CustomNPCEntity.java - registerGoals():
if (npcData.getBehavior().canMove()) {
    this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.3D));
    this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
}

// Aktivieren:
npc.getNpcData().getBehavior().setCanMove(true);
npc.getNpcData().getBehavior().setMovementSpeed(0.3f);
```

## Technische Details

### Entity Attributes

- **Health**: 20.0 (wie Spieler)
- **Movement Speed**: 0.3
- **Follow Range**: 32 Blöcke
- **Invulnerable**: true (unsterblich)
- **Persistent**: true (despawnt nie)

### AI Goals

1. **FloatGoal**: Schwimmen
2. **LookAtPlayerGoal**: Schaut Spieler an (8 Blöcke Reichweite)
3. **RandomLookAroundGoal**: Schaut zufällig umher

### NBT Struktur

```json
{
  "NPCData": {
    "NPCName": "string",
    "SkinFileName": "string",
    "NPCUUID": "uuid",
    "CurrentDialogIndex": int,
    "DialogEntries": [
      {
        "Text": "string",
        "Response": "string"
      }
    ],
    "BuyShop": {
      "Entries": [
        {
          "Item": CompoundTag,
          "Price": int
        }
      ]
    },
    "SellShop": { ... },
    "Behavior": {
      "CanMove": boolean,
      "LookAtPlayer": boolean,
      "MovementSpeed": float
    },
    "CustomData": CompoundTag
  }
}
```

## TODOs / Zukünftige Features

- [ ] Shop-System vollständig implementieren
- [ ] Eigene Bewegungslogik (nach Wunsch)
- [ ] Quest-System
- [ ] NPC-Bearbeitungs-GUI
- [ ] NPC-zu-NPC Interaktion
- [ ] Animationen
- [ ] Sound-System
- [ ] Rüstungs-/Item-Equipment
- [ ] NPC-Spawner-Ei (Spawn Egg)

## Fehlerbehebung

### NPCs spawnen nicht
- Prüfe Server-Log auf Fehler
- Stelle sicher, dass alle Registries korrekt sind
- Prüfe ob Network Handler registriert ist

### Skins werden nicht geladen
- Prüfe `config/schedulemc/npc_skins/` Ordner
- PNG-Dateien müssen 64x64 sein
- Player-Skin Format verwenden
- Fallback: `default.png` wird verwendet

### GUI öffnet nicht
- Prüfe Client-Side Registrierung
- Prüfe Network Packets
- Prüfe Server-Log

## Integration mit bestehendem System

Das NPC System ist vollständig integriert:
- ✅ In `ScheduleMC.java` registriert
- ✅ In Creative Tab hinzugefügt
- ✅ Client-Side Events registriert
- ✅ Network Handler initialisiert
- ✅ Entity Attributes registriert

## Entwickler-Notizen

Das System ist bewusst modular und erweiterbar gestaltet:

1. **Daten**: `NPCData` mit `CustomData` CompoundTag
2. **Network**: Eigener Network Handler mit erweiterbaren Packets
3. **GUI**: Separate Screens für verschiedene Funktionen
4. **Entity**: Basis-Implementierung, einfach erweiterbar

Neue Features können einfach hinzugefügt werden, ohne bestehenden Code zu brechen.
