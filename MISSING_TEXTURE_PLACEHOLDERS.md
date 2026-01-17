# Missing Texture Placeholders

Dieses Dokument listet alle PNG-Platzhalter auf, die für fehlende Texturen erstellt wurden.

## Übersicht

**Anzahl der Platzhalter:** 7
**Zweck:** Diese Platzhalter zeigen genau, welche Texturen fehlen, wie sie heißen und wo sie platziert werden müssen.

---

## 1. Fahrzeug-Rad-Texturen (4 Platzhalter)

### Speicherort
`src/main/resources/assets/schedulemc/textures/entity/`

### Fehlende Texturen

#### 1.1 sport_wheel.png
- **Farbe:** Magenta (255, 0, 255)
- **Größe:** 16x16 Pixel
- **Referenziert in:** `PartRegistry.java:37` → `PartSportTire`
- **3D-Modell:** `models/entity/wheel.obj`
- **Beschreibung:** Textur für Sport-Reifen

#### 1.2 premium_wheel.png
- **Farbe:** Cyan (0, 255, 255)
- **Größe:** 16x16 Pixel
- **Referenziert in:** `PartRegistry.java:44` → `PartPremiumTire`
- **3D-Modell:** `models/entity/wheel.obj`
- **Beschreibung:** Textur für Premium-Reifen

#### 1.3 allterrain_wheel.png
- **Farbe:** Gelb (255, 255, 0)
- **Größe:** 16x16 Pixel
- **Referenziert in:** `PartRegistry.java:59` → `PartAllterrainTire`
- **3D-Modell:** `models/entity/big_wheel.obj`
- **Beschreibung:** Textur für Geländereifen

#### 1.4 heavyduty_wheel.png
- **Farbe:** Orange (255, 128, 0)
- **Größe:** 16x16 Pixel
- **Referenziert in:** `PartRegistry.java:66` → `PartHeavyDutyTire`
- **3D-Modell:** `models/entity/big_wheel.obj`
- **Beschreibung:** Textur für Heavy-Duty-Reifen

**Vorlage-Texturen zum Kopieren:**
- `big_wheel.png` (7.8K) - für allterrain und heavyduty
- `wheel.png` (1.2K) - für sport und premium

---

## 2. GUI-Texturen (1 Platzhalter)

### Speicherort
`src/main/resources/assets/schedulemc/textures/gui/`

#### 2.1 gui_garage.png
- **Farbe:** Hellblau (128, 128, 255)
- **Größe:** 256x256 Pixel
- **Referenziert in:** `GuiGarage.java` → Garagen-Management-Bildschirm
- **Beschreibung:** GUI-Textur für das Fahrzeug-Garagen-Interface
- **Hinweis:** Eine `garage.png` Block-Textur existiert bereits, aber die GUI-Version fehlt

**Vorlage-Textur zum Kopieren:**
- `gui_vehicle.png` (2.2K) - ähnliches GUI-Layout

---

## 3. Fahrzeugteile-Texturen (2 Platzhalter)

### Speicherort
`src/main/resources/assets/schedulemc/textures/parts/`
**(Neues Verzeichnis erstellt)**

#### 3.1 fender_chrome.png
- **Farbe:** Silber/Chrom (192, 192, 192)
- **Größe:** 16x16 Pixel
- **Referenziert in:** `PartRegistry.java:83` → `PartChromeBumper`
- **Beschreibung:** Textur für Chrom-Stoßstange
- **Hinweis:** Item-Version existiert als `textures/item/fender_chrome.png`

#### 3.2 fender_sport.png
- **Farbe:** Rot (255, 0, 0)
- **Größe:** 16x16 Pixel
- **Referenziert in:** `PartRegistry.java:84` → `PartSportBumper`
- **Beschreibung:** Textur für Sport-Stoßstange
- **Hinweis:** Item-Version existiert als `textures/item/fender_sport.png`

**Vorlage-Texturen zum Kopieren:**
- `textures/item/fender_chrome.png` - für parts-Version
- `textures/item/fender_sport.png` - für parts-Version

---

## Wie man die Platzhalter durch echte Texturen ersetzt

1. **Identifizieren:** Die Platzhalter haben auffällige Farben (Magenta, Cyan, Gelb, etc.), damit Sie sie sofort erkennen können
2. **Dateiname:** Der Dateiname zeigt genau, welche Textur benötigt wird
3. **Speicherort:** Der Pfad zeigt genau, wo die finale Textur platziert werden muss
4. **Vorlage:** Nutzen Sie die oben angegebenen Vorlagen als Basis für Ihre Texturen
5. **Ersetzen:** Überschreiben Sie einfach die Platzhalter-PNG mit Ihrer finalen Textur

## Namespace-Struktur

Alle Texturen verwenden den **`schedulemc`** Namespace:

```java
ResourceLocation.fromNamespaceAndPath("schedulemc", "textures/entity/sport_wheel.png")
```

## Zusätzliche Informationen

- **Bestehende Platzhalter:** Das Projekt verwendet bereits ein Platzhalter-System mit über 421 kleinen PNG-Dateien (meist 16x16, 153 Bytes)
- **Gesamt-Texturen im Projekt:** 498 PNG-Dateien
- **Textur-Typen:** Blöcke (270), Items (189+), Entities (10), GUI (22), MapView (6)

---

**Erstellt am:** 2026-01-17
**Erstellt durch:** Automatisches Platzhalter-Generierungsskript
