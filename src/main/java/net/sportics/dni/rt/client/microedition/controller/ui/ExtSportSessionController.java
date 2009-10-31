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
package net.sportics.dni.rt.client.microedition.controller.ui;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;
import net.sportics.dni.rt.client.microedition.Pauseable;
import net.sportics.dni.rt.client.microedition.Sport;
import net.sportics.dni.rt.client.microedition.SporticsException;
import net.sportics.dni.rt.client.microedition.Startable;
import net.sportics.dni.rt.client.microedition.TypedData;
import net.sportics.dni.rt.client.microedition.TypedDataDescriptor;
import net.sportics.dni.rt.client.microedition.Variant;
import net.sportics.dni.rt.client.microedition.accu.Accumulator;
import net.sportics.dni.rt.client.microedition.accu.Assemblage;
import net.sportics.dni.rt.client.microedition.accu.Sink;
import net.sportics.dni.rt.client.microedition.controller.ConfigureController;
import net.sportics.dni.rt.client.microedition.controller.ListSelectionController;
import net.sportics.dni.rt.client.microedition.controller.MainController;
import net.sportics.dni.rt.client.microedition.controller.SportSessionController;
import net.sportics.dni.rt.client.microedition.controller.UserController;
import net.sportics.dni.rt.client.microedition.device.Device;
import net.sportics.dni.rt.client.microedition.device.DeviceDb;
import net.sportics.dni.rt.client.microedition.device.DeviceDescriptor;
import net.sportics.dni.rt.client.microedition.device.DeviceManager;
import net.sportics.dni.rt.client.microedition.device.DurationDevice;
import net.sportics.dni.rt.client.microedition.net.SrtsApi;
import net.sportics.dni.rt.client.microedition.net.SrtsApiBuilder;
import net.sportics.dni.rt.client.microedition.net.SrtsApiException;
import net.sportics.dni.rt.client.microedition.net.SrtsApiListener;
import net.sportics.dni.rt.client.microedition.ui.TrafficLightsState;
import net.sportics.dni.rt.client.microedition.util.CopyVector;
import net.sportics.dni.rt.client.microedition.util.DateSupport;
import net.sportics.dni.rt.client.microedition.util.FilterEnumeration;
import net.sportics.dni.rt.client.microedition.util.LogManager;
import net.sportics.dni.rt.client.microedition.util.StringUtil;

/**
 * @author Sascha Kohlmann
 */
public final class ExtSportSessionController extends SportSessionController {

    private static final LogManager LOG = LogManager.getInstance("ExtSportSessionController");
    static {
        LOG.debug("#class: " + ExtSportSessionController.class.getName());
    }
    public static final Command OTHER_SPORT = new Command("Other Sport", Command.OK, 2);
    public static final Command CONTINUE = new Command("Continue", Command.OK, 1);
    static final String NOVAL = "noval";

    boolean first = true;
    Command selected = null;

    final Vector devices = new Vector();
    final DataCanvas c = new DataCanvas();
    DurationDevice durationDevice;
    SrtsApi api = null;

    final Data data0 = new Data();
    final Data data1 = new Data();
    final Data data2 = new Data();
    final Data data3 = new Data();

    String last1 = NOVAL;
    String last2 = NOVAL;
    String last3 = NOVAL;

    final TrafficLightsState tls0 = new TrafficLightsState();
    final TrafficLightsState tls1 = new TrafficLightsState();
    final TrafficLightsState tls2 = new TrafficLightsState();

