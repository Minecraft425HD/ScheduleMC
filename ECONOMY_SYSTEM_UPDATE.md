# 💰 ScheduleMC Economy System - Vollständiges Update

## 🎯 Übersicht

Dieses Update erweitert das ScheduleMC Economy-System um umfassende Banking-Features, Steuersystem, Kredite und vollständiges Transaction-Logging.

---

## ✅ IMPLEMENTIERTE FEATURES

### 1. **KRITISCHER BUG-FIX**
- ✅ **Shop-Auszahlungen** funktionieren jetzt korrekt
  - Aktionäre erhalten wöchentliche Auszahlungen direkt auf ihr Konto
  - `ShopAccount.java:296` - Integration mit `EconomyManager.deposit()`

---

### 2. **TRANSAKTIONS-LOGGING SYSTEM**
Vollständiges Tracking aller Finanztransaktionen.

**Neue Dateien:**
- `TransactionType.java` - Enum mit 30+ Transaktionstypen
- `Transaction.java` - Datenmodell für einzelne Transaktionen
- `TransactionHistory.java` - Manager für Transaktionshistorie

**Features:**
- Automatisches Logging aller Transaktionen
- Speicherung in `config/plotmod_transactions.json`
- Max. 1000 Transaktionen pro Spieler (verhindert unbegrenztes Wachstum)
- Zeitstempel, Typ, Betrag, Von/An, Beschreibung, Kontostand nach Transaktion

**Commands:**
```
/money history [limit]           - Zeigt eigene Historie (Standard: 10)
/money history <player> [limit]  - Zeigt Spieler-Historie (Admin, Standard: 10)
```

**Integration:**
- Alle `EconomyManager`-Methoden loggen automatisch
- `deposit()`, `withdraw()`, `setBalance()` mit erweiterten Parametern
- Admin-Commands loggen Ausführenden

---

### 3. **GEBÜHRENSYSTEM**
Transaktionsgebühren für ATM und Transfers.

**Neue Datei:**
- `FeeManager.java`

**Gebühren:**
- **ATM-Gebühr**: 5€ pro Transaktion (Einzahlung & Auszahlung)
- **Transfer-Gebühr**: 1% des Betrags (Minimum: 10€)
- Alle Gebühren fließen in die Staatskasse

**Integration:**
- `ATMBlockEntity.java` - Gebühren bei Ein-/Auszahlungen
- `MoneyCommand.java` - Transfer-Gebühren bei `/pay`
- Anzeige der Gebühren in Transaktionsmeldungen

---

### 4. **ANTI-CHEAT & RATE-LIMITING**
Verhindert Spam und Exploits.

**Neue Datei:**
- `RateLimiter.java`

**Features:**
- Max. 10 Transaktionen pro Minute pro Spieler
- Automatische Bereinigung alter Timestamps
- Anzeige verbleibender Wartezeit bei Überschreitung

**Integration:**
- `/pay` Command (MoneyCommand.java)
- Fehlermeldung mit Countdown

---

### 5. **ZINSEN-SYSTEM**
Automatische Zinsen auf Konten.

**Neue Datei:**
- `InterestManager.java`

**Features:**
- **Zinssatz**: 2% pro Woche
- **Max. Zinsen**: 10.000€ pro Woche (verhindert Inflation)
- Automatische wöchentliche Auszahlung
- Benachrichtigung online Spieler
- Persistenz in `config/plotmod_interest.json`

**Berechnung:**
```
Zinsen = Min(Kontostand * 0.02, 10.000€)
```

---

### 6. **KREDIT-SYSTEM**
Vollständiges Kreditsystem mit Zinsen und automatischen Raten.

**Neue Dateien:**
- `Loan.java` - Datenmodell
- `LoanManager.java` - Kredit-Verwaltung
- `LoanCommand.java` - Commands

**Kredit-Typen:**
| Typ    | Betrag    | Zinssatz | Laufzeit | Tägliche Rate |
|--------|-----------|----------|----------|---------------|
| SMALL  | 5.000€    | 10%      | 14 Tage  | ~393€         |
| MEDIUM | 25.000€   | 15%      | 28 Tage  | ~1.027€       |
| LARGE  | 100.000€  | 20%      | 56 Tage  | ~2.143€       |

