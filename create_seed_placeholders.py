#!/usr/bin/env python3
"""Generate placeholder PNG files for seed items."""

import struct
import zlib
import os

def create_16x16_png(filepath, color_rgba=(255, 0, 255, 255)):
    """
    Create a simple 16x16 PNG file with a solid color.

    Args:
        filepath: Path where the PNG should be saved
        color_rgba: Tuple of (R, G, B, A) values (0-255)
    """
    width = 16
    height = 16

    # PNG signature
    png_signature = b'\x89PNG\r\n\x1a\n'

    # IHDR chunk (image header)
    ihdr_data = struct.pack('>IIBBBBB', width, height, 8, 6, 0, 0, 0)  # 8-bit RGBA
    ihdr_chunk = b'IHDR' + ihdr_data
    ihdr_crc = struct.pack('>I', zlib.crc32(ihdr_chunk))
    ihdr = struct.pack('>I', len(ihdr_data)) + ihdr_chunk + ihdr_crc

    # IDAT chunk (image data)
    # Create raw image data: for each row, prepend filter type 0 (None)
    raw_data = b''
    r, g, b, a = color_rgba
    pixel = bytes([r, g, b, a])

    for y in range(height):
        raw_data += b'\x00'  # Filter type: None
        raw_data += pixel * width

    compressed_data = zlib.compress(raw_data, 9)
    idat_chunk = b'IDAT' + compressed_data
    idat_crc = struct.pack('>I', zlib.crc32(idat_chunk))
    idat = struct.pack('>I', len(compressed_data)) + idat_chunk + idat_crc

    # IEND chunk (image end)
    iend_chunk = b'IEND'
    iend_crc = struct.pack('>I', zlib.crc32(iend_chunk))
    iend = struct.pack('>I', 0) + iend_chunk + iend_crc

    # Write PNG file
    with open(filepath, 'wb') as f:
        f.write(png_signature + ihdr + idat + iend)

    print(f"Created: {filepath}")

def main():
    base_path = "src/main/resources/assets/schedulemc/textures/item"

    # Cannabis seeds - 4 strains with different colors
    cannabis_seeds = [
        ("cannabis_seed_indica.png", (100, 50, 150, 255)),      # Purple
        ("cannabis_seed_sativa.png", (50, 150, 50, 255)),       # Green
        ("cannabis_seed_hybrid.png", (150, 150, 50, 255)),      # Yellow
        ("cannabis_seed_autoflower.png", (150, 100, 50, 255)),  # Orange
    ]

    print("Creating Cannabis seed placeholders...")
    for filename, color in cannabis_seeds:
        filepath = os.path.join(base_path, filename)
        create_16x16_png(filepath, color)

    print(f"\nCreated {len(cannabis_seeds)} Cannabis seed placeholder textures")

if __name__ == "__main__":
    main()
