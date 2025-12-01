# 🏪 Warehouse & Shop-Investment System - Implementierungs-Status

## ✅ VOLLSTÄNDIG IMPLEMENTIERT (Vorschlag 2: Aktien-System)

### 🎯 Kern-Systeme

#### 1. PlotType System
- ✅ `PlotType.java` - Enum mit SHOP, RESIDENTIAL, COMMERCIAL, etc.
- ✅ `PlotRegion.java` erweitert mit `type` und `warehouseLocation`
- ✅ Shop-Plots können nicht gekauft/vermietet werden

#### 2. Warehouse System
- ✅ `WarehouseConfig.java` - Config-basierte Einstellungen
- ✅ `WarehouseSlot.java` - Item-Slots mit Kapazität
- ✅ `WarehouseBlockEntity.java` - Block Entity mit:
  - Config-basierte Slot-Anzahl (default 32)
  - Config-basierte Kapazität (default 1024 pro Slot)
  - Automatische Lieferung alle 3 Tage
  - Staatskasse zahlt Lieferkosten
  - Integration mit Shop-Konto für Expense-Tracking
- ✅ `DeliveryPriceConfig.java` - Lieferpreise pro Item

#### 3. Staatskassen-System
- ✅ `StateAccount.java` - Verwaltet Staatsgelder
  - Zahlt Warehouse-Lieferungen
  - Admin-verwaltbar
  - Persistente Speicherung

#### 4. Shop-Account mit 7-Tage-Tracking
- ✅ `DailyRevenueRecord.java` - Tägliche Umsatz-Records
- ✅ `ShareHolder.java` - Aktionäre mit Shares
- ✅ `ShopAccount.java` - Vollständiges Aktien-System:
  - 100 Aktien total
  - Max 2 Aktionäre
  - 7-Tage-Nettoumsatz-Tracking
  - Automatische Gewinnausschüttung (alle 7 Tage)
  - Proportionale Verteilung (Shares / 100 × Nettoumsatz)
- ✅ `ShopAccountManager.java` - Zentraler Manager mit Tick-System

#### 5. NPC Integration
- ✅ `NPCData.java` erweitert mit:
  - `assignedWarehouse` Feld
  - Warehouse-Integration Methoden
  - Shop-Verkauf aus Warehouse
  - Automatische Shop-Konto Einnahmen-Registrierung

#### 6. Dynamisches Preissystem
- ✅ `PriceManager.java` - Preis-Multiplikatoren
  - Zeitbasierte Wellen (±15%)
  - Event-System Support
  - Multiplikator auf Shop-GUI-Preise
- ✅ `EconomicEvent.java` - Wirtschafts-Events

---

## ⚠️ NOCH ZU IMPLEMENTIEREN

### 📋 Priorität: Hoch

1. **Block & BlockEntity Registration**
   ```java
   // Muss erstellt werden:
   - WarehouseBlock.java (Block-Klasse)
   - ModBlockEntities.java (BlockEntity Registry)
   - ModBlocks.java (Block Registry)
   ```

2. **Commands**
   ```bash
   # Plot Commands
   /plot create <id> <price> <type>
   /plot settype <id> <type>
   /plot warehouse set <plotId>

   # NPC Commands
   /npc <name> warehouse set
   /npc <name> warehouse clear

   # Warehouse Commands
   /warehouse add <amount>
   /warehouse info
   /warehouse setshop <shopId>

   # Shop Investment Commands
   /shop list
   /shop info <shopId>
   /shop buy <shopId> <shares>
   /shop sell <shopId> <shares>
   /shop myshares

   # State Account Commands
   /state balance
   /state deposit <amount>
   /state withdraw <amount>
   ```

3. **GUI/Screen**
   ```java
   - WarehouseScreen.java (Admin-GUI zum Befüllen)
   - Shop-GUI Integration (Preis-Multiplikator anzeigen)
   ```

4. **NBT Serialization für NPCData**
   ```java
   // In NPCData.save() und load():
   - assignedWarehouse speichern/laden
   ```

### 📋 Priorität: Mittel

5. **Event System Implementation**
   ```java
   - Event-Pool definieren
   - Daily check implementieren
   - Events broadcasten
   ```

6. **Config Registration**
   ```java
   - WarehouseConfig mit ForgeConfigSpec registrieren
   ```

7. **Economy Manager Integration**
   ```java
   // In ShopAccount.performPayout():
   - Geld an Spieler geben (aktuell nur Nachricht)
   ```

### 📋 Priorität: Niedrig

8. **GUI Verbesserungen**
   - Warehouse Inventory GUI
   - Shop-History Anzeige
   - Aktien-Übersicht GUI

9. **Networking**
   - Packets für Client-Server Sync
   - Shop-Account Updates

---

## 🏗️ ARCHITEKTUR-ÜBERSICHT

```
┌─────────────────────────────────────────────────────────┐
│  PlotRegion (erweitert)                                 │
│  - PlotType type                                        │
│  - BlockPos warehouseLocation                           │
├─────────────────────────────────────────────────────────┤
│  WarehouseBlockEntity                                   │
│  - WarehouseSlot[] slots (32 default)                   │
│  - String shopId                                        │
│  - Auto-Lieferung (Staatskasse zahlt)                  │
├─────────────────────────────────────────────────────────┤
│  ShopAccount                                            │
│  - 7-Tage-Tracking (DailyRevenueRecord)                │
│  - 100 Aktien, max 2 Aktionäre                         │
│  - Gewinnausschüttung (alle 7 Tage)                    │
├─────────────────────────────────────────────────────────┤
│  NPCData (erweitert)                                    │
│  - BlockPos assignedWarehouse                           │
│  - Verkauft aus Warehouse                              │
│  - Registriert Erlöse in ShopAccount                   │
├─────────────────────────────────────────────────────────┤
│  PriceManager                                           │
│  - Multiplikatoren (±15% Wellen)                       │
│  - Event-System                                         │
│  - Finale Preis = Shop-GUI × Multiplikator             │
└─────────────────────────────────────────────────────────┘
```

