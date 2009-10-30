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


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.util.Strings;

import net.sportics.dni.rt.client.microedition.Sport;
import net.sportics.dni.rt.client.microedition.ConfigManager;
import net.sportics.dni.rt.client.microedition.Preparable;
import net.sportics.dni.rt.client.microedition.Startable;
import net.sportics.dni.rt.client.microedition.TypedData;
import net.sportics.dni.rt.client.microedition.TypedDataDescriptor;
import net.sportics.dni.rt.client.microedition.Variant;
import net.sportics.dni.rt.client.microedition.accu.Assemblage;
import net.sportics.dni.rt.client.microedition.accu.Sink;
import net.sportics.dni.rt.client.microedition.util.LogManager;
import net.sportics.dni.rt.client.microedition.util.Utc;


public final class SrtsApi implements Sink, Startable, Preparable {

    private static final LogManager LOG = LogManager.getInstance("SrtsApi");
    static {
        LOG.debug("#class: " + SrtsApi.class.getName());
    }

    /** Name of the header. Value {@value}. */
    public static final String HTTP_HEADER_AUTHORIZATION = "Authorization";
    /** Value of the Basic header. Value {@value}. */
    static final String HTTP_HEADER_AUTH_BASIC = "Basic";

    public static final String USASCII_ENC = "US-ASCII";

    public static final String LIFE_TRACKING_ALLOWED_KEY = "life.allowed";
    public static final String LIFE_TRACKING_ALLOWED_VALUE_YES = Boolean.TRUE.toString();
    public static final String LIFE_TRACKING_ALLOWED_VALUE_NO = Boolean.FALSE.toString();

    public static final String LIFE_TRACKING_INTERVAL_KEY = "life.interval";
    public static final int LIFE_TRACKING_DEFAULT_INTERVAL_VALUE = 60 * 2;

//    private static final String HTTP_HEADER_HOST = "Host";
    private static final String HTTP_HEADER_LOCATION = "Location";
    private static final String HTTP_HEADER_USER_AGENT = "User-Agent";
    private static final String HTTP_HEADER_CONNECTION = "Connection";
    private static final String HTTP_HEADER_CONNECTION_CLOSE_VALUE = "close";
    private static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HTTP_HEADER_CONNECTION_RFC4180_VALUE = "text/csv; header=present";
    private final static String KEY_RTI = "rti";
    public final static String KEY_AID = "aid";
    private final static String KEY_TS = "ts";
    public final static String KEY_TZ = "tz";
    private final static String KEY_PTYPE = "ptype";
    private final static String KEY_TCODE = "tcode";
    private final static String VALUE_PTYPE_PLAIN = "plain";
    private final static String KEY_SERVICE_ID = "serviceid";
    private final static String VALUE_SERVICE_ID = "rt.live";
    private final static String VALUE_TCODE = "3.3.16";
    private final static String KEY_STI = "sti";
    private final static String VALUE_STI = "1";
    private static final String PLATFORM = "microedition.platform";

    private static final int MILLISECONDS = 1000;
    private static final String UTF8 = "UTF-8";
    private static final int MIN_RTI = 10;

    private final Hashtable listener = new Hashtable();

    private final String sid;
    private Sport aid;
    private int rti;
    private final String password;
    private final String prepareUrl;
    private String resourceUrl;
    private final String platform;
    private boolean lifeAllowed;

    private int snapCount = 1;
    private volatile Snap snap = null;
    private int state = -1;
    private boolean started = false;

    private Timer timer;
    private final static SnapSerializer SNAP_SERIALIZER = new Rfc4180Serializer();

    SrtsApi(final String sid,
            final String password,
            final String prepareUrl) {

        if (sid == null) {
            throw new IllegalArgumentException("sid is null");
        }
        this.sid = sid;
        if (password == null) {
            throw new IllegalArgumentException("password is null");
        }
        this.password = encodePassword(sid, password);
        if (prepareUrl == null) {
            throw new IllegalArgumentException("prepareUrl is null");
        }
        this.prepareUrl = prepareUrl;
        LOG.config("prepareUrl: " + this.prepareUrl);
        this.platform = System.getProperty(PLATFORM);

        final ConfigManager configMgr = ConfigManager.getInstance();
        final String life = configMgr.get(LIFE_TRACKING_ALLOWED_KEY,
                                          LIFE_TRACKING_ALLOWED_VALUE_NO);
        this.lifeAllowed = LIFE_TRACKING_ALLOWED_VALUE_NO.equalsIgnoreCase(life) ? false : true;
        LOG.config("Life tracking: " + this.lifeAllowed);

        final String interval = configMgr.get(LIFE_TRACKING_INTERVAL_KEY,
                                              "" + LIFE_TRACKING_DEFAULT_INTERVAL_VALUE);
        try {
            final int i = Integer.parseInt(interval);
            this.rti = i <= MIN_RTI ? MIN_RTI : i;
        } catch (final NumberFormatException e) {
            this.rti = LIFE_TRACKING_DEFAULT_INTERVAL_VALUE;
        }
        LOG.config("rti: " + this.rti);
    }

