# Auftrag an Claude — ScheduleMC Registrierung vereinheitlichen

**Repo:** `Minecraft425HD/ScheduleMC`  
**Branch-Ziel:** eigener PR, nicht direkt auf `main` squash-mischen ohne Diff-Check  
**Autor der Analyse:** Grok (Docs-Sync-Runden + Quellscan 2026-09-24)  
**Mod:** Minecraft Forge 1.20.1, Java 17, Forge 47.4.0  
**Aktuelle Version laut Build:** `3.9.0-beta` (`gradle.properties` / `mods.toml`)

Dieses Dokument ist eine **Umsetzungsanweisung**. Keine neue Feature-Arbeit. Keine Gameplay-Änderung. IDs, Blockklassen, Properties, BlockEntities und Items müssen nach dem Refactor **identisch** registriert sein.

Lies diese Datei vollständig bevor du Code änderst: `docs/CLAUDE_IMPLEMENTATION_BRIEF.md`

---

## 1. Auftrag (dieser PR)

Vereinheitliche die **Block-Registrierung** aller Produktions-/Welt-Module auf **einen** Helper.

Danach:

1. Quellscan muss **148 Block-IDs** liefern (Liste unten).
2. Jede ID hat genau einen `Block`-Eintrag.
3. Jede ID, die bisher ein `BlockItem` hatte, hat weiterhin ein `BlockItem` mit derselben Registry-ID.
4. Blöcke ohne Item bleiben ohne Item.
5. `ScheduleMC.java` Bus-Attach unverändert im Verhalten: jedes Modul `BLOCKS.register(modEventBus)` + `ITEMS.register(modEventBus)` soweit vorhanden.
6. Docs-Kennzahlen: nicht mehr „97 Blöcke“ und nicht mehr „399 Items aus `.ITEMS.register(modEventBus)`“.

**Nicht in diesem PR:** neue Blöcke, Renames, Creative-Tabs umbauen, BlockEntity-Logik, Balancing, Wiki-Prosa in `Items.md` Tabellenzeilen umschreiben (nur Kennzahlen + Registry-Appendix).

---

## 2. Warum das nötig ist

Forge kennt nur ein Verfahren: `DeferredRegister<Block>` + `register("id", factory)`.

Im Repo existieren **vier Schreibweisen**. Die sind funktional gleich, aber:

- naive Greps (`BLOCKS.register("id")`) fanden nur **97** IDs
- Helper-Scan (`registerBlockWithItem("id")` / lokales `register("id")` / `BLOCK_REGISTER.register("id")`) findet **148** echte Blöcke
- `ITEMS.register(modEventBus)` und `BLOCKS.register(modEventBus)` wurden fälschlich als Item-/Block-Zählung verwendet (**399 / 125**). Das sind Event-Bus-Attaches, keine IDs.

Genau diese Uneinheitlichkeit hat die Dokumentation mehrfach falsch gemacht. Ein Stil = eine Zählung = eine Doku.

---

## 3. Ist-Zustand — die vier Stile

### Stil A — doppelt ausgeschrieben

Beispiele: `ChocolateBlocks`, `BeerBlocks`, `WineBlocks`, `HoneyBlocks`, `CheeseBlocks`, `CannabisBlocks` (Maschinen), `SecretDoors`, `EconomyBlocks`, `WarehouseBlocks`, `PlotBlocks`, Teile von `CocaBlocks` (Pflanzen).

```java
public static final RegistryObject<Block> ROASTING_STATION =
    BLOCKS.register("roasting_station", () -> new RoastingStationBlock(...));
public static final RegistryObject<Item> ROASTING_STATION_ITEM =
    ITEMS.register("roasting_station",
        () -> new BlockItem(ROASTING_STATION.get(), new Item.Properties()));
```

### Stil B — Modul-lokaler Helper `registerBlockWithItem`

