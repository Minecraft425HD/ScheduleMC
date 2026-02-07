# Complete Items Reference - ScheduleMC

**Total Registered Items:** 250+ items across 18 categories

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

## Economy Items (4)

Items related to the server economy, plot management, and universal packaging.

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Cash | `cash` | Physical money item with variable value stored in NBT. Can be dropped, traded, and stolen. Deposit at ATM blocks. | `/money withdraw <amount>` at an ATM block |
| Cash Block (Item) | `cash_block` | Block item form of the Cash Block. Decorative money storage that displays as a stack of bills. | Crafting or admin command |
| Plot Wand | `plot_selection_tool` | Plot selection tool similar to WorldEdit's wand. Left-click sets position 1, right-click sets position 2 to define a rectangular region. | `/plot wand` |
| Packaged Drug | `packaged_drug` | Universal packaging item for all processed substances. Stores drug type, quality, weight (1g-100g), variant, and package date in NBT data. Price is calculated dynamically. | Produced at Packaging Tables |

---

## NPC Tools (5)

Admin-only tools (OP Level 2) for spawning and configuring NPCs.

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| NPC Spawner Tool | `npc_spawner_tool` | Right-click to open the NPC spawn GUI. Configure NPC type (Resident, Merchant, Police), name, and skin before spawning. | `/npc give spawner` or admin command |
| NPC Location Tool | `npc_location_tool` | Sets home and work locations for NPCs. Right-click an NPC to select it, then right-click a block to assign home or work positions for their daily schedule. | `/npc give location` or admin command |
| NPC Leisure Tool | `npc_leisure_tool` | Adds leisure locations for NPCs (maximum 10 per NPC). Right-click an NPC to select, then right-click blocks to mark leisure spots that the NPC will visit randomly during free time. | `/npc give leisure` or admin command |
| NPC Patrol Tool | `npc_patrol_tool` | Sets patrol routes for Police-type NPCs. Right-click to add sequential waypoints that the NPC will walk between during patrol duty. | `/npc give patrol` or admin command |
| Entity Remover | `entity_remover` | Removes any custom ScheduleMC entity (NPCs, vehicles, etc.) by right-clicking on it. Admin cleanup tool. | Admin command |

---

## Lock Items (13)

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

## Tobacco Items (26)

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

## Cannabis Items (10)

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
| Cannabis Oil | `cannabis_oil` | Extracted oil concentrate, 3x potency of buds. Stacks to 16. | Oel Extraktor (3 cured buds + solvent) |

### Processing Tools

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Pollen Press Mold | `pollen_press_mold` | Required tool for the Hash Presse block. 100 uses before breaking. Does not stack. | Crafting |
| Extraction Solvent | `extraction_solvent` | Chemical solvent consumed during oil extraction. Stacks to 16. 1 consumed per extraction cycle. | Crafting or NPC trading |

---

## Coca Items (10)

Three coca strains, fresh leaves, and the cocaine/crack processing chain along with required chemicals.

### Seeds (3 Strains)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Bolivianisch Coca Seeds | `bolivianisch_coca_seeds` | Seeds for Bolivianisch coca (CocaType.BOLIVIANISCH). Higher quantity yield. ~15 min growth. | Admin command or NPC trading |
| Kolumbianisch Coca Seeds | `kolumbianisch_coca_seeds` | Seeds for Kolumbianisch coca (CocaType.KOLUMBIANISCH). Higher purity output. ~18 min growth. | Admin command or NPC trading |
| Peruanisch Coca Seeds | `peruanisch_coca_seeds` | Seeds for Peruanisch coca (CocaType.PERUANISCH). Balanced yield and quality. Uses Bolivianisch plant block internally. | Admin command or NPC trading |

### Fresh Leaves (3 Types)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Fresh Bolivianisch Coca Leaf | `fresh_bolivianisch_coca_leaf` | Freshly harvested Bolivianisch coca leaf. Stacks to 16. Input for extraction. | Harvest mature Bolivianisch coca plant |
| Fresh Kolumbianisch Coca Leaf | `fresh_kolumbianisch_coca_leaf` | Freshly harvested Kolumbianisch coca leaf. Stacks to 16. Input for extraction. | Harvest mature Kolumbianisch coca plant |
| Fresh Peruanisch Coca Leaf | `fresh_peruanisch_coca_leaf` | Freshly harvested Peruanisch coca leaf. Stacks to 16. Input for extraction. | Harvest mature Peruanisch coca plant |

