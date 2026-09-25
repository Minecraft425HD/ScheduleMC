# ScheduleMC — Hinweise für Claude

Diese Datei enthält Entscheidungen und Ausschlüsse, die bei zukünftigen Analysen
oder Refactoring-Scans NICHT erneut vorgeschlagen werden sollen.

---

## Bewusst übersprungene Refactorings

### Beer/Wine size-variant Konsolidierung (Supplier-Pattern)
**Status:** ABGELEHNT — nicht erneut vorschlagen

**Betrifft:**
- `beer/blockentity/Abstract*BlockEntity` + Small/Medium/Large-Subklassen
  (BrewKettle, BeerFermentationTank, ConditioningTank)
- `wine/blockentity/Abstract*BlockEntity` + Small/Medium/Large-Subklassen
  (WinePress, AgingBarrel, FermentationTank)

**Begründung:**
Das Supplier-Pattern wurde in Sprint 6 erfolgreich auf die Tobacco-Klassen
angewendet (DryingRack, FermentationBarrel), weil deren Subklassen **ausschließlich**
2 Config-Getter (@Override) enthielten und vollständig eliminierbar waren.

Die Beer/Wine-Subklassen überschreiben dagegen **4 Methoden**:
- `getCapacity()` / `getSpeedMultiplier()` — Werte
- `getDisplayName()` — klassenindividueller Übersetzungsschlüssel
- `createMenu()` — klassenindividueller Menü-Typ

Da `getDisplayName()` und `createMenu()` pro Größe unterschiedlich sind, blieben
die Subklassen auch nach dem Refactoring bestehen (nur kürzer). Der Basiskonstruktor
würde durch ein `String`-Argument und eine `TriFunction`-Fabrik aufgebläht. Der
Aufwand überwiegt den Nutzen bei weitem. Die ~43-zeiligen Subklassen sind klar und
wartbar — so sollen sie bleiben.

---

### NPC God-Class Aufteilung (NPCData + CustomNPCEntity)
**Status:** ABGESCHLOSSEN — nicht erneut vorschlagen

**Betrifft:**
- `npc/data/NPCData.java` (971 → 409 Zeilen)
- `npc/entity/CustomNPCEntity.java` (968 Zeilen)

**Umgesetzte Aufteilung von NPCData:**
- `NPCLocationData` — home, work, leisure (bis 10), assignedWarehouse
- `NPCShopData` — buyShop, sellShop
- `NPCScheduleData` — workStartTime, workEndTime, homeTime + isWithinWorkingHours()
- `NPCPoliceData` — policeStation, patrolPoints (bis 16), currentPatrolIndex, Timing
- `ShopInventory` / `ShopEntry` — als Top-Level-Klassen ausgelagert

**Abgeschlossen in:** Sprint auf Branch `claude/deepscan-repository-3Vkpv` (2026-03-24)
Alle 29 Call-Site-Dateien migriert. NBT-Keys identisch — Spielstände bleiben kompatibel.

---

## Dimension-System

**ScheduleMC nutzt genau eine Dimension (Overworld).**

- Es gibt keinen Nether- oder End-Support.
- Die Dimension-Switch-Logik in `PlotAppScreen.renderCurrentPlotTab()` (Overworld/Nether/End-Labels)
  ist toter Code — Nether/End werden nie aktiv sein.
- **Konsequenz:** Nicht vorschlagen, Multi-Dimension-Support zu ergänzen oder den Switch-Block
  zu einem echten Feature auszubauen. Der Code kann bei Bedarf auf den Overworld-Fall vereinfacht werden.
- **Festgehalten am:** 2026-04-16.

---

## Cannabis-Blöcke — Interaktionsmodell (ab 2026-04-17)

**Status:** IMPLEMENTIERT — Architekturentscheidung festhalten

**Betrifft:**
- `cannabis/blocks/TrimStationBlock` + `TrimStationBlockEntity`
- `cannabis/blocks/HashPressBlock`
- `cannabis/blocks/OilExtractorBlock`
- `cannabis/blocks/CuringJarBlock`

