package tests.BigNatInternal;

import javacard.framework.JCSystem;
import opencrypto.jcmathlib.BigNat;
import opencrypto.jcmathlib.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IsLesserTest {
    private static void printBits(short value) {
        System.out.println("Short value: " + value);

        for (int i = 15; i >= 0; i--) {
            int bit = (value >> i) & 1;
            System.out.print(bit);
        }

        System.out.println();
    }

    static short ct_negative(short a) {
        return (short) ((a & (1 << 15)) >> 15);
    }
    @Test
    public void test() {
        printBits((short) ct_negative((short) -2));
    }
    // no shifts, other.size > this.size, same memory
    @Test
    public void isLesser_otherLonger_true() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x02, 0x03, 0x04};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01, 0x02, 0x03, 0x04, 0x05};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        Assertions.assertTrue(bn1.isLesser(bn2));
    }

    @Test
    public void isLesser_otherLonger_false() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x02, 0x03, 0x04};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x00, 0x01, 0x02, 0x03, 0x04};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        Assertions.assertFalse(bn1.isLesser(bn2));
    }

    @Test
    public void isLesser_otherLonger_false2() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x00, 0x02, 0x03, 0x04};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x00, 0x00, 0x02, 0x03, 0x04};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        Assertions.assertFalse(bn1.isLesser(bn2));
    }

    // no shifts, other.size > this.size, different memory
    @Test
    public void isLesser_otherLonger_differentMemory_true() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 5, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x02, 0x03, 0x04};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01, 0x02, 0x03, 0x04, 0x05};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        Assertions.assertTrue(bn1.isLesser(bn2));
    }

    @Test
    public void isLesser_otherLonger_differentMemory_true2() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 5, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x02, 0x03, 0x04};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        Assertions.assertTrue(bn1.isLesser(bn2));
    }

    // no shifts, same length, same memory
    @Test
    public void isLesser_sameLength_true() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x02, 0x03, 0x03};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01, 0x02, 0x03, 0x04};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        Assertions.assertTrue(bn1.isLesser(bn2));
    }

    @Test
    public void isLesser_sameLength_true2() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x00, 0x02, 0x03, 0x04};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01, 0x02, 0x03, 0x04};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        Assertions.assertTrue(bn1.isLesser(bn2));
    }

    @Test
    public void isLesser_sameLength_false() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x02, 0x03, 0x04};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01, 0x02, 0x03, 0x03};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        Assertions.assertFalse(bn1.isLesser(bn2));
    }

    @Test
    public void isLesser_sameLength_false2() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x02, 0x03, 0x04};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x00, 0x02, 0x03, 0x04};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        Assertions.assertFalse(bn1.isLesser(bn2));
    }

    // no shifts, same length, different memory
    @Test
    public void isLesser_sameLength_differentMemory_true() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 3, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x02, 0x03, 0x03};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01, 0x02, 0x03, 0x04};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        Assertions.assertTrue(bn1.isLesser(bn2));
    }

    // no shifts, other.size < this.size, same memory
    @Test
    public void isLesser_thisLonger_true() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x00, 0x00, 0x00, 0x02, 0x03, 0x04};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01, 0x02, 0x03, 0x04};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        Assertions.assertTrue(bn1.isLesser(bn2));
    }

    @Test
    public void isLesser_thisLonger_true2() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x00, 0x00, 0x01, 0x02, 0x03, 0x03};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01, 0x02, 0x03, 0x04};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        Assertions.assertTrue(bn1.isLesser(bn2));
    }

    @Test
    public void isLesser_thisLonger_false() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x00, 0x00, 0x01, 0x02, 0x03, 0x04};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01, 0x02, 0x03, 0x04};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        Assertions.assertFalse(bn1.isLesser(bn2));
    }

    @Test
    public void isLesser_thisLonger_false2() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x00, 0x00, 0x01, 0x02, 0x03, 0x05};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01, 0x02, 0x03, 0x04};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        Assertions.assertFalse(bn1.isLesser(bn2));
    }

    // no shifts, other.size < this.size, different memory
    @Test
    public void isLesser_thisLonger_differentMemory_true() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 3, memoryType, rm);

        byte[] data1 = {0x00, 0x00, 0x00, 0x02, 0x03, 0x04};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01, 0x02, 0x03, 0x04};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        Assertions.assertTrue(bn1.isLesser(bn2));
    }

    // problem
    @Test
    public void isLesser_thisLonger_differentMemory_false() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 3, memoryType, rm);

        byte[] data1 = {0x00, 0x00, 0x01, 0x02, 0x03, 0x04};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01, 0x02, 0x03, 0x04};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        Assertions.assertFalse(bn1.isLesser(bn2));
    }

    // shifts
    @Test
    public void isLesser_sameLength_shift1_true() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        Assertions.assertTrue(bn1.isLesser(bn2, (short) 1, (short) 0));
    }

    @Test
    public void isLesser_thisLonger_shift3_true() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01, 0x02, 0x03, 0x04};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        Assertions.assertTrue(bn1.isLesser(bn2, (short) 3, (short) 0));
    }

    @Test
    public void isLesser_thisLonger_shift1_false() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x02, 0x03, 0x04, 0x05, 0x00};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01, 0x02, 0x03, 0x04, 0x05};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        Assertions.assertFalse(bn1.isLesser(bn2, (short) 1, (short) 0));
    }

    // start
    @Test
    public void isLesser_thisLonger_start1_false() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x02, 0x03, 0x04, 0x05};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01, 0x02, 0x03, 0x04, 0x05};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        Assertions.assertFalse(bn1.isLesser(bn2, (short) 0, (short) 1));
    }

    @Test
    public void isLesser_thisLonger_start1_true() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x00, 0x03, 0x04, 0x05};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x00, 0x02, 0x03, 0x04, 0x05};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        Assertions.assertTrue(bn1.isLesser(bn2, (short) 0, (short) 1));
    }

    @Test
    public void isLesser_thisLonger_start5_false() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x00, 0x03, 0x04, 0x05};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 =  {0x01, 0x00, 0x03, 0x04, 0x05};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        Assertions.assertFalse(bn1.isLesser(bn2, (short) 0, (short) 1));
    }

    // start & shift
    @Test
    public void isLesser_lesser_shift2Start1() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x02, 0x03, 0x04, 0x05, 0x00};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01, 0x02, 0x03, 0x04, 0x05};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        Assertions.assertTrue(bn1.isLesser(bn2, (short) 2, (short) 1));
    }

    @Test
    public void isLesser_same_shift2Start5() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x02, 0x03, 0x04, 0x05, 0x00};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01, 0x02, 0x03, 0x04, 0x05};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        Assertions.assertFalse(bn1.isLesser(bn2, (short) 2, (short) 5));
    }

    @Test
    public void isLesser_lesser_shift2Start4() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x02, 0x03, 0x04, 0x05, 0x00};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01, 0x02, 0x03, 0x04, 0x05};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        Assertions.assertTrue(bn1.isLesser(bn2, (short) 2, (short) 4));
    }
}
