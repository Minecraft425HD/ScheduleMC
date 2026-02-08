# Nachrichten-System

> Spieler-zu-Spieler und Spieler-zu-NPC Kommunikation

## Ueberblick

Das Nachrichten-System ermoeglicht direkte Kommunikation zwischen Spielern sowie zwischen Spielern und NPCs. Nachrichten werden persistent gespeichert und koennen ueber die Smartphone-App eingesehen werden. Das System unterstuetzt Echtzeit-Benachrichtigungen, Reputations-Tracking fuer NPCs und Thread-sichere Operationen.

## Funktionen

### Spieler-zu-Spieler Nachrichten

- Direkte Nachrichten zwischen Spielern
- Nachrichten-Verlauf bleibt gespeichert
- Offline-Nachrichten: Gespeichert und beim Login verfuegbar
- Echtzeit-Benachrichtigungen wenn Empfaenger online ist

### Spieler-zu-NPC Nachrichten

- Kommunikation mit NPCs im Spiel
- **Reputations-basierte Antworten**:
  - Niedrig (< 34): Unfreundliche Nachrichten
  - Mittel (34-67): Neutrale Nachrichten
  - Hoch (> 67): Freundliche Nachrichten
- 3 Nachrichtenvarianten pro Stimmungslage
- Lokalisierte Texte

### Echtzeit-Benachrichtigungen

Bei eingehenden Nachrichten erscheint ein Pop-up am oberen Bildschirmrand:
- 3 Sekunden Anzeigedauer
- Einblend-Animation (300 ms)
- Ausblend-Animation (300 ms)
- Absender-Name und Nachrichtenvorschau (max. 40 Zeichen)

## Smartphone-Integration

### Nachrichten-App

Die Nachrichten-App auf dem Smartphone zeigt:
- Alle Konversationen sortiert nach letzter Nachricht
- Nachrichtenvorschau pro Konversation
- Unterscheidung zwischen Spieler- und NPC-Nachrichten
- Vollstaendiger Nachrichtenverlauf pro Konversation

### Kontakte-App

- Kontaktliste mit allen bekannten Spielern und NPCs
- Kontaktdetails mit Nachrichtenverlauf
- Direkte Nachricht aus Kontakten heraus senden

## Nachrichten-Beschraenkungen

| Parameter | Wert |
|-----------|------|
| Max. Nachrichtenlaenge | 500 Zeichen |
| Max. Netzwerk-Paketgroesse | 1.024 Zeichen |
| Max. Teilnehmername | 100 Zeichen |
| Max. Datensaetze | 10.000 Eintraege |

## API

Das Nachrichten-System bietet eine oeffentliche API (IMessagingAPI):

### Basis-Funktionen

| Methode | Beschreibung |
|---------|-------------|
| sendMessage(von, zu, nachricht) | Nachricht senden |
| getUnreadMessageCount(spieler) | Ungelesene Nachrichten zaehlen |
| getMessages(spieler, limit) | Nachrichten abrufen |
| markAllAsRead(spieler) | Alle als gelesen markieren |
| deleteMessage(spieler, id) | Nachricht loeschen |
| deleteAllMessages(spieler) | Alle Nachrichten loeschen |
| getTotalMessageCount(spieler) | Gesamtanzahl |

### Erweiterte Funktionen (ab v3.2.0)

| Methode | Beschreibung |
|---------|-------------|
| broadcastMessage(von, nachricht) | An alle senden |
| sendSystemMessage(zu, nachricht) | System-Nachricht senden |
| getConversation(spielerA, spielerB, limit) | Konversation abrufen |
| isBlocked(spieler, geblockt) | Blockier-Status pruefen |
| blockPlayer(spieler, geblockt) | Spieler blockieren |
| unblockPlayer(spieler, geblockt) | Spieler entblockieren |

## Netzwerk-Pakete

| Paket | Richtung | Beschreibung |
|-------|----------|-------------|
| SendMessagePacket | Client zu Server | Nachricht senden |
| ReceiveMessagePacket | Server zu Client | Eingehende Nachricht |

### Bandbreiten-Optimierung

- Empfaengername wird nur bei NPC-Nachrichten mitgesendet (spart Bandbreite bei Spieler-Nachrichten)
- Server loest Spielernamen ueber den GameProfile-Cache auf

## Datenspeicherung

**Datei**: `config/plotmod_messages.json`

### Gespeicherte Daten pro Konversation

- Teilnehmer-UUID und Name
- Spieler- oder NPC-Teilnehmer
- Vollstaendiger Nachrichtenverlauf
- Reputationswert (0-100, nur bei NPC-Konversationen)

### Datenvalidierung

- UUID-Validierung mit Fehlerbehandlung
- Stringlaengen-Begrenzung
- Reputations-Clamping auf -100 bis 100
- Schutz vor korrupten Daten (max. 10.000 Eintraege)
- Automatische Korrektur beim Laden
