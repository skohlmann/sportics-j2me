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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationListener;
import javax.microedition.location.LocationProvider;
import javax.microedition.location.QualifiedCoordinates;

import net.sportics.dni.rt.client.microedition.io.BufferedOutputStream;
import net.sportics.dni.rt.client.microedition.util.DateSupport;
import net.sportics.dni.rt.client.microedition.util.Environment;
import net.sportics.dni.rt.client.microedition.util.LogManager;

/**
 *  A <code>LocationManager</code> represents a location-providing module,
 * generating <code>Coordinate</code>s.
 * @author Sascha Kohlmann
 */
final class LocationManagerImpl implements LocationManager {

    private static final LogManager LOG = LogManager.getInstance("LocationManagerImpl");
    static {
        LOG.debug("#class: " + LocationManagerImpl.class.getName());
    }

    private static final int LOCATION_LISTENER_MAX_AGE = 1;
    private static final int LOCATION_LISTENER_TIMEOUT = 1;
    private static final int LOCATION_LISTENER_INTERVAL = 1;
    private static final double SECOND_IN_MILLIS = 1000.0d;

    private static final String DOT = ".";
    private static final String PREFIX_GPS = "GPS";
    private static final String POSTFIX_PLAIN = "plain";
    private static final String POSTFIX_LIF = "lif";
    private static final String POSTFIX_NMEA = "nmea";
    private static final String MIME_TEXT_PLAIN = "text/plain";
    private static final String MIME_LIF = "application/X-jsr179-location-lif";
    private static final String MIME_NMEA = "application/X-jsr179-location-nmea";
    private static final String LINE_SEPARATOR = "line.separator";
    private static final String NEWLINE = System.getProperty(LINE_SEPARATOR);

    final private Hashtable listener = new Hashtable();
    private LocationProvider lp;

    private Writer extraInfoWriter = null;
    private boolean stillTried = false;
    private Location furtherLocation = null;

    /**
     * Adds a new listener.
     * @param listener a new listener
     */
    public void addCoordinateListener(final CoordinateListener listener) {
        synchronized(this) {
            checkLocationProvider();
            if (listener != null) {
                this.listener.put(listener, listener);
                LOG.debug("CoordinationLister added: " + listener);
            }
        }
    }

    /**
     * Removes the listener.
     * @param listener the listener to remove
     */
    public void removeCoordinateListener(final CoordinateListener listener) {
        synchronized(this) {
            checkLocationProvider();
            if (listener != null) {
                this.listener.remove(listener);
            }
        }
    }

    void checkLocationProvider() {
        if (this.lp == null) {
            try {
                LOG.debug("start LocationProvider creation");
                createLocationProvider();
            } catch (final LocationManagerException e) {
            }
        }
    }

    void createLocationProvider() {
        try {
            final Criteria c = new Criteria();
            c.setSpeedAndCourseRequired(true);
            this.lp = LocationProvider.getInstance(c);
            if (this.lp == null) {
                LOG.config("Unable to get a LocationProvider with criteria speed and course. "
                           + "Use fallback.");
                c.setSpeedAndCourseRequired(false);
                this.lp = LocationProvider.getInstance(c);
            }
            if (this.lp == null) {
                LOG.warn("Unable to get LocationProvider with default criteria. Give up.");
                return;
            }
            LOG.debug("start LocationProvider creation - done");
            addLocationListener();
            LOG.debug("Start LocationProvider creation - listener setted");
        } catch (final LocationException e) {
            final String message = e.getMessage();
            throw new LocationManagerException(message);
        }
    }

