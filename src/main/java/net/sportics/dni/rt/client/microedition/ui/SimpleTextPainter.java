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

/**
 *
 * @author Sascha Kohlmann
 */
public final class SimpleTextPainter extends BasePainter {

    public static final String AVERAGE_SIGN = "\u00F8";

    private int orientation = Graphics.LEFT;

    public void paint(final Graphics g,
                      final Object objectToPaint,
                      final int x,
                      final int y,
                      final int width,
                      final int height) {
        final int oldColor = g.getColor();
        final Font oldFont = g.getFont();
        final int color = getForegroundColor();
        g.setColor(color);
        final Font font = getFont();
        g.setFont(font);
        final String text = "" + objectToPaint;
        if (this.orientation == Graphics.LEFT) {
            g.drawString(text, x, y, Graphics.TOP | Graphics.LEFT);
        } else {
            final int stringWidth = font.stringWidth(text);
            if (this.orientation == Graphics.RIGHT) {
                final int offsetX = x + width - stringWidth;
                g.drawString(text, offsetX, y, Graphics.TOP | Graphics.LEFT);
            } else {
                final int offsetX = x + ((width - stringWidth) / 2);
                g.drawString(text, offsetX, y, Graphics.TOP | Graphics.LEFT);
            }
        }
        g.setFont(oldFont);
        g.setColor(oldColor);
    }

    public int getOrientation() {
        return this.orientation;
    }

    public void setOrientation(final int orientation) {
        this.orientation = orientation;
    }

}
