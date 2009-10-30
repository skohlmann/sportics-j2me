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
package net.sportics.dni.rt.client.microedition;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Image;

/**
 * @author Sascha Kohlmann
 * @deprecated remove in further implementations. Don't use anymore.
 */
public final class SelectionListEntry {

    private String string = null;
    private Image image = null;

    public void setViewData(final String stringPart, final Image imagePart) {
        if (stringPart == null) {
            throw new IllegalArgumentException("stringPart is null");
        }
        this.string = stringPart;
        this.image = imagePart;
    }

    public String getString() {
        return this.string;
    }

    public Image getImage() {
        return this.image;
    }

    public void addToChoice(final Choice choice) {
        if (choice == null) {
            throw new IllegalArgumentException("choice is null");
        }
        final String s = getString();
        final Image i = getImage();
        choice.append(s, i);
    }
}
