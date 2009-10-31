/* Copyright (C) 2009 Sascha Kohlmann
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
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Ticker;
import javax.microedition.midlet.MIDlet;
import net.sportics.dni.rt.client.microedition.Sport;
import net.sportics.dni.rt.client.microedition.ConfigManager;
import net.sportics.dni.rt.client.microedition.SporticsException;
import net.sportics.dni.rt.client.microedition.accu.Accumulator;
import net.sportics.dni.rt.client.microedition.bluetooth.BluetoothManager;
import net.sportics.dni.rt.client.microedition.controller.ui.ExtSportSessionController;
import net.sportics.dni.rt.client.microedition.device.Device;
import net.sportics.dni.rt.client.microedition.device.DeviceDb;
import net.sportics.dni.rt.client.microedition.device.DeviceDb.DeviceDescriptorEnumeration;
import net.sportics.dni.rt.client.microedition.device.DeviceDescriptor;
import net.sportics.dni.rt.client.microedition.device.DeviceManager;
import net.sportics.dni.rt.client.microedition.util.CopyVector;
import net.sportics.dni.rt.client.microedition.util.FilterEnumeration;
import net.sportics.dni.rt.client.microedition.util.LogManager;

/**
 *
 * @author Sascha Kohlmann
 */
public class PrepareSportController extends AbstractController {

    private static final LogManager LOG = LogManager.getInstance("SportController");
    static {
        LOG.debug("#class: " + PrepareSportController.class.getName());
    }
    static final String TEXT_UI_CONFIG_KEY = "PrepareSportController.ui.text";
    static final String TEXT_UI_ALLOWED_VALUE_YES = Boolean.TRUE.toString();
    static final String TEXT_UI_ALLOWED_VALUE_NO = Boolean.FALSE.toString();

    public static final Command START = new Command("Start", Command.ITEM, 1);
    public static final Command SELECT = new Command("Change", "Change Sport", Command.ITEM, 2);
    public static final Command BACK = new Command("Back", Command.ITEM, 3);

    private Thread lookupThread = null;
    private Sport selectedSport = null;
    private Command selected = null;

    protected void doHandle() throws SporticsException {
        this.lookupThread = startDeviceLookupThread();
        this.selectedSport = fetchSport();

        while (true) {
            final List list = createList();
            display(list);
            checkInformer();
            waitForStateChange();

            if (this.selected == START) {
                LOG.debug("doHandle() - START selected");
                waitForLookupThread();
                this.lookupThread = null;

                final SportSessionController session = sportSessionController();
                session.setSport(selectedSport);
                final MIDlet midlet = getMIDlet();
                session.handle(midlet);
                return;
            } else if (this.selected == BACK) {
                LOG.debug("doHandle() - BACK selected");
                final Displayable current = currentDisplayable();
                current.setTicker(null);
                return;
            } else if (this.selected == SELECT) {
                LOG.debug("doHandle() - SELECT selected");
                final String sportId = selectedSport.getId();
                this.selectedSport = selectSport(sportId);
            }
        }
    }

    final SportSessionController sportSessionController() {
        if (!DebugController.currentTextUiDecision()) {
            return new ExtSportSessionController();
        }
        return new SimpleSportSessionController();
    }

    final void checkInformer() {
        final Displayable current = currentDisplayable();
        if (current.getTicker() == null) {
            final Ticker searchTicker = new Ticker(DeviceController.SEARCH_TEXT);
            current.setTicker(searchTicker);
        }
    }

    protected void waitForStateChange() {
        super.waitForStateChange();
    }

    final List createList() {
        final List list = new List("Prepare Sport", List.IMPLICIT);

        final String sportName = this.selectedSport.getName();
        list.append(sportName, null);

        list.addCommand(BACK);
        list.addCommand(SELECT);
        list.addCommand(START);
        list.setSelectCommand(START);
        list.setCommandListener(new CommandListener() {
            public void commandAction(final Command cmd, final Displayable displayable) {
                PrepareSportController.this.selected = cmd;
                notifyStateChanged();
            }
        });
        return list;
    }

    final Displayable currentDisplayable() {
        final MIDlet midlet = getMIDlet();
        final Display display = Display.getDisplay(midlet);
        final Displayable current = display.getCurrent();
        return current;
    }

    final Sport fetchSport() {
        LOG.debug("enter fetchSport()");
        final ConfigManager config = ConfigManager.getInstance();
        final String activityId = config.get(Sport.SPORT_ID_CONFIG_KEY, Sport.SPORT_ID_UNKNWON);
        final Sport sport = Sport.sportById(activityId);
        if (sport != null && !sport.getId().equals(Sport.SPORT_ID_UNKNWON)) {
            LOG.debug("leave fetchSport() with " + sport);
            return sport;
        }
        LOG.debug("leave fetchSport() but first need to select sport.");
        final Sport localSelected = selectSport("-1");
        final String sportId = localSelected.getId();
        config.set(Sport.SPORT_ID_CONFIG_KEY, sportId);
        return localSelected;
    }

