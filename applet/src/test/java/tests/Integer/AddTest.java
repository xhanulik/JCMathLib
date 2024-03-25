package tests.Integer;

import opencrypto.jcmathlib.Integer;
import opencrypto.jcmathlib.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tests.Util;

public class AddTest {
    @Test
    public void add_test() {
        ResourceManager rm = new ResourceManager((short) 256);
        Integer int1 = new Integer((short) 5, rm);
        Integer int2 = new Integer((short) 5, rm);

        // 0170272B8D + 00774D48EA = 1E7747477
        byte[] data1 = {0x01, 0x70, 0x27, 0x2B, (byte) 0x8D};
        int1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x00, 0x77, 0x4D, 0x48, (byte) 0xEA};
        int2.fromByteArray(data2, (short) 0, (short) data2.length);
        int1.subtract(int2);

        byte[] expectedResult = {0x01, (byte) 0xE7, 0x74, 0x74, 0x77};
        byte[] actualResult = new byte[5];
        int1.toByteArray(actualResult, (short) 0);
        Assertions.assertArrayEquals(expectedResult, actualResult);
    }
}
