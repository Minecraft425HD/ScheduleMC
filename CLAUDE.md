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

1. **`police.max_roadblocks`/`roadblock_duration_seconds`** — ERLEDIGT
   (2026-09-25, Teil 13, siehe eigener Abschnitt unten).

2. **`police.wanted_posters_min_level`**: Neues `WantedPosterItem` +
   wandmontierter `WantedPosterBlock` (Name + Bounty aus
   `WantedListSyncPacket`-Daten), ausgegeben/platziert ab konfiguriertem
   Wanted-Level.

3. **`police.speed_limit_default`** — ERLEDIGT
   (2026-09-25, Teil 15, siehe eigener Abschnitt unten).

4. **`police.flanking_enabled`** — ERLEDIGT
   (2026-09-26, Teil 17, siehe eigener Abschnitt unten).

5. **`police.siren_sound_radius`** — ERLEDIGT
   (2026-09-26, Teil 18, siehe eigener Abschnitt unten).

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

### RedemptionQuestManager ENTFERNT (2026-09-25, Teil 11)

**Status:** ABGESCHLOSSEN — nicht erneut vorschlagen, diese Klasse wiederherzustellen

**Vorgeschichte:** In Teil 6 als "gefunden, nicht umgesetzt, Entscheidung ausstehend"
gemeldet — Vergebungs-Quest-System für Spieler mit Fraktions-Reputation < -20, 0
Aufrufer, **keine Dopplung** von `QuestManager` (der verlangt eine Mindest-Reputation
zum Annehmen, ist also für stark negative Reputation gar nicht verfügbar —
`RedemptionQuestManager` deckte eine echte, komplementäre Lücke ab).

**Auf explizite Nutzeranfrage erneut recherchiert**, ob es einen sicheren Start- oder
Progress-Trigger gibt (auch unter Einbeziehung des inzwischen entdeckten
Mission-Systems, `mission/`-Paket mit `PlayerMissionManager`/`ScenarioObjective`, als
möglicher generischer "Liefere Item"/"Patrouilliere"-Objective-Quelle):

- **Community Service** (5 NPCs helfen): kein generischer "Spieler hat NPC geholfen"-
  Hook gefunden. Der einzige `MemoryType.HELPED`-Treffer ist Begleiter-Rekrutierung —
  ein völlig anderer Kontext.
- **Spende** (Geldstrafe zahlen): Es gibt eine automatische "Ergeben bei
  Polizei-Warnung"-Geldstrafe (`PoliceWarningSystem`), aber die ist automatisch,
  reduziert den Wanted-Level (nicht Fraktions-Reputation) und ist keine
  Spieler-initiierte Wiedergutmachungs-Aktion.
- **Kurierdienst** (3 Waren an NPCs liefern): Das `mission/`-Paket ist ein
  editor-/skript-gesteuertes Haupt-/Nebenmissionssystem für kuratierte Inhalte, kein
  generischer "liefere Item X an beliebigen NPC"-Mechanismus.
- **Wachpatrouille** (5 Minuten patrouillieren): Patrouillen existieren nur für
  Polizei-NPCs (`PolicePatrolGoal`), nicht als Zeit-Tracking für Spieler.

Das deckt sich exakt mit dem ursprünglichen Teil-6-Befund — auch nach erneuter,
gründlicher Suche kein sicherer Hook für die 4 Progress-Trigger. Nur der Start-Trigger
(Dialog-Option bei niedriger Reputation) wäre risikofrei gewesen — aber eine Quest, die
sich annehmen, aber nie abschließen lässt, wäre schlechter als gar keine. **Auf
Nutzerentscheidung hin vollständig gelöscht** statt nur den halben (Start-)Trigger zu
bauen. `wiki/features/NPC-System.md` enthielt einen "Implemented, Not Wired Up"-Abschnitt
dazu (und einen zweiten, bereits seit Teil 6 veralteten Abschnitt zu
`DialogueConsequenceSystem`) — beide durch eine kurze, korrekte Notiz ersetzt.

**Nicht vorschlagen**, diese Klasse wiederherzustellen, ohne dass zuerst mindestens
einer der 4 Progress-Trigger durch ein neues, bewusst entworfenes Feature (nicht
"gefundenes Wiring") ersetzt wird — z. B. ein neuer Dialog-"Spenden"-Button oder ein
neuer Dialog-"Helfen"-Button, die von Grund auf gebaut werden müssten.

---

## Repo-weiter Orphan-Scan Teil 9: 4 echte Dopplungen + 15 isolierte Dateien entfernt (2026-09-25)

**Status:** ABGESCHLOSSEN — nicht erneut vorschlagen, diese Klassen wiederherzustellen

**Methode:** Systematischer Referenzzähl-Scan über alle 1.515 `.java`-Dateien in
`src/main/java` (Tokenisierung + Cross-Reference-Zählung pro Klassenname). 48 Klassen mit
0 externen Referenzen gefunden; nach Filtern von `package-info.java` und
`@Mod.EventBusSubscriber`/`@SubscribeEvent`-annotierten Klassen (20 Stück, bekannter
Forge-Auto-Registrierungs-Fehlalarm, nicht einzeln weiter verifiziert) blieben 23 echte
Kandidaten.

### Vier echte Dopplungen (von echtem Code ersetzt), gelöscht

- **`region/PlotChunkCache.java`** (399 Z.) — eigener Javadoc beschreibt exakt das Problem,
  das die tatsächlich genutzte `PlotCache`-Klasse (LRU-Cache + Spatial-Index, verdrahtet in
  `PlotManager.getPlotAt()`) bereits löst. Ein überholter Alternativansatz (Chunk-basiertes
  Caching statt Spatial-Index), nie eingebaut.
- **`coffee/blockentity/AbstractCoffeeDryingTrayBlockEntity.java`** (290 Z.) — **explizit im
  Code als tot markiert:** `CoffeeBlockEntities.java` Zeile 26-29 sagt wörtlich "Coffee
  Drying Trays wurden durch TobaccoBlocks.SMALL/MEDIUM/BIG_DRYING_RACK ersetzt ... Die
  Coffee-spezifischen BlockEntities ... können entfernt werden" — ein früherer Entwickler
  hatte das bereits erkannt, nur nie ausgeführt.
