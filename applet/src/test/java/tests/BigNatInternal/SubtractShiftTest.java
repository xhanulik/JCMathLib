package tests.BigNatInternal;

import javacard.framework.JCSystem;
import opencrypto.jcmathlib.BigNat;
import opencrypto.jcmathlib.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SubtractShiftTest {
    @Test
    public void subtract_otherLonger() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x03, 0x02};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x05, 0x01, 0x01};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.subtract_refactored(bn2, (short) 0, (short) 1);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {0x02, 0x01};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals(bn3));
    }

    @Test
    public void subtract_otherLonger_underflow() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x03, 0x02};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x05, 0x01, 0x03};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.subtract_refactored(bn2, (short) 0, (short) 1);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {0x01, (byte) 0xff};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals(bn3));
    }

    @Test
    public void subtract_sameLength() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x03, 0x02};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01, 0x01};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.subtract_refactored(bn2, (short) 0, (short) 1);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {0x02, 0x01};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals(bn3));
    }

    @Test
    public void subtract_sameLength_underflow() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x03, 0x02};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01, 0x03};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.subtract_refactored(bn2, (short) 0, (short) 1);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {0x01, (byte) 0xff};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals(bn3));
    }

    @Test
    public void subtract_sameLength_underflow2() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x03, 0x02};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01, 0x02, 0x03};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.subtract_refactored(bn2, (short) 0, (short) 1);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {0x00, (byte) 0xff};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals(bn3));
    }

    @Test
    public void subtract_sameLength_underflow3() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x00, 0x02};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01, 0x01};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.subtract_refactored(bn2, (short) 0, (short) 1);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {(byte) 0xff, 0x01};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals(bn3));
    }

    @Test
    public void subtract_sameLength_underflow4() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x00, 0x01};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01, 0x02};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.subtract_refactored(bn2, (short) 0, (short) 1);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {(byte) 0xfe, (byte) 0xff};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals(bn3));
    }

    @Test
    public void subtract_thisBigger() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x03, 0x03, 0x02};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01, 0x01};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.subtract_refactored(bn2, (short) 0, (short) 1);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {0x03, 0x02, 0x01};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals(bn3));
    }

    @Test
    public void subtract_thisBigger_underflow() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x03, 0x03, 0x02};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x03, 0x03};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.subtract_refactored(bn2, (short) 0, (short) 1);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {0x02, (byte) 0xff, (byte) 0xff};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals(bn3));
    }

    @Test
    public void subtract_thisBiggerMemory() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 5, memoryType, rm);
        BigNat bn2 = new BigNat((short) 2, memoryType, rm);

        byte[] data1 = {0x01, 0x01, 0x01, 0x01, 0x01, 0x01};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.subtract_refactored(bn2, (short) 0, (short) 1);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {0x01, 0x01, 0x01, 0x01, 0x01, 0x00};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals(bn3));
    }

    @Test
    public void subtract_thisBiggerMemory2() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 5, memoryType, rm);
        BigNat bn2 = new BigNat((short) 2, memoryType, rm);

        byte[] data1 = {0x01, 0x01, 0x01, 0x01, 0x01, 0x01};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {(byte) 0xff};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.subtract_refactored(bn2, (short) 0, (short) 1);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {0x01, 0x01, 0x01, 0x01, 0x00, 0x02};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals(bn3));
    }

    @Test
    public void subtract_thisBiggerMemory3() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 5, memoryType, rm);
        BigNat bn2 = new BigNat((short) 2, memoryType, rm);

        byte[] data1 = {0x01, 0x01, 0x01, 0x01, 0x01, 0x01};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01, 0x01};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.subtract_refactored(bn2, (short) 0, (short) 1);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {0x01, 0x01, 0x01, 0x01, 0x00, 0x00};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals(bn3));
    }

    @Test
    public void subtract_noShift_multiplier() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x05, 0x08};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01, 0x02};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.subtract_refactored(bn2, (short) 0, (short) 2);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {(byte) 0x03, (byte) 0x04};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals(bn3));
    }

    @Test
    public void subtract_shift2_noMultiplier() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x05, 0x08};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01, 0x02};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.subtract_refactored(bn2, (short) 2, (short) 0);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {0x05, 0x08};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals(bn3));
    }

    @Test
    public void subtract_shift1_noMultiplier() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x05, 0x08};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01, 0x02};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.subtract_refactored(bn2, (short) 1, (short) 1);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {0x03, 0x08};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals(bn3));
    }

    @Test
    public void subtract_bigShift_noOverflow() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 5, memoryType, rm);
        BigNat bn2 = new BigNat((short) 5, memoryType, rm);

        byte[] data1 = {0x01, 0x01, 0x01, 0x01, 0x01, 0x01};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.subtract_original(bn2, (short) 7, (short) 1);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {0x01, 0x01, 0x01, 0x01, 0x01, 0x01};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals(bn3));
    }
}
