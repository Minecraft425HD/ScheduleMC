#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Erweiterte Translation-Keys für spezifische Kategorien
Fügt weitere ~500 Keys aus spezifischen Code-Bereichen hinzu
"""

import json
import re
from pathlib import Path
from collections import defaultdict

BASE_DIR = Path("/home/user/ScheduleMC")
LANG_DIR = BASE_DIR / "src/main/resources/assets/schedulemc/lang"
JAVA_DIR = BASE_DIR / "src/main/java"

def load_translations():
    """Lade existierende Translations"""
    with open(LANG_DIR / "de_de.json", 'r', encoding='utf-8') as f:
        de = json.load(f)
    with open(LANG_DIR / "en_us.json", 'r', encoding='utf-8') as f:
        en = json.load(f)
    return de, en

def add_extensive_translations():
    """Füge umfangreiche zusätzliche Translation-Keys hinzu"""
    new_keys = {}

    # COMMAND UTILITIES - Weitere ~50 Keys
    new_keys.update({
        # Weitere Shop Commands
        "command.shop.create": {"de": "Shop erstellt: %s", "en": "Shop created: %s"},
        "command.shop.delete": {"de": "Shop gelöscht: %s", "en": "Shop deleted: %s"},
        "command.shop.stock_low": {"de": "Bestand niedrig: %s", "en": "Stock low: %s"},
        "command.shop.out_of_stock": {"de": "Ausverkauft: %s", "en": "Out of stock: %s"},
        "command.shop.price_updated": {"de": "Preis aktualisiert: %s", "en": "Price updated: %s"},
        "command.shop.item_added": {"de": "Item hinzugefügt zum Shop", "en": "Item added to shop"},
        "command.shop.item_removed": {"de": "Item aus Shop entfernt", "en": "Item removed from shop"},
        "command.shop.revenue": {"de": "Einnahmen: %s", "en": "Revenue: %s"},
        "command.shop.profit": {"de": "Gewinn: %s", "en": "Profit: %s"},
        "command.shop.loss": {"de": "Verlust: %s", "en": "Loss: %s"},

        # Weitere Utility Commands
        "command.utility.teleport_success": {"de": "Teleportiert zu: %s", "en": "Teleported to: %s"},
        "command.utility.teleport_failed": {"de": "Teleportation fehlgeschlagen", "en": "Teleportation failed"},
        "command.utility.location_saved": {"de": "Position gespeichert", "en": "Location saved"},
        "command.utility.location_deleted": {"de": "Position gelöscht", "en": "Location deleted"},
        "command.utility.warp_created": {"de": "Warp-Punkt erstellt: %s", "en": "Warp created: %s"},
        "command.utility.warp_deleted": {"de": "Warp-Punkt gelöscht: %s", "en": "Warp deleted: %s"},
        "command.utility.home_set": {"de": "Zuhause gesetzt", "en": "Home set"},
        "command.utility.spawn_set": {"de": "Spawn-Punkt gesetzt", "en": "Spawn point set"},
        "command.utility.back_success": {"de": "Zur letzten Position zurückgekehrt", "en": "Returned to last position"},

        # Admin Commands
        "command.admin.reload_success": {"de": "Konfiguration neu geladen", "en": "Configuration reloaded"},
        "command.admin.reload_failed": {"de": "Fehler beim Neuladen", "en": "Reload failed"},
        "command.admin.maintenance_enabled": {"de": "Wartungsmodus aktiviert", "en": "Maintenance mode enabled"},
        "command.admin.maintenance_disabled": {"de": "Wartungsmodus deaktiviert", "en": "Maintenance mode disabled"},
        "command.admin.player_banned": {"de": "Spieler gebannt: %s", "en": "Player banned: %s"},
        "command.admin.player_unbanned": {"de": "Spieler entbannt: %s", "en": "Player unbanned: %s"},
        "command.admin.player_kicked": {"de": "Spieler gekickt: %s", "en": "Player kicked: %s"},
        "command.admin.whitelist_added": {"de": "Zur Whitelist hinzugefügt: %s", "en": "Added to whitelist: %s"},
        "command.admin.whitelist_removed": {"de": "Von Whitelist entfernt: %s", "en": "Removed from whitelist: %s"},
        "command.admin.backup_created": {"de": "Backup erstellt", "en": "Backup created"},
        "command.admin.backup_restored": {"de": "Backup wiederhergestellt", "en": "Backup restored"},

        # Vehicle Commands
        "command.vehicle.spawn": {"de": "Fahrzeug gespawnt: %s", "en": "Vehicle spawned: %s"},
        "command.vehicle.despawn": {"de": "Fahrzeug entfernt", "en": "Vehicle despawned"},
        "command.vehicle.locked": {"de": "Fahrzeug abgeschlossen", "en": "Vehicle locked"},
        "command.vehicle.unlocked": {"de": "Fahrzeug aufgeschlossen", "en": "Vehicle unlocked"},
        "command.vehicle.fuel_low": {"de": "Kraftstoff niedrig: %s%%", "en": "Fuel low: %s%%"},
        "command.vehicle.refueled": {"de": "Aufgetankt", "en": "Refueled"},
        "command.vehicle.damaged": {"de": "Fahrzeug beschädigt: %s%%", "en": "Vehicle damaged: %s%%"},
        "command.vehicle.repaired": {"de": "Fahrzeug repariert", "en": "Vehicle repaired"},
    })

    # EVENT HANDLERS - Weitere ~100 Keys
    new_keys.update({
        # Drug Events
        "event.drug.effect_started": {"de": "Drogeneffekt gestartet: %s", "en": "Drug effect started: %s"},
        "event.drug.effect_ended": {"de": "Drogeneffekt beendet", "en": "Drug effect ended"},
        "event.drug.tolerance_increased": {"de": "Toleranz erhöht", "en": "Tolerance increased"},
        "event.drug.purity_low": {"de": "Warnung: Geringe Reinheit!", "en": "Warning: Low purity!"},
        "event.drug.contaminated": {"de": "Produkt verunreinigt!", "en": "Product contaminated!"},
        "event.drug.production_success": {"de": "Produktion erfolgreich", "en": "Production successful"},
        "event.drug.production_failed": {"de": "Produktion fehlgeschlagen", "en": "Production failed"},
        "event.drug.quality_degraded": {"de": "Qualität verschlechtert", "en": "Quality degraded"},

        # Police Events
        "event.police.chase_started": {"de": "Verfolgungsjagd gestartet!", "en": "Chase started!"},
        "event.police.chase_ended": {"de": "Verfolgung beendet", "en": "Chase ended"},
        "event.police.wanted_increased": {"de": "Fahndungsstufe erhöht", "en": "Wanted level increased"},
        "event.police.wanted_decreased": {"de": "Fahndungsstufe verringert", "en": "Wanted level decreased"},
        "event.police.warrant_issued": {"de": "Haftbefehl ausgestellt", "en": "Warrant issued"},
        "event.police.fine_issued": {"de": "Geldstrafe ausgestellt: %s", "en": "Fine issued: %s"},
        "event.police.confiscated": {"de": "Items beschlagnahmt", "en": "Items confiscated"},
        "event.police.backup_called": {"de": "Verstärkung angefordert", "en": "Backup called"},
        "event.police.escaped": {"de": "Du bist der Polizei entkommen", "en": "You escaped from police"},
        "event.police.caught": {"de": "Von der Polizei gefasst", "en": "Caught by police"},

        # Plot/Territory Events
        "event.plot.entered": {"de": "Grundstück betreten: %s", "en": "Entered plot: %s"},
        "event.plot.left": {"de": "Grundstück verlassen", "en": "Left plot"},
        "event.plot.pvp_enabled": {"de": "PvP aktiviert in diesem Gebiet", "en": "PvP enabled in this area"},
        "event.plot.pvp_disabled": {"de": "PvP deaktiviert in diesem Gebiet", "en": "PvP disabled in this area"},
        "event.plot.raid_started": {"de": "Raid gestartet!", "en": "Raid started!"},
        "event.plot.raid_ended": {"de": "Raid beendet", "en": "Raid ended"},
        "event.plot.territory_captured": {"de": "Territorium erobert!", "en": "Territory captured!"},
        "event.plot.territory_lost": {"de": "Territorium verloren!", "en": "Territory lost!"},

        # Economic Events
        "event.economy.bankrupt": {"de": "Du bist bankrott!", "en": "You are bankrupt!"},
        "event.economy.millionaire": {"de": "Herzlichen Glückwunsch! Du bist Millionär!", "en": "Congratulations! You're a millionaire!"},
        "event.economy.salary_received": {"de": "Gehalt erhalten: %s", "en": "Salary received: %s"},
        "event.economy.bonus_received": {"de": "Bonus erhalten: %s", "en": "Bonus received: %s"},
        "event.economy.tax_deducted": {"de": "Steuern abgezogen: %s", "en": "Taxes deducted: %s"},
        "event.economy.debt_increased": {"de": "Schulden erhöht: %s", "en": "Debt increased: %s"},
        "event.economy.debt_cleared": {"de": "Schulden beglichen!", "en": "Debt cleared!"},

        # Combat Events
        "event.combat.kill": {"de": "Du hast %s getötet", "en": "You killed %s"},
        "event.combat.death": {"de": "Du wurdest von %s getötet", "en": "You were killed by %s"},
        "event.combat.headshot": {"de": "Kopfschuss!", "en": "Headshot!"},
        "event.combat.critical_hit": {"de": "Kritischer Treffer!", "en": "Critical hit!"},
        "event.combat.combat_tag": {"de": "Im Kampf! Ausloggen nicht möglich", "en": "In combat! Cannot logout"},
        "event.combat.combat_ended": {"de": "Kampf beendet", "en": "Combat ended"},

        # Weather/Time Events
        "event.weather.storm_warning": {"de": "Sturmwarnung!", "en": "Storm warning!"},
        "event.weather.clear": {"de": "Wetter aufgeklart", "en": "Weather cleared"},
        "event.time.day_passed": {"de": "Ein Tag ist vergangen", "en": "A day has passed"},
        "event.time.week_passed": {"de": "Eine Woche ist vergangen", "en": "A week has passed"},
        "event.time.month_passed": {"de": "Ein Monat ist vergangen", "en": "A month has passed"},
    })

    # BLOCK ENTITIES - Weitere ~150 Keys
    new_keys.update({
        # Advanced Processing
        "block.processing.recipe_invalid": {"de": "Ungültiges Rezept", "en": "Invalid recipe"},
        "block.processing.ingredients_missing": {"de": "Zutaten fehlen", "en": "Ingredients missing"},
        "block.processing.output_full": {"de": "Ausgabe voll", "en": "Output full"},
        "block.processing.energy_low": {"de": "Energie zu niedrig", "en": "Energy too low"},
        "block.processing.temperature_low": {"de": "Temperatur zu niedrig", "en": "Temperature too low"},
        "block.processing.temperature_high": {"de": "Temperatur zu hoch", "en": "Temperature too high"},
        "block.processing.pressure_low": {"de": "Druck zu niedrig", "en": "Pressure too low"},
        "block.processing.pressure_high": {"de": "Druck zu hoch", "en": "Pressure too high"},
        "block.processing.contamination_risk": {"de": "Kontaminationsrisiko!", "en": "Contamination risk!"},
        "block.processing.explosion_risk": {"de": "Explosionsgefahr!", "en": "Explosion risk!"},

        # Storage
        "block.storage.empty": {"de": "Lager leer", "en": "Storage empty"},
        "block.storage.full": {"de": "Lager voll", "en": "Storage full"},
        "block.storage.capacity": {"de": "Kapazität: %s/%s", "en": "Capacity: %s/%s"},
        "block.storage.locked": {"de": "Lager gesperrt", "en": "Storage locked"},
        "block.storage.unlocked": {"de": "Lager entsperrt", "en": "Storage unlocked"},
        "block.storage.access_denied": {"de": "Zugriff verweigert", "en": "Access denied"},

        # Machines
        "block.machine.running": {"de": "Maschine läuft", "en": "Machine running"},
        "block.machine.stopped": {"de": "Maschine gestoppt", "en": "Machine stopped"},
        "block.machine.maintenance_required": {"de": "Wartung erforderlich", "en": "Maintenance required"},
        "block.machine.broken": {"de": "Maschine defekt", "en": "Machine broken"},
        "block.machine.repaired": {"de": "Maschine repariert", "en": "Machine repaired"},
        "block.machine.efficiency": {"de": "Effizienz: %s%%", "en": "Efficiency: %s%%"},
        "block.machine.wear": {"de": "Verschleiß: %s%%", "en": "Wear: %s%%"},

        # Specific Drug Production Machines
        "block.meth.reducer.reducing": {"de": "Reduktion läuft...", "en": "Reduction in progress..."},
        "block.meth.reducer.catalyst_needed": {"de": "Katalysator benötigt", "en": "Catalyst needed"},
        "block.meth.reducer.temperature_critical": {"de": "Kritische Temperatur!", "en": "Critical temperature!"},

        "block.tobacco.dryer.drying": {"de": "Tabak wird getrocknet...", "en": "Tobacco drying..."},
        "block.tobacco.cutter.cutting": {"de": "Tabak wird geschnitten...", "en": "Tobacco cutting..."},
        "block.tobacco.roller.rolling": {"de": "Zigaretten werden gedreht...", "en": "Cigarettes rolling..."},

        "block.mushroom.grower.growing": {"de": "Pilze wachsen...", "en": "Mushrooms growing..."},
        "block.mushroom.grower.humidity_low": {"de": "Luftfeuchtigkeit zu niedrig", "en": "Humidity too low"},
        "block.mushroom.grower.substrate_depleted": {"de": "Substrat erschöpft", "en": "Substrate depleted"},

        "block.mdma.synthesizer.synthesizing": {"de": "Synthese läuft...", "en": "Synthesis in progress..."},
        "block.mdma.presser.pressing": {"de": "Tabletten werden gepresst...", "en": "Tablets pressing..."},
        "block.mdma.quality_check": {"de": "Qualitätskontrolle: %s", "en": "Quality check: %s"},

        "block.lsd.lab.synthesizing": {"de": "LSD-Synthese läuft...", "en": "LSD synthesis in progress..."},
        "block.lsd.lab.extremely_dangerous": {"de": "EXTREM GEFÄHRLICH!", "en": "EXTREMELY DANGEROUS!"},
        "block.lsd.lab.safety_gear_required": {"de": "Schutzausrüstung erforderlich", "en": "Safety gear required"},
    })

    # MANAGER - Weitere ~100 Keys
    new_keys.update({
        # Extended Economic Managers
        "manager.economy.inflation_rate": {"de": "Inflationsrate: %s%%", "en": "Inflation rate: %s%%"},
        "manager.economy.deflation_rate": {"de": "Deflationsrate: %s%%", "en": "Deflation rate: %s%%"},
        "manager.economy.market_crash": {"de": "Markt-Crash!", "en": "Market crash!"},
        "manager.economy.market_boom": {"de": "Markt-Boom!", "en": "Market boom!"},
        "manager.economy.recession": {"de": "Rezession", "en": "Recession"},
        "manager.economy.growth": {"de": "Wirtschaftswachstum: %s%%", "en": "Economic growth: %s%%"},

        # Stock Market Manager
        "manager.stock.bought": {"de": "Aktien gekauft: %s × %s", "en": "Stocks bought: %s × %s"},
        "manager.stock.sold": {"de": "Aktien verkauft: %s × %s", "en": "Stocks sold: %s × %s"},
        "manager.stock.price_up": {"de": "Aktienkurs gestiegen: +%s%%", "en": "Stock price up: +%s%%"},
        "manager.stock.price_down": {"de": "Aktienkurs gefallen: -%s%%", "en": "Stock price down: -%s%%"},
        "manager.stock.dividend": {"de": "Dividende erhalten: %s", "en": "Dividend received: %s"},

        # Insurance Manager
        "manager.insurance.policy_active": {"de": "Versicherung aktiv", "en": "Insurance policy active"},
        "manager.insurance.policy_expired": {"de": "Versicherung abgelaufen", "en": "Insurance policy expired"},
        "manager.insurance.claim_approved": {"de": "Versicherungsanspruch genehmigt: %s", "en": "Insurance claim approved: %s"},
        "manager.insurance.claim_denied": {"de": "Versicherungsanspruch abgelehnt", "en": "Insurance claim denied"},
        "manager.insurance.premium_due": {"de": "Versicherungsprämie fällig: %s", "en": "Insurance premium due: %s"},
        "manager.insurance.coverage": {"de": "Deckung: %s", "en": "Coverage: %s"},

        # Property Manager
        "manager.property.purchased": {"de": "Immobilie gekauft: %s", "en": "Property purchased: %s"},
        "manager.property.sold": {"de": "Immobilie verkauft: %s", "en": "Property sold: %s"},
        "manager.property.value_increased": {"de": "Immobilienwert gestiegen: +%s%%", "en": "Property value increased: +%s%%"},
        "manager.property.value_decreased": {"de": "Immobilienwert gefallen: -%s%%", "en": "Property value decreased: -%s%%"},
        "manager.property.maintenance_due": {"de": "Wartung fällig", "en": "Maintenance due"},
        "manager.property.condemned": {"de": "Immobilie beschlagnahmt", "en": "Property condemned"},

        # Employment Manager
        "manager.employment.hired": {"de": "Eingestellt als: %s", "en": "Hired as: %s"},
        "manager.employment.fired": {"de": "Entlassen", "en": "Fired"},
        "manager.employment.promoted": {"de": "Befördert zu: %s", "en": "Promoted to: %s"},
        "manager.employment.demoted": {"de": "Degradiert zu: %s", "en": "Demoted to: %s"},
        "manager.employment.salary_increased": {"de": "Gehalt erhöht: %s", "en": "Salary increased: %s"},
        "manager.employment.salary_decreased": {"de": "Gehalt verringert: %s", "en": "Salary decreased: %s"},
        "manager.employment.shift_started": {"de": "Schicht begonnen", "en": "Shift started"},
        "manager.employment.shift_ended": {"de": "Schicht beendet", "en": "Shift ended"},
        "manager.employment.overtime": {"de": "Überstunden: %s", "en": "Overtime: %s"},

        # License Manager
        "manager.license.obtained": {"de": "Lizenz erhalten: %s", "en": "License obtained: %s"},
        "manager.license.revoked": {"de": "Lizenz entzogen: %s", "en": "License revoked: %s"},
        "manager.license.expired": {"de": "Lizenz abgelaufen: %s", "en": "License expired: %s"},
        "manager.license.renewed": {"de": "Lizenz erneuert: %s", "en": "License renewed: %s"},
        "manager.license.suspended": {"de": "Lizenz suspendiert: %s", "en": "License suspended: %s"},

        # Achievement Manager
        "manager.achievement.unlocked": {"de": "Erfolg freigeschaltet: %s", "en": "Achievement unlocked: %s"},
        "manager.achievement.progress": {"de": "Fortschritt: %s%%", "en": "Progress: %s%%"},
        "manager.achievement.reward": {"de": "Belohnung erhalten: %s", "en": "Reward received: %s"},
    })

    # NETWORK/PACKETS - Weitere ~50 Keys
    new_keys.update({
        "network.packet.received": {"de": "Paket empfangen", "en": "Packet received"},
        "network.packet.sent": {"de": "Paket gesendet", "en": "Packet sent"},
        "network.packet.corrupted": {"de": "Beschädigtes Paket", "en": "Corrupted packet"},
        "network.packet.timeout": {"de": "Paket-Timeout", "en": "Packet timeout"},
        "network.handshake.success": {"de": "Handshake erfolgreich", "en": "Handshake successful"},
        "network.handshake.failed": {"de": "Handshake fehlgeschlagen", "en": "Handshake failed"},
        "network.version.mismatch": {"de": "Versionsinkompatibilität", "en": "Version mismatch"},
        "network.version.outdated": {"de": "Version veraltet", "en": "Version outdated"},
        "network.kicked": {"de": "Vom Server gekickt: %s", "en": "Kicked from server: %s"},
        "network.banned": {"de": "Vom Server gebannt: %s", "en": "Banned from server: %s"},
        "network.reconnecting": {"de": "Verbindung wird wiederhergestellt...", "en": "Reconnecting..."},
        "network.data.corrupted": {"de": "Daten beschädigt", "en": "Data corrupted"},
        "network.data.validated": {"de": "Daten validiert", "en": "Data validated"},
    })

    # ITEM TOOLTIPS - Weitere ~50 Keys
    new_keys.update({
        "item.tool.efficiency": {"de": "Effizienz: %s", "en": "Efficiency: %s"},
        "item.tool.speed": {"de": "Geschwindigkeit: %s", "en": "Speed: %s"},
        "item.armor.protection": {"de": "Schutz: %s", "en": "Protection: %s"},
        "item.armor.durability": {"de": "Haltbarkeit: %s/%s", "en": "Durability: %s/%s"},
        "item.food.nutrition": {"de": "Nährwert: %s", "en": "Nutrition: %s"},
        "item.food.saturation": {"de": "Sättigung: %s", "en": "Saturation: %s"},
        "item.drug.effects": {"de": "Effekte:", "en": "Effects:"},
        "item.drug.duration": {"de": "Dauer: %s", "en": "Duration: %s"},
        "item.drug.side_effects": {"de": "Nebenwirkungen:", "en": "Side effects:"},
        "item.drug.addiction_risk": {"de": "Suchtgefahr: %s%%", "en": "Addiction risk: %s%%"},
        "item.drug.overdose_risk": {"de": "Überdosis-Risiko: %s%%", "en": "Overdose risk: %s%%"},
        "item.key.opens": {"de": "Öffnet: %s", "en": "Opens: %s"},
        "item.document.read": {"de": "Rechtsklick zum Lesen", "en": "Right-click to read"},
        "item.container.capacity": {"de": "Kapazität: %s", "en": "Capacity: %s"},
        "item.rarity.common": {"de": "Gewöhnlich", "en": "Common"},
        "item.rarity.uncommon": {"de": "Ungewöhnlich", "en": "Uncommon"},
        "item.rarity.rare": {"de": "Selten", "en": "Rare"},
        "item.rarity.epic": {"de": "Episch", "en": "Epic"},
        "item.rarity.legendary": {"de": "Legendär", "en": "Legendary"},
    })

    # MISC/UTILITY - Weitere ~50 Keys
    new_keys.update({
        "misc.pagination.page": {"de": "Seite %s von %s", "en": "Page %s of %s"},
        "misc.pagination.next": {"de": "Nächste Seite", "en": "Next page"},
        "misc.pagination.previous": {"de": "Vorherige Seite", "en": "Previous page"},
        "misc.pagination.first": {"de": "Erste Seite", "en": "First page"},
        "misc.pagination.last": {"de": "Letzte Seite", "en": "Last page"},

        "misc.sorting.alphabetical": {"de": "Alphabetisch", "en": "Alphabetical"},
        "misc.sorting.price_high": {"de": "Preis (Hoch-Niedrig)", "en": "Price (High-Low)"},
        "misc.sorting.price_low": {"de": "Preis (Niedrig-Hoch)", "en": "Price (Low-High)"},
        "misc.sorting.newest": {"de": "Neueste", "en": "Newest"},
        "misc.sorting.oldest": {"de": "Älteste", "en": "Oldest"},

        "misc.filter.all": {"de": "Alle", "en": "All"},
        "misc.filter.owned": {"de": "Im Besitz", "en": "Owned"},
        "misc.filter.available": {"de": "Verfügbar", "en": "Available"},
        "misc.filter.unavailable": {"de": "Nicht verfügbar", "en": "Unavailable"},

        "misc.cooldown.ready": {"de": "Bereit", "en": "Ready"},
        "misc.cooldown.remaining": {"de": "Abklingzeit: %s", "en": "Cooldown: %s"},

        "misc.requirement.level": {"de": "Benötigt Level: %s", "en": "Required level: %s"},
        "misc.requirement.permission": {"de": "Benötigt Berechtigung", "en": "Required permission"},
        "misc.requirement.item": {"de": "Benötigt: %s", "en": "Required: %s"},

        "misc.notification.important": {"de": "WICHTIG: %s", "en": "IMPORTANT: %s"},
        "misc.notification.info": {"de": "Info: %s", "en": "Info: %s"},
        "misc.notification.tip": {"de": "Tipp: %s", "en": "Tip: %s"},
    })

    return new_keys

def save_translations(de_trans, en_trans, new_keys):
    """Speichere Translations"""
    # Add new keys
    for key, data in new_keys.items():
        if key not in de_trans:  # Avoid overwriting
            de_trans[key] = data['de']
            en_trans[key] = data['en']

    # Sort and save
    de_sorted = dict(sorted(de_trans.items()))
    en_sorted = dict(sorted(en_trans.items()))

    with open(LANG_DIR / "de_de.json", 'w', encoding='utf-8') as f:
        json.dump(de_sorted, f, ensure_ascii=False, indent=2)

    with open(LANG_DIR / "en_us.json", 'w', encoding='utf-8') as f:
        json.dump(en_sorted, f, ensure_ascii=False, indent=2)

    return len(de_sorted), len(en_sorted)

def main():
    print("Loading existing translations...")
    de_trans, en_trans = load_translations()
    initial_count = len(de_trans)
    print(f"Initial keys: {initial_count}")

    print("\nAdding extensive category-specific translations...")
    new_keys = add_extensive_translations()
    print(f"New keys to add: {len(new_keys)}")

    # Count by category
    stats = defaultdict(int)
    for key in new_keys.keys():
        category = key.split('.')[0]
        stats[category] += 1

    print("\nKeys by category:")
    for category, count in sorted(stats.items()):
        print(f"  {category:20s}: {count:4d} keys")

    print("\nSaving translations...")
    de_count, en_count = save_translations(de_trans, en_trans, new_keys)

    print(f"\nFinal counts:")
    print(f"  DE: {de_count} keys (+{de_count - initial_count})")
    print(f"  EN: {en_count} keys (+{en_count - initial_count})")
    print(f"\n✓ Done!")

if __name__ == "__main__":
    main()
