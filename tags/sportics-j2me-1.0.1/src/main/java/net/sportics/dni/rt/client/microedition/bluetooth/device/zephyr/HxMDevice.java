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
package net.sportics.dni.rt.client.microedition.bluetooth.device.zephyr;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import net.sportics.dni.rt.client.microedition.TypedData;
import net.sportics.dni.rt.client.microedition.TypedDataConsumer;
import net.sportics.dni.rt.client.microedition.TypedDataDescriptor;
import net.sportics.dni.rt.client.microedition.Variant;
import net.sportics.dni.rt.client.microedition.bluetooth.AbstractBluetoothDevice;
import net.sportics.dni.rt.client.microedition.bluetooth.BluetoothManager;
import net.sportics.dni.rt.client.microedition.device.DeviceException;
import net.sportics.dni.rt.client.microedition.util.LogManager;

public class HxMDevice extends AbstractBluetoothDevice implements Runnable {

    private static final LogManager LOG = LogManager.getInstance("HxMDevice");
    static {
        LOG.debug("#class: " + HxMDevice.class.getName());
    }

    private static final int RFCOMM_UUID = 0x0003;

    private static final String TYPES[] = {TypedDataDescriptor.HEART_RATE,
                                           TypedDataDescriptor.POWER_LEVEL,
                                           TypedDataDescriptor.STRIDES};

    private static final int CHECKSUM_POLYNOMIAL = 0x8C;
    /** HXM message id */
    private static final byte MSG_ID = 0x26;
    /** End of text */
    private static final byte ETX = 0x03;
    /** Start of text */
    private static final byte STX = 0x02;
    /** HXM packet size */
    private static final byte DLC = 0x37;
    /** Message protocol bytes (e.g. ETX, STX and so on) */
    private static final byte PROTOCOL_OVERHEAD = 0x5;

    private static final int PACKET_OFFSET_STX = 0;
    private static final int PACKET_OFFSET_MSG_ID = 1;
    private static final int PACKET_OFFSET_DLC = 2;
    private static final int PACKET_OFFSET_BATTERY = 11;
    private static final int PACKET_OFFSET_HEARTRATE = 12;
    private static final int PACKET_OFFSET_HEARTBEAT = 13;
    private static final int PACKET_OFFSET_DISTANCE_START = 50;
    private static final int PACKET_OFFSET_SPEED_START = 52;
    private static final int PACKET_OFFSET_STRIDES = 54;
    private static final int PACKET_OFFSET_CADENCE_START = 56;
    private static final int PACKET_OFFSET_CRC = 58;
    private static final int PACKET_OFFSET_ETX = 59;

    private static final int ADD_STRIDES = 256;

    private String connectionUrl = null;
    private StreamConnection connection = null;
    private int state = -1;
    private volatile boolean run;
    private Thread pull = null;

    private int allStrides = 0;
    private int lastStrides = 0;

