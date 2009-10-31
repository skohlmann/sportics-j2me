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
package net.sportics.dni.rt.client.microedition.net;

import net.sportics.dni.rt.client.microedition.SporticsException;

public class SrtsApiException extends SporticsException {

    public final static int ERRORCODE_NO_LOCATION_HEADER = 1;
    public final static int ERRORCODE_UNEXPECTED_SERVER_RESPONSE = 2;
    public final static int ERRORCODE_UNKNOWN_IO_EXCEPTION = 3;
    public final static int ERRORCODE_SECURITYEXCEPTION = 4;

    private final int ec;

    public final int getErrorCode() {
        return this.ec;
    }

    public SrtsApiException(final int errorCode) {
        super();
        this.ec = errorCode;
    }

    public SrtsApiException(final int errorCode, final String s) {
        super(s);
        this.ec = errorCode;
    }
}
