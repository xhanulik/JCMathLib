package tests.BigNatInternal;

import javacard.framework.JCSystem;
import opencrypto.jcmathlib.BigNat;
import opencrypto.jcmathlib.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RemainderDivideTest {
    @Test
    public void add_thisLonger() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);
        BigNat bn3 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x09};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x02};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);

        bn1.remainderDivide(bn2, bn3);

        byte[] expectedResult = new byte[10];
        expectedResult[9] = 0x04;
        byte[] expectedRemainder = {0x01};
        byte[] actualResult = new byte[10];
        byte[] actualRemainder = new byte[1];
        bn1.copyToByteArray(actualRemainder, (short) 0);
        Assertions.assertArrayEquals(actualRemainder, expectedRemainder);
        bn3.copyToByteArray(actualResult, (short) 0);
        Assertions.assertArrayEquals(actualResult, expectedResult);
    }
}
