# Gang-System

<div align="center">

**Organisierte Kriminalitaet mit Hierarchie, Perks, Missionen und Territorien**

Level 1–30 Progression, 20 Perks in 4 Branches, automatische Missionen und Gebietsansprueche

[Back to Wiki Home](../Home.md) | [Commands Reference](../Commands.md) | [Territory System](Territory-System.md)

</div>

---

## Table of Contents

1. [Ueberblick](#ueberblick)
2. [Gang erstellen](#gang-erstellen)
3. [Rang-Hierarchie](#rang-hierarchie)
4. [XP und Level-System](#xp-und-level-system)
5. [Perk-System](#perk-system)
6. [Woechentliche Gebuehren](#woechentliche-gebuehren)
7. [Missions-System](#missions-system)
8. [Befehle](#befehle)
9. [Datenspeicherung](#datenspeicherung)
10. [Best Practices](#best-practices)
11. [Fehlerbehebung](#fehlerbehebung)

---

## Ueberblick

Das Gang-System ermoeglicht Spielern die Gruendung krimineller Organisationen mit hierarchischen Raengen, XP-Progression (Level 1-30), freischaltbaren Perks, automatischen Missionen und Gebietsanspruechen. Gangs verdienen kollektiv XP durch Aktivitaeten ihrer Mitglieder und schalten dadurch neue Faehigkeiten frei.

### Kennzahlen

| Metrik | Wert |
|--------|------|
| Max. Gang-Level | 30 |
| Max. Mitglieder (Level 30) | 20 |
| Verfuegbare Perks | 20 (in 4 Branches) |
| Missionstypen | 3 (Stuendlich, Taeglich, Woechentlich) |
| Missionsvorlagen (gesamt) | 30 |
| Szenario-Zieltypen | 63 |
| Reputationsstufen | 5 |

---

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

| Datei | Inhalt |
|-------|--------|
| `config/schedulemc_gangs.json` | Gang-Daten: Mitglieder, Level, XP, Perks, Wochen-Gebuehren |
| `config/schedulemc_gang_missions.json` | Aktive und abgeschlossene Missionen |
| `config/schedulemc_scenarios.json` | Szenario-Definitionen vom Editor |

---

## Best Practices

### Fuer Gang-Bosse

1. **Frueh in Territory investieren** — Territorien generieren 2 XP/Tag pro Chunk. Ab Level 8 mit `TERRITORY_EXPAND` koennen bis zu 9 Chunks gehalten werden — das ergibt 18 XP/Tag passiv.
2. **Missionen aktiv nutzen** — Stuendliche Missionen (50 XP + 500 EUR Bonus) werden oft ignoriert. Teile aktiven Mitgliedern spezifische Missionen zu.
3. **Wochenbeitrag moderat setzen** — Ein zu hoher Beitrag fuehrt zu Auto-Kicks nach 3 verpassten Zahlungen. Starte mit 500–1.000 EUR/Woche.
4. **Perk-Prioritaeten**:
   - Level 3: `PRODUCTION_XP_BOOST` oder `TERRITORY_EXPAND`
   - Level 5: `CRIME_PROTECTION` (wichtig fuer illegale Produktion)
   - Level 10: `ECONOMY_TAX` oder `TERRITORY_FORTIFY`
5. **Boss-Transfer vorsichtig nutzen** — Der aktuelle Boss wird automatisch zum UNDERBOSS degradiert. Nur an vertrauenswuerdige Spieler weitergeben.

### Fuer Server-Admins

1. **Gruendungskosten anpassen** — Standard: 25.000 EUR fuer Gang-Gruendung (Level 15). Zu niedrig = zu viele Gangs; zu hoch = wenig Gang-Aktivitaet.
2. **XP-Kurve balancieren** — Die XP-Formel `200 * Level^1.7` ergibt ~656.000 XP fuer Level 30. Passe `BASE_XP` an die Server-Aktivitaet an.
3. **Szenario-Editor nutzen** — Erstelle einzigartige Raid-Szenarien mit `/gang task editor` fuer Server-Events.
4. **Territorien vorplanen** — Konfiguriere mit `/map edit` farbliche Zonen fuer spaeteren Gang-Wettbewerb.
5. **Gang-Monitor** — Nutze `/gang admin info <name>` regelmaessig um inaktive Gangs zu identifizieren und ggf. aufzuloesen.

---

## Fehlerbehebung

### Gang kann nicht gegruendet werden

1. **Level pruefen** — Mindest-Produzentenlevel 15 erforderlich. Pruefe mit `/level` oder Smartphone-App.
2. **Kosten pruefen** — 25.000 EUR auf dem Bankkonto benoetigt. Pruefe mit `/money balance`.
3. **Bereits in einer Gang** — Verlasse die aktuelle Gang zuerst mit `/gang leave`.
4. **Name bereits vergeben** — Gang-Namen sind nicht case-sensitiv und muessen eindeutig sein.

### Perk kann nicht freigeschaltet werden

1. **Perk-Punkte pruefen** — `verfuegbare Punkte = Gang-Level - 2`. Bei Level 10: 8 Punkte.
2. **Nur Boss** — Nur der Gang-Boss kann Perks freischalten.
3. **Perk-Voraussetzung** — Einige Perks haben Level-Anforderungen (z.B. `TERRITORY_EXPAND` ab Level 3).

### Missionen erscheinen nicht

1. **Automatische Generierung** — Missionen werden automatisch zum Reset-Zeitpunkt generiert (stuendlich / taeglich / Montag 00:00 Uhr).
2. **Keine Mitglieder online** — Manche Missionstypen erfordern Online-Mitglieder.
3. **Datei pruefen** — Pruefe `config/schedulemc_gang_missions.json` auf Korruption.

### Gang-Daten nach Neustart verloren

1. **Datei pruefen** — `config/schedulemc_gangs.json` auf gueltiges JSON pruefen.
2. **Backup wiederherstellen** — Aus `config/backups/schedulemc_gangs_<timestamp>.json` wiederherstellen.
3. **Schreibrechte** — Der Server braucht Schreibzugriff auf das `config/`-Verzeichnis.
