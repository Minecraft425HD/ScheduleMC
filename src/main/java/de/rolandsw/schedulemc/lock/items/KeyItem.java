package de.rolandsw.schedulemc.lock.items;

import de.rolandsw.schedulemc.lock.LockData;
import de.rolandsw.schedulemc.lock.LockManager;
import de.rolandsw.schedulemc.lock.LockType;
import de.rolandsw.schedulemc.lock.network.LockNetworkHandler;
import de.rolandsw.schedulemc.lock.network.OpenCodeEntryPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import java.util.List;

/**
 * Schluessel-Rohling / Schluessel.
 *
 * Ohne NBT-Daten: Rohling (kann auf gesperrte Tuer verwendet werden = wird zum Schluessel)
 * Mit NBT-Daten (lock_id etc.): Schluessel (kann Tuer oeffnen)
 *
 * NBT-Tags:
 *   lock_id     - ID des Schlosses
 *   lock_type   - LockType name
 *   door_x/y/z  - Position der Tuer
 *   expire_time - Ablaufzeit (ms seit Epoch)
 *   uses_left   - Verbleibende Nutzungen
 *   origin      - ORIGINAL / COPY / STOLEN
 *   created     - Erstellungszeit
 */
public class KeyItem extends Item {

    private final int blankTier;      // 0=Kupfer, 1=Eisen, 2=Netherite
    private final String tierName;

    public KeyItem(int blankTier, String tierName) {
        super(new Properties().stacksTo(1));
        this.blankTier = blankTier;
        this.tierName = tierName;
    }

    public int getBlankTier() { return blankTier; }

    /** Prueft ob dieser Stack ein Rohling (ohne lock_id) ist. */
    public static boolean isBlank(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null || !tag.contains("lock_id");
    }

