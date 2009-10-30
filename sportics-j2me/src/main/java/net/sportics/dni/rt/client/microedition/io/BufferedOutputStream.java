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
package net.sportics.dni.rt.client.microedition.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * The semantic is nearly like the {@link java.io.BufferedOutputStream}. The difference
 * is in the semantic of how to flush the buffer. This implementation flushes also the
 * {@link #inner} stream.
 *
 * @author Sascha Kohlmann
 *
 */
public class BufferedOutputStream extends FilterOutputStream {

    private final byte buffer[];
    private int count;

    /** Constructs a new buffered stream with a default buffer size.
     * @param out the underlying output stream */
    public BufferedOutputStream(final OutputStream out) {
        this(out, 512);
   }

    /** Constructs a new buffered stream with the supplied buffer size.
     * @param out the underlying output stream
     * @param bufferSize the buffer size
     * @throws IllegalArgumentException if and only if {@code bufferSize &lt; 1} */
    public BufferedOutputStream(final OutputStream out, final int bufferSize) {
        super(out);
        if (bufferSize < 1) {
            throw new IllegalArgumentException("bufferSize < 1");
        }
        this.buffer = new byte[bufferSize];
    }

    public void write(final int b) throws IOException {
        synchronized(this) {
            if (count >= buffer.length) {
                flush();
            }
            buffer[count++] = (byte) b;
        }
    }

    public void write(final byte b[], final int offset, final int length) throws IOException {
        synchronized(this) {
            if (length >= buffer.length) {
                flush();
                this.out.write(b, offset, length);
                return;
            }
            if (length > buffer.length - count) {
                flush();
            }
            System.arraycopy(b, offset, buffer, count, length);
            count += length;
        }
    }

    public void flush() throws IOException {
        synchronized(this) {
            if (count > 0) {
                this.out.write(buffer, 0, count);
                count = 0;
            }
            this.out.flush();
        }
    }
}
