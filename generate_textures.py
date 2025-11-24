#!/usr/bin/env python3
"""
Generate placeholder textures for ScheduleMC mod
Creates 16x16 PNG files with colored backgrounds and labels
"""

from PIL import Image, ImageDraw, ImageFont
import os
import hashlib

# Base directory for textures
BASE_DIR = "src/main/resources/assets/schedulemc/textures"

def create_color_from_name(name):
    """Generate a consistent color based on the texture name"""
    # Use hash to generate consistent colors
    hash_val = int(hashlib.md5(name.encode()).hexdigest()[:6], 16)
    r = (hash_val >> 16) & 0xFF
    g = (hash_val >> 8) & 0xFF
    b = hash_val & 0xFF

    # Ensure colors are not too dark
    r = max(r, 60)
    g = max(g, 60)
    b = max(b, 60)

    return (r, g, b, 255)

def create_placeholder_texture(path, name, size=16, pattern_type="solid"):
    """Create a placeholder texture PNG file"""
    # Ensure directory exists
    os.makedirs(os.path.dirname(path), exist_ok=True)

    # Create image
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # Get color based on name
    color = create_color_from_name(name)

    if pattern_type == "cross":
        # For plant textures - create X pattern (cross model)
        # Draw background
        draw.rectangle([0, 0, size, size], fill=(0, 0, 0, 0))
        # Draw X pattern
        for i in range(size):
            draw.point((i, i), fill=color)
            draw.point((size-1-i, i), fill=color)
            # Make thicker
            if i > 0:
                draw.point((i-1, i), fill=color)
                draw.point((size-i, i), fill=color)
    elif pattern_type == "checkerboard":
        # Checkerboard pattern for blocks
        checker_size = 4
        for x in range(0, size, checker_size):
            for y in range(0, size, checker_size):
                if (x // checker_size + y // checker_size) % 2 == 0:
                    draw.rectangle([x, y, x+checker_size-1, y+checker_size-1], fill=color)
                else:
                    # Slightly darker shade
                    darker = tuple(max(0, c - 30) if i < 3 else c for i, c in enumerate(color))
                    draw.rectangle([x, y, x+checker_size-1, y+checker_size-1], fill=darker)
    else:
        # Solid color with border
        draw.rectangle([0, 0, size-1, size-1], fill=color)
        # Draw border
        border_color = tuple(max(0, c - 50) if i < 3 else c for i, c in enumerate(color))
        draw.rectangle([0, 0, size-1, size-1], outline=border_color)

        # Add small dot pattern
        for x in range(2, size-2, 4):
            for y in range(2, size-2, 4):
                draw.point((x, y), fill=border_color)

    # Save image
    img.save(path, 'PNG')
    print(f"Created: {path}")

def generate_plant_textures():
    """Generate all plant textures (64 files)"""
    print("\n=== Generating Plant Textures ===")
    plants = ["virginia", "burley", "oriental", "havana"]

    for plant in plants:
        for stage in range(8):
            # Lower texture
            name = f"{plant}_plant_stage{stage}"
            path = os.path.join(BASE_DIR, "block", f"{name}.png")
            create_placeholder_texture(path, name, size=16, pattern_type="cross")

            # Upper texture
            name_top = f"{plant}_plant_stage{stage}_top"
            path_top = os.path.join(BASE_DIR, "block", f"{name_top}.png")
            create_placeholder_texture(path_top, name_top, size=16, pattern_type="cross")

def generate_block_textures():
    """Generate all block textures (27 files)"""
    print("\n=== Generating Block Textures ===")

    # Simple blocks
    simple_blocks = [
        "cash_block",
        "drying_rack",
        "fermentation_barrel",
        "sink",
        "plot_info_block"
    ]

    for block in simple_blocks:
        path = os.path.join(BASE_DIR, "block", f"{block}.png")
        create_placeholder_texture(path, block, size=16, pattern_type="checkerboard")

    # Packaging tables (with top, bottom, side)
    packaging_tables = ["packaging_table", "small_packaging_table", "medium_packaging_table", "large_packaging_table"]

    for table in packaging_tables:
        for face in ["top", "bottom", "side"]:
            name = f"{table}_{face}"
            path = os.path.join(BASE_DIR, "block", f"{name}.png")
            create_placeholder_texture(path, name, size=16, pattern_type="checkerboard")

    # Grow light slabs
    slabs = ["basic_grow_light_slab", "advanced_grow_light_slab", "premium_grow_light_slab"]

    for slab in slabs:
        path = os.path.join(BASE_DIR, "block", f"{slab}.png")
        create_placeholder_texture(path, slab, size=16, pattern_type="checkerboard")

def generate_item_textures():
    """Generate all item textures (42 files)"""
    print("\n=== Generating Item Textures ===")

    # Block items
    block_items = [
        "cash_block",
        "drying_rack",
        "fermentation_barrel",
        "sink",
        "packaging_table",
        "small_packaging_table",
        "medium_packaging_table",
        "large_packaging_table",
        "basic_grow_light_slab",
        "advanced_grow_light_slab",
        "premium_grow_light_slab",
        "plot_info_block"
    ]

    for item in block_items:
        path = os.path.join(BASE_DIR, "item", f"{item}.png")
        create_placeholder_texture(path, item, size=16, pattern_type="solid")

    # Seeds
    plants = ["virginia", "burley", "oriental", "havana"]
    for plant in plants:
        name = f"{plant}_seeds"
        path = os.path.join(BASE_DIR, "item", f"{name}.png")
        create_placeholder_texture(path, name, size=16, pattern_type="solid")

    # Leaves (fresh, dried, fermented)
    for state in ["fresh", "dried", "fermented"]:
        for plant in plants:
            name = f"{state}_{plant}_leaf"
            path = os.path.join(BASE_DIR, "item", f"{name}.png")
            create_placeholder_texture(path, name, size=16, pattern_type="solid")

    # Bottles
    bottles = ["fertilizer_bottle", "growth_booster_bottle", "quality_booster_bottle"]
    for bottle in bottles:
        path = os.path.join(BASE_DIR, "item", f"{bottle}.png")
        create_placeholder_texture(path, bottle, size=16, pattern_type="solid")

    # Packaging
    packaging = ["packaging_bag", "packaging_jar", "packaging_box"]
    for pack in packaging:
        path = os.path.join(BASE_DIR, "item", f"{pack}.png")
        create_placeholder_texture(path, pack, size=16, pattern_type="solid")

    # Soil bags
    soil_bags = ["soil_bag_small", "soil_bag_medium", "soil_bag_large"]
    for bag in soil_bags:
        path = os.path.join(BASE_DIR, "item", f"{bag}.png")
        create_placeholder_texture(path, bag, size=16, pattern_type="solid")

    # Misc items
    misc_items = ["watering_can", "packaged_tobacco", "cash", "path_staff", "npc_location_tool"]
    for item in misc_items:
        path = os.path.join(BASE_DIR, "item", f"{item}.png")
        create_placeholder_texture(path, item, size=16, pattern_type="solid")

def main():
    """Main function to generate all textures"""
    print("=" * 60)
    print("Generating Placeholder Textures for ScheduleMC")
    print("=" * 60)

    generate_plant_textures()
    generate_block_textures()
    generate_item_textures()

    print("\n" + "=" * 60)
    print("✓ All placeholder textures generated successfully!")
    print("=" * 60)

if __name__ == "__main__":
    main()
