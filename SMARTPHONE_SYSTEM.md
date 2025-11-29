# Smartphone-GUI-System für ScheduleMC

## Übersicht

Das Smartphone-GUI-System wurde erfolgreich implementiert und bietet folgende Features:

### ✅ Implementierte Features

1. **Konfigurierbares Keybinding**
   - Standard-Taste: **P**
   - Konfigurierbar in den Minecraft-Einstellungen
   - Kategorie: "ScheduleMC"

2. **Haupt-Smartphone-GUI**
   - 6 Apps symmetrisch angeordnet (3 Reihen x 2 Spalten)
   - Schließen-Button oben rechts (X)
   - Zurück-Button unten zentriert
   - Mit Taste (P) öffnen und schließen

3. **6 App-Screens**
   - **Map**: Karten-Ansicht
   - **Dealer**: Händler-Übersicht
   - **Produkte**: Produktkatalog
   - **Bestellung**: Bestellungen verwalten
   - **Kontakte**: Kontaktliste
   - **Nachrichten**: Posteingang

   Jede App hat einen "Zurück"-Button zur Hauptansicht

4. **Spielerschutz beim geöffneten GUI**
   - Spieler kann nicht sterben, wenn Smartphone offen ist
   - Angreifer erhält automatisch **+1 Wanted-Stern**
   - Beide Spieler werden über den Schutz benachrichtigt

5. **Anpassbare App-Icons**
   - Platzhalter-PNG-Bilder (48x48 Pixel)
   - Einfach austauschbar
   - Verzeichnis: `src/main/resources/assets/schedulemc/textures/gui/apps/`

6. **Mehrsprachigkeit**
   - Deutsch (de_de.json)
   - Englisch (en_us.json)

## Dateistruktur

```
src/main/java/de/rolandsw/schedulemc/
├── client/
│   ├── KeyBindings.java                    # Keybinding-Definitionen
│   ├── SmartphoneKeyHandler.java           # Tastendruck-Handler
│   ├── SmartphoneProtectionHandler.java    # Schutz-Event-Handler
│   ├── SmartphonePlayerHandler.java        # Player-Event-Handler
│   ├── SmartphoneTracker.java              # Server-Side Tracking
│   ├── network/
│   │   ├── SmartphoneNetworkHandler.java   # Netzwerk-Registrierung
│   │   └── SmartphoneStatePacket.java      # GUI-Status-Paket
│   └── screen/
│       ├── SmartphoneScreen.java           # Haupt-GUI
│       └── apps/
│           ├── MapAppScreen.java           # Map-App
│           ├── DealerAppScreen.java        # Dealer-App
│           ├── ProductsAppScreen.java      # Produkte-App
│           ├── OrderAppScreen.java         # Bestellung-App
│           ├── ContactsAppScreen.java      # Kontakte-App
│           └── MessagesAppScreen.java      # Nachrichten-App

src/main/resources/assets/schedulemc/
├── textures/gui/apps/
│   ├── app_map.png          # Map-Icon (Blau)
│   ├── app_dealer.png       # Dealer-Icon (Rot)
│   ├── app_products.png     # Produkte-Icon (Grün)
│   ├── app_order.png        # Bestellung-Icon (Gelb)
│   ├── app_contacts.png     # Kontakte-Icon (Lila)
│   ├── app_messages.png     # Nachrichten-Icon (Türkis)
│   ├── close.png            # Schließen-Icon (Rot)
│   └── README.md            # Icon-Dokumentation
└── lang/
    ├── de_de.json           # Deutsche Übersetzungen
    └── en_us.json           # Englische Übersetzungen
```

## Verwendung

### Für Spieler

1. **Smartphone öffnen**: Drücke die **P-Taste** (Standard)
2. **App auswählen**: Klicke auf ein App-Icon
3. **Zurück**: Nutze den "Zurück"-Button oder drücke erneut **P**
4. **Schließen**: X-Button oben rechts oder **P-Taste**

### Schutz-Mechanismus

- Solange das Smartphone-GUI offen ist:
  - ✅ Spieler ist **immun gegen Schaden**
  - ⚠ Angreifer erhält **+1 Wanted-Stern**
  - 📢 Beide Spieler werden benachrichtigt

