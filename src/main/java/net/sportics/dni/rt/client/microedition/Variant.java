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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A variant defines a convenience data construct for systems in which the type
 * of the data is not secure for future developments. The Sportics variant type
 * can contain 5 different datatypes. The may be interchangeable but needn't. E.g.
 * a {@code int} can represented as {@code String}, {@code long}, {@code float}
 * or {@code boolean}. But a {@code String} may represent a {@code boolean} or
 * {@code double}.
 *
 * <p>The implementation contains methods to access the data in different forms.
 * But the may cause errors, if it is not possible to interchange the data from
 * one type to an other.</p>
 *
 * <p>The implementation is immutable.
 *
 * @author Sascha Kohlmann
 */
public final class Variant {

    private static final String STRING = "s";
    private static final String INTEGER = "i";
    private static final String FLOAT = "f";
    private static final String DOUBLE = "d";
    private static final String LONG = "l";
    private static final String BOOLEAN = "b";

    /** The variant value. */
    private final Object v;

    /** Constructs a new instance with the given value.
     * @param s the value
     * @throws IllegalArgumentException if and only if the supplied value is {@code null}
     */
    public Variant(final String s) {
        if (s == null) {
            throw new IllegalArgumentException();
        }
        this.v = s;
    }

    /** Constructs a new instance with the given value.
     * @param s the value */
    public Variant(final int i) {
        this.v = new Integer(i);
    }

    /** Constructs a new instance with the given value.
     * @param s the value */
    public Variant(final boolean b) {
        this.v = b ? Boolean.TRUE : Boolean.FALSE;
    }

    /** Constructs a new instance with the given value.
     * @param s the value */
    public Variant(final float f) {
        this.v = new Float(f);
    }

    /** Constructs a new instance with the given value.
     * @param s the value */
    public Variant(final double d) {
        this.v = new Double(d);
    }

    /** Constructs a new instance with the given value.
     * @param s the value */
    public Variant(final long l) {
        this.v = new Long(l);
    }

    /** @returns {@code true} if and only if the variant was constructed with a {@code String}. */
    public boolean isString() {
        return this.v instanceof String;
    }

    /** @returns {@code true} if and only if the variant was constructed with an {@code int}. */
    public boolean isInteger() {
        return this.v instanceof Integer;
    }

    /** @returns {@code true} if and only if the variant was constructed with a {@code float}. */
    public boolean isFloat() {
        return this.v instanceof Float;
    }

    /** @returns {@code true} if and only if the variant was constructed with a {@code double}. */
    public boolean isDouble() {
        return this.v instanceof Double;
    }

    /** @returns {@code true} if and only if the variant was constructed with a {@code long}. */
    public boolean isLong() {
        return this.v instanceof Long;
    }

    /** @returns {@code true} if and only if the variant was constructed with a {@code boolean}. */
    public boolean isBoolean() {
        return this.v instanceof Boolean;
    }

    /**
     * Returns the value of the variant as {@code String} representation.
     * @return the value as {@code String}
     */
    public String asString() {
        return this.v.toString();
    }

    /**
     * Returns the value of the variant as {@code int} representation. May loose information
     * if the variant based on a {@code long} or a {@code double}...
     * @return the value as {@code int}
     * @throws NumberFormatException if the variant based on a {@code String} an it is not
     *                               possible to create a {@code int} from the {@code String}
     */
    public int asInteger() {
        if (isString()) {
            return Integer.parseInt((String) this.v);
        } else if (isInteger()) {
            return ((Integer) this.v).intValue();
        } else if (isLong()) {
            return (int) ((Long) this.v).longValue();
        } else if (isDouble()) {
            return ((Double) this.v).intValue();
        } else if (isFloat()) {
            return ((Float) this.v).intValue();
        } else if (isBoolean()) {
            return Boolean.TRUE == this.v ? 1 : 0;
        }
        throw new IllegalStateException();
    }

    /**
     * Returns the value of the variant as {@code long} representation.
     * @return the value as {@code long}
     * @throws NumberFormatException if the variant based on a {@code String} an it is not
     *                               possible to create a {@code long} from the {@code String}
     */
    public long asLong() {
        if (isString()) {
            return Long.parseLong((String) this.v);
        } else if (isInteger()) {
            return ((Integer) this.v).longValue();
        } else if (isLong()) {
            return ((Long) this.v).longValue();
        } else if (isDouble()) {
            return ((Double) this.v).longValue();
        } else if (isFloat()) {
            return ((Float) this.v).longValue();
        } else if (isBoolean()) {
            return Boolean.TRUE == this.v ? 1l : 0l;
        }
        throw new IllegalStateException();
    }

    /**
     * Returns the value of the variant as {@code float} representation. May loose information
     * if the variant based on a {@code long} or a {@code double}...
     * @return the value as {@code float}
     * @throws NumberFormatException if the variant based on a {@code String} an it is not
     *                               possible to create a {@code float} from the {@code String}
     */
    public float asFloat() {
        if (isString()) {
            return Float.parseFloat((String) this.v);
        } else if (isInteger()) {
            return ((Integer) this.v).floatValue();
        } else if (isLong()) {
            return ((Long) this.v).floatValue();
        } else if (isDouble()) {
            return ((Double) this.v).floatValue();
        } else if (isFloat()) {
            return ((Float) this.v).floatValue();
        } else if (isBoolean()) {
            return Boolean.TRUE == this.v ? 1f : 0f;
        }
        throw new IllegalStateException();
    }

