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
package net.sportics.dni.rt.client.microedition.device;

import java.util.Timer;
import java.util.TimerTask;

import net.sportics.dni.rt.client.microedition.Pauseable;
import net.sportics.dni.rt.client.microedition.TypedData;
import net.sportics.dni.rt.client.microedition.TypedDataDescriptor;
import net.sportics.dni.rt.client.microedition.Variant;
import net.sportics.dni.rt.client.microedition.util.LogManager;

public final class DurationDevice extends AbstractDevice implements Pauseable {
    private static final LogManager LOG = LogManager.getInstance("DurationDevice");
    static {
        LOG.debug("#class: " + DurationDevice.class.getName());
    }

    private static final long ONE_SECOND = 1000l;
    private static final Timer TIMER = new Timer();
    private TimerTask task;
    private long lastTime = 0;
    private long totalTime = 0;
    private boolean pause = false;

    protected void doStart() {
        synchronized(this) {
            if (this.task == null) {
                this.lastTime = System.currentTimeMillis();
                this.task = new TimerTask() {
                    public void run() {
                        LOG.debug("Pause mode: " + DurationDevice.this.pause);
                        if (!DurationDevice.this.pause) {
                            final long currentTime = System.currentTimeMillis();
                            DurationDevice.this.totalTime +=
                                    currentTime - DurationDevice.this.lastTime;
                            DurationDevice.this.lastTime = currentTime;
                        }
                        final Variant vAllOver = new Variant(DurationDevice.this.totalTime);
                        final TypedData tdAllOver =
                            new TypedData(TypedDataDescriptor.DURATION, vAllOver);
                        newData(tdAllOver);
                    }
                };
                TIMER.scheduleAtFixedRate(this.task, 0l, ONE_SECOND);
            }
        }
    }

    protected void doStop() {
        synchronized(this) {
            this.pause = true;
            if (this.task != null) {
                this.task.cancel();
                this.task = null;
            }
        }
    }

    protected void doRestart() {
        synchronized(this) {
            keepOn();
        }
    }

    public String getDeviceName() {
        return "Duration Device";
    }

    public String[] supportedTypedDataList() {
        return new String[] {TypedDataDescriptor.DURATION,
                             TypedDataDescriptor.DURATION_CURRENT};
    }

    public void pause() {
        setState(STATE_DO_PAUSE);
        fireLifecycleEvent(STATE_DO_PAUSE);

        LOG.debug("Pause mode on");
        this.pause = true;

        setState(STATE_PAUSED);
        fireLifecycleEvent(STATE_PAUSED);
    }

    public void keepOn() {
        setState(STATE_DO_RESTART);
        fireLifecycleEvent(STATE_DO_RESTART);

        LOG.debug("Pause mode off");
        this.lastTime = System.currentTimeMillis();
        this.pause = false;

        setState(STATE_RESTARTED);
        fireLifecycleEvent(STATE_RESTARTED);
    }
}
