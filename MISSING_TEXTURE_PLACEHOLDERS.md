# Missing Texture Placeholders

Dieses Dokument listet **ALLE** PNG-Platzhalter auf, die für fehlende Texturen erstellt wurden.

## Übersicht

**Gesamt Platzhalter:** 188 (7 Basis-Texturen + 181 Varianten)
- **Basis-Texturen (komplett fehlend):** 7
- **Varianten-Texturen (Qualitäts-/Typ-Varianten):** 181

**Zweck:** Diese Platzhalter zeigen genau:
- Welche Texturen/Varianten fehlen (nach Dateiname)
- Welche Qualität/Typ sie repräsentieren (nach Farbe aus Code)
- Wo sie platziert werden müssen (nach Pfad)

---

# TEIL 1: BASIS-TEXTUREN (Komplett fehlend)

Diese 7 Texturen wurden im Code referenziert, hatten aber **GAR KEINE** PNG-Datei.

## 1. Fahrzeug-Rad-Texturen (4 Platzhalter)

**Speicherort:** `src/main/resources/assets/schedulemc/textures/entity/`

### 1.1 sport_wheel.png
- **Farbe:** Magenta (255, 0, 255)
- **Größe:** 16x16 Pixel
- **Referenziert in:** `PartRegistry.java:37` → `PartSportTire`
- **3D-Modell:** `models/entity/wheel.obj`

### 1.2 premium_wheel.png
- **Farbe:** Cyan (0, 255, 255)
- **Größe:** 16x16 Pixel
- **Referenziert in:** `PartRegistry.java:44` → `PartPremiumTire`
- **3D-Modell:** `models/entity/wheel.obj`

### 1.3 allterrain_wheel.png
- **Farbe:** Gelb (255, 255, 0)
- **Größe:** 16x16 Pixel
- **Referenziert in:** `PartRegistry.java:59` → `PartAllterrainTire`
- **3D-Modell:** `models/entity/big_wheel.obj`

### 1.4 heavyduty_wheel.png
- **Farbe:** Orange (255, 128, 0)
- **Größe:** 16x16 Pixel
- **Referenziert in:** `PartRegistry.java:66` → `PartHeavyDutyTire`
- **3D-Modell:** `models/entity/big_wheel.obj`

## 2. GUI-Texturen (1 Platzhalter)

**Speicherort:** `src/main/resources/assets/schedulemc/textures/gui/`

### 2.1 gui_garage.png
- **Farbe:** Hellblau (128, 128, 255)
- **Größe:** 256x256 Pixel
- **Referenziert in:** `GuiGarage.java`

## 3. Fahrzeugteile-Texturen (2 Platzhalter)

**Speicherort:** `src/main/resources/assets/schedulemc/textures/parts/`

### 3.1 fender_chrome.png
- **Farbe:** Silber (192, 192, 192)
- **Größe:** 16x16 Pixel
- **Referenziert in:** `PartRegistry.java:83` → `PartChromeBumper`

### 3.2 fender_sport.png
- **Farbe:** Rot (255, 0, 0)
- **Größe:** 16x16 Pixel
- **Referenziert in:** `PartRegistry.java:84` → `PartSportBumper`

---

# TEIL 2: VARIANTEN-TEXTUREN (Qualitäts-/Typ-System)

Diese 181 Texturen sind Varianten von Items, die **NUR EINE BASIS-TEXTUR** haben, aber laut Code **MEHRERE QUALITÄTS-/TYP-VARIANTEN** haben sollten.

**Speicherort (alle):** `src/main/resources/assets/schedulemc/textures/item/`

---

## 4. METH-SYSTEM (11 Platzhalter)

**Qualitätsstufen:** 3 (STANDARD §f, GUT §e, BLUE_SKY §b§l)

### 4.1 Meth (2 Varianten - 1 existiert bereits)
- `meth_gut.png` - Gelb (255, 255, 85)
- `meth_blue_sky.png` - Cyan (85, 255, 255)

