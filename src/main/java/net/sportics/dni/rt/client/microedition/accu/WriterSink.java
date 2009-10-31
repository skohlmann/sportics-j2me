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

import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;

import net.sportics.dni.rt.client.microedition.TypedData;
import net.sportics.dni.rt.client.microedition.Variant;
import net.sportics.dni.rt.client.microedition.util.LogManager;

/**
 * Simple writes all data to the supplied {@code Writer}. For each assemblage the
 * writer creates one line, followed be a newline character. The data are formatted
 * in the form of {@link TypedDataDescritor}, followed by a colon sign ({@code ASCII 0x3A}),
 * followed by the value of the {@code TypedData} {@linkplain Variant#asString() as string},
 * followed by a vertical line ({@code ASCII 0x7C}).
 * @author Sascha Kohlmann
 */
public class WriterSink implements Sink {

    private static final LogManager LOG = LogManager.getInstance("WriterSink");
    static {
        LOG.debug("#class: " + WriterSink.class.getName());
    }

    private static final String TUPLE_DELIMITER = "|";
    private static final String VALUE_DELIMITER = ":";
    private static final String NEWLINE = System.getProperty("line.separator");

    private final Writer out;

    /**
     * Constructs a new sink with the supplied {@code Writer}.
     * @param out the writer to write the data thru
     * @throws IllegalArgumentException if and only if the supplied data is {@code null}.
     */
    public WriterSink(final Writer out) {
        if (out == null) {
            throw new IllegalArgumentException();
        }
        this.out = out;
    }

    /**
     * Writes all data of the supplied sink to the writer.
     * Ignores {@link Writer}s may throwing {@link IOException}s.
     */
    public void sink(final Assemblage toSink) {
        if (toSink != null) {
            final StringBuffer sb = new StringBuffer();
            for (final Enumeration e = toSink.descriptors(); e.hasMoreElements(); ) {
                final String desc = (String) e.nextElement();
                if (toSink.contains(desc)) {
                    final TypedData td = toSink.get(desc);
                    final Variant v = td.getValue();
                    final String value = v.asString();
                    sb.append(desc);
                    sb.append(VALUE_DELIMITER);
                    sb.append(value);
                    sb.append(TUPLE_DELIMITER);
                }
            }
            sb.append(NEWLINE);
            try {
                this.out.write(sb.toString());
                this.out.flush();
            } catch (final IOException ex) {
                final String msg = ex.getMessage();
                LOG.warn("IOException: " + msg);
            }
        }
    }
}
