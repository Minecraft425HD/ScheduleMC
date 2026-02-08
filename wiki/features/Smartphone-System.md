# Smartphone System

<div align="center">

**11 Functional Apps with PvP Protection**

Your pocket-sized ScheduleMC companion

[Back to Wiki Home](../Home.md) | [Commands Reference](../Commands.md)

</div>

---

## Table of Contents

1. [Overview](#overview)
2. [Opening the Smartphone](#opening-the-smartphone)
3. [Available Apps](#available-apps)
4. [PvP Protection](#pvp-protection)
5. [Network Sync](#network-sync)
6. [Extensible App Framework](#extensible-app-framework)
7. [Developer API](#developer-api)
8. [Best Practices](#best-practices)
9. [Troubleshooting](#troubleshooting)

---

## Overview

The Smartphone System provides a professional in-game GUI interface for accessing ScheduleMC features. It includes 11 built-in apps, full PvP protection while in use, network synchronization, and an extensible app framework for custom additions.

### Key Features

- **11 Built-in Apps** - MAP, DEALER, PRODUCTS, ORDER, CONTACTS, MESSAGES, PLOT, SETTINGS, BANK, CRIME STATS, CHAT
- **Default Keybind: P** - Configurable through Minecraft controls
- **PvP Protection** - Damage immunity while smartphone is open
- **Attacker Penalty** - Anyone who attacks a smartphone user gets +1 wanted star
- **Professional GUI** - Clean, intuitive interface design
- **Network Sync** - Real-time data synchronization between client and server
- **Extensible Framework** - Register custom apps via the API

---

## Opening the Smartphone

### Default Keybind

**Key:** `P` (default, configurable)

**Action:**
1. Press `P` anywhere in-game
2. Smartphone GUI opens immediately
3. Home screen displays all 11 apps

**Configuring Keybind:**
```
Options -> Controls -> Key Binds -> ScheduleMC
  -> Open Smartphone: [P]
  -> Change to preferred key
  -> Done
```

### Smartphone Home Screen

```
+-----------------------------------+
|         SMARTPHONE                |
+-----------------------------------+
|                                   |
|  MAP          DEALER              |
|  PRODUCTS     ORDER               |
|  CONTACTS     MESSAGES            |
|  PLOT         SETTINGS            |
|  BANK         CRIME STATS         |
|  CHAT                             |
|                                   |
|  Protected: PvP Immune            |
+-----------------------------------+
```

---

## Available Apps

### 1. MAP

**Purpose:** View all plot locations on server

**Features:**
- Shows all plots on a navigable map
- Color-coded by type (Residential, Commercial, Shop, Public, Government)
- Clickable plot markers for details
- Your current location indicator
- Distance calculator to selected plots

---

### 2. DEALER

**Purpose:** Find tobacco dealers

**Features:**
- List of all tobacco dealers with locations
- Available strains and prices
- Stock levels
- Distance from your current position
- Dealer open/closed status based on schedule

---

### 3. PRODUCTS

**Purpose:** Browse shop catalogs

**Features:**
- All shop inventories across the server
- Product categories and search function
- Prices and quality tiers
- Stock availability
- Navigate to shop or add to cart

---

### 4. ORDER

**Purpose:** Order management system

**Features:**
- View active orders and delivery status
- Order history
- Track deliveries in real time
- Cancel orders
- Reorder from previous purchases

---

### 5. CONTACTS

**Purpose:** Player and NPC contact list

**Features:**
- Save player contacts
- NPC directory with locations
- Quick messaging shortcuts
- Online/offline status indicators
- Favorites list

---

### 6. MESSAGES

**Purpose:** Inbox and messaging system

**Features:**
- Receive and send private messages
- Conversation threads
- Read receipts
- Message history
- Compose new messages

---

### 7. PLOT

**Purpose:** Plot management interface

**Features:**
- View your owned plots
- Plot details (type, size, rating)
- Trust management (add/remove players)
- Apartment management (rent, tenants)
- Sell and transfer options

---

### 8. SETTINGS

**Purpose:** Smartphone configuration

**Features:**
- Notification preferences (messages, orders, plots, economy)
- Display settings (theme, font size, language)
- Privacy controls (location sharing, online status)
- Sound settings

---

### 9. BANK

**Purpose:** Mobile banking interface

**Features:**
- Check balance (checking and savings accounts)
- Transaction history
- Transfer money to other players
- View and manage loans
- Manage savings accounts with interest

**Banking Operations:**
```
Balance Check:
  Current Balance: 12,450
  Available: 12,450
  Pending: 0

Transfer Money:
  To: [Player name]
  Amount: [Amount]
  Fee: 1% (minimum 10)

Loan Status:
  Active Loan: MEDIUM
  Remaining: 14,375
  Daily Payment: 1,035.71
  Days Left: 14
```

---

### 10. CRIME STATS

**Purpose:** View wanted level and criminal record

**Features:**
- Current wanted level (0-5 stars)
- Active charges and crime history
- Bail amount and payment option
- Police proximity warning
- Total arrests, bail paid, days imprisoned

**Clean Record Example:**
```
CRIME STATISTICS

Wanted Level: None
Status: Clean Record
Police Status: Safe
Nearest Police: 250m

Criminal History:
  Total Arrests: 0
  Bail Paid: 0
  Days in Prison: 0
```

**Wanted Example:**
```
CRIME STATISTICS

Wanted Level: 3 stars
Status: WANTED

Crimes:
  Assault on NPC
  Trespassing (Government)

Bail Amount: 5,000
Options: [Pay Bail] [Surrender]

Police Status: CHASE ACTIVE
Nearest Police: 50m - APPROACHING
```

---

### 11. CHAT

**Purpose:** Direct messaging and group chat

**Features:**
- Private messages between players
- Group chats
- Share items and coordinates
- Chat history
- Block users

---

## PvP Protection

### Protection Mechanics

While the smartphone GUI is open, the player receives **complete damage immunity**:

**Protected from:**
- Player attacks
- NPC attacks
- Mob attacks
- Environmental damage
- Fall damage
- Fire and lava

**Restrictions while protected:**
- You cannot attack other players or entities
- You cannot deal any damage

### Attacker Penalty

If another player attacks someone who has their smartphone open:

```
Attacker hits smartphone user:
  -> Attack is blocked (no damage dealt)
  -> Attacker receives +1 wanted star
  -> Message: "Attacking smartphone users is illegal!"
  -> Police are now alerted to the attacker
```

This creates a strong deterrent against griefing players who are using the interface.

### Fair Play Enforcement

The system prevents abuse of smartphone protection:

1. **Cannot attack while open** - Weapons and attack actions are disabled
2. **3-second cooldown after closing** - Prevents rapid open/close combat exploits
3. **Movement allowed** - Players can still walk and run while using the smartphone

**Legitimate Uses:**
```
- Checking bank balance while traveling
- Reading messages during downtime
- Emergency bail payment during a chase
- Planning routes to safety
```

**Prevented Abuse:**
```
- Opening during active PvP to become invincible
- Rapidly toggling for hit-and-run attacks
- Baiting attacks to give opponents wanted stars
```

---

## Network Sync

The smartphone system uses **real-time network synchronization** between client and server to ensure all displayed data is current.

**Synchronized data includes:**
- Bank balances and transaction status
- Wanted level and police proximity
- Plot ownership and trust lists
- Message inbox updates
- Order delivery tracking
- Market prices and trends

All data displayed in smartphone apps reflects the current server state, not cached or outdated information.

---

## Extensible App Framework

The smartphone system supports **custom app registration** through the API, allowing other mods or plugins to add their own apps to the smartphone interface.

### How It Works

```
1. External mod registers app via ISmartphoneAPI.registerApp()
2. App appears on smartphone home screen
3. App can send notifications to players
4. App can be unregistered when no longer needed
```

### Custom App Registration

```java
ISmartphoneAPI api = ScheduleMCAPI.getSmartphoneAPI();

// Register a custom app
boolean success = api.registerApp("my_custom_app", "My App", "a"); // green color

// Send notifications through the app
api.sendNotification(playerUUID, "my_custom_app", "Something happened!");

// List all registered apps
Set<String> apps = api.getRegisteredApps();

// Unregister when done
api.unregisterApp("my_custom_app");
```

---

## Developer API

### ISmartphoneAPI Interface

External mods can access the smartphone system through the `ISmartphoneAPI` interface.

**Access:**
```java
ISmartphoneAPI smartphoneAPI = ScheduleMCAPI.getSmartphoneAPI();
```

### Core Methods (v3.0.0+)

| Method | Description |
|--------|-------------|
| `setSmartphoneOpen(UUID, boolean)` | Set whether a player has the smartphone open (activates/deactivates protection) |
| `hasSmartphoneOpen(UUID)` | Check if a player's smartphone is currently open |
| `removePlayer(UUID)` | Remove player from tracking (call on disconnect) |
| `getPlayersWithSmartphoneOpen()` | Get set of all UUIDs with smartphone open |
| `clearAllTracking()` | Clear all tracking data (server shutdown/tests only) |
| `getOpenSmartphoneCount()` | Count of players with smartphone currently open |

### Extended Methods (v3.2.0+)

| Method | Description |
|--------|-------------|
| `registerApp(String, String, String)` | Register a custom app (id, name, color) |
| `unregisterApp(String)` | Unregister a custom app by ID |
| `getRegisteredApps()` | Get set of all registered app IDs |
| `sendNotification(UUID, String, String)` | Send notification to player (playerUUID, appId, message) |
| `hasSmartphone(UUID)` | Check if player has the smartphone item |

### Example Usage

```java
ISmartphoneAPI smartphoneAPI = ScheduleMCAPI.getSmartphoneAPI();

// Open smartphone and activate protection
smartphoneAPI.setSmartphoneOpen(playerUUID, true);

// Check if smartphone is open (protection active)
if (smartphoneAPI.hasSmartphoneOpen(playerUUID)) {
    // Player is protected - do not deal damage
}

// Close smartphone and deactivate protection
smartphoneAPI.setSmartphoneOpen(playerUUID, false);

// Get all players currently using smartphones
Set<UUID> users = smartphoneAPI.getPlayersWithSmartphoneOpen();
int count = smartphoneAPI.getOpenSmartphoneCount();

// Register a custom app
smartphoneAPI.registerApp("auction_house", "Auction House", "6"); // gold color

// Send notification
smartphoneAPI.sendNotification(playerUUID, "auction_house", "Your item sold for 5,000!");

// Check if player has smartphone item
if (smartphoneAPI.hasSmartphone(playerUUID)) {
    // Player can use smartphone features
}

// Clean up on disconnect
smartphoneAPI.removePlayer(playerUUID);
```

**Thread Safety:** All methods are thread-safe through ConcurrentHashMap.newKeySet()-based tracking.

---

## Best Practices

### For Players

#### 1. Use for Safety

```
Dangerous Situation:
1. Press P to open smartphone
2. Become immune to damage
3. Check CRIME STATS for wanted level
4. Use BANK to pay bail if needed
5. Use MAP to navigate to safety
6. Close smartphone when safe
```

#### 2. Mobile Banking

```
Benefits of BANK app:
- Check balance anywhere without visiting ATM
- Transfer money on-the-go
- Pay bail during police chase
- Emergency transactions
- Monitor loan payments
```

#### 3. Quick Communication

```
Use CHAT and MESSAGES apps:
- Coordinate with friends for trades
- Negotiate prices before traveling to shops
- Emergency contact during pursuits
- Social interaction
```

#### 4. Market Research

```
Use PRODUCTS app before buying:
- Compare shop prices across the server
- Check stock before traveling
- Find best deals without walking around
- Plan purchases efficiently
```

### Security Tips

#### 1. Do Not Abuse Protection

```
Avoid:
- Opening smartphone to escape active PvP
- Using protection to grief other players
- Baiting attacks for wanted stars on attackers

Do:
- Use legitimately for features
- Close during fair PvP fights
- Report abuse to admins
```

#### 2. Privacy Settings

```
Configure in SETTINGS app:
- Limit location sharing (Friends Only / Everyone / Nobody)
- Control online status visibility
- Manage contact permissions
- Block unwanted users
```

---

## Troubleshooting

### Smartphone Will Not Open

**Causes:**
1. Keybind conflict with another mod or control
2. GUI scale issue
3. Mod compatibility conflict

**Solutions:**
```
1. Check keybind: Options -> Controls -> ScheduleMC
2. Adjust GUI scale: Options -> Video Settings
3. Remove conflicting mods
4. Restart the game client
```

### Apps Not Loading

**Causes:**
1. Server lag or connectivity issues
2. Data synchronization delay
3. Permission error

**Solutions:**
```
1. Close and reopen smartphone
2. Relog to the server
3. Contact admin if persistent
```

### Cannot Send Messages

**Causes:**
1. Recipient is offline
2. Blocked by recipient
3. Chat disabled in settings

**Solutions:**
```
1. Check recipient online status in CONTACTS
2. Verify you are not blocked
3. Enable chat in SETTINGS app
4. Check server chat configuration
```

### PvP Protection Not Working

**Causes:**
1. Smartphone GUI not fully loaded
2. Cooldown period active (3 seconds after last close)
3. Protection disabled by server admin

**Verification:**
```
Protection is active when:
- Smartphone GUI is fully visible
- Apps screen is showing
- No cooldown message displayed

Protection is NOT active:
- During GUI closing animation
- During 3-second cooldown after close
- If admin has disabled protection in config
```

---

<div align="center">

**Smartphone System - Complete Guide**

For related systems:
- [Economy System](Economy-System.md)
- [Plot System](Plot-System.md)
- [Police & Crime System](Police-Crime-System.md)
- [Market System](Market-System.md)

[Back to Wiki Home](../Home.md) | [All Commands](../Commands.md)

**Last Updated:** 2025-12-20 | **ScheduleMC v2.7.0-beta**

</div>
