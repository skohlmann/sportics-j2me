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
package net.sportics.dni.rt.client.microedition.bluetooth;

import java.util.Enumeration;
import net.sportics.dni.rt.client.microedition.Attribute;
import net.sportics.dni.rt.client.microedition.Variant;
import net.sportics.dni.rt.client.microedition.device.DeviceDb;
import net.sportics.dni.rt.client.microedition.device.DeviceDescriptor;
import net.sportics.dni.rt.client.microedition.util.FilterEnumeration;

/**
 * Filters out all non uncategorized major dDevice class from a enumeration
 * of {@link DeviceDescriptor}s or {@link Attribute}s.
 * @author Sascha Kohlmann
 */
public class NonUncategorizedMajorDeviceClassFilterEnumeration extends FilterEnumeration {

    /** Constructs a new instance. */
    public NonUncategorizedMajorDeviceClassFilterEnumeration(final Enumeration source) {
        super(source);
    }

    /**
     * Only {@link DeviceDescriptor} containig an {@link Attribute} - or single {@code Attribut} -
     * with a descriptor {@link DeviceDb#ATTRIBUTE_BLUETOOTH_MAJOR_CLASS} and an integer
     * {@link Variant} greater {@link BluetoothDevice#MDC_TOY} path the filter.
     * @return {@code true} if and only if the supplied value complies with the rules of
     *         the documentation. {@code false} otherwise
     */
    public boolean filter(final Object o) {
        if (o instanceof DeviceDescriptor) {
            System.out.println("filter() - instanceof DeviceDescriptor");
            final DeviceDescriptor dd = (DeviceDescriptor) o;
            final Attribute attr =
                    dd.getAttributeForDescriptor(DeviceDb.ATTRIBUTE_BLUETOOTH_MAJOR_CLASS);
            return checkAttribut(attr);
        } else if (o instanceof Attribute) {
            System.out.println("filter() - instanceof Attribute");
            return checkAttribut((Attribute) o);
        }
        System.out.println("filter() - no supported instance");
        return false;
    }

    final boolean checkAttribut(final Attribute attr) {
        System.out.println("checkAttribut() - enter: " + attr);
        if (attr != null) {
            final String desc = attr.getDescriptor();
            System.out.println("checkAttribut() - desc: " + desc);
            if (DeviceDb.ATTRIBUTE_BLUETOOTH_MAJOR_CLASS.equals(desc)) {
                final Variant v = attr.getValue();
                System.out.println("checkAttribut() - Variant: " + v);
                return checkVariant(v);
            }
        }
        return false;
    }

    final boolean checkVariant(final Variant v) {
        final int value = v.asInteger();
        if (value >= BluetoothDevice.MDC_TOY) {
            return false;
        }
        return true;
    }
}
