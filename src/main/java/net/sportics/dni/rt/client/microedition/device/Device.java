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

import net.sportics.dni.rt.client.microedition.Pauseable;
import net.sportics.dni.rt.client.microedition.Preparable;
import net.sportics.dni.rt.client.microedition.Startable;
import net.sportics.dni.rt.client.microedition.TypedDataDescriptor;
import net.sportics.dni.rt.client.microedition.TypedDataProducer;

/**
 * A {@code Device} is a system which delivers information. Normally a device is a remote
 * system like a heart beat or pulse or step measurement device. Such a device supports
 * a lifecycle.
 *
 * <dl>
 *   <dt>prepare</dt>
 *   <dd>Prepares the device. Before praparation the device fires a
 *     {@linkplain DeviceLifecycleStateListener state event} with
 *     {@link STATE_DO_PREPARATION} After praparation the device fires a
 *     {@link STATE_PREPARED} event to all registered {@code DeviceStateListener}s </dd>
 *   <dt>start</dt>
 *   <dd>Starts the device. What happen is not part of this device specification.
 *     Before starting the device fires a {@link STATE_DO_START} event.
 *     After starting the device fires a {@link STATE_STARTED} event.
 *   </dd>
 *   <dt>stop</dt>
 *   <dd>Stops the device. Before stopping the device fires a {@link STATE_DO_STOP} event.
 *     After stopping the device fires a {@link STATE_STOPPED} event.
 *   </dd>
 *   <dt>restart</dt>
 *   <dd>It may happen that the system {@link ERROR_LOST_CONNECTION} lost
 *     the connection to the device. If happens it is possible to {@link #restart()} the
 *     device. Before doing the restart, the device fires a {@link STATE_DO_RESTART} event
 *     and after restarting the device fires a {@link STATE_RESTARTED} event.
 *     </dd>
 * </dl>
 *
 * <p>If an error occurs during the lifecycle, a device should fire a
 * {@link DeviceLifecycleStateListener#onError(Device, int, DeviceException)} event. The
 * {@code DeviceException} should support a readable exception message.</p>
 *
 * @author Sascha Kohlmann
 * @see DeviceLifecycleStateListener
 */
public interface Device extends Preparable, Startable, TypedDataProducer {

    /** @see Device#prepare() */
    final int STATE_NOT_PREPARED = 0;
    /** @see Device#prepare() */
    final int STATE_DO_PREPARATION = 1;
    /** @see Device#prepare() */
    final int STATE_PREPARED = 2;
    /** @see Device#start() */
    final int STATE_DO_START = 3;
    /** @see Device#start() */
    final int STATE_STARTED = 4;
    /** @see Device#stop() */
    final int STATE_DO_STOP = 5;
    /** @see Device#stop() */
    final int STATE_STOPPED = 6;
    /** @see Device#restart() */
    final int STATE_DO_RESTART = 7;
    /** @see Device#restart() */
    final int STATE_RESTARTED = 8;
    /** If a Device implements also {@link Pausebale}.
     * @see Pauseable#pause()  */
    final int STATE_DO_PAUSE = 9;
    /** If a Device implements also {@link Pausebale}.
     * @see Pauseable#pause()  */
    final int STATE_PAUSED = 10;


    /**
     * Initialize the device. Fires the
     * {@link STATE_DO_PREPARATION}
     * event before initialization and the
     * {@link STATE_PREPARED} 
     * event after successful initialization.
     * @throws DeviceException if initialization fails
     * @see DeviceLifecycleStateListener
     */
    void prepare();

    /**
     * Starts the device. Fires the
     * {@link STATE_DO_START}
     * event before starting and the
     * {@link STATE_STARTED} event
     * after successful starting.
     * @throws DeviceException if starting fails
     * @throws IllegalStateException if {@code init()} wasn't called before {@code start()}
     * @see DeviceLifecycleStateListener
     */
    void start();

    /**
     * Adds a listener for lifecycle events.
     * @param listener the listener to add
     */
    void addStateListener(final DeviceLifecycleStateListener listener);

    /**
     * Removes the listener for lifecycle events.
     * @param listener the listener to remove
     */
    void removeStateListener(final DeviceLifecycleStateListener listener);

    /**
     * Stops the device. Fires the
     * {@link STATE_DO_STOPPING}
     * event before stopping and the
     * {@link STATE_STOPPED} event
     * after successful stopping.
     * @throws DeviceException if stopping fails
     * @throws IllegalStateException if {@code start()} wasn't called before {@code stop()}
     * @see DeviceLifecycleStateListener
     */
    void stop();

    /**
     * Restarts the device. Fires the
     * {@link STATE_DO_RESTART STATE_DO_RESTART}
     * event before stopping and the
     * {@link STATE_RESTATED} event
     * after successful restarting.
     * @throws DeviceException if stopping fails
     * @throws IllegalStateException if {@code start()} wasn't called before {@code stop()}
     * @see DeviceLifecycleStateListener
     */
    void restart();

    /**
     * Returns the name of the device. Never {@code null}.
     * @return the name of the device
     */
    String getDeviceName();

    /**
     * A list of supported types.
     * @return list of supported types. Never {@code null}
     * @see TypedDataDescriptor
     */
    String[] supportedTypedDataList();

    /**
     * Returns the current state.
     */
    int currentState();
}
