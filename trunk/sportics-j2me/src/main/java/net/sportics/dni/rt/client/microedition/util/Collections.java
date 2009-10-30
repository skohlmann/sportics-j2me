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

import net.sportics.dni.rt.client.microedition.Comparable;

public final class Collections {

    /**
     * Tuning parameter: list size at or below which insertion sort will be
     * used in preference to mergesort or quicksort.
     */
    private static final int INSERTIONSORT_THRESHOLD = 7;

    public static void sort(final String[] s) {
        final StringComparable[] src = stringToComparable(s);
        final StringComparable[] dest = new StringComparable[s.length];
        mergeSort(src, dest, 0, s.length, 0);
        for (int i = 0; i < s.length; i++) {
            s[i] = dest[i].getString();
        }
    }

    public static void sort(final Comparable[] c) {
        final Comparable[] dest = new Comparable[c.length];
        mergeSort(c, dest, 0, c.length, 0);
    }

    static StringComparable[] stringToComparable(final String[] s) {
        final StringComparable[] newSrc = new StringComparable[s.length];
        for (int i = 0; i < s.length; i++) {
            newSrc[i] = new StringComparable(s[i]);
        }
        return newSrc;
    }

    /**
     * @param src is the source array that starts at index 0
     * @param dest is the (possibly larger) array destination with a possible offset
     * @param low is the index in dest to start sorting
     * @param high is the end index in dest to end sorting
     * @param off is the offset to generate corresponding low, high in src
     */
    static void mergeSort(final Comparable[] src,
                          final Comparable[] dest,
                          int low,
                          int high,
                          final int off) {
        int length = high - low;

        // Insertion sort on smallest arrays
        if (length < INSERTIONSORT_THRESHOLD) {
            for (int i = low; i < high; i++) {
                for (int j = i; j > low && dest[j-1].compareTo(dest[j]) > 0; j--) {
                    swap(dest, j, j-1);
                }
            }
            return;
        }

        // Recursively sort halves of dest into src
        final int destLow  = low;
        final int destHigh = high;
        low  += off;
        high += off;
        final int mid = (low + high) >>> 1;
        mergeSort(dest, src, low, mid, -off);
        mergeSort(dest, src, mid, high, -off);

        // If list is already sorted, just copy from src to dest.  This is an
        // optimization that results in faster sorts for nearly ordered lists.
        if (src[mid-1].compareTo(src[mid]) <= 0) {
            System.arraycopy(src, low, dest, destLow, length);
            return;
        }

        // Merge sorted halves (now in src) into dest
        for(int i = destLow, p = low, q = mid; i < destHigh; i++) {
            if (q >= high || p < mid && src[p].compareTo(src[q]) <= 0) {
                dest[i] = src[p++];
            } else {
                dest[i] = src[q++];
            }
        }
    }

    /**
     * Swaps x[a] with x[b].
     */
    private static void swap(final Object x[], final int a, final int b) {
        Object t = x[a];
        x[a] = x[b];
        x[b] = t;
    }

    private static final class StringComparable implements Comparable {
        private final String s;
        public StringComparable(final String toCompare) {
            if (toCompare == null) {
                throw new IllegalArgumentException("toCompare is null");
            }
            this.s = toCompare;
        }

        public int compareTo(final Comparable o) {
            if (o instanceof StringComparable) {
                final StringComparable sc = (StringComparable) o;
                return s.compareTo(sc.s);
            } else {
                return -1;
            }
        }

        public String getString() {
            return this.s;
        }
    }
}
