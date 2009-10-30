/* Copyright (C) 2008-2009 Sascha Kohlmann
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sportics.dni.rt.client.microedition.util;

/**
 * Provides static methods to simpler condition handling. This implies less
 * code.
 * @author Sascha Kohlmann
 */
public class Conditions {

    private Conditions() {
    }

    /**
     * Checks the expression of an argument and throws an
     * {@code IllegalArgumentException} if the expression is {@code false}.
     *
     * @param expr the expression to test
     * @throws IllegalArgumentException if {@code expr} is {@code false}
     */
    public static void checkArgument(final boolean expr) {
        if (!expr) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Checks the expression of an argument and throws an
     * {@code IllegalArgumentException} if the expression is {@code false}.
     *
     * @param expr the expression to test
     * @param errorMessage the error message for output from {@link #toString()}
     * @throws IllegalArgumentException if {@code expr} is {@code false}
     */
    public static void checkArgument(final boolean expr, final Object errorMessage) {
        if (!expr) {
            throw new IllegalArgumentException("" + errorMessage);
        }
    }

    /**
     * Checks the expression of an argument and throws an
     * {@code IllegalStateException} if the expression is {@code false}.
     *
     * @param expr the expression to test
     * @throws IllegalStateException if {@code expr} is {@code false}
     */
    public static void checkState(final boolean expr) {
        if (!expr) {
            throw new IllegalStateException();
        }
    }

    /**
     * Checks the expression of an argument and throws an
     * {@code IllegalStateException} if the expression is {@code false}.
     *
     * @param errorMessage the error message for output from {@link #toString()}
     * @param expr the expression to test
     * @throws IllegalStateException if {@code expr} is {@code false}
     */
    public static void checkState(final boolean expr, final Object errorMessage) {
        if (!expr) {
            throw new IllegalStateException("" + errorMessage);
        }
    }
}