### 4.2 Kristall Meth (3 Varianten)
- `kristall_meth_standard.png` - Weiß (255, 255, 255)
- `kristall_meth_gut.png` - Gelb (255, 255, 85)
- `kristall_meth_blue_sky.png` - Cyan (85, 255, 255)

### 4.3 Roh Meth (3 Varianten)
- `roh_meth_standard.png` - Weiß (255, 255, 255)
- `roh_meth_gut.png` - Gelb (255, 255, 85)
- `roh_meth_blue_sky.png` - Cyan (85, 255, 255)

### 4.4 Meth Paste (3 Varianten)
- `meth_paste_standard.png` - Weiß (255, 255, 255)
- `meth_paste_gut.png` - Gelb (255, 255, 85)
- `meth_paste_blue_sky.png` - Cyan (85, 255, 255)

---

## 5. MDMA-SYSTEM (6 Platzhalter)

**Qualitätsstufen:** 4 (SCHLECHT §7, STANDARD §f, GUT §e, PREMIUM §d§l)

### 5.1 MDMA Base (3 Varianten - 1 existiert bereits)
- `mdma_base_schlecht.png` - Grau (170, 170, 170)
- `mdma_base_gut.png` - Gelb (255, 255, 85)
- `mdma_base_premium.png` - Lila (255, 85, 255)

### 5.2 MDMA Kristall (3 Varianten - 1 existiert bereits)
- `mdma_kristall_schlecht.png` - Grau (170, 170, 170)
- `mdma_kristall_gut.png` - Gelb (255, 255, 85)
- `mdma_kristall_premium.png` - Lila (255, 85, 255)

---

## 6. COCAINE-SYSTEM (8 Platzhalter)

**Typen:** 2 (BOLIVIANISCH §a, KOLUMBIANISCH §2)
**Qualitätsstufen:** 4 (SCHLECHT, GUT, SEHR_GUT, LEGENDAER)
**Kombinationen:** 2 × 4 = 8 Varianten

### 6.1 Bolivianisches Cocaine (4 Varianten - Hellgrün)
- `cocaine_bolivianisch_schlecht.png` - RGB(85, 255, 85)
- `cocaine_bolivianisch_gut.png` - RGB(85, 255, 85)
- `cocaine_bolivianisch_sehr_gut.png` - RGB(85, 255, 85)
- `cocaine_bolivianisch_legendaer.png` - RGB(85, 255, 85)

### 6.2 Kolumbianisches Cocaine (4 Varianten - Dunkelgrün)
- `cocaine_kolumbianisch_schlecht.png` - RGB(0, 170, 0)
- `cocaine_kolumbianisch_gut.png` - RGB(0, 170, 0)
- `cocaine_kolumbianisch_sehr_gut.png` - RGB(0, 170, 0)
- `cocaine_kolumbianisch_legendaer.png` - RGB(0, 170, 0)

---

## 7. CRACK-ROCK-SYSTEM (8 Platzhalter)

**Typen:** 2 (BOLIVIANISCH §a, KOLUMBIANISCH §2)
**Qualitätsstufen:** 4 (SCHLECHT, STANDARD, GUT, FISHSCALE)
**Kombinationen:** 2 × 4 = 8 Varianten

### 7.1 Bolivianischer Crack (4 Varianten - Hellgrün)
- `crack_rock_bolivianisch_schlecht.png` - RGB(85, 255, 85)
- `crack_rock_bolivianisch_standard.png` - RGB(85, 255, 85)
- `crack_rock_bolivianisch_gut.png` - RGB(85, 255, 85)
- `crack_rock_bolivianisch_fishscale.png` - RGB(85, 255, 85)

### 7.2 Kolumbianischer Crack (4 Varianten - Dunkelgrün)
- `crack_rock_kolumbianisch_schlecht.png` - RGB(0, 170, 0)
- `crack_rock_kolumbianisch_standard.png` - RGB(0, 170, 0)
- `crack_rock_kolumbianisch_gut.png` - RGB(0, 170, 0)
- `crack_rock_kolumbianisch_fishscale.png` - RGB(0, 170, 0)

---

## 8. HEROIN-SYSTEM (12 Platzhalter)

