#!/usr/bin/env python3
"""
Script zum Refaktorieren aller NPC Network Packet Klassen
Extrahiert deutsche Component.literal() Strings und konvertiert sie zu Component.translatable()
"""

import json
import re
import os
from typing import Dict, List, Tuple

# Dateipfade
BASE_DIR = "/home/user/ScheduleMC"
JAVA_DIR = f"{BASE_DIR}/src/main/java/de/rolandsw/schedulemc/npc/network"
LANG_DIR = f"{BASE_DIR}/src/main/resources/assets/schedulemc/lang"

# Zu refaktorierende Dateien
PACKET_FILES = [
    "ApplyCreditLoanPacket.java",
    "BankDepositPacket.java",
    "BankTransferPacket.java",
    "BankWithdrawPacket.java",
    "CreateRecurringPaymentPacket.java",
    "DeleteRecurringPaymentPacket.java",
    "RepayCreditLoanPacket.java",
    "ResumeRecurringPaymentPacket.java",
    "SavingsDepositPacket.java",
    "SavingsWithdrawPacket.java",
    "StockTradePacket.java"
]

# Translation Keys für NPC Network Packets
TRANSLATIONS = {
    # ApplyCreditLoanPacket
    "network.credit.invalid_loan_type": {
        "de_de": "§c§lFehler: §7Ungültiger Kredittyp!",
        "en_us": "§c§lError: §7Invalid loan type!"
    },
    "network.credit.already_active_loan": {
        "de_de": "§c§lFehler: §7Du hast bereits einen aktiven Kredit!",
        "en_us": "§c§lError: §7You already have an active loan!"
    },
    "network.credit.repay_first": {
        "de_de": "§7Bezahle diesen zuerst zurück.",
        "en_us": "§7Please repay it first."
    },
    "network.credit.loan_rejected_insufficient": {
        "de_de": "§c§lKredit abgelehnt!",
        "en_us": "§c§lLoan Rejected!"
    },
    "network.credit.insufficient_credit_score": {
        "de_de": "§7Deine Bonität reicht nicht aus.",
        "en_us": "§7Your credit score is insufficient."
    },
    "network.credit.required_rating": {
        "de_de": "§7Benötigt: §e",
        "en_us": "§7Required: §e"
    },
    "network.credit.current_rating": {
        "de_de": "§7Aktuell: §c",
        "en_us": "§7Current: §c"
    },
    "network.credit.type_label": {
        "de_de": "Typ: ",
        "en_us": "Type: "
    },
    "network.credit.effective_interest": {
        "de_de": "Effektiver Zinssatz: ",
        "en_us": "Effective Interest Rate: "
    },
    "network.credit.duration_label": {
        "de_de": "Laufzeit: ",
        "en_us": "Duration: "
    },
    "network.credit.days_suffix": {
        "de_de": " Tage",
        "en_us": " Days"
    },
    "network.credit.loan_rejected_unknown": {
        "de_de": "§c§lKredit abgelehnt!",
        "en_us": "§c§lLoan Rejected!"
    },
    "network.credit.unknown_error": {
        "de_de": "§7Unbekannter Fehler bei der Kreditvergabe.",
        "en_us": "§7Unknown error during loan approval."
    },

    # BankDepositPacket
    "network.bank.maximum_label": {
        "de_de": "Maximum: ",
        "en_us": "Maximum: "
    },
    "network.bank.cash_label": {
        "de_de": "Bargeld: ",
        "en_us": "Cash: "
    },
    "network.bank.remaining_cash": {
        "de_de": "Restliches Bargeld: ",
        "en_us": "Remaining Cash: "
    },
    "network.bank.deposit_error": {
        "de_de": "⚠ Fehler bei der Einzahlung!",
        "en_us": "⚠ Deposit Error!"
    },

    # BankTransferPacket
    "network.bank.limit_label": {
        "de_de": "Limit: ",
        "en_us": "Limit: "
    },
    "network.bank.remaining_daily_limit": {
        "de_de": "Verbleibendes Tageslimit: ",
        "en_us": "Remaining Daily Limit: "
    },
    "network.bank.money_received": {
        "de_de": "GELD ERHALTEN",
        "en_us": "MONEY RECEIVED"
    },
    "network.bank.from_label": {
        "de_de": "Von: ",
        "en_us": "From: "
    },

    # BankWithdrawPacket
    "network.bank.new_cash": {
        "de_de": "Neues Bargeld: ",
        "en_us": "New Cash: "
    },

    # CreateRecurringPaymentPacket
    "network.bank.max_count_reached": {
        "de_de": "• Maximale Anzahl erreicht",
        "en_us": "• Maximum count reached"
    },

    # DeleteRecurringPaymentPacket & ResumeRecurringPaymentPacket
    "network.bank.id_label": {
        "de_de": "ID: ",
        "en_us": "ID: "
    },

    # RepayCreditLoanPacket
    "network.credit.no_active_loan": {
        "de_de": "§c§lFehler: §7Du hast keinen aktiven Kredit!",
        "en_us": "§c§lError: §7You don't have an active loan!"
    },
    "network.credit.insufficient_funds_repay": {
        "de_de": "§c§lNicht genug Geld!",
        "en_us": "§c§lInsufficient Funds!"
    },
    "network.credit.required_label": {
        "de_de": "§7Benötigt: §c",
        "en_us": "§7Required: §c"
    },
    "network.credit.balance_label": {
        "de_de": "§7Kontostand: §e",
        "en_us": "§7Balance: §e"
    },
    "network.credit.debt_free": {
        "de_de": "§a§lDu bist nun schuldenfrei!",
        "en_us": "§a§lYou are now debt-free!"
    },
    "network.credit.repayment_error": {
        "de_de": "§c§lFehler: §7Kredit konnte nicht zurückgezahlt werden!",
        "en_us": "§c§lError: §7Loan could not be repaid!"
    },

    # ResumeRecurringPaymentPacket
    "network.bank.order_resumed": {
        "de_de": "Dauerauftrag fortgesetzt",
        "en_us": "Standing Order Resumed"
    },

    # SavingsWithdrawPacket
    "network.bank.remaining_days": {
        "de_de": "Verbleibende Tage: ",
        "en_us": "Remaining Days: "
    },
    "network.bank.hint_label": {
        "de_de": "Hinweis: ",
        "en_us": "Notice: "
    },
    "network.bank.early_withdrawal_penalty": {
        "de_de": "Vorzeitige Abhebung: -10% Strafe",
        "en_us": "Early Withdrawal: -10% Penalty"
    },
    "network.bank.withdrawal_error": {
        "de_de": "⚠ Fehler bei der Abhebung!",
        "en_us": "⚠ Withdrawal Error!"
    },

    # StockTradePacket
    "network.stock.free_slots_suffix": {
        "de_de": " freie Slots",
        "en_us": " free slots"
    },
    "network.stock.total_cost": {
        "de_de": "Gesamtkosten: ",
        "en_us": "Total Cost: "
    }
}


