package de.rolandsw.schedulemc.secretdoors.mission;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * Generator für lock_id + Zugangscode bei SecretDoor/Hatch/HiddenSwitch.
 */
public final class SecretCodeGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private SecretCodeGenerator() {
    }

    public static String newLockId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    public static String newAccessCode() {
        return String.format("%04d", RANDOM.nextInt(10_000));
    }
}
