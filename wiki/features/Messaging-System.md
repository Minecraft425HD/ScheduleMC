# Messaging-System (Nachrichten-System)

<div align="center">

**Player-to-Player and Player-to-NPC Communication**

Persistent messaging with real-time notifications, NPC reputation responses, and block/unblock functionality

[Back to Wiki Home](../Home.md) | [Commands Reference](../Commands.md)

</div>

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Player-to-Player Messaging](#player-to-player-messaging)
4. [Player-to-NPC Messaging](#player-to-npc-messaging)
5. [Real-Time Notifications](#real-time-notifications)
6. [Block and Unblock](#block-and-unblock)
7. [Smartphone Integration](#smartphone-integration)
8. [Message Limits and Constraints](#message-limits-and-constraints)
9. [Network Packets](#network-packets)
10. [Developer API](#developer-api)
11. [Data Persistence](#data-persistence)
12. [Best Practices](#best-practices)
13. [Troubleshooting](#troubleshooting)

---

## Overview

The Messaging System enables persistent, real-time communication between players and between players and NPCs. Messages survive server restarts, and offline players receive their messages on next login. NPC responses are reputation-dependent, creating dynamic social interactions based on player behavior.

### Key Features

- **Player-to-Player Messaging** - Direct messages with full conversation history
- **Offline Message Delivery** - Messages stored and delivered on next login
- **Player-to-NPC Messaging** - Context-aware NPC responses based on reputation
- **Real-Time Notifications** - On-screen pop-up with sender name and preview
- **Block/Unblock System** - Prevent specific players from messaging you *(since 3.2.0)*
- **System Messages** - Admin-sent system notifications without a sender *(since 3.2.0)*
- **Broadcast Messages** - Send a message to all online players *(since 3.2.0)*
- **Thread-Safe** - ConcurrentHashMap-based data store
- **Smartphone Integration** - Messages App and Contacts App

### Statistics

| Metric | Value |
|--------|-------|
| Max message length | 500 characters |
| Max records per player | 10,000 entries |
| Max packet size | 1,024 characters |
| Max participant name length | 100 characters |
| Notification display time | 3 seconds |
| Notification preview length | 40 characters |

---

## Architecture

```
MessagingManager
  ├── MessageData              -- Conversation record (participants, history, reputation)
  ├── MessagingNetworkHandler  -- Server-client packet coordinator
  └── network/
        ├── SendMessagePacket     -- Client → Server (outgoing message)
        └── ReceiveMessagePacket  -- Server → Client (incoming message + notification)
```

### Data Flow

```
Player sends message (Smartphone Messages App)
     │
     ▼
SendMessagePacket → Server
     │
     ▼
MessagingManager.sendMessage(fromUUID, toUUID, text)
     │
     ├── Validate: not blocked, valid UUIDs, text not empty, within size limits
     │
     ├── Is recipient online?
     │     │ Yes ──► ReceiveMessagePacket → recipient client (triggers notification)
     │     │ No  ──► Message stored for offline delivery
     │
     └── Persist to MessageData storage
```

---

## Player-to-Player Messaging

### Sending a Message

Messages are sent through the **Messages App** on the smartphone (press `P`):

1. Open the smartphone (default key: `P`)
2. Navigate to **Messages** app
3. Tap an existing conversation or open **Contacts** to start a new one
4. Type your message and press Send

Alternatively, players can message each other through the **Contacts** app by selecting a player and tapping "Message".

### Message History

- Full conversation history is stored per player pair
- Messages are sorted by timestamp (newest last in the conversation view)
- History persists across server restarts and player disconnects

### Offline Delivery

If the recipient is offline when a message is sent:
- The message is stored in the recipient's `MessageData`
- On next login, any unread messages trigger a notification count in the Smartphone HUD
- The player can open the Messages app to read them

---

## Player-to-NPC Messaging

Players can message NPCs through the **Messages** or **Contacts** app. NPC responses are dynamically generated based on the player's reputation with that NPC.

### Reputation-Based Responses

| Reputation Range | Mood | Response Type |
|-----------------|------|---------------|
| 0 – 33 | Hostile / Unfriendly | Short, dismissive responses |
| 34 – 67 | Neutral | Polite but brief replies |
| 68 – 100 | Friendly | Warm, helpful responses |

Each mood has **3 response variants** that are randomly selected to add variety. Responses are localized (German/English based on server locale settings).

### NPC Relationship Tracking

The NPC's reputation with a player (tracked by `RelationshipSystem`) affects:
- Message response tone
- Shop prices (+/- relationship bonus)
- Quest availability

Sending positive messages and completing quests raises reputation. Criminal activity near the NPC lowers it.

---

## Real-Time Notifications

When an online player receives a message, a pop-up notification appears at the top of the screen:

```
┌─────────────────────────────────┐
│  📱 Max Mustermann              │
│  "Hey, kannst du mir helfen?"   │
└─────────────────────────────────┘
```

### Notification Timing

| Phase | Duration |
|-------|----------|
| Fade-in animation | 300 ms |
| Display time | 3,000 ms |
| Fade-out animation | 300 ms |
| **Total visible** | **~3.6 seconds** |

The notification shows:
- Sender's in-game name
- Message preview (up to 40 characters, truncated with "..." if longer)

### HUD Badge

The Smartphone HUD shows an unread count badge when there are unread messages. The badge updates in real-time as messages arrive.

---

## Block and Unblock

*(Available since API version 3.2.0)*

Players can block other players from sending them messages.

### Behavior When Blocked

- Blocked player's `sendMessage()` returns `false` (silently)
- No error is shown to the blocked player (prevents confirmation that block exists)
- The blocking player does not receive the message
- Block status is persisted across restarts

### API Methods

```java
IMessagingAPI messaging = ScheduleMCAPI.getInstance().getMessagingAPI();

// Block a player
messaging.blockPlayer(myUUID, annoyingPlayerUUID);

// Check block status
boolean isBlocked = messaging.isBlocked(myUUID, annoyingPlayerUUID);

// Unblock
messaging.unblockPlayer(myUUID, annoyingPlayerUUID);
```

---

## Smartphone Integration

### Messages App

The **Messages App** in the Smartphone shows:
- All conversations sorted by **last message timestamp** (most recent first)
- Per-conversation message preview (last message, truncated)
- Unread indicator (bold conversation title + count badge)
- Visual distinction between player conversations (👤) and NPC conversations (🤖)
- Full conversation thread on tap

### Contacts App

The **Contacts App** shows:
- All known players (players you have exchanged messages with)
- All NPCs on the server
- Contact details including last interaction time
- Direct "Message" button to open a conversation

---

## Message Limits and Constraints

| Parameter | Value | Description |
|-----------|-------|-------------|
| Max message length | 500 chars | Hard limit enforced server-side |
| Max network packet size | 1,024 chars | Packet field size limit |
| Max participant name length | 100 chars | For NPC and player names |
| Max stored records | 10,000 entries | Per player, oldest removed on overflow |
| Notification preview | 40 chars | Truncated with "..." if longer |

### Bandwidth Optimization

- Recipient player name is only included in NPC-to-player messages
- For player-to-player messages, the server resolves names from the `GameProfile` cache server-side
- This reduces packet payload by ~30 bytes per message

---

## Network Packets

| Packet | Direction | Description | Key Fields |
|--------|-----------|-------------|------------|
| `SendMessagePacket` | Client → Server | Player sends a message | `toUUID`, `message` |
| `ReceiveMessagePacket` | Server → Client | Deliver incoming message | `fromUUID`, `fromName`, `message`, `isNPC` |

### Packet Validation

All incoming `SendMessagePacket` data is validated server-side:
- UUID must resolve to an existing player or NPC
- Message must not be empty and must be ≤ 500 characters
- Sender must not be blocked by recipient

---

## Developer API

Access the Messaging API through the ScheduleMC singleton:

```java
IMessagingAPI messaging = ScheduleMCAPI.getInstance().getMessagingAPI();
```

### Core Methods

---

#### `sendMessage(UUID fromUUID, UUID toUUID, String message)` → `boolean`

Send a direct message from one player to another. Returns `false` if the recipient has blocked the sender.

```java
boolean sent = messaging.sendMessage(senderUUID, recipientUUID, "Hello!");
if (!sent) {
    // Recipient may have blocked the sender
    sender.sendSystemMessage(Component.literal("Message could not be delivered."));
}
```

**Throws:** `IllegalArgumentException` if any parameter is `null` or `message` is empty.

---

#### `getUnreadMessageCount(UUID playerUUID)` → `int`

Get the number of unread messages for a player. Useful for displaying a badge or notification.

```java
int unread = messaging.getUnreadMessageCount(playerUUID);
if (unread > 0) {
    player.sendSystemMessage(Component.literal("You have " + unread + " unread messages!"));
}
```

---

#### `getMessages(UUID playerUUID, int limit)` → `List<String>`

Retrieve the most recent N messages for a player. Returns messages newest-first.

```java
List<String> recent = messaging.getMessages(playerUUID, 10);
for (String msg : recent) {
    player.sendSystemMessage(Component.literal(msg));
}
```

---

#### `markAllAsRead(UUID playerUUID)` → `void`

Mark all messages as read. Called automatically when the player opens the Messages app.

```java
messaging.markAllAsRead(playerUUID);
```

---

#### `deleteMessage(UUID playerUUID, String messageId)` → `boolean`

Delete a specific message by ID.

```java
boolean deleted = messaging.deleteMessage(playerUUID, messageId);
```

---

#### `deleteAllMessages(UUID playerUUID)` → `void`

Delete all messages for a player.

```java
messaging.deleteAllMessages(playerUUID);
```

---

#### `getTotalMessageCount(UUID playerUUID)` → `int`

Get total number of messages stored for a player.

---

#### `broadcastMessage(UUID fromUUID, String message)` → `int` *(since 3.2.0)*

Send a message to all currently online players. Returns the count of players who received it.

```java
// System-wide announcement from admin
int delivered = messaging.broadcastMessage(adminUUID, "Server restarts in 5 minutes!");
```

---

#### `sendSystemMessage(UUID toUUID, String message)` → `boolean` *(since 3.2.0)*

Send a system notification without a sender UUID. Appears as a system message in the Messages app.

```java
// Notification from a custom system event
messaging.sendSystemMessage(playerUUID, "Your production batch is ready!");
```

---

#### `getConversation(UUID playerA, UUID playerB, int limit)` → `List<String>` *(since 3.2.0)*

Get the conversation history between two specific players.

```java
List<String> history = messaging.getConversation(playerA, playerB, 20);
```

---

#### `isBlocked(UUID playerUUID, UUID blockedUUID)` → `boolean` *(since 3.2.0)*

Check if `playerUUID` has blocked `blockedUUID`.

---

#### `blockPlayer(UUID playerUUID, UUID blockedUUID)` → `void` *(since 3.2.0)*

Block a player from sending messages to `playerUUID`.

---

#### `unblockPlayer(UUID playerUUID, UUID blockedUUID)` → `void` *(since 3.2.0)*

Remove a block between two players.

---

### Complete Integration Example

```java
// Custom event handler: notify players when their shop makes a sale
@SubscribeEvent
public void onShopSale(ShopSaleEvent event) {
    IMessagingAPI messaging = ScheduleMCAPI.getInstance().getMessagingAPI();

    UUID shopOwner = event.getShopOwnerUUID();
    double amount = event.getSaleAmount();
    String itemName = event.getItemName();

    // Send a system notification to the shop owner
    messaging.sendSystemMessage(shopOwner,
        "Dein Shop hat " + itemName + " für " +
        String.format("%.2f", amount) + " EUR verkauft!"
    );
}
```

---

## Data Persistence

**File:** `config/plotmod_messages.json`

### Stored Data per Conversation

| Field | Type | Description |
|-------|------|-------------|
| `participantUUID` | UUID | The other participant |
| `participantName` | String | Display name (cached at send time) |
| `isNPC` | boolean | Whether the participant is an NPC |
| `messages` | List | Ordered message entries |
| `reputation` | int | NPC reputation (0–100, NPC conversations only) |

### Message Entry Fields

| Field | Description |
|-------|-------------|
| `id` | Unique message identifier |
| `fromUUID` | Sender UUID |
| `text` | Message content |
| `timestamp` | Epoch milliseconds |
| `read` | Whether recipient has read it |

### Data Validation

On load, GSON deserialization applies:
- UUID format validation with error recovery
- String length enforcement (message ≤ 500, name ≤ 100)
- Reputation clamping to valid range (0–100)
- Corruption protection: max 10,000 records enforced; oldest removed if exceeded
- Null-safe: corrupted individual entries are skipped, not propagated

---

## Best Practices

### For Server Administrators

1. **Monitor message storage size** - With many active players, `plotmod_messages.json` can grow large. The 10,000-record limit per player prevents unbounded growth.
2. **System messages for announcements** - Use `sendSystemMessage()` (via API) to send in-game notifications to players without using the chat system.
3. **NPC reputation** - NPC message responses depend on player reputation. If NPCs always respond negatively, check if player reputation is stuck low (use `/npc <name> reputation <player> <value>` to reset).

### For Mod Developers

1. **Use `sendSystemMessage()` over `sendMessage()`** - For automated notifications (production complete, auction won, etc.), use the system message variant to avoid flooding player-to-player conversation histories.
2. **Check block status before sending** - If implementing a custom notification system, call `isBlocked()` first if messages should respect user preferences.
3. **Respect message limits** - Trim your messages to 500 characters before calling `sendMessage()` to avoid validation failures.
4. **Thread safety** - All messaging API calls are safe from any thread.

---

## Troubleshooting

### Messages not delivered

1. **Recipient is blocked** - `sendMessage()` returns `false` silently when blocked. Check `isBlocked()`.
2. **Invalid UUID** - Ensure both UUIDs are valid and non-null. Invalid UUIDs throw `IllegalArgumentException`.
3. **Message too long** - Messages over 500 characters are rejected. Trim your message text.

### Notifications not appearing

1. **Client-side HUD** - The notification overlay renders in `MessagingNotificationRenderer`. If it's not showing, check that the client has connected to the server and received packets.
2. **Player has GUI open** - Notifications may not render while certain fullscreen GUIs are open.

### NPC not responding

1. **Reputation check** - Verify the player has a reputation value with the NPC (`/npc <name> info` shows relationship data).
2. **NPC type** - Only NPCs with `RESIDENT` or `MERCHANT` types respond to messages. `POLICE` NPCs do not engage in messaging.

### Data file size growing large

1. **Automatic trimming** - The 10,000-entry limit should prevent runaway growth.
2. **Manual cleanup** - The `/admin purge messages` command (if enabled) can delete old conversation data for inactive players.
3. **Backup recovery** - If the file is corrupted, restore from `config/backups/plotmod_messages_<timestamp>.json`.