- **`coffee/CoffeeProcessingMethod.java`** (44 Z.) — WET/DRY-Enum; der DRY-Pfad (Drying
  Tray) wurde entfernt (siehe oben), der reale `WetProcessingStationBlockEntity` nutzt
  dieses Enum an keiner Stelle.
- **`cheese/items/MilkBucketItem.java`** (12 Z.) — ebenfalls **explizit im Code als tot
  markiert:** `CheeseItems.java` Zeile 27 sagt "MILK_BUCKET wurde entfernt - verwende
  net.minecraft.world.item.Items.MILK_BUCKET". Die echte Käse-Kette nutzt den
  Vanilla-Milcheimer.

### Komplett isoliertes MapView-Subsystem + 1 triviale Utility, gelöscht (15 Dateien)

- **`mapview/entityrender/`** (ganzes Paket, 6 Dateien: `EntityVariantDataFactory`,
  `EntityVariantData`, `DefaultEntityVariantData`, `DefaultEntityVariantDataFactory`,
  `TropicalFishVariantDataFactory`, `HorseVariantDataFactory`) — Variant-Color-Rendering für
  Karten-Icons (z. B. Pferdefarben, Fischvarianten). Verifiziert: außerhalb dieses Pakets
  referenziert **nichts** irgendeine dieser 6 Klassen — die "externen Treffer" beim ersten
  Scan waren ausschließlich Querverweise der 6 Dateien untereinander (Interface→Implementierer,
  Basisklasse→Subklasse), keine echte Nutzung von außen. Kein Reflection-Zugriff
  (`Class.forName`/`.class`-Literale) im gesamten `mapview`-Modul gefunden.
- **`mapview/util/AllocatedTexture.java`, `FloatBlitRenderState.java`,
  `FourColoredRectangleRenderState.java`, `MapViewCachedOrthoProjectionMatrixBuffer.java`**
  (4 Dateien, ~135 Z.) — leere/Stub-Implementierungen mit Kommentar "GuiElementRenderState
  doesn't exist in 1.20.1"; vermutlich Überbleibsel desselben gescheiterten 1.20.1-Ports wie
  die bereits dokumentierten MapView-Mixins (siehe "Mixin-Framework entfernt" oben).
- **`mapview/util/BackgroundImageInfo.java`, `mapview/presentation/component/TextButton.java`,
  `OptionSlider.java`, `mapview/data/cache/ComparisonRegionCache.java`** (4 Dateien, ~350 Z.)
  — je einzeln verifiziert: 0 Instanziierungen. `ComparisonRegionCache` gehörte zu einem
  "Comparison"-Feature, von dem sich sonst keine Spur im gesamten `mapview`-Modul findet.
- **`vehicle/util/UniqueBlockPosList.java`** (30 Z.) — triviale Liste, 0 Referenzen.

Alle 19 Löschungen vor dem Entfernen einzeln per Grep gegen `src/main/java` UND
`src/test/java` verifiziert (0 verbleibende Referenzen), zusätzlich `src/main/resources`
auf Registrierungs-/Asset-Bezüge geprüft. Betroffene Verzeichnisse (`mapview/entityrender/`)
wurden komplett leer und sind aus dem Repo verschwunden.

**Zurückgestellt (bewusst NICHT gelöscht, tiefere Prüfung + Nutzenanalyse folgt):**
`util/CircuitBreaker.java`, `ServiceRegistry.java`, `PerformanceMonitor.java`,
`HotReloadableConfig.java`, `TickThrottler.java`, `VersionedData.java` (6 Dateien, 1.456
Zeilen) — siehe eigenen Abschnitt unten.

**Bereits bekannt, weiterhin offen (aus Teil 6):** `RedemptionQuestManager`,
`WantedListSyncPacket` — unverändert, Entscheidung steht noch aus.

**Nicht vorschlagen**, denselben repo-weiten Referenzzähl-Scan zu wiederholen, ohne neuen
Code-Zuwachs seit diesem Datum, und nicht ohne die 20 gefilterten
`@Mod.EventBusSubscriber`/`@SubscribeEvent`-Klassen erneut zu prüfen, falls die
Vollständigkeit dieses Scans in Frage steht — die wurden aus Zeitgründen nur pauschal
per Annotation ausgeschlossen, nicht einzeln funktional verifiziert.

### `util/`-Infrastruktur-Cluster ENTFERNT nach Nutzenanalyse (2026-09-25, Teil 10)

**Status:** ABGESCHLOSSEN — nicht erneut vorschlagen, diese Klassen wiederherzustellen

Die in Teil 9 zurückgestellten 6 Dateien (`CircuitBreaker`, `ServiceRegistry`,
`PerformanceMonitor`, `HotReloadableConfig`, `TickThrottler`, `VersionedData`, 1.456
Zeilen) wurden auf Nutzerwunsch einzeln gegen den echten Code verifiziert, ob sie eine
**aktuell bestehende** Lücke schließen — nicht nur, ob sie 0 Aufrufer haben:

- **`TickThrottler`** zitiert `PlantPotBlockEntity` als Beleg — diese Klasse hat aber
  bereits ihr eigenes, handgeschriebenes `tickCounter`/Intervall-Muster (Zeile 34-59).
  Dasselbe Muster existiert unverändert in Dutzenden weiterer BlockEntities. Einbetten
  würde nichts reparieren, nur eine breite Dedupliziierungs-Refaktorierung auslösen.
- **`ServiceRegistry`** würde das bestehende, vollständig funktionierende
  Manager-Initialisierungssystem (30+ Manager, geordnet in
  `ScheduleMC.onServerStarted()`, sauberes Shutdown über `IncrementalSaveManager`)
  ersetzen — ohne neue Fähigkeit zu gewinnen, nur ein anderes Architekturmuster für
  etwas bereits Funktionierendes.