**Features:**
- Automatische tägliche Ratenzahlungen
- Vorzeitige Rückzahlung möglich
- Voraussetzungen: Mindestens 1.000€, kein aktiver Kredit
- Warnungen bei Zahlungsausfall
- Persistenz in `config/plotmod_loans.json`

**Commands:**
```
/loan apply <SMALL|MEDIUM|LARGE>  - Kredit beantragen
/loan info                        - Kredit-Status anzeigen
/loan repay                       - Vorzeitig zurückzahlen
```

---

### 7. **STEUERSYSTEM**
Progressives Einkommensteuersystem.

**Neue Datei:**
- `TaxManager.java`

**Steuerstufen:**
| Kontostand           | Steuersatz |
|----------------------|------------|
| 0€ - 10.000€         | 0%         |
| 10.000€ - 50.000€    | 10%        |
| 50.000€ - 100.000€   | 15%        |
| 100.000€+            | 20%        |

**Features:**
- Monatliche Abrechnung (alle 7 MC-Tage)
- Automatische Abbuchung
- Steuerschulden bei Nicht-Zahlung
- Warnung bei Zahlungsausfall
- Einnahmen gehen an Staatskasse
- Persistenz in `config/plotmod_taxes.json`

**Berechnung:**
Progressiv: Jeder Betrag wird in seiner Stufe besteuert.

Beispiel bei 60.000€ Kontostand:
- 10.000€ Freibetrag: 0€ Steuer
- 40.000€ @ 10%: 4.000€ Steuer
- 10.000€ @ 15%: 1.500€ Steuer
- **Gesamt: 5.500€ Steuern**

---

## 📁 NEUE DATEIEN

### Economy Core
```
src/main/java/de/rolandsw/schedulemc/economy/
├── TransactionType.java
├── Transaction.java
├── TransactionHistory.java
├── FeeManager.java
├── RateLimiter.java
├── InterestManager.java
├── Loan.java
├── LoanManager.java
└── TaxManager.java
```

### Commands
```
src/main/java/de/rolandsw/schedulemc/commands/
└── LoanCommand.java
```

### Documentation
```
ECONOMY_SYSTEM_UPDATE.md (diese Datei)
```

---

## 🔧 GEÄNDERTE DATEIEN

### Economy
- **EconomyManager.java**
  - `getInstance()` Singleton-Methode
  - `initialize(MinecraftServer)` für Server-Referenz
  - Erweiterte `deposit()`, `withdraw()`, `setBalance()` mit TransactionType & Beschreibung
  - `transfer()` Methode
  - `logTransaction()` Integration
  - `saveIfNeeded()` speichert auch TransactionHistory

- **ShopAccount.java**
  - Bug-Fix: Zeile 296 - Auszahlungen werden jetzt korrekt gebucht

### Commands
- **MoneyCommand.java**
  - Imports: `FeeManager`, `RateLimiter`, `TransactionHistory`, `Transaction`
  - `/money history` Command
  - Admin-Commands nutzen TransactionType
  - `/pay` mit Transfer-Gebühren und Rate-Limiting
  - Historie-Anzeige mit Statistiken

### Block Entities
- **ATMBlockEntity.java**
  - Imports: `FeeManager`, `TransactionType`
  - `withdraw()` mit ATM-Gebühren
  - `deposit()` mit ATM-Gebühren
  - Anzeige der Gebühren in Meldungen

---

## 💾 NEUE CONFIG-DATEIEN

Das System erstellt automatisch folgende Dateien:

```
config/
├── plotmod_economy.json        (existiert bereits)
├── plotmod_wallets.json        (existiert bereits)
├── plotmod_transactions.json   (NEU - Transaction History)
├── plotmod_interest.json       (NEU - Zinsen-Tracking)
├── plotmod_loans.json          (NEU - Aktive Kredite)
└── plotmod_taxes.json          (NEU - Steuer-Tracking)
```

---

## 🎮 NEUE COMMANDS