**Entscheidungen:**
- **Alle 4 Blöcke** haben jetzt visuelle Input/Output-Slots im GUI (render-only, kein ItemStackHandler).
- Items werden weiterhin direkt in den BlockEntity-Feldern gespeichert (kein Refactoring auf ItemStackHandler).
- `CuringJarBlock`: Interaktion unverändert (RK fill, Shift+RK extract).
- `HashPressBlock` und `OilExtractorBlock`: Extraktion auf **Shift+RK** umgestellt (war: plain RK).
- `TrimStationBlock`: komplett auf Maschinen-Slot-Basis umgestellt — RK mit DriedBud = füllen, Shift+RK = entnehmen; Output wird in BE-Feldern gepuffert statt direkt ins Spieler-Inventar gegeben.
- **Nicht vorschlagen:** TrimStation auf ItemStackHandler umzustellen — bewusst direktes Feld-Storage beibehalten.

---

## Dokumentationsstatus

- Zuletzt gegen den aktuellen Repository-Stand abgeglichen am **2026-04-17**.
- Diese Datei wurde im Rahmen der Cannabis-GUI-Überarbeitung erweitert.
- Referenz für Live-Metriken: `docs/REPO_METRICS.md` (neu generiert).


---

## Sprachkonvention (Architekturentscheidung, 2026-06-10)

**Alles im Code ist Englisch.** Verbindlich für:
- Registry-Keys (Blöcke, Items, BlockEntities, Menüs, Entities)
- Java-Identifier: Klassen, Methoden, Felder, Enum-Konstanten
- Übersetzungsschlüssel (lang keys) — Werte in `de_de.json` bleiben deutsch
- NBT-/Config-Keys, Asset-Dateinamen (Texturen, Models, Blockstates)

**Keine Rückwärtskompatibilität nötig** — die Mod ist in Entwicklung.
Kein MissingMappings-Remap, keine Legacy-Aliase (NPCType-Duplikate wurden
zusammengeführt: BEWOHNER→CITIZEN, VERKAEUFER→MERCHANT, POLIZEI→POLICE,
ABSCHLEPPER→TOW_TRUCK_DRIVER).

**Erledigt (2026-06-10):** Alle hartkodierten deutschen User-facing-
Strings wurden entfernt (Phasen 1-8, siehe docs/I18N_MIGRATION_PLAN.md).
Guard gegen Rückfälle: `scripts/check-german-strings.sh`.
Deutsche Code-Kommentare sind weiterhin erlaubt.

---

## Public API entfernt (2026-09-24)

**Status:** ABGESCHLOSSEN — nicht erneut vorschlagen

**Betrifft:**
- `de.rolandsw.schedulemc.api` (komplettes Paket, 25 Dateien, 5.689 Zeilen):
  `ScheduleMCAPI`-Singleton, 11 `I*API`-Interfaces (Economy, Plot, Production,
  NPC, Police, Warehouse, Messaging, Smartphone, Vehicle, Achievement, Market),
  alle 11 `*APIImpl`-Klassen, sowie die separate Legacy-Fassade `PlotModAPI`.

**Begründung:** Explizite Entscheidung des Repo-Owners. Vor der Löschung
verifiziert: außer dem Initialisierungsaufruf in `ScheduleMC.onServerStarted()`
hatte kein einziger interner Codepfad diese Schicht benutzt — alle Systeme
sprechen direkt mit den Manager-Klassen (`EconomyManager`, `PlotManager`, …).
`PlotModAPI` war in `docs/API_REFERENCE.md` als öffentliche Legacy-API für
Drittanbieter-Mods dokumentiert, aber ebenfalls ohne internen Aufrufer.

**Konsequenz:** Es gibt aktuell keine unterstützte Integrations-API für externe
Mods. `docs/API_REFERENCE.md` wurde gelöscht, alle API-Abschnitte in README,
ARCHITECTURE, DEVELOPER_GUIDE, TESTING, FAQ und den Feature-Wiki-Seiten wurden
entfernt oder auf „keine API vorhanden“ korrigiert. **Nicht vorschlagen**,
die API wiederherzustellen oder ein neues API-Paket zu entwerfen, ohne dass
das explizit gewünscht wird.

