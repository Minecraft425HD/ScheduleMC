# Plot Management System

Chunk-based land ownership. Canonical type list is `PlotType.java`.

[Back to Wiki Home](../Home.md) | [Commands](../Commands.md) | [VERSION](../../docs/VERSION.md)

**ScheduleMC v3.9.0-beta**

## Plot types (8)

| Type | Ownership | Tradeable | Notes |
|---|---|---|---|
| `RESIDENTIAL` | Player | Yes | Housing / apartments |
| `COMMERCIAL` | Player | Yes | Businesses |
| `INDUSTRIAL` | Player | Yes | Factory floor; restricted processing blocks |
| `SHOP` | Admin/Player | Yes | NPC merchant shops |
| `PUBLIC` | Server | No | Parks, roads, spawn |
| `GOVERNMENT` | Server | No | Admin buildings |
| `PRISON` | Server | No | Police / cells |
| `TOWING_YARD` | Player/Server | Yes | Impound lots |

Older copies of this page listed **5** types. That is wrong. The enum has **8**.

## Create

```
/plot wand
/plot create <type> <name> [price]
/plot create industrial "Factory_A" 150000
/plot create prison "City_Jail"
/plot create towing_yard "Impound_1"
```

Mechanics (wand selection, trust, apartments, ratings, Plot Info Block, protection) are unchanged from the previous long-form wiki page. Use `/plot debug` and `/health plot` for diagnostics.