## Icons anpassen

### Vorhandene Icons ersetzen

1. Erstelle ein **48x48 Pixel PNG-Bild**
2. Benenne es entsprechend:
   - `app_map.png` - Map-App
   - `app_dealer.png` - Dealer-App
   - `app_products.png` - Produkte-App
   - `app_order.png` - Bestellung-App
   - `app_contacts.png` - Kontakte-App
   - `app_messages.png` - Nachrichten-App
   - `close.png` - Schließen-Button
3. Ersetze die Datei in: `src/main/resources/assets/schedulemc/textures/gui/apps/`
4. Lade Ressourcen neu (**F3+T** im Spiel) oder starte neu

### Design-Tipps

- Verwende klare, einfache Symbole
- Achte auf gute Kontraste
- Teste die Icons im Spiel
- Einheitliches Design für alle Apps empfohlen

## Technische Details

### Netzwerk-Kommunikation

Das System verwendet ein Client-Server-Netzwerk-Protokoll:

1. **Client öffnet GUI** → Sendet `SmartphoneStatePacket(true)` an Server
2. **Server trackt Status** → `SmartphoneTracker` registriert Spieler
3. **Angriff erfolgt** → `SmartphoneProtectionHandler` prüft Status
4. **Client schließt GUI** → Sendet `SmartphoneStatePacket(false)` an Server

### Event-Handler

**SmartphoneProtectionHandler**:
- Horcht auf `LivingAttackEvent`
- Prüft ob Opfer Smartphone offen hat
- Cancelt Schaden-Event
- Fügt Angreifer Wanted-Level hinzu

**SmartphonePlayerHandler**:
- Horcht auf `PlayerLoggedOutEvent`
- Bereinigt Tracking-Daten bei Disconnect

### Integration mit bestehendem System

Das Smartphone-System integriert sich nahtlos mit:
- ✅ **Crime-System**: Nutzt `CrimeManager.addWantedLevel()`
- ✅ **NPC-System**: Kompatibel mit bestehenden Events
- ✅ **Economy-System**: Bereit für Shop-Integration in Apps

## Erweiterungsmöglichkeiten

### App-Funktionalität hinzufügen

Beispiel: Map-App mit echter Karte erweitern

```java
// In MapAppScreen.java
@Override
public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    // ... bestehender Code ...

    // Füge Map-Rendering hinzu
    if (minecraft != null && minecraft.player != null) {
        Level level = minecraft.player.level();
        // Render mini-map logic here
    }
}
```

### Neue App hinzufügen

1. Erstelle neue App-Screen-Klasse in `client/screen/apps/`
2. Erstelle Icon (48x48 PNG) in `textures/gui/apps/`
3. Füge ResourceLocation in `SmartphoneScreen.java` hinzu
4. Füge Button und Icon-Rendering hinzu
5. Teste im Spiel

## Bekannte Einschränkungen

- Icons sind derzeit einfarbige Platzhalter
- App-Funktionalität ist noch minimal (Basis-UI vorhanden)
- Keine persistente Daten-Speicherung für Apps

## Zukünftige Verbesserungen

- [ ] Realistische Icons mit Details
- [ ] Map-App mit echter Minimap
- [ ] Kontakte-System mit NPC-Integration
- [ ] Nachrichten-System mit Chat-Funktion
- [ ] Produkte-App mit Shop-Integration
- [ ] Dealer-App mit NPC-Standorten
- [ ] Bestellung-App mit Tracking

## Support

Bei Problemen oder Fragen:
- Überprüfe die Logs in `logs/latest.log`
- Suche nach "Smartphone" oder "SmartphoneProtection"
- Überprüfe Keybinding in Minecraft-Einstellungen

## Changelog

### Version 1.0 (2025-11-29)
- ✅ Basis-Smartphone-GUI implementiert
- ✅ 6 App-Screens erstellt
- ✅ Keybinding-System (P-Taste)
- ✅ Spielerschutz bei geöffnetem GUI
- ✅ Wanted-System-Integration
- ✅ Platzhalter-Icons erstellt
- ✅ Deutsch/Englisch Lokalisierung
