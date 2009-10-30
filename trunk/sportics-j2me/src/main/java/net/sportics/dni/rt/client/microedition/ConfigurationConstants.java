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

public final class ConfigurationConstants {

    private ConfigurationConstants() {
    }

    public static final String POSITION_ALLOWED_KEY = "gps.allowed";
    public static final String POSITION_ALLOWED_VALUE_YES = Boolean.TRUE.toString();
    public static final String POSITION_ALLOWED_VALUE_NO = Boolean.FALSE.toString();

    public static final String GPS_STORE_NMEA_KEY = "gps.store.nmea";
    public static final String GPS_STORE_NMEA_VALUE_YES = Boolean.TRUE.toString();
    public static final String GPS_STORE_NMEA_VALUE_NO = Boolean.FALSE.toString();
}
