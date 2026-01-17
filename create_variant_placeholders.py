#!/usr/bin/env python3
"""
Create placeholder PNG files for all missing texture VARIANTS in ScheduleMC.
Items have quality levels, types, and strains defined in code but only 1 base texture exists.
"""

from PIL import Image, ImageDraw, ImageFont
import os

# Minecraft color code to RGB mapping
MC_COLORS = {
    '§0': (0, 0, 0),        # Black
    '§1': (0, 0, 170),      # Dark Blue
    '§2': (0, 170, 0),      # Dark Green
    '§3': (0, 170, 170),    # Dark Aqua
    '§4': (170, 0, 0),      # Dark Red
    '§5': (170, 0, 170),    # Dark Purple
    '§6': (255, 170, 0),    # Gold
    '§7': (170, 170, 170),  # Gray
    '§8': (85, 85, 85),     # Dark Gray
    '§9': (85, 85, 255),    # Blue
    '§a': (85, 255, 85),    # Green
    '§b': (85, 255, 255),   # Aqua
    '§c': (255, 85, 85),    # Red
    '§d': (255, 85, 255),   # Light Purple
    '§e': (255, 255, 85),   # Yellow
    '§f': (255, 255, 255),  # White
}

# All variant definitions based on code analysis
variants = {
    # METH QUALITY SYSTEM (3 quality levels)
    'meth': {
        'base_path': 'src/main/resources/assets/schedulemc/textures/item/',
        'size': (16, 16),
        'variants': [
            ('standard', '§f', 'STND'),
            ('gut', '§e', 'GUT'),
            ('blue_sky', '§b§l', 'BLUE'),
        ]
    },
    'kristall_meth': {
        'base_path': 'src/main/resources/assets/schedulemc/textures/item/',
        'size': (16, 16),
        'variants': [
            ('standard', '§f', 'STND'),
            ('gut', '§e', 'GUT'),
            ('blue_sky', '§b§l', 'BLUE'),
        ]
    },
    'roh_meth': {
        'base_path': 'src/main/resources/assets/schedulemc/textures/item/',
        'size': (16, 16),
        'variants': [
            ('standard', '§f', 'STND'),
            ('gut', '§e', 'GUT'),
            ('blue_sky', '§b§l', 'BLUE'),
        ]
    },
    'meth_paste': {
        'base_path': 'src/main/resources/assets/schedulemc/textures/item/',
        'size': (16, 16),
        'variants': [
            ('standard', '§f', 'STND'),
            ('gut', '§e', 'GUT'),
            ('blue_sky', '§b§l', 'BLUE'),
        ]
    },

    # MDMA QUALITY SYSTEM (4 quality levels)
    'mdma_base': {
        'base_path': 'src/main/resources/assets/schedulemc/textures/item/',
        'size': (16, 16),
        'variants': [
            ('schlecht', '§7', 'SCHL'),
            ('standard', '§f', 'STND'),
            ('gut', '§e', 'GUT'),
            ('premium', '§d§l', 'PREM'),
        ]
    },
    'mdma_kristall': {
        'base_path': 'src/main/resources/assets/schedulemc/textures/item/',
        'size': (16, 16),
        'variants': [
            ('schlecht', '§7', 'SCHL'),
            ('standard', '§f', 'STND'),
            ('gut', '§e', 'GUT'),
            ('premium', '§d§l', 'PREM'),
        ]
    },

    # COCAINE (2 coca types × 4 quality levels = 8 variants)
    'cocaine': {
        'base_path': 'src/main/resources/assets/schedulemc/textures/item/',
        'size': (16, 16),
        'variants': [
            ('bolivianisch_schlecht', '§a', 'B-SC'),
            ('bolivianisch_gut', '§a', 'B-GT'),
            ('bolivianisch_sehr_gut', '§a', 'B-SG'),
            ('bolivianisch_legendaer', '§a', 'B-LG'),
            ('kolumbianisch_schlecht', '§2', 'K-SC'),
            ('kolumbianisch_gut', '§2', 'K-GT'),
            ('kolumbianisch_sehr_gut', '§2', 'K-SG'),
            ('kolumbianisch_legendaer', '§2', 'K-LG'),
        ]
    },

    # CRACK ROCK (2 coca types × 4 quality levels = 8 variants)
    'crack_rock': {
        'base_path': 'src/main/resources/assets/schedulemc/textures/item/',
        'size': (16, 16),
        'variants': [
            ('bolivianisch_schlecht', '§a', 'B-SC'),
            ('bolivianisch_standard', '§a', 'B-ST'),
            ('bolivianisch_gut', '§a', 'B-GT'),
            ('bolivianisch_fishscale', '§a', 'B-FS'),
            ('kolumbianisch_schlecht', '§2', 'K-SC'),
            ('kolumbianisch_standard', '§2', 'K-ST'),
            ('kolumbianisch_gut', '§2', 'K-GT'),
            ('kolumbianisch_fishscale', '§2', 'K-FS'),
        ]
    },

    # HEROIN (3 poppy types × 4 quality levels = 12 variants)
    'heroin': {
        'base_path': 'src/main/resources/assets/schedulemc/textures/item/',
        'size': (16, 16),
        'variants': [
            ('afghanisch_schlecht', '§4', 'A-SC'),
            ('afghanisch_gut', '§4', 'A-GT'),
            ('afghanisch_sehr_gut', '§4', 'A-SG'),
            ('afghanisch_legendaer', '§4', 'A-LG'),
            ('tuerkisch_schlecht', '§6', 'T-SC'),
            ('tuerkisch_gut', '§6', 'T-GT'),
            ('tuerkisch_sehr_gut', '§6', 'T-SG'),
            ('tuerkisch_legendaer', '§6', 'T-LG'),
            ('indisch_schlecht', '§5', 'I-SC'),
            ('indisch_gut', '§5', 'I-GT'),
            ('indisch_sehr_gut', '§5', 'I-SG'),
            ('indisch_legendaer', '§5', 'I-LG'),
        ]
    },

    # CANNABIS BUDS (4 strains × 5 quality levels = 20 variants each)
    'cured_cannabis_bud': {
        'base_path': 'src/main/resources/assets/schedulemc/textures/item/',
        'size': (16, 16),
        'variants': []
    },
    'trimmed_cannabis_bud': {
        'base_path': 'src/main/resources/assets/schedulemc/textures/item/',
        'size': (16, 16),
        'variants': []
    },
    'dried_cannabis_bud': {
        'base_path': 'src/main/resources/assets/schedulemc/textures/item/',
        'size': (16, 16),
        'variants': []
    },
    'fresh_cannabis_bud': {
        'base_path': 'src/main/resources/assets/schedulemc/textures/item/',
        'size': (16, 16),
        'variants': []
    },
    'cannabis_hash': {
        'base_path': 'src/main/resources/assets/schedulemc/textures/item/',
        'size': (16, 16),
        'variants': []
    },
    'cannabis_oil': {
        'base_path': 'src/main/resources/assets/schedulemc/textures/item/',
        'size': (16, 16),
        'variants': []
    },

    # FERMENTED TOBACCO (4 types × 4 quality levels = 16 variants)
    'packaged_tobacco': {
        'base_path': 'src/main/resources/assets/schedulemc/textures/item/',
        'size': (16, 16),
        'variants': [
            ('virginia_schlecht', '§e', 'V-SC'),
            ('virginia_gut', '§e', 'V-GT'),
            ('virginia_sehr_gut', '§e', 'V-SG'),
            ('virginia_legendaer', '§e', 'V-LG'),
            ('burley_schlecht', '§6', 'B-SC'),
            ('burley_gut', '§6', 'B-GT'),
            ('burley_sehr_gut', '§6', 'B-SG'),
            ('burley_legendaer', '§6', 'B-LG'),
            ('oriental_schlecht', '§d', 'O-SC'),
            ('oriental_gut', '§d', 'O-GT'),
            ('oriental_sehr_gut', '§d', 'O-SG'),
            ('oriental_legendaer', '§d', 'O-LG'),
            ('havana_schlecht', '§c§l', 'H-SC'),
            ('havana_gut', '§c§l', 'H-GT'),
            ('havana_sehr_gut', '§c§l', 'H-SG'),
            ('havana_legendaer', '§c§l', 'H-LG'),
        ]
    },
}

