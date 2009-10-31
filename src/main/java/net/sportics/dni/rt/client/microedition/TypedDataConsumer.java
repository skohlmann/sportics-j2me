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
 * Implementing interfaces must handle new data from a {@link TypedDataProducer}.
 * @author Sascha Kohlmann
 *
 */
public interface TypedDataConsumer {

    /**
     * Handle one new typed datum.
     * @param source the source object which produces the data
     * @param type the new data. Should not be {@code null}.
     */
    void newData(final Object source, final TypedData type);

    /**
     * Handles a bulk of new typed datums.
     * @param source the source object which produces the datums
     * @param types a bulk array of new data . Should not be {@code null}.
     */
    void newData(final Object source, final TypedData[] types);
}
