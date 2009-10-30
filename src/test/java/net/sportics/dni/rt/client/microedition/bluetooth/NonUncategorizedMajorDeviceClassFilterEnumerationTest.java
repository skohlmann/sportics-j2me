/** (c) 2009 Sascha Kohlmann. All rights reserved. */
package net.sportics.dni.rt.client.microedition.bluetooth;

import net.sportics.dni.rt.client.microedition.bluetooth.NonUncategorizedMajorDeviceClassFilterEnumeration;
import java.util.Vector;
import junit.framework.TestCase;
import net.sportics.dni.rt.client.microedition.Attribute;
import net.sportics.dni.rt.client.microedition.Variant;
import net.sportics.dni.rt.client.microedition.device.DeviceDb;
import net.sportics.dni.rt.client.microedition.device.DeviceDescriptor;

/**
 *
 * @author Sascha Kohlmann
 */
public class NonUncategorizedMajorDeviceClassFilterEnumerationTest extends TestCase {

    private static final NonUncategorizedMajorDeviceClassFilterEnumeration FILTER =
            new NonUncategorizedMajorDeviceClassFilterEnumeration(new Vector().elements());
    private static final Attribute VALID_ATTR =
            new Attribute(DeviceDb.ATTRIBUTE_BLUETOOTH_MAJOR_CLASS, new Variant(123));
    private static final Attribute INVALID_ATTR = new Attribute("test", new Variant(123));

    public void testWithUnsupportedAttribute() {
        final boolean filtered = FILTER.filter(INVALID_ATTR);
        assertFalse(filtered);
    }

    public void testWithSupportedAttribute() {
        final boolean filtered = FILTER.filter(VALID_ATTR);
        assertTrue(filtered);
    }

//    public void testWithUnsupportedDeviceDescriptor() {
//        final DeviceDb db = DeviceDb.getInstance();
//        final DeviceDescriptor dd = db.newDeviceDescriptor();
//        dd.addAttribute(INVALID_ATTR);
//        final boolean filtered = FILTER.filter(dd);
//        assertFalse(filtered);
//    }
//
//    public void testWithSupportedDeviceDescriptor() {
//        final DeviceDescriptor dd = DeviceDb.getInstance().newDeviceDescriptor();
//        dd.addAttribute(VALID_ATTR);
//        final boolean filtered = FILTER.filter(dd);
//        assertTrue(filtered);
//    }

    public void testWithUnsupportedObject() {
        final boolean filtered = FILTER.filter(new Object());
        assertFalse(filtered);
    }
}
