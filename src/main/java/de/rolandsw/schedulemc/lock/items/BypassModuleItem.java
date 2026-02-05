package de.rolandsw.schedulemc.lock.items;

import de.rolandsw.schedulemc.lock.LockType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Rarity;

import java.util.List;

/**
 * Bypass-Modul: Knackt nur Dual-Locks.
 *
 * Haltbarkeit: 5 Versuche.
 * Erfolgschance: 50%.
 * Alarm + Fahndungslevel bei Fehlschlag (Dual-Lock hat triggersAlarm=true).
 */
public class BypassModuleItem extends HackingToolItem {

    public BypassModuleItem() {
        super(5, Rarity.UNCOMMON);
    }

    @Override
    protected boolean canHackLockType(LockType type) {
        return type == LockType.DUAL;
    }

    @Override
    protected String getToolName() {
        return "Bypass-Modul";
    }

    @Override
    protected String getToolColor() {
        return "\u00A7e";
    }

    @Override
    protected void appendSpecificTooltip(List<Component> tips) {
        tips.add(Component.literal("\u00A78Knackt:"));
        tips.add(Component.literal("\u00A76  Zahlenschloss: \u00A7c\u2716"));
        tips.add(Component.literal("\u00A75  Dual-Lock: \u00A7a\u2714 \u00A7c(Alarm bei Fehlschlag)"));
    }
}
