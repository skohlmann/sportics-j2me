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
import java.util.Enumeration;
import java.util.Vector;

import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;

import net.sportics.dni.rt.client.microedition.controller.ConfigureController;
import net.sportics.dni.rt.client.microedition.util.LogManager;

/**
 * The manager handels the discovering of Bluetooth devices and services. If you call
 * {@link #discoverServices(UUID[]) before a call of {@link #discoverDevices()} nothing
 * happens. Each call of a discover method resets the value of the structures.
 * @author Sascha Kohlmann
 */
public final class BluetoothManager {

    public static final String BLUETOOTH_ALLOWED_VALUE_YES = Boolean.TRUE.toString();
    public static final String BLUETOOTH_ALLOWED_VALUE_NO = "bluetooth.allowed";
    public static final String BLUETOOTH_ALLOWED_KEY = Boolean.FALSE.toString();

    private static final LogManager LOG = LogManager.getInstance("BluetoothManager");
    static {
        LOG.debug("#class: " + BluetoothManager.class.getName());
    }

    private static final BluetoothManager MANAGER = new BluetoothManager();
    private final Object lock = new Object();
    private static final int BLUETOOTH_TIMEOUT = 5000;

    private volatile Vector devices = new Vector();
    private boolean isLookupComplete;

    /** Only one instance useful. */
    private BluetoothManager() { }

    /** Returns an instance of the manager
     * @return an instance of the manager. Never <code>null</code> */
    public static final BluetoothManager getInstance() {
        LOG.debug("BluetoothManager - Instance: " + MANAGER + " - " + MANAGER.getClass());
        return MANAGER;
    }

    /**
     * Lookup for the device. Returns the number of founded devices or 0.
     * @return the number of founded devices. May 0.
     * @throws IOException if an error occurs
     */
    public int discoverDevices() throws IOException {
        synchronized(lock) {
            this.devices = new Vector();
            if (ConfigureController.currentBluetoothDecision()) {
                this.isLookupComplete = false;

                LOG.debug("start getLocalDevice()");
                final LocalDevice local = LocalDevice.getLocalDevice();
                LOG.debug("finish getLocalDevice(): " + local);
                final DiscoveryAgent discoveryAgent = local.getDiscoveryAgent();
                final DiscoveryListener listener = new EmbeddedDiscoveryListener();
                discoveryAgent.startInquiry(DiscoveryAgent.GIAC, listener);

                while ((!isLookupComplete)) {
                    try {
                        synchronized(MANAGER) {
                            MANAGER.wait(BLUETOOTH_TIMEOUT);
                        }
                    } catch (final InterruptedException e) {
                        final String msg = e.getMessage();
                        System.err.print(msg);
                    }
                    if (isLookupComplete) {
                        discoveryAgent.cancelInquiry(listener);
                    }
                }
            }
            return this.devices.size();
        }
    }

    /**
     * Lookup for the Bluetooth services. Returns the number of founded services or 0.
     * @return a {@code Vector} of {@link ServiceRecord} elements
     * @throws IOException if an error occurs
     * @throws IllegalArgumentException is the parameter is <code>null</code>
     */
    public Vector discoverServices(final UUID[] uuidSet) throws IOException {
        if (uuidSet == null) {
            throw new IllegalArgumentException("uuidSet is null");
        }

        final Vector services = new Vector();
        if (ConfigureController.currentBluetoothDecision()) {
            for (final Enumeration e = this.devices.elements(); e.hasMoreElements(); ) {
                final RemoteDeviceContainer container = (RemoteDeviceContainer) e.nextElement();
                final RemoteDevice remote = container.getRemoteDevice();
                final Vector deviceServices = discoverServices(uuidSet, remote.getFriendlyName(true));
                for (final Enumeration copy = deviceServices.elements(); copy.hasMoreElements(); ) {
                    final Object o = copy.nextElement();
                    services.addElement(o);
                }
            }
        }
        return services;
    }

    /**
     * Lookup for the Bluetooth services. Returns the number of founded services or 0.
     * @return a {@code Vector} of {@link ServiceRecord} elements
     * @throws IOException if an error occurs
     * @throws IllegalArgumentException if one of the parameters is <code>null</code>
     */
    public Vector discoverServices(final UUID[] uuidSet, final String name) throws IOException {
        if (uuidSet == null) {
            throw new IllegalArgumentException("uuidSet is null");
        }
        if (name == null) {
            throw new IllegalArgumentException("name is null");
        }
        final EmbeddedDiscoveryListener listener = new EmbeddedDiscoveryListener();
        if (ConfigureController.currentBluetoothDecision()) {

            final LocalDevice local = LocalDevice.getLocalDevice();
            final DiscoveryAgent discoveryAgent = local.getDiscoveryAgent();

            for (final Enumeration e = this.devices.elements(); e.hasMoreElements(); ) {
                final RemoteDeviceContainer container = (RemoteDeviceContainer) e.nextElement();
                final RemoteDevice remote = container.getRemoteDevice();
                final String friendlyName = remote.getFriendlyName(false);
                if (name.equals(friendlyName)) {
                    synchronized(this.lock) {
                        final int id = discoveryAgent.searchServices(null, uuidSet, remote, listener);

                        while ((!listener.isLookupComplete())) {
                            try {
                                synchronized(listener) {
                                    listener.wait(BLUETOOTH_TIMEOUT);
                                }
                            } catch (final InterruptedException ex) {
                                final String msg = ex.getMessage();
                                LOG.warn("InterruptedException: " + msg);
                            }
                            if (listener.isLookupComplete()) {
                                discoveryAgent.cancelServiceSearch(id);
                            }
                        }
                    }
                }
            }
        }
        return listener.getServices();
    }

    /**
     * Simple implementation for the listener to interact with the surrounding class.
     * @author Sascha Kohlmann
     */
    private class EmbeddedDiscoveryListener implements DiscoveryListener {

        private final Vector services;
        private boolean isLookupComplete;

        public EmbeddedDiscoveryListener() {
            this.services = new Vector();
            this.isLookupComplete = false;
        }

        public void deviceDiscovered(final RemoteDevice btDevice, final DeviceClass cod) {
            final RemoteDeviceContainer container = new RemoteDeviceContainer(btDevice, cod);
            BluetoothManager.this.devices.addElement(container);
        }

        public void inquiryCompleted(final int discType) {
            BluetoothManager.this.isLookupComplete = true;
            this.isLookupComplete = true;
            synchronized(this) {
                this.notify();
            }
        }

        public void serviceSearchCompleted(final int transID, final int respCode) {
            this.isLookupComplete = true;
            synchronized(this) {
                this.notify();
            }
        }

        public void servicesDiscovered(final int id, final ServiceRecord[] serviceRecords) {
            for (int i = 0; i < serviceRecords.length; i++) {
                this.services.addElement(serviceRecords[i]);
            }
        }

        public Vector getServices() {
            return services;
        }

        public boolean isLookupComplete() {
            return this.isLookupComplete;
        }
    }

    /**
     * The type of the object returns by {@link Enumeration#nextElement()} is
     * {@link RemoteDeviceContainer}.
     * @return never <code>null.
     */
    public Enumeration getDevices() {
        synchronized(lock) {
            return this.devices.elements();
        }
    }
}