    void addLocationListener() {
        this.lp.setLocationListener(new LocationListener() {
            public void locationUpdated(final LocationProvider provider,
                                        final Location location) {
                if (location != null && location.isValid()) {
                    final CoordinateImpl coord = new CoordinateImpl();
                    coord.course = location.getCourse();
                    coord.speed = location.getSpeed();
                    coord.time = location.getTimestamp();
                    LOG.info("locationUpdated() - speed: " + coord.speed);
                    final QualifiedCoordinates coords = location.getQualifiedCoordinates();

                    if (coords != null) {
                        coordinates(coord, coords);
                        if (LocationManagerImpl.this.furtherLocation != null
                                && coord.lat != Double.NaN
                                && coord.lon != Double.NaN) {
                            distanceAndSpeed(location, coord, coords);
//                            LOG.info("locationUpdated() - again speed: " + coord.speed
//                                     + " - distance: " + coord.distance);
                        }
                        if (coord.lat != Double.NaN && coord.lon != Double.NaN) {
//                            LOG.info("locationUpdated() - coord.lat: " + coord.lat
//                                     + " - coord.lon: " + coord.lon);
                            LOG.info("locationUpdated() - old location: "
                                     + LocationManagerImpl.this.furtherLocation
                                     + " - new location: " + location);
                            LocationManagerImpl.this.furtherLocation = location;
                        }
                    } else {
                        coord.alt = Float.NaN;
                        coord.lat = Double.NaN;
                        coord.lon = Double.NaN;
                        coord.horiziontalAccuracy = Float.NaN;
                        coord.verticalAccuracy = Float.NaN;
                    }
                    LOG.info("locationUpdated() - coord: " /*+ coord*/);

                    for (final Enumeration keys = LocationManagerImpl.this.listener.keys(); 
                            keys.hasMoreElements(); ) {
                        final CoordinateListener l = (CoordinateListener) keys.nextElement();
                        try {
                            l.onCoordinateUpdated(coord);
                        } catch (final Exception ex) {
                            LOG.warn("Exception at onCoordinateUpdated: "
                                     + ex.getClass().getName() + ": " + ex.getMessage());
                        }
                    }
                    handleExtraInfo(location);
                }
            }

            final void coordinates(final CoordinateImpl coord,
                                   final QualifiedCoordinates coords) {
                try {
                    coord.alt = coords.getAltitude();
                } catch (final Exception e) {
                    coord.alt = Float.NaN;
                }
                try {
                    coord.lat = coords.getLatitude();
                } catch (final Exception e) {
                    coord.lat = Double.NaN;
                }
                try {
                    coord.lon = coords.getLongitude();
                } catch (final Exception e) {
                    coord.lon = Double.NaN;
                }
                try {
                    coord.horiziontalAccuracy = coords.getHorizontalAccuracy();
                } catch (final Exception e) {
                    coord.horiziontalAccuracy = Float.NaN;
                }
                try {
                    coord.verticalAccuracy = coords.getVerticalAccuracy();
                } catch (final Exception e) {
                    coord.verticalAccuracy = Float.NaN;
                }
            }

            final void distanceAndSpeed(final Location location,
                                        final CoordinateImpl coord,
                                        final QualifiedCoordinates coords) {
                if (location != null && coords != null) {
                    LOG.info("distanceAndSpeed() - location and coords are not null");
                    final QualifiedCoordinates furtherCoordinate =
                        LocationManagerImpl.this.furtherLocation.getQualifiedCoordinates();
                    if (furtherCoordinate != null) {
                        coord.distance = furtherCoordinate.distance(coords);
                        LOG.info("distanceAndSpeed() - coord.distance: " /*+ coord.distance*/);
                    }
                }

                if (coord.speed == Float.NaN) {
                    coord.speed = calculateSpeed(location, coord);
                }
            }

            final float calculateSpeed(final Location location,
                                       final CoordinateImpl coord) {
                LOG.info("calculateSpeed(): " /*+ location + " - " + coord*/);
                if (location != null && coord != null) {
                    final long currentTime = location.getTimestamp();
                    final long oldTime =
                        LocationManagerImpl.this.furtherLocation.getTimestamp();
                    final long diff = currentTime - oldTime;
                    final double seconds = diff / SECOND_IN_MILLIS;
                    return (float) (coord.distance / seconds);
                }
                return 0.0f;
            }

            public void providerStateChanged(final LocationProvider provider,
                                             final int newState) {
                for (final Enumeration keys = LocationManagerImpl.this.listener.keys(); 
                        keys.hasMoreElements(); ) {
                    final CoordinateListener l = (CoordinateListener) keys.nextElement();
                    try {
                        l.onStateChange(newState);
                    } catch (final Exception ex) {
                        LOG.warn("Exception at onStateChange: "
                                 + ex.getClass().getName() + ": " + ex.getMessage());
                    }
                }
            }
        }, LOCATION_LISTENER_INTERVAL, LOCATION_LISTENER_TIMEOUT, LOCATION_LISTENER_MAX_AGE);
    }