**Typen:** 3 (AFGHANISCH §4, TUERKISCH §6, INDISCH §5)
**Qualitätsstufen:** 4 (SCHLECHT, GUT, SEHR_GUT, LEGENDAER)
**Kombinationen:** 3 × 4 = 12 Varianten

### 8.1 Afghanisches Heroin (4 Varianten - Dunkelrot)
- `heroin_afghanisch_schlecht.png` - RGB(170, 0, 0)
- `heroin_afghanisch_gut.png` - RGB(170, 0, 0)
- `heroin_afghanisch_sehr_gut.png` - RGB(170, 0, 0)
- `heroin_afghanisch_legendaer.png` - RGB(170, 0, 0)

### 8.2 Türkisches Heroin (4 Varianten - Gold/Orange)
- `heroin_tuerkisch_schlecht.png` - RGB(255, 170, 0)
- `heroin_tuerkisch_gut.png` - RGB(255, 170, 0)
- `heroin_tuerkisch_sehr_gut.png` - RGB(255, 170, 0)
- `heroin_tuerkisch_legendaer.png` - RGB(255, 170, 0)

### 8.3 Indisches Heroin (4 Varianten - Dunkellila)
- `heroin_indisch_schlecht.png` - RGB(170, 0, 170)
- `heroin_indisch_gut.png` - RGB(170, 0, 170)
- `heroin_indisch_sehr_gut.png` - RGB(170, 0, 170)
- `heroin_indisch_legendaer.png` - RGB(170, 0, 170)

---

## 9. CANNABIS-SYSTEM (120 Platzhalter!)

**Strains:** 4 (INDICA §5, SATIVA §a, HYBRID §e, AUTOFLOWER §b)
**Qualitätsstufen:** 5 (SCHWAG §8, MIDS §7, DANK §a, TOP_SHELF §6, EXOTIC §d§l)
**Kombinationen:** 4 × 5 = 20 Varianten pro Item

### Farbschema nach Qualität:
- **SCHWAG** - Dunkelgrau RGB(85, 85, 85)
- **MIDS** - Grau RGB(170, 170, 170)
- **DANK** - Grün RGB(85, 255, 85)
- **TOP_SHELF** - Gold RGB(255, 170, 0)
- **EXOTIC** - Lila RGB(255, 85, 255)

### 9.1 Cured Cannabis Bud (20 Varianten)
- `cured_cannabis_bud_indica_schwag.png` bis `..._exotic.png`
- `cured_cannabis_bud_sativa_schwag.png` bis `..._exotic.png`
- `cured_cannabis_bud_hybrid_schwag.png` bis `..._exotic.png`
- `cured_cannabis_bud_autoflower_schwag.png` bis `..._exotic.png`

### 9.2 Trimmed Cannabis Bud (20 Varianten)
- `trimmed_cannabis_bud_indica_schwag.png` bis `..._exotic.png`
- `trimmed_cannabis_bud_sativa_schwag.png` bis `..._exotic.png`
- `trimmed_cannabis_bud_hybrid_schwag.png` bis `..._exotic.png`
- `trimmed_cannabis_bud_autoflower_schwag.png` bis `..._exotic.png`

### 9.3 Dried Cannabis Bud (20 Varianten)
- `dried_cannabis_bud_indica_schwag.png` bis `..._exotic.png`
- `dried_cannabis_bud_sativa_schwag.png` bis `..._exotic.png`
- `dried_cannabis_bud_hybrid_schwag.png` bis `..._exotic.png`
- `dried_cannabis_bud_autoflower_schwag.png` bis `..._exotic.png`

### 9.4 Fresh Cannabis Bud (20 Varianten)
- `fresh_cannabis_bud_indica_schwag.png` bis `..._exotic.png`
- `fresh_cannabis_bud_sativa_schwag.png` bis `..._exotic.png`
- `fresh_cannabis_bud_hybrid_schwag.png` bis `..._exotic.png`
- `fresh_cannabis_bud_autoflower_schwag.png` bis `..._exotic.png`

