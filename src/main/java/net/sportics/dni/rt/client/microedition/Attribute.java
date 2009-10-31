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
 * Contains a key/value pair. The key describes the value. Use {@link #serialize(OutputStream)}
 * if storing the value. There are additional convenience to support less code writing.
 *
 * <p>The implementation is immutable.</p>
 * @author Sascha Kohlmann
 */
public final class Attribute {

    public static final Attribute EMPTY = new Attribute("", new Variant(""));

    private final String descriptor;
    private final Variant value;

    /**
     * Constructs a new instance with the description and the value.
     * @param descriptor the descriptor of the value.
     * @param value the value
     * @throws IllegalArgumentException if and only if the values of the supplied parameter
     *                                  are {@code null}
     */
    public Attribute(final String descriptor, final Variant value) {
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
    public Attribute(final String descriptor, final int value) {
        this(descriptor, new Variant(value));
    }

    /**
     * Convenience constructor. Stores the value in a {@link variant}.
     * @param descriptor the descriptor of the value.
     * @param value the value
     * @throws IllegalArgumentException if and only if the values of {@code descriptor} is
     *                                  {@code null}
     */
    public Attribute(final String descriptor, final float value) {
        this(descriptor, new Variant(value));
    }

    /**
     * Convenience constructor. Sores the value in a {@link variant}.
     * @param descriptor the descriptor of the value.
     * @param value the value
     * @throws IllegalArgumentException if and only if the values of {@code descriptor} is
     *                                  {@code null}
     */
    public Attribute(final String descriptor, final double value) {
        this(descriptor, new Variant(value));
    }

    /**
     * Convenience constructor. Stores the value in a {@link variant}.
     * @param descriptor the descriptor of the value.
     * @param value the value
     * @throws IllegalArgumentException if and only if the values of {@code descriptor} is
     *                                  {@code null}
     */
    public Attribute(final String descriptor, final long value) {
        this(descriptor, new Variant(value));
    }

    /**
     * Convenience constructor. Stores the value in a {@link variant}.
     * @param descriptor the descriptor of the value.
     * @param value the value
     * @throws IllegalArgumentException if and only if the values of {@code descriptor} is
     *                                  {@code null}
     */
    public Attribute(final String descriptor, final boolean value) {
        this(descriptor, new Variant(value));
    }

    /**
     * Convenience constructor. Stores the value in a {@link variant}.
     * @param descriptor the descriptor of the value.
     * @param value the value
     * @throws IllegalArgumentException if and only if the values of the supplied parameter
     *                                  are {@code null}
     */
    public Attribute(final String descriptor, final String value) {
        this(descriptor, new Variant(value));
    }

    /**
     * Returns the descriptor of the {@code Attribute}.
     * @return the descriptor
     */
    public String getDescriptor() {
        return this.descriptor;
    }

    /**
     * Returns the value of the {@code Attribute}.
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
        final StringBuffer sb = new StringBuffer("Attribute@");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append("[[desc:");
        sb.append(getDescriptor());
        sb.append("][value:");
        sb.append(getValue());
        sb.append("]]");

        return sb.toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((descriptor == null) ? 0 : descriptor.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Attribute other = (Attribute) obj;
        if (descriptor == null) {
            if (other.descriptor != null)
                return false;
        } else if (!descriptor.equals(other.descriptor))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

    /**
     * Constructs a {@code Attribute} from the given stream. The stream must follow the rules
     * of the {@link #serialize(OutputStream)} method.
     * @param in the input stream to deserialize the {@code Attribute} from
     * @throws IOException may occurs if the stream have problems, e.g. a wrong format
     * @throws IllegalArgumentException if {@code in} represents {@code null}
     * @see {@link #serialize(OutputStream)}
     */
    public Attribute(final InputStream in) throws IOException {
        if (in == null) {
            throw new IllegalArgumentException("in is null");
        }
        final DataInputStream dataIn;
        if (in instanceof DataInputStream) {
            dataIn = (DataInputStream) in;
        } else {
            dataIn = new DataInputStream(in);
        }
        this.descriptor = dataIn.readUTF();
        this.value = new Variant(dataIn);
    }

    /**
     * Serialize this {@code Attribute} to the supplied output stream.
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
        dataOut.writeUTF(descriptor);
        this.value.serialize(dataOut);
        dataOut.flush();
    }
}
