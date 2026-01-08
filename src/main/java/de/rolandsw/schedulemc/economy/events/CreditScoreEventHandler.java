package de.rolandsw.schedulemc.economy.events;

import de.rolandsw.schedulemc.economy.CreditScoreManager;
import de.rolandsw.schedulemc.events.ModEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

/**
 * Event Handler für CreditScore-Updates
 *
 * OPTIMIERUNG: Entkoppelt CreditScoreManager von direkten Manager-Aufrufen.
 * Stattdessen reagiert er auf Events und aktualisiert den Score.
 *
 * Vorteile:
 * - Lose Kopplung: Manager müssen sich nicht gegenseitig kennen
 * - Erweiterbar: Neue Listener können ohne Codeänderung hinzugefügt werden
 * - Testbar: Manager können isoliert getestet werden
 */
@Mod.EventBusSubscriber(modid = "schedulemc")
public class CreditScoreEventHandler {

    /**
     * Aktualisiert CreditScore basierend auf Kredit-Rückzahlungen
     */
    @SubscribeEvent
    public static void onBalanceChanged(ModEvents.BalanceChangedEvent event) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        CreditScoreManager scoreManager = CreditScoreManager.getInstance(server);

        // Kredit-Rückzahlungen sind positiv für den Score
        if (event.getReason() == ModEvents.BalanceChangedEvent.ChangeReason.LOAN_REPAYMENT) {
            scoreManager.recordOnTimePayment(event.getPlayerUUID());
        }
    }

    /**
     * Kredit vollständig zurückgezahlt
     */
    @SubscribeEvent
    public static void onLoanRepaid(ModEvents.LoanRepaidEvent event) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        CreditScoreManager scoreManager = CreditScoreManager.getInstance(server);

        if (event.isFullyRepaid()) {
            // Vollständige Rückzahlung ist sehr positiv für den Score
            scoreManager.recordLoanCompleted(event.getPlayerUUID(), event.getAmount());
        }
    }
}
