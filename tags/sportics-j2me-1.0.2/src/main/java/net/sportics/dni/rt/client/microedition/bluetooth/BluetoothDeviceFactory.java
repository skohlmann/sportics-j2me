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
package net.sportics.dni.rt.client.microedition.bluetooth;

import net.sportics.dni.rt.client.microedition.Attribute;
import net.sportics.dni.rt.client.microedition.Variant;
import net.sportics.dni.rt.client.microedition.bluetooth.device.zephyr.HxMDevice;
import net.sportics.dni.rt.client.microedition.device.Device;
import net.sportics.dni.rt.client.microedition.device.DeviceDb;
import net.sportics.dni.rt.client.microedition.device.DeviceDescriptor;
import net.sportics.dni.rt.client.microedition.device.DeviceFactory;


public final class BluetoothDeviceFactory implements DeviceFactory {

    private static final BluetoothDeviceFactory FACTORY = new BluetoothDeviceFactory();

    private static final String ZEPHYR_HXM_NAME_PREFIX = "HXM";

    /** Only one instance useful. */
    private BluetoothDeviceFactory() {
        
    }

    /** Returns an instance of the manager
     * @return an instance of the manager. Never <code>null</code> */
    public static final BluetoothDeviceFactory getInstance() {
        return FACTORY;
    }

    /** The descriptor of a remote bluetooth device.
     * @param descriptor the descriptor of the device
     * @return a instance of the return type or {@code null}
     * @see javax.bluetooth.RemoteDevice#getFriendlyName(boolean) */
    public Device deviceForDescriptor(final DeviceDescriptor descriptor) {
        if (descriptor != null) {
            final Attribute attr = descriptor.getAttributeForDescriptor(DeviceDb.ATTRIBUTE_NAME);
            final Variant v = attr.getValue();
            final String name = v.asString();
            return deviceForName(name);
        }
        return null;
    }

    /** The name of a remote bluetooth device.
     * <p>Use the real name of the device.</p>
     * @param name the name of the device
     * @return a instance of the return type or {@code null}
     * @see javax.bluetooth.RemoteDevice#getFriendlyName(boolean)
     * @deprecated use {@link #deviceForDescriptor(DeviceDescriptor)} instead.
     * */
    public BluetoothDevice deviceForName(final String name) {
        if (name == null) {
            return null;
        }
        BluetoothDevice dev = null;
        if (name.startsWith(ZEPHYR_HXM_NAME_PREFIX)) {
            dev = new HxMDevice();
        }
        if (dev != null) {
            dev.setRealDeviceName(name);
        }
        return dev;
    }

    public boolean existsDeviceForDescriptor(final DeviceDescriptor dd) {
        final Device device = deviceForDescriptor(dd);
        return device == null ? false : true;
    }
}
