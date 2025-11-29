#!/usr/bin/env python3
"""
Erstellt sehr einfache einfarbige Platzhalter-PNG-Bilder
Diese sollten durch richtige Icons ersetzt werden
"""

import struct
import zlib
import os

def create_simple_png(filename, width, height, r, g, b):
    """Erstellt eine einfarbige PNG-Datei ohne externe Libraries"""

    def png_pack(tag, data):
        chunk_head = tag + data
        return struct.pack("!I", len(data)) + chunk_head + struct.pack("!I", 0xFFFFFFFF & zlib.crc32(chunk_head))

    # PNG Header
    png_header = b'\x89PNG\r\n\x1a\n'

    # IHDR Chunk
    ihdr = struct.pack("!2I5B", width, height, 8, 2, 0, 0, 0)

    # Erstelle Pixel-Daten (RGB)
    raw_data = b''
    for y in range(height):
        raw_data += b'\x00'  # Filter type
        for x in range(width):
            raw_data += struct.pack('3B', r, g, b)

    # IDAT Chunk (komprimierte Bilddaten)
    compressed_data = zlib.compress(raw_data, 9)

    # Baue PNG zusammen
    png_data = png_header
    png_data += png_pack(b'IHDR', ihdr)
    png_data += png_pack(b'IDAT', compressed_data)
    png_data += png_pack(b'IEND', b'')

    # Schreibe Datei
    with open(filename, 'wb') as f:
        f.write(png_data)

# Ausgabeverzeichnis
output_dir = "src/main/resources/assets/schedulemc/textures/gui/apps"
os.makedirs(output_dir, exist_ok=True)

# Icon-Größe
size = 48

# App-Definitionen: (Dateiname, R, G, B)
apps = [
    ("app_map.png", 74, 144, 226),        # Blau
    ("app_dealer.png", 226, 74, 74),      # Rot
    ("app_products.png", 74, 226, 144),   # Grün
    ("app_order.png", 226, 212, 74),      # Gelb
    ("app_contacts.png", 155, 74, 226),   # Lila
    ("app_messages.png", 74, 226, 212),   # Türkis
    ("close.png", 255, 0, 0),             # Rot für Schließen-Button
]

print("Erstelle einfarbige Platzhalter-Icons...")
print("-" * 50)

for filename, r, g, b in apps:
    output_path = os.path.join(output_dir, filename)
    create_simple_png(output_path, size, size, r, g, b)
    print(f"✓ Erstellt: {output_path}")

print("-" * 50)
print(f"✓ {len(apps)} Icons erfolgreich erstellt!")
print(f"\nDie Icons befinden sich in: {output_dir}")
print("\n⚠ WICHTIG: Dies sind einfache einfarbige Platzhalter!")
print("Ersetzen Sie diese durch richtige 48x48 PNG-Icons für ein besseres Aussehen.")
