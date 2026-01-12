# Abschleppsystem - Einrichtungsanleitung

Das Abschleppsystem ermöglicht es Spielern, ihre Fahrzeuge zu einem Abschlepphof abschleppen zu lassen, wenn sie defekt sind oder nicht mehr fahren können.

## Komponenten

### 1. Grundstückstyp: TOWING_YARD (Abschlepphof)

Der `TOWING_YARD` PlotType ist bereits im System definiert und kann für Grundstücke verwendet werden.

**Eigenschaften:**
- Kann gekauft werden (`canBePurchased: true`)
- Kann gemietet werden (`canBeRented: true`)
- Wird für Parkplätze des Abschleppdienstes verwendet

**Einrichtung:**
```
/plot create <plotName> TOWING_YARD
```

### 2. NPC-Typ: ABSCHLEPPER

Der ABSCHLEPPER NPC-Typ wurde hinzugefügt und kann für Service-NPCs verwendet werden.

**NPC erstellen:**
1. Verwenden Sie den NPC-Spawn-Befehl oder das NPC-Item
2. Öffnen Sie das NPC-Konfigurationsmenü
3. Setzen Sie den NPC-Typ auf `ABSCHLEPPER`
4. Geben Sie dem NPC einen passenden Namen (z.B. "Hans Schlepper", "ADAC Service")
5. Wählen Sie einen passenden Skin (z.B. Mechaniker-Outfit)

### 3. Parkplätze einrichten

Parkplätze müssen manuell auf einem TOWING_YARD-Grundstück erstellt werden.

**Über Server-Konsole oder Admin-Command:**
```java
// In-Game über einen Command (muss noch implementiert werden)
// Oder direkt im Code:
TowingYardManager.addParkingSpot(blockPos, towingYardPlotId);
```

**Empfohlene Anzahl:** 10-20 Parkplätze pro Abschlepphof

### 4. Mitgliedschafts-Stufen

Das System bietet 4 Mitgliedschaftsstufen:

| Stufe | Abdeckung | Monatliche Gebühr |
|-------|-----------|-------------------|
| NONE | 0% | 0€ |
| BRONZE | Config-gesteuert | Config-gesteuert |
| SILVER | Config-gesteuert | Config-gesteuert |
| GOLD | Config-gesteuert | Config-gesteuert |

**Konfiguration in `schedulemc-common.toml`:**
```toml
[vehicle]
    # Membership coverage percentages
    membershipBronzeCoveragePercent = 50
    membershipSilverCoveragePercent = 75
    membershipGoldCoveragePercent = 100

    # Monthly membership fees
    membershipBronzeFee = 500.0
    membershipSilverFee = 1000.0
    membershipGoldFee = 2000.0

    # Payment interval
    membershipPaymentIntervalDays = 30

    # Towing costs
    towingBaseFee = 200.0
    towingDistanceFeePerBlock = 0.5
```

### 5. Service-Kontakte

Das System erstellt automatisch zwei Service-Kontakte:

1. **ADAC Pannenhilfe** (`towing_adac`)
   - Haupt-Abschleppdienst
   - Icon: 🔧

2. **Stadtabschleppdienst** (`towing_city`)
   - Alternative/städtischer Service
   - Icon: 🔧

Diese erscheinen automatisch in der Kontakte-App der Spieler.

## Spieler-Workflow

### Mitgliedschaft abschließen

1. Öffne das Smartphone (E-Taste)
2. Öffne die "Pannenhilfe ADAC" App
3. Klicke auf "Mitgliedschaft"
4. Wähle eine Stufe (Bronze/Silver/Gold)
5. Bestätige die monatliche Gebühr

### Fahrzeug abschleppen lassen

1. Öffne das Smartphone
2. Öffne die "Pannenhilfe ADAC" App
3. Deine Fahrzeuge werden angezeigt
4. Klicke auf "Abschleppen" bei einem Fahrzeug
5. Wähle den Ziel-Abschlepphof
6. Bestätige die Kosten (mit Mitgliedschaftsrabatt)
7. Das Fahrzeug wird teleportiert

**Kosten:**
- Grundgebühr + (Distanz × Distanzgebühr)
- Mit Mitgliedschaft: Reduziert um Coverage-Prozentsatz
- Beispiel: Bronze (50% Abdeckung) → Nur 50% der Kosten

### Fahrzeug abholen

1. Gehe zum Abschlepphof
2. Finde deinen Parkplatz
3. Repariere das Fahrzeug (Wartungskit)
4. Fahre mit dem Fahrzeug weg

## Admin-Setup Checkliste

- [ ] TOWING_YARD Grundstück erstellt
- [ ] Parkplätze auf dem Grundstück definiert
- [ ] ABSCHLEPPER NPC gespawnt und konfiguriert
- [ ] Config-Werte angepasst (Gebühren, Abdeckung)
- [ ] System getestet (Mitgliedschaft, Abschleppen)
- [ ] Spielern erklärt, wie das System funktioniert

## Technische Details

### Datenbanken

**MembershipManager** (`plotmod_towing_memberships.json`)
- Speichert Spieler-Mitgliedschaften
- Tracking von Abschlepp-Anzahl
- Zahlungsdaten

**TowingYardManager** (`plotmod_towing_yards.json`)
- Parkplatz-Verwaltung
- Belegungs-Status
- Fahrzeug-Zuordnungen

### Network Packets

- `ChangeMembershipPacket`: Client → Server Mitgliedschaftsänderung
- `RequestTowingPacket`: Client → Server Abschleppanfrage

### GUI Screens

- `TowingServiceAppScreen`: Haupt-App (Smartphone)
- `MembershipSelectionScreen`: Mitgliedschaftsauswahl
- `TowingYardSelectionScreen`: Ziel-Abschlepphof wählen

## Zukünftige Erweiterungen

Mögliche Features für zukünftige Versionen:

1. **Automatisches Abschleppen**
   - Bei vollständigem Fahrzeugschaden
   - Nach Unfällen mit Polizei

2. **Abschlepp-Animation**
   - NPC fährt zum Fahrzeug
   - Visuelles Abschleppen

3. **Abschlepphof-Business**
   - Spieler können Abschlepphöfe besitzen
   - Einnahmen pro Abschleppvorgang
   - Mitarbeiter-NPCs

4. **Versicherungssystem**
   - Vollkasko-Optionen
   - Schadensersatz bei Diebstahl
   - Kooperation mit Bank-NPCs

5. **Notfall-Hotline**
   - Direkter Anruf über Smartphone
   - Voice-Chat Integration
   - Prioritäts-Service

## Troubleshooting

**Problem:** Keine Parkplätze verfügbar
- **Lösung:** Mehr Parkplätze auf dem TOWING_YARD erstellen

**Problem:** Mitgliedschaft wird nicht abgerechnet
- **Lösung:** Prüfen Sie die Config für `membershipPaymentIntervalDays`

**Problem:** Fahrzeug wird nicht abgeschleppt
- **Lösung:** Prüfen Sie, ob der Spieler genug Geld hat

**Problem:** Kein ABSCHLEPPER NPC sichtbar
- **Lösung:** NPC mit korrektem NPCType spawnen (siehe Anleitung oben)
