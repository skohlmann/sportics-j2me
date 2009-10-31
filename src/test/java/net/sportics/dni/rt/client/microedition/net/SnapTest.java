/* Copyright (c) 2008-2009 Sascha Kohlmann - all rights reserved */
package net.sportics.dni.rt.client.microedition.net;

import java.util.Enumeration;

import net.sportics.dni.rt.client.microedition.TypedData;
import net.sportics.dni.rt.client.microedition.TypedDataDescriptor;
import net.sportics.dni.rt.client.microedition.Variant;
import net.sportics.dni.rt.client.microedition.accu.Assemblage;
import net.sportics.dni.rt.client.microedition.net.SrtsApi.Snap;
import junit.framework.Assert;
import junit.framework.TestCase;

public class SnapTest extends TestCase {

    public void testCreation() {
        new Snap();
    }

    public void testSingleAdd() {
        final Variant v1 = new Variant(1d);
        final TypedData t1 = new TypedData(TypedDataDescriptor.HEART_RATE, v1);
        final Variant v2 = new Variant(2);
        final TypedData t2 = new TypedData(TypedDataDescriptor.ALTITUDE, v2);

        final Assemblage a = new Assemblage();
        a.put(t1);
        a.put(t2);

        final Snap snap = new Snap();
        snap.addAssemblage(a);

        int descCount = 0;
        for (final Enumeration e = snap.allDescriptors(); e.hasMoreElements(); ) {
            e.nextElement();
            descCount++;
        }
        Assert.assertEquals(2, descCount);

        int assemblageCount = 0;
        for (final Enumeration e = snap.assemblages(); e.hasMoreElements(); ) {
            e.nextElement();
            assemblageCount++;
        }
        Assert.assertEquals(1, assemblageCount);
    }

    public void testDifferentAdd() {
        final Variant v1a = new Variant(1d);
        final TypedData t1a = new TypedData(TypedDataDescriptor.HEART_RATE, v1a);
        final Variant v2a = new Variant(2);
        final TypedData t2a = new TypedData(TypedDataDescriptor.ALTITUDE, v2a);

        final Assemblage a1 = new Assemblage();
        a1.put(t1a);
        a1.put(t2a);

        final Variant v1b = new Variant(1d);
        final TypedData t1b = new TypedData(TypedDataDescriptor.HEART_RATE, v1b);
        final Variant v2b = new Variant(2);
        final TypedData t2b = new TypedData(TypedDataDescriptor.DISTANCE, v2b);

        final Assemblage a2 = new Assemblage();
        a2.put(t1b);
        a2.put(t2b);

        final Snap snap = new Snap();
        snap.addAssemblage(a1);
        snap.addAssemblage(a2);

        int descCount = 0;
        for (final Enumeration e = snap.allDescriptors(); e.hasMoreElements(); ) {
            e.nextElement();
            descCount++;
        }
        Assert.assertEquals(3, descCount);

        int assemblageCount = 0;
        for (final Enumeration e = snap.assemblages(); e.hasMoreElements(); ) {
            e.nextElement();
            assemblageCount++;
        }
        Assert.assertEquals(2, assemblageCount);
    }
}
