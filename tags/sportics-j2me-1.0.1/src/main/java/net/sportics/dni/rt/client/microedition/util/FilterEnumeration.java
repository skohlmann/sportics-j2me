/* Copyright (C) 2009 Sascha Kohlmann
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
import java.util.NoSuchElementException;

/**
 * @author Sascha Kohlmann
 */
public abstract class FilterEnumeration implements Enumeration {

    private final Enumeration source;

    public FilterEnumeration(final Enumeration source) {
        if (source == null) {
            throw new IllegalArgumentException("source is null");
        }
        this.source = source;
    }

    private Object next;

    public boolean hasMoreElements() {
        if (this.next != null) {
            return true;
        }
        while (this.source.hasMoreElements()) {
            final Object o = this.source.nextElement();
            if (o != null && filter(o)) {
                this.next = o;
                return true;
            }
        }
        return false;
    }

    public Object nextElement() {
        if (this.next == null) {
            hasMoreElements();
            if (this.next == null) {
                throw new NoSuchElementException();
            }
        }
        final Object retval = this.next;
        this.next = null;
        return retval;
    }

    public abstract boolean filter(final Object o);
}
