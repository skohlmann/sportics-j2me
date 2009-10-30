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

import net.sportics.dni.rt.client.microedition.util.LogManager;

/**
 *
 * @author Sascha Kohlmann
 */
public class PainterConfigurator {

    private static LogManager LOG = LogManager.getInstance("PainterConfigurator");
    static {
        LOG.debug("#class: " + PainterConfigurator.class.getName());
    }
    public static final String BACKGROUND_COLOR_CONFIG_KEY = "ui.color.backgroud";
    public static final String TEXT_COLOR_CONFIG_KEY = "ui.color.text";
    public static final String FONT_STYLE_CONFIG_KEY = "ui.font.style";
    public static final String FONT_SIZE_CONFIG_KEY = "ui.font.size";
    public static final String FONT_FACE_CONFIG_KEY = "ui.font.face";

    public void configure(final BasePainter painter) {
        
    }
}
