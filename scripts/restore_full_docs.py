#!/usr/bin/env python3
from pathlib import Path

ROOTS = [Path("README.md"), Path("docs"), Path("wiki")]
SKIP_NAMES = {"restore_full_docs.py"}


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
    t = t.replace(
        "5 plot types (Residential, Commercial, Shop, Public, Government)",
        "8 plot types (Residential, Commercial, Industrial, Shop, Public, Government, Prison, Towing Yard)",
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
    t = t.replace(
        "24 achievements across 5 categories",
        "~35 achievements across 4 used categories",
    )
    t = t.replace(
        "24 achievements in 5 categories",
        "~35 achievements in 4 used categories",
    )
    t = t.replace(
        "3 attachments (Scope/Silencer/Laser)",
        "2 attachments (Scope/Silencer)",
    )
    t = t.replace("Last Updated:** 2026-04-13", "Last Updated:** 2026-09-24")
    return t


def patch_changelog(t: str) -> str:
    t = t.replace("## [3.8.0-beta]", "## [__KEEP_380__]")
    t = patch_generic(t)
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
    return t


def iter_markdown() -> list[Path]:
    out: list[Path] = []
    for root in ROOTS:
        if root.is_file() and root.suffix == ".md":
            out.append(root)
        elif root.is_dir():
            for p in root.rglob("*.md"):
                if p.name not in SKIP_NAMES:
                    out.append(p)
    return sorted(out)


def main() -> None:
    files = iter_markdown()
    print("candidates", len(files))
    for p in files:
        raw = p.read_text(encoding="utf-8", errors="replace")
        if p.as_posix() == "docs/CHANGELOG.md":
            new = patch_changelog(raw)
        else:
            new = patch_generic(raw)
        if new != raw:
            p.write_text(new, encoding="utf-8")
            print("patched", p.as_posix(), p.stat().st_size)
        else:
            print("unchanged", p.as_posix())


if __name__ == "__main__":
    main()