### Spieler-Commands
```bash
# Transaktionshistorie
/money history                  # Zeigt letzte 10 Transaktionen
/money history 20               # Zeigt letzte 20 Transaktionen

# Kredite
/loan apply SMALL               # 5k Kredit (10%, 14 Tage)
/loan apply MEDIUM              # 25k Kredit (15%, 28 Tage)
/loan apply LARGE               # 100k Kredit (20%, 56 Tage)
/loan info                      # Kredit-Status
/loan repay                     # Vorzeitig zurückzahlen
```

### Admin-Commands
```bash
# Transaction History
/money history <player>         # Spieler-Historie anzeigen
/money history <player> 50      # 50 Transaktionen anzeigen
```

---

## 🔄 AUTOMATISCHE PROZESSE

### Täglich (bei Tag-Wechsel)
- ✅ Kredit-Ratenzahlungen (LoanManager)
- ✅ Steuern-Prüfung (alle 7 Tage)

### Wöchentlich (alle 7 MC-Tage)
- ✅ Zinsen-Auszahlung (InterestManager)
- ✅ Steuer-Abrechnung (TaxManager)
- ✅ Shop-Auszahlungen (ShopAccount - bereits implementiert)

### Bei jeder Transaktion
- ✅ Transaction Logging
- ✅ Gebühren-Abzug
- ✅ Rate-Limiting-Check

---

## 📊 STATISTIKEN & MONITORING

### Transaction History
- Gesamt-Einnahmen pro Spieler
- Gesamt-Ausgaben pro Spieler
- Anzahl Transaktionen
- Filterung nach Typ, Zeitraum

### Steuer-Tracking
- Letzte Steuer-Zahlung
- Steuerschulden
- Automatische Mahnungen

### Kredit-Tracking
- Aktive Kredite
- Verbleibender Betrag
- Tägliche Ratenhöhe
- Verbleibende Laufzeit

---

## 🏦 STAATSKASSE INTEGRATION

Alle Gebühren und Steuern fließen in die Staatskasse:

**Einnahmen:**
- ATM-Gebühren (5€ pro Transaktion)
- Transfer-Gebühren (1% + min. 10€)
- Einkommenssteuern (0-20%)

**Commands:**
```bash
/state balance      # Staatskassen-Stand
/state deposit      # Einzahlung (Admin)
/state withdraw     # Auszahlung (Admin)
```

---

## ⚙️ KONFIGURATION

### Gebühren (FeeManager.java)
```java
ATM_FEE = 5.0€
TRANSFER_FEE_PERCENTAGE = 1%
MIN_TRANSFER_FEE = 10.0€
```

### Rate-Limiting (RateLimiter.java)
```java
MAX_TRANSACTIONS_PER_MINUTE = 10
```

### Zinsen (InterestManager.java)
```java
INTEREST_RATE = 2%
MAX_INTEREST_PER_WEEK = 10.000€
WEEK_IN_DAYS = 7
```

### Steuern (TaxManager.java)
```java
TAX_FREE_AMOUNT = 10.000€
TAX_BRACKET_1 = 50.000€ (10%)
TAX_BRACKET_2 = 100.000€ (15%)
// Darüber: 20%
TAX_PERIOD_DAYS = 7
```

### Kredite (Loan.java)
```java
SMALL:  5.000€, 10%, 14 Tage
MEDIUM: 25.000€, 15%, 28 Tage
LARGE:  100.000€, 20%, 56 Tage
```

---

## 🚀 INSTALLATION & AKTIVIERUNG

### ✅ VOLLSTÄNDIG INTEGRIERT!

**Alle Manager sind bereits vollständig integriert in `ScheduleMC.java`!**

#### ✅ Server-Start (onServerStarted):
```java
EconomyManager.initialize(event.getServer());
TransactionHistory.getInstance(event.getServer());
InterestManager.getInstance(event.getServer());
LoanManager.getInstance(event.getServer());
TaxManager.getInstance(event.getServer());
LOGGER.info("Advanced Economy Systems initialized");
```

#### ✅ Server-Tick (onServerTick):
```java
long dayTime = event.getServer().overworld().getDayTime();
InterestManager.getInstance(event.getServer()).tick(dayTime);
LoanManager.getInstance(event.getServer()).tick(dayTime);
TaxManager.getInstance(event.getServer()).tick(dayTime);
```

