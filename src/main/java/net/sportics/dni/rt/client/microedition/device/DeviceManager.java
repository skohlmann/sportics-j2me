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

import net.sportics.dni.rt.client.microedition.Attribute;
import net.sportics.dni.rt.client.microedition.bluetooth.BluetoothDeviceFactory;
import net.sportics.dni.rt.client.microedition.util.LogManager;

public class DeviceManager implements DeviceFactory {

    private static final LogManager LOG = LogManager.getInstance("DeviceManager");
    static {
        LOG.debug("#class: " + DeviceManager.class.getName());
    }

    private static final DeviceManager MANAGER = new DeviceManager();
    private static final BluetoothDeviceFactory BLUETOOTH = BluetoothDeviceFactory.getInstance();

    /** Only one instance useful. */
    private DeviceManager() {
    }

    /** Returns an instance of the manager
     * @return an instance of the manager. Never <code>null</code> */
    public static final DeviceManager getInstance() {
        LOG.debug("Instance: " + MANAGER + " - class: " + MANAGER.getClass().getName());
        return MANAGER;
    }

    public Device deviceForDescriptor(final DeviceDescriptor dd) {
        if (dd != null) {
            final Attribute classNameAttr =
                dd.getAttributeForDescriptor(DeviceDb.ATTRIBUTE_CLASSNAME);
            if (classNameAttr != null) {
                final String className = classNameAttr.getValue().asString();
                try {
                    final Class clazz = Class.forName(className);
                    final Object o = clazz.newInstance();
                    if (o instanceof Device) {
                        return (Device) o;
                    }
                    return null;
                } catch (final ClassNotFoundException e) {
                    e.printStackTrace();
                    return null;
                } catch (final InstantiationException e) {
                    e.printStackTrace();
                    return null;
                } catch (final IllegalAccessException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            return BLUETOOTH.deviceForDescriptor(dd);
        }
        return null;
    }

    public boolean existsDeviceForDescriptor(DeviceDescriptor dd) {
        final Device device = deviceForDescriptor(dd);
        return device == null ? false : true;
    }
}
