package tests.BigNatInternal;

import javacard.framework.JCSystem;
import opencrypto.jcmathlib.BigNat;
import opencrypto.jcmathlib.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AddShiftTest {
    @Test
    public void add_thisLonger() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x01, 0x02};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x03, 0x04};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.add_shift(bn2, (short) 0, (short) 1);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {0x01, 0x04, 0x06};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals_original(bn3));
    }

    @Test
    public void add_thisLonger_overflow_otherHigherBytes() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x01, 0x02};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {(byte) 0xff, 0x04};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        short carry = bn1.add_shift(bn2, (short) 0, (short) 1);

        Assertions.assertEquals(0, carry);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {0x02, 0x00, 0x06};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals_original(bn3));
    }

    @Test
    public void add_thisLonger_overflow_otherHigherBytes2() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x01, 0x02};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {(byte) 0xff, (byte) 0xff};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        short carry = bn1.add_shift(bn2, (short) 0, (short) 1);

        Assertions.assertEquals(0, carry);

        bn1.resize((short) (bn1.length() + 1));
        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {0x00, 0x02, 0x01, 0x01};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals_original(bn3));
    }

    @Test
    public void add_thisLonger_overflow() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x08, (byte) 0xff};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {(byte) 0xff, (byte) 0x09};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.add_shift(bn2, (short) 0, (short) 1);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {0x02, 0x08, 0x08};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals_original(bn3));
    }

    @Test
    public void add_thisLonger_overflow2() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x08, (byte) 0xff};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {(byte) 0xff, (byte) 0xff};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.add_shift(bn2, (short) 0, (short) 1);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {0x02, 0x08, (byte) 0xfe};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals_original(bn3));
    }

    @Test
    public void add_thisLonger_highestByteOverflow() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {(byte) 0xff, 0x08, (byte) 0xff};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {(byte) 0xff, (byte) 0xff};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        byte carry = bn1.add_shift(bn2, (short) 0, (short) 1);

        Assertions.assertEquals((byte) 128, carry);

        // test no overflow to higher byte
        bn1.resize((short) (bn1.length() + 1));
        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {0x00, 0x00, 0x08, (byte) 0xfe};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals_original(bn3));
    }

    @Test
    public void add_sameLength() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x02};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x03, 0x04};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.add_shift(bn2, (short) 0, (short) 1);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {0x04, 0x06};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals_original(bn3));
    }

    @Test
    public void add_sameLength_overflow() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x02};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {(byte) 0xff, 0x04};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        byte carry = bn1.add_shift(bn2, (short) 0, (short) 1);

        Assertions.assertEquals((byte) 128, carry);

        // test no overflow to higher byte
        bn1.resize((short) (bn1.length() + 1));
        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {0x00, 0x00, 0x06};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals_original(bn3));
    }

    @Test
    public void add_otherLonger_overflow() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {(byte) 0xff, 0x04};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01, 0x01, 0x02};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        byte carry = bn1.add_shift(bn2, (short) 0, (short) 1);

        Assertions.assertEquals((byte) 128, carry);

        bn1.resize((short) (bn1.length() + 1));
        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {0x00, 0x00, 0x06};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals_original(bn3));
    }

    @Test
    public void add_otherLonger() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x04};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01, 0x01, 0x01, 0x02};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        byte carry = bn1.add_shift(bn2, (short) 0, (short) 1);

        Assertions.assertEquals(0, carry);

        bn1.resize((short) (bn1.length() + 3));
        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {0x00, 0x00, 0x00, 0x06};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals_original(bn3));
    }

    @Test
    public void add_thisBiggerMemory() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 5, memoryType, rm);
        BigNat bn2 = new BigNat((short) 2, memoryType, rm);

        byte[] data1 = {(byte) 0x00, (byte) 0xff, (byte) 0xff, (byte) 0xff};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {(byte) 0x1};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.add_shift(bn2, (short) 0, (short) 1);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {0x01, 0x00, 0x00, 0x00};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals(bn3));
    }

    @Test
    public void add_thisBiggerMemory2() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 5, memoryType, rm);
        BigNat bn2 = new BigNat((short) 2, memoryType, rm);

        byte[] data1 = {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {(byte) 0x1};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.add_shift(bn2, (short) 0, (short) 1);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals(bn3));
    }

    @Test
    public void add_thisBiggerMemory3() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 5, memoryType, rm);
        BigNat bn2 = new BigNat((short) 2, memoryType, rm);

        byte[] data1 = {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x1, 0x1, 0x1};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.add_shift(bn2, (short) 0, (short) 1);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {0x00, 0x00, 0x00, 0x01, 0x01, 0x00};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals(bn3));
    }

    // tests for shift
    @Test
    public void add_shift_thisLonger() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x01, 0x02};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x03, 0x04};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.add_shift(bn2, (short) 1, (short) 1);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {0x04, 0x05, 0x02};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals_original(bn3));
    }

    @Test
    public void add_shift_thisLonger2() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x01, 0x02};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x03, 0x04};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.add_shift(bn2, (short) 2, (short) 1);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {0x05, 0x01, 0x02};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals_original(bn3));
    }

    @Test
    public void add_shift_thisLonger3() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x01, 0x02};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x03, 0x04};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.add_shift(bn2, (short) 3, (short) 1);

        bn1.resize((short) (bn1.length() + 1));
        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {0x00, 0x01, 0x01, 0x02};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals_original(bn3));
    }

    @Test
    public void add_shift_thisLonger_overflow() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x08, (byte) 0xff};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {(byte) 0xff, (byte) 0x09};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        short carry = bn1.add_shift(bn2, (short) 1, (short) 1);

        Assertions.assertEquals((byte) 128, carry);

        bn1.resize((short) (bn1.length() + 1));
        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {0x00, 0x00, 0x11, (byte) 0xff};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals_original(bn3));
    }

    // multiplication
    @Test
    public void add_multiplication_thisLonger() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x01, 0x02};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x03, 0x04};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.add_shift(bn2, (short) 0, (short) 2);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {0x01, 0x07, 0x0A};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals_original(bn3));
    }

    @Test
    public void add_multiplication_thisLonger_overflow() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x08, (byte) 0xff};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {(byte) 0xff, (byte) 0x09};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        short carry = bn1.add_shift(bn2, (short) 0, (short) 2);

        Assertions.assertEquals(0, carry);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {0x03, 0x07, (byte) 0x11};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals_original(bn3));
    }
}
