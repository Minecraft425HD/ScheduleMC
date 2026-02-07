# Abschlepp-System

> Fahrzeug-Transport und Reparaturservice mit Mitgliedschaftsstufen

## Ueberblick

Das Abschlepp-System ermoeglicht Spielern, beschaedigte Fahrzeuge zu Abschlepphofen transportieren zu lassen. Es umfasst ein Mitgliedschaftsmodell mit unterschiedlichen Kostendeckungen, ein Rechnungssystem und Einnahmen-Tracking fuer Hofbesitzer.

## Mitgliedschaftsstufen

| Stufe | Kostendeckung | Monatliche Gebuehr |
|-------|-------------|-------------------|
| **KEINE** | 0% | 0 EUR |
| **BRONZE** | 50% | 500 EUR |
| **SILBER** | 75% | 1.000 EUR |
| **GOLD** | 100% (kostenlos) | 2.000 EUR |

### Zahlungsmechanik

- **Intervall**: 30 Minecraft-Tage (konfigurierbar)
- **Abzug**: Automatisch vom Bankkonto
- **Bei unzureichendem Guthaben**: Mitgliedschaft wird gekuendigt
- **Kostenberechnung**: `Spielerkosten = Gesamtkosten x (1 - Deckung%)`

### Beispielrechnung (BRONZE, 50% Deckung)

```
Grundgebuehr:     200 EUR
Entfernungsgebuehr: 50 Bloecke x 0,50 EUR = 25 EUR
Gesamtkosten:     225 EUR
Spieler zahlt:    225 x (1 - 0,50) = 112,50 EUR
```

## Abschlepp-Vorgang

### Schritt fuer Schritt

1. **Smartphone-App oeffnen** (ADAC Pannenhilfe oder Stadtabschleppdienst)
2. **Fahrzeug auswaehlen** (muss dem Spieler gehoeren, keine Passagiere)
3. **Ziel-Abschlepphof waehlen** (mit freiem Parkplatz)
4. **Kosten pruefen** (Grundgebuehr + Entfernung, abzueglich Mitgliedschaftsrabatt)
5. **Bestaetigen** - Fahrzeug wird teleportiert

### Server-seitige Verarbeitung

- Fahrzeugbesitz wird validiert
- Freier Parkplatz wird gesucht
- Entfernung und Kosten werden berechnet
- Fahrzeugschaden wird um 10% reduziert (Reparaturvorteil)
- Fahrzeug wird zum Parkplatz teleportiert
- Treibstoffverbrauch wird deaktiviert (isOnTowingYard = true)
- Rechnung wird erstellt

## Rechnungszahlung

1. Mit dem **ABSCHLEPPER-NPC** am Abschlepphof interagieren
2. Rechnungs-GUI wird angezeigt (Betrag, Fahrzeug-Details)
3. **"Bezahlen"**-Button klicken
4. Betrag wird vom Bankkonto abgezogen (TransactionType: WERKSTATT_FEE)
5. Fahrzeug kann abgefahren werden

## Abschlepphof einrichten (Admin)

### Voraussetzungen

1. Grundstueck mit Typ `TOWING_YARD` erstellen:
   ```
   /plot create <name> TOWING_YARD
   ```
2. Parkplaetze hinzufuegen
3. ABSCHLEPPER-NPC fuer die Rechnungszahlung spawnen

### Automatische Service-Kontakte

Das System erstellt automatisch zwei Service-Kontakte im Smartphone:
- **ADAC Pannenhilfe** (ID: `towing_adac`)
- **Stadtabschleppdienst** (ID: `towing_city`)

## Einnahmen-Tracking

### Statistiken pro Abschlepphof

| Metrik | Beschreibung |
|--------|-------------|
| Gesamteinnahmen (X Tage) | Brutto-Einnahmen im Zeitraum |
| Anzahl Abschleppvorgaenge (X Tage) | Anzahl der Transporte |
| Durchschnittliche Einnahmen | Durchschnitt pro Transport |
| Transaktionsliste | Vollstaendige Auflistung |

### Umsatzverteilung

- **19% MwSt** wird automatisch abgezogen und an das Staatskonto ueberwiesen
- **81% Netto** werden gleichmaessig auf alle Abschlepphoefe verteilt
- Wenn keine Hoefe existieren: Gesamtbetrag geht an den Staat

### Speicherlimits

- Max 1.000 Transaktionen pro Hof (aelteste werden entfernt)
- Max 50.000 Parkplaetze gesamt
- Max 10.000 unbezahlte Rechnungen
- Automatische Bereinigung von Transaktionen aelter als 30 Tage

## Konfiguration

```toml
# Mitgliedschafts-Deckung (Prozent)
membershipBronzeCoveragePercent = 50
membershipSilverCoveragePercent = 75
membershipGoldCoveragePercent = 100

# Monatliche Gebuehren
membershipBronzeFee = 500.0
membershipSilverFee = 1000.0
membershipGoldFee = 2000.0

# Zahlungsintervall (Minecraft-Tage)
membershipPaymentIntervalDays = 30

# Abschleppkosten
towingBaseFee = 200.0
towingDistanceFeePerBlock = 0.5
```

## Datenspeicherung

- **Mitgliedschaften**: `config/plotmod_towing_memberships.json`
- **Parkplaetze**: `config/plotmod_towing_parking_spots.json`