### Processing Chain & Chemicals

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Coca Paste | `coca_paste` | Brown intermediate paste from leaf extraction. Stacks to 16. Requires diesel and leaves. | Extraction Vat (leaves + diesel canister) |
| Cocaine | `cocaine` | White powder refined from coca paste. Stacks to 16. Purity 70-95%. Packageable. | Refinery (from coca paste) |
| Crack Rock | `crack_rock` | Crystallized form of cocaine cooked with baking soda. Stacks to 16. Packageable. | Crack Kocher (cocaine + backpulver) |
| Backpulver | `backpulver` | Baking soda, a required reagent for crack production. Stacks to 64. Ratio 1:1 with cocaine. | Crafting or NPC trading |

---

## Poppy Items (8)

Three poppy strains and the full opium-to-heroin refinement chain.

### Seeds (3 Strains)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Afghanisch Poppy Seeds | `afghanisch_poppy_seeds` | Seeds for Afghanisch poppy (PoppyType.AFGHANISCH). High opium yield. ~12 min growth. | Admin command or NPC trading |
| Tuerkisch Poppy Seeds | `tuerkisch_poppy_seeds` | Seeds for Tuerkisch poppy (PoppyType.TUERKISCH). Medium opium yield. ~15 min growth. | Admin command or NPC trading |
| Indisch Poppy Seeds | `indisch_poppy_seeds` | Seeds for Indisch poppy (PoppyType.INDISCH). Premium quality opium. ~18 min growth. | Admin command or NPC trading |

### Processing Chain

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Poppy Pod | `poppy_pod` | Harvested seed pod from mature poppy plant. Stacks to 16. Must be scored to extract opium. | Harvest mature poppy plant |
| Raw Opium | `raw_opium` | Dark brown raw opium extracted from scored pods. Stacks to 16. Intermediate product. | Ritzmaschine or Scoring Knife |
| Morphine | `morphine` | Morphine base refined from pressed opium. Stacks to 16. Purity 60-85%. | Kochstation (cooking station) |
| Heroin | `heroin` | White powder refined from morphine. Stacks to 16. Purity 80-99%. Packageable. | Heroin Raffinerie |

### Tool

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Scoring Knife | `scoring_knife` | Hand tool for manually scoring poppy pods to extract opium. Alternative to the Ritzmaschine block. Stacks to 1 with durability. | Crafting |

---

## Meth Items (8)

Chemical synthesis items for the four-step methamphetamine production process.

### Base Chemicals (4)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Ephedrin | `ephedrin` | Primary precursor chemical for meth synthesis. Regulated substance. Stacks to 16. | NPC trading or crafting |
| Pseudoephedrin | `pseudoephedrin` | Alternative precursor chemical for meth synthesis. Regulated substance. Stacks to 16. | NPC trading or crafting |
| Roter Phosphor | `roter_phosphor` | Red phosphorus, a flammable reagent used in the reduction step. Stacks to 16. | Crafting |
| Jod | `jod` | Iodine, a corrosive reagent used in the reduction step. Stacks to 16. | Crafting |

### Processing Chain (4)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Meth Paste | `meth_paste` | Wet intermediate paste from mixing base chemicals. Stacks to 16. | Chemie Mixer (step 1) |
| Roh Meth | `roh_meth` | Raw methamphetamine from the dangerous reduction step. Stacks to 16. Explosion risk during production. | Reduktionskessel (step 2) |
| Kristall Meth | `kristall_meth` | Crystallized methamphetamine with visible crystal structure. Stacks to 16. | Kristallisator (step 3) |
| Meth | `meth` | Final dried methamphetamine product, 95-99% purity. Stacks to 16. Packageable. | Vakuum Trockner (step 4) |

---

## LSD Items (6)