---

## 📊 FEATURE-STATUS

| Feature | Status | Datei |
|---------|--------|-------|
| PlotType Enum | ✅ Fertig | `PlotType.java` |
| PlotRegion Erweiterung | ✅ Fertig | `PlotRegion.java` |
| Warehouse Config | ✅ Fertig | `WarehouseConfig.java` |
| Warehouse Slot | ✅ Fertig | `WarehouseSlot.java` |
| Warehouse BlockEntity | ✅ Fertig | `WarehouseBlockEntity.java` |
| Staatskasse | ✅ Fertig | `StateAccount.java` |
| Daily Revenue Record | ✅ Fertig | `DailyRevenueRecord.java` |
| ShareHolder | ✅ Fertig | `ShareHolder.java` |
| ShopAccount | ✅ Fertig | `ShopAccount.java` |
| ShopAccountManager | ✅ Fertig | `ShopAccountManager.java` |
| NPC Integration | ✅ Fertig | `NPCData.java` |
| PriceManager | ✅ Fertig | `PriceManager.java` |
| Economic Events | ✅ Fertig | `EconomicEvent.java` |
| Warehouse Block | ❌ TODO | - |
| Block Registration | ❌ TODO | - |
| Commands | ❌ TODO | - |
| GUI | ❌ TODO | - |
| NBT Serialization | ⚠️ Teilweise | `NPCData.java` |

---

## 🚀 NÄCHSTE SCHRITTE

1. **Block Registration**
   - `WarehouseBlock` erstellen
   - In `ModBlocks` registrieren
   - `ModBlockEntities` registrieren

2. **Commands implementieren**
   - Plot-Commands
   - NPC-Commands
   - Warehouse-Commands
   - Shop-Investment-Commands

3. **NBT Serialization vervollständigen**
   - `assignedWarehouse` in `NPCData.save()/load()`

4. **Testen**
   - Warehouse platzieren
   - Shop erstellen
   - NPC verknüpfen
   - Investment testen

---

## 💾 DATEIEN

### Neu erstellt:
```
src/main/java/de/rolandsw/schedulemc/
├── region/
│   └── PlotType.java ✅
├── warehouse/
│   ├── WarehouseConfig.java ✅
│   ├── WarehouseSlot.java ✅
│   ├── WarehouseBlockEntity.java ✅
│   └── DeliveryPriceConfig.java ✅
└── economy/
    ├── StateAccount.java ✅
    ├── DailyRevenueRecord.java ✅
    ├── ShareHolder.java ✅
    ├── ShopAccount.java ✅
    ├── ShopAccountManager.java ✅
    ├── PriceManager.java ✅
    └── EconomicEvent.java ✅
```

### Erweitert:
```
src/main/java/de/rolandsw/schedulemc/
├── region/
│   └── PlotRegion.java ✅ (type + warehouseLocation)
└── npc/data/
    └── NPCData.java ✅ (assignedWarehouse + Methoden)
```

---

## 🎯 IMPLEMENTIERTER WORKFLOW (Vorschlag 2)

```bash
# 1. Shop-Plot erstellen (Admin)
/plot create shop_bakery 0 SHOP

# 2. Warehouse platzieren (Block)
# Admin platziert Block im Shop

# 3. Warehouse konfigurieren
/warehouse setshop bakery_account

# 4. NPC erstellen (NPCSpawnerTool)
# Rechtsklick auf Boden → GUI → "Hans"

# 5. NPC mit Warehouse verknüpfen
/npc Hans warehouse set

# 6. Shop konfigurieren
# Shift+Rechtsklick auf Hans → Shop-Editor
# Weizen: 100€, unlimited=false

# 7. Warehouse befüllen (Admin)
/warehouse add 1024

# === SPIELER INVESTIERT ===

# 8. Spieler kauft Aktien
/shop info bakery_account
/shop buy bakery_account 25
→ Kauft 25 Aktien (25%)

# 9. Spieler kauft Items
# Rechtsklick auf Hans
# Weizen: 85€ (100€ × 0.85 Multiplikator)

# === AUTOMATIK ===

# 10. Alle 3 Tage: Lieferung
# Staatskasse zahlt, Shop-Konto registriert Ausgaben

# 11. Alle 7 Tage: Gewinnausschüttung
# 7-Tage-Nettoumsatz berechnen
# Aktionäre erhalten Anteil (Shares / 100 × Nettoumsatz)
```

---

## ⚙️ CONFIG (Planned)

```toml
[warehouse]
    slotCount = 32
    maxCapacityPerSlot = 1024
    deliveryIntervalDays = 3

[shop_shares]
    totalShares = 100
    maxShareholders = 2
    payoutIntervalDays = 7
    sellbackPercentage = 0.75
    minSharePurchase = 5
    maxSharePurchase = 75

[state_account]
    startingBalance = 100000

[delivery_prices]
    # siehe DeliveryPriceConfig.java
```

---

**Status: Kern-System vollständig implementiert! ✅**
**Nächste Schritte: Block-Registration, Commands, Testing**
