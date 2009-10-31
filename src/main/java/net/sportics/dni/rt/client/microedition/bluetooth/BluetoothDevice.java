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

import net.sportics.dni.rt.client.microedition.device.Device;

public interface BluetoothDevice extends Device {

    /** Bluetooth {@linkplain javax.bluetooth.DeviceClass#getMajorDeviceClass() major class}
     * for phones. */
    public static final int MDC_PHONE = 0x200;

    /** Bluetooth {@linkplain javax.bluetooth.DeviceClass#getMajorDeviceClass() major class}
     * for computer. */
    public static final int MDC_COMPUTER = 0x100;

    /** Bluetooth {@linkplain javax.bluetooth.DeviceClass#getMajorDeviceClass() major class}
     * for <acronym title='Local Area Network'>LAN</acronym>. */
    public static final int MDC_LAN = 0x300;

    /** Bluetooth {@linkplain javax.bluetooth.DeviceClass#getMajorDeviceClass() major class}
     * for audio/video. */
    public static final int MDC_AV = 0x400;

    /** Bluetooth {@linkplain javax.bluetooth.DeviceClass#getMajorDeviceClass() major class}
     * for peripheral. */
    public static final int MDC_PERIPHERAL = 0x500;

    /** Bluetooth {@linkplain javax.bluetooth.DeviceClass#getMajorDeviceClass() major class}
     * for imaging. */
    public static final int MDC_IMAGEING = 0x600;

    /** Bluetooth {@linkplain javax.bluetooth.DeviceClass#getMajorDeviceClass() major class}
     * for wearable. */
    public static final int MDC_WEARABLE = 0x700;

    /** Bluetooth {@linkplain javax.bluetooth.DeviceClass#getMajorDeviceClass() major class}
     * for toy. */
    public static final int MDC_TOY = 0x800;

    /**
     * The name must be setted before the first call of {@link #prepare()}.
     * @param name the real name of the device. Maybe the 
     * {@link javax.bluetooth.RemoteDevice#getFriendlyName(boolean)}.
     * @see BluetoothDeviceFactory#deviceForName(String)
     */
    void setRealDeviceName(final String name);
}