    public void prepare() {
        this.state = STATE_DO_PREPARATION;
        fireLifecycleEvent(this.state);
        final String realName = this.getRealDeviceName();
        if (realName == null) {
            throw new IllegalStateException("no real devicename");
        }
        final BluetoothManager manager = BluetoothManager.getInstance();
        try {
            final Vector services =
                manager.discoverServices(new UUID[] {new UUID(RFCOMM_UUID)}, realName);

            LOG.debug("service count: " + services.size());
            for (final Enumeration e = services.elements(); e.hasMoreElements(); ) {
                final ServiceRecord sr = (ServiceRecord) e.nextElement();
                final boolean master = bluetoothMasterFlagSwitch();
                this.connectionUrl =
                    sr.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, master);
                break;
            }
            if (this.connectionUrl != null) {
                LOG.info("connection URL: " + this.connectionUrl);
                this.state = STATE_PREPARED;
                fireLifecycleEvent(this.state);
            } else {
                final DeviceException ne = new DeviceException(2);
                fireErrorEvent(STATE_DO_PREPARATION, ne);
            }
        } catch (final IOException e) {
            final DeviceException ne = new DeviceException(1, e.getMessage());
            fireErrorEvent(STATE_DO_PREPARATION, ne);
        }
    }

    public void restart() {
    }

    public void start() {
        this.state = STATE_DO_START;
        fireLifecycleEvent(this.state);

        final String url = getServiceUrl();
        if (url != null) {
            try {
                LOG.debug("connect to URL: " + url);
                this.connection = (StreamConnection) Connector.open(url);
                final String rdn = getRealDeviceName();
                this.pull = new Thread(this, rdn);
                this.pull.start();
                LOG.debug("Thread started: " + this.pull);
            } catch (final IOException e) {
                final DeviceException ne = new DeviceException(1, e.getMessage());
                fireErrorEvent(STATE_DO_START, ne);
                return;
            }
        } else {
            LOG.config("unable to start connection to " + getDeviceName()
                       + " - no connection URL");
            final DeviceException ne = new DeviceException(1, "No connection URL");
            fireErrorEvent(STATE_DO_START, ne);
            return;
        }

        this.state = STATE_STARTED;
        fireLifecycleEvent(this.state);
    }

    public void stop() {
        this.run = false;
        if (this.pull != null && this.pull.isAlive()) {
            try {
                this.pull.join();
            } catch (final InterruptedException e) {
                // ignoring
            }
            this.pull = null;
        }
        if (this.connection != null) {
            try {
                this.connection.close();
            } catch (final IOException e) {
                // ignoring
            }
            this.connection = null;
        }
    }

    public void run() {
        this.run = true;
        try {
            final InputStream in = this.connection.openInputStream();
            LOG.debug("InputStream: " + in + " - starting loop");
            while(this.run) {
                final byte[] packet = readFromStreamLoop(in);
                if (vaildPacket(packet)) {
                    final Hashtable parameter = parse(packet);
                    pushToDeviceTypedDataConsumer(parameter);
                } else {
                    LOG.info("no valid packet");
                }
            }
            in.close();
        } catch (final IOException e) {
            final DeviceException ne = new DeviceException(1, e.getMessage());
            fireErrorEvent(STATE_STARTED, ne);
        }
    }

    final byte[] readFromStreamLoop(final InputStream in) throws IOException {
        if (in == null) {
            throw new IllegalArgumentException("in is null");
        }
        byte[] buffer = new byte[DLC + PROTOCOL_OVERHEAD];
        int index = 0;
        int maxIndex = DLC + PROTOCOL_OVERHEAD - 1;

        int read = 0;
        while((read = in.read(buffer, index, 1)) != -1) {
            if (LOG.isLoggable(LogManager.TRACE)) {
                LOG.trace("readLoop - idx: " + index
                           + " val: " + readUnsignedByte(buffer[index]));
            }
            if (index == PACKET_OFFSET_STX) {
                if (buffer[PACKET_OFFSET_STX] != STX) {
                    throw new IOException("No STX (0x02) at index " + index + " of the stream");
                }
            } else if (index == PACKET_OFFSET_DLC) {
                final int dlc = readUnsignedByte(buffer[PACKET_OFFSET_DLC]);
                if (dlc != DLC) {
                    maxIndex = dlc + PROTOCOL_OVERHEAD - 1;
                    final byte[] newBuffer = new byte[dlc + PROTOCOL_OVERHEAD];
                    System.arraycopy(buffer, 0, newBuffer, 0, index + 1);
                    buffer = newBuffer;
                }
            } else if (index == maxIndex) {
                if (buffer[maxIndex] != ETX) {
                    throw new IOException("No ETX (0x03) at index " + index + " of the stream");
                }
                break;
            }
            index++;
        }
        if (read == -1) {
            LOG.warn("Lost connection");
            throw new IOException("Lost connection");
        }
        logData(buffer);
 
        return buffer;
    }

    private void logData(byte[] buffer) {
        if (LOG.isLoggable(LogManager.DEBUG)) {
            final StringBuffer sb = new StringBuffer("readed: ");
            sb.append(buffer.length);
            sb.append(" bytes - ");
            for (int i = 0; i < buffer.length; i++) {
                final int unsigned = readUnsignedByte(buffer[i]);
                sb.append("0x");
                sb.append(Integer.toHexString(unsigned));
                sb.append(",");
            }
            final String msg = sb.toString();
            LOG.debug(msg);
        }
    }

    final void pushToDeviceTypedDataConsumer(final Hashtable parameter) {

        if (parameter == null) {
            return;
        }

        final TypedData[] td = toArray(parameter);
        for (final Enumeration e = getDeviceDataCollectorEnumeration(); e.hasMoreElements(); ) {
            final TypedDataConsumer accu = (TypedDataConsumer) e.nextElement();
            accu.newData(this, td);
        }
    }

    final TypedData[] toArray(final Hashtable parameter) {
        final int size = parameter.size();
        int i = 0;
        final TypedData[] td = new TypedData[size];
        for (final Enumeration e = parameter.keys(); e.hasMoreElements(); ) {
            final String typeDescriptor = (String) e.nextElement();
            final Variant v = (Variant) parameter.get(typeDescriptor);
            td[i] = new TypedData(typeDescriptor, v);
            i++;
        }
        return td;
    }

    /**
     * The returning Hashtable contains the {@link TypedDataDescriptor} as key and a instance of
     * {@link Variant} as value. 
     * @param packet the raw bytes to parse
     * @return the relevant parameter from the raw byte packet. Never {@code null}
     */
    final Hashtable parse(final byte[] packet) {
        final Hashtable parameter = new Hashtable();
        if (packet.length >= DLC + PROTOCOL_OVERHEAD) {
            final String battery = parseString(packet, PACKET_OFFSET_BATTERY); 
            final String heartBeat = parseString(packet, PACKET_OFFSET_HEARTBEAT);

            final int strides = readUnsignedByte(packet[PACKET_OFFSET_STRIDES]);
            final int newStrides = handleStrides(strides);

            final int d = mergeUnsigned(packet[PACKET_OFFSET_DISTANCE_START],
                                        packet[PACKET_OFFSET_DISTANCE_START + 1]);
            final String distance = String.valueOf(Math.abs(((double) d / 16d)));

            final int s = mergeUnsigned(packet[PACKET_OFFSET_SPEED_START],
                                        packet[PACKET_OFFSET_SPEED_START + 1]);
            final String speed = String.valueOf(Math.abs(((double) s / 256d)));

            final int c = mergeUnsigned(packet[PACKET_OFFSET_CADENCE_START],
                                        packet[PACKET_OFFSET_CADENCE_START + 1]);
            final double cadence = Math.abs((double) c / 16d);

            final int hr = readUnsignedByte(packet[PACKET_OFFSET_HEARTRATE]);

            if (LOG.isLoggable(LogManager.DEBUG)) {
                final StringBuffer sb = new StringBuffer("values: battery=");
                sb.append(battery);
                sb.append(" heartRate=");
                sb.append(hr);
                sb.append(" heartBeat=");
                sb.append(heartBeat);
                sb.append(" strides=");
                sb.append(strides);
                sb.append(" totalstrides=");
                sb.append(newStrides);
                sb.append(" distance=");
                sb.append(distance);
                sb.append(" speed=");
                sb.append(speed);
                sb.append(" cadence=");
                sb.append(cadence);

                final String msg = sb.toString();
                LOG.debug(msg);
            }

            final Variant heartRateAsVariant = new Variant(hr);
            parameter.put(TypedDataDescriptor.HEART_RATE, heartRateAsVariant);
            final Variant batteryAsVariant = new Variant(battery);
            parameter.put(TypedDataDescriptor.POWER_LEVEL, batteryAsVariant);
            final Variant stridesAsVariant = new Variant(newStrides);
            parameter.put(TypedDataDescriptor.STRIDES, stridesAsVariant);
            final Variant cadenceAsVariant = new Variant(cadence);
            parameter.put(TypedDataDescriptor.CADENCE, cadenceAsVariant);
        }
        return parameter;
    }

    final int handleStrides(final int strides) {
        if (strides < this.lastStrides) {
            this.allStrides += (ADD_STRIDES - this.lastStrides);
            this.allStrides += strides;
        } else if (strides != this.lastStrides) {
            this.allStrides += (strides - this.lastStrides);
        }
        this.lastStrides = strides;

        return this.allStrides;
    }

    /**
     * 
     * @param packet of bytes 
     * @param index of the byte to parse in the byte array 
     * @return a String of the indexed byte as hexadecimal value
     */
    final static String parseString(final byte[] packet, final int index){ 
        final String hex = byteToHex(packet[index]);
        final short value = Short.parseShort(hex, 16);
        return String.valueOf(value);
    }
    
    /**
     * 
     * @param packet of bytes 
     * @param index of the byte to parse in the byte array 
     * @return a String of the indexed byte as hexadecimal value
     */
    final static short parseShort(final byte[] packet, final int index){
        final String hex = byteToHex(packet[index]);
        return Short.parseShort(hex, 16);
    }
    
    /**
     * Merge two bytes into a signed 2's complement integer
     *  
     * @param low byte is LSB
     * @param high byte is the MSB
     * @return a signed {@code int} value 
     */
    final static int merge(final byte low, final byte high){
        int b = 0;
        b += (high << 8) + low;
        if ((high & 0x80) != 0) {
            b = -(0xffffffff - b);
        }
        return b;
    }
    
    /**
     * Merge two bytes into a unsigned integer
     *  
     * @param low byte is LSB
     * @param high byte is the MSB
     * @return an unsigned {@code int} value 
     */
    final static int mergeUnsigned(final byte low, final byte high){
        int b = 0;
        b += (high << 8) + low;
        if ((high & 0x80) != 0) {
            b = -(0xffffffff - b);
        }
        return b;
    }
    
    /**
     * Convert a byte to a hex string.
     * 
     * @param data the byte to convert
     * @return String the converted byte
     */
    final static String byteToHex(final byte data) {
        final StringBuffer buf = new StringBuffer();
        buf.append(toHexChar((data >>> 4) & 0x0F));
        buf.append(toHexChar(data & 0x0F));
        return buf.toString();
    }

    /**
     * Convert a byte array to a hex string.
     * 
     * @param data the byte[] to convert
     * @return String the converted byte[]
     */
    final String bytesToHex(final byte[] data) {
        final StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            buf.append(byteToHex(data[i]));
        }
        return buf.toString();
    }

    /**
     * Convert an {@code int} to a hex char.
     * 
     * @param i is the {@code int} to convert
     * @return char the converted char
     */
    final static char toHexChar(final int i) {
        if ((0 <= i) && (i <= 9)) {
            return (char) ('0' + i);
        } else {
            return (char) ('a' + (i - 10));
        }
    }

    final boolean vaildPacket(final byte[] packet) {
        
        if( packet == null) return false;
        if( packet.length != DLC + PROTOCOL_OVERHEAD ) {
            LOG.debug("Validate: packet with illegal length");
            return false;
        }
        if( packet[PACKET_OFFSET_STX]  != STX) {
            LOG.debug("Validate: STX error");
            return false;
        }
        if( packet[PACKET_OFFSET_MSG_ID] != MSG_ID) {
            LOG.debug("Validate: MSG ID error");
            return false; 
        }
        if( packet[PACKET_OFFSET_DLC] != DLC) {
            LOG.debug("Validate: DLC error");
            return false; 
        }
        if( packet[PACKET_OFFSET_ETX] != ETX ) {
            LOG.debug("Validate: ETX error");
            return false; 
        }

        if(checkCrc(packet)) {
            LOG.debug("Validate: CRC error");
            return false; 
        }

        LOG.debug("Validate: is valid");
        return true;
    }

    final static boolean checkCrc(final byte[] packet){
        
        int crc = 0;
        for (int i = 2; i < PACKET_OFFSET_CRC; i++) {
            crc = checksumPushByte(crc, readUnsignedByte(packet[i]));
        }
        if(crc == readUnsignedByte(packet[PACKET_OFFSET_CRC]))
            return true;
        
       return false;
    } 

    final static int checksumPushByte(final int currentChecksum, final int newByte) {

        int checksun = currentChecksum ^ newByte;

        for (int bit = 0; bit < 8; bit++){
            if ((currentChecksum & 1) == 1) {
                checksun = ((checksun >> 1) ^ CHECKSUM_POLYNOMIAL);
            } else { 
                checksun = (checksun >> 1);
            }
        }
          
        return currentChecksum;
    }

    final static int readUnsignedByte(final byte b)  {
        return (int) (b & 0xff);
    }

    public String getDeviceName() {
        return "Zephyr-HxM";
    }

    public String getServiceUrl() {
        return this.connectionUrl;
    }

    final boolean bluetoothMasterFlagSwitch() {
        final String platform = System.getProperty("microedition.platform");
        LOG.debug("microedition.platform: " + platform);
        if (platform != null) {
            final String lower = platform.toLowerCase();
            if (lower.startsWith("nokia")) {
                return false;
            }
        }
        return true;
    }

    public String[] supportedTypedDataList() {
        return TYPES;
    }

    public int currentState() {
        return this.state;
    }
}
