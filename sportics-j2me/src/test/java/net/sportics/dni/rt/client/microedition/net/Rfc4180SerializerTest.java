/* Copyright (c) 2008-2009 Sascha Kohlmann - all rights reserved */
package net.sportics.dni.rt.client.microedition.net;

import java.io.BufferedReader;
import java.io.StringReader;

import net.sportics.dni.rt.client.microedition.TypedData;
import net.sportics.dni.rt.client.microedition.TypedDataDescriptor;
import net.sportics.dni.rt.client.microedition.Variant;
import net.sportics.dni.rt.client.microedition.accu.Assemblage;
import net.sportics.dni.rt.client.microedition.net.SrtsApi.Snap;
import net.sportics.dni.rt.client.microedition.net.SrtsApi.Rfc4180Serializer;
import junit.framework.Assert;
import junit.framework.TestCase;

public class Rfc4180SerializerTest extends TestCase {

    public void testCreation() {
        new Rfc4180Serializer();
    }

    public void testDifferentAdd() throws Exception {
        final Variant v1a = new Variant(1d);
        final TypedData t1a = new TypedData(TypedDataDescriptor.HEART_RATE, v1a);
        final Variant v2a = new Variant(2);
        final TypedData t2a = new TypedData(TypedDataDescriptor.ALTITUDE, v2a);
        final Variant v3a = new Variant(123);
        final TypedData t3a = new TypedData(TypedDataDescriptor.TIMESTAMP, v3a);


        final Assemblage a1 = new Assemblage();
        a1.put(t1a);
        a1.put(t2a);
        a1.put(t3a);

        final Variant v1b = new Variant(25.7d);
        final TypedData t1b = new TypedData(TypedDataDescriptor.HEART_RATE, v1b);
        final Variant v2b = new Variant(4);
        final TypedData t2b = new TypedData(TypedDataDescriptor.DISTANCE, v2b);
        final Variant v3b = new Variant(456);
        final TypedData t3b = new TypedData(TypedDataDescriptor.TIMESTAMP, v3b);

        final Assemblage a2 = new Assemblage();
        a2.put(t1b);
        a2.put(t2b);
        a2.put(t3b);

        final Snap snap = new Snap();
        snap.addAssemblage(a1);
        snap.addAssemblage(a2);

        final Rfc4180Serializer serializer = new Rfc4180Serializer();
        final String serialized = serializer.serialize(snap);
        final StringReader sr = new StringReader(serialized);
        final BufferedReader br = new BufferedReader(sr);

        final String line1 = br.readLine();
        System.out.println("line1: " + line1);
        Assert.assertTrue(line1.indexOf(TypedDataDescriptor.TIMESTAMP) == 0);
        Assert.assertTrue(line1.indexOf(TypedDataDescriptor.ALTITUDE) != -1);
        Assert.assertTrue(line1.indexOf(TypedDataDescriptor.HEART_RATE) != -1);
        Assert.assertTrue(line1.indexOf(TypedDataDescriptor.DISTANCE) != -1);
        Assert.assertTrue(line1.charAt(line1.length() -1) != Rfc4180Serializer.DELIMITER.charAt(0));

        final String line2 = br.readLine();
        System.out.println("line2: " + line2);
        Assert.assertTrue(line2.indexOf("123") == 0);
        Assert.assertTrue(line2.indexOf("1.0") != -1);
        Assert.assertTrue(line2.indexOf("2") != -1);
        Assert.assertTrue(line2.indexOf(Rfc4180Serializer.NOVAL) != -1);
        Assert.assertTrue(line2.charAt(line2.length() -1) != ',');

        final String line3 = br.readLine();
        System.out.println("line3: " + line3);
        Assert.assertTrue(line3.indexOf("456") == 0);
        Assert.assertTrue(line3.indexOf("25.7") != -1);
        Assert.assertTrue(line3.indexOf("4") != -1);
        Assert.assertTrue(line3.indexOf(Rfc4180Serializer.NOVAL) != -1);
        Assert.assertTrue(line3.charAt(line3.length() -1) != ',');
    }
}
