#!/usr/bin/env python3
"""
Create placeholder PNG files for missing textures in ScheduleMC.
These placeholders help identify which textures are missing, their names, and where they should be placed.
"""

from PIL import Image, ImageDraw, ImageFont
import os

# Define placeholder textures to create
placeholders = {
    # Wheel textures (16x16 for entity textures)
    'src/main/resources/assets/schedulemc/textures/entity/sport_wheel.png': {
        'size': (16, 16),
        'color': (255, 0, 255),  # Magenta
        'label': 'SPORT'
    },
    'src/main/resources/assets/schedulemc/textures/entity/premium_wheel.png': {
        'size': (16, 16),
        'color': (0, 255, 255),  # Cyan
        'label': 'PREM'
    },
    'src/main/resources/assets/schedulemc/textures/entity/allterrain_wheel.png': {
        'size': (16, 16),
        'color': (255, 255, 0),  # Yellow
        'label': 'ATER'
    },
    'src/main/resources/assets/schedulemc/textures/entity/heavyduty_wheel.png': {
        'size': (16, 16),
        'color': (255, 128, 0),  # Orange
        'label': 'HVDY'
    },
    # GUI texture (256x256 is common for GUI textures)
    'src/main/resources/assets/schedulemc/textures/gui/gui_garage.png': {
        'size': (256, 256),
        'color': (128, 128, 255),  # Light blue
        'label': 'GUI_GARAGE'
    },
    # Parts textures (16x16 for parts)
    'src/main/resources/assets/schedulemc/textures/parts/fender_chrome.png': {
        'size': (16, 16),
        'color': (192, 192, 192),  # Silver/Chrome
        'label': 'CHRM'
    },
    'src/main/resources/assets/schedulemc/textures/parts/fender_sport.png': {
        'size': (16, 16),
        'color': (255, 0, 0),  # Red
        'label': 'SPRT'
    }
}

def create_placeholder(filepath, size, color, label):
    """Create a placeholder PNG with distinct color and label."""
    # Create image with solid color
    img = Image.new('RGBA', size, color + (255,))

    # Add a border to make it more visible
    draw = ImageDraw.Draw(img)
    draw.rectangle([(0, 0), (size[0]-1, size[1]-1)], outline=(0, 0, 0, 255), width=1)

    # For larger images, add text label
    if size[0] >= 64:
        try:
            # Try to use a default font
            font = ImageFont.load_default()
            # Get text bounding box
            bbox = draw.textbbox((0, 0), label, font=font)
            text_width = bbox[2] - bbox[0]
            text_height = bbox[3] - bbox[1]

            # Center the text
            x = (size[0] - text_width) // 2
            y = (size[1] - text_height) // 2

            # Draw text with outline for visibility
            draw.text((x, y), label, fill=(0, 0, 0, 255), font=font)
        except:
            pass  # Skip text if font loading fails

    # Create directory if it doesn't exist
    os.makedirs(os.path.dirname(filepath), exist_ok=True)

    # Save the image
    img.save(filepath, 'PNG')
    print(f"Created: {filepath} ({size[0]}x{size[1]}, {color})")

def main():
    print("Creating placeholder PNG files for missing textures...\n")

    for filepath, config in placeholders.items():
        create_placeholder(filepath, config['size'], config['color'], config['label'])

    print(f"\nCreated {len(placeholders)} placeholder PNG files successfully!")
    print("\nThese placeholders show:")
    print("  - Which textures are missing (by filename)")
    print("  - Where they should be placed (by directory)")
    print("  - Distinct colors to easily identify them in-game")

if __name__ == '__main__':
    main()
