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

---

## Vier verwaiste Systeme eingebaut, zwei Dopplungen gelöscht (2026-09-25, Teil 4)

**Status:** ABGESCHLOSSEN — nicht erneut vorschlagen

**Auslöser:** Fortsetzung der Dopplungssuche. Vier weitere, komplett fertig
gebaute, aber nie verdrahtete Systeme gefunden (0 externe Aufrufer,
verifiziert per Grep) sowie eine dritte Dopplung des NPC-Preismodifikators.
Nutzer-Entscheidung: alle einbauen statt löschen (außer der offensichtlich
obsoleten Dopplung).

### 1. `WarehouseMarketBridge` → in DynamicPriceManager eingebaut, 1x/Tag + 7-Tage-Ø

`economy/WarehouseMarketBridge.java` (+ `WarehouseStockLevel.java`) berechnet
aus echten Warehouse-Füllständen einen Preis-Multiplikator
(`getWarehousePriceMultiplier(itemDescriptionId)`). War zu 100 % tot
(`updateWarehouseData()` nie getickt, `setServer()` nie aufgerufen).

**Einbau (bewusst NICHT in Echtzeit, wie vom Nutzer verlangt):**
- `ScheduleMC.onServerStarted()`: `WarehouseMarketBridge.getInstance().setServer(server)`
  direkt neben `DynamicPriceManager.initialize(server)`.
- `DynamicPriceManager.onDayChange()`: ruft `WarehouseMarketBridge.updateWarehouseData()`
  **einmal pro Minecraft-Tag** auf (VOR `updatePriceSmoothingSnapshots()`), NICHT
  mehr "alle 5 Minuten" wie ursprünglich in der (nie erreichten) Doku vorgesehen.
- `DynamicPriceManager.computeRawItemMultiplier(Item, MarketData)`: multipliziert
  den Warehouse-Preis-Multiplikator (`item.getDescriptionId()`-Key, identisch zum
  Schema in `WarehouseMarketBridge`) mit ein. Da diese Methode selbst nur beim
  täglichen Snapshot bzw. beim Erst-Seeden eines Items aufgerufen wird (siehe
  Preisglättung-Abschnitt oben), erbt das Warehouse-Signal automatisch dieselbe
  1x/Tag-+-7-Tage-Ø-Dämpfung wie alle anderen S&D-Faktoren — **keine
  separate Glättungslogik nötig.**

### 2. `PriceModifier` vollständig eingebaut, `TradingComponent` gelöscht (dritte Dopplung!)

Bei der Umsetzung stellte sich heraus: es gab nicht zwei, sondern **drei**
Implementierungen desselben NPC-Preismodifikators:
1. `CustomNPCEntity.getPersonalPriceModifier()` — die tatsächlich von allen 3
   realen Aufrufern (`PurchaseItemPacket`, `NegotiationPacket`,
   `OpenMerchantShopPacket`) genutzte Methode (nur Gier + Emotion).
2. `npc/entity/component/TradingComponent.java` — tickte alle 20 Ticks für
   JEDEN NPC eine fast identische Gier+Emotion-Berechnung, aber **niemand las
   das Ergebnis** (`getPersonalPriceModifier()`/`isWillingToTrade()`/
   `setTradeCooldown()` der Komponente hatten 0 externe Aufrufer) — reine
   Verschwendung von Tick-Zeit, nicht nur toter Code.
3. `npc/life/economy/PriceModifier.java` — die vollständigste Version
   (Gier + Emotion + Fraktions-Reputation + Spieler-Beziehung/Memory-Tags +
   Marktbedingung), 0 Aufrufer.

**Fix:** `CustomNPCEntity.getPersonalPriceModifier()` bekam eine neue
Signatur `(ServerPlayer player, ServerLevel level, boolean isBuying)` und
delegiert jetzt vollständig an `PriceModifier.calculateModifier(this, player,
level, isBuying)`. Alle 3 realen Aufrufer wurden angepasst (`isBuying=true`
für Käufe des Spielers, `isBuying=false` für `NegotiationPacket`, wo der
Spieler an den NPC verkauft) — `player.serverLevel()` statt manuellem
`player.level() instanceof ServerLevel`-Check. `TradingComponent.java` und
seine Registrierung in `CustomNPCEntity.initializeComponents()` wurden
komplett gelöscht (spart NBT-Persistenz eines nutzlosen `tradeCooldown`-Feldes
und den 20-Tick-Berechnungsaufwand pro NPC). **Nicht vorschlagen**, eine der
beiden gelöschten/ersetzten Implementierungen wiederherzustellen.

