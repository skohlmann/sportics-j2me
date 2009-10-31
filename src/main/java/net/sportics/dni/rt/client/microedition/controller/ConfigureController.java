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
import net.sportics.dni.rt.client.microedition.bluetooth.BluetoothManager;
import net.sportics.dni.rt.client.microedition.net.SrtsApi;
import net.sportics.dni.rt.client.microedition.util.LogManager;

public final class ConfigureController extends AbstractController {

    private static final LogManager LOG = LogManager.getInstance("ConfigureController");
    static {
        LOG.debug("#class: " + ConfigureController.class.getName());
    }

    private static final ConfigManager CFG_MGR = ConfigManager.getInstance();

    private static final String[] LIVE_TRACKING_INTERVAL =
        {" 0.5 min", " 1 min", " 2 min", " 3 min", " 5 min", "10 min"};

    private Command selected = null;

    protected void doHandle() throws SporticsException {
        LOG.debug("doHandle");

        boolean loop = true;
        while(loop) {
            final List list = createList();
            display(list);
            waitForStateChange();

            if (this.selected == MainController.BACK) {
                loop = false;
                continue;
            } else if ((this.selected == List.SELECT_COMMAND
                    || this.selected == MainController.SELECT) && list.isSelected(0)) {
                handleUserDataSelection();
            } else if ((this.selected == List.SELECT_COMMAND
                    || this.selected == MainController.SELECT) && list.isSelected(1)) {
                handlePositionTrackingAllowedSelection();
            } else if ((this.selected == List.SELECT_COMMAND
                    || this.selected == MainController.SELECT) && list.isSelected(2)) {
                handleLiveTrackingAllowedSelection();
            } else if ((this.selected == List.SELECT_COMMAND
                    || this.selected == MainController.SELECT) && list.isSelected(4)) {
                handleBluetoothAllowedSelection();
            } else if ((this.selected == List.SELECT_COMMAND
                    || this.selected == MainController.SELECT) && list.isSelected(3)) {
                handleIntervalSelection();
            } else if ((this.selected == List.SELECT_COMMAND
                    || this.selected == MainController.SELECT) && list.isSelected(5)) {
                final DebugController ctrl = new DebugController();
                final MIDlet midlet = this.getMIDlet();
                ctrl.handle(midlet);
            }
        }
        LOG.debug("stop display()");
    }

    final void handlePositionTrackingAllowedSelection() {
        final BooleanSelectionController ctrl = new BooleanSelectionController();
        final boolean current = currentPositionTrackingDecision();
        ctrl.setCurrentDecision(current);
        ctrl.setTitle("Position tracking");
        ctrl.setQuestion("Allow position tracking?");
        final MIDlet midlet = this.getMIDlet();
        ctrl.handle(midlet);
        final boolean decision = ctrl.getCurrentDecision();
        LOG.config("New position tracking decision: " + decision);
        setNewPositionTrackingDecision(decision);
    }

    final void handleBluetoothAllowedSelection() {
        final BooleanSelectionController ctrl = new BooleanSelectionController();
        final boolean current = currentBluetoothDecision();
        ctrl.setCurrentDecision(current);
        ctrl.setTitle("Bluetooth");
        ctrl.setQuestion("Allow Bluetooth?");
        final MIDlet midlet = this.getMIDlet();
        ctrl.handle(midlet);
        final boolean decision = ctrl.getCurrentDecision();
        LOG.config("New Bluetooth decision: " + decision);
        setNewBluetoothDecision(decision);
    }

    final void handleLiveTrackingAllowedSelection() {
        final BooleanSelectionController ctrl = new BooleanSelectionController();
        final boolean current = currentLiveTrackingDecision();
        ctrl.setCurrentDecision(current);
        ctrl.setTitle("Live tracking");
        ctrl.setQuestion("Allow live tracking?");
        final MIDlet midlet = this.getMIDlet();
        ctrl.handle(midlet);
        final boolean decision = ctrl.getCurrentDecision();
        LOG.config("New live tracking decision: " + decision);
        setNewLiveTrackingDecision(decision);
    }