Beispiele: `TobaccoBlocks` (Töpfe, Racks, Fässer, Tische, Grow-Lights), `CocaBlocks` (Vats/Refinery/Crack), `MethBlocks`, `LSDBlocks`, `MDMABlocks`, `CoffeeBlocks` (Röster/Mühle/Packaging), `PoppyBlocks` (Maschinen).

```java
private static <T extends Block> RegistryObject<T> registerBlockWithItem(String name, Supplier<T> block) {
    RegistryObject<T> registeredBlock = BLOCKS.register(name, block);
    ITEMS.register(name, () -> new BlockItem(registeredBlock.get(), new Item.Properties()));
    return registeredBlock;
}
```

### Stil C — lokales `register(...)` (Fans)

`FanBlocks.java`: `register("fan_tier1", ...)`. Intern ebenfalls Block + BlockItem.

### Stil D — anderer Register-Name + konkreter Typ

`vehicle/blocks/ModBlocks.java`:

```java
BLOCK_REGISTER.register("fuel_station", () -> new BlockFuelStation());
BLOCK_REGISTER.register("fuel_station_top", () -> new BlockFuelStationTop());
BLOCK_REGISTER.register("workshop", () -> new BlockWorkshop());
```

`fuel_station_top` ist typischerweise **kein** eigenständiges Inventar-Item (Multiblock-Oberteil). Nicht blind ein BlockItem dranhängen.

---

## 4. Soll-Zustand

Neue gemeinsame Utility:

`src/main/java/de/rolandsw/schedulemc/registry/ModBlockReg.java`

```java
public final class ModBlockReg {
    private ModBlockReg() {}

    public static <T extends Block> RegistryObject<T> blockAndItem(
            DeferredRegister<Block> blocks,
            DeferredRegister<Item> items,
            String id,
            Supplier<T> factory) {
        RegistryObject<T> block = blocks.register(id, factory);
        items.register(id, () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }

    public static <T extends Block> RegistryObject<T> blockOnly(
            DeferredRegister<Block> blocks,
            String id,
            Supplier<T> factory) {
        return blocks.register(id, factory);
    }
}
```

Wenn ein Modul bereits `Item.Properties()` mit Extra-Flags setzt, diese Properties 1:1 übernehmen — Helper muss `Item.Properties` optional akzeptieren.

Keine Creative-Tab-Änderung in diesem PR, außer der alte Code hat explizit eine Tab-Zuweisung.

Modul-lokale `registerBlockWithItem` / `register` löschen, nachdem alle Aufrufe umgestellt sind.

`DeferredRegister`-Felder dürfen `BLOCKS` / `ITEMS` heißen bleiben. Vehicle darf intern `BLOCK_REGISTER` behalten, soll aber denselben Helper nutzen.

---

## 5. Harte Ausnahmen — KEIN BlockItem hinzufügen

| ID | Grund |
|---|---|
| `door_filler` | Technikblock der geheimen Tür — Ist-Zustand prüfen |
| `fuel_station_top` | Multiblock-Oberteil |
| jede ID ohne bestehendes `ITEMS.register` / Helper-Item | Ist-Zustand ist Gesetz |

Pflanzen (`*_plant`, `grapevine`): Ist prüfen. Manche haben nur Block (Seed ist separates Item in `*Items.java`). Dann `blockOnly`.

---

## 6. Vollständige Block-Soll-Liste (148)

### Tobacco — TobaccoBlocks.java (21)
`virginia_plant`, `burley_plant`, `oriental_plant`, `havana_plant`, `terracotta_pot`, `ceramic_pot`, `iron_pot`, `golden_pot`, `small_drying_rack`, `medium_drying_rack`, `big_drying_rack`, `small_fermentation_barrel`, `medium_fermentation_barrel`, `big_fermentation_barrel`, `sink`, `small_packaging_table`, `medium_packaging_table`, `large_packaging_table`, `basic_grow_light_slab`, `advanced_grow_light_slab`, `premium_grow_light_slab`

