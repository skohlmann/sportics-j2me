/* Copyright (C) 2008-2009 Sascha Kohlmann
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

public interface Painter {

    /**
     * @param g the {@link Graphics} context to paint into
     * @param objectToPaintg the object to paint containing the model
     * @param x the x coordinate of the rectangle of the grapics context to paint
     * @param y the y coordinate of the rectangle of the grapics context to paint
     * @param width the width of the rectangle of the grapics context to paint
     * @param height the height of the rectangle of the grapics context to paint
     */
    void paint(final Graphics g,
               final Object objectToPaint,
               final int x,
               final int y,
               final int width,
               final int height);
}

