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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.Ticker;
import javax.microedition.midlet.MIDlet;

import net.sportics.dni.rt.client.microedition.Sport;
import net.sportics.dni.rt.client.microedition.SporticsException;
import net.sportics.dni.rt.client.microedition.StorageManager;
import net.sportics.dni.rt.client.microedition.StorageManagerFactory;
import net.sportics.dni.rt.client.microedition.TypedDataDescriptor;
import net.sportics.dni.rt.client.microedition.accu.Accumulator;
import net.sportics.dni.rt.client.microedition.accu.Assemblage;
import net.sportics.dni.rt.client.microedition.accu.Sink;
import net.sportics.dni.rt.client.microedition.accu.WriterSink;
import net.sportics.dni.rt.client.microedition.bluetooth.BluetoothManager;
import net.sportics.dni.rt.client.microedition.device.Device;
import net.sportics.dni.rt.client.microedition.device.DeviceDb;
import net.sportics.dni.rt.client.microedition.device.DeviceDescriptor;
import net.sportics.dni.rt.client.microedition.device.DeviceManager;
import net.sportics.dni.rt.client.microedition.device.DurationDevice;
import net.sportics.dni.rt.client.microedition.device.DeviceDb.DeviceDescriptorEnumeration;
import net.sportics.dni.rt.client.microedition.net.SrtsApi;
import net.sportics.dni.rt.client.microedition.net.SrtsApiBuilder;
import net.sportics.dni.rt.client.microedition.net.SrtsApiException;
import net.sportics.dni.rt.client.microedition.net.SrtsApiListener;
import net.sportics.dni.rt.client.microedition.util.CopyVector;
import net.sportics.dni.rt.client.microedition.util.DateSupport;
import net.sportics.dni.rt.client.microedition.util.Environment;
import net.sportics.dni.rt.client.microedition.util.LogManager;

public final class SimpleSportSessionController extends SportSessionController {

    private static final LogManager LOG = LogManager.getInstance("SessionController");
    static {
        LOG.debug("#class: " + SimpleSportSessionController.class.getName());
    }

    private static final String NOVAL = "-noval-";

    private Command selected = null;
    DurationDevice durationDevice;

    public Command getSelected() {
        return selected;
    }

    public void setSelected(Command selected) {
        this.selected = selected;
    }

    private final Vector devices = new Vector();

