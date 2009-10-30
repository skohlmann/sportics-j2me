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

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.lcdui.Image;
import net.sportics.dni.rt.client.microedition.util.LogManager;
import net.sportics.dni.rt.client.microedition.util.Properties;
import org.bouncycastle.util.Strings;

/**
 *
 * @author Sascha Kohlmann
 */
public final class FontManager {

    // TODO: The implementation contains room for improvement to reduce heap usage.
    //       1. reuse Integer objects
    //       2. reuse WithHeight objects
    private static LogManager LOG = LogManager.getInstance("FontManager");

    private static final FontManager INSTANCE = new FontManager();
    static final String BASE_PATH = "/images/fonts/";
    static final String IMAGE_SUFFIX = ".png";
    static final String PATH_DELIMITER = "/";

    /** The vector contains {@link KeyWidthHeight} entries. */
    private final Vector lookup = new Vector();
    /** The vector contains {@link Integer}s as key and {@link WidthHeight} as values. */
    private final Hashtable charData = new Hashtable();
    private final ImageFont imageFont = new ImageFont();
    private final Hashtable imageMap = new Hashtable();

    public static FontManager getInstance() {
        return INSTANCE;
    }

    private FontManager() {
        LOG.info("start init FontManager #class: " + FontManager.class.getName());
        initLookup();
        initCharData();
        LOG.info("finish init FontManager");
    }

    public Image[] imagesForText(final String text, final int maxWidth, final int maxHeight)
            throws Exception {
//        LOG.debug("imagesForText() enter - text: " + text + " - maxWidth: " + maxWidth
//                  + " - maxHeight: " + maxHeight);
        final String[] charPathFragments =
            this.imageFont.createCharPath(maxWidth, maxHeight, text, lookup, charData);
//        LOG.debug("imagesForText() charPathFragments: " + charPathFragments);
        try {
            if (charPathFragments != null) {
                final Image[] images = new Image[charPathFragments.length];
                for (int i = 0; i < charPathFragments.length; i++) {
//                    LOG.debug("imagesForText() do fetch ");
                    fetchImages(images, i, charPathFragments);
                }
                return images;
            }
        } catch (final Exception e) {
            LOG.warn("An exception occured - " + e.getMessage() + " - " + e.getClass());
        }
        return null;
    }

    public String baseFont(final String text, final int maxWidth, final int maxHeight) {
//        LOG.debug("baseFont() enter - text: " + text + " - maxWidth: " + maxWidth
//                  + " - maxHeight: " + maxHeight);
        final String[] charPathFragments =
            this.imageFont.createCharPath(maxWidth, maxHeight, text, lookup, charData);
        if (charPathFragments != null && charPathFragments.length > 0) {
            final int firstIndex = charPathFragments[0].indexOf(PATH_DELIMITER);
            if (firstIndex >= 1) {
                return charPathFragments[0].substring(0, firstIndex - 1);
            }
        }
        return null;
    }

    void initCharData() {
        for (final Enumeration e = this.lookup.elements(); e.hasMoreElements();) {
            final KeyWidthHeight keyWidthHeight = (KeyWidthHeight) e.nextElement();
            final String path = BASE_PATH + keyWidthHeight.key + "/char.data";
            final InputStream in = FontManager.class.getResourceAsStream(path);
//            LOG.debug("InputStream for path " + path + ": " + in);
            final Properties props = new Properties();
            try {
                try {
                    props.load(in);
                    final Hashtable innerMap = new Hashtable();
                    for (final Enumeration inner = props.keys(); inner.hasMoreElements();) {
                        final String innerKey = (String) inner.nextElement();
                        final String value = props.getProperty(innerKey);
                        final String[] widthHeight = Strings.split(value, ',');

                        final int width = Integer.parseInt(widthHeight[0]);
                        final int height = Integer.parseInt(widthHeight[1]);
                        final WidthHeight wh = new WidthHeight(width, height);
                        final int innerKeyAsInt = Integer.parseInt(innerKey);
                        final Integer innerKeyAsInteger = new Integer(innerKeyAsInt);
                        innerMap.put(innerKeyAsInteger, wh);
                        LOG.config("Init char metric for font \"" + keyWidthHeight.key
                                   + "\" and char '" + ((char) Integer.parseInt(innerKey))
                                   + "': " + wh);

                    }
                    this.charData.put(keyWidthHeight.key, innerMap);
                } finally {
                    in.close();
                }
            } catch (final NumberFormatException ex) {
                LOG.warn("Unable to transform character into int: " + ex.getMessage());
            } catch (final Exception ex) {
                LOG.warn("Exception occured: " + ex.getMessage() + " - " + ex.getClass());
            }
        }
    }

    void initLookup() {
        final String fontDataPath = BASE_PATH + "font.data";
        final InputStream in = FontManager.class.getResourceAsStream(fontDataPath);
//        LOG.debug("InputStream for path " + fontDataPath + ": " + in);
        final Properties props = new Properties();
        try {
            try {
                props.load(in);
                for (final Enumeration e = props.keys(); e.hasMoreElements();) {
                    final String key = (String) e.nextElement();
                    final String value = props.getProperty(key);
                    final String[] widthHeight = Strings.split(value, ',');
                    final int width = Integer.parseInt(widthHeight[0]);
                    final int height = Integer.parseInt(widthHeight[1]);
                    final Integer inKey = new Integer(Integer.parseInt(key));
                    final KeyWidthHeight wh = new KeyWidthHeight(inKey, width, height);
                    lookup.addElement(wh);
                    LOG.config("Init font metric for: " + wh);
                }
            } finally {
                in.close();
            }
        } catch (final NumberFormatException ex) {
            LOG.warn("Unable to transform character into int: " + ex.getMessage());
        } catch (final Exception ex) {
            LOG.warn("Exception occured: " + ex.getMessage() + " - " + ex.getClass());
        }
    }

    private void fetchImages(final Image[] images, int i, final String[] charPathFragments)
            throws IOException {
        images[i] = (Image) this.imageMap.get(charPathFragments[i]);
        if (images[i] == null) {
            final String path = BASE_PATH + charPathFragments[i];
//            LOG.debug("Lookup for image " + path);
            images[i] = Image.createImage(path);
//            LOG.debug("Loaded image " + images[i] + " for path " + path);
            this.imageMap.put(charPathFragments[i], images[i]);
        }
    }
}
