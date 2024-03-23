package tests.BigNatInternal;

import javacard.framework.JCSystem;
import opencrypto.jcmathlib.BigNat;
import opencrypto.jcmathlib.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ShiftRightTest {
    @Test
    public void shiftRight_0() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x05, 0x08};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        bn1.ctShiftRight((short) 0);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {0x05, 0x08};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals(bn3));
    }

    @Test
    public void shiftRight_1() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x05, 0x08};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        bn1.ctShiftRight((short) 1);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {0x02, (byte) 0x84};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals(bn3));
    }

    @Test
    public void shiftRight_7() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x40, 0x00};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        bn1.ctShiftRight((short) 7);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {0x00, (byte) 0x80};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals(bn3));
    }

    @Test
    public void shiftRight_carry1() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x00, 0x02};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        bn1.ctShiftRight((short) 1, (short) 1);

        byte[] expectedResult = {0x01, 0x01};
        byte[] actualResult = new byte[2];
        bn1.copyToByteArray(actualResult, (short) 0);
        Assertions.assertArrayEquals(expectedResult, actualResult);
    }

    @Test
    public void shiftRight_carry128() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x00, 0x02};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        bn1.ctShiftRight((short) 1, (short) 128);

        byte[] expectedResult = {(byte) 128, 0x01};
        byte[] actualResult = new byte[2];
        bn1.copyToByteArray(actualResult, (short) 0);
        Assertions.assertArrayEquals(expectedResult, actualResult);
    }
}
