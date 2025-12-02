# ScheduleMC Update-Benachrichtigungssystem

## Übersicht

ScheduleMC verfügt über ein umfassendes Update-Benachrichtigungssystem mit drei Komponenten:

### 1. Client-Side Update-Button (Hauptmenü)
- **Location**: `UpdateNotificationHandler.java`
- **Funktion**: Zeigt einen Update-Button oben rechts im Hauptmenü und Multiplayer-Screen
- **Trigger**: Automatisch 5 Sekunden nach Client-Start
- **Verhalten**: Button öffnet Download-Seite im Browser und kopiert Link in Zwischenablage

### 2. In-Game Benachrichtigung für OPs
- **Location**: `UpdateNotificationHandler.java` (onPlayerLogin)
- **Funktion**: Sendet Chat-Benachrichtigung an Server-OPs beim Login
- **Trigger**: Beim ersten Login eines OPs (Permission Level 2+)
- **Verhalten**: Zeigt Version-Info und klickbaren Download-Link

### 3. Forge Update-Checker
- **Location**: `mods.toml` (updateJSONURL)
- **Funktion**: Forge's eingebautes Update-System
- **JSON-Datei**: `update.json` (muss auf GitHub gehostet werden)
- **Verhalten**: Forge prüft automatisch und zeigt Update-Icon im Mod-Menü

## Technische Details

### VersionChecker
- Prüft GitHub Releases API: `https://api.github.com/repos/Minecraft425HD/ScheduleMC/releases/latest`
- Vergleicht Versionen mit Semantic Versioning (x.y.z)
- Unterstützt Pre-Release-Versionen (z.B. 1.7.0-alpha)
- Async-Prüfung (blockiert nicht den Main-Thread)

### update.json Format
```json
{
  "homepage": "https://github.com/Minecraft425HD/ScheduleMC",
  "1.20.1": {
    "1.7.0-alpha": "Changelog hier...",
    "1.6.0": "Changelog hier..."
  },
  "promos": {
    "1.20.1-latest": "1.7.0-alpha",
    "1.20.1-recommended": "1.6.0"
  }
}
```

## Update.json auf GitHub hosten

Die `update.json` muss im Root des Repositories liegen und über GitHub Pages oder Raw-URL erreichbar sein:
- **Raw-URL**: `https://raw.githubusercontent.com/Minecraft425HD/ScheduleMC/main/update.json`
- **Wichtig**: Bei jedem Release die update.json aktualisieren!

## Bei neuen Releases

1. GitHub Release erstellen mit Tag (z.B. `v1.7.0-alpha`)
2. `update.json` aktualisieren:
   - Neue Version hinzufügen unter `"1.20.1"`
   - Changelog eintragen
   - `promos` aktualisieren
3. Committen und pushen
4. Forge und Custom-Checker erkennen automatisch das Update

## Vorteile dieses Systems

✅ Doppelte Absicherung (Custom + Forge)
✅ Benutzerfreundlich (Button + In-Game)
✅ Automatische Prüfung
✅ OPs werden proaktiv informiert
✅ Clickable Links für einfachen Download
