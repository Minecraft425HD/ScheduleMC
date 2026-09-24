# README / docs errata (code-aligned)

Use this page when a large doc still carries old stamps. Runtime truth is `gradle.properties` + `docs/VERSION.md`.

## README.md

| Location | Written | Actual |
|---|---|---|
| Client install JAR | `schedulemc-3.9.0-beta.jar` | `schedulemc-3.9.0-beta.jar` |
| “11 I*API modules” | 12 | **11** `I*API` interfaces |
| Plot type table | 7 types | **8** — add `INDUSTRIAL` |
| `PlotType` blurb | “all 7 plot types” | **8** |
| LOC / file counts | ~249k / 1561 | ~260k / ~1610 |

Server install already names `schedulemc-3.9.0-beta.jar`.

## docs/ARCHITECTURE.md

Replace the header line:

`Version 3.9.0-beta | ~249k LOC across 1,561 Java files`

with:

`Version 3.9.0-beta | ~260k LOC across 1,610 Java files`

The API section already lists 11 `I*API` impls (correct).

## Wiki version footers

Any `ScheduleMC v3.9.0-beta` footer means **3.9.0-beta**.
