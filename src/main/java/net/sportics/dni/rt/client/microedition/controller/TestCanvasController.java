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
package net.sportics.dni.rt.client.microedition.controller;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.midlet.MIDlet;

import net.sportics.dni.rt.client.microedition.SporticsException;
import net.sportics.dni.rt.client.microedition.TypedData;
import net.sportics.dni.rt.client.microedition.TypedDataDescriptor;
import net.sportics.dni.rt.client.microedition.Variant;
import net.sportics.dni.rt.client.microedition.accu.Accumulator;
import net.sportics.dni.rt.client.microedition.accu.Assemblage;
import net.sportics.dni.rt.client.microedition.accu.Sink;
import net.sportics.dni.rt.client.microedition.device.DurationDevice;
import net.sportics.dni.rt.client.microedition.ui.ImageTextPainter;
import net.sportics.dni.rt.client.microedition.ui.SimpleTextPainter;
import net.sportics.dni.rt.client.microedition.ui.TrafficLightsState;
import net.sportics.dni.rt.client.microedition.ui.TrafficLightsStatePainter;
import net.sportics.dni.rt.client.microedition.util.DateSupport;
import net.sportics.dni.rt.client.microedition.util.LogManager;

public class TestCanvasController extends AbstractController {

    private static final LogManager LOG = LogManager.getInstance("TestCanvasController");
    static {
        LOG.debug("#class: " + TestCanvasController.class.getName());
    }

    boolean first = true;
    private Command selected = null;
    private String duration = "00:00:00";

    final DurationDevice durationDevice = new DurationDevice();
    private final Canvas c = new Canvas() {
        protected void paint(final Graphics g) {

            final int height = getHeight();
            final int width = getWidth();
            if (first) {
                g.setColor(0);
                g.fillRect(0, 0, width, height);
            }
            final GridCalculator grid = new GridCalculator(width, height);

            g.setColor(0);

            final ImageTextPainter itp = new ImageTextPainter();
            final int valueCellDataHeight = grid.getValueCellDataHeight();
            final int valueCellTitleHeight = grid.getValueCellTitleHeight();
            final int offsetY0 = grid.getValueCellY(0) + valueCellTitleHeight;
            itp.paint(g, TestCanvasController.this.duration , 0, offsetY0, width, valueCellDataHeight);
            final int offsetY1 = grid.getValueCellY(1) + valueCellTitleHeight;
            itp.paint(g, "123" , 0, offsetY1, width, valueCellDataHeight);
            final int offsetY2 = grid.getValueCellY(2) + valueCellTitleHeight;
            itp.paint(g, "2.45" , 0, offsetY2, width, valueCellDataHeight);
            final int offsetY3 = grid.getValueCellY(3) + valueCellTitleHeight;
            itp.paint(g, "123.89" , 0, offsetY3, width, valueCellDataHeight);

            final Font smallFont = grid.getCalculationFont();
            final SimpleTextPainter stp = new SimpleTextPainter();
            stp.setFont(smallFont);
            stp.setForegroundColor(0xff6600);

            stp.setOrientation(Graphics.RIGHT);
            stp.paint(g, "Heartrate", 0, offsetY1 - valueCellTitleHeight, width, valueCellTitleHeight);
            stp.paint(g, "\u00F8 km/h", 0, offsetY2 - valueCellTitleHeight, width, valueCellTitleHeight);
            stp.paint(g, "km", 0, offsetY3 - valueCellTitleHeight, width, valueCellTitleHeight);

            final int triple = width / 3;
            final TrafficLightsStatePainter tlsp = new TrafficLightsStatePainter();
            final int headerHeight = grid.getHeaderHeight();
            final int headerY = grid.getHeaderY();
            tlsp.setFont(smallFont);
            tlsp.setForegroundColor(0xaaaaaa);
            final TrafficLightsState tls = new TrafficLightsState();
            tls.setText("GPS");
            tls.setRed();
            tlsp.paint(g, tls, 0, headerY, triple, headerHeight);
            tls.setGreen();
            tls.setText("HxM");
            tlsp.paint(g, tls, triple, headerY, triple, headerHeight);
            tls.setYellow();
            tls.setText("Air");
            tlsp.paint(g, tls, triple * 2, headerY, triple, headerHeight);

            stp.setForegroundColor(0xaaaaaa);
            final int footerY = grid.getFooterY();
            final int footerHeight = grid.getFooterHeight();
            stp.setOrientation(Graphics.LEFT);
            stp.paint(g, "Active", 0, footerY, width / 2, footerHeight);
        }
        protected void sizeChanged(final int w, final int h) {
            LOG.config("sizeChanged: " + w + " " + h);
            TestCanvasController.this.first = true;
            repaint();
        }
    };