### 3. `NegotiationSystem` gelöscht (echte Dopplung, kein Einbau sinnvoll)

`npc/life/economy/NegotiationSystem.java` — ein generisches, rundenbasiertes
Rabatt-Verhandlungssystem, 0 Aufrufer. Wurde ersetzt durch das
tabak-/drogenspezifische `tobacco/business/NegotiationEngine.java`
(tatsächlich von `NegotiationPacket` genutzt, inkl. Stimmungs-Tracking,
Cooldowns, NPC-Ablehnung). Da beide dieselbe Aufgabe für denselben
Anwendungsfall (Preisverhandlung mit einem NPC) lösen und die Engine
bereits vollständig produktiv ist, wurde `NegotiationSystem` ersatzlos
gelöscht statt eingebaut. **Nicht vorschlagen**, es wiederherzustellen.

### 4. `BatchTransactionManager` in `InterestManager` eingebaut

`economy/BatchTransactionManager.java` — fertige Fluent-API zum Sammeln
mehrerer Economy-Transaktionen und Ausführen in einem Durchlauf, 0 Aufrufer.
**Wichtige Einschränkung, die beim Einbau berücksichtigt wurde:**
`EconomyManager.markDirty()` setzt nur ein `volatile boolean` — bei N
Aufrufen entsteht dadurch praktisch kein Mehraufwand; die im Klassenkommentar
behauptete "66-90% Performance-Gewinn"-Begründung trifft auf die aktuelle
`EconomyManager`-Implementierung NICHT zu. Der reale Wert des Einbaus liegt
also nicht in der (nicht vorhandenen) Performance-Ersparnis, sondern in der
saubereren Bulk-API mit Statistik-Rückgabe (`BatchResult`). Eingebaut in
`InterestManager.checkWeeklyPayouts()` (wöchentliche Zinsauszahlung an ALLE
Konten — der klassische Bulk-Anwendungsfall): sammelt alle fälligen
Zinszahlungen in einem `BatchTransactionManager`, führt sie in einem
Durchlauf aus, benachrichtigt danach die betroffenen Online-Spieler. Die alte
`payoutInterest(UUID, double)`-Methode wurde durch `calculateInterest(double)`
(reine Berechnung) + `notifyInterestRecipients(Map)` (Benachrichtigung nach
Batch-Ausführung) ersetzt. **Nicht vorschlagen**, denselben Einbau an
weiteren Stellen zu wiederholen, ohne dass dort echte Massentransaktionen
(viele Accounts in einer Schleife) vorliegen — für Einzeltransaktionen bringt
`BatchTransactionManager` keinen Vorteil.

### 5. `CrimeEventHandler`: `registerVandalism`/`registerTrespassing` eingebaut, Rest dokumentiert

Von den 8 toten `register*`-Hilfsmethoden in `npc/life/witness/
CrimeEventHandler.java` wurden `registerVandalism`/`registerTrespassing`
mit einem verifizierten, bereits existierenden Hook verdrahtet:
`events/BlockProtectionHandler.java`s `onBlockBreak`/`onBlockPlace` kennen
bereits `checkPlotPermission()` (true/false, ob der Spieler im fremden Plot
agieren darf) — bei Ablehnung wird das Event zwar schon gecancelt, aber
bisher NIE als Verbrechen gemeldet. Jetzt: abgelehntes Abbauen →
`registerVandalism(serverPlayer, pos)`, abgelehntes Platzieren →
`registerTrespassing(serverPlayer, pos)`.

**Bewusst NICHT geraten (verifiziert, kein sicherer Hook gefunden):**
- `registerDrugUse`/`CrimeType.DRUG_USE` — kein "Spieler konsumiert Droge"-
  Event/Hook im Code gefunden.
- `registerFraud`/`CrimeType.FRAUD` — keine Betrugs-/Täuschungs-Mechanik im
  Code gefunden, die das auslösen könnte.
