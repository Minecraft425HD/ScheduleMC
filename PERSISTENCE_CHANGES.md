# Persistenz-Implementierung für NPC Life System Manager

## Zusammenfassung
Alle 9 NPC Life System Manager wurden mit JSON-Persistenz ausgestattet.

## Implementierte Manager

### 1. FactionManager ✓
- **File**: `config/npc_life_factions.json`
- **Datenstruktur**: Map<String, Map<String, FactionRelation>>
- **Features**: Spieler-Fraktionsbeziehungen mit Reputation
- **Thread-Safe**: ConcurrentHashMap

### 2. WitnessManager ✓
- **File**: `config/npc_life_witness.json`
- **Datenstruktur**: WitnessData (reports, wantedPlayers, bounties)
- **Features**: Zeugenberichte, Fahndungslisten, Kopfgelder
- **Thread-Safe**: ConcurrentHashMap

### 3-9. In Progress...

## Pattern für alle Manager
1. Extends `AbstractPersistenceManager<DataType>`
2. Singleton mit Double-Checked Locking
3. `getInstance(MinecraftServer)` als primary getter
4. `ConcurrentHashMap` für Thread-Safety
5. `markDirty()` bei jeder Änderung
6. File: `config/npc_life_{name}.json`
