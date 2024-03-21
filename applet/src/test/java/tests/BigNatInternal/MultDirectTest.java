package tests.BigNatInternal;

import javacard.framework.JCSystem;
import opencrypto.jcmathlib.BigNat;
import opencrypto.jcmathlib.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MultDirectTest {
    @Test
    public void mult_oneByteOne() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x02};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.ctMultDirect(bn2);

        byte[] expectedResult = {0x01, 0x02};
        byte[] actualResult = new byte[2];
        bn1.copyToByteArray(actualResult, (short) 0);
        Assertions.assertArrayEquals(expectedResult, actualResult);
    }

    @Test
    public void mult_oneByteFive() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x02};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x05};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.ctMultDirect(bn2);

        byte[] expectedResult = {0x05, 0x0A};
        byte[] actualResult = new byte[2];
        bn1.copyToByteArray(actualResult, (short) 0);
        Assertions.assertArrayEquals(expectedResult, actualResult);
    }

    @Test
    public void mult_oneByteFF() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 3, memoryType, rm);
        BigNat bn2 = new BigNat((short) 3, memoryType, rm);

        byte[] data1 = {0x02};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {(byte) 0xff};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.ctMultDirect(bn2);

        byte[] expectedResult = {0x01, (byte) 0xfe};
        byte[] actualResult = new byte[2];
        bn1.copyToByteArray(actualResult, (short) 0);
        Assertions.assertArrayEquals(expectedResult, actualResult);
    }

    @Test
    public void mult_twoBytesFF() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 3, memoryType, rm);
        BigNat bn2 = new BigNat((short) 3, memoryType, rm);

        byte[] data1 = {(byte) 0xff};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {(byte) 0xff, (byte) 0xff};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.ctMultDirect(bn2);

        byte[] expectedResult = {(byte) 0xfe, (byte) 0xff, 0x01};
        byte[] actualResult = new byte[3];
        bn1.copyToByteArray(actualResult, (short) 0);
        Assertions.assertArrayEquals(expectedResult, actualResult);
    }

    @Test
    public void mult_thisLong() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {(byte) 0xff, (byte) 0xfe, (byte) 0xfd, (byte) 0xfc, (byte) 0xfb};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {(byte) 0xff, (byte) 0x56};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.ctMultDirect(bn2);

        byte[] expectedResult = {(byte) 0xFF, (byte) 0x54, (byte) 0xFE, (byte) 0xA8, (byte) 0x51, (byte) 0x01, (byte) 0x52};
        byte[] actualResult = new byte[7];
        bn1.copyToByteArray(actualResult, (short) 0);
        Assertions.assertArrayEquals(expectedResult, actualResult);
    }

    @Test
    public void mult_otherLong() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {(byte) 0xff, (byte) 0x56};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {(byte) 0xff, (byte) 0xfe, (byte) 0xfd, (byte) 0xfc, (byte) 0xfb};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.ctMultDirect(bn2);

        byte[] expectedResult = {(byte) 0xFF, (byte) 0x54, (byte) 0xFE, (byte) 0xA8, (byte) 0x51, (byte) 0x01, (byte) 0x52};
        byte[] actualResult = new byte[7];
        bn1.copyToByteArray(actualResult, (short) 0);
        Assertions.assertArrayEquals(expectedResult, actualResult);
    }
}
