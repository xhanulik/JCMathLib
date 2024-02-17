package tests.BigNatInternal;

import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import opencrypto.jcmathlib.BigNat;
import opencrypto.jcmathlib.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CopyTest {
    @Test
    public void copy_empty() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);

        bn1.copy(bn2);
        bn1.shrink_original();
        Assertions.assertTrue(bn1.equals_original(bn2));
    }

    @Test
    public void copy_thisLonger() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x0a, 0x0b, 0x0c, 0x0d, 0x0e};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);

        bn1.copy(bn2);
        bn1.shrink_original();
        Assertions.assertTrue(bn1.equals_original(bn2));
    }

    @Test
    public void copy_leadingZeroes_thisLonger() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);
        BigNat bn2 = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);

        byte[] data1 = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x00, 0x00, 0x00, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.copy(bn2);
        bn1.shrink_original();
        bn2.shrink_original();
        Assertions.assertTrue(bn1.equals_original(bn2));
    }

    @Test
    public void copy_leadingZeroes_sameLength() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);
        BigNat bn2 = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);

        byte[] data1 = {0x01, 0x02, 0x03, 0x04, 0x05};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x00, 0x00, 0x00, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.copy(bn2);
        bn1.shrink_original();
        bn2.shrink_original();
        Assertions.assertTrue(bn1.equals_original(bn2));
    }

    @Test
    public void copy_thisShorter() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);
        BigNat bn2 = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);

        byte[] data1 = {0x01, 0x02, 0x03, 0x04};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x0a, 0x0b, 0x0c, 0x0d, 0x0e};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);

        Assertions.assertThrows(ISOException.class, () -> bn1.copy(bn2));
    }
}