### Chocolate — ChocolateBlocks.java (15)
`roasting_station`, `winnowing_machine`, `grinding_mill`, `pressing_station`, `small_conching_machine`, `medium_conching_machine`, `large_conching_machine`, `tempering_station`, `small_molding_station`, `medium_molding_station`, `large_molding_station`, `enrobing_machine`, `cooling_tunnel`, `wrapping_station`, `chocolate_storage_cabinet`

### Honey — HoneyBlocks.java (14)
`beehive`, `advanced_beehive`, `apiary`, `honey_extractor`, `centrifugal_extractor`, `filtering_station`, `small_aging_chamber`, `medium_aging_chamber`, `large_aging_chamber`, `processing_station`, `creaming_station`, `bottling_station`, `honey_storage_barrel`, `honey_display_case`

### Beer — BeerBlocks.java (12)
`malting_station`, `mash_tun`, `small_brew_kettle`, `medium_brew_kettle`, `large_brew_kettle`, `small_beer_fermentation_tank`, `medium_beer_fermentation_tank`, `large_beer_fermentation_tank`, `small_conditioning_tank`, `medium_conditioning_tank`, `large_conditioning_tank`, `beer_bottling_station`

### Wine — WineBlocks.java (12)
`grapevine`, `crushing_station`, `small_wine_press`, `medium_wine_press`, `large_wine_press`, `small_fermentation_tank`, `medium_fermentation_tank`, `large_fermentation_tank`, `small_aging_barrel`, `medium_aging_barrel`, `large_aging_barrel`, `wine_bottling_station`

### Coffee — CoffeeBlocks.java (10)
`arabica_plant`, `robusta_plant`, `liberica_plant`, `excelsa_plant`, `wet_processing_station`, `small_coffee_roaster`, `medium_coffee_roaster`, `large_coffee_roaster`, `coffee_grinder`, `coffee_packaging_table`

### Cheese — CheeseBlocks.java (9)
`pasteurization_station`, `curdling_vat`, `small_cheese_press`, `medium_cheese_press`, `large_cheese_press`, `small_aging_cave`, `medium_aging_cave`, `large_aging_cave`, `packaging_station`

### Coca — CocaBlocks.java (9)
`bolivian_coca_plant`, `colombian_coca_plant`, `small_extraction_vat`, `medium_extraction_vat`, `big_extraction_vat`, `small_refinery`, `medium_refinery`, `big_refinery`, `crack_cooker`

### Cannabis — CannabisBlocks.java (8)
`cannabis_indica_plant`, `cannabis_sativa_plant`, `cannabis_hybrid_plant`, `cannabis_autoflower_plant`, `cannabis_trim_station`, `cannabis_curing_jar`, `cannabis_hash_press`, `cannabis_oil_extractor`

### Poppy — PoppyBlocks.java (7)
`afghan_poppy_plant`, `turkish_poppy_plant`, `indian_poppy_plant`, `scoring_machine`, `opium_press`, `cooking_station`, `heroin_refinery`

### Secret doors — SecretDoors.java (5 Blöcke)
`secret_door`, `hatch`, `hidden_switch_stone`, `door_filler`, `elevator`  
Nicht als Block: `secret_door_be`, `hidden_switch_be`, `door_filler_be`, `elevator_be`, `remote_control`.

### LSD — LSDBlocks.java (4)
`fermentation_tank`, `distillation_apparatus`, `micro_doser`, `perforation_press`

### Meth — MethBlocks.java (4)
`chemical_mixer`, `reduction_kettle`, `crystallizer`, `vacuum_dryer`

### Mushroom — MushroomBlocks.java (4)
`climate_lamp_small`, `climate_lamp_medium`, `climate_lamp_large`, `water_tank`

### Fan — FanBlocks.java (3)
`fan_tier1`, `fan_tier2`, `fan_tier3`

### MDMA — MDMABlocks.java (3)
`reaction_kettle`, `drying_oven`, `pill_press`

