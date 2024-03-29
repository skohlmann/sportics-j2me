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

import net.sportics.dni.rt.client.microedition.SporticsException;

public class DeviceException extends SporticsException {

    private final int errorCode;

    public DeviceException(final int error) {
        this.errorCode = error;
    }

    public DeviceException(final int error, final String message) {
        super(message);
        this.errorCode = error;
    }

    public int getErrorCode() {
        return this.errorCode;
    }
}
