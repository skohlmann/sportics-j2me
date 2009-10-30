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
package net.sportics.dni.rt.client.microedition.ui;

class WidthHeight {

    public final int width;
    public final int height;

    public WidthHeight(final int width, final int height) {
        super();
        this.width = width;
        this.height = height;
    }

    /**
     * The value of the implementation. Do not use the value to access data.
     * The format of the returned value may change in later implementations.
     */
    public String toString() {
        final StringBuffer sb = new StringBuffer("WidthHeight@");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append("[[width:");
        sb.append(this.width);
        sb.append("][height:");
        sb.append(this.height);
        sb.append("]]");
        return sb.toString();
    }

    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WidthHeight other = (WidthHeight) obj;
        if (this.width != other.width) {
            return false;
        }
        if (this.height != other.height) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + this.width;
        hash = 23 * hash + this.height;
        return hash;
    }

}
