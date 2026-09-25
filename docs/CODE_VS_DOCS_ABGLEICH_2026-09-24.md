# Code ↔ Dokumentation — Abgleich (2026-09-24)

**Vorgehen:** Zuerst wurde nur der Code analysiert (`src/`, `build.gradle`,
`gradle.properties`, Ressourcen, CI-Workflow). Die Dokumentation wurde dabei
nicht gelesen. Danach wurden die Code-Fakten mit `README.md`, `CLAUDE.md`,
`docs/*.md` und `wiki/**/*.md` verglichen.
Ein lokaler Gradle-Build war nicht möglich, weil Maven Central mit HTTP 429
geantwortet hat. Build- und Testfakten stammen deshalb aus dem letzten CI-Lauf
auf `main` (Run #1769, Commit `0aebcf5`).

Legende: ✅ stimmt · ⚠️ weicht leicht ab / veraltet · ❌ falsch

---

## 1. Code-Fakten (aus dem Code allein ermittelt)

| Bereich | Wert im Code |
|---|---|
| Version / MC / Forge / Java | `3.9.0-beta` / 1.20.1 / 47.4.0 / 17 |
| Java-Dateien | 1 569 main + 41 test = **1 610** |
| LOC | 248 733 main + 9 811 test = **~258,5k** |
| Top-Level-Pakete | 47 (+ `ScheduleMC`, `ModCreativeTabs`, `package-info`) |
| Größtes Paket | `npc` (202 Dateien, ~47,8k LOC) |
| Blöcke (Registry-Namen) | **~148** |
| Items (`ITEMS.register`, ohne Hilfsmethoden-BlockItems) | **~374** |
| BlockEntity-Typen | **113** |
| Menü-Typen | **103** |
| Entity-Typen | 4 |
| Screens (`extends …Screen`) | 148 |
| `*Manager.java` | 62 |
| Netzwerk | 24 `SimpleChannel` + 1 CoreLib-Channel (Vehicle), ≥134 Paket-Registrierungen |
| Public API | **11** `I*API`-Interfaces mit **168** Methoden; `ScheduleMCAPI.VERSION = "3.2.0"` |
| Root-Commands (registriert) | **21**: plot, money, hospital, npc, admintools, warehouse, state, utility, strom, wasser, health, prison, bail, jailtime, map, bounty, market, gang, admin, lock, secretdoor |
| Config | 1 COMMON-Spec (`schedulemc-common.toml`, 166 Keys + 86 punktierte Vehicle-Keys), 1 CLIENT-Spec, `schedulemc-weapons.toml`, CoreLib-Dynamic-Config `fuel` |
| NPCType | CITIZEN, MERCHANT, POLICE, BANK, TOW_TRUCK_DRIVER, BANKER, DRUG_DEALER |
| PlotType | 8 (inkl. INDUSTRIAL) |
| Achievements | 35 (Kategorien: 20 ECONOMY, 6 CRIME, 5 PRODUCTION, 4 SOCIAL; EXPLORATION definiert, aber ungenutzt) |
| Missionen | 34 |
| Producer-Level | MAX_LEVEL = 30, 64 Unlockables |
| Weapon-Items | **28** (6 Schusswaffen, 3 Nahkampf, 3 Granaten, Munition/Magazine/Aufsätze/Upgrades) |
| Smartphone-Apps | **15** (Map, Dealer, Products, Order, Contacts, Messages, Plot, Settings, Bank, CrimeStats, Achievement, Towing, ProducerLevel, Gang, Missions) |
| Tests | 41 Dateien, 681 `@Test` → CI: 610 bestanden, 73 übersprungen, 0 fehlgeschlagen |
| JaCoCo | Bundle-Abdeckung **3 %** (Minimum 60 %) → **CI auf `main` ist rot** |

---

## 2. Probleme im Code selbst (unabhängig von der Doku)

| # | Befund | Beleg |
|---|---|---|
| C1 | **CI ist dauerhaft rot.** `check.dependsOn jacocoTestCoverageVerification` bei 3 % statt 60 % Abdeckung; `util.*` verletzt die 80-%-Regel an ≥18 Klassen. | `build.gradle`, CI-Run #1769 |
| C2 | **MapView-Mixins sind nirgends registriert.** 8 `@Mixin`-Klassen unter `mapview/**/mixins`, aber `schedulemc.mixins.json` listet nur `vehicle.mixins.GuiMixin` und `SoundOptionsScreenMixin`. | `src/main/resources/schedulemc.mixins.json` |
| C3 | **Mixin-Config wird vermutlich gar nicht geladen:** kein `MixinConfigs`-Manifest-Eintrag in `build.gradle`, kein MixinGradle-Plugin. `vehicle/MixinConnector` verweist auf ein nicht existierendes `vehicle.mixins.json` und ist selbst nirgends registriert. | `build.gradle` (jar-Manifest), `vehicle/MixinConnector.java:10` |
| C4 | **`/crimerecord` wird nie registriert.** `CrimeRecordCommand` existiert, fehlt aber in `ScheduleMC.onRegisterCommands()`. | `ScheduleMC.java:433-458` |
| C5 | **Verwaiste Ressource** `src/main/resources/schedulemc-server.toml` („Version 3.0.0“, z. B. `start_balance = 10000`). Kein Code liest die Datei; der echte Default ist 1000. | `ModConfigHandler.java:284` |
| C6 | **Config-Default verweist auf nicht existierende Blöcke:** `coffee_*_pot`, `small_coffee_drying_tray` usw. | `ModConfigHandler.java:419-420` |
| C7 | **Mission-IDs sind deutsch** (`haupt_erster_kontakt`, `neben_geldwaescher` …). Das widerspricht der Sprachkonvention in `CLAUDE.md` („Alles im Code ist Englisch“). | `mission/MissionRegistry.java` |
| C8 | **API-Version divergiert:** `ScheduleMCAPI.getVersion()` liefert `"3.2.0"`, `PlotModAPI` `"3.0"`. | `api/ScheduleMCAPI.java:45` |
| C9 | **`ISmartphoneAPI.registerApp()` hat keine Wirkung:** Die Home-Grid-Einträge in `SmartphoneScreen` sind hart kodiert (Indizes 0–14); registrierte Apps werden nie angezeigt. | `SmartphoneScreen.java:226-241` |
| C10 | JEI/Jade/TOP sind nur `compileOnly`, es gibt **keine einzige** Integrationsklasse (0 Referenzen auf `mezz.jei`, `snownee.jade`, `mcjty.theoneprobe`). | `build.gradle`, `src/` |
| C11 | `scripts/run_gradle_tests.sh` wählt lokal JDK 21, wenn kein JDK 17 gefunden wird. | lokaler Lauf |

---

## 3. Abgleich mit `CLAUDE.md`

| Aussage | Status | Code |
|---|---|---|
| NPCData 971 → 409 Zeilen, Aufteilung in Location/Shop/Schedule/Police + ShopInventory/ShopEntry | ✅ | 409 Zeilen, alle Klassen vorhanden |
| CustomNPCEntity 968 Zeilen | ⚠️ | 971 |
| Beer/Wine-Subklassen ~43 Zeilen | ✅ | 43 |
| Tobacco DryingRack/FermentationBarrel „vollständig eliminierbar“ | ⚠️ | Subklassen existieren weiter (18 bzw. 36 Zeilen) und nutzen nur das Supplier-Pattern |
| Dimension-Switch in `PlotAppScreen.renderCurrentPlotTab()` | ✅ | vorhanden (Zeile ~472) |
| **Cannabis-Interaktion:** RK = füllen, Shift+RK = entnehmen (HashPress, OilExtractor, TrimStation, CuringJar) | ❌ | Alle 4 Blöcke öffnen bei **jedem** Rechtsklick die GUI (0 Shift-Abfragen). Befüllen und Entnehmen laufen über Slot-Klicks im Menü (`*Menu.clicked()`). |
| Kein ItemStackHandler bei Cannabis | ✅ | render-only Slots über `SimpleContainer`-Dummy |
| NPCType-Duplikate zusammengeführt (BEWOHNER→CITIZEN …) | ✅ im Code | ❌ in der Wiki, siehe §4.5 |
| Keine deutschen User-facing-Strings, Guard-Skript | ✅ | `check-german-strings.sh`: OK |
| „Dokumentationsstatus 2026-04-17“ | ⚠️ | veraltet |

Dasselbe veraltete Cannabis-Interaktionsmodell steht auch in `docs/CHANGELOG.md` (Abschnitt 3.7.2-beta).

---

## 4. Abgleich README / docs / wiki

### 4.1 Kennzahlen — die Dokumente widersprechen sich gegenseitig

| Kennzahl | Code | Doku-Werte (Fundstelle) |
|---|---|---|
| Java-Dateien | 1 610 | 1 610 (README-Highlights, Home, ARCHITECTURE) ✅ · **1 522 + 39** (README „Project Statistics“, REPO_METRICS vom 2026-04-13) ❌ · **1 419** (README-Strukturbaum) ❌ · 32 Testdateien (PROJECT_STRUCTURE) ❌ |
| Blöcke | ~148 | **152** (README, Blocks.md) · **125** (Home-Linktabelle) · **97** (Home-Metriktabelle) |
| Items | ~374 | **354** (README) · **353** (Items.md, Home) · **399** (Home-Linktabelle) · „250+“ (Items.md:863) |
| BlockEntity-Typen | 113 | 111 (Home) ⚠️ |
| Root-Commands | 21 (+1 unregistriert) | **25** (Home, Commands-Link) ❌; „132 Literals“ ✅ |
| Weapon-Items | 28 | **26** (VERSION.md) · **29** (Weapon-System.md) · 28 (CHANGELOG) ✅ |
| Weapon-Typen | 6 Guns + 3 Melee + 3 Granaten = 12 | **13, „4 melee“** (Home) ❌ |
| Vehicle-Dateien | 114 | **137** (README) ❌ |
| NPC-AI-Goals | 5 Goal-Klassen, 9 `addGoal`-Aufrufe | „9 AI goals“ (README) ⚠️ |
| Achievement-Kategorien | 4 genutzt + EXPLORATION ungenutzt | „4 used categories: Production, Economy, Social, Crime, **and Exploration**“ (README) — nennt 5 ❌ |
| Smartphone-Apps | **15** | **11** (Smartphone-System.md), Home-Grid-Grafik mit nicht existierender „CHAT“-App ❌ |
| Produktionsmodule | 14 Module, **623** Dateien | 14 ✅, „607 files“ (ARCHITECTURE) ⚠️ |
| Tests | 681 in 41 Dateien | ✅ (Home) |
| API-Version | `"3.2.0"` | API_REFERENCE:110 behauptet, `getVersion()` liefere `"3.9.0-beta"` ❌ |

Chain-Zahlen im README (Items/Blöcke je Kette) weichen teils ab, z. B.
Tobacco 32/23 statt 26/21, Coca 9 statt 12 Items, Mushroom 15 statt 12 Items,
Cannabis 10/9 statt 9/8. Meth, LSD und MDMA stimmen.

### 4.2 Commands (`wiki/Commands.md`, Feature-Seiten)

Dokumentiert, im Code aber **nicht vorhanden oder nicht registriert**:

| Doku | Realität |
|---|---|
| `/crimerecord …` (Commands.md:2533 ff.) | Klasse existiert, wird nicht registriert (C4) |
| `/level set/addxp/reset/info` (Level-System.md:248) | Existiert nicht; im Code: `/admin setlevel / addxp / getlevel`; `reset` fehlt ganz |
| `/towing addspot/removespot/listspots/revenue/clearinvoices/setmembership/listinvoices` (Towing-System.md:347 ff.) | Kein `/towing`-Command |
| `/autopay resume`, `/loan repay`, `/savings forcewithdraw/close` (Economy-System.md) | Existieren nicht |
| `/achievement reset` (Achievement-System.md:515) | Existiert nicht |
| `/scenario edit` (Mission-System.md:283) | Existiert nicht |
| Commands.md:570: `/npc` akzeptiere `BEWOHNER`, `VERKAEUFER`, `POLIZEI`, `ABSCHLEPPER` als Aliase | Keine Aliase mehr (siehe `CLAUDE.md`) |

Positiv: Alle 21 registrierten Root-Commands sind in `Commands.md` beschrieben,
ebenso die Subcommands von `/npc`, `/gang`, `/lock`, `/warehouse`, `/money`,
`/health`, `/prison`, `/utility`, `/bounty`, `/hospital`, `/state`,
`/secretdoor` und `/map`. Die ehemaligen `/plot buy|sell|trust…` sind korrekt
als „Former Command“ gekennzeichnet.

### 4.3 Konfiguration (`docs/CONFIGURATION.md`)

- ❌ **Falsche Defaults:** `tobacco.drying_time` steht dort mit 6000, im Code ist es **750**. `tobacco.fermenting_time` steht mit 12000, im Code ist es **500** (`TobaccoConfig.java:66/70`).
- ❌ **`schedulemc-weapons.toml` fehlt** in der Dateiliste, und alle Waffen-Keys sind undokumentiert (`pistol_range`, `ak47_range` …).
- ⚠️ **52 Keys undokumentiert**, u. a.:
  - Police: `roadblock_*`, `siren_*`, `vehicle_pursuit_enabled`, `traffic_violations_enabled`, `speed_limit_default`, `wanted_posters_min_level`, `flanking_enabled`, `evidence_multiplier_enabled`
  - Utility: `electricity_price_per_kwh`, `water_price_per_liter`
  - Topf-Kapazitäten: `*_soil`, `*_water`
  - Workshop-Upgradekosten: `*_upgrade_cost_lvl*`, `paint_change_cost`
  - `secret_door_allowed_plot_types`, `enable_debug_logging`
- Der Rest (~120 Keys und alle punktierten Vehicle-Keys) existiert und hat passende Defaults ✅.

### 4.4 Tests (`docs/TESTING.md`)

- ❌ **16 dokumentierte Testklassen existieren nicht** (ohne die Vorlagen `MyManagerTest`/`UnderTest`): `ConfigCacheTest`, `CreditScoreManagerTest`, `EventBusTest`, `GangLevelTest`, `GangMissionsTest`, `InterestManagerTest`, `NPCCrimeTest`, `NPCEmotionTest`, `PlantGrowthTest`, `ProductionSerializationTest`, `ProductionSizesTest`, `SavingsAccountManagerTest`, `ThreadPoolManagerTest`, `ValidationUtilTest`, `VehicleFuelTest`, `VehicleTireTest`.
- ⚠️ **26 vorhandene Testklassen fehlen** in der Doku, z. B. `PlotManagerTest`, `PlotSpatialIndexTest`, `RoadGraphTest`, `WarehouseSlotTest`, `TobaccoQualityTest`, `PlayerMissionTest`.
- ❌ „60 % / 80 % Coverage werden erzwungen“: Die Regel existiert, wird aber verfehlt (3 %), und CI ist deshalb rot (C1).

### 4.5 NPC-System (`wiki/features/NPC-System.md`, `Commands.md`)

- ❌ Die Wiki nutzt weiter `BEWOHNER`, `VERKAEUFER`, `POLIZEI` als Typnamen und Überschriften. Das Codebeispiel `NPCType.VERKAEUFER` (NPC-System.md:787) kompiliert nicht. Commit `ff2a3e2` („align NPC types“) hat das nur teilweise umgesetzt.
- ⚠️ `TOW_TRUCK_DRIVER` kommt in der gesamten Doku nur einmal vor.

### 4.6 API (`docs/API_REFERENCE.md`)

- ✅ Alle 11 Module sind beschrieben, und keine dokumentierte Methode fehlt im Code.
- ⚠️ **69 von 168 Interface-Methoden sind undokumentiert**, z. B.:
  - `IEconomyAPI.batchTransfer/getTopBalances`
  - `IPlotAPI.setPlotType/addTrustedPlayer`
  - `IPoliceAPI.isImprisoned/releaseFromPrison`
  - `ISmartphoneAPI.registerApp` (ohnehin wirkungslos, C9)
  - `IVehicleAPI.repairVehicle`
- ❌ `getVersion()` liefert `"3.2.0"`, nicht `"3.9.0-beta"`.

### 4.7 Struktur- und Architekturdokumente

- ❌ **`docs/PROJECT_STRUCTURE.md` (Stand 2026-03-17)** listet **126 Java-Dateien, die es nicht mehr gibt** (fast alle mit deutschen Namen vor der Englisch-Umbenennung: `ChemieMixerBlock`, `CuringGlasBlock`, `BoerseScreen`, `BlockWerkstatt` …). **256 existierende Dateien fehlen.**
- ❌ **MapView-Mixins:** `MapView-System.md:380` sagt, sie seien in `schedulemc.mixins.json` deklariert. Das stimmt nicht (C2). DEVELOPER_GUIDE:123 („Mixin … MapView integration“) ist ebenso irreführend.
- ❌ **JEI/Jade/TOP:** README:4709 und FAQ versprechen automatische Integration. Es gibt keinen Integrationscode (C10).
- ✅ ARCHITECTURE: „16 managers parallel geladen“ stimmt (16 Tasks in `onServerStarted`). 11 API-Impls ✅.

### 4.8 Wiki-Items / -Blocks

- ⚠️ `Items.md` führt ~61 IDs, die keine registrierten Items sind:
  - Fahrzeugteile aus der internen `PartRegistry` (`limousine_chassis`, `normal_motor`, `fender_*`, `cargo_module` …); das sind keine Items.
  - Nicht existierende Produktgrößen (`cheese_wedge`, `gouda_wheel`, `chocolate_bar_100g`, `honey_jar_1kg`, `empty_wine_bottle_750ml` …).
- ⚠️ `Blocks.md` führt 7 nicht existierende Coffee-Blöcke (`coffee_*_pot`, `*_coffee_drying_tray`).

### 4.9 Formale Doku-Defekte

- ❌ `wiki/features/Plot-System.md:108-115`: Die Tabellenzeilen PRISON, TOWING_YARD und INDUSTRIAL sind **4× dupliziert** (kaputter Merge oder Generator-Fehler).
- ⚠️ `docs/VERSION.md` behauptet „Verified against source (2026-09-24)“ mit **26** Weapon-Items. Der Code hat 28, und der CHANGELOG vom selben Tag sagt ebenfalls 28.
- ⚠️ `docs/REPO_METRICS.md` ist vom 2026-04-13 (1 522/39/249 349) und wird im README als „Live-Metriken“ referenziert. Das Skript läuft nur in der CI und schreibt nicht zurück.
- ⚠️ `update.json` und README sind konsistent mit `3.9.0-beta` ✅. Dagegen tragen `schedulemc-server.toml` („3.0.0“) und `PlotModAPI` („3.0“) alte Stände.

---

## 5. Empfohlene Reihenfolge

1. **CI grün bekommen:** die JaCoCo-Schwelle realistisch setzen (oder `check.dependsOn` entfernen) und `TESTING.md` anpassen.
2. **Mixins klären:** entweder `MixinConfigs` im Manifest plus MapView-Mixins in `schedulemc.mixins.json` eintragen, oder toten Mixin-Code und `MixinConnector` entfernen. Danach MapView-Doku korrigieren.
3. **`CrimeRecordCommand` registrieren** oder löschen, dazu Commands.md anpassen.
4. **Phantom-Commands** (`/level`, `/towing`, `/loan`, `/savings`, `/autopay`, `/achievement`, `/scenario`) aus der Wiki streichen oder implementieren.
5. **`CLAUDE.md`- und CHANGELOG-Abschnitt „Cannabis-Interaktionsmodell“** auf „RK öffnet GUI, Slot-Klicks füllen/entnehmen“ korrigieren.
6. NPC-Wiki auf CITIZEN/MERCHANT/POLICE/TOW_TRUCK_DRIVER umstellen.
7. `PROJECT_STRUCTURE.md` neu generieren oder löschen. Die Kennzahlen (Blöcke/Items/Commands/Apps/Waffen) aus **einer** generierten Quelle ziehen, statt sie in 5 Dokumenten zu pflegen.
8. `CONFIGURATION.md`: Tobacco-Defaults korrigieren, `schedulemc-weapons.toml` und die 52 fehlenden Keys ergänzen. `schedulemc-server.toml` aus den Ressourcen entfernen.

---

## 6. Reichweite dieses Abgleichs — Nachtrag

Der erste Durchgang (Abschnitte 1–5) war **stichprobenbasiert**: grep-Muster auf
Zahlenbehauptungen, Commands, Config-Keys und API-Methoden — kein
zeilenweiser Vergleich. Auf Nachfrage wurde ein zweiter, gezielterer
Durchgang gemacht: Gang-Level-Formel, Lock-Typen, Towing-Mitgliedschaften,
Vehicle-Chassis/Motoren/Reifen sowie eine Klassennamen-Suche über **alle
1 569 Hauptklassen** gegen den gesamten Dokutext (README, CLAUDE.md, alle
`docs/*.md`, alle `wiki/**/*.md`).

**Zur Ausgangsfrage „ist jede Zeile doku-belegt?":** Nein, und das ist bei
258 500 Zeilen auch kein sinnvoller Maßstab — Dokumentation beschreibt
Verhalten und Schnittstellen, nicht Zeilen. Realistischer ist die Frage, ob
jedes **Feature** irgendwo erwähnt ist. Auch das ist nicht der Fall:

| # | Befund | Beleg |
|---|---|---|
| D1 | **Komplettes In-Game-Config-Editor-System ist in keiner Doku erwähnt.** 17 Screen-Klassen unter `client/gui/config/` (4 566 Zeilen: `EconomyConfigScreen`, `PoliceConfigScreen`, `TobaccoConfigScreen`, `PlotConfigScreen`, `WarehouseConfigScreen`, `DynamicPricingConfigScreen`, `StealingConfigScreen` u. a.), erreichbar über den regulären Forge-„Config"-Button im Mod-Menü (`ScheduleMC.java:341-343`, `ConfigScreenHandler`). Weder README, docs/ noch wiki/ erwähnen, dass es diese grafische Konfigurationsoberfläche überhaupt gibt. | `client/gui/ConfigScreen.java`, `ScheduleMC.java:341-343` |
| D2 | **„Redemption Quest"-System (Reputations-Vergebung) ist komplett undokumentiert.** `npc/life/quest/RedemptionQuestManager.java` (229 Zeilen): Spieler können bei Fraktions-Reputation < -20 automatisch Quests (Sozialdienst, Geldstrafe, Kurierdienst, Wachpatrouille) erhalten und so +15 Reputation zurückgewinnen. Null Treffer für „Redemption" oder „Vergebungs-Quest" in README/docs/wiki. | `npc/life/quest/RedemptionQuestManager.java:14-24` |
| D3 | **Faction-System (CITIZENS/TRADERS/LAW/UNDERWORLD) wird nur erwähnt, nie erklärt.** Taucht in `README.md` nur als Health-Check-Name (`/health faction`) und im Dateibaum auf; keine Doku beschreibt Reputationswerte, Schwellen oder Auswirkungen. | `npc/life/social/Faction.java` |
| D4 | **Reifentypen:** README nennt „6 tire types", der Code registriert **7** (`standard`, `sport`, `premium`, `offroad`, `allterrain`, `heavyduty`, `winter`). | `vehicle/entity/vehicle/parts/PartRegistry.java` |

Bestätigt (keine Abweichung gefunden): Gang-Level-Formel (200·Level^1,7,
Max-Level 30, 656 059 XP, 20 Mitglieder-Cap), 5 Lock-Typen, 3
Towing-Mitgliedschaftsstufen mit Config-gestützten Gebühren/Deckungen,
5 Vehicle-Chassis-Typen, 3 Motoren (inkl. `PERFORMANCE_2_MOTOR`, den README
korrekt mitzählt).

**Bekannte Lücken auch in diesem Nachtrag:** Die Klassennamen-Suche markiert
~230 weitere Klassen als „Name kommt im Dokutext nicht vor" (v. a. in
`client`, `poppy`, `meth`, `vehicle`, `npc`) — das sind größtenteils interne
Bausteine (Packets, Screens, BlockEntities einzelner Produktionsstufen), die
über die jeweilige Feature-Seite thematisch, aber nicht namentlich abgedeckt
sind (z. B. deckt `Poppy-System.md` die Opium/Heroin-Kette ab, nennt aber
nicht `OpiumPressBlockEntity.java` beim Klassennamen). Diese Liste wurde
nicht einzeln geprüft; ein vollständiger, funktionaler Check jeder dieser
Klassen gegen die jeweilige Feature-Doku steht noch aus.