    public void sink(final Assemblage toSink) {
        LOG.debug("sink: " + toSink);
        if (this.snap != null && toSink != null) {
            synchronized(this) {
                this.snap.addAssemblage(toSink);
            }
        }
    }

    protected void snap() {
        LOG.debug("start snap()");
        if (!this.lifeAllowed) {
            LOG.debug("snap(): life tracking not allowed");
            return;
        }
//        if (this.state != SrtsApiListener.STATE_STARTED
//                && this.state != SrtsApiListener.STATE_DO_STOP
//                && this.state != SrtsApiListener.STATE_CONTINUED) {
//            this.snap = new Snap();
//            LOG.debug("snap() not allowed. State: " + this.state);
//            return;
//        }
        if (this.snap != null) {
            LOG.debug("snap() - snap not null");

            final int oldState = this.state;
            this.state = SrtsApiListener.STATE_DO_SNAP;
            fireLifecycleEvent(this.state);

            Snap oldSnap = null;
            synchronized(this) {
                oldSnap = this.snap;
                this.snap = new Snap();
            }
            final String toTransfer = SNAP_SERIALIZER.serialize(oldSnap);

            // Todo: make code cleaner
            if (toTransfer.length() == 0) {
                this.state = SrtsApiListener.STATE_SNAPED;
                fireLifecycleEvent(this.state);
                this.state = oldState;
                return;
            }
            final String requestUrl = this.resourceUrl + "/snap/" + this.snapCount;


            try {
                LOG.debug("snap - start with URL: " + requestUrl);
                final HttpConnection con = 
                    (HttpConnection) Connector.open(requestUrl, Connector.READ_WRITE, true);
                con.setRequestMethod(HttpConnection.POST);
                prepareCommonHeader(con);
                prepareAuthorizationHeader(con);
                con.setRequestProperty(HTTP_HEADER_CONTENT_TYPE,
                                       HTTP_HEADER_CONNECTION_RFC4180_VALUE);

                final OutputStream out = con.openOutputStream();
                final OutputStreamWriter writer = new OutputStreamWriter(out, UTF8);
                writer.write(toTransfer);
                writer.flush();

                LOG.debug("snap - connect for " + toTransfer.length() + " characters");
                final int rc = con.getResponseCode();
                LOG.debug("snap - finished with response code " + rc);
                if (rc != HttpConnection.HTTP_CREATED) {
                    final SrtsApiException e =
                        new SrtsApiException(SrtsApiException.ERRORCODE_UNEXPECTED_SERVER_RESPONSE,
                                             "" + rc);
                    fireErrorEvent(this.state, e);
                    e.printStackTrace();
                } else {
                    this.snapCount++;
                }
            } catch (final IOException e) {
                final SrtsApiException newEx =
                    new SrtsApiException(SrtsApiException.ERRORCODE_UNKNOWN_IO_EXCEPTION,
                                         e.getMessage());
                fireErrorEvent(this.state, newEx);
                e.printStackTrace();
            }
            final Runtime rt = Runtime.getRuntime();
            LOG.debug("Total memory: " + rt.totalMemory() + " - freeMemory: " + rt.freeMemory());

            this.state = SrtsApiListener.STATE_SNAPED;
            fireLifecycleEvent(this.state);
            this.state = oldState;
        }
        LOG.debug("finish snap()");
    }

