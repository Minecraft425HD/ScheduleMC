# Configuration Validation Guide

ScheduleMC includes a comprehensive configuration validation system that runs at server startup to prevent invalid game states and ensure system stability.

## Overview

The validation system checks all critical configuration values for:
- **Range Validity**: Values are within acceptable bounds
- **Logical Consistency**: Related values make sense together (e.g., MAX ≥ MIN)
- **Safety Constraints**: Values won't cause crashes, exploits, or performance issues

**When Validation Runs**: During server startup, before any game systems initialize.

**Failure Behavior**: If validation fails, the server will **not start** and will display a clear error message indicating which configuration value is invalid and why.

---

## Economy System Validation

### START_BALANCE
- **Range**: 0.0 to 1,000,000,000.0
- **Description**: Starting balance for new players
- **Why**: Prevents negative balances or unrealistic initial wealth that could break the economy

### SAVINGS_INTEREST_RATE
- **Range**: 0.0% to 100.0%
- **Description**: Interest rate per week for savings accounts
- **Why**: Prevents negative interest (money loss) or exploitative hyperinflation (>100%)

### OVERDRAFT_INTEREST_RATE
- **Range**: 0.0% to 100.0%
- **Description**: Interest rate per week for overdraft/negative balances
- **Why**: Prevents unfair penalty rates or exploits

### TAX_SALES_RATE
- **Range**: 0.0% to 100.0%
- **Description**: Sales tax / VAT rate
- **Why**: Rates over 100% would result in negative net income

### SAVINGS_MAX_PER_PLAYER
- **Constraint**: Must be positive (> 0)
- **Description**: Maximum savings account balance per player
- **Why**: Prevents overflow and ensures reasonable wealth caps

### OVERDRAFT_MAX_LIMIT
- **Constraint**: Must be positive (> 0)
- **Description**: Maximum overdraft limit (debt ceiling)
- **Why**: Prevents infinite debt exploits

---

## Plot System Validation

### MIN_PLOT_SIZE
- **Range**: 1 to 1,000,000 blocks
- **Description**: Minimum plot size in blocks
- **Why**: Prevents zero-size or negative plots; caps at reasonable server limits

### MAX_PLOT_SIZE
- **Range**: MIN_PLOT_SIZE to 10,000,000 blocks
- **Description**: Maximum plot size in blocks
- **Why**:
  - Must be ≥ MIN_PLOT_SIZE (logical consistency)
  - Capped at 10M blocks to prevent server performance degradation

### MIN_PLOT_PRICE
- **Constraint**: Must be positive (> 0)
- **Description**: Minimum price for a plot
- **Why**: Prevents free plot exploitation

### MAX_PLOT_PRICE
- **Constraint**: Must be positive (> 0) and ≥ MIN_PLOT_PRICE
- **Description**: Maximum price for a plot
- **Why**: Logical consistency with MIN_PLOT_PRICE

### REFUND_ON_ABANDON
- **Range**: 0.0 (0%) to 1.0 (100%)
- **Description**: Percentage of plot price refunded when abandoned
- **Why**: Values > 100% would create money from nothing

---

## Daily Reward System Validation

### DAILY_REWARD
- **Constraint**: Must be positive (> 0)
- **Description**: Base daily reward amount
- **Why**: Negative rewards would punish login activity

### DAILY_REWARD_STREAK_BONUS
- **Constraint**: Must be positive (> 0)
- **Description**: Bonus per consecutive login day
- **Why**: Prevents negative streak bonuses

### MAX_STREAK_DAYS
- **Range**: 1 to 365 days
- **Description**: Maximum streak length that counts for bonuses
- **Why**: Prevents overflow and encourages regular engagement cycles

---

## Rent System Validation

### MIN_RENT_PRICE
- **Constraint**: Must be positive (> 0)
- **Description**: Minimum rent price per period
- **Why**: Prevents free rental exploits

### MIN_RENT_DAYS
- **Range**: 1 to 365 days
- **Description**: Minimum rental period
- **Why**: Prevents instant rental cycling exploits

### MAX_RENT_DAYS
- **Range**: MIN_RENT_DAYS to 3,650 days (10 years)
- **Description**: Maximum rental period
- **Why**:
  - Must be ≥ MIN_RENT_DAYS (logical consistency)
  - Capped at 10 years for reasonable gameplay

---

## Shop System Validation

### BUY_MULTIPLIER
- **Range**: 0.01 to 100.0
- **Description**: Price multiplier when players buy from NPC shops
- **Why**:
  - Minimum 0.01 prevents zero-cost purchases
  - Maximum 100 prevents unrealistic pricing