### 9.5 Cannabis Hash (20 Varianten)
- `cannabis_hash_indica_schwag.png` bis `..._exotic.png`
- `cannabis_hash_sativa_schwag.png` bis `..._exotic.png`
- `cannabis_hash_hybrid_schwag.png` bis `..._exotic.png`
- `cannabis_hash_autoflower_schwag.png` bis `..._exotic.png`

### 9.6 Cannabis Oil (20 Varianten)
- `cannabis_oil_indica_schwag.png` bis `..._exotic.png`
- `cannabis_oil_sativa_schwag.png` bis `..._exotic.png`
- `cannabis_oil_hybrid_schwag.png` bis `..._exotic.png`
- `cannabis_oil_autoflower_schwag.png` bis `..._exotic.png`

---

## 10. TABAK-SYSTEM (16 Platzhalter)

**Typen:** 4 (VIRGINIA §e, BURLEY §6, ORIENTAL §d, HAVANA §c§l)
**Qualitätsstufen:** 4 (SCHLECHT, GUT, SEHR_GUT, LEGENDAER)
**Kombinationen:** 4 × 4 = 16 Varianten

### 10.1 Virginia Tabak (4 Varianten - Gelb)
- `packaged_tobacco_virginia_schlecht.png` - RGB(255, 255, 85)
- `packaged_tobacco_virginia_gut.png` - RGB(255, 255, 85)
- `packaged_tobacco_virginia_sehr_gut.png` - RGB(255, 255, 85)
- `packaged_tobacco_virginia_legendaer.png` - RGB(255, 255, 85)

### 10.2 Burley Tabak (4 Varianten - Gold/Orange)
- `packaged_tobacco_burley_schlecht.png` - RGB(255, 170, 0)
- `packaged_tobacco_burley_gut.png` - RGB(255, 170, 0)
- `packaged_tobacco_burley_sehr_gut.png` - RGB(255, 170, 0)
- `packaged_tobacco_burley_legendaer.png` - RGB(255, 170, 0)

### 10.3 Oriental Tabak (4 Varianten - Lila)
- `packaged_tobacco_oriental_schlecht.png` - RGB(255, 85, 255)
- `packaged_tobacco_oriental_gut.png` - RGB(255, 85, 255)
- `packaged_tobacco_oriental_sehr_gut.png` - RGB(255, 85, 255)
- `packaged_tobacco_oriental_legendaer.png` - RGB(255, 85, 255)

### 10.4 Havana Tabak (4 Varianten - Rot)
- `packaged_tobacco_havana_schlecht.png` - RGB(255, 85, 85)
- `packaged_tobacco_havana_gut.png` - RGB(255, 85, 85)
- `packaged_tobacco_havana_sehr_gut.png` - RGB(255, 85, 85)
- `packaged_tobacco_havana_legendaer.png` - RGB(255, 85, 85)

---

# Zusammenfassung nach Kategorien

| Kategorie | Items | Varianten pro Item | Gesamt Platzhalter |
|-----------|-------|-------------------|-------------------|
| **Meth** | 4 | 3 | **11** |
| **MDMA** | 2 | 4 | **6** |
| **Cocaine** | 1 | 8 | **8** |
| **Crack Rock** | 1 | 8 | **8** |
| **Heroin** | 1 | 12 | **12** |
| **Cannabis** | 6 | 20 | **120** |
| **Tabak** | 1 | 16 | **16** |
| **Fahrzeug-Räder** | 4 | - | **4** |
| **GUI** | 1 | - | **1** |
| **Fahrzeugteile** | 2 | - | **2** |
| **GESAMT** | **23** | - | **188** |

---

# Wie man die Platzhalter ersetzt

1. **Identifizieren:** Auffällige Farben zeigen sofort fehlende Texturen im Spiel
2. **Dateiname:** Zeigt genau Qualität/Typ (z.B. `meth_blue_sky.png`)
3. **Farbe:** Basiert auf Minecraft-Farbcodes aus dem Spiel-Code
4. **Ersetzen:** Überschreiben Sie die Platzhalter-PNG mit finalen Texturen

---

**Erstellt am:** 2026-01-17
**Basis-Texturen:** 7
**Varianten-Texturen:** 181
**Gesamt:** 188 Platzhalter