    public void prepare() {
        if (!this.lifeAllowed) {
            LOG.debug("prepare(): life tracking not allowed");
            return;
        }
        this.state = SrtsApiListener.STATE_DO_PREPARE;
        fireLifecycleEvent(this.state);

        final UrlBuilder urlBuilder = UrlBuilder.newInstance(this.prepareUrl);
//        urlBuilder.addParameter(KEY_SID, this.sid);
//        urlBuilder.addParameter(KEY_PASSWORD, this.password);
        urlBuilder.addParameter(KEY_RTI, "" + this.rti);
        urlBuilder.addParameter(KEY_STI, VALUE_STI);
        urlBuilder.addParameter(KEY_SERVICE_ID, VALUE_SERVICE_ID);
        urlBuilder.addParameter(KEY_PTYPE, VALUE_PTYPE_PLAIN);
        urlBuilder.addParameter(KEY_TCODE, VALUE_TCODE);

        final String url = urlBuilder.build();
        LOG.info("prepare - URL: " + url);

        try {
            final HttpConnection con = 
                (HttpConnection) Connector.open(url, Connector.READ_WRITE, true);
            con.setRequestMethod(HttpConnection.GET);

            prepareCommonHeader(con);
            prepareAuthorizationHeader(con);

            LOG.debug("prepare - connect");
            final int rc = con.getResponseCode();

            LOG.debug("prepare - finished with response code " + rc);
            if (rc == HttpConnection.HTTP_CREATED) {
                final String location = con.getHeaderField(HTTP_HEADER_LOCATION);
                if (location == null) {
                    final SrtsApiException e =
                        new SrtsApiException(SrtsApiException.ERRORCODE_NO_LOCATION_HEADER);
                    fireErrorEvent(this.state, e);
                    e.printStackTrace();
                } else {
                    this.resourceUrl = location;
                    LOG.info("prepare - resourceUrl: " + this.resourceUrl);
                }
            } else {
                final String message = readBody(con);
                LOG.info(message);
                final SrtsApiException e =
                    new SrtsApiException(SrtsApiException.ERRORCODE_UNEXPECTED_SERVER_RESPONSE,
                                         "" + rc);
                fireErrorEvent(this.state, e);
                e.printStackTrace();
            }
        } catch (final IOException e) {
            final SrtsApiException newEx =
                new SrtsApiException(SrtsApiException.ERRORCODE_UNKNOWN_IO_EXCEPTION,
                                     e.getMessage());
            fireErrorEvent(this.state, newEx);
            e.printStackTrace();
        } catch (final SecurityException e) {
            this.lifeAllowed = false;
            final SrtsApiException newEx =
                new SrtsApiException(SrtsApiException.ERRORCODE_SECURITYEXCEPTION,
                                     e.getMessage());
            fireErrorEvent(this.state, newEx);
            e.printStackTrace();
            LOG.warn("Unable to connect to server. Set to \"life not allowed\". Exception message: "
                     + e.getMessage());
        }
        this.state = SrtsApiListener.STATE_PREPARED;
        fireLifecycleEvent(this.state);
    }

    String readBody(final HttpConnection c) {

        InputStream is = null;
        try {
            is = c.openInputStream();
            int len = (int) c.getLength();
            if (len > 0) {
                int actual = 0;
                int bytesread = 0 ;
                byte[] data = new byte[len];
                while ((bytesread != len) && (actual != -1)) {
                    actual = is.read(data, bytesread, len - bytesread);
                    bytesread += actual;
                }
                return new String(data);
            } else {
                int count = 0;
                int ch;
                final StringBuffer sb = new StringBuffer();
                while ((ch = is.read()) != -1) {
                    sb.append((char) ch);
                    count++;
                    if (count > 4096) {
                        break;
                    }
                }
                return sb.toString();
            }
        } catch (final IOException e) {
            throw new SrtsApiException(SrtsApiException.ERRORCODE_UNKNOWN_IO_EXCEPTION,
                                       e.getMessage());
        } finally {
            try {
                is.close();
            } catch (final IOException e) {
                // ignore
            }
        }
    }

