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
package net.sportics.dni.rt.client.microedition.net;


public interface SrtsApiListener {

    /** @see SrtsApi#DoPrepare() */
    final int STATE_DO_PREPARE = 1;
    /** @see SrtsApi#prepare() */
    final int STATE_PREPARED = 2;
    /** @see SrtsApi#start() */
    final int STATE_DO_START = 3;
    /** @see SrtsApi#start() */
    final int STATE_STARTED = 4;
    /** @see SrtsApi#stop() */
    final int STATE_DO_STOP = 5;
    /** @see SrtsApi#stop() */
    final int STATE_STOPPED = 6;
    /** @see SrtsApi#carryOn() */
    final int STATE_DO_CONTINUE = 7;
    /** @see SrtsApi#doContine() */
    final int STATE_CONTINUED = 8;
    /** @see SrtsApi#snap() */
    final int STATE_DO_SNAP = 9;
    /** @see SrtsApi#snap() */
    final int STATE_SNAPED = 10;
    /** @see SrtsApi#pause() */
    final int STATE_DO_PAUSE = 11;
    /** @see SrtsApi#pause() */
    final int STATE_PAUSED = 12;
    /** @see SrtsApi#mark() */
    final int STATE_DO_MARK = 13;
    /** @see SrtsApi#mark() */
    final int STATE_MARKED = 14;

    void onStateChange(final SrtsApi source, final int state);

    void onError(final SrtsApi source, final int state, final SrtsApiException exception);
}
