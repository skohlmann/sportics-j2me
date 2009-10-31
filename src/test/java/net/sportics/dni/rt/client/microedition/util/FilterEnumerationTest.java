/* (c) 2009 Sascha Kohlmann. All rights reserved. */
package net.sportics.dni.rt.client.microedition.util;

import net.sportics.dni.rt.client.microedition.util.FilterEnumeration;
import java.util.Enumeration;
import java.util.Vector;
import junit.framework.TestCase;

/**
 *
 * @author Sascha Kohlmann
 */
public class FilterEnumerationTest extends TestCase {

    public void testWithNull() {
        try {
            class NullTestFilterEnumeration extends FilterEnumeration {
                public NullTestFilterEnumeration() {
                    super(null);
                }
                public boolean filter(final Object o) {
                    return false;
                }
            }
            new NullTestFilterEnumeration();
            fail("Oops... missing IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
        }
    }

    public void testSimpleFilterWithElements() {
        final Vector v = new Vector();
        v.addElement(new Integer(1));
        v.addElement(new Integer(2));
        v.addElement(new Integer(3));
        v.addElement(new Integer(4));

        final Enumeration orig = v.elements();
        final Enumeration filter = new ModuloFilter(orig);

        int count = 0;
        while(filter.hasMoreElements()) {
            filter.nextElement();
            count++;
        }
        assertEquals(count, 2);
    }

    public void testSimpleFilterWithNoElements() {
        final Vector v = new Vector();
        final Enumeration orig = v.elements();
        final Enumeration filter = new ModuloFilter(orig);

        int count = 0;
        while(filter.hasMoreElements()) {
            filter.nextElement();
            count++;
        }
        assertEquals(count, 0);
    }

    public static final class ModuloFilter extends FilterEnumeration {

        public ModuloFilter(final Enumeration e) {
            super(e);
        }
        public boolean filter(final Object o) {
            if (o instanceof Integer) {
                final int i = ((Integer) o).intValue();
                return i % 2 == 0;
            }
            return false;
        }
    }
}
