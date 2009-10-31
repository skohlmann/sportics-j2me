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

/**
 * Indicates the state of the {@link Device} lifecycle.
 * @author Sascha Kohlmann
 */
public interface DeviceLifecycleStateListener {

    /** Will be fired during lifecycle change.
     * @param source the device which fires the event
     * @param state the new state of the device */
    void onStateChange(final Device source, final int state);

    /** Will be fired if the device registers an error.
     * @param source the device which fires the event
     * @param state the state of the device
     * @param exception the exception type of the device */
    void onError(final Device source, final int state, final DeviceException exception);
}
