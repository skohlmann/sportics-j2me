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
package net.sportics.dni.rt.client.microedition;

import java.io.OutputStream;


/**
 * Contains a key/value pair. The key describes the value. Use {@link #serialize(OutputStream)}
 * if storing the value. There are additional convenience to support less code writing.
 *
 * <p><strong>NOTE: The descriptor will based on {@link TypedDataDescriptor}s in future
 * implementations.</strong></p>
 *
 * <p>The implementation is not imutable.</p>
 * @author Sascha Kohlmann
 */
public class TypedData {

    /** An empty data set. */
    public static final TypedData EMPTY = new TypedData("", new Variant(""));

    private final String descriptor;
    private final Variant value;
    
    /**
     * Constructs a new instance with the description and the value.
     * @param descriptor the descriptor of the value.
     * @param value the value
     * @throws IllegalArgumentException if and only if the values of the supplied parameter
     *                                  are {@code null}
     */
    public TypedData(final String descriptor, final Variant value) {
        if (descriptor == null || value == null) {
            throw new IllegalArgumentException();
        }
        this.value = value;
        this.descriptor = descriptor;
    }

    /**
     * Convenience constructor. Stores the value in a {@link variant}.
     * @param descriptor the descriptor of the value.
     * @param value the value
     * @throws IllegalArgumentException if and only if the values of {@code descriptor} is
     *                                  {@code null}
     */
    public TypedData(final String descriptor, final int value) {
        this(descriptor, new Variant(value));
    }

    /**
     * Convenience constructor. Stores the value in a {@link variant}.
     * @param descriptor the descriptor of the value.
     * @param value the value
     * @throws IllegalArgumentException if and only if the values of {@code descriptor} is
     *                                  {@code null}
     */
    public TypedData(final String descriptor, final float value) {
        this(descriptor, new Variant(value));
    }

    /**
     * Convenience constructor. Sores the value in a {@link variant}.
     * @param descriptor the descriptor of the value.
     * @param value the value
     * @throws IllegalArgumentException if and only if the values of {@code descriptor} is
     *                                  {@code null}
     */
    public TypedData(final String descriptor, final double value) {
        this(descriptor, new Variant(value));
    }

    /**
     * Convenience constructor. Stores the value in a {@link variant}.
     * @param descriptor the descriptor of the value.
     * @param value the value
     * @throws IllegalArgumentException if and only if the values of {@code descriptor} is
     *                                  {@code null}
     */
    public TypedData(final String descriptor, final long value) {
        this(descriptor, new Variant(value));
    }

    /**
     * Convenience constructor. Stores the value in a {@link variant}.
     * @param descriptor the descriptor of the value.
     * @param value the value
     * @throws IllegalArgumentException if and only if the values of {@code descriptor} is
     *                                  {@code null}
     */
    public TypedData(final String descriptor, final boolean value) {
        this(descriptor, new Variant(value));
    }

    /**
     * Convenience constructor. Stores the value in a {@link variant}.
     * @param descriptor the descriptor of the value.
     * @param value the value
     * @throws IllegalArgumentException if and only if the values of the supplied parameter
     *                                  are {@code null}
     */
    public TypedData(final String descriptor, final String value) {
        this(descriptor, new Variant(value));
    }

    /**
     * Returns the descriptor of the {@code TypedData}.
     * @return the descriptor
     */
    public String getDescriptor() {
        return this.descriptor;
    }

    /**
     * Returns the value of the {@code TypedData}.
     * @return the value
     */
    public Variant getValue() {
        return this.value;
    }

    /**
     * The value of the implementation. Do not use the value to access data.
     * The format of the returned value may change in later implementations.
     */
    public String toString() {
        final StringBuffer sb = new StringBuffer("TypedData@");
        sb.append(Integer.toHexString(System.identityHashCode(this)));

        sb.append("[[desc:");
        sb.append(getDescriptor());
        sb.append("][value:");
        sb.append(getValue());
        sb.append("]]");

        return sb.toString();
    }
}
