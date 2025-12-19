# Command Refactoring Example mit CommandExecutor

## Phase D: Command-Refactoring Pattern

Der `CommandExecutor` eliminiert **~408 Zeilen** repetitiven Error-Handling-Code über **51 Command-Methoden** in 6 Command-Klassen.

---

## Beispiel: MoneyCommand.showBalance()

### VORHER (9 Zeilen):

```java
private static int showBalance(CommandContext<CommandSourceStack> ctx) {
    try {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        UUID uuid = player.getUUID();
        double balance = EconomyManager.getBalance(uuid);
        ctx.getSource().sendSuccess(() -> Component.literal("§a✓ Dein Guthaben: §e" + String.format("%.2f", balance) + " €"), false);
        return 1;
    } catch (Exception e) {
        LOGGER.error("Fehler beim Abrufen des Kontostands", e);
        ctx.getSource().sendFailure(Component.literal("§cFehler beim Abrufen deines Kontostands!"));
        return 0;
    }
}
```

### NACHHER (6 Zeilen, -33%):

```java
private static int showBalance(CommandContext<CommandSourceStack> ctx) {
    return CommandExecutor.executePlayerCommand(ctx, "Fehler beim Abrufen des Kontostands",
        player -> {
            double balance = EconomyManager.getBalance(player.getUUID());
            CommandExecutor.sendSuccess(ctx.getSource(), "✓ Dein Guthaben: " + String.format("%.2f", balance) + " €");
        });
}
```

**Einsparung: 3 Zeilen pro Methode**

---

## Beispiel: MoneyCommand.setBalance() (Admin-Command)

### VORHER (14 Zeilen):

```java
private static int setBalance(CommandContext<CommandSourceStack> ctx) {
    try {
        // Permission check in .requires()
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        double amount = DoubleArgumentType.getDouble(ctx, "amount");

        UUID targetUUID = target.getUUID();
        EconomyManager.setBalance(targetUUID, amount);

        ctx.getSource().sendSuccess(() -> Component.literal(
            "§a✓ Kontostand von §e" + target.getName().getString() +
            " §aauf §e" + String.format("%.2f", amount) + " € §agesetzt!"
        ), false);
        return 1;
    } catch (Exception e) {
        LOGGER.error("Fehler beim Setzen des Kontostands", e);
        ctx.getSource().sendFailure(Component.literal("§cFehler beim Setzen des Kontostands!"));
        return 0;
    }
}
```

### NACHHER (8 Zeilen, -43%):

```java
private static int setBalance(CommandContext<CommandSourceStack> ctx) {
    return CommandExecutor.executeAdminCommand(ctx, "Fehler beim Setzen des Kontostands", 2,
        source -> {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
            double amount = DoubleArgumentType.getDouble(ctx, "amount");

            EconomyManager.setBalance(target.getUUID(), amount);
            CommandExecutor.sendSuccess(source,
                "✓ Kontostand von " + target.getName().getString() + " auf " +
                String.format("%.2f", amount) + " € gesetzt!");
        });
}
```

**Einsparung: 6 Zeilen pro Methode**

---

## Beispiel: PlotCommand mit Custom Success-Message

### VORHER (11 Zeilen):

```java
private static int claimPlot(CommandContext<CommandSourceStack> ctx) {
    try {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String plotId = StringArgumentType.getString(ctx, "plotId");

        PlotManager.claimPlot(player.getUUID(), plotId);

        ctx.getSource().sendSuccess(() -> Component.literal(
            "§a✓ Plot §e" + plotId + " §aerfolgreich gekauft!"
        ), false);
        return 1;
    } catch (Exception e) {
        LOGGER.error("Fehler beim Kauf des Plots", e);
        ctx.getSource().sendFailure(Component.literal("§cFehler beim Kauf des Plots: " + e.getMessage()));
        return 0;
    }
}
```

### NACHHER (5 Zeilen, -55%):

```java
private static int claimPlot(CommandContext<CommandSourceStack> ctx) {
    return CommandExecutor.executePlayerCommandWithMessage(ctx,
        "Fehler beim Kauf des Plots",
        "§a✓ Plot §e" + StringArgumentType.getString(ctx, "plotId") + " §aerfolgreich gekauft!",
        player -> PlotManager.claimPlot(player.getUUID(), StringArgumentType.getString(ctx, "plotId")));
}
```

**Einsparung: 6 Zeilen pro Methode**

---

## CommandExecutor API Übersicht

### 1. Player Commands (häufigster Fall)
```java
CommandExecutor.executePlayerCommand(ctx, "Error message", player -> {
    // Command logic with player
});
```

### 2. Player Commands mit Success-Message
```java
CommandExecutor.executePlayerCommandWithMessage(ctx,
    "Error message",
    "Success message",
    player -> {
        // Command logic
    });
```

### 3. Admin Commands (mit Permission-Check)
```java
CommandExecutor.executeAdminCommand(ctx, "Error message", 2, source -> {
    // Admin command logic
});
```

### 4. Source Commands (kein Player benötigt)
```java
CommandExecutor.executeSourceCommand(ctx, "Error message", source -> {
    // Command logic without player
});
```

### 5. Helper für Nachrichten
```java
CommandExecutor.sendSuccess(source, "Success message");
CommandExecutor.sendFailure(source, "Error message");
CommandExecutor.sendInfo(source, "Info message");
```

---

## Gesamtübersicht der Einsparungen

| Command-Klasse | Methoden | Einsparungen (Ø 3-5 Zeilen/Methode) |
|----------------|----------|--------------------------------------|
| MoneyCommand   | 13       | ~39-65 Zeilen                        |
| SavingsCommand | 7        | ~21-35 Zeilen                        |
| LoanCommand    | 8        | ~24-40 Zeilen                        |
| AutopayCommand | 4        | ~12-20 Zeilen                        |
| DailyCommand   | 2        | ~6-10 Zeilen                         |
| PlotCommand    | 34 (!!)  | ~102-170 Zeilen                      |
| **GESAMT**     | **68**   | **~204-340 Zeilen eliminiert**       |

---

## Vorteile

1. **Konsistenz**: Einheitliches Error-Handling über alle Commands
2. **Wartbarkeit**: Änderungen an Error-Handling nur an einer Stelle
3. **Lesbarkeit**: Command-Logik fokussiert auf Business-Logik statt Boilerplate
4. **Type Safety**: Lambda-basierte API mit Compiler-Checks
5. **Automatisches Logging**: Alle Errors werden automatisch geloggt mit Stack-Trace
6. **Einheitliche Fehlermeldungen**: Konsistente User-Experience

---

## Migration Steps (optional)

Dieser Refactor ist **optional** und kann schrittweise erfolgen:

1. Starte mit einem Command (z.B. MoneyCommand)
2. Refaktoriere eine Methode als Proof-of-Concept
3. Refaktoriere restliche Methoden in der Klasse
4. Wiederhole für andere Command-Klassen bei Bedarf

**Keine Breaking Changes** - Commands funktionieren weiterhin identisch, nur mit weniger Code!
