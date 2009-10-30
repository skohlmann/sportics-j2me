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

/**
 *
 * @author Sascha Kohlmann
 */
final class State {

    public static final State ACTIVE = new State("Active");
    public static final State STOPPED = new State("Stopped");
    public static final State PAUSED = new State("Pause");

    private final String description;

    private State(final String description) {
        this.description = description;
    }

    public String getStateAsText() {
        return this.description;
    }

    public String toString() {
        final StringBuffer sb = new StringBuffer("State@");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append("[[description=");
        sb.append(this.description);
        sb.append("]]");

        return sb.toString();
    }
}
