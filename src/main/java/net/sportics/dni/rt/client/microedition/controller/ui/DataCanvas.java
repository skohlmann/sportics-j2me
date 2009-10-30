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

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import net.sportics.dni.rt.client.microedition.ui.ImageTextPainter;
import net.sportics.dni.rt.client.microedition.ui.SimpleTextPainter;
import net.sportics.dni.rt.client.microedition.ui.TrafficLightsState;
import net.sportics.dni.rt.client.microedition.ui.TrafficLightsStatePainter;
import net.sportics.dni.rt.client.microedition.util.LogManager;

/**
 *
 * @author Sascha Kohlmann
 */
final class DataCanvas extends Canvas {
    private static final LogManager LOG = LogManager.getInstance("DataCanvas");
    static {
        LOG.debug("#class: " + ExtSportSessionController.class.getName());
    }

    private static final int MAX_DATA_FIELDS = 4;
    private static final int MAX_HEADER_FIELDS = 3;
    private static final int MAX_FOOTER_FIELDS = 2;

    private final Object[] headerFields = new Object[MAX_HEADER_FIELDS];
    private final Object[] footerFields = new Object[MAX_FOOTER_FIELDS];
    private final Object[] dataFields = new Object[MAX_DATA_FIELDS];

    public int headerFields() {
        return MAX_HEADER_FIELDS;
    }

    public int footerFields() {
        return MAX_FOOTER_FIELDS;
    }

    public int dataFields() {
        return MAX_DATA_FIELDS;
    }

    public void setHeaderValue(final int index, final Object o) {
        this.headerFields[index] = o;
    }

    public Object getHeaderValue(final int index) {
        return this.headerFields[index];
    }

    public void setFooterValue(final int index, final Object o) {
        this.footerFields[index] = o;
    }

    public Object getFooterValue(final int index) {
        return this.footerFields[index];
    }

    public void setDataValue(final int index, final Object o) {
        this.dataFields[index] = o;
    }

    public Object getDataValue(final int index) {
        return this.dataFields[index];
    }

    boolean first = true;

    protected void paint(final Graphics g) {
        try {
            final int height = getHeight();
            final int width = getWidth();
            LOG.debug("paint() start for width: " + width + " - height: " + height);
            if (this.first) {
                g.setColor(0);
                g.fillRect(0, 0, width, height);
                this.first = true;
            }
            final GridCalculator grid = new GridCalculator(width, height);

            g.setColor(0);

            final ImageTextPainter itp = new ImageTextPainter();
            final SimpleTextPainter stp = new SimpleTextPainter();
            final Font smallFont = grid.getCalculationFont();
            stp.setFont(smallFont);
            stp.setForegroundColor(0xff6600);
            final int valueCellDataHeight = grid.getValueCellDataHeight();
            final int valueCellTitleHeight = grid.getValueCellTitleHeight();

            for (int i = 0; i < MAX_DATA_FIELDS; i++) {
                final Object o = getDataValue(i);
                if (o != null && o instanceof Data) {
                    final Data data = (Data) o;
                    final int offset = grid.getValueCellY(i) + valueCellTitleHeight;
                    final int valueCellTitleHeightOffset = offset - valueCellTitleHeight;
                    final String desc = data.getDescription();
                    if (desc != null && desc.length() != 0) {
                        stp.setOrientation(Graphics.LEFT);
                        stp.paint(g, desc, 0, valueCellTitleHeightOffset, width, valueCellTitleHeight);
                    }
                    final String unit = data.getUnit();
                    if (unit != null && unit.length() != 0) {
                        stp.setOrientation(Graphics.RIGHT);
                        stp.paint(g, unit, 0, valueCellTitleHeightOffset, width, valueCellTitleHeight);
                    }
                    final String value = data.getValue();
                    final String toDisplay = variantToDisplayString(value);
                    itp.paint(g, toDisplay , 0, offset, width, valueCellDataHeight);
                }
            }
            LOG.debug("paint() data painted");

            final int triple = width / MAX_HEADER_FIELDS;
            int offsetX = 0;
            final TrafficLightsStatePainter tlsp = new TrafficLightsStatePainter();
            final int headerHeight = grid.getHeaderHeight();
            final int headerY = grid.getHeaderY();
            tlsp.setFont(smallFont);
            tlsp.setForegroundColor(0xaaaaaa);

            for (int i = 0; i < MAX_HEADER_FIELDS; i++) {
                final Object o = this.headerFields[i];
                if (o != null && o instanceof TrafficLightsState) {
                    final TrafficLightsState tls = (TrafficLightsState) o;
                    tlsp.paint(g, tls, offsetX, headerY, triple, headerHeight);
                    offsetX += triple;
                }
            }
            LOG.debug("paint() header painted");

            if (footerFields[0] != null && footerFields[0] instanceof State) {
                stp.setForegroundColor(0xaaaaaa);
                final int footerY = grid.getFooterY();
                final int footerHeight = grid.getFooterHeight();
                stp.setOrientation(Graphics.LEFT);
                final String text = ((State) footerFields[0]).getStateAsText();
                stp.paint(g, text, 0, footerY, width / 2, footerHeight);
            }
            if (footerFields[1] != null) {
                stp.setForegroundColor(0xaaaaaa);
                final int footerY = grid.getFooterY();
                final int footerHeight = grid.getFooterHeight();
                stp.setOrientation(Graphics.RIGHT);
                final int x = width / 2;
                stp.paint(g, footerFields[1], x, footerY, x, footerHeight);
            }
            LOG.debug("paint() finish for width: " + width + " - height: " + height);
        } catch (final Exception e) {
            LOG.warn("Exception catched with message: " + e.getMessage() + " - " + e);
        }
    }

    protected void sizeChanged(final int w, final int h) {
        LOG.config("sizeChanged: " + w + " " + h);
        this.first = true;
        LOG.debug("Send sizeChanged repaint to #" + this);
        repaint();
    }

    private String variantToDisplayString(final String value) {
        if (value == null || value.length() == 0 || value.equals(ExtSportSessionController.NOVAL)) {
            return "---";
        }
        return value;
    }
}
