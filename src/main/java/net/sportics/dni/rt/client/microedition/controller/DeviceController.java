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
import java.util.NoSuchElementException;
import java.util.Vector;

import javax.bluetooth.RemoteDevice;
import javax.bluetooth.DeviceClass;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Ticker;

import net.sportics.dni.rt.client.microedition.Attribute;
import net.sportics.dni.rt.client.microedition.SporticsException;
import net.sportics.dni.rt.client.microedition.Variant;
import net.sportics.dni.rt.client.microedition.bluetooth.BluetoothManager;
import net.sportics.dni.rt.client.microedition.bluetooth.RemoteDeviceContainer;
import net.sportics.dni.rt.client.microedition.device.DeviceDb;
import net.sportics.dni.rt.client.microedition.device.DeviceDescriptor;
import net.sportics.dni.rt.client.microedition.device.Jsr179LocationDevice;
import net.sportics.dni.rt.client.microedition.device.DeviceDb.DeviceDescriptorEnumeration;
import net.sportics.dni.rt.client.microedition.util.Environment;
import net.sportics.dni.rt.client.microedition.util.LogManager;

public final class DeviceController extends AbstractController {

    private static final LogManager LOG = LogManager.getInstance("DeviceController");
    static {
        LOG.debug("#class: " + DeviceController.class.getName());
    }

    private static Image SELECTED;
    static {
        try {
            final InputStream in =
                ListSelectionController.class.getResourceAsStream("/images/16x16/view-refresh.png");
            SELECTED = Image.createImage(in);
        } catch (final IOException e) {
            LOG.warn("Unable to load selection image.");
            SELECTED = null;
        }
    }

    static final String SEARCH_TEXT = "Searching for devices - Please wait...   ";

    private Vector devices;
    private Command selected = null;

    protected void doHandle() throws SporticsException {
        LOG.debug("doHandle() enter on " + this.getClass());

        try {
            boolean loop = true;
            this.devices = fillDeviceList();

            while (loop) {
                final List list = createList();
                display(list);
                waitForStateChange();

                LOG.debug("selected Index: " + list.getSelectedIndex());
                if (this.selected == MainController.BACK) {
                    loop = false;
                    continue;
                } else if ((this.selected == List.SELECT_COMMAND
                        || this.selected == MainController.SELECT) && list.isSelected(0)) {
                    final Ticker searchTicker = new Ticker(SEARCH_TEXT);
                    list.setTicker(searchTicker);
                    handleInquireDevices();
                    list.setTicker(null);
                }
            }
            LOG.debug("doHandle() leave on " + this.getClass());
        } catch (final Exception e) {
            LOG.warn("Unexpected error: " + e.getMessage() + " - " + e);
        }
    }

    final Vector fillDeviceList() {
        final Vector v = new Vector();
        final DeviceDb db = DeviceDb.getInstance();
        LOG.debug("DeviceDb: " + db);
        LOG.debug("DeviceController.class: " + this.getClass().getName());
        try {
            final DeviceDescriptorEnumeration e = db.enumerateDeviceDescriptors();
            LOG.debug("DeviceDescriptorEnumeration: " + e);
            try {
                while (e.hasMoreElements()) {
                    final DeviceDescriptor dd = (DeviceDescriptor) e.nextElement();
                    LOG.debug("found: " + dd);
                    v.addElement(dd);
                }
            } catch(final NoSuchElementException e2) {
                e2.printStackTrace();
                final String msg = e2.getMessage();
                LOG.warn("NoSuchElementException: " + msg);
            } finally {
                e.dispose();
            }
        } catch (final IOException e) {
            e.printStackTrace();
            final String msg = e.getMessage();
            LOG.warn("fillDeviceList() - IOException: " + msg + " - " + e + " - " + this);
        }

        return v;
    }

