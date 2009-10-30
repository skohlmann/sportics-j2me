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
 * Base exception for all application exceptions.
 * @author Sascha Kohlmann
 *
 */
public class SporticsException extends RuntimeException {

    /** Constructs a new example. */
    public SporticsException() {
        super();
    }

    /**
     * Constructs a new example with the describing message.
     * @param message the message
     */
    public SporticsException(String s) {
        super(s);
    }
}