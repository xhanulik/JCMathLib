package tests.Integer;

import javacard.framework.ISO7816;
import javacard.framework.JCSystem;
import opencrypto.jcmathlib.BigNat;
import opencrypto.jcmathlib.Integer;
import opencrypto.jcmathlib.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tests.Util;

public class SubtractTest {

    @Test
    public void subtract_thisBiggerMemory2() {
        ResourceManager rm = new ResourceManager((short) 256);
        Integer int1 = new Integer((short) 5, rm);
        Integer int2 = new Integer((short) 5, rm);

        byte[] data1 = Util.intToBytes(0x007DD54139);
        int1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = Util.intToBytes(0x0073B24C4D);
        int2.fromByteArray(data2, (short) 0, (short) data2.length);
        int1.subtract(int2);

        byte[] expectedResult = Util.intToBytes(0x0A22F4EC);
        byte[] actualResult = new byte[5];
        int1.toByteArray(actualResult, (short) 0);
        Assertions.assertArrayEquals(expectedResult, actualResult);
    }
}
