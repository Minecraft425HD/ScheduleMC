# ScheduleMC - API-Dokumentation

## Inhaltsverzeichnis

1. [Übersicht](#übersicht)
2. [API-Zugriff](#api-zugriff)
3. [Plot-System API](#plot-system-api)
4. [Economy-System API](#economy-system-api)
5. [NPC-System API](#npc-system-api)
6. [Tabak-System API](#tabak-system-api)
7. [Crime-System API](#crime-system-api)
8. [Events](#events)
9. [Netzwerk-Integration](#netzwerk-integration)
10. [Beispiele](#beispiele)
11. [Best Practices](#best-practices)

---

## Übersicht

Die **ScheduleMC API** ermöglicht es anderen Mods, mit ScheduleMC-Features zu interagieren. Die API ist vollständig dokumentiert und bietet thread-safe Zugriff auf alle Hauptsysteme.

### API-Prinzipien

- **Stability**: API-Methoden sind abwärtskompatibel
- **Thread-Safety**: Alle API-Methoden sind thread-safe
- **Documentation**: Vollständige JavaDoc-Dokumentation
- **Validation**: Eingabe-Validierung und Fehlerbehandlung

### Abhängigkeit hinzufügen

**build.gradle**:
```gradle
repositories {
    maven {
        url = "https://cursemaven.com"
    }
}

dependencies {
    implementation fg.deobf("curse.maven:schedulemc-PROJECT_ID:FILE_ID")
}
```

---

## API-Zugriff

### Haupt-API-Klasse

**Datei**: `de.rolandsw.schedulemc.api.PlotModAPI`

```java
import de.rolandsw.schedulemc.api.PlotModAPI;

public class MyMod {
    public void myMethod() {
        // Prüfe ob ScheduleMC geladen ist
        if (ModList.get().isLoaded("schedulemc")) {
            // Verwende API
            PlotModAPI.getPlotAt(pos);
        }
    }
}
```

### Verfügbarkeits-Check

Immer vor API-Nutzung prüfen:

```java
public static boolean isScheduleMCAvailable() {
    return ModList.get().isLoaded("schedulemc");
}
```

---

## Plot-System API

### Plot an Position abrufen

```java
/**
 * Gibt den Plot an einer bestimmten Position zurück.
 *
 * @param pos Die BlockPos
 * @return PlotRegion oder null wenn kein Plot
 */
public static PlotRegion getPlotAt(BlockPos pos)
```

**Beispiel**:
```java
PlotRegion plot = PlotModAPI.getPlotAt(blockPos);
if (plot != null) {
    System.out.println("Plot ID: " + plot.getId());
    System.out.println("Owner: " + plot.getOwnerId());
}
```

### Plot-Schutz prüfen

```java
/**
 * Prüft ob eine Position geschützt ist.
 *
 * @param pos Die Position
 * @return true wenn geschützt
 */
public static boolean isProtected(BlockPos pos)
```

**Beispiel**:
```java
if (PlotModAPI.isProtected(pos)) {
    player.sendMessage(Component.literal("Diese Position ist geschützt!"));
}
```

### Bau-Berechtigung prüfen

```java
/**
 * Prüft ob ein Spieler an einer Position bauen darf.
 *
 * @param playerId UUID des Spielers
 * @param pos Die Position
 * @return true wenn erlaubt
 */
public static boolean canPlayerBuild(UUID playerId, BlockPos pos)
```

**Beispiel**:
```java
if (PlotModAPI.canPlayerBuild(player.getUUID(), pos)) {
    // Spieler darf bauen
    placeBlock(pos);
} else {
    // Spieler nicht berechtigt
    event.setCanceled(true);
}
```

### Alle Plots eines Spielers

```java
/**
 * Gibt alle Plots eines Spielers zurück.
 *
 * @param playerId UUID des Spielers
 * @return Liste von PlotRegions
 */
public static List<PlotRegion> getPlayerPlots(UUID playerId)
```

**Beispiel**:
```java
List<PlotRegion> plots = PlotModAPI.getPlayerPlots(player.getUUID());
player.sendMessage(Component.literal("Du besitzt " + plots.size() + " Plots"));
```

### Plot erstellen (programmatisch)

```java
/**
 * Erstellt einen neuen Plot.
 *
 * @param ownerId Besitzer UUID
 * @param pos1 Erste Ecke
 * @param pos2 Zweite Ecke
 * @param price Verkaufspreis
 * @return PlotRegion oder null bei Fehler
 */
public static PlotRegion createPlot(UUID ownerId, BlockPos pos1,
                                    BlockPos pos2, double price)
```

**Beispiel**:
```java
PlotRegion plot = PlotModAPI.createPlot(
    player.getUUID(),
    new BlockPos(0, 64, 0),
    new BlockPos(100, 100, 100),
    1000.0
);

if (plot != null) {
    player.sendMessage(Component.literal("Plot erstellt: ID " + plot.getId()));
}
```

### Plot löschen

```java
/**
 * Löscht einen Plot.
 *
 * @param plotId Plot-ID
 * @return true bei Erfolg
 */
public static boolean deletePlot(int plotId)
```

**Beispiel**:
```java
if (PlotModAPI.deletePlot(123)) {
    System.out.println("Plot 123 gelöscht");
}
```

### PlotRegion-Klasse

```java
public class PlotRegion {
    // Getters
    public int getId();
    public UUID getOwnerId();
    public BlockPos getPos1();
    public BlockPos getPos2();
    public double getPrice();
    public boolean isForSale();
    public Set<UUID> getTrustedPlayers();
    public List<Rating> getRatings();
    public double getAverageRating();

    // Permission check
    public boolean canBuild(UUID playerId);
    public boolean isTrusted(UUID playerId);
    public boolean isOwner(UUID playerId);

    // Rent
    public boolean isForRent();
    public double getRentPrice();
    public UUID getCurrentRenter();
    public long getRentEndTime();

    // Apartments
    public List<PlotArea> getApartments();
}
```

---

## Economy-System API

### Kontostand abrufen

```java
/**
 * Gibt den Kontostand eines Spielers zurück.
 *
 * @param playerId UUID des Spielers
 * @return Kontostand in €
 */
public static double getBalance(UUID playerId)
```

**Beispiel**:
```java
double balance = PlotModAPI.getBalance(player.getUUID());
player.sendMessage(Component.literal("Kontostand: " + balance + "€"));
```

### Geld hinzufügen

```java
/**
 * Fügt Geld zum Konto hinzu.
 *
 * @param playerId UUID des Spielers
 * @param amount Betrag (positiv)
 * @return true bei Erfolg
 */
public static boolean addMoney(UUID playerId, double amount)
```

**Beispiel**:
```java
if (PlotModAPI.addMoney(player.getUUID(), 100.0)) {
    player.sendMessage(Component.literal("Du hast 100€ erhalten!"));
}
```

### Geld abziehen

```java
/**
 * Zieht Geld vom Konto ab.
 *
 * @param playerId UUID des Spielers
 * @param amount Betrag (positiv)
 * @return true wenn genug Geld vorhanden war
 */
public static boolean removeMoney(UUID playerId, double amount)
```

**Beispiel**:
```java
if (PlotModAPI.removeMoney(player.getUUID(), 50.0)) {
    player.sendMessage(Component.literal("50€ wurden abgebucht"));
} else {
    player.sendMessage(Component.literal("Nicht genug Geld!"));
}
```

### Geld transferieren

```java
/**
 * Transferiert Geld zwischen zwei Spielern.
 *
 * @param fromId Sender UUID
 * @param toId Empfänger UUID
 * @param amount Betrag
 * @return true bei Erfolg
 */
public static boolean transferMoney(UUID fromId, UUID toId, double amount)
```

**Beispiel**:
```java
UUID sender = player1.getUUID();
UUID receiver = player2.getUUID();

if (PlotModAPI.transferMoney(sender, receiver, 500.0)) {
    player1.sendMessage(Component.literal("500€ gesendet"));
    player2.sendMessage(Component.literal("500€ erhalten"));
}
```

### Konto existiert prüfen

```java
/**
 * Prüft ob ein Spieler ein Konto hat.
 *
 * @param playerId UUID des Spielers
 * @return true wenn Konto existiert
 */
public static boolean hasAccount(UUID playerId)
```

**Beispiel**:
```java
if (!PlotModAPI.hasAccount(player.getUUID())) {
    PlotModAPI.createAccount(player.getUUID(), 1000.0);
}
```

### Konto erstellen

```java
/**
 * Erstellt ein neues Konto.
 *
 * @param playerId UUID des Spielers
 * @param initialBalance Startguthaben
 * @return true bei Erfolg
 */
public static boolean createAccount(UUID playerId, double initialBalance)
```

---

## NPC-System API

### NPC spawnen

```java
/**
 * Spawnt einen NPC.
 *
 * @param level Die Welt
 * @param pos Position
 * @param type NPC-Typ (RESIDENT, MERCHANT, POLICE)
 * @param name NPC-Name
 * @return CustomNPCEntity oder null
 */
public static CustomNPCEntity spawnNPC(Level level, BlockPos pos,
                                       NPCType type, String name)
```

**Beispiel**:
```java
CustomNPCEntity npc = PlotModAPI.spawnNPC(
    level,
    player.blockPosition(),
    NPCType.MERCHANT,
    "Händler Karl"
);

if (npc != null) {
    player.sendMessage(Component.literal("NPC gespawnt!"));
}
```

### NPC-Daten abrufen

```java
/**
 * Gibt NPC-Daten für eine UUID zurück.
 *
 * @param npcId NPC-UUID
 * @return NPCData oder null
 */
public static NPCData getNPCData(UUID npcId)
```

**Beispiel**:
```java
NPCData data = PlotModAPI.getNPCData(npcUUID);
if (data != null) {
    System.out.println("NPC Name: " + data.getName());
    System.out.println("NPC Type: " + data.getType());
}
```

### NPC entfernen

```java
/**
 * Entfernt einen NPC.
 *
 * @param npcId NPC-UUID
 * @return true bei Erfolg
 */
public static boolean removeNPC(UUID npcId)
```

### Shop zu NPC zuweisen

```java
/**
 * Weist einem NPC eine Shop-Kategorie zu.
 *
 * @param npcId NPC-UUID
 * @param category Shop-Kategorie
 * @return true bei Erfolg
 */
public static boolean setNPCShop(UUID npcId, ShopCategory category)
```

**Beispiel**:
```java
PlotModAPI.setNPCShop(npcUUID, ShopCategory.BAUMARKT);
```

### NPCData-Klasse

```java
public class NPCData {
    public UUID getUuid();
    public String getName();
    public NPCType getType();
    public NPCPersonality getPersonality();
    public BlockPos getHomeLocation();
    public BlockPos getWorkLocation();
    public BlockPos getLeisureLocation();
    public ShopCategory getShopCategory();
    public String getSkinUrl();
}
```

### Enums

```java
public enum NPCType {
    RESIDENT,   // Bewohner
    MERCHANT,   // Händler
    POLICE      // Polizei
}

public enum NPCPersonality {
    FRIENDLY,   // Freundlich
    NEUTRAL,    // Neutral
    AGGRESSIVE, // Aggressiv
    SHY         // Schüchtern
}

public enum ShopCategory {
    BAUMARKT,
    LEBENSMITTEL,
    WAFFEN,
    SONSTIGES
}
```

---

## Tabak-System API

### Tabak-Qualität berechnen

```java
/**
 * Berechnet die Qualität basierend auf Anbau-Parametern.
 *
 * @param soilType Bodentyp
 * @param waterLevel Wasserlevel (0-100)
 * @param fertilizerLevel Düngerlevel (0-100)
 * @return TobaccoQuality
 */
public static TobaccoQuality calculateQuality(SoilType soilType,
                                              int waterLevel,
                                              int fertilizerLevel)
```

**Beispiel**:
```java
TobaccoQuality quality = PlotModAPI.calculateQuality(
    SoilType.CLAY,
    80,
    50
);
// Gibt: HIGH oder PREMIUM zurück
```

### Verhandlungspreis berechnen

```java
/**
 * Berechnet NPC-Angebotspreis für Tabak.
 *
 * @param product Tabak-Paket
 * @param npc NPC-Daten
 * @return Angebotspreis
 */
public static double calculateNegotiationOffer(TobaccoPackage product,
                                               NPCData npc)
```

**Beispiel**:
```java
TobaccoPackage package = new TobaccoPackage(
    TobaccoType.HAVANA,
    TobaccoQuality.PREMIUM,
    PackageSize.LARGE
);

double offer = PlotModAPI.calculateNegotiationOffer(package, npcData);
// Gibt z.B. 1500.0 zurück
```

### Enums

```java
public enum TobaccoType {
    VIRGINIA,
    BURLEY,
    ORIENTAL,
    HAVANA
}

public enum TobaccoQuality {
    LOW,      // Niedrig
    MEDIUM,   // Mittel
    HIGH,     // Hoch
    PREMIUM   // Premium
}

public enum PackageSize {
    SMALL,    // 50g
    MEDIUM,   // 100g
    LARGE,    // 250g
    XLARGE    // 500g
}
```

---

## Crime-System API

### Wanted-Level abrufen

```java
/**
 * Gibt den Wanted-Level eines Spielers zurück.
 *
 * @param playerId Spieler UUID
 * @return Wanted-Level (0-5)
 */
public static int getWantedLevel(UUID playerId)
```

**Beispiel**:
```java
int wantedLevel = PlotModAPI.getWantedLevel(player.getUUID());
if (wantedLevel > 0) {
    player.sendMessage(Component.literal("Wanted Level: " + wantedLevel + " ⭐"));
}
```

### Wanted-Level hinzufügen

```java
/**
 * Fügt Wanted-Level hinzu.
 *
 * @param playerId Spieler UUID
 * @param stars Anzahl Sterne (1-5)
 */
public static void addWantedLevel(UUID playerId, int stars)
```

**Beispiel**:
```java
// Spieler hat etwas Illegales getan
PlotModAPI.addWantedLevel(player.getUUID(), 2);
```

### Wanted-Level entfernen

```java
/**
 * Entfernt Wanted-Level.
 *
 * @param playerId Spieler UUID
 * @param stars Anzahl Sterne
 */
public static void removeWantedLevel(UUID playerId, int stars)
```

**Beispiel**:
```java
// Nach Kaution
PlotModAPI.removeWantedLevel(player.getUUID(), 5);
```

### Wanted-Level zurücksetzen

```java
/**
 * Setzt Wanted-Level auf 0.
 *
 * @param playerId Spieler UUID
 */
public static void clearWantedLevel(UUID playerId)
```

---

## Events

ScheduleMC feuert Custom Events, die du abonnieren kannst.

### PlotPurchaseEvent

```java
@Mod.EventBusSubscriber(modid = "your_mod")
public class MyEventHandler {

    @SubscribeEvent
    public static void onPlotPurchase(PlotPurchaseEvent event) {
        PlotRegion plot = event.getPlot();
        Player buyer = event.getBuyer();
        double price = event.getPrice();

        // Custom logic
        buyer.sendMessage(Component.literal(
            "Glückwunsch zum Kauf von Plot " + plot.getId()
        ));
    }
}
```

### PlotAbandonEvent

```java
@SubscribeEvent
public static void onPlotAbandon(PlotAbandonEvent event) {
    PlotRegion plot = event.getPlot();
    Player owner = event.getOwner();
    double refund = event.getRefund();

    // Custom logic
    LOGGER.info("{} abandoned plot {} for {}€ refund",
        owner.getName().getString(),
        plot.getId(),
        refund
    );
}
```

### EconomyTransactionEvent

```java
@SubscribeEvent
public static void onTransaction(EconomyTransactionEvent event) {
    UUID from = event.getFrom();
    UUID to = event.getTo();
    double amount = event.getAmount();
    TransactionType type = event.getType();

    // Custom logic (z.B. Logging, Steuern)
    if (amount > 10000.0) {
        LOGGER.warn("Large transaction: {} → {} ({}€)",
            from, to, amount);
    }
}
```

### NPCSpawnEvent

```java
@SubscribeEvent
public static void onNPCSpawn(NPCSpawnEvent event) {
    CustomNPCEntity npc = event.getNPC();
    Player spawner = event.getSpawner();

    // Custom logic
    spawner.sendMessage(Component.literal(
        "NPC " + npc.getNPCData().getName() + " gespawnt"
    ));
}
```

### WantedLevelChangeEvent

```java
@SubscribeEvent
public static void onWantedLevelChange(WantedLevelChangeEvent event) {
    Player player = event.getPlayer();
    int oldLevel = event.getOldLevel();
    int newLevel = event.getNewLevel();

    // Custom logic
    if (newLevel > oldLevel) {
        player.sendMessage(Component.literal(
            "§cWanted Level erhöht: " + newLevel + " ⭐"
        ));
    }
}
```

---

## Netzwerk-Integration

### Custom Packets senden

Du kannst eigene Packets via ScheduleMC's Netzwerk senden:

```java
// Packet-Klasse
public class MyCustomPacket {
    private final String message;

    public MyCustomPacket(String message) {
        this.message = message;
    }

    public static void encode(MyCustomPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.message);
    }

    public static MyCustomPacket decode(FriendlyByteBuf buf) {
        return new MyCustomPacket(buf.readUtf());
    }

    public static void handle(MyCustomPacket packet,
                              Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Handle packet
            ServerPlayer player = ctx.get().getSender();
            player.sendSystemMessage(Component.literal(packet.message));
        });
        ctx.get().setPacketHandled(true);
    }
}

// Registrierung
ScheduleMC.NETWORK.registerMessage(
    100, // ID (wähle > 50 um Konflikte zu vermeiden)
    MyCustomPacket.class,
    MyCustomPacket::encode,
    MyCustomPacket::decode,
    MyCustomPacket::handle
);

// Senden
ScheduleMC.NETWORK.sendToServer(new MyCustomPacket("Hello"));
```

---

## Beispiele

### Beispiel 1: Custom Plot-Belohnung

```java
@Mod.EventBusSubscriber(modid = "my_mod")
public class PlotRewardSystem {

    @SubscribeEvent
    public static void onPlotPurchase(PlotPurchaseEvent event) {
        Player buyer = event.getBuyer();
        PlotRegion plot = event.getPlot();

        // Belohnung für ersten Plot
        List<PlotRegion> plots = PlotModAPI.getPlayerPlots(buyer.getUUID());
        if (plots.size() == 1) {
            PlotModAPI.addMoney(buyer.getUUID(), 500.0);
            buyer.sendMessage(Component.literal(
                "§aErster Plot! Du erhältst 500€ Bonus!"
            ));
        }
    }
}
```

### Beispiel 2: Custom NPC-Interaktion

```java
public class MyNPCInteraction {

    public void handleNPCClick(Player player, CustomNPCEntity npc) {
        NPCData data = npc.getNPCData();

        if (data.getType() == NPCType.MERCHANT) {
            // Custom Shop-Logik
            openCustomShop(player, npc);
        } else if (data.getPersonality() == NPCPersonality.FRIENDLY) {
            // Freundlicher NPC gibt Rabatt
            PlotModAPI.addMoney(player.getUUID(), 10.0);
            player.sendMessage(Component.literal(
                "§a" + data.getName() + " gibt dir 10€!"
            ));
        }
    }
}
```

### Beispiel 3: Plot-basierte Permissions

```java
public class PlotPermissionSystem {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        BlockPos pos = event.getPos();

        // Erlaube nur auf eigenem Plot abzubauen
        PlotRegion plot = PlotModAPI.getPlotAt(pos);

        if (plot != null) {
            if (!plot.isOwner(player.getUUID())) {
                event.setCanceled(true);
                player.sendMessage(Component.literal(
                    "§cNur der Plot-Besitzer darf hier abbauen!"
                ));
            }
        }
    }
}
```

### Beispiel 4: Economy-basierte Features

```java
public class TollGateSystem {

    private static final double TOLL_FEE = 10.0;

    public void passTollGate(Player player, BlockPos gatePos) {
        UUID playerId = player.getUUID();
        double balance = PlotModAPI.getBalance(playerId);

        if (balance >= TOLL_FEE) {
            if (PlotModAPI.removeMoney(playerId, TOLL_FEE)) {
                player.sendMessage(Component.literal(
                    "§aMaut bezahlt: " + TOLL_FEE + "€"
                ));
                openGate(gatePos);
            }
        } else {
            player.sendMessage(Component.literal(
                "§cNicht genug Geld für Maut! (" + TOLL_FEE + "€)"
            ));
        }
    }

    private void openGate(BlockPos pos) {
        // Gate-Logik
    }
}
```

### Beispiel 5: Wanted-Level Integration

```java
public class CrimeDetectionSystem {

    @SubscribeEvent
    public static void onPlayerAttack(LivingAttackEvent event) {
        if (event.getSource().getEntity() instanceof Player attacker) {
            if (event.getEntity() instanceof Villager) {
                // Villager angegriffen = +1 Wanted-Level
                PlotModAPI.addWantedLevel(attacker.getUUID(), 1);
                attacker.sendMessage(Component.literal(
                    "§cDu hast einen Dorfbewohner angegriffen! +1 ⭐"
                ));
            }
        }
    }
}
```

---

## Best Practices

### 1. Immer Verfügbarkeit prüfen

```java
// ✅ GUT
if (ModList.get().isLoaded("schedulemc")) {
    PlotModAPI.getPlotAt(pos);
}

// ❌ SCHLECHT
PlotModAPI.getPlotAt(pos); // Crash wenn Mod nicht geladen
```

### 2. Null-Checks

```java
// ✅ GUT
PlotRegion plot = PlotModAPI.getPlotAt(pos);
if (plot != null) {
    plot.doSomething();
}

// ❌ SCHLECHT
PlotModAPI.getPlotAt(pos).doSomething(); // NPE wenn kein Plot
```

### 3. Exception-Handling

```java
// ✅ GUT
try {
    PlotModAPI.createPlot(owner, pos1, pos2, price);
} catch (Exception e) {
    LOGGER.error("Failed to create plot", e);
}

// ❌ SCHLECHT
PlotModAPI.createPlot(owner, pos1, pos2, price); // Unhandled exceptions
```

### 4. Event-Priorität

```java
// ✅ GUT: Verwende Priorität wenn nötig
@SubscribeEvent(priority = EventPriority.HIGH)
public static void onPlotPurchase(PlotPurchaseEvent event) {
    // Wird vor anderen Handlern aufgerufen
}
```

### 5. Thread-Safety

```java
// ✅ GUT: Enqueue work bei Packets
ctx.get().enqueueWork(() -> {
    PlotModAPI.addMoney(playerId, amount);
});

// ❌ SCHLECHT: Direkter Zugriff
PlotModAPI.addMoney(playerId, amount); // Kann Race Conditions verursachen
```

### 6. Resource Locations

```java
// ✅ GUT: Verwende korrekte Namespace
new ResourceLocation("schedulemc", "textures/gui/my_gui.png");

// ❌ SCHLECHT: Falscher Namespace
new ResourceLocation("minecraft", "schedulemc_gui.png");
```

---

## Versionierung

Die API folgt Semantic Versioning:

```
MAJOR.MINOR.PATCH

1.0.0 → 1.0.1: Bug Fixes (abwärtskompatibel)
1.0.0 → 1.1.0: Neue Features (abwärtskompatibel)
1.0.0 → 2.0.0: Breaking Changes (NICHT abwärtskompatibel)
```

**Aktuelle API-Version**: 1.0.0

---

## Support

### API-Fragen

- GitHub Issues: [Report API Issue](https://github.com/YourUsername/ScheduleMC/issues)
- Discord: [Join Server](https://discord.gg/YourServer)

### JavaDoc

Vollständige JavaDoc-Dokumentation:
```bash
./gradlew javadoc
# Output: build/docs/javadoc/index.html
```

### Weitere Ressourcen

- [Entwickler-Dokumentation](ENTWICKLER_DOKUMENTATION.md)
- [Benutzer-Anleitung](BENUTZER_ANLEITUNG.md)
- [Konfiguration](KONFIGURATION.md)

---

<div align="center">

**Happy Developing!**

[⬆ Nach oben](#schedulemc---api-dokumentation)

</div>
