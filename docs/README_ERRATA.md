# README / docs errata (code-aligned)

Use this page when a large doc still carries old stamps. Runtime truth is `gradle.properties` + `docs/VERSION.md`.

## Status (2026-09-24)

The previous entries on this page (API module count, plot type count,
LOC/file counts) have been applied directly to README.md and
docs/ARCHITECTURE.md — this page is not a queue of pending edits, just a
record of the last reconciliation pass.

Current values, applied throughout README.md, docs/ARCHITECTURE.md,
docs/DEVELOPER_GUIDE.md, wiki/Home.md, wiki/FAQ.md:

| Metric | Value |
|---|---|
| Public API | none (removed — see `docs/VERSION.md`) |
| Plot types | 8, including `INDUSTRIAL` |
| Java files (main + test) | 1,568 |
| LOC (main + test) | ~251k |
| Largest Java file | `MapViewRenderer.java` (~1672 LOC) |

Client/server install JAR name (`schedulemc-3.9.0-beta.jar`) was already correct.

## Wiki version footers

Any `ScheduleMC v3.9.0-beta` footer means **3.9.0-beta**.
