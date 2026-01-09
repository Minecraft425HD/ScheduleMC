#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Finales Script - Fügt die letzten ~250 Translation-Keys hinzu
Fokus auf: detaillierte Messages, GUI-Texte, Config-Messages, erweiterte Tooltips
"""

import json
from pathlib import Path
from collections import defaultdict

BASE_DIR = Path("/home/user/ScheduleMC")
LANG_DIR = BASE_DIR / "src/main/resources/assets/schedulemc/lang"

def load_translations():
    with open(LANG_DIR / "de_de.json", 'r', encoding='utf-8') as f:
        de = json.load(f)
    with open(LANG_DIR / "en_us.json", 'r', encoding='utf-8') as f:
        en = json.load(f)
    return de, en

def add_final_comprehensive_keys():
    """Füge die letzten umfassenden Keys hinzu"""
    new_keys = {}

    # GUI/SCREEN MESSAGES (~80 Keys)
    new_keys.update({
        # Main Menu GUI
        "gui.main_menu.title": {"de": "Hauptmenü", "en": "Main Menu"},
        "gui.main_menu.shop": {"de": "Shop", "en": "Shop"},
        "gui.main_menu.bank": {"de": "Bank", "en": "Bank"},
        "gui.main_menu.inventory": {"de": "Inventar", "en": "Inventory"},
        "gui.main_menu.profile": {"de": "Profil", "en": "Profile"},
        "gui.main_menu.settings": {"de": "Einstellungen", "en": "Settings"},
        "gui.main_menu.quit": {"de": "Beenden", "en": "Quit"},

        # Bank GUI
        "gui.bank.title": {"de": "Bank", "en": "Bank"},
        "gui.bank.balance": {"de": "Kontostand", "en": "Balance"},
        "gui.bank.deposit": {"de": "Einzahlen", "en": "Deposit"},
        "gui.bank.withdraw": {"de": "Abheben", "en": "Withdraw"},
        "gui.bank.transfer": {"de": "Überweisen", "en": "Transfer"},
        "gui.bank.history": {"de": "Transaktionshistorie", "en": "Transaction History"},
        "gui.bank.savings": {"de": "Sparkonto", "en": "Savings Account"},
        "gui.bank.loans": {"de": "Kredite", "en": "Loans"},
        "gui.bank.statements": {"de": "Kontoauszüge", "en": "Statements"},

        # Shop GUI
        "gui.shop.title": {"de": "Shop", "en": "Shop"},
        "gui.shop.buy": {"de": "Kaufen", "en": "Buy"},
        "gui.shop.sell": {"de": "Verkaufen", "en": "Sell"},
        "gui.shop.cart": {"de": "Warenkorb", "en": "Cart"},
        "gui.shop.checkout": {"de": "Zur Kasse", "en": "Checkout"},
        "gui.shop.total": {"de": "Gesamt", "en": "Total"},
        "gui.shop.discount": {"de": "Rabatt", "en": "Discount"},
        "gui.shop.stock": {"de": "Lagerbestand", "en": "Stock"},
        "gui.shop.category": {"de": "Kategorie", "en": "Category"},

        # Profile/Stats GUI
        "gui.profile.title": {"de": "Spielerprofil", "en": "Player Profile"},
        "gui.profile.level": {"de": "Level", "en": "Level"},
        "gui.profile.experience": {"de": "Erfahrung", "en": "Experience"},
        "gui.profile.reputation": {"de": "Reputation", "en": "Reputation"},
        "gui.profile.playtime": {"de": "Spielzeit", "en": "Playtime"},
        "gui.profile.achievements": {"de": "Erfolge", "en": "Achievements"},
        "gui.profile.statistics": {"de": "Statistiken", "en": "Statistics"},
        "gui.profile.money_earned": {"de": "Verdientes Geld", "en": "Money Earned"},
        "gui.profile.money_spent": {"de": "Ausgegebenes Geld", "en": "Money Spent"},
        "gui.profile.kills": {"de": "Kills", "en": "Kills"},
        "gui.profile.deaths": {"de": "Tode", "en": "Deaths"},
        "gui.profile.kd_ratio": {"de": "K/D Verhältnis", "en": "K/D Ratio"},

        # Inventory GUI
        "gui.inventory.title": {"de": "Inventar", "en": "Inventory"},
        "gui.inventory.sort": {"de": "Sortieren", "en": "Sort"},
        "gui.inventory.search": {"de": "Suchen", "en": "Search"},
        "gui.inventory.weight": {"de": "Gewicht", "en": "Weight"},
        "gui.inventory.capacity": {"de": "Kapazität", "en": "Capacity"},
        "gui.inventory.drop": {"de": "Fallenlassen", "en": "Drop"},
        "gui.inventory.use": {"de": "Benutzen", "en": "Use"},
        "gui.inventory.equip": {"de": "Ausrüsten", "en": "Equip"},
        "gui.inventory.unequip": {"de": "Ablegen", "en": "Unequip"},

        # Settings GUI
        "gui.settings.title": {"de": "Einstellungen", "en": "Settings"},
        "gui.settings.audio": {"de": "Audio", "en": "Audio"},
        "gui.settings.video": {"de": "Video", "en": "Video"},
        "gui.settings.controls": {"de": "Steuerung", "en": "Controls"},
        "gui.settings.language": {"de": "Sprache", "en": "Language"},
        "gui.settings.notifications": {"de": "Benachrichtigungen", "en": "Notifications"},
        "gui.settings.privacy": {"de": "Privatsphäre", "en": "Privacy"},
        "gui.settings.reset": {"de": "Zurücksetzen", "en": "Reset"},
        "gui.settings.apply": {"de": "Anwenden", "en": "Apply"},
        "gui.settings.cancel": {"de": "Abbrechen", "en": "Cancel"},
        "gui.settings.save": {"de": "Speichern", "en": "Save"},

        # Dialog/Confirmation GUI
        "gui.dialog.confirm": {"de": "Bestätigen", "en": "Confirm"},
        "gui.dialog.cancel": {"de": "Abbrechen", "en": "Cancel"},
        "gui.dialog.yes": {"de": "Ja", "en": "Yes"},
        "gui.dialog.no": {"de": "Nein", "en": "No"},
        "gui.dialog.ok": {"de": "OK", "en": "OK"},
        "gui.dialog.close": {"de": "Schließen", "en": "Close"},
        "gui.dialog.back": {"de": "Zurück", "en": "Back"},
        "gui.dialog.next": {"de": "Weiter", "en": "Next"},
        "gui.dialog.finish": {"de": "Fertig", "en": "Finish"},

        # Map/Navigation GUI
        "gui.map.title": {"de": "Karte", "en": "Map"},
        "gui.map.zoom_in": {"de": "Vergrößern", "en": "Zoom In"},
        "gui.map.zoom_out": {"de": "Verkleinern", "en": "Zoom Out"},
        "gui.map.marker": {"de": "Markierung", "en": "Marker"},
        "gui.map.waypoint": {"de": "Wegpunkt", "en": "Waypoint"},
        "gui.map.location": {"de": "Standort", "en": "Location"},
    })

    # VALIDATION MESSAGES - Erweitert (~40 Keys)
    new_keys.update({
        "validation.input.empty": {"de": "Eingabe darf nicht leer sein", "en": "Input cannot be empty"},
        "validation.input.too_short": {"de": "Eingabe ist zu kurz (min. %s Zeichen)", "en": "Input too short (min. %s characters)"},
        "validation.input.too_long": {"de": "Eingabe ist zu lang (max. %s Zeichen)", "en": "Input too long (max. %s characters)"},
        "validation.input.invalid_format": {"de": "Ungültiges Format", "en": "Invalid format"},
        "validation.input.contains_special_chars": {"de": "Enthält ungültige Sonderzeichen", "en": "Contains invalid special characters"},
        "validation.input.only_numbers": {"de": "Nur Zahlen erlaubt", "en": "Only numbers allowed"},
        "validation.input.only_letters": {"de": "Nur Buchstaben erlaubt", "en": "Only letters allowed"},
        "validation.input.alphanumeric_only": {"de": "Nur alphanumerische Zeichen erlaubt", "en": "Only alphanumeric characters allowed"},

        "validation.number.not_integer": {"de": "Muss eine Ganzzahl sein", "en": "Must be an integer"},
        "validation.number.not_positive": {"de": "Muss positiv sein", "en": "Must be positive"},
        "validation.number.out_of_range": {"de": "Außerhalb des gültigen Bereichs (%s-%s)", "en": "Out of valid range (%s-%s)"},
        "validation.number.not_divisible": {"de": "Muss durch %s teilbar sein", "en": "Must be divisible by %s"},

        "validation.date.invalid": {"de": "Ungültiges Datum", "en": "Invalid date"},
        "validation.date.past": {"de": "Datum liegt in der Vergangenheit", "en": "Date is in the past"},
        "validation.date.future": {"de": "Datum liegt in der Zukunft", "en": "Date is in the future"},
        "validation.date.format": {"de": "Erwartetes Format: %s", "en": "Expected format: %s"},

        "validation.email.invalid": {"de": "Ungültige E-Mail-Adresse", "en": "Invalid email address"},
        "validation.url.invalid": {"de": "Ungültige URL", "en": "Invalid URL"},
        "validation.uuid.invalid": {"de": "Ungültige UUID", "en": "Invalid UUID"},

        "validation.file.not_found": {"de": "Datei nicht gefunden", "en": "File not found"},
        "validation.file.too_large": {"de": "Datei zu groß (max. %s)", "en": "File too large (max. %s)"},
        "validation.file.wrong_type": {"de": "Falscher Dateityp (erlaubt: %s)", "en": "Wrong file type (allowed: %s)"},
        "validation.file.corrupted": {"de": "Datei beschädigt", "en": "File corrupted"},

        "validation.password.too_weak": {"de": "Passwort zu schwach", "en": "Password too weak"},
        "validation.password.no_uppercase": {"de": "Passwort muss Großbuchstaben enthalten", "en": "Password must contain uppercase letters"},
        "validation.password.no_lowercase": {"de": "Passwort muss Kleinbuchstaben enthalten", "en": "Password must contain lowercase letters"},
        "validation.password.no_numbers": {"de": "Passwort muss Zahlen enthalten", "en": "Password must contain numbers"},
        "validation.password.no_special": {"de": "Passwort muss Sonderzeichen enthalten", "en": "Password must contain special characters"},
        "validation.password.mismatch": {"de": "Passwörter stimmen nicht überein", "en": "Passwords do not match"},
    })

    # CONFIG/SYSTEM MESSAGES (~30 Keys)
    new_keys.update({
        "config.loaded": {"de": "Konfiguration geladen", "en": "Configuration loaded"},
        "config.saved": {"de": "Konfiguration gespeichert", "en": "Configuration saved"},
        "config.error": {"de": "Fehler in Konfigurationsdatei: %s", "en": "Error in config file: %s"},
        "config.invalid_value": {"de": "Ungültiger Wert für %s: %s", "en": "Invalid value for %s: %s"},
        "config.missing_key": {"de": "Fehlender Konfigurationsschlüssel: %s", "en": "Missing config key: %s"},
        "config.deprecated": {"de": "Veraltete Konfigurationsoption: %s", "en": "Deprecated config option: %s"},
        "config.reset": {"de": "Konfiguration zurückgesetzt", "en": "Configuration reset"},

        "system.startup": {"de": "System wird gestartet...", "en": "System starting..."},
        "system.shutdown": {"de": "System wird heruntergefahren...", "en": "System shutting down..."},
        "system.restart": {"de": "System wird neu gestartet...", "en": "System restarting..."},
        "system.update_available": {"de": "Update verfügbar: Version %s", "en": "Update available: Version %s"},
        "system.update_downloading": {"de": "Update wird heruntergeladen...", "en": "Downloading update..."},
        "system.update_installing": {"de": "Update wird installiert...", "en": "Installing update..."},
        "system.update_complete": {"de": "Update abgeschlossen", "en": "Update complete"},
        "system.backup_started": {"de": "Backup gestartet...", "en": "Backup started..."},
        "system.backup_complete": {"de": "Backup abgeschlossen", "en": "Backup complete"},
        "system.backup_failed": {"de": "Backup fehlgeschlagen", "en": "Backup failed"},
        "system.maintenance_mode": {"de": "Wartungsmodus aktiv", "en": "Maintenance mode active"},
        "system.performance_warning": {"de": "Leistungswarnung: %s", "en": "Performance warning: %s"},
        "system.memory_low": {"de": "Geringer Arbeitsspeicher", "en": "Low memory"},
        "system.disk_space_low": {"de": "Geringer Speicherplatz", "en": "Low disk space"},
    })

    # DETAILED BLOCK ENTITY MESSAGES (~50 Keys)
    new_keys.update({
        # General Processing
        "block.processing.speed": {"de": "Verarbeitungsgeschwindigkeit: %s", "en": "Processing speed: %s"},
        "block.processing.quality_bonus": {"de": "Qualitätsbonus: +%s%%", "en": "Quality bonus: +%s%%"},
        "block.processing.yield": {"de": "Ausbeute: %s", "en": "Yield: %s"},
        "block.processing.batch_size": {"de": "Chargengröße: %s", "en": "Batch size: %s"},
        "block.processing.cycles": {"de": "Durchläufe: %s", "en": "Cycles: %s"},
        "block.processing.success_rate": {"de": "Erfolgsrate: %s%%", "en": "Success rate: %s%%"},

        # Specific Processes
        "block.extraction.rate": {"de": "Extraktionsrate: %s ml/min", "en": "Extraction rate: %s ml/min"},
        "block.extraction.solvent_level": {"de": "Lösungsmittelstand: %s%%", "en": "Solvent level: %s%%"},
        "block.extraction.add_solvent": {"de": "Lösungsmittel hinzufügen", "en": "Add solvent"},

        "block.refinery.stage": {"de": "Raffinierungsstufe: %s/%s", "en": "Refining stage: %s/%s"},
        "block.refinery.impurities": {"de": "Verunreinigungen: %s%%", "en": "Impurities: %s%%"},
        "block.refinery.heat": {"de": "Temperatur: %s°C", "en": "Temperature: %s°C"},

        "block.crystallizer.seed_added": {"de": "Kristallisationskeim hinzugefügt", "en": "Seed crystal added"},
        "block.crystallizer.crystal_size": {"de": "Kristallgröße: %s", "en": "Crystal size: %s"},
        "block.crystallizer.formation_rate": {"de": "Bildungsrate: %s%%", "en": "Formation rate: %s%%"},

        "block.mixer.rpm": {"de": "Umdrehungen: %s/min", "en": "RPM: %s"},
        "block.mixer.homogeneity": {"de": "Homogenität: %s%%", "en": "Homogeneity: %s%%"},
        "block.mixer.viscosity": {"de": "Viskosität: %s", "en": "Viscosity: %s"},

        "block.dryer.humidity": {"de": "Feuchtigkeit: %s%%", "en": "Humidity: %s%%"},
        "block.dryer.airflow": {"de": "Luftstrom: %s m³/h", "en": "Airflow: %s m³/h"},
        "block.dryer.complete_at": {"de": "Fertig um: %s", "en": "Complete at: %s"},

        # Energy/Power
        "block.energy.consumption": {"de": "Energieverbrauch: %s EU/t", "en": "Energy consumption: %s EU/t"},
        "block.energy.stored": {"de": "Gespeicherte Energie: %s/%s EU", "en": "Stored energy: %s/%s EU"},
        "block.energy.generation": {"de": "Energieerzeugung: %s EU/t", "en": "Energy generation: %s EU/t"},
        "block.energy.efficiency": {"de": "Energieeffizienz: %s%%", "en": "Energy efficiency: %s%%"},

        # Upgrades
        "block.upgrade.installed": {"de": "Upgrade installiert: %s", "en": "Upgrade installed: %s"},
        "block.upgrade.removed": {"de": "Upgrade entfernt: %s", "en": "Upgrade removed: %s"},
        "block.upgrade.slot": {"de": "Upgrade-Slot: %s/%s", "en": "Upgrade slot: %s/%s"},
        "block.upgrade.speed": {"de": "Geschwindigkeits-Upgrade", "en": "Speed Upgrade"},
        "block.upgrade.efficiency": {"de": "Effizienz-Upgrade", "en": "Efficiency Upgrade"},
        "block.upgrade.quality": {"de": "Qualitäts-Upgrade", "en": "Quality Upgrade"},
        "block.upgrade.capacity": {"de": "Kapazitäts-Upgrade", "en": "Capacity Upgrade"},
    })

    # EXTENDED ITEM TOOLTIPS (~30 Keys)
    new_keys.update({
        "item.tooltip.rarity": {"de": "Seltenheit: %s", "en": "Rarity: %s"},
        "item.tooltip.value": {"de": "Wert: %s", "en": "Value: %s"},
        "item.tooltip.weight": {"de": "Gewicht: %s kg", "en": "Weight: %s kg"},
        "item.tooltip.stackable": {"de": "Stapelbar: %s", "en": "Stackable: %s"},
        "item.tooltip.tradeable": {"de": "Handelbar", "en": "Tradeable"},
        "item.tooltip.bound": {"de": "Seelengebunden", "en": "Soulbound"},
        "item.tooltip.unique": {"de": "Einzigartig", "en": "Unique"},
        "item.tooltip.set_bonus": {"de": "Set-Bonus:", "en": "Set Bonus:"},
        "item.tooltip.requirement": {"de": "Voraussetzung:", "en": "Requirement:"},
        "item.tooltip.cooldown": {"de": "Abklingzeit: %s", "en": "Cooldown: %s"},
        "item.tooltip.charges": {"de": "Ladungen: %s/%s", "en": "Charges: %s/%s"},
        "item.tooltip.expires": {"de": "Läuft ab: %s", "en": "Expires: %s"},
        "item.tooltip.crafted_by": {"de": "Hergestellt von: %s", "en": "Crafted by: %s"},
        "item.tooltip.enchanted": {"de": "Verzaubert", "en": "Enchanted"},
        "item.tooltip.cursed": {"de": "Verflucht", "en": "Cursed"},

        # Drug-specific
        "item.drug.tooltip.strain": {"de": "Sorte: %s", "en": "Strain: %s"},
        "item.drug.tooltip.batch": {"de": "Charge: #%s", "en": "Batch: #%s"},
        "item.drug.tooltip.producer": {"de": "Produzent: %s", "en": "Producer: %s"},
        "item.drug.tooltip.production_date": {"de": "Hergestellt: %s", "en": "Produced: %s"},
        "item.drug.tooltip.shelf_life": {"de": "Haltbarkeit: %s Tage", "en": "Shelf life: %s days"},
    })

    # ACHIEVEMENT/QUEST MESSAGES (~20 Keys)
    new_keys.update({
        "achievement.category.drugs": {"de": "Drogenhandel", "en": "Drug Dealing"},
        "achievement.category.combat": {"de": "Kampf", "en": "Combat"},
        "achievement.category.economy": {"de": "Wirtschaft", "en": "Economy"},
        "achievement.category.exploration": {"de": "Erkundung", "en": "Exploration"},
        "achievement.category.social": {"de": "Soziales", "en": "Social"},

        "quest.started": {"de": "Quest gestartet: %s", "en": "Quest started: %s"},
        "quest.completed": {"de": "Quest abgeschlossen: %s", "en": "Quest completed: %s"},
        "quest.failed": {"de": "Quest fehlgeschlagen: %s", "en": "Quest failed: %s"},
        "quest.objective_complete": {"de": "Ziel erreicht: %s", "en": "Objective complete: %s"},
        "quest.reward_claimed": {"de": "Belohnung erhalten: %s", "en": "Reward claimed: %s"},
        "quest.progress": {"de": "Fortschritt: %s/%s", "en": "Progress: %s/%s"},
        "quest.time_limit": {"de": "Zeitlimit: %s", "en": "Time limit: %s"},
        "quest.daily": {"de": "Tägliche Quest", "en": "Daily Quest"},
        "quest.weekly": {"de": "Wöchentliche Quest", "en": "Weekly Quest"},
        "quest.repeatable": {"de": "Wiederholbar", "en": "Repeatable"},
    })

    return new_keys

def save_translations(de_trans, en_trans, new_keys):
    for key, data in new_keys.items():
        if key not in de_trans:
            de_trans[key] = data['de']
            en_trans[key] = data['en']

    de_sorted = dict(sorted(de_trans.items()))
    en_sorted = dict(sorted(en_trans.items()))

    with open(LANG_DIR / "de_de.json", 'w', encoding='utf-8') as f:
        json.dump(de_sorted, f, ensure_ascii=False, indent=2)

    with open(LANG_DIR / "en_us.json", 'w', encoding='utf-8') as f:
        json.dump(en_sorted, f, ensure_ascii=False, indent=2)

    return len(de_sorted), len(en_sorted)

def main():
    print("Final Translation Keys Addition")
    print("=" * 60)

    de_trans, en_trans = load_translations()
    initial = len(de_trans)
    print(f"Initial keys: {initial}")

    print("\nAdding final comprehensive keys...")
    new_keys = add_final_comprehensive_keys()
    print(f"New keys to add: {len(new_keys)}")

    stats = defaultdict(int)
    for key in new_keys.keys():
        category = key.split('.')[0]
        stats[category] += 1

    print("\nKeys by category:")
    for category, count in sorted(stats.items()):
        print(f"  {category:20s}: {count:4d} keys")

    de_count, en_count = save_translations(de_trans, en_trans, new_keys)

    print(f"\nFinal counts:")
    print(f"  DE: {de_count} keys (+{de_count - initial})")
    print(f"  EN: {en_count} keys (+{en_count - initial})")
    print(f"\n✓ Complete!")

if __name__ == "__main__":
    main()
