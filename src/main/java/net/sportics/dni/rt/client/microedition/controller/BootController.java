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
package net.sportics.dni.rt.client.microedition.controller;

import java.io.IOException;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;
import javax.microedition.midlet.MIDlet;

import net.sportics.dni.rt.client.microedition.ConfigManager;
import net.sportics.dni.rt.client.microedition.LocationManager;
import net.sportics.dni.rt.client.microedition.SporticsException;
import net.sportics.dni.rt.client.microedition.StorageManager;
import net.sportics.dni.rt.client.microedition.StorageManagerFactory;
import net.sportics.dni.rt.client.microedition.ui.FontManager;
import net.sportics.dni.rt.client.microedition.ui.TrafficLightsStatePainter;
import net.sportics.dni.rt.client.microedition.util.Environment;
import net.sportics.dni.rt.client.microedition.util.LogManager;

public final class BootController extends AbstractController {

    private static final LogManager LOG = LogManager.getInstance("BootController");
    static {
        LOG.debug("#class: " + BootController.class.getName());
    }

    private static final String SMALL_INTRO_IMAGE = "/images/sportics125x125.png";
    private static final String BIG_INTRO_IMAGE = "/images/sportics238x238.png";
    private static final int MIN_WIDTH_FOR_BIG_INTRO_PICTURE = 240;

    public static final Command NEXT = new Command("Next", Command.OK, 1);

    private static final String USER_TEXT =
        "It seems as if you start the program for the first time.\n"
        + "Please insert your Sportics.net user login and password first.";

    private static final String STORAGE_TEXT =
        "Next: Setup directory to store local data, if necessary.";

    private Command selected = null;
    private MIDlet midlet;

    protected void doHandle() throws SporticsException {
        final Canvas startCanvas = getStartCanvas();
        this.midlet = getMIDlet();
        display(startCanvas);
        startCanvas.repaint();
        try {
            final Runnable initFontManager = new InitFontManagerRunnable();
            final Thread t = new Thread(initFontManager, "Thread-InitFontManager");
            LOG.debug("initFontManager Thread: " + t);
            t.start();
            Thread.sleep(2222);
            properties();
            LOG.debug("doHandle() - Finished waiting - start joining thread " + t);
            t.join();
            LOG.debug("doHandle() - Finished joining thread " + t);
        } catch (final InterruptedException e) {
        }

        final UserController uc = new UserController();
        final String user = uc.getLogin();
        final String pwd = uc.getPassword();
        if (user == null || user.length() < 1 || pwd == null || pwd.length() == 0) {
            final ConfigManager cfgMgr = ConfigManager.getInstance();
//            cfgMgr.set(UserController.LOGIN_ID_KEY, "livetracker");
//            cfgMgr.set(UserController.PASSWORD_KEY, "info4U");

            ConfigureController.setNewPositionTrackingDecision(true);
            ConfigureController.setNewLifeTrackingDecision(true);
            ConfigureController.setNewBluetoothDecision(false);

            boolean loop = true;
//            boolean loop = false;
            while (loop) {
                final TextBox tb = new TextBox("First Start",
                                               USER_TEXT,
                                               USER_TEXT.length(),
                                               TextField.ANY | TextField.UNEDITABLE);
                tb.addCommand(NEXT);
                tb.setCommandListener(new CommandListener() {
                    public void commandAction(final Command cmd, final Displayable displayable) {
                        BootController.this.selected = cmd;
                        notifyStateChanged();
                    }
                });
                LOG.config("Set First Start text to display");
                display(tb);
                waitForStateChange();

                if (this.selected == NEXT) {
                    LOG.config("Call handle of UserController");
                    uc.handle(this.midlet);
                }
                LOG.config("Boot finished");
                loop = false;
            }
        }

        final ConfigManager cfgMgr = ConfigManager.getInstance();
        final String storage = cfgMgr.get(StorageManager.STORE_CONFIG_NAME);
        if (Environment.storageSupported() && storage == null || storage.length() == 0) {
            final TextBox tb1 = new TextBox("First Start",
                                            STORAGE_TEXT,
                                            STORAGE_TEXT.length(),
                                            TextField.ANY | TextField.UNEDITABLE);
            tb1.addCommand(NEXT);
            tb1.setCommandListener(new CommandListener() {
                public void commandAction(final Command cmd, final Displayable displayable) {
                    BootController.this.selected = cmd;
                    notifyStateChanged();
                }
            });
//            LOG.config("Set Storage setup text to display");
            display(tb1);
            waitForStateChange();

            if (this.selected == NEXT) {
                try {
                    final StorageManagerFactory factory =
                        StorageManagerFactory.getInstance();
                    final StorageManager manager = factory.getManager();
//                    LOG.config("Call StorageManager.setup");
                    manager.setup(this.midlet, false);
                } catch (final IOException e) {
                    e.printStackTrace();
                    final String msg = e.getMessage();
                    throw new IllegalStateException("IOException: " + msg);
                }
            }
        }
    }