    final void handleIntervalSelection() {
        final ListSelectionController ctrl = new ListSelectionController();
        ctrl.addToList(LIVE_TRACKING_INTERVAL[0]);
        ctrl.addToList(LIVE_TRACKING_INTERVAL[1]);
        ctrl.addToList(LIVE_TRACKING_INTERVAL[2]);
        ctrl.addToList(LIVE_TRACKING_INTERVAL[3]);
        ctrl.addToList(LIVE_TRACKING_INTERVAL[4]);
        ctrl.addToList(LIVE_TRACKING_INTERVAL[5]);
        ctrl.setTitle("Select live tracking interval");

        final String value =
            CFG_MGR.get(SrtsApi.LIVE_TRACKING_INTERVAL_KEY, "" + 120);
        if (("" + 30).equals(value)) {
            ctrl.setSelectedElement(0);
        } else if (("" + 60).equals(value)) {
            ctrl.setSelectedElement(1);
        } else if (("" + 180).equals(value)) {
            ctrl.setSelectedElement(3);
        } else if (("" + 300).equals(value)) {
            ctrl.setSelectedElement(4);
        } else if (("" + 600).equals(value)) {
            ctrl.setSelectedElement(5);
        } else {
            ctrl.setSelectedElement(2);
        }

        ctrl.setTitle("Live tracking");
        final MIDlet midlet = getMIDlet();
        ctrl.handle(midlet);
        final int selection = ctrl.getSelectedElement();
        int interval;
        switch(selection) {
            case 0:
                interval = 30;
                break;
            case 1:
                interval = 60;
                break;
            case 3:
                interval = 180;
                break;
            case 4:
                interval = 300;
                break;
            case 5:
                interval = 600;
                break;
            default:
                interval = 120;
                break;
        }
        LOG.debug("new interval time: " + interval + "s");
        CFG_MGR.set(SrtsApi.LIVE_TRACKING_INTERVAL_KEY, "" + interval);
    }

    final List createList() {
        final List list = new List("Configure", List.IMPLICIT);

        list.append("Login and password", null);
        final String position = isPositionAllowed();
        final String live = isLiveTrackingAllowed();
        final String bt = isBluetoothAllowed();
        list.append(position, null);
        list.append(live, null);
        list.append("Live tracking interval", null);
        list.append(bt, null);
        list.append("Debug", null);

        list.addCommand(MainController.BACK);
        list.addCommand(MainController.SELECT);
        list.setSelectCommand(List.SELECT_COMMAND);
        list.setCommandListener(new CommandListener() {
            public void commandAction(final Command cmd, final Displayable displayable) {
                ConfigureController.this.selected = cmd;
                notifyStateChanged();
            }
        });
        return list;
    }

    final void handleUserDataSelection() {
        final Controller ctrl = new UserController();
        final MIDlet midlet = getMIDlet();
        ctrl.handle(midlet);
    }

    final String isPositionAllowed() {
        if (currentPositionTrackingDecision()) {
            return "Position tracking allowed";
        }
        return "Position tracking not allowed";
    }

    final String isLiveTrackingAllowed() {
        if (currentLiveTrackingDecision()) {
            return "Live tracking allowed";
        }
        return "Live tracking not allowed";
    }

    final String isBluetoothAllowed() {
        if (currentBluetoothDecision()) {
            return "Bluetooth allowed";
        }
        return "Bluetooth not allowed";
    }

    public static final boolean currentLiveTrackingDecision() {
        final String allowed = CFG_MGR.get(SrtsApi.LIVE_TRACKING_ALLOWED_KEY,
                                           SrtsApi.LIVE_TRACKING_ALLOWED_VALUE_YES);
        if (SrtsApi.LIVE_TRACKING_ALLOWED_VALUE_YES.equalsIgnoreCase(allowed)) {
            return true;
        }
        return false;
    }

    static final void setNewLiveTrackingDecision(final boolean desision) {
        CFG_MGR.set(SrtsApi.LIVE_TRACKING_ALLOWED_KEY,
                    desision ? SrtsApi.LIVE_TRACKING_ALLOWED_VALUE_YES
                             : SrtsApi.LIVE_TRACKING_ALLOWED_VALUE_NO);
    }

    public static final boolean currentPositionTrackingDecision() {
        final String allowed = CFG_MGR.get(ConfigurationConstants.POSITION_ALLOWED_KEY,
                                           ConfigurationConstants.POSITION_ALLOWED_VALUE_YES);
        if (ConfigurationConstants.POSITION_ALLOWED_VALUE_YES.equalsIgnoreCase(allowed)) {
            return true;
        }
        return false;
    }

    static final void setNewPositionTrackingDecision(final boolean desision) {
        CFG_MGR.set(ConfigurationConstants.POSITION_ALLOWED_KEY,
                    desision ? ConfigurationConstants.POSITION_ALLOWED_VALUE_YES
                             : ConfigurationConstants.POSITION_ALLOWED_VALUE_NO);
    }

    public static final boolean currentBluetoothDecision() {
        final String allowed = CFG_MGR.get(BluetoothManager.BLUETOOTH_ALLOWED_KEY,
                                           BluetoothManager.BLUETOOTH_ALLOWED_VALUE_YES);
        if (BluetoothManager.BLUETOOTH_ALLOWED_VALUE_YES.equalsIgnoreCase(allowed)) {
            return true;
        }
        return false;
    }

    static final void setNewBluetoothDecision(final boolean desision) {
        CFG_MGR.set(BluetoothManager.BLUETOOTH_ALLOWED_KEY,
                    desision ? BluetoothManager.BLUETOOTH_ALLOWED_VALUE_YES
                             : BluetoothManager.BLUETOOTH_ALLOWED_VALUE_NO);
    }
}
