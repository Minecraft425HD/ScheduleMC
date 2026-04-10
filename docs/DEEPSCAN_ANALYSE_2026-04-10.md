# ScheduleMC DeepScan (Stand: 10. April 2026)

## Methodik

Dieser DeepScan basiert auf einer statischen Vollanalyse der Repository-Struktur, Build-Konfiguration und Quellcode-Indikatoren (Größe, Komplexitäts-Heuristiken, Fehler-/Wartungsgeruchsmuster).

Ausgeführte Kernchecks:

- Dateibestand und Modulverteilung (`rg --files`, Paketaggregation via Python)
- Größenanalyse (`wc -l`, Top-Dateien nach LOC)
- Risiko-Marker (`TODO|FIXME|HACK|XXX`)
- Wartungsgerüche (`catch (...) {}`, `Thread.sleep`, manuelle Thread-Erstellung)
- Build-/Test-Fähigkeit (`./gradlew test --no-daemon`)
- Architekturdokumentation/README-Konsistenz

## Executive Summary

- **Codebase-Größe ist sehr hoch**: 1.522 Main-Java-Dateien + 39 Testdateien; ca. **248.945 Zeilen** in `src/main/java` + `src/test/java`.
- **Monolithische Hotspots** dominieren mehrere Kernsysteme (MapView, UI-Screens, Commands, Vehicle/NPC) mit bis zu ~1.700 LOC pro Datei.
- **Build/Test ist im aktuellen Environment blockiert** (Gradle-Semantic-Analysis-Fehler: `Unsupported class file major version 69`).
- **Viele stille Fehlerbehandlungen** (`catch (...) {}` mit `ignored`) erschweren Debugging/Observability.
- **Dokumentationsdrift**: README-/Architektur-Metriken passen nicht vollständig zur tatsächlichen Codebase.
- **Repository-Hygiene**: temporäre/Backup-Artefakte und mindestens eine offensichtlich unplatzierte Java-Datei im Repo-Root gefunden.

## 1) Strukturanalyse

### 1.1 Größen- und Bestandsdaten

- Main-Java-Dateien: **1.522**
- Test-Java-Dateien: **39**
- Gesamt-LOC (main+test): **248.945**

### 1.2 Größte Module (Dateianzahl)

Top-Module nach Anzahl Java-Dateien:

1. `npc` (202)
2. `mapview` (125)
3. `vehicle` (114)
4. `tobacco` (82)
5. `chocolate` (69)
6. `economy` (66)
7. `wine` (63)
8. `beer` (62)

**Interpretation:** Fachlich sehr breit, mit hoher Kopplungswahrscheinlichkeit zwischen Gameplay-, Economy- und UI-Subsystemen.

### 1.3 Monolithische Dateien (Top-Länge)

Auffällige große Klassen:

- `MapViewRenderer.java` (~1696 LOC)
- `WorldMapScreen.java` (~1520 LOC)
- `PlotCommand.java` (~1495 LOC)
- `ScenarioEditorScreen.java` (~1393 LOC)
- `WarehouseScreen.java` (~1389 LOC)
- `NPCCommand.java` (~1247 LOC)
- `EntityGenericVehicle.java` (~1220 LOC)

**Risiko:** Hohe Änderungsrisiken, erhöhte Defect-Rate bei Feature-Erweiterungen, erschwerte Testbarkeit.

## 2) Qualitäts- und Wartungsbefunde

### 2.1 Build-/Test-Fähigkeit

`./gradlew test --no-daemon` schlägt fehl mit:

- `Unsupported class file major version 69`

**Bewertung:** Kritisch für CI/Regression-Sicherheit. Wahrscheinlich JDK-/Gradle-/Groovy-Kompatibilitätsproblem im Environment.

### 2.2 Error-Handling-Geruch (stille Exceptions)

Es wurden viele Stellen mit `catch (IllegalArgumentException ignored) {}` und verwandten Mustern gefunden (u. a. in Honey-, Coffee-, Wine-, Coca-, Tobacco-, NPC- und MapView-Komponenten).

**Bewertung:** Mittel bis hoch. Das Muster verhindert Fehlerdiagnose im Betrieb und kann Daten-/State-Inkonsistenzen maskieren.

