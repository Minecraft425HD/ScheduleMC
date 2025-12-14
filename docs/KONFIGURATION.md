# ScheduleMC - Konfiguration

## Inhaltsverzeichnis

1. [Übersicht](#übersicht)
2. [Konfigurations-Dateien](#konfigurations-dateien)
3. [Economy-Einstellungen](#economy-einstellungen)
4. [Plot-Einstellungen](#plot-einstellungen)
5. [Rent-System](#rent-system)
6. [Daily-Rewards](#daily-rewards)
7. [Shop-System](#shop-system)
8. [Rating-System](#rating-system)
9. [Police-System](#police-system)
10. [Stealing-Minigame](#stealing-minigame)
11. [NPC-Einstellungen](#npc-einstellungen)
12. [Tobacco-Konfiguration](#tobacco-konfiguration)
13. [Erweiterte Optionen](#erweiterte-optionen)

---

## Übersicht

ScheduleMC verwendet **Forge's Configuration System** für alle Einstellungen. Die Konfiguration ist vollständig anpassbar und ermöglicht es Servern, das Gameplay nach ihren Wünschen zu gestalten.

### Konfigurations-Speicherort

```
config/
├── schedulemc-common.toml     # Haupt-Konfiguration
└── schedulemc-tobacco.toml    # Tabak-spezifische Config
```

### Konfigurations-Kategorien

- **Common Config**: Gameplay-Einstellungen (beide Seiten)
- **Server Config**: Server-only Einstellungen
- **Client Config**: Client-only Einstellungen

---

## Konfigurations-Dateien

### schedulemc-common.toml

Wird automatisch beim ersten Start generiert.

**Standard-Struktur**:
```toml
[economy]
    startBalance = 1000.0
    saveIntervalMinutes = 5

[plot]
    minPlotSize = 64
    maxPlotSize = 1000000
    # ... weitere Optionen

[police]
    arrestCooldownSeconds = 5
    detectionRadius = 32
    # ... weitere Optionen
```

### Manuelle Bearbeitung

1. **Server stoppen**
2. Öffne `config/schedulemc-common.toml`
3. Ändere Werte
4. Speichere Datei
5. **Server starten**

**Wichtig**: Fehlerhafte Werte werden auf Standard zurückgesetzt.

---

## Economy-Einstellungen

### Konfigurations-Optionen

| Option | Typ | Standard | Beschreibung |
|--------|-----|----------|--------------|
| `startBalance` | Double | 1000.0 | Startguthaben für neue Spieler |
| `saveIntervalMinutes` | Integer | 5 | Auto-Save Intervall in Minuten |

### Beispiel-Konfiguration

```toml
[economy]
    # Startguthaben für neue Spieler
    # Min: 0.0, Max: 1000000.0
    startBalance = 1000.0

    # Auto-Save Intervall (Minuten)
    # Min: 1, Max: 60
    saveIntervalMinutes = 5
```

### Verwendung

**Datei**: `de.rolandsw.schedulemc.config.ModConfigHandler`

```java
public class ModConfigHandler {
    public static ForgeConfigSpec.DoubleValue START_BALANCE;
    public static ForgeConfigSpec.IntValue SAVE_INTERVAL_MINUTES;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Economy Settings").push("economy");

        START_BALANCE = builder
            .comment("Starting balance for new players")
            .defineInRange("startBalance", 1000.0, 0.0, 1000000.0);

        SAVE_INTERVAL_MINUTES = builder
            .comment("Auto-save interval in minutes")
            .defineInRange("saveIntervalMinutes", 5, 1, 60);

        builder.pop();
    }
}
```

---

## Plot-Einstellungen

### Konfigurations-Optionen

| Option | Typ | Standard | Beschreibung |
|--------|-----|----------|--------------|
| `minPlotSize` | Integer | 64 | Minimale Plot-Größe in Blöcken |
| `maxPlotSize` | Integer | 1000000 | Maximale Plot-Größe in Blöcken |
| `minPlotPrice` | Double | 1.0 | Minimaler Plot-Preis |
| `maxPlotPrice` | Double | 1000000.0 | Maximaler Plot-Preis |
| `maxTrustedPlayers` | Integer | 10 | Max. vertraute Spieler pro Plot |
| `allowPlotTransfer` | Boolean | true | Erlaube Plot-Übertragungen |
| `refundOnAbandon` | Double | 0.5 | Rückerstattung bei Aufgabe (50%) |

### Beispiel-Konfiguration

```toml
[plot]
    # Minimale Plot-Größe (Blöcke)
    minPlotSize = 64

    # Maximale Plot-Größe (Blöcke)
    maxPlotSize = 1000000

    # Minimaler Plot-Preis (€)
    minPlotPrice = 1.0

    # Maximaler Plot-Preis (€)
    maxPlotPrice = 1000000.0

    # Maximale Anzahl vertrauter Spieler
    maxTrustedPlayers = 10

    # Erlaube Plot-Übertragungen
    allowPlotTransfer = true

    # Rückerstattung bei Plot-Aufgabe (0.0-1.0)
    # 0.5 = 50% Rückerstattung
    refundOnAbandon = 0.5
```

### Anpassungs-Beispiele

#### Kleinere Plots erlauben

```toml
[plot]
    minPlotSize = 16  # Kleinere Plots (4x4 Blöcke)
```

#### Mehr vertraute Spieler

```toml
[plot]
    maxTrustedPlayers = 20  # Doppelt so viele
```

#### Keine Rückerstattung

```toml
[plot]
    refundOnAbandon = 0.0  # Keine Rückerstattung
```

---

## Rent-System

### Konfigurations-Optionen

| Option | Typ | Standard | Beschreibung |
|--------|-----|----------|--------------|
| `rentEnabled` | Boolean | true | Aktiviere Mietsystem |
| `minRentPrice` | Double | 10.0 | Mindestpreis pro Tag |
| `minRentDays` | Integer | 1 | Mindestmietdauer (Tage) |
| `maxRentDays` | Integer | 30 | Maximalmietdauer (Tage) |
| `autoEvictExpired` | Boolean | true | Auto-Rauswurf nach Ablauf |

### Beispiel-Konfiguration

```toml
[rent]
    # Aktiviere Mietsystem
    rentEnabled = true

    # Mindestpreis pro Tag (€)
    minRentPrice = 10.0

    # Mindestmietdauer (Tage)
    minRentDays = 1

    # Maximalmietdauer (Tage)
    maxRentDays = 30

    # Automatischer Rauswurf nach Ablauf
    autoEvictExpired = true
```

### Anpassungs-Beispiele

#### Längere Mietdauer erlauben

```toml
[rent]
    maxRentDays = 90  # 3 Monate
```

#### Höhere Mindestmiete

```toml
[rent]
    minRentPrice = 50.0  # 50€/Tag
```

#### Mietsystem deaktivieren

```toml
[rent]
    rentEnabled = false
```

---

## Daily-Rewards

### Konfigurations-Optionen

| Option | Typ | Standard | Beschreibung |
|--------|-----|----------|--------------|
| `dailyReward` | Double | 50.0 | Basis-Belohnung pro Tag |
| `dailyRewardStreakBonus` | Double | 10.0 | Bonus pro Streak-Tag |
| `maxStreakDays` | Integer | 30 | Max. Streak-Dauer |

### Beispiel-Konfiguration

```toml
[dailyRewards]
    # Basis-Belohnung (€)
    dailyReward = 50.0

    # Streak-Bonus pro Tag (€)
    dailyRewardStreakBonus = 10.0

    # Maximale Streak-Dauer (Tage)
    maxStreakDays = 30
```

### Belohnungs-Berechnung

```
Belohnung = dailyReward + (Streak × dailyRewardStreakBonus)
```

**Beispiel** (Standard-Werte):
- Tag 1: 50€
- Tag 2: 60€ (50 + 1×10)
- Tag 3: 70€ (50 + 2×10)
- ...
- Tag 30: 350€ (50 + 29×10)

### Anpassungs-Beispiele

#### Höhere Belohnungen

```toml
[dailyRewards]
    dailyReward = 100.0
    dailyRewardStreakBonus = 25.0
    # Tag 1: 100€
    # Tag 30: 825€
```

#### Längere Streaks

```toml
[dailyRewards]
    maxStreakDays = 60  # 2 Monate
```

#### Keine Streak-Boni

```toml
[dailyRewards]
    dailyRewardStreakBonus = 0.0
    # Immer 50€, unabhängig vom Streak
```

---

## Shop-System

### Konfigurations-Optionen

| Option | Typ | Standard | Beschreibung |
|--------|-----|----------|--------------|
| `shopEnabled` | Boolean | true | Aktiviere Shop-System |
| `buyMultiplier` | Double | 1.5 | Kauf-Preismultiplikator |
| `sellMultiplier` | Double | 0.5 | Verkaufs-Preismultiplikator |

### Beispiel-Konfiguration

```toml
[shop]
    # Aktiviere Shop-System
    shopEnabled = true

    # Kauf-Preismultiplikator
    # Kaufpreis = Basispreis × buyMultiplier
    buyMultiplier = 1.5

    # Verkaufs-Preismultiplikator
    # Verkaufspreis = Basispreis × sellMultiplier
    sellMultiplier = 0.5
```

### Preis-Berechnung

```
Kaufpreis = Basispreis × buyMultiplier
Verkaufspreis = Basispreis × sellMultiplier
```

**Beispiel** (Dirt, Basispreis: 1€):
- Kaufpreis: 1€ × 1.5 = 1.50€
- Verkaufspreis: 1€ × 0.5 = 0.50€

### Anpassungs-Beispiele

#### Teurere Preise

```toml
[shop]
    buyMultiplier = 2.0  # Kaufpreis 2x Basispreis
    sellMultiplier = 0.3  # Verkaufspreis 30% Basispreis
```

#### Günstigere Preise

```toml
[shop]
    buyMultiplier = 1.2
    sellMultiplier = 0.8
```

#### Shop deaktivieren

```toml
[shop]
    shopEnabled = false
```

---

## Rating-System

### Konfigurations-Optionen

| Option | Typ | Standard | Beschreibung |
|--------|-----|----------|--------------|
| `ratingsEnabled` | Boolean | true | Aktiviere Bewertungssystem |
| `allowMultipleRatings` | Boolean | false | Erlaube mehrfache Bewertungen |
| `minRating` | Integer | 1 | Minimale Bewertung (Sterne) |
| `maxRating` | Integer | 5 | Maximale Bewertung (Sterne) |

### Beispiel-Konfiguration

```toml
[ratings]
    # Aktiviere Bewertungssystem
    ratingsEnabled = true

    # Erlaube mehrfache Bewertungen vom selben Spieler
    allowMultipleRatings = false

    # Minimale Bewertung
    minRating = 1

    # Maximale Bewertung
    maxRating = 5
```

### Anpassungs-Beispiele

#### Mehrfache Bewertungen erlauben

```toml
[ratings]
    allowMultipleRatings = true
```

#### Bewertungssystem deaktivieren

```toml
[ratings]
    ratingsEnabled = false
```

---

## Police-System

### Konfigurations-Optionen

| Option | Typ | Standard | Beschreibung |
|--------|-----|----------|--------------|
| `policeArrestCooldownSeconds` | Integer | 5 | Cooldown nach Verhaftung (Sekunden) |
| `policeDetectionRadius` | Double | 32.0 | Erkennungsradius (Blöcke) |
| `policeArrestDistance` | Double | 2.0 | Verhaftungsdistanz (Blöcke) |
| `policeSearchDurationSeconds` | Integer | 60 | Such-Dauer nach Verlust (Sekunden) |
| `policeSearchRadius` | Double | 50.0 | Such-Radius (Blöcke) |
| `policeIndoorHidingEnabled` | Boolean | true | Indoor-Verstecken aktiviert |
| `policeBlockDoorsEnabled` | Boolean | true | Tür-Blockierung aktiviert |
| `policeRaidScanRadius` | Double | 20.0 | Raid-Scan-Radius (Blöcke) |
| `policeIllegalCashThreshold` | Double | 10000.0 | Illegales Cash-Limit (€) |
| `policeRaidAccountPercentage` | Double | 0.1 | Raid-Strafe (10% des Kontostands) |
| `policeRaidMinFine` | Double | 1000.0 | Mindest-Raid-Strafe (€) |

### Beispiel-Konfiguration

```toml
[police]
    # Arrest Cooldown (Sekunden)
    policeArrestCooldownSeconds = 5

    # Erkennungsradius (Blöcke)
    policeDetectionRadius = 32.0

    # Verhaftungsdistanz (Blöcke)
    policeArrestDistance = 2.0

    # Such-Dauer nach Verlust (Sekunden)
    policeSearchDurationSeconds = 60

    # Such-Radius (Blöcke)
    policeSearchRadius = 50.0

    # Indoor-Verstecken aktiviert
    policeIndoorHidingEnabled = true

    # Tür-Blockierung während Verfolgung
    policeBlockDoorsEnabled = true

    # Raid-Scan-Radius (Blöcke)
    policeRaidScanRadius = 20.0

    # Illegales Cash-Limit (€)
    policeIllegalCashThreshold = 10000.0

    # Raid-Strafe (Prozent des Kontostands)
    policeRaidAccountPercentage = 0.1

    # Mindest-Raid-Strafe (€)
    policeRaidMinFine = 1000.0
```

### Raid-Strafen-Berechnung

```
Strafe = max(
    Kontostand × policeRaidAccountPercentage,
    policeRaidMinFine
)
```

**Beispiel** (Standard-Werte):
- Kontostand: 50.000€
- Strafe: max(50.000€ × 0.1, 1.000€) = **5.000€**

**Beispiel** (niedriger Kontostand):
- Kontostand: 500€
- Strafe: max(500€ × 0.1, 1.000€) = **1.000€** (Mindeststrafe)

### Anpassungs-Beispiele

#### Aggressivere Polizei

```toml
[police]
    policeDetectionRadius = 64.0  # Doppelter Radius
    policeSearchDurationSeconds = 120  # Längere Suche
```

#### Höhere Raid-Strafen

```toml
[police]
    policeIllegalCashThreshold = 5000.0  # Niedrigeres Limit
    policeRaidAccountPercentage = 0.2  # 20% Strafe
    policeRaidMinFine = 5000.0  # Höhere Mindeststrafe
```

#### Indoor-Verstecken deaktivieren

```toml
[police]
    policeIndoorHidingEnabled = false
```

---

## Stealing-Minigame

### Konfigurations-Optionen

| Option | Typ | Standard | Beschreibung |
|--------|-----|----------|--------------|
| `stealingIndicatorSpeed` | Double | 0.04 | Indikator-Geschwindigkeit |
| `stealingMaxAttempts` | Integer | 3 | Maximale Versuche |
| `stealingMinZoneSize` | Double | 0.05 | Minimale Erfolgszone (5%) |
| `stealingMaxZoneSize` | Double | 0.15 | Maximale Erfolgszone (15%) |

### Beispiel-Konfiguration

```toml
[stealing]
    # Indikator-Geschwindigkeit
    # Höher = schneller (schwieriger)
    stealingIndicatorSpeed = 0.04

    # Maximale Versuche
    stealingMaxAttempts = 3

    # Minimale Erfolgszone (schwer)
    # 0.05 = 5% der Leiste
    stealingMinZoneSize = 0.05

    # Maximale Erfolgszone (einfach)
    # 0.15 = 15% der Leiste
    stealingMaxZoneSize = 0.15
```

### Schwierigkeits-Berechnung

Die tatsächliche Zonengröße wird zufällig zwischen min und max gewählt:

```
ZoneSize = random(stealingMinZoneSize, stealingMaxZoneSize)
```

### Anpassungs-Beispiele

#### Schwieriger

```toml
[stealing]
    stealingIndicatorSpeed = 0.08  # Doppelt so schnell
    stealingMaxAttempts = 1  # Nur 1 Versuch
    stealingMinZoneSize = 0.02  # Kleinere Zone
    stealingMaxZoneSize = 0.05
```

#### Einfacher

```toml
[stealing]
    stealingIndicatorSpeed = 0.02  # Langsamer
    stealingMaxAttempts = 5  # Mehr Versuche
    stealingMinZoneSize = 0.1
    stealingMaxZoneSize = 0.3  # Größere Zone
```

---

## NPC-Einstellungen

### Konfigurations-Optionen

| Option | Typ | Standard | Beschreibung |
|--------|-----|----------|--------------|
| `npcWalkableBlocks` | List<String> | [...] | Liste erlaubter Block-Typen |

### Beispiel-Konfiguration

```toml
[npc]
    # Liste der Blöcke, auf denen NPCs laufen können
    npcWalkableBlocks = [
        "minecraft:stone",
        "minecraft:grass_block",
        "minecraft:dirt",
        "minecraft:cobblestone",
        "minecraft:oak_planks",
        "minecraft:stone_bricks",
        "minecraft:stone_stairs",
        "minecraft:oak_stairs",
        "minecraft:stone_slab",
        "minecraft:oak_slab"
    ]
```

### Block hinzufügen

```toml
[npc]
    npcWalkableBlocks = [
        # ... Standard-Blöcke ...
        "minecraft:diamond_block",
        "minecraft:emerald_block",
        "your_mod:custom_block"
    ]
```

**Wichtig**: Verwende vollständige Resource Locations (`modid:blockname`)

---

## Tobacco-Konfiguration

### Separate Konfigurations-Datei

**Datei**: `config/schedulemc-tobacco.toml`

### Konfigurations-Optionen

| Option | Typ | Standard | Beschreibung |
|--------|-----|----------|--------------|
| `growthRatePerStage` | Integer | 1200 | Ticks pro Wachstumsstufe |
| `lightBonus` | Double | 1.5 | Wachstums-Bonus bei Licht |
| `waterConsumptionRate` | Integer | 1 | Wasserverbrauch pro Tick |
| `minWaterLevel` | Integer | 20 | Mindest-Wasserlevel für Wachstum |
| `dryingDuration` | Integer | 3600 | Trocknungsdauer (Ticks) |
| `fermentationDuration` | Integer | 7200 | Fermentationsdauer (Ticks) |
| `qualityBonus` | Double | 1.2 | Qualitäts-Bonus |

### Beispiel-Konfiguration

```toml
[tobacco]
    # Wachstumsrate (Ticks pro Stufe)
    # 1200 Ticks ≈ 1 Minute
    growthRatePerStage = 1200

    # Licht-Wachstums-Bonus
    lightBonus = 1.5

    # Wasserverbrauch (pro Tick)
    waterConsumptionRate = 1

    # Mindest-Wasserlevel für Wachstum (%)
    minWaterLevel = 20

    # Trocknungsdauer (Ticks)
    # 3600 Ticks = 3 Minuten
    dryingDuration = 3600

    # Fermentationsdauer (Ticks)
    # 7200 Ticks = 6 Minuten
    fermentationDuration = 7200

    # Qualitäts-Bonus (Multiplikator)
    qualityBonus = 1.2
```

### Tick-zu-Zeit-Umrechnung

```
20 Ticks = 1 Sekunde
1200 Ticks = 60 Sekunden = 1 Minute
3600 Ticks = 3 Minuten
7200 Ticks = 6 Minuten
```

### Anpassungs-Beispiele

#### Schnelleres Wachstum

```toml
[tobacco]
    growthRatePerStage = 600  # 30 Sekunden pro Stufe
    dryingDuration = 1200  # 1 Minute
    fermentationDuration = 2400  # 2 Minuten
```

#### Realistischeres Wachstum

```toml
[tobacco]
    growthRatePerStage = 24000  # 20 Minuten pro Stufe
    dryingDuration = 72000  # 1 Stunde
    fermentationDuration = 144000  # 2 Stunden
```

#### Höherer Wasserverbrauch

```toml
[tobacco]
    waterConsumptionRate = 5  # 5x schneller
    minWaterLevel = 50  # Höheres Minimum
```

---

## Erweiterte Optionen

### In-Game Config-Reload

Einige Werte können zur Laufzeit neu geladen werden:

```
/reload
```

**Wichtig**: Nicht alle Werte werden live aktualisiert. Server-Neustart empfohlen.

### Config-Backup

Erstelle regelmäßig Backups der Config-Dateien:

```bash
cp config/schedulemc-common.toml config/schedulemc-common.toml.backup
```

### Config zurücksetzen

Lösche die Config-Datei, sie wird beim nächsten Start neu generiert:

```bash
rm config/schedulemc-common.toml
```

### Mehrere Konfigurationen

Für verschiedene Server-Modi (z.B. Survival vs Creative):

1. Erstelle Config-Profile:
   ```
   config/
   ├── schedulemc-common-survival.toml
   ├── schedulemc-common-creative.toml
   └── schedulemc-common.toml (aktuelle)
   ```

2. Wechsle zwischen Profilen:
   ```bash
   cp config/schedulemc-common-survival.toml config/schedulemc-common.toml
   ```

---

## Konfigurations-Vorlagen

### Hardcore-Modus

```toml
[economy]
    startBalance = 100.0  # Weniger Startgeld

[dailyRewards]
    dailyReward = 10.0  # Niedrigere Belohnungen
    dailyRewardStreakBonus = 2.0

[plot]
    refundOnAbandon = 0.0  # Keine Rückerstattung
    minPlotPrice = 100.0  # Höhere Preise

[police]
    policeDetectionRadius = 64.0  # Aggressivere Polizei
    policeRaidAccountPercentage = 0.25  # 25% Strafe
    policeIllegalCashThreshold = 5000.0

[stealing]
    stealingMaxAttempts = 1  # Nur 1 Versuch
    stealingIndicatorSpeed = 0.08  # Schneller
```

### Casual-Modus

```toml
[economy]
    startBalance = 5000.0  # Viel Startgeld

[dailyRewards]
    dailyReward = 200.0  # Hohe Belohnungen
    dailyRewardStreakBonus = 50.0

[plot]
    refundOnAbandon = 1.0  # Volle Rückerstattung
    minPlotPrice = 1.0

[police]
    policeDetectionRadius = 16.0  # Weniger aggressiv
    policeRaidAccountPercentage = 0.05  # 5% Strafe
    policeIllegalCashThreshold = 50000.0

[stealing]
    stealingMaxAttempts = 5  # Viele Versuche
    stealingIndicatorSpeed = 0.02  # Langsam
```

### RP-Server (Roleplay)

```toml
[economy]
    startBalance = 500.0

[plot]
    maxTrustedPlayers = 20  # Mehr für Fraktionen
    allowPlotTransfer = true

[rent]
    maxRentDays = 90  # Längere Mieten

[police]
    policeIndoorHidingEnabled = true
    policeBlockDoorsEnabled = true
    policeSearchDurationSeconds = 300  # 5 Minuten

[shop]
    buyMultiplier = 1.3  # Realistischere Preise
    sellMultiplier = 0.7

[tobacco]
    growthRatePerStage = 12000  # Realistisches Wachstum
    dryingDuration = 36000
    fermentationDuration = 72000
```

---

## Troubleshooting

### Problem: Config wird nicht geladen

**Lösung**:
1. Überprüfe Syntax (TOML-Format)
2. Schaue in `logs/latest.log` nach Fehlern
3. Lösche Config und lasse sie neu generieren

### Problem: Werte werden auf Standard zurückgesetzt

**Lösung**:
- Wert liegt außerhalb des erlaubten Bereichs
- Überprüfe Min/Max-Werte in Kommentaren

### Problem: Änderungen werden nicht übernommen

**Lösung**:
- Server neu starten
- Nicht `/reload` alleine verwenden

---

## Weitere Ressourcen

- [Entwickler-Dokumentation](ENTWICKLER_DOKUMENTATION.md) - Für eigene Config-Optionen
- [Benutzer-Anleitung](BENUTZER_ANLEITUNG.md) - Gameplay-Features
- [API-Dokumentation](API_DOKUMENTATION.md) - Programmierung

---

<div align="center">

**Viel Erfolg beim Konfigurieren!**

[⬆ Nach oben](#schedulemc---konfiguration)

</div>
