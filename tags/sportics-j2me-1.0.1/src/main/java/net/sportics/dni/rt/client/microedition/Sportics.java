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

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import net.sportics.dni.rt.client.microedition.controller.BootController;
import net.sportics.dni.rt.client.microedition.controller.Controller;
import net.sportics.dni.rt.client.microedition.controller.MainController;
import net.sportics.dni.rt.client.microedition.util.LogManager;

/**
 * The main entry implementation of the Sportics SRTS implementation.
 *
 * @author Sascha Kohlmann
 */
public class Sportics extends MIDlet {

    private static final LogManager LOG = LogManager.getInstance("Sportics");
    static {
        LOG.debug("#class: " + Sportics.class.getName());
    }

    private boolean stillStarted = false;

    protected void destroyApp(final boolean unconditional) throws MIDletStateChangeException {
    }

    protected void pauseApp() {
    }

    protected void startApp() throws MIDletStateChangeException {

        LOG.debug("Startup");
        try {
            final ConfigManager cm = ConfigManager.getInstance();
//            LOG.debug("size: " + cm.rs.getSize());
//            LOG.debug("available: " + cm.rs.getSizeAvailable());
//            LOG.debug("version: " + cm.rs.getVersion());
        } catch (final Exception e) {
            LOG.debug("Unable to get record store information");
        }

        try {
            if (!this.stillStarted) {
                final Controller ctrl = new BootController();
                ctrl.handle(this);

                final Controller c = new MainController();
                c.handle(this);
                notifyDestroyed();
                System.exit(0);
            }
        } finally {
            final Runtime rt = Runtime.getRuntime();
            LOG.debug("Total memory: " + rt.totalMemory() + " - freeMemory: " + rt.freeMemory());
        }
    }
}
