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


import net.sportics.dni.rt.client.microedition.LocationManager;
import net.sportics.dni.rt.client.microedition.LocationManagerFactory;
import net.sportics.dni.rt.client.microedition.TypedData;
import net.sportics.dni.rt.client.microedition.TypedDataDescriptor;
import net.sportics.dni.rt.client.microedition.Variant;
import net.sportics.dni.rt.client.microedition.LocationManager.Coordinate;
import net.sportics.dni.rt.client.microedition.LocationManager.CoordinateListener;
import net.sportics.dni.rt.client.microedition.Pauseable;
import net.sportics.dni.rt.client.microedition.controller.ConfigureController;
import net.sportics.dni.rt.client.microedition.util.LogManager;


/**
 * Supplies position and speed information based on the JSR 179 API.
 * <p>May supress position information if and only if suppling of position information
 * in not allowed by the user. In that case the position information will be {@code null}.</p>
 * @author Sascha Kohlmann
 * @see ConfigureController#currentPositionTrackingDecision()
 */
public class Jsr179LocationDevice extends AbstractDevice implements Pauseable {

    private static final LogManager LOG = LogManager.getInstance("Jsr179LocationDevice");
    static {
        LOG.debug("#class: " + Jsr179LocationDevice.class.getName());
    }

    private static final LocationManager LOCATION_MANAGER;
    static {
        final LocationManagerFactory factory = LocationManagerFactory.getInstance();
        if (factory.isGpsSupported()) {
            LOCATION_MANAGER = factory.getManager();
        } else {
            LOCATION_MANAGER = null;
        }
    }

    private float distance = 0.0f;
    private boolean pause = false;

    private CoordinateListener listener;
    boolean positionTrackingAllowed = false;

    protected void doStart() {
        LOG.debug("doStart()");
        setupPositionTrackingAllowed();
        this.distance = 0.0f;
    }

        void setupPositionTrackingAllowed() {
        this.positionTrackingAllowed = ConfigureController.currentPositionTrackingDecision();
    }

