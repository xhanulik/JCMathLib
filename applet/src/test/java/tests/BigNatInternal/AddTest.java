package tests.BigNatInternal;

import javacard.framework.JCSystem;
import opencrypto.jcmathlib.BigNat;
import opencrypto.jcmathlib.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AddTest {
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
        bn1.add(bn2);

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
        bn1.add(bn2);

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
        bn1.add(bn2);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {0x02, 0x01, 0x01};
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
        bn1.add(bn2);

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
        bn1.add(bn2);

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
        bn1.add(bn2);

        // test no overflow to higher byte
        bn1.resize_original((short) (bn1.length() + 1));
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
        bn1.add(bn2);

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
        bn1.add(bn2);

        // test no overflow to higher byte
        bn1.resize_original((short) (bn1.length() + 1));
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
        bn1.add(bn2);

        bn1.resize_original((short) (bn1.length() + 1));
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
        bn1.add(bn2);

        bn1.resize_original((short) (bn1.length() + 3));
        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {0x00, 0x00, 0x00, 0x06};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals_original(bn3));
    }
}
