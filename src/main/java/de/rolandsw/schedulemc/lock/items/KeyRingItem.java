package de.rolandsw.schedulemc.lock.items;

import de.rolandsw.schedulemc.lock.LockManager;
import de.rolandsw.schedulemc.lock.LockType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import java.util.ArrayList;
import java.util.List;

/**
 * Schluesselring: Haelt bis zu 8 Schluessel.
 *
 * - Rechtsklick auf Tuer: Versucht passenden Schluessel zu finden und zu nutzen
 * - Rechtsklick in Luft: Zeigt Info ueber alle gespeicherten Schluessel
 * - Sneak + Rechtsklick mit Schluessel in anderer Hand: Schluessel hinzufuegen
 *
 * NBT: { "Keys": [ {key compound tags}, ... ] }
 */
public class KeyRingItem extends Item {

    public static final int MAX_KEYS = 8;

    public KeyRingItem() {
        super(new Properties().stacksTo(1));
    }

    /** Schluessel zum Ring hinzufuegen. Gibt true zurueck bei Erfolg. */
    public static boolean addKey(ItemStack ring, ItemStack key) {
        if (getKeyCount(ring) >= MAX_KEYS) return false;
        CompoundTag tag = ring.getOrCreateTag();
        ListTag keys = tag.getList("Keys", Tag.TAG_COMPOUND);
        CompoundTag keyData = key.getTag() != null ? key.getTag().copy() : new CompoundTag();
        // Item-Registry-Name speichern fuer spaetere Rekonstruktion
        keyData.putString("_item", key.getItem().builtInRegistryHolder().key().location().toString());
        keys.add(keyData);
        tag.put("Keys", keys);
        return true;
    }

    /** Schluessel aus dem Ring entfernen (nach Index). */
    public static void removeKey(ItemStack ring, int index) {
        CompoundTag tag = ring.getTag();
        if (tag == null) return;
        ListTag keys = tag.getList("Keys", Tag.TAG_COMPOUND);
        if (index >= 0 && index < keys.size()) keys.remove(index);
    }

    /** Anzahl der Schluessel im Ring. */
    public static int getKeyCount(ItemStack ring) {
        CompoundTag tag = ring.getTag();
        if (tag == null) return 0;
        return tag.getList("Keys", Tag.TAG_COMPOUND).size();
    }

    /** Abgelaufene Schluessel entfernen. Gibt Anzahl entfernter Schluessel zurueck. */
    public static int cleanExpiredKeys(ItemStack ring) {
        CompoundTag tag = ring.getTag();
        if (tag == null) return 0;
        ListTag keys = tag.getList("Keys", Tag.TAG_COMPOUND);
        int removed = 0;
        List<Integer> toRemove = new ArrayList<>();
        long now = System.currentTimeMillis();
        for (int i = 0; i < keys.size(); i++) {
            CompoundTag kt = keys.getCompound(i);
            if (kt.contains("expire_time") && now > kt.getLong("expire_time")) {
                toRemove.add(i);
            }
        }
        for (int i = toRemove.size() - 1; i >= 0; i--) {
            keys.remove((int) toRemove.get(i));
            removed++;
        }
        return removed;
    }

    /** Passenden Schluessel fuer ein Schloss finden. Gibt Index zurueck, -1 wenn keiner passt. */
    public static int findKeyForLock(ItemStack ring, String lockId) {
        CompoundTag tag = ring.getTag();
        if (tag == null) return -1;
        ListTag keys = tag.getList("Keys", Tag.TAG_COMPOUND);
        long now = System.currentTimeMillis();
        for (int i = 0; i < keys.size(); i++) {
            CompoundTag kt = keys.getCompound(i);
            if (!kt.getString("lock_id").equals(lockId)) continue;
            // Abgelaufen?
            if (kt.contains("expire_time") && now > kt.getLong("expire_time")) continue;
            // Nutzungen?
            if (kt.contains("uses_left") && kt.getInt("uses_left") <= 0) continue;
            return i;
        }
        return -1;
    }

