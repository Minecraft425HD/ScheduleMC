package de.rolandsw.schedulemc.weapon.gun;

import de.rolandsw.schedulemc.weapon.attachment.Attachment;
import de.rolandsw.schedulemc.weapon.attachment.BaseAttachmentItem;
import de.rolandsw.schedulemc.weapon.attachment.WeaponAttachments;
import de.rolandsw.schedulemc.weapon.entity.WeaponBulletEntity;
import de.rolandsw.schedulemc.weapon.item.WeaponItems;
import de.rolandsw.schedulemc.weapon.particle.WeaponParticles;
import de.rolandsw.schedulemc.weapon.sound.WeaponSounds;
import de.rolandsw.schedulemc.weapon.upgrade.BaseFireModeUpgradeItem;
import de.rolandsw.schedulemc.weapon.upgrade.FireModeUpgradeType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class GunItem extends Item {
    protected final GunProperties properties;

    private static final String TAG_AMMO = "CurrentAmmo";
    private static final String TAG_FIRE_MODE = "FireMode";
    private static final String TAG_LAST_SHOT_TIME = "LastShotTime";
    private static final String TAG_SHOTS_FIRED = "ShotsFired";
    private static final String TAG_ATTACHMENT_1 = "Attachment1";
    private static final String TAG_ATTACHMENT_2 = "Attachment2";
    private static final String TAG_AMMO_TYPE = "AmmoType";
    private static final String TAG_UNLOCKED_MODES = "UnlockedModes";
    private static final String TAG_SINGLE_PRECISION = "SinglePrecisionUpgrade";
    private static final String TAG_LAST_FIRE_TIME = "LastFireTime";

    public GunItem(GunProperties properties) {
        super(new Item.Properties().stacksTo(1).durability(properties.getDurability()));
        this.properties = properties;
    }

    protected int getConfigRange() {
        return properties.getRange();
    }

    public int getCurrentAmmo(ItemStack stack) {
        return stack.getOrCreateTag().getInt(TAG_AMMO);
    }

    public void setCurrentAmmo(ItemStack stack, int ammo) {
        stack.getOrCreateTag().putInt(TAG_AMMO, Math.max(0, Math.min(ammo, properties.getMaxAmmo())));
    }

    public int getFireMode(ItemStack stack) {
        return stack.getOrCreateTag().getInt(TAG_FIRE_MODE);
    }

    public int getUnlockedModes(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(TAG_UNLOCKED_MODES)) return 1;
        return tag.getInt(TAG_UNLOCKED_MODES);
    }

    public void setUnlockedModes(ItemStack stack, int bitmask) {
        stack.getOrCreateTag().putInt(TAG_UNLOCKED_MODES, bitmask);
    }

    public Set<Integer> getCompatibleFireModes() {
        return Set.of(0);
    }

    public void cycleFireMode(ItemStack stack, Player player) {
        int current = getFireMode(stack);
        int unlocked = getUnlockedModes(stack);

        for (int i = 1; i <= 2; i++) {
            int next = (current + i) % 3;
            if ((unlocked & (1 << next)) != 0) {
                stack.getOrCreateTag().putInt(TAG_FIRE_MODE, next);
                String modeName = switch (next) {
                    case 0 -> "§aEinzelfeuer";
                    case 1 -> "§eBurst (3)";
                    case 2 -> "§cVollauto";
                    default -> "";
                };
                player.displayClientMessage(Component.literal("Feuermodus: " + modeName), true);
                player.playSound(WeaponSounds.CLICK.get(), 0.5F, 1.0F);
                return;
            }
        }
        player.displayClientMessage(Component.literal("§7Keine weiteren Modi freigeschaltet."), true);
    }

    public boolean addAttachment(ItemStack gunStack, Attachment attachment) {
        if (hasAttachmentType(gunStack, attachment.getType())) return false;
        CompoundTag tag = gunStack.getOrCreateTag();
        if (!tag.contains(TAG_ATTACHMENT_1)) {
            tag.putString(TAG_ATTACHMENT_1, attachment.getType().name());
            return true;
        }
        if (!tag.contains(TAG_ATTACHMENT_2)) {
            tag.putString(TAG_ATTACHMENT_2, attachment.getType().name());
            return true;
        }
        return false;
    }

    public boolean hasAttachment(ItemStack gunStack) {
        if (!gunStack.hasTag()) return false;
        CompoundTag tag = gunStack.getTag();
        return tag.contains(TAG_ATTACHMENT_1) || tag.contains(TAG_ATTACHMENT_2);
    }

    public boolean hasAttachmentType(ItemStack gunStack, Attachment.Type type) {
        if (!gunStack.hasTag()) return false;
        String name = type.name();
        CompoundTag tag = gunStack.getTag();
        return name.equals(tag.getString(TAG_ATTACHMENT_1)) || name.equals(tag.getString(TAG_ATTACHMENT_2));
    }

    public List<Attachment> getAttachments(ItemStack gunStack) {
        List<Attachment> result = new ArrayList<>();
        if (!gunStack.hasTag()) return result;
        CompoundTag tag = gunStack.getTag();
        if (tag.contains(TAG_ATTACHMENT_1)) {
            Attachment a = WeaponAttachments.getByType(tag.getString(TAG_ATTACHMENT_1));
            if (a != null) result.add(a);
        }
        if (tag.contains(TAG_ATTACHMENT_2)) {
            Attachment a = WeaponAttachments.getByType(tag.getString(TAG_ATTACHMENT_2));
            if (a != null) result.add(a);
        }
        return result;
    }

    public Attachment getAttachment(ItemStack gunStack) {
        List<Attachment> atts = getAttachments(gunStack);
        return atts.isEmpty() ? null : atts.get(0);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack thisStack, ItemStack other, Slot slot,
                                             ClickAction action, Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY) return false;

        if (other.getItem() instanceof BaseFireModeUpgradeItem upgradeItem) {
            FireModeUpgradeType upgradeType = upgradeItem.getUpgradeType();
            int requiredMode = upgradeType.getModeIndex();

            if (requiredMode != -1 && !getCompatibleFireModes().contains(requiredMode)) {
                if (!player.level().isClientSide) {
                    player.displayClientMessage(
                        Component.literal("§cDiese Waffe unterstützt diesen Feuermodus nicht!"), true);
                }
                return true;
            }

            if (requiredMode != -1) {
                int unlocked = getUnlockedModes(thisStack);
                if ((unlocked & (1 << requiredMode)) != 0) {
                    if (!player.level().isClientSide) {
                        player.displayClientMessage(
                            Component.literal("§cDieser Modus ist bereits freigeschaltet!"), true);
                    }
                    return true;
                }
            } else {
                if (thisStack.getOrCreateTag().getBoolean(TAG_SINGLE_PRECISION)) {
                    if (!player.level().isClientSide) {
                        player.displayClientMessage(
                            Component.literal("§cPräzisions-Upgrade bereits montiert!"), true);
                    }
                    return true;
                }
            }

            if (requiredMode != -1) {
                int unlocked = getUnlockedModes(thisStack);
                setUnlockedModes(thisStack, unlocked | (1 << requiredMode));
            } else {
                thisStack.getOrCreateTag().putBoolean(TAG_SINGLE_PRECISION, true);
            }
            other.shrink(1);
            if (!player.level().isClientSide) {
                player.displayClientMessage(
                    Component.literal("§a" + upgradeType.getDisplayName() + " angewendet!"), true);
            }
            return true;
        }

        if (!(other.getItem() instanceof BaseAttachmentItem attachmentItem)) return false;

        Attachment attachment = attachmentItem.getModAttachment();
        if (hasAttachmentType(thisStack, attachment.getType())) {
            if (!player.level().isClientSide) {
                player.displayClientMessage(
                    Component.literal("§cDieses Zubehör ist bereits montiert!"), true);
            }
            return true;
        }
        if (!addAttachment(thisStack, attachment)) {
            if (!player.level().isClientSide) {
                player.displayClientMessage(
                    Component.literal("§cMaximal 2 Zubehörteile pro Waffe!"), true);
            }
            return true;
        }
        other.shrink(1);
        if (!player.level().isClientSide) {
            String name = switch (attachment.getType()) {
                case SCOPE -> "Visier";
                case SILENCER -> "Schalldämpfer";
            };
            player.displayClientMessage(Component.literal("§a" + name + " montiert!"), true);
        }
        return true;
    }

    public Item getLoadedAmmoType(ItemStack gunStack) {
        if (!gunStack.hasTag() || !gunStack.getTag().contains(TAG_AMMO_TYPE))
            return WeaponItems.AMMO_STANDARD.get();
        String ammoKey = gunStack.getTag().getString(TAG_AMMO_TYPE);
        Item found = ForgeRegistries.ITEMS.getValue(new ResourceLocation(ammoKey));
        return found != null ? found : WeaponItems.AMMO_STANDARD.get();
    }

    public void setLoadedAmmoType(ItemStack gunStack, Item ammoItem) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(ammoItem);
        if (key != null) {
            gunStack.getOrCreateTag().putString(TAG_AMMO_TYPE, key.toString());
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack gunStack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                cycleFireMode(gunStack, player);
            }
            return InteractionResultHolder.success(gunStack);
        }

        return InteractionResultHolder.pass(gunStack);
    }

    public boolean isOnCooldown(ItemStack stack, Level level) {
        long lastFire = stack.getOrCreateTag().getLong(TAG_LAST_FIRE_TIME);
        return level.getGameTime() - lastFire < getCurrentCooldown(stack);
    }

    public void performShots(Level level, Player player, ItemStack gunStack, int count) {
        if (isOnCooldown(gunStack, level)) return;
        for (int i = 0; i < count; i++) {
            if (!canShoot(gunStack, player)) break;
            if (!player.isCreative()) {
                int currentAmmo = getCurrentAmmo(gunStack);
                if (currentAmmo <= 0) break;
                setCurrentAmmo(gunStack, currentAmmo - 1);
            }
            shootProjectile(level, player, gunStack);
            addShotHistory(gunStack, level);
        }
        gunStack.getOrCreateTag().putLong(TAG_LAST_FIRE_TIME, level.getGameTime());
    }

    public void shootProjectile(Level level, Player player, ItemStack gunStack) {
        Item ammoType = getLoadedAmmoType(gunStack);
        List<Attachment> attachments = getAttachments(gunStack);

        float baseDamage = properties.getBaseDamage();
        if (ammoType == WeaponItems.AMMO_AP.get()) baseDamage *= 0.8;
        else if (ammoType == WeaponItems.AMMO_RUBBER.get()) baseDamage *= 0.3;
        for (Attachment a : attachments) baseDamage *= a.getDamageMultiplier();

        Attachment firstAttachment = attachments.isEmpty() ? null : attachments.get(0);
        WeaponBulletEntity bullet = new WeaponBulletEntity(level, player, baseDamage, ammoType, firstAttachment, getConfigRange());
        double accuracy = getCurrentAccuracy(gunStack, level);
        double spread;
        if (getFireMode(gunStack) == 0) {
            spread = (1.0 - accuracy) * 0.1;
        } else {
            int shotsFired = getShotsFired(gunStack);
            spread = (1.0 - accuracy) * 0.1 * (1 + shotsFired * 0.1);
        }

        Vec3 lookVec = player.getLookAngle();
        bullet.shoot(
                lookVec.x + (level.random.nextDouble() - 0.5) * spread,
                lookVec.y + (level.random.nextDouble() - 0.5) * spread,
                lookVec.z + (level.random.nextDouble() - 0.5) * spread,
                3.0F, 0F
        );

        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, gunStack) > 0) {
            bullet.setSecondsOnFire(100);
        }

        level.addFreshEntity(bullet);

        float volume = 1.0f;
        for (Attachment a : attachments) volume *= a.getSoundMultiplier();
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                WeaponSounds.GUN_SHOT.get(), SoundSource.PLAYERS, volume, 1.0F);

        // Muzzle flash particles disabled
    }

    public boolean canShoot(ItemStack stack, Player player) {
        if (player.isCreative()) return true;
        if (properties.usesMagazines()) {
            return getCurrentAmmo(stack) > 0;
        } else {
            return hasAmmo(player);
        }
    }

    private boolean hasAmmo(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == properties.getAmmoType()) return true;
        }
        return false;
    }

    public void reload(Player player, ItemStack gunStack) {
        if (player.isCreative()) {
            setCurrentAmmo(gunStack, properties.getMaxAmmo());
            setLoadedAmmoType(gunStack, WeaponItems.AMMO_STANDARD.get());
            return;
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == properties.getAmmoType()) {
                Item loadedAmmo = stack.getItem();
                stack.shrink(1);
                setCurrentAmmo(gunStack, properties.getMaxAmmo());
                setLoadedAmmoType(gunStack, loadedAmmo);
                player.playSound(WeaponSounds.RELOAD.get(), 1.0F, 1.0F);
                return;
            }
        }
        player.displayClientMessage(Component.literal("§cKein Magazin gefunden!"), true);
    }

    private int getShotsFired(ItemStack stack) {
        return stack.getOrCreateTag().getInt(TAG_SHOTS_FIRED);
    }

    public void addShotHistory(ItemStack stack, Level level) {
        CompoundTag tag = stack.getOrCreateTag();
        int shots = Math.min(tag.getInt(TAG_SHOTS_FIRED) + 1, 1000);
        tag.putInt(TAG_SHOTS_FIRED, shots);
        tag.putLong(TAG_LAST_SHOT_TIME, level.getGameTime());
    }

    public void resetShotHistory(ItemStack stack) {
        stack.getOrCreateTag().putInt(TAG_SHOTS_FIRED, 0);
    }

    protected double getCurrentAccuracy(ItemStack stack, Level level) {
        double accuracy = properties.getBaseAccuracy();
        for (Attachment a : getAttachments(stack)) accuracy *= a.getAccuracyMultiplier();
        int shots = getShotsFired(stack);
        long last = stack.getOrCreateTag().getLong(TAG_LAST_SHOT_TIME);
        long now = level.getGameTime();
        long elapsed = now - last;
        if (elapsed < 0 || elapsed > 40) {
            resetShotHistory(stack);
        } else {
            accuracy *= Math.max(0.5, 1.0 - shots * 0.05);
        }
        if (getFireMode(stack) == 0 && stack.getOrCreateTag().getBoolean(TAG_SINGLE_PRECISION)) {
            accuracy = Math.min(1.0, accuracy * 1.1);
        }
        return accuracy;
    }

    public int getCurrentCooldown(ItemStack stack) {
        int cd = properties.getBaseCooldown();
        for (Attachment a : getAttachments(stack)) {
            if (a.getType() == Attachment.Type.SILENCER) {
                cd = (int) (cd * 1.2f);
            }
        }
        return cd;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        int ammo = getCurrentAmmo(stack);
        tooltip.add(Component.literal("§7Munition: §a" + ammo + "/" + properties.getMaxAmmo()));
        int mode = getFireMode(stack);
        String modeStr = switch (mode) {
            case 0 -> "Einzelfeuer";
            case 1 -> "Burst";
            case 2 -> "Vollauto";
            default -> "";
        };
        tooltip.add(Component.literal("§7Modus: §e" + modeStr));
        List<Attachment> atts = getAttachments(stack);
        if (!atts.isEmpty()) {
            String names = atts.stream()
                .map(a -> switch (a.getType()) {
                    case SCOPE -> "Visier";
                    case SILENCER -> "Schalldämpfer";
                })
                .collect(Collectors.joining(", "));
            tooltip.add(Component.literal("§7Zubehör: §d" + names));
        }
        if (stack.hasTag()) {
            int unlocked = getUnlockedModes(stack);
            List<String> modeNames = new ArrayList<>();
            if ((unlocked & 2) != 0) modeNames.add("Burst");
            if ((unlocked & 4) != 0) modeNames.add("Vollauto");
            if (!modeNames.isEmpty()) {
                tooltip.add(Component.literal("§7Freigeschaltete Modi: §e" + String.join(", ", modeNames)));
            }
            if (stack.getTag().getBoolean(TAG_SINGLE_PRECISION)) {
                tooltip.add(Component.literal("§7Upgrade: §aPräzisions-Verbesserung (+10%)"));
            }
        }
        Item ammoType = getLoadedAmmoType(stack);
        tooltip.add(Component.literal("§7Geladene Munition: §b" + ammoType.getDescription().getString()));
    }

    public GunProperties getProperties() { return properties; }
}
