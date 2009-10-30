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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotOpenException;

import net.sportics.dni.rt.client.microedition.ConfigurationException;
import net.sportics.dni.rt.client.microedition.Variant;
import net.sportics.dni.rt.client.microedition.util.LogManager;

public class DeviceDb {

    private static final LogManager LOG = LogManager.getInstance("DeviceDb");
    static {
        LOG.debug("#class: " + DeviceDb.class.getName());
    }

    private static final String CDEVICE_DB_STORE_NAME = "device";
    private static DeviceDb MANAGER = new DeviceDb();

    /** The name Attribute of the device.<br />
     * {@link Variant} should be of type {@link String}. */
    public static final String ATTRIBUTE_NAME = "Name";
    /** The alias name Attribute of the device.<br />
     * {@link Variant} should be of type {@link String}. */
    public static final String ATTRIBUTE_ALIAS_NAME = "Alias name";
    /** The classname Attribute of the device representing the device class implementation.<br />
     * {@link Variant} should be of type {@link String} pointing to an instance of {@link Device}.
     * */
    public static final String ATTRIBUTE_CLASSNAME = "Classname";
    /** Indicates that the device is a bluetooth device or not.<br />
     * {@link Variant} should be of type {@code boolean}. {@code true} if the device is
     * a bluetooth device. {@code false} otherwise. */
    public static final String ATTRIBUTE_BLUETOOTH = "Bluetooth";
    /** Indicates that the device is build in the agent.<br />
     * {@link Variant} should be of type {@code boolean}. {@code true} if the device is
     * build in the agent. {@code false} otherwise. */
    public static final String ATTRIBUTE_BLUETOOTH_ADDRESS = "Bluetooth address";
    /** Indicates the bluetooth major device.<br />
     * {@link Variant} should be of type {@code int}.
     * @see javax.bluetooth.DeviceClass#getMajorDeviceClass() */
    public static final String ATTRIBUTE_BLUETOOTH_MAJOR_CLASS = "Bluetooth major class";
    /** Indicates the bluetooth minor device.<br />
     * {@link Variant} should be of type {@code int}.
     * @see javax.bluetooth.DeviceClass#getMinorDeviceClass() */
    public static final String ATTRIBUTE_BLUETOOTH_MINOR_CLASS = "Bluetooth minor class";
    /** Indicates the bluetooth service classes.<br />
     * {@link Variant} should be of type {@code int}.
     * @see javax.bluetooth.DeviceClass#getServiceClasses() */
    public static final String ATTRIBUTE_BLUETOOTH_SERVICE_CLASSES = "Bluetooth service classes";
    /** Indicates the bluetooth device address.<br />
     * {@link Variant} should be of type {@code String}. */
    public static final String ATTRIBUTE_BUILD_IN = "Build in";
    /** Connecting URL for use with the GCF.<br />
     * {@link Variant} should be of type {@code String}. */
    public static final String ATTRIBUTE_CONNECT_URL = "Connect URL";
    /** Indicates that the device is useable for the application.<br />
     * {@link Variant} should be of type {@code boolean}. {@code true} if the device is
     * useable. {@code false} otherwise. */
    public static final String ATTRIBUTE_USE = "Use";

    private RecordStore rs = null;

    /** Only one instance useful. */
    private DeviceDb() {
    }

    /** Returns an instance of the manager
     * @return an instance of the manager. Never <code>null</code> */
    public static final DeviceDb getInstance() {
        LOG.debug("DeviceDb - Instance: " + MANAGER + " - " + MANAGER.getClass());
        return MANAGER;
    }

    public int countDeviceDescriptors() {
        synchronized(this) {
            try {
                checkStorage();
                return this.rs.getNumRecords();
            } catch (final RecordStoreException e) {
                throw new ConfigurationException();
            }
        }
    }

