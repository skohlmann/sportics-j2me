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

import java.util.Enumeration;
import java.util.Vector;

public class CopyVector extends Vector {

    public CopyVector(final Vector v) {
        super(v.size());
        synchronized(v) {
            final Enumeration e = v.elements();
            copy(e);
        }
    }

    public CopyVector(final Object[] array) {
        super(array.length);
        synchronized(array) {
            for (int i = 0; i < array.length; i++) {
                addElement(array[i]);
            }
        }
    }

    public CopyVector(final Enumeration e) {
        copy(e);
    }

    private final void copy(final Enumeration e) {
        while(e.hasMoreElements()) {
            final Object o = e.nextElement();
            addElement(o);
        }
    }
}
