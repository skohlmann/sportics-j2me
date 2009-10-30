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
package net.sportics.dni.rt.client.microedition.accu;

import java.util.Enumeration;
import java.util.Hashtable;

import net.sportics.dni.rt.client.microedition.TypedData;
import net.sportics.dni.rt.client.microedition.util.LogManager;

/**
 * Container for a bulk of {@link TypedData}. The container only contains one example
 * of a {@code TypedData} at once.
 *
 * @author Sascha Kohlmann
 *
 */
public final class Assemblage {

    private static final LogManager LOG = LogManager.getInstance("Assemblage");
    static {
        LOG.debug("#class: " + Assemblage.class.getName());
    }

    private final Hashtable types = new Hashtable();

    /** Constructs new instance. */
    public Assemblage() { }

    /** Copy constructor. */
    public Assemblage(final Assemblage assemblage) {
        if (assemblage != null) {
            synchronized(assemblage) {
                for (final Enumeration e = assemblage.types.keys(); e.hasMoreElements(); ) {
                    final Object key = e.nextElement();
                    final Object value = assemblage.types.get(key);
                    this.types.put(key, value);
                }
            }
        }
    }

    /** Adds the supplied typed data to the container and returns one. The returned
     * typed data may be {@linkplain TypedData#EMPTY empty} of a further supplied typed
     * data instance.
     * <p><strong>NOTE: the signatur may change in further implementations.</strong></p>
     * @param t the typed data to add
     * @return never {@code null}
     */
    public TypedData put(final TypedData t) {
        synchronized(this) {
//            System.out.println("put: " + t);
            if (t != null) {
                final String key = t.getDescriptor();
                final TypedData in = (TypedData) this.types.put(key, t);
//                System.out.println("assemblage after put: " + this);
                return in != null ? in : TypedData.EMPTY;
            }
        }
        return TypedData.EMPTY;
    }

    /**
     * Returns a typed data for the given {@link TypedDataDescriptor} or {@code null}.
     * <p><strong>NOTE: The supplied type will change to {@link TypedDataDescriptor} in
     * further versions.</strong></p>
     * @param descriptor the descriptor for lookup
     * @return a typed data element or {@code null}
     */
    public TypedData get(final String descriptor) {
        final TypedData t = (TypedData) this.types.get(descriptor);
        LOG.debug("fetch for descriptor " + descriptor + ": " + t);
        return t;
    }

    /**
     * Returns all descriptors. The value of {@link Enumeration#nextElement()} returns
     * yet {@String} and may return {@link TypedDataDescriptor} in further versions.
     * @return
     */
    public Enumeration descriptors() {
        return this.types.keys();
    }

    /**
     * Tests if the specified descriptor is in this assemblage. 
     * <p><strong>NOTE: The supplied type will change to {@link TypedDataDescriptor} in
     * further versions.</strong></p>
     * @param descriptor possible descriptor
     * @return {@code true} if and only if the assemblage contains a value for the
     *         supplied descriptor. {@code false} otherwise.
     */
    public boolean contains(final String descriptor) {
        LOG.debug("contains descriptor " + descriptor + ": " + this.types.containsKey(descriptor));
        return this.types.containsKey(descriptor);
    }

    /**
     * The value of the implementation. Do not use the value to access data.
     * The format of the returned value may change in later implementations.
     */
    public String toString() {
        final StringBuffer sb = new StringBuffer("Assemblage@");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append("[");
        for (final Enumeration e = this.types.elements(); e.hasMoreElements(); ) {
            final TypedData t = (TypedData) e.nextElement();
            sb.append("[");
            sb.append(t);
            sb.append("]");
        }
        sb.append("]");
        return sb.toString();
    }
}