    protected void doHandle() throws SporticsException {
        boolean loop = true;

        tls0.setText("Air");
        c.setHeaderValue(0, tls0);
        tls1.setText("HxM");
        c.setHeaderValue(1, tls1);
        tls1.setRed();
        tls2.setText("GPS");
        c.setHeaderValue(2, tls2);
        c.setDataValue(0, data0);
        data1.setDescription("Heartrate");
        data1.setUnit("bpm");
        c.setDataValue(1, data1);
        data2.setUnit("km");
        c.setDataValue(2, data2);
        data3.setUnit("km/h");
        c.setDataValue(3, data3);

        final Accumulator accu = Accumulator.getInstance();

        final Sink sink = new Sink() {
            public void sink(final Assemblage toSink) {
                final TypedData tdd = toSink.get(TypedDataDescriptor.DURATION);
                if (tdd != null) {
                    final Variant v = tdd.getValue();
                    if (v.isLong()) {
                        final long duration = v.asLong();
                        final String asString = DateSupport.millisecondsToTime(duration, true);
                        ExtSportSessionController.this.data0.setValue(asString);
                    } else {
                        ExtSportSessionController.this.data0.setValue("noval");
                    }
                } else {
                    ExtSportSessionController.this.data0.setValue("noval");
                }

                final TypedData tdHeartrate = toSink.get(TypedDataDescriptor.HEART_RATE);
                if (tdHeartrate != null) {
                    final Variant v = tdHeartrate.getValue();
                    final String heartrate = v.asString();
                    ExtSportSessionController.this.data1.setValue(heartrate);
                    ExtSportSessionController.this.last1 = heartrate;
                } else {
                    ExtSportSessionController.this.data1.setValue(ExtSportSessionController.this.last1);
                }

                final TypedData tdDistance = toSink.get(TypedDataDescriptor.DISTANCE);
                if (tdDistance != null) {
                    final Variant v = tdDistance.getValue();
                    if (v.isFloat()) {
                        final float m = v.asFloat();
                        final float km = m / 1000;
                        final String distance = String.valueOf(km);
                        final String lengthAdjusted = StringUtil.decimalShorter(distance, 2);
                        ExtSportSessionController.this.data2.setValue(lengthAdjusted);
                        ExtSportSessionController.this.last2 = lengthAdjusted;
                    } else {
                        ExtSportSessionController.this.data2.setValue(ExtSportSessionController.this.last2);
                    }
                } else {
                    ExtSportSessionController.this.data2.setValue(ExtSportSessionController.this.last2);
                }

                final TypedData tdSpeed = toSink.get(TypedDataDescriptor.SPEED);
                if (tdSpeed != null) {
                    final Variant v = tdSpeed.getValue();
                    if (v.isFloat()) {
                        final float mps = v.asFloat();
                        final float kmh = mps * 3.6f;
                        final String speed = String.valueOf(kmh);
                        final String speedAdjusted = StringUtil.decimalShorter(speed, 2);
                        ExtSportSessionController.this.data3.setValue(speedAdjusted);
                        ExtSportSessionController.this.last3 = speedAdjusted;
                    } else {
                        ExtSportSessionController.this.data3.setValue(ExtSportSessionController.this.last3);
                    }
                } else {
                    ExtSportSessionController.this.data3.setValue(ExtSportSessionController.this.last3);
                }

                LOG.debug("Send repaint to #" + ExtSportSessionController.this.c);
                ExtSportSessionController.this.c.repaint();
            }
        };
        accu.registerSink(sink);

        c.addCommand(MainController.PAUSE);
        c.addCommand(MainController.STOP);

        c.setFooterValue(0, State.ACTIVE);

        c.setCommandListener(new CommandListener() {
            public void commandAction(final Command cmd, final Displayable displayable) {
                ExtSportSessionController.this.selected = cmd;
                notifyStateChanged();
            }
        });
        c.setFullScreenMode(true);
        display(c);
        LOG.debug("Send first repaint to #" + ExtSportSessionController.this.c);

        c.repaint();

        creatRtApi();
        accu.registerSink(this.api);

        try {
            this.api.prepare();
            LOG.debug("doHandle) - RT prepared");
            initDevices(accu);
            final Sport currentSport = getSport();
            api.setSport(currentSport);
            this.api.start();
            LOG.debug("doHandle) - RT started");

            while (loop) {
                waitForStateChange();
                if (this.selected == MainController.STOP) {
                    for (final Enumeration e = this.devices.elements(); e.hasMoreElements(); ) {
                        final Object o = e.nextElement();
                        if (o instanceof Startable) {
                            final Startable s = (Startable) o;
                            s.stop();
                        }
                    }
                    this.api.stop();
                    loop = false;
                    continue;
                } else if (this.selected == MainController.PAUSE) {
                    for (final Enumeration e = this.devices.elements(); e.hasMoreElements(); ) {
                        final Object o = e.nextElement();
                        if (o instanceof Pauseable) {
                            final Pauseable p = (Pauseable) o;
                            p.pause();
                        }
                    }
                    this.api.pause();
                    this.c.setFooterValue(0, State.PAUSED);
                    c.removeCommand(MainController.PAUSE);
                    c.addCommand(CONTINUE);
                    c.addCommand(OTHER_SPORT);
                    continue;
                } else if (this.selected == CONTINUE) {
                    for (final Enumeration e = this.devices.elements(); e.hasMoreElements(); ) {
                        final Object o = e.nextElement();
                        if (o instanceof Pauseable) {
                            final Pauseable p = (Pauseable) o;
                            p.keepOn();
                        }
                    }
                    final Sport sport = getSport();
                    final Hashtable parameter = new Hashtable();
                    parameter.put(SrtsApi.KEY_AID, sport);
                    this.api.doContinue(parameter);
                    this.c.setFooterValue(0, State.ACTIVE);
                    c.addCommand(MainController.PAUSE);
                    c.removeCommand(CONTINUE);
                    c.removeCommand(OTHER_SPORT);
                    continue;
                } else if (this.selected == OTHER_SPORT) {
                    final MIDlet midlet = getMIDlet();
                    final Display display = Display.getDisplay(midlet);
                    Displayable current = display.getCurrent();
                    final Sport sport = getSport();
                    final String sportId = sport.getId();
                    final Sport newSport = selectSport(sportId);
                    setSport(newSport);
                    display.setCurrent(current);
                    continue;
                }
            }
        } catch (final Exception e) {
            LOG.warn("doHandle() - Problems in loop: " + e.getMessage() + " - " + e);
        }
    }

