# Complete Items Reference - ScheduleMC

**Total Registered Items:** 374 `ITEMS.register("id")` entries

This page documents every item registered in the ScheduleMC mod, organized by module. Each item listing includes its registry name, a description of its purpose, and how players or admins can obtain it.

---

## Table of Contents

- [Economy Items](#economy-items-4)
- [NPC Tools](#npc-tools-5)
- [Lock Items](#lock-items-13)
- [Tobacco Items](#tobacco-items-26)
- [Cannabis Items](#cannabis-items-10)
- [Coca Items](#coca-items-10)
- [Poppy Items](#poppy-items-8)
- [Meth Items](#meth-items-8)
- [LSD Items](#lsd-items-6)
- [MDMA Items](#mdma-items-6)
- [Mushroom Items](#mushroom-items-12)
- [Beer Items](#beer-items-30)
- [Wine Items](#wine-items-22)
- [Coffee Items](#coffee-items-26)
- [Cheese Items](#cheese-items-19)
- [Chocolate Items](#chocolate-items-33)
- [Honey Items](#honey-items-27)
- [Vehicle Items](#vehicle-items-36)

---

## Economy Items (3 in `ModItems.java`)

Items related to the server economy, plot management, and universal packaging.

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Cash | `cash` | Physical money item with variable value stored in NBT. Can be dropped, traded, and stolen. Deposit at ATM blocks. | `/money withdraw <amount>` at an ATM block |
| Cash Block (Item) | `cash_block` | Block item form of the Cash Block. Decorative money storage that displays as a stack of bills. | Crafting or admin command |
| Plot Wand | `plot_selection_tool` | Plot selection tool similar to WorldEdit's wand. Left-click sets position 1, right-click sets position 2 to define a rectangular region. | `/plot wand` |
| Packaged Drug | `packaged_drug` | Universal packaging item for all processed substances. Stores drug type, quality, weight (1g-100g), variant, and package date in NBT data. Price is calculated dynamically. | Produced at Packaging Tables |

---

## NPC Tools (5 in `NPCItems.java`)

Admin-only tools (OP Level 2) for spawning and configuring NPCs.

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| NPC Spawner Tool | `npc_spawner_tool` | Right-click to open the NPC spawn GUI. Configure NPC type (Resident, Merchant, Police), name, and skin before spawning. | `/npc give spawner` or admin command |
| NPC Location Tool | `npc_location_tool` | Sets home and work locations for NPCs. Right-click an NPC to select it, then right-click a block to assign home or work positions for their daily schedule. | `/npc give location` or admin command |
| NPC Leisure Tool | `npc_leisure_tool` | Adds leisure locations for NPCs (maximum 10 per NPC). Right-click an NPC to select, then right-click blocks to mark leisure spots that the NPC will visit randomly during free time. | `/npc give leisure` or admin command |
| NPC Patrol Tool | `npc_patrol_tool` | Sets patrol routes for Police-type NPCs. Right-click to add sequential waypoints that the NPC will walk between during patrol duty. | `/npc give patrol` or admin command |
| Entity Remover | `entity_remover` | Removes any custom ScheduleMC entity (NPCs, vehicles, etc.) by right-clicking on it. Admin cleanup tool. | Admin command |

---

## Lock Items (13 in `LockItems.java`)

Items for the door lock and security system. Includes 5 lock types, 3 key blank tiers, a key ring, a lock pick set, and 3 hacking tools.

### Locks (5)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Simple Lock | `simple_lock` | Basic door lock (LockType.SIMPLE). Easy to pick, low security. | Crafting |
| Security Lock | `security_lock` | Improved door lock (LockType.SECURITY). Moderate pick resistance. | Crafting |
| High Security Lock | `high_security_lock` | Advanced door lock (LockType.HIGH_SECURITY). Very difficult to pick. | Crafting |
| Combination Lock | `combination_lock` | Code-based door lock (LockType.COMBINATION). Requires numeric code instead of key. | Crafting |
| Dual Lock | `dual_lock` | Requires two keys to open (LockType.DUAL). Maximum physical security. | Crafting |

### Key Blanks (3)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Copper Key Blank | `key_blank_copper` | Tier 0 key blank (Kupfer-Rohling). Works with Simple Locks. | Crafting with copper ingots |
| Iron Key Blank | `key_blank_iron` | Tier 1 key blank (Eisen-Rohling). Works with Security Locks. | Crafting with iron ingots |
| Netherite Key Blank | `key_blank_netherite` | Tier 2 key blank (Netherite-Rohling). Works with High Security and Dual Locks. | Crafting with netherite |

### Utility & Hacking (5)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Key Ring | `key_ring` | Holds multiple keys in a single inventory slot for convenience. | Crafting |
| Lock Pick | `lock_pick` | Allows attempting to pick locks. Success chance depends on lock tier and player skill. Consumes durability on each attempt. | Crafting or NPC trading |
| Code Cracker | `hacking_code_cracker` | Electronic tool for cracking Combination Locks. Tier 1 hacking tool. | Crafting or NPC trading |
| Bypass Module | `hacking_bypass` | Bypasses electronic lock mechanisms. Tier 2 hacking tool. | Crafting or NPC trading |
| Omni Hack | `hacking_omni` | Universal hacking tool that can defeat any electronic lock. Tier 3 hacking tool. | Crafting (expensive) or NPC trading |

---

## Tobacco Items (26 in `TobaccoItems.java`)

The full tobacco production chain: seeds, leaves at three processing stages, growth tools, soil, and packaging materials.

### Seeds (4 Strains)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Virginia Seeds | `virginia_seeds` | Seeds for Virginia tobacco (TobaccoType.VIRGINIA). Warm climate, moderate humidity. ~10 min growth time. | Admin command or NPC shop |
| Burley Seeds | `burley_seeds` | Seeds for Burley tobacco (TobaccoType.BURLEY). Cool, dry climate. ~12 min growth time. | Admin command or NPC shop |
| Oriental Seeds | `oriental_seeds` | Seeds for Oriental tobacco (TobaccoType.ORIENTAL). Hot, dry climate. ~15 min growth time. | Admin command or NPC shop |
| Havana Seeds | `havana_seeds` | Seeds for Havana tobacco (TobaccoType.HAVANA). Tropical, humid. Slowest growth (~20 min) but highest quality potential. | Admin command or NPC shop |

### Fresh Leaves (4 Types)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Fresh Virginia Leaf | `fresh_virginia_leaf` | Freshly harvested Virginia tobacco leaf. Must be dried before further processing. Stacks to 16. | Harvest mature Virginia plant (stage 7) |
| Fresh Burley Leaf | `fresh_burley_leaf` | Freshly harvested Burley tobacco leaf. Must be dried before further processing. Stacks to 16. | Harvest mature Burley plant (stage 7) |
| Fresh Oriental Leaf | `fresh_oriental_leaf` | Freshly harvested Oriental tobacco leaf. Must be dried before further processing. Stacks to 16. | Harvest mature Oriental plant (stage 7) |
| Fresh Havana Leaf | `fresh_havana_leaf` | Freshly harvested Havana tobacco leaf. Must be dried before further processing. Stacks to 16. | Harvest mature Havana plant (stage 7) |

### Dried Leaves (4 Types)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Dried Virginia Leaf | `dried_virginia_leaf` | Dried Virginia leaf, ready for fermentation. Stacks to 16. | Drying Rack (Small/Medium/Big) |
| Dried Burley Leaf | `dried_burley_leaf` | Dried Burley leaf, ready for fermentation. Stacks to 16. | Drying Rack (Small/Medium/Big) |
| Dried Oriental Leaf | `dried_oriental_leaf` | Dried Oriental leaf, ready for fermentation. Stacks to 16. | Drying Rack (Small/Medium/Big) |
| Dried Havana Leaf | `dried_havana_leaf` | Dried Havana leaf, ready for fermentation. Stacks to 16. | Drying Rack (Small/Medium/Big) |

### Fermented Leaves (4 Types)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Fermented Virginia Leaf | `fermented_virginia_leaf` | Fully fermented Virginia leaf. Final processing stage, ready for packaging. Stacks to 16. | Fermentation Barrel (Small/Medium/Big) |
| Fermented Burley Leaf | `fermented_burley_leaf` | Fully fermented Burley leaf. Final processing stage, ready for packaging. Stacks to 16. | Fermentation Barrel (Small/Medium/Big) |
| Fermented Oriental Leaf | `fermented_oriental_leaf` | Fully fermented Oriental leaf. Final processing stage, ready for packaging. Stacks to 16. | Fermentation Barrel (Small/Medium/Big) |
| Fermented Havana Leaf | `fermented_havana_leaf` | Fully fermented Havana leaf. Final processing stage, ready for packaging. Stacks to 16. | Fermentation Barrel (Small/Medium/Big) |

### Tools & Boosters (4)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Watering Can | `watering_can` | Refillable watering tool (1000 water units). Prevents wilting, provides +5% growth speed. Refill at water source or Sink block. Stacks to 1. | Admin command or crafting |
| Fertilizer Bottle | `fertilizer_bottle` | Applies +10% growth speed to potted plants. 10 applications per bottle. Right-click on pot to use. | Admin command or NPC shop |
| Growth Booster Bottle | `growth_booster_bottle` | Applies +25% growth speed (stacks with fertilizer). 5 applications per bottle. Right-click on pot to use. | Admin command or NPC shop |
| Quality Booster Bottle | `quality_booster_bottle` | Increases chance of higher quality tier by +1. 5 applications per bottle. Right-click on pot to use. | Admin command or NPC shop |

### Soil Bags (3 Sizes)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Small Soil Bag | `soil_bag_small` | Contains 100 soil units, enough for 1 terracotta pot. Stacks to 16. Right-click empty pot to fill. | Admin command or NPC shop |
| Medium Soil Bag | `soil_bag_medium` | Contains 500 soil units, enough for 5 terracotta pots. Stacks to 8. Right-click empty pot to fill. | Admin command or NPC shop |
| Large Soil Bag | `soil_bag_large` | Contains 1000 soil units, enough for 10 terracotta pots. Stacks to 4. Right-click empty pot to fill. | Admin command or NPC shop |

### Packaging Materials (3)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Packaging Bag | `packaging_bag` | For small packages (1g-10g). Stacks to 64. Made from paper and string. | Crafting |
| Packaging Jar | `packaging_jar` | For medium packages (25g-50g). Stacks to 16. Made from glass. | Crafting |
| Packaging Box | `packaging_box` | For large packages (100g+). Stacks to 8. Made from wood and nails. | Crafting |

---

## Cannabis Items (9 in `CannabisItems.java`)

Cannabis cultivation and processing items covering seeds, four processing stages, by-products, concentrates, and processing tools.

### Seeds & Processing Stages

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Cannabis Seed | `cannabis_seed` | Plantable seed for cannabis (4 strains: Indica, Sativa, Hybrid, Autoflower). Stacks to 64. 12-20 min growth. | Admin command or NPC trading |
| Fresh Cannabis Bud | `fresh_cannabis_bud` | Freshly harvested bud, requires drying. Stacks to 16. | Harvest mature cannabis plant |
| Dried Cannabis Bud | `dried_cannabis_bud` | Dried bud ready for trimming. Stacks to 16. | Trocknungsnetz (Drying Net) |
| Trimmed Cannabis Bud | `trimmed_cannabis_bud` | Trimmed bud ready for curing. Stacks to 16. Packageable. | Trimm Station |
| Cured Cannabis Bud | `cured_cannabis_bud` | Fully cured bud, the primary final product. Stacks to 16. Can be used for hash or oil production. Packageable. | Curing Glas |

### By-Products & Concentrates

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Cannabis Trim | `cannabis_trim` | Leaf trimmings from the trimming process. Stacks to 64. Used for hash production (4 trim = 1 hash). | By-product of Trimm Station |
| Cannabis Hash | `cannabis_hash` | Pressed concentrate from trim or buds. Stacks to 16. Quality inherited from source material. | Hash Presse (4 trim or 2 cured buds) |
| Cannabis Oil | `cannabis_oil` | Extracted oil concentrate, 3x potency of buds. Stacks to 16. | Oil Extractor (3 cured buds + solvent) |

### Processing Tools

`pollen_press_mold` does not exist as a registered item.

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Extraction Solvent | `extraction_solvent` | Chemical solvent consumed during oil extraction. Stacks to 16. 1 consumed per extraction cycle. | Crafting or NPC trading |

---

## Coca Items (12 in `CocaItems.java`)

Three coca strains, fresh leaves, and the cocaine/crack processing chain along with required chemicals.

### Seeds (3 Strains)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Bolivianisch Coca Seeds | `bolivian_coca_seeds` | Seeds for Bolivianisch coca (CocaType.BOLIVIANISCH). Higher quantity yield. ~15 min growth. | Admin command or NPC trading |
| Kolumbianisch Coca Seeds | `colombian_coca_seeds` | Seeds for Kolumbianisch coca (CocaType.KOLUMBIANISCH). Higher purity output. ~18 min growth. | Admin command or NPC trading |
| Peruanisch Coca Seeds | `peruvian_coca_seeds` | Seeds for Peruanisch coca (CocaType.PERUANISCH). Balanced yield and quality. Uses Bolivianisch plant block internally. | Admin command or NPC trading |

### Fresh Leaves (3 Types)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Fresh Bolivianisch Coca Leaf | `fresh_bolivian_coca_leaf` | Freshly harvested Bolivianisch coca leaf. Stacks to 16. Input for extraction. | Harvest mature Bolivianisch coca plant |
| Fresh Kolumbianisch Coca Leaf | `fresh_colombian_coca_leaf` | Freshly harvested Kolumbianisch coca leaf. Stacks to 16. Input for extraction. | Harvest mature Kolumbianisch coca plant |
| Fresh Peruanisch Coca Leaf | `fresh_peruvian_coca_leaf` | Freshly harvested Peruanisch coca leaf. Stacks to 16. Input for extraction. | Harvest mature Peruanisch coca plant |

### Processing Chain & Chemicals

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Coca Paste | `coca_paste_bolivian` / `coca_paste_colombian` / `coca_paste_peruvian` | Brown intermediate paste from leaf extraction, one registered item per coca strain (there is no plain `coca_paste`). Stacks to 16. Requires diesel and leaves. | Extraction Vat (leaves + diesel canister) |
| Cocaine | `cocaine` | White powder refined from coca paste. Stacks to 16. Purity 70-95%. Packageable. | Refinery (from coca paste) |
| Crack Rock | `crack_rock` | Crystallized form of cocaine cooked with baking soda. Stacks to 16. Packageable. | Crack Cooker (cocaine + baking_powder) |
| Baking Powder | `baking_powder` | Baking soda, a required reagent for crack production. Stacks to 64. Ratio 1:1 with cocaine. | Crafting or NPC trading |

---

## Poppy Items (10 in `PoppyItems.java`)

Three poppy strains and the full opium-to-heroin refinement chain.

### Seeds (3 Strains)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Afghanisch Poppy Seeds | `afghan_poppy_seeds` | Seeds for Afghanisch poppy (PoppyType.AFGHANISCH). High opium yield. ~12 min growth. | Admin command or NPC trading |
| Tuerkisch Poppy Seeds | `turkish_poppy_seeds` | Seeds for Tuerkisch poppy (PoppyType.TUERKISCH). Medium opium yield. ~15 min growth. | Admin command or NPC trading |
| Indisch Poppy Seeds | `indian_poppy_seeds` | Seeds for Indisch poppy (PoppyType.INDISCH). Premium quality opium. ~18 min growth. | Admin command or NPC trading |

### Processing Chain

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Poppy Pod | `afghan_poppy_pod` / `turkish_poppy_pod` / `indian_poppy_pod` | Harvested seed pod, one registered item per poppy strain (there is no plain `poppy_pod`). Stacks to 16. Must be scored to extract opium. | Harvest mature poppy plant |
| Raw Opium | `raw_opium` | Dark brown raw opium extracted from scored pods. Stacks to 16. Intermediate product. | Ritzmaschine or Scoring Knife |
| Morphine | `morphine` | Morphine base refined from pressed opium. Stacks to 16. Purity 60-85%. | Kochstation (cooking station) |
| Heroin | `heroin` | White powder refined from morphine. Stacks to 16. Purity 80-99%. Packageable. | Heroin Raffinerie |

### Tool

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Scoring Knife | `scoring_knife` | Hand tool for manually scoring poppy pods to extract opium. Alternative to the Ritzmaschine block. Stacks to 1 with durability. | Crafting |

---

## Meth Items (8 in `MethItems.java`)

Chemical synthesis items for the four-step methamphetamine production process.

### Base Chemicals (4)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Ephedrin | `ephedrine` | Primary precursor chemical for meth synthesis. Regulated substance. Stacks to 16. | NPC trading or crafting |
| Pseudoephedrin | `pseudoephedrine` | Alternative precursor chemical for meth synthesis. Regulated substance. Stacks to 16. | NPC trading or crafting |
| Roter Phosphor | `red_phosphorus` | Red phosphorus, a flammable reagent used in the reduction step. Stacks to 16. | Crafting |
| Jod | `iodine` | Iodine, a corrosive reagent used in the reduction step. Stacks to 16. | Crafting |

### Processing Chain (4)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Meth Paste | `meth_paste` | Wet intermediate paste from mixing base chemicals. Stacks to 16. | Chemie Mixer (step 1) |
| Roh Meth | `raw_meth` | Raw methamphetamine from the dangerous reduction step. Stacks to 16. Explosion risk during production. | Reduktionskessel (step 2) |
| Kristall Meth | `crystal_meth` | Crystallized methamphetamine with visible crystal structure. Stacks to 16. | Kristallisator (step 3) |
| Meth | `meth` | Final dried methamphetamine product, 95-99% purity. Stacks to 16. Packageable. | Vakuum Trockner (step 4) |

---

## LSD Items (6 in `LSDItems.java`)

Laboratory synthesis items for the precision LSD production process.

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Mutterkorn | `ergot` | Ergot fungus, the raw starting material. Toxic if consumed raw. Stacks to 16. | Specialized farming or NPC trading |
| Blotter Papier | `blotter_paper` | Absorbent blotter paper sheet (10x10 grid). Used in the final dosing step. Stacks to 64. | Crafting |
| Ergot Kultur | `ergot_culture` | Fermented ergot culture, intermediate product from ergot fermentation. Stacks to 16. | Fermentations Tank (step 1) |
| Lysergsaeure | `lysergic_acid` | Lysergic acid distilled from ergot culture. High-precision intermediate. Stacks to 16. | Destillations Apparat (step 2) |
| LSD Loesung | `lsd_solution` | LSD solution at 100ug/ml concentration. Ready for blotter application. Stacks to 16. | Chemical synthesis from lysergic acid |
| LSD Blotter | `lsd_blotter` | Finished LSD blotter tabs (100 per sheet). Final product. Stacks to 16. | Mikro Dosierer + Perforations Presse (steps 3-4) |

---

## MDMA Items (6 in `MDMAItems.java`)

Chemical synthesis and pill-pressing items for ecstasy production.

### Raw Materials (3)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Safrol | `safrole` | Base chemical extracted from sassafras. Primary precursor for MDMA synthesis. Stacks to 16. | Extraction or NPC trading |
| Bindemittel | `binding_agent` | Pill binder material that holds ecstasy pills together. Stacks to 64. | Crafting |
| Pillen Farbstoff | `pill_dye` | Pill dye for coloring ecstasy pills. Customizable colors. Stacks to 64. | Crafting |

### Processing Chain (3)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| MDMA Base | `mdma_base` | Liquid MDMA base synthesized from safrole. Stacks to 16. | Reaktions Kessel (step 1) |
| MDMA Kristall | `mdma_crystal` | Dried MDMA crystals. Purity 80-95%. Stacks to 16. | Trocknungs Ofen (step 2) |
| Ecstasy Pill | `ecstasy_pill` | Finished pressed ecstasy pill with custom shape and color. Stacks to 64. Produced via timing minigame. | Pillen Presse (step 3, minigame) |

---

## Mushroom Items (12 in `MushroomItems.java`)

Psilocybin mushroom cultivation items across three strains: Cubensis, Azurescens, and Mexicana.

### Mist Bags (3 Sizes)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Small Mist Bag | `manure_bag_small` | Small substrate bag for mushroom cultivation. Holds 1 spore syringe, produces up to 3 mushrooms. Stacks to 16. | Crafting or NPC trading |
| Medium Mist Bag | `manure_bag_medium` | Medium substrate bag. Holds 2 spore syringes, produces up to 6 mushrooms. Stacks to 8. | Crafting or NPC trading |
| Large Mist Bag | `manure_bag_large` | Large substrate bag. Holds 3 spore syringes, produces up to 9 mushrooms. Stacks to 4. | Crafting or NPC trading |

### Spore Syringes (3 Strains)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Cubensis Spore Syringe | `spore_syringe_cubensis` | Spore syringe for Psilocybe cubensis (MushroomType.CUBENSIS). Standard potency, ~8 min growth. | Admin command or NPC trading |
| Azurescens Spore Syringe | `spore_syringe_azurescens` | Spore syringe for Psilocybe azurescens (MushroomType.AZURESCENS). High potency, ~12 min growth. | Admin command or NPC trading |
| Mexicana Spore Syringe | `spore_syringe_mexicana` | Spore syringe for Psilocybe mexicana (MushroomType.MEXICANA). Premium potency, ~15 min growth. | Admin command or NPC trading |

### Fresh Mushrooms (3 Strains)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Fresh Cubensis | `fresh_cubensis` | Freshly harvested Cubensis mushroom. Requires drying. Stacks to 16. | Harvest from mist bag (Klimalampe required) |
| Fresh Azurescens | `fresh_azurescens` | Freshly harvested Azurescens mushroom. Requires drying. Stacks to 16. | Harvest from mist bag (Klimalampe required) |
| Fresh Mexicana | `fresh_mexicana` | Freshly harvested Mexicana mushroom. Requires drying. Stacks to 16. | Harvest from mist bag (Klimalampe required) |

### Dried Mushrooms (3 Strains)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Dried Cubensis | `dried_cubensis` | Dried Cubensis mushroom. Final product, ready for packaging. Stacks to 16. Packageable. | Climate-controlled drying (~5 min) |
| Dried Azurescens | `dried_azurescens` | Dried Azurescens mushroom. Final product, ready for packaging. Stacks to 16. Packageable. | Climate-controlled drying (~6 min) |
| Dried Mexicana | `dried_mexicana` | Dried Mexicana mushroom. Final product, ready for packaging. Stacks to 16. Packageable. | Climate-controlled drying (~7 min) |

---

## Beer Items (30 in `BeerItems.java`)

Complete brewing chain from grain to bottle, including 3 grains, malts, hops, yeasts, intermediates, containers, and additives.

### Grains (3)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Barley | `barley` | Raw barley grain. Edible (1 nutrition). Base grain for most beers. | Farming or NPC trading |
| Wheat Grain | `wheat_grain` | Raw wheat grain. Edible (1 nutrition). Used for wheat beers. | Farming or NPC trading |
| Rye | `rye` | Raw rye grain. Edible (1 nutrition). Used for rye beers and dark ales. | Farming or NPC trading |

### Malted Grains (3)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Malted Barley | `malted_barley` | Malted barley ready for mashing. Core ingredient for standard beers. | Malting Station |
| Malted Wheat | `malted_wheat` | Malted wheat for wheat-based beers (Weizen, Hefeweizen). | Malting Station |
| Malted Rye | `malted_rye` | Malted rye for darker, spicier beer styles. | Malting Station |

### Hops (4)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Hops Cone | `hops_cone` | Fresh hop cone, provides bitterness and aroma. | Farming |
| Dried Hops | `dried_hops` | Dried hop flowers for brewing. Longer shelf life than fresh. | Drying process |
| Hop Extract | `hop_extract` | Concentrated hop extract for precise bitterness control. | Processing |
| Hop Pellets | `hop_pellets` | Compressed hop pellets, most efficient form for brewing. | Processing |

### Yeasts (4)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Yeast | `yeast` | Basic yeast culture. Starter for brewing yeasts. | NPC trading or crafting |
| Brewing Yeast | `brewing_yeast` | General-purpose brewing yeast for standard beers. | Cultivated from yeast |
| Lager Yeast | `lager_yeast` | Bottom-fermenting yeast for lager-style beers. Requires cooler temperatures. | Cultivated from yeast |
| Ale Yeast | `ale_yeast` | Top-fermenting yeast for ales. Works at warmer temperatures, faster fermentation. | Cultivated from yeast |

### Intermediates (5)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Malt Extract | `malt_extract` | Concentrated malt extract from the mashing process. | Mash Tun |
| Wort Bucket | `wort_bucket` | Bucket of unfermented wort (sweet liquid). Stacks to 16. | Brew Kettle |
| Fermenting Beer | `fermenting_beer` | Beer actively undergoing fermentation. | Fermentation Tank |
| Green Beer | `green_beer` | Young, unmatured beer that needs conditioning. | Fermentation Tank (completed) |
| Conditioned Beer | `conditioned_beer` | Fully conditioned beer ready for bottling. | Conditioning Tank |

### Containers (5)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Beer Bottle | `beer_bottle` | Filled beer bottle with NBT data (type, quality, ABV). Stacks to 16. Consumable with alcohol effects. | Bottling Station |
| Beer Keg | `beer_keg` | Large beer container. Stacks to 4. | Crafting |
| Empty Beer Bottle | `beer_bottle_empty` | Empty glass bottle for beer filling. | Crafting |
| Empty Beer Can | `beer_can_empty` | Empty aluminum can for beer filling. | Crafting |
| Bottle Cap | `bottle_cap` | Standard bottle cap for sealing beer bottles. | Crafting |

### Additives & Specialty (6)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Crown Cap | `crown_cap` | Premium bottle cap for specialty beers. | Crafting |
| Brewing Sugar | `brewing_sugar` | Fermentable sugar for boosting alcohol content. | NPC trading or crafting |
| Irish Moss | `irish_moss` | Fining agent for clearing beer. | NPC trading |
| Brewing Salt | `brewing_salt` | Water treatment salt for adjusting mineral content. | NPC trading or crafting |
| Roasted Barley | `roasted_barley` | Dark-roasted barley for stouts and porters. | Roasting |
| Chocolate Malt | `chocolate_malt` | Dark malt with chocolate flavor notes for dark beers. | Malting Station |
| Caramel Malt | `caramel_malt` | Caramel-flavored malt for amber and red ales. | Malting Station |

---

## Wine Items (21 in `WineItems.java`)

Winemaking chain from grape seedling to bottled wine, covering 4 grape varieties (Riesling, Pinot Noir, Chardonnay, Merlot).

### Seedlings (4)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Riesling Grape Seedling | `riesling_grape_seedling` | Seedling for Riesling white wine grapes (WineType.RIESLING). | NPC trading or admin command |
| Pinot Noir Grape Seedling | `pinot_noir_grape_seedling` | Seedling for Pinot Noir red wine grapes (WineType.PINOT_NOIR). | NPC trading or admin command |
| Chardonnay Grape Seedling | `chardonnay_grape_seedling` | Seedling for Chardonnay white wine grapes (WineType.CHARDONNAY). | NPC trading or admin command |
| Merlot Grape Seedling | `merlot_grape_seedling` | Seedling for Merlot red wine grapes (WineType.MERLOT). | NPC trading or admin command |

### Grapes (4)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Riesling Grapes | `riesling_grapes` | Harvested Riesling grapes. Edible (2 nutrition). | Harvest grapevine |
| Pinot Noir Grapes | `pinot_noir_grapes` | Harvested Pinot Noir grapes. Edible (2 nutrition). | Harvest grapevine |
| Chardonnay Grapes | `chardonnay_grapes` | Harvested Chardonnay grapes. Edible (2 nutrition). | Harvest grapevine |
| Merlot Grapes | `merlot_grapes` | Harvested Merlot grapes. Edible (2 nutrition). | Harvest grapevine |

### Mash & Juice (8)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Riesling Mash | `riesling_mash` | Crushed Riesling grape mash. | Crushing Station |
| Pinot Noir Mash | `pinot_noir_mash` | Crushed Pinot Noir grape mash. | Crushing Station |
| Chardonnay Mash | `chardonnay_mash` | Crushed Chardonnay grape mash. | Crushing Station |
| Merlot Mash | `merlot_mash` | Crushed Merlot grape mash. | Crushing Station |
| Riesling Juice | `riesling_juice` | Pressed Riesling grape juice ready for fermentation. | Wine Press |
| Pinot Noir Juice | `pinot_noir_juice` | Pressed Pinot Noir grape juice. | Wine Press |
| Chardonnay Juice | `chardonnay_juice` | Pressed Chardonnay grape juice. | Wine Press |
| Merlot Juice | `merlot_juice` | Pressed Merlot grape juice. | Wine Press |

### Intermediates & Final Products (6)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Fermenting Wine | `fermenting_wine` | Wine actively undergoing fermentation. | Fermentation Tank |
| Young Wine | `young_wine` | Unmatured wine that requires aging. | Fermentation Tank (completed) |
| Wine Bottle | `wine_bottle` | Bottle of wine; volume (default 0.75L) and other data tracked via constructor/NBT, not separate registry IDs. Stacks to 16. | Bottling Station |
| Glass of Wine | `glass_of_wine` | Poured glass of wine for consumption. Provides buffs. Stacks to 1. | Pouring from bottle |

### Empty Bottles (1)

There is a single `empty_wine_bottle` item, not separate 375ml/750ml/1500ml registry IDs.

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Empty Wine Bottle | `empty_wine_bottle` | Empty bottle for filling. | Crafting |

---

## Coffee Items (21 in `CoffeeItems.java`)

Full coffee production from seedling to brewed cup across 4 varieties (Arabica, Robusta, Liberica, Excelsa).

### Seedlings (4)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Arabica Seedling | `arabica_seedling` | Seedling for Arabica coffee (CoffeeType.ARABICA). Premium flavor, moderate yield. | NPC trading or admin command |
| Robusta Seedling | `robusta_seedling` | Seedling for Robusta coffee (CoffeeType.ROBUSTA). Strong flavor, high yield. | NPC trading or admin command |
| Liberica Seedling | `liberica_seedling` | Seedling for Liberica coffee (CoffeeType.LIBERICA). Unique smoky flavor. | NPC trading or admin command |
| Excelsa Seedling | `excelsa_seedling` | Seedling for Excelsa coffee (CoffeeType.EXCELSA). Tart, fruity flavor. | NPC trading or admin command |

### Coffee Cherries (4)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Arabica Cherry | `arabica_cherry` | Fresh Arabica coffee cherry. Must be processed to extract beans. | Harvest mature Arabica plant |
| Robusta Cherry | `robusta_cherry` | Fresh Robusta coffee cherry. | Harvest mature Robusta plant |
| Liberica Cherry | `liberica_cherry` | Fresh Liberica coffee cherry. | Harvest mature Liberica plant |
| Excelsa Cherry | `excelsa_cherry` | Fresh Excelsa coffee cherry. | Harvest mature Excelsa plant |

### Green Beans (4)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Green Arabica Beans | `green_arabica_beans` | Unroasted Arabica coffee beans ready for roasting. | Wet Processing Station |
| Green Robusta Beans | `green_robusta_beans` | Unroasted Robusta coffee beans. | Wet Processing Station |
| Green Liberica Beans | `green_liberica_beans` | Unroasted Liberica coffee beans. | Wet Processing Station |
| Green Excelsa Beans | `green_excelsa_beans` | Unroasted Excelsa coffee beans. | Wet Processing Station |

### Processed Products (4)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Roasted Coffee Beans | `roasted_coffee_beans` | Roasted beans with NBT data (variety, roast level, quality). | Coffee Roaster |
| Ground Coffee | `ground_coffee` | Ground coffee ready for brewing or packaging. | Coffee Grinder |
| Brewed Coffee | `brewed_coffee` | Hot brewed coffee. Consumable with energy/speed buffs. | Brewing (in development) |
| Espresso | `espresso` | Concentrated espresso shot. Stronger buffs than brewed coffee. | Brewing (in development) |

### Packages (1)

There is a single `coffee_package` item, not separate 250g/500g/1kg
registry IDs; size is presumably tracked via NBT like other production
chains' weight fields.

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Coffee Package | `coffee_package` | Packaged coffee for sale. | Coffee Packaging Table |

### Tools (2)

`coffee_watering_can`, `coffee_fertilizer`, `growth_accelerator`, and
`quality_enhancer` do not exist as registered items.

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Pulping Tool | `pulping_tool` | Tool for removing cherry pulp from beans. Stacks to 1. | Crafting |
| Roasting Tray | `roasting_tray` | Tray used in the roasting process. Stacks to 16. | Crafting |

### Packaging (2)

There is a single `coffee_bag` item, not separate small/medium/large
registry IDs. `vacuum_seal` does not exist as a registered item.

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Coffee Bag | `coffee_bag` | Bag for packaged coffee. Stacks to 64. | Crafting |

---

## Cheese Items (7 in `CheeseItems.java`)

Cheese production from milk to aged wheels and wedges, with 5 varieties (Standard, Gouda, Emmental, Camembert, Parmesan).

### Raw Materials (2)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Milk Bucket | `milk_bucket` | Bucket of milk for cheese production. Stacks to 16. | Milking cows or NPC trading |
| Rennet | `rennet` | Coagulating enzyme used to curdle milk. | Crafting or NPC trading |

### Intermediates (2)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Cheese Curd | `cheese_curd` | Curdled milk with quality tracking. Ready for pressing. | Curdling Vat (milk + rennet) |
| Whey | `whey` | Liquid by-product of cheese curdling. | By-product of Curdling Vat |

### Cheese Wheel (1 item, 4 type variants)

There is only **one** registered wheel item, `cheese_wheel`
(`CheeseWheelItem`); Gouda/Emmental/Camembert/Parmesan are `CheeseType` NBT
variants of it, not separate registry IDs. There is also no separate wedge
item — cheese is not cut into wedges as an item.

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Cheese Wheel | `cheese_wheel` | Cheese wheel; NBT tracks `CheeseType` (Gouda/Emmental/Camembert/Parmesan), quality, and age. Stacks to 1. | Cheese Press + Aging Cave |

`CheeseProcessingMethod` (SMOKED, HERB) is a further NBT-tracked processing
variant on the same item — `smoked_cheese` and `herb_cheese` are not
separate registered items either.

### Specialty & Packaging (3)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Cheese Cloth | `cheese_cloth` | Cloth wrap for aging cheese. | Crafting |
| Wax Coating | `wax_coating` | Wax coating to seal cheese wheels during aging. | Crafting |
| Cheese Paper | `cheese_paper` | Paper wrapping for finished cheese products. | Crafting |

---

## Chocolate Items (35 in `ChocolateItems.java`)

Bean-to-bar chocolate production with cocoa processing, additives, molds, and finished products.

### Cocoa Raw Materials (5)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Raw Cocoa Beans | `cocoa_beans_raw` | Unprocessed cocoa beans. Edible (1 nutrition). | Farming or NPC trading |
| Roasted Cocoa Beans | `roasted_cocoa_beans` | Roasted beans with enhanced flavor. Edible (2 nutrition). | Roasting Station |
| Cocoa Nibs | `cocoa_nibs` | Crushed cocoa bean pieces. Edible (2 nutrition). | Winnowing Machine |
| Cocoa Mass | `cocoa_mass` | Ground cocoa paste (cocoa liquor + cocoa solids). | Grinding Mill |
| Cocoa Liquor | `cocoa_liquor` | Liquid cocoa mass, base for all chocolate products. | Grinding Mill |

### Processed Cocoa (3)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Cocoa Butter | `cocoa_butter` | Fat pressed from cocoa mass. Key ingredient for smooth chocolate. | Pressing Station |
| Cocoa Powder | `cocoa_powder` | Defatted cocoa powder for baking and hot chocolate. | Pressing Station |
| Cocoa Cake | `cocoa_cake` | Pressed cocoa cake, by-product of butter extraction. | Pressing Station |

### Additives (6)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Milk Powder | `milk_powder` | Dried milk for milk chocolate production. | NPC trading or crafting |
| Refined Sugar | `sugar_refined` | Refined sugar. Edible (1 nutrition). | NPC trading or crafting |
| Vanilla Extract | `vanilla_extract` | Flavoring for premium chocolate. | NPC trading |
| Lecithin | `lecithin` | Emulsifier for smooth chocolate texture. | NPC trading or crafting |
| Caramel | `caramel` | Caramel for filled chocolates. Edible (3 nutrition). | Cooking |
| Nougat | `nougat` | Nougat filling for pralines. Edible (3 nutrition). | Cooking |

### Nuts & Fruits (6)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Hazelnuts | `hazelnuts` | Raw hazelnuts. Edible (3 nutrition). | Farming or NPC trading |
| Almonds | `almonds` | Raw almonds. Edible (3 nutrition). | Farming or NPC trading |
| Roasted Hazelnuts | `roasted_hazelnuts` | Roasted hazelnuts for chocolate. Edible (3 nutrition, 0.6 saturation). | Roasting |
| Roasted Almonds | `roasted_almonds` | Roasted almonds for chocolate. Edible (3 nutrition, 0.6 saturation). | Roasting |
| Dried Fruits | `dried_fruits` | Dried fruit mix for chocolate. Edible (2 nutrition). | Drying |
| Raisins | `raisins` | Dried grapes for chocolate and baking. Edible (2 nutrition). | Drying |

### Intermediates (3)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Chocolate Mixture | `chocolate_mixture` | Raw chocolate mixture before conching. | Mixing |
| Conched Chocolate | `conched_chocolate` | Smooth chocolate after conching process. | Conching Machine |
| Tempered Chocolate | `tempered_chocolate` | Properly tempered chocolate with glossy finish. | Tempering Station |

### Molds & Packaging (7)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Chocolate Mold | `chocolate_mold` | Generic chocolate mold. Stacks to 16. | Crafting |
| Chocolate Bar Mold | `chocolate_mold_bar` | Bar-shaped mold for chocolate bars. Stacks to 16. | Crafting |
| Praline Mold | `chocolate_mold_praline` | Mold for individual pralines. Stacks to 16. | Crafting |
| Chocolate Wrapper | `chocolate_wrapper` | Standard foil wrapper for bars. | Crafting |
| Gold Wrapper | `chocolate_wrapper_gold` | Premium gold foil wrapper for luxury products. | Crafting |
| Chocolate Box | `chocolate_box` | Standard gift box for assorted chocolates. Stacks to 16. | Crafting |
| Premium Chocolate Box | `chocolate_box_premium` | Luxury gift box for premium assortments. Stacks to 16. | Crafting |

### Finished Products (3 + Specialties)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Chocolate Bar | `chocolate_bar` | Chocolate bar; weight (default 100g), type, quality and ingredients are tracked via NBT (`WeightKg`), not separate registry IDs. Stacks to 16. | Molding Station |
| Chocolate Truffle | `chocolate_truffle` | Handcrafted chocolate truffle. Edible (4 nutrition, 0.6 saturation). Stacks to 16. | Enrobing Machine |
| Chocolate Praline | `chocolate_praline` | Filled chocolate praline. Edible (3 nutrition, 0.5 saturation). Stacks to 16. | Molding Station (praline mold) |
| Hot Chocolate Mix | `hot_chocolate_mix` | Instant hot chocolate powder mix. | Grinding + mixing |

---

## Honey Items (22 in `HoneyItems.java`)

Beekeeping and honey production: raw materials, processing stages, by-products, packaging, and consumables.

### Honey Jars (3 Sizes)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Honey Jar | `honey_jar` | Jar of honey; size, type, quality, and origin are tracked via NBT, not separate registry IDs. Stacks to 16. | Bottling Station |

### Raw Honeycomb (3)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Raw Honeycomb | `raw_honeycomb` | Unprocessed honeycomb from the hive. Edible (1 nutrition). | Beehive or Apiary (with Hive Tool) |
| Filtered Honeycomb | `filtered_honeycomb` | Cleaned and filtered honeycomb. | Filtering Station |
| Honeycomb Chunk | `honeycomb_chunk` | Cut piece of honeycomb. Edible (2 nutrition). | Processing |

### Liquid Honey (3)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Raw Honey Bucket | `raw_honey_bucket` | Bucket of unfiltered raw honey. Stacks to 1. | Honey Extractor |
| Filtered Honey Bucket | `filtered_honey_bucket` | Bucket of filtered, clean honey. Stacks to 1. | Filtering Station |
| Liquid Honey Bottle | `liquid_honey_bottle` | Bottle of liquid honey. Stacks to 16. | Bottling |

### By-Products (5)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Beeswax | `beeswax` | Raw beeswax from honeycomb processing. | Honey Extractor (by-product) |
| Beeswax Block | `beeswax_block` | Compressed block of beeswax for crafting. | Pressing beeswax |
| Propolis | `propolis` | Antimicrobial resin collected by bees. | Beehive harvest |
| Pollen | `pollen` | Bee pollen collected from hives. | Beehive harvest |
| Royal Jelly | `royal_jelly` | Rare royal jelly with strong nutrition. Edible (4 nutrition, 0.8 saturation). | Advanced Beehive or Apiary |

### Packaging Materials (5)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Glass Jar | `glass_jar` | Glass jar for honey (single item; no separate small/large registry IDs). | Crafting |
| Jar Lid | `jar_lid` | Standard metal lid for jars. | Crafting |
| Gold Jar Lid | `jar_lid_gold` | Premium gold-colored lid for jars. | Crafting |

### Processed Honey (3)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Creamed Honey | `creamed_honey` | Whipped, spreadable honey variant. | Creaming Station |
| Crystallized Honey | `crystallized_honey` | Naturally crystallized honey. Edible (3 nutrition). | Aging Chamber |
| Honey Crystals | `honey_crystals` | Dehydrated honey crystals. Edible (2 nutrition). | Processing Station |

### Tools & Equipment (3)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Smoker | `smoker` | Bee smoker tool to calm bees during harvest. 256 durability. Stacks to 1. | Crafting |
| Hive Tool | `hive_tool` | Metal tool for prying open hives and extracting frames. 512 durability. Stacks to 1. | Crafting |

### Consumables (2)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Honey Candy | `honey_candy` | Sweet honey candy. Edible (3 nutrition, fast eating). | Cooking/crafting |
| Honeycomb Treat | `honeycomb_treat` | Chocolate-dipped honeycomb treat. Edible (4 nutrition). | Cooking/crafting |

---

## Vehicle Items (36)

Modular vehicle system with complete vehicles, individual parts, fuel, tools, and accessories.

### Complete Vehicles (5)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Limousine | `limousine` | Pre-built sedan with Limousine chassis. 4 seats, fast, light. | Admin command or Vehicle Spawn Tool |
| Van | `van` | Pre-built cargo van with Van chassis. 8 seats, medium speed. | Admin command or Vehicle Spawn Tool |
| Truck | `truck` | Pre-built heavy truck with Truck chassis. 2 seats, slow, maximum cargo. | Admin command or Vehicle Spawn Tool |
| SUV | `suv` | Pre-built SUV with Offroad chassis. 5 seats, medium speed, all-terrain. | Admin command or Vehicle Spawn Tool |
| Sports Car | `sports_car` | Pre-built sports car with Luxus chassis. 2 seats, very fast. | Admin command or Vehicle Spawn Tool |

### Engines (3)

Engines are **not obtainable items** — `normal_motor`/`performance_motor`/
`performance_2_motor` are `Part` configuration objects in `PartRegistry`,
selected and paid for directly in the Vehicle Workshop GUI (`GuiWorkshop`),
with no crafting recipe and no item form.

| Name | Registry ID | Description |
|------|-------------|-------------|
| Normal Motor | `normal_motor` | Standard engine. 100 HP, standard fuel efficiency, 1.0x speed. |
| Performance Motor | `performance_motor` | Upgraded engine. 150 HP, poor fuel efficiency, 1.5x speed. |
| Performance 2 Motor | `performance_2_motor` | Top-tier engine. 200 HP, good fuel efficiency, high speed. |

### Tires - Standard (3)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Standard Tire | `standard_tire` | Basic road tire. Standard durability and speed. | Crafting or NPC trading |
| Sport Tire | `sport_tire` | Performance road tire. +20% speed, reduced durability. | Crafting or NPC trading |
| Premium Tire | `premium_tire` | All-purpose premium tire. +10% speed, +50% durability. | Crafting or NPC trading |

### Tires - Truck (3)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Offroad Tire | `offroad_tire` | Rugged offroad tire for dirt and rough terrain. Heavy load rated. | Crafting or NPC trading |
| Allterrain Tire | `allterrain_tire` | Versatile tire for all surface types. Heavy load rated. | Crafting or NPC trading |
| Heavy Duty Tire | `heavyduty_tire` | Maximum load capacity tire for trucks. Maximum durability. | Crafting or NPC trading |

### Chassis (5)

Like engines, chassis are workshop-only `Part` configs — not items.

| Name | Registry ID | Description |
|------|-------------|-------------|
| Limousine Chassis | `limousine_chassis` | Sedan body frame. 4 seats, 2 module slots, lightweight. |
| Van Chassis | `van_chassis` | Van body frame. 8 seats, 4 module slots, medium weight. |
| Truck Chassis | `truck_chassis` | Truck body frame. 2 seats, 6 module slots, heavy. |
| Offroad Chassis | `offroad_chassis` | SUV body frame. 5 seats, 3 module slots, medium weight, off-road capable. |
| Luxus Chassis | `luxus_chassis` | Sports car body frame. 2 seats, 2 module slots, lightweight, aerodynamic. |

### Fenders (3)

Workshop-only `Part` configs — not items.

| Name | Registry ID | Description |
|------|-------------|-------------|
| Basic Fender | `fender_basic` | Standard bumper with basic impact protection. |
| Chrome Fender | `fender_chrome` | Chrome-plated bumper. +10% durability, shiny appearance. |
| Sport Fender | `fender_sport` | Aerodynamic sport bumper. +5% speed bonus. |

### Modules (2)

Workshop-only `Part` configs — not items. (There is no separate license
plate holder part; the license plate itself is an `InternalVehiclePartItem`
variant added automatically when a vehicle is spawned.)

| Name | Registry ID | Description |
|------|-------------|-------------|
| Cargo Module | `cargo_module` | Adds inventory slots to the vehicle for item storage. |
| Fluid Module | `fluid_module` | Adds liquid tank capacity to the vehicle. |

### Fuel Tanks (3)

Workshop-only `Part` configs — not items.

| Name | Registry ID | Description |
|------|-------------|-------------|
| Tank 15L | `tank_15l` | Small fuel tank, 15 liter capacity. Lightweight. |
| Tank 30L | `tank_30l` | Standard fuel tank, 30 liter capacity. |
| Tank 50L | `tank_50l` | Large fuel tank, 50 liter capacity. Heavy. |

### Tools & Accessories (6)

There is no separate `empty_diesel_can`; the registered canister item is
`full_diesel_can` only. The license plate is not its own registered item
either — it's an `internal_vehicle_part` NBT variant tagged `license_sign`,
added automatically when a vehicle is spawned.

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Full Diesel Can | `full_diesel_can` | Diesel canister filled with fuel. Single use, consumed on refueling. Also used as reagent in coca processing. | Fuel Station or crafting |
| Maintenance Kit | `maintenance_kit` | Repair tool for damaged vehicles. Multiple uses before depleted. | Crafting or NPC trading |
| Vehicle Key | `key` | Ignition key bound to a specific vehicle. Required to start and lock/unlock. | Created when vehicle is spawned |
| Starter Battery | `starter_battery` | Jump-start battery for vehicles with dead batteries. Limited uses. | Crafting or NPC trading |
| Internal Vehicle Part | `internal_vehicle_part` | Generic wrapper item for internal parts (e.g. the license plate) shown in vehicle inventories. | Created automatically |
| Vehicle Spawn Tool | `spawn_tool` | Admin tool for spawning and configuring vehicles. Unlimited uses. | Admin command |

### Block Items (2)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Fuel Station | `fuel_station` | Item form of the Fuel Station block. Place to create a vehicle refueling point. | Admin command or crafting |
| Garage | `workshop` | Item form of the Garage (Werkstatt) block. Place to create a vehicle repair station. | Admin command or crafting |

---

## Item Categories Summary

| Category | Count | Key Features |
|----------|-------|--------------|
| Economy | 4 | Cash, plot wand, universal packaging |
| NPC Tools | 5 | Admin tools for NPC spawning, scheduling, and removal |
| Lock | 13 | 5 lock types, 3 key tiers, lock picks, 3 hacking tools |
| Tobacco | 26 | 4 strains, fresh/dried/fermented leaves, tools, soil, packaging |
| Cannabis | 10 | 4 strains, 4-stage processing, hash, oil, trim |
| Coca | 10 | 3 strains, cocaine/crack chain, chemicals |
| Poppy | 8 | 3 strains, opium/morphine/heroin chain |
| Meth | 8 | 4 chemicals, 4-step synthesis |
| LSD | 6 | Ergot-based lab synthesis, blotter production |
| MDMA | 6 | Safrol synthesis, pill press minigame |
| Mushroom | 12 | 3 strains, mist bags, climate-controlled growth |
| Beer | 30 | 3 grains, 4 yeasts, 4 hops, full brewing chain |
| Wine | 22 | 4 grape varieties, 3 bottle sizes, aging system |
| Coffee | 26 | 4 varieties, wet/dry processing, roasting, grinding |
| Cheese | 19 | 5 cheese types, wheels/wedges, smoking/herb infusion |
| Chocolate | 33 | Bean-to-bar, 6 additives, molds, 3 bar sizes |
| Honey | 27 | Beekeeping, 3 jar sizes, by-products, tools |
| Vehicle | 36 | 5 vehicles, modular parts, fuel system |

**Grand Total: 374 Registered Items**

---

[Back to Wiki Home](Home.md) | [Blocks Reference](Blocks.md) | [Commands Reference](Commands.md)

---

## Dokumentationsstatus

- Zuletzt gegen den aktuellen Repository-Stand abgeglichen am **2026-04-13**.
- Diese Datei wurde im Rahmen der Vollständigkeits-Aktualisierung überarbeitet.
- Referenz für Live-Metriken: `docs/REPO_METRICS.md` (neu generiert).

<!-- BEGIN SOURCE-ITEM-REGISTRY -->
# Source item registry

Generated from `ITEMS.register("id")` in source. **374 IDs**. Narrative tables above may use alias IDs that are not the registry name.

## `de/rolandsw/schedulemc/beer/blocks/BeerBlocks.java` (12)

- `malting_station`
- `mash_tun`
- `small_brew_kettle`
- `medium_brew_kettle`
- `large_brew_kettle`
- `small_beer_fermentation_tank`
- `medium_beer_fermentation_tank`
- `large_beer_fermentation_tank`
- `small_conditioning_tank`
- `medium_conditioning_tank`
- `large_conditioning_tank`
- `beer_bottling_station`

## `de/rolandsw/schedulemc/beer/items/BeerItems.java` (30)

- `beer_bottle`
- `barley`
- `rye`
- `malted_barley`
- `malted_wheat`
- `malted_rye`
- `hops_cone`
- `dried_hops`
- `hop_extract`
- `hop_pellets`
- `yeast`
- `brewing_yeast`
- `lager_yeast`
- `ale_yeast`
- `malt_extract`
- `wort_bucket`
- `fermenting_beer`
- `green_beer`
- `conditioned_beer`
- `beer_keg`
- `beer_bottle_empty`
- `beer_can_empty`
- `bottle_cap`
- `crown_cap`
- `brewing_sugar`
- `irish_moss`
- `brewing_salt`
- `roasted_barley`
- `chocolate_malt`
- `caramel_malt`

## `de/rolandsw/schedulemc/cannabis/blocks/CannabisBlocks.java` (4)

- `cannabis_trim_station`
- `cannabis_curing_jar`
- `cannabis_hash_press`
- `cannabis_oil_extractor`

## `de/rolandsw/schedulemc/cannabis/items/CannabisItems.java` (9)

- `cannabis_seed`
- `fresh_cannabis_bud`
- `dried_cannabis_bud`
- `trimmed_cannabis_bud`
- `cured_cannabis_bud`
- `cannabis_trim`
- `cannabis_hash`
- `cannabis_oil`
- `extraction_solvent`

## `de/rolandsw/schedulemc/cheese/blocks/CheeseBlocks.java` (9)

- `pasteurization_station`
- `curdling_vat`
- `small_cheese_press`
- `medium_cheese_press`
- `large_cheese_press`
- `small_aging_cave`
- `medium_aging_cave`
- `large_aging_cave`
- `packaging_station`

## `de/rolandsw/schedulemc/cheese/items/CheeseItems.java` (7)

- `rennet`
- `cheese_curd`
- `whey`
- `cheese_wheel`
- `cheese_cloth`
- `wax_coating`
- `cheese_paper`

## `de/rolandsw/schedulemc/chocolate/blocks/ChocolateBlocks.java` (15)

- `roasting_station`
- `winnowing_machine`
- `grinding_mill`
- `pressing_station`
- `small_conching_machine`
- `medium_conching_machine`
- `large_conching_machine`
- `tempering_station`
- `small_molding_station`
- `medium_molding_station`
- `large_molding_station`
- `enrobing_machine`
- `cooling_tunnel`
- `wrapping_station`
- `chocolate_storage_cabinet`

## `de/rolandsw/schedulemc/chocolate/items/ChocolateItems.java` (35)

- `cocoa_beans_raw`
- `roasted_cocoa_beans`
- `cocoa_nibs`
- `cocoa_shells`
- `cocoa_mass`
- `cocoa_liquor`
- `cocoa_butter`
- `cocoa_powder`
- `cocoa_cake`
- `milk_powder`
- `sugar_refined`
- `vanilla_extract`
- `lecithin`
- `caramel`
- `nougat`
- `hazelnuts`
- `almonds`
- `roasted_hazelnuts`
- `roasted_almonds`
- `dried_fruits`
- `raisins`
- `conched_chocolate`
- `tempered_chocolate`
- `chocolate_mixture`
- `chocolate_mold`
- `chocolate_mold_bar`
- `chocolate_mold_praline`
- `chocolate_wrapper`
- `chocolate_wrapper_gold`
- `chocolate_box`
- `chocolate_box_premium`
- `chocolate_bar`
- `chocolate_truffle`
- `chocolate_praline`
- `hot_chocolate_mix`

## `de/rolandsw/schedulemc/coca/items/CocaItems.java` (12)

- `bolivian_coca_seeds`
- `colombian_coca_seeds`
- `peruvian_coca_seeds`
- `fresh_bolivian_coca_leaf`
- `fresh_colombian_coca_leaf`
- `fresh_peruvian_coca_leaf`
- `coca_paste_bolivian`
- `coca_paste_colombian`
- `coca_paste_peruvian`
- `cocaine`
- `crack_rock`
- `baking_powder`

## `de/rolandsw/schedulemc/coffee/items/CoffeeItems.java` (21)

- `arabica_seedling`
- `robusta_seedling`
- `liberica_seedling`
- `excelsa_seedling`
- `arabica_cherry`
- `robusta_cherry`
- `liberica_cherry`
- `excelsa_cherry`
- `green_arabica_beans`
- `green_robusta_beans`
- `green_liberica_beans`
- `green_excelsa_beans`
- `roasted_coffee_beans`
- `ground_coffee`
- `coffee_package`
- `pulping_tool`
- `roasting_tray`
- `coffee_bag`
- `vacuum_seal`
- `brewed_coffee`
- `espresso`

## `de/rolandsw/schedulemc/economy/blocks/EconomyBlocks.java` (2)

- `cash_block`
- `atm`

## `de/rolandsw/schedulemc/honey/blocks/HoneyBlocks.java` (14)

- `beehive`
- `advanced_beehive`
- `apiary`
- `honey_extractor`
- `centrifugal_extractor`
- `filtering_station`
- `small_aging_chamber`
- `medium_aging_chamber`
- `large_aging_chamber`
- `processing_station`
- `creaming_station`
- `bottling_station`
- `honey_storage_barrel`
- `honey_display_case`

## `de/rolandsw/schedulemc/honey/items/HoneyItems.java` (22)

- `honey_jar`
- `raw_honeycomb`
- `filtered_honeycomb`
- `honeycomb_chunk`
- `raw_honey_bucket`
- `filtered_honey_bucket`
- `liquid_honey_bottle`
- `beeswax`
- `beeswax_block`
- `propolis`
- `pollen`
- `royal_jelly`
- `glass_jar`
- `jar_lid`
- `jar_lid_gold`
- `creamed_honey`
- `crystallized_honey`
- `honey_crystals`
- `smoker`
- `hive_tool`
- `honey_candy`
- `honeycomb_treat`

## `de/rolandsw/schedulemc/items/ModItems.java` (3)

- `plot_selection_tool`
- `cash`
- `packaged_drug`

## `de/rolandsw/schedulemc/lock/items/LockItems.java` (13)

- `simple_lock`
- `security_lock`
- `high_security_lock`
- `combination_lock`
- `dual_lock`
- `key_blank_copper`
- `key_blank_iron`
- `key_blank_netherite`
- `key_ring`
- `lock_pick`
- `hacking_code_cracker`
- `hacking_bypass`
- `hacking_omni`

## `de/rolandsw/schedulemc/lsd/items/LSDItems.java` (6)

- `ergot`
- `blotter_paper`
- `ergot_culture`
- `lysergic_acid`
- `lsd_solution`
- `lsd_blotter`

## `de/rolandsw/schedulemc/mdma/items/MDMAItems.java` (6)

- `safrole`
- `binding_agent`
- `pill_dye`
- `mdma_base`
- `mdma_crystal`
- `ecstasy_pill`

## `de/rolandsw/schedulemc/meth/items/MethItems.java` (8)

- `ephedrine`
- `pseudoephedrine`
- `red_phosphorus`
- `iodine`
- `meth_paste`
- `raw_meth`
- `crystal_meth`
- `meth`

## `de/rolandsw/schedulemc/mushroom/blocks/MushroomBlocks.java` (4)

- `climate_lamp_small`
- `climate_lamp_medium`
- `climate_lamp_large`
- `water_tank`

## `de/rolandsw/schedulemc/mushroom/items/MushroomItems.java` (12)

- `manure_bag_small`
- `manure_bag_medium`
- `manure_bag_large`
- `spore_syringe_cubensis`
- `spore_syringe_azurescens`
- `spore_syringe_mexicana`
- `fresh_cubensis`
- `fresh_azurescens`
- `fresh_mexicana`
- `dried_cubensis`
- `dried_azurescens`
- `dried_mexicana`

## `de/rolandsw/schedulemc/npc/items/NPCItems.java` (5)

- `npc_spawner_tool`
- `npc_location_tool`
- `npc_leisure_tool`
- `npc_patrol_tool`
- `entity_remover`

## `de/rolandsw/schedulemc/poppy/items/PoppyItems.java` (10)

- `afghan_poppy_seeds`
- `turkish_poppy_seeds`
- `indian_poppy_seeds`
- `afghan_poppy_pod`
- `turkish_poppy_pod`
- `indian_poppy_pod`
- `raw_opium`
- `morphine`
- `heroin`
- `scoring_knife`

## `de/rolandsw/schedulemc/region/blocks/PlotBlocks.java` (2)

- `plot_info_block`
- `industrial_floor`

## `de/rolandsw/schedulemc/secretdoors/SecretDoors.java` (5)

- `secret_door`
- `hatch`
- `hidden_switch_stone`
- `remote_control`
- `elevator`

## `de/rolandsw/schedulemc/tobacco/items/TobaccoItems.java` (26)

- `virginia_seeds`
- `burley_seeds`
- `oriental_seeds`
- `havana_seeds`
- `fresh_virginia_leaf`
- `fresh_burley_leaf`
- `fresh_oriental_leaf`
- `fresh_havana_leaf`
- `dried_virginia_leaf`
- `dried_burley_leaf`
- `dried_oriental_leaf`
- `dried_havana_leaf`
- `fermented_virginia_leaf`
- `fermented_burley_leaf`
- `fermented_oriental_leaf`
- `fermented_havana_leaf`
- `fertilizer_bottle`
- `growth_booster_bottle`
- `quality_booster_bottle`
- `watering_can`
- `soil_bag_small`
- `soil_bag_medium`
- `soil_bag_large`
- `packaging_bag`
- `packaging_jar`
- `packaging_box`

## `de/rolandsw/schedulemc/warehouse/WarehouseBlocks.java` (1)

- `warehouse`

## `de/rolandsw/schedulemc/weapon/item/WeaponItems.java` (28)

- `ak47_magazine`
- `pistol_magazine`
- `shotgun_shells`
- `sniper_magazine`
- `mp5_magazine`
- `ammo_standard`
- `ammo_ap`
- `ammo_tracer`
- `ammo_rubber`
- `rifle_ammo`
- `pistol_ammo`
- `ak47`
- `pistol`
- `shotgun`
- `sniper`
- `revolver`
- `mp5`
- `baseball_bat`
- `machete`
- `combat_knife`
- `frag_grenade`
- `smoke_grenade`
- `flash_grenade`
- `scope`
- `silencer`
- `upgrade_single_precision`
- `upgrade_burst`
- `upgrade_auto`

## `de/rolandsw/schedulemc/wine/blocks/WineBlocks.java` (11)

- `crushing_station`
- `small_wine_press`
- `medium_wine_press`
- `large_wine_press`
- `small_fermentation_tank`
- `medium_fermentation_tank`
- `large_fermentation_tank`
- `small_aging_barrel`
- `medium_aging_barrel`
- `large_aging_barrel`
- `wine_bottling_station`

## `de/rolandsw/schedulemc/wine/items/WineItems.java` (21)

- `riesling_grape_seedling`
- `pinot_noir_grape_seedling`
- `chardonnay_grape_seedling`
- `merlot_grape_seedling`
- `riesling_grapes`
- `pinot_noir_grapes`
- `chardonnay_grapes`
- `merlot_grapes`
- `riesling_mash`
- `pinot_noir_mash`
- `chardonnay_mash`
- `merlot_mash`
- `riesling_juice`
- `pinot_noir_juice`
- `chardonnay_juice`
- `merlot_juice`
- `fermenting_wine`
- `young_wine`
- `wine_bottle`
- `empty_wine_bottle`
- `glass_of_wine`

<!-- END SOURCE-ITEM-REGISTRY -->
