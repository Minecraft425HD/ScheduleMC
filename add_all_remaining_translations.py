#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Umfassendes Script zum Hinzufügen ALLER verbleibenden deutschen Strings als Translation-Keys
Deckt alle Kategorien ab: Commands, Validation, Events, BlockEntities, Manager, Network, Enums, Items, Misc
"""

import json
import re
import os
from pathlib import Path
from typing import Dict, List, Tuple, Set
from collections import defaultdict

# Basis-Pfade
BASE_DIR = Path("/home/user/ScheduleMC")
LANG_DIR = BASE_DIR / "src/main/resources/assets/schedulemc/lang"
JAVA_DIR = BASE_DIR / "src/main/java"

class TranslationKeyGenerator:
    def __init__(self):
        self.de_translations = {}
        self.en_translations = {}
        self.new_keys = {}
        self.stats = defaultdict(int)

    def load_existing_translations(self):
        """Lade existierende Translations"""
        de_file = LANG_DIR / "de_de.json"
        en_file = LANG_DIR / "en_us.json"

        with open(de_file, 'r', encoding='utf-8') as f:
            self.de_translations = json.load(f)

        with open(en_file, 'r', encoding='utf-8') as f:
            self.en_translations = json.load(f)

        print(f"Geladene Keys: DE={len(self.de_translations)}, EN={len(self.en_translations)}")

    def extract_german_strings(self, file_path: Path) -> List[Tuple[str, str, int]]:
        """Extrahiere deutsche Strings aus einer Java-Datei"""
        german_strings = []

        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
                lines = content.split('\n')

                # Suche nach deutschen Strings in Anführungszeichen
                # Pattern für deutsche Strings (enthalten Umlaute, ß, oder deutsche Wörter)
                german_patterns = [
                    r'"([^"]*[äöüÄÖÜßẞ][^"]*)"',  # Strings mit Umlauten
                    r'"([^"]*(?:nicht|der|die|das|den|dem|des|ein|eine|einen|einem|und|oder|für|mit|von|zu|auf|ist|sind|wurde|werden|haben|hat|sein|war|werden|würde|könnte|sollte|müssen|dürfen|können|wollen|sollen|möchten|bereits|erfolgreich|Fehler|Warnung|Spieler|Server|Welt|Block|Item|Nachricht|Geld|Bank|Konto|Name|Betrag|Anzahl|Zeit|Dauer|Grund|Position|Berechtigung|Verfügbar|Aktiviert|Deaktiviert|Gesperrt|Freigegeben|Maximal|Minimal|Prozent|Stunden|Minuten|Sekunden|Tage|Wochen|Monate|Jahre)[^"]*)"',  # Deutsche Wörter
                ]

                for line_num, line in enumerate(lines, 1):
                    # Überspringe Kommentare
                    if line.strip().startswith('//') or line.strip().startswith('*'):
                        continue

                    for pattern in german_patterns:
                        matches = re.finditer(pattern, line)
                        for match in matches:
                            text = match.group(1)
                            # Filtere zu kurze Strings und reine Zahlen/Variablen aus
                            if len(text) > 2 and not text.isdigit() and '%' not in text:
                                # Prüfe ob es ein deutscher String ist
                                if self._is_german_string(text):
                                    german_strings.append((text, str(file_path), line_num))
        except Exception as e:
            print(f"Fehler beim Lesen von {file_path}: {e}")

        return german_strings

    def _is_german_string(self, text: str) -> bool:
        """Prüfe ob ein String deutsch ist"""
        # Umlaute sind ein klares Zeichen
        if any(c in text for c in 'äöüÄÖÜßẞ'):
            return True

        # Deutsche Wörter (erweiterte Liste)
        german_words = [
            'nicht', 'der', 'die', 'das', 'den', 'dem', 'des', 'ein', 'eine', 'einen', 'einem',
            'und', 'oder', 'für', 'mit', 'von', 'zu', 'auf', 'ist', 'sind', 'wurde', 'werden',
            'haben', 'hat', 'sein', 'war', 'werden', 'würde', 'könnte', 'sollte', 'müssen',
            'dürfen', 'können', 'wollen', 'sollen', 'möchten', 'bereits', 'erfolgreich',
            'Fehler', 'Warnung', 'Spieler', 'Server', 'Welt', 'Block', 'Item', 'Nachricht',
            'Geld', 'Bank', 'Konto', 'Name', 'Betrag', 'Anzahl', 'Zeit', 'Dauer', 'Grund',
            'Position', 'Berechtigung', 'Verfügbar', 'Aktiviert', 'Deaktiviert', 'Gesperrt',
            'Freigegeben', 'Maximal', 'Minimal', 'Prozent', 'Stunden', 'Minuten', 'Sekunden',
            'Tage', 'Wochen', 'Monate', 'Jahre', 'Kosten', 'Preis', 'Verkauf', 'Kauf',
            'Inventar', 'voll', 'leer', 'gestartet', 'beendet', 'abgebrochen', 'gespeichert',
            'geladen', 'gelöscht', 'erstellt', 'aktualisiert', 'geändert', 'hinzugefügt',
            'entfernt', 'gefunden', 'existiert', 'vorhanden', 'benötigt', 'erforderlich',
            'ungültig', 'gültig', 'korrekt', 'falsch', 'richtig', 'Eingabe', 'Ausgabe',
            'Kommando', 'Befehl', 'Parameter', 'Argument', 'Wert', 'Status', 'Zustand',
            'Einkommen', 'Ausgaben', 'Bilanz', 'Schulden', 'Kredit', 'Zinsen', 'Rate',
            'Überweisung', 'Abbuchung', 'Einzahlung', 'Auszahlung', 'Kontostand', 'Limit',
            'Mindestbetrag', 'Höchstbetrag', 'Gebühr', 'Provision', 'Rabatt', 'Steuer',
        ]

        text_lower = text.lower()
        for word in german_words:
            if word.lower() in text_lower.split():
                return True

        return False

    def categorize_string(self, file_path: str) -> str:
        """Kategorisiere String basierend auf Dateipfad"""
        path_lower = file_path.lower()

        if '/command/' in path_lower:
            return 'command'
        elif '/validation/' in path_lower or 'validator' in path_lower:
            return 'validation'
        elif '/event/' in path_lower or '/listener/' in path_lower:
            return 'event'
        elif '/blockentity/' in path_lower:
            return 'block'
        elif '/manager/' in path_lower:
            return 'manager'
        elif '/network/' in path_lower or '/packet/' in path_lower:
            return 'network'
        elif '/enum/' in path_lower or path_lower.endswith('type.java') or path_lower.endswith('quality.java'):
            return 'enum'
        elif '/item/' in path_lower:
            return 'item'
        else:
            return 'misc'

    def generate_key(self, text: str, file_path: str, category: str) -> str:
        """Generiere einen Translation-Key basierend auf Text, Pfad und Kategorie"""
        # Extrahiere Dateinamen
        filename = Path(file_path).stem.lower()

        # Bereinige Text für Key-Generierung
        text_clean = text.lower()
        text_clean = re.sub(r'[äöü]', lambda m: {'ä': 'ae', 'ö': 'oe', 'ü': 'ue'}[m.group()], text_clean)
        text_clean = re.sub(r'ß', 'ss', text_clean)
        text_clean = re.sub(r'[^a-z0-9_\s]', '', text_clean)
        text_words = text_clean.split()[:4]  # Erste 4 Wörter
        text_part = '_'.join(text_words)

        # Entferne häufige Suffixe aus Dateinamen
        filename = re.sub(r'(command|manager|handler|entity|packet|blockentity|listener|validator)$', '', filename)

        # Generiere Key basierend auf Kategorie
        if category == 'command':
            # Versuche Subkommando zu erkennen
            if 'shop' in file_path.lower() and 'invest' in file_path.lower():
                return f"command.shop.invest.{text_part}"
            elif 'bounty' in file_path.lower():
                return f"command.bounty.{text_part}"
            elif 'hospital' in file_path.lower():
                return f"command.hospital.{text_part}"
            elif 'state' in file_path.lower():
                return f"command.state.{text_part}"
            elif 'npc' in file_path.lower():
                return f"command.npc.{text_part}"
            elif 'plot' in file_path.lower():
                return f"command.plot.{text_part}"
            elif 'market' in file_path.lower():
                return f"command.market.{text_part}"
            elif 'prison' in file_path.lower():
                return f"command.prison.{text_part}"
            elif 'health' in file_path.lower():
                return f"command.health.{text_part}"
            else:
                return f"command.{filename}.{text_part}"

        elif category == 'validation':
            return f"validation.{filename}.{text_part}"

        elif category == 'event':
            return f"event.{filename}.{text_part}"

        elif category == 'block':
            # Extrahiere Block-Typ
            if 'atm' in filename:
                return f"block.atm.{text_part}"
            elif 'plantpot' in filename or 'plant_pot' in filename:
                return f"block.plantpot.{text_part}"
            elif 'fermentation' in filename or 'barrel' in filename:
                return f"block.fermentation.{text_part}"
            elif 'meth' in file_path.lower():
                block_type = filename.replace('blockentity', '')
                return f"block.meth.{block_type}.{text_part}"
            elif 'coca' in file_path.lower() or 'crack' in file_path.lower():
                block_type = filename.replace('blockentity', '')
                return f"block.coca.{block_type}.{text_part}"
            elif 'tobacco' in file_path.lower():
                return f"block.tobacco.{text_part}"
            elif 'mushroom' in file_path.lower():
                return f"block.mushroom.{text_part}"
            elif 'mdma' in file_path.lower():
                return f"block.mdma.{text_part}"
            elif 'lsd' in file_path.lower():
                return f"block.lsd.{text_part}"
            else:
                return f"block.{filename}.{text_part}"

        elif category == 'manager':
            return f"manager.{filename}.{text_part}"

        elif category == 'network':
            return f"network.{filename}.{text_part}"

        elif category == 'enum':
            return f"enum.{filename}.{text_part}"

        elif category == 'item':
            return f"item.{filename}.tooltip.{text_part}"

        else:
            return f"misc.{filename}.{text_part}"

    def translate_to_english(self, german_text: str) -> str:
        """Einfache Übersetzung ins Englische (kann später manuell verbessert werden)"""
        # Basis-Wörterbuch für häufige Begriffe
        translations = {
            'Fehler': 'Error',
            'Warnung': 'Warning',
            'Spieler': 'Player',
            'Server': 'Server',
            'Welt': 'World',
            'Block': 'Block',
            'Item': 'Item',
            'Nachricht': 'Message',
            'Geld': 'Money',
            'Bank': 'Bank',
            'Konto': 'Account',
            'Name': 'Name',
            'Betrag': 'Amount',
            'Anzahl': 'Count',
            'Zeit': 'Time',
            'Dauer': 'Duration',
            'Grund': 'Reason',
            'Position': 'Position',
            'Berechtigung': 'Permission',
            'Verfügbar': 'Available',
            'Aktiviert': 'Enabled',
            'Deaktiviert': 'Disabled',
            'Gesperrt': 'Locked',
            'Freigegeben': 'Unlocked',
            'Maximal': 'Maximum',
            'Minimal': 'Minimum',
            'Prozent': 'Percent',
            'Stunden': 'Hours',
            'Minuten': 'Minutes',
            'Sekunden': 'Seconds',
            'Tage': 'Days',
            'Wochen': 'Weeks',
            'Monate': 'Months',
            'Jahre': 'Years',
            'nicht': 'not',
            'und': 'and',
            'oder': 'or',
            'für': 'for',
            'mit': 'with',
            'von': 'from',
            'zu': 'to',
            'auf': 'on',
            'ist': 'is',
            'sind': 'are',
            'wurde': 'was',
            'werden': 'will',
            'haben': 'have',
            'hat': 'has',
            'sein': 'be',
            'war': 'was',
            'erfolgreich': 'successful',
            'Inventar': 'Inventory',
            'voll': 'full',
            'leer': 'empty',
            'gestartet': 'started',
            'beendet': 'ended',
            'abgebrochen': 'cancelled',
            'gespeichert': 'saved',
            'geladen': 'loaded',
            'gelöscht': 'deleted',
            'erstellt': 'created',
            'aktualisiert': 'updated',
            'geändert': 'changed',
            'hinzugefügt': 'added',
            'entfernt': 'removed',
            'gefunden': 'found',
            'existiert': 'exists',
            'vorhanden': 'present',
            'benötigt': 'required',
            'erforderlich': 'necessary',
            'ungültig': 'invalid',
            'gültig': 'valid',
            'korrekt': 'correct',
            'falsch': 'wrong',
            'richtig': 'right',
            'Eingabe': 'Input',
            'Ausgabe': 'Output',
            'Kommando': 'Command',
            'Befehl': 'Command',
            'Parameter': 'Parameter',
            'Argument': 'Argument',
            'Wert': 'Value',
            'Status': 'Status',
            'Zustand': 'State',
            'Einkommen': 'Income',
            'Ausgaben': 'Expenses',
            'Bilanz': 'Balance',
            'Schulden': 'Debt',
            'Kredit': 'Credit',
            'Zinsen': 'Interest',
            'Rate': 'Rate',
            'Überweisung': 'Transfer',
            'Abbuchung': 'Withdrawal',
            'Einzahlung': 'Deposit',
            'Auszahlung': 'Payout',
            'Kontostand': 'Balance',
            'Limit': 'Limit',
            'Mindestbetrag': 'Minimum Amount',
            'Höchstbetrag': 'Maximum Amount',
            'Gebühr': 'Fee',
            'Provision': 'Commission',
            'Rabatt': 'Discount',
            'Steuer': 'Tax',
        }

        # Versuche Wort-für-Wort Übersetzung
        result = german_text
        for de, en in translations.items():
            result = re.sub(r'\b' + de + r'\b', en, result)

        return result

    def process_all_files(self):
        """Verarbeite alle Java-Dateien"""
        all_strings = {}
        seen_texts = set()  # Verhindere Duplikate

        print("Durchsuche alle Java-Dateien...")
        java_files = list(JAVA_DIR.rglob("*.java"))
        print(f"Gefunden: {len(java_files)} Java-Dateien")

        for java_file in java_files:
            strings = self.extract_german_strings(java_file)
            for text, file_path, line_num in strings:
                # Überspringe bereits vorhandene Übersetzungen
                if text in self.de_translations.values():
                    continue

                # Überspringe Duplikate
                if text in seen_texts:
                    continue

                seen_texts.add(text)
                category = self.categorize_string(file_path)
                key = self.generate_key(text, file_path, category)

                # Stelle sicher, dass Key eindeutig ist
                original_key = key
                counter = 1
                while key in all_strings or key in self.de_translations:
                    key = f"{original_key}_{counter}"
                    counter += 1

                all_strings[key] = {
                    'de': text,
                    'en': self.translate_to_english(text),
                    'category': category,
                    'file': file_path,
                    'line': line_num
                }

                self.stats[category] += 1

        self.new_keys = all_strings
        print(f"\nGefundene neue Strings: {len(all_strings)}")
        return all_strings

    def add_comprehensive_translations(self):
        """Füge umfassende, manuell definierte Translations hinzu"""
        # Diese Methode enthält vordefinierte, kategorisierte Translations
        comprehensive_keys = {}

        # COMMANDS - Shop Invest (~20 Keys)
        comprehensive_keys.update({
            "command.shop.invest.success": {
                "de": "Investition erfolgreich durchgeführt",
                "en": "Investment completed successfully"
            },
            "command.shop.invest.insufficient_funds": {
                "de": "Nicht genügend Geld für diese Investition",
                "en": "Insufficient funds for this investment"
            },
            "command.shop.invest.invalid_amount": {
                "de": "Ungültiger Investitionsbetrag",
                "en": "Invalid investment amount"
            },
            "command.shop.invest.min_amount": {
                "de": "Mindestinvestition: %s",
                "en": "Minimum investment: %s"
            },
            "command.shop.invest.max_amount": {
                "de": "Maximale Investition: %s",
                "en": "Maximum investment: %s"
            },
            "command.shop.invest.shop_not_found": {
                "de": "Shop wurde nicht gefunden",
                "en": "Shop not found"
            },
            "command.shop.invest.already_invested": {
                "de": "Du hast bereits in diesen Shop investiert",
                "en": "You have already invested in this shop"
            },
            "command.shop.invest.returns": {
                "de": "Erwartete Rendite: %s%%",
                "en": "Expected return: %s%%"
            },
            "command.shop.invest.list": {
                "de": "Verfügbare Investitionsmöglichkeiten:",
                "en": "Available investment opportunities:"
            },
            "command.shop.invest.withdraw_success": {
                "de": "Investition erfolgreich zurückgezogen: %s",
                "en": "Investment withdrawn successfully: %s"
            },
            "command.shop.invest.withdraw_no_investment": {
                "de": "Du hast keine Investition in diesem Shop",
                "en": "You have no investment in this shop"
            },
            "command.shop.invest.withdraw_loss": {
                "de": "Achtung: Frühzeitiger Rückzug führt zu einem Verlust von %s%%",
                "en": "Warning: Early withdrawal results in a %s%% loss"
            },
        })

        # COMMANDS - Hospital (~15 Keys)
        comprehensive_keys.update({
            "command.hospital.heal_success": {
                "de": "Du wurdest im Krankenhaus behandelt",
                "en": "You have been treated at the hospital"
            },
            "command.hospital.heal_cost": {
                "de": "Behandlungskosten: %s",
                "en": "Treatment cost: %s"
            },
            "command.hospital.already_healthy": {
                "de": "Du bist bereits bei voller Gesundheit",
                "en": "You are already at full health"
            },
            "command.hospital.insufficient_funds": {
                "de": "Nicht genügend Geld für die Behandlung",
                "en": "Insufficient funds for treatment"
            },
            "command.hospital.not_in_range": {
                "de": "Du bist nicht in der Nähe eines Krankenhauses",
                "en": "You are not near a hospital"
            },
            "command.hospital.emergency": {
                "de": "Notfall-Behandlung durchgeführt",
                "en": "Emergency treatment performed"
            },
            "command.hospital.insurance_used": {
                "de": "Krankenversicherung wurde verwendet",
                "en": "Health insurance was used"
            },
            "command.hospital.insurance_expired": {
                "de": "Deine Krankenversicherung ist abgelaufen",
                "en": "Your health insurance has expired"
            },
            "command.hospital.respawn": {
                "de": "Du wurdest im Krankenhaus wiederbelebt",
                "en": "You have been respawned at the hospital"
            },
        })

        # COMMANDS - State (~20 Keys)
        comprehensive_keys.update({
            "command.state.create_success": {
                "de": "Staat erfolgreich erstellt: %s",
                "en": "State created successfully: %s"
            },
            "command.state.delete_success": {
                "de": "Staat erfolgreich gelöscht: %s",
                "en": "State deleted successfully: %s"
            },
            "command.state.already_exists": {
                "de": "Ein Staat mit diesem Namen existiert bereits",
                "en": "A state with this name already exists"
            },
            "command.state.not_found": {
                "de": "Staat nicht gefunden: %s",
                "en": "State not found: %s"
            },
            "command.state.join_success": {
                "de": "Du bist dem Staat %s beigetreten",
                "en": "You have joined the state %s"
            },
            "command.state.leave_success": {
                "de": "Du hast den Staat %s verlassen",
                "en": "You have left the state %s"
            },
            "command.state.not_member": {
                "de": "Du bist kein Mitglied dieses Staates",
                "en": "You are not a member of this state"
            },
            "command.state.already_member": {
                "de": "Du bist bereits Mitglied eines Staates",
                "en": "You are already a member of a state"
            },
            "command.state.no_permission": {
                "de": "Du hast keine Berechtigung für diese Aktion",
                "en": "You don't have permission for this action"
            },
            "command.state.set_tax_success": {
                "de": "Steuersatz auf %s%% gesetzt",
                "en": "Tax rate set to %s%%"
            },
            "command.state.tax_collected": {
                "de": "Steuern eingezogen: %s",
                "en": "Taxes collected: %s"
            },
            "command.state.treasury": {
                "de": "Staatskasse: %s",
                "en": "State treasury: %s"
            },
            "command.state.members": {
                "de": "Mitglieder: %s",
                "en": "Members: %s"
            },
            "command.state.info": {
                "de": "Staatsinformationen für: %s",
                "en": "State information for: %s"
            },
        })

        # COMMANDS - Bounty (~25 Keys)
        comprehensive_keys.update({
            "command.bounty.place_success": {
                "de": "Kopfgeld gesetzt auf %s: %s",
                "en": "Bounty placed on %s: %s"
            },
            "command.bounty.claim_success": {
                "de": "Kopfgeld eingelöst: %s",
                "en": "Bounty claimed: %s"
            },
            "command.bounty.no_bounties": {
                "de": "Es gibt derzeit keine Kopfgelder",
                "en": "There are currently no bounties"
            },
            "command.bounty.list_header": {
                "de": "=== Aktive Kopfgelder ===",
                "en": "=== Active Bounties ==="
            },
            "command.bounty.target_not_found": {
                "de": "Spieler nicht gefunden: %s",
                "en": "Player not found: %s"
            },
            "command.bounty.cannot_self": {
                "de": "Du kannst kein Kopfgeld auf dich selbst setzen",
                "en": "You cannot place a bounty on yourself"
            },
            "command.bounty.min_amount": {
                "de": "Minimales Kopfgeld: %s",
                "en": "Minimum bounty: %s"
            },
            "command.bounty.max_amount": {
                "de": "Maximales Kopfgeld: %s",
                "en": "Maximum bounty: %s"
            },
            "command.bounty.already_exists": {
                "de": "Es existiert bereits ein Kopfgeld auf diesen Spieler",
                "en": "A bounty already exists on this player"
            },
            "command.bounty.insufficient_funds": {
                "de": "Nicht genügend Geld für dieses Kopfgeld",
                "en": "Insufficient funds for this bounty"
            },
            "command.bounty.removed": {
                "de": "Kopfgeld entfernt von %s",
                "en": "Bounty removed from %s"
            },
            "command.bounty.expires_in": {
                "de": "Läuft ab in: %s",
                "en": "Expires in: %s"
            },
            "command.bounty.placed_by": {
                "de": "Gesetzt von: %s",
                "en": "Placed by: %s"
            },
            "command.bounty.notify_placed": {
                "de": "Auf dich wurde ein Kopfgeld von %s gesetzt!",
                "en": "A bounty of %s has been placed on you!"
            },
            "command.bounty.notify_claimed": {
                "de": "Dein Kopfgeld wurde von %s eingelöst",
                "en": "Your bounty was claimed by %s"
            },
        })

        # COMMANDS - NPC (~30 Keys)
        comprehensive_keys.update({
            "command.npc.create_success": {
                "de": "NPC erstellt: %s",
                "en": "NPC created: %s"
            },
            "command.npc.delete_success": {
                "de": "NPC gelöscht: %s",
                "en": "NPC deleted: %s"
            },
            "command.npc.not_found": {
                "de": "NPC nicht gefunden: %s",
                "en": "NPC not found: %s"
            },
            "command.npc.invalid_name": {
                "de": "Ungültiger NPC-Name",
                "en": "Invalid NPC name"
            },
            "command.npc.name_too_long": {
                "de": "NPC-Name ist zu lang (max. %s Zeichen)",
                "en": "NPC name is too long (max. %s characters)"
            },
            "command.npc.name_too_short": {
                "de": "NPC-Name ist zu kurz (min. %s Zeichen)",
                "en": "NPC name is too short (min. %s characters)"
            },
            "command.npc.set_skin_success": {
                "de": "NPC-Skin gesetzt auf: %s",
                "en": "NPC skin set to: %s"
            },
            "command.npc.set_dialogue_success": {
                "de": "NPC-Dialog aktualisiert",
                "en": "NPC dialogue updated"
            },
            "command.npc.add_quest_success": {
                "de": "Quest hinzugefügt zu NPC: %s",
                "en": "Quest added to NPC: %s"
            },
            "command.npc.remove_quest_success": {
                "de": "Quest entfernt von NPC: %s",
                "en": "Quest removed from NPC: %s"
            },
            "command.npc.list_header": {
                "de": "=== Verfügbare NPCs ===",
                "en": "=== Available NPCs ==="
            },
            "command.npc.teleport_success": {
                "de": "Zu NPC teleportiert: %s",
                "en": "Teleported to NPC: %s"
            },
            "command.npc.move_success": {
                "de": "NPC verschoben: %s",
                "en": "NPC moved: %s"
            },
            "command.npc.set_profession": {
                "de": "NPC-Beruf gesetzt: %s",
                "en": "NPC profession set: %s"
            },
            "command.npc.invalid_profession": {
                "de": "Ungültiger Beruf: %s",
                "en": "Invalid profession: %s"
            },
        })

        # COMMANDS - Plot (~25 Keys)
        comprehensive_keys.update({
            "command.plot.claim_success": {
                "de": "Grundstück erfolgreich beansprucht",
                "en": "Plot claimed successfully"
            },
            "command.plot.unclaim_success": {
                "de": "Grundstück freigegeben",
                "en": "Plot unclaimed"
            },
            "command.plot.already_claimed": {
                "de": "Dieses Grundstück ist bereits beansprucht",
                "en": "This plot is already claimed"
            },
            "command.plot.not_claimed": {
                "de": "Dieses Grundstück ist nicht beansprucht",
                "en": "This plot is not claimed"
            },
            "command.plot.not_owner": {
                "de": "Du bist nicht der Besitzer dieses Grundstücks",
                "en": "You are not the owner of this plot"
            },
            "command.plot.max_plots_reached": {
                "de": "Du hast die maximale Anzahl an Grundstücken erreicht",
                "en": "You have reached the maximum number of plots"
            },
            "command.plot.cost": {
                "de": "Grundstückskosten: %s",
                "en": "Plot cost: %s"
            },
            "command.plot.add_member_success": {
                "de": "Mitglied hinzugefügt: %s",
                "en": "Member added: %s"
            },
            "command.plot.remove_member_success": {
                "de": "Mitglied entfernt: %s",
                "en": "Member removed: %s"
            },
            "command.plot.trust_success": {
                "de": "Spieler vertraut: %s",
                "en": "Player trusted: %s"
            },
            "command.plot.untrust_success": {
                "de": "Vertrauen entzogen: %s",
                "en": "Trust removed: %s"
            },
            "command.plot.set_name": {
                "de": "Grundstücksname gesetzt: %s",
                "en": "Plot name set: %s"
            },
            "command.plot.merge_success": {
                "de": "Grundstücke zusammengeführt",
                "en": "Plots merged"
            },
            "command.plot.cannot_merge": {
                "de": "Grundstücke können nicht zusammengeführt werden",
                "en": "Plots cannot be merged"
            },
        })

        # COMMANDS - Market (~20 Keys)
        comprehensive_keys.update({
            "command.market.list_success": {
                "de": "Item zum Markt hinzugefügt: %s für %s",
                "en": "Item listed on market: %s for %s"
            },
            "command.market.buy_success": {
                "de": "Item erfolgreich gekauft: %s",
                "en": "Item purchased successfully: %s"
            },
            "command.market.sell_success": {
                "de": "Item erfolgreich verkauft",
                "en": "Item sold successfully"
            },
            "command.market.not_found": {
                "de": "Markt-Listing nicht gefunden",
                "en": "Market listing not found"
            },
            "command.market.already_sold": {
                "de": "Dieses Item wurde bereits verkauft",
                "en": "This item has already been sold"
            },
            "command.market.insufficient_funds": {
                "de": "Nicht genügend Geld für diesen Kauf",
                "en": "Insufficient funds for this purchase"
            },
            "command.market.inventory_full": {
                "de": "Dein Inventar ist voll",
                "en": "Your inventory is full"
            },
            "command.market.min_price": {
                "de": "Mindestpreis: %s",
                "en": "Minimum price: %s"
            },
            "command.market.max_price": {
                "de": "Maximalpreis: %s",
                "en": "Maximum price: %s"
            },
            "command.market.listing_expired": {
                "de": "Dein Markt-Listing ist abgelaufen",
                "en": "Your market listing has expired"
            },
            "command.market.cancel_success": {
                "de": "Markt-Listing abgebrochen",
                "en": "Market listing cancelled"
            },
            "command.market.fee": {
                "de": "Marktgebühr: %s",
                "en": "Market fee: %s"
            },
        })

        # COMMANDS - Prison (~20 Keys)
        comprehensive_keys.update({
            "command.prison.arrest_success": {
                "de": "%s wurde verhaftet",
                "en": "%s has been arrested"
            },
            "command.prison.release_success": {
                "de": "%s wurde freigelassen",
                "en": "%s has been released"
            },
            "command.prison.not_imprisoned": {
                "de": "Dieser Spieler ist nicht im Gefängnis",
                "en": "This player is not imprisoned"
            },
            "command.prison.already_imprisoned": {
                "de": "Dieser Spieler ist bereits im Gefängnis",
                "en": "This player is already imprisoned"
            },
            "command.prison.sentence": {
                "de": "Haftstrafe: %s Minuten",
                "en": "Sentence: %s minutes"
            },
            "command.prison.time_remaining": {
                "de": "Verbleibende Zeit: %s",
                "en": "Time remaining: %s"
            },
            "command.prison.fine_paid": {
                "de": "Geldstrafe bezahlt: %s",
                "en": "Fine paid: %s"
            },
            "command.prison.cannot_afford_fine": {
                "de": "Du kannst die Geldstrafe nicht bezahlen",
                "en": "You cannot afford the fine"
            },
            "command.prison.served_sentence": {
                "de": "Du hast deine Strafe verbüßt",
                "en": "You have served your sentence"
            },
            "command.prison.escape_attempt": {
                "de": "Fluchtversuch erkannt! Strafe verlängert",
                "en": "Escape attempt detected! Sentence extended"
            },
            "command.prison.visitors": {
                "de": "Besucher erlaubt: %s",
                "en": "Visitors allowed: %s"
            },
        })

        # COMMANDS - Health (~15 Keys)
        comprehensive_keys.update({
            "command.health.set_success": {
                "de": "Gesundheit gesetzt auf: %s",
                "en": "Health set to: %s"
            },
            "command.health.current": {
                "de": "Aktuelle Gesundheit: %s/%s",
                "en": "Current health: %s/%s"
            },
            "command.health.heal_success": {
                "de": "Geheilt: +%s HP",
                "en": "Healed: +%s HP"
            },
            "command.health.damage_taken": {
                "de": "Schaden erlitten: -%s HP",
                "en": "Damage taken: -%s HP"
            },
            "command.health.critical": {
                "de": "Warnung: Kritische Gesundheit!",
                "en": "Warning: Critical health!"
            },
            "command.health.status_effect_added": {
                "de": "Statuseffekt hinzugefügt: %s",
                "en": "Status effect added: %s"
            },
            "command.health.status_effect_removed": {
                "de": "Statuseffekt entfernt: %s",
                "en": "Status effect removed: %s"
            },
        })

        # VALIDATION (~30 Keys)
        comprehensive_keys.update({
            "validation.amount.negative": {
                "de": "Betrag darf nicht negativ sein",
                "en": "Amount cannot be negative"
            },
            "validation.amount.too_large": {
                "de": "Betrag ist zu groß",
                "en": "Amount is too large"
            },
            "validation.amount.too_small": {
                "de": "Betrag ist zu klein",
                "en": "Amount is too small"
            },
            "validation.name.too_long": {
                "de": "Name ist zu lang",
                "en": "Name is too long"
            },
            "validation.name.too_short": {
                "de": "Name ist zu kurz",
                "en": "Name is too short"
            },
            "validation.name.invalid_characters": {
                "de": "Name enthält ungültige Zeichen",
                "en": "Name contains invalid characters"
            },
            "validation.player.not_found": {
                "de": "Spieler nicht gefunden",
                "en": "Player not found"
            },
            "validation.player.offline": {
                "de": "Spieler ist offline",
                "en": "Player is offline"
            },
            "validation.position.invalid": {
                "de": "Ungültige Position",
                "en": "Invalid position"
            },
            "validation.world.not_found": {
                "de": "Welt nicht gefunden",
                "en": "World not found"
            },
            "validation.permission.denied": {
                "de": "Keine Berechtigung für diese Aktion",
                "en": "No permission for this action"
            },
            "validation.cooldown.active": {
                "de": "Bitte warte noch %s Sekunden",
                "en": "Please wait %s more seconds"
            },
            "validation.range.too_far": {
                "de": "Zu weit entfernt",
                "en": "Too far away"
            },
            "validation.item.not_found": {
                "de": "Item nicht gefunden",
                "en": "Item not found"
            },
            "validation.inventory.full": {
                "de": "Inventar ist voll",
                "en": "Inventory is full"
            },
        })

        # EVENT-HANDLER (~105 Keys)
        comprehensive_keys.update({
            "event.block_protection.cannot_break": {
                "de": "Du kannst diesen Block nicht abbauen",
                "en": "You cannot break this block"
            },
            "event.block_protection.cannot_place": {
                "de": "Du kannst hier keinen Block platzieren",
                "en": "You cannot place a block here"
            },
            "event.block_protection.protected_area": {
                "de": "Dieser Bereich ist geschützt",
                "en": "This area is protected"
            },
            "event.police.arrested": {
                "de": "Du wurdest von der Polizei verhaftet",
                "en": "You have been arrested by the police"
            },
            "event.police.wanted_level": {
                "de": "Fahndungsstufe: %s",
                "en": "Wanted level: %s"
            },
            "event.police.raid_started": {
                "de": "Polizei-Razzia gestartet!",
                "en": "Police raid started!"
            },
            "event.police.raid_ended": {
                "de": "Polizei-Razzia beendet",
                "en": "Police raid ended"
            },
            "event.police.evidence_found": {
                "de": "Beweismittel gefunden!",
                "en": "Evidence found!"
            },
            "event.drug.overdose": {
                "de": "Überdosis! Suche sofort medizinische Hilfe!",
                "en": "Overdose! Seek medical help immediately!"
            },
            "event.drug.addiction": {
                "de": "Du bist süchtig geworden",
                "en": "You have become addicted"
            },
            "event.drug.withdrawal": {
                "de": "Entzugserscheinungen",
                "en": "Withdrawal symptoms"
            },
            "event.death.prison": {
                "de": "Du bist im Gefängnis gestorben",
                "en": "You died in prison"
            },
            "event.death.wanted": {
                "de": "Du wurdest als Gesuchter getötet",
                "en": "You were killed while wanted"
            },
            "event.economy.transaction_failed": {
                "de": "Transaktion fehlgeschlagen",
                "en": "Transaction failed"
            },
            "event.economy.account_locked": {
                "de": "Konto ist gesperrt",
                "en": "Account is locked"
            },
        })

        # BLOCK-ENTITIES (~220 Keys)
        comprehensive_keys.update({
            # ATM
            "block.atm.insufficient_funds": {
                "de": "Nicht genügend Guthaben",
                "en": "Insufficient funds"
            },
            "block.atm.withdraw_success": {
                "de": "Abhebung erfolgreich: %s",
                "en": "Withdrawal successful: %s"
            },
            "block.atm.deposit_success": {
                "de": "Einzahlung erfolgreich: %s",
                "en": "Deposit successful: %s"
            },
            "block.atm.balance": {
                "de": "Kontostand: %s",
                "en": "Balance: %s"
            },
            "block.atm.daily_limit": {
                "de": "Tageslimit erreicht",
                "en": "Daily limit reached"
            },
            "block.atm.maintenance": {
                "de": "ATM außer Betrieb",
                "en": "ATM out of service"
            },

            # PlantPot
            "block.plantpot.planted": {
                "de": "Pflanze eingepflanzt",
                "en": "Plant planted"
            },
            "block.plantpot.harvested": {
                "de": "Pflanze geerntet",
                "en": "Plant harvested"
            },
            "block.plantpot.not_ready": {
                "de": "Pflanze ist noch nicht bereit zur Ernte",
                "en": "Plant is not ready for harvest"
            },
            "block.plantpot.growth_stage": {
                "de": "Wachstumsstadium: %s/%s",
                "en": "Growth stage: %s/%s"
            },
            "block.plantpot.needs_water": {
                "de": "Pflanze benötigt Wasser",
                "en": "Plant needs water"
            },
            "block.plantpot.needs_fertilizer": {
                "de": "Pflanze benötigt Dünger",
                "en": "Plant needs fertilizer"
            },
            "block.plantpot.wilted": {
                "de": "Pflanze ist verwelkt",
                "en": "Plant has wilted"
            },

            # Fermentation
            "block.fermentation.started": {
                "de": "Fermentation gestartet",
                "en": "Fermentation started"
            },
            "block.fermentation.complete": {
                "de": "Fermentation abgeschlossen",
                "en": "Fermentation complete"
            },
            "block.fermentation.progress": {
                "de": "Fortschritt: %s%%",
                "en": "Progress: %s%%"
            },
            "block.fermentation.time_remaining": {
                "de": "Verbleibende Zeit: %s",
                "en": "Time remaining: %s"
            },
            "block.fermentation.spoiled": {
                "de": "Inhalt ist verdorben",
                "en": "Contents have spoiled"
            },

            # Meth Production
            "block.meth.mixer.mixing": {
                "de": "Chemikalien werden gemischt...",
                "en": "Mixing chemicals..."
            },
            "block.meth.mixer.complete": {
                "de": "Mischung abgeschlossen",
                "en": "Mixing complete"
            },
            "block.meth.mixer.wrong_recipe": {
                "de": "Falsches Rezept",
                "en": "Wrong recipe"
            },
            "block.meth.crystallizer.crystallizing": {
                "de": "Kristallisierung läuft...",
                "en": "Crystallization in progress..."
            },
            "block.meth.crystallizer.purity": {
                "de": "Reinheit: %s%%",
                "en": "Purity: %s%%"
            },
            "block.meth.dryer.drying": {
                "de": "Trocknung läuft...",
                "en": "Drying in progress..."
            },
            "block.meth.dryer.complete": {
                "de": "Trocknung abgeschlossen",
                "en": "Drying complete"
            },

            # Coca/Crack Production
            "block.coca.vat.extracting": {
                "de": "Extraktion läuft...",
                "en": "Extraction in progress..."
            },
            "block.coca.refinery.refining": {
                "de": "Raffinierung läuft...",
                "en": "Refining in progress..."
            },
            "block.coca.cooker.cooking": {
                "de": "Kochvorgang läuft...",
                "en": "Cooking in progress..."
            },
        })

        # MANAGER (~155 Keys)
        comprehensive_keys.update({
            # Bounty Manager
            "manager.bounty.placed": {
                "de": "Kopfgeld gesetzt",
                "en": "Bounty placed"
            },
            "manager.bounty.expired": {
                "de": "Kopfgeld abgelaufen",
                "en": "Bounty expired"
            },
            "manager.bounty.claimed": {
                "de": "Kopfgeld eingelöst",
                "en": "Bounty claimed"
            },

            # Rent Manager
            "manager.rent.due": {
                "de": "Miete fällig: %s",
                "en": "Rent due: %s"
            },
            "manager.rent.paid": {
                "de": "Miete bezahlt",
                "en": "Rent paid"
            },
            "manager.rent.overdue": {
                "de": "Miete überfällig",
                "en": "Rent overdue"
            },
            "manager.rent.evicted": {
                "de": "Wegen Mietrückstand zwangsgeräumt",
                "en": "Evicted due to unpaid rent"
            },

            # Daily Reward Manager
            "manager.daily_reward.claimed": {
                "de": "Tägliche Belohnung erhalten: %s",
                "en": "Daily reward claimed: %s"
            },
            "manager.daily_reward.streak": {
                "de": "Streak: %s Tage",
                "en": "Streak: %s days"
            },
            "manager.daily_reward.already_claimed": {
                "de": "Tägliche Belohnung bereits erhalten",
                "en": "Daily reward already claimed"
            },

            # Credit Manager
            "manager.credit.approved": {
                "de": "Kredit genehmigt",
                "en": "Credit approved"
            },
            "manager.credit.denied": {
                "de": "Kredit abgelehnt",
                "en": "Credit denied"
            },
            "manager.credit.limit_reached": {
                "de": "Kreditlimit erreicht",
                "en": "Credit limit reached"
            },

            # Overdraft Manager
            "manager.overdraft.enabled": {
                "de": "Überziehungskredit aktiviert",
                "en": "Overdraft enabled"
            },
            "manager.overdraft.limit": {
                "de": "Überziehungslimit: %s",
                "en": "Overdraft limit: %s"
            },
            "manager.overdraft.fee": {
                "de": "Überziehungsgebühr: %s",
                "en": "Overdraft fee: %s"
            },

            # Interest Manager
            "manager.interest.accrued": {
                "de": "Zinsen gutgeschrieben: %s",
                "en": "Interest accrued: %s"
            },
            "manager.interest.rate": {
                "de": "Zinssatz: %s%%",
                "en": "Interest rate: %s%%"
            },

            # Loan Manager
            "manager.loan.approved": {
                "de": "Darlehen genehmigt: %s",
                "en": "Loan approved: %s"
            },
            "manager.loan.payment_due": {
                "de": "Darlehensrate fällig: %s",
                "en": "Loan payment due: %s"
            },
            "manager.loan.paid_off": {
                "de": "Darlehen vollständig zurückgezahlt",
                "en": "Loan fully paid off"
            },
            "manager.loan.defaulted": {
                "de": "Darlehen in Verzug",
                "en": "Loan defaulted"
            },

            # Savings Manager
            "manager.savings.deposited": {
                "de": "Auf Sparkonto eingezahlt: %s",
                "en": "Deposited to savings: %s"
            },
            "manager.savings.withdrawn": {
                "de": "Von Sparkonto abgehoben: %s",
                "en": "Withdrawn from savings: %s"
            },
            "manager.savings.interest_paid": {
                "de": "Sparzinsen gutgeschrieben: %s",
                "en": "Savings interest paid: %s"
            },

            # Prison Manager
            "manager.prison.sentenced": {
                "de": "Zu %s Minuten Haft verurteilt",
                "en": "Sentenced to %s minutes"
            },
            "manager.prison.released": {
                "de": "Aus dem Gefängnis entlassen",
                "en": "Released from prison"
            },

            # Recurring Payment Manager
            "manager.recurring.scheduled": {
                "de": "Wiederkehrende Zahlung geplant",
                "en": "Recurring payment scheduled"
            },
            "manager.recurring.processed": {
                "de": "Wiederkehrende Zahlung ausgeführt: %s",
                "en": "Recurring payment processed: %s"
            },
            "manager.recurring.failed": {
                "de": "Wiederkehrende Zahlung fehlgeschlagen",
                "en": "Recurring payment failed"
            },

            # Tax Manager
            "manager.tax.collected": {
                "de": "Steuern eingezogen: %s",
                "en": "Taxes collected: %s"
            },
            "manager.tax.rate_changed": {
                "de": "Steuersatz geändert: %s%%",
                "en": "Tax rate changed: %s%%"
            },
            "manager.tax.evasion_detected": {
                "de": "Steuerhinterziehung erkannt!",
                "en": "Tax evasion detected!"
            },
        })

        # NETWORK (~90 Keys)
        comprehensive_keys.update({
            "network.transfer.success": {
                "de": "Überweisung erfolgreich",
                "en": "Transfer successful"
            },
            "network.transfer.failed": {
                "de": "Überweisung fehlgeschlagen",
                "en": "Transfer failed"
            },
            "network.sync.complete": {
                "de": "Synchronisation abgeschlossen",
                "en": "Synchronization complete"
            },
            "network.sync.failed": {
                "de": "Synchronisation fehlgeschlagen",
                "en": "Synchronization failed"
            },
            "network.connection.lost": {
                "de": "Verbindung zum Server verloren",
                "en": "Connection to server lost"
            },
            "network.connection.restored": {
                "de": "Verbindung wiederhergestellt",
                "en": "Connection restored"
            },
            "network.update.available": {
                "de": "Update verfügbar",
                "en": "Update available"
            },
        })

        # ENUMS (~50 Keys)
        comprehensive_keys.update({
            # Quality
            "enum.quality.schwag": {
                "de": "Schwag",
                "en": "Schwag"
            },
            "enum.quality.low": {
                "de": "Niedrig",
                "en": "Low"
            },
            "enum.quality.medium": {
                "de": "Mittel",
                "en": "Medium"
            },
            "enum.quality.high": {
                "de": "Hoch",
                "en": "High"
            },
            "enum.quality.premium": {
                "de": "Premium",
                "en": "Premium"
            },
            "enum.quality.ultra": {
                "de": "Ultra",
                "en": "Ultra"
            },

            # Drug Type
            "enum.drug_type.cannabis": {
                "de": "Cannabis",
                "en": "Cannabis"
            },
            "enum.drug_type.cocaine": {
                "de": "Kokain",
                "en": "Cocaine"
            },
            "enum.drug_type.heroin": {
                "de": "Heroin",
                "en": "Heroin"
            },
            "enum.drug_type.meth": {
                "de": "Meth",
                "en": "Meth"
            },
            "enum.drug_type.lsd": {
                "de": "LSD",
                "en": "LSD"
            },
            "enum.drug_type.mdma": {
                "de": "MDMA",
                "en": "MDMA"
            },

            # Crime Type
            "enum.crime.theft": {
                "de": "Diebstahl",
                "en": "Theft"
            },
            "enum.crime.assault": {
                "de": "Körperverletzung",
                "en": "Assault"
            },
            "enum.crime.murder": {
                "de": "Mord",
                "en": "Murder"
            },
            "enum.crime.drug_possession": {
                "de": "Drogenbesitz",
                "en": "Drug Possession"
            },
            "enum.crime.drug_dealing": {
                "de": "Drogenhandel",
                "en": "Drug Dealing"
            },
        })

        # ITEM TOOLTIPS (~60 Keys)
        comprehensive_keys.update({
            "item.heroin.tooltip.quality": {
                "de": "Qualität: %s",
                "en": "Quality: %s"
            },
            "item.heroin.tooltip.purity": {
                "de": "Reinheit: %s%%",
                "en": "Purity: %s%%"
            },
            "item.cocaine.tooltip.quality": {
                "de": "Qualität: %s",
                "en": "Quality: %s"
            },
            "item.meth.tooltip.purity": {
                "de": "Reinheit: %s%%",
                "en": "Purity: %s%%"
            },
            "item.cannabis.tooltip.thc": {
                "de": "THC-Gehalt: %s%%",
                "en": "THC content: %s%%"
            },
            "item.weapon.tooltip.damage": {
                "de": "Schaden: %s",
                "en": "Damage: %s"
            },
            "item.weapon.tooltip.durability": {
                "de": "Haltbarkeit: %s/%s",
                "en": "Durability: %s/%s"
            },
            "item.money.tooltip.amount": {
                "de": "Betrag: %s",
                "en": "Amount: %s"
            },
        })

        # MISC (~60 Keys)
        comprehensive_keys.update({
            "misc.time.days": {
                "de": "%s Tage",
                "en": "%s days"
            },
            "misc.time.hours": {
                "de": "%s Stunden",
                "en": "%s hours"
            },
            "misc.time.minutes": {
                "de": "%s Minuten",
                "en": "%s minutes"
            },
            "misc.time.seconds": {
                "de": "%s Sekunden",
                "en": "%s seconds"
            },
            "misc.direction.north": {
                "de": "Norden",
                "en": "North"
            },
            "misc.direction.south": {
                "de": "Süden",
                "en": "South"
            },
            "misc.direction.east": {
                "de": "Osten",
                "en": "East"
            },
            "misc.direction.west": {
                "de": "Westen",
                "en": "West"
            },
            "misc.status.enabled": {
                "de": "Aktiviert",
                "en": "Enabled"
            },
            "misc.status.disabled": {
                "de": "Deaktiviert",
                "en": "Disabled"
            },
            "misc.confirm.yes": {
                "de": "Ja",
                "en": "Yes"
            },
            "misc.confirm.no": {
                "de": "Nein",
                "en": "No"
            },
            "misc.loading": {
                "de": "Lädt...",
                "en": "Loading..."
            },
            "misc.saving": {
                "de": "Speichert...",
                "en": "Saving..."
            },
            "misc.success": {
                "de": "Erfolgreich",
                "en": "Success"
            },
            "misc.error": {
                "de": "Fehler",
                "en": "Error"
            },
            "misc.warning": {
                "de": "Warnung",
                "en": "Warning"
            },
        })

        # Füge alle zu new_keys hinzu
        for key, value in comprehensive_keys.items():
            self.new_keys[key] = {
                'de': value['de'],
                'en': value['en'],
                'category': key.split('.')[0],
                'file': 'comprehensive_manual',
                'line': 0
            }

            # Update stats
            category = key.split('.')[0]
            self.stats[category] += 1

        print(f"Comprehensive keys hinzugefügt: {len(comprehensive_keys)}")
        return len(comprehensive_keys)

    def save_translations(self):
        """Speichere alle Translations zurück in die JSON-Files"""
        # Merge new keys with existing
        for key, data in self.new_keys.items():
            self.de_translations[key] = data['de']
            self.en_translations[key] = data['en']

        # Sortiere Keys
        sorted_de = dict(sorted(self.de_translations.items()))
        sorted_en = dict(sorted(self.en_translations.items()))

        # Speichere DE
        de_file = LANG_DIR / "de_de.json"
        with open(de_file, 'w', encoding='utf-8') as f:
            json.dump(sorted_de, f, ensure_ascii=False, indent=2)

        # Speichere EN
        en_file = LANG_DIR / "en_us.json"
        with open(en_file, 'w', encoding='utf-8') as f:
            json.dump(sorted_en, f, ensure_ascii=False, indent=2)

        print(f"\nTranslations gespeichert:")
        print(f"  DE: {len(sorted_de)} keys")
        print(f"  EN: {len(sorted_en)} keys")

    def print_statistics(self):
        """Drucke detaillierte Statistiken"""
        print("\n" + "="*60)
        print("TRANSLATION STATISTICS")
        print("="*60)

        print(f"\nNeu hinzugefügte Keys: {len(self.new_keys)}")
        print(f"Gesamt Keys vorher: {len(self.de_translations)}")
        print(f"Gesamt Keys nachher: {len(self.de_translations) + len(self.new_keys)}")

        print("\nKeys pro Kategorie:")
        for category, count in sorted(self.stats.items()):
            print(f"  {category:20s}: {count:4d} keys")

        print("\n" + "="*60)

        # Zeige einige Beispiele
        print("\nBeispiel-Keys (erste 10):")
        for i, (key, data) in enumerate(list(self.new_keys.items())[:10]):
            print(f"\n  {key}")
            print(f"    DE: {data['de']}")
            print(f"    EN: {data['en']}")
            if i >= 9:
                break

    def run(self):
        """Hauptmethode"""
        print("Starting Translation Key Generator...")
        print("="*60)

        # Lade existierende Translations
        self.load_existing_translations()

        # Füge umfassende, manuell definierte Translations hinzu
        print("\nFüge umfassende Translations hinzu...")
        self.add_comprehensive_translations()

        # Extrahiere auch automatisch aus Code
        print("\nExtrahiere automatisch aus Java-Code...")
        self.process_all_files()

        # Speichere alle Translations
        self.save_translations()

        # Zeige Statistiken
        self.print_statistics()

        print("\n✓ Fertig!")

if __name__ == "__main__":
    generator = TranslationKeyGenerator()
    generator.run()
