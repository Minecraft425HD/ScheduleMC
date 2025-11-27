# ScheduleMC - Benutzer-Anleitung

## Inhaltsverzeichnis

1. [Erste Schritte](#erste-schritte)
2. [Plot-System](#plot-system)
3. [Apartment-System](#apartment-system)
4. [Wirtschaftssystem](#wirtschaftssystem)
5. [NPC-System](#npc-system)
6. [Tabak-System](#tabak-system)
7. [Polizei & Verbrechen](#polizei--verbrechen)
8. [Diebstahl-Minigame](#diebstahl-minigame)
9. [Shop-System](#shop-system)
10. [Tägliche Belohnungen](#tägliche-belohnungen)
11. [Admin-Befehle](#admin-befehle)
12. [FAQ](#faq)

---

## Erste Schritte

### Beim ersten Beitritt

Wenn du zum ersten Mal auf einen Server mit ScheduleMC beitrittst:

1. **Automatische Konto-Erstellung**: Du erhältst automatisch ein Bankkonto mit 1000€ Startguthaben
2. **Überprüfe dein Guthaben**: `/money`
3. **Hole deine erste tägliche Belohnung**: `/daily`

### Grundlegende Konzepte

- **Plots**: Geschützte Grundstücke, die du kaufen, verkaufen und vermieten kannst
- **Apartments**: Unterplots innerhalb von Plots zur Untervermietung
- **Economy**: Ingame-Währung (€) für alle Transaktionen
- **NPCs**: Nicht-Spieler-Charaktere mit KI, die du treffen und handeln kannst
- **Wanted-Level**: Dein Verbrechenslevel (0-5 Sterne)

---

## Plot-System

### Was sind Plots?

Plots sind geschützte Grundstücksbereiche, die du:
- Kaufen und verkaufen kannst
- An andere Spieler vermieten kannst
- Mit Freunden teilen kannst (Trusted Players)
- Bewerten kannst

### Plot erstellen

**Schritt 1: Selection-Tool erhalten**
```
/plot wand
```
Du erhältst einen goldenen Spaten als Selection-Tool.

**Schritt 2: Bereich markieren**
- **Rechtsklick** auf den ersten Eckblock
- **Rechtsklick** auf den zweiten Eckblock (diagonal gegenüber)
- Du erhältst eine Bestätigung der Auswahlgröße

**Schritt 3: Plot erstellen**
```
/plot create <preis>
```
- `<preis>`: Der Kaufpreis für den Plot (z.B. 500)
- Mindestgröße: 64 Blöcke
- Maximalgröße: 1.000.000 Blöcke
- Mindestpreis: 1€
- Maximalpreis: 1.000.000€

**Beispiel**:
```
/plot create 500
```
Erstellt einen Plot mit 500€ Kaufpreis.

### Plot-Verwaltung

#### Informationen anzeigen

```
/plot info
```
Zeigt Informationen über den Plot an, auf dem du stehst:
- Plot-ID
- Besitzer
- Größe
- Preis
- Vertraute Spieler
- Durchschnittliche Bewertung
- Miete-Status

#### Alle Plots auflisten

```
/plot list
```
Zeigt alle Plots auf dem Server an.

#### Deine Plots anzeigen

```
/plot owned
```
Liste aller Plots, die dir gehören.

#### Plot kaufen

```
/plot buy [plot_id]
```
- Ohne `plot_id`: Kauft den Plot, auf dem du stehst
- Mit `plot_id`: Kauft einen bestimmten Plot

**Beispiel**:
```
/plot buy
/plot buy 123
```

#### Plot verkaufen

```
/plot sell <preis>
```
Bietet deinen Plot zum Verkauf an.

**Beispiel**:
```
/plot sell 1000
```

#### Verkaufsangebot abbrechen

```
/plot cancel
```
Entfernt den Plot vom Verkauf.

#### Plot übertragen

```
/plot transfer <spieler>
```
Überträgt den Besitz des Plots direkt an einen anderen Spieler (kostenlos).

**Beispiel**:
```
/plot transfer Steve
```

#### Plot aufgeben

```
/plot abandon
```
- Gibt den Plot auf
- Du erhältst 50% des ursprünglichen Kaufpreises zurück
- Alle Trusted Players werden entfernt

### Vertraute Spieler (Trusted Players)

#### Spieler berechtigen

```
/plot trust <spieler>
```
Erlaubt einem Spieler, auf deinem Plot zu bauen.

**Beispiel**:
```
/plot trust Alex
```

#### Berechtigung entfernen

```
/plot untrust <spieler>
```
Entfernt die Berechtigung eines Spielers.

#### Alle vertrauten Spieler anzeigen

```
/plot trusted
```

**Limits**:
- Maximal 10 vertraute Spieler pro Plot

### Plot-Miete

#### Plot zur Miete anbieten

```
/plot rent <preis_pro_tag>
```
Bietet deinen Plot zur Miete an.

**Beispiel**:
```
/plot rent 50
```
Miete: 50€ pro Tag

#### Plot mieten

```
/plot rentplot <tage> [plot_id]
```
Miete einen Plot für eine bestimmte Anzahl von Tagen.

**Beispiel**:
```
/plot rentplot 7
/plot rentplot 14 123
```

**Limits**:
- Mindestpreis: 10€ pro Tag
- Mindestdauer: 1 Tag
- Maximaldauer: 30 Tage

#### Mietangebot abbrechen

```
/plot cancelrent
```

#### Mieter automatisch rauswerfen

Wenn die Mietdauer abgelaufen ist, wird der Mieter automatisch rausgeworfen (wenn `AUTO_EVICT_EXPIRED = true`).

### Plot-Bewertungen

#### Plot bewerten

```
/plot rate <1-5>
```
Bewerte den Plot, auf dem du stehst (1-5 Sterne).

**Beispiel**:
```
/plot rate 5
```

**Einschränkungen**:
- Du kannst nur Plots bewerten, die dir nicht gehören
- Standardmäßig nur eine Bewertung pro Plot möglich

#### Bewertungen anzeigen

```
/plot ratings
```
Zeigt alle Bewertungen des aktuellen Plots an.

### Plot-Schutz

#### Was ist geschützt?

- **Blöcke**: Nur der Besitzer und vertraute Spieler können Blöcke platzieren/abbauen
- **Inventare**: Nur berechtigte Spieler können Truhen, Öfen, etc. öffnen
- **Interaktionen**: Türen, Knöpfe, Hebel sind geschützt

#### Schutz-Overlay

Bewege deine Maus über einen Block, um zu sehen:
- Ob du auf einem Plot stehst
- Wem der Plot gehört
- Ob du berechtigt bist zu bauen

---

## Apartment-System

### Was sind Apartments?

Apartments sind **Unterplots innerhalb von Plots**. Als Plot-Besitzer kannst du deinen Plot in mehrere Apartments unterteilen und diese separat vermieten.

### Unterschied zu Plots

| Feature | Plot | Apartment |
|---------|------|-----------|
| Besitz | Kaufbar | Nur mietbar |
| Erstellung | Mit Selection-Tool | Innerhalb eines Plots |
| Kaution | Nein | Ja |
| Untervermietung | Ja (Apartments) | Nein |

### Apartment erstellen (als Plot-Besitzer)

**Schritt 1: Apartment Selection-Tool erhalten**
```
/plot apartment wand
```
Du erhältst einen Diamant-Spaten als Apartment-Selection-Tool.

**Schritt 2: Bereich markieren**
- **Rechtsklick** auf den ersten Eckblock (innerhalb deines Plots)
- **Rechtsklick** auf den zweiten Eckblock
- Der Bereich muss vollständig innerhalb deines Plots liegen

**Schritt 3: Apartment erstellen**
```
/plot apartment create <name> <miete_pro_tag>
```

**Beispiel**:
```
/plot apartment create Wohnung_1A 25
```
Erstellt ein Apartment namens "Wohnung_1A" mit 25€ Miete pro Tag.

### Apartment-Verwaltung

#### Alle Apartments auflisten

```
/plot apartment list
```
Zeigt alle verfügbaren Apartments auf dem Server.

#### Deine vermieteten Apartments anzeigen

```
/plot apartment owned
```
Zeigt alle Apartments, die du vermietest.

#### Apartment mieten

```
/plot apartment rent <apartment_id>
```

**Kaution**: Beim Mieten wird automatisch eine Kaution in Höhe von 2 Monatsmieten fällig.

**Beispiel**:
```
/plot apartment rent 5
```
- Miete: 25€/Tag
- Kaution: 1500€ (25€ × 30 Tage × 2)
- Gesamtkosten beim Einzug: 1525€ (erste Miete + Kaution)

#### Aus Apartment ausziehen

```
/plot apartment leave
```
- Beendet das Mietverhältnis
- Die Kaution wird vollständig zurückerstattet
- Verbleibende Miettage werden nicht erstattet

#### Mieter rauswerfen (als Apartment-Besitzer)

```
/plot apartment evict <apartment_id>
```
- Wirft den Mieter sofort raus
- Die Kaution wird zurückerstattet

**Beispiel**:
```
/plot apartment evict 5
```

#### Apartment löschen (als Apartment-Besitzer)

```
/plot apartment delete <apartment_id>
```
- Löscht das Apartment permanent
- Mieter werden automatisch rausgeworfen
- Kaution wird zurückerstattet

### Apartment-Schutz

- Apartments haben den gleichen Schutz wie Plots
- Nur der Mieter kann innerhalb des Apartments bauen
- Der Plot-Besitzer kann ebenfalls bauen (Ausnahme)

---

## Wirtschaftssystem

### Konten-System

Jeder Spieler hat ein Bankkonto mit:
- **Kontostand**: Dein aktuelles Guthaben
- **Transaktions-Historie**: Alle Ein- und Auszahlungen (intern)
- **Automatisches Auto-Save**: Alle 5 Minuten

#### Kontostand anzeigen

```
/money
```
Zeigt deinen aktuellen Kontostand an.

**Ausgabe**:
```
Dein Kontostand: 1.250,00€
```

#### Geld senden

```
/pay <spieler> <betrag>
```
Sendet Geld an einen anderen Spieler.

**Beispiel**:
```
/pay Steve 100
```
Sendet 100€ an Steve.

**Einschränkungen**:
- Du kannst nicht mehr senden, als du hast
- Betrag muss positiv sein
- Spieler muss online sein

### Wallet-System

Das Wallet-System erlaubt es dir, **physisches Geld** als Item bei dir zu tragen.

#### Cash-Item

- **Name**: Cash
- **Item**: Smaragd
- **Werte**: 1€, 5€, 10€, 50€, 100€, 500€, 1000€

#### Geld abheben

Verwende einen **Geldautomaten (ATM)**:
1. **Rechtsklick** auf einen ATM-Block
2. Wähle den Betrag zum Abheben
3. Das Geld wird von deinem Konto abgebucht
4. Du erhältst Cash-Items in dein Inventar

#### Geld einzahlen

Verwende einen **Geldautomaten (ATM)**:
1. **Rechtsklick** auf einen ATM-Block
2. Wähle "Einzahlen"
3. Cash-Items aus deinem Inventar werden eingezahlt
4. Dein Kontostand erhöht sich

#### Cash-Item Schutz

- Cash-Items können **nicht** in bestimmte Slots verschoben werden
- Verhindert versehentliches Droppen oder Verlieren

### Einnahmequellen

| Quelle | Beschreibung | Betrag |
|--------|--------------|--------|
| **Startguthaben** | Beim ersten Beitritt | 1000€ |
| **Tägliche Belohnung** | Einmal pro Tag | 50€ + Streak-Bonus |
| **Plot-Verkauf** | Plots verkaufen | Variabel |
| **Plot-Miete** | Plots vermieten | Preis × Tage |
| **Apartment-Miete** | Apartments vermieten | Miete pro Tag |
| **Shop-Verkauf** | Items an NPCs verkaufen | Variabel |
| **NPC-Gehalt** | Als NPC-Besitzer (wenn implementiert) | Variabel |

### Ausgaben

| Ausgabe | Beschreibung | Betrag |
|---------|--------------|--------|
| **Plot-Kauf** | Plots kaufen | Verkaufspreis |
| **Plot-Erstellung** | Plot erstellen | Kostenlos |
| **Plot-Miete** | Plots mieten | Miete × Tage |
| **Apartment-Miete** | Apartment mieten | Miete + Kaution |
| **Shop-Kauf** | Items von NPCs kaufen | Variabel |
| **Kaution** | Verhaftung | Variabel |
| **Polizei-Raid** | Illegal Cash | 10% des Kontostands |

---

## NPC-System

### NPC-Typen

#### 1. Bewohner (RESIDENT)
- Normale Stadtbewohner
- Haben Home, Work, Leisure Locations
- Können bestohlen werden
- Reagieren auf Diebstahl

#### 2. Händler (MERCHANT)
- Betreiben Shops
- Kaufen und verkaufen Items
- Verhandeln Preise (Tabak-Handel)
- Haben Business-Metriken

#### 3. Polizei (POLICE)
- Verfolgen Spieler mit Wanted-Level
- Verhaften bei Kontakt
- Rufen Verstärkung
- Führen Raids durch

### Persönlichkeits-Typen

Jeder NPC hat eine Persönlichkeit, die sein Verhalten beeinflusst:

1. **FRIENDLY**: Freundlich, kooperativ, niedrige Preise
2. **NEUTRAL**: Ausgewogen, faire Preise
3. **AGGRESSIVE**: Aggressiv, hohe Preise, schnell verärgert
4. **SHY**: Schüchtern, vorsichtig, bevorzugt bekannte Spieler

### NPC-Interaktionen

#### Mit NPC sprechen

- **Rechtsklick** auf einen NPC
- Je nach NPC-Typ öffnet sich:
  - **Händler**: Shop-Menü
  - **Bewohner**: Dialog/Diebstahl-Option
  - **Polizei**: Keine direkte Interaktion

#### NPC-Skins

NPCs können echte Spieler-Skins verwenden:
- Wird beim Spawnen festgelegt
- Lädt Skin von Mojang-Servern
- Fallback zu Standard-Skin bei Fehler

### NPC-KI & Verhalten

#### Zeitplan-System

NPCs folgen einem Tagesablauf:

| Tageszeit | Aktivität | Ort |
|-----------|-----------|-----|
| 6:00 - 9:00 | Zu Arbeit gehen | Work Location |
| 9:00 - 17:00 | Arbeiten | Work Location |
| 17:00 - 19:00 | Nach Hause gehen | Home Location |
| 19:00 - 22:00 | Freizeit | Leisure Location |
| 22:00 - 6:00 | Schlafen | Home Location |

#### Navigation

- NPCs verwenden Pathfinding zur Navigation
- Vermeiden Hindernisse
- Nur auf "walkable" Blöcken (konfigurierbar)
- Können Treppen, Stufen und Platten verwenden

### NPC-Shops

#### Shop-Kategorien

- **BAUMARKT**: Baumaterialien, Werkzeuge
- **LEBENSMITTEL**: Nahrung, Getränke
- **WAFFEN**: Waffen, Rüstung
- **SONSTIGES**: Verschiedenes

#### Bei NPC kaufen

1. Rechtsklick auf Händler-NPC
2. Wähle Item aus Shop-Liste
3. Klicke auf "Kaufen"
4. Geld wird von deinem Konto abgebucht
5. Item geht in dein Inventar

#### An NPC verkaufen

1. Rechtsklick auf Händler-NPC
2. Wähle "Verkaufen"
3. Wähle Item aus deinem Inventar
4. Bestätige Verkauf
5. Geld wird deinem Konto gutgeschrieben

#### Preise

- **Kaufpreis**: Basispreis × 1.5
- **Verkaufspreis**: Basispreis × 0.5

---

## Tabak-System

### Übersicht

Das Tabak-System bietet einen kompletten Anbauzyklus von der Saat bis zur fertigen Verpackung.

### Tabak-Sorten

| Sorte | Beschreibung | Verwendung |
|-------|--------------|------------|
| **Virginia** | Mild, süßlich | Premium-Mischungen |
| **Burley** | Kräftig, nussig | Stärke |
| **Oriental** | Aromatisch, würzig | Aroma |
| **Havana** | Vollmundig, edel | Luxus-Produkte |

### Qualitätsstufen

1. **LOW**: Niedrige Qualität (billig)
2. **MEDIUM**: Mittlere Qualität
3. **HIGH**: Hohe Qualität
4. **PREMIUM**: Premium-Qualität (teuer)

**Qualität wird beeinflusst durch**:
- Topf-Typ (Ton, Keramik, etc.)
- Bewässerung
- Bodenqualität
- Fermentationsdauer
- Verwendung von Boostern

### Anbauprozess

#### 1. Samen kaufen

Kaufe Samen bei einem Händler-NPC oder im Creative Mode:
- Virginia Seeds
- Burley Seeds
- Oriental Seeds
- Havana Seeds

#### 2. Topf platzieren

Platziere einen Tabak-Topf:
- **Einfacher Topf**: Basis-Qualität
- **Keramik-Topf**: Mittlere Qualität
- **Glasierter Topf**: Hohe Qualität
- **Premium-Topf**: Beste Qualität

**Topf-Eigenschaften**:
- Jeder Topf benötigt einen Bodentyp (Erde, Lehm, etc.)
- Muss bewässert werden
- Kann mit Düngern verbessert werden

#### 3. Pflanzen

- **Rechtsklick** mit Samen auf Topf
- Samen werden verbraucht
- Pflanze beginnt zu wachsen

#### 4. Wachstum

**Wachstumsstufen**: 0-7 (8 Stufen insgesamt)

**Wachstumsgeschwindigkeit** wird beeinflusst durch:
- Lichtlevel (Pflanzenlicht erhöht Wachstum)
- Bewässerung (regelmäßig gießen)
- Dünger (Wachstums-Booster)

**Gießen**:
- Verwende Gießkanne (Rechtsklick auf Topf)
- Wasserlevel sinkt über Zeit
- Bei zu wenig Wasser verlangsamt sich Wachstum

#### 5. Ernten

Wenn die Pflanze Stufe 7 erreicht hat:
- **Rechtsklick** auf Pflanze
- Du erhältst **Frische Blätter** (Qualität abhängig vom Anbau)
- Pflanze wird entfernt

#### 6. Trocknen

Platziere ein **Trockengestell** (Drying Rack):
- **Rechtsklick** mit frischen Blättern auf Gestell
- Trocknungsprozess startet
- Nach einiger Zeit: **Getrocknete Blätter**

**Trocknungsdauer**: Konfigurierbar (Standard: einige Minuten)

#### 7. Fermentieren

Platziere ein **Fermentationsfass** (Fermentation Barrel):
- **Rechtsklick** mit getrockneten Blättern auf Fass
- Fermentationsprozess startet
- Nach Abschluss: **Fermentierte Blätter** (erhöhte Qualität)

**Fermentationsdauer**: Konfigurierbar (Standard: länger als Trocknung)

#### 8. Verpacken

Verwende einen **Verpackungstisch** (Packaging Table):

**Verpackungsgrößen**:
1. **SMALL** (Klein): 50g
2. **MEDIUM** (Mittel): 100g
3. **LARGE** (Groß): 250g
4. **XLARGE** (XL): 500g

**Verpackungs-Container**:
- **Beutel**: Basis-Verpackung
- **Glas**: Luftdicht (bessere Qualität)
- **Box**: Luxus-Verpackung

**Prozess**:
1. Rechtsklick auf Packaging Table
2. Lege fermentierte Blätter ein
3. Wähle Container-Typ
4. Wähle Verpackungsgröße
5. Starte Verpackung
6. Erhalte verpackten Tabak

### Tabak-Items

#### Samen
- Virginia Seeds
- Burley Seeds
- Oriental Seeds
- Havana Seeds

#### Blätter (jeweils 3 Stufen pro Sorte)
- Frische Blätter (Fresh Leaves)
- Getrocknete Blätter (Dried Leaves)
- Fermentierte Blätter (Fermented Leaves)

#### Verpackte Produkte
- 4 Sorten × 3 Qualitäten × 4 Größen = 48 mögliche Kombinationen

#### Hilfsmittel
- **Gießkanne**: Zum Bewässern
- **Erdsäcke**: Verschiedene Bodentypen
- **Wachstums-Flasche**: Beschleunigt Wachstum
- **Qualitäts-Flasche**: Erhöht Qualität
- **Dünger-Flasche**: Verbessert Bodenqualität

### Tabak verkaufen

#### An NPC-Händler verkaufen

1. Rechtsklick auf Händler-NPC
2. Öffne **Tabak-Verhandlungs-Menü**
3. Wähle dein verpacktes Produkt
4. **Preisverhandlung**:
   - NPC macht Angebot basierend auf:
     - Sorte
     - Qualität
     - Größe
     - NPC-Persönlichkeit
     - Business-Metriken (Angebot, Nachfrage, Reputation)
   - Du kannst:
     - **Akzeptieren**: Verkauf bestätigen
     - **Ablehnen**: Verhandlung abbrechen
     - **Gegenangebot**: Höheren Preis vorschlagen

**Verhandlungs-System**:
- NPCs haben eine **Akzeptanz-Schwelle**
- Zu hohe Gegenangebote werden abgelehnt
- **Friendly** NPCs: Akzeptieren eher höhere Preise
- **Aggressive** NPCs: Lehnen leicht ab
- Erfolgreiche Verkäufe erhöhen **Reputation**

#### Preis-Faktoren

| Faktor | Einfluss |
|--------|----------|
| **Sorte** | Havana > Oriental > Virginia > Burley |
| **Qualität** | Premium > High > Medium > Low |
| **Größe** | XL > Large > Medium > Small |
| **Container** | Box > Glas > Beutel |
| **NPC-Persönlichkeit** | Friendly zahlt mehr |
| **Business-Metrics** | Hohe Nachfrage = höhere Preise |

### Tabak-Befehle

```
/tobacco info
```
Zeigt Informationen über das Tabak-System.

```
/tobacco recipes
```
Zeigt alle Tabak-Rezepte und Verarbeitungsschritte.

---

## Polizei & Verbrechen

### Wanted-Level System

#### Wanted-Level Stufen

| Sterne | Beschreibung | Polizei-Reaktion |
|--------|--------------|------------------|
| ⭐ | Kleines Vergehen | 1 Polizist |
| ⭐⭐ | Vergehen | 2 Polizisten |
| ⭐⭐⭐ | Verbrechen | 3 Polizisten + Verstärkung |
| ⭐⭐⭐⭐ | Schweres Verbrechen | 4 Polizisten + Helikopter (wenn implementiert) |
| ⭐⭐⭐⭐⭐ | Kapitalverbrechen | Massive Polizeipräsenz |

#### Wie erhält man Wanted-Level?

- **Diebstahl** (erfolgreich): +1 Stern
- **Diebstahl** (erwischt): +2 Sterne
- **NPC K.O. schlagen**: +1 Stern
- **Illegales Cash besitzen**: Polizei-Raid (bei Entdeckung)

#### Abbau von Wanted-Level

- **Automatisch**: -1 Stern pro Tag (reale Zeit)
- **Kaution zahlen**: Entfernt alle Sterne (im Krankenhaus)

### Polizei-Verfolgung

#### Wie funktioniert die Verfolgung?

1. **Erkennung**: Polizei-NPCs erkennen Spieler mit Wanted-Level im Radius von 32 Blöcken
2. **Verfolgung**: Polizei verfolgt den Spieler
3. **Verstärkung**: Bei höherem Wanted-Level rufen Polizisten Verstärkung
4. **Verhaftung**: Bei Kontakt wird der Spieler verhaftet

#### Verhaftung

Wenn ein Polizist dich erreicht:
- Du wirst **ins Krankenhaus teleportiert**
- Du wirst für kurze Zeit **bewegungsunfähig** (Arrest Cooldown: 5 Sekunden)
- Du musst **Kaution zahlen**, um frei zu kommen

### Vor Polizei verstecken

#### Indoor-Versteck

- Gehe in ein **Gebäude** (geschlossener Raum mit Dach)
- Polizei verliert dich, wenn du nicht mehr sichtbar bist
- **Search-Duration**: 60 Sekunden (Polizei sucht weiter)
- Nach Ablauf: Polizei gibt auf

**Tür-Blockierung**:
- Während Verfolgung blockiert die Polizei Türen
- Du kannst Türen **nicht öffnen**, während Polizei in der Nähe ist
- Verhindert einfaches Entkommen

#### Search-Verhalten

Wenn Polizei dich verliert:
- **Search-Radius**: 50 Blöcke
- **Search-Duration**: 60 Sekunden
- Polizei durchsucht die Umgebung
- Nach Ablauf: Polizei kehrt zu Patrouille zurück

### Kaution & Krankenhaus

#### Hospital-Commands

```
/hospital bail <betrag>
```
Zahle Kaution, um aus dem Krankenhaus rauszukommen.

**Kautions-Berechnung**:
- Basisbetrag: 100€ pro Stern
- Beispiel: 3 Sterne = 300€ Kaution

```
/hospital status
```
Zeigt deinen Arrest-Status an:
- Ob du verhaftet bist
- Remaining arrest time
- Benötigte Kaution

#### Respawn

Nach Verhaftung:
- Du spawnst im **Krankenhaus** (Hospital Respawn Point)
- Muss konfiguriert sein (Standard: World Spawn)

### Polizei-Raids

#### Was sind Raids?

Wenn du **illegales Cash** besitzt (> 10.000€), kann die Polizei einen Raid durchführen.

#### Raid-Ablauf

1. **Erkennung**: Polizei scannt Spieler im Radius von 20 Blöcken
2. **Cash-Überprüfung**: Prüft, ob Spieler > 10.000€ Cash bei sich trägt
3. **Raid-Strafe**:
   - **10% des Kontostands** als Geldstrafe
   - **Mindeststrafe**: 1.000€
   - Cash-Items werden konfisziert

**Beispiel**:
- Kontostand: 50.000€
- Cash bei dir: 15.000€
- Strafe: 5.000€ (10% von 50.000€)
- Verbleibender Kontostand: 45.000€
- Cash-Items: Konfisziert

#### Wie vermeidet man Raids?

- Trage nicht zu viel Cash bei dir
- Zahle regelmäßig Cash aufs Konto ein (ATM)
- Vermeide Polizei-NPCs mit viel Cash

---

## Diebstahl-Minigame

### Übersicht

Das Diebstahl-Minigame ist ein interaktives Gameplay-Element zum Bestehlen von Bewohner-NPCs.

### Wie funktioniert es?

#### 1. NPC auswählen

- Finde einen **Bewohner-NPC** (RESIDENT)
- **Rechtsklick** auf NPC
- Wähle "Bestehlen" im Dialog

#### 2. Minigame startet

Ein GUI öffnet sich mit:
- **Beweglicher Indikator**: Bewegt sich schnell hin und her
- **Erfolgszone** (grün): Kleiner Bereich in der Mitte
- **Fehlzone** (rot): Außerhalb der Erfolgszone

#### 3. Timing

- **Klicke**, wenn der Indikator in der **Erfolgszone** ist
- **Erfolg**: Du erhältst Beute
- **Fehlschlag**: Versuch verbraucht

#### 4. Versuche

- Du hast **bis zu 3 Versuche**
- Nach 3 Fehlschlägen: Minigame endet (erwischt)

#### 5. NPC-Reaktion

- **Erfolg**: NPC wird K.O. geschlagen (für kurze Zeit)
- **Fehlschlag**: NPC greift dich an
- **Wanted-Level**: +1 Stern (Erfolg) oder +2 Sterne (erwischt)

### Schwierigkeit

Die Schwierigkeit wird durch die **Erfolgszonen-Größe** bestimmt:

| Schwierigkeit | Zonengröße | Beschreibung |
|---------------|------------|--------------|
| **Einfach** | 15% | Große grüne Zone |
| **Mittel** | 10% | Mittlere Zone |
| **Schwer** | 5% | Kleine Zone |

**Faktor-Bestimmung**:
- NPC-Persönlichkeit
- Tageszeit
- NPC-Aktivität
- Zufall

### Beute

Bei erfolgreichem Diebstahl:
- **Geld**: 10-100€ (Zufall)
- **Items**: Zufällige Items aus NPC-Inventar (wenn implementiert)
- **Cash-Items**: NPC kann Cash bei sich tragen

### Risiken

- **Wanted-Level**: +1-2 Sterne
- **NPC-Angriff**: NPC greift dich an (wenn erwischt)
- **Polizei**: Polizei wird auf dich aufmerksam
- **Reputation**: Verschlechtert sich (bei Händlern)

### Tipps

- Wähle **Shy** oder **Friendly** NPCs (einfacher)
- Diebe **nachts** (weniger Zeugen, wenn implementiert)
- Verstecke dich nach Diebstahl (Polizei-Verfolgung)
- Zahle Cash regelmäßig ein (Raid-Vermeidung)

---

## Shop-System

### Shop-Kategorien

- **BAUMARKT**: Blöcke, Werkzeuge, Baumaterialien
- **LEBENSMITTEL**: Nahrung, Getränke
- **WAFFEN**: Waffen, Rüstung, Pfeile
- **SONSTIGES**: Verschiedenes, seltene Items

### Items kaufen

```
/shop list [kategorie]
```
Zeigt alle verfügbaren Shop-Items an.

**Beispiel**:
```
/shop list
/shop list baumarkt
```

```
/shop buy <item_id> [anzahl]
```
Kauft ein Item aus dem Shop.

**Beispiel**:
```
/shop buy dirt 64
/shop buy diamond_sword
```

### Items verkaufen

```
/shop sell <item> [anzahl]
```
Verkauft ein Item aus deinem Inventar.

**Beispiel**:
```
/shop sell dirt 64
/shop sell diamond
```

### Preise

- **Kaufpreis**: Basispreis × 1.5
- **Verkaufspreis**: Basispreis × 0.5

**Beispiel**:
- Dirt (Basispreis: 1€)
  - Kaufpreis: 1.50€
  - Verkaufspreis: 0.50€

### NPC-Shops vs. Command-Shops

| Feature | NPC-Shop | Command-Shop |
|---------|----------|--------------|
| Zugriff | Rechtsklick auf NPC | `/shop` Befehle |
| Preise | Dynamisch (NPC-abhängig) | Fix (Multiplikatoren) |
| Verhandlung | Ja (Tabak) | Nein |
| Verfügbarkeit | NPC-abhängig | Immer |

---

## Tägliche Belohnungen

### System

Erhalte jeden Tag eine Belohnung für deinen Login.

### Belohnungen claimen

```
/daily
```

**Belohnungs-Berechnung**:
- **Basis-Belohnung**: 50€
- **Streak-Bonus**: +10€ pro konsekutivem Tag
- **Maximum**: 30 Tage Streak

**Beispiel**:
- Tag 1: 50€
- Tag 2: 60€ (50€ + 10€)
- Tag 3: 70€ (50€ + 20€)
- ...
- Tag 30: 350€ (50€ + 300€)

### Streak-System

#### Was ist ein Streak?

Ein Streak ist die Anzahl aufeinanderfolgender Tage, an denen du deine Belohnung geclaimt hast.

#### Streak anzeigen

```
/daily streak
```

**Ausgabe**:
```
Deine täglichen Belohnungs-Statistiken:
- Aktueller Streak: 5 Tage
- Längster Streak: 12 Tage
- Gesamt geclaimt: 47 mal
- Nächste Belohnung: 100€
```

#### Streak verlieren

- Wenn du einen Tag **nicht** claimst, wird dein Streak auf 0 zurückgesetzt
- Der längste Streak bleibt als Rekord erhalten

### Cooldown

- Du kannst nur **einmal pro 24 Stunden** eine Belohnung claimen
- Der Cooldown basiert auf **realer Zeit** (nicht Ingame-Zeit)

---

## Admin-Befehle

### Economy-Admin

#### Guthaben setzen

```
/money set <spieler> <betrag>
```
Setzt das Guthaben eines Spielers auf einen bestimmten Betrag.

**Beispiel**:
```
/money set Steve 5000
```

#### Geld geben

```
/money give <spieler> <betrag>
```
Gibt einem Spieler Geld (addiert zum aktuellen Guthaben).

**Beispiel**:
```
/money give Steve 1000
```

#### Geld nehmen

```
/money take <spieler> <betrag>
```
Nimmt einem Spieler Geld weg.

**Beispiel**:
```
/money take Steve 500
```

### NPC-Admin

#### NPC spawnen

```
/npc spawn <type> <name>
```

**NPC-Typen**:
- `resident`: Bewohner
- `merchant`: Händler
- `police`: Polizist

**Beispiel**:
```
/npc spawn merchant Händler_Karl
/npc spawn police Officer_Schmidt
/npc spawn resident Anna_Müller
```

#### NPC entfernen

```
/npc remove <uuid>
```
Entfernt einen NPC permanent.

**Beispiel**:
```
/npc remove 123e4567-e89b-12d3-a456-426614174000
```

**UUID finden**:
- Verwende F3+H im Spiel
- Schaue auf den NPC
- UUID wird im Tooltip angezeigt

#### NPC-Shop konfigurieren

```
/npc setshop <uuid> <kategorie>
```

**Kategorien**:
- `baumarkt`
- `lebensmittel`
- `waffen`
- `sonstiges`

**Beispiel**:
```
/npc setshop 123e4567-e89b-12d3-a456-426614174000 baumarkt
```

### Plot-Admin

Die meisten Plot-Befehle sind bereits für normale Spieler verfügbar. Admins haben jedoch zusätzliche Rechte:

- Können **jeden Plot** bearbeiten (nicht nur eigene)
- Können Plots **löschen** ohne Rückerstattung
- Können Spieler aus **jedem Plot** entfernen

---

## FAQ

### Allgemein

**Q: Wie starte ich mit ScheduleMC?**
A: Trete dem Server bei, verwende `/daily` für die erste Belohnung und `/plot wand` um deinen ersten Plot zu erstellen.

**Q: Wie verdiene ich Geld?**
A: Tägliche Belohnungen, Plot-Verkauf/-Vermietung, Shop-Verkäufe, Tabak-Handel mit NPCs.

**Q: Kann ich mein Guthaben verlieren?**
A: Ja, durch Ausgaben (Käufe, Mieten), Polizei-Raids oder Admin-Actions.

### Plots

**Q: Wie groß kann mein Plot sein?**
A: Minimum 64 Blöcke, Maximum 1.000.000 Blöcke.

**Q: Kann ich mehrere Plots besitzen?**
A: Ja, es gibt kein Limit (standardmäßig).

**Q: Was passiert, wenn ich einen Plot aufgebe?**
A: Du erhältst 50% des ursprünglichen Kaufpreises zurück.

**Q: Kann ich einen Plot zurückkaufen?**
A: Nein, aufgegebene Plots sind permanent gelöscht.

**Q: Wie schütze ich meinen Plot?**
A: Plots sind automatisch geschützt. Nur du und vertraute Spieler können bauen.

### Apartments

**Q: Was ist der Unterschied zwischen Plot und Apartment?**
A: Apartments sind Unterplots innerhalb von Plots und können nur gemietet (nicht gekauft) werden.

**Q: Brauche ich einen Plot, um ein Apartment zu mieten?**
A: Nein, Apartments sind unabhängig.

**Q: Wird die Kaution immer zurückerstattet?**
A: Ja, die volle Kaution wird bei Auszug zurückerstattet (außer bei Schäden, falls implementiert).

### Economy

**Q: Wie überweise ich Geld?**
A: Verwende `/pay <spieler> <betrag>`.

**Q: Was passiert, wenn ich kein Geld habe?**
A: Du kannst keine Käufe tätigen, aber dein Konto kann nicht negativ werden.

**Q: Gibt es Zinsen?**
A: Nein, Konten generieren keine Zinsen (standardmäßig).

### NPCs

**Q: Wie spawne ich einen NPC?**
A: Verwende `/npc spawn <type> <name>` (Admin-Befehl).

**Q: Warum greift mich ein NPC an?**
A: Wahrscheinlich hast du versucht, ihn zu bestehlen und wurdest erwischt.

**Q: Kann ich NPCs töten?**
A: Ja, aber sie können respawnen (abhängig von Konfiguration).

### Polizei

**Q: Wie werde ich mein Wanted-Level los?**
A: Zahle Kaution im Krankenhaus (`/hospital bail <betrag>`) oder warte (1 Stern pro Tag).

**Q: Kann ich vor der Polizei fliehen?**
A: Ja, verstecke dich in Gebäuden für 60 Sekunden.

**Q: Was passiert bei Verhaftung?**
A: Du wirst ins Krankenhaus teleportiert und musst Kaution zahlen.

### Tabak

**Q: Welche Sorte ist am wertvollsten?**
A: Havana (Premium-Qualität) ist am teuersten.

**Q: Wie lange dauert der Anbau?**
A: Von Pflanzung bis Ernte ca. 15-30 Minuten (abhängig von Konfiguration).

**Q: Muss ich Tabak verkaufen?**
A: Nein, du kannst ihn auch lagern oder verschenken.

### Diebstahl

**Q: Kann ich jeden NPC bestehlen?**
A: Nur Bewohner (RESIDENT), nicht Händler oder Polizei.

**Q: Was passiert, wenn ich erwischt werde?**
A: NPC greift dich an, +2 Sterne Wanted-Level, kein Loot.

**Q: Gibt es einen Cooldown?**
A: Ja, du kannst denselben NPC nur alle X Minuten bestehlen (konfigurierbar).

### Technisch

**Q: Wo werden meine Daten gespeichert?**
A: In `config/plotmod_*.json` Dateien.

**Q: Kann ich meine Daten exportieren?**
A: Ja, kopiere einfach die JSON-Dateien.

**Q: Was passiert bei Server-Crash?**
A: Daten werden alle 5 Minuten auto-gespeichert. Maximaler Verlust: 5 Minuten.

**Q: Kann ich mit anderen Mods interagieren?**
A: Ja, über die PlotModAPI (siehe Entwickler-Dokumentation).

---

## Weitere Hilfe

**Dokumentation**:
- [Entwickler-Dokumentation](ENTWICKLER_DOKUMENTATION.md)
- [Konfiguration](KONFIGURATION.md)
- [API-Dokumentation](API_DOKUMENTATION.md)

**Support**:
- GitHub Issues: [Report Bug](https://github.com/YourUsername/ScheduleMC/issues)
- Discord: [Join Server](https://discord.gg/YourServer)

---

<div align="center">

**Viel Spaß mit ScheduleMC!**

[⬆ Nach oben](#schedulemc---benutzer-anleitung)

</div>
