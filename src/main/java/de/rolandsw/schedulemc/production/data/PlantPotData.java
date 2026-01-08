package de.rolandsw.schedulemc.production.data;

import de.rolandsw.schedulemc.cannabis.CannabisStrain;
import de.rolandsw.schedulemc.cannabis.data.CannabisPlantData;
import de.rolandsw.schedulemc.coca.CocaType;
import de.rolandsw.schedulemc.coca.data.CocaPlantData;
import de.rolandsw.schedulemc.mushroom.MushroomType;
import de.rolandsw.schedulemc.mushroom.data.MushroomPlantData;
import de.rolandsw.schedulemc.poppy.PoppyType;
import de.rolandsw.schedulemc.poppy.data.PoppyPlantData;
import de.rolandsw.schedulemc.production.core.PotType;
import de.rolandsw.schedulemc.tobacco.TobaccoType;
import de.rolandsw.schedulemc.tobacco.data.TobaccoPlantData;

import javax.annotation.Nullable;

/**
 * Speichert Daten eines universellen Pflanzen-Topfes
 * Unterstützt: Tabak, Cannabis, Koka, Mohn und Pilze
 */
public class PlantPotData {

    private PotType potType;
    private double waterLevel; // Aktuelles Wasser (als double für präzisen Verbrauch)
    private double soilLevel; // Aktuelle Erde (als double für präzisen Verbrauch)
    private TobaccoPlantData plant; // Gepflanzte Tabakpflanze (null wenn leer)
    private CannabisPlantData cannabisPlant; // Gepflanzte Cannabis-Pflanze (null wenn leer)
    private CocaPlantData cocaPlant; // Gepflanzte Koka-Pflanze (null wenn leer)
    private PoppyPlantData poppyPlant; // Gepflanzte Mohn-Pflanze (null wenn leer)
    private MushroomPlantData mushroomPlant; // Gepflanzte Pilzkultur (null wenn leer)
    private boolean hasSoil; // Wurde Erde hinzugefügt?
    private boolean hasMist; // Wurde Mist hinzugefügt? (für Pilze)

    public PlantPotData(PotType potType) {
        this.potType = potType;
        this.waterLevel = 0;
        this.soilLevel = 0;
        this.plant = null;
        this.cannabisPlant = null;
        this.cocaPlant = null;
        this.poppyPlant = null;
        this.mushroomPlant = null;
        this.hasSoil = false;
        this.hasMist = false;
    }

    public PotType getPotType() {
        return potType;
    }

    public int getWaterLevel() {
        return (int) Math.ceil(waterLevel); // Runde auf für Anzeige
    }

    public int getSoilLevel() {
        return (int) Math.ceil(soilLevel); // Runde auf für Anzeige
    }

    /**
     * Gibt exakte Wasser-Level als double zurück
     */
    public double getWaterLevelExact() {
        return waterLevel;
    }

    /**
     * Gibt exakte Erde-Level als double zurück
     */
    public double getSoilLevelExact() {
        return soilLevel;
    }

    public int getMaxWater() {
        return potType.getWaterCapacity();
    }

    public int getMaxSoil() {
        return potType.getSoilCapacity();
    }

    public TobaccoPlantData getPlant() {
        return plant;
    }

    public CannabisPlantData getCannabisPlant() {
        return cannabisPlant;
    }

    public CocaPlantData getCocaPlant() {
        return cocaPlant;
    }

    public PoppyPlantData getPoppyPlant() {
        return poppyPlant;
    }

    public MushroomPlantData getMushroomPlant() {
        return mushroomPlant;
    }

    public boolean hasPlant() {
        return plant != null || cannabisPlant != null || cocaPlant != null || poppyPlant != null || mushroomPlant != null;
    }

    public boolean hasTobaccoPlant() {
        return plant != null;
    }

    public boolean hasCannabisPlant() {
        return cannabisPlant != null;
    }

    public boolean hasCocaPlant() {
        return cocaPlant != null;
    }

    public boolean hasPoppyPlant() {
        return poppyPlant != null;
    }

    public boolean hasMushroomPlant() {
        return mushroomPlant != null;
    }

    public boolean hasSoil() {
        return hasSoil;
    }

    public boolean hasMist() {
        return hasMist;
    }

    public void setMist(boolean hasMist) {
        this.hasMist = hasMist;
    }

    /**
     * Fügt Mist für eine bestimmte Anzahl von Pilzkulturen hinzu
     */
    public void addMistForPlants(int plantsPerBag) {
        this.hasMist = true;
        int baseMistPerPlant = 15;
        int targetMist = (int) (baseMistPerPlant * plantsPerBag / potType.getConsumptionMultiplier());
        this.soilLevel = Math.min(soilLevel + targetMist, potType.getSoilCapacity());
    }