    final void handleInquireDevices() {
        LOG.debug("handleInquireDevices() - start");
        final BluetoothManager manager = BluetoothManager.getInstance();
        final DeviceDb db = DeviceDb.getInstance();
        try {
            final int count = manager.discoverDevices();
            LOG.debug("Found " + count + " device(s)");
            boolean cleaned = false;
            if (count > 0) {
                LOG.debug("DeviceDb: " + db.getClass());
                for (final Enumeration e = this.devices.elements(); e.hasMoreElements(); ) {
                    final Object o = e.nextElement();
                    LOG.debug("Element to delete: " + o);
                    final DeviceDescriptor toDelete = (DeviceDescriptor) o;
                    LOG.debug("delete: " + toDelete);
                    db.removeDeviceDescriptor(toDelete);
                }

                this.devices = new Vector(count + 2);
                cleaned = true;
                for (final Enumeration e = manager.getDevices(); e.hasMoreElements(); ) {
                    final Object o = e.nextElement();
                    final RemoteDevice device = ((RemoteDeviceContainer) o).getRemoteDevice();
                    final DeviceClass devClazz = ((RemoteDeviceContainer) o).getDeviceClass();
                    final String name = device.getFriendlyName(false);
                    final String btAddress = device.getBluetoothAddress();
                    final int btMajor = devClazz.getMajorDeviceClass();
                    final int btMinor = devClazz.getMinorDeviceClass();
                    final int btService = devClazz.getServiceClasses();

                    final Variant nameVariant = new Variant(name);
                    final Variant btAddressVariant = new Variant(btAddress);
                    final Variant isBtVariant = new Variant(true);
                    final Variant isBuilInVariant = new Variant(false);
                    final Variant btMajorVariant = new Variant(btMajor);
                    final Variant btMinorVariant = new Variant(btMinor);
                    final Variant btServiceVariant = new Variant(btService);

                    final Attribute attrName = new Attribute(DeviceDb.ATTRIBUTE_NAME, nameVariant);
                    final Attribute attrAddress =
                        new Attribute(DeviceDb.ATTRIBUTE_BLUETOOTH_ADDRESS, btAddressVariant);
                    final Attribute attrIsBt =
                        new Attribute(DeviceDb.ATTRIBUTE_BLUETOOTH, isBtVariant);
                    final Attribute attrBuildIn =
                        new Attribute(DeviceDb.ATTRIBUTE_BUILD_IN, isBuilInVariant);

                    final Attribute attrBtMajor =
                        new Attribute(DeviceDb.ATTRIBUTE_BLUETOOTH_MAJOR_CLASS, btMajorVariant);
                    final Attribute attrBtMinor =
                        new Attribute(DeviceDb.ATTRIBUTE_BLUETOOTH_MINOR_CLASS, btMinorVariant);
                    final Attribute attrBtService =
                        new Attribute(DeviceDb.ATTRIBUTE_BLUETOOTH_SERVICE_CLASSES,
                                      btServiceVariant);

                    final DeviceDescriptor dd = db.newDeviceDescriptor();
                    dd.addAttribute(attrName);
                    dd.addAttribute(attrAddress);
                    dd.addAttribute(attrIsBt);
                    dd.addAttribute(attrBtMajor);
                    dd.addAttribute(attrBuildIn);
                    dd.addAttribute(attrBtMinor);
                    dd.addAttribute(attrBtService);

                    db.storeDeviceDescriptor(dd);
                    LOG.debug("stored: " + dd);

                    this.devices.addElement(dd);
                }
            }

            LOG.debug("Devices: " + this.devices.size());
        } catch (final IOException e) {
            final String msg = e.getMessage();
            LOG.warn("handleInquireDevices() - IOException: " + msg);
        } finally {
            try {
                doBuildInGps(db);
            } catch (final IOException e) {
                LOG.warn("handleInquireDevices() - unable to handle GPS build in: " + e.getMessage());
            }
        }
    }

    private void doBuildInGps(final DeviceDb db) throws IOException {
        if (Environment.gpsSupported()) {
            final DeviceDescriptorEnumeration e = db.enumerateDeviceDescriptors();
            boolean setBuildIn = true;
            try {
                while (e.hasMoreElements()) {
                    final DeviceDescriptor dd = (DeviceDescriptor) e.nextElement();
                    final Attribute classnameAttr =
                        dd.getAttributeForDescriptor(DeviceDb.ATTRIBUTE_CLASSNAME);
                    if (classnameAttr != null) {
                        final Variant v = classnameAttr.getValue();
                        final String classname = v.asString();
                        if (classname.equals(Jsr179LocationDevice.class.getName())) {
                            setBuildIn = false;
                        }
                    }
                }
            } finally {
                e.dispose();
            }
            if (setBuildIn) {
                final Variant nameVariant = new Variant("GPS Build-in");
                final Variant isBtVariant = new Variant(false);
                final Variant isBuilInVariant = new Variant(true);
                final Variant classNameVariant =
                    new Variant(Jsr179LocationDevice.class.getName());

                final Attribute attrName = new Attribute(DeviceDb.ATTRIBUTE_NAME, nameVariant);
                final Attribute attrIsBt =
                    new Attribute(DeviceDb.ATTRIBUTE_BLUETOOTH, isBtVariant);
                final Attribute attrBuildIn =
                    new Attribute(DeviceDb.ATTRIBUTE_BUILD_IN, isBuilInVariant);
                final Attribute attrClassName =
                    new Attribute(DeviceDb.ATTRIBUTE_CLASSNAME, classNameVariant);

                final DeviceDescriptor dd = db.newDeviceDescriptor();
                dd.addAttribute(attrName);
                dd.addAttribute(attrIsBt);
                dd.addAttribute(attrBuildIn);
                dd.addAttribute(attrClassName);

                db.storeDeviceDescriptor(dd);
                this.devices.addElement(dd);
            }
        } else {
            LOG.debug("No buildin GPS");
        }
    }

    final List createList() {
        final List list = new List("Devices", List.IMPLICIT);

        list.append("Check for Devices", SELECTED);

        for (final Enumeration e = this.devices.elements(); e.hasMoreElements(); ) {
            final DeviceDescriptor dd = (DeviceDescriptor) e.nextElement();
            final Attribute attr = dd.getAttributeForDescriptor(DeviceDb.ATTRIBUTE_NAME);
            final Variant value = attr.getValue();
            list.append(value.asString(), null);
        }

        list.addCommand(MainController.BACK);
        list.addCommand(MainController.SELECT);
        list.setSelectCommand(List.SELECT_COMMAND);
        list.setCommandListener(new CommandListener() {
            public void commandAction(final Command cmd, final Displayable displayable) {
                DeviceController.this.selected = cmd;
                notifyStateChanged();
            }
        });

        return list;
    }
}