# Generate cannabis variants (4 strains × 5 qualities = 20)
cannabis_strains = [
    ('indica', '§5', 'IND'),
    ('sativa', '§a', 'SAT'),
    ('hybrid', '§e', 'HYB'),
    ('autoflower', '§b', 'AUT'),
]
cannabis_qualities = [
    ('schwag', '§8', 'SWG'),
    ('mids', '§7', 'MID'),
    ('dank', '§a', 'DNK'),
    ('top_shelf', '§6', 'TOP'),
    ('exotic', '§d§l', 'EXO'),
]

for strain_name, strain_color, strain_label in cannabis_strains:
    for quality_name, quality_color, quality_label in cannabis_qualities:
        # Combine colors - use quality color as it's more distinctive
        variant_name = f'{strain_name}_{quality_name}'
        variant_color = quality_color
        variant_label = f'{strain_label[0]}-{quality_label}'

        # Add to all cannabis items
        for item in ['cured_cannabis_bud', 'trimmed_cannabis_bud', 'dried_cannabis_bud',
                     'fresh_cannabis_bud', 'cannabis_hash', 'cannabis_oil']:
            variants[item]['variants'].append((variant_name, variant_color, variant_label))


def get_rgb_from_mc_color(mc_color_code):
    """Convert Minecraft color code to RGB."""
    # Remove §l (bold) if present
    mc_color_code = mc_color_code.replace('§l', '')
    return MC_COLORS.get(mc_color_code, (255, 0, 255))  # Default to magenta