---

## Mixin-Framework entfernt (2026-09-24)

**Status:** ABGESCHLOSSEN — nicht erneut vorschlagen

**Betrifft:**
- Alle 10 Mixin-/Connector-Klassen (`vehicle/mixins/GuiMixin`,
  `vehicle/mixins/SoundOptionsScreenMixin`, `vehicle/MixinConnector`, sowie
  8 MapView-Mixins unter `mapview/integration/*/mixins/`), `schedulemc.mixins.json`,
  die `org.spongepowered:mixin`- und `mixinextras-common`-Abhängigkeiten in
  `build.gradle`.

**Begründung:** Alle 8 MapView-Mixins hatten ihre `@Mixin`-Annotation
auskommentiert (Rest eines gescheiterten 1.20.1-Ports) — sie taten buchstäblich
nichts. Die 2 Vehicle-Mixins hatten zwar aktive Annotationen, aber es gab
nirgends einen Lademechanismus: kein `[[mixins]]`-Eintrag in `mods.toml`, kein
`MixinConfigs`-Manifest-Attribut, kein `IMixinConnector`-Service-Eintrag, kein
`-mixin.config`-JVM-Flag. `MixinConnector` selbst verwies zudem auf eine nicht
existierende `vehicle.mixins.json` und wurde nirgends aufgerufen.

**Konsequenz:** Die Tank-/Tempoanzeige beim Fahren (vorher per Mixin über die
XP-Leiste) wurde mixin-frei über `RenderGuiOverlayEvent` in `RenderEvents`
nachgebaut — Funktion bleibt erhalten. Der Vehicle-Lautstärkeregler
(`SoundOptionsScreenMixin`) wurde ersatzlos gestrichen, da derselbe Wert
bereits über `ClientConfigScreen` einstellbar ist. MapView-Rendering läuft
vollständig über reguläre Forge-Client-Events (`RenderGuiOverlayEvent`,
`ClientTickEvent`), die es ohnehin schon parallel gab. **Nicht vorschlagen**,
Mixins für neue Features einzuführen, ohne einen echten Lademechanismus
(`[[mixins]]` in `mods.toml`) gleich mit einzurichten.

---

## JEI/Jade/The One Probe entfernt (2026-09-24)

**Status:** ABGESCHLOSSEN — nicht erneut vorschlagen

**Betrifft:** Die drei `compileOnly`-Abhängigkeiten in `build.gradle`.

**Begründung:** Null Zeilen Integrationscode für irgendeines der drei Mods
existierten je im Quellcode. README und FAQ versprachen automatische
Integration, die nie implementiert wurde.

---

## Dead-Code-Sweep (2026-09-24)

**Status:** ABGESCHLOSSEN, dokumentiert in `docs/CODE_VS_DOCS_ABGLEICH_2026-09-24.md`

Mehrstufiger automatisierter Scan (private/protected Member ohne Aufrufer,
ganze verwaiste Klassen, ungenutzte Imports) über den gesamten Hauptcode,
jeder Treffer manuell gegen Fehlalarme (Vanilla-/Forge-Overrides,
`@Mod.EventBusSubscriber`-Klassen) geprüft. Insgesamt >7.000 Zeilen toter Code
entfernt, u. a. `commands/PlotCommand.java` (392 Zeilen — 16 private
Handler-Methoden für Plot-Subcommands, die laut `wiki/Commands.md` schon
länger als „Former Command — moved to Settings App UI" markiert waren, aber
nie aus dem Code entfernt wurden), `gui/PlotMenuGUI.java` (214 Zeilen, alte
Chest-GUI), `messaging/NPCMessageTemplates.java`, `npc/pathfinding/NPCNodeEvaluator.java`,
`mapview/util/LayoutVariables.java`, sowie 381 ungenutzte Imports über 217
Dateien. **Nicht erneut vorschlagen**, denselben Scan zu wiederholen, ohne
neuen Code-Zuwachs seit diesem Datum — er wurde bis zur Konvergenz
durchlaufen (0 verbleibende Funde außer verifizierten Fehlalarmen).

