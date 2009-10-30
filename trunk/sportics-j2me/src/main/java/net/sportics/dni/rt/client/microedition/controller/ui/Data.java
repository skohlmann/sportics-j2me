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
package net.sportics.dni.rt.client.microedition.controller.ui;


/**
 *
 * @author Sascha Kohlmann
 */
final class Data {

    private String description;
    private String value;
    private String unit;

    public String getDescription() {
        return this.description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getUnit() {
        return this.unit;
    }

    public void setUnit(final String unit) {
        this.unit = unit;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(final String value) {
        this.value = value;
    }


    public String toString() {
        final StringBuffer sb = new StringBuffer("Data@");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append("[[description=");
        sb.append(this.description);
        sb.append("][value=");
        sb.append(this.value);
        sb.append("][unit=");
        sb.append(this.unit);
        sb.append("]]");

        return sb.toString();
    }
}
