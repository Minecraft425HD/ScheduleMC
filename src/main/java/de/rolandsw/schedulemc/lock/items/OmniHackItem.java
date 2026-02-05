package de.rolandsw.schedulemc.lock.items;

import de.rolandsw.schedulemc.lock.LockType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import java.util.List;

/**
 * Omni-Hack: Universelles Hacking-Tool fuer alle Code-Schloesser.
 *
 * Haltbarkeit: 3 Versuche (selten!).
 * Erfolgschance: 50%.
 * Alarm nur bei Dual-Lock (weil triggersAlarm=true auf dem Lock-Typ).
 * Enchantment-Glow (Foil).
 */
public class OmniHackItem extends HackingToolItem {

    public OmniHackItem() {
        super(3, Rarity.RARE);
    }

    @Override
    protected boolean canHackLockType(LockType type) {
        return type == LockType.COMBINATION || type == LockType.DUAL;
    }

    @Override
    protected String getToolName() {
        return "Omni-Hack";
    }

    @Override
    protected String getToolColor() {
        return "\u00A7b";
    }

    @Override
    protected void appendSpecificTooltip(List<Component> tips) {
        tips.add(Component.literal("\u00A78Knackt:"));
        tips.add(Component.literal("\u00A76  Zahlenschloss: \u00A7a\u2714"));
        tips.add(Component.literal("\u00A75  Dual-Lock: \u00A7a\u2714 \u00A7c(Alarm bei Fehlschlag)"));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
