# Gang-System

> Organisierte Kriminalitaet mit Hierarchie, Perks, Missionen und Territorien

## Ueberblick

Das Gang-System ermoeglicht Spielern die Gruendung krimineller Organisationen mit hierarchischen Raengen, XP-Progression (Level 1-30), freischaltbaren Perks, automatischen Missionen und Gebietsanspruechen. Gangs verdienen kollektiv XP durch Aktivitaeten ihrer Mitglieder und schalten dadurch neue Faehigkeiten frei.

## Gang erstellen

### Voraussetzungen

| Anforderung | Wert |
|---|---|
| Mindest-Produzentenlevel | 15 |
| Kosten | 25.000 EUR |
| Darf keiner anderen Gang angehoeren | Ja |

### Erstellung

```
/gang create <name> <tag> [farbe]
```

- **Name**: 3-20 Zeichen (eindeutig, nicht case-sensitiv)
- **Tag**: 2-5 Zeichen (Grossbuchstaben, eindeutig)
- **Farbe**: Optional (Standard: WEISS) - Minecraft ChatFormatting Farben

### Gang beitreten

| Anforderung | Wert |
|---|---|
| Mindest-Produzentenlevel | 5 |
| Kosten | 2.500 EUR |
| Einladung erforderlich | Ja (5 Minuten gueltig) |

```
/gang invite <spieler>     # Einladung senden (UNDERBOSS+ Rang)
/gang accept               # Einladung annehmen
```

## Rang-Hierarchie

| Rang | Stufe | Einladen | Kicken | Territorium | Perks | Aufloesen | Gebuehr-Anteil |
|------|-------|----------|--------|-------------|-------|-----------|----------------|
| BOSS | 4 | Ja | Ja | Ja | Ja | Ja | 0% |
| UNDERBOSS | 3 | Ja | Ja (niedrigere) | Ja | Nein | Nein | 10% |
| MEMBER | 2 | Nein | Nein | Nein | Nein | Nein | 50% |
| RECRUIT | 1 | Nein | Nein | Nein | Nein | Nein | 100% |

### Befoerderung

```
/gang promote <spieler> <rang>    # Gueltige Raenge: RECRUIT, MEMBER, UNDERBOSS, BOSS
/gang kick <spieler>              # Mitglied entfernen (hoeherer Rang erforderlich)
```

- Boss-Transfer degradiert den aktuellen Boss automatisch zum UNDERBOSS

## XP und Level-System

### XP-Quellen

| Quelle | XP |
|---|---|
| Legaler Verkauf | 3 XP pro Verkauf |
| Illegaler Verkauf | 6 XP pro Verkauf |
| Territorium gehalten | 2 XP pro Chunk pro Tag |
| Territorium erobert | 15 XP pro neues Gebiet |
| Mission abgeschlossen | 50 XP |
| Missions-Bonus (alle eines Typs) | 100 XP |
| Mitglied Level-Up | 20 XP |
| Mitglied beigetreten | 10 XP |
| Polizei-Razzia ueberlebt | 30 XP |
| Erfolgreiche Bestechung | 8 XP |
| Taegliche Aktivitaet | 5 XP |

### XP-Formel

```
XP fuer Level = BASE_XP * (Level ^ 1.7)
BASE_XP = 200
```

### Level-Progression

| Level | Max Mitglieder | Basis-Territorien | Perk-Punkte |
|-------|----------------|-------------------|-------------|
| 1 | 5 | 1 | 0 |
| 5 | 5 | 1 | 3 |
| 8 | 8 | 4 | 6 |
| 10 | 8 | 4 | 8 |
| 15 | 11 | 9 | 13 |
| 20 | 14 | 16 | 18 |
| 25 | 17 | 16 | 23 |
| 30 | 20 | 25 | 28 |

### Reputations-Stufen

| Level-Bereich | Reputation | Farbe |
|---------------|-----------|-------|
| 0-4 | UNBEKANNT | Grau |
| 5-11 | BEKANNT | Gelb |
| 12-19 | RESPEKTIERT | Gold |
| 20-26 | GEFUERCHTET | Rot |
| 27-30 | LEGENDAER | Dunkelblau |

Sterne-Anzeige: 1-5 Sterne im Gang-Tag (1 pro ~6 Level)

## Perk-System

### Mechanik

- **Freischaltkosten**: 1 Perk-Punkt pro Perk
- **Punkte pro Level**: 1 Punkt (ab Level 3)
- **Verfuegbare Punkte**: Gang-Level - 2
- **Maximum**: 28 Perks bei Level 30

```
/gang perk <perkname>    # Perk freischalten (nur Boss)
```

### Territorium-Perks (Gruen)

| Perk | Level | Effekt |
|------|-------|--------|
| TERRITORY_EXPAND | 3 | Territoriumslimit: 4 auf 9 Chunks |
| TERRITORY_FORTIFY | 8 | Benachrichtigung bei fremden Gangs im Gebiet |
| TERRITORY_DOMINANCE | 15 | Territoriumslimit: 9 auf 16 Chunks |
| TERRITORY_STRONGHOLD | 22 | Territoriumslimit: 16 auf 25 Chunks + Verkaufsbonus |
| TERRITORY_EMPIRE | 28 | Unbegrenztes Territorium |

### Wirtschaft-Perks (Gold)

| Perk | Level | Effekt |
|------|-------|--------|
| ECONOMY_BANK | 4 | Gemeinsames Gang-Bankkonto |
| ECONOMY_TAX | 10 | +5% Einnahmen aus Shops im Gang-Gebiet |
| ECONOMY_TRADE | 16 | -10% Lager-Lieferkosten |
| ECONOMY_LAUNDERING | 21 | Reduzierte Steuern fuer Gang-Mitglieder |
| ECONOMY_MONOPOLY | 27 | +15% Verkaufsbonus im eigenen Gebiet |

