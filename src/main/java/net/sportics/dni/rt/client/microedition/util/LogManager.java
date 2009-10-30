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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import net.sportics.dni.rt.client.microedition.ConfigManager;
import net.sportics.dni.rt.client.microedition.StorageManager;
import net.sportics.dni.rt.client.microedition.StorageManagerFactory;
import net.sportics.dni.rt.client.microedition.io.BufferedOutputStream;

/**
 * The abstract manager is a factory for concrete implementations. The level
 * follows the rule of {@code java.util.logging}.
 *
 * <p>There are 5 defined log levels and the {@link #OFF} level. To set the
 * level use {@link #setLogLevel(int)}. If the setted level is {@code OFF}
 * the logger instances stop writing to a logging sink. The other log levels
 * support a more or less fine granularity. of log messages. The lowest log
 * level is {@link #TRACE}. If this level is setted all log messages are
 * populate to the sink. The highest log level is {@link #SEVERE}. Only
 * very important messages should use this level.</p>
 *
 * @author Sascha Kohlmann
 */
public abstract class LogManager {

    /** The configuration key of the log level. */
    public static final String LOG_LEVEL_KEY = "LogManager.level";

    private static final String DEBUG_SINK_KEY = 
        "net.sportics.dni.rt.client.microedition.remote.LogManager.debug.sink";

    /** Value for no logging. */
    public static final int OFF     = Integer.MIN_VALUE;
    /** Value for the finest logging level. */
    public static final int TRACE   = 1;
    /** Value for the debug logging level. */
    public static final int DEBUG   = 2;
    /** Value for the config logging level. */
    public static final int CONFIG  = 3;
    /** Value for the info logging level. */
    public static final int INFO    = 4;
    /** Value for the warning logging level. */
    public static final int WARNING = 5;
    /** Value for the severe logging level. */
    public static final int SEVERE  = 6;

    /** For internal use only. Never reference outside without permission. */
    public static final String OFF_STR = "OFF";
    /** For internal use only. Never reference outside without permission. */
    public static final String TRACE_STR = "TRACE";
    /** For internal use only. Never reference outside without permission. */
    public static final String DEBUG_STR = "DEBUG";
    /** For internal use only. Never reference outside without permission. */
    public static final String CONFIG_STR = "CONFIG";
    /** For internal use only. Never reference outside without permission. */
    public static final String INFO_STR = "INFO";
    /** For internal use only. Never reference outside without permission. */
    public static final String WARNING_STR = "WARNING";
    /** For internal use only. Never reference outside without permission. */
    public static final String SEVERE_STR = "SEVERE";

    private static final String LOG_FILE_PREFIX = "L";
    private static final String LOG_FILE_POSTFIX = ".log";
    private static final String SPACE = " ";

    private static final int BUFFER_SIZE = 1024;

    private static int gLevel = OFF;
    private static boolean open = false;
    private static Writer writer = null;

    /** Only one instance useful. */
    static {
        final String value = System.getProperty(DEBUG_SINK_KEY);
        final boolean debug = Boolean.TRUE.toString().equalsIgnoreCase(value);
        if (debug) {
            setLogLevel(DEBUG, true);
        } else {
            final ConfigManager mgr = ConfigManager.getInstance();
            final String logLevel = mgr.get(LOG_LEVEL_KEY, OFF_STR);
            final int lLevel = getLogLevelForString(logLevel);
            setLogLevel(lLevel);
        }
    }

    /**
     * Creates a new instance for the given name. The name should be a classname.
     * @param type the name of the logger.
     * @return a new logger.
     */
    public static final LogManager getInstance(final String type) {
        return new LogManager() {
            public String getType() {
                return type;
            }
        };
    }

    abstract String getType();

    /** Logs the level {@code finer}.
     * @param message the message to log */
    public void trace(final Object message) {
        log(TRACE, message);
    }

    /** Logs the level {@code debug}.
     * @param message the message to log */
    public void debug(final Object message) {
        log(DEBUG, message);
    }