    public void start() {
        if (!this.lifeAllowed) {
            LOG.debug("start(): life tracking not allowed");
            return;
        }
        this.state = SrtsApiListener.STATE_DO_START;
        LOG.debug("start(): do start " + this.state);
        fireLifecycleEvent(this.state);
        LOG.debug("start(): fireLifecycleEvent done");

        final String requestUrl = this.resourceUrl + "/start";
        final UrlBuilder urlBuilder = UrlBuilder.newInstance(requestUrl);

        urlBuilder.addParameter(KEY_AID, this.aid.getId());
        urlBuilder.addParameter(KEY_TS, "" + System.currentTimeMillis());
        urlBuilder.addParameter(KEY_TZ, new Utc().utc());

        final String url = urlBuilder.build();
        LOG.debug("start with URL: " + url);

        try {
            final HttpConnection con =
                (HttpConnection) Connector.open(url, Connector.READ_WRITE, true);
            con.setRequestMethod(HttpConnection.GET);
            prepareCommonHeader(con);
            prepareAuthorizationHeader(con);

            LOG.debug("start - connect");
            this.snap = new Snap();
            final int rc = con.getResponseCode();
            LOG.debug("start - finish with response code " + rc);
            if (rc != HttpConnection.HTTP_OK) {
                final SrtsApiException e =
                    new SrtsApiException(SrtsApiException.ERRORCODE_UNEXPECTED_SERVER_RESPONSE,
                                         "" + rc);
                fireErrorEvent(this.state, e);
                e.printStackTrace();
            }
        } catch (final IOException e) {
            final SrtsApiException newEx =
                new SrtsApiException(SrtsApiException.ERRORCODE_UNKNOWN_IO_EXCEPTION,
                                     e.getMessage());
            fireErrorEvent(this.state, newEx);
            e.printStackTrace();
        }

        if (this.timer != null) {
            this.timer.cancel();
        }

        this.timer = new Timer();
        this.timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                LOG.debug("SrtsApi TimerTask #class " + getClass().getName());
                final long time = System.currentTimeMillis();
                LOG.debug("run timer at " + time);
                snap();
            }
        }, this.rti, this.rti * MILLISECONDS);
        LOG.debug("start with delay: " + (this.rti * MILLISECONDS));

        this.state = SrtsApiListener.STATE_STARTED;
        fireLifecycleEvent(this.state);
    }

    public void stop() {
        if (!this.lifeAllowed) {
            LOG.debug("stop(): life tracking not allowed");
            return;
        }
        this.state = SrtsApiListener.STATE_DO_STOP;
        fireLifecycleEvent(this.state);
        snap();

        final String requestUrl = this.resourceUrl + "/stop";
        final UrlBuilder urlBuilder = UrlBuilder.newInstance(requestUrl);

        urlBuilder.addParameter(KEY_TS, "" + System.currentTimeMillis());

        final String url = urlBuilder.build();

        try {
            final HttpConnection con =
                (HttpConnection) Connector.open(url, Connector.READ_WRITE, true);
            con.setRequestMethod(HttpConnection.GET);
            prepareCommonHeader(con);
            prepareAuthorizationHeader(con);

            LOG.debug("stop - connect");
            final int rc = con.getResponseCode();
            LOG.debug("stop - finish with response code " + rc);
            if (rc != HttpConnection.HTTP_OK) {
                final SrtsApiException e =
                    new SrtsApiException(SrtsApiException.ERRORCODE_UNEXPECTED_SERVER_RESPONSE,
                                         "" + rc);
                fireErrorEvent(this.state, e);
                e.printStackTrace();
            }
        } catch (final IOException e) {
            final SrtsApiException newEx =
                new SrtsApiException(SrtsApiException.ERRORCODE_UNKNOWN_IO_EXCEPTION,
                                     e.getMessage());
            fireErrorEvent(this.state, newEx);
            e.printStackTrace();
        }

        if (this.timer != null) {
            this.timer.cancel();
        }

        this.state = SrtsApiListener.STATE_STOPPED;
        fireLifecycleEvent(this.state);
    }

    public void pause() {
        if (!this.lifeAllowed) {
            LOG.debug("pause(): life tracking not allowed");
            return;
        }
        this.state = SrtsApiListener.STATE_DO_PAUSE;
        fireLifecycleEvent(this.state);

        final String requestUrl = this.resourceUrl + "/pause";
        final UrlBuilder urlBuilder = UrlBuilder.newInstance(requestUrl);

        urlBuilder.addParameter(KEY_TS, "" + System.currentTimeMillis());

        final String url = urlBuilder.build();

        try {
            final HttpConnection con =
                (HttpConnection) Connector.open(url, Connector.READ_WRITE, true);
            con.setRequestMethod(HttpConnection.GET);
            prepareCommonHeader(con);
            prepareAuthorizationHeader(con);

            LOG.debug("pause - connect: " + url);
            final int rc = con.getResponseCode();
            LOG.debug("pause - finish with response code " + rc);
            if (rc != HttpConnection.HTTP_OK) {
                final SrtsApiException e =
                    new SrtsApiException(SrtsApiException.ERRORCODE_UNEXPECTED_SERVER_RESPONSE,
                                         "" + rc);
                fireErrorEvent(this.state, e);
                e.printStackTrace();
            }
        } catch (final IOException e) {
            LOG.warn("Exception during pause: " + e.getMessage() + " - " + e.getClass());
            final SrtsApiException newEx =
                new SrtsApiException(SrtsApiException.ERRORCODE_UNKNOWN_IO_EXCEPTION,
                                     e.getMessage());
            fireErrorEvent(this.state, newEx);
            e.printStackTrace();
        }

        if (this.timer != null) {
            this.timer.cancel();
        }

        this.state = SrtsApiListener.STATE_PAUSED;
        fireLifecycleEvent(this.state);
    }

    public void doContinue(final Hashtable parameter) {
        if (!this.lifeAllowed) {
            LOG.debug("carryOn(): life tracking not allowed");
            return;
        }
        this.state = SrtsApiListener.STATE_DO_CONTINUE;
        fireLifecycleEvent(this.state);

        final String requestUrl = this.resourceUrl + "/continue";
        final UrlBuilder urlBuilder = UrlBuilder.newInstance(requestUrl);

        urlBuilder.addParameter(KEY_TS, "" + System.currentTimeMillis());
        this.aid = (Sport) parameter.get(KEY_AID);
        if (aid != null) {
            urlBuilder.addParameter(KEY_AID, "" + aid.getId());
        }
        final Object sti = parameter.get(KEY_STI);
        if (sti != null) {
            urlBuilder.addParameter(KEY_STI, "" + sti);
        }

        final String url = urlBuilder.build();

        try {
            final HttpConnection con =
                (HttpConnection) Connector.open(url, Connector.READ_WRITE, true);
            con.setRequestMethod(HttpConnection.GET);
            prepareCommonHeader(con);

            LOG.debug("continue - connect: " + url);
            final int rc = con.getResponseCode();
            LOG.debug("continue - finish with response code " + rc);
            if (rc != HttpConnection.HTTP_OK) {
                final SrtsApiException e =
                    new SrtsApiException(SrtsApiException.ERRORCODE_UNEXPECTED_SERVER_RESPONSE,
                                         "" + rc);
                fireErrorEvent(this.state, e);
                e.printStackTrace();
            }
        } catch (final IOException e) {
            LOG.warn("Exception during continue: " + e.getMessage() + " - " + e.getClass());
            final SrtsApiException newEx =
                new SrtsApiException(SrtsApiException.ERRORCODE_UNKNOWN_IO_EXCEPTION,
                                     e.getMessage());
            fireErrorEvent(this.state, newEx);
            e.printStackTrace();
        }

        if (this.timer != null) {
            this.timer.cancel();
        }

        this.state = SrtsApiListener.STATE_CONTINUED;
        fireLifecycleEvent(this.state);
    }

    public void addSrtsApiListener(final SrtsApiListener listener) {
        if (listener != null) {
            this.listener.put(listener, listener);
        }
    }

    public void removeSrtsApiListener(final SrtsApiListener listener) {
        if (listener != null) {
            this.listener.remove(listener);
        }
    }

    /**
     * Simple support for fire state events.
     * @param state the state to announce
     */
    protected void fireLifecycleEvent(final int state) {
        for (final Enumeration e = this.listener.keys(); e.hasMoreElements(); ) {
            final SrtsApiListener l = (SrtsApiListener) e.nextElement();
            try {
                l.onStateChange(this, state);
            } catch (final Exception ex) {
                final String msg = ex.getMessage();
                LOG.warn("fireLifecycleEvent Exception " + ex.getClass().getName()
                         + " with message \"" + msg + "\"");
            }
        }
    }

    /**
     * Simple support for fire error events.
     * @param state the state in which the error occurs
     * @param exception the exception which occurs
     */
    protected void fireErrorEvent(final int state, final SrtsApiException exception) {
        for (final Enumeration e = this.listener.keys(); e.hasMoreElements(); ) {
            final SrtsApiListener l = (SrtsApiListener) e.nextElement();
            try {
                l.onError(this, state, exception);
            } catch (final Exception ex) {
                final String msg = ex.getMessage();
                LOG.warn("fireErrorEvent Exception " + ex.getClass().getName()
                         + " with message \"" + msg + "\"");
            }
        }
    }

    final void prepareAuthorizationHeader(final HttpConnection con) {
        if (con == null) {
            return;
        }
        try {
            final String value = HTTP_HEADER_AUTH_BASIC + " " + this.password;
            con.setRequestProperty(HTTP_HEADER_AUTHORIZATION, value);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    final void prepareCommonHeader(final HttpConnection con) {
        if (con == null) {
            return;
        }

        try {
            con.setRequestProperty(HTTP_HEADER_USER_AGENT, this.platform);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        try {
            con.setRequestProperty(HTTP_HEADER_CONNECTION, HTTP_HEADER_CONNECTION_CLOSE_VALUE);
        } catch (final IOException e) {
            e.printStackTrace();
        }
//        try {
//            final String host = con.getHost();
//            final int port = con.getPort();
//            final String hostName = host + (port == DEFAULT_HTTP_PORT ? "" : ":" + port);
//            con.setRequestProperty(HTTP_HEADER_HOST, hostName);
//        } catch (final IOException e) {
//            e.printStackTrace();
//        }
    }

    final static class Snap {
        private final Hashtable keys = new Hashtable();
        private final Vector assemblages = new Vector();

        public void addAssemblage(final Assemblage assemblage) {
            if (assemblage != null) {
                synchronized(Snap.this) {
                    synchronized(assemblage) {
                        for (final Enumeration e = assemblage.descriptors(); e.hasMoreElements(); )
                        {
                            final Object o = e.nextElement();
                            keys.put(o, o);
                        }
                        assemblages.addElement(assemblage);
                    }
                }
            }
        }

        public Enumeration allDescriptors() {
            return this.keys.keys();
        }

        public Enumeration assemblages() {
            return this.assemblages.elements();
        }
    }

    static interface SnapSerializer {
        String serialize(Snap snap);
    }

    final static class Rfc4180Serializer implements SnapSerializer {

        static final String DELIMITER = ",";
        private static final String LF = "\r\n";
        static final String NOVAL = "noval";

        public String serialize(final Snap snap) {
            if (snap == null) {
                throw new IllegalArgumentException("snap is null");
            }
            final Vector descriptors = new Vector();
            // Follows the rule that the timestamp should be the first element
            descriptors.addElement(TypedDataDescriptor.TIMESTAMP);
            final StringBuffer sb = new StringBuffer();
            sb.append(TypedDataDescriptor.TIMESTAMP);
            sb.append(DELIMITER);
            for (final Enumeration e = snap.allDescriptors(); e.hasMoreElements(); ) {
                final Object o = e.nextElement();
                if (! TypedDataDescriptor.TIMESTAMP.equals(o)) {
                    descriptors.addElement(o);
                    sb.append(o);
                    sb.append(DELIMITER);
                }
            }

            int length = sb.length();
            if (length > 0) {
                sb.deleteCharAt(length - 1);
                sb.append(LF);
                for (final Enumeration e = snap.assemblages(); e.hasMoreElements(); ) {
                    final Assemblage assemblage = (Assemblage) e.nextElement();

                    for (final Enumeration types = descriptors.elements();
                            types.hasMoreElements(); ) {
                        final String descriptor = (String) types.nextElement();
                        final TypedData type = assemblage.get(descriptor);

                        if (type != null) {
                            final Variant v = type.getValue();
                            final String value = v.asString();
                            sb.append(value);
                            sb.append(DELIMITER);
                        } else {
                            sb.append(NOVAL);
                            sb.append(DELIMITER);
                        }
                    }
                    sb.deleteCharAt(sb.length() - 1);
                    sb.append(LF);
                }
                sb.deleteCharAt(sb.length() - 2);

                return sb.toString();
            }
            return "";
        }
    }

    public void setSport(final Sport aid) {
        this.aid = aid;
    }

    public Sport getActivityIdentifier() {
        return this.aid;
    }

    final String encodePassword(final String sid, final String password) {
        try {
            final Digest digester = new SHA256Digest();
            final byte[] asArray = Strings.toUTF8ByteArray(password);
            digester.update(asArray, 0, asArray.length);
            final int length = digester.getDigestSize();
            final byte[] digest = new byte[length];
            digester.doFinal(digest, 0);
            final byte[] hex = Hex.encode(digest);
            final String hexString = new String(hex, USASCII_ENC);
            final String auth = sid + ":" + hexString;
            LOG.debug("pwd as auth: " + auth);
            final byte[] asBytes = auth.getBytes(USASCII_ENC);
            final byte[] base64 = Base64.encode(asBytes);
            final String base64String = new String(base64, USASCII_ENC);
            LOG.debug("auth as base64: " + base64String);
            return base64String;
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported encoding US-ASCII");
        }
    }

    public boolean isLifeAllowed() {
        return lifeAllowed;
    }
}