    protected void doHandle() throws SporticsException {
        final DeviceDb deviceDb = deviceDb();
        if (deviceDb.countDeviceDescriptors() == 0) {
            
        }

        try {
            final Accumulator accu = Accumulator.getInstance();
            LOG.debug("doHandler() - Device count: " + this.devices.size());

            final SrtsApiBuilder apiBuilder = SrtsApiBuilder.getInstance();
            final UserController userCtrl = new UserController();
            final String login = userCtrl.getLogin();
            final String password = userCtrl.getPassword();
            apiBuilder.setPassword(password).setSporticsId(login);
            final SrtsApi api = apiBuilder.build();
            final Sport localAid = getSport();
            api.setSport(localAid);
            api.addSrtsApiListener(new SrtsApiListener() {
                public void onError(final SrtsApi source,
                                    final int state,
                                    final SrtsApiException exception) {
                    LOG.warn("error in SrtsApi in state " + state + " - message: "
                             + exception.getMessage());
                }
                public void onStateChange(final SrtsApi source, final int state) {
                    LOG.info("SrtsApi state: " + state);
                }
            });

            accu.registerSink(api);
            api.prepare();
            initDevices(accu);
            api.start();

            final TextBox coordinateForm = new TextBox("Workout Data",
                                                       "",
                                                       2000,
                                                       TextField.UNEDITABLE);
            coordinateForm.addCommand(MainController.BACK);
            coordinateForm.setCommandListener(new CommandListener() {
                public void commandAction(final Command cmd, final Displayable displayable) {
                    LOG.debug("coordinateForm CommandListener: #class: "
                              + this.getClass().getName());
                    SimpleSportSessionController.this.selected = cmd;
                    notifyStateChanged();
                }
            });

            final class InnerSink implements Sink {
                public void sink(final Assemblage toSink) {
                    LOG.debug("InnerSink: " + toSink);
                    final StringBuffer sb = new StringBuffer();
                    sb.append("ts: ");
                    sb.append(DateSupport.currentTimeAsCalendar());
                    sb.append("\nlatitude: ");
                    if (toSink.contains(TypedDataDescriptor.LATITUDE)) {
                        sb.append(toSink.get(TypedDataDescriptor.LATITUDE).getValue().asString());
                    } else {
                        sb.append(NOVAL);
                    }
                    sb.append("\nlongitude: ");
                    if (toSink.contains(TypedDataDescriptor.LONGITUDE)) {
                        sb.append(toSink.get(TypedDataDescriptor.LONGITUDE).getValue().asString());
                    } else {
                        sb.append(NOVAL);
                    }
                    sb.append("\nheart rate: ");
                    if (toSink.contains(TypedDataDescriptor.HEART_RATE)) {
                        sb.append(toSink.get(TypedDataDescriptor.HEART_RATE).getValue().asString());
                    } else {
                        sb.append(NOVAL);
                    }
                    sb.append("\nduration: ");
                    if (toSink.contains(TypedDataDescriptor.DURATION)) {
                        final long duration = 
                            toSink.get(TypedDataDescriptor.DURATION).getValue().asLong();
                        final String asString = DateSupport.secondsToTime(duration);
                        
                        sb.append(asString);
                    } else {
                        sb.append(NOVAL);
                    }
                    sb.append("\nspeed: ");
                    if (toSink.contains(TypedDataDescriptor.SPEED)) {
                        final float speed = 
                            toSink.get(TypedDataDescriptor.SPEED).getValue().asFloat();
                        if (speed == Float.NaN) {
                            sb.append(NOVAL);
                        } else {
                            sb.append(speed);
                        }
                    } else {
                        sb.append(NOVAL);
                    }
                    sb.append("\ndistance: ");
                    if (toSink.contains(TypedDataDescriptor.DISTANCE)) {
                        final double distance = 
                            toSink.get(TypedDataDescriptor.DISTANCE).getValue().asDouble();
                        sb.append(distance / 1000.0d);
                    } else {
                        sb.append(NOVAL);
                    }
                    sb.append("\npace: ");
                    if (toSink.contains(TypedDataDescriptor.PACE)) {
                        final double pace = 
                            toSink.get(TypedDataDescriptor.PACE).getValue().asDouble();
                        sb.append(pace);
                    } else {
                        sb.append(NOVAL);
                    }

                    sb.append("\nHxM strides: ");
                    if (toSink.contains(TypedDataDescriptor.STRIDES)) {
                        final String strides = 
                            toSink.get(TypedDataDescriptor.STRIDES).getValue().asString();
                        sb.append(strides);
                    } else {
                        sb.append(NOVAL);
                    }
                    sb.append("\nHxM cadence: ");
                    if (toSink.contains(TypedDataDescriptor.CADENCE)) {
                        final String cadence = 
                            toSink.get(TypedDataDescriptor.CADENCE).getValue().asString();
                        sb.append(cadence);
                    } else {
                        sb.append(NOVAL);
                    }
                    sb.append("\nHxM battery: ");
                    if (toSink.contains(TypedDataDescriptor.POWER_LEVEL)) {
                        final String battery = 
                            toSink.get(TypedDataDescriptor.POWER_LEVEL).getValue().asString();
                        sb.append(battery);
                    } else {
                        sb.append(NOVAL);
                    }
                    coordinateForm.setString(sb.toString());
                }
            }
            final Sink sink = new InnerSink();
            accu.registerSink(sink);

            Writer assemblageWriter = null;
            try {
                WriterSink writerSink = null;
                if (Environment.storageSupported()) {
                    final StorageManagerFactory factory = StorageManagerFactory.getInstance();
                    final StorageManager manager = factory.getManager();
                    final String name = DateSupport.currentTimeAsCalendar() + ".dat";
                    final OutputStream out = manager.newOutputStream(name);
                    assemblageWriter = new OutputStreamWriter(out) {
                        public void close() throws IOException {
                            LOG.info("close called"); // Later remove.Only in beta phase
                            super.close();
                        }
                    };
                    writerSink = new WriterSink(assemblageWriter);
                }
                accu.registerSink(writerSink);
            } catch (final SecurityException se) {
                LOG.warn("Unable to open writer sink: " + se.getMessage());
            }
            display(coordinateForm);

            boolean loop = true;
            while (loop) {
                waitForStateChange();
                LOG.debug("Wakeup with command: " + this.selected.getLabel());
                if (this.selected == MainController.BACK) {
                    loop = false;
                    api.stop();
                    final Vector copy = new CopyVector(this.devices);
                    LOG.debug("unregister divces");
                    for (final Enumeration e2 = copy.elements(); e2.hasMoreElements(); ) {
                        final Device d = (Device) e2.nextElement();
                        d.unregisterTypedDataConsumer(accu);
                        d.stop();
                    }
                    LOG.debug("unregister sink from accu");
                    accu.unregisterSink(sink);
                    LOG.debug("close assemblage writer");
                    if (assemblageWriter != null) {
                        assemblageWriter.close();
                    }
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
            final String msg = e.getMessage();
            throw new SporticsException(msg);
        }
    }

    DeviceDb deviceDb() {
        final DeviceDb deviceDb = DeviceDb.getInstance();
        return deviceDb;
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
}
