package tests.BigNatInternal;

import javacard.framework.JCSystem;
import opencrypto.jcmathlib.BigNat;
import opencrypto.jcmathlib.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ResizeTest {
    @Test
    public void resize_smallerLength() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);

        byte[] data = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        bn.fromByteArray(data, (short) 0, (short) data.length);
        bn.resize((short) (data.length - 3));
        Assertions.assertEquals(data.length - 3, bn.length());
    }

    @Test
    public void resize_biggerLength() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);

        byte[] data = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        bn.fromByteArray(data, (short) 0, (short) data.length);
        bn.resize((short) (data.length * 2));
        Assertions.assertEquals(data.length * 2, bn.length());
    }

    @Test
    public void resize_smallerLength_truncateNumber() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);
        BigNat bn_result = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);

        byte[] data = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06};
        bn.fromByteArray(data, (short) 0, (short) data.length);
        bn.resize((short) (data.length - 3));
        Assertions.assertEquals(data.length - 3, bn.length());

        bn_result.fromByteArray(new byte[]{0x04, 0x05, 0x06}, (short) 0, (short) 3);
        Assertions.assertTrue(bn.equals_original(bn_result));
    }

    @Test
    public void resize_smallerLength_enlargeNumber() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);
        BigNat bn_result = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);

        byte[] data = {0x01, 0x02, 0x03};
        bn.fromByteArray(data, (short) 0, (short) data.length);
        bn.resize((short) (data.length + 3));
        Assertions.assertEquals(data.length + 3, bn.length());

        bn_result.fromByteArray(new byte[]{0x00, 0x00, 0x00, 0x01, 0x02, 0x03}, (short) 0, (short) 6);
        Assertions.assertTrue(bn.equals_original(bn_result));
    }

    @Test
    public void resize_sameSize() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);
        BigNat bn_result = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);

        byte[] data = {0x01, 0x02, 0x03};
        bn.fromByteArray(data, (short) 0, (short) data.length);
        bn.resize((short) data.length);
        Assertions.assertEquals(data.length, bn.length());

        bn_result.fromByteArray(new byte[]{0x01, 0x02, 0x03}, (short) 0, (short) 3);
        Assertions.assertTrue(bn.equals_original(bn_result));
    }

    @Test
    public void resize_empty() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);
        BigNat bn_result = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);

        byte[] data = {0x01, 0x02, 0x03};
        bn.fromByteArray(data, (short) 0, (short) 3);
        bn.resize((short) 0);
        Assertions.assertEquals(0, bn.length());

        bn_result.fromByteArray(new byte[]{}, (short) 0, (short) 0);
        Assertions.assertTrue(bn.equals_original(bn_result));
    }
}