    public void setSoil(boolean hasSoil) {
        this.hasSoil = hasSoil;
        if (hasSoil) {
            this.soilLevel = potType.getSoilCapacity();
        } else {
            this.soilLevel = 0;
        }
    }

    /**
     * Fügt Erde für eine bestimmte Anzahl von Pflanzen hinzu
     * @param plantsPerBag Anzahl der Pflanzen, für die die Erde reichen soll
     */
    public void addSoilForPlants(int plantsPerBag) {
        this.hasSoil = true;
        // Kalibrierung: 15 Einheiten Erde reichen genau für 1 Pflanze (bei Terracotta)
        // Verbrauch: 15/7 = 2.1429 Erde pro Wachstumsschritt
        // Bei 7 Wachstumsschritten: 7 × 2.1429 = 15 Einheiten

        // Bessere Töpfe haben kleineren consumptionMultiplier (verbrauchen weniger)
        // Daher wird mehr Erde hinzugefügt, damit die gleiche Anzahl Pflanzen möglich ist
        int baseSoilPerPlant = 15;
        int targetSoil = (int) (baseSoilPerPlant * plantsPerBag / potType.getConsumptionMultiplier());

        // Addiere zur aktuellen Erde (falls schon welche vorhanden)
        this.soilLevel = Math.min(soilLevel + targetSoil, potType.getSoilCapacity());
    }

    /**
     * Setzt Wasser-Level direkt (für Deserialisierung)
     */
    public void setWaterLevel(double waterLevel) {
        this.waterLevel = Math.max(0, Math.min(waterLevel, getMaxWater()));
    }

    /**
     * Setzt Erde-Level direkt (für Deserialisierung)
     */
    public void setSoilLevel(double soilLevel) {
        this.soilLevel = Math.max(0, Math.min(soilLevel, getMaxSoil()));
        this.hasSoil = soilLevel > 0;
    }

    /**
     * Fügt Wasser hinzu
     */
    public void addWater(int amount) {
        waterLevel = Math.min(waterLevel + amount, getMaxWater());
    }

    /**
     * Verbraucht Wasser (präzise Berechnung mit double)
     */
    public boolean consumeWater(double amount) {
        double actualAmount = potType.calculateWaterConsumption(amount);
        if (waterLevel >= actualAmount) {
            waterLevel -= actualAmount; // Kein Cast mehr, präziser Verbrauch
            return true;
        }
        return false;
    }

    /**
     * Verbraucht Erde (präzise Berechnung mit double)
     */
    public boolean consumeSoil(double amount) {
        double actualAmount = potType.calculateSoilConsumption(amount);
        if (soilLevel >= actualAmount) {
            soilLevel -= actualAmount; // Kein Cast mehr, präziser Verbrauch
            // Aktualisiere hasSoil Flag wenn Erde aufgebraucht ist
            if (soilLevel <= 0.01) { // Kleine Toleranz für Floating-Point-Fehler
                hasSoil = false;
                soilLevel = 0;
            }
            return true;
        }
        return false;
    }

    /**
     * Pflanzt Tabak-Samen
     */
    public boolean plantSeed(TobaccoType type) {
        if (!hasSoil || hasPlant()) {
            return false;
        }

        this.plant = new TobaccoPlantData(type);
        return true;
    }

    /**
     * Pflanzt Cannabis-Samen
     */
    public boolean plantCannabisSeed(CannabisStrain strain) {
        if (!hasSoil || hasPlant()) {
            return false;
        }

        this.cannabisPlant = new CannabisPlantData(strain);
        return true;
    }

    /**
     * Pflanzt Koka-Samen
     */
    public boolean plantCocaSeed(CocaType type) {
        if (!hasSoil || hasPlant()) {
            return false;
        }

        this.cocaPlant = new CocaPlantData(type);
        return true;
    }

    /**
     * Pflanzt Mohn-Samen
     */
    public boolean plantPoppySeed(PoppyType type) {
        if (!hasSoil || hasPlant()) {
            return false;
        }

        this.poppyPlant = new PoppyPlantData(type);
        return true;
    }

    /**
     * Impft Mist mit Pilzsporen
     */
    public boolean plantMushroomSpore(MushroomType type) {
        if (!hasMist || hasPlant()) {
            return false;
        }

        this.mushroomPlant = new MushroomPlantData(type);
        return true;
    }

    /**
     * Erntet die Tabak-Pflanze
     */
    @Nullable
    public TobaccoPlantData harvest() {
        if (plant == null || !plant.isFullyGrown()) {
            return null;
        }

        TobaccoPlantData harvested = plant;
        plant = null;
        return harvested;
    }

    /**
     * Erntet die Cannabis-Pflanze
     */
    @Nullable
    public CannabisPlantData harvestCannabis() {
        if (cannabisPlant == null || !cannabisPlant.isFullyGrown()) {
            return null;
        }

        CannabisPlantData harvested = cannabisPlant;
        cannabisPlant = null;
        return harvested;
    }

