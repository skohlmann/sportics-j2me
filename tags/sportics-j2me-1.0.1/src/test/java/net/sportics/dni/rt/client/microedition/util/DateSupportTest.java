package net.sportics.dni.rt.client.microedition.util;

import net.sportics.dni.rt.client.microedition.util.DateSupport;
import java.util.Calendar;

import junit.framework.Assert;
import junit.framework.TestCase;

public class DateSupportTest extends TestCase {

    public void testSimpleDateformat() {

        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2008);
        cal.set(Calendar.MONTH, 6);
        cal.set(Calendar.DAY_OF_MONTH, 25);
        cal.set(Calendar.HOUR_OF_DAY, 16);
        cal.set(Calendar.MINUTE, 37);
        cal.set(Calendar.SECOND, 5);
        cal.set(Calendar.MILLISECOND, 2);

        final String datetime = DateSupport.timeAsCalendar(cal);
        Assert.assertEquals("20080725163705002", datetime);
    }

    public void testAllLowDateformat() {

        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2008);
        cal.set(Calendar.MONTH, 6);
        cal.set(Calendar.DAY_OF_MONTH, 5);
        cal.set(Calendar.HOUR_OF_DAY, 6);
        cal.set(Calendar.MINUTE, 7);
        cal.set(Calendar.SECOND, 5);
        cal.set(Calendar.MILLISECOND, 21);

        final String datetime = DateSupport.timeAsCalendar(cal);
        Assert.assertEquals("20080705060705021", datetime);
    }


    public void testAllFullDateformat() {

        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2008);
        cal.set(Calendar.MONTH, 9);
        cal.set(Calendar.DAY_OF_MONTH, 15);
        cal.set(Calendar.HOUR_OF_DAY, 16);
        cal.set(Calendar.MINUTE, 37);
        cal.set(Calendar.SECOND, 55);
        cal.set(Calendar.MILLISECOND, 121);

        final String datetime = DateSupport.timeAsCalendar(cal);
        Assert.assertEquals("20081015163755121", datetime);
    }

    public void testSecondsToTimeWithZeroSeconds() {
        final String time = DateSupport.secondsToTime(0l);
        Assert.assertEquals("00:00:00", time);
    }

    public void testSecondsToTimeWith10Seconds() {
        final String time = DateSupport.secondsToTime(10l);
        Assert.assertEquals("00:00:10", time);
    }

    public void testSecondsToTimeWith9Seconds() {
        final String time = DateSupport.secondsToTime(9l);
        Assert.assertEquals("00:00:09", time);
    }

    public void testSecondsToTimeWith9Minutes() {
        final String time = DateSupport.secondsToTime(9 * 60l);
        Assert.assertEquals("00:09:00", time);
    }

    public void testSecondsToTimeWith10Minutes() {
        final String time = DateSupport.secondsToTime(10 * 60l);
        Assert.assertEquals("00:10:00", time);
    }

    public void testSecondsToTimeWith9Hours() {
        final String time = DateSupport.secondsToTime(9 * 60 * 60l);
        Assert.assertEquals("09:00:00", time);
    }

    public void testSecondsToTimeWith10Hours() {
        final String time = DateSupport.secondsToTime(10 * 60 * 60l);
        Assert.assertEquals("10:00:00", time);
    }
}

