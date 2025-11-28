# ScheduleMC

<div align="center">

![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-green?style=for-the-badge)
![Forge](https://img.shields.io/badge/Forge-47.4.0-orange?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-17-blue?style=for-the-badge)
![Version](https://img.shields.io/badge/Version-1.0.0--alpha-red?style=for-the-badge)

**Plot-Verwaltung mit Economy-System für Minecraft**

Erstelle und verkaufe Plots mit WorldEdit-Integration und einem umfangreichen NPC-, Tabak- und Wirtschaftssystem.

</div>

---

## 📋 Inhaltsverzeichnis

- [Überblick](#überblick)
- [Features](#features)
- [Installation](#installation)
- [Schnellstart](#schnellstart)
- [Dokumentation](#dokumentation)
- [Systemanforderungen](#systemanforderungen)
- [Support](#support)
- [Lizenz](#lizenz)

---

## 🎯 Überblick

**ScheduleMC** ist eine umfassende Minecraft Forge Mod, die ein komplettes Wirtschafts- und Grundstückssystem zu deinem Server hinzufügt. Mit integrierten NPCs, einem Tabak-Anbausystem, Polizei-Verfolgungen und vielen weiteren Features bietet die Mod ein immersives Gameplay-Erlebnis.

### Hauptmerkmale

- ✅ **Plot-Management** - Kaufe, verkaufe und vermiete Grundstücke
- ✅ **Apartment-System** - Erstelle Untervermietungen mit Kautionssystem
- ✅ **Wirtschaftssystem** - Spieler-Konten, Geldtransfer und Shops
- ✅ **NPC-System** - Intelligente NPCs mit KI, Zeitplänen und Skins
- ✅ **Polizei & Verbrechen** - Wanted-Level System mit Verfolgungsjagden
- ✅ **Tabak-Anbau** - Kompletter Anbauzyklus von der Saat bis zur Verpackung
- ✅ **Diebstahl-Minigame** - Interaktives Gameplay-Element
- ✅ **Tägliche Belohnungen** - Login-Belohnungen mit Streak-System
- ✅ **Rating-System** - Bewerte Plots von anderen Spielern
- ✅ **Mehrsprachig** - Deutsch und Englisch

---

## 🚀 Features

### 🏘️ Plot-System

Erstelle, verwalte und handle mit Grundstücken:

- **Plot-Erstellung** mit visuellem Selection-Tool
- **Besitzer & Vertraute** - Lade Freunde zu deinem Plot ein
- **Verkauf & Miete** - Biete Plots zum Verkauf oder zur Miete an
- **Apartments** - Erstelle Untervermietungen mit Kautionssystem
- **Schutz** - Automatischer Block- und Inventarschutz
- **Bewertungen** - 5-Sterne Bewertungssystem
- **Spatial Indexing** - Optimierte Performance auch bei vielen Plots

### 💰 Wirtschaftssystem

Vollständiges Ingame-Wirtschaftssystem:

- **Spieler-Konten** - Jeder Spieler erhält ein Bankkonto (Standard: 1000€)
- **Geldautomaten (ATM)** - Ein- und Auszahlungen
- **Geldtransfer** - Sende Geld an andere Spieler
- **Cash-Items** - Physisches Geld als Item
- **Wallet-System** - Trage Geld bei dir
- **Admin-Tools** - Setze, gebe und nehme Geld

### 🤖 NPC-System

Intelligente NPCs mit komplexer KI:

- **3 NPC-Typen**: Bewohner, Händler, Polizei
- **Persönlichkeitssystem**: 4 verschiedene Charaktere
- **Zeitplan-basiert**: Home, Work, Leisure Locations
- **Shop-Integration**: Kaufe und verkaufe bei NPC-Händlern
- **Player-Skins**: NPCs können echte Spieler-Skins verwenden
- **Dialog-System**: Interagiere mit NPCs
- **Gehaltssystem**: NPCs erhalten tägliche Gehälter

### 🚔 Polizei & Verbrechen

Dynamisches Crime-System mit Konsequenzen:

- **Wanted-Level**: 0-5 Sterne System
- **Polizei-Verfolgung**: Police-NPCs verfolgen Verbrecher
- **Verhaftung**: Zahle Kaution im Krankenhaus
- **Verstecken**: Verstecke dich in Gebäuden
- **Tür-Blockierung**: Polizei blockiert Türen während Verfolgung
- **Backup-System**: Polizei ruft Verstärkung
- **Raid-Strafen**: Bei illegalen Bargeldbeständen
- **Abbau-System**: 1 Stern pro Tag automatisch entfernt

### 🌿 Tabak-System

Kompletter Tabak-Anbau und Verarbeitung:

**4 Tabak-Sorten**: Virginia, Burley, Oriental, Havana

**Anbauprozess**:
1. **Pflanzung** in speziellen Töpfen (4 Topftypen)
2. **Wachstum** über 8 Stufen
3. **Trocknung** auf Trockengestellen
4. **Fermentation** in Fermentationsfässern
5. **Verpackung** in 4 Größen (Klein, Mittel, Groß, XL)

**Qualitätsstufen**: Niedrig, Mittel, Hoch, Premium

**NPC-Integration**: Verhandle Preise mit NPC-Händlern

### 🎮 Diebstahl-Minigame

Interaktives Minigame zum Bestehlen von NPCs:

- Schneller beweglicher Indikator
- Variable Erfolgszone (abhängig von Schwierigkeit)
- Bis zu 3 Versuche
- NPC-Reaktionen (Kampf oder K.O.)
- Wanted-Level Konsequenzen
- Belohnungssystem

### 🎁 Tägliche Belohnungen

Login-Belohnungen mit Streak-System:

- **Basis-Belohnung**: 50€ pro Tag
- **Streak-Bonus**: +10€ pro konsekutivem Tag (max 30 Tage)
- **Statistiken**: Longest Streak, Total Claims
- **Automatischer Reset** nach verpasstem Tag

### 🛒 Shop-System

NPC-basierte Shops mit dynamischen Preisen:

- Kategorien (Baumarkt, Lebensmittel, etc.)
- Kauf- und Verkaufsmultiplikatoren
- NPC-Händler Integration
- Admin-Shop-Editor

### 🔔 Update-Benachrichtigungen

Automatische Version-Überprüfung:

- GitHub-basiert
- Asynchron (non-blocking)
- Pre-Release Support (alpha, beta, rc)
- Ingame-Benachrichtigungen
- Download-Links

---

## 📦 Installation

### Voraussetzungen

- Minecraft 1.20.1
- Minecraft Forge 47.4.0 oder höher
- Java 17

### Schritte

1. **Forge installieren**
   - Lade [Minecraft Forge 47.4.0](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html) herunter
   - Installiere Forge für Minecraft 1.20.1

2. **Mod herunterladen**
   - Lade die neueste `ScheduleMC-1.0.0-alpha.jar` herunter
   - Oder baue die Mod selbst (siehe [Entwickler-Dokumentation](docs/ENTWICKLER_DOKUMENTATION.md))

3. **Mod installieren**
   ```
   .minecraft/
   └── mods/
       └── ScheduleMC-1.0.0-alpha.jar
   ```

4. **Starte Minecraft** mit dem Forge-Profil

---

## 🎯 Schnellstart

### Als Spieler

1. **Trete dem Server bei**
   - Du erhältst automatisch ein Bankkonto mit 1000€

2. **Erstelle deinen ersten Plot**
   ```
   /plot wand
   ```
   - Rechtsklick auf zwei Ecken um die Plot-Grenzen zu markieren
   ```
   /plot create 500
   ```
   - Erstelle den Plot für 500€ Kaufpreis

3. **Hole deine tägliche Belohnung**
   ```
   /daily
   ```

4. **Kaufe einen Plot**
   ```
   /plot list
   /plot buy <id>
   ```

### Als Admin

1. **Spawne einen NPC**
   ```
   /npc spawn merchant Händler_Karl
   ```

2. **Konfiguriere einen Shop**
   ```
   /npc setshop <uuid> baumarkt
   ```

3. **Verwalte die Wirtschaft**
   ```
   /money give <spieler> 1000
   ```

---

## 📚 Dokumentation

Vollständige Dokumentation findest du in den folgenden Dateien:

- **[Benutzer-Anleitung](docs/BENUTZER_ANLEITUNG.md)** - Alle Befehle und Features erklärt
- **[Entwickler-Dokumentation](docs/ENTWICKLER_DOKUMENTATION.md)** - Architektur und API
- **[Konfiguration](docs/KONFIGURATION.md)** - Alle Config-Optionen
- **[API-Dokumentation](docs/API_DOKUMENTATION.md)** - Für Mod-Entwickler

### Wichtige Befehle (Übersicht)

| Befehl | Beschreibung |
|--------|--------------|
| `/plot wand` | Erhalte das Plot-Selection-Tool |
| `/plot create <preis>` | Erstelle einen Plot |
| `/plot buy [id]` | Kaufe einen Plot |
| `/money` | Zeige deinen Kontostand |
| `/daily` | Hole deine tägliche Belohnung |
| `/shop list` | Zeige verfügbare Shop-Items |
| `/npc spawn <type> <name>` | Spawne einen NPC |

Vollständige Befehlsliste: [Benutzer-Anleitung](docs/BENUTZER_ANLEITUNG.md)

---

## ⚙️ Systemanforderungen

### Minimum

- **Minecraft**: 1.20.1
- **Forge**: 47.4.0
- **Java**: 17
- **RAM**: 4 GB
- **CPU**: Dual-Core 2.5 GHz

### Empfohlen

- **RAM**: 8 GB+
- **CPU**: Quad-Core 3.0 GHz+
- **SSD**: Für schnellere Ladezeiten

---

## 🛠️ Entwicklung

### Projekt bauen

```bash
# Repository klonen
git clone https://github.com/YourUsername/ScheduleMC.git
cd ScheduleMC

# Gradle Build
./gradlew build

# Client starten (zum Testen)
./gradlew runClient

# Server starten
./gradlew runServer
```

Weitere Informationen: [Entwickler-Dokumentation](docs/ENTWICKLER_DOKUMENTATION.md)

---

## 🗂️ Projektstruktur

```
ScheduleMC/
├── src/main/java/de/rolandsw/schedulemc/
│   ├── ScheduleMC.java              # Haupt-Mod-Klasse
│   ├── commands/                     # Alle Befehle
│   ├── economy/                      # Wirtschaftssystem
│   ├── region/                       # Plot-System
│   ├── npc/                          # NPC-System
│   ├── tobacco/                      # Tabak-System
│   ├── managers/                     # Manager-Klassen
│   └── config/                       # Konfiguration
├── src/main/resources/
│   ├── META-INF/mods.toml           # Mod-Metadaten
│   └── assets/schedulemc/
│       ├── lang/                     # Übersetzungen
│       ├── models/                   # Item/Block-Modelle
│       └── textures/                 # Texturen
├── docs/                             # Dokumentation
└── build.gradle                      # Build-Konfiguration
```

---

## 🐛 Support

### Probleme melden

Hast du einen Bug gefunden? [Erstelle ein Issue](https://github.com/YourUsername/ScheduleMC/issues)

### Häufige Probleme

**Q: Mod lädt nicht**
- Überprüfe, dass du Forge 47.4.0+ und Java 17 verwendest
- Stelle sicher, dass die Mod im `mods/` Ordner liegt

**Q: Plots werden nicht gespeichert**
- Überprüfe die Datei `config/plotmod_plots.json`
- Stelle sicher, dass der Server Schreibrechte hat

**Q: NPCs spawnen nicht**
- Verwende `/npc spawn <type> <name>`
- Überprüfe die Server-Logs auf Fehler

**Q: Update-Benachrichtigung erscheint nicht**
- GitHub-API könnte blockiert sein
- Überprüfe deine Internetverbindung

---

## 📄 Lizenz

Dieses Projekt ist unter der [MIT Lizenz](LICENSE) lizenziert.

---

## 🙏 Credits

- **Entwickelt von**: Luckas R. Schneider
- **Minecraft Version**: 1.20.1
- **Forge Version**: 47.4.0
- **Libraries**: Gson 2.10.1

---

## 🔗 Links

- [GitHub Repository](https://github.com/YourUsername/ScheduleMC)
- [Discord](https://discord.gg/YourServer)
- [Wiki](https://github.com/YourUsername/ScheduleMC/wiki)
- [Changelog](CHANGELOG.md)

---

<div align="center">

**Made with ❤️ for the Minecraft Community**

[⬆ Nach oben](#schedulemc)

</div>
