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

import java.io.IOException;

import javax.bluetooth.DeviceClass;
import javax.bluetooth.RemoteDevice;

public final class RemoteDeviceContainer {

    private final RemoteDevice remoteDevice;
    private final DeviceClass deviceClass;

    RemoteDeviceContainer(final RemoteDevice device, final DeviceClass clazz) {
        this.remoteDevice = device;
        this.deviceClass = clazz;
    }

    /**
     * @return the remoteDevice
     */
    public RemoteDevice getRemoteDevice() {
        return this.remoteDevice;
    }

    /**
     * @return the deviceClass
     */
    public DeviceClass getDeviceClass() {
        return this.deviceClass;
    }

    public String toString() {
        final StringBuffer sb = new StringBuffer("RemoteDeviceContainer@");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append("[RemoteDevice[address:");
        sb.append(this.remoteDevice.getBluetoothAddress());
        sb.append("][friendlyName:");
        try {
            sb.append(this.remoteDevice.getFriendlyName(false));
        } catch (IOException e) {
            sb.append((Object) null);
        }
        sb.append("][authenticated:");
        sb.append(this.remoteDevice.isAuthenticated());
        sb.append("][encrypted:");
        sb.append(this.remoteDevice.isEncrypted());
        sb.append("]][DeviceClass[major:");
        sb.append(this.deviceClass.getMajorDeviceClass());
        sb.append("][minor:");
        sb.append(this.deviceClass.getMinorDeviceClass());
        sb.append("][services:");
        sb.append(this.deviceClass.getServiceClasses());
        sb.append("]]");

        return sb.toString();
    }
}
