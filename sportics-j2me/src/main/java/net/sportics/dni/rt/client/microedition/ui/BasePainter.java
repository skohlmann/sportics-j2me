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

/**
 *
 * @author carbonlogistics
 */
public abstract class BasePainter implements Painter {

    private static final PainterConfigurator CONFIGURATOR = new PainterConfigurator();
    public BasePainter() {
        CONFIGURATOR.configure(this);
    }

    private Font font = Font.getDefaultFont();
    private int backgroundColor = 0;
    private int textColor = 0xaaaaaa;

    public int getBackgroundColor() {
        return this.backgroundColor;
    }

    public void setBackgroundColor(final int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Font getFont() {
        return this.font;
    }

    public void setFont(final Font font) {
        this.font = font;
    }

    public int getForegroundColor() {
        return this.textColor;
    }

    public void setForegroundColor(final int foregroundColor) {
        this.textColor = foregroundColor;
    }
}
