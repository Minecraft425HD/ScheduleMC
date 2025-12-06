═══════════════════════════════════════════════════════════════════════════════
TABAK-PFLANZEN TEXTUREN - Anleitung
═══════════════════════════════════════════════════════════════════════════════

Die folgenden 64 Texturen werden für die Pflanzen benötigt:

VIRGINIA (4 Pflanzen × 8 Stufen × 2 Höhen = 64 Texturen):
----------------------------------------
Untere Hälfte (Stufe 0-7):
  - virginia_plant_stage0.png
  - virginia_plant_stage1.png
  - virginia_plant_stage2.png
  - virginia_plant_stage3.png
  - virginia_plant_stage4.png (ab hier 2 Blöcke hoch)
  - virginia_plant_stage5.png
  - virginia_plant_stage6.png
  - virginia_plant_stage7.png (voll ausgewachsen)

Obere Hälfte (Stufe 0-7, wird ab Stufe 4 verwendet):
  - virginia_plant_stage0_top.png
  - virginia_plant_stage1_top.png
  - virginia_plant_stage2_top.png
  - virginia_plant_stage3_top.png
  - virginia_plant_stage4_top.png
  - virginia_plant_stage5_top.png
  - virginia_plant_stage6_top.png
  - virginia_plant_stage7_top.png

BURLEY (gleiche Struktur):
----------------------------------------
  - burley_plant_stage0.png bis burley_plant_stage7.png
  - burley_plant_stage0_top.png bis burley_plant_stage7_top.png

ORIENTAL (gleiche Struktur):
----------------------------------------
  - oriental_plant_stage0.png bis oriental_plant_stage7.png
  - oriental_plant_stage0_top.png bis oriental_plant_stage7_top.png

HAVANA (gleiche Struktur):
----------------------------------------
  - havana_plant_stage0.png bis havana_plant_stage7.png
  - havana_plant_stage0_top.png bis havana_plant_stage7_top.png

═══════════════════════════════════════════════════════════════════════════════
TECHNISCHE DETAILS:
═══════════════════════════════════════════════════════════════════════════════

Format:         PNG (16x16 oder 32x32 empfohlen)
Modell-Typ:     Cross (X-förmig, Draufsicht)
Transparenz:    Alpha-Kanal für Hintergrund
Höhe:
  - Stufe 0-3:  1 Block hoch (nur untere Textur sichtbar)
  - Stufe 4-7:  2 Blöcke hoch (untere + obere Textur)

Kollision:      KEINE - Pflanzen haben keine Kollisions-Box!

Wachstum:
  - Stufe 0:    Keimling (klein)
  - Stufe 1-3:  Wachsend (1 Block hoch)
  - Stufe 4-6:  Wachsend (2 Blöcke hoch)
  - Stufe 7:    Ausgewachsen, erntebereit (2 Blöcke hoch)

═══════════════════════════════════════════════════════════════════════════════
HINWEIS:
═══════════════════════════════════════════════════════════════════════════════

Falls Texturen fehlen, wird Minecraft sie als schwarz-pinkes Schachbrettmuster
(Missing Texture) anzeigen. Platziere alle 64 Texturen in diesem Ordner:

  /src/main/resources/assets/schedulemc/textures/block/

Beispiel-Pfad für Virginia Stufe 0:
  /src/main/resources/assets/schedulemc/textures/block/virginia_plant_stage0.png
