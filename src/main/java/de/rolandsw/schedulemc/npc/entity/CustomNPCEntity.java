package de.rolandsw.schedulemc.npc.entity;

import de.rolandsw.schedulemc.npc.data.NPCData;
import de.rolandsw.schedulemc.npc.goals.MoveToHomeGoal;
import de.rolandsw.schedulemc.npc.goals.MoveToWorkGoal;
import de.rolandsw.schedulemc.npc.menu.NPCInteractionMenu;
import de.rolandsw.schedulemc.npc.pathfinding.NPCPathNavigation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;

/**
 * Custom NPC Entity
 * - Kein Villager
 * - Eigener Player-Skin
 * - Interaktionssystem (Dialog, Kaufen, Verkaufen)
 * - Erweiterbar für zukünftige Features
 */
public class CustomNPCEntity extends PathfinderMob {

    // Synced Data für Client-Server Kommunikation
    private static final EntityDataAccessor<String> NPC_NAME =
        SynchedEntityData.defineId(CustomNPCEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> SKIN_FILE =
        SynchedEntityData.defineId(CustomNPCEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> NPC_TYPE_ORDINAL =
        SynchedEntityData.defineId(CustomNPCEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MERCHANT_CATEGORY_ORDINAL =
        SynchedEntityData.defineId(CustomNPCEntity.class, EntityDataSerializers.INT);

    // NPC Daten (Server-Side)
    private NPCData npcData;

    public CustomNPCEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.npcData = new NPCData();
        this.setMaxUpStep(1.0F); // Ermöglicht das Steigen auf Blöcke (Treppen)
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(NPC_NAME, "NPC");
        this.entityData.define(SKIN_FILE, "default.png");
        this.entityData.define(NPC_TYPE_ORDINAL, 0); // BEWOHNER
        this.entityData.define(MERCHANT_CATEGORY_ORDINAL, 0); // BAUMARKT
    }

    @Override
    protected void registerGoals() {
        // Grundlegende AI Goals
        this.goalSelector.addGoal(0, new FloatGoal(this)); // Schwimmen
        this.goalSelector.addGoal(1, new OpenDoorGoal(this, true)); // Türen öffnen (und schließen)
        this.goalSelector.addGoal(2, new MoveToHomeGoal(this)); // Nachts nach Hause gehen
        this.goalSelector.addGoal(3, new MoveToWorkGoal(this)); // Tagsüber zur Arbeit gehen
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F)); // Spieler anschauen
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this)); // Zufällig umschauen
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new NPCPathNavigation(this, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.3D)
            .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            // Admin: Shift + Rechtsklick öffnet Shop-Editor (nur für Verkäufer)
            if (player.isShiftKeyDown() && serverPlayer.hasPermissions(2)) {
                if (getNpcType() == de.rolandsw.schedulemc.npc.data.NPCType.VERKAEUFER) {
                    openShopEditor(serverPlayer);
                    return InteractionResult.SUCCESS;
                }
            }

            // Normal: Öffne Interaktions-GUI
            openInteractionMenu(serverPlayer);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    /**
     * Öffnet das Interaktionsmenü für den Spieler
     */
    private void openInteractionMenu(ServerPlayer player) {
        NetworkHooks.openScreen(player, new SimpleMenuProvider(
            (id, playerInventory, p) -> new NPCInteractionMenu(id, playerInventory, this),
            Component.literal(this.getNpcName())
        ), buf -> {
            buf.writeInt(this.getId()); // Entity ID für Client-Side
        });
    }

    /**
     * Öffnet den Shop-Editor für Admins (nur Verkäufer)
     */
    private void openShopEditor(ServerPlayer player) {
        NetworkHooks.openScreen(player, new SimpleMenuProvider(
            (id, playerInventory, p) -> new de.rolandsw.schedulemc.npc.menu.ShopEditorMenu(id, playerInventory, this),
            Component.literal("Shop Editor: " + this.getNpcName())
        ), buf -> {
            buf.writeInt(this.getId()); // Entity ID
            // Sende aktuelle Shop-Items
            var shopItems = this.getNpcData().getBuyShop().getEntries();
            buf.writeInt(shopItems.size());
            for (var entry : shopItems) {
                buf.writeItem(entry.getItem());
                buf.writeInt(entry.getPrice());
                buf.writeBoolean(entry.isUnlimited());
                buf.writeInt(entry.getStock());
            }
        });
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            // Server-Side Logic

            // Look at nearest player
            if (npcData.getBehavior().shouldLookAtPlayer()) {
                Player nearestPlayer = this.level().getNearestPlayer(this, 8.0D);
                if (nearestPlayer != null) {
                    this.getLookControl().setLookAt(nearestPlayer, 10.0F, 10.0F);
                }
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("NPCData", npcData.save(new CompoundTag()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("NPCData")) {
            npcData.load(tag.getCompound("NPCData"));
            // Sync to client
            syncToClient();
        }
    }

    /**
     * Synchronisiert wichtige Daten zum Client
     */
    private void syncToClient() {
        this.entityData.set(NPC_NAME, npcData.getNpcName());
        this.entityData.set(SKIN_FILE, npcData.getSkinFileName());
        this.entityData.set(NPC_TYPE_ORDINAL, npcData.getNpcType().ordinal());
        this.entityData.set(MERCHANT_CATEGORY_ORDINAL, npcData.getMerchantCategory().ordinal());
    }

    // Custom Name Handling
    @Override
    public Component getName() {
        return Component.literal(getNpcName());
    }

    @Override
    public boolean hasCustomName() {
        return true;
    }

    @Nullable
    @Override
    public Component getCustomName() {
        return Component.literal(getNpcName());
    }

    // Getters & Setters
    public NPCData getNpcData() {
        return npcData;
    }

    public void setNpcData(NPCData data) {
        this.npcData = data;
        syncToClient();
    }

    public String getNpcName() {
        return this.entityData.get(NPC_NAME);
    }

    public void setNpcName(String name) {
        this.npcData.setNpcName(name);
        this.entityData.set(NPC_NAME, name);
    }

    public String getSkinFileName() {
        return this.entityData.get(SKIN_FILE);
    }

    public void setSkinFileName(String skinFile) {
        this.npcData.setSkinFileName(skinFile);
        this.entityData.set(SKIN_FILE, skinFile);
    }

    /**
     * Gibt den NPC-Typ zurück (Client-safe via synced data)
     */
    public de.rolandsw.schedulemc.npc.data.NPCType getNpcType() {
        return de.rolandsw.schedulemc.npc.data.NPCType.fromOrdinal(this.entityData.get(NPC_TYPE_ORDINAL));
    }

    /**
     * Gibt die Verkäufer-Kategorie zurück (Client-safe via synced data)
     */
    public de.rolandsw.schedulemc.npc.data.MerchantCategory getMerchantCategory() {
        return de.rolandsw.schedulemc.npc.data.MerchantCategory.fromOrdinal(this.entityData.get(MERCHANT_CATEGORY_ORDINAL));
    }

    // Verhindern von Despawning
    @Override
    public boolean isPersistenceRequired() {
        return true;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    @Override
    public void checkDespawn() {
        // NPCs despawnen nie
    }

    // Verhindern von Schaden (optional - kann angepasst werden)
    @Override
    public boolean isInvulnerable() {
        return true; // NPCs sind unsterblich (kann später konfigurierbar gemacht werden)
    }
}
