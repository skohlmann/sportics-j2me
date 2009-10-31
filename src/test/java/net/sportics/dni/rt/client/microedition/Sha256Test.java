/** (c) 2009 Sascha Kohlmann. All rights reserved. */
package net.sportics.dni.rt.client.microedition;

import junit.framework.TestCase;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.util.Strings;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

/**
 *
 * @author Sascha Kohlmann
 */
public class Sha256Test extends TestCase {

    public void testCoding() throws Exception {
        final Digest digester = new SHA256Digest();
        final String test = "test";
        final byte[] asArray = Strings.toUTF8ByteArray(test);
        digester.update(asArray, 0, asArray.length);
        final int length = digester.getDigestSize();
        final byte[] digest = new byte[length];
        digester.doFinal(digest, 0);
        final byte[] hex = Hex.encode(digest);
        final String hexString = new String(hex, "US-ASCII");
        System.out.println("Hex: " + hexString);
        final byte[] base64 = Base64.encode(hex);
        final String base64String = new String(base64, "US-ASCII");
        System.out.println("Base64: " + base64String);
    }
}
