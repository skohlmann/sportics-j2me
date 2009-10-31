/* (c) 2009 Sascha Kohlmann. All rights reserved. */
package net.sportics.dni.rt.client.microedition.ui;

import java.util.Hashtable;
import java.util.Vector;
import junit.framework.TestCase;

/**
 *
 * @author Sascha Kohlmann
 */
public class ImageFontTest extends TestCase {

    public void test_findFontForMaxHeight() {
        final Vector metrics = new Vector();
        final KeyWidthHeight font1 = new KeyWidthHeight(new Integer(1), 1, 1);
        final KeyWidthHeight font2 = new KeyWidthHeight(new Integer(2), 1, 2);
        final KeyWidthHeight font3 = new KeyWidthHeight(new Integer(3), 1, 3);
        metrics.addElement(font1);
        metrics.addElement(font2);
        metrics.addElement(font3);

        final ImageFont imageFont = new ImageFont();
        final KeyWidthHeight metric1 = imageFont.findFontForMaxHeight(1, metrics);
        final KeyWidthHeight metric2 = imageFont.findFontForMaxHeight(2, metrics);
        final KeyWidthHeight metric3 = imageFont.findFontForMaxHeight(3, metrics);
        final KeyWidthHeight metric4 = imageFont.findFontForMaxHeight(4, metrics);

        assertEquals(metric1, font1);
        assertEquals(metric2, font2);
        assertEquals(metric3, font3);
        assertEquals(metric4, font3);
    }

    public void test_createCharPath_lookupTwice() {
        final Integer one = new Integer(1);
        final Integer two = new Integer(2);
        final Integer three = new Integer(3);
        final KeyWidthHeight font1 = new KeyWidthHeight(one, 3, 2);
        final KeyWidthHeight font2 = new KeyWidthHeight(two, 4, 4);
        final KeyWidthHeight font3 = new KeyWidthHeight(three, 7, 6);

        final Vector fontMetric = new Vector();
        fontMetric.addElement(font1);
        fontMetric.addElement(font2);
        fontMetric.addElement(font3);

        final Hashtable fontData = new Hashtable();

        final Hashtable charData1 = new Hashtable();
        final WidthHeight wh11 = new WidthHeight(2, 2);
        final Integer wh11Key = new Integer('a');
        final WidthHeight wh21 = new WidthHeight(3, 2);
        final Integer wh21Key = new Integer('b');
        charData1.put(wh11Key, wh11);
        charData1.put(wh21Key, wh21);
        fontData.put(one, charData1);

        final Hashtable charData2 = new Hashtable();
        final WidthHeight wh12 = new WidthHeight(4, 4);
        final Integer wh12Key = new Integer('a');
        final WidthHeight wh22 = new WidthHeight(5, 4);
        final Integer wh22Key = new Integer('b');
        charData2.put(wh12Key, wh12);
        charData2.put(wh22Key, wh22);
        fontData.put(two, charData2);

        final Hashtable charData3 = new Hashtable();
        final WidthHeight wh13 = new WidthHeight(6, 6);
        final Integer wh13Key = new Integer('a');
        final WidthHeight wh23 = new WidthHeight(7, 6);
        final Integer wh23Key = new Integer('b');
        charData3.put(wh13Key, wh13);
        charData3.put(wh23Key, wh23);
        fontData.put(three, charData2);

        final ImageFont imageFont = new ImageFont();
        final String[] imagePaths1 = imageFont.createCharPath(8, 4, "ab", fontMetric, fontData);
        assertNotNull(imagePaths1);
        assertEquals(2, imagePaths1.length);
        assertEquals(imagePaths1[0], "1/97.png");

        final String[] imagePaths2 = imageFont.createCharPath(9, 4, "ab", fontMetric, fontData);
        assertNotNull(imagePaths2);
        assertEquals(2, imagePaths2.length);
        assertEquals(imagePaths2[0], "2/97.png");
    }

    public void test_findCharData_with_matching_fontData() {
        final Integer one = new Integer(1);
        final KeyWidthHeight font1 = new KeyWidthHeight(one, 1, 1);
        final Hashtable fontData = createFontData(one);

        final ImageFont imageFont = new ImageFont();
        final String[] imagePaths = imageFont.findCharData(font1, "ab", 5, fontData);
        assertNotNull(imagePaths);
        assertEquals(2, imagePaths.length);
        assertEquals(imagePaths[0], "1/97.png");
        assertEquals(imagePaths[1], "1/98.png");
    }

    public void test_findCharData_with_toLong_text() {
        final Integer one = new Integer(1);
        final KeyWidthHeight font1 = new KeyWidthHeight(one, 1, 1);
        final Hashtable fontData = createFontData(one);

        final ImageFont imageFont = new ImageFont();
        final String[] imagePaths = imageFont.findCharData(font1, "ab", 4, fontData);
        assertNull(imagePaths);
    }

    final Hashtable createFontData(final Integer one) {
        final Hashtable charData = new Hashtable();
        final WidthHeight wh1 = new WidthHeight(2, 3);
        final Integer wh1Key = new Integer('a');
        final WidthHeight wh2 = new WidthHeight(3, 4);
        final Integer wh2Key = new Integer('b');
        charData.put(wh1Key, wh1);
        charData.put(wh2Key, wh2);
        final Hashtable fontData = new Hashtable();
        fontData.put(one, charData);
        return fontData;
    }
}