    /**
     * Erntet die Koka-Pflanze
     */
    @Nullable
    public CocaPlantData harvestCoca() {
        if (cocaPlant == null || !cocaPlant.isFullyGrown()) {
            return null;
        }

        CocaPlantData harvested = cocaPlant;
        cocaPlant = null;
        return harvested;
    }

    /**
     * Erntet die Mohn-Pflanze
     */
    @Nullable
    public PoppyPlantData harvestPoppy() {
        if (poppyPlant == null || !poppyPlant.isFullyGrown()) {
            return null;
        }

        PoppyPlantData harvested = poppyPlant;
        poppyPlant = null;
        return harvested;
    }

    /**
     * Erntet Pilze (mit Flush-System)
     * @return MushroomPlantData oder null wenn nicht erntbar
     */
    @Nullable
    public MushroomPlantData harvestMushroom() {
        if (mushroomPlant == null || !mushroomPlant.canHarvest()) {
            return null;
        }

        // Speichere Daten vor der Ernte
        MushroomPlantData harvested = new MushroomPlantData(mushroomPlant.getType());
        harvested.setQuality(mushroomPlant.getQuality());
        harvested.setGrowthStage(mushroomPlant.getGrowthStage());

        // Führe Ernte durch (startet nächsten Flush oder beendet)
        boolean moreFlushes = mushroomPlant.harvest();

        if (!moreFlushes) {
            // Substrat erschöpft - Pflanze entfernen
            mushroomPlant = null;
            hasMist = false;
        }

        return harvested;
    }

    /**
     * Entfernt die Pflanze (ohne Ernte-Bedingungen)
     */
    public void clearPlant() {
        this.plant = null;
        this.cannabisPlant = null;
        this.cocaPlant = null;
        this.poppyPlant = null;
        this.mushroomPlant = null;
    }

    /**
     * Prüft ob die Pflanze wachsen kann (alle Pflanzentypen)
     */
    public boolean canGrow() {
        // Prüfe Tabak-Pflanze
        if (plant != null && !plant.isFullyGrown()) {
            return checkResourcesForGrowth();
        }

        // Prüfe Cannabis-Pflanze
        if (cannabisPlant != null && !cannabisPlant.isFullyGrown()) {
            return checkResourcesForGrowth();
        }

        // Prüfe Koka-Pflanze
        if (cocaPlant != null && !cocaPlant.isFullyGrown()) {
            return checkResourcesForGrowth();
        }

        // Prüfe Mohn-Pflanze
        if (poppyPlant != null && !poppyPlant.isFullyGrown()) {
            return checkResourcesForGrowth();
        }

        // Prüfe Pilzkultur (spezielle Regeln)
        if (mushroomPlant != null && !mushroomPlant.isFullyGrown()) {
            return checkResourcesForMushroomGrowth();
        }

        return false;
    }

    /**
     * Prüft ob Ressourcen für Pilzwachstum vorhanden sind
     * Pilze brauchen nur Wasser während Fruchtung (Stage 4-7)
     */
    private boolean checkResourcesForMushroomGrowth() {
        // Während Inkubation: Kein Wasser nötig
        if (mushroomPlant.isIncubating()) {
            return soilLevel > 0.5; // Nur Substrat nötig
        }

        // Während Fruchtung: Wasser nötig
        double waterNeeded = getMaxWater() / 7.0;
        double soilNeeded = 15.0 / 7.0;
        double tolerance = 0.5;

        return waterLevel >= (potType.calculateWaterConsumption(waterNeeded) - tolerance) &&
                soilLevel >= (potType.calculateSoilConsumption(soilNeeded) - tolerance);
    }

    /**
     * Prüft ob genug Ressourcen für Wachstum vorhanden sind
     */
    private boolean checkResourcesForGrowth() {
        // Ressourcen-Anforderungen für einen Wachstumsschritt
        // 7 Wachstumsschritte (0→1, 1→2, ... 6→7)
        // Verbrauche 1/7 der Topfkapazität pro Schritt
        double waterNeeded = getMaxWater() / 7.0;
        double soilNeeded = 15.0 / 7.0;

        // Kleine Toleranz (0.5) für Floating-Point-Rundungsfehler
        double tolerance = 0.5;

        return waterLevel >= (potType.calculateWaterConsumption(waterNeeded) - tolerance) &&
                soilLevel >= (potType.calculateSoilConsumption(soilNeeded) - tolerance);
    }

    /**
     * Gibt Wasser-Prozentsatz zurück
     */
    public float getWaterPercentage() {
        return (float) waterLevel / getMaxWater();
    }

    /**
     * Gibt Erde-Prozentsatz zurück
     */
    public float getSoilPercentage() {
        return (float) soilLevel / getMaxSoil();
    }
}
