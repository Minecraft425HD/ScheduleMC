#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Generiere detaillierte Statistiken über die hinzugefügten Translation-Keys
"""

import json
from pathlib import Path
from collections import defaultdict

BASE_DIR = Path("/home/user/ScheduleMC")
LANG_DIR = BASE_DIR / "src/main/resources/assets/schedulemc/lang"

def analyze_translations():
    """Analysiere Translation-Files"""
    with open(LANG_DIR / "de_de.json", 'r', encoding='utf-8') as f:
        de = json.load(f)

    with open(LANG_DIR / "en_us.json", 'r', encoding='utf-8') as f:
        en = json.load(f)

    # Kategorisiere alle Keys
    categories = defaultdict(lambda: defaultdict(int))

    for key in de.keys():
        parts = key.split('.')
        if len(parts) >= 2:
            main_category = parts[0]
            sub_category = parts[1] if len(parts) > 1 else 'general'
            categories[main_category][sub_category] += 1
        else:
            categories['other']['uncategorized'] += 1

    return de, en, categories

def print_statistics(de, en, categories):
    """Drucke detaillierte Statistiken"""

    print("=" * 80)
    print("TRANSLATION KEYS - COMPREHENSIVE STATISTICS")
    print("=" * 80)

    print(f"\nGESAMTÜBERSICHT:")
    print(f"  Deutsch (DE): {len(de):,} keys")
    print(f"  Englisch (EN): {len(en):,} keys")
    print(f"  Vorher: ~2,812 keys")
    print(f"  Neu hinzugefügt: ~{len(de) - 2812:,} keys")

    print(f"\n{'='*80}")
    print("VERTEILUNG NACH HAUPTKATEGORIEN:")
    print(f"{'='*80}")

    # Sortiere Kategorien nach Anzahl
    sorted_categories = sorted(categories.items(), key=lambda x: sum(x[1].values()), reverse=True)

    for main_cat, sub_cats in sorted_categories:
        total = sum(sub_cats.values())
        print(f"\n{main_cat.upper():20s} - {total:4d} keys")
        print(f"{'-' * 50}")

        # Sortiere Subkategorien
        sorted_subs = sorted(sub_cats.items(), key=lambda x: x[1], reverse=True)
        for sub_cat, count in sorted_subs[:10]:  # Top 10
            print(f"  {sub_cat:30s}: {count:4d} keys")

        if len(sorted_subs) > 10:
            remaining = sum(count for _, count in sorted_subs[10:])
            print(f"  {'... weitere Subkategorien':30s}: {remaining:4d} keys")

    print(f"\n{'='*80}")
    print("BEISPIELE AUS JEDER HAUPTKATEGORIE:")
    print(f"{'='*80}")

    # Zeige Beispiele aus jeder Kategorie
    for main_cat, _ in sorted_categories[:10]:  # Top 10 Kategorien
        matching_keys = [k for k in de.keys() if k.startswith(main_cat + '.')]
        if matching_keys:
            print(f"\n{main_cat.upper()}:")
            for key in matching_keys[:3]:  # 3 Beispiele
                print(f"  {key}")
                print(f"    DE: {de[key][:80]}{'...' if len(de[key]) > 80 else ''}")
                print(f"    EN: {en.get(key, 'MISSING')[:80]}{'...' if len(en.get(key, '')) > 80 else ''}")

    print(f"\n{'='*80}")
    print("ZUSAMMENFASSUNG NACH FUNKTIONSBEREICHEN:")
    print(f"{'='*80}")

    # Gruppiere nach Funktionsbereichen
    functional_groups = {
        'Commands': ['command'],
        'GUI/Screens': ['gui', 'screen'],
        'Blocks': ['block'],
        'Items': ['item'],
        'Events': ['event'],
        'Validation': ['validation'],
        'Manager': ['manager'],
        'Network': ['network'],
        'Enums': ['enum'],
        'System/Config': ['config', 'system'],
        'Achievements/Quests': ['achievement', 'quest'],
        'Misc': ['misc'],
    }

    for group_name, prefixes in functional_groups.items():
        count = sum(1 for key in de.keys() if any(key.startswith(p + '.') for p in prefixes))
        if count > 0:
            print(f"  {group_name:30s}: {count:4d} keys")

    print(f"\n{'='*80}")
    print("✓ STATISTIK-GENERIERUNG ABGESCHLOSSEN")
    print(f"{'='*80}\n")

def main():
    de, en, categories = analyze_translations()
    print_statistics(de, en, categories)

    # Zusätzliche Validierung
    print("VALIDIERUNG:")
    print(f"  DE == EN count: {len(de) == len(en)}")

    de_keys = set(de.keys())
    en_keys = set(en.keys())
    diff = de_keys.symmetric_difference(en_keys)

    if diff:
        print(f"  ⚠ WARNING: {len(diff)} keys unterschiedlich zwischen DE und EN")
    else:
        print(f"  ✓ Alle Keys sind in beiden Files vorhanden")

    print()

if __name__ == "__main__":
    main()