---

## Backlog: Fehlende Features für 12 verbleibende Dead-Config-Werte (2026-09-25)

**Status:** GEPLANT, NICHT UMGESETZT — bewusst zurückgestellt, kommt später

Im Rahmen des Config-Wiring-Sweeps (siehe `docs/CHANGELOG.md`, Commit
"fix: wire ~40 of 59 dead config settings into real game logic") wurden 59
Config-Werte gefunden, die nirgends von echter Spiellogik gelesen wurden.
47 davon wurden korrekt verdrahtet oder als verwaist entfernt. Für die
verbleibenden 12 existiert **kein passendes Feature im Code** — sie
brauchen neue Funktionalität, keine reine Verdrahtung. Plan pro Wert
(Reihenfolge = empfohlene Umsetzungsreihenfolge):

1. **`police.max_roadblocks`/`roadblock_duration_seconds` — Trigger fehlt**
   (Quick Win, Infrastruktur existiert bereits vollständig in
   `PoliceRoadblock.java`, nur `createRoadblock()` hat aktuell 0 Aufrufer):
   In `PoliceAIHandler` bei `wantedLevel >= 4` und `POLICE_ROADBLOCK_ENABLED`
   eine Position vor dem fliehenden Spieler berechnen und
   `PoliceRoadblock.createRoadblock(...)` aufrufen.

2. **`police.wanted_posters_min_level`**: Neues `WantedPosterItem` +
   wandmontierter `WantedPosterBlock` (Name + Bounty aus
   `WantedListSyncPacket`-Daten), ausgegeben/platziert ab konfiguriertem
   Wanted-Level.

3. **`police.speed_limit_default`**: Geschwindigkeitskontrolle — Fahrzeug-
   Geschwindigkeit gegen das Limit prüfen, bei Überschreitung + Polizei-NPC
   in Sichtweite eine Verkehrsstrafe wie in `TrafficViolationHandler`
   auslösen.

4. **`police.flanking_enabled`**: Bei ≥2 verfolgenden Polizei-NPCs in
   `PoliceAIHandler` einen Offset-Punkt relativ zur Fluchtrichtung
   anvisieren statt exakt dieselbe Zielposition wie der erste Verfolger.

5. **`police.siren_sound_radius`**: Periodischer Sirenensound während
   `PoliceVehiclePursuit`-Verfolgung. **Braucht eine Entscheidung:** eigenes
   Sound-Asset registrieren oder bewusst einen Vanilla-Sound (z. B.
   `RAID_HORN`) zweckentfremden — keine Audiodatei im Repo vorhanden.

6. **`level_system.max_level`/`base_xp`/`xp_exponent`**: `LevelRequirements`s
   `static final` XP-Tabelle (berechnet beim Klassenladen, vor
   garantiertem Config-Load) auf eine bei Serverstart + Config-Reload neu
   berechnete Tabelle umstellen (`static` statt `static final`,
   `rebuildTable()`-Methode). Risiko: Off-by-one beim Array-Resize wäre
   fatal fürs gesamte Levelsystem — sorgfältig gegen Level-Grenzwerte und
   bestehende Spielstände mit Level > neuem Max testen.

7. **`shop.buy_multiplier`/`sell_multiplier`**: **Braucht zuerst eine
   Design-Entscheidung:** Soll `sell_multiplier` ein neues "Spieler
   verkauft Item direkt an NPC"-Feature auslösen, oder den bestehenden
   Warehouse-Sell-Preis skalieren? Außerdem: `buy_multiplier`s Default
   (`1.5`) müsste vor dem Wiring auf `1.0` gesenkt werden, sonst steigen
   alle Shop-Preise beim Wiring schlagartig um 50 % — das ist eine
   Balance-Entscheidung, kein Bugfix.

