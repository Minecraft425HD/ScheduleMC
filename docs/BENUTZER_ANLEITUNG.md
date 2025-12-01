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
10. [Shop-Investment System](#shop-investment-system)
11. [Warehouse-System](#warehouse-system)
12. [Messaging-System](#messaging-system)
13. [Smartphone-System](#smartphone-system)
14. [Tägliche Belohnungen](#tägliche-belohnungen)
15. [Admin-Befehle](#admin-befehle)
16. [FAQ](#faq)

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

## Shop-Investment System

### Übersicht

Das Shop-Investment System erlaubt es Spielern, Anteile an NPC-Shops zu kaufen und dadurch an den Einnahmen beteiligt zu werden.

### Wie funktioniert es?

#### Shares (Anteile)

- Jeder Shop hat **100 Anteile** insgesamt
- Maximal **2 Aktionäre** pro Shop
- **1000€ pro Anteil**
- Automatische Ausschüttung alle 7 Tage

#### Rendite-Berechnung

```
Deine Ausschüttung = (Deine Anteile / 100) × Netto-Umsatz (7 Tage)
```

**Beispiel**:
- Du besitzt: 40 Anteile (40%)
- Shop-Umsatz (7 Tage): 10.000€
- Shop-Ausgaben (7 Tage): 3.000€
- Netto-Umsatz: 7.000€
- **Deine Ausschüttung: 2.800€** (40% von 7.000€)

### Befehle

#### Shops auflisten

```
/shopinvest list
```
Zeigt alle verfügbaren Shops mit:
- Shop-ID
- Verfügbare Anteile
- 7-Tage Netto-Umsatz
- Aktuelle Aktionäre

#### Shop-Details anzeigen

```
/shopinvest info <shopId>
```

**Beispiel**:
```
/shopinvest info baumarkt_1
```

**Ausgabe**:
```
Shop: Baumarkt (ID: baumarkt_1)
Verfügbare Anteile: 60/100
7-Tage Netto-Umsatz: 5.250€
Aktionäre:
  - Steve: 30 Anteile (30%)
  - Alex: 10 Anteile (10%)
```

#### Anteile kaufen

```
/shopinvest buy <shopId> <anzahl>
```

**Parameter**:
- `anzahl`: 1-99 Anteile

**Beispiel**:
```
/shopinvest buy baumarkt_1 25
```

**Kosten**: 25.000€ (25 Anteile × 1.000€)

**Einschränkungen**:
- Maximal 99 Anteile pro Spieler
- Maximal 2 Aktionäre pro Shop
- Ausreichend verfügbare Anteile

#### Anteile verkaufen

```
/shopinvest sell <shopId> <anzahl>
```

**Rückerstattung**: 75% des Kaufpreises (750€ pro Anteil)

**Beispiel**:
```
/shopinvest sell baumarkt_1 10
```

**Rückerstattung**: 7.500€ (10 Anteile × 750€)

#### Deine Investments anzeigen

```
/shopinvest myshares
```

**Ausgabe**:
```
Deine Shop-Investments:
  - Baumarkt: 25 Anteile (25%)
  - Lebensmittel: 40 Anteile (40%)
  - Gesamt investiert: 65.000€
  - 7-Tage Ausschüttung: 3.200€
```

### Strategien

#### Profitable Shops identifizieren

- Schaue auf **7-Tage Netto-Umsatz**
- Höherer Umsatz = höhere Rendite
- Shops in frequentierten Bereichen sind profitabler

#### Diversifikation

- Investiere in mehrere Shops
- Reduziert Risiko
- Stabilere Einnahmen

#### Maximale Beteiligung

- Kaufe viele Anteile in einem profitablen Shop
- Höhere Rendite, aber riskanter
- Beobachte Umsatz-Entwicklung

### Risiken

- **Umsatzrückgang**: Weniger Kunden = weniger Rendite
- **Ausgaben**: Hohe Warehouse-Kosten reduzieren Netto-Umsatz
- **Wiederverkauf**: Nur 75% Rückerstattung

---

## Warehouse-System

### Übersicht

Das Warehouse-System ermöglicht es, Lagerbestände für NPC-Händler zu verwalten. Händler verkaufen Items aus dem Warehouse-Inventar.

### Was ist ein Warehouse?

Ein Warehouse (Lager) ist ein Block, der:
- **Inventar** speichert (32 Slots, je 1024 Items)
- An einen **Shop-Plot** gebunden ist
- Mit **NPC-Händlern** verknüpft werden kann
- Automatische **Lieferungen** alle 3 Tage erhält

### Warehouse-Befehle

#### Warehouse-Info anzeigen

```
/warehouse info
```

**Verwendung**: Schaue auf oder stehe auf einem Warehouse-Block

**Ausgabe**:
```
Warehouse-Informationen:
Position: (100, 64, -200)
Belegte Slots: 18/32
Gesamt Items: 8.450
Verknüpfte Shop-ID: baumarkt_1
Verknüpfte Verkäufer: 2
Tage seit letzter Lieferung: 1
```

#### Items hinzufügen (Admin)

```
/warehouse add <item> <anzahl>
```

**Beispiel**:
```
/warehouse add minecraft:stone 500
/warehouse add minecraft:diamond 64
```

**Limits**:
- Max. 1-10.000 Items pro Befehl
- Max. 1.024 Items pro Slot

#### Items entfernen (Admin)

```
/warehouse remove <item> <anzahl>
```

**Beispiel**:
```
/warehouse remove minecraft:stone 200
```

#### Warehouse leeren (Admin)

```
/warehouse clear
```

**Warnung**: Löscht alle Items unwiderruflich!

#### Shop verknüpfen (Admin)

```
/warehouse setshop <shopId>
```

**Beispiel**:
```
/warehouse setshop baumarkt_1
```

Verknüpft das Warehouse mit einem Shop-Account für:
- Umsatz-Tracking
- Ausgaben-Tracking
- Investment-Auszahlungen

### Plot-Warehouse Integration

#### Warehouse zu Plot hinzufügen

```
/plot warehouse set
```

**Verwendung**: Schaue auf oder stehe auf einem Warehouse-Block

**Effekt**: Verknüpft Warehouse mit aktuellem Plot

#### Warehouse-Verknüpfung entfernen

```
/plot warehouse clear
```

Entfernt Warehouse-Verknüpfung vom Plot.

#### Warehouse-Info für Plot

```
/plot warehouse info
```

Zeigt Warehouse-Details für aktuellen Plot.

### NPC-Warehouse Integration

#### NPC mit Warehouse verbinden

```
/npc <npcName> warehouse set
```

**Verwendung**: Schaue auf oder stehe auf einem Warehouse-Block

**Effekt**: NPC verkauft Items aus diesem Warehouse

#### Warehouse-Verknüpfung entfernen

```
/npc <npcName> warehouse clear
```

#### Warehouse-Info für NPC

```
/npc <npcName> warehouse info
```

Zeigt Warehouse-Details für diesen NPC.

### Automatische Lieferungen

#### Wie funktioniert es?

- **Intervall**: Alle 3 Tage (konfigurierbar)
- **Kosten**: Vom State Account bezahlt
- **Inhalt**: Basierend auf Shop-Kategorie
- **Menge**: Füllt Lagerbestand auf

#### Beispiel-Lieferung (Baumarkt)

```
Neue Lieferung erhalten:
  - Stone: +500
  - Oak Planks: +300
  - Glass: +200
  - Iron Ingot: +100
Kosten: 2.500€ (State Account)
```

### Warehouse-Mechaniken

#### Verkauf aus Warehouse

1. Spieler kauft bei NPC
2. NPC prüft Warehouse-Inventar
3. Item wird aus Warehouse entnommen
4. Geld geht an Shop-Account
5. Umsatz wird getrackt

#### Lagerbestand-Management

- **Automatisch**: Lieferungen alle 3 Tage
- **Manuell**: Admin kann Items hinzufügen/entfernen
- **Warnung**: Bei niedrigem Bestand (< 10% Kapazität)

#### Shop-Account Integration

- Einnahmen aus Verkäufen
- Ausgaben für Lieferungen
- Netto-Umsatz für Investment-Auszahlungen

---

## Messaging-System

### Übersicht

Das Messaging-System ermöglicht Kommunikation zwischen Spielern und NPCs.

### Features

- **Player-to-Player** Nachrichten
- **Player-to-NPC** Nachrichten
- **Konversations-Historie**
- **Ungelesene Nachrichten**
- **Zeitstempel**

### Nachrichten senden

#### Via Smartphone-App

1. Öffne Smartphone (Taste **P**)
2. Wähle **Messages App** (§3Cyan)
3. Wähle Konversation oder starte neue
4. Schreibe Nachricht
5. Sende

#### Via Command (wenn implementiert)

```
/msg <spieler> <nachricht>
```

**Beispiel**:
```
/msg Steve Hallo, wie geht's?
```

### Nachrichten lesen

#### Smartphone Messages App

- Zeigt alle Konversationen
- Sortiert nach neuesten Nachrichten
- Ungelesene Nachrichten hervorgehoben
- Vollständige Historie

#### Notification Overlay

- Erscheint automatisch bei neuer Nachricht
- Zeigt Absender und Vorschau
- Verschwindet nach 5 Sekunden
- Klicken öffnet Messages App

### Konversationen

#### Konversations-Liste

Zeigt:
- **Kontakt-Name**
- **Letzte Nachricht** (Vorschau)
- **Zeitstempel**
- **Ungelesene Anzahl**

#### Konversations-Ansicht

Zeigt:
- Vollständige Nachrichtenhistorie
- Timestamps für jede Nachricht
- Eingabefeld für neue Nachricht
- Sende-Button

### NPC-Nachrichten

#### NPC-Templates

NPCs senden automatische Nachrichten bei:
- Shop-Angeboten
- Event-Einladungen
- Quests (wenn implementiert)
- Persönlichkeits-basierte Grüße

**Beispiel**:
```
Von: Händler Karl
"Hallo! Neue Ware eingetroffen! Komm vorbei!"
```

#### Antworten auf NPCs

Du kannst auf NPC-Nachrichten antworten. NPCs können:
- Mit vordefinierten Templates antworten
- Basierend auf Persönlichkeit reagieren
- Quests oder Aufgaben geben

---

## Smartphone-System

### Übersicht

Das Smartphone ist ein ingame Werkzeug mit mehreren Apps für verschiedene Features.

### Smartphone öffnen

**Tastenbelegung**: Taste **P** (Standard)

**Ändern**: Minecraft Optionen → Steuerung → ScheduleMC → "Open Smartphone"

### Apps

#### 1. MAP APP (§9Blau)

**Features**:
- Karten-Ansicht
- Aktuelle Position
- Plot-Markierungen
- Waypoints (geplant)

**Verwendung**:
- Klicke auf Map-Icon
- Zoome mit Scroll-Rad
- Klicke für Details

#### 2. DEALER APP (§cRot)

**Features**:
- Finde Tabak-Händler
- NPC-Standorte
- Preisvergleiche
- Beste Angebote

**Verwendung**:
- Liste aller Händler
- Filtern nach Entfernung
- Navigation zu Händler

#### 3. PRODUCTS APP (§aGrün)

**Features**:
- Shop-Katalog
- Item-Browsing
- Preislisten
- Verfügbarkeit

**Verwendung**:
- Durchsuche alle Shop-Items
- Vergleiche Preise
- Finde billigste Angebote

#### 4. ORDER APP (§eGelb)

**Features**:
- Bestellverwaltung (geplant)
- Lieferungstracking (geplant)
- Kaufhistorie

**Verwendung**:
- Zeige offene Bestellungen
- Tracke Lieferungen
- Historie ansehen

#### 5. CONTACTS APP (§5Lila)

**Features**:
- Spieler-Kontakte
- NPC-Kontakte
- Schnellwahl für Messages
- Kontakt-Details

**Verwendung**:
- Liste aller Kontakte
- Klicke für Nachricht
- Hinzufügen/Entfernen

#### 6. MESSAGES APP (§3Cyan)

**Features**:
- Inbox
- Konversations-Management
- Nachrichtenhistorie
- Ungelesene Nachrichten

**Verwendung**:
- Siehe [Messaging-System](#messaging-system)

### Smartphone-Schutz

#### Was ist Smartphone-Schutz?

Wenn du dein Smartphone offen hast, bist du:
- **Immun gegen Schaden**
- Andere Spieler können dich nicht angreifen
- NPCs können dich nicht angreifen
- Fallschaden ist deaktiviert

#### Angreifer-Strafe

Wenn jemand versucht, dich anzugreifen während Smartphone offen:
- Angreifer erhält **+1 Wanted-Level** ⭐
- Du erhältst Benachrichtigung
- Angreifer erhält Warnung

**Nachricht**:
```
§c[NAME] hat versucht dich anzugreifen während du am Smartphone warst!
§c[NAME] erhält +1 Wanted-Level!
```

#### Einschränkungen

- Kein Movement während Smartphone offen
- Keine Block-Interaktionen
- Keine Item-Nutzung
- Automatisch schließen bei Damage (ohne Smartphone)

### Smartphone-Einstellungen

#### Tastenbelegung ändern

1. Minecraft Optionen
2. Steuerung
3. Kategorie: **ScheduleMC**
4. **Open Smartphone**
5. Neue Taste zuweisen

**Empfohlene Tasten**: P, I, K, O

#### Deaktivieren

Wenn du Smartphone nicht nutzen willst:
- Weise keine Taste zu
- Oder verwende Command (wenn implementiert)

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
- `resident` oder `bewohner`: Bewohner
- `merchant` oder `verkaeufer`: Händler
- `police` oder `polizei`: Polizist

**Beispiel**:
```
/npc spawn merchant Händler_Karl
/npc spawn police Officer_Schmidt
/npc spawn resident Anna_Müller
```

#### NPC-Informationen anzeigen

```
/npc <npcName> info
```

Zeigt vollständige NPC-Daten:
- Typ, Bewegungsstatus, Geschwindigkeit
- Home & Work Locations
- Zeitplan (workstart, workend, hometime)
- Leisure Locations
- Inventar (für Bewohner & Händler)
- Wallet (für Bewohner & Händler)
- Warehouse-Verknüpfung

**Beispiel**:
```
/npc Händler_Karl info
```

#### Bewegung aktivieren/deaktivieren

```
/npc <npcName> movement <true|false>
```

**Beispiel**:
```
/npc Händler_Karl movement true
/npc Officer_Schmidt movement false
```

#### Bewegungsgeschwindigkeit setzen

```
/npc <npcName> speed <wert>
```

**Parameter**: `wert` = 0.1 bis 1.0

**Beispiel**:
```
/npc Händler_Karl speed 0.8
```

### NPC-Zeitplan konfigurieren

#### Arbeitszeit Beginn

```
/npc <npcName> schedule workstart <HHMM>
```

**Beispiel**:
```
/npc Händler_Karl schedule workstart 0800
```
(NPC geht um 8:00 Uhr zur Arbeit)

#### Arbeitszeit Ende

```
/npc <npcName> schedule workend <HHMM>
```

**Beispiel**:
```
/npc Händler_Karl schedule workend 1700
```
(NPC verlässt Arbeit um 17:00 Uhr)

#### Schlafenszeit

```
/npc <npcName> schedule home <HHMM>
```

**Beispiel**:
```
/npc Anna_Müller schedule home 2200
```
(NPC geht um 22:00 Uhr schlafen)

### NPC-Leisure Locations

#### Leisure Location hinzufügen

```
/npc <npcName> leisure add
```

Fügt aktuelle Position als Leisure Location hinzu (max. 10)

**Beispiel**:
```
/npc Anna_Müller leisure add
```

#### Leisure Location entfernen

```
/npc <npcName> leisure remove <index>
```

**Parameter**: `index` = 0 bis 9

**Beispiel**:
```
/npc Anna_Müller leisure remove 2
```

#### Alle Leisure Locations auflisten

```
/npc <npcName> leisure list
```

**Ausgabe**:
```
Leisure Locations für Anna_Müller:
0: (100, 64, 200)
1: (150, 65, 180)
2: (120, 64, 220)
```

#### Alle Leisure Locations löschen

```
/npc <npcName> leisure clear
```

Entfernt alle Leisure Locations von diesem NPC.

### NPC-Inventar (Bewohner & Händler)

#### Inventar anzeigen

```
/npc <npcName> inventory
```

Zeigt alle 9 Slots des NPC-Inventars.

**Ausgabe**:
```
Inventar von Händler_Karl:
Slot 0: Stone x64
Slot 1: Oak Planks x32
Slot 2: (leer)
...
```

#### Item geben

```
/npc <npcName> inventory give <slot> <item>
```

**Parameter**:
- `slot`: 0-8
- `item`: Minecraft Item-ID

**Beispiel**:
```
/npc Händler_Karl inventory give 0 minecraft:diamond
/npc Anna_Müller inventory give 5 minecraft:bread 16
```

#### Inventar leeren

```
/npc <npcName> inventory clear [slot]
```

**Ohne Slot**: Leert gesamtes Inventar

**Mit Slot**: Leert nur diesen Slot

**Beispiel**:
```
/npc Händler_Karl inventory clear
/npc Anna_Müller inventory clear 3
```

### NPC-Wallet (Bewohner & Händler)

#### Wallet anzeigen

```
/npc <npcName> wallet
```

Zeigt aktuellen Cash-Bestand des NPCs.

**Ausgabe**:
```
Händler_Karl Wallet: 450.00€
```

#### Wallet-Betrag setzen

```
/npc <npcName> wallet set <betrag>
```

**Beispiel**:
```
/npc Händler_Karl wallet set 1000
```

#### Geld hinzufügen

```
/npc <npcName> wallet add <betrag>
```

**Beispiel**:
```
/npc Anna_Müller wallet add 500
```

#### Geld entfernen

```
/npc <npcName> wallet remove <betrag>
```

**Beispiel**:
```
/npc Händler_Karl wallet remove 200
```

### NPC-Warehouse Verknüpfung

#### Warehouse setzen

```
/npc <npcName> warehouse set
```

**Verwendung**: Schaue auf oder stehe auf Warehouse-Block

#### Warehouse entfernen

```
/npc <npcName> warehouse clear
```

#### Warehouse-Info

```
/npc <npcName> warehouse info
```

### NPC-Shop konfigurieren

```
/npc setshop <uuid> <kategorie>
```

**Kategorien**:
- `baumarkt` - Hardware Store
- `waffenhaendler` - Gun Shop
- `tankstelle` - Gas Station
- `lebensmittel` - Grocery
- `personalmanagement` - HR
- `illegaler_haendler` - Black Market

**Beispiel**:
```
/npc setshop 123e4567-e89b-12d3-a456-426614174000 baumarkt
```

### NPC entfernen

```
/npc remove <uuid>
```

Entfernt NPC permanent.

**UUID finden**: F3+H aktivieren, auf NPC schauen

### Plot-Admin

Die meisten Plot-Befehle sind bereits für normale Spieler verfügbar. Admins haben jedoch zusätzliche Rechte:

- Können **jeden Plot** bearbeiten (nicht nur eigene)
- Können Plots **löschen** ohne Rückerstattung
- Können Spieler aus **jedem Plot** entfernen

#### Plot-Typ ändern

```
/plot settype <type>
```

**Typen**:
- `RESIDENTIAL` - Wohngebiet (kaufbar)
- `COMMERCIAL` - Gewerbefläche (kaufbar)
- `SHOP` - Laden (Regierung, nicht kaufbar)
- `PUBLIC` - Öffentlich (Regierung, nicht kaufbar)
- `GOVERNMENT` - Regierung (nicht kaufbar)

**Verwendung**: Stehe auf Plot

**Beispiel**:
```
/plot settype COMMERCIAL
/plot settype SHOP
```

#### Plot-Besitzer setzen

```
/plot setowner <player>
```

Setzt Besitzer des Plots, auf dem du stehst.

**Beispiel**:
```
/plot setowner Steve
```

#### Plot entfernen

```
/plot remove
```

Entfernt Plot permanent (keine Rückerstattung).

**Warnung**: Unwiderruflich!

#### Spatial Index neu aufbauen

```
/plot reindex
```

Baut Spatial Index für alle Plots neu auf.

**Verwendung**: Bei Performance-Problemen oder nach Datenbank-Korruption.

#### Plot-Debug-Info

```
/plot debug
```

Zeigt Debug-Informationen für aktuelle Position:
- Chunk-Koordinaten
- Spatial Index Buckets
- Plot-Lookups
- Performance-Metriken

### State-Account Admin

#### Kontostand anzeigen

```
/state balance
```

Zeigt State-Account Balance.

**Ausgabe**:
```
State Account: 125.450€
```

#### Geld einzahlen

```
/state deposit <betrag>
```

Zahlt Geld vom eigenen Konto in State Account ein.

**Beispiel**:
```
/state deposit 10000
```

#### Geld abheben

```
/state withdraw <betrag>
```

Hebt Geld aus State Account ab (zu eigenem Konto).

**Beispiel**:
```
/state withdraw 5000
```

**Hinweis**: State Account zahlt für Warehouse-Lieferungen.

### Hospital-Admin

#### Hospital-Spawn setzen

```
/hospital setspawn
```

Setzt Hospital Respawn-Point auf aktuelle Position.

**Verwendung**: Spieler spawnen hier nach Verhaftung.

#### Kaution-Gebühr setzen

```
/hospital setfee <betrag>
```

Setzt Basis-Kautionsgebühr.

**Standard**: 100€ pro Wanted-Star

**Beispiel**:
```
/hospital setfee 200
```
(200€ pro Stern)

#### Hospital-Info

```
/hospital info
```

Zeigt:
- Hospital Spawn Location
- Kautions-Gebühr
- Anzahl Verhaftungen (gesamt)

### Warehouse-Admin

Alle Warehouse-Befehle erfordern Admin-Rechte. Siehe [Warehouse-System](#warehouse-system) für Details:

- `/warehouse info` - Info anzeigen
- `/warehouse add <item> <anzahl>` - Items hinzufügen
- `/warehouse remove <item> <anzahl>` - Items entfernen
- `/warehouse clear` - Komplett leeren
- `/warehouse setshop <shopId>` - Shop verknüpfen

### Tabak-Admin

#### Items geben

```
/tobacco give <item>
```

**Items**:
- Seeds: `virginia_seeds`, `burley_seeds`, `oriental_seeds`, `havana_seeds`
- Boosters: `fertilizer`, `growth_booster`, `quality_booster`
- Tools: `watering_can`

**Beispiel**:
```
/tobacco give havana_seeds
/tobacco give growth_booster
```

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