Laboratory synthesis items for the precision LSD production process.

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Mutterkorn | `mutterkorn` | Ergot fungus, the raw starting material. Toxic if consumed raw. Stacks to 16. | Specialized farming or NPC trading |
| Blotter Papier | `blotter_papier` | Absorbent blotter paper sheet (10x10 grid). Used in the final dosing step. Stacks to 64. | Crafting |
| Ergot Kultur | `ergot_kultur` | Fermented ergot culture, intermediate product from mutterkorn fermentation. Stacks to 16. | Fermentations Tank (step 1) |
| Lysergsaeure | `lysergsaeure` | Lysergic acid distilled from ergot culture. High-precision intermediate. Stacks to 16. | Destillations Apparat (step 2) |
| LSD Loesung | `lsd_loesung` | LSD solution at 100ug/ml concentration. Ready for blotter application. Stacks to 16. | Chemical synthesis from lysergic acid |
| LSD Blotter | `lsd_blotter` | Finished LSD blotter tabs (100 per sheet). Final product. Stacks to 16. | Mikro Dosierer + Perforations Presse (steps 3-4) |

---

## MDMA Items (6)

Chemical synthesis and pill-pressing items for ecstasy production.

### Raw Materials (3)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Safrol | `safrol` | Base chemical extracted from sassafras. Primary precursor for MDMA synthesis. Stacks to 16. | Extraction or NPC trading |
| Bindemittel | `bindemittel` | Pill binder material that holds ecstasy pills together. Stacks to 64. | Crafting |
| Pillen Farbstoff | `pillen_farbstoff` | Pill dye for coloring ecstasy pills. Customizable colors. Stacks to 64. | Crafting |

### Processing Chain (3)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| MDMA Base | `mdma_base` | Liquid MDMA base synthesized from safrol. Stacks to 16. | Reaktions Kessel (step 1) |
| MDMA Kristall | `mdma_kristall` | Dried MDMA crystals. Purity 80-95%. Stacks to 16. | Trocknungs Ofen (step 2) |
| Ecstasy Pill | `ecstasy_pill` | Finished pressed ecstasy pill with custom shape and color. Stacks to 64. Produced via timing minigame. | Pillen Presse (step 3, minigame) |

---

## Mushroom Items (12)

Psilocybin mushroom cultivation items across three strains: Cubensis, Azurescens, and Mexicana.

### Mist Bags (3 Sizes)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Small Mist Bag | `mist_bag_small` | Small substrate bag for mushroom cultivation. Holds 1 spore syringe, produces up to 3 mushrooms. Stacks to 16. | Crafting or NPC trading |
| Medium Mist Bag | `mist_bag_medium` | Medium substrate bag. Holds 2 spore syringes, produces up to 6 mushrooms. Stacks to 8. | Crafting or NPC trading |
| Large Mist Bag | `mist_bag_large` | Large substrate bag. Holds 3 spore syringes, produces up to 9 mushrooms. Stacks to 4. | Crafting or NPC trading |

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

## Beer Items (30)

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

## Wine Items (22)

Winemaking chain from grape seedling to bottled wine, covering 4 grape varieties (Riesling, Spaetburgunder, Chardonnay, Merlot).

### Seedlings (4)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Riesling Grape Seedling | `riesling_grape_seedling` | Seedling for Riesling white wine grapes (WineType.RIESLING). | NPC trading or admin command |
| Spaetburgunder Grape Seedling | `spaetburgunder_grape_seedling` | Seedling for Spaetburgunder (Pinot Noir) red wine grapes (WineType.SPAETBURGUNDER). | NPC trading or admin command |
| Chardonnay Grape Seedling | `chardonnay_grape_seedling` | Seedling for Chardonnay white wine grapes (WineType.CHARDONNAY). | NPC trading or admin command |
| Merlot Grape Seedling | `merlot_grape_seedling` | Seedling for Merlot red wine grapes (WineType.MERLOT). | NPC trading or admin command |

### Grapes (4)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Riesling Grapes | `riesling_grapes` | Harvested Riesling grapes. Edible (2 nutrition). | Harvest grapevine |
| Spaetburgunder Grapes | `spaetburgunder_grapes` | Harvested Spaetburgunder grapes. Edible (2 nutrition). | Harvest grapevine |
| Chardonnay Grapes | `chardonnay_grapes` | Harvested Chardonnay grapes. Edible (2 nutrition). | Harvest grapevine |
| Merlot Grapes | `merlot_grapes` | Harvested Merlot grapes. Edible (2 nutrition). | Harvest grapevine |

