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
package net.sportics.dni.rt.client.microedition.controller.ui;

import javax.microedition.lcdui.Font;

/**
 *
 * @author Sascha Kohlmann
 */
final class GridCalculator {

    private final int width;
    private final int height;
    private int headerHeight;
    private int footerHeight;
    private int valueCellHeight;
    private int valueCellTitleHeight;
    private int valueCellDataHeight;
    private Font calculationFont;


    public GridCalculator(final int width, final int height) {
        this.width = width;
        this.height = height;
        calculate();
    }

    final void calculate() {
        this.calculationFont =
                Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        final int fontHeight = calculationFont.getHeight();
        this.headerHeight = fontHeight + 1;
        this.footerHeight = this.headerHeight;
        final int maxContentHeight = this.height - this.headerHeight - this.footerHeight;
        this.valueCellHeight = maxContentHeight / 4;
        this.valueCellTitleHeight = this.headerHeight;
        this.valueCellDataHeight = this.valueCellHeight - this.valueCellTitleHeight;
    }

    public int getHeaderY() {
        return 0;
    }

    public int getHeaderHeight() {
        return this.headerHeight;
    }

    public int getFooterY() {
        return this.height - this.footerHeight;
    }

    public int getFooterHeight() {
        return this.footerHeight;
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

    public int getValueCellHeight() {
        return this.valueCellHeight;
    }

    public int getValueCellTitleHeight() {
        return this.valueCellTitleHeight;
    }

    public int getValueCellDataHeight() {
        return this.valueCellDataHeight;
    }

    public int getValueCellCount() {
        return 4;
    }

    public Font getCalculationFont() {
        return this.calculationFont;
    }

    /**
     * @param index starts at 0
     */
    public int getValueCellY(final int index) {
        return (index * this.valueCellHeight) + this.headerHeight;
    }

    public String toString() {
        final StringBuffer sb = new StringBuffer("GridCalculator@");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append("[[width=");
        sb.append(this.width);
        sb.append("][height=");
        sb.append(this.height);
        sb.append("][headerHeight=");
        sb.append(this.headerHeight);
        sb.append("][footerHeight=");
        sb.append(this.footerHeight);
        sb.append("][valueCellHeight=");
        sb.append(this.valueCellHeight);
        sb.append("][valueCellTitleHeight=");
        sb.append(this.valueCellTitleHeight);
        sb.append("][valueCellHeight=");
        sb.append(this.valueCellDataHeight);
        sb.append("]]");

        return sb.toString();
    }
}
