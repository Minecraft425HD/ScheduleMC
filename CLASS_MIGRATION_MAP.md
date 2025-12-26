# Klassen-Migrations-Plan: VoxelMap-Struktur → Layer-Architektur

## 📋 Kategorisierung aller Klassen

### 🎯 CORE LAYER (Domain Models & Events)

**core/model/** - Daten-Modelle
- util/ChunkData.java → core/model/MapChunk.java
- util/BiomeMapData.java → core/model/BiomeData.java
- util/BlockModel.java → core/model/BlockModel.java
- MapConfiguration.java → config/MapViewConfiguration.java

**core/event/** - Events & Interfaces
- interfaces/IChangeObserver.java → core/event/MapChangeListener.java
- interfaces/AbstractMapData.java → core/model/AbstractMapData.java
- interfaces/ISettingsManager.java → core/event/SettingsManager.java
- interfaces/ISubSettingsManager.java → core/event/SubSettingsManager.java

---

### 💼 SERVICE LAYER (Business Logic)

**service/render/** - Rendering Services
- MapViewRenderer.java → service/render/MapRenderService.java
- BlockColorCache.java → service/render/ColorCalculationService.java
- util/CPUMapRenderer.java → service/render/LightingCalculator.java
- util/ColorUtils.java → service/render/ColorUtils.java

**service/scan/** - Chunk Scanning Services
- util/ChunkCache.java → service/scan/ChunkScanService.java
- util/BiomeParser.java → service/scan/BiomeScanner.java
- util/BlockStateParser.java → service/scan/BlockStateAnalyzer.java
- util/HeightUtils.java → service/scan/HeightCalculator.java

**service/data/** - Data Management Services
- MapCore.java → service/data/MapDataManager.java
- persistent/WorldMapData.java → service/data/WorldMapService.java
- util/DimensionManager.java → service/data/DimensionService.java
- ConfigurationChangeNotifier.java → service/data/ConfigNotificationService.java

---

### 💾 DATA LAYER (Storage & Caching)

**data/repository/** - Data Repositories
- util/ChunkCache.java → data/repository/ChunkDataRepository.java (split)
- util/MapDataStore.java → data/repository/MapDataRepository.java
- persistent/WorldMapSettings.java → data/repository/WorldMapRepository.java

**data/cache/** - Caching
- persistent/RegionCache.java → data/cache/RegionCache.java
- persistent/EmptyRegionCache.java → data/cache/EmptyRegionCache.java
- persistent/ComparisonRegionCache.java → data/cache/ComparisonRegionCache.java
- util/MutableBlockPosCache.java → data/cache/BlockPositionCache.java

**data/persistence/** - File I/O & Persistence
- persistent/ThreadManager.java → data/persistence/AsyncPersistenceManager.java
- persistent/CompressedMapData.java → data/persistence/CompressedMapData.java
- persistent/CompressedGLImage.java → data/persistence/CompressedImageData.java
- util/CompressionUtils.java → data/persistence/CompressionUtils.java

---

### 🎨 PRESENTATION LAYER (UI)

**presentation/widget/** - UI Widgets
- (neu) → presentation/widget/MinimapWidget.java
- (neu) → presentation/widget/MapOverlayWidget.java

**presentation/screen/** - Screens
- persistent/WorldMapScreen.java → presentation/screen/WorldMapScreen.java
- gui/GuiMapViewOptions.java → presentation/screen/MapOptionsScreen.java
- gui/overridden/GuiScreenMapView.java → presentation/screen/BaseMapScreen.java
- gui/overridden/PopupGuiScreen.java → presentation/screen/PopupScreen.java

**presentation/renderer/** - Renderers
- MapViewRenderer.java → presentation/renderer/HudMapRenderer.java (split)
- (neu) → presentation/renderer/FullscreenMapRenderer.java

**presentation/component/** - UI Components
- gui/overridden/GuiButtonText.java → presentation/component/TextButton.java
- gui/overridden/GuiOptionButtonMapView.java → presentation/component/OptionButton.java
- gui/overridden/GuiOptionSliderMapView.java → presentation/component/OptionSlider.java
- gui/overridden/Popup.java → presentation/component/PopupComponent.java
- gui/overridden/PopupGuiButton.java → presentation/component/PopupButton.java

---

### 🔌 INTEGRATION LAYER (External Integration)

**integration/forge/** - Forge Integration
- forge/ForgeEvents.java → integration/forge/ForgeEventHandler.java
- forge/ForgeModApiBridge.java → integration/forge/ForgeModBridge.java
- forge/ForgePacketBridge.java → integration/forge/ForgeNetworkHandler.java
- forge/MapViewSettingsChannelHandlerForge.java → integration/forge/SettingsPacketHandler.java
- forge/MapViewWorldIdChannelHandlerForge.java → integration/forge/WorldIdPacketHandler.java
- forge/mixins/MixinRenderPipelines.java → integration/forge/mixins/MixinRenderPipelines.java

**integration/minecraft/** - Minecraft Integration
- mixins/APIMixinChatListenerHud.java → integration/minecraft/ChatHudMixin.java
- mixins/APIMixinMinecraftClient.java → integration/minecraft/MinecraftClientMixin.java
- mixins/APIMixinNetHandlerPlayClient.java → integration/minecraft/NetworkHandlerMixin.java
- mixins/AccessorEnderDragonRenderer.java → integration/minecraft/EnderDragonAccessor.java
- mixins/MixinChatHud.java → integration/minecraft/ChatHudMixin.java
- mixins/MixinInGameHud.java → integration/minecraft/InGameHudMixin.java
- mixins/MixinWorldRenderer.java → integration/minecraft/WorldRendererMixin.java
- util/GameVariableAccessShim.java → integration/minecraft/MinecraftAccessor.java

**integration/network/** - Network Packets
- packets/MapViewSettingsS2C.java → integration/network/MapSettingsPacket.java
- packets/WorldIdC2S.java → integration/network/WorldIdClientPacket.java
- packets/WorldIdS2C.java → integration/network/WorldIdServerPacket.java

---

### ⚙️ CONFIG LAYER

**config/** - Configuration
- MapConfiguration.java → config/MapViewConfiguration.java
- persistent/WorldMapSettings.java → config/WorldMapConfiguration.java
- gui/overridden/EnumOptionsMapView.java → config/MapOption.java

---

### 🛠️ UTILITY LAYER (Behalten, aber reorganisieren)

**util/** - Utilities (nur echte Utils, keine Business Logic)
- util/TextUtils.java → util/TextUtils.java
- util/MessageUtils.java → util/MessageUtils.java
- util/ReflectionUtils.java → util/ReflectionUtils.java
- util/ImageHelper.java → util/ImageUtils.java
- util/GLUtils.java → util/GLUtils.java
- util/EasingUtils.java → util/AnimationUtils.java
- util/MutableBlockPos.java → util/MutableBlockPos.java

---

### 📦 BEHALTEN (nicht verschieben)

**textures/** - Texture System (eigenständiges Modul)
- textures/* (alle behalten)

**entityrender/** - Entity Rendering (eigenständiges Modul)
- entityrender/* (alle behalten)

---

## 🎯 Migrations-Strategie

### Phase 1A: Core Layer (einfach, keine Dependencies)
1. model/ Klassen verschieben
2. event/ Interfaces verschieben

### Phase 1B: Data Layer (mittlere Dependencies)
1. cache/ Klassen verschieben
2. persistence/ Klassen verschieben
3. repository/ Klassen verschieben

### Phase 1C: Service Layer (viele Dependencies)
1. service/scan/ verschieben
2. service/render/ verschieben
3. service/data/ verschieben

### Phase 1D: Presentation Layer
1. presentation/component/ verschieben
2. presentation/screen/ verschieben
3. presentation/renderer/ verschieben

### Phase 1E: Integration Layer
1. integration/minecraft/ verschieben
2. integration/forge/ verschieben
3. integration/network/ verschieben

### Phase 1F: Config Layer
1. config/ verschieben

---

## ⚠️ Spezielle Fälle (Split-Klassen)

### MapViewRenderer.java → AUFTEILEN in:
1. **service/render/MapRenderService.java** - Business Logic
2. **presentation/renderer/HudMapRenderer.java** - HUD Rendering
3. **presentation/renderer/FullscreenMapRenderer.java** - Fullscreen Rendering

### ChunkCache.java → AUFTEILEN in:
1. **data/cache/ChunkCache.java** - Caching Logic
2. **data/repository/ChunkDataRepository.java** - Data Access
3. **service/scan/ChunkScanService.java** - Scanning Logic

### MapCore.java → AUFTEILEN in:
1. **service/data/MapDataManager.java** - Orchestrator
2. **config/MapViewConfiguration.java** - Config Management

---

## 📊 Statistik

- **Gesamt Klassen:** ~98
- **Core Layer:** ~10
- **Service Layer:** ~15
- **Data Layer:** ~12
- **Presentation Layer:** ~20
- **Integration Layer:** ~25
- **Config Layer:** ~3
- **Utility:** ~8
- **Behalten (textures, entityrender):** ~5