#### ✅ Periodisches Speichern (alle 6000 Ticks):
```java
InterestManager.getInstance(event.getServer()).save();
LoanManager.getInstance(event.getServer()).save();
TaxManager.getInstance(event.getServer()).save();
```

#### ✅ Server-Stop (onServerStopping):
```java
InterestManager.getInstance(event.getServer()).save();
LoanManager.getInstance(event.getServer()).save();
TaxManager.getInstance(event.getServer()).save();
TransactionHistory.getInstance().save();
LOGGER.info("Advanced Economy Systems saved");
```

#### ✅ Command-Registrierung (onRegisterCommands):
```java
LoanCommand.register(event.getDispatcher());
```

**🎉 KEINE WEITEREN SCHRITTE ERFORDERLICH - EINFACH STARTEN!**

---

## 🐛 BEKANNTE LIMITIERUNGEN

1. **TransactionHistory**: Max. 1000 Transaktionen pro Spieler
   - Älteste werden automatisch gelöscht
   - Erhöhbar in `TransactionHistory.java:21`

2. **Zinsen**: Max. 10.000€ pro Woche
   - Verhindert Hyperinflation
   - Änderbar in `InterestManager.java:27`

3. **Rate-Limiting**: Nur für `/pay` Command
   - ATM-Transaktionen nicht limitiert
   - Erweiterbar auf andere Commands

4. **Steuerschulden**: Keine automatische Zwangsvollstreckung
   - Nur Warnungen
   - Kann um Pfändung erweitert werden

---

## 🎯 ZUKÜNFTIGE ERWEITERUNGEN

### Nicht implementiert (ursprünglich geplant):
- ❌ GUI-Verbesserungen für ATM
- ❌ Finanz-Dashboard für Admins
- ❌ Sparkonten mit höheren Zinsen
- ❌ Staatsanleihen-System
- ❌ Versicherungs-System
- ❌ Grundsteuer für Plots
- ❌ Umsatzsteuer für Shops
- ❌ Steuerfahndung mit Strafen

Diese Features können in zukünftigen Updates hinzugefügt werden.

---

## 📝 CHANGELOG

### Version 2.0.0 - Vollständiges Economy Update

#### Added
- Transaction Logging System mit 30+ Transaktionstypen
- Gebührensystem (ATM: 5€, Transfer: 1%)
- Anti-Cheat Rate-Limiting (10 Transaktionen/Minute)
- Zinsen-System (2% pro Woche, max. 10k)
- Kredit-System mit 3 Typen (5k, 25k, 100k)
- Steuersystem progressiv (0%, 10%, 15%, 20%)
- `/money history` Command
- `/loan` Commands (apply, info, repay)

#### Fixed
- Shop-Auszahlungen funktionieren jetzt korrekt (kritischer Bug)

#### Changed
- `EconomyManager` erweitert mit Transaction-Logging
- `MoneyCommand` erweitert mit Historie und Gebühren
- `ATMBlockEntity` erweitert mit Gebühren
- Admin-Commands loggen jetzt den Ausführenden

---

## 👨‍💻 ENTWICKLER-NOTIZEN

### Thread-Safety
- Alle Manager nutzen `ConcurrentHashMap`
- Dirty-Flag-Pattern für Batch-Saving
- Keine Race-Conditions bei Transaktionen

### Performance
- Transaction History: O(1) für neue Einträge
- Rate-Limiting: O(n) wobei n ≤ 10
- Auto-Cleanup alter Daten

### Persistence
- Alle Daten JSON-serialisiert
- Automatisches Speichern bei Änderungen
- Graceful Degradation bei Ladefehlern

---

## 🎉 FERTIG!

Das ScheduleMC Economy-System ist jetzt ein vollwertiges Banking-System mit:
- ✅ Transaktions-Logging
- ✅ Gebühren
- ✅ Zinsen
- ✅ Kredite
- ✅ Steuern
- ✅ Anti-Cheat
- ✅ Staatskassen-Integration

**Viel Spaß beim Spielen! 💰**
