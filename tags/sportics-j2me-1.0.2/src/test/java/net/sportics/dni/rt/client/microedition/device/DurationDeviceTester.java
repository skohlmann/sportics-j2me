/* Copyright (c) 2008-2009 Sascha Kohlmann - all rights reserved */
package net.sportics.dni.rt.client.microedition.device;

import net.sportics.dni.rt.client.microedition.device.DurationDevice;
import net.sportics.dni.rt.client.microedition.device.Device;
import net.sportics.dni.rt.client.microedition.TypedData;
import net.sportics.dni.rt.client.microedition.TypedDataConsumer;

public class DurationDeviceTester {

    public static final void main(final String[] args) throws Exception {
        final Device d = new DurationDevice();
        d.registerTypedDataConsumer(new TypedDataConsumer() {
            public void newData(final Object source, final TypedData type) {
                newData(source, new TypedData[] {type});
            }
            public void newData(final Object source, final TypedData[] types) {
                for (int i = 0; i < types.length; i++) {
                    System.out.println("Source: " + source.getClass() + " - Value: " + types[i]);
                }
            }
        });
        d.start();
        synchronized(DurationDeviceTester.class) {
            DurationDeviceTester.class.wait(5000, 0);
        }
        d.restart();
        synchronized(DurationDeviceTester.class) {
            DurationDeviceTester.class.wait(5000, 0);
        }
        d.stop();
    }
}
