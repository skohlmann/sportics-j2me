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

/**
 * There are three states defined:
 * @author Sascha Kohlmann
 */
public final class TrafficLightsState {

    private static final int GREEN = 1;
    private static final int YELLOW = 2;
    private static final int RED = 3;

    int state = GREEN;
    private String text;

    public String getText() {
        return this.text;
    }

    public void setText(final String text) {
        this.text = text;
    }

    public void setGreen() {
        this.state = GREEN;
    }
    public void setRed() {
        this.state = RED;
    }
    public void setYellow() {
        this.state = YELLOW;
    }

    public boolean isGreen() {
        return this.state == GREEN;
    }
    public boolean isRed() {
        return this.state == RED;
    }
    public boolean isYellow() {
        return this.state == YELLOW;
    }

    /**
     * Equality about the state.
     * @param obj the object to test equality for
     * @return {@code true} if and only if booth states are the same. {@code false} otherwise
     */
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TrafficLightsState other = (TrafficLightsState) obj;
        if (this.state != other.state) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + this.state;
        return hash;
    }

    /**
     * The value of the implementation. Do not use the value to access data.
     * The format of the returned value may change in later implementations.
     */
    public String toString() {
        final StringBuffer sb = new StringBuffer("TrafficLightsState@");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append("[[state:");
        switch(this.state) {
            case GREEN:
                sb.append("green");
                break;
            case YELLOW:
                sb.append("yellow");
                break;
            default:
                sb.append("RED");
        }
        sb.append("][text:");
        sb.append(this.text);
        sb.append("]]");

        return sb.toString();
    }

}
