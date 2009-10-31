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

import javax.microedition.lcdui.Graphics;

/**
 *
 * @author Sascha Kohlmann
 */
public final class HorizontalLinePainter extends BasePainter {

    private int lineColor = 0x444444;

    public void paint(final Graphics g,
                      final Object objectToPaint,
                      final int x,
                      final int y,
                      final int width,
                      final int height) {
        final int oldColor = g.getColor();
        final int color = getForegroundColor();
        g.setColor(lineColor);
        g.fillRect(x, y, width, height);
        g.setColor(oldColor);
    }

    public void setLineColor(final int lineColor) {
        this.lineColor = lineColor;
    }

    public int getLineColor() {
        return this.lineColor;
    }
}
