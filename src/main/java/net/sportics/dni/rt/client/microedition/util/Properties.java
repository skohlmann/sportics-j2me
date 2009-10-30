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

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

public class Properties extends Hashtable {

    private static final char SEPARATOR = '=';
    private static final char COMMENT = '#';
    private static final char LF = '\n';
    private static final char CR = '\r';
    private static final int BUFFER_LENGTH = 256;

    public Properties() {
        super();
    }

    public String getProperty(final String key) {
        return (String) get(key);
    }

    public void setProperty(final String key, final String value) {
        put(key, value);
    }

    public void load(final InputStream in) throws IOException {
        load( in, null);
    }

    public void load(final InputStream in, final String encoding) throws IOException {

        byte[] buffer = new byte[BUFFER_LENGTH];
        int read;
        int start = 0;
        int end = 0;
        boolean newLineFound;

        while ((read = in.read(buffer, start, BUFFER_LENGTH - start)) != -1) {
            final String line;
            if (encoding != null) {
                line = new String(buffer, 0, read + start, encoding);
            } else {
                line = new String(buffer, 0, read + start);
            }
            start = 0;
            newLineFound = true;
            while (newLineFound) {
                newLineFound = false;
//                char c = LF;
                for (int i = start; i < line.length(); i++) {
                    final char c = line.charAt(i);
                    if (c == CR || c == LF) {
                        end = i;
                        newLineFound = true;
                        break;
                    }
                }
                if (newLineFound) {
                    if (line.indexOf(COMMENT, start) != start) { // this is not a comment
                        final int splitPos = line.indexOf(SEPARATOR, start);
                        if(splitPos == -1) {
                            final String l = line.substring(start, end);
                            final String trimmed = l.trim();
                            if (trimmed.length() != 0) {
                                throw new IOException("no " + SEPARATOR + " separator: " + l);
                            }
                            continue;
                        }
                        final String key = line.substring(start, splitPos);
                        final String value = line.substring(splitPos + 1, end );
                        put(key, value);
                    }
//                    if (c == CR) {
//                        start = end + 2;
//                    } else {
                        start = end + 1;
//                    }
                }
            }
            // now all key-value pairs have been read, now move any remaining data
            // to the beginning of the buffer:
            if (start < read) {
                System.arraycopy(buffer, start, buffer, 0, read - start);
                start = read - start;
            } else {
                start = 0;
            }
        }
    }

    /**
     * Read a line from the input, with newlines stripped. Consecutive newlines are
     * ignored. null is returned when we attempt to read beyond the end of the stream.
     * 
     * @param input
     * @return
     * @throws IOException
     */
    final static String readLine(InputStream input) throws IOException {
        // ??: StringBuffer is slow... use something faster
        StringBuffer builder = new StringBuffer(80);

        int i;
        while ((i = input.read()) != -1) {
            final char c = (char) i;
            if ((c == '\n') || (c == '\r')) {
                // end of line
                if (builder.length() == 0) {
                    // newline at start of line, ignore
                    continue;
                }
                return builder.toString();
            }
            builder.append(c);
        }
        if (builder.length() > 0) {
            return builder.toString();
        }
        return null;
    }
}
