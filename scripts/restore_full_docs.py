#!/usr/bin/env python3
from pathlib import Path

ROOTS = [Path("README.md"), Path("docs"), Path("wiki")]


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
    t = t.replace("Public API (12 modules)", "Public API (11 I*API modules)")
    t = t.replace("**~249k LOC** across **1,561 Java files**", "**~260k LOC** across **1,610 Java files**")
    t = t.replace("- **~249k LOC (main + tests)** and **1,561 Java files**", "- **~260k LOC (main + tests)** and **1,610 Java files**")
    t = t.replace("| **Lines of Code** | 224,000+ |", "| **Lines of Code** | ~260,000 |")
    t = t.replace("| **Total Files** | 1,453 |", "| **Total Files** | 1,610 Java |")
    t = t.replace("| **Registered Items** | 383 |", "| **Registered Items** | 354 catalogued |")
    t = t.replace("all 1,448 Java files", "all 1,610 Java files")
    t = t.replace("all 1,494 Java files", "all 1,610 Java files")
    t = t.replace("Java-Quelldateien (main): 1.494", "Java-Quelldateien (main + tests): 1.610")
    t = t.replace(
        "With over 93,000 lines of Java code, 354 items, 152 blocks, and 139 commands",
        "With ~260,000 lines of Java code, 354 catalogued items, 152 blocks, and 139 commands",
    )
    t = t.replace("Enum defining all 7 plot types", "Enum defining all 8 plot types")
    t = t.replace("has **7 plot types**", "has **8 plot types**")
    t = t.replace("ScheduleMC supports 5 plot types:", "ScheduleMC supports 8 plot types:")
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
        "Plots come in types: Residential, Commercial, Shop, Public, and Government.",
        "Plots come in types: Residential, Commercial, Industrial, Shop, Public, Government, Prison, and Towing Yard.",
    )
    t = t.replace(
        "3 NPC roles (Resident, Merchant, Police)",
        "7 NPC types (Citizen, Merchant, Police, Bank, Tow Truck Driver, Banker, Drug Dealer)",
    )
    t = t.replace("There are 3 core NPC types:", "There are 7 NPC types in `NPCType`:")
    t = t.replace(
        "| **Police** | Law enforcement officers | Chase criminals, arrest wanted players, call for backup, block doors during pursuit |\n\nAll NPC types share",
        "| **Police** | Law enforcement officers | Chase criminals, arrest wanted players, call for backup, block doors during pursuit |\n| **Bank** | Bank branch NPC | Banking services |\n| **Tow Truck Driver** | Impound / towing | Vehicle towing |\n| **Banker** | Credit / bank teller | Loans and accounts |\n| **Drug Dealer** | Underground trade | Illegal goods |\n\nAll NPC types share",
    )
    t = t.replace(
        "| `COMMERCIAL` | Yes | Yes | Player-owned businesses |\n| `SHOP` |",
        "| `COMMERCIAL` | Yes | Yes | Player-owned businesses |\n| `INDUSTRIAL` | Yes | Yes | Factories; restricted processing blocks require factory floor |\n| `SHOP` |",
    )
    t = t.replace(
        "| **GOVERNMENT** | Red | Server | No | No | Admin-only |",
        "| **GOVERNMENT** | Red | Server | No | No | Admin-only |\n| **PRISON** | Dark red | Server | No | No | Police/admin |\n| **TOWING_YARD** | Orange | Player/Server | Yes | No | Full |\n| **INDUSTRIAL** | Brown | Player | Yes | No | Full (factory floor) |",
    )
    t = t.replace(
        "    COMMERCIAL(true, true),     // Purchasable, rentable\n    SHOP(false, false),",
        "    COMMERCIAL(true, true),     // Purchasable, rentable\n    INDUSTRIAL(true, true),     // Factory floor\n    SHOP(false, false),",
    )
    t = t.replace(
        "| **Commercial** | Businesses, offices, and shops | All players |\n| **Shop** |",
        "| **Commercial** | Businesses, offices, and shops | All players |\n| **Industrial** | Factories / processing | All players |\n| **Shop** |",
    )
    t = t.replace(
        "| **Commercial** | Yes | Yes | Businesses and offices |\n| **Shop** |",
        "| **Commercial** | Yes | Yes | Businesses and offices |\n| **Industrial** | Yes | Yes | Factories / processing |\n| **Shop** |",
    )
    t = t.replace(
        "| **Government** | Town halls, prisons, hospitals, and official buildings | Admins only |\n\nEach type",
        "| **Government** | Town halls, prisons, hospitals, and official buildings | Admins only |\n| **Prison** | Jail facilities for the crime system | Admins only |\n| **Towing Yard** | Vehicle impound lots | All players |\n\nEach type",
    )
    t = t.replace(
        '/plot create commercial "Downtown Office" 75000\n/plot create shop',
        '/plot create commercial "Downtown Office" 75000\n/plot create industrial "Factory" 80000\n/plot create shop',
    )
    t = t.replace(
        "Purchasable types (residential, commercial, towing_yard)",
        "Purchasable types (residential, commercial, industrial, towing_yard)",
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
        "24 Achievements in 5 Kategorien",
        "~35 Achievements in 4 Kategorien",
    )
    t = t.replace(
        "provides 24 achievements across 5 categories",
        "provides ~35 achievements across 4 used categories",
    )
    t = t.replace(
        "with 24 achievements across 5 categories",
        "with ~35 achievements across 4 used categories",
    )
    t = t.replace(
        "- **24 achievements** across 5 categories",
        "- **~35 achievements** across 4 used categories",
    )
    t = t.replace(
        "3 attachments (Scope/Silencer/Laser)",
        "2 attachments (Scope/Silencer)",
    )
    t = t.replace(
        "| [Tutorial System](features/Tutorial-System.md) | Player Onboarding | 7-step interactive tutorial",
        "| [Tutorial System](features/Tutorial-System.md) | Player Onboarding (**not implemented**) | Design doc only — 7-step tutorial",
    )
    t = t.replace("Last Updated:** 2026-04-13", "Last Updated:** 2026-09-24")
    t = t.replace(
        "distributed under the **All Rights Reserved** license. The `gradle.properties` file specifies the license as `All Rights Reserved`",
        "distributed under the **GNU GPLv3**. The `gradle.properties` / `mods.toml` license field is `GNU GPLv3`",
    )
    t = t.replace(
        "meaning all rights are retained by the author, Luckas R. Schneider (Minecraft425HD). While the source code is available on GitHub, redistribution, modification, and commercial use may be restricted. Refer to the project's license terms for specific permissions.\n\n**Note:** The repository's LICENSE file contains the text of the GNU GPL v3, which may indicate a transition or dual-licensing arrangement. When in doubt, contact the developer for clarification.",
        "Author: Luckas R. Schneider (Minecraft425HD). LICENSE, gradle.properties and mods.toml all state GNU GPLv3.",
    )
    t = t.replace("*License: All Rights Reserved*", "*License: GNU GPLv3*")
    t = t.replace(
        "/state balance              Check your bank balance",
        "/state balance              View the government treasury balance",
    )
    return t