    final Canvas getStartCanvas() {
        final Canvas canvas = new Canvas() {
            private Image img = null;
            protected void paint(final Graphics g) {
                final int width = getWidth();
                final int height = getHeight();
                if (img == null) {
                    try {
                        if (width >= MIN_WIDTH_FOR_BIG_INTRO_PICTURE
                                || height >= MIN_WIDTH_FOR_BIG_INTRO_PICTURE) {
                            this.img = Image.createImage(BIG_INTRO_IMAGE);
                        } else {
                            this.img = Image.createImage(SMALL_INTRO_IMAGE);
                        }
                    } catch (final IOException e) {
                        // ignore it
                    }
                }
                if (img != null) {
                    final int imgHeight = this.img.getHeight();
                    final int imgWidth = this.img.getWidth();
                    g.setColor(0, 0, 0);
                    g.fillRect(0, 0, width, height);
                    final int imgXOffset = (width - imgWidth) / 2;
                    final int imgYOffset = (height - imgHeight) / 2;
                    g.drawImage(this.img, imgXOffset, imgYOffset, Graphics.TOP | Graphics.LEFT);
                }
            }
            protected void sizeChanged(final int w, final int h) {
                repaint();
            }
        };
        canvas.setFullScreenMode(true);
        return canvas;
    }

    private static final String[] PROPERTY_KEYS = new String[] {
        "microedition.platform", "microedition.encoding", "microedition.commports",
        "microedition.profiles", "microedition.configuration", "microedition.locale",
        "microedition.hostname", "microedition.jtwi.version", "microedition.msa.version",
        "microedition.location.version", "microedition.io.file.FileConnection.version",
        "microedition.sensor.version", "fileconn.dir.private", "file.separator",
        "fileconn.dir.memorycard.name", "microedition.m2g.svg.version",
        "microedition.m2g.svg.baseProfile",

        "com.nokia.mid.imei", "com.nokia.memoryramfree", "com.nokia.mid.batterylevel",
        "com.nokia.mid.networkid", "com.nokia.mid.networksignal", "com.nokia.mid.countrycode",
        "com.nokia.mid.networkavailability",

        "com.sonyericsson.imei", "com.sonyericsson.sim.subscribernumber",
        "com.sonyericsson.java.platform", "com.sonyericsson.net.networkname",
        "com.sonyericsson.net.serviceprovider", "com.sonyericsson.net.rat"
    };

    private void properties() {
        for (int i = 0; i < PROPERTY_KEYS.length; i++) {
            final String key = PROPERTY_KEYS[i];
            final StringBuffer sb = new StringBuffer(key);
            sb.append("=");
            sb.append(System.getProperty(key));
            final String s = sb.toString();
            LOG.config(s);
        }
    }

    final class InitFontManagerRunnable implements Runnable {

        public void run() {
            final Font smallFont =
                    Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
            LOG.config("Small font: height=" + smallFont.getHeight() + " - baseline="
                       + smallFont.getBaselinePosition() + " - " + smallFont);
            final Font mediumFont =
                    Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
            LOG.config("Medium font: height=" + mediumFont.getHeight() + " - baseline="
                       + mediumFont.getBaselinePosition() + " - " + mediumFont);
            final Font largeFont =
                    Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_LARGE);
            LOG.config("Large font: height=" + largeFont.getHeight() + " - baseline="
                       + largeFont.getBaselinePosition() + " - " + largeFont);
            final Display display = Display.getDisplay(BootController.this.midlet);
            final Displayable displayable = display.getCurrent();
            LOG.config("Screen: width=" + displayable.getWidth()
                       + " - height=" + displayable.getHeight());

            LOG.debug("Start getting instance from FontManager");
            final FontManager fm = FontManager.getInstance();
            LOG.debug("Finish getting instance from FontManager: " + fm);
            new TrafficLightsStatePainter();
            LOG.debug("Finish getting init " + TrafficLightsStatePainter.class.getName());
        }
    }
}