### Mash & Juice (8)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Riesling Mash | `riesling_mash` | Crushed Riesling grape mash. | Crushing Station |
| Spaetburgunder Mash | `spaetburgunder_mash` | Crushed Spaetburgunder grape mash. | Crushing Station |
| Chardonnay Mash | `chardonnay_mash` | Crushed Chardonnay grape mash. | Crushing Station |
| Merlot Mash | `merlot_mash` | Crushed Merlot grape mash. | Crushing Station |
| Riesling Juice | `riesling_juice` | Pressed Riesling grape juice ready for fermentation. | Wine Press |
| Spaetburgunder Juice | `spaetburgunder_juice` | Pressed Spaetburgunder grape juice. | Wine Press |
| Chardonnay Juice | `chardonnay_juice` | Pressed Chardonnay grape juice. | Wine Press |
| Merlot Juice | `merlot_juice` | Pressed Merlot grape juice. | Wine Press |

### Intermediates & Final Products (6)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Fermenting Wine | `fermenting_wine` | Wine actively undergoing fermentation. | Fermentation Tank |
| Young Wine | `young_wine` | Unmatured wine that requires aging. | Fermentation Tank (completed) |
| Wine Bottle 375ml | `wine_bottle_375ml` | Half-bottle of wine with NBT data. Stacks to 16. | Bottling Station |
| Wine Bottle 750ml | `wine_bottle_750ml` | Standard bottle of wine with NBT data. Stacks to 16. | Bottling Station |
| Wine Bottle 1500ml | `wine_bottle_1500ml` | Magnum bottle of wine with NBT data. Stacks to 8. | Bottling Station |
| Glass of Wine | `glass_of_wine` | Poured glass of wine for consumption. Provides buffs. Stacks to 1. | Pouring from bottle |

### Empty Bottles (3)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Empty Wine Bottle 375ml | `empty_wine_bottle_375ml` | Empty half-bottle for filling. | Crafting |
| Empty Wine Bottle 750ml | `empty_wine_bottle_750ml` | Empty standard bottle for filling. | Crafting |
| Empty Wine Bottle 1500ml | `empty_wine_bottle_1500ml` | Empty magnum bottle for filling. | Crafting |

---

## Coffee Items (26)

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

### Packages (3)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Coffee Package 250g | `coffee_package_250g` | 250g packaged coffee for sale. | Coffee Packaging Table |
| Coffee Package 500g | `coffee_package_500g` | 500g packaged coffee for sale. | Coffee Packaging Table |
| Coffee Package 1kg | `coffee_package_1kg` | 1kg packaged coffee for sale. | Coffee Packaging Table |

### Tools & Boosters (7)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Coffee Watering Can | `coffee_watering_can` | Watering can for coffee plants. 100 durability. Stacks to 1. | Crafting |
| Pulping Tool | `pulping_tool` | Tool for removing cherry pulp from beans. 200 durability. Stacks to 1. | Crafting |
| Roasting Tray | `roasting_tray` | Tray used in the roasting process. Stacks to 16. | Crafting |
| Coffee Fertilizer | `coffee_fertilizer` | Growth booster for coffee plants. Stacks to 64. | Crafting or NPC trading |
| Growth Accelerator | `growth_accelerator` | Speeds up coffee plant growth. Stacks to 64. | Crafting or NPC trading |
| Quality Enhancer | `quality_enhancer` | Improves coffee bean quality tier. Stacks to 64. | Crafting or NPC trading |
| Vacuum Seal | `vacuum_seal` | Sealing material for coffee packages. Stacks to 64. | Crafting |

### Packaging Bags (3)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Small Coffee Bag | `coffee_bag_small` | Small bag for 250g coffee packages. Stacks to 64. | Crafting |
| Medium Coffee Bag | `coffee_bag_medium` | Medium bag for 500g coffee packages. Stacks to 64. | Crafting |
| Large Coffee Bag | `coffee_bag_large` | Large bag for 1kg coffee packages. Stacks to 64. | Crafting |

