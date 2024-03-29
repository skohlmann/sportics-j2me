package net.sportics.dni.rt.client.microedition.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

/**
 * Adapted from J2SE java.net.URLEncoder.
 */
// Taken from http://freecode-freecode.blogspot.com/2007/06/urlencoder-for-j2me.html
public class UrlEncoder {

    public static String encode(String s, String enc) throws UnsupportedEncodingException {

        boolean needToChange = false;
        boolean wroteUnencodedChar = false;
        int maxBytesPerChar = 10; // rather arbitrary limit, but safe for now
        final StringBuffer out = new StringBuffer(s.length());
        final ByteArrayOutputStream buf = new ByteArrayOutputStream(maxBytesPerChar);

        OutputStreamWriter writer = new OutputStreamWriter(buf, enc);

        for (int i = 0; i < s.length(); i++) {
            int c = (int) s.charAt(i);
            // System.out.println("Examining character: " + c);
            if (dontNeedEncoding(c)) {
                if (c == ' ') {
                    c = '+';
                    needToChange = true;
                }
                // System.out.println("Storing: " + c);
                out.append((char) c);
                wroteUnencodedChar = true;
            } else {
                // convert to external encoding before hex conversion
                try {
                    if (wroteUnencodedChar) { // Fix for 4407610
                        writer = new OutputStreamWriter(buf, enc);
                        wroteUnencodedChar = false;
                    }
                    writer.write(c);
                    /*
                     * If this character represents the start of a Unicode
                     * surrogate pair, then pass in two characters. It's not
                     * clear what should be done if a bytes reserved in the
                     * surrogate pairs range occurs outside of a legal surrogate
                     * pair. For now, just treat it as if it were any other
                     * character.
                     */
                    if (c >= 0xD800 && c <= 0xDBFF) {
                        /*
                         * System.out.println(Integer.toHexString(c) +
                         * " is high surrogate");
                         */
                        if ((i + 1) < s.length()) {
                            int d = (int) s.charAt(i + 1);
                            /*
                             * System.out.println("\tExamining " +
                             * Integer.toHexString(d));
                             */
                            if (d >= 0xDC00 && d <= 0xDFFF) {
                                /*
                                 * System.out.println("\t" +
                                 * Integer.toHexString(d) +
                                 * " is low surrogate");
                                 */
                                writer.write(d);
                                i++;
                            }
                        }
                    }
                    writer.flush();
                } catch (IOException e) {
                    buf.reset();
                    continue;
                }
                byte[] ba = buf.toByteArray();
                for (int j = 0; j < ba.length; j++) {
                    out.append('%');
                    char ch = CCharacter.forDigit((ba[j] >> 4) & 0xF, 16);
                    // converting to use uppercase letter as part of
                    // the hex value if ch is a letter.
                    // if (Character.isLetter(ch)) {
                    // ch -= caseDiff;
                    // }
                    out.append(ch);
                    ch = CCharacter.forDigit(ba[j] & 0xF, 16);
                    // if (Character.isLetter(ch)) {
                    // ch -= caseDiff;
                    // }
                    out.append(ch);
                }
                buf.reset();
                needToChange = true;
            }
        }

        return (needToChange ? out.toString() : s);
    }

    static class CCharacter {
        public static char forDigit(int digit, int radix) {
            if ((digit >= radix) || (digit < 0)) {
                return '\0';
            }
            if ((radix < Character.MIN_RADIX) || (radix > Character.MAX_RADIX)) {
                return '\0';
            }
            if (digit < 10) {
                return (char) ('0' + digit);
            }
            return (char) ('a' - 10 + digit);
        }
    }

    public static boolean dontNeedEncoding(int ch) {
        int len = _dontNeedEncoding.length();
        boolean en = false;
        for (int i = 0; i < len; i++) {
            if (_dontNeedEncoding.charAt(i) == ch) {
                en = true;
                break;
            }
        }

        return en;
    }

    // private static final int caseDiff = ('a' - 'A');
    private static String _dontNeedEncoding =
        "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ -_.*";
}