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

/**
 * The factory constructs a real example of the {@link StorageManager} interface.
 * Call {@link #isStorageSupported()} to check if it is possible to get a manager instance
 * from {@link #getManager()}. If {@code isStorageSupported()} returns {@code true},
 * {@code getManager()} may not return {@code null}.
 *
 * @author Sascha Kohlmann
 */
public final class StorageManagerFactory {

    private static final StorageManagerFactory FACTORY = new StorageManagerFactory();

    private static volatile StorageManager manager = null;

    /** Only one instance useful. */
    private StorageManagerFactory() {
    }

    /** Returns an instance of the manager factory.
     * @return an instance of the manager factory. Never <code>null</code> */
    public static final StorageManagerFactory getInstance() {
        return FACTORY;
    }

    /**
     * Returns an instance of a {@code StorageManager}. If {@link #isStorageSupported()}
     * the method will throw an {@code IllegalStateException}.
     * @return a manager instance
     * @throws IllegalStateException if it is not possible to create a
     *                               {@code StorageManager} instance
     */
    public StorageManager getManager() {
        if (manager == null) {
            synchronized (this) {
                if (manager == null) {
                    try {
                        final Class clazz = Class.forName(
                                "net.sportics.dni.rt.client.microedition.StorageManagerImpl"
                        );
                        manager = (StorageManager) clazz.newInstance();
                    } catch (final ClassNotFoundException e) {
                        final String msg = e.getMessage();
                        throw new IllegalStateException(msg);
                    } catch (final InstantiationException e) {
                        final String msg = e.getMessage();
                        throw new IllegalStateException(msg);
                    } catch (final IllegalAccessException e) {
                        final String msg = e.getMessage();
                        throw new IllegalStateException(msg);
                    }
                }
            }
        }
        return manager;
    }


    /** Checks if the system supports file storage environment.
     * @return {@code true} if and only if file system storage environment is supported.
     *         {@code false} otherwise.
     */
    public boolean isStorageSupported() {
        try {
            Class.forName("javax.microedition.io.file.FileConnection");
            return true;
        } catch (final ClassNotFoundException e) {
            return false;
        }
    }
}
