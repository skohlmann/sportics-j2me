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

import java.io.UnsupportedEncodingException;

import net.sportics.dni.rt.client.microedition.SporticsException;

public class UrlBuilder {

    private static final String UTF8 = "UTF-8";
    private StringBuffer sb;
    private boolean hasParameters;

    public static final UrlBuilder newInstance(final String base) {
        return new UrlBuilder(base);
    }

    UrlBuilder(final String base) {
        if (base == null) {
            throw new IllegalArgumentException("base is null");
        }
//        try {
//            final String encodedBase = UrlEncoder.encode(base, "UTF-8");
//            this.sb = new StringBuffer(encodedBase);
            this.sb = new StringBuffer(base);
//        } catch (final UnsupportedEncodingException e) {
//            throw new SporticsException("UTF-8 is not supported");
//        }
        this.hasParameters = false;
    } 

    public void addParameter(final String name, final String value) {
        if (name == null) {
            return;
        }
        final String theValue;
        if (value == null) {
            theValue = "";
        } else {
            theValue = value;
        }
        // Append a separator.
        if (hasParameters == false) {
            this.sb.append('?');
            this.hasParameters = true;
        } else {
            this.sb.append('&');
        }   
        try {
            final String encodedName = UrlEncoder.encode(name, UTF8);
            this.sb.append(encodedName);
            this.sb.append('=');
            final String encodedValue = UrlEncoder.encode(theValue, UTF8);
            this.sb.append(encodedValue);
        } catch (final UnsupportedEncodingException e) {
            throw new SporticsException("UTF-8 is not supported");
        }
    }

    public String build() { 
        return sb.toString();
    } 
}
