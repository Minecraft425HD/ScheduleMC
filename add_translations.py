#!/usr/bin/env python3
"""
Script to add new translation keys to both en_us.json and de_de.json
"""
import json
import sys
from pathlib import Path

# Translation keys to add (German → English)
NEW_TRANSLATIONS = {
    # BankAppScreen - Tab names
    "gui.app.bank.tab.account": ("Konto", "Account"),
    "gui.app.bank.tab.history": ("Historie", "History"),
    "gui.app.bank.tab.transfer": ("Überweisung", "Transfer"),
    "gui.app.bank.tab.recurring": ("Daueraufträge", "Recurring Payments"),

    # BankAppScreen - Messages
    "message.bank.data_refreshed": ("§aDaten aktualisiert!", "§aData refreshed!"),
    "message.bank.enter_recipient": ("§cBitte Empfänger eingeben!", "§cPlease enter recipient!"),
    "message.bank.enter_amount": ("§cBitte Betrag eingeben!", "§cPlease enter amount!"),
    "message.bank.invalid_amount": ("§cUngültiger Betrag!", "§cInvalid amount!"),
    "message.bank.amount_positive": ("§cBetrag muss positiv sein!", "§cAmount must be positive!"),
    "message.bank.insufficient_funds": ("§cNicht genug Geld!", "§cInsufficient funds!"),
    "message.bank.transfer_processing": ("§aÜberweisung an %s (%.2f€) wird verarbeitet...", "§aTransfer to %s (%.2f€) is being processed..."),
    "message.bank.recurring_created": ("§aDauerauftrag an %s (%.2f€ %s) erstellt!", "§aRecurring payment to %s (%.2f€ %s) created!"),

    # BankAppScreen - Account Tab
    "gui.app.bank.balance_title": ("§f§lKontostand", "§f§lBalance"),
    "gui.app.bank.balance_positive": ("Positiv", "Positive"),
    "gui.app.bank.balance_overdraft": ("Dispo", "Overdraft"),
    "gui.app.bank.statistics": ("§6§lStatistiken", "§6§lStatistics"),
    "gui.app.bank.income": ("§aEinnahmen:", "§aIncome:"),
    "gui.app.bank.expenses": ("§cAusgaben:", "§cExpenses:"),
    "gui.app.bank.balance_calc": ("§fBilanz:", "§fBalance:"),
    "gui.app.bank.transactions_count": ("§7Transaktionen: ", "§7Transactions: "),

    # BankAppScreen - History Tab
    "gui.app.bank.no_transactions": ("§7Keine Transaktionen", "§7No transactions"),
    "gui.app.bank.transactions_header": ("§e%d Transaktionen", "§e%d Transactions"),

    # BankAppScreen - Transfer Tab
    "gui.app.bank.available": ("§7Verfügbar:", "§7Available:"),
    "gui.app.bank.recipient_name": ("§fEmpfängername:", "§fRecipient name:"),
    "gui.app.bank.amount_label": ("§fBetrag in €:", "§fAmount in €:"),
    "gui.app.bank.transfer_info": ("§8Überweisung an andere Spieler", "§8Transfer to other players"),

    # BankAppScreen - Recurring Payments Tab
    "gui.app.bank.recurring_title": ("§6§lDaueraufträge", "§6§lRecurring Payments"),
    "gui.app.bank.limit": ("Limit: ", "Limit: "),
    "gui.app.bank.max": ("§c§lMAX!", "§c§lMAX!"),
    "gui.app.bank.new_recurring": ("§fNeuer Dauerauftrag:", "§fNew recurring payment:"),
    "gui.app.bank.recipient_label": ("§7Empfänger:", "§7Recipient:"),
    "gui.app.bank.amount_recurring_label": ("§7Betrag:", "§7Amount:"),
    "gui.app.bank.interval_label": ("§7Intervall:", "§7Interval:"),
    "gui.app.bank.active_recurring": ("§fAktive Daueraufträge:", "§fActive recurring payments:"),
    "gui.app.bank.no_recurring": ("§7Keine aktiven Daueraufträge", "§7No active recurring payments"),

    # BankAppScreen - Credit info
    "gui.app.bank.credit_title": ("§6💳 KREDIT", "§6💳 CREDIT"),
    "gui.app.bank.credit_daily": ("§fTäglich: ", "§fDaily: "),
    "gui.app.bank.credit_remaining": ("§fRest: ", "§fRemaining: "),

    # BankAppScreen - Payment details
    "gui.app.bank.payment_to": ("§fAn: §b", "§fTo: §b"),
    "gui.app.bank.payment_amount": ("§fBetrag: ", "§fAmount: "),
    "gui.app.bank.payment_interval": ("§fIntervall: §e", "§fInterval: §e"),
    "gui.app.bank.payment_status_active": ("§a● Aktiv", "§a● Active"),
    "gui.app.bank.payment_status_paused": ("§e⏸ Pausiert", "§e⏸ Paused"),
}

def load_json(filepath):
    """Load JSON file"""
    with open(filepath, 'r', encoding='utf-8') as f:
        return json.load(f)

def save_json(filepath, data):
    """Save JSON file with proper formatting"""
    with open(filepath, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=2)

def add_translations():
    """Add new translation keys to both language files"""
    base_path = Path(__file__).parent / "src/main/resources/assets/schedulemc/lang"

    de_file = base_path / "de_de.json"
    en_file = base_path / "en_us.json"

    # Load existing files
    print("Loading language files...")
    de_data = load_json(de_file)
    en_data = load_json(en_file)

    print(f"DE file: {len(de_data)} entries")
    print(f"EN file: {len(en_data)} entries")

    # Add new translations
    added_count = 0
    for key, (de_value, en_value) in NEW_TRANSLATIONS.items():
        if key not in de_data:
            de_data[key] = de_value
            en_data[key] = en_value
            added_count += 1
            print(f"Added: {key}")
        else:
            print(f"Skipped (already exists): {key}")

    # Save files
    if added_count > 0:
        print(f"\nSaving {added_count} new translations...")
        save_json(de_file, de_data)
        save_json(en_file, en_data)
        print("✓ Done!")
        print(f"New DE file: {len(de_data)} entries")
        print(f"New EN file: {len(en_data)} entries")
    else:
        print("\nNo new translations to add.")

    return added_count

if __name__ == "__main__":
    try:
        count = add_translations()
        sys.exit(0 if count >= 0 else 1)
    except Exception as e:
        print(f"Error: {e}", file=sys.stderr)
        sys.exit(1)