    final Sport selectSport(final String sportId) {
        LOG.debug("enter selectSport(String) with id: " + sportId);
        final Sport[] sports = Sport.supportedSport();
        final Vector v = new CopyVector(sports);
        final Enumeration filter = new FilterEnumeration(v.elements()) {
            public boolean filter(final Object o) {
                return !Sport.SPORT_ID_UNKNWON.equals(((Sport) o).getId());
            }
        };
        final ListSelectionController lsc = createListSelectionController();
        startListSelectionController(lsc, filter, sportId);
        final MIDlet midlet = getMIDlet();
        LOG.debug("selectSport(String) start selection");
        lsc.handle(midlet);
        final String localSelected = lsc.getSelectedElementAsString();
        final Sport localSelectedSport = Sport.sportByName(localSelected);
        LOG.debug("leave selectSport(String) - selected " + localSelectedSport);
        return localSelectedSport;
    }

    final Thread startDeviceLookupThread() {
        final Accumulator accu = Accumulator.getInstance();
        final DeviceLookup deviceLookup = new DeviceLookup(accu);
        final Thread innerLookupThread = new Thread(deviceLookup, "BluetoothLookup");
        innerLookupThread.start();
        return innerLookupThread;
    }

    final ListSelectionController createListSelectionController() {
        final ListSelectionController lsc = new ListSelectionController() {

            protected void doHandle() throws SporticsException {
                final MIDlet midlet = getMIDlet();
                final Display display = Display.getDisplay(midlet);
                final Displayable current = display.getCurrent();
                if (PrepareSportController.this.lookupThread != null
                        && PrepareSportController.this.lookupThread.isAlive()) {
                    final Ticker searchTicker = new Ticker(DeviceController.SEARCH_TEXT);
                    current.setTicker(searchTicker);
                }
                try {
                    super.doHandle();
                } finally {
                    current.setTicker(null);
                }
            }
        };
        return lsc;
    }

    final void startListSelectionController(final ListSelectionController lsc,
                                            final Enumeration filter,
                                            final String sportId) {
        lsc.setTitle("Select Sport");
        while (filter.hasMoreElements()) {
            final Sport sport = (Sport) filter.nextElement();
            LOG.debug("selectSport(String) add " + sport + " to list");
            final String name = sport.getName();
            final int idx = lsc.addToList(name);
            final String localSportId = sport.getId();
            if (localSportId.equals(sportId)) {
                lsc.setSelectedElement(idx);
            }
        }
    }

    void waitForLookupThread() {
        try {
            LOG.debug("waitForLookupThread() - on thread: " + this.lookupThread);
            if (this.lookupThread != null) {
                this.lookupThread.join();
            }
        } catch (final InterruptedException ex) {
            LOG.info("doHandle() - Thread interrupted: " + this.lookupThread + " - msg: "
                     + ex.getMessage());
        }
    }

    final class DeviceLookup implements Runnable {

        private final Accumulator accu;
        private final Vector devices = new Vector();

        public Vector getDevices() {
            return this.devices;
        }

        public DeviceLookup(final Accumulator accu) {
            if (accu == null) {
                throw new IllegalArgumentException("accu is null");
            }
            this.accu = accu;
        }

        public void run() {
            try {
                final DeviceDb deviceDb = DeviceDb.getInstance();
                final DeviceDescriptorEnumeration e = deviceDb.enumerateDeviceDescriptors();
                try {

                    try {
                        final BluetoothManager manager = BluetoothManager.getInstance();
                        checkInformer();
                        manager.discoverDevices();
                    } finally {
                        final Displayable current = currentDisplayable();
                        current.setTicker(null);
                    }

                    final DeviceManager deviceManager = DeviceManager.getInstance();
                    while (e.hasMoreElements()) {
                        final DeviceDescriptor dd = (DeviceDescriptor) e.nextElement();
                        LOG.debug("device descriptor: " + dd);
                        final Device device = deviceManager.deviceForDescriptor(dd);
                        if (device != null) {
                            LOG.debug("device name: " + device.getDeviceName() + " - device class: "
                                      + device.getClass().getName());
                            device.prepare();
                            device.start();
                            device.registerTypedDataConsumer(accu);
                            this.devices.addElement(device);
                        }
                    }
                } finally {
                    e.dispose();
                }
            } catch (final Exception e) {
                
            }
        }
    }
}