### Verbrechen-Perks (Rot)

| Perk | Level | Effekt |
|------|-------|--------|
| CRIME_PROTECTION | 5 | Fahndungslevel sinkt 20% schneller im Gebiet |
| CRIME_BRIBERY | 11 | -30% Bestechungskosten |
| CRIME_ESCAPE | 17 | -25% Fluchttimer |
| CRIME_INTIMIDATION | 23 | NPCs im Gebiet melden keine Verbrechen |
| CRIME_UNTOUCHABLE | 29 | Max Fahndungslevel auf 3 begrenzt (statt 5) im eigenen Gebiet |

### Produktion-Perks (Cyan)

| Perk | Level | Effekt |
|------|-------|--------|
| PRODUCTION_XP_BOOST | 3 | +15% Produzenten-XP fuer alle Mitglieder |
| PRODUCTION_QUALITY | 9 | +10% Produktionsqualitaet |
| PRODUCTION_SHARED_STORAGE | 14 | Gang-Lager fuer alle Mitglieder zugaenglich |
| PRODUCTION_EFFICIENCY | 20 | -15% Produktionszeit |
| PRODUCTION_MASTERY | 26 | +25% XP + 15% Qualitaet |

## Woechentliche Gebuehren

- **Bereich**: 0-10.000 EUR pro Woche (vom Boss festgelegt)
- **Erhebung**: Automatisch woechentlich bei Online-Mitgliedern
- **Gebuehr nach Rang**: Boss 0%, Underboss 10%, Member 50%, Recruit 100%
- **Auto-Kick**: Nach 3 verpassten Zahlungen in Folge

## Missions-System

### Missionstypen

| Typ | Reset | Anzahl | Bonus-XP | Bonus-Geld |
|-----|-------|--------|----------|------------|
| STUENDLICH | Jede Stunde | 2 | 10 | 500 EUR |
| TAEGLICH | Mitternacht | 3 | 50 | 2.000 EUR |
| WOECHENTLICH | Montag 00:00 | 2 | 150 | 10.000 EUR |

### Beispiel-Missionen

**Stuendlich** (10 Vorlagen): Produkte verkaufen, Items produzieren, Geld verdienen, Pflanzen ernten, Waren liefern, Vielfalt verkaufen, Qualitaetsitems produzieren, Trinkgeld verdienen, Handel abschliessen, Ressourcen sammeln

**Taeglich** (12 Vorlagen): Tagesumsatz, Mitglieder online, Territorien halten, Produktvielfalt, Gang-Guthaben, Gang-XP verdienen, Batch produzieren, Tagesverdienst, Batch verkaufen, Gang-Einzahlung, Batch ernten, Handelsvolumen

**Woechentlich** (8 Vorlagen): Wochenumsatz, Mitglied befoerdern, Perk freischalten, Territorien halten, Missionen abschliessen, Mitglied rekrutieren, Wochen-XP, Wochen-Guthaben

### Szenario-Editor

Der Szenario-Editor (OP Level 2) ermoeglicht das Erstellen komplexer Missionsablaeufe mit 63 Zieltypen in 10 Kategorien:

- **Bewegung** (6): Zu Ort gehen, Zu NPC gehen, NPC folgen, Hinfahren, usw.
- **Interaktion** (12): Mit NPC reden, Handeln, Items sammeln, Schloss knacken, usw.
- **Raub** (11): Laden ausrauben, Bank ueberfallen, Tresor knacken, usw.
- **Ueberleben** (5): Zeit ueberleben, Zone entkommen, Polizei entkommen, usw.
- **Kampf** (6): Mobs toeten, Gebiet verteidigen, NPC bekaempfen, usw.
- **Wirtschaft** (6): Geld verdienen, Items verkaufen, Geld waschen, usw.
- **Fahrzeug** (3): Verfolgungsjagd, Rennen, Fahrzeuglieferung
- **Tarnung** (3): Vorbeischleichen, Verkleiden, NPC ablenken
- **Sonstiges** (9): Warten, Patrouille, Checkpoint, Nachricht senden, usw.
- **Spezial** (2): START (Einstieg), REWARD (Belohnung mit XP/Geld)

```
/gang task editor    # Szenario-Editor oeffnen (OP Level 2)
```

## Befehle

### Spieler-Befehle

| Befehl | Beschreibung |
|--------|-------------|
| `/gang create <name> <tag> [farbe]` | Gang gruenden |
| `/gang invite <spieler>` | Spieler einladen |
| `/gang accept` | Einladung annehmen |
| `/gang leave` | Gang verlassen |
| `/gang kick <spieler>` | Mitglied entfernen |
| `/gang promote <spieler> <rang>` | Mitglied befoerdern |
| `/gang info` | Gang-Info anzeigen |
| `/gang list` | Alle Gangs auflisten |
| `/gang disband` | Gang aufloesen (nur Boss) |
| `/gang perk <perkname>` | Perk freischalten (nur Boss) |

### Admin-Befehle

| Befehl | Beschreibung |
|--------|-------------|
| `/gang admin setlevel <gang-name> <level>` | Gang-Level setzen |
| `/gang admin addxp <gang-name> <xp>` | XP zur Gang hinzufuegen |
| `/gang admin info <gang-name>` | Gang-Details anzeigen |
| `/gang admin setfee <betrag>` | Woechentliche Gebuehr festlegen |

## Datenspeicherung

- **Gang-Daten**: `config/schedulemc_gangs.json`
- **Missions-Daten**: `config/schedulemc_gang_missions.json`
- **Szenario-Daten**: `config/schedulemc_scenarios.json`
