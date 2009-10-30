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

import net.sportics.dni.rt.client.microedition.LocationManagerFactory;
import net.sportics.dni.rt.client.microedition.StorageManagerFactory;

public final class Environment {

    private Environment() { };

    /** Checks if the system supports file storage environment.
     * @return {@code true} if and only if file system storage environment is supported.
     *         {@code false} otherwise.
     */
    public static boolean storageSupported() {
        return StorageManagerFactory.getInstance().isStorageSupported();
    }

    /** Checks if the system supports a bluetooth environment.
     * @return {@code true} if and only if a bluetooth environment is supported.
     *         {@code false} otherwise.
     */
    public static boolean bluetoothSupported() {
        try {
            Class.forName("javax.bluetooth.LocalDevice");
            return true;
        } catch (final ClassNotFoundException e) {
            return false;
        }
    }

    /** Checks if the system supports a build in GPS environment.
     * @return {@code true} if and only if a build in GPS environment is supported.
     *         {@code false} otherwise.
     */
    public static boolean gpsSupported() {
        return LocationManagerFactory.getInstance().isGpsSupported();
    }
}
