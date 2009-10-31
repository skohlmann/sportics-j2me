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
package net.sportics.dni.rt.client.microedition.controller;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.midlet.MIDlet;

import net.sportics.dni.rt.client.microedition.SelectionListEntry;
import net.sportics.dni.rt.client.microedition.SporticsException;
import net.sportics.dni.rt.client.microedition.util.LogManager;


public final class MainController extends AbstractController {

    private static final LogManager LOG = LogManager.getInstance("MainController");
    static {
        LOG.debug("#class: " + MainController.class.getName());
    }

    static final Command EXIT = new Command("Exit", Command.EXIT, 3);
    public static final Command BACK = new Command("Back", Command.BACK, 3);
    public static final Command NEXT = new Command("Next", Command.OK, 3);
    public static final Command SELECT = new Command("Select", Command.OK, 2);
    public static final Command PAUSE = new Command("Pause", Command.OK, 1);
    public static final Command CONTINUE = new Command("Continue", Command.OK, 1);
    public static final Command STOP = new Command("Stop", Command.OK, 2);

    private final Vector entries = new Vector();

    /** Constructs a new controller. */
    public MainController() {
        final SelectionListEntry e1 = new SelectionListEntry();
        e1.setViewData("Start Sport", null);
        entries.addElement(e1);

        final SelectionListEntry e2 = new SelectionListEntry();
        e2.setViewData("Devices", null);
        entries.addElement(e2);

        final SelectionListEntry e3 = new SelectionListEntry();
        e3.setViewData("Configure", null);
        entries.addElement(e3);

        final SelectionListEntry e4 = new SelectionListEntry();
        e4.setViewData("About", null);
        entries.addElement(e4);

//        final SelectionListEntry e5 = new SelectionListEntry();
//        e5.setViewData("Test", null);
//        entries.addElement(e5);
    }

    private Command selected = null;

    protected void doHandle() throws SporticsException {
        final List list = new List("Sportics", List.IMPLICIT);
        for (final Enumeration e = this.entries.elements(); e.hasMoreElements(); ) {
            final SelectionListEntry entry = (SelectionListEntry) e.nextElement();
            entry.addToChoice(list);
        }
        list.addCommand(EXIT);
        list.addCommand(SELECT);
        list.setSelectCommand(List.SELECT_COMMAND);
        list.setCommandListener(new CommandListener() {
            public void commandAction(final Command cmd, final Displayable displayable) {
                LOG.debug("command: " + cmd.getLabel() + " - index: "
                          + list.getSelectedIndex());
                MainController.this.selected = cmd;
                notifyStateChanged();
            }
        });
        display(list);
        boolean again = true;
        while (again) {
            waitForStateChange();
            if (this.selected == EXIT) {
                again = false;
            } /*else if ((this.selected == List.SELECT_COMMAND || this.selected == SELECT)
                    && list.getSelectedIndex() == 4) {
                final Controller ctrl = new TestCanvasController();
                final MIDlet midlet = MainController.this.getMIDlet();
                ctrl.handle(midlet);
            }*/ else if ((this.selected == List.SELECT_COMMAND || this.selected == SELECT)
                    && list.getSelectedIndex() == 3) {
                final Controller ctrl = new AboutController();
                final MIDlet midlet = MainController.this.getMIDlet();
                ctrl.handle(midlet);
            } else if ((this.selected == List.SELECT_COMMAND || this.selected == SELECT)
                    && list.getSelectedIndex() == 2) {
                LOG.debug("called: Command: " + this.selected.getLabel()
                          + " - index: " + list.getSelectedIndex());
                final AbstractController ctrl = new ConfigureController();
                final MIDlet midlet = MainController.this.getMIDlet();
                ctrl.handle(midlet);
            } else if ((this.selected == List.SELECT_COMMAND || this.selected == SELECT)
                    && list.getSelectedIndex() == 1) {
                final Controller ctrl = new DeviceController();
                final MIDlet midlet = MainController.this.getMIDlet();
                ctrl.handle(midlet);
            } else if ((this.selected == List.SELECT_COMMAND || this.selected == SELECT)
                    && list.getSelectedIndex() == 0) {
//                final Controller ctrl = new SessionController();
                final Controller ctrl = new PrepareSportController();
                final MIDlet midlet = MainController.this.getMIDlet();
                try {
                    ctrl.handle(midlet);
                } catch (final Exception e) {
                    LOG.warn(e.getClass().getName() + ": " + e.getMessage());
                }
            }
        }
    }
}