    protected void doHandle() throws SporticsException {
        boolean loop = true;

        while (loop) {
            final Accumulator accu = Accumulator.getInstance();
            this.durationDevice.prepare();
            this.durationDevice.start();
            this.durationDevice.registerTypedDataConsumer(accu);
            final Sink sink = new Sink() {

                public void sink(final Assemblage toSink) {
                    final TypedData td = toSink.get(TypedDataDescriptor.DURATION);
                    if (td != null) {
                        final Variant v = td.getValue();
                        final long duration = v.asLong();
                        TestCanvasController.this.duration =
                                DateSupport.millisecondsToTime(duration, true);
                        TestCanvasController.this.c.repaint();
                    }
                }
            };
            accu.registerSink(sink);
            

            c.addCommand(MainController.BACK);

            c.setCommandListener(new CommandListener() {
                public void commandAction(final Command cmd, final Displayable displayable) {
                    TestCanvasController.this.selected = cmd;
                    notifyStateChanged();
                }
            });
            c.setFullScreenMode(true);
            display(c);
            c.repaint();
            LOG.config("Canvas width: " + c.getWidth());
            LOG.config("Canvas height: " + c.getHeight());

            final MIDlet midlet = getMIDlet();
            final Display d = Display.getDisplay(midlet);
            LOG.config("numColors: " + d.numColors());
            LOG.config("numAlphaLevels: " + d.numAlphaLevels());
            LOG.config("COLOR_BACKGROUND: "
                       + Integer.toHexString(d.getColor(Display.COLOR_BACKGROUND)));
            LOG.config("COLOR_HIGHLIGHTED_FOREGROUND: "
                       + Integer.toHexString(d.getColor(Display.COLOR_HIGHLIGHTED_FOREGROUND)));
            LOG.config("COLOR_HIGHLIGHTED_BORDER: "
                       + Integer.toHexString(d.getColor(Display.COLOR_HIGHLIGHTED_BORDER)));
            LOG.config("COLOR_HIGHLIGHTED_BACKGROUND: "
                       + Integer.toHexString(d.getColor(Display.COLOR_HIGHLIGHTED_BACKGROUND)));
            LOG.config("COLOR_FOREGROUND: "
                       + Integer.toHexString(d.getColor(Display.COLOR_FOREGROUND)));
            LOG.config("COLOR_BORDER : "
                       + Integer.toHexString(d.getColor(Display.COLOR_BORDER)));

            waitForStateChange();

            if (this.selected == MainController.BACK) {
                loop = false;
                continue;
            }
        }
    }

    static final class GridCalculator {
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
            sb.append(width);
            sb.append("][height=");
            sb.append(height);
            sb.append("][headerHeight=");
            sb.append(headerHeight);
            sb.append("][footerHeight=");
            sb.append(footerHeight);
            sb.append("][valueCellHeight=");
            sb.append(valueCellHeight);
            sb.append("][valueCellTitleHeight=");
            sb.append(valueCellTitleHeight);
            sb.append("][valueCellHeight=");
            sb.append(valueCellDataHeight);
            sb.append("]]");

            return sb.toString();
        }
    }
}
