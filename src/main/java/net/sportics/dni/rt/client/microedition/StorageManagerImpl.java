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
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.midlet.MIDlet;

final class StorageManagerImpl implements StorageManager {

    /** The root name of the directory where to store the Sportics data. */
    String STORE_DIR_NAME = "Sportics.net";

    private static final String SLASH = "/";
    private static final String FILE_URL_PREFIX = "file:" + SLASH + SLASH + SLASH;

    public OutputStream newOutputStream(final String filename) throws IOException {
        if (filename == null) {
            throw new IllegalArgumentException();
        }

        final ConfigManager config = ConfigManager.getInstance();
        final String path = config.get(STORE_CONFIG_NAME);
        if (path != null) {
            final String url = path + SLASH + filename;
            final FileConnection con = (FileConnection) Connector.open(url);
            if (!con.exists()) {
                con.create();
                final OutputStream out = con.openOutputStream();
                return new ConnectionClosingOutputStream(con, out);
            } else {
                throw new IOException("file exists");
            }
        }
        throw new IllegalStateException("Filesystem not yes initialized");
    }

    public void setup(final MIDlet source, final boolean redo) throws IOException {

        final ConfigManager config = ConfigManager.getInstance();
        final String path = config.get(STORE_CONFIG_NAME);
        if (path != null) {
            return;
        }

        final Vector roots = new Vector();
        for (final Enumeration e = FileSystemRegistry.listRoots(); e.hasMoreElements(); ) {
            final Object o = e.nextElement();
            roots.addElement(o);
        }

        final int size = roots.size();
        final String[] root = new String[size];
        for (int i = 0; i < size; i++) {
            root[i] = (String) roots.elementAt(i);
            System.out.println("root: " + root[i]);
        }
        final List list = new List("Roots", List.EXCLUSIVE, root, null);

        final Command selectCommand = new Command("Select", Command.ITEM, 1);
        list.addCommand(selectCommand);
        list.setCommandListener(new CommandListener() {
            public void commandAction(final Command command, final Displayable display) {
                synchronized(roots) {
                    roots.notify();
                }
            }
        });

        final Display display = Display.getDisplay(source);
        display.setCurrent(list);
        synchronized(roots) {
            try {
                roots.wait();
            } catch (final InterruptedException e) {
            }
        }
        final int index = list.getSelectedIndex();
        final String selected = list.getString(index);
        final String newPath = FILE_URL_PREFIX + selected + STORE_DIR_NAME;
        try {
            final FileConnection fc = (FileConnection) Connector.open(newPath);
            if (!fc.exists()) {
                fc.mkdir();
            }
            config.set(STORE_CONFIG_NAME, newPath);
        } catch (final Exception e) {
            final String msg = e.getMessage() + " " + newPath;
            throw new IOException(msg);
        }
    }

    private static class ConnectionClosingOutputStream extends OutputStream {
        private final Connection con;
        private final OutputStream out;
        public ConnectionClosingOutputStream(final Connection con, final OutputStream out) {
            if (con == null || out == null) {
                throw new IllegalArgumentException();
            }
            this.con = con;
            this.out = out;
        }
        public void flush() throws IOException {
            this.out.flush();
        }
        public void close() throws IOException {
            this.con.close();
            this.out.close();
        }
        public void write(final int b) throws IOException {
            this.out.write(b);
        }
        public void write(final byte[] b) throws IOException {
            this.out.write(b);
        }
        public void write(final byte[] b, final int off, final int len) throws IOException {
            this.out.write(b, off, len);
        }
    }
}