---

## Cheese Items (19)

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

### Cheese Wheels (5)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Cheese Wheel | `cheese_wheel` | Standard cheese wheel. Stacks to 1. Cut into wedges for sale/consumption. | Cheese Press + Aging Cave |
| Gouda Wheel | `gouda_wheel` | Gouda cheese wheel with mild, creamy flavor. Stacks to 1. | Cheese Press + Aging Cave |
| Emmental Wheel | `emmental_wheel` | Emmental (Swiss) cheese wheel with characteristic holes. Stacks to 1. | Cheese Press + Aging Cave |
| Camembert Wheel | `camembert_wheel` | Camembert soft cheese wheel with white rind. Stacks to 1. | Cheese Press + Aging Cave |
| Parmesan Wheel | `parmesan_wheel` | Parmesan hard cheese wheel, longest aging time. Stacks to 1. | Cheese Press + Aging Cave (extended) |

### Cheese Wedges (5) -- Consumable

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Cheese Wedge | `cheese_wedge` | Standard cheese wedge. Edible (4 nutrition, 0.6 saturation). | Cut from Cheese Wheel |
| Gouda Wedge | `gouda_wedge` | Gouda cheese wedge. Edible (4 nutrition, 0.6 saturation). | Cut from Gouda Wheel |
| Emmental Wedge | `emmental_wedge` | Emmental cheese wedge. Edible (5 nutrition, 0.7 saturation). | Cut from Emmental Wheel |
| Camembert Wedge | `camembert_wedge` | Camembert cheese wedge. Edible (4 nutrition, 0.8 saturation). | Cut from Camembert Wheel |
| Parmesan Wedge | `parmesan_wedge` | Parmesan cheese wedge. Edible (6 nutrition, 0.8 saturation). Best stats. | Cut from Parmesan Wheel |

### Specialty & Packaging (5)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Smoked Cheese | `smoked_cheese` | Smoke-flavored cheese. Edible (5 nutrition, 0.7 saturation). | Smoking process |
| Herb Cheese | `herb_cheese` | Herb-infused cheese. Edible (5 nutrition, 0.8 saturation). | Herb infusion process |
| Cheese Cloth | `cheese_cloth` | Cloth wrap for aging cheese. | Crafting |
| Wax Coating | `wax_coating` | Wax coating to seal cheese wheels during aging. | Crafting |
| Cheese Paper | `cheese_paper` | Paper wrapping for finished cheese products. | Crafting |

---

## Chocolate Items (33)

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
| Chocolate Bar 100g | `chocolate_bar_100g` | 100g chocolate bar with NBT data (type, quality, ingredients). Stacks to 16. | Molding Station |
| Chocolate Bar 200g | `chocolate_bar_200g` | 200g chocolate bar with NBT data. Stacks to 16. | Molding Station |
| Chocolate Bar 500g | `chocolate_bar_500g` | 500g chocolate bar with NBT data. Stacks to 8. | Molding Station |
| Chocolate Truffle | `chocolate_truffle` | Handcrafted chocolate truffle. Edible (4 nutrition, 0.6 saturation). Stacks to 16. | Enrobing Machine |
| Chocolate Praline | `chocolate_praline` | Filled chocolate praline. Edible (3 nutrition, 0.5 saturation). Stacks to 16. | Molding Station (praline mold) |
| Hot Chocolate Mix | `hot_chocolate_mix` | Instant hot chocolate powder mix. | Grinding + mixing |

---

## Honey Items (27)

Beekeeping and honey production: raw materials, processing stages, by-products, packaging, and consumables.

