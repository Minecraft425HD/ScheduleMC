# Codebase Audit (2026-04-10)

## Scope

Requested full-repo audit for bugs, quality, and performance risks.

Repository-wide checks executed:

- `rg --files` (full file inventory)
- `bash scripts/repo_hygiene_check.sh`
- `bash scripts/quality_guard.sh`
- `bash scripts/generate_repo_metrics.sh`
- `./gradlew test -q` (build/test capability)

## Key Findings

### 1) Build/test pipeline currently blocked in this environment (critical)

`./gradlew test -q` fails during Gradle script semantic analysis with:

- `Unsupported class file major version 69`

Impact: no reliable regression validation in this runtime environment until toolchain compatibility is resolved.

### 2) Error-handling quality debt remains notable (medium)

`quality_guard.sh` currently reports:

- Empty catches in main code: **51**
- `Thread.sleep` in main code: **2**
- `new Thread` in main code: **2**

Interpretation:

- Empty catches are still high and can hide production issues.
- Direct thread management and sleep calls indicate potential runtime timing/control risks.

### 3) Scale and hotspot pressure (medium-high)

Auto-metrics report:

- Main Java files: **1522**
- Test Java files: **39**
- Combined LOC (`src/main/java` + `src/test/java`): **249094**
- Largest class: `MapViewRenderer.java` (~1696 LOC)

Interpretation: maintenance complexity and coupling risk remain high in large, central classes.

### 4) Hygiene issue remediated (fixed)

A misplaced root-level Java snippet file (`AbstractSecretDoorBlock.java`) existed outside `src/main/java/...` and has been removed.

Additionally, the hygiene script now fails fast if `AbstractSecretDoorBlock.java` or `HiddenSwitchBlock.java` appears in repo root.

### 5) Follow-up quality fixes applied (2026-04-10, later pass)

- Removed 12 additional silent `catch (IllegalArgumentException ignored) {}` occurrences in honey block entities by replacing them with contextual warning logs.
- `quality_guard.sh` delta after this pass:
  - Empty catches in main: **51 → 39**
  - `Thread.sleep` in main: **2** (unchanged)
  - `new Thread` in main: **2** (unchanged)

### 6) Follow-up quality fixes applied (2026-04-10, final pass)

- Removed 7 additional silent enum-parse catches in tobacco/beer block entities and replaced them with contextual warning logs.
- `quality_guard.sh` delta after this pass:
  - Empty catches in main: **39 → 32**
  - `Thread.sleep` in main: **2** (unchanged)
  - `new Thread` in main: **2** (unchanged)

### 7) Follow-up quality fixes applied (2026-04-10, extended pass)

- Removed 10 additional silent enum-parse catches in cheese/coffee block entities and replaced them with contextual warning logs.
- `quality_guard.sh` delta after this pass:
  - Empty catches in main: **32 → 22**
  - `Thread.sleep` in main: **2** (unchanged)
  - `new Thread` in main: **2** (unchanged)

### 8) Follow-up quality fixes applied (2026-04-11, continued pass)

- Removed 9 additional silent parse catches in screen/item/network/quest modules by enforcing explicit fallback assignments in catch paths.
- `quality_guard.sh` delta after this pass:
  - Empty catches in main: **22 → 13**
  - `Thread.sleep` in main: **2** (unchanged)
  - `new Thread` in main: **2** (unchanged)

## Suggested Next Steps (prioritized)

1. Fix Java/Gradle toolchain compatibility so tests run in CI and local environments.
2. Reduce empty-catch count in high-traffic runtime modules first (log with context at minimum).
3. Replace ad-hoc thread usage with centralized executors and remove blocking sleeps from runtime paths.
4. Gradually split monolithic hotspots (starting with mapview/ui command classes).