    protected void doPrepare() {
        LOG.debug("doPrepare()");
        setupPositionTrackingAllowed();
        if (LOCATION_MANAGER != null) {
            this.listener = new CoordinateListener() {
                public void onCoordinateUpdated(final Coordinate coordinate) {
                    LOG.info("got coordinates: " + coordinate);
                    if (currentState() == STATE_STARTED) {
                        final double longitude = coordinate.getLongitude();
                        final double latitude = coordinate.getLatitude();
                        // Make no sense if the coordinates are unavailable
                        if (longitude != Double.NaN && latitude != Double.NaN) {
                            // Do a lot of stupid copy work.
                            // TODO: May change TypeDataCollector interface to perform
                            //       a TypedDataContainer object.
                            final long time = coordinate.getTime();
                            final TypedData timeType = new TypedData(TypedDataDescriptor.TIMESTAMP,
                                                                     new Variant(time));

                            final float speed = getSpeed(coordinate);
                            final TypedData speedType =
                                new TypedData(TypedDataDescriptor.SPEED, speed);
                            final double distance = coordinate.getDistance();
                            if (!pause) {
                                Jsr179LocationDevice.this.distance += distance;
                            }
                            final TypedData distanceType =
                                new TypedData(TypedDataDescriptor.DISTANCE,
                                              Jsr179LocationDevice.this.distance);

                            final TypedData paceType = calculatePace(speed);

                            final float horizontalAccuracy = coordinate.getHorizontalAccuracy();
                            final float verticalAccuracy = coordinate.getVerticalAccuracy();
                            final TypedData horizontalAccuracyType =
                                handleHorizontalAccuracyType(horizontalAccuracy);
                            final TypedData verticalAccuracyType =
                                handleVerticalAccuracyType(verticalAccuracy);
                            final TypedData longitudeType = handleLongitude(longitude);
                            final TypedData latitudeType = handleLatitude(latitude);
                            final float altitude = coordinate.getAlitude();
                            final TypedData altitudeType = handleAltitude(altitude);

                            TypedData[] types = null;
                            if (paceType == null) {
                                types = new TypedData[] {longitudeType,
                                                         latitudeType,
                                                         altitudeType,
                                                         timeType,
                                                         horizontalAccuracyType,
                                                         verticalAccuracyType,
                                                         speedType,
                                                         distanceType};
                            } else {
                                types = new TypedData[] {longitudeType,
                                                         latitudeType,
                                                         altitudeType,
                                                         timeType,
                                                         paceType,
                                                         horizontalAccuracyType,
                                                         verticalAccuracyType,
                                                         speedType,
                                                         distanceType};
                            }

                            LOG.debug("fire coordinates to accumulator");
                            newData(types);
                        }
                    }
                }

                private float getSpeed(final Coordinate coordinate) {
                    final float speed = coordinate.getSpeed();
                    if (Float.isNaN(speed)) {
                        return 0.0f;
                    }
                    return speed;
                }

                final TypedData calculatePace(final float speed) {
                    if (speed > 0.0f) {
                        try {
                            final float pace = 1 / speed;
                            return new TypedData(TypedDataDescriptor.PACE, pace);
                        } catch(final ArithmeticException e) {
                            // ignore and use Float.POSITIVE_INFINITY
                            LOG.warn("calculatePace(): " + e.getMessage());
                        }
                    }
                    return new TypedData(TypedDataDescriptor.PACE, Float.MAX_VALUE);
                }

                public void onStateChange(final int newState) {
                }

                private TypedData handleAltitude(final float altitude) {
                    if (Jsr179LocationDevice.this.positionTrackingAllowed) {
                        final TypedData altitudeType =
                                new TypedData(TypedDataDescriptor.ALTITUDE, new Variant(altitude));
                        return altitudeType;
                    }
                    return null;
                }

                private TypedData handleHorizontalAccuracyType(final float horizontalAccuracy) {
                    if (Jsr179LocationDevice.this.positionTrackingAllowed) {
                        final TypedData horizontalAccuracyType =
                                new TypedData(TypedDataDescriptor.HORIZONTAL_ACCURACY,
                                              new Variant(horizontalAccuracy));
                        return horizontalAccuracyType;
                    }
                    return null;
                }

                private TypedData handleLatitude(final double latitude) {
                    if (Jsr179LocationDevice.this.positionTrackingAllowed) {
                        final TypedData latitudeType =
                                new TypedData(TypedDataDescriptor.LATITUDE, new Variant(latitude));
                        return latitudeType;
                    }
                    return null;
                }

                private TypedData handleLongitude(final double longitude) {
                    if (Jsr179LocationDevice.this.positionTrackingAllowed) {
                        final TypedData longitudeType =
                                new TypedData(TypedDataDescriptor.LONGITUDE, new Variant(longitude));
                        return longitudeType;
                    }
                    return null;
                }

                private TypedData handleVerticalAccuracyType(final float verticalAccuracy) {
                    if (Jsr179LocationDevice.this.positionTrackingAllowed) {
                        final TypedData verticalAccuracyType =
                                new TypedData(TypedDataDescriptor.VERTICAL_ACCURACY,
                                              new Variant(verticalAccuracy));
                        return verticalAccuracyType;
                    }
                    return null;
                }
            };
            LOG.debug("doPrepare() finish: " + this.listener);
            LOCATION_MANAGER.addCoordinateListener(this.listener);
        }
    }

    public String getDeviceName() {
        return "GPS Build-in";
    }

    public String[] supportedTypedDataList() {
        return new String[] {TypedDataDescriptor.LONGITUDE,
                             TypedDataDescriptor.LATITUDE,
                             TypedDataDescriptor.HORIZONTAL_ACCURACY,
                             TypedDataDescriptor.VERTICAL_ACCURACY,
                             TypedDataDescriptor.SPEED,
                             TypedDataDescriptor.PACE,
                             TypedDataDescriptor.DISTANCE,
                             TypedDataDescriptor.ALTITUDE};
    }

    public void pause() {
        setState(STATE_DO_PAUSE);
        fireLifecycleEvent(STATE_DO_PAUSE);

        this.pause = true;

        setState(STATE_PAUSED);
        fireLifecycleEvent(STATE_PAUSED);
    }

    public void keepOn() {
        setState(STATE_DO_RESTART);
        fireLifecycleEvent(STATE_DO_RESTART);

        setupPositionTrackingAllowed();
        this.pause = false;

        setState(STATE_RESTARTED);
        fireLifecycleEvent(STATE_RESTARTED);
    }
}
