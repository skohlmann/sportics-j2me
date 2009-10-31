/* Copyright (c) 2008-2009 Sascha Kohlmann - all rights reserved */
package net.sportics.dni.rt.client.microedition.bluetooth.device.zephyr;

import net.sportics.dni.rt.client.microedition.bluetooth.device.zephyr.*;
import net.sportics.dni.rt.client.microedition.bluetooth.device.zephyr.HxMDevice;
import junit.framework.TestCase;

public class HxmDeviceTest extends TestCase {

    public void testStrides() {
        final HxMDevice hxm = new HxMDevice();
        assertEquals(1, hxm.handleStrides(1));
        assertEquals(2, hxm.handleStrides(2));
        assertEquals(257, hxm.handleStrides(1));
        assertEquals(257, hxm.handleStrides(1));
        assertEquals(258, hxm.handleStrides(2));
    }

    public void testReadUnsignedByte() {
        final byte b1 = 1;
        assertEquals(1, HxMDevice.readUnsignedByte(b1));
        final byte b2 = 127;
        assertEquals(127, HxMDevice.readUnsignedByte(b2));
        final byte b3 = -1;
        assertEquals(255, HxMDevice.readUnsignedByte(b3));
        final byte b4 = -127;
        assertEquals(129, HxMDevice.readUnsignedByte(b4));
        final byte b5 = -128;
        assertEquals(128, HxMDevice.readUnsignedByte(b5));
    }
}
