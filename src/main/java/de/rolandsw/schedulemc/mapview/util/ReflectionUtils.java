package de.rolandsw.schedulemc.mapview.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PERFORMANCE: Reflection-Ergebnisse werden gecacht.
 * - getDeclaredFields() Ergebnisse pro Class gecacht
 * - classExists() Ergebnisse gecacht
 * Vermeidet wiederholte teure Reflection-Lookups bei häufigen Aufrufen.
 */
public final class ReflectionUtils {
    private ReflectionUtils() {}

    // PERFORMANCE: Cache für getDeclaredFields() Ergebnisse
    private static final Map<Class<?>, Field[]> fieldCache = new ConcurrentHashMap<>();
    // PERFORMANCE: Cache für classExists() Ergebnisse
    private static final Map<String, Boolean> classExistsCache = new ConcurrentHashMap<>();

    private static Field[] getCachedFields(Class<?> clazz) {
        return fieldCache.computeIfAbsent(clazz, Class::getDeclaredFields);
    }

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
        Field[] fields = getCachedFields(objectClass);

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
            Field[] fields = getCachedFields(objectClass);

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
        return classExistsCache.computeIfAbsent(className, name -> {
            try {
                Class.forName(name);
                return true;
            } catch (ClassNotFoundException e) {
                return false;
            }
        });
    }
}