- **`PerformanceMonitor`** hat kein Ziel: keine Stelle im Code misst aktuell
  Operationszeiten; `HealthCheckManager` (real, bereits existierend) deckt einen
  anderen Zweck ab (Zustandsprüfung, keine Zeitmessung).
- **`HotReloadableConfig<T>`** zielt auf JSON-Config-Dateien mit `WatchService`. Die
  echte Konfiguration läuft komplett über Forges `ForgeConfigSpec` (TOML) — **Forge
  bringt Hot-Reload dafür bereits eingebaut mit** (`ModConfigEvent.Reloading`). Keine
  JSON-Config-Datei im Repo würde dieses Format überhaupt nutzen.
- **`CircuitBreaker`** schützt vor wiederholten Aufrufen unzuverlässiger externer
  Abhängigkeiten. Im gesamten Repo gibt es genau einen externen Netzwerkaufruf
  (`VersionChecker.java`) — ein einmaliger Check beim Serverstart mit bereits eigenem
  try/catch, kein wiederholter Hot-Path, für den ein Circuit-Breaker Sinn ergäbe.
- **`VersionedData`** (Datenmigration bei Speicherformat-Änderungen) widerspricht der
  bereits getroffenen Architekturentscheidung oben ("Sprachkonvention", 2026-06-10):
  *"Keine Rückwärtskompatibilität nötig — die Mod ist in Entwicklung."* Kein Manager hat
  aktuell einen echten Migrationsbedarf.

**Ergebnis:** Bei keiner der 6 Dateien existiert eine aktuelle, echte Lücke im Code, die
sie schließen würde — alle sind durchdacht dokumentierte, aber rein spekulative
Infrastruktur nach demselben Muster wie fast jeder Fund dieser Session. Gelöscht.

**Nachfolgender repo-weiter Re-Scan (gleiche Methode wie Teil 9):** Keine neuen
Kandidaten gefunden — die verbleibenden 23 Klassen mit 0 externen Referenzen sind exakt
die bereits bekannten: die 21 `@Mod.EventBusSubscriber`/`@SubscribeEvent`-Klassen (real,
Forge-Fehlalarm) sowie `RedemptionQuestManager`/`WantedListSyncPacket` (bereits
dokumentiert, Entscheidung offen).

**Nachtrag: die 21 `@Mod.EventBusSubscriber`-Klassen einzeln verifiziert** (auf
Nutzerwunsch, statt nur pauschal per Annotation gefiltert): `WeaponClientEventHandler`,
`WeaponClientSetup`, `RecurringPaymentEventHandler`, `CreditScoreEventHandler`,
`SecretDoorEventHandler`, `SecretDoorClientEventHandler`, `TrafficViolationHandler`,
`NPCClientEvents`, `PrisonEventHandler`, `CompanionEventHandler`, `GangTabListHandler`,
`GangNametagRenderer`, `SmartphoneKeyHandler`, `WantedLevelOverlay`,
`HotbarTooltipOverlay`, `TobaccoPotHudOverlay`, `SmartphonePlayerHandler`,
`InventoryBlockHandler`, `UpdateNotificationHandler`, `SmartphoneProtectionHandler`,
`PlotProtectionHandler`. Bei allen 21: korrekte `@Mod.EventBusSubscriber`-Parameter
(`modid`/`bus`/`value`, kein auskommentierter oder fehlender Lademechanismus wie einst
bei den MapView-Mixins), mindestens eine `@SubscribeEvent`-Methode mit echter Logik
(keine leeren Stubs), und die aufgerufenen Downstream-Manager (`PrisonManager`,
`RecurringPaymentManager`, `CreditScoreManager`, `SmartphoneTracker`, `ClientGangCache`
u. a.) sind selbst real und mehrfach referenziert. **Ergebnis: alle 21 echt und
funktionsfähig — kein weiterer Fund.** Der Referenzzähl-Scan aus Teil 9/10 hat damit
vollständige Abdeckung erreicht. **Nicht vorschlagen**, diese 21 Klassen erneut auf
Totheit zu prüfen, ohne neuen Code-Zuwachs seit diesem Datum.

**Gesamtbilanz des Aufräum-Durchlaufs (Teil 4 bis Teil 10, dieser durchgehenden
Session):** 42 Dateien vollständig gelöscht, **8.026 Zeilen** entfernter Code (36 Dateien
/ 6.570 Zeilen in den zuvor dokumentierten Commits, plus diese 6 Dateien / 1.456 Zeilen).
Dazu kommen die parallel dokumentierten *Einbau*-Fixes (WarehouseMarketBridge,
PriceModifier, BatchTransactionManager, CompanionBehavior, TutorialManager-Lebenszyklus,
CrimeEventHandler-Vandalismus/Trespassing), die eigenständig gezählt werden, da sie
Code hinzufügen statt entfernen.

---

## Wanted-Poster-Feature vollständig gebaut (2026-09-25, Teil 12)

**Status:** ABGESCHLOSSEN — nicht erneut vorschlagen, das Feature erneut zu bauen oder
`WantedListSyncPacket` wiederherzustellen

