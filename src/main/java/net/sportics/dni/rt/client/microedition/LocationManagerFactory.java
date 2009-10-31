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

import net.sportics.dni.rt.client.microedition.util.LogManager;


/**
 * The factory constructs a real example of the {@link LocationManager} interface.
 * Call {@link #isGpsSupported()} to check if it is possible to get a manager instance
 * from {@link #getManager()}. If {@code isGpsSupported()} returns {@code true},
 * {@code getManager()} may not return {@code null}.
 *
 * <p>The factory only checks build-in location systems.</p>
 *
 * @author Sascha Kohlmann
 *
 */
public final class LocationManagerFactory {

    private static final LogManager LOG = LogManager.getInstance("LocationManagerFactory");
    static {
        LOG.debug("#class: " + LocationManagerFactory.class.getName());
    }
    private static final LocationManagerFactory FACTORY = new LocationManagerFactory();

    private static volatile LocationManager manager = null;

    /** Only one instance useful. */
    private LocationManagerFactory() {
    }

    /** Returns an instance of the manager factory.
     * @return an instance of the manager. Never <code>null</code> */
    public static final LocationManagerFactory getInstance() {
        return FACTORY;
    }

    /**
     * Returns an instance of a {@code LocationManager}. If {@link #isGpsSupported()}
     * the method will throw an {@code IllegalStateException}.
     * @return a manager instance
     * @throws IllegalStateException if it is not possible to create a
     *                               {@code LocationManager} instance
     */
    public LocationManager getManager() {
        if (manager == null) {
            synchronized (this) {
                if (manager == null) {
                    try {
                        final Class clazz = Class.forName(
                                "net.sportics.dni.rt.client.microedition.LocationManagerImpl"
                        );
                        manager = (LocationManager) clazz.newInstance();
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

    /** Checks if the system supports a build in GPS environment.
     * @return {@code true} if and only if a build in GPS environment is supported.
     *         {@code false} otherwise.
     */
    public boolean isGpsSupported() {
        try {
            Class.forName("javax.microedition.location.LocationProvider");
            return true;
        } catch (final ClassNotFoundException e) {
            return false;
        }
    }
}