    /** Logs the level {@code config}.
     * @param message the message to log */
    public void config(final Object message) {
        log(CONFIG, message);
    }

    /** Logs the level {@code info}.
     * @param message the message to log */
    public void info(final Object message) {
        log(INFO, message);
    }

    /** Logs the level {@code warn}.
     * @param message the message to log */
    public void warn(final Object message) {
        log(WARNING, message);
    }

    /** Logs the level {@code severe}.
     * @param message the message to log */
    public void severe(final Object message) {
        log(SEVERE, message);
    }

    public void log(final int level, final Object message) {
        if (open && message != null) {
            if (level >= gLevel && writer != null) {
                final StringBuffer sb = new StringBuffer();
                final String time = DateSupport.currentTimeAsCalendar();
                sb.append(time);
                sb.append(SPACE);
                final Thread current = Thread.currentThread();
                final String name = current.getName();
                sb.append(name);
                sb.append(SPACE);
                final String asString = getStringForLogLevel(level);
                sb.append(asString);
                sb.append(SPACE);
                final String type = getType();
                sb.append(type);
                sb.append(" - ");
                sb.append(SPACE);
                sb.append(message);
                sb.append("\n");

                final String out = sb.toString();
                try {
                    synchronized(LogManager.class) {
                        writer.write(out);
                        writer.flush();
                    }
                    writer.flush();
                } catch (final IOException e) {
                    // Ignore at this time of development
                }
            }
        }
    }

    /** For internal use only. Never reference outside without permission. */
    public static String getStringForLogLevel(final int level) {
        switch (level) {
            case OFF: return OFF_STR;
            case TRACE: return TRACE_STR;
            case DEBUG: return DEBUG_STR;
            case CONFIG: return CONFIG_STR;
            case INFO: return INFO_STR;
            case WARNING: return WARNING_STR;
            case SEVERE: return SEVERE_STR;
            default: return "<unknown>";
        }
    }

    /** For internal use only. Never reference outside without permission. */
    public static int getLogLevelForString(final String level) {
        if (level != null) {
            if (OFF_STR.equalsIgnoreCase(level)) {return OFF;}
            if (TRACE_STR.equalsIgnoreCase(level)) {return TRACE;}
            if (DEBUG_STR.equalsIgnoreCase(level)) {return DEBUG;}
            if (CONFIG_STR.equalsIgnoreCase(level)) {return CONFIG;}
            if (INFO_STR.equalsIgnoreCase(level)) {return INFO;}
            if (WARNING_STR.equalsIgnoreCase(level)) {return WARNING;}
            if (SEVERE_STR.equalsIgnoreCase(level)) {return SEVERE;}
        }
        return OFF;
    }

    private static void setLogLevel(final int level, final boolean debug) {
        if (level != OFF || debug) {
            synchronized(LogManager.class) {
                if (!open) {
                    if (debug) {
                        writer = new OutputStreamWriter(System.out);
                    } else {
                        final StorageManagerFactory factory = StorageManagerFactory.getInstance();
                        if (factory.isStorageSupported()) {
                            final StorageManager mgr = factory.getManager();
                            final String name = LOG_FILE_PREFIX + DateSupport.currentTimeAsCalendar()
                                                + LOG_FILE_POSTFIX;
                            try {
                                final OutputStream out = mgr.newOutputStream(name);
                                final OutputStream buffered =
                                    new BufferedOutputStream(out, BUFFER_SIZE);
                                writer = new OutputStreamWriter(buffered);
                            } catch (final IOException e) {
                                // Ignore at this time of development
                            } catch (final SecurityException e) {
                                // Ignore at this time of development
                            }
                        } else {
                            final OutputStream out = new OutputStream() {
                                public void write(final int a) throws IOException { }
                            };
                            writer = new OutputStreamWriter(out);
                        }
                    }
                    open = true;
                }
            }
        }

        gLevel = level;
    }

    public boolean isLoggable(final int level) {
        return level >= gLevel;
    }

    /**
     * Sets the log level.
     * @param level the level
     */
    public static void setLogLevel(final int level) {
        setLogLevel(level, false);
    }
}
