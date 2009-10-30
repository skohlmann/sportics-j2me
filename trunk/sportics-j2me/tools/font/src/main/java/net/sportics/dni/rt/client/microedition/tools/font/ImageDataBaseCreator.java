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
package net.sportics.dni.rt.client.microedition.tools.font;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Properties;
import javax.imageio.ImageIO;

public class ImageDataBaseCreator {

    private static final Properties baseData = new Properties();

    /**
     * @param args
     */
    public static void main(final String[] args) throws Exception {
        final File basedir = new File(args[0]);
        System.out.println("basedir: " + basedir.getAbsolutePath());
        final File subdirs[] = basedir.listFiles(new FilenameFilter() {
            public boolean accept(final File dir, final String name) {
                if (name.startsWith(".") || name.endsWith(".data")) {
                    return false;
                }
                return true;
            }
        });
        for (final File subdir : subdirs) {
            System.out.println("subdir: " + subdir.getAbsolutePath());
            final String name = subdir.getName();
            final File characters[] = subdir.listFiles(new FileFilter() {
                public boolean accept(final File pathname) {
                    final String name = pathname.getName();
                    if (name.endsWith(".png")) {
                        return true;
                    }
                    return false;
                }
            });
            final File wCharacter = fetchW(characters);
            final int startHeight = startHeight(wCharacter);
            final Properties fontData = new Properties();
            for (final File charFile : characters) {
                String charName = charFile.getName();
                final int indexOfDot = charName.lastIndexOf(".");
                if (indexOfDot != -1) {
                    charName = charName.substring(0, indexOfDot);
                }
                final BufferedImage bi = shrinkImage(charFile, startHeight);
                final int height = bi.getHeight();
                final int width = bi.getWidth();
                System.out.println("font: " + name + " - char: " + charName + " - height="
                                   + height + " - width=" + width);
                if ("56".equals(charName)) {
                    baseData.put(name, "" + width + "," + height);
                }
                fontData.put(charName, "" + width + "," + height);
            }
            final OutputStream out = new FileOutputStream(new File(subdir, "char.data"));
            fontData.store(out, " font metric. key=char, value=width, height");
            out.close();
        }
        final OutputStream out = new FileOutputStream(new File(basedir, "font.data"));
        baseData.store(out, " fonts metric. key=font dir, value=width, height of char ASCII 56");
        System.out.println("basedata: " + baseData);
        out.close();
    }

    static final int startHeight(final File file) throws Exception {
        final BufferedImage bi = ImageIO.read(file);
        final int width = bi.getWidth();
        final int height = bi.getHeight();
        for (int gh = 0; gh < height; gh++) {
            for (int gw = 0; gw < width; gw++) {
                final int argb = bi.getRGB(gw, gh);
                if (argb != 0) {
                    final int retval = gh - 1;
                    if (retval < 0) {
                        return 0;
                    }
                    return retval;
                }
            }
        }
        return 0;
    }

    static final File fetchW(final File[] characters) {
        for (final File file : characters) {
            final String name = file.getName();
            if ("87.png".equals(name)) {
                return file;
            }
        }
        throw new NoSuchElementException();
    }

    private static BufferedImage shrinkImage(final File charFile, final int startHeight)
                throws IOException {
        final BufferedImage oi = ImageIO.read(charFile);
        final int oiHeight = oi.getHeight();
        final int oiWidth = oi.getWidth();
        final BufferedImage bi = oi.getSubimage(0, startHeight, oiWidth, oiHeight - startHeight);
        charFile.delete();
        ImageIO.write(bi, "png", charFile);
        return bi;
    }
}
