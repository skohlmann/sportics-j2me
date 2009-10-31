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

import java.util.Enumeration;
import java.util.Hashtable;

import net.sportics.dni.rt.client.microedition.TypedData;
import net.sportics.dni.rt.client.microedition.TypedDataConsumer;
import net.sportics.dni.rt.client.microedition.accu.Accumulator;
import net.sportics.dni.rt.client.microedition.util.CopyHashtable;
import net.sportics.dni.rt.client.microedition.util.LogManager;

/**
 * Simple support for common {@link Device} methods. Overwrite the "{@code do} methods
 * like {@link #prepare()}. These methods are called by the state methods like
 * {@link #prepare()}. The interface implementing methods handles the state callbacks to
 * registered {@link DeviceLifecycleStateListener}.
 * <p>Use {@link #newData(TypedData)} to handle callbacks to registered
 * {@link TypedDataConsumer}.</p>
 * @author Sascha Kohlmann
 */
public abstract class AbstractDevice implements Device {

    private static final LogManager LOG = LogManager.getInstance("AbstractDevice");
    static {
        LOG.debug("#class: " + AbstractDevice.class.getName());
    }

    private int state = STATE_NOT_PREPARED;
    private final Hashtable listener = new Hashtable();
    private final Hashtable collectors = new Hashtable();

    /**
     * Adds the supplied {@code listener} to the list of listeners to inform
     * at lifecycle change.
     * @param listener a listener
     */
    public void addStateListener(final DeviceLifecycleStateListener listener) {
        if (listener != null) {
            this.listener.put(listener, listener);
        }
    }

    /**
     * Removes the supplied {@code listener} from the list of listeners to inform
     * at lifecycle change.
     * @param listener a listener
     */
    public void removeStateListener(final DeviceLifecycleStateListener listener) {
        if (listener != null) {
            this.listener.remove(listener);
        }
    }

    /**
     * Simple support for fire state events.
     * @param state the state to announce
     */
    protected void fireLifecycleEvent(final int state) {
        final Hashtable copy = new CopyHashtable(this.listener);
        for (final Enumeration e = copy.keys(); e.hasMoreElements(); ) {
            final DeviceLifecycleStateListener l = (DeviceLifecycleStateListener) e.nextElement();
            try {
                l.onStateChange(this, state);
            } catch (final Exception ex) {
                LOG.warn(getClass().getName() + " - Exception at onStateChange: "
                         + ex.getClass().getName() + ": " + ex.getMessage());
            }
        }
    }

    /**
     * Simple support for fire error events.
     * @param state the state in which the error occurs
     * @param exception the exception which occurs
     */
    protected void fireErrorEvent(final int state, final DeviceException exception) {
        final Hashtable copy = new CopyHashtable(this.listener);
        for (final Enumeration e = copy.keys(); e.hasMoreElements(); ) {
            final DeviceLifecycleStateListener l = (DeviceLifecycleStateListener) e.nextElement();
            try {
                l.onError(this, state, exception);
            } catch (final Exception ex) {
                LOG.warn(getClass().getName() + " - Exception at onError: "
                         + ex.getClass().getName() + ": " + ex.getMessage());
            }
        }
    }

    public void registerTypedDataConsumer(final TypedDataConsumer collector) {
        if (collector != null) {
            this.collectors.put(collector, collector);
        }
    }

    public void unregisterTypedDataConsumer(final TypedDataConsumer collector) {
        if (collector != null) {
            this.collectors.remove(collector);
        }
    }

    /**
     * Fires the supplied {@code type} the registered {@link TypedDataConsumer}.
     * If {@code type} is {@code null} nothing happens.
     * @param type the {@code TypedData} to promote
     */
    protected final void newData(final TypedData type) {
        newData(new TypedData[] {type});
    }

    /**
     * Fires the supplied {@code type} the registered {@link TypedDataConsumer}.
     * If {@code type} is {@code null}or the array length is {@code null} nothing happens.
     * @param type the {@code TypedData} to promote
     */
    protected final void newData(final TypedData[] types) {
        if (types != null && types.length != 0) {
            final Hashtable copy = new CopyHashtable(this.collectors);
            for (final Enumeration e = copy.keys(); e.hasMoreElements(); ) {
                final TypedDataConsumer tdc = (TypedDataConsumer) e.nextElement();
                tdc.newData(this, types);
            }
        }
    }

    /**
     * An {@code Enumeration} about all registered {@link Accumulator Accumulators}.
     * @return contains instances of {@link Accumulator}. Never {@code null}
     */
    protected final Enumeration getDeviceDataCollectorEnumeration() {
        return this.collectors.keys();
    }

    /**
     * Handles the state events. After the
     * {@link DeviceLifecycleStateListener#STATE_DO_PREPARATION STATE_DO_PREPARATION}
     * the method calls {@link #prepare()}.
     */
    public void prepare() {
        if (this.state == STATE_NOT_PREPARED) {
            this.state = STATE_DO_PREPARATION;
            fireLifecycleEvent(this.state);

            doPrepare();

            this.state = STATE_PREPARED;
            fireLifecycleEvent(this.state);
        } else {
            ;
        }
    }

    /** Overwrite if required. This default implementation does nothing. and returns immediately */
    protected void doPrepare() {}

    /** Overwrite if required. This default implementation does nothing. and returns immediately */
    protected void doStop() {}

    /** Overwrite if required. This default implementation does nothing. and returns immediately */
    protected void doStart() {}

    /** Overwrite if required. This default implementation does nothing. and returns immediately */
    protected void doRestart() {}

    /**
     * Handles the state events. After the {@link STATE_DO_RESTART} the method
     * calls {@link #doRestart()}.
     */
    public void restart() {
        this.state = STATE_DO_RESTART;
        fireLifecycleEvent(this.state);

        doRestart();

        this.state = STATE_RESTARTED;
        fireLifecycleEvent(this.state);
    }

    /**
     * Handles the state events. After the {@link STATE_DO_START} the method
     * calls {@link #doStart()}.
     */
    public void start() {
        this.state = STATE_DO_START;
        fireLifecycleEvent(this.state);

        doStart();

        this.state = STATE_STARTED;
        fireLifecycleEvent(this.state);
    }

    /**
     * Handles the state events. After the {@link STATE_DO_STOP} the method calls {@link #doStop()}.
     */
    public void stop() {
        this.state = STATE_DO_STOP;
        fireLifecycleEvent(this.state);

        doStop();

        this.state = STATE_STOPPED;
        fireLifecycleEvent(this.state);
    }

    public int currentState() {
        return this.state;
    }

    protected final int getState() {
        return this.state;
    }

    protected final void setState(final int state) {
        this.state = state;
    }


}
