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

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;

import net.sportics.dni.rt.client.microedition.SporticsException;

/**
 * Support for {@link MIDlet} handling. Applications must override {@link #doHandle()}.
 * @author Sascha Kohlmann
 */
public abstract class AbstractController implements Controller {

    /** The {@code MIDlet} of the application. */
    private MIDlet midlet;

    /** The {@link Displayable} to restore. */
    private Displayable restorable;

    /** Returns the current {@code MIDlet} instance.
     * @return the current {@code MIDlet}
     * @see #handle(MIDlet)
     */
    protected final MIDlet getMIDlet() {
        return this.midlet;
    }

    /** Restores the old {@link Displayable} after. Call it before leaving
     * {@link #handle(MIDlet)}. */
    final void restoreDisplayable() {
        final Display display = Display.getDisplay(this.midlet);
        display.setCurrent(this.restorable);
    }

    /**
     * Call the method with {@code super} to check for {@code null}.
     * @param midlet the application {@code MIDlet}
     * @throws IllegalArgumentException if and only if {@code midlet} is {@code null}
     * @see #getMIDlet()
     */
    public final void handle(final MIDlet midlet) throws SporticsException {
        if (midlet == null) {
            throw new IllegalArgumentException("midlet is null");
        }
        this.midlet = midlet;
        final Display display = Display.getDisplay(this.midlet);
        this.restorable = display.getCurrent();
        doHandle();
        restoreDisplayable();
    }

    /**
     * Shows the supplied {@code Displayable}.
     * @param displayable the {@code Displayable} to show
     * @throws IllegalArgumentException if and only if the supplied {@code Displayable} is
     *                                  {@code null}
     * @throws IllegalStateException if and only if there is no {@link MIDlet} available
     * @see #getMIDlet()
     */
    void display(final Displayable displayable) {
        if (displayable == null) {
            throw new IllegalArgumentException("displayble is null");
        }
        final MIDlet m = getMIDlet();
        if (m == null) {
            throw new IllegalStateException("midlet is null");
        }
        final Display display = Display.getDisplay(m);
        display.setCurrent(displayable);
    }

    /**
     * Informs the controller {@link #waitForStateChange()} to wake up.
     * @see #waitForStateChange()
     */
    protected void notifyStateChanged() {
        synchronized(this) {
            notify();
        }
    }

    /**
     * The method only returns after a call of {@link #notifyStateChanged()}.
     * @see #notifyStateChanged()
     */
    protected void waitForStateChange() {
        synchronized(this) {
            try {
                wait();
            } catch (final InterruptedException e) {
            }
        }
    }

    /**
     * Does the real work
     */
    protected abstract void doHandle() throws SporticsException;
}