def create_placeholder(filepath, size, mc_color_code, label):
    """Create a placeholder PNG with color based on Minecraft color code."""
    rgb = get_rgb_from_mc_color(mc_color_code)

    # Create image with solid color
    img = Image.new('RGBA', size, rgb + (255,))

    # Add a border
    draw = ImageDraw.Draw(img)
    draw.rectangle([(0, 0), (size[0]-1, size[1]-1)], outline=(0, 0, 0, 255), width=1)

    # Create directory if it doesn't exist
    os.makedirs(os.path.dirname(filepath), exist_ok=True)

    # Save the image
    img.save(filepath, 'PNG')
    return rgb


def main():
    print("Creating placeholder PNG files for all missing texture VARIANTS...\n")

    created_count = 0
    categories = {}

    for item_name, config in variants.items():
        base_path = config['base_path']
        size = config['size']
        item_variants = config['variants']

        category_key = item_name.split('_')[0]  # meth, cannabis, etc.
        if category_key not in categories:
            categories[category_key] = []

        for variant_name, mc_color_code, label in item_variants:
            # Skip if it's the base texture (already exists)
            if variant_name in ['standard'] and item_name in ['meth', 'mdma_base', 'mdma_kristall']:
                continue

            filename = f'{item_name}_{variant_name}.png'
            filepath = os.path.join(base_path, filename)

            rgb = create_placeholder(filepath, size, mc_color_code, label)
            created_count += 1

            categories[category_key].append({
                'filename': filename,
                'rgb': rgb,
                'label': label,
                'mc_color': mc_color_code
            })

            print(f"  [{created_count:3d}] {filename:50s} RGB{rgb}")

    print(f"\n{'='*80}")
    print(f"✓ Created {created_count} placeholder PNG files successfully!")
    print(f"{'='*80}\n")

    print("Summary by category:")
    for category, items in sorted(categories.items()):
        print(f"  {category.upper():15s} → {len(items):3d} variants")

    print("\nThese placeholders show:")
    print("  - Which texture VARIANTS are missing (by filename)")
    print("  - What quality/type they represent (by color from code)")
    print("  - Distinct colors matching Minecraft color codes")


if __name__ == '__main__':
    main()