    public void storeDeviceDescriptor(final DeviceDescriptor descriptor) throws IOException {
        if (descriptor == null) {
            LOG.debug("storeDeviceDescriptor: Descriptor is null");
            return;
        }
        if (!descriptor.isChanged()) {
            LOG.debug("storeDeviceDescriptor: Descriptor not changed - " + descriptor);
            return;
        }
        synchronized(this) {
            try {
                checkStorage();
                final byte[] data = descriptorToBytes(descriptor);
                LOG.debug("storeDeviceDescriptor: byte.length: " + data.length);
                final int id = descriptor.getId();
                if (id == DeviceDescriptor.NO_ID) {
                    LOG.debug("storeDeviceDescriptor: Store for new descriptor id");
                    final int newId = this.rs.addRecord(data, 0, data.length);
                    descriptor.setId(newId);
                    LOG.debug("storeDeviceDescriptor: With new ID; " + descriptor);
                } else {
                    LOG.debug("storeDeviceDescriptor: Store without new descriptor id");
                    this.rs.setRecord(id, data, 0, data.length);
                }
                descriptor.clean();
            } catch (final RecordStoreException e) {
                throw new ConfigurationException();
            }
        }
    }

    public void removeDeviceDescriptor(final DeviceDescriptor descriptor) {
        if (descriptor == null) {
            return;
        }
        synchronized(this) {
            try {
                checkStorage();
                final int id = descriptor.getId();
                this.rs.deleteRecord(id);
            } catch (final RecordStoreException e) {
                throw new ConfigurationException();
            }
        }
    }

    public DeviceDescriptorEnumeration enumerateDeviceDescriptors() throws IOException {
        try {
            checkStorage();
            final RecordEnumeration re = this.rs.enumerateRecords(null, null, true);
            LOG.debug("RecordEnumeration: " + re);
            return new DeviceDescriptorEnumerationImpl(this.rs, re);
        } catch (final RecordStoreNotOpenException e) {
            final String msg = e.getMessage();
            throw new IOException("RecordStoreNotOpenException: " + msg);
        } catch (final RecordStoreException e) {
            final String msg = e.getMessage();
            throw new IOException("RecordStoreException: " + msg);
        }
    }

    public DeviceDescriptor newDeviceDescriptor() {
        return new DeviceDescriptor();
    }

    final void checkStorage() throws RecordStoreException {
        if (this.rs == null) {
            openRecordStore();
        }
    }

    final void openRecordStore() throws RecordStoreException {
        if (this.rs == null) {
            this.rs = RecordStore.openRecordStore(CDEVICE_DB_STORE_NAME,
                                                  true,
                                                  RecordStore.AUTHMODE_PRIVATE,
                                                  true);
        }
    }

    public interface DeviceDescriptorEnumeration extends Enumeration {
        void dispose();
    }

    static final class DeviceDescriptorEnumerationImpl implements DeviceDescriptorEnumeration {

        private final RecordEnumeration enumeration;
        private final RecordStore store;

        public DeviceDescriptorEnumerationImpl(final RecordStore s, final RecordEnumeration e) {
            this.enumeration = e;
            this.store = s;
        }

        public void dispose() {
            this.enumeration.destroy();
        }

        public boolean hasMoreElements() {
            return this.enumeration.hasNextElement();
        }

        public Object nextElement() {
            try {
                final int id = this.enumeration.nextRecordId();
                final byte[] data = this.store.getRecord(id);
                LOG.debug("nextElement: data.length: " + data.length);
                final DeviceDescriptor descriptor = bytesToDescriptor(data);
                descriptor.setId(id);
                return descriptor;
            } catch (final Exception e) {
                e.printStackTrace();
                final String msg = e.getMessage();
                throw new NoSuchElementException(msg);
            }
        }
    }

    static final DeviceDescriptor bytesToDescriptor(final byte[] data) throws IOException {
        final ByteArrayInputStream in = new ByteArrayInputStream(data);
        return new DeviceDescriptor(in);
    }


    static final byte[] descriptorToBytes(final DeviceDescriptor descriptor) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        descriptor.serialized(out);
        return out.toByteArray();
    }
}
