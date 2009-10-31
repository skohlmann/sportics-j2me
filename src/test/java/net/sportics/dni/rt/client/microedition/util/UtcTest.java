/** (c) 2009 Sascha Kohlmann. All rights reserved. */
package net.sportics.dni.rt.client.microedition.util;

import net.sportics.dni.rt.client.microedition.util.Utc;
import junit.framework.TestCase;

/**
 *
 * @author Sascha Kohlmann
 */
public class UtcTest extends TestCase {

    public void testUtc0530() {
        final Utc utc = new Utc() {
            public int getRawOffset() {
                return ((5 * 60) + 30) * 60 * 1000;
            }
        };
        final String tz = utc.utc();
        assertEquals("UTC+05:30", tz);
    }

    public void testUtcMinus1201() {
        final Utc utc = new Utc() {
            public int getRawOffset() {
                return ((-12 * 60) - 01) * 60 * 1000;
            }
        };
        final String tz = utc.utc();
        assertEquals("UTC-12:01", tz);
    }

    public void testUtc0() {
        final Utc utc = new Utc() {
            public int getRawOffset() {
                return 0;
            }
        };
        final String tz = utc.utc();
        assertEquals("UTC0", tz);
    }
}
