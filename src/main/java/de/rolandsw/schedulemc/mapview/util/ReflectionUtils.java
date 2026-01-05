package de.rolandsw.schedulemc.mapview.util;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Set;

public final class ReflectionUtils {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * SICHERHEIT: Whitelist erlaubter Package-Präfixe für Reflection
     * Nur Klassen aus diesen Packages dürfen per Reflection geladen werden
     */
    private static final Set<String> ALLOWED_PACKAGE_PREFIXES = Set.of(
        "de.rolandsw.schedulemc.mapview.",
        "net.minecraft.client.renderer.",
        "net.minecraft.client.gui.",
        "com.mojang.blaze3d.",
        "java.awt.",
        "java.util.",
        "javax.imageio."
    );

    private ReflectionUtils() {}

    public static Object getPrivateFieldValueByType(Object o, Class<?> objectClasstype, Class<?> fieldClasstype) {
        return getPrivateFieldValueByType(o, objectClasstype, fieldClasstype, 0);
    }

    public static Object getPrivateFieldValueByType(Object o, Class<?> objectClasstype, Class<?> fieldClasstype, int index) {
        Class<?> objectClass;
        if (o != null) {
            objectClass = o.getClass();
        } else {
            objectClass = objectClasstype;
        }

        while (!objectClass.equals(objectClasstype) && objectClass.getSuperclass() != null) {
            objectClass = objectClass.getSuperclass();
        }

        int counter = 0;
        Field[] fields = objectClass.getDeclaredFields();

        for (Field field : fields) {
            if (fieldClasstype.equals(field.getType())) {
                if (counter == index) {
                    try {
                        field.setAccessible(true);
                        return field.get(o);
                    } catch (IllegalAccessException ignored) {}
                }

                ++counter;
            }
        }

        return null;
    }

    public static ArrayList<Field> getFieldsByType(Object o, Class<?> objectClassBaseType, Class<?> fieldClasstype) {
        ArrayList<Field> matches = new ArrayList<>();

        for (Class<?> objectClass = o.getClass(); !objectClass.equals(objectClassBaseType) && objectClass.getSuperclass() != null; objectClass = objectClass.getSuperclass()) {
            Field[] fields = objectClass.getDeclaredFields();

            for (Field field : fields) {
                if (fieldClasstype.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    matches.add(field);
                }
            }
        }

        return matches;
    }

    public static boolean classExists(String className) {
        // SICHERHEIT: Validiere Klassenname gegen Whitelist
        if (!isClassAllowed(className)) {
            LOGGER.warn("Reflection blocked for non-whitelisted class: {}", className);
            return false;
        }

        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException var2) {
            return false;
        }
    }

    /**
     * SICHERHEIT: Prüft ob eine Klasse zur Whitelist gehört
     */
    private static boolean isClassAllowed(String className) {
        if (className == null || className.isEmpty()) {
            return false;
        }

        // Verhindere gefährliche Klassen
        if (className.contains("Runtime") ||
            className.contains("ProcessBuilder") ||
            className.contains("ClassLoader") ||
            className.contains("Thread") ||
            className.contains("System") ||
            className.contains("SecurityManager")) {
            return false;
        }

        // Prüfe gegen Whitelist
        for (String allowedPrefix : ALLOWED_PACKAGE_PREFIXES) {
            if (className.startsWith(allowedPrefix)) {
                return true;
            }
        }

        return false;
    }
}
