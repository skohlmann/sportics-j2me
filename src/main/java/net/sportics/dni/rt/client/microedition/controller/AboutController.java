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

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

import org.bouncycastle.LICENSE;

import net.sportics.dni.rt.client.microedition.SporticsException;

/**
 * Shows the copyright and license information.
 * @author Sascha Kohlmann
 */
public final class AboutController extends AbstractController {

    public static final String LICENSE_TEXT =
        "Copyright (c) 2008-2009 Sascha Kohlmann\n\n"
        + "This program is free software: you can redistribute it and/or modify "
        + "it under the terms of the GNU General Public License as published by "
        + "the Free Software Foundation, either version 3 of the License, or "
        + "(at your option) any later version.\n\n"
        + "This program is distributed in the hope that it will be useful, "
        + "but WITHOUT ANY WARRANTY; without even the implied warranty of "
        + "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the "
        + "GNU General Public License for more details.\n\n"
        + "You should have received a copy of the GNU General Public License "
        + "along with this program.  If not, see <http://www.gnu.org/licenses/>.\n\n"
        + "Most of the art work is derived from Tango Project, and thus this "
        + "portion of Sportics.net is covered by their license (Creative Commons"
        + "Attribution Share-Alike license). You are free:\n"
        + "to Share - to copy, distribute and transmit the work)\n"
        + "to Remix - to adapt the work\n"
        + "Under the following conditions:\n"
        + "Attribution. You must attribute the work in the manner specified by the author or "
        + "licensor (but not in any way that suggests that they endorse you or your use of "
        + "the work).\n"
        + "Share Alike. If you alter, transform, or build upon this work, you may distribute"
        + " the resulting work only under the same or similar license to this one.\n"
        + "For more information see http://creativecommons.org/licenses/by-sa/2.5/\n\n"
        + LICENSE.licenseText;

    private Command selected = null;

    protected void doHandle() throws SporticsException {
        boolean loop = true;

        while (loop) {
            final TextBox tb = new TextBox("About",
                                           LICENSE_TEXT,
                                           LICENSE_TEXT.length(),
                                           TextField.ANY | TextField.UNEDITABLE);
            tb.addCommand(MainController.BACK);

            tb.setCommandListener(new CommandListener() {
                public void commandAction(final Command cmd, final Displayable displayable) {
                    AboutController.this.selected = cmd;
                    notifyStateChanged();
                }
            });
            display(tb);
            waitForStateChange();

            if (this.selected == MainController.BACK) {
                loop = false;
                continue;
            }
        }
    }
}
