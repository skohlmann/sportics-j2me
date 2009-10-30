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

import java.io.IOException;
import java.io.OutputStream;

import javax.microedition.midlet.MIDlet;

/**
 * Yet a simple interface to store data. Before you can use the storage first time,
 * call {@link #setup(MIDlet, boolean)} to init the storage system.
 *
 * @author Sascha Kohlmann
 *
 */
public interface StorageManager {

    /** The configuration key bind to the value of the root directory where to
     * find the Sportics data.
     */
    String STORE_CONFIG_NAME = "StorageManager.store";

    /**
     * Setsups the storage system.
     * @param source the source MIDlet to get environment information from
     * @param redo if {@code true} the implementationmust redo the storage configuration
     * @throws IOException if an I/O problem occurs during setup
     */
    void setup(final MIDlet source, final boolean redo) throws IOException;

    /**
     * Returns a new outputstream for the given name. If there is a file of the
     * given name, it may be overwritten. Don't use forbidden characters which are
     * environment dependend.
     * @param filename the name of the file to create
     * @return an open output stream
     * @throws IOException if an erroroccurs
     */
    OutputStream newOutputStream(final String filename) throws IOException;
}
