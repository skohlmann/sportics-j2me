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
package net.sportics.dni.rt.client.microedition.util;

import java.util.Calendar;

public final class DateSupport {

    private static final String COLON = ":";
    private static final String ZERO = "0";
    private static final int HOUR_OR_MINUTE_VALUE = 60;
    private static final int TEN_DEVIDER = 10;

    public static String currentTimeAsCalendar() {
        return timeAsCalendar(Calendar.getInstance());
    }

    public static String millisecondsToTime(final long millis, final boolean withoutMillis) {
        final long seconds = millis / 1000;
        final String time = secondsToTime(seconds);
        if (!withoutMillis) {
            final long milliPart = millis % 1000;
            return time + "." + milliPart;
        }
        return time;
    }

    public static String secondsToTime(final long s) {
        final long internal = s;
        final StringBuffer sb = new StringBuffer();
        final long hours = internal / (HOUR_OR_MINUTE_VALUE * HOUR_OR_MINUTE_VALUE);
        final long hoursMinus = hours * HOUR_OR_MINUTE_VALUE * HOUR_OR_MINUTE_VALUE;
        if (hours < TEN_DEVIDER) {
            sb.append(ZERO);
        }
        sb.append(hours);
        sb.append(COLON);

        final long forMinutes = internal - hoursMinus;
        final long minutes = forMinutes / HOUR_OR_MINUTE_VALUE;
        final long minutesMinus = minutes * HOUR_OR_MINUTE_VALUE;
        if (minutes < TEN_DEVIDER) {
            sb.append(ZERO);
        }
        sb.append(minutes);
        sb.append(COLON);

        final long seconds = forMinutes - minutesMinus;
        if (seconds < TEN_DEVIDER) {
            sb.append(ZERO);
        }
        sb.append(seconds);

        return sb.toString();
    }

    public static String timeAsCalendar(final Calendar cal) {

        if (cal == null) {
            throw new IllegalArgumentException("cal is null");
        }
        final Calendar datetime = cal;
        final int year = datetime.get(Calendar.YEAR);
        final int month = datetime.get(Calendar.MONTH) + 1;
        final int day = datetime.get(Calendar.DAY_OF_MONTH);
        final int hour = datetime.get(Calendar.HOUR_OF_DAY);
        final int minute = datetime.get(Calendar.MINUTE);
        final int second = datetime.get(Calendar.SECOND);
        final int milliSecond = datetime.get(Calendar.MILLISECOND);

        final StringBuffer sb = new StringBuffer();
        sb.append(year);
        if (month < TEN_DEVIDER) {
            sb.append(ZERO);
        }
        sb.append(month);
        if (day < TEN_DEVIDER) {
            sb.append(ZERO);
        }
        sb.append(day);
        if (hour < TEN_DEVIDER) {
            sb.append(ZERO);
        }
        sb.append("T");
        sb.append(hour);
        if (minute < TEN_DEVIDER) {
            sb.append(ZERO);
        }
        sb.append(minute);
        if (second < TEN_DEVIDER) {
            sb.append(ZERO);
        }
        sb.append(second);
        if (milliSecond < (TEN_DEVIDER * TEN_DEVIDER)) {
            sb.append(ZERO);
            if (milliSecond < TEN_DEVIDER) {
                sb.append(ZERO);
            }
        }
        sb.append(milliSecond);

        return sb.toString();
    }
}
