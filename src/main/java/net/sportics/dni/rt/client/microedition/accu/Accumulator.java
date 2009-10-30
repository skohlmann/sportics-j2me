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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import net.sportics.dni.rt.client.microedition.TypedData;
import net.sportics.dni.rt.client.microedition.TypedDataConsumer;
import net.sportics.dni.rt.client.microedition.TypedDataDescriptor;
import net.sportics.dni.rt.client.microedition.Variant;
import net.sportics.dni.rt.client.microedition.util.CopyHashtable;
import net.sportics.dni.rt.client.microedition.util.LogManager;


/**
 * The class represents a typed data collector/consumer. It collects the data for a period
 * of time from the {@link TypedDataProducer} where it is registered. After that period of
 * time, the accumulator propagates the data to registerd {@link Sink Sinks}.
 *
 * <p>Get the instance of the accu with {@link #getInstance()}.</p>
 *
 * @author Sascha Kohlmann
 */
public final class Accumulator implements TypedDataConsumer {

    private static final LogManager LOG = LogManager.getInstance("Accumulator");
    static {
        LOG.debug("#class: " + Accumulator.class.getName());
    }

    static final String TEST_DESCRIPTOR = "#test";

    final Hashtable sinks = new Hashtable();
    private volatile Assemblage current;
//    private volatile long currentTime = -1;

    private Timer timer;
    private long delay = 1050;

    private static Accumulator INSTANCE = new Accumulator();

    /**
     * There is only one instance of the accu in the system.
     * @return the instance of the accu
     */
    public static Accumulator getInstance() {
        return INSTANCE;
    }

    private Accumulator() {
        this.current = new Assemblage();
    }

    /**
     * Registers the supplied {@code Sink} for propagating the collected {@link TypedData}.
     * @param sink the {@code Sink} to register
     */
    public void registerSink(final Sink sink) {
        if (sink != null) {
            synchronized(this) {
                this.sinks.put(sink, sink);
                LOG.debug("sink registered: " + sink.getClass().getName());
                if (timer == null) {
                    this.timer = new Timer();
                    this.timer.scheduleAtFixedRate(new TimerTask() {
                        public void run() {
                            final long time = System.currentTimeMillis();
                            LOG.debug("run timer at " + time);
                            accumulate(this, TypedDataDescriptor.TIMESTAMP, new Variant(time));
                        }
                    }, this.delay, this.delay);
                    LOG.debug("time init done");
                }
            }
        }
    }

    /**
     * Unregisters the supplied {@code Sink} from the list of {@code Sink Sinks}.
     * @param sink the {@code Sink} to unregister
     */
    public void unregisterSink(final Sink sink) {
        if (sink != null) {
            synchronized(this) {
                this.sinks.remove(sink);
                if (this.sinks.size() == 0) {
                    if (this.timer != null) {
                        this.timer.cancel();
                        this.timer = null;
                    }
                }
            }
        }
    }

    final void accumulate(final Object source, final String type, final Variant v) {
        final TypedData t = new TypedData(type, v);
        newData(source, t);
    }

    public void newData(final Object source, final TypedData type) {
        if (type == null) {
            return;
        }
        final TypedData[] t = new TypedData[] {type};
        newData(source, t);
    }

    public void newData(final Object source, final TypedData[] types) {
        if (types != null) {
            final TypedData timestamp = timestamp(types);

            synchronized(this) {
                if (timestamp != null && source instanceof TimerTask) {
                    LOG.debug("accumulate comes from TimerTask");
                    final Assemblage assemble = this.current;
                    this.current = new Assemblage();
                    if (!assemble.contains(timestamp.getDescriptor())) {
                        assemble.put(timestamp);
                    }
//                    this.currentTime = timestamp.getValue().asLong();

                    final Hashtable copy = new CopyHashtable(this.sinks);
                    for (final Enumeration e = copy.keys(); e.hasMoreElements(); ) {
                        final Sink sink = (Sink) e.nextElement();
                        try {
                            sink.sink(assemble);
                        } catch (final Exception ex) {
                            LOG.warn("Exception "+ ex.getClass().getName() + ": \""
                                     + ex.getMessage() + "\" in Sink: " + sink.getClass());
                        }
                    }
                } else {
                    for (int i = 0; i < types.length; i++) {
                        if (types[i] != null) {
                            LOG.debug("assemble: " + types[i]);
                            this.current.put(types[i]);
                        }
                    }
                }
            }
        }
    }

    final TypedData timestamp(final TypedData[] types) {
        for (int i = 0; i < types.length; i++) {
            if (types[i] != null) {
                final String descriptor = types[i].getDescriptor();
                if (TypedDataDescriptor.TIMESTAMP.equals(descriptor)) {
                    return types[i];
                }
            }
        }
        return null;
    }
}