### SELL_MULTIPLIER
- **Range**: 0.01 to 100.0
- **Description**: Price multiplier when players sell to NPC shops
- **Why**:
  - Minimum 0.01 ensures players get some value
  - Maximum 100 prevents money printing exploits

---

## Rating System Validation

### MIN_RATING
- **Range**: -10 to +10
- **Description**: Minimum reputation rating
- **Why**: Reasonable bounds for reputation mechanics

### MAX_RATING
- **Range**: MIN_RATING to +10
- **Description**: Maximum reputation rating
- **Why**:
  - Must be ≥ MIN_RATING (logical consistency)
  - Capped at +10 for balanced progression

---

## NPC System Validation

### NPC_SALARY
- **Range**: 0.0 to 1,000,000.0
- **Description**: Daily salary paid to NPCs
- **Why**: Prevents economy drain from excessive NPC salaries

### NPC_WANTED_DECAY
- **Range**: 0.0 to 5.0
- **Description**: How quickly wanted level decreases over time
- **Why**: Prevents instant forgiveness or permanent wanted status

### MAX_WANTED_LEVEL
- **Range**: 1 to 10
- **Description**: Maximum police wanted level
- **Why**: Balanced crime system with clear escalation stages

---

## Warehouse System Validation

### WAREHOUSE_DELIVERY_INTERVAL
- **Range**: 1 to 365 days
- **Description**: Days between deliveries
- **Why**: Prevents instant delivery spam or year-long waits

### WAREHOUSE_MAX_CAPACITY
- **Range**: 1 to 100,000 items
- **Description**: Maximum items per warehouse
- **Why**: Server performance and reasonable inventory limits

---

## Vehicle System Validation

### FUEL_CAPACITY
- **Constraint**: Must be positive (> 0)
- **Description**: Maximum fuel tank capacity
- **Why**: Prevents infinite range vehicles

### FUEL_CONSUMPTION_RATE
- **Range**: 0.0001 to 1.0
- **Description**: Fuel consumed per distance unit
- **Why**:
  - Minimum prevents effectively infinite fuel
  - Maximum prevents vehicles consuming entire tank instantly

---

## Production System Validation (Tobacco)

### TOBACCO_GROWTH_STAGES
- **Range**: 1 to 20 stages
- **Description**: Number of growth stages for tobacco plants
- **Why**: Reasonable growth progression (too many stages = poor UX)

### TOBACCO_GROWTH_SPEED
- **Constraint**: Must be positive (> 0)
- **Description**: Growth speed multiplier
- **Why**: Zero or negative would prevent crop growth

---

## Error Messages

When validation fails, you'll see output like this:

```
═══════════════════════════════════════════════════════════
CRITICAL: Configuration validation failed!
═══════════════════════════════════════════════════════════
Config 'SAVINGS_INTEREST_RATE' has invalid value 150.00 (must be between 0.00 and 100.00)
═══════════════════════════════════════════════════════════
Please fix your configuration file and restart the server.
═══════════════════════════════════════════════════════════
```

**To Fix**:
1. Open `config/schedulemc-common.toml`
2. Locate the invalid config value mentioned in the error
3. Adjust the value to be within the valid range
4. Save the file and restart the server

---

## Benefits of Validation

✅ **Prevents Server Crashes**: Invalid configs caught before they cause runtime errors

✅ **Clear Error Messages**: Tells you exactly what's wrong and how to fix it

✅ **Economy Protection**: Prevents exploits from misconfigured multipliers or rates

✅ **Performance Safety**: Caps values that could cause lag (plot sizes, warehouse capacity)

✅ **Fail-Fast Design**: Bad config = immediate stop at startup, not mysterious failures hours later

---

## Technical Details

**Implementation**: `ModConfigHandler.validateConfig()` (src/main/java/de/rolandsw/schedulemc/config/ModConfigHandler.java:684)

**Called From**: `ScheduleMC.commonSetup()` during FMLCommonSetupEvent

**Exception Type**: `IllegalStateException` with descriptive message

**Thread Safety**: Called during single-threaded initialization phase

---

## FAQ

**Q: Can I disable validation?**
A: No. Validation is mandatory for server stability. If you need extreme values, modify the validation ranges in the source code.

**Q: What happens if I edit config while server is running?**
A: Changes are not validated until next restart. Use `/reload` or restart to apply and validate changes.

**Q: Are default values always valid?**
A: Yes. All default values in the mod are guaranteed to pass validation.

**Q: Can I add custom validation rules?**
A: Yes. Developers can extend `validateConfig()` with custom rules. See ModConfigHandler.java for examples.

---

## Related Documentation

- [Commands.md](Commands.md) - Admin commands for config management
- [Getting-Started.md](Getting-Started.md) - Initial server setup
- [FAQ.md](FAQ.md) - Common configuration questions