### 2.3 Threading-Indikatoren

- `Thread.sleep(...)` in Tests und in produktivem Pfad (`PlotManager` Retry-Abschnitt)
- explizite `new Thread(...)`-Verwendung (u. a. `ThreadPoolManager`, `HotReloadableConfig`)

**Bewertung:** Mittel. Kann zu Timing-Flakiness (Tests) bzw. schwer reproduzierbarem Verhalten im Runtime-Betrieb führen, wenn nicht zentral orchestriert.

### 2.4 Potenziell inkonsistente Repo-Hygiene

Gefundene Artefakte:

- Sprachdatei-Backups/Tempfiles in `src/main/resources/assets/schedulemc/lang/` (`*.tmp`, `*.FULL_BACKUP`)
- Root-Datei `AbstractSecretDoorBlock.java` enthält offensichtlich **Platzhalter-/Snippet-Inhalt** und liegt außerhalb der üblichen `src/main/java`-Struktur.

**Bewertung:** Niedrig bis mittel (direktes Runtime-Risiko gering), aber negatives Signal für Release-/Review-Hygiene.

## 3) Dokumentationsdrift

Die in `README.md`/`docs/ARCHITECTURE.md` genannten Metriken (Datei-/LOC-Zahlen) weichen von den aktuell gemessenen Werten ab.

**Risiko:** Onboarding-/Planungsfehler, falsche Erwartung an Testabdeckung und Wartungsaufwand.

## 4) Priorisierte Handlungsempfehlungen

### P0 (sofort)

1. **Build-Fix für Testpipeline**
   - CI-Java-Version und lokale Toolchain harmonisieren (Projekt target ist Java 17).
   - Gradle/Groovy-Kompatibilität gegen laufendes JDK verifizieren.
2. **Release-Hygiene absichern**
   - `.tmp`/`.FULL_BACKUP` und Root-Snippets aufräumen oder sauber ignorieren.

### P1 (kurzfristig)

1. **Exception-Handling-Policy einführen**
   - Keine stummen catches in produktivem Code.
   - Mindestens Debug-Logging + Kontext-ID + Metrik-Inkrement.
2. **Hotspot-Refactoring starten**
   - Top-5 Monolithen schrittweise in Service/Controller/Renderer/State-Module schneiden.
   - Zielgrenzen: ~300–500 LOC pro Klasse, klare Single Responsibility.

### P2 (mittelfristig)

1. **Architektur-Governance automatisieren**
   - PMD/SpotBugs/Jacoco in CI als Gate betreiben (derzeit konfiguriert, aber nicht verifiziert lauffähig im Scan-Environment).
2. **Dokudrift vermeiden**
   - README-/Architecture-Metriken automatisch aus Repo generieren (Script + CI Schritt).

## 5) Vorschlag für nächsten DeepScan (technisch tiefer)

Für den nächsten Zyklus empfehle ich eine instrumentierte Analyse mit:

- PMD/SpotBugs-Bericht (Top-Regelverletzungen + Delta zum Baseline)
- Cyclomatic Complexity per Klasse/Methode (z. B. via PMD metrics)
- Kopplungs-/Abhängigkeitsgraph pro Modul
- Testabdeckungsreport (JaCoCo XML/HTML) inkl. Hotspot-Matrix (hohe Komplexität + niedrige Coverage)

---

## Anhang: Wichtigste rohe Messwerte

- Main Java files: 1522
- Test Java files: 39
- LOC (`src/main/java` + `src/test/java`): 248945
- Größte Datei: `MapViewRenderer.java` (~1696 LOC)


## 6) Umgesetzte Maßnahmen (nach DeepScan)

- Repo-Hygiene-Script vorhanden (`scripts/repo_hygiene_check.sh`)
- Baseline-Quality-Guard gegen Regressions bei Empty-Catches/Thread-Nutzung (`scripts/quality_guard.sh`)
- Auto-generierte Metriken zur Reduktion von Dokumentationsdrift (`scripts/generate_repo_metrics.sh` -> `docs/REPO_METRICS.md`)
- CI führt Hygiene-, Quality- und Metrics-Schritte explizit aus (`.github/workflows/ci.yml`)
