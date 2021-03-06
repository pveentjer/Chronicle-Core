/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.openhft.chronicle.core;

import java.lang.reflect.Field;
import java.util.concurrent.locks.LockSupport;

import static java.lang.management.ManagementFactory.getRuntimeMXBean;

/**
 * Utility class to access information in the JVM.
 */
public enum Jvm {
    ;

    public static final boolean IS_DEBUG = getRuntimeMXBean().getInputArguments().toString().contains("jdwp") || Boolean.getBoolean("debug");

    /**
     * Cast a CheckedException as an unchecked one.
     *
     * @param throwable to cast
     * @throws T the throwable as an unchecked thorwable
     */
    @SuppressWarnings("unchecked")
    public static <T extends Throwable> RuntimeException rethrow(Throwable throwable) throws T {
        throw (T) throwable; // rely on vacuous cast
    }

    /**
     * Append the StackTraceElements to the StringBuilder trimming some internal methods.
     *
     * @param sb to append to
     * @param stes stack trace elements
     */
    public static void trimStackTrace(StringBuilder sb, StackTraceElement... stes) {
        int first = trimFirst(stes);
        int last = trimLast(first, stes);
        for (int i = first; i <= last; i++)
            sb.append("\n\tat ").append(stes[i]);
    }

    static int trimFirst(StackTraceElement[] stes) {
        int first = 0;
        for (; first < stes.length; first++)
            if (!isInternal(stes[first].getClassName()))
                break;
        return Math.max(0, first - 2);
    }

    public static int trimLast(int first, StackTraceElement[] stes) {
        int last = stes.length - 1;
        for (; first < last; last--)
            if (!isInternal(stes[last].getClassName()))
                break;
        if (last < stes.length - 1) last++;
        return last;
    }

    static boolean isInternal(String className) {
        return className.startsWith("jdk.") || className.startsWith("sun.") || className.startsWith("java.");
    }

    /**
     * @return is the JVM in debug mode.
     */
    @SuppressWarnings("SameReturnValue")
    public static boolean isDebug() {
        return IS_DEBUG;
    }

    /**
     * Silently pause for milli seconds.
     * @param millis to sleep for.
     */
    public static void pause(long millis) {
        long timeNanos = millis * 1000000;
        if (timeNanos > 1e9) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                throw new AssertionError(e);
            }
        } else {
            LockSupport.parkNanos(timeNanos);
        }
    }

    /**
     * Get the Field for a class by name.
     * @param clazz to get the field for
     * @param name of the field
     * @return the Field.
     */
    public static Field getField(Class clazz, String name) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            throw new AssertionError(e);
        }
    }
}