def add_translations_to_json():
    """Fügt die Translation Keys zu beiden Language Files hinzu"""

    for lang_code in ["de_de", "en_us"]:
        file_path = f"{LANG_DIR}/{lang_code}.json"

        # Lade existierende Translations
        with open(file_path, 'r', encoding='utf-8') as f:
            data = json.load(f)

        # Füge neue Keys hinzu
        added_count = 0
        for key, translations in TRANSLATIONS.items():
            if key not in data:
                data[key] = translations[lang_code]
                added_count += 1
                print(f"  + {key}: {translations[lang_code]}")

        # Sortiere Keys alphabetisch
        data = dict(sorted(data.items()))

        # Speichere zurück
        with open(file_path, 'w', encoding='utf-8') as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
            f.write('\n')  # Trailing newline

        print(f"\n✓ {lang_code}.json: {added_count} neue Keys hinzugefügt")


def main():
    print("=" * 60)
    print("REFAKTORIERUNG: NPC Network Packet Klassen")
    print("=" * 60)
    print()

    # Füge Translations hinzu
    print("Schritt 1: Translation Keys zu Language Files hinzufügen")
    print("-" * 60)
    add_translations_to_json()

    print("\n" + "=" * 60)
    print("✓ Translation Keys erfolgreich hinzugefügt!")
    print("=" * 60)
    print()
    print("Nächste Schritte:")
    print("1. Jede Java-Datei manuell refaktorieren")
    print("2. Component.literal() → Component.translatable()")
    print("3. Tests durchführen")
    print()


if __name__ == "__main__":
    main()