### Honey Jars (3 Sizes)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Honey Jar 250g | `honey_jar_250g` | 250g jar of honey with NBT data (type, quality, origin). Stacks to 16. | Bottling Station |
| Honey Jar 500g | `honey_jar_500g` | 500g jar of honey with NBT data. Stacks to 16. | Bottling Station |
| Honey Jar 1kg | `honey_jar_1kg` | 1kg jar of honey with NBT data. Stacks to 12. | Bottling Station |

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
| Glass Jar | `glass_jar` | Standard glass jar for honey. | Crafting |
| Small Glass Jar | `glass_jar_small` | Small glass jar (250g). | Crafting |
| Large Glass Jar | `glass_jar_large` | Large glass jar (1kg). | Crafting |
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
| Beekeeper Suit | `beekeeper_suit` | Protective suit for handling beehives. Prevents bee stings. Stacks to 1. | Crafting |
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

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Normal Motor | `normal_motor` | Standard engine. 100 HP, standard fuel efficiency, 1.0x speed. | Crafting or NPC trading |
| Performance Motor | `performance_motor` | Upgraded engine. 150 HP, poor fuel efficiency, 1.5x speed. | Crafting or NPC trading |
| Performance 2 Motor | `performance_2_motor` | Top-tier engine. 200 HP, good fuel efficiency, high speed. | Crafting (expensive) |

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

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Limousine Chassis | `limousine_chassis` | Sedan body frame. 4 seats, 2 module slots, lightweight. | Crafting or NPC trading |
| Van Chassis | `van_chassis` | Van body frame. 8 seats, 4 module slots, medium weight. | Crafting or NPC trading |
| Truck Chassis | `truck_chassis` | Truck body frame. 2 seats, 6 module slots, heavy. | Crafting or NPC trading |
| Offroad Chassis | `offroad_chassis` | SUV body frame. 5 seats, 3 module slots, medium weight, off-road capable. | Crafting or NPC trading |
| Luxus Chassis | `luxus_chassis` | Sports car body frame. 2 seats, 2 module slots, lightweight, aerodynamic. | Crafting or NPC trading |

### Fenders (3)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Basic Fender | `fender_basic` | Standard bumper with basic impact protection. | Crafting |
| Chrome Fender | `fender_chrome` | Chrome-plated bumper. +10% durability, shiny appearance. | Crafting |
| Sport Fender | `fender_sport` | Aerodynamic sport bumper. +5% speed bonus. | Crafting |

### Modules (3)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Cargo Module | `cargo_module` | Adds 16 inventory slots to the vehicle for item storage. | Crafting or NPC trading |
| Fluid Module | `fluid_module` | Adds 1000L liquid tank capacity to the vehicle. | Crafting or NPC trading |
| License Plate Holder | `license_sign_mount` | Mounting bracket for displaying a license plate on the vehicle. | Crafting |

### Fuel Tanks (3)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Tank 15L | `tank_15l` | Small fuel tank, 15 liter capacity. Lightweight. | Crafting |
| Tank 30L | `tank_30l` | Standard fuel tank, 30 liter capacity. | Crafting |
| Tank 50L | `tank_50l` | Large fuel tank, 50 liter capacity. Heavy. | Crafting |

### Tools & Accessories (7)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Empty Diesel Can | `empty_diesel_can` | Empty portable fuel canister. Refillable at Fuel Stations. | Crafting |
| Full Diesel Can | `full_diesel_can` | Diesel canister filled with 5L of fuel. Single use, consumed on refueling. Also used as reagent in coca processing. | Fuel Station or crafting |
| Maintenance Kit | `maintenance_kit` | Repair tool for damaged vehicles. Multiple uses before depleted. | Crafting or NPC trading |
| Vehicle Key | `key` | Ignition key bound to a specific vehicle. Required to start and lock/unlock. | Created when vehicle is spawned |
| Starter Battery | `starter_battery` | Jump-start battery for vehicles with dead batteries. Limited uses. | Crafting or NPC trading |
| License Plate | `license_sign` | Customizable license plate with text. Attach to License Plate Holder. | Crafting |
| Vehicle Spawn Tool | `spawn_tool` | Admin tool for spawning and configuring vehicles. Unlimited uses. | Admin command |

### Block Items (2)

| Name | Registry ID | Description | How to Obtain |
|------|-------------|-------------|---------------|
| Fuel Station | `fuel_station` | Item form of the Fuel Station block. Place to create a vehicle refueling point. | Admin command or crafting |
| Garage | `werkstatt` | Item form of the Garage (Werkstatt) block. Place to create a vehicle repair station. | Admin command or crafting |

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

**Grand Total: 250+ Registered Items**

---

[Back to Wiki Home](Home.md) | [Blocks Reference](Blocks.md) | [Commands Reference](Commands.md)
