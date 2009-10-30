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
package net.sportics.dni.rt.client.microedition.device;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;

import net.sportics.dni.rt.client.microedition.Attribute;

public final class DeviceDescriptor {

    static final int NO_ID = Integer.MIN_VALUE;

    private final Hashtable attributes = new Hashtable();
    private int id;
    private volatile boolean changed = false;

    DeviceDescriptor() {
        this(NO_ID);
    }

    DeviceDescriptor(final int id) {
        this.id = id;
    }

    DeviceDescriptor(final InputStream in) throws IOException {
        this(in, NO_ID);
    }

    DeviceDescriptor(final InputStream in, final int id) throws IOException {
        if (in == null) {
            throw new IllegalArgumentException("in is null");
        }
        this.id = id;
        final DataInputStream dataIn;
        if (in instanceof DataInputStream) {
            dataIn = (DataInputStream) in;
        } else {
            dataIn = new DataInputStream(in);
        }
        final int size = dataIn.readInt();
        for (int i = 0; i < size; i++) {
            final Attribute a = new Attribute(dataIn);
            addAttribute(a);
        }
    }

    final void serialized(final OutputStream out) throws IOException {
        if (out == null) {
            throw new IllegalArgumentException("out is null");
        }
        final DataOutputStream dataOut;
        if (out instanceof DataOutputStream) {
            dataOut = (DataOutputStream) out;
        } else {
            dataOut = new DataOutputStream(out);
        }
        synchronized(this.attributes) {
            final int size = this.attributes.size();
            dataOut.writeInt(size);
            for (final Enumeration e = attributes(); e.hasMoreElements(); ) {
                final Attribute a = (Attribute) e.nextElement();
                a.serialize(dataOut);
            }
        }
    }

    /* (non-Javadoc)
     * @see net.sportics.srts.client.microedition.device.DeviceDescriptor#addAttribute(net.sportics.srts.client.microedition.Attribute)
     */
    public void addAttribute(final Attribute attr) {
        if (attr != null) {
            synchronized(this.attributes) {
                final Object key = attr.getDescriptor();
                this.attributes.put(key, attr);
                this.changed = true;
            }
        }
    }

    /* (non-Javadoc)
     * @see net.sportics.srts.client.microedition.device.DeviceDescriptor#removeAttribute(net.sportics.srts.client.microedition.Attribute)
     */
    public void removeAttribute(final Attribute attr) {
        if (attr != null) {
            synchronized(this.attributes) {
                final Object key = attr.getDescriptor();
                this.attributes.remove(key);
                this.changed = true;
            }
        }
    }

    /* (non-Javadoc)
     * @see net.sportics.srts.client.microedition.device.DeviceDescriptor#attributes()
     */
    public Enumeration attributes() {
        return this.attributes.elements();
    }

    /**
     * 
     * @param descriptor
     * @return the {@code Attribute} or {@code null}
     */
    public Attribute getAttributeForDescriptor(final String descriptor) {
        if (descriptor != null) {
            for (final Enumeration e = attributes(); e.hasMoreElements(); ) {
                final Attribute attr = (Attribute) e.nextElement();
                final String descriptorName = attr.getDescriptor();
                if (descriptor.equals(descriptorName)) {
                    return attr;
                }
            }
        }
        return null;
    }

    boolean isChanged() {
        return this.changed;
    }

    int getId() {
        return this.id;
    }

    void setId(final int id) {
        this.id = id;
    }

    void clean() {
        this.changed = false;
    }

    /* (non-Javadoc)
     * @see net.sportics.srts.client.microedition.device.DeviceDescriptor#toString()
     */
    public String toString() {
        final StringBuffer sb = new StringBuffer("DeviceDescriptor@");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append("[[changed:");
        sb.append(this.changed);
        sb.append("][id:");
        sb.append(this.id);
        sb.append("][attribute:[");
        for (final Enumeration e = attributes(); e.hasMoreElements(); ) {
            final Object o = e.nextElement();
            sb.append(o);
        }
        sb.append("]]");
        return sb.toString();
    }

}
