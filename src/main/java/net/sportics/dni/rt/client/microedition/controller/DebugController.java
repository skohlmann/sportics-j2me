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

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.midlet.MIDlet;

import net.sportics.dni.rt.client.microedition.ConfigManager;
import net.sportics.dni.rt.client.microedition.ConfigurationConstants;
import net.sportics.dni.rt.client.microedition.SporticsException;
import net.sportics.dni.rt.client.microedition.util.LogManager;

public final class DebugController extends AbstractController {

    private static final LogManager LOG = LogManager.getInstance("DebugController");
    static {
        LOG.debug("#class: " + DebugController.class.getName());
    }

    private Command selected = null;

    protected void doHandle() throws SporticsException {
        boolean loop = true;
        LOG.debug("start display()");
        while(loop) {
            final List list = createList();
            display(list);
            waitForStateChange();

            if (this.selected == MainController.BACK) {
                loop = false;
                continue;
            } else if (this.selected == MainController.SELECT && list.isSelected(0)) {
                final ListSelectionController ctrl = new ListSelectionController();
                ctrl.addToList(LogManager.OFF_STR);
                ctrl.addToList(LogManager.SEVERE_STR);
                ctrl.addToList(LogManager.WARNING_STR);
                ctrl.addToList(LogManager.INFO_STR);
                ctrl.addToList(LogManager.CONFIG_STR);
                ctrl.addToList(LogManager.DEBUG_STR);
                ctrl.addToList(LogManager.TRACE_STR);
                ctrl.setSelectedElement(0);
                ctrl.setTitle("Select log level");

                final ConfigManager cfgMgr = ConfigManager.getInstance();
                final String current = cfgMgr.get(LogManager.LOG_LEVEL_KEY, LogManager.OFF_STR);
                if (LogManager.OFF_STR.equals(current)) {
                    ctrl.setSelectedElement(0);
                } else if (LogManager.SEVERE_STR.equals(current)) {
                    ctrl.setSelectedElement(1);
                } else if (LogManager.WARNING_STR.equals(current)) {
                    ctrl.setSelectedElement(2);
                } else if (LogManager.INFO_STR.equals(current)) {
                    ctrl.setSelectedElement(3);
                } else if (LogManager.CONFIG_STR.equals(current)) {
                    ctrl.setSelectedElement(4);
                } else if (LogManager.DEBUG_STR.equals(current)) {
                    ctrl.setSelectedElement(5);
                } else if (LogManager.TRACE_STR.equals(current)) {
                    ctrl.setSelectedElement(6);
                } else {
                    ctrl.setSelectedElement(0);
                }
                final MIDlet midlet = getMIDlet();
                ctrl.handle(midlet);

                final String selection = ctrl.getSelectedElementAsString();
                cfgMgr.set(LogManager.LOG_LEVEL_KEY, selection);
                final int asInt = LogManager.getLogLevelForString(selection);
                LogManager.setLogLevel(asInt);
            } else if (this.selected == MainController.SELECT && list.isSelected(1)) {
                final BooleanSelectionController ctrl = new BooleanSelectionController();
                final boolean current = currentTextUiDecision();
                ctrl.setCurrentDecision(current);
                ctrl.setTitle("Text UI");
                ctrl.setQuestion("Use Debug UI?");
                final MIDlet midlet = getMIDlet();
                ctrl.handle(midlet);
                final boolean decision = ctrl.getCurrentDecision();
                setNewTextUiDecision(decision);
            } else if (this.selected == MainController.SELECT && list.isSelected(2)) {
                final BooleanSelectionController ctrl = new BooleanSelectionController();
                final boolean current = currentGpsNmeaDecision();
                ctrl.setCurrentDecision(current);
                ctrl.setTitle("GPS NMEA");
                ctrl.setQuestion("Should GPS NMEA storing allowed?");
                final MIDlet midlet = getMIDlet();
                ctrl.handle(midlet);
                final boolean decision = ctrl.getCurrentDecision();
                setNewGpsNmeaDecision(decision);
            }
        }
        LOG.debug("stop display()");
    }

    final List createList() {
        final List list = new List("Debug Settings", List.IMPLICIT);

        list.append("Debug Logging", null);
        list.append("Text UI", null);
        list.append("Collect GPS NMEA (if available)", null);

        list.addCommand(MainController.BACK);
        list.addCommand(MainController.SELECT);
        list.setSelectCommand(MainController.SELECT);
        list.setCommandListener(new CommandListener() {
            public void commandAction(final Command cmd, final Displayable displayable) {
                DebugController.this.selected = cmd;
                notifyStateChanged();
            }
        });
        return list;
    }

    public static final boolean currentGpsNmeaDecision() {
        final ConfigManager cfgMgr = ConfigManager.getInstance();
        final String allowed = cfgMgr.get(ConfigurationConstants.GPS_STORE_NMEA_KEY,
                                          ConfigurationConstants.GPS_STORE_NMEA_VALUE_NO);
        if (ConfigurationConstants.GPS_STORE_NMEA_VALUE_YES.equalsIgnoreCase(allowed)) {
            return true;
        }
        return false;
    }

    public static final boolean currentTextUiDecision() {
        final ConfigManager cfgMgr = ConfigManager.getInstance();
        final String allowed = cfgMgr.get(PrepareSportController.TEXT_UI_CONFIG_KEY,
                                          PrepareSportController.TEXT_UI_ALLOWED_VALUE_NO);
        if (PrepareSportController.TEXT_UI_ALLOWED_VALUE_NO.equalsIgnoreCase(allowed)) {
            return false;
        }
        return true;
    }

    final void setNewTextUiDecision(final boolean desision) {
        final ConfigManager cfgMgr = ConfigManager.getInstance();
        cfgMgr.set(PrepareSportController.TEXT_UI_CONFIG_KEY,
                   desision ? PrepareSportController.TEXT_UI_ALLOWED_VALUE_YES
                            : PrepareSportController.TEXT_UI_ALLOWED_VALUE_NO);
    }

    final void setNewGpsNmeaDecision(final boolean desision) {
        final ConfigManager cfgMgr = ConfigManager.getInstance();
        cfgMgr.set(ConfigurationConstants.GPS_STORE_NMEA_KEY, 
                   desision ? ConfigurationConstants.GPS_STORE_NMEA_VALUE_YES
                            : ConfigurationConstants.GPS_STORE_NMEA_VALUE_NO);
    }
}
