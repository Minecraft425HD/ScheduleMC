#!/usr/bin/env python3
from pathlib import Path


def patch_generic(t: str) -> str:
    t = t.replace("3.8.0--beta", "3.9.0--beta")
    t = t.replace("schedulemc-3.8.0-beta.jar", "schedulemc-3.9.0-beta.jar")
    t = t.replace("ScheduleMC-3.8.0-beta.jar", "ScheduleMC-3.9.0-beta.jar")
    t = t.replace("3.8.0-beta", "3.9.0-beta")
    t = t.replace("comprehensive public API with 12 modules", "comprehensive public API with 11 I*API modules")
    t = t.replace("**12 API modules**", "**11 API modules**")
    t = t.replace("12 API modules", "11 I*API modules")
    t = t.replace("12 public API modules", "11 public I*API modules")
    t = t.replace("12 public modules", "11 public I*API modules")
    t = t.replace("all 12 public modules", "all 11 public I*API modules")
    t = t.replace("| **API Modules** | 12 |", "| **API Modules** | 11 |")
    t = t.replace("**~249k LOC** across **1,561 Java files**", "**~260k LOC** across **1,610 Java files**")
    t = t.replace("- **~249k LOC (main + tests)** and **1,561 Java files**", "- **~260k LOC (main + tests)** and **1,610 Java files**")
    t = t.replace("Enum defining all 7 plot types", "Enum defining all 8 plot types")
    t = t.replace(
        "- **5 Plot Types** -- Residential, Commercial, Shop, Public, Government",
        "- **8 Plot Types** -- Residential, Commercial, Industrial, Shop, Public, Government, Prison, Towing Yard",
    )
    t = t.replace("ScheduleMC supports 5 distinct plot types", "ScheduleMC supports 8 distinct plot types")
    t = t.replace(
        "| `COMMERCIAL` | Yes | Yes | Player-owned businesses |\n| `SHOP` |",
        "| `COMMERCIAL` | Yes | Yes | Player-owned businesses |\n| `INDUSTRIAL` | Yes | Yes | Factories; restricted processing blocks require factory floor |\n| `SHOP` |",
    )
    t = t.replace(
        "| **GOVERNMENT** | Red | Server | No | No | Admin-only |",
        "| **GOVERNMENT** | Red | Server | No | No | Admin-only |\n| **PRISON** | Dark red | Server | No | No | Police/admin |\n| **TOWING_YARD** | Orange | Player/Server | Yes | No | Full |\n| **INDUSTRIAL** | Brown | Player | Yes | No | Full (factory floor) |",
    )
    return t


def main() -> None:
    for rel in (
        "README.md",
        "docs/ARCHITECTURE.md",
        "wiki/Home.md",
        "wiki/features/Plot-System.md",
    ):
        p = Path(rel)
        p.write_text(patch_generic(p.read_text(encoding="utf-8")), encoding="utf-8")
        print("patched", rel, p.stat().st_size)

    cl = Path("docs/CHANGELOG.md")
    t = cl.read_text(encoding="utf-8")
    t = t.replace("## [3.8.0-beta]", "## [__KEEP_380__]")
    t = t.replace("3.8.0-beta", "3.9.0-beta")
    t = t.replace("## [__KEEP_380__]", "## [3.8.0-beta]")
    if "## [3.9.0-beta]" not in t:
        insert = (
            "## [3.9.0-beta] - 2026-09-24\n\n"
            "### Docs / accuracy\n"
            "- 11 public I*API modules (no ITutorialAPI in source)\n"
            "- 8 plot types including INDUSTRIAL\n"
            "- Persistence: JSON + IncrementalSaveManager\n"
            "- License metadata: GNU GPLv3\n"
            "- WeaponItems: 26 registered items; no Laser/Heavy magazine items\n\n"
            "---\n\n"
        )
        t = t.replace("## [3.7.2-beta]", insert + "## [3.7.2-beta]", 1)
    t = t.replace(
        "`ITutorialAPI` — Tutorial progress control API for external mods",
        "`ITutorialAPI` — **not implemented** (no interface in source)",
    )
    t = t.replace(
        "Increased total API modules to 12 (added Achievement and Market)",
        "Public API is 11 I*API modules (Achievement + Market added; no Tutorial API)",
    )
    cl.write_text(t, encoding="utf-8")
    print("patched CHANGELOG", cl.stat().st_size)


if __name__ == "__main__":
    main()
