#!/usr/bin/env python3
"""
Erstellt Platzhalter-PNG-Bilder für die Smartphone-Apps
Diese können später durch eigene Bilder ersetzt werden
"""

from PIL import Image, ImageDraw, ImageFont
import os

# Ausgabeverzeichnis
output_dir = "src/main/resources/assets/schedulemc/textures/gui/apps"
os.makedirs(output_dir, exist_ok=True)

# Icon-Größe
icon_size = 48

# App-Definitionen: (Dateiname, Farbe, Text, Textfarbe)
apps = [
    ("app_map.png", "#4A90E2", "MAP", "#FFFFFF"),           # Blau
    ("app_dealer.png", "#E24A4A", "DEL", "#FFFFFF"),        # Rot
    ("app_products.png", "#4AE290", "PRO", "#FFFFFF"),      # Grün
    ("app_order.png", "#E2D44A", "ORD", "#000000"),         # Gelb
    ("app_contacts.png", "#9B4AE2", "CON", "#FFFFFF"),      # Lila
    ("app_messages.png", "#4AE2D4", "MSG", "#000000"),      # Türkis
    ("close.png", "#FF0000", "X", "#FFFFFF"),               # Rot für Schließen-Button
]

def create_app_icon(filename, bg_color, text, text_color):
    """Erstellt ein einfaches App-Icon mit Hintergrundfarbe und Text"""

    # Erstelle Image
    img = Image.new('RGBA', (icon_size, icon_size), bg_color)
    draw = ImageDraw.Draw(img)

    # Zeichne Rahmen
    border_color = "#000000"
    border_width = 2
    draw.rectangle(
        [(0, 0), (icon_size-1, icon_size-1)],
        outline=border_color,
        width=border_width
    )

    # Versuche eine Font zu laden, sonst verwende Standard
    try:
        # Verwende eine größere Schriftgröße
        font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", 16)
    except:
        font = ImageFont.load_default()

    # Zentriere Text
    bbox = draw.textbbox((0, 0), text, font=font)
    text_width = bbox[2] - bbox[0]
    text_height = bbox[3] - bbox[1]

    position = (
        (icon_size - text_width) // 2,
        (icon_size - text_height) // 2 - 2  # Leichte Anpassung nach oben
    )

    # Zeichne Text
    draw.text(position, text, fill=text_color, font=font)

    # Speichere
    output_path = os.path.join(output_dir, filename)
    img.save(output_path)
    print(f"✓ Erstellt: {output_path}")

# Erstelle alle Icons
print("Erstelle Platzhalter-Icons für Smartphone-Apps...")
print("-" * 50)

for app_data in apps:
    create_app_icon(*app_data)

print("-" * 50)
print(f"✓ {len(apps)} Icons erfolgreich erstellt!")
print(f"\nDie Icons befinden sich in: {output_dir}")
print("\nSie können jetzt durch eigene 48x48 PNG-Bilder ersetzt werden.")
