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
package net.sportics.dni.rt.client.microedition.accu;


/**
 * Implementations should handle the supplied assemblage. Implementations should also
 * tajke care not to say a long time in the implementing method.
 *
 * @author Sascha Kohlmann
 *
 */
public interface Sink {

    /**
     * Should handle the supplied assemblage.
     * @param toSink assemblage to sink
     */
    void sink(final Assemblage toSink);
}
