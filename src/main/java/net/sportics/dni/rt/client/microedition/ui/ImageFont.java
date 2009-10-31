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
package net.sportics.dni.rt.client.microedition.ui;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import net.sportics.dni.rt.client.microedition.util.LogManager;

/**
 * Calculates character names.
 * @author Sascha Kohlmann
 */
final class ImageFont {

    private static final int HEIGHT_MINUS = 2;
    private static LogManager LOG = LogManager.getInstance("ImageFont");
    static {
        LOG.debug("#class: " + ImageTextPainter.class.getName());
    }

    ImageFont() {
    }

    String[] createCharPath(final int maxWidth,
                            final int maxHeight,
                            final String text,
                            final Vector lookup,
                            final Hashtable charData) {
        int height = maxHeight;
        KeyWidthHeight metric = null;
        while (height >= HEIGHT_MINUS && metric == null) {
            final KeyWidthHeight fontMetric = findFontForMaxHeight(height, lookup);
            final String[] charDescriptor = findCharData(fontMetric, text, maxWidth, charData);
            if (charDescriptor != null) {
                return charDescriptor;
            }
            height -= HEIGHT_MINUS;
        }
        return null;
    }

    String[] findCharData(final KeyWidthHeight fontMetric,
                          final String text,
                          final int maxWidth,
                          final Hashtable charData) {
//        LOG.debug("findCharData() enter - fontMetric: " + fontMetric + " - text: " + text
//                  + " - maxWidth: " + maxWidth);
        final Hashtable fontData = (Hashtable) charData.get(fontMetric.key);
        if (fontData == null) {
            return null;
        }
        int width = 0;

//        LOG.debug("findCharData() fontData: " + fontData);
        final char[] chars = text.toCharArray();
        final String[] retval = new String[chars.length];
        for (int i = 0; i < chars.length; i++) {
            final int charAsInt = (int) chars[i];
//            LOG.debug("Char '" + chars[i] + "' as int "+ charAsInt + " at index " + i);
            final Integer charValue = new Integer(charAsInt);
            final WidthHeight wh = (WidthHeight) fontData.get(charValue);
//            LOG.debug("findCharData() wh: " + wh);
            width += wh.width;
            retval[i] = String.valueOf(charAsInt);
        }
        if (width <= maxWidth) {
            final String base = fontMetric.key.toString() + FontManager.PATH_DELIMITER;
            for (int i = 0; i < retval.length; i++) {
                retval[i] = base + retval[i] + FontManager.IMAGE_SUFFIX;
            }
//            LOG.debug("findCharData() leave - retval: " + retval);
            return retval;
        }
//        LOG.debug("findCharData() leave - null");
        return null;
    }

    KeyWidthHeight findFontForMaxHeight(final int maxHeight, final Vector lookup) {
        LOG.debug("findFontForMaxHeight() enter - maxHeight: " + maxHeight);
        KeyWidthHeight found = new KeyWidthHeight(new Integer(0), 0, 0);
        for (final Enumeration e = lookup.elements(); e.hasMoreElements(); ) {
            final KeyWidthHeight metric = (KeyWidthHeight) e.nextElement();
            if (metric.height > found.height && metric.height <= maxHeight) {
                found = metric;
            }
        }
//        LOG.debug("findFontForMaxHeight() leave - found: " + found);
        return found;
    }
}
