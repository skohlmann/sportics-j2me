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

import java.io.IOException;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import net.sportics.dni.rt.client.microedition.util.LogManager;

/**
 *
 * @author Sascha Kohlmann
 */
public final class TrafficLightsStatePainter extends BasePainter {
    private static final int IMAGE_WIDTH_AND_HEIGHT = 16;
    private static LogManager LOG = LogManager.getInstance("TrafficLightsStatePainter");
    static {
        LOG.debug("#class: " + TrafficLightsStatePainter.class.getName());
    }
    private static final Image[] TRAFFIC_DOTS = new Image[3];
    static {
        try {
            LOG.debug("Init images");
            TRAFFIC_DOTS[0] = Image.createImage("/images/16x16/signal_green.png");
            TRAFFIC_DOTS[1] = Image.createImage("/images/16x16/signal_yellow.png");
            TRAFFIC_DOTS[2] = Image.createImage("/images/16x16/signal_red.png");
        } catch (final IOException e) {
            LOG.debug("Fallback to default images.");
            TRAFFIC_DOTS[0] = Image.createImage(IMAGE_WIDTH_AND_HEIGHT, IMAGE_WIDTH_AND_HEIGHT);
            drawArc(TRAFFIC_DOTS[0], 0x12ff12);
            TRAFFIC_DOTS[1] = Image.createImage(IMAGE_WIDTH_AND_HEIGHT, IMAGE_WIDTH_AND_HEIGHT);
            drawArc(TRAFFIC_DOTS[1], 0xffff04);
            TRAFFIC_DOTS[2] = Image.createImage(IMAGE_WIDTH_AND_HEIGHT, IMAGE_WIDTH_AND_HEIGHT);
            drawArc(TRAFFIC_DOTS[2], 0xff0000);
        }
    }

    private static void drawArc(final Image img, final int color) {
        final Graphics g0 = img.getGraphics();
        g0.setColor(0);
        g0.fillRect(0, 0, IMAGE_WIDTH_AND_HEIGHT, IMAGE_WIDTH_AND_HEIGHT);
        g0.setColor(color);
        g0.fillArc(1, 1, IMAGE_WIDTH_AND_HEIGHT - 1, IMAGE_WIDTH_AND_HEIGHT - 1, 0, 360);
    }

    public void paint(final Graphics g,
                      final Object objectToPaint,
                      final int x,
                      final int y,
                      final int width,
                      final int height) {
        if (objectToPaint != null) {
            final TrafficLightsState tls = (TrafficLightsState) objectToPaint;
            final String text = tls.getText();
            if (text != null) {
                final Font font = getFont();
                final int fontHeight = font.getHeight();
                final int yOff = fontHeight > IMAGE_WIDTH_AND_HEIGHT
                                        ? ((fontHeight - IMAGE_WIDTH_AND_HEIGHT) / 2) : 0;
        //        final int textSize = font.stringWidth(text);
                final Image toDraw = TRAFFIC_DOTS[tls.state - 1];
                final int oldColor = g.getColor();
                final int backgroundColor = getBackgroundColor();
                g.setColor(backgroundColor);
                g.fillRect(x, y, width, height);
                g.drawImage(toDraw, x, y + yOff, Graphics.TOP | Graphics.LEFT);
                final Font oldFont = g.getFont();
                g.setFont(font);
                final int textColor = getForegroundColor();
                g.setColor(textColor);
                g.drawString(text, x + IMAGE_WIDTH_AND_HEIGHT + 2, y, Graphics.TOP | Graphics.LEFT);
                g.setColor(oldColor);
                g.setFont(oldFont);
            }
        }
    }
}
