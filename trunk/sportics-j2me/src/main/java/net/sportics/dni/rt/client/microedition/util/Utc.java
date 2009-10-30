/* Copyright (C) 2009 Sascha Kohlmann
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
package net.sportics.dni.rt.client.microedition.util;

import java.util.TimeZone;

/**
 * Support for <a href='http://en.wikipedia.org/wiki/Coordinated_Universal_Time'>Coordinated
 * Universal Time</a>.
 * @author Sascha Kohlmann
 */
public class Utc {

    /**
     * Returns the UTC value as requiered in
     * <a href='http://carbonintranet.de/wiki/index.php/Projekte/sportics/technik/schnittstellen/rt/rest-apii#.7Buwiid.7D.2Fstart>Sportics
     * DNI RT</a> specification.
     * @return ever starts with <tt>UTC</tt>. Never {@code null}
     */
    public String utc() {
        final int offset = getRawOffset();
        if (offset == 0) {
            return "UTC0";
        }
        final int minutes = offset / 1000 / 60;
        final int minutesOfHour = minutes % 60;
        final int hours = minutes / 60;
        final StringBuffer sb = new StringBuffer("UTC");
        if (hours > 0) {
            sb.append("+");
        }
        if (hours < 10 && hours > -10) {
            sb.append("0");
        }
        sb.append(hours);
        if (minutesOfHour != 0) {
            final int workMinutes = Math.abs(minutesOfHour);
            sb.append(":");
            if (workMinutes < 10) {
                sb.append("0");
            }
            sb.append(workMinutes);
        }
        return sb.toString();
    }

    int getRawOffset() {
        final TimeZone tz = TimeZone.getDefault();
        return tz.getRawOffset();
    }
}
