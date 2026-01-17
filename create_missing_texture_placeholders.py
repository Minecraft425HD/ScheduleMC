#!/usr/bin/env python3
"""
Erstellt richtige "Missing Texture" Platzhalter für alle 82-Byte-Platzhalter.
Das klassische Minecraft Missing-Texture-Muster: Rosa-schwarzes Schachbrettmuster.
"""

from PIL import Image
import os
from pathlib import Path

# Minecraft Missing Texture Farben
BLACK = (0, 0, 0, 255)
MAGENTA = (255, 0, 255, 255)

def create_missing_texture(size=16):
    """Erstellt ein rosa-schwarzes Schachbrettmuster (Minecraft Missing Texture)"""
    img = Image.new('RGBA', (size, size))
    pixels = img.load()

    # Erstelle Schachbrettmuster
    square_size = size // 2
    for y in range(size):
        for x in range(size):
            # Bestimme, in welchem Quadranten wir sind
            is_top = y < square_size
            is_left = x < square_size

            # Schachbrettmuster: oben-links und unten-rechts = schwarz, rest = magenta
            if (is_top and is_left) or (not is_top and not is_left):
                pixels[x, y] = BLACK
            else:
                pixels[x, y] = MAGENTA

    return img

def find_placeholder_textures(textures_dir):
    """Findet alle 82-Byte-Platzhalter-Texturen"""
    placeholders = []
    textures_path = Path(textures_dir)

    for png_file in textures_path.rglob('*.png'):
        if png_file.stat().st_size == 82:
            placeholders.append(png_file)

    return placeholders

def main():
    script_dir = Path(__file__).parent
    textures_dir = script_dir / 'src/main/resources/assets/schedulemc/textures'

    if not textures_dir.exists():
        print(f"❌ Textures-Verzeichnis nicht gefunden: {textures_dir}")
        return

    print("🔍 Suche nach Platzhalter-Texturen (82 Bytes)...")
    placeholders = find_placeholder_textures(textures_dir)

    if not placeholders:
        print("✅ Keine Platzhalter gefunden!")
        return

    print(f"\n📋 Gefunden: {len(placeholders)} Platzhalter-Texturen\n")

    # Erstelle Missing-Texture-Bild
    missing_texture = create_missing_texture(16)

    replaced = 0
    for placeholder in sorted(placeholders):
        relative_path = placeholder.relative_to(textures_dir)
        try:
            # Backup der Original-Datei (falls gewünscht)
            # backup_path = placeholder.with_suffix('.png.backup')
            # if not backup_path.exists():
            #     placeholder.replace(backup_path)

            # Ersetze mit Missing-Texture
            missing_texture.save(placeholder)
            print(f"✅ {relative_path}")
            replaced += 1
        except Exception as e:
            print(f"❌ {relative_path}: {e}")

    print(f"\n🎉 Fertig! {replaced}/{len(placeholders)} Texturen ersetzt")
    print(f"📁 Alle Texturen sind jetzt 16x16 Pixel rosa-schwarze Schachbrettmuster")

if __name__ == '__main__':
    main()
