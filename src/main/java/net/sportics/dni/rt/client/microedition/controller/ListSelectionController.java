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

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;

import net.sportics.dni.rt.client.microedition.SporticsException;
import net.sportics.dni.rt.client.microedition.util.LogManager;

public class ListSelectionController extends AbstractController {

    private static final LogManager LOG = LogManager.getInstance("ListSelectionController");
    static {
        LOG.debug("#class: " + ListSelectionController.class.getName());
    }

    private static Image SELECTED;
    static {
        try {
            final InputStream in =
                ListSelectionController.class.getResourceAsStream("/images/16x16/weather-clear.png");
            SELECTED = Image.createImage(in);
        } catch (final IOException e) {
            LOG.warn("Unable to load selection image.");
            SELECTED = null;
        }
    }

    private String title = null;
    private Vector list = new Vector();
    private int selected = -1;

    protected void doHandle() throws SporticsException {
        final List entries = new List(this.title, List.IMPLICIT);
        int i = 0;
        for (final Enumeration e = this.list.elements(); e.hasMoreElements(); ) {
            final Object o = e.nextElement();
            if (this.selected == i) {
                entries.append((String) o, SELECTED);
            } else {
                entries.append((String) o, null);
            }
            i++;
        }
        entries.addCommand(MainController.SELECT);
        entries.addCommand(MainController.BACK);
        entries.setSelectCommand(List.SELECT_COMMAND);
        entries.setCommandListener(new CommandListener() {
            public void commandAction(final Command cmd, final Displayable displayable) {
                if (cmd == MainController.BACK) {
                    notifyStateChanged();
                } else if (cmd == MainController.SELECT || cmd == List.SELECT_COMMAND) {
                    final int select = entries.getSelectedIndex();
                    LOG.debug("Old selection index: "
                              + ListSelectionController.this.selected + " - new selected index: "
                              + select);
                    if (select != ListSelectionController.this.selected) {
                        if (ListSelectionController.this.selected != -1) {
                            final String text =
                                (String) list.elementAt(ListSelectionController.this.selected);
//                            entries.delete(ListSelectionController.this.selected);
//                            entries.insert(ListSelectionController.this.selected, text, null);
                            entries.set(ListSelectionController.this.selected, text, null);
                        }
                        final String text = (String) list.elementAt(select);
                        entries.set(select, text, SELECTED);
                        ListSelectionController.this.selected = select; 
                    }
                }
            }
        });
        display(entries);
        waitForStateChange();
    }

    public int addToList(final String o) {
        this.list.addElement(o);
        return list.size() - 1;
    }

    public int getSelectedElement() {
        return this.selected;
    }

    public String getSelectedElementAsString() {
        if (this.selected != -1) {
            return (String) this.list.elementAt(this.selected);
        }
        return null;
    }

    public void setSelectedElement(final int index) {
        this.selected = index;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }
}
