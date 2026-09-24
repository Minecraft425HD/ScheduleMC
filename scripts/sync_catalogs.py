#!/usr/bin/env python3
from collections import defaultdict
from pathlib import Path
import re

ROOT = Path(".")
JAVA = ROOT / "src/main/java"


def collect_register(kind: str) -> dict:
    by_file = defaultdict(list)
    for p in JAVA.rglob("*.java"):
        t = p.read_text(encoding="utf-8", errors="replace")
        found = re.findall(rf"{kind}\.register\(\s*\"([^\"]+)\"", t)
        if found:
            by_file[str(p.relative_to(JAVA))] = found
    return by_file


def patch_metrics(t: str, items: int, blocks: int) -> str:
    t = t.replace("| **Registered Items** | 399 (`ITEMS.register`) |", f"| **Registered Items** | {items} string IDs |")
    t = t.replace("| **Registered Items** | 354 catalogued |", f"| **Registered Items** | {items} string IDs |")
    t = t.replace("| **Registered Blocks** | 125 |", f"| **Registered Blocks** | {blocks} |")
    t = t.replace("| **Registered Blocks** | 152 |", f"| **Registered Blocks** | {blocks} |")
    t = t.replace("| **BlockEntity Types** | 128 |", "| **BlockEntity Types** | 111 |")
    t = t.replace("| **BlockEntity Types** | 131 |", "| **BlockEntity Types** | 111 |")
    t = t.replace(
        "399 registered items, 125 blocks",
        f"{items} registered items, {blocks} blocks",
    )
    t = t.replace("399 (`ITEMS.register`)", f"{items} (`ITEMS.register(\"id\")`)")
    return t


def patch_items(text: str, by_file: dict) -> str:
    all_ids = []
    for ids in by_file.values():
        all_ids.extend(ids)
    n = len(all_ids)
    text = re.sub(
        r"\*\*Total Registered Items:\*\* [^\n]+",
        f"**Total Registered Items:** {n} `ITEMS.register(\"id\")` entries across {len(by_file)} Java files",
        text,
        count=1,
    )
    # category headings that map cleanly to a single *Items.java file
    mapping = {
        "## Tobacco Items (": "tobacco/items/TobaccoItems.java",
        "## Cannabis Items (": "cannabis/items/CannabisItems.java",
        "## Coca Items (": "coca/items/CocaItems.java",
        "## Poppy Items (": "poppy/items/PoppyItems.java",
        "## Meth Items (": "meth/items/MethItems.java",
        "## LSD Items (": "lsd/items/LSDItems.java",
        "## MDMA Items (": "mdma/items/MDMAItems.java",
        "## Mushroom Items (": "mushroom/items/MushroomItems.java",
        "## Beer Items (": "beer/items/BeerItems.java",
        "## Wine Items (": "wine/items/WineItems.java",
        "## Coffee Items (": "coffee/items/CoffeeItems.java",
        "## Cheese Items (": "cheese/items/CheeseItems.java",
        "## Chocolate Items (": "chocolate/items/ChocolateItems.java",
        "## Honey Items (": "honey/items/HoneyItems.java",
        "## Lock Items (": "lock/items/LockItems.java",
        "## NPC Tools (": "npc/items/NPCItems.java",
        "## Economy Items (": "items/ModItems.java",
    }
    lines = text.splitlines(keepends=True)
    out = []
    for line in lines:
        replaced = False
        for prefix, rel in mapping.items():
            if line.startswith(prefix):
                key = "de/rolandsw/schedulemc/" + rel
                count = len(by_file.get(key, []))
                # keep rest of heading title after the number paren
                rest = line.split(")", 1)[1] if ")" in line else "\n"
                title = prefix[3:].split(" (")[0]
                out.append(f"## {title} ({count} in `{rel.split('/')[-1]}`){rest}")
                replaced = True
                break
        if not replaced:
            out.append(line)
    text = "".join(out)

    appendix = ["\n\n<!-- BEGIN SOURCE-ITEM-REGISTRY -->\n"]
    appendix.append("# Source item registry\n\n")
    appendix.append(
        f"Generated from `ITEMS.register(\"id\")` in source. **{n} IDs**. "
        "Narrative tables above may use alias IDs that are not the registry name.\n\n"
    )
    for rel, ids in sorted(by_file.items()):
        appendix.append(f"## `{rel}` ({len(ids)})\n\n")
        for i in ids:
            appendix.append(f"- `{i}`\n")
        appendix.append("\n")
    appendix.append("<!-- END SOURCE-ITEM-REGISTRY -->\n")
    body = "".join(appendix)
    mark = "<!-- BEGIN SOURCE-ITEM-REGISTRY -->"
    end = "<!-- END SOURCE-ITEM-REGISTRY -->"
    if mark in text:
        pre = text.split(mark, 1)[0]
        post = text.split(end, 1)[1] if end in text else ""
        text = pre.rstrip() + body + post
    else:
        text = text.rstrip() + body
    return text