def patch_changelog(t: str) -> str:
    t = t.replace("## [3.8.0-beta]", "## [__KEEP_380__]")
    t = t.replace("5 plot types (Residential, Commercial, Shop, Public, Government)", "__KEEP_5PLOTS__")
    t = t.replace("24 achievements in 5 categories", "__KEEP_24ACH__")
    t = patch_generic(t)
    t = t.replace("## [__KEEP_380__]", "## [3.8.0-beta]")
    t = t.replace("__KEEP_5PLOTS__", "5 plot types (Residential, Commercial, Shop, Public, Government)")
    t = t.replace("__KEEP_24ACH__", "24 achievements in 5 categories")
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


def iter_markdown():
    out = []
    for root in ROOTS:
        if root.is_file() and root.suffix == ".md":
            out.append(root)
        elif root.is_dir():
            out.extend(root.rglob("*.md"))
    return sorted(out)


def main() -> None:
    files = iter_markdown()
    print("candidates", len(files))
    for p in files:
        raw = p.read_text(encoding="utf-8", errors="replace")
        new = patch_changelog(raw) if p.as_posix() == "docs/CHANGELOG.md" else patch_generic(raw)
        if new != raw:
            p.write_text(new, encoding="utf-8")
            print("patched", p.as_posix(), p.stat().st_size)
        else:
            print("unchanged", p.as_posix())


if __name__ == "__main__":
    main()
