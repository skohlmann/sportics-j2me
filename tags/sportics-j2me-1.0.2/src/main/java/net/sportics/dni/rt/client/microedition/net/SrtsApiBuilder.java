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

public final class SrtsApiBuilder {

    private static final String DEBUG_URL_KEY = 
        " net.sportics.dni.rt.client.microedition.remote.SrtsApi.debug.url";

    private static final String PREPARE_URL = "http://dni.sportics.net/rt/prepare";
    public static final int DEFAULT_SNAP_DELAY = 120;

    private final static SrtsApiBuilder INSTANCE = new SrtsApiBuilder();

    public static SrtsApiBuilder getInstance() {
        return INSTANCE;
    }

    private final String concretUrl;
    private String password;
    private String sid;
    private String prepareUrl = null;

    private SrtsApiBuilder() {
        final String url = System.getProperty(DEBUG_URL_KEY);
        if (url != null) {
            this.concretUrl = url;
        } else {
            this.concretUrl = PREPARE_URL;
        }
        reset();
    }

    public SrtsApiBuilder setPassword(final String password) {
        this.password = password;
        return this;
    }

    public SrtsApiBuilder setSporticsId(final String sid) {
        this.sid = sid;
        return this;
    }

    public SrtsApi build() {
        if (this.sid == null) {
            throw new IllegalStateException("Sportics ID is null");
        }
        if (this.password == null) {
            throw new IllegalStateException("password is null");
        }
        final SrtsApi api = new SrtsApi(this.sid,
                                        this.password,
                                        this.prepareUrl);
        reset();
        return api;
    }

    final void reset() {
        this.sid = null;
        this.password = null;
        this.prepareUrl = concretUrl;
    }

    public String toString() {
        final StringBuffer sb = new StringBuffer();
        final String className = SrtsApiBuilder.class.getName();
        sb.append(className);
        sb.append("[concretUrl=");
        sb.append(concretUrl);
        sb.append("|sid=");
        sb.append(sid);
        sb.append("]");

       return sb.toString();
    }
}
