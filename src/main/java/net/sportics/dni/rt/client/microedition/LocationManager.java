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
 *  A <code>LocationManager</code> represents a location-providing module,
 * generating <code>Coordinate</code>s.
 * @author Sascha Kohlmann
 */
public interface LocationManager {

    /** Status code indicates that the location provider is available. */
    int AVAILABLE = 1;
    /** Status code indicates that the location provider is out of service. */
    int OUT_OF_SERVICE = 3;
    /** Status code indicates that the location provider is temporarily unavailable. */
    int TEMPORARILY_UNAVAILABLE = 2;
    /** The degree sign */
    String DEGREE_SIGN = "\u00b0";

    /**
     * Adds a new listener.
     * @param listener a new listener
     */
    void addCoordinateListener(final CoordinateListener listener);

    /**
     * Removes the listener.
     * @param listener the listener to remove
     */
    void removeCoordinateListener(final CoordinateListener listener);

    /** Register an instance of the <code>CoordinateListener</code> at
     * {@link LocationManagerImpl#addCoordinateListener(CoordinateListener)} to get a stream
     * of <code>Coordinate</code>s. 
     * @author Sascha Kohlmann 
     */
    interface CoordinateListener {
        /** Called by the {@link LocationManagerImpl} if a new <code>Coordinate</code>
         * is available.
         * @param coordinate a new <code>Coordinate</code>
         */
        void onCoordinateUpdated(final Coordinate coordinate);
        /** Called by the {@link LocationManagerImpl} the state of the manager changed.
         * @param newState the new state
         * @see {@link LocationManagerImpl#AVAILABLE}
         * @see {@link LocationManagerImpl#OUT_OF_SERVICE}
         * @see {@link LocationManagerImpl#TEMPORARILY_UNAVAILABLE}
         */
        void onStateChange(final int newState);
    }

    /**
     * The <code>Corordinate</code> represents the standard set of basic location information.
     * This includes timestamped coordinates, speed and course,
     * @author Sascha Kohlmann
     */
    interface Coordinate {

        /** Returns the terminal's course in degrees relative to true north.
         * The value is in a range of [0.0,360.0) degrees.
         * @return the terminal's course in degrees relative to true north or
         *         {@link Float#NaN} if the course is not known
         */
        float getCourse();
        /** Returns the terminal's current ground speed in meters per second (m/s) at the
         * time of measurement. The speed is always a non-negative value.
         * @return the current ground speed in m/s for the terminal or
         *         {@link Float#NaN} if the speed is not known
         */
        float getSpeed();
        /** Returns the time stamp at which the data was collected. The timestamp follows
         * the rules of {@link System#currentTimeMillis()}.
         * @return a timestamp representing the time
         * @see {@link System#currentTimeMillis()}
         */
        long getTime();
        /** Returns the longitude component of this coordinate. Positive values indicate
         * eastern longitude and negative values western longitude. {@link Float#NaN}
         * if the longitude is not available.
         * <p>The longitude is given in WGS84 datum.</p>
         * @return the longitude in degrees
         */
        double getLongitude();
        /** Returns the latitude component of this coordinate. Positive values indicate
         * northern latitude and negative values southern latitude. {@link Double#NaN}
         * if the latitude is not available.
         * <p>The latitude is given in WGS84 datum.</p>
         * @return the latitude in degrees
         */
        double getLatitude();
        /** Returns the altitude component of this coordinate. Altitude is defined to mean
         * height above the WGS84 reference ellipsoid. 0.0 means a location at the ellipsoid
         * surface, negative values mean the location is below the ellipsoid surface,
         * {@link Double#NaN} that the altitude is not available.
         * @return the altitude in meters above the reference ellipsoid
         */
        float getAlitude();
        /**
         * Returns the horizontal accuracy of the location in meters (1-sigma standard deviation).
         * A value of {@link Float#NaN} means the horizontal accuracy could not be determined.
         * @return the horizontal accuracy in meters
         */
        float getHorizontalAccuracy();
        /**
         * Returns the accuracy of the location in meters in vertical direction 
         * (orthogonal to ellipsoid surface, 1-sigma standard deviation).
         * A value of {@link Float#NaN} means the vertical accuracy could not be determined.
         * @return the vertical accuracy in meters
         */
        float getVerticalAccuracy();
        /**
         * Returns the geodetic distance between this coordinate and the further supplied
         * coordinate. The value is represented in meters or may be of type {@link Float#NaN}.
         * @return the distance in meters
         */
        double getDistance();
    }
}