- `registerTheft`/`registerRobbery`/`registerDrugDealing`/
  `registerEvadingPolice` — diese Verbrechen werden bereits real erkannt,
  aber über **direkte** `witnessManager.registerCrime(...)`-Aufrufe in
  `StealingAttemptPacket`, `PoliceRaidPenalty`, `PoliceAIHandler`,
  `HackingToolItem` (nicht über die `CrimeEventHandler`-Fassade). Ein
  Umbau auf die Fassade wurde NICHT vorgenommen, da die Wrapper-Methoden
  ihren `CrimeType` teils selbst aus einem Schwellenwert ableiten
  (z. B. `registerTheft(amount)`), was bei blindem Austausch die
  Verbrechens-Einstufung an diesen Stellen unbeabsichtigt ändern könnte —
  hätte zuerst pro Aufrufer einzeln verifiziert werden müssen.
  `PoliceWarningSystem.EVADING_POLICE` nutzt zudem gar nicht das
  Witness-System, sondern das separate `CrimeManager`-Wanted-Level-System
  — zwei unterschiedliche Mechaniken, die zufällig denselben `CrimeType`-
  Enum-Wert teilen.

**Neuer, verwandter Fund (nicht behoben, da eigenständiges Feature):**
`npc/life/witness/BriberySystem.java` — eine komplette "Zeugen bestechen um
Meldung zu unterdrücken"-Mechanik (`attemptBribe()`), ebenfalls 0 Aufrufer
(keine GUI/Packet nutzt sie). Zusammen mit `CrimeType.BRIBERY` (auch 0
Aufrufer) ist das vermutlich ein eigenes, nie fertiggestelltes Feature
(bräuchte einen Dialog-/Packet-Flow, kein reines Wiring). **Nicht
vorschlagen**, dies "nebenbei" mit zu implementieren — eigenständig
einplanen, falls gewünscht.

---

## NPCInteractionManager/NPCSocialInteractionManager-Merge + zwei Quick-Wins (2026-09-25, Teil 5)

**Status:** ABGESCHLOSSEN — nicht erneut vorschlagen

**Auslöser:** Fortsetzung des Orphan-Scans. Auf den ersten Blick sahen
`npc/life/social/NPCInteractionManager.java` und
`npc/life/social/NPCSocialInteractionManager.java` wie eine klassische
Dopplung aus (beide: NPC-NPC-Beziehungen, beide: "Interaktion" im Namen).
Der Nutzer verlangte ausdrücklich, das VOR einer Löschung zu verifizieren
("prüfe ob es eine dopplung ist oder ob die features aktueller sind") —
diese Prüfung ergab, dass es **keine einfache Dopplung** war:

- `NPCInteractionManager`: wird tatsächlich über `NPCLifeSystemIntegration
  .tick()` als `interactionManager.tick()` regelmäßig aufgerufen (per Grep
  auf die lokale Variable verifiziert, nicht nur auf den Klassennamen —
  ein Klassennamen-Grep hätte hier einen falschen Negativbefund geliefert).
  Es besitzt bereits reichhaltige Aktionsmethoden (`converse()`, `greet()`,
  `initiateNPCTrade()`, Cooldown-Verwaltung), aber **keine** dieser
  Methoden wird von irgendwo aus ausgelöst — der Tick lief leer.
- `NPCSocialInteractionManager`: ein vollständiger, eigenständiger
  Beziehungs-Simulator (`npcRelations`-Map, `NPCInteractionType`-Enum mit
  Beziehungsänderungswerten, `tick(ServerLevel)` mit Scan-und-Trigger-Logik,
  `mediateConflict`) — aber komplett unregistriert, `tick()` wurde von
  niemandem aufgerufen.

Beide Klassen waren also **halb-tot, aber auf komplementäre Art**: die eine
war angeschlossen, aber inhaltsleer; die andere war inhaltlich vollständig,
aber nie angeschlossen. Eine simple "lösche die ältere" hätte funktionierende
Aktionsmethoden (`NPCInteractionManager`) oder die einzige vorhandene
autonome Trigger-Logik (`NPCSocialInteractionManager`) vernichtet.

**Merge (statt Löschung):** Die Scan-und-Trigger-Logik von
`NPCSocialInteractionManager` wurde nach `NPCInteractionManager` übernommen
(`autoTriggerNearbyInteractions(ServerLevel)` + `triggerAmbientInteraction()`),
ruft dort aber die bereits vorhandenen, reicheren Aktionsmethoden
(`converse()`/`greet()`/`initiateNPCTrade()`) auf statt eigene zu
duplizieren. Die Beziehungsverwaltung (`npcRelations`, `getRelation()`,
`modifyRelation()`, `mediateConflict()`) wurde ebenfalls übernommen, jetzt
aber **persistiert** (`InteractionManagerData` via
`AbstractPersistenceManager`, vorher war `NPCSocialInteractionManager`s
Map rein transient und ging bei jedem Serverneustart verloren).
`NPCLifeSystemIntegration.tick()` ruft `interactionManager
.autoTriggerNearbyInteractions(level)` jetzt alle 200 Ticks (10 Sekunden)
auf — ein neuer, eigener Zeitraster-Block, getrennt vom bestehenden
100-Tick-Block. `NPCSocialInteractionManager.java` wurde danach vollständig
gelöscht (verifiziert: 0 verbleibende Referenzen).

