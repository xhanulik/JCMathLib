package tests.BigNat;

import javacard.framework.JCSystem;
import opencrypto.jcmathlib.BigNat;
import opencrypto.jcmathlib.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GcdTest {
    @Test
    public void gcd_10_2() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {10};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {2};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);

        bn1.gcd(bn2);

        byte[] expectedResult = {2};
        byte[] actualResult = new byte[1];
        bn1.copyToByteArray(actualResult, (short) 0);
        Assertions.assertArrayEquals(expectedResult, actualResult);
    }
}
