#!/usr/bin/env python3
"""
Create the final 26 missing placeholder PNG files for ScheduleMC.
"""

from PIL import Image, ImageDraw
import os

# Minecraft color code to RGB mapping
MC_COLORS = {
    '§4': (170, 0, 0),      # Dark Red (Afghanisch)
    '§5': (170, 0, 170),    # Dark Purple (Indisch)
    '§6': (255, 170, 0),    # Gold (Tuerkisch)
    '§f': (255, 255, 255),  # White (Standard)
}

def get_rgb_from_mc_color(mc_color_code):
    """Convert Minecraft color code to RGB."""
    mc_color_code = mc_color_code.replace('§l', '')
    return MC_COLORS.get(mc_color_code, (255, 0, 255))


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


# Base path for all item textures
BASE_PATH = 'src/main/resources/assets/schedulemc/textures/item/'
SIZE = (16, 16)

# Define all missing placeholders
missing_textures = []

# 1. MDMA standard variants (2 missing)
missing_textures.extend([
    ('mdma_base_standard.png', '§f', 'STND'),
    ('mdma_kristall_standard.png', '§f', 'STND'),
])

# 2. Morphine variants (12 missing) - 3 poppy types × 4 quality levels
poppy_types = [
    ('afghanisch', '§4', 'A'),
    ('tuerkisch', '§6', 'T'),
    ('indisch', '§5', 'I'),
]

quality_levels = ['schlecht', 'gut', 'sehr_gut', 'legendaer']

for poppy_name, poppy_color, poppy_label in poppy_types:
    for quality in quality_levels:
        missing_textures.append((
            f'morphine_{poppy_name}_{quality}.png',
            poppy_color,
            f'{poppy_label}-{quality[:2].upper()}'
        ))

# 3. Raw Opium variants (12 missing) - 3 poppy types × 4 quality levels
for poppy_name, poppy_color, poppy_label in poppy_types:
    for quality in quality_levels:
        missing_textures.append((
            f'raw_opium_{poppy_name}_{quality}.png',
            poppy_color,
            f'{poppy_label}-{quality[:2].upper()}'
        ))


def main():
    print("Creating final 26 missing placeholder PNG files...\n")
    print("=" * 80)

    created_count = 0
    categories = {
        'MDMA': [],
        'Morphine': [],
        'Raw Opium': []
    }

    for filename, mc_color_code, label in missing_textures:
        filepath = os.path.join(BASE_PATH, filename)
        rgb = create_placeholder(filepath, SIZE, mc_color_code, label)
        created_count += 1

        # Categorize
        if 'mdma' in filename:
            category = 'MDMA'
        elif 'morphine' in filename:
            category = 'Morphine'
        else:
            category = 'Raw Opium'

        categories[category].append({
            'filename': filename,
            'rgb': rgb,
            'label': label
        })

        print(f"  [{created_count:2d}] {filename:45s} RGB{rgb}")

    print("=" * 80)
    print(f"\n✓ Created {created_count} placeholder PNG files successfully!\n")

    print("Summary by category:")
    for category, items in categories.items():
        print(f"  {category:15s} → {len(items):2d} placeholders")

    print("\nAll textures are now complete!")
    print("Total texture placeholders in project: 188 + 26 = 214")


if __name__ == '__main__':
    main()
