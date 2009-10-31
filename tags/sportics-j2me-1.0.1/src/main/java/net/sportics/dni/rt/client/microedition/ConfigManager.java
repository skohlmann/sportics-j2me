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
package net.sportics.dni.rt.client.microedition;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import net.sportics.dni.rt.client.microedition.util.LogManager;

/**
 * The <code>ConfigManager</code> stores key value pairs of for configuration data permanently.
 * @author Sascha Kohlmann
 */
public class ConfigManager {

    private static final String CONFIG_STORE_NAME = "config";
    private static ConfigManager MANAGER = new ConfigManager();

    RecordStore rs = null;

    /** Only one instance useful. */
    private ConfigManager() {
    }

    /** Returns an instance of the manager
     * @return an instance of the manager. Never <code>null</code> */
    public static final ConfigManager getInstance() {
        return MANAGER;
    }

    /** Returns a value for the given key if available.
     * @param name the name of the key.
     * @return the value for the given key or <code>null</code> if no value is available
     */
    public String get(final String name) {
        return get(name, null);
    }

    /** Returns a value for the given key if available.
     * @param name the name of the key
     * @param defaultValue the default value if 
     * @return the value for the given key or the default value if the value is <code>null</code>
     */
    public String get(final String name, final String defaultValue) {
        if (name == null) {
            return null;
        }
        synchronized(this) {
            try {
                checkStorage();
                final RecordEnumeration e = this.rs.enumerateRecords(null, null, false);
                try {
                    while(e.hasNextElement()) {
                        final int id = e.nextRecordId();
                        final byte[] array = this.rs.getRecord(id);
                        final EntryPair sp = new EntryPair(array);
                        if (name.equals(sp.getKey())) {
                            final String value = sp.getValue();
                            return value != null ? value : defaultValue;
                        }
                    }
                } finally {
                    e.destroy();
                }
            } catch (final RecordStoreException e) {
                throw new ConfigurationException();
            }
            return defaultValue;
        }
    }

    /** Stores the given values permanently.
     * @param name the name of the key
     * @param value the value
     */
    public void set(final String name, final String value) {
        if (name == null || value == null) {
            return;
        }
        synchronized(this) {
            try {
                checkStorage();
                final RecordEnumeration e = this.rs.enumerateRecords(null, null, true);
                try {
                    while(e.hasNextElement()) {
                        final int id = e.nextRecordId();
                        final byte[] array = this.rs.getRecord(id);
                        final EntryPair sp = new EntryPair(array);
                        if (name.equals(sp.getKey())) {
                            this.rs.deleteRecord(id);
                            break;
                        }
                    }
                } finally {
                    e.destroy();
                }
                final EntryPair toStore = new EntryPair(name, value);
                final byte[] asArray = toStore.toByteArray();
                this.rs.addRecord(asArray, 0, asArray.length);
            } catch (final RecordStoreException e) {
                throw new ConfigurationException();
            }
        }
    }

    final void checkStorage() throws RecordStoreException {
        if (this.rs == null) {
            openRecordStore();
        }
    }

    final void openRecordStore() throws RecordStoreException {
        if (this.rs == null) {
            this.rs = RecordStore.openRecordStore(CONFIG_STORE_NAME,
                                                  true,
                                                  RecordStore.AUTHMODE_PRIVATE,
                                                  true);
        }
    }

    private final static class EntryPair {
        private final String key;
        private final String value;

        public EntryPair(final String key, final String value) {
            if (key == null || value == null) {
                throw new IllegalArgumentException();
            }
            this.key = key;
            this.value = value;
        }

        EntryPair(final byte[] array) {
            if (array == null) {
                throw new IllegalArgumentException();
            }
            final InputStream in = new ByteArrayInputStream(array);
            final DataInputStream din = new DataInputStream(in);
            try {
                this.key = din.readUTF();
                this.value = din.readUTF();
            } catch (final IOException e) {
                throw new ConfigurationException();
            } finally {
                doClose(din);
                doClose(in);
            }
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        byte[] toByteArray() {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final DataOutputStream dos = new DataOutputStream(baos);
            try {
                dos.writeUTF(this.key);
                dos.writeUTF(this.value);
                dos.flush();
                baos.flush();
                return baos.toByteArray();
            } catch (final IOException e) {
                throw new ConfigurationException();
            } finally {
                doClose(dos);
                doClose(baos);
            }
        }

        public String toString() {
            final StringBuffer sb = new StringBuffer("EntryPair@");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append("[");
            sb.append(this.key);
            sb.append("=");
            sb.append(this.value);
            sb.append("]");
            return sb.toString();
        }

        final void doClose(final Object o) {
            try {
                if (o instanceof InputStream) {
                    ((InputStream) o ).close();
                } else  if (o instanceof OutputStream) {
                    ((OutputStream) o ).flush();
                    ((OutputStream) o ).close();
                } else  if (o instanceof RecordStore) {
                    ((RecordStore) o ).closeRecordStore();
                }
            } catch (final IOException e) {
                // Ignore it.
            } catch (final RecordStoreException e) {
                // Ignore it.
            }
        }
    }
}
