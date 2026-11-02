/*
* AMRIT - Accessible Medical Records via Integrated Technologies
* Integrated EHR (Electronic Health Records) Solution
*
* Copyright (C) "Piramal Swasthya Management and Research Institute"
*
* This file is part of AMRIT.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see https://www.gnu.org/licenses/.
*/
package com.iemr.tm.data;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Reflective checks for the Lombok-generated accessors, equality and string
 * representation of the plain data carriers in this service.
 *
 * <p>The data classes hold a non-comparable {@code outputMapper} helper, so two
 * independently constructed instances are never equal. The verifier therefore
 * copies every field - the helper included - onto the second instance before it
 * starts comparing, and only then varies one property at a time.
 */
final class BeanContract {

    private BeanContract() {
    }

    static void verify(Class<?> type) throws Exception {
        Object left = type.getDeclaredConstructor().newInstance();
        Object right = type.getDeclaredConstructor().newInstance();
        copyFields(type, left, right);

        assertEquals(left, right, type.getSimpleName() + " with identical fields must compare equal");
        assertEquals(left.hashCode(), right.hashCode(),
                type.getSimpleName() + " must hash consistently with equals");
        assertTrue(left.equals(left), "a bean must equal itself");
        assertFalse(left.equals(null), "a bean must never equal null");
        assertFalse(left.equals("not a bean"), "a bean must never equal an unrelated type");
        assertNotNull(left.toString(), type.getSimpleName() + " must render a string form");

        verifyProperties(type, left, right);
    }

    private static void verifyProperties(Class<?> type, Object left, Object right) throws Exception {
        for (Field field : type.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            Method getter = findGetter(type, field);
            Method setter = findSetter(type, field);
            if (getter == null || setter == null) {
                continue;
            }
            Object value = sampleValue(field.getType());
            if (value == null) {
                continue;
            }

            setter.invoke(left, value);
            assertEquals(value, getter.invoke(left),
                    type.getSimpleName() + "." + field.getName() + " must round-trip through its accessors");
            assertNotEquals(left, right,
                    type.getSimpleName() + " must compare unequal while " + field.getName() + " differs");

            setter.invoke(right, value);
            assertEquals(left, right,
                    type.getSimpleName() + " must compare equal once " + field.getName() + " matches again");
        }
        assertNotNull(left.toString(), type.getSimpleName() + " must render a populated string form");
    }

    private static void copyFields(Class<?> type, Object from, Object to) throws Exception {
        for (Field field : type.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            field.setAccessible(true);
            field.set(to, field.get(from));
        }
    }

    private static Method findGetter(Class<?> type, Field field) {
        String suffix = capitalise(field.getName());
        for (String prefix : new String[] { "get", "is" }) {
            try {
                return type.getMethod(prefix + suffix);
            } catch (NoSuchMethodException ignored) {
                // try the next accessor style
            }
        }
        return null;
    }

    private static Method findSetter(Class<?> type, Field field) {
        try {
            return type.getMethod("set" + capitalise(field.getName()), field.getType());
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static String capitalise(String name) {
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private static Object sampleValue(Class<?> type) {
        if (type == String.class) {
            return "sample";
        }
        if (type == Long.class || type == long.class) {
            return 7L;
        }
        if (type == Integer.class || type == int.class) {
            return 7;
        }
        if (type == Boolean.class || type == boolean.class) {
            return Boolean.TRUE;
        }
        if (type == Timestamp.class) {
            return Timestamp.valueOf("2026-02-17 09:30:00");
        }
        if (type == Date.class) {
            return new Date(1_771_286_400_000L);
        }
        if (type == LocalTime.class) {
            return LocalTime.of(9, 30);
        }
        if (type == List.class) {
            return new ArrayList<>(List.of(1, 2));
        }
        if (type.isPrimitive() || type.isEnum() || type.isArray()) {
            return null;
        }
        try {
            // Any remaining carrier - the JSON mapper helper, a nested entity - is
            // built through its no-argument constructor so equality still varies on it.
            return type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }
}