def patch_commands(text: str) -> str:
    heads = re.findall(r"^### `/([^`]+)`", text, re.M)
    from collections import Counter

    roots = Counter(h.split()[0].split("<")[0] for h in heads)
    total = len(heads)
    replacements = {
        ") (24 commands)": f") ({roots.get('plot', 0)} commands)",
        ") (28 commands)": f") ({roots.get('npc', 0)} commands)",
        ") (7 commands)": f") ({roots.get('warehouse', 0)} commands)",
        ") (10 commands)": f") ({roots.get('prison', 0)} commands)",
        ") (8 commands)": None,  # utility + lock both 8 in TOC; handle below
        ") (5 commands)": None,
    }
    text = text.replace(
        "[NPC Commands](#4-npc-commands) (28 commands)",
        f"[NPC Commands](#4-npc-commands) ({roots.get('npc', 0)} documented headings)",
    )
    text = text.replace(
        "[Prison and Crime Commands](#6-prison-and-crime-commands) (10 commands)",
        f"[Prison and Crime Commands](#6-prison-and-crime-commands) ({roots.get('prison', 0)} `/prison` + bail/jailtime)",
    )
    text = text.replace(
        "[Lock Commands](#13-lock-commands) (8 commands)",
        f"[Lock Commands](#13-lock-commands) ({roots.get('lock', 0)} commands)",
    )
    text = text.replace(
        "[Utility Commands](#10-utility-commands) (8 commands)",
        f"[Utility Commands](#10-utility-commands) ({roots.get('utility', 0)} commands)",
    )
    text = text.replace(
        "[Health and Diagnostics Commands](#15-health-and-diagnostics-commands) (5 commands)",
        f"[Health and Diagnostics Commands](#15-health-and-diagnostics-commands) ({roots.get('health', 0)} `/health` headings)",
    )
    note = (
        f"\n**Source sync:** {total} `### /command` headings in this page; "
        f"25 root `dispatcher.register` commands. "
        f"Largest groups: /health {roots.get('health', 0)}, /plot {roots.get('plot', 0)}, "
        f"/npc {roots.get('npc', 0)}, /gang {roots.get('gang', 0)}.\n"
    )
    if "**Source sync:**" not in text:
        text = text.replace(
            "All registered commands organized by system, with syntax, permissions, and examples.",
            "All registered commands organized by system, with syntax, permissions, and examples." + note,
            1,
        )
    return text


def main() -> None:
    items = collect_register("ITEMS")
    blocks = collect_register("BLOCKS")
    n_items = sum(len(v) for v in items.values())
    n_blocks = sum(len(v) for v in blocks.values())
    print("items", n_items, "files", len(items))
    print("blocks", n_blocks, "files", len(blocks))

    ip = Path("wiki/Items.md")
    ip.write_text(patch_items(ip.read_text(encoding="utf-8"), items), encoding="utf-8")
    print("wrote", ip, ip.stat().st_size)

    cp = Path("wiki/Commands.md")
    cp.write_text(patch_commands(cp.read_text(encoding="utf-8")), encoding="utf-8")
    print("wrote", cp, cp.stat().st_size)

    for rel in ("wiki/Home.md", "wiki/FAQ.md", "docs/VERSION.md"):
        p = Path(rel)
        if p.exists():
            p.write_text(patch_metrics(p.read_text(encoding="utf-8"), n_items, n_blocks), encoding="utf-8")
            print("metrics", rel)


if __name__ == "__main__":
    main()