    /**
     * Stores extra info of the NMEA or other.
     * @param location checks the <em>location</em> for extra information.
     * @see Location#getExtraInfo(String)
     */
    void handleExtraInfo(final Location location) {
        if (location != null) {
            final String nmea = location.getExtraInfo(MIME_NMEA);
            if (nmea != null) {
                storeExtraInfo(nmea, POSTFIX_NMEA);
                return;
            }
            final String lif = location.getExtraInfo(MIME_LIF);
            if (lif != null) {
                storeExtraInfo(lif, POSTFIX_LIF);
                return;
            }
            final String plain = location.getExtraInfo(MIME_TEXT_PLAIN);
            if (plain != null) {
                storeExtraInfo(plain, POSTFIX_PLAIN);
                return;
            }
        }
    }

    /**
     * May stores extra info if it is allowed. Type may be {@#link POSTFIX_NMEA},
     * {@link #POSTFIX_LIF} or {@link #POSTFIX_PLAIN}.
     * @param extraInfo the extra info to store
     * @param type the type of the extra info
     */
    void storeExtraInfo(final String extraInfo, final String type) {
        if (!stillTried) {
            this.stillTried = true;
            LOG.info("Build-in GPS extra info of type \"" +  type + "\"");
            if (storeExtraInfoAllowed()) {
                LOG.config("Logging allowed");
                if (Environment.storageSupported()) {
                    final String name = PREFIX_GPS + DateSupport.currentTimeAsCalendar()
                                        + DOT + type;
                    final StorageManagerFactory factory = StorageManagerFactory.getInstance();
                    final StorageManager manager = factory.getManager();
                    try {
                        final OutputStream out = manager.newOutputStream(name);
                        final OutputStream buffered = new BufferedOutputStream(out);
                        this.extraInfoWriter = new OutputStreamWriter(buffered);
                    } catch (final IOException e) {
                        LOG.warn("IOException: " + e.getMessage());
                    }
                }
            }
        }
        if (this.extraInfoWriter != null) {
            try {
                this.extraInfoWriter.write(extraInfo);
                this.extraInfoWriter.write(NEWLINE);
            } catch (final IOException e) {
                // Ignore it cause the data are not relevant, only for debugging yet.
            }
        }
    }

    /**
     * Checks if it is allowed to store extra info.
     * @return {@code true} if it is allowed to store the extra info. {@code false} otherwise.
     */
    boolean storeExtraInfoAllowed() {
        final ConfigManager configMgr = ConfigManager.getInstance();
        final String allowed = configMgr.get(ConfigurationConstants.GPS_STORE_NMEA_KEY,
                                             ConfigurationConstants.GPS_STORE_NMEA_VALUE_NO);
        LOG.info("GPS extra info allowed: " + allowed);
        return ConfigurationConstants.GPS_STORE_NMEA_VALUE_NO.equalsIgnoreCase(allowed)
                ? false : true;
    }

    static final class CoordinateImpl implements Coordinate {

        public float alt;
        public double lon;
        public double lat;
        public float course;
        public long time;
        public float speed;
        public float verticalAccuracy;
        public float horiziontalAccuracy;
        public double distance = 0.0d;

        public float getAlitude() {
            return this.alt;
        }

        public float getCourse() {
            return this.course;
        }

        public double getLatitude() {
            return this.lat;
        }

        public double getLongitude() {
            return this.lon;
        }

        public float getSpeed() {
            return this.speed;
        }

        public long getTime() {
            return this.time;
        }

        public float getHorizontalAccuracy() {
            return this.horiziontalAccuracy;
        }

        public float getVerticalAccuracy() {
            return this.verticalAccuracy;
        }

        public double getDistance() {
            return this.distance == Double.NaN ? 0.0d : this.distance;
        }

//        public String toString() {
//            final StringBuilder sb = new StringBuilder("CoordinateImpl[longitude=");
//
//            sb.append(this.lon);
//            sb.append("][latitude=");
//            sb.append(this.lat);
//            sb.append("][altitude=");
//            sb.append(this.alt);
//            sb.append("][course=");
//            sb.append(this.course);
//            sb.append("][time=");
//            sb.append(this.time);
//            sb.append("][speed=");
//            sb.append(this.speed);
//            sb.append("][distance=");
//            sb.append(this.distance);
//            sb.append("][horiziontal_accuracy=");
//            sb.append(this.horiziontalAccuracy);
//            sb.append("][vertical_accuracy=");
//            sb.append(this.verticalAccuracy);
//            sb.append("]]");
//
//            return sb.toString();
//        }
    }
}