### Vehicle — ModBlocks.java (3)
`fuel_station`, `fuel_station_top`, `workshop`

### Economy — EconomyBlocks.java (2)
`cash_block`, `atm`  
Nicht als Block: `cash_block_entity`, `atm_block_entity`.

### Plot — PlotBlocks.java (2)
`plot_info_block`, `industrial_floor`

### Warehouse — WarehouseBlocks.java (1)
`warehouse`  
Nicht als Block: `warehouse_block_entity`.

**Summe: 21+15+14+12+12+10+9+9+8+7+5+4+4+4+3+3+3+2+2+1 = 148**

---

## 7. Dateien

**Neu:** `src/main/java/de/rolandsw/schedulemc/registry/ModBlockReg.java`

**Umbau nur Register-Aufrufe:** TobaccoBlocks, ChocolateBlocks, HoneyBlocks, BeerBlocks, WineBlocks, CoffeeBlocks, CheeseBlocks, CocaBlocks, CannabisBlocks, PoppyBlocks, SecretDoors, LSDBlocks, MethBlocks, MushroomBlocks, FanBlocks, MDMABlocks, vehicle/blocks/ModBlocks, EconomyBlocks, PlotBlocks, WarehouseBlocks.

**Nicht anfassen:** `*Block.java` Implementierungen; `ScheduleMC.java` außer fehlender Bus-Register (prüfen, nicht erfinden).

**Docs nur Zahlen:** wiki/Home.md, wiki/FAQ.md, docs/VERSION.md; Scan-Scripts auf 148 Blöcke.

---

## 8. Migrationsregeln

1. Registry-ID-String nicht ändern.
2. Block-Klasse nicht ändern.
3. BlockBehaviour.Properties 1:1.
4. Öffentliche RegistryObject-Feldnamen beibehalten.
5. Öffentliche `*_ITEM`-Felder nicht still löschen — Usages suchen.
6. Keine neuen IDs, keine IDs entfernen.
7. BlockEntity-Register unverändert.
8. Tests müssen kompilieren.
9. Kein README-Kürzen.

---

## 9. Abnahme

```
unique BLOCK ids = 148
ID-Menge = Abschnitt 6
gradle compileJava erfolgreich
```

---

## 10. Weitere Code-Erkenntnisse (nicht dieser PR, Docs nicht zurückdrehen)

- Version **3.9.0-beta**. CHANGELOG `## [3.8.0-beta]` ist Historie.
- **11** I*API, kein ITutorialAPI. Tutorial nicht implementiert.
- **8** PlotTypes inkl. INDUSTRIAL.
- **7** NPCType: CITIZEN, MERCHANT, POLICE, BANK, TOW_TRUCK_DRIVER, BANKER, DRUG_DEALER.
- WeaponItems **28**. Attachments: scope, silencer — kein Laser.
- ~35 Achievements, 4 genutzte Kategorien.
- Items: **353** `ITEMS.register("id")`. Nicht 399 (das war Bus-Attach).
- Commands: 25 Root, 132 Literals, 161 dokumentierte Headings; /health hat 41.
- Java 1610 Dateien, ~258568 LOC, 681 @Test / 41 Testdateien, 147 Screens, 62 Manager, 111 BlockEntity-IDs.
- Lizenz GNU GPLv3.
- Persistenz JSON + IncrementalSaveManager.
- `/state balance` = Staatskasse, nicht Spielerkonto.
- Items.md Tabellen enthalten Alias-IDs; Appendix ist die Register-Wahrheit.

## 11. Verboten

README/ARCHITECTURE kürzen. CHANGELOG 3.8 umschreiben. ITutorialAPI anlegen. 3.8.0-beta als aktuell setzen. Waffen/Rezepte anfassen.

## 12. Commits

1. feat: add ModBlockReg helper
2. refactor: route *Blocks registries through ModBlockReg
3. docs: block count 148 from unified registry scan

PR-Titel: `refactor: unify block registration via ModBlockReg`