---

## 7. Vollständiger Abgleich der 166 namentlich unauffindbaren Klassen

Alle 166 Klassen aus dem Nachtrag in §6 wurden jetzt einzeln geprüft: nicht
nur, ob der Klassenname im Dokutext vorkommt, sondern ob die **Funktion**
dahinter irgendwo beschrieben ist (z. B. unter dem deutschen Blocknamen, der
Registry-ID oder einer Konzeptbeschreibung).

### 7.1 Ergebnis: größtenteils unbegründete Treffer

**~150 der 166 Klassen sind inhaltlich abgedeckt**, nur der exakte
Klassenname taucht nicht auf. Beispiele:
- `meth/*`, `poppy/*`, `lsd/*`, `mdma/*`, `coca/*`, `mushroom/*` (Block-,
  BlockEntity-, Menu-, Screen- und Item-Klassen der jeweiligen Produktkette):
  Alle Maschinen und Zutaten (Chemical Mixer, Crystallizer, Reduction Kettle,
  Vacuum Dryer, Ephedrine, Iodine, Pseudoephedrine … / Cooking Station,
  Heroin Refinery, Scoring Machine, Opium Press / Distillation Apparatus,
  Micro-Doser („Micro Dosing Station"), Perforation Press, Blotter Paper,
  Ergot(-Culture), Lysergic Acid / Drying Oven, Pill Press, Reaction Kettle,
  Binding Agent, Safrole / Crack Cooker, Extraction Vat, Refinery, Backpulver
  („Baking Powder"/„baking soda") / Climate Lamp, Water Tank, Manure) sind in
  den jeweiligen `wiki/production/*.md`-Seiten beschrieben — nur unter
  eigener Formulierung statt Klassenname.
- `tobacco/menu`, `tobacco/screen` (Fermentation-Barrel-Menüs/-Screens),
  `cannabis/menu`, `cannabis/screen`: funktional durch `Tobacco-System.md`
  bzw. `Cannabis-System.md` abgedeckt.
- `production/growth/CoffeeGrowthHandler`, `GrapeGrowthHandler`,
  `production/nbt/CoffeePlantSerializer`, `GrapePlantSerializer`: interne
  Wachstums-/Serialisierungslogik, die Ketten selbst (Coffee/Wine) sind
  dokumentiert.
- `weapon/upgrade/*FireModeUpgradeItem`: **doch dokumentiert** —
  `Weapon-System.md` Abschnitt „Fire-Mode Upgrades" beschreibt Single
  Precision, Burst Fire und Auto Fire vollständig.
- `vehicle/gui/GuiWorkshop`, `ContainerWorkshop`, `TileEntityWorkshop`: Das
  Workshop-Feature selbst ist dokumentiert.
- `managers/TutorialManager`: `Tutorial-System.md` existiert und deckt das
  Feature ab.
- `npc/entity/component/*` (Trading/Driving/ActivityTracking-Component),
  `npc/life/dialogue/DefaultDialogueTrees`: interne Bausteine des bereits
  dokumentierten NPC-Verhaltens/Dialogsystems.
- `util/CircuitBreaker`, `ModConstants`, `MoneyFormat`, `SmokeEmitter`,
  `UUIDHelper`: reine interne Hilfsklassen ohne eigenen Doku-Anspruch.

### 7.2 Echte, bisher übersehene Lücken (neu in diesem Durchgang)

| # | Befund | Beleg |
|---|---|---|
| E1 | **Reifenwechsel-Mechanik am Fahrzeug-Workshop ist nirgends dokumentiert.** `GuiTireChange`, `MessageTireSwap`, `MessageOpenTireChange` sowie das Werkzeug dafür (`ItemCarJack`, Registry-ID `wagon_jack`) — `Vehicle-System.md` beschreibt den Workshop, aber nicht den eigenständigen Reifenwechsel-Screen/-Ablauf. | `vehicle/gui/GuiTireChange.java`, `vehicle/net/MessageTireSwap.java`, `vehicle/items/ItemCarJack.java` |
| E2 | **Fan-Blöcke (3 Stufen) + Multiblock-Booster-Mechanik sind als Feature nirgends beschrieben.** `fan/blocks/FanBlocks` registriert `fan_tier1/2/3`; `multiblock/MultiblockHelper` + `IMultiblockBooster` lassen Fans benachbarte Trocknungs-/Fermentationsblöcke beschleunigen (genutzt von `AbstractDryingRackBlockEntity`, `FanBlock`, `TallFanBlock`). Einzige Erwähnung überhaupt: eine interne Implementierungs-Notiz (`docs/CLAUDE_IMPLEMENTATION_BRIEF.md`), keine Nutzer-Doku. | `fan/blocks/FanBlocks.java`, `multiblock/MultiblockHelper.java` |
| E3 | **OptiFine-Kompatibilitätsschicht der MapView ist nirgends erwähnt.** `mapview/service/render/OptiFineColorLoader.java` liest OptiFine-Biome-Tint-Properties zur Laufzeit, wird geprüft (`optifineInstalled`) und beeinflusst das Rendering. `MapView-System.md` und die README-Liste optionaler Mods (JEI/Jade/TOP) erwähnen OptiFine nicht. | `mapview/service/render/OptiFineColorLoader.java` |
| E4 | **Saisonale Preisschwankungen (`SeasonalPriceModifier`) sind nirgends dokumentiert.** Wird von `DynamicPriceManager` genutzt, um Marktpreise nach Jahreszeit zu modifizieren. (Korrektur 2026-09-25: der ursprüngliche Eintrag nannte hier fälschlich auch `ProductionEventManager` — dieser referenzierte `SeasonalPriceModifier` nie und wurde inzwischen als vollständig toter Code entfernt, siehe `CLAUDE.md`.) Weder `Economy-System.md` noch `Market-System.md` erwähnen eine saisonale Komponente. | `market/SeasonalPriceModifier.java`, `npc/life/economy/DynamicPriceManager.java:144,291` |
| E5 | **Secret-Doors ↔ Mission-Integration ist in beiden betroffenen Feature-Seiten unerwähnt.** `secretdoors/mission/SecretDoorMissionAccessManager` gewährt temporären Zugriff auf `SecretDoorBlockEntity`/`HiddenSwitchBlockEntity` über aktive Mission-Objectives (`PlayerMissionScenarioExecutor`, `ScenarioObjective`); `SecretCodeGenerator`/`SecretBlockRegistry` verwalten dafür generierte Codes. Weder `Secret-Doors-System.md` noch `Mission-System.md` erwähnen diese Kopplung. | `secretdoors/mission/SecretDoorMissionAccessManager.java:14-20` |
| E6 | *(gering)* **`DialogueConsequenceSystem` (261 Zeilen)** — Dialogentscheidungen wirken sich auf Reputation/Beziehungen aus. `NPC-System.md` nennt nur pauschal „Social interactions", ohne diesen Mechanismus zu erklären. | `npc/life/dialogue/DialogueConsequenceSystem.java` |

### 7.3 Nebenbefund: deutsche Maschinennamen in aktueller Doku (Sprachkonvention)

Beim funktionalen Abgleich fiel auf, dass mehrere **aktuelle** Wiki-Seiten
(nicht nur die bereits als veraltet markierte `PROJECT_STRUCTURE.md`) noch
deutsche Maschinennamen in der Prosa verwenden, obwohl der Code seit dem
2026-06-10-Commit vollständig auf Englisch umgestellt ist:

- **„Crack Kocher"** statt „Crack Cooker" (Registry-ID `crack_cooker`) — **8
  Vorkommen** in `wiki/production/Coca-System.md` (4×), `wiki/Blocks.md` (2×),
  `wiki/Production-Systems.md` (1×), `wiki/Items.md` (1×).
- **„Pillen Presse"**, **„Reaktions Kessel"**, **„Trocknungs Ofen"** in
  `wiki/production/MDMA-System.md` neben den korrekten englischen Namen
  (Pill Press, Reaction Kettle, Drying Oven).
- Insgesamt verwenden **8 von 14** `wiki/production/*.md`-Seiten
  (Beer, Cannabis, Coca, Coffee, Honey, LSD, MDMA, Poppy) noch mindestens
  einen deutschen Maschinennamen (Presse/Kessel/Kocher/Ofen/Extraktor/Fass/
  Gestell/Trocknungs-) im Fließtext.

Das ist kein Code-Defekt (`CLAUDE.md`s Sprachkonvention gilt für Code, nicht
für deutsche UI-Texte), aber ein Doku-Inkonsistenz-Muster: Innerhalb
derselben Seite wechseln englische Registry-/Item-Namen und deutsche
Maschinenbezeichnungen unvermittelt.

### 7.4 Fazit zum vollständigen Abgleich

Von 166 zunächst als „Name nicht in Doku" markierten Klassen sind:
- **~150 funktional abgedeckt** (nur Klassenname weicht von der Doku-Formulierung ab — kein Mangel),
- **6 Klassen/Features komplett oder größtenteils undokumentiert** (E1–E6),
- **zusätzlich ein sprachliches Konsistenzproblem** (§7.3) über 8 Wiki-Seiten hinweg.

Damit ist der Abgleich für diese 166 Klassen abgeschlossen. Er deckt alle
Klassen ab, deren Name nirgends im Dokutext vorkam; er deckt **nicht** ab,
ob jede bereits benannte Klasse auch in allen Details (Parameterwerte,
Edge-Cases, genaue Formeln) korrekt beschrieben ist — das würde einen
Methoden-für-Methoden-Vergleich erfordern, der über den Rahmen dieser
Anfrage hinausgeht.
