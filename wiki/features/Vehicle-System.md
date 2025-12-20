# Vehicle System

<div align="center">

**Drivable Vehicles with Fuel, Customization & Modular Parts**

Build, customize, and drive your own vehicles

[🏠 Back to Wiki Home](../Home.md) • [📋 Commands Reference](../Commands.md)

</div>

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [Vehicle Types](#vehicle-types)
3. [Modular Parts System](#modular-parts-system)
4. [Fuel System](#fuel-system)
5. [Durability & Damage](#durability--damage)
6. [Customization](#customization)
7. [Driving Mechanics](#driving-mechanics)
8. [Vehicle Systems](#vehicle-systems)
9. [Garage System](#garage-system)
10. [Best Practices](#best-practices)
11. [Troubleshooting](#troubleshooting)

---

## Overview

The Vehicle System provides fully functional, customizable vehicles with modular parts, fuel consumption, durability, and realistic driving physics.

### Key Features

✅ **5 Chassis Types** - Limousine, Van, Truck, Luxury, Offroad
✅ **Modular Parts** - 15-slot part system
✅ **3 Engine Types** - Normal, Performance, Industrial
✅ **6 Tire Types** - Standard to Heavy Duty
✅ **Fuel System** - Gasoline, Diesel, Bio-Diesel
✅ **Durability** - 0-100 damage with effects
✅ **Temperature System** - Engine heating/cooling
✅ **Customization** - Parts, paint (5 colors)
✅ **Inventories** - Internal, External, Parts
✅ **Security** - Locking, keys, ownership

---

## Vehicle Types

### Chassis Determines Type

Vehicles are defined by their chassis part. Each chassis provides different characteristics.

### 1. Limousine Chassis

**Purpose:** Luxury sedan / passenger car

**Characteristics:**
- **Size:** Medium (width/height moderate)
- **Speed:** Moderate-High
- **Acceleration:** Good
- **Fuel Efficiency:** Good
- **Seats:** 1-2 players
- **Cargo:** Small (with container module)

**Best For:**
- City driving
- Personal transport
- Speed and style

---

### 2. Van Chassis

**Purpose:** Multi-purpose transport

**Characteristics:**
- **Size:** Large (tall and wide)
- **Speed:** Moderate
- **Acceleration:** Moderate
- **Fuel Efficiency:** Moderate
- **Seats:** 1-2 players
- **Cargo:** Medium (with container module)

**Best For:**
- Delivery
- Moderate cargo transport
- Balanced use

---

### 3. Truck Chassis

**Purpose:** Heavy transport and cargo

**Characteristics:**
- **Size:** Very Large
- **Speed:** Lower
- **Acceleration:** Slower
- **Fuel Efficiency:** Lower
- **Seats:** 1-2 players
- **Cargo:** Large (54 slots with container module!)

**Best For:**
- Mass transport
- Business deliveries
- Maximum cargo capacity

---

### 4. Luxury Chassis

**Purpose:** High-end luxury vehicle

**Characteristics:**
- **Size:** Medium-Large
- **Speed:** High
- **Acceleration:** Very Good
- **Fuel Efficiency:** Moderate
- **Seats:** 1-2 players
- **Cargo:** Small

**Best For:**
- Prestige
- Performance driving
- Style over function

---

### 5. Offroad Chassis

**Purpose:** SUV / All-terrain vehicle

**Characteristics:**
- **Size:** Large (raised height)
- **Speed:** Moderate
- **Acceleration:** Good
- **Fuel Efficiency:** Moderate
- **Seats:** 1-2 players
- **Cargo:** Medium

**Best For:**
- Rough terrain
- All-weather driving
- Adventure

---

## Modular Parts System

### Part Inventory

**Slots:** 15 (indexed 0-14)
**Purpose:** Defines vehicle configuration

**Required Parts (5 minimum):**
1. ✅ **1 Engine** (Normal, Performance, or Industrial)
2. ✅ **1 Tank** (15L, 30L, or 50L fuel capacity)
3. ✅ **1 Chassis** (Limousine, Van, Truck, Luxury, Offroad)
4. ✅ **1 Tire Set** (Standard, Premium, Sport, etc.)
5. ✅ **1 Fender** (Basic, Chrome, Sport bumper)

**Optional Parts:**
- **Container Module** (adds cargo space)
- **Tank Container** (adds fluid storage)
- **License Plate Holder** (for license plate)

---

### Engine Types

**Three Engine Options:**

| Engine | Max Speed | Acceleration | Fuel Efficiency | Sound | Best For |
|--------|-----------|--------------|-----------------|-------|----------|
| **Normal** | Config | Config | Config | Standard | Balanced use |
| **Performance** | Config | Config | Config | Sport (high pitch) | Speed |
| **Industrial** | Config | Config | Config | Truck (deep) | Power |

**Configuration:**
All values configurable in server config.

**Speed Reference:**
```
Transporter 3rd gear: ~27.54 km/h
Transporter 6th gear: ~35.8 km/h
Sport 3rd gear: ~36 km/h
Sport 6th gear: ~46.8 km/h
```

**Engine Stats Example:**
```
Normal Motor:
- Max Speed: 1.0× chassis speed
- Acceleration: 1.0× chassis accel
- Fuel Efficiency: 1.0× chassis efficiency

Performance Motor:
- Max Speed: 1.3× chassis speed
- Acceleration: 1.4× chassis accel
- Fuel Efficiency: 0.8× chassis efficiency (uses more fuel)

Industrial Motor:
- Max Speed: 0.8× chassis speed
- Acceleration: 0.7× chassis accel
- Fuel Efficiency: 1.2× chassis efficiency (uses less fuel)
```

---

### Tire Types

**Six Tire Options:**

| Tire Type | Purpose | Properties |
|-----------|---------|------------|
| **Standard** | Basic tires | Normal performance |
| **Premium** | Better quality | Improved handling |
| **Sport** | Performance | High speed, better grip |
| **Allterrain** | All-purpose | Works on all surfaces |
| **Offroad** | Rough terrain | Best for SUVs |
| **Heavy Duty** | Trucks | Best for heavy vehicles |

**Tire Properties:**
- **Step Height:** How tall blocks can be stepped over
- **Rotation Modifier:** Visual wheel rotation speed

**Recommendations:**
```
Limousine → Sport or Premium
Van → Standard or Allterrain
Truck → Heavy Duty
Luxury → Sport or Premium
Offroad → Offroad or Allterrain
```

---

### Fuel Tanks

**Three Tank Sizes:**

| Size | Capacity | Weight | Best For |
|------|----------|--------|----------|
| **Small (15L)** | ~500 mB | Light | Short trips |
| **Medium (30L)** | ~1,500 mB | Medium | Standard use |
| **Large (50L)** | ~5,000 mB | Heavy | Long distance |

**Formula:**
```
1 Liter (L) = 1,000 millibuckets (mB)
15L = 15,000 mB
30L = 30,000 mB
50L = 50,000 mB
```

---

### Fenders (Bumpers)

**Three Fender Options:**

1. **Basic Bumper** - Standard look
2. **Chrome Bumper** - Shiny metal appearance
3. **Sport Bumper** - Aggressive styling

**Purpose:** Cosmetic only

---

### Modules

#### Container Module

**Purpose:** Adds cargo storage

**Capacity:**
- **Truck Chassis:** 54 slots
- **Other Chassis:** 27 slots

**Use Cases:**
```
✓ Transporting items
✓ Delivery services
✓ Moving resources
✓ Business operations
```

---

#### Tank Container Module

**Purpose:** Adds fluid storage

**Capacity:** Configurable

**Use Cases:**
```
✓ Fuel transport
✓ Liquid goods
✓ Water/lava transport
✓ Specialized deliveries
```

---

#### License Plate Holder

**Purpose:** Holds license plate item

**Features:**
- Text display
- Customizable offset
- Vehicle identification

---

## Fuel System

### Fuel Types

**Three Fuel Options:**

1. **Gasoline** - Standard fuel
2. **Diesel** - Standard fuel
3. **Bio-Diesel** - Renewable option

**Configurable:** All fuel types and efficiency in config

---

### Fuel Consumption

**Consumption Rate:**
```
Fuel Used per Tick = 1 / Efficiency

Efficiency Calculation:
Total Efficiency = Fuel Config × Engine × Chassis

Example:
Fuel Efficiency: 100 ticks
Engine Efficiency: 1.2× (Industrial)
Chassis Efficiency: 1.0×

Total: 100 × 1.2 × 1.0 = 120 ticks per fuel unit
Result: 1 mB consumed every 120 ticks (6 seconds)
```

**Consumption States:**
- **Accelerating:** Full consumption rate
- **Idling:** Slower consumption (configurable interval)
- **Engine Off:** No consumption

---

### Refueling

**Method 1: Fuel Station Block**

**Process:**
```
1. Drive to fuel station
2. Right-click fuel station
3. Select fuel type (gasoline/diesel)
4. Pay from wallet
5. Tank filled automatically
```

**Cost:**
- Based on amount filled
- Price per liter configurable
- Paid to state account

---

**Method 2: Fuel Cans**

**Process:**
```
1. Fill fuel can at station
2. Right-click vehicle with full can
3. Transfer fuel to tank
4. Empty can returned
```

---

### Fuel Efficiency Tips

**Best Practices:**
```
✓ Use Industrial engine for best efficiency
✓ Choose appropriate chassis (smaller = better efficiency)
✓ Avoid constant acceleration
✓ Use Medium tank for balance
✓ Refuel before long trips
```

**Worst Efficiency:**
```
✗ Performance engine (high consumption)
✗ Large chassis (Truck)
✗ Constant full-speed driving
✗ Small tank (frequent refills)
```

---

## Durability & Damage

### Damage System

**Range:** 0.0 to 100.0

**Damage Sources:**

| Source | Damage | Effect |
|--------|--------|--------|
| **Collision** | Speed-based | 0-5× damage multiplier |
| **Water** | +25 | Engine stops immediately |
| **Lava** | +1 per tick | Continuous damage |

---

### Collision Damage

**Formula:**
```java
Speed Percentage = Current Speed / Max Speed

if (Speed % > 80%) {
    Damage = Speed % × 5

    if (Speed % > 90%) {
        Engine Stops
    }
}
```

**Examples:**
```
Max Speed: 10 blocks/sec
Current: 5 blocks/sec
Speed %: 50%
Result: No damage (< 80% threshold)

Current: 9 blocks/sec
Speed %: 90%
Damage: 90% × 5 = 4.5 damage
Engine: Stops (> 90%)

Current: 8.5 blocks/sec
Speed %: 85%
Damage: 85% × 5 = 4.25 damage
Engine: Still running
```

---

### Damage Effects

**Visual Effects (Particles):**

| Damage | Particles | Frequency |
|--------|-----------|-----------|
| 0-49 | None | - |
| 50-69 | Low smoke | 1 in 10 chance |
| 70-79 | Medium smoke | 1 in 5 chance |
| 80-89 | Constant smoke | 2 particles |
| 90-100 | Heavy smoke | 3 particles |

---

### Engine Start Delays

**Based on Damage:**

| Damage | Start Delay | Notes |
|--------|-------------|-------|
| 0-49 | Instant | Perfect condition |
| 50-79 | 5-15 ticks (0.25-0.75s) | Slight delay |
| 80-89 | 10-25 ticks (0.5-1.25s) | Moderate delay |
| 90-94 | 30-45 ticks (1.5-2.25s) | Long delay |
| 95-99 | 50-75 ticks (2.5-3.75s) | Very long delay |
| 100 | **Cannot Start** | Destroyed |

---

### Damage at 100

**Effects:**
- ❌ Engine cannot start
- ❌ Vehicle effectively destroyed
- ✅ Can still be repaired
- ✅ Parts can be salvaged

**Recovery:**
```
Option 1: Repair Kit (destroy vehicle, get parts back)
Option 2: Garage repair (restore to working condition)
```

---

## Customization

### Paint Colors

**5 Color Options:**

0. **White** (default)
1. **Black**
2. **Red**
3. **Blue**
4. **Yellow**

**Changing Color:**
- Via garage system
- Or item/command (if implemented)

---

### Part Swapping

**Method:** Garage system

**Process:**
```
1. Drive vehicle to garage
2. Vehicle locked in garage
3. Open garage GUI
4. Remove/add parts from 15 slots
5. Validate configuration
6. Apply changes
7. Vehicle updated with new parts
```

**Requirements:**
- Must have required parts (engine, tank, chassis, tires, fender)
- Parts must be compatible
- Garage must have space

---

### Building Custom Vehicles

**Assembly Process:**
```
1. Gather parts:
   - 1 Engine
   - 1 Tank
   - 1 Chassis
   - 1 Tire set
   - 1 Fender
   - Optional modules

2. Place parts in crafting/assembly system

3. Create vehicle entity

4. Customize:
   - Paint color
   - Additional modules
   - License plate

5. Spawn vehicle

6. Add fuel

7. Drive!
```

---

## Driving Mechanics

### Speed System

**Speed Calculation:**
```
Max Speed = Engine Max × Chassis Max

Example:
Engine: Performance (1.3× multiplier)
Chassis: Limousine (1.0× base speed)
Max Speed: 1.3× chassis base

Result: Faster than normal engine
```

**Acceleration:**
```
Acceleration = Engine Accel × Chassis Accel

Example:
Engine: Performance (1.4× multiplier)
Chassis: Limousine (1.0× base accel)
Acceleration: 1.4× chassis base

Result: Faster acceleration
```

---

### Speed Display

**km/h Conversion:**
```java
km/h = |speed| × 20

Example:
Speed value: 0.5
km/h: 0.5 × 20 = 10 km/h

Speed value: 1.8
km/h: 1.8 × 20 = 36 km/h
```

---

### Controls

**Driving Controls:**

| Action | Default Key | Effect |
|--------|-------------|--------|
| **Forward** | W | Accelerate forward |
| **Backward** | S | Brake / Reverse |
| **Left** | A | Turn left |
| **Right** | D | Turn right |
| **Horn** | (Configurable) | Honk (costs 10 battery) |
| **Exit** | Shift | Leave vehicle |

---

### Handling

**Rotation Speed:**
- **Speed-dependent** - Faster = tighter turns
- **Min Rotation:** Chassis-defined
- **Max Rotation:** Chassis-defined

**Formula:**
```
Rotation = lerp(minRotation, maxRotation, speedPercent)

At low speed: Wider turns (min rotation)
At high speed: Tighter turns (max rotation)
```

---

### Physics

**Roll Resistance:** 0.02 (constant deceleration when not accelerating)

**Movement Calculation:**
```java
Motion X = sin(-rotationYaw × 0.017453292) × speed
Motion Z = cos(rotationYaw × 0.017453292) × speed
```

**Collision:**
- Entity detection
- Block collision
- Speed-based damage
- Crash sounds

---

## Vehicle Systems

### Temperature System

**Range:** 0°C to 150°C

**Temperature Factors:**
```
1. Biome Temperature (base)
   - Hot biomes: Higher base temp
   - Cold biomes: Lower base temp

2. Engine Running
   - Target: 90°C (optimal)
   - Hot biome: 100°C target
   - Cold biome: 80°C target

3. Speed
   - Higher speed = Higher temp
   - Air cooling effect
```

**Temperature Formula:**
```java
Biome Temp (°C) = (biomeTempValue - 0.3) × 30

Optimal Temp:
if (biomeTemp > 45°C) → 100°C (hot biome)
else if (biomeTemp <= 0°C) → 80°C (cold biome)
else → 90°C (normal)

Current Temp gradually moves toward optimal temp
```

---

### Battery System

**Battery Level:** 0 to 1,000 (configurable max)

**Battery Usage:**
- **Engine Start:** Consumes battery
- **Horn:** 10 battery per honk
- **Min for Horn:** 10 battery required

**Starting Sound:**
- Pitch varies with battery level
- Low battery = lower pitch

**Charging:**
- Via garage
- Or battery items

---

### Security System

**Features:**
- **Locking:** Lock/unlock with key
- **Ownership:** UUID-based
- **License Plate:** Unique ID
- **Key Item:** Required for access

**Locked Effects:**
```
✗ Cannot enter vehicle
✗ Cannot open inventories
✗ Cannot destroy vehicle
✓ Owner can always access
✓ Key holders can access
```

**Commands (if available):**
```
Lock vehicle
Unlock vehicle
Add key holder
Remove key holder
```

---

### Horn & Monster Flee

**Horn Effects:**
```
Cost: 10 battery per honk
Range: 15 blocks
Effect: Monsters flee

Monsters within 15 blocks:
→ Run away from vehicle
→ Distance: 10 blocks
→ Speed: 2.5 blocks/sec
```

**Use Cases:**
```
✓ Clear path of monsters
✓ Scare away attackers
✓ Warning signal
✓ Fun/roleplay
```

---

## Garage System

### Garage Functions

**Features:**
1. **Part Swapping** - Change vehicle parts
2. **Repairs** - Fix damage
3. **Storage** - Safe vehicle parking
4. **Customization** - Paint, modules

---

### Vehicle Locking

**When in Garage:**
```
Vehicle locked = true
Movement = disabled (Delta movement = 0)
Position = fixed to garage

Player cannot:
✗ Drive vehicle
✗ Move vehicle
✗ Damage vehicle

Player can:
✓ Access garage GUI
✓ Swap parts
✓ Repair
✓ Customize
```

---

### Part Swapping Process

**In Garage GUI:**
```
1. View 15 part slots
2. Remove unwanted parts
3. Add new parts
4. System validates:
   - Required parts present?
   - Parts compatible?
   - Configuration valid?
5. If valid: Apply changes
6. If invalid: Show error, revert
```

**Validation Example:**
```
Removed engine:
→ Error: "Vehicle requires an engine!"
→ Cannot apply

Added second engine:
→ Error: "Vehicle can only have one engine!"
→ Cannot apply

Replaced Normal → Performance engine:
→ Success: "Engine upgraded!"
→ Changes applied
```

---

## Best Practices

### For Players

#### 1. Choose Right Vehicle

**For Speed:**
```
Chassis: Limousine or Luxury
Engine: Performance
Tires: Sport
Tank: Medium (balance)
Result: Fast, good acceleration
```

**For Cargo:**
```
Chassis: Truck
Engine: Industrial (efficiency)
Tires: Heavy Duty
Tank: Large
Module: Container
Result: Max cargo (54 slots), efficient
```

**For All-Terrain:**
```
Chassis: Offroad
Engine: Normal (balance)
Tires: Offroad or Allterrain
Tank: Medium
Result: Works everywhere
```

---

#### 2. Maintenance Schedule

**Regular Checks:**
```
Every 7 days:
- Check damage level
- Repair if > 50 damage
- Check fuel efficiency
- Refuel if < 20% tank

Every 30 days:
- Full garage inspection
- Replace worn parts
- Deep clean (roleplay)
```

---

#### 3. Avoid Damage

**Tips:**
```
✓ Slow down before turns
✓ Avoid high-speed collisions
✓ Don't drive in water (engine stops + 25 damage!)
✓ Avoid lava entirely
✓ Watch speed on rough terrain
✓ Brake before impact

✗ Full speed everywhere
✗ Water crossings
✗ Ramming other vehicles
✗ Jumping off cliffs
```

---

### For Admins

#### 1. Fuel Station Placement

**Strategic Locations:**
```
✓ Major roads every 500-1,000 blocks
✓ City centers
✓ Near spawn
✓ Player plot areas
✓ Highway rest stops

Recommended: 1 fuel station per 250 blocks² in populated areas
```

---

#### 2. Garage Setup

**Placement Tips:**
```
✓ Accessible locations
✓ Near player bases
✓ Protected areas
✓ Covered/indoor spaces
✓ Multiple garages for large servers
```

---

#### 3. Fuel Pricing

**Balanced Pricing:**
```
Too Cheap: Fuel becomes meaningless
Too Expensive: Players avoid driving

Recommended (per liter):
Gasoline: 10-20€
Diesel: 8-15€
Bio-Diesel: 12-25€

Example costs:
Small tank (15L): 150-300€
Medium tank (30L): 300-600€
Large tank (50L): 500-1,000€
```

---

## Troubleshooting

### "Vehicle Won't Start"

**Causes:**
1. No fuel
2. Damage = 100
3. Engine stopped from collision/water
4. Low battery
5. Missing engine part

**Solutions:**
```
1. Check fuel:
   - Open vehicle GUI
   - Check tank level
   - Refuel if empty

2. Check damage:
   - If 100: Repair or use repair kit
   - If < 100: Try starting again

3. Check battery:
   - Must have > 0 battery
   - Charge in garage

4. Check parts:
   - Garage GUI → Part inventory
   - Ensure engine present
```

---

### "Vehicle Slow/Poor Performance"

**Causes:**
1. Wrong engine type
2. High damage (slows vehicle)
3. Wrong tire type
4. Heavy chassis + weak engine

**Solutions:**
```
1. Upgrade engine:
   Normal → Performance
   +30% speed, +40% acceleration

2. Repair damage:
   High damage affects performance
   Repair in garage or with kit

3. Match tires to chassis:
   Truck → Heavy Duty
   Limousine → Sport
   Offroad → Offroad tires

4. Check part combinations:
   Heavy chassis needs strong engine
```

---

### "High Fuel Consumption"

**Causes:**
1. Performance engine (low efficiency)
2. Constant full-speed driving
3. Poor fuel type for engine
4. Large chassis

**Solutions:**
```
1. Switch to Industrial engine:
   Best fuel efficiency (1.2× multiplier)

2. Drive moderately:
   Avoid constant acceleration
   Coast when possible

3. Choose smaller chassis:
   Limousine uses less than Truck

4. Use larger tank:
   Fewer refuel stops
   More efficient long-term
```

---

### "Vehicle Damaged Instantly"

**Cause:** Water damage (+25 damage on water contact)

**Prevention:**
```
✓ Avoid water crossings
✓ Build bridges
✓ Use boats for water
✓ Plan routes around water

If in water:
1. Exit vehicle immediately
2. Push vehicle out of water
3. Repair damage in garage
```

---

<div align="center">

**Vehicle System - Complete Guide**

For related systems:
- [⛽ Fuel & Resources](../Items.md)
- [🏪 Shop System](NPC-System.md)
- [💰 Economy](Economy-System.md)

[🏠 Back to Wiki Home](../Home.md) • [📋 All Commands](../Commands.md)

**Last Updated:** 2025-12-20 | **ScheduleMC v2.7.0-beta**

</div>