    /**
     * Shows the supplied {@code Displayable}.
     * @param displayable the {@code Displayable} to show
     * @throws IllegalArgumentException if and only if the supplied {@code Displayable} is
     *                                  {@code null}
     * @throws IllegalStateException if and only if there is no {@link MIDlet} available
     * @see #getMIDlet()
     */
    void display(final Displayable displayable) {
        if (displayable == null) {
            throw new IllegalArgumentException("displayble is null");
        }
        final MIDlet m = getMIDlet();
        if (m == null) {
            throw new IllegalStateException("midlet is null");
        }
        final Display display = Display.getDisplay(m);
        display.setCurrent(displayable);
    }

    DeviceDb deviceDb() {
        final DeviceDb deviceDb = DeviceDb.getInstance();
        return deviceDb;
    }

    void creatRtApi() {
        final SrtsApiBuilder apiBuilder = SrtsApiBuilder.getInstance();
        final UserController userCtrl = new UserController();
        final String login = userCtrl.getLogin();
        final String password = userCtrl.getPassword();
        apiBuilder.setPassword(password).setSporticsId(login);
        this.api = apiBuilder.build();
        if (this.api.isLifeAllowed()) {
            this.api.addSrtsApiListener(new SrtsApiListener() {
                public void onError(final SrtsApi source,
                                    final int state,
                                    final SrtsApiException exception) {
                    ExtSportSessionController.this.tls2.setRed();
                    ExtSportSessionController.this.c.repaint();
                    LOG.warn("error in SrtsApi in state " + state + " - message: "
                             + exception.getMessage());
                }
                public void onStateChange(final SrtsApi source, final int state) {
                    LOG.info("SrtsApi state: " + state);
                    if (state == SrtsApiListener.STATE_DO_CONTINUE
                            || state == SrtsApiListener.STATE_DO_PAUSE
                            || state == SrtsApiListener.STATE_DO_PREPARE
                            || state == SrtsApiListener.STATE_DO_SNAP
                            || state == SrtsApiListener.STATE_DO_START
                            || state == SrtsApiListener.STATE_DO_STOP) {
                        ExtSportSessionController.this.tls2.setYellow();
                    } else {
                        ExtSportSessionController.this.tls2.setGreen();
                    }
                    ExtSportSessionController.this.c.repaint();
                }
            });
            this.tls0.setGreen();
        } else {
            this.tls0.setRed();
        }
        this.api.addSrtsApiListener(new SrtsApiListener() {

            public void onStateChange(final SrtsApi source, int state) {
                switch(state) {
                    case SrtsApiListener.STATE_DO_CONTINUE:
                        ExtSportSessionController.this.c.setFooterValue(1, "Send continue");
                        ExtSportSessionController.this.c.repaint();
                        break;
                    case SrtsApiListener.STATE_DO_PAUSE:
                        ExtSportSessionController.this.c.setFooterValue(1, "Send pause");
                        ExtSportSessionController.this.c.repaint();
                        break;
                    case SrtsApiListener.STATE_DO_PREPARE:
                        ExtSportSessionController.this.c.setFooterValue(1, "Send prepare");
                        ExtSportSessionController.this.c.repaint();
                        break;
                    case SrtsApiListener.STATE_DO_SNAP:
                        ExtSportSessionController.this.c.setFooterValue(1, "Send snap");
                        ExtSportSessionController.this.c.repaint();
                        break;
                    case SrtsApiListener.STATE_DO_START:
                        ExtSportSessionController.this.c.setFooterValue(1, "Send start");
                        ExtSportSessionController.this.c.repaint();
                        break;
                    case SrtsApiListener.STATE_DO_STOP:
                        ExtSportSessionController.this.c.setFooterValue(1, "Send stop");
                        ExtSportSessionController.this.c.repaint();
                        break;
                    case SrtsApiListener.STATE_DO_MARK:
                        ExtSportSessionController.this.c.setFooterValue(1, "Send marker");
                        ExtSportSessionController.this.c.repaint();
                        break;
                    default:
                        ExtSportSessionController.this.c.setFooterValue(1, null);
                        ExtSportSessionController.this.c.repaint();
                        break;
                }
            }

            public void onError(final SrtsApi source, int state, SrtsApiException exception) {
            }
        });
    }

