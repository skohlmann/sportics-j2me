/* (c) 2009 Sascha Kohlmann. All rights reserved. */
package net.sportics.dni.rt.client.microedition.util;

import junit.framework.TestCase;

/**
 *
 * @author Sascha Kohlmann
 */
public class StringUtilTest extends TestCase {

    public void test_decimalShorter_with_no_decimal_part() {
        final String decimal = StringUtil.decimalShorter("123", 2);
        assertEquals(decimal, "123");
    }

    public void test_decimalShorter_with_not_too_long_decimal_part() {
        final String decimal = StringUtil.decimalShorter("123.1", 2);
        assertEquals(decimal, "123.1");
    }

    public void test_decimalShorter_with_exact_long_decimal_part() {
        final String decimal = StringUtil.decimalShorter("123.12", 2);
        assertEquals(decimal, "123.12");
    }

    public void test_decimalShorter_with_too_long_decimal_part() {
        final String decimal = StringUtil.decimalShorter("123.123", 2);
        assertEquals(decimal, "123.12");
    }
}
