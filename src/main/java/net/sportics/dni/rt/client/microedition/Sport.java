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
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;
import net.sportics.dni.rt.client.microedition.util.LogManager;
import net.sportics.dni.rt.client.microedition.util.Properties;

public final class Sport {

    private static final LogManager LOG = LogManager.getInstance("Sport");
    static {
        LOG.debug("#class: " + Sport.class.getName());
    }

    public static final String SPORT_ID_CONFIG_KEY = "sport.id";
    public static final String SPORT_ID_UNKNWON = "63";

    private static Vector SPORTS;

    private String id;
    private String name;

    private Sport(final String id, final String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * @return the id
     */
    public final String getId() {
        return this.id;
    }

    /**
     * @return the name
     */
    public final String getName() {
        return this.name;
    }

    public static Sport[] supportedSport() {
        init();
        final int size = SPORTS.size();
        final Sport[] retval = new Sport[size];
        SPORTS.copyInto(retval);
        return retval;
    }

    public static Sport sportById(final String id) {
        init();
        for (final Enumeration e = SPORTS.elements(); e.hasMoreElements(); ) {
            final Sport activity = (Sport) e.nextElement();
            if (activity.id.equals(id)) {
                return activity;
            }
        }
        return null;
    }

    public static Sport sportByName(final String name) {
        init();
        for (final Enumeration e = SPORTS.elements(); e.hasMoreElements(); ) {
            final Sport activity = (Sport) e.nextElement();
            if (activity.name.equals(name)) {
                return activity;
            }
        }
        return null;
    }

    static final void init() {
        synchronized(Sport.class) {
            if (SPORTS != null) {
                return;
            }
            SPORTS = new Vector();
            final InputStream in = Sport.class.getResourceAsStream("/activities.map");
            final Properties props = new Properties();
            try {
                props.load(in);
                for (final Enumeration e = props.keys(); e.hasMoreElements(); ) {
                    final String key = (String) e.nextElement();
                    final String value = (String) props.get(key);
                    final Sport aid = new Sport(key, value);
                    LOG.config("init() - loaded " + aid);
                    SPORTS.addElement(aid);
                }
            } catch (final IOException e) {
                LOG.warn("init() - unable to load Sport map: " + e.getMessage());
                final Sport unkown = new Sport("1", "unkown");
                final Sport running = new Sport("2", "Running");
                final Sport cycling = new Sport("6", "Cycling");
                final Sport inlining = new Sport("18", "Inline Skating");
                SPORTS.addElement(unkown);
                SPORTS.addElement(running);
                SPORTS.addElement(cycling);
                SPORTS.addElement(inlining);
            } finally {
                try {
                    in.close();
                } catch (final IOException ex) {
                    LOG.warn("init() - unable to close InputStream: " + ex.getMessage());
                }
            }
            LOG.debug("init() - all Sports: " + SPORTS);
        }
    }

    public String toString() {
        final StringBuffer sb = new StringBuffer("Sport@");

        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append("[[id:");
        sb.append(this.id);
        sb.append("][name:");
        sb.append(this.name);
        sb.append("]]");

        return sb.toString();
    }

    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Sport other = (Sport) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
}