    private void initDevices(final Accumulator accu) {
        LOG.debug("initDevices() - enter " + accu);
        try {
            this.durationDevice = new DurationDevice();
            this.durationDevice.prepare();
            this.durationDevice.registerTypedDataConsumer(accu);
            this.devices.addElement(this.durationDevice);
            this.durationDevice.start();

            final DeviceDb deviceDb = deviceDb();
            LOG.debug("initDevices() - deviceDb: " + deviceDb);
            final DeviceManager deviceManager = DeviceManager.getInstance();
            LOG.debug("initDevices() - deviceManager: " + deviceManager);
            try {
                final DeviceDb.DeviceDescriptorEnumeration e = deviceDb.enumerateDeviceDescriptors();
                LOG.debug("initDevices() - e: " + e);
                try {
                    while (e.hasMoreElements()) {
                        final DeviceDescriptor dd = (DeviceDescriptor) e.nextElement();
                        LOG.debug("device descriptor: " + dd);
                        final Device device = deviceManager.deviceForDescriptor(dd);
                        LOG.debug("initDevices() - device: " + device);
                        if (device != null) {
                            LOG.debug("device name: " + device.getDeviceName());
                            LOG.debug("device class: " + device.getClass().getName());
                            device.prepare();
                            device.start();
                            device.registerTypedDataConsumer(accu);
                            this.devices.addElement(device);
                            checkForHxM(device);
                        }
                    }
                } finally {
                    e.dispose();
                }
            } catch (final IOException e) {
                LOG.warn("Unexpected IO problem: " + e.getMessage() + " - " + e);
            }
        } catch (final Exception e) {
            LOG.warn("initDevices() - Problems in loop: " + e.getMessage() + " - " + e);
        }
        LOG.debug("initDevices() - leave " + accu);
    }

    final void checkForHxM(final Device device) {
        if (device != null) {
            final String[] types = device.supportedTypedDataList();
            for (int i = 0; i < types.length; i++) {
                if (TypedDataDescriptor.HEART_RATE.equals(types[i])) {
                    this.tls1.setYellow();
                    break;
                }
            }
        }
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

    final ListSelectionController createListSelectionController() {
        final ListSelectionController lsc = new ListSelectionController() {

            protected void doHandle() throws SporticsException {
                final MIDlet midlet = getMIDlet();
                final Display display = Display.getDisplay(midlet);
                final Displayable current = display.getCurrent();
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
}
