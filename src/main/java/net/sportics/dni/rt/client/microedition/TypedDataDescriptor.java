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
 * Some predefined types for measurement.
 * <p><strong>Note: This call will have major changes in future devlopment. Its used all over
 * so use with care.</strong></p>
 * @author Sascha Kohlmann
 */
public final class TypedDataDescriptor {

    private TypedDataDescriptor() {}

    /** Defines the longitude of a position.
     * @see LocationManager#Coordinate#getLongitude() */
    public static final String LONGITUDE = "lon";
    /** Defines the latitude of a position.
     * @see LocationManager#Coordinate#getLatitude() */
    public static final String LATITUDE = "lat";
    /** Defines the altitude of a position.
     * @see LocationManager#Coordinate#getAlitude() */
    public static final String ALTITUDE = "alt";
    /** @see LocationManager#Coordinate#getHorizontalAccuracy() */
    public static final String HORIZONTAL_ACCURACY = "hac";
    /** @see LocationManager#Coordinate#getVerticalAccuracy() */
    public static final String VERTICAL_ACCURACY = "vac";
    /** Defines the speed in meters per second.
     * @see LocationManager#Coordinate#getSpeed() */
    public static final String SPEED = "spd";
    /** The course
     * @see LocationManager#Coordinate#getCourse() */
    public static final String COURSE = "crs";
    /** Distance in meter. */
    public static final String DISTANCE = "dst";
    /** Pulse of the heart in the body. The pulse may differ from the heart rate
     *  @see #HEART_RATE */
    public static final String PULSE = "pls";
    /** Heart rate. The heart rate may differ from the pulse.
     * @see #PULSE */
    public static final String HEART_RATE = "hrt";
    /** Steps or strides as an upcounting value. */
    public static final String STRIDES = "str";
    /** Defines a timestamp in milliseconds since epoche. */
    public static final String TIMESTAMP = "tme";
    /** Duration given in seconds since start. */
    public static final String DURATION = "dur";
    /** Duration given in seconds since last {@link Device#restart()}. */
    public static final String DURATION_CURRENT = "cdr";
    /** The current battery level in percent. The value should be in a range from 0 to 100.
     * 0 means battery is empty. 100 means battery is full. */
    public static final String POWER_LEVEL = "plv";
    /** Cadence value. This maybe strides, strokes or pedal frequency. The
     * value based on minutes. */
    public static final String CADENCE = "cad";
    /** Pace in second for meter. */
    public static final String PACE = "pac";
}
