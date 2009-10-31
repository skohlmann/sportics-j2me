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
 * Implementing instances produces new {@link TypedData} and will delegate the data
 * to regirstered {@link TypedDataConsumer}s. Implementing classes should never
 * promote {@code null} references.
 *
 * @author Sascha Kohlmann
 *
 */
public interface TypedDataProducer {

    /**
     * Registers an {@link TypedDataConsumer}.
     * If the value is {@link null} the implementing classes must ignore the value.
     * @param consumer the consumer to register
     */
    void registerTypedDataConsumer(final TypedDataConsumer consumer);

    /**
     * Unregisters an {@link TypedDataConsumer}.
     * If the value is {@link null} the implementing classes must ignore the value.
     * @param consumer the consumer to unregister
     */
    void unregisterTypedDataConsumer(final TypedDataConsumer consumer);

}
