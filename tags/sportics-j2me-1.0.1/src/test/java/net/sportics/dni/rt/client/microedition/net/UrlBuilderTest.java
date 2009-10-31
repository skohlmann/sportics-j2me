/* Copyright (c) 2008-2009 Sascha Kohlmann - all rights reserved */
package net.sportics.dni.rt.client.microedition.net;

import net.sportics.dni.rt.client.microedition.net.UrlBuilder;
import junit.framework.TestCase;
import junit.framework.Assert;

public class UrlBuilderTest extends TestCase {

    public void testCreation() {
        final UrlBuilder builder = UrlBuilder.newInstance("http://www.sportics.net/");
        Assert.assertNotNull(builder);
    }

    public void testCreationWithDifferentInstances() {
        final UrlBuilder builder1 = UrlBuilder.newInstance("http://www.sportics.net/");
        final UrlBuilder builder2 = UrlBuilder.newInstance("http://www.sportics.net/");
        Assert.assertNotSame(builder1, builder2);
    }

    public void testSingleParameter() {
        final UrlBuilder builder = UrlBuilder.newInstance("http://www.sportics.net/");
        builder.addParameter("key", "value");
        final String url = builder.build();
        Assert.assertEquals("http://www.sportics.net/?key=value", url);
    }

    public void testTwoParameters() {
        final UrlBuilder builder = UrlBuilder.newInstance("http://www.sportics.net/");
        builder.addParameter("key", "value");
        builder.addParameter("Johanna", "Charlotte");
        final String url = builder.build();
        Assert.assertEquals("http://www.sportics.net/?key=value&Johanna=Charlotte", url);
    }

    public void testEscapedParameter() {
        final UrlBuilder builder = UrlBuilder.newInstance("http://www.sportics.net/");
        builder.addParameter("k&y", "va ue");
        final String url = builder.build();
        Assert.assertEquals("http://www.sportics.net/?k%26y=va+ue", url);
    }
}
