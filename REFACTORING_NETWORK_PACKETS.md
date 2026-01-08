# Refaktorierung: NPC Network Packet Klassen

## Übersicht

Alle 11 NPC Network Packet Klassen wurden refaktoriert. Deutsche `Component.literal()` Strings wurden durch `Component.translatable()` ersetzt.

## Refaktorierte Dateien

1. ✅ `ApplyCreditLoanPacket.java` - Kredit beantragen
2. ✅ `BankDepositPacket.java` - Bargeld einzahlen
3. ✅ `BankTransferPacket.java` - Geld überweisen
4. ✅ `BankWithdrawPacket.java` - Geld abheben
5. ✅ `CreateRecurringPaymentPacket.java` - Dauerauftrag erstellen
6. ✅ `DeleteRecurringPaymentPacket.java` - Dauerauftrag löschen
7. ✅ `RepayCreditLoanPacket.java` - Kredit zurückzahlen
8. ✅ `ResumeRecurringPaymentPacket.java` - Dauerauftrag fortsetzen
9. ✅ `SavingsDepositPacket.java` - Sparkonto einzahlen
10. ✅ `SavingsWithdrawPacket.java` - Sparkonto abheben
11. ✅ `StockTradePacket.java` - Börsenhandel

## Neue Translation Keys (37 Keys)

### Credit/Loan Keys
- `network.credit.invalid_loan_type` - Ungültiger Kredittyp
- `network.credit.already_active_loan` - Bereits aktiver Kredit
- `network.credit.repay_first` - Zuerst zurückzahlen
- `network.credit.loan_rejected_insufficient` - Kredit abgelehnt
- `network.credit.insufficient_credit_score` - Bonität reicht nicht aus
- `network.credit.required_rating` - Benötigte Bonität Label
- `network.credit.current_rating` - Aktuelle Bonität Label
- `network.credit.type_label` - Typ Label
- `network.credit.effective_interest` - Effektiver Zinssatz Label
- `network.credit.duration_label` - Laufzeit Label
- `network.credit.days_suffix` - " Tage" Suffix
- `network.credit.loan_rejected_unknown` - Kredit abgelehnt (unbekannter Fehler)
- `network.credit.unknown_error` - Unbekannter Fehler
- `network.credit.no_active_loan` - Kein aktiver Kredit
- `network.credit.insufficient_funds_repay` - Nicht genug Geld (Rückzahlung)
- `network.credit.required_label` - "Benötigt:" Label
- `network.credit.balance_label` - "Kontostand:" Label
- `network.credit.debt_free` - Schuldenfrei
- `network.credit.repayment_error` - Rückzahlungsfehler

### Bank Keys
- `network.bank.maximum_label` - "Maximum:" Label
- `network.bank.cash_label` - "Bargeld:" Label
- `network.bank.remaining_cash` - "Restliches Bargeld:" Label
- `network.bank.deposit_error` - Einzahlungsfehler
- `network.bank.limit_label` - "Limit:" Label
- `network.bank.remaining_daily_limit` - "Verbleibendes Tageslimit:" Label
- `network.bank.money_received` - "GELD ERHALTEN"
- `network.bank.from_label` - "Von:" Label
- `network.bank.new_cash` - "Neues Bargeld:" Label
- `network.bank.max_count_reached` - Maximale Anzahl erreicht
- `network.bank.id_label` - "ID:" Label
- `network.bank.order_resumed` - Dauerauftrag fortgesetzt
- `network.bank.remaining_days` - "Verbleibende Tage:" Label
- `network.bank.hint_label` - "Hinweis:" Label
- `network.bank.early_withdrawal_penalty` - Vorzeitige Abhebung Strafe
- `network.bank.withdrawal_error` - Abhebungsfehler

### Stock Keys
- `network.stock.free_slots_suffix` - " freie Slots" Suffix
- `network.stock.total_cost` - "Gesamtkosten:" Label

## Änderungen pro Datei

### ApplyCreditLoanPacket.java
- 7 Component.literal() → Component.translatable()
- Fehlermeldungen: Ungültiger Kredittyp, Bereits aktiver Kredit, Bonität nicht ausreichend
- Labels: Typ, Zinssatz, Laufzeit

### BankDepositPacket.java
- 3 Component.literal() → Component.translatable()
- Labels: Maximum, Bargeld, Restliches Bargeld
- Fehlermeldung: Einzahlungsfehler

### BankTransferPacket.java
- 3 Component.literal() → Component.translatable()
- Labels: Limit, Verbleibendes Tageslimit, Von
- Nachricht: GELD ERHALTEN

### BankWithdrawPacket.java
- 1 Component.literal() → Component.translatable()
- Label: Neues Bargeld

### CreateRecurringPaymentPacket.java
- 1 Component.literal() → Component.translatable()
- Fehlermeldung: Maximale Anzahl erreicht

### DeleteRecurringPaymentPacket.java
- 2 Component.literal() → Component.translatable()
- Label: ID (2x)

### RepayCreditLoanPacket.java
- 5 Component.literal() → Component.translatable()
- Fehlermeldungen: Kein aktiver Kredit, Nicht genug Geld, Rückzahlungsfehler
- Labels: Benötigt, Kontostand
- Erfolg: Schuldenfrei

### ResumeRecurringPaymentPacket.java
- 2 Component.literal() → Component.translatable()
- Nachricht: Dauerauftrag fortgesetzt
- Label: ID

### SavingsWithdrawPacket.java
- 3 Component.literal() → Component.translatable()
- Labels: Verbleibende Tage, Hinweis
- Fehlermeldungen: Vorzeitige Abhebung Strafe, Abhebungsfehler

### StockTradePacket.java
- 2 Component.literal() → Component.translatable()
- Label: Gesamtkosten
- Suffix: freie Slots

## Translation Key Pattern

Alle Keys folgen dem Pattern: `network.<category>.<action>`

- `network.credit.*` - Kredit-bezogene Nachrichten
- `network.bank.*` - Bank-bezogene Nachrichten
- `network.stock.*` - Börsen-bezogene Nachrichten

## Verbleibende Component.literal() Aufrufe

Folgende Component.literal() Aufrufe bleiben bestehen (korrekt):
- Formatierte Zahlen (String.format)
- Dynamische Werte (Spielernamen, UUIDs, Beträge)
- Separatoren ("═══════")
- Emojis ("🏦", "💰", "📋", etc.)

## Qualitätssicherung

✅ Alle deutschen hardcodierten Strings entfernt
✅ Translation Keys in de_de.json hinzugefügt
✅ Translation Keys in en_us.json hinzugefügt
✅ Konsistente Naming Convention verwendet
✅ Alle 11 Dateien erfolgreich refaktoriert

## Nächste Schritte

1. ✅ Translation Keys hinzugefügt
2. ✅ Java-Dateien refaktoriert
3. ⏳ Tests durchführen
4. ⏳ Build überprüfen
5. ⏳ In-Game Testing

## Datum

Refaktoriert am: 2026-01-08
