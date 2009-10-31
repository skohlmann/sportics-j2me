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

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import net.sportics.dni.rt.client.microedition.util.LogManager;

/**
 *
 * @author Sascha Kohlmann
 */
public final class ImageTextPainter implements Painter {
    private static LogManager LOG = LogManager.getInstance("ImageTextPainter");
    static {
        LOG.debug("#class: " + ImageTextPainter.class.getName());
    }

    private String asText(final Object o) {
        if (o == null) {
            return null;
        }
        return o.toString();
    }

    public void paint(final Graphics g,
                      final Object objectToPaint,
                      final int x,
                      final int y,
                      final int width,
                      final int height) {
        final String text = asText(objectToPaint);
        if (text == null || text.length() == 0) {
            return;
        }
        try {
            final FontManager fm = FontManager.getInstance();
            final Image[] images = fm.imagesForText(text, width, height);
            if (images == null) {
                fallback(g, text, x, y, width, height);
                return;
            }
            final int imageHeight = images[0].getHeight();
            final int yOff = ((height - imageHeight) / 2) + y;
            int maxWidth = 0;
            for (int i = 0; i < images.length; i++) {
                if (images[i] != null) {
                    maxWidth += images[i].getWidth();
                }
            }
            final int oldColor = g.getColor();
//            g.setColor(0xffffff);
//            g.drawLine(0, y, 18, y);
//            g.drawLine(0, y + height, 36, y + height);
            g.setColor(oldColor);
            int xOff = ((width - maxWidth) / 2) + x;
            for (int i = 0; i < images.length; i++) {
                if (images[i] != null) {
                    g.drawImage(images[i], xOff, yOff, Graphics.TOP | Graphics.LEFT);
                    xOff += images[i].getWidth();
                }
            }

        } catch (final Exception e) {
            LOG.warn("Exception occured: " + e.getMessage() + " - Exception: " + e.getClass());
            e.printStackTrace();
            fallback(g, text, x, y, width, height);
        }
    }

    private final void fallback(final Graphics g,
                                final String text,
                                final int x,
                                final int y,
                                final int width,
                                final int height) {
        final Font font = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_BOLD, Font.SIZE_LARGE);
        final int textWidth = font.stringWidth(text);
        final int xOff = ((width - textWidth) / 2) + x;
        final int fontHeight = font.getHeight();
        final int yOff = ((height - fontHeight) / 2) + y;
        final int oldColor = g.getColor();
        final Font oldFont = g.getFont();
        g.setFont(font);
        g.setColor(0xff6600);
        g.drawString(text, xOff, yOff, Graphics.TOP | Graphics.LEFT);
        g.setColor(oldColor);
        g.setFont(oldFont);
    }
}
