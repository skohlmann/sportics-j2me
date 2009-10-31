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
public class BackgroundPainter implements Painter {

    public void paint(final Graphics g,
                      final Object objectToPaint,
                      final int x,
                      final int y,
                      final int width,
                      final int height) {
        final int color = g.getColor();
        g.setColor(0);
        g.fillRect(x, y, width, height);
        g.setColor(color);
    }
}
