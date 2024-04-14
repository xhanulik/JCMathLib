package tests.BigNat;

import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.security.CryptoException;
import opencrypto.jcmathlib.BigNat;
import opencrypto.jcmathlib.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SqTest {

    /* ctSq tests */
    @Test
    public void zero() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x00, 0x00};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        bn1.ctSq();

        byte[] expectedResult = {0x00, 0x00};
        byte[] actualResult = new byte[2];
        bn1.copyToByteArray(actualResult, (short) 0);
        Assertions.assertArrayEquals(expectedResult, actualResult);
    }

    @Test
    public void one() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x00, 0x01};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        bn1.ctSq();

        byte[] expectedResult = {0x01};
        byte[] actualResult = new byte[1];
        bn1.copyToByteArray(actualResult, (short) 0);
        Assertions.assertArrayEquals(expectedResult, actualResult);
    }

    @Test
    public void two() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x00, 0x02};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        bn1.ctSq();

        byte[] expectedResult = {0x04};
        byte[] actualResult = new byte[1];
        bn1.copyToByteArray(actualResult, (short) 0);
        Assertions.assertArrayEquals(expectedResult, actualResult);
    }

    @Test
    public void bigNumber() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x12, (byte) 0x8D, 0x4C, (byte) 0xA6};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        bn1.ctSq();

        byte[] expectedResult = {0x01, 0x58, 0x2C, (byte) 0xC4, (byte) 0xDD, (byte) 0xCE, (byte) 0xFB, (byte) 0xA4};
        byte[] actualResult = new byte[8];
        bn1.copyToByteArray(actualResult, (short) 0);
        Assertions.assertArrayEquals(expectedResult, actualResult);
    }

    /* ctHWSq tests */
    @Test
    public void notRunning() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x00, 0x00};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        Assertions.assertThrows(CryptoException.class, bn1::ctHWSq);
    }
}