    /** Prueft ob dieser Schluessel abgelaufen ist. */
    public static boolean isExpired(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("expire_time")) return false;
        return System.currentTimeMillis() > tag.getLong("expire_time");
    }

    /** Nutzungen verbrauchen. Gibt true zurueck wenn Schluessel danach leer ist. */
    public static boolean consumeUse(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("uses_left")) return false;
        int left = tag.getInt("uses_left") - 1;
        tag.putInt("uses_left", left);
        return left <= 0;
    }

    /** Erstellt einen Schluessel-Stack aus einem Rohling fuer ein bestimmtes Schloss. */
    public static ItemStack createKey(ItemStack blankStack, LockData lockData, LockType.KeyOrigin origin) {
        ItemStack key = blankStack.copy();
        CompoundTag tag = key.getOrCreateTag();
        tag.putString("lock_id", lockData.getLockId());
        tag.putString("lock_type", lockData.getType().name());
        tag.putInt("door_x", lockData.getDoorX());
        tag.putInt("door_y", lockData.getDoorY());
        tag.putInt("door_z", lockData.getDoorZ());
        tag.putString("origin", origin.name());
        tag.putLong("created", System.currentTimeMillis());

        long duration = lockData.getType().getKeyDuration(origin);
        tag.putLong("expire_time", System.currentTimeMillis() + duration);
        tag.putInt("uses_left", lockData.getType().getKeyUses(origin));

        return key;
    }

    /** Erstellt eine Kopie eines vorhandenen Schluessels (neuer Rohling wird Key). */
    public static ItemStack copyKey(ItemStack originalKey, ItemStack blankStack) {
        CompoundTag origTag = originalKey.getTag();
        if (origTag == null) return ItemStack.EMPTY;

        ItemStack copy = blankStack.copy();
        CompoundTag tag = copy.getOrCreateTag();
        tag.putString("lock_id", origTag.getString("lock_id"));
        tag.putString("lock_type", origTag.getString("lock_type"));
        tag.putInt("door_x", origTag.getInt("door_x"));
        tag.putInt("door_y", origTag.getInt("door_y"));
        tag.putInt("door_z", origTag.getInt("door_z"));
        tag.putString("origin", LockType.KeyOrigin.COPY.name());
        tag.putLong("created", System.currentTimeMillis());

        try {
            LockType lt = LockType.valueOf(origTag.getString("lock_type"));
            long duration = lt.getKeyDuration(LockType.KeyOrigin.COPY);
            tag.putLong("expire_time", System.currentTimeMillis() + duration);
            tag.putInt("uses_left", lt.getKeyUses(LockType.KeyOrigin.COPY));
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
        return copy;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();

        if (level.isClientSide()) return InteractionResult.PASS;
        if (!(level.getBlockState(pos).getBlock() instanceof DoorBlock)) return InteractionResult.PASS;
        if (!(ctx.getPlayer() instanceof ServerPlayer player)) return InteractionResult.PASS;

        // Immer untere Haelfte
        if (level.getBlockState(pos).getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
            pos = pos.below();
        }

        LockManager mgr = LockManager.getInstance();
        if (mgr == null) return InteractionResult.FAIL;

        String dim = level.dimension().location().toString();
        String posKey = LockManager.posKey(dim, pos.getX(), pos.getY(), pos.getZ());
        LockData lockData = mgr.getLock(posKey);

        if (lockData == null) {
            // Tuer nicht gesperrt
            return InteractionResult.PASS;
        }

        ItemStack stack = ctx.getItemInHand();

        // Rohling → Schluessel erstellen
        if (isBlank(stack)) {
            return handleBlankOnDoor(player, stack, lockData);
        }

        // Schluessel → Tuer oeffnen
        return handleKeyOnDoor(player, stack, lockData, level, pos);
    }

    private InteractionResult handleBlankOnDoor(ServerPlayer player, ItemStack blank, LockData lockData) {
        // Nur Besitzer oder autorisierte Spieler duerfen Schluessel erstellen
        if (!lockData.isAuthorized(player.getUUID())) {
            player.sendSystemMessage(Component.literal(
                    "\u00A7cDu bist nicht berechtigt, einen Schluessel zu erstellen!"));
            return InteractionResult.FAIL;
        }

        // Tier pruefen
        int requiredTier = lockData.getType().getRequiredBlankTier();
        if (requiredTier < 0) {
            player.sendSystemMessage(Component.literal(
                    "\u00A7cDieses Schloss benoetigt keinen Schluessel (Zahlenschloss)."));
            return InteractionResult.FAIL;
        }
        if (this.blankTier < requiredTier) {
            String[] tierNames = {"Kupfer", "Eisen", "Netherite"};
            player.sendSystemMessage(Component.literal(
                    "\u00A7cDu brauchst mindestens einen \u00A7e" + tierNames[requiredTier] +
                            "-Rohling\u00A7c fuer dieses Schloss!"));
            return InteractionResult.FAIL;
        }

        // Schluessel erstellen
        ItemStack key = createKey(blank, lockData, LockType.KeyOrigin.ORIGINAL);
        player.getInventory().setItem(player.getInventory().selected, key);

        player.sendSystemMessage(Component.literal(
                "\u00A7a\u2714 Schluessel erstellt fuer Lock \u00A7e" + lockData.getLockId()));

        long dur = lockData.getType().getKeyDuration(LockType.KeyOrigin.ORIGINAL);
        String durStr = dur >= 86400000 ? (dur / 86400000) + " Tage" : (dur / 3600000) + "h";
        int uses = lockData.getType().getKeyUses(LockType.KeyOrigin.ORIGINAL);
        player.sendSystemMessage(Component.literal(
                "\u00A77Haltbarkeit: " + durStr + " | Nutzungen: " + uses));

        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleKeyOnDoor(ServerPlayer player, ItemStack key,
                                               LockData lockData, Level level, BlockPos pos) {
        CompoundTag tag = key.getTag();
        if (tag == null) return InteractionResult.FAIL;

        // Abgelaufen?
        if (isExpired(key)) {
            player.sendSystemMessage(Component.literal(
                    "\u00A7cDieser Schluessel ist abgelaufen!"));
            player.getInventory().setItem(player.getInventory().selected, ItemStack.EMPTY);
            return InteractionResult.FAIL;
        }

        // Lock-ID pruefen
        String keyLockId = tag.getString("lock_id");
        if (!keyLockId.equals(lockData.getLockId())) {
            player.sendSystemMessage(Component.literal(
                    "\u00A7cDieser Schluessel passt nicht zu diesem Schloss!"));
            return InteractionResult.FAIL;
        }

        // Bei Dual-Lock: Schluessel akzeptiert, jetzt Code-GUI oeffnen
        if (lockData.getType() == LockType.DUAL) {
            player.sendSystemMessage(Component.literal(
                    "\u00A7a\u2714 Schluessel akzeptiert! Gib jetzt den Code ein."));
            // Code-Eingabe GUI oeffnen
            String dim = level.dimension().location().toString();
            LockNetworkHandler.sendToPlayer(
                    new OpenCodeEntryPacket(lockData.getLockId(), pos, dim), player);
            // Nutzung wird bei Code-Eingabe im CodeEntryPacket abgezogen
            return InteractionResult.SUCCESS;
        }

        // Nutzung abziehen
        boolean depleted = consumeUse(key);
        if (depleted) {
            player.getInventory().setItem(player.getInventory().selected, ItemStack.EMPTY);
            player.sendSystemMessage(Component.literal("\u00A77Schluessel aufgebraucht."));
        }

        // Tuer oeffnen (nur oeffnen, nicht toggle - schliesst automatisch)
        net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);
        if (!state.getValue(DoorBlock.OPEN)) {
            DoorBlock door = (DoorBlock) state.getBlock();
            door.setOpen(null, level, state, pos, true);
            player.sendSystemMessage(Component.literal("\u00A7a\u2714 Tuer entriegelt!"));

            // Automatisch schliessen nach 3 Sekunden
            if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                scheduleAutoClose(serverLevel, pos);
            }
        }

        return InteractionResult.SUCCESS;
    }

    /** Ticks bis die Tuer sich automatisch schliesst (3 Sekunden = 60 Ticks). */
    private static final int AUTO_CLOSE_TICKS = 60;

    /**
     * Plant das automatische Schliessen der Tuer nach AUTO_CLOSE_TICKS.
     */
    private void scheduleAutoClose(net.minecraft.server.level.ServerLevel level, BlockPos pos) {
        level.getServer().tell(new net.minecraft.server.TickTask(
                level.getServer().getTickCount() + AUTO_CLOSE_TICKS,
                () -> {
                    net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);
                    if (state.getBlock() instanceof DoorBlock && state.getValue(DoorBlock.OPEN)) {
                        level.setBlock(pos, state.setValue(DoorBlock.OPEN, false), 10);
                        // Schliess-Sound
                        level.levelEvent(null, 1006, pos, 0);
                    }
                }
        ));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tips, TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("lock_id")) {
            // Rohling
            tips.add(Component.literal("\u00A7e" + tierName).withStyle(ChatFormatting.GOLD));
            tips.add(Component.literal("\u00A77Rechtsklick auf gesperrte Tuer"));
            String[] lockNames = {"Einfach", "Sicher", "Hochsicher/Dual"};
            tips.add(Component.literal("\u00A78Fuer: " + lockNames[Math.min(blankTier, 2)] + "+"));
            return;
        }

        // Schluessel
        tips.add(Component.literal("\u00A76\u2714 Schluessel").withStyle(ChatFormatting.GOLD));
        tips.add(Component.literal("\u00A78Lock-ID: " + tag.getString("lock_id")));
        tips.add(Component.literal("\u00A78Typ: " + tag.getString("lock_type")));

        // Position
        tips.add(Component.literal("\u00A78Tuer: " + tag.getInt("door_x") + ", " +
                tag.getInt("door_y") + ", " + tag.getInt("door_z")));

        // Herkunft
        String origin = tag.getString("origin");
        ChatFormatting oc = origin.equals("STOLEN") ? ChatFormatting.RED :
                origin.equals("COPY") ? ChatFormatting.YELLOW : ChatFormatting.GREEN;
        try {
            tips.add(Component.literal("Herkunft: " + LockType.KeyOrigin.valueOf(origin).getDisplayName()).withStyle(oc));
        } catch (Exception e) {
            tips.add(Component.literal("Herkunft: Unbekannt").withStyle(ChatFormatting.GRAY));
        }

        // Verbleibende Nutzungen
        if (tag.contains("uses_left")) {
            int left = tag.getInt("uses_left");
            ChatFormatting uc = left > 5 ? ChatFormatting.GREEN : left > 1 ? ChatFormatting.YELLOW : ChatFormatting.RED;
            tips.add(Component.literal("Nutzungen: " + left).withStyle(uc));
        }

        // Ablaufzeit
        if (tag.contains("expire_time")) {
            long remaining = tag.getLong("expire_time") - System.currentTimeMillis();
            if (remaining <= 0) {
                tips.add(Component.literal("\u00A7c\u2716 ABGELAUFEN"));
            } else {
                String time;
                if (remaining > 86400000) time = (remaining / 86400000) + "d " + ((remaining % 86400000) / 3600000) + "h";
                else if (remaining > 3600000) time = (remaining / 3600000) + "h " + ((remaining % 3600000) / 60000) + "m";
                else time = (remaining / 60000) + "m";
                tips.add(Component.literal("\u00A77\u23F1 " + time + " verbleibend"));
            }
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return !isBlank(stack); // Schluessel glitzern
    }
}