    /** Nutzung auf einem Schluessel im Ring abziehen. Gibt true wenn leer. */
    public static boolean consumeKeyUse(ItemStack ring, int index) {
        CompoundTag tag = ring.getTag();
        if (tag == null) return false;
        ListTag keys = tag.getList("Keys", Tag.TAG_COMPOUND);
        if (index < 0 || index >= keys.size()) return false;
        CompoundTag kt = keys.getCompound(index);
        if (!kt.contains("uses_left")) return false;
        int left = kt.getInt("uses_left") - 1;
        kt.putInt("uses_left", left);
        if (left <= 0) {
            keys.remove(index);
            return true;
        }
        return false;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();

        if (level.isClientSide()) return InteractionResult.PASS;
        if (!(level.getBlockState(pos).getBlock() instanceof DoorBlock)) return InteractionResult.PASS;
        if (!(ctx.getPlayer() instanceof ServerPlayer player)) return InteractionResult.PASS;

        if (level.getBlockState(pos).getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
            pos = pos.below();
        }

        LockManager mgr = LockManager.getInstance();
        if (mgr == null) return InteractionResult.FAIL;

        String dim = level.dimension().location().toString();
        String posKey = LockManager.posKey(dim, pos.getX(), pos.getY(), pos.getZ());
        var lockData = mgr.getLock(posKey);

        if (lockData == null) return InteractionResult.PASS; // Nicht gesperrt

        ItemStack ring = ctx.getItemInHand();

        // Abgelaufene Schluessel bereinigen
        int expired = cleanExpiredKeys(ring);
        if (expired > 0) {
            player.sendSystemMessage(Component.translatable("lock.ring.expired_removed", expired));
        }

        // Passenden Schluessel suchen
        int keyIdx = findKeyForLock(ring, lockData.getLockId());
        if (keyIdx < 0) {
            player.sendSystemMessage(Component.translatable("lock.ring.no_matching_key"));
            return InteractionResult.FAIL;
        }

        // Dual-Lock: Code noetig
        if (lockData.getType() == LockType.DUAL) {
            player.sendSystemMessage(Component.translatable("lock.ring.dual_enter_code"));
            return InteractionResult.SUCCESS;
        }

        // Nutzung abziehen
        boolean depleted = consumeKeyUse(ring, keyIdx);
        if (depleted) {
            player.sendSystemMessage(Component.translatable("lock.ring.key_depleted"));
        }

        // Tuer oeffnen
        DoorBlock door = (DoorBlock) level.getBlockState(pos).getBlock();
        door.setOpen(null, level, level.getBlockState(pos), pos, !level.getBlockState(pos).getValue(DoorBlock.OPEN));
        player.sendSystemMessage(Component.translatable("lock.ring.unlocked"));

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack ring = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResultHolder.pass(ring);

        // Abgelaufene bereinigen
        int expired = cleanExpiredKeys(ring);
        if (expired > 0) {
            player.sendSystemMessage(Component.translatable("lock.ring.expired_removed", expired));
        }

        // Sneak: Schluessel aus anderer Hand hinzufuegen
        if (player.isShiftKeyDown()) {
            InteractionHand other = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            ItemStack otherItem = player.getItemInHand(other);
            if (otherItem.getItem() instanceof KeyItem && !KeyItem.isBlank(otherItem)) {
                if (getKeyCount(ring) >= MAX_KEYS) {
                    player.sendSystemMessage(Component.translatable("lock.ring.full", MAX_KEYS));
                } else {
                    addKey(ring, otherItem);
                    player.setItemInHand(other, ItemStack.EMPTY);
                    player.sendSystemMessage(Component.translatable("lock.ring.added",
                            getKeyCount(ring), MAX_KEYS));
                }
                return InteractionResultHolder.success(ring);
            }
        }

        // Info anzeigen
        showKeyRingInfo(player, ring);
        return InteractionResultHolder.success(ring);
    }

    private void showKeyRingInfo(Player player, ItemStack ring) {
        int count = getKeyCount(ring);
        player.sendSystemMessage(Component.translatable("lock.ring.info_header", count, MAX_KEYS));

        CompoundTag tag = ring.getTag();
        if (tag == null || count == 0) {
            player.sendSystemMessage(Component.translatable("lock.ring.empty"));
            return;
        }

        ListTag keys = tag.getList("Keys", Tag.TAG_COMPOUND);
        long now = System.currentTimeMillis();
        for (int i = 0; i < keys.size(); i++) {
            CompoundTag kt = keys.getCompound(i);
            String lockId = kt.getString("lock_id");
            String lockType = kt.getString("lock_type");
            String origin = kt.getString("origin");
            int usesLeft = kt.contains("uses_left") ? kt.getInt("uses_left") : -1;
            long remaining = kt.contains("expire_time") ? kt.getLong("expire_time") - now : -1;

            String timeStr;
            if (remaining < 0) {
                timeStr = "§a" + Component.translatable("lock.ring.key_info.infinite").getString();
            } else if (remaining <= 0) {
                timeStr = "§c" + Component.translatable("lock.ring.key_info.expired").getString();
            } else if (remaining > 86400000) {
                timeStr = "§a" + Component.translatable("lock.time.days_hours",
                        (int)(remaining / 86400000),
                        (int)((remaining % 86400000) / 3600000)).getString();
            } else if (remaining > 3600000) {
                timeStr = "§e" + Component.translatable("lock.time.hours_short",
                        (int)(remaining / 3600000)).getString();
            } else {
                timeStr = "§c" + Component.translatable("lock.time.minutes",
                        (int)(remaining / 60000)).getString();
            }

            String usesStr = usesLeft > 0 ? usesLeft + "x" :
                    "§c" + Component.translatable("lock.ring.key_info.empty").getString();

            String originShort;
            if (origin.equals("STOLEN")) {
                originShort = "§c" + Component.translatable("lock.ring.key_info.origin.stolen").getString();
            } else if (origin.equals("COPY")) {
                originShort = "§e" + Component.translatable("lock.ring.key_info.origin.copy").getString();
            } else {
                originShort = "§a" + Component.translatable("lock.ring.key_info.origin.original").getString();
            }

            player.sendSystemMessage(Component.literal(
                    "§7" + (i + 1) + ". " + originShort + " §f" + lockId +
                            " §8(" + lockType + ") " + timeStr + " §8| " + usesStr));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tips, TooltipFlag flag) {
        int count = getKeyCount(stack);
        tips.add(Component.translatable("lock.ring.tooltip.title").withStyle(ChatFormatting.GOLD));
        tips.add(Component.translatable("lock.ring.tooltip.count", count, MAX_KEYS));
        tips.add(Component.translatable("lock.ring.tooltip.use_on_door"));
        tips.add(Component.translatable("lock.ring.tooltip.use_in_air"));
        tips.add(Component.translatable("lock.ring.tooltip.sneak_add"));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return getKeyCount(stack) > 0;
    }
}