    /**
     * Returns the value of the variant as {@code boolean} representation. Number representations
     * like {@code int} or {@code double} are {@code false} if the value is zero. Otherwise
     * the value is {@code true}. A {@code String} must have the value "{@code true}", may in
     * mixed cases, to be {@code true}. Otherwise its interpreted as {@code false}.
     * @return the value as {@code boolean}
     */
    public boolean asBoolean() {
        if (isString()) {
            return ((String) this.v).equalsIgnoreCase(Boolean.TRUE.toString()) ? true : false;
        } else if (isInteger()) {
            return ((Integer) this.v).intValue() == 0 ? false : true;
        } else if (isLong()) {
            return ((Long) this.v).longValue() == 0 ? false : true;
        } else if (isDouble()) {
            return ((Double) this.v).doubleValue() == 0.0d ? false : true;
        } else if (isFloat()) {
            return ((Float) this.v).floatValue() == 0.0d ? false : true;
        } else if (isBoolean()) {
            return ((Boolean) this.v).booleanValue();
        }
        throw new IllegalStateException();
    }

    /**
     * Returns the value of the variant as {@code double} representation. May loose information
     * if the variant based on a {@code long}.
     * @return the value as {@code double}
     * @throws NumberFormatException if the variant based on a {@code String} an it is not
     *                               possible to create a {@code double} from the {@code String}
     */
    public double asDouble() {
        if (isString()) {
            return Double.parseDouble((String) this.v);
        } else if (isInteger()) {
            return ((Integer) this.v).doubleValue();
        } else if (isLong()) {
            return ((Long) this.v).doubleValue();
        } else if (isDouble()) {
            return ((Double) this.v).doubleValue();
        } else if (isFloat()) {
            return ((Float) this.v).doubleValue();
        } else if (isBoolean()) {
            return Boolean.TRUE == this.v ? 1d : 0d;
        }
        throw new IllegalStateException();
    }

    /**
     * Constructs a {@code Variant} from the given stream. The stream must follow the rules
     * of the {@link #serialize(OutputStream)} method.
     * @param in the input stream to deserialize the {@code Variant} from
     * @throws IOException may occurs if the stream have problems, e.g. a wrong format
     * @throws IllegalArgumentException if {@code in} represents {@code null}
     * @see {@link #serialize(OutputStream)}
     */
    public Variant(final InputStream in) throws IOException {
        if (in == null) {
            throw new IllegalArgumentException("in is null");
        }
        final DataInputStream dataIn;
        if (in instanceof DataInputStream) {
            dataIn = (DataInputStream) in;
        } else {
            dataIn = new DataInputStream(in);
        }
        final String type = dataIn.readUTF();
        final String value = dataIn.readUTF();

        if (STRING.equals(type)) {
            this.v = value;
        } else if (INTEGER.equals(type)) {
            this.v = Integer.valueOf(value);
        } else if (LONG.equals(type)) {
            this.v = new Long(Long.parseLong(value));
        } else if (DOUBLE.equals(type)) {
            this.v = Double.valueOf(value);
        } else if (FLOAT.equals(type)) {
            this.v = Float.valueOf(value);
        } else if (BOOLEAN.equals(type)) {
            this.v = Boolean.TRUE.toString().equalsIgnoreCase(value) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            throw new IllegalStateException("unknown type " + type + " for value " + value);
        }
    }

    /**
     * Serialize this instance to the supplied output stream.
     * @param out the stream to store the data in
     * @throws IOException if an error occurs during writing to the supplied stream
     * @throws IllegalArgumentException if {@code out} represents {@code null}
     */
    public void serialize(final OutputStream out) throws IOException {
        if (out == null) {
            throw new IllegalArgumentException("out is null");
        }
        final DataOutputStream dataOut;
        if (out instanceof DataOutputStream) {
            dataOut = (DataOutputStream) out;
        } else {
            dataOut = new DataOutputStream(out);
        }
        if (isString()) {
            dataOut.writeUTF(STRING);
        } else if (isInteger()) {
            dataOut.writeUTF(INTEGER);
        } else if (isLong()) {
            dataOut.writeUTF(LONG);
        } else if (isDouble()) {
            dataOut.writeUTF(DOUBLE);
        } else if (isFloat()) {
            dataOut.writeUTF(FLOAT);
        } else if (isBoolean()) {
            dataOut.writeUTF(BOOLEAN);
        } else {
            throw new IllegalStateException("unsupported type: " + this.v);
        }
        dataOut.writeUTF(asString());
        dataOut.flush();
    }

    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Variant other = (Variant) obj;
        if (this.v != other.v && (this.v == null || !this.v.equals(other.v))) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.v != null ? this.v.hashCode() : 0);
        return hash;
    }

    /**
     * The value of the implementation. Do not use the value to access data.
     * The format of the returned value may change in later implementations.
     */
    /**
     * The value of the implementation. Do not use the value to access data.
     * The format of the returned value may change in later implementations.
     */
    public String toString() {
        final StringBuffer sb = new StringBuffer("Variant@");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append("[[value:");
        sb.append(this.v);
        sb.append("]]");

        return sb.toString();
    }
}