**Nicht vorschlagen**, diese beiden Klassen erneut als Dopplung zu prüfen
oder eine der beiden Implementierungen wiederherzustellen.

### Quick Win 1: `CrimeRecordCommand` registriert

`npc/crime/CrimeRecordCommand.java` (`/crimerecord <player> [evidence|clear]`)
war vollständig implementiert (nutzt reale `CrimeManager`-/
`EvidenceManager`-Methoden, alle Signaturen per Grep verifiziert), hatte
aber 0 Aufrufer — der Befehl wurde nirgends registriert. Fix: ein Aufruf
`CrimeRecordCommand.register(event.getDispatcher())` in
`ScheduleMC.onRegisterCommands()`, direkt neben der bestehenden
`BountyCommand`-Registrierung.

**Bekannter, nicht behobener Altbestand:** `CrimeRecordCommand.java`
enthält mehrere hartkodierte deutsche User-facing-Strings (z. B. "Crime
Record:", "Wanted Level:", "Verbrechen gesamt:"). Das verstößt gegen die
Sprachkonvention oben, wurde aber von `scripts/check-german-strings.sh`
nicht erkannt (Guard meldet weiterhin "OK") und ist nicht durch diese
Registrierung neu entstanden. **Nicht im Rahmen dieser Änderung behoben**
— eigenständig einplanen, falls gewünscht (inkl. Prüfung, warum der Guard
diese Strings nicht erfasst).

### Quick Win 2 (geprüft, NICHT umgesetzt): `WantedListSyncPacket`

`npc/network/WantedListSyncPacket.java` + `WantedListClientCache` — laut
eigenem Kommentar für "Feature 5: Fahndungsplakate" (Wanted-Posters-App
auf dem Smartphone) gedacht. Verifiziert: das Paket ist **nicht** in
`NPCNetworkHandler` registriert, `WantedListClientCache` hat 0 Referenzen
irgendwo sonst im Code. Zusätzlich verifiziert (`SmartphoneScreen.java`,
alle 15 registrierten Apps durchgesehen): es gibt **keine** "Wanted
Posters"-App im Smartphone — `CrimeStatsAppScreen` existiert, ist aber ein
anderes Feature. Es gibt außerdem keinen Server-Code, der dieses Paket
jemals konstruieren/senden würde.

**Entscheidung:** Nicht registriert. Ein bloßes `registerMessage()` würde
ein Paket an den Netzwerk-Channel anschließen, das nie gesendet und dessen
Cache nie gelesen wird — technisch "verdrahtet", aber ohne jede
Spielwirkung, also kein echter Fix. Eine echte Lösung bräuchte eine neue
UI-Screen (Wanted-Posters-App) UND einen neuen Sende-Trigger (z. B.
periodischer Server-Broadcast oder On-Demand-Anfrage) — das ist ein neues
Feature, kein Wiring-Quick-Win. **Nicht vorschlagen**, dieses Paket ohne
UI + Sende-Trigger zu registrieren — das wäre ein hohler Fix. Eigenständig
als Feature einplanen, falls gewünscht (Screen + Trigger + Registrierung
in einem Zug).

---

## Restliche Kandidaten aus dem Orphan-Scan (2026-09-25, Teil 6)

**Status:** TEILWEISE UMGESETZT — Rest dokumentiert, Entscheidung ausstehend

Fortsetzung des Scans über die 7 zuvor gemeldeten Kandidaten
(TutorialManager, RedemptionQuestManager, ProductionEventManager,
ProductionRegistry, DialogueConsequenceSystem, DialogueHelper,
CompanionBehavior).

### Umgesetzt: Zwei echte Dopplungen gelöscht (DialogueHelper, DialogueConsequenceSystem)

Verifiziert per Nachlesen des bereits real funktionierenden Dialogsystems
(`DialogueManager`/`DialogueNode`/`DialogueOption`/`DialogueAction`/
`DialogueCondition`/`DefaultDialogueTrees`/`NPCDialogueProvider`, angebunden
über `StartDialoguePacket`/`SelectDialogueOptionPacket`/`DialogueStatePacket`,
0 Bezug zu `DialogueHelper`/`DialogueConsequenceSystem`):

- `DialogueHelper.getGreeting()`/`getAvailableOptions()` duplizierten exakt
  das, was `DialogueNode.getDisplayText()` (mit `ConditionalText`) und
  `DialogueNode.getVisibleOptions()` (mit `DialogueOption.isVisible()`) im
  echten System bereits generisch und bedingungsbasiert leisten.
- `DialogueConsequenceSystem.applyConsequence()` duplizierte exakt das, was
  `DialogueAction.modifyFactionReputation()`/`payMoney()`/
  `giveTempDiscount()`/`startQuest()`/`completeQuest()` im echten System
  bereits leisten — und zwar vollständiger (inkl. echter
  `MissionEventBridge`-Anbindung, die `DialogueConsequenceSystem` gar nicht
  hatte).

Beide Klassen hatten 0 externe Aufrufer und waren reine, nie angeschlossene
Parallel-Implementierungen — echte Dopplungen wie zuvor `NegotiationSystem`.
Gelöscht (307 + 259 Zeilen). **Nicht vorschlagen**, sie wiederherzustellen.

### Umgesetzt: CompanionBehavior tatsächlich verdrahtet (echter Gameplay-Bug-Fix)

`CompanionManager` (Rekrutierung/Beschwörung/Befehle/Persistenz) war
vollständig funktional und über Packets erreichbar — ein rekrutierter
Begleiter konnte beschworen und per `giveCommand()` (FOLLOW/STAY/SCOUT/
ATTACK/DEFEND/HEAL/RETURN/FREE) gesteuert werden. Aber `CompanionBehavior`
(die Klasse, die diese Befehle tatsächlich in Bewegung/Kampf/Aktion
umsetzt) hatte 0 Aufrufer — `CompanionManager.tick()` rief nur
`data.tick()` (reine Cooldown-Buchhaltung) auf. **Konsequenz vor dem Fix:**
Ein beschworener Begleiter stand nur da, folgte nicht, kämpfte nicht,
erkundete nicht — trotz vollständig funktionierender Rekrutierung/Befehle.

`CompanionBehavior` selbst enthielt bereits die vorgesehene
Anschlussstelle: eine innere `CompanionFollowGoal extends Goal`-Klasse, die
pro Tick `behavior.tick(level)` aufruft. Fix: neue Methode
`CustomNPCEntity.attachCompanionBehavior(CompanionBehavior)` (fügt die Goal
mit Priorität 1 zum `goalSelector` hinzu), aufgerufen aus
`CompanionManager.summon()` direkt nach dem Erzeugen der Entity — vor
`level.addFreshEntity(entity)`. Kein bestehendes Verhalten wurde entfernt,
nur die fehlende Verbindung ergänzt.

### Umgesetzt: TutorialManager-Lebenszyklus verdrahtet (mit dokumentierter Einschränkung)

`TutorialManager` (`managers/TutorialManager.java`) war ein vollständiges,
aber 0 Aufrufer habendes Onboarding-System (6 Phasen, 14 Schritte,
JSON-persistiert). Verifiziert: **kein** `/tutorial`-Befehl existierte
irgendwo im Code — `TutorialManager.java` war die einzige Fundstelle für
"tutorial" im gesamten Repository.

**Umgesetzt (sicher, ohne Ratespiel):**
- `TutorialManager.initialize(server)` in `ScheduleMC.onServerStarted()`.
- Registrierung beim `IncrementalSaveManager` (Persistenz beim Shutdown).
- `TutorialManager.onPlayerJoin(player)` in `ScheduleMC.onPlayerLoggedIn()`
  (zeigt Willkommensnachricht / aktuellen Fortschritt beim Login).
- Neuer Befehl `/tutorial skip` (`managers/TutorialCommand.java`), registriert
  in `ScheduleMC.onRegisterCommands()`.

**Bewusst NICHT umgesetzt:** Die 14 `completeStep()`-Aufrufe, die den
Spieler tatsächlich durch die Phasen bringen sollen (z. B. "Kaufe dein
erstes Grundstück" → `TutorialStep.BUY_PLOT`), sind über komplett
unabhängige Systeme verteilt (Economy-Commands, Plot-Kauf, Produktions-
Blockentities, NPC-Handel-Packets, Gang-Missionen, `/market`). Jeden dieser
14 Trigger-Punkte richtig zu identifizieren und zu verifizieren wäre ein
eigener, breiter Sweep über viele unabhängige Systeme — zu groß und zu
ratespiel-anfällig, um hier nebenbei erledigt zu werden. **Aktueller
Zustand:** Ein Spieler sieht beim Login die Phase-1-Hinweise ("Prüfe dein
Guthaben mit /money", "Besuche einen Bankautomaten") und kann mit
`/tutorial skip` überspringen, aber die Phase schaltet nie automatisch
weiter (das UI ist konsistent und ehrlich — nur eben statisch, bis zum
Skip). **Nicht vorschlagen**, die 14 `completeStep()`-Trigger blind zu
raten — einzeln pro System verifizieren, falls gewünscht.

### ProductionConfig/ProductionRegistry/UnifiedProcessingBlockEntity — komplett totes Parallel-Framework ENTFERNT (2026-09-25, Teil 7)

**Status:** ABGESCHLOSSEN — nicht erneut vorschlagen, dieses Framework wiederherzustellen

**Vorgeschichte:** Ursprünglich (Teil 6) als "3 Dateien, 1.372 Zeilen" gemeldet und aus
Vorsicht nicht gelöscht, weil das wie eine architektonische Grundsatzentscheidung aussah.
Auf explizite Nutzeranfrage ("extrem gründlich recherchieren") wurde eine vollständige
Tiefenanalyse nachgeholt — dabei stellte sich heraus, dass die ursprüngliche Meldung
**zwei weitere Dateien desselben Frameworks übersehen hatte** und dass die Löschung, sauber
verifiziert, gar keine Grundsatzentscheidung war, sondern ein normaler Dead-Code-Fund wie
`NegotiationSystem` — nur größer.

**Git-Archäologie:** Alle betroffenen Dateien reichen bis zum allerersten Commit des Repos
zurück (`a070ed4`, Repo-Historie beginnt dort komplett neu) — keine Rückschlüsse über
"wann/warum abgebrochen" möglich, das Framework war von Anfang an fertig und unbenutzt.

**Die tatsächlichen 9 Dateien (statt der ursprünglich gemeldeten 3), 2.753 Zeilen:**

- `production/config/ProductionConfig.java` (316 Z.) — Builder-Pattern-Config pro Warensorte.
- `production/config/ProductionRegistry.java` (511 Z.) — Singleton-Katalog; hardcodet einen
  kompletten, parallelen 8-Kategorien-Warenkatalog (Tobacco/Cannabis/Coca/Poppy/Mushroom/
  MDMA/LSD/Meth) mit echten, real registrierten Item-IDs (verifiziert z. B. gegen
  `FreshTobaccoLeafItem`/`DriedBudItem`) — technisch funktionsfähig gemeint, aber `getInstance()`
  wurde nachweislich nur aus der eigenen Methode heraus aufgerufen, nie von außen.
- `production/blockentity/UnifiedProcessingBlockEntity.java` (545 Z.) — generische,
  config-getriebene BlockEntity. Eigener Javadoc-Wortlaut: *"Ersetzt: AbstractDryingRackBlockEntity
  (und 3 Subklassen), AbstractFermentationBarrelBlockEntity (und 3 Subklassen),
  AbstractExtractionVatBlockEntity (und 3 Subklassen), AbstractRefineryBlockEntity (und 3
  Subklassen), ReactionKettleBlockEntity (MDMA), FermentationTankBlockEntity (LSD), und viele
  mehr... Reduziert ~2000 Zeilen Code auf eine einzige konfigurierbare Klasse."* Alle 6 dort
  genannten Zielklassen existieren weiterhin unverändert und aktiv genutzt — die Ersetzung
  fand nie statt. 0 Instanziierungen, 0 Subklassen.
- `production/blockentity/AbstractProcessingBlockEntity.java` (317 Z.) — **zuvor nicht
  gemeldeter, zweiter, unabhängiger** generischer Versuch (`<T extends ProductionType, Q
  extends ProductionQuality>`, nutzt echten `ItemStackHandler`, erbt von der echten,
  produktiv genutzten `AbstractItemHandlerBlockEntity`). 0 Subklassen — auch dieser zweite
  Anlauf wurde nie zu Ende geführt.
- `production/core/GenericQuality.java` (380 Z.) — eigener Javadoc-Wortlaut: *"Ersetzt:
  TobaccoQuality (4 Tiers), CannabisQuality (5 Tiers), MDMAQuality (4 Tiers)."* Alle drei
  Ziel-Enums sind weiterhin die real genutzten (46/26/12 Referenzen) — auch diese Ersetzung
  fand nie statt.
- `production/core/GenericPlantData.java` (319 Z.) — generische Pflanzenwachstums-Datenklasse,
  0 echte Aufrufer (nur von den jetzt ebenfalls gelöschten Tests genutzt).
- `production/core/ProductionStage.java` (56 Z.) — Interface für Produktionsphasen, 0
  Implementierungen irgendwo, auch nicht innerhalb des toten Frameworks selbst.
- `production/blocks/AbstractPlantBlock.java` (218 Z.) — der Block-Ebene-Gegenpart zu
  `UnifiedProcessingBlockEntity`, 0 Subklassen. Die echten Pflanzenblöcke
  (`TobaccoPlantBlock`, `CannabisPlantBlock`, `CocaPlantBlock`, …) erben direkt von `Block`.
- `production/blocks/AbstractProcessingBlock.java` (91 Z.) — 0 Subklassen.

Plus 2 Testdateien (653 Zeilen), die ausschließlich dieses Framework testeten und mitgelöscht
wurden: `GenericProductionSystemTest.java`, `GenericQualityLookupTest.java`.

**Verifiziert vor der Löschung:** Repo-weiter Grep auf alle 9 Klassennamen ergab 0
verbleibende Referenzen außerhalb der zu löschenden Dateien selbst; die von den Javadocs
behaupteten "ersetzten" Klassen (`TobaccoQuality`/`CannabisQuality`/`MDMAQuality`,
`AbstractDryingRackBlockEntity`, `AbstractFermentationBarrelBlockEntity`,
`AbstractExtractionVatBlockEntity`, `AbstractRefineryBlockEntity`, `ReactionKettleBlockEntity`,
`FermentationTankBlockEntity`, `PlantPotBlock`, `AbstractItemHandlerBlockEntity`) sind alle
unverändert vorhanden und aktiv referenziert — die Löschung hat keinerlei Auswirkung auf das
tatsächlich laufende Produktionssystem.

**Reales System zum Vergleich:** 115 echte Produktions-BlockEntity-Dateien über 12
Warengruppen, gespalten in zwei komplett getrennte Architektur-Familien: 45 "legale" Klassen
(Beer/Wine/Cheese/Chocolate/Coffee/Honey/Tobacco) mit der echten gemeinsamen
`AbstractItemHandlerBlockEntity` als Basis, und 24 "illegale" Klassen (Cannabis/Coca/MDMA/
LSD/Meth), die alle direkt von `BlockEntity` erben, ohne jede gemeinsame Basis — nicht einmal
untereinander. Das tote Framework wollte offenbar genau diese Lücke schließen, hat es aber
nie geschafft.

**Doku-Bereinigung:** `docs/DEVELOPER_GUIDE.md` enthielt einen kompletten, irreführenden
"Adding a New Production System"-Leitfaden (Step 1-8) mit der falschen Behauptung, alle
existierenden Systeme (Tobacco, Cannabis, Coffee, Wine, …) seien auf diesem Framework
aufgebaut — ersetzt durch eine korrekte Beschreibung des tatsächlichen Patterns
(handgeschriebene Enums pro Warengruppe + `AbstractItemHandlerBlockEntity` für legale Waren
+ Supplier-Pattern für Größenvarianten). Passende Korrekturen auch in
`docs/ARCHITECTURE.md`, `docs/CONFIGURATION.md`, `docs/TESTING.md`.

**Nicht vorschlagen**, dieses Framework (oder Teile davon) wiederherzustellen. Falls
künftig eine echte Vereinheitlichung gewünscht ist: der sinnvolle Ansatzpunkt wäre eine
gemeinsame Basisklasse für die 24 "illegalen" BlockEntities (analog zu
`AbstractItemHandlerBlockEntity` für die legalen Waren) — nicht die Wiederbelebung dieses
Frameworks.

### ProductionEventManager ENTFERNT (2026-09-25, Teil 8)

**Status:** ABGESCHLOSSEN — nicht erneut vorschlagen, diese Klasse wiederherzustellen

**Vorgeschichte:** In Teil 6 als "gefunden, nicht umgesetzt, Entscheidung ausstehend"
gemeldet (vollständiges Zufallsevent-System, 8 Events wie Polizeirazzia/Rekordernte/
Chemieunfall mit Ertrags-/Geschwindigkeits-/Preis-/Qualitäts-Modifikatoren, 0 Aufrufer).
Auf explizite Nutzerfrage ("ist der productioneventmanager in nutzung?! gründlich
recherchieren!") wurde die Nutzung ein zweites Mal, unabhängig von der ursprünglichen
Meldung, vollständig neu verifiziert:

- Repo-weite Textsuche (alle Dateitypen, nicht nur `.java`): der Klassenname taucht nur
  in der eigenen Datei sowie in `CLAUDE.md`/`docs/CHANGELOG.md` (dieser eigenen
  Dokumentation) und einem einzigen, bereits vor dieser Session bestehenden Audit-Dokument
  (`docs/CODE_VS_DOCS_ABGLEICH_2026-09-24.md`) auf — dort mit einer nachweislich falschen
  Behauptung ("wird von `SeasonalPriceModifier` genutzt"; die Klasse referenzierte
  `SeasonalPriceModifier` an keiner Stelle), korrigiert.
- `getInstance()`: 0 Aufrufe irgendwo im Code.
- Alle öffentlichen API-Methoden (`onDayChange`, `getCombinedYieldModifier`,
  `getCombinedSpeedModifier`, `getCombinedPriceModifier`, `getCombinedQualityChange`,
  `isEventActive`, `getEventReport`, `getActiveEvents`): 0 Aufrufe. (Eine andere, echte
  Klasse `WorldEventManager` hat zufällig gleichnamige Methoden — dort aber real verdrahtet
  und aktiv genutzt von `NegotiationPacket`/`OpenMerchantShopPacket`/`PurchaseItemPacket`;
  keine Verwechslungsgefahr im Code, nur bei reiner Methodennamen-Suche.)
- Kein `@Mod.EventBusSubscriber`/`@SubscribeEvent` (schließt den bekannten
  Forge-Auto-Registrierungs-Fehlalarm aus), keine Reflection-Nutzung, kein statischer
  Initialisierungsblock.

**Ergebnis:** zu 100 % bestätigt toter Code, keine neuen Erkenntnisse gegenüber Teil 6 —
nur vollständigere Verifikation. `production/events/ProductionEventManager.java` (432
Zeilen) gelöscht, das jetzt leere Verzeichnis `production/events/` mitentfernt. Keine
Testdatei referenzierte die Klasse. Die im ursprünglichen Fund beschriebene
Integrationsoption (Trigger via `onDayChange()` + Preis-Modifikator risikoarm, Ertrag/
Geschwindigkeit/Qualität nur mit großem Aufwand über ~115 Produktions-BlockEntities
riskant) bleibt als Referenz erhalten, falls das Feature künftig doch gewünscht und neu
gebaut werden soll — aber nicht durch Wiederherstellung dieser Datei.

### Gefunden, NICHT umgesetzt (Entscheidung ausstehend): RedemptionQuestManager

`npc/life/quest/RedemptionQuestManager.java` (229 Zeilen) — Vergebungs-
Quest-System für Spieler mit Fraktions-Reputation < -20, 0 Aufrufer.
**Keine Dopplung** von `QuestManager` (904 Zeilen, real verdrahtet, tickt,
persistiert): `QuestManager`s Quests verlangen eine **Mindest**-Reputation
zum Annehmen (`minFactionRep`), sind also für Spieler mit stark negativer
Reputation gar nicht verfügbar — `RedemptionQuestManager` deckt exakt diese
Lücke ab (Reputation *wiederherstellen*, nicht *ausbauen*). Eine
komplementäre, nicht dopplerische Funktion.

**Warum trotzdem nicht umgesetzt:** Es fehlt ein Start-Trigger (z. B. ein
neuer `DialogueAction.offerRedemptionQuest()`, analog zu
`DialogueAction.checkForQuest()`/`offerQuest()`, der bei niedriger
Reputation im Dialog erscheint) UND die 4 `reportProgress()`-Trigger
(Community Service: 5 NPCs helfen: kein verifizierbarer "NPC geholfen"-Hook
gefunden; Spende: Geldstrafe zahlen; Kurierdienst: 3 Waren an NPCs liefern;
Wachpatrouille: 5 Minuten patrouillieren) sind über mehrere unabhängige
Systeme verteilt und teilweise (Community Service) hat keinen
verifizierbaren Hook im Code. **Nicht vorschlagen**, `reportProgress()` an
geratene Stellen zu hängen — der Dialog-Trigger allein wäre machbar, aber
ohne die Progress-Hooks bliebe die Quest niemals abschließbar
(schlimmer als gar nicht angeboten). Eigenständig als Feature einplanen,
falls gewünscht (Dialog-Trigger + alle 4 Progress-Hooks in einem Zug).
