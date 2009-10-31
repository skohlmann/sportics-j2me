/* Copyright (c) 2008-2009 Sascha Kohlmann - all rights reserved */
package net.sportics.dni.rt.client.microedition.util;

import net.sportics.dni.rt.client.microedition.util.Collections;
import junit.framework.Assert;
import junit.framework.TestCase;

public class CollectionsTest extends TestCase {

    public void testStringSort() {
        final String[] toSort = new String[] {"z", "y", "x", "w", "u", "t", "s", "r", "q", "p"};
        Collections.sort(toSort);
        final String[] s = new String[] {"p", "q", "r", "s", "t", "u", "w", "x", "y", "z"};
        for (int i = 0; i < toSort.length; i++) {
            Assert.assertEquals(s[i], toSort[i]);
        }
    }
}
