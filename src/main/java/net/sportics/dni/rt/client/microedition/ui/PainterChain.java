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

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Graphics;

/**
 * Calls all painters in the given manner.
 * @author Sascha Kohlmann
 */
public final class PainterChain implements Painter {

    private final Vector painters = new Vector();

    /**
     * Constructs a new instance with the given {@code Painter}.
     * <p>A change in the array has no effects to the created instance after
     * {@code new PainterChain(...)} returns.</p>
     * @param painter a painter chain
     * @throws IllegalArgumentException if a reference in the array is {@code null}
     */
    public PainterChain(final Painter[] painter) {
        if (painter != null) {
            for (int i = 0; i < painter.length; i++) {
                if (painter[i] != null) {
                    this.painters.addElement(painter[i]);
                } else {
                    throw new IllegalArgumentException("painter at index " + i + " is null");
                }
            }
        }
    }

    /**
     * @param g the {@link Graphics} context to paint into
     * @param objectToPaintg the object to paint containing the model
     * @param x the x coordinate of the rectangle of the grapics context to paint
     * @param y the y coordinate of the rectangle of the grapics context to paint
     * @param width the width of the rectangle of the grapics context to paint
     * @param height the height of the rectangle of the grapics context to paint
     */
    public void paint(final Graphics g,
                      final Object objectToPaint,
                      final int x,
                      final int y,
                      final int width,
                      final int height) {
        if (g != null) {
            for (final Enumeration e = painters.elements(); e.hasMoreElements(); ) {
                final Painter p = (Painter) e.nextElement();
                p.paint(g, objectToPaint, x, y, width, height);
            }
        }
    }
}

