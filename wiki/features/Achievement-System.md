# Achievement-System

<div align="center">

**Erfolge freischalten und Belohnungen verdienen**

24 Achievements in 5 Kategorien mit bis zu 50.000 EUR Belohnung

[Back to Wiki Home](../Home.md) | [Commands Reference](../Commands.md)

</div>

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Achievement Categories](#achievement-categories)
4. [Difficulty Tiers and Rewards](#difficulty-tiers-and-rewards)
5. [Complete Achievement List](#complete-achievement-list)
   - [Economy Achievements](#economy-achievements-11)
   - [Crime Achievements](#crime-achievements-6)
   - [Production Achievements](#production-achievements-5)
   - [Social Achievements](#social-achievements-4)
6. [Automatic Tracking System](#automatic-tracking-system)
7. [Smartphone App](#smartphone-app)
8. [Network Synchronization](#network-synchronization)
9. [Developer API](#developer-api)
10. [Data Persistence](#data-persistence)
11. [Best Practices](#best-practices)
12. [Troubleshooting](#troubleshooting)

---

## Overview

The Achievement System provides 24 achievements across 5 categories with 5 difficulty tiers. Players unlock achievements through various in-game activities and receive monetary rewards. Progress is tracked automatically every 60 seconds and synchronized between server and client in real-time.

### Key Features

- **24 Achievements** in 5 categories (Economy, Crime, Production, Social, Exploration)
- **5 Difficulty Tiers** with rewards from 100 EUR to 50,000 EUR
- **Automatic Tracking** - checked every 60 seconds for balance/crime/plot conditions
- **Manual Trigger Points** - event-driven tracking for actions like sales, loan repayments
- **Hidden Achievements** - some achievements hide their requirements until you are close to unlocking
- **Smartphone Integration** - dedicated Achievements app with progress visualization
- **Thread-Safe** - ConcurrentHashMap-based data store
- **Developer API** - full `IAchievementAPI` for external mod integration

### Statistics

| Metric | Value |
|--------|-------|
| Total Achievements | 24 |
| Economy Achievements | 11 |
| Crime Achievements | 6 |
| Production Achievements | 5 |
| Social Achievements | 4 |
| Difficulty Tiers | 5 (Bronze to Platinum) |
| Maximum Single Reward | 50,000 EUR (Platinum) |
| Maximum Total Rewards | ~200,000+ EUR (all unlocked) |

---

## Architecture

```
AchievementManager
  ├── AchievementTracker         -- Periodic 60-second tracker (balance, crime, plots)
  ├── Achievement                -- Achievement data class (id, name, category, tier, requirement)
  ├── AchievementCategory        -- Enum: ECONOMY, CRIME, PRODUCTION, SOCIAL, EXPLORATION
  ├── AchievementTier            -- Enum: BRONZE, SILVER, GOLD, DIAMOND, PLATINUM
  ├── PlayerAchievements         -- Per-player state (unlocked set, progress map, timestamps)
  └── network/
        ├── AchievementNetworkHandler  -- Server-client sync coordinator
        ├── RequestAchievementDataPacket  -- Client → Server data request
        └── SyncAchievementDataPacket     -- Server → Client full sync
```

### Data Flow

```
Player Action
     │
     ▼
AchievementManager.addProgress() / unlockAchievement()
     │
     ├── Check if requirement met
     │        │ Yes
     │        ▼
     │   Award monetary reward (EconomyManager.deposit)
     │   Mark as unlocked with timestamp
     │   Send unlock notification to player
     │        │
     ▼        ▼
PlayerAchievements updated
     │
     ▼
AchievementNetworkHandler.sync()
     │
     ▼
SyncAchievementDataPacket → Client
     │
     ▼
ClientAchievementCache updated
```

---

## Achievement Categories

| Category | Icon | Description |
|----------|------|-------------|
| `ECONOMY` | 💰 | Financial milestones - earning money, loans, savings, trading |
| `CRIME` | 🚔 | Criminal activities - wanted levels, escapes, prison time |
| `PRODUCTION` | 🏭 | Production and crafting - harvests, quantities, production sites |
| `SOCIAL` | 🏠 | Social gameplay - plots, rental income, player ratings |
| `EXPLORATION` | 🗺️ | Exploration and discovery - world exploration milestones |

---

## Difficulty Tiers and Rewards

| Tier | Reward | Description |
|------|--------|-------------|
| **BRONZE** | 100 EUR | Entry-level, easy to achieve |
| **SILVER** | 500 EUR | Moderate effort required |
| **GOLD** | 2,000 EUR | Significant gameplay investment |
| **DIAMOND** | 10,000 EUR | Long-term commitment required |
| **PLATINUM** | 50,000 EUR | Endgame prestige achievement |

Rewards are automatically deposited into the player's bank account when an achievement is unlocked.

---

## Complete Achievement List

### Economy Achievements (11)

| Achievement ID | Name | Requirement | Tier | Reward |
|---------------|------|-------------|------|--------|
| `FIRST_EURO` | Erstes Geld | 1 EUR account balance | Bronze | 100 EUR |
| `RICH` | Reich | 10,000 EUR account balance | Bronze | 100 EUR |
| `WEALTHY` | Wohlhabend | 100,000 EUR account balance | Silver | 500 EUR |
| `MILLIONAIRE` | Millionär | 1,000,000 EUR account balance | Gold | 2,000 EUR |
| `LOAN_MASTER` | Kredit-Meister | Repay 10 loans | Silver | 500 EUR |
| `SAVINGS_KING` | Sparkönig | 100,000 EUR savings balance | Gold | 2,000 EUR |
| `BIG_SPENDER` | Großzügig | Spend 1,000,000 EUR total | Diamond | 10,000 EUR |
| `FIRST_TRADE` | Erster Handel | Complete first stock trade | Bronze | 100 EUR |
| `FIRST_PROFIT` | Erster Gewinn | First profitable trade | Bronze | 100 EUR |
| `FIRST_LOSS` | Erster Verlust | First loss-making trade | Bronze | 100 EUR |
| `PROFIT_MASTER` | Handels-Meister | Cumulative profit milestones | Bronze–Diamond | 100–10,000 EUR |

**Tracking:** Balance-based achievements are checked every 60 seconds. Loan repayments, trading milestones, and spending amounts are tracked at the point of action.

---

### Crime Achievements (6)

| Achievement ID | Name | Requirement | Tier | Reward |
|---------------|------|-------------|------|--------|
| `FIRST_CRIME` | Erster Regelverstoß | Obtain any wanted level | Bronze | 100 EUR |
| `WANTED` | Gesucht | Reach wanted level 3 or higher | Silver | 500 EUR |
| `MOST_WANTED` | Meistgesucht | Reach wanted level 5 (maximum) | Gold | 2,000 EUR |
| `ESCAPE_ARTIST` | Flucht-Künstler | Escape from police 10 times | Silver | 500 EUR |
| `PRISON_VETERAN` | Gefängnisprofi | Accumulate 100 days in prison | Gold | 2,000 EUR |
| `CLEAN_RECORD` | Weißeste Weste | Go 30 consecutive days without any crime | Diamond | 10,000 EUR |

**Note:** `CLEAN_RECORD` and `MOST_WANTED` are mutually exclusive gameplay paths. Pursuing both requires careful timing.

---

### Production Achievements (5)

| Achievement ID | Name | Requirement | Tier | Reward |
|---------------|------|-------------|------|--------|
| `HOBBYIST` | Hobbygärtner | Harvest 100 plants | Bronze | 100 EUR |
| `FARMER` | Bauer | Produce 100 kg of items total | Silver | 500 EUR |
| `PRODUCER` | Produzent | Produce 1,000 kg of items total | Gold | 2,000 EUR |
| `DRUG_LORD` | Drogenbaron | Produce 10,000 kg of items total | Diamond | 10,000 EUR |
| `EMPIRE_BUILDER` | Imperium-Erbauer | Own 10 active production sites simultaneously | Platinum | 50,000 EUR |

**Note:** `EMPIRE_BUILDER` is the most prestigious achievement in the game, requiring ownership of 10 simultaneous production operations.

---

### Social Achievements (4)

| Achievement ID | Name | Requirement | Tier | Reward |
|---------------|------|-------------|------|--------|
| `FIRST_PLOT` | Erster Besitz | Purchase your first plot | Bronze | 100 EUR |
| `PROPERTY_MOGUL` | Immobilien-Mogul | Own 5 or more plots simultaneously | Gold | 2,000 EUR |
| `LANDLORD` | Vermieter | Earn 100,000 EUR total from rent | Diamond | 10,000 EUR |
| `POPULAR` | Beliebt | Receive 50 positive plot ratings | Gold | 2,000 EUR |

---

## Automatic Tracking System

The `AchievementTracker` runs every **60 seconds** and checks conditions for:

### Periodic Checks (60-second interval)

| Check | Achievements Evaluated |
|-------|----------------------|
| Account balance | `FIRST_EURO`, `RICH`, `WEALTHY`, `MILLIONAIRE` |
| Wanted level | `FIRST_CRIME`, `WANTED`, `MOST_WANTED` |
| Owned plot count | `FIRST_PLOT`, `PROPERTY_MOGUL` |

### Event-Driven Triggers

These are tracked at the moment the action occurs:

| Trigger | Achievements |
|---------|-------------|
| Loan repaid | `LOAN_MASTER` progress |
| Savings deposit/balance update | `SAVINGS_KING` |
| Money spent (any transaction) | `BIG_SPENDER` progress |
| Police escape | `ESCAPE_ARTIST` progress |
| Day in prison | `PRISON_VETERAN` progress |
| Day without crime | `CLEAN_RECORD` progress |
| Plant harvested | `HOBBYIST` progress |
| Items produced (kg) | `FARMER`, `PRODUCER`, `DRUG_LORD` progress |
| Production site added | `EMPIRE_BUILDER` check |
| Plot rental income received | `LANDLORD` progress |
| Positive plot rating received | `POPULAR` progress |
| First stock trade | `FIRST_TRADE` |
| First profitable trade | `FIRST_PROFIT` |
| First loss trade | `FIRST_LOSS` |

---

## Smartphone App

Players can view their achievement progress through the **Achievements App** on the smartphone (press `P` to open).

### App Features

- **Category Tabs** - Filter by Economy, Crime, Production, Social, Exploration
- **Progress Bars** - Visual progress display for each achievement
- **Lock/Unlock Indicator** - Clear visual distinction between completed and pending
- **Reward Preview** - Shows EUR reward for each achievement
- **Summary Stats** - Total unlocked, completion percentage, total EUR earned from achievements
- **Hidden Achievement Placeholders** - Shows "???" until you approach the threshold

### Unlock Notification

When an achievement is unlocked, the player receives:
- An in-game system message showing the achievement name and tier
- A toast notification on screen
- A smartphone notification badge
- The reward is silently deposited and logged in transaction history

---

## Network Synchronization

| Packet | Direction | Trigger | Payload |
|--------|-----------|---------|---------|
| `RequestAchievementDataPacket` | Client → Server | Player opens Achievements app | Empty |
| `SyncAchievementDataPacket` | Server → Client | Response to request, or on unlock | All achievement state |

### Client Cache

`ClientAchievementCache` maintains a client-side copy of all achievement data, avoiding repeated server requests while the player has the app open. Cache is invalidated and re-synced on:
- Achievement unlock
- Player login
- Explicit request via the Achievements app

---

## Developer API

The `IAchievementAPI` provides full external mod access to the achievement system.

**Access:**
```java
IAchievementAPI achievementAPI = ScheduleMCAPI.getInstance().getAchievementAPI();
```

### Method Reference

---

#### `getPlayerAchievements(UUID playerUUID)` → `PlayerAchievements`

Returns the full achievement state object for a player. Creates it if it doesn't exist yet.

```java
PlayerAchievements pa = achievementAPI.getPlayerAchievements(playerUUID);
int unlockedCount = pa.getUnlockedCount();
```

---

#### `addProgress(UUID playerUUID, String achievementId, double amount)` → `void`

Adds incremental progress towards an achievement. Auto-unlocks and pays reward if requirement is met.

```java
// Record 10 kg of production
achievementAPI.addProgress(playerUUID, "FARMER", 10.0);
```

---

#### `setProgress(UUID playerUUID, String achievementId, double value)` → `void`

Sets absolute progress. Useful for syncing balance-based achievements.

```java
// Sync current balance for MILLIONAIRE
achievementAPI.setProgress(playerUUID, "MILLIONAIRE", currentBalance);
```

---

#### `unlockAchievement(UUID playerUUID, String achievementId)` → `boolean`

Manually unlock an achievement and pay the reward. Returns `false` if already unlocked.

```java
boolean wasNew = achievementAPI.unlockAchievement(playerUUID, "FIRST_TRADE");
```

---

#### `isUnlocked(UUID playerUUID, String achievementId)` → `boolean`

Check if a specific achievement is unlocked.

```java
if (!achievementAPI.isUnlocked(playerUUID, "EMPIRE_BUILDER")) {
    // Player hasn't reached endgame yet
}
```

---

#### `getProgress(UUID playerUUID, String achievementId)` → `double`

Get current progress towards an achievement.

```java
double progress = achievementAPI.getProgress(playerUUID, "DRUG_LORD");
double required = 10000.0; // 10,000 kg
double pct = (progress / required) * 100;
```

---

#### `getAchievement(String achievementId)` → `Achievement` (nullable)

Look up an achievement's metadata by ID.

```java
Achievement a = achievementAPI.getAchievement("MILLIONAIRE");
if (a != null) {
    AchievementTier tier = a.getTier();
    double reward = tier.getReward();
}
```

---

#### `getAllAchievements()` → `Collection<Achievement>`

Get all 24 registered achievements.

```java
for (Achievement a : achievementAPI.getAllAchievements()) {
    System.out.println(a.getId() + ": " + a.getName());
}
```

---

#### `getAchievementsByCategory(AchievementCategory category)` → `List<Achievement>`

Get all achievements for a specific category.

```java
List<Achievement> crimeAch = achievementAPI.getAchievementsByCategory(
    AchievementCategory.CRIME
);
```

---

#### `getStatistics(UUID playerUUID)` → `String`

Returns a formatted statistics string for display.

```java
String stats = achievementAPI.getStatistics(playerUUID);
// Output: "Achievements: 14/24 (58%) - €87,600 verdient"
```

---

#### `getCompletionPercentage(UUID playerUUID)` → `double` *(since 3.2.0)*

Returns completion percentage from 0.0 to 100.0.

```java
double pct = achievementAPI.getCompletionPercentage(playerUUID);
```

---

#### `getTotalRewardsEarned(UUID playerUUID)` → `double` *(since 3.2.0)*

Returns total EUR earned from achievements.

```java
double totalEarned = achievementAPI.getTotalRewardsEarned(playerUUID);
```

---

#### `getUnlockedAchievements(UUID playerUUID)` → `List<Achievement>` *(since 3.2.0)*

Get the list of all achievements a player has unlocked.

```java
List<Achievement> unlocked = achievementAPI.getUnlockedAchievements(playerUUID);
```

---

#### `resetPlayerAchievements(UUID playerUUID)` → `void` *(since 3.2.0)*

Reset all achievement progress for a player (admin/debug use).

```java
achievementAPI.resetPlayerAchievements(playerUUID);
```

---

#### `getTopAchievers(int limit)` → `List<Map.Entry<UUID, Integer>>` *(since 3.2.0)*

Returns the top N players sorted by achievement count (descending).

```java
List<Map.Entry<UUID, Integer>> top10 = achievementAPI.getTopAchievers(10);
for (Map.Entry<UUID, Integer> entry : top10) {
    UUID player = entry.getKey();
    int count = entry.getValue();
    System.out.println(player + ": " + count + " achievements");
}
```

---

#### `getUnlockedCount(UUID playerUUID)` → `int`

Returns the number of unlocked achievements for a player.

---

#### `getTotalAchievementCount()` → `int`

Returns the total number of registered achievements (currently 24).

---

### Integration Example

```java
// Award a custom achievement when a player completes a special event
@SubscribeEvent
public void onSpecialEventComplete(SpecialEventCompleteEvent event) {
    IAchievementAPI achievements = ScheduleMCAPI.getInstance().getAchievementAPI();
    UUID playerUUID = event.getPlayer().getUUID();

    // Check if not already done this milestone
    if (!achievements.isUnlocked(playerUUID, "EMPIRE_BUILDER")) {
        // Update production site count
        int siteCount = getPlayerProductionSiteCount(playerUUID);
        achievements.setProgress(playerUUID, "EMPIRE_BUILDER", siteCount);
    }
}
```

---

## Data Persistence

**File:** `config/plotmod_achievements.json`

Data saved per player:

| Field | Description |
|-------|-------------|
| `playerUUID` | Player unique identifier |
| `unlockedAchievements` | Set of unlocked achievement IDs |
| `progress` | Map of achievement ID → current progress value |
| `unlockTimestamps` | Map of achievement ID → unlock time (epoch ms) |
| `totalRewardsEarned` | Cumulative EUR earned from achievements |

### Save/Load

- Data is saved automatically on server shutdown and periodically by `IncrementalSaveManager`
- On player login, data is loaded and injected into the tracker
- GSON deserialization with null-safety guards against corrupt entries

---

## Best Practices

### For Server Administrators

1. **Don't reset achievements lightly** - Players value their progress. Use `/achievement reset` only for testing or at player request.
2. **Reward balance** - The default reward tier values (Bronze 100 EUR to Platinum 50,000 EUR) are designed to be meaningful but not game-breaking relative to the starting balance of 1,000 EUR.
3. **EMPIRE_BUILDER** - This achievement requires 10 active production sites. On small servers, consider whether this is achievable. You can grant it manually with the API if needed.

### For Mod Developers

1. **Track incrementally** - Use `addProgress()` rather than `setProgress()` for actions that accumulate over time (e.g., harvests, sales).
2. **Use `setProgress()` for balance syncs** - When syncing account balance to `MILLIONAIRE`, use `setProgress()` since the balance can go up or down.
3. **Check before awarding** - Use `isUnlocked()` before calling `unlockAchievement()` to avoid redundant calls.
4. **Thread safety** - All API methods are safe to call from any thread, including networking threads.

---

## Troubleshooting

### Achievement not unlocking

1. **Check the tracker interval** - The 60-second periodic tracker may not have run yet. Wait 60 seconds after meeting the condition.
2. **Check if already unlocked** - Use `achievementAPI.isUnlocked(playerUUID, id)` to verify.
3. **Check log output** - Enable `DEBUG` logging for `de.rolandsw.schedulemc.achievement` to see tracker activity.

### Rewards not received

1. **Verify bank account exists** - If the player joined but the economy system failed to initialize their account, rewards cannot be deposited.
2. **Check transaction history** - The reward deposit creates a `ACHIEVEMENT_REWARD` transaction type entry.

### Data lost after restart

1. **Check file integrity** - Open `config/plotmod_achievements.json` to verify it is valid JSON.
2. **Check permissions** - The server must have write access to the `config/` directory.
3. **Backup system** - The `BackupManager` creates periodic backups in `config/backups/`. Restore from backup if needed.

### Client not showing updated achievements

1. **Re-open the app** - The Achievements app requests fresh data from the server when opened.
2. **Reconnect** - Achievement data is synced on join. A reconnect triggers a fresh sync.

---

## Dokumentationsstatus

- Zuletzt gegen den aktuellen Repository-Stand abgeglichen am **2026-04-13**.
- Diese Datei wurde im Rahmen der Vollständigkeits-Aktualisierung überarbeitet.
- Referenz für Live-Metriken: `docs/REPO_METRICS.md` (neu generiert).