8. **UDPS `sd_decay_rate`/`daily_food_cost`/`daily_reference_income`**:
   **`sd_decay_rate` ist erledigt** (siehe "DynamicPriceManager/
   DynamicMarketManager Merge" unten) — `daily_food_cost`/
   `daily_reference_income` bleiben offen (keine NPC-Lebenshaltungskosten-
   Simulation im Code vorhanden, die diese Werte lesen könnte).

**Nicht vorschlagen**, diese Werte durch reine Constant-Swaps zu
"fixen" — dafür fehlt echtes Feature-Code, kein Wiring-Fehler.

---

## DynamicPriceManager/DynamicMarketManager Merge (2026-09-25)

**Status:** ABGESCHLOSSEN — nicht erneut vorschlagen

**Betrifft:**
- `npc/life/economy/DynamicPriceManager.java` (UDPS-Kategorie-Preisstaffel,
  `MarketCondition`-Zustandsmaschine)
- `market/DynamicMarketManager.java` (gelöscht) — hatte ein vollständiges,
  aber nie befülltes Item-Level Supply&Demand-Modell (`MarketData`).

**Begründung:** Es gab vier parallele, sich überschneidende Preissysteme:
`DynamicPriceManager` (einziges mit echter Wirkung, über
`NPCLifeSystemIntegration`/`PriceModifier`), `DynamicMarketManager`
(fertiges S&D-Modell, aber 100 % inert — `registerItem`/`setEnabled`/
`.tick()`/`load()` wurden nie von außen aufgerufen), `EconomyController
.marketDataMap` (dritte, separat String-keyed `MarketData`-Map, ebenfalls
100 % inert, da `registerMarketData()` nirgends aufgerufen wird) und
`EconomyController.getDynamicShopPrice()` (hartkodiert `sdMult=1.0` für
NPC-Shop-Käufe).

**Umsetzung:** `DynamicMarketManager`s Item-Level-S&D-Tracking
(`registerItem`, `onItemSoldToNPC`/`onItemBoughtFromNPC`,
`getCurrentPrice`, `getTopPricedItems`, `getTrendingUp/DownItems`,
`getStatistics`, `getPlayerMarketReport`, Decay) wurde vollständig in
`DynamicPriceManager` übernommen (neue Map `itemMarketData`, Methoden
siehe Abschnitt "ITEM MARKET"). Nutzt jetzt die bereits verdrahteten
Config-Werte `DYNAMIC_PRICING_SD_FACTOR`/`MIN_MULTIPLIER`/
`MAX_MULTIPLIER`/`ENABLED` statt eigener hartkodierter Defaults, und gibt
`DYNAMIC_PRICING_SD_DECAY_RATE` erstmals eine echte Wirkung (Supply/
Demand-Decay im gleichen Intervall wie `updateMarketConditions()`).
Persistenz läuft jetzt über die bestehende `DynamicPriceManagerData`/
`npc_life_prices.json` (kein separates `plotmod_market.json` mehr nötig).

`PurchaseItemPacket` (NPC-Shop-Kauf) registriert das gekaufte Item beim
ersten Kauf automatisch, multipliziert den UDPS-Preis mit
`getItemPriceMultiplier()` und meldet den Kauf per
`onItemBoughtFromNPC()` — das S&D-Modell wird dadurch zum ersten Mal
tatsächlich befüllt und wirkt auf zukünftige Preise. `MarketCommand`
(`/market`) und `HealthCheckManager` wurden auf `DynamicPriceManager`
umgestellt. `EconomyController.marketDataMap`/`getDynamicShopPrice()`
wurden bewusst **nicht** angefasst — der Nutzer hat explizit nur die
beiden genannten Klassen zum Zusammenführen benannt; `EconomyController`
ist ein größeres, für Produktionsgüter bereits aktiv genutztes System
(`getSellPrice`/`getBuyPrice`) und bleibt ein separates, offenes Thema.

`getItemPriceMultiplier()` bezieht zusätzlich `SeasonalPriceModifier` mit
ein (Nutzer-Nachtrag: "Vergiss nicht den saisonalen Preis mit
reinzunehmen!") — `registerItem(Item, double, ItemCategory)` ordnet das
Item über `mapToSeasonalCategory()` einer der bestehenden
`SeasonalPriceModifier`-Kategorien zu (PLANT/MUSHROOM/CHEMICAL/FOOD/
WEAPONS/LUXURY/BUILDING; Kategorien ohne sinnvollen saisonalen Bezug wie
Maschinen/Werkzeuge/Sonstiges bleiben unzugeordnet = neutraler Faktor
1.0). `PurchaseItemPacket` übergibt dafür die ohnehin schon berechnete
`shopCategory` (aus `ItemCategory.fromMerchantCategory()`). Ergebnis wird
nach dem saisonalen Multiplizieren erneut auf `min_multiplier`/
`max_multiplier` geclamped, damit Season × S&D nicht über die
konfigurierten Grenzen hinausschießt.

**Konsequenz:** `market/DynamicMarketManager.java` wurde gelöscht.
**Nicht vorschlagen**, ein neues separates Item-S&D-System zu bauen oder
`DynamicMarketManager` wiederherzustellen.

---

## Vollständige S&D-Konsolidierung + toter Verkaufsabschluss-Pfad (2026-09-25, Teil 2)

**Status:** ABGESCHLOSSEN — nicht erneut vorschlagen

**Auslöser:** Nutzer-Anfrage: "Alle Preise müssen von einem System überwacht
werden ... es muss aber alles verifiziert und belegt sein, dass das gesamte
dynamische Preis/kosten system auch wirklich nahtlos funktioniert!" — als
Fortsetzung des DynamicPriceManager/DynamicMarketManager-Merges (siehe oben),
da dort noch eine **dritte** S&D-Instanz übrig war: `EconomyController
.marketDataMap` (String-keyed, getrennt von der Item-keyed `itemMarketData`
in `DynamicPriceManager`).

### Teil A: `EconomyController.marketDataMap` entfernt, auf DynamicPriceManager umgeleitet

`marketDataMap`, `registerMarketData(String, MarketData)`, `getMarketData
(String)` gelöscht (verifiziert: `registerMarketData` hatte im gesamten
Repo null Aufrufer — die Map war zu 100 % tot, jeder S&D-Multiplikator für
Produktions-Verkaufspreise war immer `1.0`). `DynamicPriceManager` bekam
eine zweite, String-keyed Zustandsklasse `ProductMarketState` (supply/demand,
gleiche Ratio^Faktor-Formel wie `MarketData`) für genau diesen Zweck —
`getProductPriceMultiplier(String)`/`onProductSold(String,int)`/
`onProductBought(String,int)`, Decay im selben Tick-Intervall wie das
Item-Market. `EconomyController.getSupplyDemandMultiplier()`/
`updateSupplyOnSale()` delegieren jetzt komplett dorthin. **Konsequenz:**
`DynamicPriceManager` ist jetzt der EINZIGE Ort im gesamten Mod, der S&D-
Zustand hält — sowohl für Item-keyed NPC-Shop-Ware als auch für String-keyed
Produktions-Güter (Cannabis-Sorten, Tabak-Typen, Koka-Sorten, etc.).

### Teil B: Verifizierter, gravierenderer Fund — Verkaufsabschluss rief nie mit echter UUID auf

Beim Verifizieren, ob das S&D-System nach der Umleitung überhaupt jemals
befüllt wird, wurde per Grep über **alle** Aufrufer von `EconomyController
.getSellPrice()`/`ProductionType.calculateDynamicPrice()` im gesamten Repo
(Honey/Beer/Chocolate/Wine/Coffee/Tobacco/Cannabis/Coca-Items, `ProductionType`
selbst) festgestellt: **jeder einzelne Aufrufer übergibt `playerUUID = null`.**
Diese Methoden werden ausschließlich aus `appendHoverText()` (Tooltip-Vorschau)
aufgerufen, nie beim tatsächlichen Verkaufsabschluss. Der reale Geldfluss für
Drogenverkäufe läuft komplett getrennt über `tobacco/network/NegotiationPacket
.java` (NPC-Verhandlung: `WalletManager.addMoney`/`npc.getNpcData()
.removeMoney()` direkt, ohne je `EconomyController` mit echter UUID
aufzurufen). Konsequenz vor dem Fix: **`ProducerLevel.awardSaleXP()`
(SELL_LEGAL/SELL_ILLEGAL) wurde im gesamten Spiel nie ausgelöst**,
`GlobalEconomyTracker.onSale()` (Inflation/Geldmengen-Tracking) ebenfalls
nie, und selbst nach Teil A wäre `ProductMarketState` für Produktions-Güter
nie befüllt worden — trotz korrekter Verdrahtung wäre das System weiterhin
funktional tot geblieben.

**Fix:** `EconomyController.getSellPrice()`s duplizierter Tracking-Block
(Economy-Tracking + S&D-Update + XP-Vergabe) wurde in eine neue, wieder-
verwendbare Methode `recordCompletedSale(productId, amount, quality,
revenue, playerUUID)` extrahiert (reduziert zugleich Code-Duplikation
zwischen den beiden `getSellPrice()`-Overloads). `NegotiationPacket` ruft
diese Methode direkt nach dem erfolgreichen Geldtransfer auf — der Preis
wird NICHT neu berechnet (der bereits verhandelte/bezahlte Preis bleibt
unverändert), nur Tracking/XP/S&D werden nachträglich gemeldet.
Produkt-Identifikation läuft über `PackagedDrugItem.parseVariant()` +
`ProductionType.getProductId()`/`getItemCategory()` (bereits vorhandene,
korrekt funktionierende Infrastruktur — nur nie mit dem realen
Verkaufsabschluss verknüpft).

**Update (2026-09-25, Teil 3): Meth-Lücke geschlossen.** Nutzer-Korrektur:
Meth hat sehr wohl 3 unterschiedlich zu bepreisende Varianten — nur eben
nicht als anbaubare Sorte (keine "Variant"-NBT), sondern als
Reinheitsstufe. Neue Klasse `meth/MethVariant.java` (`implements
ProductionType`, 3 Konstanten STANDARD/GOOD/BLUE_SKY, je eigener
Basispreis 30/50/80€, `getProductId()` = "METH_STANDARD"/"METH_GOOD"/
"METH_BLUE_SKY") mit `fromQuality(MethQuality)`-Mapping (POOR+GOOD→
STANDARD, VERY_GOOD→GOOD, LEGENDARY→BLUE_SKY). `PackagedDrugItem` bekam
eine neue `resolveVariant(DrugType, ItemStack)`-Methode, die für METH über
die Qualität auflöst statt über die (bei Meth leere) Varianten-NBT, und für
alle anderen Drogen wie bisher `parseVariant()` nutzt — verwendet jetzt von
sowohl `PackagedDrugItem.calculatePrice()` (Tooltip) als auch
`NegotiationPacket` (echter Verkaufsabschluss, siehe oben). Der alte,
deutsche Produkt-Key `METH_GUT` wurde durchgängig zu `METH_GOOD`
umbenannt (Sprachkonvention; betrifft `EconomyController
.initializeReferencePrices()`, `ModConfigHandler.PRODUCT_PRICES`-Default
und `EconomyPricesConfigScreen`). **Nicht vorschlagen**, diese Änderung
rückgängig zu machen oder Meth wieder auf einen einzigen Fallback-Preis
zu reduzieren.

**Nicht vorschlagen**, `AntiExploitManager.checkAndGetMultiplier()`
nachträglich in `recordCompletedSale()` einzubauen — dessen Vertrag ist,
den Preis VOR der Auszahlung zu reduzieren; bei einem bereits ausgehandelten
und bezahlten Verkauf (wie in `NegotiationPacket`) kann das nicht mehr
rückwirkend greifen, ohne die Auszahlungslogik neu zu designen.

---

## Preisglättung: 1x/Minecraft-Tag, 7-Tage-Gleitdurchschnitt (2026-09-25, Teil 3)

**Status:** ABGESCHLOSSEN — nicht erneut vorschlagen

**Auslöser:** Nutzer-Anfrage: "der preis soll nur 1 mal pro minecraft Tag
neu berechnet werden anhand der Lagerbestände ... es soll sich aber im
Rahmen bewegen ... eher der Durchschnitt von 7 minecraft Tagen angepasst
werden."

**Problem vorher:** `getItemPriceMultiplier()`/`getProductPriceMultiplier()`
berechneten den S&D-Multiplikator bei JEDER Preisabfrage live aus dem
aktuellen Supply/Demand-Stand — ein einzelner großer Kauf konnte den Preis
sofort und beliebig weit (bis zum konfigurierten Min/Max) springen lassen.

**Lösung — "intelligente" Glättung statt reiner Drosselung:**
Rohberechnung (S&D-Ratio^Faktor × Saison, wie bisher) bleibt unverändert
und läuft weiterhin laufend (Supply/Demand ändern sich sofort bei jedem
Kauf/Verkauf, `updateItemMarketData()`/`updateProductMarketData()` decayen
weiterhin im `DYNAMIC_PRICING_UPDATE_INTERVAL_MINUTES`-Takt). NEU: der von
`getItemPriceMultiplier()`/`getProductPriceMultiplier()` tatsächlich
zurückgegebene (= für Preise genutzte) Wert ist ab jetzt ein **gleitender
Durchschnitt der letzten 7 Minecraft-Tage** dieses Rohwerts
(`PRICE_SMOOTHING_WINDOW_DAYS = 7`), der nur 1x pro Minecraft-Tag (bei
Tageswechsel, `onDayChange()` → `updatePriceSmoothingSnapshots()`) neu
berechnet wird. Ein einzelner Tag mit extremem Angebots-/Nachfrage-
Ausschlag verschiebt den genutzten Preis dadurch nur um ca. 1/7 — spürbar,
aber gedämpft; hält sich über mehrere Tage sustained Druck (z. B. andauernd
niedriges Angebot) trotzdem klar durch, weil der Durchschnitt dann
kontinuierlich in diese Richtung wandert.

Implementiert über zwei kleine Speicherklassen: `DynamicPriceManager
.PriceSmoothingState` (Item-Ebene, eigene `Map<Item, PriceSmoothingState>
itemPriceSmoothing`) und `ProductMarketState` bekam die Felder
`multiplierHistory`/`effectiveMultiplier` direkt dazu (da dort ohnehin
schon ein persistiertes Objekt pro Produkt existiert). Gemeinsame Logik in
der statischen Hilfsmethode `recordAndAverage(List<Double> history,
double rawValue)` (FIFO-Deque-Verhalten über eine simple `List`, Cap bei 7
Einträgen, gibt den neuen Mittelwert zurück). Bei der allerersten Abfrage
eines frisch registrierten Items/Produkts (vor dem ersten Tageswechsel)
wird einmalig sofort mit dem aktuellen Rohwert geseedet, damit neue Ware
nicht bis zum nächsten Tag künstlich neutral (1.0) bepreist bleibt.
Beide History-Listen werden vollständig persistiert (`SerializedItemSmoothing`
in `DynamicPriceManagerData.itemPriceSmoothing`), überleben also
Serverneustarts.

**Bewusst unverändert gelassen:** `/market prices|trends|stats|top`
(`getAllItemMarketData()`/`getCurrentItemPrice()`/`getPriceTrend()` etc.)
zeigt weiterhin die ROHEN, live aktualisierten Supply/Demand-Werte — das
ist die Admin-/Diagnose-Ansicht des tatsächlichen Marktzustands, nicht der
geglättete Endkundenpreis. Nur der tatsächlich beim Kauf/Verkauf
verrechnete Preis (`getItemPriceMultiplier()`/`getProductPriceMultiplier()`,
genutzt von `PurchaseItemPacket`/`EconomyController.getSellPrice()`/
`getBuyPrice()`) läuft durch die Glättung. **Nicht vorschlagen**, die
Glättung auch auf die `/market`-Diagnoseausgabe anzuwenden — das würde die
Fehlersuche/Balance-Beobachtung erschweren.

**Nicht vorschlagen**, das 7-Tage-Fenster oder den Tages-Takt als neuen
Config-Wert aufzubohren, ohne dass das explizit gewünscht wird — der
Nutzer hat "7 Minecraft Tage" und "1x pro Tag" konkret benannt.