**Auslöser:** Fortsetzung der `WantedListSyncPacket`-Untersuchung aus Teil 5/6/9. Statt
das alte, nie gesendete Sync-Paket samt Client-Cache zu registrieren (das hätte laut
damaliger Analyse ein "hohler Fix" ohne echte UI ergeben), hat der Nutzer explizit
verlangt, das im Backlog-Abschnitt oben ("Backlog: Fehlende Features für 12 verbleibende
Dead-Config-Werte", Punkt 2) bereits beschriebene Feature **vollständig neu zu bauen**:
"Baue das feature vollständig ein! Komplett!"

**Neues Paket:** `npc/crime/poster/` mit vier neuen Klassen:
- `WantedPosterItem` (extends `BlockItem`) — trägt Zielspieler-UUID/-Name, Wanted-Level
  und Kopfgeld als NBT; `create(UUID, String, int, double)`-Fabrikmethode; Tooltip zeigt
  Name/Sterne/Kopfgeld.
- `WantedPosterBlock` (extends `HorizontalDirectionalBlock implements EntityBlock`) —
  wandmontiert wie ein Bild, nur an vertikalen, tragfähigen Wänden platzierbar
  (`getStateForPlacement`/`canSurvive` prüfen `isFaceSturdy`), je Blickrichtung eigene
  dünne `VoxelShape`. Rechtsklick zeigt die Daten als Chat-Nachrichten.
- `WantedPosterBlockEntity` — übernimmt die NBT-Daten des Items beim Platzieren
  (`readFromItem`), persistiert sie (`saveAdditional`/`load`), synct sie an Clients
  (`getUpdateTag`/`getUpdatePacket` via `ClientboundBlockEntityDataPacket`).
- `WantedPosterRegistry` — `DeferredRegister` für Block/Item/BlockEntityType
  (`WANTED_POSTER_BLOCK`/`WANTED_POSTER_ITEM`/`WANTED_POSTER_BLOCK_ENTITY`), registriert
  in `ScheduleMC`s Konstruktor neben dem Wine-System.

**Bewusste Scope-Entscheidungen (kein Ratespiel, dokumentiert statt stillschweigend
vereinfacht):**
- **Keine dynamische In-World-Textdarstellung.** Es gibt im gesamten Repo kein Präzedenzbeispiel
  für einen `BlockEntityRenderer`, der Text auf einen Block rendert (verifiziert per Grep).
  Ein neuer, ungetesteter Renderer für dieses eine Feature wäre ein hohes Risiko ohne
  Möglichkeit, ihn in dieser Umgebung zu kompilieren/zu testen. Stattdessen zeigt Rechtsklick
  die Daten als `Component.translatable`-Chat-Nachrichten (`message.wanted_poster.*`).
- **Wiederverwendete Textur statt neuer Pixel-Art.** Es steht in dieser Umgebung kein
  Bildgenerierungs-Tool zur Verfügung. Item-Icon und Block-Textur nutzen die bereits
  vorhandene `textures/item/blotter_paper.png` (32×32 RGBA, verifiziert).
- **Auto-Ausgabe an alle Online-Spieler außer dem Gesuchten selbst**, nicht an den
  Gesuchten oder eine einzelne Polizei-NPC-Inventar-Instanz — es gibt keine
  "Polizei-NPC-Inventar"-Mechanik, die ein Item ausgeben könnte. Broadcast an
  `server.getPlayerList().getPlayers()` ist ein bereits etabliertes Muster im Repo (siehe
  z. B. `GangSyncHelper`, `NPCNameSyncHandler`, `PoliceAIHandler`).

**Auto-Ausgabe-Hook:** `CrimeManager.addWantedLevel(UUID, int, long)` — neue Methode
`issueWantedPosterIfThresholdCrossed(UUID, int previousLevel, int newLevel)`, aufgerufen
direkt nach der bereits bestehenden `BountyManager.createAutoBounty`-Anbindung (FIX 2).
Löst nur beim **Überschreiten** der konfigurierten Schwelle
(`ModConfigHandler.COMMON.POLICE_WANTED_POSTERS_MIN_LEVEL`, Default 3) aus
(`previousLevel < minLevel && newLevel >= minLevel`), nicht bei jedem weiteren Verbrechen
danach. Kopfgeld-Betrag kommt aus `BountyManager.getInstance().getActiveBounty(UUID)`
(0.0 falls noch keine Bounty existiert). Ist zum Auslösezeitpunkt kein Online-Spieler mit
dieser UUID vorhanden, wird kein Plakat ausgegeben (kein Offline-Namens-Fallback über
Profile-Cache — Wanted-Level-Eskalationen passieren ausschließlich durch Live-Aktionen
eines Online-Spielers, ein Offline-Fall wäre ohnehin nicht real erreichbar).

**`WantedListSyncPacket.java` + `WantedListClientCache` gelöscht:** Verifiziert 0
verbleibende Referenzen (nur noch in `docs/CHANGELOG.md` als historische Notiz). Das neue
Design braucht kein separates "sync ganze Wanted-Liste"-Paket — die `BlockEntity`
synct sich über den vorhandenen vanilla `ClientboundBlockEntityDataPacket`-Mechanismus
selbst, pro Plakat statt pro globaler Liste.

**Assets:** `blockstates/wanted_poster.json` (4 Facing-Varianten, je eigenes Modell statt
Rotations-Mathematik, um Y-Rotations-Fehler ohne Testmöglichkeit zu vermeiden),
`models/block/wanted_poster_{north,south,west,east}.json` (dünne Box passend zur
jeweiligen `VoxelShape`), `models/item/wanted_poster.json` (`item/generated` mit
`blotter_paper`-Textur). Lang-Keys in `en_us.json`/`de_de.json`:
`block.schedulemc.wanted_poster`, `item.schedulemc.wanted_poster`,
`message.wanted_poster.{blank,bounty,header,issued,level,name}`.

**Nicht vorschlagen:** dieses Feature erneut zu bauen, `WantedListSyncPacket`
wiederherzustellen, oder die Scope-Entscheidungen (Chat-Anzeige statt In-World-Renderer,
wiederverwendete Textur) ohne explizite Nutzeranfrage rückgängig zu machen/auszubauen.

---

## Roadblock-Trigger verdrahtet (2026-09-25, Teil 13)

**Status:** ABGESCHLOSSEN — nicht erneut vorschlagen

**Betrifft:** Backlog-Punkt 1 ("Fehlende Features für 12 verbleibende Dead-Config-Werte",
`police.max_roadblocks`/`police.roadblock_duration_seconds`).

**Vorgefundener Zustand:** `PoliceRoadblock.java` war vollständig implementiert
(Platzierung, Ablauf-Timer, Cleanup bei Festnahme/Logout), aber sowohl
`createRoadblock()` als auch `tick()` hatten 0 Aufrufer — verifiziert per Grep, die
einzigen externen Referenzen waren `removeAllForPlayer()` (aus `PoliceAIHandler.arrestPlayer`
und `PlayerDisconnectHandler`).

**Fix, zwei Teile:**
1. `ScheduleMC.onServerTick()`: `PoliceRoadblock.tick(server.overworld())` direkt neben
   den bestehenden `PoliceAIHandler.updatePlayerCache`/`updatePoliceCache`-Aufrufen
   ergänzt. Nutzt bewusst `server.overworld()` statt einer Schleife über
   `server.getAllLevels()`, konsistent mit der oben dokumentierten Architekturentscheidung
   "ScheduleMC nutzt genau eine Dimension (Overworld)".
2. `PoliceAIHandler.onPoliceAI()`: neuer Block direkt nach der bestehenden
   Verfolgungs-/Fahrzeugverfolgungs-Logik (nicht versteckt, normale Verfolgung),
   ausgelöst alle 100 Ticks (5 Sekunden, gleiches Intervall wie die bestehende
   "Stop"-Warnung) wenn `POLICE_ROADBLOCK_ENABLED` UND `highestWantedLevel >= 4`:
   neue Methode `computeRoadblockPosition(ServerLevel, ServerPlayer)` berechnet eine
   Position vor dem Spieler in dessen horizontaler Bewegungsrichtung
   (`target.getDeltaMovement()`, normalisiert, 15 Blöcke voraus), Bodenhöhe über
   `level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z)` (gleiches Muster wie
   `RoadBlockDetector`/`MapViewRenderer`). Bewegt sich der Spieler kaum (Geschwindigkeit
   ≤ 0.01² horizontal), wird bewusst **kein** Plakat platziert (`null`-Rückgabe) statt
   eine willkürliche Richtung zu raten — ein stehender Spieler braucht keine Sperre vor
   sich. `PoliceRoadblock.createRoadblock()` übernimmt danach selbst die
   Max-Sperren-pro-Spieler-Prüfung (Config `POLICE_MAX_ROADBLOCKS`) und den
   `POLICE_ROADBLOCK_ENABLED`-Check erneut (doppelt geprüft, aber redundant statt riskant).

**Update (2026-09-25, Teil 14): Straßen-Erkennung ergänzt, aber nur bei Fahrzeugflucht.**
Nutzer-Korrektur: "Das soll natürlich nur Straßenerkennung haben wenn man sich in einem
Fahrzeug befindet sonst macht es doch keinen Sinn!" — zu Fuß ist ein Spieler nicht an
Straßen gebunden (kann über jedes Gelände fliehen), eine Sperre "im Nichts" ist dort
trotzdem sinnvoll; im Fahrzeug wäre eine Sperre abseits der Straße dagegen wirkungslos
(einfach umfahrbar).

**Umsetzung:** Neue private Methode `PoliceAIHandler.isOnRoad(ServerLevel, BlockPos)`
prüft den Block unter der berechneten Sperr-Position gegen
`RoadBlockDetector.isRoadBlock(BlockState)` (aus dem MapView-Modul,
`mapview/navigation/graph/RoadBlockDetector.java`). **Wichtig, verifiziert vor dem
Einbau:** `RoadBlockDetector` hat mehrere Overloads — `isRoadAt(WorldMapData, ...)` und
dessen privates `getBlockStateFromWorld(...)` greifen auf `Minecraft.getInstance()` zu
und sind damit **client-only**; ein Aufruf davon aus dem server-seitigen
`PoliceAIHandler` (läuft in `LivingEvent.LivingTickEvent`, welches auf beiden Seiten
feuert, hier aber im Server-Kontext ausgewertet wird) hätte auf einem Dedicated Server
zu einem `NullPointerException`/Crash geführt. Die tatsächlich genutzten Overloads
`isRoadBlock(Block)`/`isRoadBlock(BlockState)` sind dagegen rein config-basiert
(`ModConfigHandler.COMMON.NAVIGATION_ROAD_BLOCKS`/`NPC_WALKABLE_BLOCKS`, per
`ForgeRegistries.BLOCKS` aufgelöst) und funktionieren auf beiden Seiten identisch —
nur diese werden verwendet.

Der Trigger-Block in `onPoliceAI()` prüft jetzt `PoliceVehiclePursuit.isPlayerInVehicle
(targetCriminal)`: ist der Spieler im Fahrzeug, wird die Sperre nur errichtet wenn
`isOnRoad(...)` true zurückgibt; zu Fuß entfällt die Prüfung komplett (Verhalten wie in
Teil 13). Liegt die berechnete Position bei einer Fahrzeugflucht nicht auf einem
Straßenblock, wird in diesem 5-Sekunden-Zyklus einfach keine Sperre gebaut — die
Verfolgung läuft normal weiter und der nächste Versuch folgt automatisch 5 Sekunden
später an der dann aktuellen Spielerposition.

**Nicht vorschlagen:** die Straßen-Erkennung auch für Fußgänger-Verfolgungen zu
aktivieren, oder `RoadBlockDetector.isRoadAt(...)`/`getBlockStateFromWorld(...)`
(die client-only Overloads) direkt aus server-seitigem Code aufzurufen.

---

## Blitzer-Feature gebaut (2026-09-25/26, Teil 15)

**Status:** ABGESCHLOSSEN — nicht erneut vorschlagen

**Betrifft:** Backlog-Punkt 3 ("Fehlende Features für 12 verbleibende Dead-Config-Werte",
`police.speed_limit_default`).

**Fehlversuch, der verworfen wurde:** Der erste Ansatz interpretierte "Polizei-NPC in
Sichtweite" aus dem ursprünglichen Backlog-Wortlaut wörtlich und baute eine Erweiterung
in `TrafficViolationHandler` (`onPlayerTick`), die eine Verkehrsstrafe nur auslöste, wenn
eine Polizei-NPC laut `PoliceSearchBehavior.isPlayerHidden()` freie Sicht auf den zu
schnell fahrenden Spieler hatte. **Nutzer-Korrektur:** "police.speed_limit_default ist
dafür da um einen Blitzer block an den Straßenrand zu stellen nicht dass Polizisten
'sehen' können wie schnell jemand fährt!" — dieser gesamte Ansatz wurde vollständig
zurückgenommen (Methode, Imports, Lang-Key `event.traffic.speeding` wieder entfernt,
Datei auf den Stand vor Teil 15 zurückgesetzt), da er auf einer falschen Annahme beruhte.

**Neu recherchiert:** Repo-weite Suche nach "blitzer"/"speedcamera"/"radar"/"cctv"
ergab **keinen** existierenden Blitzer-/Kamera-Block irgendeiner Art — dies ist also ein
komplett neues Feature, kein reines Wiring. Vor dem Bau per `AskUserQuestion` geklärt:

1. **Platzierung:** Nutzer-Antwort: "Es soll verschiedene markierte Punkte geben die man
   konfigurieren können soll und dort kann ein Blitzer sein, so wie im echten Leben
   sollen nicht alle Blitzer statisch sein. Der admin/Stadtmauer kümmert sich um
   Platzierung." → Admin platziert den Block physisch (wie ATM/Warehouse-Blöcke), aber
   nicht jeder platzierte Blitzer ist dauerhaft aktiv — eine rotierende Teilmenge ist
   "scharf", der Rest ist Attrappe/inaktiv, genau wie im echten Straßenverkehr.
2. **Konsequenz bei Verstoß:** Sofort-Strafe (identischer `CrimeManager`/`WitnessManager`-
   Pfad wie die bestehenden Kollisions-Verkehrsdelikte in `TrafficViolationHandler`).
3. **Textur:** Vanilla-Block ohne Custom-Textur (Beispiel Observer wegen der
   "Linse"-Optik wurde in der Frage selbst vorgeschlagen und vom Nutzer gewählt).

**Neues Paket `npc/events/speedcamera/`:**
- `SpeedCameraBlock` (extends `HorizontalDirectionalBlock implements EntityBlock`) —
  freistehender, admin-platzierbarer Block (kein Wandmontage wie beim Wanted-Poster).
  FACING ist rein kosmetisch (Blickrichtung beim Platzieren, wie bei `ATMBlock`), die
  Erfassung selbst ist radiusbasiert und richtungsunabhängig — bewusst **keine**
  Fahrspur-/Blickrichtungs-Erkennung, um kein ungetestetes Winkel-/Rotationsrisiko
  einzugehen (Lehre aus dem Roadblock-Feature, Teil 13/14). Rechtsklick zeigt
  Aktiv/Inaktiv-Status als Chat-Nachricht. `setPlacedBy`/`onRemove` registrieren/
  deregistrieren die Position bei `SpeedCameraManager`.
- `SpeedCameraBlockEntity` — tickt (via `getTicker`, Standard-Ticker-Muster wie
  `PerforationPressBlock` u.a.) 1x/Sekunde, aber nur wenn `active == true`: scannt
  einen Radius von 6 Blöcken (`AABB(worldPosition).inflate(6.0)`, Muster aus
  `WitnessManager.java`) nach `ServerPlayer` in einem `EntityGenericVehicle`, vergleicht
  `Math.abs(vehicle.getSpeed())` gegen `POLICE_SPEED_LIMIT_DEFAULT`. Bei Verstoß: gleicher
  `CrimeManager.addWantedLevel(..., CrimeType.TRAFFIC_VIOLATION, ...)` +
  `WitnessManager.registerCrime(...)`-Pfad wie die bestehenden Kollisions-Verkehrsdelikte.
  `active` wird persistiert und an Clients gesynct (`getUpdateTag`/`getUpdatePacket`,
  Standardmuster aus Teil 12).
- `SpeedCameraManager` (extends `AbstractPersistenceManager`, Singleton-Muster wie
  `BountyManager`) — verwaltet **alle** platzierten Blitzer-Positionen
  (`Set<Long>` via `BlockPos.asLong()`/`BlockPos.of(long)`, bereits etabliertes
  Persistenz-Muster aus `SecretDoorMissionAccessManager`/`RemoteControlItem`) und rollt im
  konfigurierten Minuten-Takt (`police.speed_camera_rotation_minutes`) eine neue aktive
  Teilmenge (`police.speed_camera_active_count`) aus — echtes Rotieren, nicht nur ein
  Anzeige-Flag: die jeweiligen `SpeedCameraBlockEntity`-Instanzen werden aktiv
  umgeschaltet. Eigener, von `TrafficViolationHandler`s Kollisions-Cooldown getrennter
  Pro-Spieler-Cooldown (`tryRecordViolation`), damit ein stehendes/langsam vorbeifahrendes
  Fahrzeug im Erfassungsradius nicht mehrfach hintereinander geblitzt wird.
- `SpeedCameraRegistry` — `DeferredRegister` für Block/Item/BlockEntityType, registriert
  in `ScheduleMC`s Konstruktor neben dem Wanted-Poster-Paket.

**Neue Config-Werte** (nicht im ursprünglichen 12er-Backlog enthalten, aber für das vom
Nutzer verlangte Rotations-Verhalten notwendig): `police.speed_camera_active_count`
(Default 3, Range 0–50) und `police.speed_camera_rotation_minutes` (Default 30, Range
5–240), inkl. UI-Einträge in `PoliceConfigScreen`.

**Lifecycle:** `SpeedCameraManager.initialize(server)` in `ScheduleMC.onServerStarted()`
+ Registrierung beim `IncrementalSaveManager` (identisches Muster wie `BountyManager`).
`SpeedCameraManager.tick(server.overworld())` in `ScheduleMC.onServerTick()`, direkt neben
`PoliceRoadblock.tick(...)` — rollt intern selbst, wie oft tatsächlich rotiert wird.

**Assets:** `blockstates/speed_camera.json` (4 Y-Rotationen 0/90/180/270 für
north/east/south/west — bei einem vollen Würfel unproblematisch, anders als beim dünnen
Wanted-Poster in Teil 12 keine Rotations-Risiken), `models/block/speed_camera.json`
(ein Würfel-Element, Texturen `minecraft:block/observer_top`/`observer_side`/
`observer_front`, komplett wiederverwendete Vanilla-Texturen, keine eigene Pixel-Art),
`models/item/speed_camera.json` (referenziert das Block-Modell). Lang-Keys:
`block.schedulemc.speed_camera`, `item.schedulemc.speed_camera`,
`message.speed_camera.status_{active,inactive}`, `event.traffic.speed_camera`.

**Nicht vorschlagen:** eine Fahrspur-/Blickrichtungs-abhängige Erfassung nachzurüsten,
oder die zurückgenommene "Polizei-NPC in Sichtweite"-Interpretation erneut aufzugreifen —
`police.speed_limit_default` gehört ausschließlich zum Blitzer-Feature.

**Update (2026-09-26): Marker-Tool statt Admin-Block-Platzierung, 7-Tage-Rotation statt
Minuten-Config.** Nutzer-Bestätigung des in der vorherigen Antwort vorgeschlagenen Umbaus:
"Ja genau so aber die Position soll sich random alle 7 tage wechseln." Zwei Änderungen:

1. **Trennung von Markierung und physischem Block.** Vorher musste der Admin an JEDEM
   möglichen Standort dauerhaft einen sichtbaren `SpeedCameraBlock` hinstellen (auch wenn
   inaktiv). Jetzt: neues Item `SpeedCameraMarkerItem` (`speed_camera_marker`,
   `useOn(UseOnContext)`, Muster wie `RemoteControlItem`) markiert/entmarkiert bei
   Rechtsklick nur die **Position** bei `SpeedCameraManager` (`registerMarker`/
   `unregisterMarker`) — es wird dabei kein Block platziert. Der Manager selbst
   platziert/entfernt den echten `SpeedCameraBlock` per `level.setBlock(...)` an den
   jeweils aktiven markierten Punkten. Der Blitzer "taucht auf" und "verschwindet" also
   wirklich, statt als Attrappe dauerhaft sichtbar zu sein. `SpeedCameraBlock` hat dadurch
   keine `setPlacedBy`/`onRemove`-Registrierungshooks mehr (das alte
   `SPEED_CAMERA_ITEM`/`BlockItem` bleibt trotzdem registriert und im Creative-Tab — ein
   Admin kann den Block weiterhin auch direkt/dauerhaft platzieren, z. B. für einen
   bewusst permanenten Kontrollpunkt; das ist unabhängig von der Rotation).
2. **Rotation alle 7 Minecraft-Tage statt konfigurierbarer Minuten.** `SpeedCameraManager
   .tick(ServerLevel)` vergleicht jetzt `overworld.getDayTime() / 24000L` gegen
   `lastRotationDay` (persistiert) mit einem fest verdrahteten `ROTATION_INTERVAL_DAYS = 7`
   — bewusst **kein** neuer Config-Wert, analog zur Preisglättung (Teil 3), da der Nutzer
   "alle 7 Tage" konkret benannt hat. `police.speed_camera_rotation_minutes` wieder aus
   `ModConfigHandler`/`PoliceConfigScreen` entfernt; `police.speed_camera_active_count`
   bleibt (beschreibt weiterhin, wie viele der markierten Punkte gleichzeitig aktiv sind).

**Nebeneffekt-Vereinfachung:** Da der Block jetzt nur noch existiert, während er aktiv
ist, wurde das vorherige `active`-Boolean-Feld auf der `SpeedCameraBlockEntity`
(inkl. `setActive`/`isActive`/NBT-Persistenz/Client-Sync) komplett entfernt — die bloße
Existenz des Blocks IST das Aktiv-Signal. Rechtsklick auf einen aktiven Blitzer zeigt
jetzt stattdessen die verbleibenden Tage bis zur nächsten Rotation
(`SpeedCameraManager.getDaysUntilNextRotation(ServerLevel)`).

**Bewusst NICHT umgesetzt:** kein Sicherheits-Check, ob ein markierter Punkt beim
Aktivieren noch frei ist (`placeCameraIfEmpty` überspringt die Platzierung einfach, wenn
dort inzwischen etwas Nicht-Ersetzbares gebaut wurde, statt es zu zerstören) — und beim
Entfernen wird nur abgeräumt, wenn an der Position tatsächlich noch ein
`SpeedCameraBlock` steht (kein blindes Überschreiben von etwas, das ein Spieler
inzwischen dort gebaut hat).

**Nicht vorschlagen:** die 7-Tage-Rotation als Config-Wert aufzubohren, oder die
Markierungs-Trennung zurückzunehmen, ohne dass das explizit gewünscht wird.

---

## Flankieren verdrahtet (2026-09-26, Teil 17)

**Status:** ABGESCHLOSSEN — nicht erneut vorschlagen

**Betrifft:** Backlog-Punkt 4 ("Fehlende Features für 12 verbleibende Dead-Config-Werte",
`police.flanking_enabled`).

**Vorab per `AskUserQuestion` geklärt (drei Entscheidungen, jeweils empfohlene Option
gewählt):**
1. Geltungsbereich: **immer** (Fuß- UND Fahrzeugverfolgung), nicht nur Fahrzeug.
2. Offset-Winkel/-Abstand: **feste Konstanten** im Code, kein neuer Config-Wert.
3. Rollenverteilung: **nächster Verfolger bleibt direkt**, alle weiter entfernten
   flankieren (dynamisch, kein fester Zustand).

**Vorgefundene Datenbasis:** `PoliceBackupSystem.activePolice` (`Map<UUID Spieler,
Set<UUID> Polizei>`) trackt bereits, welche Polizei-NPCs einen Spieler aktuell verfolgen
(`registerPolice`/`getActivePoliceCount`) — nur eine öffentliche Möglichkeit, die
tatsächliche Menge (nicht nur die Anzahl) abzufragen, fehlte. Neue Methode
`PoliceBackupSystem.getAssignedPolice(UUID)` (gibt eine unveränderliche Kopie zurück).

**Umsetzung — neue Methode `PoliceAIHandler.computeFlankingTarget(CustomNPCEntity,
ServerPlayer)`:**
- Gibt `null` zurück, wenn weniger als 2 Verfolger aktiv sind, ODER dieser NPC selbst der
  nächste Verfolger ist (Distanzvergleich gegen alle anderen zugewiesenen Polizei-NPCs im
  bereits bestehenden `policeCache`, kein zusätzlicher World-Scan nötig).
- Andernfalls: Rang unter den ANDEREN, näheren Verfolgern bestimmt Winkel/Seite
  (wechselseitig links/rechts, mit `FLANKING_ANGLE_DEGREES = 50.0` pro weiterem Rang-Paar,
  `FLANKING_DISTANCE = 8.0` Blöcke Abstand vom Ziel).
- Fluchtrichtung: `target.getDeltaMovement()` (horizontal), bei nahezu stehendem Ziel
  Fallback auf die Richtung von diesem NPC zum Ziel (Flankieren soll auch bei stehendem
  Ziel einen sinnvollen Punkt liefern — anders als bei der Straßensperre in Teil 13, wo
  bei Stillstand bewusst KEINE Sperre gebaut wird, weil eine Sperre ohne Bewegungsrichtung
  dort keinen Sinn ergibt; ein Flankier-Punkt um ein stehendes Ziel herum aber schon).

**Fuß-Verfolgung** (`PoliceAIHandler.onPoliceAI()`): der bisherige
`npc.getNavigation().moveTo(targetCriminal, POLICE_SPEED)`-Aufruf nutzt bei aktiviertem
Flankieren und vorhandenem Offset-Punkt jetzt `moveTo(x, y, z, POLICE_SPEED)` auf den
Flankier-Punkt statt direkt auf den Spieler.

**Fahrzeug-Verfolgung** (`PoliceVehiclePursuit`): neue private Methode
`computeDrivingDestination(CustomNPCEntity, ServerPlayer)` liefert beim Start der
Verfolgung (`startVehiclePursuit`) denselben Flankier-Punkt als Fahrziel
(`BlockPos.containing(...)`) statt `target.blockPosition()`. **Wichtige Ergänzung:** Da
`tick()` bereits alle 3 Sekunden prüft, ob sich der Spieler >20 Blöcke bewegt hat, aber
bisher nur Buchhaltung (`lastKnownTargetPos`) aktualisierte, OHNE die Fahrt tatsächlich neu
anzustoßen (Kommentar im Code deutete das als bereits automatisch gelöst an — war es aber
nicht, das Fahrziel blieb der ursprüngliche einmalige Punkt), wurde dieser Effekt jetzt
genutzt: bei Bewegung >20 Blöcke wird der Flankier-Punkt neu berechnet und
`startDrivingToTarget(...)` erneut aufgerufen — sonst hätte ein Flankierer sein einmalig
gesetztes Offset-Ziel nie an eine geänderte Fluchtrichtung angepasst. Dafür neue Methode
`PoliceAIHandler.findPoliceByUUID(UUID)` (sucht im bereits bestehenden `policeCache` nach
der Polizei-NPC-Instanz, kein zusätzlicher World-Scan), da `PoliceVehiclePursuit.tick()`
bisher nur die Spieler-UUID, nicht die Polizei-Entity selbst auflöste.

**Nicht vorschlagen:** die festen Winkel-/Abstands-Konstanten in Config-Werte
umzuwandeln, oder die Rollenverteilung auf eine feste UUID-Reihenfolge statt der
dynamischen Nächster-Verfolger-Logik umzustellen, ohne dass das explizit gewünscht wird.

---

## Sirenensound verdrahtet (2026-09-26, Teil 18)

**Status:** ABGESCHLOSSEN — nicht erneut vorschlagen

**Betrifft:** Backlog-Punkt 5 ("Fehlende Features für 12 verbleibende Dead-Config-Werte",
`police.siren_sound_radius`). Vorgefunden: `POLICE_SIREN_ENABLED`/`setSirenActive()`
steuerten bereits ausschließlich eine **visuelle** Blaulicht-Anzeige (`NPCSirenLayer`,
Client-Renderer) — kein Ton. `police.siren_sound_radius` hatte 0 Lesestellen.

**Sound-Entscheidung (per `AskUserQuestion`):** Vanilla `SoundEvents.RAID_HORN`
zweckentfremdet statt eigenes Asset — keine Audiodatei im Repo, kein Bildgenerierungs-
/Audiogenerierungs-Tool in dieser Umgebung verfügbar.

**Umsetzung:** Neue private Methode `PoliceVehiclePursuit.playSirenSound(CustomNPCEntity)`,
aufgerufen aus `tick()` im bereits bestehenden 3-Sekunden-Takt (`PATH_UPDATE_INTERVAL`) für
jede aktive Fahrzeugverfolgung — unabhängig davon, ob sich der Spieler bewegt hat (anders
als der direkt daneben liegende Pfad-Update-Block, der nur bei Bewegung >20 Blöcke
auslöst). Nutzt `level.playSound(null, x, y, z, RAID_HORN, SoundSource.NEUTRAL, volume,
1.0f)` (etabliertes Muster aus `GunItem`/`GrenadeItem`/`ModSounds`), mit
`volume = max(1.0, radius/16)` — Vanilla-Lautstärke 1.0 entspricht ca. 16 Blöcken Hörweite,
der konfigurierte `siren_sound_radius` (Default 50, Range 10–200) wird darüber linear
skaliert. Bewusst nur bei Fahrzeugverfolgung (wie im Backlog-Text selbst benannt), nicht
bei Fußverfolgung — Sirenen gehören zum Fahrzeug, nicht zum laufenden Polizisten.

**Nicht vorschlagen:** ein eigenes Sirenen-Sound-Asset zu registrieren oder die Sirene auf
Fußverfolgungen auszuweiten, ohne dass das explizit gewünscht wird.
